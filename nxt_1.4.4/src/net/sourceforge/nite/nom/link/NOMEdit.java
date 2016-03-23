/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.link;
import net.sourceforge.nite.nom.nomwrite.*;

/**
 * A NOMEdit is a single change to the NOM structure. Edits are always
 * taken to be related to a specific NOMElement (which is returned by
 * the getElement() method)
 *
 * @author jonathan 
 */
public interface NOMEdit {
    public static final int SET_ATTRIBUTE=0;
    public static final int ADD_ELEMENT=1;
    public static final int DELETE_ELEMENT=2;
    public static final int ADD_CHILD=3;
    public static final int REMOVE_CHILD=4;
    public static final int ADD_POINTER=5;
    public static final int EDIT_POINTER=6;
    public static final int DELETE_POINTER=7;
    public static final int SET_START_TIME=8;
    public static final int SET_END_TIME=9;
    public static final int SET_TEXT=10;

    /** Returns the type of the edit made. Returns one of
	SET_ATTRIBUTE, ADD_ELEMENT, DELETE_ELEMENT, ADD_CHILD,
	REMOVE_CHILD, ADD_POINTER, EDIT_POINTER, DELETE_POINTER,
	SET_START_TIME, SET_END_TIME or SET_TEXT; */
    public int getType();
    /** The element on which the edit has taken place. This will be
	the parent element of any deleted child or of any added child,
	rather than the newly added or deleted child (which will be
	available via getObject()). For setting attributes this
	returns the element to which the attribute belongs with
	getObject returning a string (the edited attribute name). For
	adding and deleting pointers, this returns the element from
	which the pointer points and getObject() returns the pointer
	that has been added or removed.  */
    public NOMElement getElement();
    /** An Object that holds different things depending on the edit
        type. When Adding / deleting pointers this returns the
        pointer. For setting start and end times this returns
        null. For setting other attributes this returns the attribute
        name. For adding / deleting children, this returns the child */
    public Object getObject();
    /** provides a textual description of the edit */
    public String toString();
} 
