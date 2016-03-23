/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import java.util.List;
import java.util.ArrayList;

/** 
 * A attribute as referred to in the metadata
 *
 * @author jonathan
 */

public class NiteAttribute implements net.sourceforge.nite.meta.NAttribute {
    private boolean serialize=true;
    private String name;
    private int type = STRING_ATTRIBUTE;
    private ArrayList values=null;
    
    public NiteAttribute (String name, int type, ArrayList values) {
	this.name=name;
	this.values=values;
	if ((type==NUMBER_ATTRIBUTE) || (type==ENUMERATED_ATTRIBUTE)) { 
	    this.type=type; 
	}
    }

    /** returns the name of the attribute */
    public String getName() {
	return name;
    }

    /** returns one of STRING_ATTRIBUTE, NUMBER_ATTRIBUTE or 
	ENUMERATED_ATTRIBUTE. */
    public int getType() { 
	return type;
    }

    /** Find the possible stringvalues for this attribute - returns an
        ArrayList of Strings */
    public List getEnumeratedValues() {
	return (List)values;
    }

    /** returns true if the attribute should be serialized */
    public boolean isSerialized() {
	return serialize;
    }

    /** set whether this attribute will be serialized if the corpus is
        saved. This affects both the metadata and the associated NOM
        data. */
    public void setSerialized(boolean bool) {
	serialize=bool;
    }

}

