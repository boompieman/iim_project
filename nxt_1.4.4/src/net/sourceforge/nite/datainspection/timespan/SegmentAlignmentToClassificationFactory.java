package net.sourceforge.nite.datainspection.timespan;

import net.sourceforge.nite.datainspection.data.Classification;
import net.sourceforge.nite.datainspection.data.Item;
import net.sourceforge.nite.datainspection.impl.BooleanValue;
import net.sourceforge.nite.datainspection.impl.PairItem;
import net.sourceforge.nite.datainspection.data.NOMElementToValueDelegate;
import net.sourceforge.nite.util.Pair;
import net.sourceforge.nite.util.WeightedPair;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;

/**
 * Given two aligned segment lists, make two calc.Classification where all Pairs of aligned 
 * segments are Items; and the Values are determined by the NOMElementToValueDelegate
 */
public class SegmentAlignmentToClassificationFactory {
    
    /** Given two aligned segment lists, make two calc.Classification where all Pairs of aligned 
    segments are Items; and the Values are determined by the NOMElementToValueDelegate.
    Returns a Pair of Classifications. */
    public static Pair makeClassificationsFromAlignments(
                    SegmentAlignment alignment,    //the segment alignment information
                    NOMElementToValueDelegate nomElementToValue
                   ) {
            Classification c1 = new Classification("Annotator 1");
            Classification c2 = new Classification("Annotator 2");
            //process all aligned pairs
            for (int i = 0; i < alignment.alignedSegments.size(); i++) {
                WeightedPair nextPair = (WeightedPair)alignment.alignedSegments.get(i);
                Item newItem = new PairItem(nextPair);
                c1.add(newItem, nomElementToValue.getValueForNOMElement((NOMElement)nextPair.o1));
                c2.add(newItem, nomElementToValue.getValueForNOMElement((NOMElement)nextPair.o2));
            }
            //return result
            return new Pair(c1,c2);
    }

}