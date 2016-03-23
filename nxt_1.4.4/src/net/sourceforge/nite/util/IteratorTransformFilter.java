/* @author Dennis Reidsma
 * @version  0, revision $Revision: 1.1 $,
 */
// Last modification by: $Author: reidsma $
// $Log: IteratorTransformFilter.java,v $
// Revision 1.1  2004/12/10 16:08:43  reidsma
// DR: Upmigration of AMI code into sourceforge
//
// Revision 1.2  2003/07/07 23:02:57  dennisr
// *** empty log message ***
//
// Revision 1.1  2003/07/06 21:33:13  dennisr
// Adapted to Predicate and Transform interfaces
//
// Revision 1.4  2003/07/02 19:50:21  dennisr
// *** empty log message ***
//
package net.sourceforge.nite.util;

import java.util.*;

/**
 *
 * An IteratorTransform is used to transform Objects in an Iterator 
 * and present them as a new Iterator.
 * The difference with a normal IteratorTransform is that this implementation will 
 * not throw exceptions when the transformation fails but rather will act as a filter
 * for those objects, passing on only those objects in the original iterator on
 * which the transformation can be successfully applied.
 * <p>
 * I.e., the function hasNext will return true if the original iterator has no
 * next object that can be transformed.
 * <p>
 * NB: This is a VERY expensive iterator when there are many objects that cannot 
 * be transformed, due to all the exceptions that will be thrown.
 * 
 * @author Dennis Reidsma, UTwente
 * <br>
 * Adapted from the parlevink package (HMI group, University of Twente)
 */
public class IteratorTransformFilter implements Iterator {
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

    private Transform theTransform;
  
    /**
     * @param it The Iterator to filter
     * @param trans The Transformation
     */
    public IteratorTransformFilter(Iterator it, Transform trans) {
        if ((it == null) || (trans == null)) {
            throw new NullPointerException();
        }
        iter = it;
        theTransform = trans;
    }
    
    private void startTraversal() {
        traversalStarted = true;
        moveToNextObject();
    }
    
    /**
     * Used to skip over all objects that cannot be transformed
     */
    private void moveToNextObject() {
        next = null; //otherwise it gets stuck on the last object
        while (iter.hasNext()) {
            try {
                next = theTransform.transform(iter.next());
                break;
            } catch (Exception ex) {
                next = null;
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