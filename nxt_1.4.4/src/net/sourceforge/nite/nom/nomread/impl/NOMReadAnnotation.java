/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomread.impl;

import org.xml.sax.Attributes;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.meta.*;

/**
 * NOMAnnotation extends NOMElement and represents an individual
 * annotation on the data. Annotatoions do not extend NOMElement in
 * any way, but are contrasted to NOMObjects and NOMTypeElement,
 * though all are types of NOMElement and as such can be treated
 * similarly by the query engine.
 *
 * @author jonathan */
public class NOMReadAnnotation extends NOMReadElement implements NOMAnnotation {

    protected NOMReadAnnotation(NOMCorpus corpus, String name, 
			Attributes attributes, String colour, NResource resource,
			boolean stream) throws NOMException {
	super(corpus, name, attributes, colour, resource, stream);
    }

    /** This constructor is called when new elements are added
        programatically. This version takes an ID and colour and
        checks neither! Use with caution */
    protected NOMReadAnnotation(NOMCorpus corpus, String name, String colour, NResource resource,
			   boolean stream, String id ) throws NOMException {
	super(corpus, name, colour, resource, stream, id);
    }


    /** This constructor will be the most commonly used in application
        programs. The colour of the element is derived from the
        metadata and the ID is derived from that. IDs generated will
        be unique with respect to the corpus subset that is currently
        loaded. */
    public NOMReadAnnotation(NOMCorpus corpus, String name,
			   String observation, String agent) throws NOMException {
	super(corpus, name, observation, agent);
    }

    /** This constructor can also be used in application programs -
        it's identical except that the last argument is an NAgent
        rather than the agent name. */
    public NOMReadAnnotation(NOMCorpus corpus, String name,
			   String observation, NAgent nagent) throws NOMException {
	super(corpus,name,observation,nagent.getShortName());
    }

    /** This constructor creates a comment element */
    public NOMReadAnnotation(NOMCorpus corpus, String comment, String colour, NResource resource) 
     throws NOMException {
	super(corpus, comment, colour, resource);
    }    
}
