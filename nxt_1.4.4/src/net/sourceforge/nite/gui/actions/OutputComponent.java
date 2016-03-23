/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.actions;

import javax.swing.JComponent;

import net.sourceforge.nite.nxt.ObjectModelElement;

/**
 * @author judyr
 *
 * SwingOutputTarget is an interface for JComponentHandlers to which data output can be written
 */
public interface OutputComponent {

    /**
    * @author judyr
    *
    * This interface is used to update display components once a change has been
    * made to the underlying xml which is represented on the user interface
     */
    
    /**
     * Display this element on the user interface
     */
    public JComponent displayElement(ObjectModelElement e, boolean selected);
    /**
     * Used to refresh the display of an element on the screen after changes to
     * textual content or attribute values on underlying xml
     * @param e The element which should have its representation refreshed.
     */
    public void redisplayElement(ObjectModelElement e);
    
    /**
     * Remove the on screen representation of an element once it has been
     * removed from the object model
     * @param e
     */
    public void removeDisplayComponent(ObjectModelElement e);
    
    /**
     * Used to update the display after an addChild operation. Place the new
     * element "after" the positionth child of the parent element. After is
     * easily defined a tree component, but it is far less obvious how this
     * shoud be implemented for text areas.
     * @param newElement The element which is to be inserted
     * @param parent The parent of the element to be added
     * @param position The index into the list of children belonging to the
     * parent 
     */
    public void insertDisplayElement(ObjectModelElement newElement, ObjectModelElement parent, int position);

}
