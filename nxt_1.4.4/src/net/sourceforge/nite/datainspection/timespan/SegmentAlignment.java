package net.sourceforge.nite.datainspection.timespan;

import java.util.ArrayList;

/**
 * A SegmentAlignment keeps track of the alignment between the segments annotated by two different annotators
 * (ann1 and ann2), and of the alignment between the two ('which segments are detected commonly?').
 *
 * Basically, it is a step between NOM annotation layers, and the final classifications that are based on the alignment of
 * the layers for two annotators and that are used for the calculation of some reliability measures.
 *
 * See design on paper for documentation
 */
public class SegmentAlignment {
    
    public ArrayList segments1 = new ArrayList();
    public ArrayList segments2 = new ArrayList();

    public ArrayList alignedSegments = new ArrayList();

    public ArrayList unalignedSegments1 = new ArrayList();
    public ArrayList unalignedSegments2 = new ArrayList();
    
    String info = "";

}