/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import java.util.List;

/** 
 * An ObjectSet as referred to in the metadata
 *
 * @author jonathan
 */

public class NiteObjectSet implements net.sourceforge.nite.meta.NObjectSet {
    private String name;
    private String description;
    private String filename;
    private List elements=null;
    
    public NiteObjectSet (String name, String description, String filename) {
	this.name=name;
	this.description=description;
	this.filename=filename;
    }

    /** returns the name of this ObjectSet as used for pointing to it */
    public String getName() {
	return name;
    }

    /** returns a description of the ObjectSet. */
    public String getDescription() {
	return description;
    }

    /** returns the filename of this ObjectSet */
    public String getFileName() {
	return filename;
    }

    /** get the list of elements valid in this layer - returns an
        ArrayList of NElements */
    public List getContentElements() {
	return (List)elements;
    }

    /** set the list of elements valid in this layer - takes an
        ArrayList of NElements */
    public void setContentElements(List elements) {
	this.elements=elements;
    }

}

