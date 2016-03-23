/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite.impl;

import java.lang.Comparable;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.link.*;

/**
 * A simple implementation of an attribute in a NOM corpus allowing
 * for either numerical or string attributes and teh standard get and
 * set methods.
 * @author jonathan 
 */
public class NOMWriteAttribute implements NOMAttribute {
    private String name=null;
    private int type=NOMATTR_STRING;
    private String string_value=null;
    private Double number_value=null;
    private NOMElement element=null; // the element we belong to
    private NOMCorpus corpus=null; // the corpus we belong to

    /** generic constructor for creating attributes. The first
     * argument is the type of the attribute (NOMATTR_STRING or
     * NOMATTR_NUMBER), the second is the na,e of the attribute, the
     * third is the string value (or null) and the fourth is the
     * Double value (or null).  */
    public NOMWriteAttribute(int type, String name, String string_value,
			    Double double_value) {
	this.type=type;
	this.name=name;
	this.string_value=string_value;
	this.number_value=double_value;
    }

    /** create a string attribute */
    public NOMWriteAttribute(String name, String string_value) {
	this.type=NOMATTR_STRING;
	this.name=name;
	this.string_value=string_value;
    }

    /** create a numeric attribute */
    public NOMWriteAttribute(String name, Double double_value) {
	this.type=NOMATTR_NUMBER;
	this.name=name;
	this.number_value=double_value;
	if (double_value!=null) {  string_value=double_value.toString(); }
    }

    /** set the value of the attribute (for a String type attribute
        only) */
    public void setStringValue(String value) throws NOMException {
	if (element!=null) {
	    element.setStringAttribute(name, value);
	} else {
	    string_value=value;
	}
    }

    /** set the value of the attribute (for a 'Number' type attribute
        only) */
    public void setDoubleValue(Double value) throws NOMException {
	if (element!=null) {
	    element.setDoubleAttribute(name, value);
	} else {
	    number_value=value;
	}
    }

    /** get the value of the attribute (for a String type attribute
        only) */
    public String getStringValue() {
	if (type==NOMATTR_STRING) {
	    return string_value;
	} else {
	    if (number_value!=null) {
		String full = number_value.toString();
		return full;
	    }
	}
	return null;
    }

    /** get the value of the attribute. This is mainly for a 'number' type attribute
        but if we call it on a string we'll try to convert */
    public Double getDoubleValue() {
	if (number_value!=null) 
	    return number_value;
	if (string_value!=null) {
	    try { number_value = new Double(string_value); } 
	    catch (NumberFormatException ex) { number_value=null; }
	}
	return number_value;
    }

    /** get the Comparable value of the attribute (for either String
        or Number type attributes) */
    public java.lang.Comparable getComparableValue() {
	if (type==NOMATTR_STRING) {
	    return string_value;
	} else {
	    return number_value;
	}	
    }

    /** set the Comparable value of the attribute (for either String
        or Number type attributes) */
    public void setComparableValue(java.lang.Comparable value) throws NOMException {
	if (element!=null) {
	    if (type==NOMATTR_STRING) {
		element.setStringAttribute(name, (String)value);
	    } else {
		element.setDoubleAttribute(name, (Double)value);
	    }
	} else {
	    setComparableValueUnnotified(value);
	}
    }

    /** returns the (q)name of the attribute */
    public String getName() {
	return name;
    }

    /** returns a pre-defined integer value: NOMATTR_STRING or
        NOMATTR_NUMBER. */
    public int getType() {
	return type;
    }

    /** get the Element to which this attribute belongs */
    public NOMElement getElement() {
	return element;
    }

    /** set the element to which this attribute belongs */
    public void setElement(NOMElement element) {
	this.element = element;
	if (element!=null) {
	    corpus = element.getCorpus();
	}
    }

    /*----------------------------------------------------------------*
      Protected methods that are called when attribute edits are done
      by the containing NOMElement - this means we won't get multiple
      notifications for a single edit.
     *----------------------------------------------------------------*/

    /** set the value of the attribute (for a String type attribute
        only) - this version does not notify any NOMViews of the
        change. */
    protected void setStringValueUnnotified(String value) {
	string_value = (String)value;	
    }

    /** set the value of the attribute (for a 'Number' type attribute
        only)  - this version does not notify any NOMViews of the
        change. */
    protected void setDoubleValueUnnotified(Double value) {
	number_value=value;
    }

    /** set the Comparable value of the attribute (for either String
        or Number type attributes) - this version does not notify any
        NOMViews of the change. */
    protected void setComparableValueUnnotified(java.lang.Comparable value) {
	if (type==NOMATTR_STRING) {
	    string_value = (String)value;
	} else {
	    number_value = (Double)value;
	}
    }

    /** return a shared view of this attribute which simply provides
        utility functions for editing the attribute without thinking
        about locking and unlocking the corpus. */
    public SharedAtt getShared() {
	return new SharedAttribute(this);
    }
    
    /** This inner class provides a utility functions for shared
        NOM users */
    public class SharedAttribute implements NOMAttribute.SharedAtt {
	NOMWriteAttribute nwa;
	public SharedAttribute (NOMWriteAttribute nwa) {
	    this.nwa=nwa;
	}

	/** set the string value of this attribute - NOM-sharing version */
	public void setStringValue(NOMView view, String value) throws NOMException {
	    if (nwa.getElement().getCorpus().lock(view)) {
		nwa.setStringValue(value);
		nwa.getElement().getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }
	}

	/** set the numerical value of this attribute - NOM-sharing version */
	public void setDoubleValue(NOMView view, Double value) throws NOMException {
	    if (nwa.getElement().getCorpus().lock(view)) {
		nwa.setDoubleValue(value);
		nwa.getElement().getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }
	}

	/** set the comparable value of this attribute - NOM-sharing version*/
	public void setComparableValue(NOMView view, Comparable value) throws NOMException {
	    if (nwa.getElement().getCorpus().lock(view)) {
		nwa.setComparableValue(value);
		nwa.getElement().getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }
	}

    }

}
