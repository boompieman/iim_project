/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.actions;

import java.awt.event.ActionEvent;
import net.sourceforge.nite.nstyle.JDomParser;
import net.sourceforge.nite.nxt.ObjectModelElement;

/**
 * @author judyr
 *
 * This action is used to change the attribute value of an xml element
 */
public class ChangeAttributeValue extends XMLAction { 
    /**
     * The attribute whose value will be changed
     */
    String attributeName;
        
    /**
     * The new text for this element
     */
    String newContent;
        
    OutputComponent outputComponent;
       
    public ChangeAttributeValue(JDomParser p, ObjectModelElement e, String c, String attribute){
	parser = p;
	element = e;
	newContent = c;
	attributeName = attribute;
    }
    
    public ChangeAttributeValue( OutputComponent o, ObjectModelElement e, String c, String attribute){
	outputComponent = o;
	element = e;
	newContent = c;
	attributeName = attribute;
    }
    
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
	//        System.out.println("Action performed in Change Attribute value content");
  	if (getAttributeName() == null){
	    System.err.println("error: an attribute was not specified for the change attribute value action");
  	}
  	element.addAttribute(getAttributeName(), getNewContent());

  	if (getParser() != null) {
	    getParser().redisplayAll();
	} else if (getDisplayer() != null) {
	    getDisplayer().redisplay(element);
	} else if (outputComponent != null) {
	    outputComponent.redisplayElement(element);
	}
    }
    
    /**
     * Returns the newContent.
     * @return String
     */
    public String getNewContent() {
	return newContent;
    }
    
    /**
     * Sets the newContent.
     * @param newContent The newContent to set
     */
    public void setNewContent(String newContent) {
	this.newContent = newContent;
    }
    
    /**
     * Returns the attributeName.
     * @return String
     */
    public String getAttributeName() {
	return attributeName;
    }
    
    /**
     * Sets the attributeName.
     * @param attributeName The attributeName to set
     */
    public void setAttributeName(String attributeName) {
	this.attributeName = attributeName;
    }

}
