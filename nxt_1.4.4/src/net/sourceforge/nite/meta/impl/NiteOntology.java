/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;


import java.util.List;

/** 
 * An ontology as referred to in the metadata
 *
 * @author jonathan
 */

public class NiteOntology implements net.sourceforge.nite.meta.NOntology {
    private String name;
    private String description;
    private String filename;
    private String elementname;
    private String attributename;
    private List other_attributes=null;
    
    /** Constructor taking name, description, filename, element name
        and distinguished attribute name (the name of the subtypes in
        the ontology. */
    public NiteOntology (String name, String description, String filename,
			 String elementname, String attributename) {
	this.name=name;
	this.description=description;
	this.filename=filename;
	this.elementname=elementname;
	this.attributename=attributename;
    }

    /** returns the name of this ontology as used in filenames etc. */
    public String getName() {
	return name;
    }

    /** returns a description of the ontology. */
    public String getDescription() {
	return description;
    }

    /** returns the file name of this ontology. */
    public String getFileName() {
	return filename;
    }

    /** returns the element name of this ontology. */
    public String getElementName() {
	return elementname;
    }

    /** returns the significant attribute name for this ontology. */
    public String getAttributeName() {
	return attributename;
    }

}

