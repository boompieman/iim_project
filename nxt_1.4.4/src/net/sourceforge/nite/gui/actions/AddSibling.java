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
 * This action is used to insert a sibling to an xml element
 */
public class AddSibling extends XMLAction{
        
        
       
        
        /*
         * The element which will be added
         */
         ObjectModelElement newElement;
        /**
         *The index to where in the child list the new child should be inserted
         */
       int index;
        
       
        public AddSibling(JDomParser p, ObjectModelElement old, ObjectModelElement newEl,  int i){
                parser = p;;
                element = old;
                this.newElement = newEl;
                index = i;
                
        }
        

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        System.out.println("Action performed in AddSibling");
  	element.addSibling(newElement, index);
    if (getParser() != null) getParser().redisplayAll();
  	
    }

        

        /**
         * Returns the index.
         * @return int
         */
        public int getIndex() {
            return index;
        }

        /**
         * Sets the index.
         * @param index The index to set
         */
        public void setIndex(int index) {
            this.index = index;
        }

}
