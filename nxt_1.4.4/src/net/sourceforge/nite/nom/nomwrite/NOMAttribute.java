/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite;

import net.sourceforge.nite.nom.link.NOMView;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.NOMElement;

/**
 * Extends the nomread version with methods for editing attributes
 *
 * @author jonathan 
 */
public interface NOMAttribute {
    public static final int NOMATTR_STRING=0;
    public static final int NOMATTR_NUMBER=1;

    /** returns the (q)name of the attribute */
    public String getName();

    /** returns a pre-defined integer value: NOMATTR_STRING or
        NOMATTR_NUMBER. */
    public int getType(); 

    /** get the value of the attribute (if it's a numerical attribute,
     * return the a string conversion). */
    public String getStringValue();

    /** get the numeric value of the attribute (or null if it's a
        string attribute) */
    public Double getDoubleValue();

    /** get the Comparable value of the attribute (for either String
        or Number type attributes) */
    public java.lang.Comparable getComparableValue();

    /** get the Element to which this attribute belongs */
    public NOMElement getElement();

    /*-------------------------------------------------------------*/
    /* WRITE ONLY METHODS. These generally throw an exception if the
     * NOM is in fact read-only. */
    /*-------------------------------------------------------------*/

    /** set the value of the attribute (for a String type attribute
        only) */
    public void setStringValue(String value) throws NOMException;

    /** set the value of the attribute (for a 'Number' type attribute
        only) */
    public void setDoubleValue(Double value) throws NOMException;

    /** set the Comparable value of the attribute (for either String
        or Number type attributes) */
    public void setComparableValue(java.lang.Comparable value) throws NOMException;

    /** return a shared view of this attribute which simply provides
        utility functions for editing the attribute without thinking
        about locking and unlocking the corpus. */
    public SharedAtt getShared();

    /** This inner interface provides utility functions for shared
        NOM users - can be ignored if NOM is not shared amongst
        multiple applications that can write to the NOM. */
    public interface SharedAtt {
	/** set the string value of this attribute - NOM-sharing version */
	public void setStringValue(NOMView view, String value) throws NOMException;
	/** set the numerical value of this attribute - NOM-sharing version */
	public void setDoubleValue(NOMView view, Double value) throws NOMException;
	/** set the comparable value of this attribute - NOM-sharing version*/
	public void setComparableValue(NOMView view, Comparable value) throws NOMException;
    }
    /** set the element to which this attribute belongs - used internally */
    public void setElement(NOMElement element);

} 

