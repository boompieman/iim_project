package net.sourceforge.nite.util;

/**
 * A simple data class to hold a weighted pair of objects (o1,o1,w).
 * Used in the datainspection packages
 * <p>Compare rules:
 * <br>pair1 &gt; null
 * <br>pair1 == pair1
 * <br>otherwise, ordering is on w
 */
public class WeightedPair extends Pair implements Comparable {
    public double w=0;
    public WeightedPair(Object a, Object b, double weight) {
        super(a,b);
        w = weight;
    }
    public int compareTo(Object o) {
        if( o == null) {
            return 1;
        } 
        if (o==this)
            return 0;
            
        double w2 = ((WeightedPair)o).w;
        if (w2==w) return 0;
        if (w2 < w) 
            return 1;
        else 
            return -1;
    }        
}
