/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite.impl;

import org.xml.sax.Attributes;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
// import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;

/**
 * NOMAnnotation extends NOMElement and represents an element within an
 * Annotation set. Such elements extend NOMElement so that they can be
 * used in the query language.
 *
 * @author jonathan 
 */
public class NOMWriteAnnotation extends NOMWriteElement implements NOMAnnotation {
    protected NOMWriteAnnotation(NOMCorpus corpus, String name, 
			 Attributes attributes, String colour, NResource resource,
			  boolean stream) throws NOMException {
	super(corpus, name, attributes, colour, resource, stream);
    }

    /** This constructor is called when new elements are added
        programatically. This version takes an ID and colour and
        checks neither! Use with caution */
    protected NOMWriteAnnotation(NOMCorpus corpus, String name, String colour, NResource resource,
			   boolean stream, String id ) throws NOMException {
	super(corpus, name, colour, resource, stream, id);
    }


    /** Create an annotation, explicitly naming the resource to which
        it belongs. IDs generated will be unique with respect to the
        corpus subset that is currently loaded and probably globally
        unique as resource ID should be used. */
    public NOMWriteAnnotation(NOMCorpus corpus, String name,
			   String observation, String agent, NResource resource) throws NOMException {
	super(corpus, name, observation, agent, resource);
    }

    /** This constructor will be the most commonly used in application
        programs. The colour of the element is derived from the
        metadata and the ID is derived from that. IDs generated will
        be unique with respect to the corpus subset that is currently
        loaded. */
    public NOMWriteAnnotation(NOMCorpus corpus, String name,
			   String observation, String agent) throws NOMException {
	super(corpus, name, observation, agent,
	      corpus.selectResourceForCreatedElement(name,observation));
    }

    /** Create an annotation element where the colour of the element
        is derived from the metadata and the ID is provided by the
        caller. */
    public NOMWriteAnnotation(NOMCorpus corpus, String name,
			   String observation, String agent, String id) throws NOMException {
	super(corpus, name, observation, agent, id, 
	      corpus.selectResourceForCreatedElement(name,observation));
    }

    /** Create an annotation element where the colour of the element
        is derived from the metadata and the ID is provided by the
        caller. The resource to which this element belongs is named */
    public NOMWriteAnnotation(NOMCorpus corpus, String name,
			      String observation, String agent, String id, NResource resource) throws NOMException {
	super(corpus, name, observation, agent, id, resource);
    }

    /** Create an annotation element: here, the final argument is an
        NAgent rather than the agent name. */
    public NOMWriteAnnotation(NOMCorpus corpus, String name,
			      String observation, NAgent nagent, NResource resource) throws NOMException {
	super(corpus,name,observation,nagent,resource);
    }

    /** Create an annotation element: here, the NAgent is given rather
     * than the name of the agent, and an ID is provided by the
     * user. */
    public NOMWriteAnnotation(NOMCorpus corpus, String name,
			      String observation, NAgent nagent, String id, NResource resource) throws NOMException {
	super(corpus,name,observation,nagent.getShortName(), id, resource);
    }

    /** Create an annotation element: here, the final argument is an
        NAgent rather than the agent name. */
    public NOMWriteAnnotation(NOMCorpus corpus, String name,
			      String observation, NAgent nagent) throws NOMException {
	super(corpus,name,observation,nagent,
	      corpus.selectResourceForCreatedElement(name,observation));
    }

    /** Create an annotation element: here, the NAgent is given rather
     * than the name of the agent, and an ID is provided by the
     * user. */
    public NOMWriteAnnotation(NOMCorpus corpus, String name,
			      String observation, NAgent nagent, String id) throws NOMException {
	super(corpus,name,observation,nagent.getShortName(), id, 
	      corpus.selectResourceForCreatedElement(name,observation));
    }

    /** This constructor creates a comment element */
    protected NOMWriteAnnotation(NOMCorpus corpus, String comment, String colour, NResource resource) 
     throws NOMException {
	super(corpus, comment, colour, resource);
    }

}
