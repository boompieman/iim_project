package net.sourceforge.nite.datainspection.timespan;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;

import net.sourceforge.nite.util.Pair;
import net.sourceforge.nite.util.WeightedPair;
import net.sourceforge.nite.util.Predicate;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;


/**
 * Aligns two 'segment annotations'.
 *
 * See design on paper for documentation
 */
public class SegmentAligner {
    
    /** This default alignment method takes two segments to be 'equal' if the difference in time is at most 'th'.
    First all potential candidates are selected (based on th); then the final matchings are selected, searching
    through the candidates for the 'best' matches.
    It is certain that each segments is aligned only once, but there is no garantuee about how this is achieved. */
    public static SegmentAlignment alignSegments(
                    ArrayList segmentList1,    
                    ArrayList segmentList2,    
                    double th,
                    Predicate isIgnoreP
                   ) {
            SegmentAlignment result = new SegmentAlignment();
            result.segments1 = segmentList1;
            result.segments2 = segmentList2;
            result.info = "th="+th;

            ArrayList candidateMatchings = collectCandidateMatchings(result, th, isIgnoreP);
            selectFinalMatchings(result, candidateMatchings);
            return result;
    }
    

    /**
     * First step in the alignment / matching process: collect all candidate 
     * matchings for pairs of segments from two annotators.
     * Leans heavily on implementation of 'getCandidate'.
     */
    private static ArrayList collectCandidateMatchings(SegmentAlignment data, double th,Predicate isIgnoreP) {
        ArrayList candidateMatchings = new ArrayList();
        for (int m = 0; m < data.segments1.size(); m++) {
            for (int n = 0; n < data.segments2.size(); n++) {
                WeightedPair candidate = getCandidate((NOMWriteElement)data.segments1.get(m),(NOMWriteElement)data.segments2.get(n), th, isIgnoreP);
                if (candidate!=null) {
                    candidateMatchings.add(candidate);
                    //System.out.println("candidate");
                } else {
                    //System.out.println("no candidate");
                }

            }
        }
        Collections.sort(candidateMatchings); //sort on weight
        return candidateMatchings;
    }                

    /**
     * Given two segments from different coders, return null if they cannot be aligned (e.g. too far away),
     * or a weighted pair as candidate if they MIGHT be aligned.
     *
     * Parameters & rules for deciding candidates, in order of application:
     *      If one of the elements is an ignore element, there is no alignment. 
     *      To be considered a candidate alginemt, the start points, and the end points, 
     *          should be apart no more than /th/ seconds.
     * <p>
     * If the timing constraints are met, the pair is a candidate for alignment. The WEIGHT of the alignments
     * is calculated based on timing difference 
     
     *<p><p><p>NOT APPLIED YET is the following extension. (see also package documentation)
     *          as a special case, the elements should be 'very large'. In the last case
     *          there should be a large /overlap/ between the segments. (The rationale is that if 
     *          segments are 20 seconds long, then a delay of 2 seconds is not as bad as long as the
     *          /overlap/ between the segments is e.g. 15 seconds).
     *          'Large enough': overlap divided by sum of lengths should be .25 or larger
     *          ( they overlap for more than half (equalsized) or e.g. one is no more than 3 
     *           times size of other (unequalsized containment))
     
     *<p><p><p>NOT APPLIED YET is the following extension. (see also package documentation)
     * WEIGHT is also determined by the labelling difference as calculated by the DistanceMetric
     * from the constructor.
     */
    public static WeightedPair getCandidate(NOMWriteElement nwe1, NOMWriteElement nwe2, double th, Predicate isIgnoreP) {
        if (isIgnoreP.valid(nwe1)||isIgnoreP.valid(nwe2)) { 
            return null;
        }
        //return, if one of the times is NaN
        if (new Double(nwe1.getStartTime()).toString().equals("NaN")) {
            return null;
        }
        if (new Double(nwe2.getStartTime()).toString().equals("NaN")) {
            return null;
        }
        if (new Double(nwe1.getEndTime()).toString().equals("NaN")) {
            return null;
        }
        if (new Double(nwe2.getEndTime()).toString().equals("NaN")) {
            return null;
        }
        
        //check timing: if start points more than threshold apart and/or end points more than threshold apart,
        // overlap should be 'large enough'.
        if (   (Math.abs(nwe1.getStartTime()-nwe2.getStartTime()) > th) 
            || (Math.abs(nwe1.getEndTime()  -nwe2.getEndTime()  ) > th) ) {
            return null;
        }
        //timedistance: 
        //  exact same timing: distance = 0.
        //  maximum difference on end and start (based on th): distance = 1
        double timeDistance = Math.min(1,
                                (  Math.abs(nwe1.getStartTime()-nwe2.getStartTime())
                                 + Math.abs(nwe1.getEndTime()-nwe2.getEndTime())
                                )/(2d*th)
                              );
        //weight is an inverse of distance. label counts more than distance
        double w = 1-(timeDistance);

        WeightedPair result = new WeightedPair(nwe1,nwe2,w);
        return result;
    }            


    /**
     * Use a derivation of the MUMIS merging algorithm to select final alignments between segments of different annotators.
     */
    public static void selectFinalMatchings(SegmentAlignment data, ArrayList candidateMatchings) {
        Set unmatchedSegments1 =  new HashSet();
        unmatchedSegments1.addAll(data.segments1);
        Set unmatchedSegments2 =  new HashSet();
        unmatchedSegments2.addAll(data.segments2);
        data.alignedSegments = createFinalMatching(candidateMatchings,unmatchedSegments1,unmatchedSegments2);
        data.unalignedSegments1.addAll(unmatchedSegments1); //modified by the call to createFinalMatching
        data.unalignedSegments2.addAll(unmatchedSegments2); //modified by the call to createFinalMatching
    }
    /**recursive: make one heaviest match final, then split in left and right and do again
    method taken from {kuper2004,IJCAI, MUMIS merging}
    candidatematching lists are sorted on weight! but final matching list will automatically be sorted on time!
    
    also modifies the unmatchedSegmentLists...
    */
    public static ArrayList createFinalMatching(ArrayList candidates, Set unmatched1, Set unmatched2) {
        if (candidates.size()==0) {
            return new ArrayList();
        }
        WeightedPair fix = getHeaviest(candidates);
        unmatched1.remove(fix.o1);
        unmatched2.remove(fix.o2);
        ArrayList left = createFinalMatching(makeLeft(candidates,fix),unmatched1,unmatched2);
        ArrayList right = createFinalMatching(makeRight(candidates,fix),unmatched1,unmatched2);
        ArrayList result = new ArrayList();
        result.addAll(left);
        result.add(fix);
        result.addAll(right);
        return result;
    }
    protected static WeightedPair getHeaviest(ArrayList candidates) {
        return (WeightedPair)candidates.get(candidates.size()-1);
    }
    //makeleft: still sorted on weight!
    protected static ArrayList makeLeft(ArrayList candidates, WeightedPair fix) {
        ArrayList result = new ArrayList();
        double t1 = ((NOMWriteElement)fix.o1).getStartTime();
        double t2 = ((NOMWriteElement)fix.o2).getStartTime();
        Iterator it = candidates.iterator();
        while (it.hasNext()) {
            WeightedPair next = (WeightedPair)it.next();
            if((next==fix)||(next.o1==fix.o1)||(next.o2==fix.o2)||(next.o1==fix.o2)||(next.o2==fix.o1))continue;
            if ((((NOMWriteElement)next.o1).getEndTime()<=t1) && (((NOMWriteElement)next.o2).getEndTime()<=t2)) {
                result.add(next);
            }
        }
        return result;
    }
    //makeright: still sorted on weight!
    protected static ArrayList makeRight(ArrayList candidates, WeightedPair fix) {
        ArrayList result = new ArrayList();
        double t1 = ((NOMWriteElement)fix.o1).getEndTime();
        double t2 = ((NOMWriteElement)fix.o2).getEndTime();
        Iterator it = candidates.iterator();
        while (it.hasNext()) {
            WeightedPair next = (WeightedPair)it.next();
            if((next==fix)||(next.o1==fix.o1)||(next.o2==fix.o2)||(next.o1==fix.o2)||(next.o2==fix.o1))continue;
            if ((((NOMWriteElement)next.o1).getStartTime()>=t1) && (((NOMWriteElement)next.o2).getStartTime()>=t2)) {
                result.add(next);
            }
        }
        return result;
    }    
}