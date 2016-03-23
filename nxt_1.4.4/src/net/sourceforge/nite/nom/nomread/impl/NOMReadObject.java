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
 * NOMObject extends NOMElement and represents an element within an
 * object set. Such elements extend NOMElement so that they can be
 * used in the query language. Objects may not have any children. 
 *
 * @author jonathan */
public class NOMReadObject extends NOMReadElement implements NOMObject {
    protected NOMReadObject(NOMCorpus corpus, String name, 
		    Attributes attributes, String colour, NResource resource,
		    boolean stream) throws NOMException {
	super(corpus, name, attributes, colour, resource, stream);
    }

    /** This constructor will be the most commonly used in application
        programs. The colour of the element is derived from the
        metadata and the ID is derived from that. IDs generated will
        be unique with respect to the corpus subset that is currently
        loaded. */
    public NOMReadObject(NOMCorpus corpus, String name) throws NOMException {
	super(corpus,name,null,(String)null);
    }

    /** This constructor is called when new elements are added
        programatically. This version takes an ID and colour and
        checks neither! Use with caution */
    protected NOMReadObject(NOMCorpus corpus, String name, String colour, NResource resource,
			   boolean stream, String id ) throws NOMException {
	super(corpus, name, colour, resource, stream, id);
    }
}
