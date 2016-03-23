/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

import javax.swing.*;

import java.util.Iterator;
import java.util.Vector;

import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.meta.NObservation;

/** This class is a utility to pop up a dialog that lets the user
 select an oblservation from those listed in the metadata
 file. Intended use is: <br>
   ChooseObservation co = new ChooseObservation(metadata); <br>
   observationname=co.popupDialog();
 */
public class ChooseObservation extends JDialog {
    JList list;
    NMetaData meta;
    String selection=null;

    public ChooseObservation(NMetaData metadata) {
	meta=metadata;
	//	popupDialog();
	//	super(parent, true); modal
    }

    /** pop up an interface where the user chooses an
        observtaion. Return the name of the observation. If there is
        only one observation in the corpus, just return it without
        popping up the dialog box. This is slightly more involved than
        it strictly needs to be, but that's because resizability was
        desired (it's useful when there are lots of observations). */
    public String popupDialog() {
	if (meta.getObservations().size()==1) {
	    NObservation nob = (NObservation) meta.getObservations().get(0);
	    return nob.getShortName();
	}
	JList list = setupList();
	JScrollPane jsp = new JScrollPane(list);
	JOptionPane jop = new JOptionPane(jsp, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
	JDialog dialog = jop.createDialog(null, "Choose observation...");
	dialog.setResizable(true);
	dialog.show();
	Object selectedValue = jop.getValue();
	if (!(selectedValue instanceof Integer)) {
	    return null;
	}
	if (((Integer)selectedValue).intValue()==JOptionPane.OK_OPTION) {
	    return (String)list.getSelectedValue();
	} else {
	    return null;
	}
    }
    
    private JList setupList() {
	Vector obnames=new Vector();
	for (Iterator oit=meta.getObservations().iterator(); oit.hasNext(); ) {
	    NObservation nob = (NObservation) oit.next();
	    obnames.add(nob.getShortName());
	}
	return(new JList(obnames));
    }
    
}
