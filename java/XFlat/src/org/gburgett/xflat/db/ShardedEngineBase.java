/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gburgett.xflat.db;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.gburgett.xflat.Cursor;
import org.gburgett.xflat.EngineStateException;
import org.gburgett.xflat.ShardsetConfig;
import org.gburgett.xflat.TableConfig;
import org.gburgett.xflat.XflatException;
import org.gburgett.xflat.convert.ConversionException;
import org.gburgett.xflat.query.Interval;
import org.jdom2.Element;

/**
 *
 * @author Gordon
 */
public abstract class ShardedEngineBase<T> extends EngineBase {
    protected ConcurrentMap<Interval<T>, TableMetadata> openShards = new ConcurrentHashMap<>();
    protected ConcurrentMap<Interval<T>, File> knownShards = new ConcurrentHashMap<>();
    
    
    //the engines that are spinning down while this engine spins down
    private Map<Interval<T>, EngineBase> spinningDownEngines = new HashMap<>();
    
    private final Object spinDownSyncRoot = new Object();
    
    protected File directory;
        
    protected ShardsetConfig<T> config;
    
    private TableMetadataFactory metadataFactory;
    /**
     * Gets a metadata factory which can be used to generate {@link TableMetadata} objects.
     * This allows the engine to spawn additional engines as necessary.
     * The metadata factory is set up to read and write metadata from the same
     * {@link File} given to the {@link EngineFactory#newEngine(java.io.File, java.lang.String, org.gburgett.xflat.TableConfig) } method,
     * so if the engine uses this it must also use that file as a directory.
     * @return 
     */
    protected TableMetadataFactory getMetadataFactory(){
        return this.metadataFactory;
    }
    /** @see #getMetadataFactory() */
    protected void setMetadataFactory(TableMetadataFactory metadataFactory){
        this.metadataFactory = metadataFactory;
    }
    
    
    public ShardedEngineBase(File file, String tableName, ShardsetConfig<T> config){
        super(tableName);
        
        this.directory = file;
        this.config = config;
        
        if(file.exists() && ! file.isDirectory()){
            //TODO: automatically convert old data in this case.
            throw new UnsupportedOperationException("Cannot create sharded engine for existing non-sharded table");
        }
    }
    
    protected Interval<T> getRangeForRow(Element row){
        Object selected = config.getShardPropertySelector().evaluateFirst(row);
        return getInterval(selected);
    }
    
    protected Interval<T> getInterval(Object value){
        T converted;
        if(value == null || !this.config.getShardPropertyClass().isAssignableFrom(value.getClass())){
            try {
                converted = this.getConversionService().convert(value, this.config.getShardPropertyClass());
            } catch (ConversionException ex) {
                throw new XflatException("Data cannot be sharded: sharding expression " + config.getShardPropertySelector().getExpression() +
                        " selected non-convertible value " + value, ex);
            }
        }
        else{
            converted = (T)value;
        }
        
        Interval<T> ret;
        try{
            ret = this.config.getIntervalProvider().getInterval(converted);
        }catch(java.lang.NullPointerException ex){
            throw new XflatException("Data cannot be sharded: sharding expression " + config.getShardPropertySelector().getExpression() +
                    " selected null value which cannot be mapped to a range");
        }
        
        if(ret == null){
            throw new XflatException("Data cannot be sharded: sharding expression " + config.getShardPropertySelector().getExpression() +
                    " selected value " + converted + " which cannot be mapped to a range");
        }
        
        return ret;
    }
    
    private EngineBase getEngine(Interval<T> interval){
        
        TableMetadata metadata = openShards.get(interval);
        if(metadata == null){
            //definitely ensure we aren't spinning down before we start up a new engine
            synchronized(spinDownSyncRoot){
                
                EngineState state = getState();
                if(state == EngineState.SpunDown){
                    throw new XflatException("Engine has already spun down");
                }
                
                //build the new metadata element so we can use it to provide engines
                String name = this.config.getIntervalProvider().getName(interval);
                File file = new File(directory, name + ".xml");
                this.knownShards.put(interval, file);
                
                metadata = this.getMetadataFactory().makeTableMetadata(name, file);
                metadata.config = TableConfig.Default; //not even really used for our purposes
                
                TableMetadata weWereLate = openShards.putIfAbsent(interval, metadata);
                if(weWereLate != null){
                    //another thread put the new metadata already
                    metadata = weWereLate;
                }

                if(state == EngineState.SpinningDown){
                    EngineBase eng = spinningDownEngines.get(interval);
                    if(eng == null){
                        //we're requesting a new engine for some kind of read, get it and let the task spin it down.
                        eng = metadata.provideEngine();
                        spinningDownEngines.put(interval, eng);
                        return eng;
                    }
                }
            }
        }
        
        return metadata.provideEngine();
    }
    
    protected <U> U doWithEngine(Interval<T> range, EngineAction<U> action){
        
        EngineState state = getState();
        if(state == EngineState.Uninitialized || state == EngineState.SpunDown){
            throw new XflatException("Attempt to read or write to an engine in an uninitialized state");
        }
        
        try{
            return action.act(getEngine(range));
        }
        catch(EngineStateException ex){
            //try one more time with a potentially new engine, if we still fail then let it go
            return action.act(getEngine(range));
        }
    }
    
    protected void update(){
        
        
        Iterator<TableMetadata> it = openShards.values().iterator();
        while(it.hasNext()){
            TableMetadata table = it.next();
            if(table.canSpinDown()){
                //remove right now - if between the check and the remove we got some activity
                //then oh well, we can spin up a new instance.
                it.remove();
                
                table.spinDown();
                try {
                    this.getMetadataFactory().saveTableMetadata(table);
                } catch (IOException ex) {
                    //oh well
                    this.log.warn("Failure to save metadata for sharded table " + this.getTableName() + " shard " + table.getName(), ex);
                }
            }
        }
    }
    
    @Override
    protected boolean hasUncomittedData() {
        EngineState state = this.state.get();
        if(state == EngineState.SpinningDown){
            for(EngineBase e : this.spinningDownEngines.values()){
                if(e.hasUncomittedData()){
                    return true;
                }
            }
        }
        else if(state == EngineState.Running){
            for(TableMetadata table : this.openShards.values()){
                if(table.hasUncommittedData()){
                    return true;
                }
            }
        }
        return false;
    }
        
    @Override
    protected boolean spinUp() {
        if(!this.state.compareAndSet(EngineState.Uninitialized, EngineState.SpinningUp)){
            return false;
        }
        
        if(!directory.exists()){
            directory.mkdirs();
        }
        else{
            //need to scan the directory for existing known shards.
            for(File f : directory.listFiles()){
                if(!f.getName().endsWith(".xml")){
                    continue;
                }
                
                String shardName = f.getName().substring(0, f.getName().length() - 4);
                Interval<T> i = config.getIntervalProvider().getInterval(shardName);
                if(i != null){
                    knownShards.put(i, f);
                }
            }
        }
        
        //we'll spin up tables as we need them.
        this.getExecutorService().scheduleWithFixedDelay(new Runnable(){
            @Override
            public void run() {
                EngineState state = getState();
                if(state == EngineState.SpinningDown ||
                        state == EngineState.SpunDown){
                    throw new RuntimeException("task termination");
                }
                
                update();
            }
        }, 500, 500, TimeUnit.MILLISECONDS);
        
        
        this.state.compareAndSet(EngineState.SpinningUp, EngineState.SpunUp);
        return true;
    }

    @Override
    protected boolean beginOperations() {
        return this.state.compareAndSet(EngineState.SpunUp, EngineState.Running);
    }

    @Override
    protected boolean spinDown(final SpinDownEventHandler completionEventHandler) {
        if(!this.state.compareAndSet(EngineState.Running, EngineState.SpinningDown)){
            //we're in the wrong state.
            return false;
        }
        
        synchronized(spinDownSyncRoot){
            for(Map.Entry<Interval<T>, TableMetadata> m : this.openShards.entrySet()){
                EngineBase spinningDown = m.getValue().spinDown();
                this.spinningDownEngines.put(m.getKey(), spinningDown);
            }
        }
        
        Runnable spinDownMonitor = new Runnable(){
            @Override
            public void run() {
                if(getState() != EngineState.SpinningDown){
                    throw new RuntimeException("task complete");
                }
                
                synchronized(spinDownSyncRoot){
                    if(isSpunDown()){
                        if(state.compareAndSet(EngineState.SpinningDown, EngineState.SpunDown)){
                            completionEventHandler.spinDownComplete(new SpinDownEvent(ShardedEngineBase.this));
                        }
                        else{
                            //somehow we weren't in the spinning down state
                            forceSpinDown();
                        }
                        throw new RuntimeException("task complete");
                    }
                    
                    
                    Iterator<EngineBase> it = spinningDownEngines.values().iterator();
                    while(it.hasNext()){
                        EngineBase spinningDown = it.next();
                        EngineState state = spinningDown.getState();
                        if(state == EngineState.SpunDown || state == EngineState.Uninitialized){
                            it.remove();
                        }
                        else if(state == EngineState.Running){
                            spinningDown.spinDown(null);
                        }
                    }
                    //give it a few more ms just in case
                }
            }
        };
        
        this.getExecutorService().scheduleWithFixedDelay(spinDownMonitor, 5, 10, TimeUnit.MILLISECONDS);
        
        
        return true;
    }
    
    /**
     * Invoked in a synchronized context to see if the sharded engine is 
     * fully spun down.  Default implementation checks whether the spinning
     * down engines have all spun down.
     * @return 
     */
    protected boolean isSpunDown(){
        return spinningDownEngines.isEmpty();
    }

    @Override
    protected boolean forceSpinDown() {
        this.state.set(EngineState.SpunDown);
        
        synchronized(spinDownSyncRoot){
            for(Map.Entry<Interval<T>, TableMetadata> m : this.openShards.entrySet()){
                EngineBase spinningDown = m.getValue().spinDown();
                this.spinningDownEngines.put(m.getKey(), spinningDown);
            }
            
            for(EngineBase spinningDown : spinningDownEngines.values()){
                spinningDown.forceSpinDown();
            }
        }
        
        return true;
    }
}
