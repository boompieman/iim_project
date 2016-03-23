/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * IMS, University of Stuttgart
 * Holger Voormann
 */
package net.sourceforge.nite.search;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * ListPermuteIterator implement an {@linkplain java.util.Iterator} to
 * walk through all permutations of the given list.
 * I.e. {@linkplain #next()} of a new ListPermuteIterator( {a, b, c} ) will
 * visit these lists:<br>
 * {a, b, c}<br>
 * {a, c, b}<br>
 * {b, a, c}<br>
 * {b, c, a}<br>
 * {c, a, b}<br>
 * {c, b, a}.
 */
public class ListPermuteIterator
implements java.util.Iterator
{
  private List list;
  private List nextList;
  private int[] counter;

  /**
   * Creates a new {@linkplain java.util.Iterator} to walk through all
   * permutations of the given list.
   * @param list the list to permutate
   */
  public ListPermuteIterator(List list)
  {
    this.list = list;
    counter = new int[ list.size() ];
    for( int i=0; i<counter.length; i++ ){ counter[i]=0; }
    if( list.size() != 1 ){
      increment();
    } else {
      nextList = list;
    }
  }

  /**
   * Finds the next permutation. If there is no next permutation {@linkplain
   * #nextList} will be set to null.
   */
  private void increment()
  {
    boolean pairwiseUnequale;
    boolean incremented;

    do {

      //increment counter
      incremented = false;
      for( int i=counter.length-1; i>=0; i-- ){
        counter[i]++;
        if( counter[i] < list.size() ){
          incremented = true;
          break;
        } else {
          counter[i] = 0;
        }
      }

      //digits must be pairwise unequale
      pairwiseUnequale = true;
      pairwiseTestLoop:
      for( int i=0; i<counter.length-1; i++ ){
        for( int j=i+1; j<counter.length; j++ ){
          if( counter[i] == counter[j] ){
            pairwiseUnequale = false;
            break pairwiseTestLoop;
          }
        }
      }

    } while( !pairwiseUnequale && incremented );

    //set next list
    if( incremented ){
      //next permutation available
      List newList = new ArrayList( list.size() );
      for( int i=0; i<counter.length; i++ ){
        newList.add( list.get(counter[i]) );
      }
      nextList = newList;
    } else {
      //no next permutation
      nextList = null;
    }

  }

  /**
   * Returns true if the iteration has more permutations. (In other words,
   * returns true if {@linkplain #next()} would return an element rather than
   * throwing an exception.)
   * @return true if the iteration has more permutations
   */
  public boolean hasNext(){ return ( nextList != null ); }

  /**
   * Returns the next permutation in the iteration.
   * @return the next permutation in the iteration
   * @throws NoSuchElementException there are no more permutations
   */
  public Object next() throws NoSuchElementException
  {
    if( nextList == null ){ throw new NoSuchElementException(); }
    Object ret = nextList;
    increment();
    return ret;
  }

  /** The remove operation is not supported by this Iterator. */
  public void remove(){ throw new UnsupportedOperationException(); }

}