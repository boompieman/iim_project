package net.sourceforge.nite.datainspection.timespan;

import java.util.ArrayList;

/**
 * A BoundaryAlignment keeps track of the alignment between the boundaries annotated by two different annotators
 * (ann1 and ann2), and of the alignment between the two ('which boundaries are detected commonly?').
 *
 * Basically, it is a step between NOM annotation layers, and the final classifications that are based on the alignment of
 * the layers for two annotators and that are used for the calculation of some reliability measures.
 *
 * See design on paper for documentation :)
 */
public class BoundaryAlignment {
    
    public ArrayList boundaries1 = new ArrayList();
    public ArrayList boundaries2 = new ArrayList();

    public ArrayList alignedBoundaries = new ArrayList();

    public ArrayList unalignedBoundaries1 = new ArrayList();
    public ArrayList unalignedBoundaries2 = new ArrayList();
    
    String info = "";

}