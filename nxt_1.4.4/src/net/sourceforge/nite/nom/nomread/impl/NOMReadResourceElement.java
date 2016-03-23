/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomread.impl;

import org.xml.sax.Attributes;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.meta.NResource;

/**
 * NOMResourceElement extends NOMElement and represents an individual
 * element in a corpus resource. These do not extend NOMElement in
 * any way, but are contrasted to NOMObjects and NOMTypeElement,
 * though all are types of NOMElement and as such can be treated
 * similarly by the query engine.
 *
 * @author jonathan */
public class NOMReadResourceElement extends NOMReadElement implements NOMResourceElement {
    protected NOMReadResourceElement(NOMCorpus corpus, String name, 
			     Attributes attributes, String colour, NResource resource,
			     boolean stream) throws NOMException {
	super(corpus, name, attributes, colour, resource, stream);
    }

    /** This constructor will be the most commonly used in application
        programs. The colour of the element is derived from the
        metadata and the ID is derived from that. IDs generated will
        be unique with respect to the corpus subset that is currently
        loaded. */
    public NOMReadResourceElement(NOMCorpus corpus, String name) throws NOMException {
	super(corpus,name,null,(String)null);
    }

    /** This constructor is called when new elements are added
        programatically. This version takes an ID and colour and
        checks neither! Use with caution */
    protected NOMReadResourceElement(NOMCorpus corpus, String name, String colour, NResource resource,
			   boolean stream, String id ) throws NOMException {
	super(corpus, name, colour, resource, stream, id);
    }    

    /** This constructor creates a comment element */
    public NOMReadResourceElement(NOMCorpus corpus, String comment, String colour, NResource resource) 
     throws NOMException {
	super(corpus, comment, colour, resource);
    }

}
