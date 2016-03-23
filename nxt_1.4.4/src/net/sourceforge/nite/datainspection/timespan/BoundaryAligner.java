package net.sourceforge.nite.datainspection.timespan;

import java.util.ArrayList;

import net.sourceforge.nite.util.Pair;

/**
 * Aligns two 'derived boundary annotations'.
 * SHOULD BE REPLACED BY ALIGNMENT LIKE IN SEGMENTALIGNER
 
 */
public class BoundaryAligner {
    
    /** This default alignment method takes two boundaries to be 'equal' if the difference in time is at most 'th'.
    It is certain that each boundary is aligned only once, but there is no garantuee about how this is achieved. */
    public static BoundaryAlignment alignBoundaries(
                    ArrayList boundaryList1,    //boundaries identified by annotator 1
                    ArrayList boundaryList2,    //boundaries identified by annotator 2
                    double th                   //This default alignment method takes two boundaries to be 'equal' if the difference in time is at most 'th'.
                   ) {
            BoundaryAlignment result = new BoundaryAlignment();
            result.boundaries1 = boundaryList1;
            result.boundaries2 = boundaryList2;
            result.info = "th="+th;
            //start at the first boundaries in the lists; check if boundaries are the same while going forward through time;
            //create matched Pairs and put them in the alignedlist or add boundaries to the unaligned list as relevant.
            int i1 = 0;
            int i2 = 0;
            while (true) {
                //no more boundaries in list 1: add remaining in list 2 to unaligned, then exit loop
                if (i1 >= boundaryList1.size()) {
                    for (int n = i2; n < boundaryList2.size(); n++) {
                        result.unalignedBoundaries2.add(boundaryList2.get(n));
                    }
                    break;
                }
                //no more boundaries in list 2: add remaining in list 1 to unaligned, then exit loop
                if (i2 >= boundaryList2.size()) {
                    for (int n = i1; n < boundaryList1.size(); n++) {
                        result.unalignedBoundaries1.add(boundaryList1.get(n));
                    }
                    break;
                }
                //i and j both point to a boundary:
                Boundary b1 = (Boundary)boundaryList1.get(i1);
                Boundary b2 = (Boundary)boundaryList2.get(i2);
                if (Math.abs(b1.time-b2.time) <= th) {
                    //==if less than threshold apart, make pair, advance both counters i1 and i2
                    result.alignedBoundaries.add(new Pair(b1,b2));
                    i1++;
                    i2++;
                } else {
                    //==more than threshold apart: put 'earliest' in unaligned list and advance that counter
                    if (b1.time < b2.time) {
                        result.unalignedBoundaries1.add(b1);
                        i1++;
                    } else {
                        result.unalignedBoundaries2.add(b2);
                        i2++;
                    }
                }
            }
            
            return result;
    }

}