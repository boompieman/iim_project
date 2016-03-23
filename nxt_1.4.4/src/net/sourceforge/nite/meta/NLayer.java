/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/**
 * Layers describe the permissible content models of the hierarchies
 * in the NOM. We assume a content model of 
 *   (child1|child2|...|childn)*
 * A stricter model may be used in other parts of the system.
 *
 * @author jonathan 
 */
public interface NLayer {
    public static final int FEATURAL_LAYER=0;
    public static final int STRUCTURAL_LAYER=1;
    public static final int TIMED_LAYER=2;
    public static final int EXTERNAL_POINTER_LAYER=3;

    /** returns the name of the layer. */
    public String getName();
    /** returns one of TIMED_LAYER, STRUCTURAL_LAYER, FEATURAL_LAYER
     * or EXTERNAL_POINTER_LAYER */
    public int getLayerType();
    /** Returns the coding to which this layer belongs. Codings help
        us make the link to file names 
	public NCoding getCoding(); */
    /** Returns the container to which this layer belongs. Containers
        are one of NCoding, NOntology or NObjectSet */
    public Object getContainer();
    /** get the parent(s) of this layer - returns a List of NLayers */
    public List getParentLayers();
    /** Find the parent layer in the given coding */
    public NLayer getParentLayerInCoding(Object container);
    /** Get the layer below this in the hierarchy. Note that this
        child may not be in the same NCoding. */
    public NLayer getChildLayer();
    /** get the list of elements valid in this layer - returns a
        List of NElements */
    public List getContentElements();
    /** Whether the sub-layer is pointed to recursively */
    public boolean getRecursive();
    public boolean isTopLayerInCoding();
    /** return true if this layer can inherit time from lower
     * layers. This allows us to have structural layers that have
     * temporally overlapping children */
    public boolean inheritsTime();

    /** the content type of this external layer: this value will only
     * ever be non-null when the layer type is
     * EXTERNAL_POINTER_LAYER. This can be used instead of getProgram. */
    public String getContentType();

    /** the program associated with the external pointer for this
     * layer: this value will only ever be non-null when the layer
     * type is EXTERNAL_POINTER_LAYER. */
    public String getProgram();

    /** the arguments to pass to the program associated with the
     * external pointer for this layer: this value will only ever be
     * non-null when the layer type is EXTERNAL_POINTER_LAYER. */
    public List getProgramArguments();

    /** the arguments to pass to the program associated with the
     * external pointer for this layer: this value will only ever be
     * non-null when the layer type is EXTERNAL_POINTER_LAYER. */
    public void addProgramArgument(String parameter, String value);

    /** get the name of the layer from which this layer draws children */
    public String getChildLayerName();

}
