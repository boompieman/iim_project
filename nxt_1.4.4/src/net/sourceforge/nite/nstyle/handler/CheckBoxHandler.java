/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;


import javax.swing.JCheckBox;

import net.sourceforge.nite.gui.actions.NiteAction;

import net.sourceforge.nite.nxt.ObjectModelElement;

/**
 * @author judyr
 *
 * 
 */
public class CheckBoxHandler extends JComponentHandler {

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    //FIX ME - once actions are implemented, set up the event handlers
    protected void createPeer() {
        component = new JCheckBox(content);
        setUpForegroundColour();
        setUpBackgroundColour();
        setUpFont();
        setUpImage();
        setUpToolTip();
        if (image != null)
             ((JCheckBox) component).setIcon(image);

    }
    
   
     public void writeData(String s){
    	throw new RuntimeException("Attempt to write to Checkbox failed");
    }
    
    public ObjectModelElement getSelectedElement(){
    	throw new RuntimeException("Attempt to read from Checkbox failed");
    }

   
}
