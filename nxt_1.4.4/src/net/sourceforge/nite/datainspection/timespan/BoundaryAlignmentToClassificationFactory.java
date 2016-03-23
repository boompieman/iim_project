package net.sourceforge.nite.datainspection.timespan;

import net.sourceforge.nite.datainspection.data.Classification;
import net.sourceforge.nite.datainspection.data.Item;
import net.sourceforge.nite.datainspection.impl.BooleanValue;
import net.sourceforge.nite.datainspection.impl.PairItem;
import net.sourceforge.nite.util.Pair;

/**
 * Given two aligned boundary lists, make two calc.Classification as follows:
 * <br>1) All Pairs of aligned boundaries are Items; the Value for both annotators is True
 * <br>2) For each unaligned boundary a Pair is made as Item with the unaligned boundary, 
 * and a null for the other annotator; the Values for this Item are True for the annotator 
 * who DID find a boundary, and False for the other.
 * <br>NOTE: not used right now. See package doc for explanation why not.
 * See design on paper for documentation.
 */
public class BoundaryAlignmentToClassificationFactory {
    
    /** Given two aligned boundary lists, make two calc.Classification as follows:
    <br>1) All Pairs of aligned boundaries are Items; the Value for both annotators is True
    <br>2) For each unaligned boundary a Pair is made as Item with the unaligned boundary, 
    and a null for the other annotator; the Values for this Item are True for the annotator 
    who DID find a boundary, and False for the other. Returns a Pair of Classifications. */
    public static Pair makeClassificationsFromAlignments(
                    BoundaryAlignment alignment    //the boundary alignment information
                   ) {
            Classification c1 = new Classification("Annotator 1");
            Classification c2 = new Classification("Annotator 2");
            //process all aligned pairs
            for (int i = 0; i < alignment.alignedBoundaries.size(); i++) {
                Item newItem = new PairItem((Pair)alignment.alignedBoundaries.get(i));
                c1.add(newItem, new BooleanValue(true));
                c2.add(newItem, new BooleanValue(true));
            }
            //process all unaligned boundaries for annotator 1
            for (int i = 0; i < alignment.unalignedBoundaries1.size(); i++) {
                Item newItem = new PairItem(new Pair(alignment.unalignedBoundaries1.get(i),null));
                c1.add(newItem, new BooleanValue(true));
                c2.add(newItem, new BooleanValue(false));
            }
            //process all unaligned boundaries for annotator 2
            for (int i = 0; i < alignment.unalignedBoundaries2.size(); i++) {
                Item newItem = new PairItem(new Pair(null,alignment.unalignedBoundaries2.get(i)));
                c1.add(newItem, new BooleanValue(false));
                c2.add(newItem, new BooleanValue(true));
            }
            
            //return result
            return new Pair(c1,c2);
    }

}