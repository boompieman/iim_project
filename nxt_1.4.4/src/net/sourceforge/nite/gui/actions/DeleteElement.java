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
 * This action is used to delete an element from the XML
 */
public class DeleteElement extends XMLAction{

    public DeleteElement(JDomParser p){
	parser =p;
    }
       
    public DeleteElement(JDomParser p, ObjectModelElement el){
	parser = p;
	element = el;
    }
        
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
	//   System.out.println("Action performed in deleteElement");
  	element.deleteElement();
	if (getParser() != null) getParser().redisplayAll();
    }

}
