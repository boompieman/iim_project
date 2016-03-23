/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import net.sourceforge.nite.gui.actions.NiteAction;

/**
 * Used to trigger actions by key board events
 *
 * @author judyr
 */
public class NiteKeyListener extends KeyAdapter {
    NiteAction action;
    String binding;

    public NiteKeyListener(NiteAction a, String s){
	action = a;
	binding = s;
    }

    /**
     * @return NiteAction
     */
    public NiteAction getAction() {
        return action;
    }

    /**
     * Sets the action.
     * @param action The action to set
     */
    public void setAction(NiteAction action) {
        this.action = action;
    }

    public void keyPressed(KeyEvent e) {
	// Need to test whether the key pressed is the key which we're
	// interestd in, and if so execute the action
	// System.out.println(KeyEvent.getKeyText(e.getKeyCode()));
	if (KeyEvent.getKeyText(e.getKeyCode()).equalsIgnoreCase(binding) ){
	    if (action != null) action.actionPerformed(new ActionEvent(this, 0, "nitekeylistener"));
	}
    }

    /**
     * @return String
     */
    public String getBinding() {
        return binding;
    }

    /**
     * Sets the binding.
     * @param binding The binding to set
     */
    public void setBinding(String binding) {
        this.binding = binding;
    }

}


