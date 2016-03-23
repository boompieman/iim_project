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
 * NOMObject extends NOMElement and represents an element within an
 * object set. Such elements extend NOMElement so that they can be
 * used in the query language. Objects may not have any children. 
 *
 * @author jonathan 
 */
public class NOMWriteObject extends NOMWriteElement implements NOMObject {
    protected NOMWriteObject(NOMCorpus corpus, String name, 
		     Attributes attributes, String colour, NResource resource,
		     boolean stream) throws NOMException {
	super(corpus, name, attributes, colour, resource, stream);
    }

    /** This constructor will be the most commonly used in application
        programs. The colour of the element is derived from the
        metadata and the ID is derived from that. IDs generated will
        be unique with respect to the corpus subset that is currently
        loaded. */
    public NOMWriteObject(NOMCorpus corpus, String name) throws NOMException {
	super(corpus,name,null,(String)null);
    }

    /** This constructor is called when new elements are added
        programatically. This version takes an ID and colour and
        checks neither! Use with caution */
    protected NOMWriteObject(NOMCorpus corpus, String name, String colour, NResource resource,
			   boolean stream, String id ) throws NOMException {
	super(corpus, name, colour, resource, stream, id);
    }

    /* override some methods that have different behaviour on
       Objects. Remember Objects can have no children! */
    private void childException() throws NOMException {
	throw new NOMException("Objects in Object Sets within the NITE Object Model can have no children!");
    }

    /** adds the NOMElement as the first child */
    public void addFirstChild(NOMElement child) throws NOMException {
	if (isStreamElement()) {
	    super.addFirstChild(child);
	} else {
	    childException();
	}
    }

    /** adds the NOMElement as the last child */
    public void addLastChild(NOMElement child) throws NOMException {
	if (isStreamElement()) {
	    super.addLastChild(child);
	} else {
	    childException();
	}
    }

    /** adds the NOMElement newchild immediately before the given
        existing child, or reports an exception if the child cannot be
        found */
    public void addChildBefore(NOMElement oldchild, NOMElement newchild) throws NOMException {
	if (isStreamElement()) {
	    super.addChildBefore(oldchild,newchild);
	} else {
	    childException();
	}
    }

    /** adds the NOMElement newchild immediately after the given
        existing child, or reports an exception if the child cannot be
        found */
    public void addChildAfter(NOMElement oldchild, NOMElement newchild) throws NOMException {
	if (isStreamElement()) {
	    super.addChildAfter(oldchild,newchild);
	} else {
	    childException();
	}
    }

    /** Remove a nite:child from an element without actually deleting
     *  the element referred to. Throw an error if we're trying to
     *  remove the child from its serialized parent.
     */
    public void removeChild(NOMElement child) throws NOMException {
	if (isStreamElement()) {
	    super.removeChild(child);
	} else {
	    childException();
	}
    }

    /** Delete an element. Remove the child from all its
     * parents. Ranges need to be handled separately (in a different
     * thread?)  
     */
    public void deleteChild(NOMElement child) throws NOMException {
	if (isStreamElement()) {
	    super.deleteChild(child);
	} else {
	    childException();
	}
    }

    /** adds a child to this element. Since no order is specified, add
	the child to the end of the list of children. */
    public void addChild(NOMElement child) throws NOMException {
	if (isStreamElement()) {
	    super.addChild(child);
	} else {
	    childException();
	}
    }
    
}
