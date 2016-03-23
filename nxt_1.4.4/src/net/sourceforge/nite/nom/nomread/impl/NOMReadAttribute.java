/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomread.impl;

import net.sourceforge.nite.nom.link.NOMView;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteAttribute;

/**
 * Extends the nomread version with methods for editing attributes
 *
 * @author jonathan 
 */
public class NOMReadAttribute extends NOMWriteAttribute {

    /** generic constructor for creating attributes. The first
     * argument is the type of the attribute (NOMATTR_STRING or
     * NOMATTR_NUMBER), the second is the name of the attribute, the
     * third is the string value (or null) and the fourth is the
     * Double value (or null).  */
    public NOMReadAttribute(int type, String name, String string_value,
			    Double double_value) {
	super(type, name, string_value, double_value);
    }

    /** create a string attribute */
    public NOMReadAttribute(String name, String string_value) {
	super(name, string_value);
    }

    /** create a numeric attribute */
    public NOMReadAttribute(String name, Double double_value) {
	super(name, double_value);
    }

    /** set the value of the attribute - for the nomread
     * implementation throw an exception! */
    public void setStringValue(String value) throws NOMException {
	throw new NOMException("NOMRead corpus: cannot set attribute value.");
    }

    /** set the value of the attribute - for the nomread
     * implementation throw an exception!  */
    public void setDoubleValue(Double value) throws NOMException {
	throw new NOMException("NOMRead corpus: cannot set attribute value.");
    }

    /** set the Comparable value of the attribute - for the nomread
     * implementation throw an exception! */
    public void setComparableValue(java.lang.Comparable value) throws NOMException {
	throw new NOMException("NOMRead corpus: cannot set attribute value.");
    }

    /** return a shared view of this attribute which simply provides
        utility functions for editing the attribute without thinking
        about locking and unlocking the corpus. */
    public SharedAtt getShared() {
	return null;
    }
} 

