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
 * This is used to trigger actions which are caused by left mouse clicks. 
 * @author Judy Robertson
 */
public class LeftMouseListener extends MouseAdapter {
    
    private NiteAction action;
    
    public LeftMouseListener(NiteAction a){
        action = a;    
    }
    public void mousePressed(MouseEvent e) {
        showPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        showPopup(e);
    }

    private void showPopup(MouseEvent e) {
	ActionEvent ae = new ActionEvent(e, 0, "leftmouseclick");
	action.actionPerformed(ae);
    }
}



