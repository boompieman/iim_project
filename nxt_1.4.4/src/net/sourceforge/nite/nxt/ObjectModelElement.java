/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nxt;

import java.util.List;

/**
 *This is a wrapper for NOM and JDOM elements so that NXT can use 
 * either of these object models. Jonathan, you might want to change this 
 * or put it in a different package at some point
 *
 * @author judyr
 */
public interface ObjectModelElement {

    /**Returns the unique ID of this element**/
    public String getID();

    /**
     * Replace the specified attribute of this element with the specified element
     * Or create the specified attribute if it did not previously exist
     * */
    public void addAttribute(String name, String value);

    /**
     * Replace the current textual content with the specified textual content
     * */
    public void addContent(String content);

   /**
    * Remove the specified child from this element
    */
   public void removeChild(ObjectModelElement e);

    /**
     *  Delete this element
     * */
    public void deleteElement();
    
    /**
     * 
     * Returns a new (empty) object model element. 
     * FIX ME: I reckon we need to
	rethink this. Factory classes might sort this problem out, and also
address the problem of allowing full access to the NOM through this
		 interface
     */
	
    public ObjectModelElement makeElement();
    
    
    public void addPointer(String role, ObjectModelElement el);

	public void removePointer(String role, ObjectModelElement e);

	/**
	 * Returns the textual content of this element
	 * @return String
	 */
    public String getTextualContent();

	

    /**
     * Returns the value of the specified attribute
     */
    public String getAttributeValue(String s);

    /**
     * Returns the name of the attribute which is displayed on-screen by this
     * object model element
     */
    public String getDisplayedAttribute();
    
    /**
     * Keeps a record of which attribute this object displays on-screen
     * @param a
     */
    public void setDisplayedAttribute(String a);
    
    /**
     * Add the specified child element to this at the specified position in the
     * child list
     */
    public void addChild(ObjectModelElement e, int pos);
    
    public void addSibling(ObjectModelElement e, int pos);
    
    public ObjectModelElement getParent();
    
    public void setParent(ObjectModelElement e);
    
    /**
     * Returns the children of this element as a list of ObjectModelElements
     * @return List
     */
    public List getChildren();
    
    /**
     * Returns the index of this element into its parent's child list. 
     * @return int
     */
    public int getChildIndex();
    
    /**
     * Sets the index of this element into its parent's children list
     * @param i
     */
    public void setChildIndex(int i);
    
    /**
     * Returns list of string, string tuples representing the attributes and
     * values of this element
     * @return List
     */
    public java.util.List getAttributes();

    /**
     * Method getName. Returns the name of the element
     * @return String
     */
    public String getName();
    
    /**
     * Returns the namespace which this element is in
     * @return String
     */
    public String getNamespace();
    public void setNamespace(String namespace);
    
    /**
     * Returns a new objectmodelelement which is a copy of this one
     * @return
     */
   public ObjectModelElement copy();

}
