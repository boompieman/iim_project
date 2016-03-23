package net.sourceforge.nite.datainspection.timespan;


/**
 * See design on paper for documentation :)
 *
 * Duplicates are identified as being boundaries with the same time stamp.
 */
public class Boundary implements Comparable {
    
    public double time = 0;
    
    /**Not used yet, but may be interesting in the future*/
    //public Object elementBefore = null; 
    /**Not used yet, but may be interesting in the future*/
    //public Object elementAfter = null;
    
    public Boundary(double t) {
        time = t;
    }
    
    /**
     * comparison is on value of time.
     */
    public int compareTo (Object o) {
        return Double.compare(time, ((Boundary)o).time);
    }
    
    public boolean equals(Object o) {
        return compareTo(o)==0;
    }    
   
}