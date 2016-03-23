/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/** 
 * NAttribute describes an attribute along with its valid values if it is
 * enumerated. 
 */
public interface NAttribute {
    public static final int STRING_ATTRIBUTE=0;
    public static final int NUMBER_ATTRIBUTE=1;
    public static final int ENUMERATED_ATTRIBUTE=2;

    /** returns the name of the attribute */
    public String getName();
    /** returns one of STRING_ATTRIBUTE, NUMBER_ATTRIBUTE or 
	ENUMERATED_ATTRIBUTE. */
    public int getType();
    /** Find the possible stringvalues for this attribute - returns a
        List of Strings */
    public List getEnumeratedValues();

    /** returns true if the attribute should be serialized */
    public boolean isSerialized();

    /** set whether this attribute will be serialized if the corpus is
        saved. This affects both the metadata and the associated NOM
        data. */
    public void setSerialized(boolean bool);

}
