package net.sourceforge.nite.util;

/**
 * A simple data class to hold a pair of objects (o1,o1)
 * Used in the datainspection packages
 */
public class Pair {
    public Object o1;
    public Object o2;
    public Pair(Object a, Object b) {
        o1 = a;
        o2 = b;
    }

    public boolean equals(Object o ) {
        return (((Pair)o).o1==o1) && (((Pair)o).o2==o2);
    }
        
}
