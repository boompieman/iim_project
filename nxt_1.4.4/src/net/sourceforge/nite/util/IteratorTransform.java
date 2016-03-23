/* @author Dennis Reidsma
 * @version  0, revision $Revision: 1.1 $,
 */
// Last modification by: $Author: reidsma $
// $Log: IteratorTransform.java,v $
// Revision 1.1  2004/12/10 16:08:43  reidsma
// DR: Upmigration of AMI code into sourceforge
//
// Revision 1.2  2003/07/07 23:00:43  dennisr
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
 * <br>
 * The filter is initialised with a Transform; for every object in the
 * original Iterator the transformed Object will be returned. 
 * This implementation is lazy, meaning that the next object from the original 
 * iterator will not be accessed before it is needed due to a call to the 
 * next() method of this IteratorTransform.
 * The original Iterator will therefore be traversed only once and the elements 
 * will not be stored in an intermediate array or something like that.
 * <p>NB: If the transformation results in an exception, this exception will be 
 * thrown at the call of iterator.next(). If you want to avoid these exceptions,
 * use IteratorTransformFilter.
 * <br>
 * By default, the IteratorTransform does not allow removal of objects.
 * <br>
 * Example: <br>
 * <code>
 * ..
 * Iterator it = someSetOfStrings.iterator();
 * Transform t = new Transform() {
 *     public boolean transform(Object o) {
 *         return ((String)o).reverse();
 *     }
 * };            
 * Iterator tansformedIt = new IteratorTransform(it, t);
 * ..
 * </code>
 * The new iterator will return all reversed strings from the original Iterator.
 * <br>
 * You can also implement special subclasses of Transform
 * to achieve more complex transformations.
 * <p>
 * Examples of the use of the different Iterator modifyers (IteratorFilter,
 * IteratorTransform, IteratorChain) can be found in the class RTSI.
 * 
 * @author Dennis Reidsma, UTwente
 * <br>
 * Adapted from the parlevink package (HMI group, University of Twente)
 */
public class IteratorTransform implements Iterator {
    /**
     * The Iterator to filter
     */
    private Iterator iter;
    
    private Transform theTransform;
  
    /**
     * @param it The Iterator to filter
     * @param trans The Transform
     */
    public IteratorTransform(Iterator it, Transform trans) {
        if ((it == null) || (trans == null)) {
            throw new NullPointerException();
        }
        iter = it;
        theTransform = trans;
    }
    
    /**
     * @throws UnsupportedOperationException
     */
    public void remove(){
        throw new UnsupportedOperationException();
    }
    
    public boolean hasNext() {
        return iter.hasNext();
    }

    public Object next() {
        return theTransform.transform(iter.next());
    }

}
