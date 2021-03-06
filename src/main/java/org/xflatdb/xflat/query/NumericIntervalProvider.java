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
package org.xflatdb.xflat.query;

import java.util.Comparator;
import org.xflatdb.xflat.util.ComparableComparator;

/**
 * A class containing a number of factory methods for getting {@link IntervalProvider}
 * objects for numbers.
 * 
 * Each IntervalProvider provides fixed-width intervals calculated based on the
 * width and base parameters to each function.  The base is the offset on the number line
 * from which to start, and the width is the size of each interval on the number line.
 * <p/>
 * Example: <br/>
 * for base = 25 and width = 100, intervals would be the following: <br/>
 * ... [-175, -75) [-75, 25) [25, 125) [125, 225) ...
 * @author Gordon
 */
public class NumericIntervalProvider {
    
    private NumericIntervalProvider(){
        
    }
    
    /**
     * Creates a IntervalProvider for {@link Integer} based intervals.
     * @param base The base from which intervals should be calculated.  Usually 0.
     * @param width The width of one interval.
     * @return A IntervalProvider providing intervals based on these settings.
     */
    public static IntervalProvider<Integer> forInteger(final int base, final int width){
        return new IntervalProvider<Integer>(){
            @Override
            public Interval<Integer> getInterval(Integer value) {
                
                int diff = Math.abs(value - base) % width; 
                
                int lower, upper;
                if(value < base){
                    //if the diff was zero, it was an exact mod, then we need to add the width instead of zero.
                    upper = value + (diff == 0 ? width : diff);
                    lower = upper - width;
                }
                else{
                    lower = value - diff;
                    upper = lower + width;
                }    
                                
                return new Interval<>(lower, true, upper, false);
            }

            @Override
            public Interval<Integer> nextInterval(Interval<Integer> current, long factor) {
                
                int lower = (int) (current.getBegin() + (width * factor));
                int upper = lower + width;
                return new Interval<>(lower, true, upper, false);
            }

            @Override
            public Comparator<Integer> getComparator() {
                return ComparableComparator.getComparator(Integer.class);
            }
            
            @Override
            public String getName(Interval<Integer> interval){
                return interval.getBegin().toString();
            }

            @Override
            public Interval<Integer> getInterval(String name) {
                try{
                    int i = Integer.parseInt(name);
                    return getInterval(i);
                }catch(Exception ex){
                    return null;
                }
            }
        };
    }    
    
    /**
     * Creates a IntervalProvider for {@link Long} based intervals.
     * @param base The base from which intervals should be calculated.  Usually 0.
     * @param width The width of one interval.
     * @return A IntervalProvider providing intervals based on these settings.
     */
    public static IntervalProvider<Long> forLong(final long base, final long width){
        return new IntervalProvider<Long>(){
            @Override
            public Interval<Long> getInterval(Long value) {
                long diff = Math.abs(value - base) % width; 
                
                long lower, upper;
                if(value < base){
                    //if the diff was zero, it was an exact mod, then we need to add the width instead of zero.
                    upper = value + (diff == 0 ? width : diff);
                    lower = upper - width;
                }
                else{
                    lower = value - diff;
                    upper = lower + width;
                }    
                
                return new Interval<>(lower, true, upper, false);
            }

            @Override
            public Interval<Long> nextInterval(Interval<Long> current, long factor) {
                long lower = (current.getBegin() + (width * factor));
                long upper = lower + width;
                return new Interval<>(lower, true, upper, false);
            }

            @Override
            public Comparator<Long> getComparator() {
                return ComparableComparator.getComparator(Long.class);
            }
            
            @Override
            public String getName(Interval<Long> interval){
                return interval.getBegin().toString();
            }

            @Override
            public Interval<Long> getInterval(String name) {
                try{
                    long i = Long.parseLong(name);
                    return getInterval(i);
                }catch(Exception ex){
                    return null;
                }
            }
        };
    }
    
    /**
     * Creates a IntervalProvider for {@link Double} based intervals.
     * @param base The base from which intervals should be calculated.  Usually 0.
     * @param width The width of one interval.
     * @return A IntervalProvider providing intervals based on these settings.
     */
    public static IntervalProvider<Double> forDouble(final double base, final double width){
        return new IntervalProvider<Double>(){

            @Override
            public Interval<Double> getInterval(Double value) {
                double diff = Math.abs(value - base) % width; 
                
                double lower, upper;
                if(value < base){
                    //if the diff was zero, it was an exact mod, then we need to add the width instead of zero.
                    upper = value + (diff == 0 ? width : diff);
                    lower = upper - width;
                }
                else{
                    lower = value - diff;
                    upper = lower + width;
                }    
                
                return new Interval<>(lower, true, upper, false);
            }

            @Override
            public Interval<Double> nextInterval(Interval<Double> current, long factor) {
                double lower = (current.getBegin() + (width * factor));
                double upper = lower + width;
                return new Interval<>(lower, true, upper, false);
            }

            @Override
            public Comparator<Double> getComparator() {
                return ComparableComparator.getComparator(Double.class);
            }
            
            @Override
            public String getName(Interval<Double> interval){
                return interval.getBegin().toString();
            }

            @Override
            public Interval<Double> getInterval(String name) {
                try{
                    double i = Double.parseDouble(name);
                    return getInterval(i);
                }catch(Exception ex){
                    return null;
                }
            }
        };
        
    }
}
