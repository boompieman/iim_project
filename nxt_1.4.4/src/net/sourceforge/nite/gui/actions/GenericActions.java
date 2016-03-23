package net.sourceforge.nite.gui.actions;

import java.io.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;

import javax.swing.event.*;
import javax.swing.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.util.*;

/**
 * A bunch of useful actions.
 * Uses, if possible and if not overridden from another source, the Eclipse icon set (<a href="http://www.eclipse.org">http://www.eclipse.org</a>).
 */
public class GenericActions {
    
    /**
     * Returns a save-action for the given corpus.
     */
    public static Action getSaveAction(NOMWriteCorpus nom) {
        return new SaveAction(nom);
    }

    /**
     * Returns a exit-action for the given corpus, which includes a 'save changed' check
     */
    public static Action getExitAction(NOMWriteCorpus nom) {
        return new ExitAction(nom);
    }

    /**
     * Returns a print-action for the given NTV
     */
    public static Action getPrintAction(NTranscriptionView ntv) {
        return new PrintAction(ntv);
    }

} 


class PrintAction extends AbstractAction {
    protected NTranscriptionView ntv;
    public PrintAction(NTranscriptionView ntv) {
        super("Print transcription area");
        this.ntv = ntv;
        if (getClass().getResource("/eclipseicons/etool16/print_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/print_edit.gif"));
            putValue(Action.SMALL_ICON, value);
        }
        //putValue(Action.ACCELERATOR_KEY,"ctrl typed p"); hm. doesn't work yet?
        putValue(Action.SHORT_DESCRIPTION,"Print the text from the transcription area, including the annotation markup.");
        
    }
    public void actionPerformed(ActionEvent ev) {
        ntv.printTextArea();
    }
}
class SaveAction extends AbstractAction {
    protected NOMWriteCorpus nom;
    public SaveAction(NOMWriteCorpus nom) {
        super("Save corpus");
        this.nom = nom;
        if (getClass().getResource("/eclipseicons/etool16/save_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/save_edit.gif"));
            putValue(Action.SMALL_ICON, value);
        }
        //putValue(Action.ACCELERATOR_KEY,"ctrl typed s"); hm. doesn't work yet?
        putValue(Action.SHORT_DESCRIPTION,"Save the current corpus.");
        
    }
    public void actionPerformed(ActionEvent ev) {
        try {
            nom.getMetaData().writeMetaData(nom.getMetaData().getFilename());
            nom.serializeCorpusChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class ExitAction extends AbstractAction {
    protected NOMWriteCorpus nom;
    public ExitAction(NOMWriteCorpus nom) {
        super("Exit");
        this.nom = nom;
        if (getClass().getResource("/eclipseicons/elcl16/close_view.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/elcl16/close_view.gif"));
            putValue(Action.SMALL_ICON, value);
        }
        putValue(Action.SHORT_DESCRIPTION,"Exit application after checking whether the corpus needs to be saved.");
        
    }
    public void actionPerformed(ActionEvent ev) {
        CheckSave co = new CheckSave(nom); 
        if (!(co.popupDialog()==JOptionPane.CANCEL_OPTION)) { 
            System.exit(0);
        }
    }
}

