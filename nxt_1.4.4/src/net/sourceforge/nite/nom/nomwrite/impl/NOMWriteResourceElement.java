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
 * NOMWriteResourceElement extends NOMWriteElement and represents an
 * element within a corpus resource. Such elements extend NOMElement
 * so that they can be used in the query language.
 *
 * @author jonathan */
public class NOMWriteResourceElement extends NOMWriteElement implements NOMResourceElement {
    protected NOMWriteResourceElement(NOMCorpus corpus, String name, 
			      Attributes attributes, String colour, NResource resource,
			      boolean stream) throws NOMException {
	super(corpus, name, attributes, colour, resource, stream);
    }

    /** This constructor is called when new elements are added
        internally. This version takes an ID and colour and
        checks neither! Use with caution */
    protected NOMWriteResourceElement(NOMCorpus corpus, String name, String colour, NResource resource,
			   boolean stream, String id ) throws NOMException {
	super(corpus, name, colour, resource, stream, id);
    }


    /** This constructor creates a comment element */
    protected NOMWriteResourceElement(NOMCorpus corpus, String comment, String colour, NResource resource) 
     throws NOMException {
	super(corpus, comment, colour, resource);
    }


    /** This PUBLIC constructor creates a corpus resource element */
    public NOMWriteResourceElement(NOMCorpus corpus, String name) throws NOMException {
	super(corpus, name, null, (String)null);
    }

}
