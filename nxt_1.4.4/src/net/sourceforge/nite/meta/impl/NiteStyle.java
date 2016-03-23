/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import java.util.List;
import java.util.ArrayList;

/** 
 * A stylesheet as referred to in the metadata
 *
 * @author jonathan
 */

public class NiteStyle implements net.sourceforge.nite.meta.NStyle {
    private int type=DISPLAY;
    private int application=OTAB;
    private String name=null;
    private String extension=null;
    private String description=null;
    private ArrayList codings=null;
    private ArrayList objectsets=null;
    private ArrayList ontologies=null;
    
    public NiteStyle (int type, int application, String name, 
		      String extension, String description, 
		       ArrayList codings, ArrayList objectsets,
		       ArrayList ontologies  ) {
	if (type==EDITOR) { this.type=EDITOR; }
	if (application==NIE) { this.application=NIE; }
	this.name=name;
	this.extension=extension;
	this.description=description;
	this.codings=codings;
	this.objectsets=objectsets;
	this.ontologies=ontologies;
    }

    /** returns the name of the style as used in filenames */
    public String getName() {
	return name;
    }

    /** returns the filename extension of the style as used in
        filenames */
    public String getExtension() {
	return extension;
    }

    /** returns a textual description of the style that can be used,
        for example, in a GUI list to be chosen from */
    public String getDescription() {
	return description;
    }

    /** returns one of EDITOR or DISPLAY depending on whether the NOM
        can be changed by this stylesheet. */
    public int getType() {
	return type;
    }

    /** returns one of NIE or OTAB depending on which program this is
        a spec for. */
    public int getApplication() {
	return application;
    }

    /** Find the codings that can are used / edited - returns an ArrayList of
        Strings */
    public List getCodingNames() {
	return (List)codings;
    }

    /** Find the object sets that can be used / edited - returns a
        List of Strings */
    public List getObjectSetNames() {
	return (List)objectsets;
    }

    /** Find the ontologies that can be used - returns a
        List of Strings */
    public List getOntologyNames() {
	return (List)ontologies;
    }

    /** Find the names of the signals that should be displayed with
        this stylesheet - returns ArrayList of Strings 
    public List getSignals() {
	return (List)signals;
	} */
}

