/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import org.w3c.dom.*;


/**
 * Information about a coding performed by a user. 
 *
 * @author jonathan 
 */
public class NiteUserCoding implements net.sourceforge.nite.meta.NUserCoding {
    private String coding_name=null;
    private String agent=null;
    private String coder=null;
    private String checker=null;
    private String date=null;
    private int status=CODING_UNSTARTED;
    private Node node=null;

    /*
    public NiteUserCoding(String coding_name) {
	this.coding_name=coding_name;
    }
    */
    
    /* This constructor is called when a new a new user coding is to
       be added to an existing observation programatically. It should
       then be added to the metadata via the NiteObservation.addUserCoding
       method. */
    public NiteUserCoding(String coding_name, String agent, String coder, 
			  String checker, int status, String date) {
	this.coding_name=coding_name;
	this.agent=agent;
	this.coder=coder;
	this.checker=checker;
	this.date=date;
	if ((status==CODING_UNSTARTED) || (status==CODING_DRAFT) || 
	    (status==CODING_FINISHED) || (status==CODING_CHECKED)) {
	    this.status=status;
	} else {
	    System.err.println("WARNING: attempted to set coding status to an invalid integer: " + status);
	}
    }

    /** This version is called when building the UserCodings from an
        already-loaded metadata file (as opposed to adding a new user
        coding). The constructor takes the arguments required to
        uniquely identify this user coding. Also passed is the Node
        (in the in-memory XML version of the metadata file) which may
        be directly altered so that changes are serialized */
    public NiteUserCoding(String coding_name, String agent, String coder, 
			  String checker, int status, String date, Node node) {
	this.coding_name=coding_name;
	this.agent=agent;
	this.coder=coder;
	this.checker=checker;
	this.date=date;
	this.node=node;
	if ((status==CODING_UNSTARTED) || (status==CODING_DRAFT) || 
	    (status==CODING_FINISHED) || (status==CODING_CHECKED)) {
	    this.status=status;
	} else {
	    System.err.println("WARNING: attempted to set coding status to an invalid integer: " + status);
	}
    }

    /** get the name of the agent - this refers to a NAgent */
    public String getAgentName() {
	return agent;
    }

    /** get the name of the coding - this refers to a NLayer */
    public String getCodingName() {
	return coding_name;
    }

    /** The name of the coder */
    public String getCoder() {
	return coder;
    }

    /** The name of the checker */
    public String getChecker() {
	return checker;
    }

    /** The date */
    public String getDate() {
	return date;
    }

    /** one of the four ints defined here: CODING_UNSTARTED, CODING_DRAFT, 
     *  CODING_FINISHED or CODING_CHECKED. */
    public int getStatus() {
	return status;
    }

    /** set the name of the coder */
    public void setCoder(String coder) {
	if (node!=null) {
	    ((Element) node).setAttribute(NiteMetaConstants.coder, coder);
	} 
	this.coder=coder;
    }

    /** set the name of the checker */
    public void setChecker(String checker) {
	if (node!=null) {
	    ((Element) node).setAttribute(NiteMetaConstants.checker, checker);
	} 
	this.checker=checker;
    }

    /** set the date */
    public void setDate(String date) {
	if (node!=null) {
	    ((Element) node).setAttribute(NiteMetaConstants.date, date);
	} 
 	this.date=date;
    }

    /** set the status */
    public void setStatus(int status) {
	if ((status==CODING_UNSTARTED) || (status==CODING_DRAFT) || 
	    (status==CODING_FINISHED) || (status==CODING_CHECKED)) {
	    this.status=status;
	    if (node!=null) {
		String statstring=NiteMetaConstants.statusUnstarted;
		if (status==CODING_DRAFT) { statstring=NiteMetaConstants.statusDraft; }
		else if (status==CODING_FINISHED) { statstring=NiteMetaConstants.statusFinished; }
		else if (status==CODING_CHECKED) { statstring=NiteMetaConstants.statusChecked; }
		((Element) node).setAttribute(NiteMetaConstants.status, statstring);
	    } else {
		System.out.println("Setting status but node is null!");
	    }
	} else {
	    System.err.println("WARNING: attempted to set coding status to an invalid integer: " + status);
	}
    }

    /** Get the DOM node which represents this coding in the tree. 
     *  Only used by methods that edit the DOM. */
    public Node getNode() {
	return node;
    }

    /** set the DOM node which represents this coding in the tree. 
     *  Only used by methods that edit the DOM. */
    public void setNode(Node node) {
	this.node=node;
    }

}
