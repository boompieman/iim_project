/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004, Jean Carletta, Jonathan Kilgour, Natasa Jovanovic, Dennis Reidsma
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import java.util.EventListener;

/**
 * Listener interface to get notification of changes in the selection of an NTextArea.
 * 
 * @author Dennis Reidsma 
 * @author Natasa Jovanovic
 * @author Dennis Hofs
 */
public interface NTASelectionListener extends EventListener {

    /**
     * Called when the selection of the NTextArea is changed. 
     */
    public void selectionChanged();
}