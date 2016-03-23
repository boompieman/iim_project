/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/** 
 * NElement describes an element type. It simply comprises of its
 * attributes or pointers. Valid parents and children are defined at
 * the NLayer level.  
 */
public interface NElement {
    public static final int CODING=0;
    public static final int ONTOLOGY=1;
    public static final int OBJECTSET=2;
    public static final int CORPUSRESOURCE=3;

    /** returns the name of the element */
    public String getName();
    /** Find the attributes for this element - returns a List of
        NAttributes */
    public List getAttributes();
    /** find the NAttribute with the given name in this Element, or
        return null */
    public NAttribute getAttributeByName(String attr);
    /** find the valid pointers on this element - returns a List
	of NPointers */
    public List getPointers(); 
    /** returns the NLayer to which this element belongs. Note that we
        disallow the use of the same element name in multiple
        layers. */
    public NLayer getLayer(); 
    /** returns the type of the container of this element: if it's an
        element in an ontology, this returns ONTOLOGY etc. */
    public int getContainerType();
    /** returns the Object to which this element belongs. This can be
        an NLayer, an NCorpusResource, an NOntology or an NObjectSet.  */
    public Object getContainer(); 
    /** returns the NFile to which this element belongs. This can be
        an NCoding, NCorpusResource, an NOntology or an NObjectSet and
        it's the file-level entity to which this elemnent must
        belong.  */
    public NFile getFile(); 
    /** returns true if text content is permitted in this
        element. Note that since mixed content is disallowed, this
        will override any content model suggested by the layer to
        which this element belongs. */
    public boolean textContentPermitted();
    /** return the colour used to display this type of element on the
        OTAB. If no colour is set in the metadata or programatically,
        the return will be black */
    public java.awt.Color getDisplayColor();
    /** return the role of any external pointer for this element: will
     * only ever be non-null for elements in layers of type EXTERNAL_POINTER_LAYER. */
    public String getExternalPointerRole();
}
