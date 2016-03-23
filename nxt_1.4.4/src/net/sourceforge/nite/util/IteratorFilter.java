/* @author Dennis Reidsma
 * @version  0, revision $Revision: 1.1 $,
 */
// Last modification by: $Author: reidsma $
// $Log: IteratorFilter.java,v $
// Revision 1.1  2004/12/10 16:08:43  reidsma
// DR: Upmigration of AMI code into sourceforge
//
// Revision 1.6  2003/07/07 20:32:17  dennisr
// *** empty log message ***
//
// Revision 1.5  2003/07/06 21:33:13  dennisr
// Adapted to Predicate and Transform interfaces
//
// Revision 1.4  2003/07/02 19:50:21  dennisr
// *** empty log message ***
//
package net.sourceforge.nite.util;

import java.util.*;

/**
 *
 * An IteratorFilter is used to filter Objects in an Iterator 
 * and present them as a new Iterator.
 * <br>
 * The filter is initialised with a Predicate; only Objects for which 
 * the predicate p.valid(Object o) returns true
 * will be passed on, other object will be left out.
 * This implementation is lazy, meaning that the next object from the original 
 * iterator will not be accessed before it is needed due to a call to the 
 * next() method of this IteratorFilter.
 * The original Iterator will therefore be traversed only once and the elements 
 * will not be stored in an intermediate array or something like that.
 * <br>
 * By default, the IteratorFilter does not allow removal of objects.
 * <br>
 * Example: <br>
 * <code>
 * ..
 * Iterator it = someSetOfStrings.iterator();
 * Predicate p = new Predicate() {
 *     public boolean valid(Object o) {
 *         return ((String)o).startsWith("a");
 *     }
 * };            
 * Iterator filteredIt = new IteratorFilter(it, p);
 * ..
 * </code>
 * <br>
 * You can also implement special subclasses of Predicate
 * to achieve more complex boolean tests.
 * <p>
 * Examples of the use of the different Iterator modifyers (IteratorFilter,
 * IteratorTransform, IteratorChain) can be found in the class RTSI.
 *
 * @author Dennis Reidsma, UTwente
 * <br>
 * Adapted from the parlevink package (HMI group, University of Twente)
 */
public class IteratorFilter implements Iterator {
    /**
     * The Iterator to filter
     */
    private Iterator iter;
    
    /** 
     * A look-ahead for the Iterator, to allow examination of
     * the properties of the next object.
     */
    private Object next;

    private boolean traversalStarted = false;

    private Predicate thePred;
  
    /**
     * @param it The Iterator to filter
     * @param pred The Predicate
     */
    public IteratorFilter(Iterator it, Predicate pred) {
        if ((it == null) || (pred == null)) {
            throw new NullPointerException();
        }
        iter = it;
        thePred = pred;
    }
    
    private void startTraversal() {
        traversalStarted = true;
        moveToNextObject();
    }
    
    /**
     * Used to skip over all objects that should NOT
     * be passed on.
     */
    private void moveToNextObject() {
        next = null; //otherwise it gets stuck on the last object
        while (iter.hasNext()) {
            next = iter.next();
            if (!thePred.valid(next)) {
                next = null;
            } else {
                break;
            }
        }
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void remove(){
        throw new UnsupportedOperationException();
    }
    
    public boolean hasNext() {
        if (!traversalStarted)
            startTraversal();
        return next != null;
    }

    public Object next() {
        if (!traversalStarted)
            startTraversal();
        if (next == null) {
            throw new NoSuchElementException ();
        }
        Object result = next;
        moveToNextObject();
        return result;
    }

}
