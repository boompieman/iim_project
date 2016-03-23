/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2005, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

import javax.swing.*;

/** This class is a utility to pop up a message dialog. Intended use is: <br>
    PopupMessage pe = new PopupMessage("Hello!");
    pe.popup();
 */
public class PopupMessage extends JDialog {
    String message=null;

    public PopupMessage(String mess) {
	message=mess;
    }

    /** pop up the interface. */
    public void popup() {
	JOptionPane.showMessageDialog(null,message,"Message",JOptionPane.INFORMATION_MESSAGE);
    }
    
}
