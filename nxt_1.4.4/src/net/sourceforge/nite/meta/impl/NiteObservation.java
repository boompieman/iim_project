/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;


import net.sourceforge.nite.meta.NUserCoding;
import net.sourceforge.nite.meta.NAttribute;
import net.sourceforge.nite.util.Debug;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.*;

/** 
 * An observation as referred to in the metadata
 *
 * @author jonathan
 */

public class NiteObservation implements net.sourceforge.nite.meta.NObservation {
    private String name;
    private String description;
    private ArrayList variables=null;
    private ArrayList user_codings=null;
    private Document document=null;
    private Node node=null;
    private NiteMetaData metadata=null;
    
    /** This constructor is for users adding observations
	programatically with no observation variables or user
	codings. Once the Observatoion has been created, don't forget
	to add it to the metadata file using NiteMetaData.addObservation.  
    */
    public NiteObservation (NiteMetaData meta, String name, String description) {
	this.metadata=meta;
	this.name=name;
	this.description=description;
	/*	node=(Node)new DOMElement("observation");
	node.setAttribute(NiteMetaConstants.objectName, name);
	node.setAttribute(NiteMetaConstants.description, description); */
    }


   /** Create an observation with observation variables and user
	codings. This constructor is used for adding observations
	programatically.  Once the Observatoion has been created,
	don't forget to add it to the metadata file using
	NiteMetaData.addObservation.  Variables are assumed to be a
	List of Strings in the same order as the variables are
	returned from the getObservationVariables call of
	NiteMetaData. Warnings will be emitted if they don't conform
	in number and type (though only if validation is switched
	on). User codings are passed as a List of ready-made
	NiteUserCodings. */
    public NiteObservation (NiteMetaData meta, String name, String description, 
			    List vars, List user_codings ) {
	this.metadata=meta;
	this.name=name;
	this.description=description;
	this.user_codings=(ArrayList)user_codings;
	/* handle variables and do some type checking if validation is on */
	if (metadata==null) { variables=(ArrayList)vars; }
	if (!metadata.isValidating()) { variables=(ArrayList)vars; }
	else { variables=validateVariables(vars); }
    }

    /** Create an observation with no observation variables or user
	codings. This constructor is used internally for building up
	the metadata structure from an existing file. The node that
	represents this observation in the in-memory XML version of
	the metadata file is passed as an argument. */
    public NiteObservation (NiteMetaData meta, String name, String description, 
			    Document doc, Node node) {
	this.metadata=meta;
	this.name=name;
	this.description=description;
	this.document=doc;
	this.node=node;
    }

    /** Create an observation with observation variables and user
	codings. This constructor is used internally for building up
	the metadata structure from an existing file. Variables are
	assumed to be a List of Strings in the same order as the
	variables are returned from the getObservationVariables call
	of NiteMetaData! Warnings will be emitted if they don't
	conform in number and type (though only if validation is
	switched on). User codings are passed as a List of ready-made
	NiteUserCodings. */
    public NiteObservation (NiteMetaData meta, String name, String description, 
			    List vars, List user_codings, Document doc, 
			    Node node) {
	this.metadata=meta;
	this.name=name;
	this.description=description;
	this.user_codings=(ArrayList)user_codings;
	this.document=doc;
	this.node=node;

	/* handle variables and do some type checking if validation is on */
	if (metadata==null) { variables=(ArrayList)vars; }
	if (!metadata.isValidating()) { variables=(ArrayList)vars; }
	else { variables=validateVariables(vars); }
    }

    private ArrayList validateVariables(List vars) {
	ArrayList variables=new ArrayList();
	if (metadata==null) { return null; }
	List obsvars=metadata.getObservationVariables();
	if (obsvars!=null && vars!=null) {
	    Iterator oit=vars.iterator();
	    Iterator vit=obsvars.iterator();
	    while (vit.hasNext()) {
		NAttribute nat = (NAttribute) vit.next();
		String value=null;
		if (oit.hasNext()) { value=(String)oit.next();	}
		if (okValue(nat, value)) {
		    variables.add((Object)value);
		} else {
		    if (value==null) {
			Debug.print("WARNING: Observation variable " + nat.getName() + " is undefined on observation " + name + ".", Debug.WARNING);
		    } else {
			Debug.print("WARNING: Ignoring observation variable " + nat.getName() + " on observation " + name + ". Value (" + value + ") does not match declared type.", Debug.WARNING);
		    }
		    variables.add((Object)null);
		}
	    }
	}
	return variables;
    }

    /** The identifier for this observation as used in filenames
        etc. Could be something like "q4nc8" for the Map Task. */
    public String getShortName() {
	return name;
    }

    /** returns a description of the observation. */
    public String getDescription() {
	return description;
    }

    /** returns a list of strings: these are the values of the
        observation variables in the same order as you get the
        declarations from the getObservationVariables call from the
        metadata. */
    public List getVariables() {
	return variables;
    }
    
    /** return the value of the observation variable for the given
        variable name */
    public String getVariable(String variable) {
	if (metadata==null) { return null; }
	if (variables==null) { return null; }
	List obs = metadata.getObservationVariables();
	if (obs==null) { return null; }
	Iterator vit=variables.iterator();
	for (Iterator oit=obs.iterator(); oit.hasNext(); ) {
	    if (!vit.hasNext()) { return null; }
	    String value=(String)vit.next();
	    NAttribute nat=(NAttribute)oit.next();
	    if (variable.equals(nat.getName())) {
		return value;
	    }
	}
	return null;
    }

    /** returns a list of user codings (see NiteUserCoding). */
    public List getUserCodings() {
	return user_codings;
    }

    /** find a user coding for this observation that has coding and
        agent matching. Note that either or both Strings may be null. */
    public NUserCoding findCoding(String codingname, String agentname) {
	if (user_codings==null) { return null; }
	Iterator ucit = user_codings.iterator();
	while (ucit.hasNext()) {
	    NUserCoding nuc = (NUserCoding) ucit.next();
	    if (agentname != null) {
		if (!nuc.getAgentName().equalsIgnoreCase(agentname)) {
		    continue;
		}
	    }
	    if (codingname != null) {
		if (!nuc.getCodingName().equalsIgnoreCase(codingname)) {
		    continue;
		}
	    }
	    // notice that if both coding and agent are null, the
	    // first coding is returned.
	    return nuc; 
	}
	return null;
    }

    /** Add a user coding to the corpus. This is an administration
        function that allows a user to start a new user coding for this
        observation */
    public void addUserCoding (NUserCoding coding) {
	if (user_codings==null) {
	    user_codings=new ArrayList();
	}
	user_codings.add((Object)coding);
	if (node!=null && document!=null) {
	    try{
		NodeList nl = ((Element) node).getChildNodes();
		Element ch = null;
		for (int i=0; i<nl.getLength(); i++) {
		    if (nl.item(i).getNodeType()==Node.ELEMENT_NODE) {
			ch=(Element)nl.item(i);
			break;
		    }
		}

		if (ch==null || !ch.getNodeName().equals(NiteMetaConstants.codings_element)) {
		    ch=document.createElement(NiteMetaConstants.codings_element);
		    ((Element) node).appendChild(ch);
		}

		Element codenode = (Element)coding.getNode();
		if (codenode==null) {
		    codenode=document.createElement(NiteMetaConstants.coding_element);
		    codenode.setAttribute(NiteMetaConstants.objectName, coding.getCodingName());
		    codenode.setAttribute(NiteMetaConstants.coder, coding.getCoder());
		    codenode.setAttribute(NiteMetaConstants.date, coding.getDate());
		    if (coding.getAgentName()!=null && !coding.getAgentName().equals("")) {
			codenode.setAttribute(NiteMetaConstants.agent, coding.getAgentName());
		    }
		    if (coding.getChecker()!=null && !coding.getChecker().equals("")) {
			codenode.setAttribute(NiteMetaConstants.checker, coding.getChecker());
		    }
		    coding.setNode((Node)codenode);
		    coding.setStatus(coding.getStatus());
		}
		ch.appendChild(codenode);
	    } catch (DOMException dex) {
		dex.printStackTrace();
	    }
	}
    }

    /** Get the DOM node which represents this observation in the tree. 
     *  Only used by methods that edit the DOM. */
    public Node getNode() {
	return node;
    }

    /** set the DOM node which represents this observation in the tree. 
     *  Only used by methods that edit the DOM. */
    public void setNode(Node node) {
	this.node=node;
    }
    
    /** set the DOM document of which  this observation is a part
     *  Only used by methods that edit the DOM. */
    public void setDocument(Document document) {
	this.document=document;
    }
    

    /** Find the named attribute in a list of them or return null */
    private boolean okValue (NAttribute nat, String value) {

	if (value==null) { return false; }
	if (nat.getType()==NAttribute.STRING_ATTRIBUTE) {
	    return true;
	} else if (nat.getType()==NAttribute.ENUMERATED_ATTRIBUTE) {
	    for (Iterator nit=nat.getEnumeratedValues().iterator(); nit.hasNext(); ) {
		String val=(String)nit.next();
		if (val.equals(value)) {
		    return true;
		}
	    }
	    return false;
	} else if (nat.getType()==NAttribute.NUMBER_ATTRIBUTE) {
	    try {
		Double num = Double.valueOf(value);
	    } catch (NumberFormatException ex) {
		return false;
	    }
	    return true;
	}
	return false;
    }

}

