/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

import javax.swing.*;

import java.util.Iterator;
import java.util.Vector;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.nom.nomwrite.NOMCorpus;
import net.sourceforge.nite.meta.NObservation;

/** This class is a utility to pop up a dialog that lets the user
 choose whether to save any changes made. Intended use is: <br>
   CheckSave co = new CheckSave(nom); <br>
   if (!(co.popupDialog()==JOptionPane.CANCEL_OPTION)) { <br>
     do the action.... <br>
   }
 */
public class CheckSave extends JDialog {
    NOMCorpus nom;
    String selection=null;
    
    public CheckSave(NOMCorpus nom) {
	this.nom=nom;
	//	popupDialog();
    }
    
    /* Saves the corpus if yes clicked. Returns
       JOptionPane.YES_OPTION, JOtionPane.NO_OPTION or
       JOptionPane.CANCEL_OPTION */
    public int popupDialog() {
	if (!nom.edited()) { return JOptionPane.YES_OPTION; }
	int ret= JOptionPane.showConfirmDialog(null,"Save changes?","Save changes?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	if (ret==JOptionPane.YES_OPTION) {
	    try {
		nom.serializeCorpusChanged();
	    } catch (NOMException nex) {
		nex.printStackTrace();
		return JOptionPane.CANCEL_OPTION;
	    }
	} 
	return ret;
    }

}
