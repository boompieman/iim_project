/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.util.Debug;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;

/** 
 * A element as referred to in the metadata
 *
 * @author jonathan
 */

public class NiteElement implements net.sourceforge.nite.meta.NElement {
    private String name;
    private NiteLayer layer;
    private ArrayList attributes=null;
    private ArrayList pointers=null;
    private NitePointer extpointer=null;
    private int type;
    private Object container;
    private boolean textcontent=false;
    private Color colour=java.awt.Color.BLACK;
    
    public NiteElement (String name, ArrayList attributes,
			ArrayList pointers, NiteLayer layer, String col, String textc) {
	this.name=name;
	this.attributes=attributes;
	this.pointers=pointers;
	this.layer=layer;
	this.container=(Object)layer;
	this.type=CODING;
	if (col!=null) {
	    this.colour=findColour(col);
	}
	if (textc!=null && textc.equalsIgnoreCase("true")) {
	    textcontent=true;
	}
    }


    public NiteElement (String name, ArrayList attributes,
			ArrayList pointers, Object object, String col, String textc) {
	this.name=name;
	this.attributes=attributes;
	this.pointers=pointers;
	this.container=object;
	if (object instanceof NiteLayer) {
	    NFile nf = getFile();
	    if (nf instanceof NCorpusResourceCoding) {
		this.type=CORPUSRESOURCE;
	    } else {
		this.type=CODING;
	    }
	} else if (object instanceof NiteOntology) {
	    this.type=ONTOLOGY;
	} else if (object instanceof NiteObjectSet) {
	    this.type=OBJECTSET;
	} else if (object instanceof NiteCorpusResource) {
	    this.type=CORPUSRESOURCE;
	}
	this.colour=findColour(col);
	if (textc.equalsIgnoreCase("true")) {
	    textcontent=true;
	}
    }

    /** returns the name of the element */
    public String getName() {
	return name;
    }

    /** returns the NLayer to which this element belongs. Note that we
        disallow the use of the same element name in multiple
        layers. */
    public NLayer getLayer() {
	if (container instanceof NLayer) {
	    return (NLayer)container;
	} else {
	    return null;
	}
    }

    /** Find the attributes for this element - returns an ArrayList of
        NAttributes */
    public List getAttributes() {
	return (List)attributes;
    }

    /** find the NAttribute with the given name in this Element, or
        return null */
    public NAttribute getAttributeByName(String attname) {
	Iterator ait = attributes.iterator();
	while (ait.hasNext()) {
	    NiteAttribute tat = (NiteAttribute) ait.next();
	    if (tat.getName().equals(attname)) { return tat; }
	}
	return null;
    }

    /** find the valid pointers on this element - returns an ArrayList
	of NPointers */
    public List getPointers() {
	return (List)pointers;
    }

    /** Set the pointers from this element */
    protected void setPointers(List ps) {
	pointers=(ArrayList)ps;
    }

    /** returns the type of the container of this element: if it's an
        element in an ontology, this returns ONTOLOGY etc. */
    public int getContainerType() {
	return type;
    }

    /** returns the Object to which this element belongs. This can be
        an NLayer, an NOntology or an NObjectSet.  */
    public Object getContainer() {
	return container;
    }

    /** returns the NFile to which this element belongs. This can be
        an NCoding, NCorpusResource, an NOntology or an NObjectSet and
        it's the file-level entity to which this elemnent must
        belong.  */
    public NFile getFile() {
	if (container instanceof NFile) { return (NFile)container; }
	if (container instanceof NLayer) {
	    return (NFile)((NLayer)container).getContainer();
	}
	return null;
    }

    /** add an attribute declaration to the list for this element */
    public void addAttribute(NAttribute nat) {
	if (attributes==null) { attributes=new ArrayList(); }
	attributes.add((Object)nat);
    }

    /** add an external pointer for this element: will only ever be
     * used for elements in layers of type EXTERNAL_POINTER_LAYER. */
    protected void addExternalPointer(String role) {
	extpointer = new NitePointer(role, null, "1");
    }

    /** return the role of any external pointer for this element: will
     * only ever be non-null for elements in layers of type EXTERNAL_POINTER_LAYER. */
    public String getExternalPointerRole() {
	return extpointer.getRole();
    }

    /** returns true if text content is permitted in this
        element. Note that since mixed content is disallowed, this
        will override any content model suggested by the layer to
        which this element belongs. */
    public boolean textContentPermitted() {
	return textcontent;
    }

    /** return the colour used to display this type of element on the
        OTAB. If no colour is set in the metadata or programatically,
        the return will be black */
    public java.awt.Color getDisplayColor() {
	return colour;
    }

    /** set the colour used to display this type of element on the
        OTAB. */
    protected void setDisplayColor(Color col) {
	colour=col;
    }

    /** set the colour used to display this type of element on the
        OTAB. This version interprets a string of the format 'dd01aa'
        (three 2 byte hex numbers for RGB levels) */
    protected void setDisplayColor(String col) {
	colour=findColour(col);
    }

    /** parse a string and try to find a colour (or return black) */
    private Color findColour(String col) {
	Color retcol = Color.BLACK;
	try {
	    Color ncol=Color.decode(col);
	    return ncol;
	} catch (NumberFormatException nex) {
	    
	}
	return retcol;
    }

}


