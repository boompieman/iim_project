/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * Information about an Observation. This may be expanded in the
 * future when we wish to allow editing of metadata.
 *
 * @author jonathan 
 */
public interface NObservation {
    /** The identifier for this observation as used in filenames
        etc. Could be something like "q4nc8" for the Map Task. */
    public String getShortName();
    /** returns a description of the observation. */
    public String getDescription(); 
    /** returns a list of strings: these are the values of the
        observation variables in the same order as you get the
        declarations from the getObservationVariables call from the
        metadata. */
    public List getVariables();
    /** return the value of the observation variable for the given
        variable name */
    public String getVariable(String variable);
    /** returns a list of user codings (see NUserCoding). */
    public List getUserCodings(); 
    /** find a user coding for this observation that has coding and
        agent matching. Note that either or both Strings may be null. */
    public NUserCoding findCoding(String codingname, String agentname);
    /** Add a user coding to the corpus. This is an administration
        function that allows a user to start a new observation */
    public void addUserCoding (NUserCoding coding);
    /** Get the DOM node which represents this observation in the tree. 
     *  Only used by methods that edit the DOM. */
    public Node getNode();
    /** set the DOM node which represents this observation in the tree. 
     *  Only used by methods that edit the DOM. */
    public void setNode(Node node);
    /** set the DOM document of which this observation is a part
     *  Only used by methods that edit the DOM. */
    public void setDocument(Document document);
    
}
