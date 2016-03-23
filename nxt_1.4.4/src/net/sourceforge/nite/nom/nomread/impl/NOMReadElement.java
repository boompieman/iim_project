/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomread.impl;

import org.xml.sax.Attributes;

import java.io.File;
import java.util.List;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;

/**
 * Extends the nomread version with methods for adding & deleting
 * elements & attributes
 *
 * @author jonathan 
 */
public abstract class NOMReadElement extends NOMWriteElement {

    /** This constructor is used by the process that builds the
        NOM from files: it uses the org.xml.sax.Attributes to code the
        attributes. */
    protected NOMReadElement(NOMCorpus corpus, String name, 
		     Attributes attributes, String colour, NResource resource,
		     boolean stream) throws NOMException {
	super(corpus, name, attributes, colour, resource, stream);
    }


    /** This constructor is called when new elements are added
        programatically. This version takes an ID and colour and
        checks neither! Use with caution */
    protected NOMReadElement(NOMCorpus corpus, String name, String colour, NResource resource,
			   boolean stream, String id ) throws NOMException {
	super(corpus, name, colour, resource, stream, id); 
    }


    /** The colour of the element is derived from the
        metadata and the ID is derived from that. IDs generated will
        be unique with respect to the corpus subset that is currently
        loaded. */
    protected NOMReadElement(NOMCorpus corpus, String name,
			   String observation, String agent) throws NOMException {
	super(corpus, name, observation, agent);
    }


    /** This constructor creates a comment element */
    protected NOMReadElement (NOMCorpus corpus, String comment, String colour, NResource resource) throws NOMException {
	super(corpus, comment, colour, resource);
    }

    /** Set the value of a named string attribute */
    public void setStringAttribute(String name, String value) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.setStringAttribute(name, value); }
	else { throw new NOMException("Read-only corpus: cannot set attribute value."); }
    }

    /** Set the value of a named Double attribute */
    public void setDoubleAttribute(String name, Double value) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.setDoubleAttribute(name, value); }
	else { throw new NOMException("Read-only corpus: cannot set attribute value."); }
    }

    /** add an attribute to the element or if it exists already, just set it */
    public void addAttribute(NOMAttribute attribute) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.addAttribute(attribute); }
	else { throw new NOMException("Read-only corpus: cannot set attribute value."); }
    }

    /** Remove an attribute completely from an element */
    public void removeAttribute(String name) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.removeAttribute(name); }
	else { throw new NOMException("Read-only corpus: cannot remove attribute."); }
    }

    /** add a pointer to the element */
    public void addPointer(NOMPointer pointer) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.addPointer(pointer); }
	else { throw new NOMException("Read-only corpus: cannot add pointer."); }
    }

    /** remove a pointer from the element */
    public void removePointer(NOMPointer pointer) throws NOMException {
	throw new NOMException("Read-only corpus: cannot remove pointer.");
    }

    /** Set the start time of this element. Only elements in a timed
        layer can have their start and end times set directly; all
        structural times are strictly inherited. */
    public void setStartTime(double time) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.setStartTime(time); }
	else { throw new NOMException("Read-only corpus: cannot set element start time."); }
    }

    /** Set the end time of this element. Only elements in a timed
        layer can have their start and end times set directly; all
        structural times are strictly inherited. */
    public void setEndTime(double time) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.setEndTime(time); }
	else { throw new NOMException("Read-only corpus: cannot set element end time."); }
    }

    /** adds the NOMElement as the first child */
    public void addFirstChild(NOMElement child) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.addFirstChild(child); }
	else { throw new NOMException("Read-only corpus: cannot add children to elements"); }
    }

    /** adds the NOMElement as the last child */
    public void addLastChild(NOMElement child) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.addLastChild(child); }
	else { throw new NOMException("Read-only corpus: cannot add children to elements"); }
    }

    /** adds the NOMElement newchild immediately before the given
        existing child, or reports an exception if the child cannot be
        found */
    public void addChildBefore(NOMElement oldchild, NOMElement newchild) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.addChildBefore(oldchild, newchild); }
	else { throw new NOMException("Read-only corpus: cannot add children to elements"); }
    }

    /** adds the NOMElement newchild immediately after the given
        existing child, or reports an exception if the child cannot be
        found */
    public void addChildAfter(NOMElement oldchild, NOMElement newchild) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.addChildAfter(oldchild, newchild); }
	else { throw new NOMException("Read-only corpus: cannot add children to elements"); }
    }

    /** Remove a nite:child from an element without actually deleting
     *  the element referred to. Throw an error if we're trying to
     *  remove the child from its serialized parent. */
    public void removeChild(NOMElement child) throws NOMException {
	throw new NOMException("Read-only corpus: cannot remove children from elements");
    }

    /** Delete an element. Remove the child from all its
     * parents. Ranges need to be handled separately (in a different
     * thread?) */
    public void deleteChild(NOMElement child) throws NOMException {
	throw new NOMException("Read-only corpus: cannot delete elements");
    }

    /** sets the textual content of an element. */
    public void setText(String chars) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.setText(chars); }
	else { throw new NOMException("Read-only corpus: cannot edit elements"); }
    }

    /** appends to the textual content of an element. */
    public void appendText(String chars) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.appendText(chars); }
	else { throw new NOMException("Read-only corpus: cannot edit elements"); }
    }

    /** sets the name of the element - not commonly used! */
    public void setName(String name) {
	//throw new NOMException("Read-only corpus: cannot edit elements");
    }

    /** sets the Graphical Visual Markup string for this element */
    public void setGVM(String name) {

    }

    /** add an element to the corpus with no information about
        location. This should only be used with elements that are in
        the top level of a coding. If the element is in a timed layer
        and has start and end times, the placement of the addition in
        the "stream" is derived from those. Otherwise the element is
        added at the end of the stream. */
    public void addToCorpus() throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.addToCorpus(); }
	else { throw new NOMException("Read-only corpus: cannot add elements"); }
    }

    /** add the element as a sibling of the given element. Of course
        the siblings must be in the same layer and the ordering must
        not violate any of the precedence constraints */
    public void addBeforeSibling(NOMElement sibling) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.addBeforeSibling(sibling); }
	else { throw new NOMException("Read-only corpus: cannot add elements"); }
    }

    /** add the element as a sibling of the given element. Of course
        the siblings must be in the same layer and the ordering must
        not violate any of the precedence constraints */
    public void addAfterSibling(NOMElement sibling) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.addAfterSibling(sibling); }
	else { throw new NOMException("Read-only corpus: cannot add elements"); }
    }

    /** adds a child to this element. Since no order is specified, add
	the child to the end of the list of children. */
    public void addChild(NOMElement child) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.addChild(child); }
	else { throw new NOMException("Read-only corpus: cannot add elements"); }
    }

    /** return a shared view of this element which simply provides
        utility functions for editing the element without thinking
        about locking and unlocking the corpus. */
    public SharedEl getShared() {
	return null;
    }

    /** set the contents of the reserved comment attribute */
    public void setComment(String comment) {

    }

    /** returns a string containing a full Link to this element
        including filename - we override the nomwrite version to add
        coder-specific path in case we're in reliability mode. */
    public String getLink() {
	NOMReadCorpus c = (NOMReadCorpus)corpus;
	if (c.reliabilityMode && c.attname!=null && getAttribute(c.attname)!=null) { 
	    String cr = (String) getAttributeComparableValue(c.attname);
	    return cr + File.separator + super.getLink();
	} else {
	    return super.getLink();
	}
    }

} 
