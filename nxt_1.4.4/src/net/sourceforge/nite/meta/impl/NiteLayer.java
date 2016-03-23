/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import net.sourceforge.nite.meta.NCoding;
import net.sourceforge.nite.meta.NLayer;
import java.util.List;
import java.util.ArrayList;

/** 
 * A layer as referred to in the metadata
 *
 * @author jonathan
 */

public class NiteLayer implements net.sourceforge.nite.meta.NLayer {
    private String name;
    private int type = FEATURAL_LAYER;
    //    private NiteCoding coding=null;
    private Object container=null;
    private ArrayList parents=null;
    private NiteLayer parentincoding=null;
    private NiteLayer childlayer=null;
    private ArrayList elements=null;
    private String child_layer=null;
    private String program=null;
    private String content_type=null;
    private boolean recursive_child=false;
    private boolean inherits_time=false;
    private List arguments=null;
    
    public NiteLayer (String name, int type, Object container, 
		      String childlayer, String recursivechild, 
		      String program, String content, boolean inheritstime) {
	this.name=name;
	if ((type==STRUCTURAL_LAYER) || (type==TIMED_LAYER) || 
	    (type==EXTERNAL_POINTER_LAYER)) { this.type=type; }
	this.container=container;
	
	if (recursivechild!=null && !recursivechild.equals("")) {
	    recursive_child=true;
	    if (!recursivechild.equalsIgnoreCase(name)) {
		this.child_layer=recursivechild;
	    }
	} else {
	    this.child_layer=childlayer;
	}
	this.program=program;
	this.content_type=content;
	this.inherits_time=inheritstime;
    }

    public NiteLayer (String name, int type, Object container) {
	this.name=name;
	if ((type==STRUCTURAL_LAYER) || (type==TIMED_LAYER)
	    || (type==EXTERNAL_POINTER_LAYER)) { this.type=type; }
	this.container=container;
    }

    /** the name of the layer */
    public String getName() {
	return name;
    }

    /** The type of the layer - returns TIMED_LAYER or
        FEATURAL_LAYER or STRUCTURAL_LAYER or EXTERNAL_POINTER_LAYER */
    public int getLayerType() {
	return type;
    }

    /** Returns the coding to which this layer belongs. Codings help
        us make the link to file names 
    public NCoding getCoding() {
	return coding;
    } 
    */

    /** Returns the container to which this layer belongs. Containers
        are one of NCoding, NOntology or NObjectSet */
    public Object getContainer() {
	return container;
    }

    /** get the parent(s) of this layer - returns an ArrayList of NLayers */
    public List getParentLayers() {
	return (List)parents;
    }

    /** Find the parent layer in the given coding */
    public NLayer getParentLayerInCoding(Object contain) {
	return parentincoding;
    }

    public NLayer getChildLayer() {
	return childlayer;
    }

    public void setChildLayer(NiteLayer layer) {
	childlayer=layer;
    }

    public void addParentLayer(NiteLayer layer) {
	if (parents==null) {
	    parents=new ArrayList();
	}
	parents.add(layer);
	if (layer!=this) {
	    if (layer.getContainer()==container) {
		parentincoding=layer;
	    }
	}
    }

    /** get the list of elements valid in this layer - returns an
        ArrayList of NElements */
    public List getContentElements() {
	return (List)elements;
    }

    /** set the list of elements valid in this layer - takes an
        ArrayList of NElements */
    public void setContentElements(ArrayList elements) {
	this.elements=elements;
    }


    /** Whether the sub-layer is pointed to recursively */
    public boolean getRecursive() {
	return recursive_child;
    }

    /** get the name of the layer from which this layer draws children
     * @deprecated Use {@link #getChildLayerName()}
     */
    public String getPointsTo() {
	return child_layer;
    }

    /** get the name of the layer from which this layer draws children */
    public String getChildLayerName() {
	return child_layer;
    }

    /** set the name of the child layer - this is only used internally 
     * @deprecated Use {@link #setChildLayerName()}
     */
    protected void setPointsTo(String layername) {
	child_layer=layername;
    }

    /** set the name of the child layer - this is only used internally 
     */
    protected void setChildLayerName(String layername) {
	child_layer=layername;
    }

    /** Return true if this is the top layer in a file. */
    public boolean isTopLayerInCoding() {
	if (container instanceof NCoding) {
	    NCoding cod = (NCoding) container;
	    if (this==cod.getTopLayer()) { 
		return true; 
	    }
	}
	return false;
    }

    /** return true if this layer can inherit time from lower
     * layers. This allows us to have structural layers that have
     * temporally overlapping children */
    public boolean inheritsTime() {
	return inherits_time;
    }

    /** This stores the program associated with the external
     * pointer for this layer: this value will only ever be non-null
     * when the layer type is EXTERNAL_POINTER_LAYER. */
    public String getProgram() {
	return program;
    }

    /** the content type of this external layer: this value will only
     * ever be non-null when the layer type is
     * EXTERNAL_POINTER_LAYER. */
    public String getContentType() {
	return content_type;
    }

    /** the arguments to pass to the program associated with the
     * external pointer for this layer: this value will only ever be
     * non-null when the layer type is EXTERNAL_POINTER_LAYER. */
    public List getProgramArguments() {
	return arguments;
    }

    /** the arguments to pass to the program associated with the
     * external pointer for this layer: this value will only ever be
     * non-null when the layer type is EXTERNAL_POINTER_LAYER. */
    public void addProgramArgument(String parameter, String value) {
	String arg = parameter + " " + value;
	if (arguments==null) {
	    arguments = new ArrayList();
	}
	arguments.add(arg);
    }


}

