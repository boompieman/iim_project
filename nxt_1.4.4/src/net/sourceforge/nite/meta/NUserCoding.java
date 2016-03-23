/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import org.w3c.dom.Node;

/**
 * Information about a coding performed by a user. User codings are keyed 
 * on observation / coding / agent
 *
 * @author jonathan 
 */
public interface NUserCoding {
    public static final int CODING_UNSTARTED=0;
    public static final int CODING_DRAFT=1;
    public static final int CODING_FINISHED=2;
    public static final int CODING_CHECKED=3;

    /** get the name of the coding - this refers to an NCoding */
    public String getCodingName();
    /** get the name of the agent - this refers to an NAgent */
    public String getAgentName();
    /** returns the name of the coder */
    public String getCoder(); 
    /** returns the name of the checker */
    public String getChecker(); 
    /** returns the date in a string form */
    public String getDate(); 
    /** one of the four ints defined here: CODING _UNSTARTED, CODING_DRAFT, 
     *  CODING_FINISHED or CODING_CHECKED. */
    public int getStatus(); 
    /** Get the DOM node which represents this coding in the tree. 
     *  Only used by methods that edit the DOM. */
    public Node getNode(); 
    /** set the DOM node which represents this coding in the tree. 
     *  Only used by methods that edit the DOM. */
    public void setNode(Node node);

    /** set the name of the coder */
    public void setCoder(String coder); 
    /** set the name of the checker */
    public void setChecker(String checker); 
    /** set the date */
    public void setDate(String date); 
    /** set the status */
    public void setStatus(int status); 
}


