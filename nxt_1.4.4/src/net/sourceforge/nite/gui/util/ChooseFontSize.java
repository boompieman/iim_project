/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

import javax.swing.*;

import java.util.Iterator;
import java.util.Vector;

/** This class is a utility to pop up a dialog that lets the user
 choose a font size for the basic text.
 Intended use is: <br>
   ChooseFontSize cfs = new ChooseFontSize(fs); <br>
   fontsize=cfs.popupDialog();
   
 Where fs is the font size to prechoose on the menu.
 What you do with the returned string after that is application specific.
 */
public class ChooseFontSize extends JDialog {
    JList list;
    String selection=null;
    int prechosen;
    
    public ChooseFontSize(int p) {
       prechosen = p;
    }
    
    public String popupDialog() {
	String[] list = setupList();
	Integer pp = new Integer(prechosen);

	/* this puts up the list, preselects the default, and lets the
	 * user choose a new value and then hit OK or cancel
	 */
	Object ret= JOptionPane.showInputDialog(null, 
	            "Choose basic text font size",
	           "Input", JOptionPane.PLAIN_MESSAGE, null, list, pp.toString());
	return (String) ret;
    }

 
    
    private String[] setupList() {
	String[] sizes = new String[20];
	for (int i = 0; i < 10; i++) {
	   Integer jj = new Integer((i+ 2) *4);
	   sizes[i] = (jj.toString());
	}

	return(sizes);

    }
    
}
