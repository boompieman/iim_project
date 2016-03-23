/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.actions;

import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.sourceforge.nite.nstyle.JDomParser;
import net.sourceforge.nite.nstyle.handler.JComponentHandler;
import net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler;
import net.sourceforge.nite.nxt.ObjectModelElement;

/**
 * @author judyr
 *
 * This component is specifically for displaying an option pane which asks a
 * user to type in new text to enter a new value for a specified a attribute
 * selected xml element
 */
public class ChangeAttributeValueOptionPane extends JComponentHandler {

    private ObjectModelElement selectedElement;
    private String attributeName;
    private ChangeTextualContent action;
    private String newContent;
    private String message = "";
    private JOptionPane pane;
    private JTextField textField = new JTextField(10);
    private JDomParser parser;
    Map attributeContext;
    private OutputComponent outputComponent;

    public ChangeAttributeValueOptionPane(JDomParser p) {

        parser = p;
    }
    public ChangeAttributeValueOptionPane(OutputComponent o) {
        outputComponent = o;
    }

    public ChangeAttributeValueOptionPane(
        ObjectModelElement e,
        String m,
        String attribute) {

        selectedElement = e;
        message = m;
        attributeName = attribute;

    }

    public void createPeer() {

        Object[] array = { textField, "Enter", "cancel" };
        final String btnString1 = "Enter";
        final String btnString2 = "Cancel";

	if (getAttributeName()==null) {
	    setAttributeName(selectedElement.getDisplayedAttribute());
	}

	if (getAttributeName()==null) {
	    System.err.println("Interface has benn wrongly set up: no attribute name has been specified to be edited. ");
	    return;
	}
	

        //set up the event handling for the option pane
            int result =
                JOptionPane
                    .showOptionDialog(
                        null,
                        "Change the value for "
                            + getAttributeName()
                            + " in element "
                            + getSelectedElement().getName(),
                        "Change Attribute Value",
        // the title of the dialog window
        JOptionPane.DEFAULT_OPTION, // option type
        JOptionPane.INFORMATION_MESSAGE, // message type
        null, // optional icon, use null to use the default icon
        array, // options string array, will be made into buttons
        array[2] // option that should be made into a default button
    );
        switch (result) {
            case 1 :

            case 2 :

            case 3 : // enter
                newContent = textField.getText();
                ChangeAttributeValue cav;
                if (parser != null) {

                    cav =
                        new ChangeAttributeValue(
                            parser,
                            selectedElement,
			    newContent,
                            getAttributeName()
                            );
                    cav.actionPerformed(
                        new ActionEvent(this, 0, "changeattributevalueoption"));
                } else if (outputComponent != null) {
                    cav =
                        new ChangeAttributeValue(
                            outputComponent,
                            selectedElement,
                            
                            newContent,
					getAttributeName());

                    cav.actionPerformed(
                        new ActionEvent(this, 0, "changeattributevalueoption"));
                }

                break;
            case 4 : // cancel

                break;
            default :
                break;
        }

    }

    /**
     * Returns the newContent.
     * @return String
     */
    public String getNewContent() {
        return newContent;
    }

    /**
     * Sets the newContent.
     * @param newContent The newContent to set
     */
    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }

    /**
     * This method does nothing, as the same action is always carried out from
     * this class
     * @see net.sourceforge.nite.nstyle.handler.JComponentHandler#registerAction(NiteAction)
     */
    public void registerAction(String s, NiteAction a) {

    }

    /**
     * Attempts to add a child to this should fail, as it is a self containted
     * display
     * @see net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#addChild(NDisplayObjectHandler)
     */
    public void addChild(NDisplayObjectHandler nah) {
        System.err.println(
            "Attempt to add handler "
                + nah.getID()
                + "to "
                + getID()
                + "failed");
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

    /**
     * Returns the attributeName 
     * @return String
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Sets the attributeName.
     * @param attributeName The attributeName to set
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

}
