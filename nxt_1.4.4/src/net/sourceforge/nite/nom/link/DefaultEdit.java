/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.link;
import net.sourceforge.nite.nom.nomwrite.*;

/**
 * A DefaultEdit is a single change to the NOM structure
 *
 * @author jonathan
 */
public class DefaultEdit implements NOMEdit {
    /*    These are defined in NOMEdit
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
    */

    NOMElement element;
    int edittype;
    Object object;

    public DefaultEdit (NOMElement element, int edittype, Object object) {
	this.element=element;
	this.edittype=edittype;
	this.object=object;
    }

    /** Returns the type of the edit made. Returns one of
	SET_ATTRIBUTE, ADD_ELEMENT, DELETE_ELEMENT, ADD_CHILD,
	REMOVE_CHILD, ADD_POINTER, EDIT_POINTER, DELETE_POINTER,
	SET_START_TIME, SET_END_TIME or SET_TEXT; */
    public int getType() {
	return edittype;
    }

    /** The element on which the edit has taken place. NOTE: if the
        edit type is DELETE_ELEMENT, this will be the parent element,
        and getDetail will hold the deleted child. If the
        edit type is ADD_ELEMENT, this will be the added element
        itself. */
    public NOMElement getElement() {
	return element;
    }

    /** An Object that holds different things depending on the edit
        type. If the edit type is DELETE_ELEMENT, this contains the
        deleted element. If the edit type is SET_ATTRIBUTE, this holds
        the Attribute. If a pointer has been added or deleted, this is
        the pointer. */
    public Object getObject() {
	return object;
    }


    /** provides a textual description of the edit */
    public String toString() {
	String editstr="";
	switch (edittype) {
	case SET_ATTRIBUTE: editstr="set attribute"; break;
	case  ADD_ELEMENT: editstr="add element"; break; 
	case  DELETE_ELEMENT: editstr="delete element"; break; 
	case  ADD_CHILD: editstr="add child"; break; 
	case  REMOVE_CHILD: editstr="remove child"; break; 
	case  ADD_POINTER: editstr="add pointer"; break; 
	case  EDIT_POINTER: editstr="edit pointer"; break; 
	case  DELETE_POINTER: editstr="delete pointer"; break; 
	case SET_START_TIME: editstr="set start time"; break; 
	case  SET_END_TIME: editstr="set end time"; break;
	case SET_TEXT: editstr="set text"; break; 
	default: editstr="null"; break;
	}
	if (object != null) return  editstr + ": " + object.getClass().getName();
	return editstr;
    }

} 
