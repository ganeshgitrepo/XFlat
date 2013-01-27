/* 
*	Copyright 2013 Gordon Burgett and individual contributors
*
*	Licensed under the Apache License, Version 2.0 (the "License");
*	you may not use this file except in compliance with the License.
*	You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*	Unless required by applicable law or agreed to in writing, software
*	distributed under the License is distributed on an "AS IS" BASIS,
*	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*	See the License for the specific language governing permissions and
*	limitations under the License.
*/
package org.xflatdb.xflat.db;

import java.util.concurrent.atomic.AtomicInteger;
import org.jdom2.Element;

/**
 *
 * @author gordon
 */
public class IntegerIdGenerator extends IdGenerator {

    private AtomicInteger lastId = new AtomicInteger(0);
    
    @Override
    public boolean supports(Class<?> idType) {
        return Integer.class.equals(idType) ||
                Float.class.equals(idType) ||
                Double.class.equals(idType) ||
                Long.class.equals(idType) ||
                String.class.equals(idType);
    }

    @Override
    public Object generateNewId(Class<?> idType) {
        
        int id = lastId.incrementAndGet();
        
        if(Integer.class.equals(idType)){
            return new Integer(id);
        }
        if(Float.class.equals(idType)){
            return new Float(id);
        }
        if(Double.class.equals(idType)){
            return new Double(id);
        }
        if(Long.class.equals(idType)){
            return new Long(id);
        }
        if(String.class.equals(idType)){
            return Integer.toString(id);
        }
        
        throw new UnsupportedOperationException("Unsupported ID type " + idType);
    }

    @Override
    public String idToString(Object id) {
        if(id == null){
            return "0";
        }
        
        Class<?> idType = id.getClass();
        if(String.class.equals(idType)){
            return (String)id;
        }
        if(Integer.class.equals(idType)){
            return ((Integer)id).toString();
        }
        if(Float.class.equals(idType)){
            return Integer.toString(((Float)id).intValue());
        }
        if(Double.class.equals(idType)){
            return Integer.toString(((Double)id).intValue());
        }
        if(Long.class.equals(idType)){
            return Integer.toString(((Long)id).intValue());
        }
        
        throw new UnsupportedOperationException("Unsupported ID type " + idType);
    }

    @Override
    public Object stringToId(String id, Class<?> idType) {
        
        if(String.class.equals(idType)){
            return id;
        }
        
        Integer i;
        if(id == null){
            i = new Integer(0);
        }
        else{
            i = Integer.parseInt(id);
        }
        
        if(Integer.class.equals(idType)){
            return Integer.parseInt(id);
        }
        if(Float.class.equals(idType)){
            return i.floatValue();
        }
        if(Double.class.equals(idType)){
            return i.doubleValue();
        }
        if(Long.class.equals(idType)){
            return i.longValue();
        }
        
        throw new UnsupportedOperationException("Unsupported ID type " + idType);
    }
    
    
    @Override
    public void saveState(Element state){
        state.setAttribute("maxId", Integer.toString(this.lastId.get()), XFlatDatabase.xFlatNs);
    }
    
    @Override
    public void loadState(Element state){
        String maxId = state.getAttributeValue("maxId", XFlatDatabase.xFlatNs);
        this.lastId.set(Integer.parseInt(maxId));
    }
    
}
