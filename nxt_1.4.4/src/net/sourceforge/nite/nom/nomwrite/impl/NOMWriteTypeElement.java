/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite.impl;

import org.xml.sax.Attributes;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.meta.NResource;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
// import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;

/**
 * NOMTypeElement extends NOMElement and represents an element within
 * a type hierarchy or "ontology". Such elements extend NOMElement so
 * that they can be used in the query language (though in general they
 * will not be returned as search results). Type elements only have
 * one attribute (named in the metadata file) which holds the type name.
 *
 * @author jonathan */
public class NOMWriteTypeElement extends NOMWriteElement implements NOMTypeElement {
    protected NOMWriteTypeElement(NOMCorpus corpus, String name, 
			  Attributes attributes, String colour, NResource resource,
			  boolean stream) throws NOMException {
	super(corpus, name, attributes, colour, resource, stream);
    }

    /** This public constructor creates a corpus type (ontology) element */
    public NOMWriteTypeElement(NOMCorpus corpus, String name) throws NOMException {
	super(corpus, name, null, (String)null);
    }

    /** This constructor is called when new elements are added
        programatically. This version takes an ID and colour and
        checks neither! Use with caution */
    protected NOMWriteTypeElement(NOMCorpus corpus, String name, String colour, NResource resource,
			   boolean stream, String id ) throws NOMException {
	super(corpus, name, colour, resource, stream, id);
    }

    /* override some methods that have different behaviour on
       Types. Remember Types can have children but they have to have
       the same element name as the parent, and the children (of
       course) must themselves be type elements. */
    /** throws an exception if the attempt to add a child to a type
        element is invalid */
    private void checkKid(NOMElement child) throws NOMException {
	if (child.isComment()) { return; } 
	if (!getName().equals(meta.getStreamElementName()) && !child.getName().equals(getName())) {
	    NOMException nex = new NOMException("Type elements in NOM ontologies must have the same name: " + getName() + "; " + child.getName() );
	    throw nex;
	}
	if (!(child instanceof NOMWriteTypeElement)) {
	    throw new NOMException("Attempt to add an element to an ontology that is not a type element!");
	}
    }

    /** adds the NOMElement as the first child */
    public void addFirstChild(NOMElement child) throws NOMException {
	checkKid(child);
	super.addFirstChild(child);
    }

    /** adds the NOMElement as the last child */
    public void addLastChild(NOMElement child) throws NOMException {
	checkKid(child);
	super.addLastChild(child);
    }

    /** adds the NOMElement newchild immediately before the given
        existing child, or reports an exception if the child cannot be
        found */
    public void addChildBefore(NOMElement oldchild, NOMElement newchild) throws NOMException {
	checkKid(newchild);
	super.addChildBefore(oldchild, newchild);
    }

    /** adds the NOMElement newchild immediately after the given
        existing child, or reports an exception if the child cannot be
        found */
    public void addChildAfter(NOMElement oldchild, NOMElement newchild) throws NOMException {
	checkKid(newchild);
	super.addChildAfter(oldchild, newchild);
    }

    /** adds a child to this element. Since no order is specified, add
	the child to the end of the list of children. */
    public void addChild(NOMElement child) throws NOMException {
	checkKid(child);
	super.addChild(child);
    }

}
