/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import java.util.List;
import java.util.ArrayList;

/** 
 * A AnnotationSpec as referred to in the metadata
 *
 * @author jonathan
 */

public class NiteAnnotationSpec implements net.sourceforge.nite.meta.NAnnotationSpec {
    private int type=DISPLAY;
    private String filename=null;
    private String description=null;
    private ArrayList editable_codings=null;
    private ArrayList editable_objectsets=null;
    private ArrayList used_codings=null;
    private ArrayList used_ontologies=null;
    private ArrayList used_objectsets=null;
    private ArrayList used_signals=null;
    
    public NiteAnnotationSpec (int type, String name, String description, 
		       ArrayList ecodings, ArrayList ucodings, 
		       ArrayList eobjectsets, ArrayList uobjectsets, 
		       ArrayList uontologies, ArrayList signals  ) {
	if (type==EDITOR) { this.type=EDITOR; }
	this.filename=name;
	this.description=description;
	this.editable_codings=ecodings;
	this.used_codings=ucodings;
	this.editable_objectsets=eobjectsets;
	this.used_objectsets=uobjectsets;
	this.used_ontologies=uontologies;
	this.used_signals=signals;
    }

    /** returns the file name in which the annotation-board
        description is stored */
    public String getFileName() {
	return filename;
    }

    /** returns some descriptive text about the annotation-board for
        the purposes of GUI list of choices for example. */
    public String getDescription() {
	return description;
    }

    /** returns one of EDITOR or DISPLAY depending on whether the NOM
        can be changed by this annotation spec. */
    public int getType() {
	return type;
    }

    /** Find the codings that can be edited - returns an ArrayList of
        Strings */
    public List getEditableCodingNames() {
	return (List)editable_codings;
    }

    /** Find the codings that can be used but not edited - returns an
        ArrayList of Strings */
    public List getUsedCodingNames() {
	return (List)used_codings;
    }

    /** Find the codings that can be used but not edited - returns a
        List of Strings */
    public List getUsedObjectSetNames() {
	return (List)used_objectsets;
    }

    /** Find the object sets that can be edited - returns n List of
        Strings */
    public List getEditableObjectSetNames() {
	return (List)editable_objectsets;
    }

    /** Find the ontologies that can be used but not edited - returns a
        List of Strings */
    public List getUsedOntologyNames() {
	return (List)used_ontologies;
    }

    /** Find the names of the signals that should be displayed with
        this AnnotationSpec - returns ArrayList of Strings */
    public List getUsedSignals() {
	return (List)used_signals;
    }
}

