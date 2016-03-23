/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.actions;

import net.sourceforge.nite.nxt.ObjectModelElement;

/**
 * @author judyr
 *
 * A super class for the actions which directly edit the xml
 */
public abstract class XMLAction extends NiteAction {

    /**
     * The element which will be edited by this action
     */
    ObjectModelElement element;

    /**
     * Returns the element.
     * @return ObjectModelElement
     */
    public ObjectModelElement getElement() {
        return element;
    }

    /**
     * Sets the element.
     * @param element The element to set
     */
    public void setElement(ObjectModelElement element) {
        this.element = element;
    }

}
