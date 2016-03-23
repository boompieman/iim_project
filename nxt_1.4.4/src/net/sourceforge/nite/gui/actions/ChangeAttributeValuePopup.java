/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


import net.sourceforge.nite.nstyle.JDomParser;
import net.sourceforge.nite.nstyle.handler.JComponentHandler;
import net.sourceforge.nite.nxt.ObjectModelElement;
import net.sourceforge.nite.gui.textviewer.Displayer;

/**
 * @author judyr
 *
 * A popup menu which allows the user to select from a set of
 * pre-specified values for a specified attribute 
 */
public class ChangeAttributeValuePopup extends JComponentHandler
    implements ActionListener {
    
    Map attributeContext; 

    /**
     * The element which the user has selected and wants to change the value
     * of an attribute on
     */
    ObjectModelElement selectedElement;

    JPopupMenu popup;
    javax.swing.JComponent location;
    int x = 0;
    int y = 0;
    /** if set, the object that is called upon to redisplay */
    protected Displayer displayer=null;

    /**
     * The parser which will be used to redisplay the interface once the
     * action has changed the underlying xml data
     */
    JDomParser parser;
    OutputComponent outputComponent;

    public ChangeAttributeValuePopup(JDomParser p) {
        parser = p;
    }
    
    public ChangeAttributeValuePopup(OutputComponent o){
	outputComponent =o;
    }

    /**
     * @see net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
	popup = new JPopupMenu();
        // Before bothering to set up this menu, check to see whether
        // the selectedElement even has an attribute that we are
        // trying to change. If not, we don't need to display a popup
	//	System.out.println("Checking displayed att of element " + selectedElement + ": " + selectedElement.getDisplayedAttribute());
        if (selectedElement != null) {
	    if (selectedElement.getDisplayedAttribute() != null) {
		//		System.out.println("Create peer: " + selectedElement + "; " + selectedElement.getID() + "; " + selectedElement.getDisplayedAttribute());
		String displayed=selectedElement.getDisplayedAttribute();
           	List valueOptions = (List) attributeContext.get(displayed);
		if (valueOptions==null) {
		    System.out.println("There are no values to display the popup (attribute: " + displayed + ")");
		    return;
		}
                Iterator it = valueOptions.iterator();
                //populate the menu with the strings specified by the user
                while (it.hasNext()) {
                    String option = (String) it.next();
                    if (option != null) {
                        JMenuItem item = new JMenuItem(option);
                        //action performed in this class will handle clicks on the menu item
                        item.addActionListener(this);
                        popup.add(item);
                    }
                }
	    }
	    popup.show(location, x, y);
	    popup.setVisible(true);
        }
    }

    /**
     * There should be no actions registered with this, so nothing happens here
     * @see net.sourceforge.nite.nstyle.handler.JComponentHandler#registerAction(net.sourceforge.nite.gui.actions.NiteAction)
     */
    public void registerAction(String s, NiteAction a) {
    }
    

    /**
     * Returns the selectedElement.
     * @return ObjectModelElement
     */
    public ObjectModelElement getSelectedElement() {
        return selectedElement;
    }

 
    /**
     * Sets the selectedElement.
     * @param selectedElement The selectedElement to set
     */
    public void setSelectedElement(ObjectModelElement selectedElement) {
        this.selectedElement = selectedElement;
    }
   

    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) (e.getSource());
        //the value which the attribute should be changed to
        String value = source.getText();
        ChangeAttributeValue cav;
	if (parser != null) cav = new ChangeAttributeValue(parser, selectedElement, value, selectedElement.getDisplayedAttribute());
	else cav = new ChangeAttributeValue(outputComponent, selectedElement, value, selectedElement.getDisplayedAttribute());
	if (this.displayer!=null) { cav.setDisplayer(this.displayer); }
	ActionEvent newe = new ActionEvent(this, 0, "changeattributevaluepopup");
        cav.actionPerformed(newe);
    }
    
    /**
     * Sets the displayer which is the object claiming it can
     * redisplay after this action. This will override any default
     * redisplay behaviour.
     */
    public void setDisplayer(Displayer displayer) {
        this.displayer = displayer;
    }

    /**
     * Sets the location.
     * @param location The location to set
     */
    public void setLocation(javax.swing.JComponent location, int x, int y) {
        this.x = x;
        this.y = y;
        this.location = location;
    }

    /**
     * Sets the attributeContext.
     * @param attributeContext The attributeContext to set
     */
    public void setAttributeContext(Map popupContext) {
        this.attributeContext = popupContext;
    }

}
