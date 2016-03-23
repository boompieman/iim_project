/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite;

import org.xml.sax.Attributes;
import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.meta.NResource;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.meta.NObservation;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;

import java.util.List;
import java.util.Iterator;

/** This interface is for internally building a NOM, so has stubs for
 * all the constructors of the NOM that are required to build from
 * files up (i.e. not necessarily all the public constructors) */
public interface NOMMaker {

    public NOMAnnotation make_annotation(NOMCorpus corpus, String name, 
		 Attributes attributes, String colour, NResource resource,
		 boolean stream) throws NOMException;
    public NOMAnnotation make_annotation(NOMCorpus corpus, String name, 
	      String colour, NResource resource, boolean stream, String id) throws NOMException;
    public NOMAnnotation make_annotation(NOMCorpus corpus, String name,
		 String observation, String agent, NResource resource) throws NOMException;
    /** This creates a comment element */
    public NOMAnnotation make_annotation(NOMCorpus corpus, String comment, 
		 String colour,  NResource resource) throws NOMException;

    public NOMObject make_object(NOMCorpus corpus, String name, 
	      Attributes attributes, String colour, NResource resource,
	      boolean stream) throws NOMException;
    public NOMObject make_object(NOMCorpus corpus, String name, 
	      String colour, NResource resource, boolean stream, String id) throws NOMException;

    public NOMResourceElement make_resource_element(NOMCorpus corpus, String name, 
	      Attributes attributes, String colour, NResource resource, 
	      boolean stream) throws NOMException;
    public NOMResourceElement make_resource_element(NOMCorpus corpus, String name, 
	      String colour, NResource resource, boolean stream, String id) throws NOMException;
    /** This creates a comment element */
    public NOMResourceElement make_resource_element(NOMCorpus corpus, String comment, 
	    String colour, NResource resource) throws NOMException;

    public NOMTypeElement make_type_element(NOMCorpus corpus, String name, 
	      Attributes attributes, String colour, NResource resource, 
	      boolean stream) throws NOMException;
    public NOMTypeElement make_type_element(NOMCorpus corpus, String name, 
	    String colour, NResource resource, boolean stream, String id) throws NOMException;


    public NOMAttribute make_attribute(int type, String name, 
	      String string_value, Double double_value);
    /** create a numeric attribute */
    public NOMAttribute make_attribute(String name, Double double_value);
    /** create a string attribute */
    public NOMAttribute make_attribute(String name, String string_value);

    public NOMPointer make_pointer (NOMCorpus corpus, String role, 
					 NOMElement source, String targetstr);
    public NOMPointer make_pointer (NOMCorpus corpus, String role, 
					 NOMElement source, NOMElement target);

}


