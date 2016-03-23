/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;

import net.sourceforge.nite.gui.textviewer.Displayer;
import net.sourceforge.nite.nstyle.JDomParser;
import net.sourceforge.nite.nstyle.handler.JComponentHandler;
import net.sourceforge.nite.nxt.ObjectModelElement;

/**
 * @author judyr
 *
 * Abstract actions are useful because you can create one action
 * handling class for several outputComponents which need to do the
 * same thing, such as a save button and a save menu item Furthermore,
 * they can be added as event handlers to most Swing outputComponents
 * which are used in NITE 
 */
public class NiteAction extends AbstractAction {

    /**
     * If the action was created via a stylesheet, this is the
     * JDomParser which created this action. It'll be needed to call
     * redisplay when the action is performed  */
    protected JDomParser parser;

    /** if set, the object that is called upon to redisplay */
    protected Displayer displayer=null;

    /**
     * A unique ID for this action
     * */
    private String id;

    /**
     * A textual description of this action
     * */
    private String description;

    /**
     * IDs of the SwingTarget outputComponents. These will be used to
     * match up the correct component handlers with the actions
     * */
    private List outputComponentIDs;
    private List inputComponentIDs;
    private List componentIDs;
    private Map popupContexts = new HashMap();
    private String actionType;
    private NiteAction xmlAction;
    private String attributeName;
    /**
     * A list of JComponentHandlers which wrap the outputComponents
     * from which user input is extracted
     * */
    private List inputComponents;
    private List outputComponents;
    private List intermediateComponents;

    public NiteAction(JDomParser parser) {
        //initialise all lists
        inputComponents = new ArrayList();
        outputComponentIDs = new ArrayList();
        inputComponentIDs = new ArrayList();
        componentIDs = new ArrayList();
        outputComponents = new ArrayList();
        intermediateComponents = new ArrayList();
        this.parser = parser;
    }

    public NiteAction() {
        //initialise all lists
        inputComponents = new ArrayList();
        outputComponentIDs = new ArrayList();
        inputComponentIDs = new ArrayList();
        componentIDs = new ArrayList();
        outputComponents = new ArrayList();
        intermediateComponents = new ArrayList();
    }

    /**
     * When this action is executed, collect the user input from the source,
     * and initiate the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
	//        System.out.println("Action performed in NiteAction");
        ObjectModelElement el = null;

        Iterator inputit = inputComponents.iterator();
        while (inputit.hasNext()) {
            Object ob = inputit.next();
            if (ob instanceof InputComponent) {
                InputComponent c = (InputComponent) ob;
                Set set = c.getSelectedObjectModelElements();
                
                Iterator setiterator = set.iterator();
                while (setiterator.hasNext()) {
                    Object test = setiterator.next();
                    el = (ObjectModelElement) test;
                    // if there is already an xmlAction, then just call
                    // that. But if not, there needs to be an
                    // intermediate output screen for the user 
		    if (xmlAction != null) executeXmlAction( el);
		    else { 
			displayIntermediateComponent(e, el); 
			// break; 
		    }
                }
            }
        }
    }
    
    private void executeXmlAction(ObjectModelElement e){
    	if (xmlAction instanceof XMLAction) {
	    ((XMLAction)xmlAction).setElement(e);
	    xmlAction.actionPerformed(new ActionEvent(this, 0, "executeXMLAction"));
	    //	System.out.println("Executed xml action");
    	} else 
	    System.err.println("Attempt to execute action which was not of type XMLAction failed");
    }

    private void displayIntermediateComponent(ActionEvent e, ObjectModelElement el) {
	Iterator it = intermediateComponents.iterator();
	while (it.hasNext()) {
	    Object o = it.next();
	    if (o instanceof ChangeTextualContentOptionPane) {
		// FIXME for the momement, the intermediate display
		// components will use the first output component for
		// redisplay - in future this shuld be extended to a
		// list of output components
		OutputComponent oc = null;
		ChangeTextualContentOptionPane pane = null;
		if ((outputComponents!=null) && (outputComponents.size()!=0)) {
		    oc=(OutputComponent)outputComponents.get(0);
		    pane = new ChangeTextualContentOptionPane(oc);
		} else if (intermediateComponents!=null && intermediateComponents.size()!=0) {
		    Object pq = intermediateComponents.get(0);
		    if (pq instanceof ChangeTextualContentOptionPane) {
			pane = (ChangeTextualContentOptionPane)pq;
		    }
		}
		 
		if (el!=null && pane!=null) {
		    pane.setSelectedElement(el);
		    pane.createPeer();
		}
	    } else if (o instanceof ChangeAttributeValueOptionPane) {
		OutputComponent oc = null;
		ChangeAttributeValueOptionPane apane = null;
		if ((outputComponents!=null) && (outputComponents.size()!=0)) {
		    oc=(OutputComponent)outputComponents.get(0);
		    apane = new ChangeAttributeValueOptionPane(oc);
		} else if (intermediateComponents!=null && intermediateComponents.size()!=0) {
		    Object pq = intermediateComponents.get(0);
		    if (pq instanceof ChangeAttributeValueOptionPane) {
			apane = (ChangeAttributeValueOptionPane)pq;
		    }
		}
		//		apane.setAttributeName(((ChangeAttributeValueOptionPane) o).getAttributeName());
		apane.setAttributeName(getAttributeName());
		if (el != null) {
		    apane.setSelectedElement(el);
		    apane.createPeer();
		}
	    } else if (o instanceof AddChildOptionPane) {
		AddChildOptionPane apane = (AddChildOptionPane) o;
		if (el != null) {
		    apane.setSelectedElement(el);
		    // a new child should be appended to the list of
		    // children for this element
		    apane.setIndex(el.getChildren().size());
		    apane.createPeer();
		}
	    } else if (o instanceof AddSiblingOptionPane) {
		AddSiblingOptionPane apane = (AddSiblingOptionPane) o;
		if (el != null) {
		    apane.setSelectedElement(el);
		    apane.setIndex(el.getChildIndex());
		    apane.createPeer();
		}
	    } else if (o instanceof ChangeAttributeValuePopup) {
		ChangeAttributeValuePopup pop = (ChangeAttributeValuePopup) o;
		if (el != null) {
		    pop.setAttributeContext(popupContexts);
		    if (e.getSource() instanceof MouseEvent) {
			MouseEvent me = (MouseEvent) e.getSource();
			pop.setLocation((JComponent) me.getComponent(), me.getX(), me.getY());
		    }
		    //		    System.out.println("create peer");
		    pop.setSelectedElement(el);
		    pop.createPeer();
		}
	    } else if (o instanceof AltChangeAttributeValuePopup) {
		AltChangeAttributeValuePopup pop = (AltChangeAttributeValuePopup) o;
		if (el != null) {
		    pop.setAttributeContext(popupContexts);
		    if (e.getSource() instanceof MouseEvent) {
			MouseEvent me = (MouseEvent) e.getSource();
			pop.setLocation((JComponent) me.getComponent(), me.getX(), me.getY());
		    }
		    pop.setSelectedElement(el);
		    pop.createPeer();
		}
		
	    }
	}
    }

    public void addInputComponent(InputComponent s) {
        inputComponents.add(s);
    }

    public void addOutputComponent(OutputComponent j) {
        outputComponents.add(j);
    }
    
    public void addIntermediateComponent(JComponentHandler j){
	intermediateComponents.add(j);
    }

    public void addInputComponentID(String id) {
        inputComponentIDs.add(id);
    }

    public void addComponentID(String id) {
        componentIDs.add(id);
    }

    /**
     * Returns the description.
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the id.
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the swingTargets.
     * @return List
     */
    public List getInputComponents() {
        return inputComponents;
    }

    /**
     * Sets the description.
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the id.
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    public void setInputComponents(List ic) {
        inputComponents = ic;
    }

    public List getOutputComponentIDs() {
        return outputComponentIDs;
    }

    public List getInputComponentIDs() {
        return inputComponentIDs;
    }

    /**
     * Returns the componentsIDs.
     * @return List
     */
    public List getComponentsIDs() {
        return componentIDs;
    }

    /**
     * Sets the componentsIDs.
     * @param componentsIDs The componentsIDs to set
     */
    public void setComponentsIDs(List componentsIDs) {
        componentIDs = componentsIDs;
    }

    /**
     * Method addOptionValue.
     * @param option
     */
    public void addPopupContext(String attribute, String optionValue) {
        List l = (List) popupContexts.get(attribute);
        if (l == null) {
            l = new ArrayList();
            l.add(optionValue);
            popupContexts.put(attribute, l);
        } else {
            l.add(optionValue);
            popupContexts.put(attribute, l);
        }
    }

    /**
     * Returns the optionValues.
     * @return List
     */
    public List getOptionValues(String attribute) {
        return (List) popupContexts.get(attribute);
    }

    /**
     * Returns the parser. This is only relevant for stylesheet display
     * @return JDomParser
     */
    public JDomParser getParser() {
        return parser;
    }

    /**
     * Sets the parser.
     * @param parser The parser to set
     */
    public void setParser(JDomParser parser) {
        this.parser = parser;
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
     * Returns the displayer which is the object claiming it can
     * redisplay after this action.
     */
    public Displayer getDisplayer() {
        return displayer;
    }

    /**
     * @return String
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * Sets the actionType.
     * @param actionType The actionType to set
     */
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    /**
     * @param object
     */
    public void addXmlAction(NiteAction a) {
      xmlAction = a;
    }

    /**
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

    /**
     * @return List
     */
    public List getOutputComponents() {
        return outputComponents;
    }

    /**
     * Sets the outputComponents.
     * @param outputComponents The outputComponents to set
     */
    public void setOutputComponents(List outputComponents) {
        this.outputComponents = outputComponents;
    }

}
