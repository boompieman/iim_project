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

import net.sourceforge.nite.gui.textviewer.NTextArea;
import net.sourceforge.nite.nstyle.JDomParser;
import net.sourceforge.nite.nstyle.handler.JComponentHandler;
import net.sourceforge.nite.nxt.ObjectModelElement;

/**
 * @author judyr, jeanc
 *
 A popup menu which allows the user to select from a set of pre-specified values for a specified attribute
 Jeanc changing so that instead of relying on the displayedAttribute
 setting of the text area, this is given a tag name and an attribute name
 * JEAN, I HAD TO CHANGE THIS FOR YOU WHEN UPDATING MY ACTION CODE. In future, wiser to extend my classes and reimplement only the necessary alterations for your own code.
 */
public class AltChangeAttributeValuePopup
	extends JComponentHandler
	implements ActionListener {

	Map popupContext;

	/**
	 * The element which the user has selected and wants to change the value
	 * of an attribute on
	 * 
	 */
	ObjectModelElement selectedElement;

	JPopupMenu popup;
	javax.swing.JComponent location;
	int x = 0;
	int y = 0;

	/**
	 * The parser which will be used to redisplay the interface once the
	 * action has changed the underlying xml data
	 */
	JDomParser parser;
	OutputComponent outputComponent;

	/** The name of the tag we're changing an attribute for 
	 */
	String tag_name;
	/** The name of the attribute we want to change
	 */
	String attribute_name;

	public AltChangeAttributeValuePopup(JDomParser p, String tag, String att) {
		parser = p;
		tag_name = tag;
		attribute_name = att;
	}

	public AltChangeAttributeValuePopup(OutputComponent o, String tag, String att) {
		outputComponent = o;
		tag_name = tag;
		attribute_name = att;
	}

	/**
	 * @see net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
	 */
    protected void createPeer() {
	popup = new JPopupMenu();
	//Before bothering to set up this menu, check to see 
	//  whether there is a selectedElement
	//  whether its tag is the correct tag
	// if not, we can beep.
	// LATER, extend to climb XML from tag up to a matching tag.
	if (selectedElement != null) {
	    if (selectedElement.getName().equals(tag_name)) {

		// we used to pop up a menu corresponding to the
		// attribute displayed by the text area.  Now pop up
		// whatever we said when we created this popup action
		// String displayed =
		// selectedElement.getDisplayedAttribute();
		//List valueOptions = (List) popupContext.get(displayed);
		List valueOptions = (List) popupContext.get(attribute_name);
		if (valueOptions==null) return;
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
		popup.show(location, x, y);
		popup.setVisible(true);
	    }
	    else { // how does beep work in Java?
		System.out.println("Not on a " + tag_name + "!") ;
	    }
	    
	}
	else { // how does beep work in Java?
	    System.out.println("No element is selected!") ;
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
		if (parser != null)
			cav =
				//new ChangeAttributeValue(parser, selectedElement, value, selectedElement.getDisplayedAttribute());
	new ChangeAttributeValue(parser, selectedElement, value, attribute_name);
		//else cav = new ChangeAttributeValue(textArea, selectedElement, value, selectedElement.getDisplayedAttribute());
		else
			cav =
				new ChangeAttributeValue(
					outputComponent,
					selectedElement,
					value,
					attribute_name);
		ActionEvent newe =
			new ActionEvent(this, 0, "altchangeattributevaluepopup");
		cav.actionPerformed(newe);

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
	 * Sets the popupContext.
	 * @param popupContext The popupContext to set
	 */
	public void setPopupContext(Map popupContext) {
		this.popupContext = popupContext;
	}

    /**
     * Sets the attributeContext.
     * @param attributeContext The attributeContext to set
     */
    public void setAttributeContext(Map popupContext) {
        this.popupContext = popupContext;
    }
}
