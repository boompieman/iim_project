/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JLabel;


import net.sourceforge.nite.gui.actions.OutputComponent;
import net.sourceforge.nite.nxt.ObjectModelElement;

/**
 * @author judyr
 *
 * 
 */
public class LabelHandler extends JComponentHandler implements OutputComponent{

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
        component = new JLabel(content);
	setUpForegroundColour();
        setUpBackgroundColour() ;
        setUpFont();
        setUpImage();
        setUpToolTip();
        if (image != null)
	    ((JLabel) component).setIcon(image);
	if (getElement() != null) displayElement(getElement(), false);
    }
    
    public void setElement(ObjectModelElement e){
        super.setElement(e);
        displayElement(getElement(), false);
    }
    
    
    //FIX ME - IMPLEMENT THESE METHODS

    /**
     * @see net.sourceforge.nite.gui.actions.OutputComponent#redisplayElement(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void redisplayElement(ObjectModelElement e) {
    }

    /**
     * @see net.sourceforge.nite.gui.actions.OutputComponent#removeDisplayComponent(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void removeDisplayComponent(ObjectModelElement e) {
    }


    /**
     * @see net.sourceforge.nite.gui.actions.OutputComponent#insertDisplayElement(net.sourceforge.nite.nxt.ObjectModelElement, net.sourceforge.nite.nxt.ObjectModelElement, int)
     */
    public void insertDisplayElement(
        ObjectModelElement newElement,
        ObjectModelElement parent,
        int position) {
    }


    /**
     * @see net.sourceforge.nite.gui.actions.OutputComponent#displayElement(net.sourceforge.nite.nxt.ObjectModelElement)
     * FIX ME At the moment this tries to display the value of displayAttribute,
     * but if there isn't one it displays the textual content. Maybe should make
     * this more explicit somewhere
     */
    public JComponent displayElement(ObjectModelElement e, boolean selected) {
	//FIX ME - selection is indicated by red text for labels, which is a bit lame
	if (selected)  ((JLabel)component).setForeground(Color.red);
	else ((JLabel)component).setForeground(Color.black);
	if (e != null){
	    String type = e.getDisplayedAttribute();
	    String displayme = e.getAttributeValue(type);
	    if (displayme == null) displayme = content;
	    ((JLabel)component).setText(displayme);
	    //System.out.println("Label display element " + content + " " + displayme +  " " + type);
	}
	return component;
    }

}
