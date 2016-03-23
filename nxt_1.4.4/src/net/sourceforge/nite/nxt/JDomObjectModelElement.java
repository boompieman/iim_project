/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nxt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.nite.nstyle.NConstants;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 * This is a wrapper for JDomElement so that it implements the
 * ObjectModelElement interface
 *
 * @author judyr
 * */
public class JDomObjectModelElement implements ObjectModelElement {

    /**
     * Constructor JDomObjectModelElement.
     * @param element
     */
    public JDomObjectModelElement(Element element) {
        this.element = element;
	//	System.out.println("New jdomobjectmodelelement: " + this);
    }

    private Element element;
    private String namespace;
    /** This is the name of the attribute belonging to this element which is displayed onscreen.**/
    private String displayedAttribute;
    private JDomObjectModelElement parent;
    private int index = 0;

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#addAttribute(String, String)
     */
    public void addAttribute(String name, String value) {
    	
        element.setAttribute(name, value);
       
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#deleteElement()
     */
    public void deleteElement() {
        //delete this element from its parent. 
       
        element.getParent().removeContent(element);
    }

    public void addContent(String c) {

        element.setText(c);
    }

    public String getTextualContent() {
        return element.getText();
    }

    public String getAttribute(String aname) {
        return element.getAttributeValue(aname);
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getAttributeValue()
     */
    public String getAttributeValue(String name) {
        return element.getAttributeValue(name);
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getDisplayedAttribute()
     */
    public String getDisplayedAttribute() {
	//	System.out.println("Getting displayed attribute of " + getID() + ": " + displayedAttribute);
        return displayedAttribute;
    }
    /**
     * Sets the displayedAttribute.
     * @param displayedAttribute The displayedAttribute to set
     */
    public void setDisplayedAttribute(String displayedAttribute) {
        this.displayedAttribute = displayedAttribute;
	//	System.out.println("Setting displayed attribute of " + getID() + " to : " + displayedAttribute);
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getID()
     */
    public String getID() {
        return element.getAttributeValue(NConstants.id);
    }

    /**
     * Returns the element.
     * @return Element
     */
    public Element getElement() {
        return element;
    }

    /**
     * Sets the element.
     * @param element The element to set
     */
    public void setElement(Element element) {
        this.element = element;
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#removeChild(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void removeChild(ObjectModelElement e) {
        if (e instanceof JDomObjectModelElement) {
            JDomObjectModelElement je = (JDomObjectModelElement) e;
            element.removeChild(je.element.getName());
        }
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#makeElement()
     */
    public ObjectModelElement makeElement() {
	//	System.out.println("make new jdomobjectmodelelement: " + getID());
        return new JDomObjectModelElement(new Element("user-created element"));
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#addPointer(java.lang.String, net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void addPointer(String role, ObjectModelElement el) {
        System.err.println(
            "Warning: Action add pointer cannot be performed on a JDom document");
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#removePointer(java.lang.String, net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void removePointer(String role, ObjectModelElement e) {
        System.err.println(
            "Warning: Action remove pointer cannot be performed on a JDom document");
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#addChild(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void addChild(ObjectModelElement e, int pos) {
        JDomObjectModelElement je = (JDomObjectModelElement) e;
        List kids = element.getChildren();

        kids.add(pos, je.element);
	
	System.out.println("finished");
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getParent()
     */
    public ObjectModelElement getParent() {
        return parent;
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#setParent(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void setParent(ObjectModelElement e) {
        parent = (JDomObjectModelElement) e;

    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getChildIndex()
     */
    public int getChildIndex() {
        return index;
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#setChildIndex(int)
     */
    public void setChildIndex(int i) {
        index = i;
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getAttributeValuePairs()
     */
    public List getAttributes() {
        return element.getAttributes();

    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getName()
     */
    public String getName() {
        return element.getName();
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getNamespace()
     */
    public String getNamespace() {
        return element.getNamespace().getURI();
    }

    /**
     * Method setNamespace.
     * @param string
     */
    public void setNamespace(String string) {
        element.setNamespace(Namespace.getNamespace(string));
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#addSibling(net.sourceforge.nite.nxt.ObjectModelElement, int)
     */
    public void addSibling(ObjectModelElement e, int pos) {
        //first find the parent, then update the parent's child list with the new element
        Element parent = element.getParent();
        if (parent != null) {

            JDomObjectModelElement je = (JDomObjectModelElement) e;
            List kids = parent.getChildren();
            //getChildren returns a live list, so we don't need to reset the children
            kids.add(pos, je.element);
        }else System.err.println("Warning: attempt to add sibling failed because no parent could be found");

    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getChildren()
     */
    public List getChildren() {
            List objectmodelchildren = new ArrayList();
            List children = element.getChildren();
            Iterator it = children.iterator();
            while(it.hasNext()){
             Object o = it.next();
             //FIX ME - THERE IS A PROBLEM HERE WITH MIXED TEXTUAL CONTENT AND ELEMENT CHILDREN WHICH I AM IGNORING
             if (o instanceof Element){
             	JDomObjectModelElement jdomel = new JDomObjectModelElement((Element) o);    
             	objectmodelchildren.add(jdomel);    
             }       
             
            
            }
            
        return objectmodelchildren;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.nxt.ObjectModelElement#copy()
     */
    public ObjectModelElement copy() {
       JDomObjectModelElement el = new JDomObjectModelElement(this.element);
       el.setDisplayedAttribute(this.getDisplayedAttribute());
       //       System.out.println("copy jdomobjectmodelelement: " + el.getID() + "; display: " + el.getDisplayedAttribute());
       el.setNamespace(this.getNamespace());
       el.setParent(this.getParent());
       return el;
       
    }

}
