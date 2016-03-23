/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.actions;

import java.awt.event.ActionEvent;

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
 * user to type in new text to change the textual content of a selected xml
 * element
 */
public class ChangeTextualContentOptionPane extends JComponentHandler {

    private ObjectModelElement selectedElement;
    private ChangeTextualContent action;
    private String newContent;
    private String message = "";
    private JOptionPane pane;
    private JTextField textField = new JTextField(10);
    private JDomParser parser;
    private OutputComponent outputComponent;

    public ChangeTextualContentOptionPane(JDomParser p) {
        parser = p;
    }

    public ChangeTextualContentOptionPane(OutputComponent o) {
        outputComponent = o;
    }

    public ChangeTextualContentOptionPane(ObjectModelElement e, String m) {
        selectedElement = e;
        message = m;
    }

    public void createPeer() {
        // this is used to store the things which will be displayed on
        // this option pane
        Object[] array = { textField, "Enter", "cancel" };
        final String btnString1 = "Enter";
        final String btnString2 = "Cancel";

        //set up the event handling for the option pane
	int result =
	    JOptionPane.showOptionDialog(null, selectedElement.getTextualContent(), "Change Text",
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
                ChangeTextualContent ctc;
                if (parser != null){
                    ctc = new ChangeTextualContent(parser, selectedElement, newContent);
                    ctc.actionPerformed(new ActionEvent(this, 0, "changetextualcontentoption"));
                } else { // if (outputComponent != null) {
                    ctc = new ChangeTextualContent(outputComponent, selectedElement, newContent);
		    ctc.actionPerformed(new ActionEvent(this, 0, "changetextualcontentoption"));
                }
        
        break;
        case 4 : // cancel
    System.out.println("Cancelled");
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
public void registerAction(NiteAction a) {

}

/**
 * Attempts to add a child to this should fail, as it is a self containted
 * display
 * @see net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#addChild(NDisplayObjectHandler)
 */
public void addChild(NDisplayObjectHandler nah) {
    System.err.println("Attempt to add handler " + nah.getID() + "to " + getID() + "failed");
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
 * @see net.sourceforge.nite.nstyle.handler.JComponentHandler#registerAction(java.lang.String, net.sourceforge.nite.gui.actions.NiteAction)
 */
public void registerAction(String binding, NiteAction a) {
}

}
