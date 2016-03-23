/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 
 * This is used to trigger actions which are caused by right mouse clicks. Note
 * that in Windows and Motif, you can tell whether it is a right hand click by
 * querying the popuptrigger mehtod. MacOS users should just specify another key
 * binding to get round this
 * @author Judy Robertson
 */
public class RightMouseListener extends MouseAdapter {
    
    private NiteAction action;
    
    public RightMouseListener(NiteAction a){
        action = a;    
    }
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        // if this click was a trigger for a pop-up menu, manufacture
        // an action performed event and send it along to the
        // NiteAction we need to store this mouse event
	//System.out.println("Gee. I'm maybe showing a popup " + e.isPopupTrigger()); 
        if (e.isPopupTrigger()) {
           ActionEvent ae = new ActionEvent(e, 0, "rightmouseclick");
           action.actionPerformed(ae);
        }
    }
}



