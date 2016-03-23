/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.actions;

import java.util.Set;



/**
 * SwingTarget is an interface for JComponentHandlers from which user
 * input can be gathered
 *
 * @author judyr
 */
public interface InputComponent {
    
    /**
     * Returns a list of object model elements which corresponds to the
     * input currently selected or entered in this JComponentHandler's
     * component. */
    public Set getSelectedObjectModelElements();

}
