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
 * This action is used to change the textual content of an xml element
 */
public class ChangeTextualContent extends XMLAction{
        
        
       
        
        /**
         * The new text for this element
         */
        String newContent;
        OutputComponent outputComponent;
        
       
        public ChangeTextualContent(JDomParser p, ObjectModelElement e, String c){
                parser = p;
                element = e;
                newContent = c;
                
        }
        
        public ChangeTextualContent(OutputComponent o, ObjectModelElement e, String c){
        	outputComponent = o;
        	element = e;
        	newContent = c;
        }
        

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        System.out.println("Action performed in Change Textual content");
  	
  	element.addContent(getNewContent());
  	
	if (getParser() != null) getParser().redisplayAll();
	if (outputComponent != null) outputComponent.redisplayElement(element);
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

}
