/* @author Dennis Reidsma
 * @version  0, revision $Revision: 1.1 $,
 */
// Last modification by: $Author: reidsma $
// $Log: IteratorChain.java,v $
// Revision 1.1  2004/12/10 16:08:43  reidsma
// DR: Upmigration of AMI code into sourceforge
//
// Revision 1.2  2003/07/06 21:33:12  dennisr
// Adapted to Predicate and Transform interfaces
//
// Revision 1.1  2003/07/02 19:50:20  dennisr
// *** empty log message ***
//

package net.sourceforge.nite.util;

import java.util.*;

/**
 * An IteratorChain is used to chain several Iterators, i.e.
 * iterate consecutively through each of the Iterators and present
 * them as one Iterator. The order in qhich the original Iterators are 
 * traversed is determined by the constructor parameter.
 * <p>
 * This implementation is lazy, meaning that the next object from a source 
 * iterator will not be accessed before it is needed due to a call to the 
 * next() method of this IteratorChain.
 * <p>
 * The original Iterators are stored internally in an array, though.
 * <p>
 * By default, the IteratorChain does not allow removal of objects.
 * <br>
 * Example: <br>
 * <code>
 * ..
 * Iterator[] iterators;
 * 
 * Iterator chainedIt = new IteratorChain(iterators);
 * ..
 * </code>
 * <p>
 * Examples of the use of the different Iterator modifyers (IteratorFilter,
 * IteratorTransform, IteratorChain) can be found in the class RTSI.
 *
 * @author Dennis Reidsma, UTwente
 * <br>
 * Adapted from the parlevink package (HMI group, University of Twente)
 */
public class IteratorChain implements Iterator {
    /**
     * The list of Iterators to chain
     */
    private Iterator[] iterators;
    
    /**
     * The index of the Iterator that is curently being traversed
     */
    private int currentIterator;
    
    /**
     * @param its The Iterators to be chained
     */
    public IteratorChain(Iterator[] its) {
        if (its == null) {
            throw new NullPointerException();
        }
        iterators = its;
        currentIterator = 0;
    }
    
    /**
     * @throws UnsupportedOperationException
     */
    public void remove(){
        throw new UnsupportedOperationException();
    }
    
    public boolean hasNext() {
        if (iterators[currentIterator].hasNext())
            return true;
        while (currentIterator < iterators.length - 1) {
            currentIterator++;
            if (iterators[currentIterator].hasNext())
                return true;
        }
        return false;
    }

    public Object next() {
        try {
            return iterators[currentIterator].next(); 
            //NB: if at end of one iterator, and hasNext was not called, this results in exception!
            //solution: catch exception and try "hasnext" to force move to next iterator with elements
        } catch (NoSuchElementException ex) {
            if (hasNext()) //forces move to next iterator...
                return iterators[currentIterator].next(); //MOET een element zijn; behalve als er ECHT niks meer was.
            throw new NoSuchElementException();
        }
    }

}
