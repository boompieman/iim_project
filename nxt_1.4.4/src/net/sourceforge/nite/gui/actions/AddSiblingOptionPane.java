/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.sourceforge.nite.nstyle.JDomParser;
import net.sourceforge.nite.nstyle.handler.JComponentHandler;
import net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler;
import net.sourceforge.nite.nxt.JDomObjectModelElement;
import net.sourceforge.nite.nxt.ObjectModelElement;
import javax.swing.JPanel;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.swing.BoxLayout;

import org.jdom.Attribute;
import org.jdom.Element;


/**
 * @author judyr
 *
 * This component is specifically for displaying an option pane which asks a
 * user to type in entries which specify a new sibling for the currently
 * selected xml element
 */
public class AddSiblingOptionPane extends JComponentHandler {

    private ObjectModelElement selectedElement;
   
    private String message = "";
    private JOptionPane pane;
    private JLabel contentLabel = new JLabel("Name of Element ");
    private JTextField textField = new JTextField(10);
    private JDomParser parser;
    private int index;
    private String elementName;
    private JPanel attributePane;
    private List attributeButtons = new ArrayList();
    public AddSiblingOptionPane(JDomParser p){
            
    parser = p;
    }

    public AddSiblingOptionPane(ObjectModelElement e, String m, int index) {
        
        selectedElement = e;
        message = m;
        this.index = index;

    }

    public void createPeer() {
        createAttributePane();  
        elementName = selectedElement.getName(); 
        textField.setText(elementName);         
        Object[] array = {  attributePane,contentLabel, textField, "Enter", "cancel" };
        
        ObjectModelElement parentel = getSelectedElement().getParent();
    String prompt = "add new sibling to " + getSelectedElement().getAttributeValue("name");
       
        //set up the event handling for the option pane
            int result = JOptionPane.showOptionDialog(null,
        prompt,
        "Create new sibling", // the title of the dialog window
        JOptionPane.DEFAULT_OPTION, // option type
        JOptionPane.INFORMATION_MESSAGE, // message type
        null, // optional icon, use null to use the default icon
        array, // options string array, will be made into buttons
        array[2] // option that should be made into a default button
    );
        switch (result) {
	case 1:
       
	case 2:
	
            case 3 : // enter
            
               ObjectModelElement newElement = makeNewElement();
               AddSibling ac = new AddSibling(parser, selectedElement,newElement, index);
                ac.actionPerformed(new ActionEvent(this, 0, "addsiblingoptionpane"));
                break;
            case 4 : // cancel
           
              
                break;
            default :
                break;
        }

    }

    

    /**
     * Method makeNewElement.
     * @return ObjectModelElement
     */
    private ObjectModelElement makeNewElement() {
            //FIX ME - CHANGE THIS FOR NOM
            JDomObjectModelElement jel = new JDomObjectModelElement(new Element(textField.getText()));
            Iterator it = attributeButtons.iterator();
            while (it.hasNext()){
                    //set up all the attributes which the user has entered
                    AttributeInput a = (AttributeInput) it.next();
                    String val = a.getInputBox().getText();
                    jel.addAttribute(a.attributeName, val);
                    jel.setNamespace(selectedElement.getNamespace());
                    jel.setDisplayedAttribute(selectedElement.getDisplayedAttribute());
            }
        return jel;
    }

    /**
     * Method createAttributePane. This sets up a panel for the user to specify
     * the attribute values for the attributes which are stored in the sibling
     * of this new element
     */
    private void createAttributePane() {
            attributePane = new JPanel();
            attributePane.setLayout(new BoxLayout(attributePane, BoxLayout.Y_AXIS));
            List attrs = selectedElement.getAttributes();
            Iterator it = attrs.iterator();
            JLabel explain = new JLabel("Please enter the attribute values");
            attributePane.add(explain);
            while(it.hasNext()){
            String at = ((Attribute) it.next()).getName();
            String val = selectedElement.getAttributeValue(at);
            AttributeInput a = new AttributeInput(at,val);
            attributeButtons.add(a);
            attributePane.add(a.getLabel());
            attributePane.add(a.getInputBox());
            }
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
     * Returns the index.
     * @return int
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index.
     * @param index The index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }
    /**
     * 
     * 
     * An inner class used to map between attributes the user can change and the
     * textboxews they use to enter value for them in the interface
     * @author Judy Robertson
     */
    class AttributeInput{
            JLabel label;
            JTextField inputBox;
            String attributeName;
            String attributeValue;
            
            AttributeInput(String name, String value){
             attributeName = name;
             attributeValue = value;
             label = new JLabel(attributeName);
             inputBox = new JTextField(10);       
            
            }
            
    
            /**
             * Returns the inputBox.
             * @return JTextField
             */
            public JTextField getInputBox() {
                return inputBox;
            }

            /**
             * Sets the inputBox.
             * @param inputBox The inputBox to set
             */
            public void setInputBox(JTextField inputBox) {
                this.inputBox = inputBox;
            }

            /**
             * Returns the label.
             * @return JLabel
             */
            public JLabel getLabel() {
                return label;
            }

            /**
             * Sets the label.
             * @param label The label to set
             */
            public void setLabel(JLabel label) {
                this.label = label;
            }

    }

}

