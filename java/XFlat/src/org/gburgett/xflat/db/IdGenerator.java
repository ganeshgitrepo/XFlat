/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gburgett.xflat.db;

import org.jdom2.Element;

/**
 * The base class for an ID generator.  ID generators are selected on a per-table
 * basis by the {@link Database} based on an ID generation strategy.  The strategy
 * selects the first IdGenerator in the strategy that supports the ID property's
 * type.  The strategy can be overridden by a custom {@link TableConfig}.
 * 
 * All IdGenerator implementations MUST have a no-args constructor so that they
 * can be constructed by reflection when necessary.
 * @author gordon
 */
public abstract class IdGenerator {
    
    /**
     * Indicates whether the given ID property type is supported by this
     * ID generator.  If false, then IDs generated by this generator
     * cannot be assigned to the ID property of the POJO, and thus the POJO
     * cannot be stored in this Table.
     * @param idType The type of the ID property on the POJO.
     * @return true if the type is supported by the ID generator.
     */
    public abstract boolean supports(Class<?> idType);
    
    /**
     * Generates a new ID, converting it to the given id type.
     * @param idType The type to convert the ID to, must be one of the supported
     * types as given by {@link #supports(java.lang.Class) }
     * @return The converted ID value.
     */
    public abstract Object generateNewId(Class<?> idType);
    
    /**
     * A convenience for converting IDs to their string representations.
     * Since all engines take string IDs (which can be stored in the XML DOM)
     * this is how we persist the ID.
     * @param id The ID to convert, cannot be null.
     * @return The string representation of the given ID.
     */
    public abstract String idToString(Object id);
    
    /**
     * A convenience for converting IDs from their string representations.
     * Since all engines take string IDs (which can be stored in the XML DOM)
     * this is how we get a persisted ID.
     * @param id The string value of the ID.
     * @param idType The type to convert the ID to, must be one of the supported
     * types as given by {@link #supports(java.lang.Class) }.
     * @return The converted ID.
     */
    public abstract Object stringToId(String id, Class<?> idType);
    
    /**
     * Saves this ID generator's state to a JDOM Element so it can save when
     * its associated Engine spins down.
     * When saving state the IdGenerator is guaranteed to have no invocations on
     * {@link #generateNewId}.
     * Stateless ID generators such as the {@link UuidIdGenerator} do not have to
     * override this.
     * @param state The table metadata element to which the IdGenerator should save
     * any state info.
     */
    public void saveState(Element state){
    }
    
    /**
     * Loads this ID generator's state from a JDOM element so it can resume when
     * its associated engine spins up.
     * When loading state the IdGenerator is guaranteed to have no invocations on
     * {@link #generateNewId}.
     * Stateless ID generators such as the {@link UuidIdGenerator} do not have to
     * override this.
     * @param state The table metadata element from which the IdGenerator should load
     * any state info.
     */
    public void loadState(Element state){
    }
    
}
