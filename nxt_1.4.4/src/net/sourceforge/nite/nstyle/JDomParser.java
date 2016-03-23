/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */

package net.sourceforge.nite.nstyle;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JDesktopPane;

import net.sourceforge.nite.gui.actions.AddChildOptionPane;
import net.sourceforge.nite.gui.actions.AddSiblingOptionPane;
import net.sourceforge.nite.gui.actions.ChangeAttributeValueOptionPane;
import net.sourceforge.nite.gui.actions.ChangeAttributeValuePopup;
import net.sourceforge.nite.gui.actions.ChangeTextualContentOptionPane;
import net.sourceforge.nite.gui.actions.DeleteElement;
import net.sourceforge.nite.gui.actions.InputComponent;
import net.sourceforge.nite.gui.actions.OutputComponent;

import net.sourceforge.nite.gui.actions.NActionReference;
import net.sourceforge.nite.gui.actions.NiteAction;

import net.sourceforge.nite.meta.NMetaData;

import net.sourceforge.nite.nstyle.handler.HandlerFactory;
import net.sourceforge.nite.nstyle.handler.JComponentHandler;
import net.sourceforge.nite.nstyle.handler.NActionReferenceHandler;
import net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler;

import net.sourceforge.nite.nxt.JDomObjectModelElement;


import net.sourceforge.nite.time.Clock;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * A parser which produces a Swing component tree based on
 * a JDOM XML document containing <code>JDisplayObject</code>
 * elements.
 * The general alogrithm for setting up the interface, based on the
 * specification is as follows:
 * Parse the Actions, putting the IDs of components as placeholders when they
 * are referred to in an action
 * Parse the DisplayObjects, putting the IDs of actions as placeholders
 * Go through each of the actions , and replace the place holders with the
 * actual display components which they refer to. Also replace
 * actionID placeholders with the actions the refer to.
 * Go through each of the display objects and replace the action placeholders
 * with the actual actions. Also deal with cases where an output component value
 * is prefixed by a '#' - this means that the data will be retrieved from the
 * contents of an input component
 * 
 * @author Judy Robertson
 */
public class JDomParser {

    /** JDom namespace object for the nite namespace. */
    private static final Namespace niteNamespace =
        Namespace.getNamespace("nite", "http://nite.sourceforge.net/");

    /** The JDOM document with the interface specification. */
    private Document interfaceSpecDoc;

    /**
     * The original source xml doc which the user is editing
     */
    private Document sourceDocument;

    /** The JDesktopPane in which we create everything. */
    private JDesktopPane desktopPane;

    private Clock clock;
    private NMetaData metadata;
    private NStyle nstyle;

    /**
     * All the actions in this specification, indexed by ID
     */
    private Map actions = new HashMap();

    /**
     * All the display objects, index by ID. This is required to speed up the
     * matching of components with the actions which do the event handling for
     * them
     */
    private Map displayobjects = new HashMap();

    private JComponentHandler rootHandler;

    /**
     * Construct a <code>JDomParser</code> which will parse the
     * specified XML document, contributing components to the 
     * specified Swing <code>JDesktopPane</code>.
     * 
     * @param interfaceSpecDoc
     * @param frame
     * @param clock
     */
    public JDomParser(
        NStyle n,
        Document original,
        Document spec,
        JDesktopPane desktopPane,
        Clock clock) {
        this.interfaceSpecDoc = spec;
        this.sourceDocument = original;
        this.desktopPane = desktopPane;
        desktopPane.repaint();
        this.clock = clock;
        nstyle = n;
    }

    /**
     * Used to apply the stylesheet to data which has just changed so it can be
     * re-displayed on the interface
     */
    public void redisplayAll() {
        //better remove the internal frame from the display before we do anything else
        desktopPane.removeAll();
        nstyle.redisplay(sourceDocument);
	//  System.out.println("Redisplaying all!");
    }

    /**
     * Parse the XML document, causing the appropriate components
     * to be added to the top-level Swing container.
     */
    public void parse() {
        //initialise the actions
        parseActionObjects();
        //initialise the display objects
        parseDisplayObjects();
        //match up all the actions with component handlers
        matchActionsToDisplayObjects();
        matchDisplayObjectsToActions();

    }

    /** Finds the element in the source document with the unique
        * ID field matching the value "value".  */
    public Element findElementForID(Element element, String value) {
        //the default value for the name of the sourceid attribute

        String attr =
            net.sourceforge.nite.meta.impl.NiteMetaConstants.defaultReservedID;
        attr = "id";
        if (metadata != null) {
            attr = metadata.getIDAttributeName();
        }

        if (value != null) {

            if (value.equals(element.getAttributeValue(attr))) {
                return element;
            }
        }
        if (!element.hasChildren()) {
            return null;
        }
        for (Iterator i = element.getChildren().iterator(); i.hasNext();) {
            Element son = findElementForID((Element) i.next(), value);
            if (son != null) {
                return son;
            }
        }
        return null;

    }

    /**
     * Match up all the swing targets which were specified in the
     * actions with the appropriate components from the component tree
     * */
    private void matchDisplayObjectsToActions() {
        NiteAction action;
        Collection c = actions.values();
        Iterator it = c.iterator();
        while (it.hasNext()) {
            action = (NiteAction) it.next();
            List inputTargetIDs = action.getInputComponentIDs();
            List targetIDs = action.getComponentsIDs();
            findComponents(action, targetIDs);
            findInputComponents(action, inputTargetIDs);
        }
    }

    /**
     * Assign components in the specified action with IDs matching those
     * specified
     */
    private void findComponents(NiteAction action, List targetIDs) {

        Iterator it = targetIDs.iterator();
        while (it.hasNext()) {
            String targetId = (String) it.next();
	    Object obj = actions.get(targetId);
	    //	    System.out.println("Find components for: " + action.getId() + " target: " + targetId + ". Object: " + obj);

            if (obj == null) {
                if (targetId.equals("ChangeTextualContentOptionPane")) {
                    createDynamicComponent(targetId, action);
                } else if (targetId.equals("ChangeAttributeValueOptionPane")) {
                    createDynamicComponent(targetId, action);
                } else if (targetId.equals("ChangeAttributeValuePopUp")) {
                    createDynamicComponent(targetId, action);
                } else if (targetId.equals("AddChildOptionPane")) {
                    createDynamicComponent(targetId, action);
                } else if (targetId.equals("AddSiblingOptionPane")) {
                    createDynamicComponent(targetId, action);
                } else if (targetId.equals("DeleteElement")) {
                    action.addXmlAction(new DeleteElement(this));
                }
            } else if (obj instanceof OutputComponent) {
                OutputComponent handler = (OutputComponent) actions.get(targetId);
                action.addOutputComponent(handler);
            } else if (obj instanceof InputComponent) {
		InputComponent handler = (InputComponent) actions.get(targetId);
		action.addInputComponent(handler);
	    }
        }
    }

    /**
         * Assign components in the specified action with IDs matching those
         * specified
         */
    private void findInputComponents(NiteAction action, List targetIDs) {

        Iterator it = targetIDs.iterator();
        while (it.hasNext()) {
            String targetId = (String) it.next();
            Object obj = displayobjects.get(targetId);
            if (obj == null) {
                System.err.println("Couldn't find an input component");
                System.err.println(targetId + "; " + displayobjects);		
            } else if (obj instanceof InputComponent) {
                InputComponent handler = (InputComponent) displayobjects.get(targetId);
                action.addInputComponent(handler);
            }
        }
    }

    /**
     * Method createDynamicComponent.
     * @param targetId
     * @param action
     * 
     */
    private void createDynamicComponent(String targetId, NiteAction action) {
        if (targetId.equals("ChangeTextualContentOptionPane")) {
	    JComponentHandler j = new ChangeTextualContentOptionPane(this);
            action.addIntermediateComponent(j);
	    // I think the above line should really be
            // action.addOutputComponent(j);
	    // but it's not implemented (jonathan 9/5/3)
        } else if (targetId.equals("ChangeAttributeValueOptionPane")) {
            ChangeAttributeValueOptionPane j =
                new ChangeAttributeValueOptionPane(this);
            action.addIntermediateComponent(j);
        } else if (targetId.equals("ChangeAttributeValuePopUp")) {
            ChangeAttributeValuePopup j = new ChangeAttributeValuePopup(this);
            action.addIntermediateComponent(j);
        } else if (targetId.equals("AddChildOptionPane")) {
            AddChildOptionPane j = new AddChildOptionPane(this);
            action.addIntermediateComponent(j);
        } else if (targetId.equals("AddSiblingOptionPane")) {
            AddSiblingOptionPane j = new AddSiblingOptionPane(this);
            action.addIntermediateComponent(j);
        }
    }

    /**
     * This is used to link the actions specified in the xml to the
     * components. The actions each have an id, which is matched to
     * the actionID attribute in JComponentHandlers.  Once the correct
     * action is found for a component, an event listener is set up
     * */
    private void matchActionsToDisplayObjects() {

        Collection c = displayobjects.values();
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof JComponentHandler) {
                JComponentHandler handler = (JComponentHandler) o;
                //          if this component handler has action ids specified
                List refs = handler.getActionReferences();
                if (refs != null) {
                    //go through all the action references associated with the handler, and find the corresponding action
                    Iterator rit = refs.iterator();
                    while (rit.hasNext()) {
                        NActionReference ref = (NActionReference) rit.next();
                        String actionID = ref.getActionID();
                        //find the action which matches this ID.
                        String keyBinding = ref.getTrigger();
                        handler.registerAction(
                            keyBinding,
                            findAction(actionID));
                    }
                }
            }

        }
    }

    private NiteAction findAction(String actionID) {

        return (NiteAction) actions.get(actionID);
    }

    private void parseActionObjects() {
        // Navigate to the element which contains all the Nite:Actions.
        Element docRoot = interfaceSpecDoc.getRootElement();
        Element actionRoot = docRoot.getChild("Actions", niteNamespace);
        if (actionRoot != null) {
            // Now recurse on all the top-level Nite:Action elements   

            for (Iterator it = actionRoot.getChildren().iterator();
                it.hasNext();
                ) {
                Element child = (Element) it.next();

                NiteAction a = processAction(child);
                Element popupContextRoot =
                    child.getChild("PopupContexts", niteNamespace);
                if (popupContextRoot != null)
                    processPopupContexts(popupContextRoot, a);
                actions.put(a.getId(), a);
            }
        }

    }

    /**
     * Method processPopupContexts.
     * @param popupContextRoot
     * @param a
     */
    private void processPopupContexts(Element popupContextRoot, NiteAction a) {
        //          Now recurse on all the top-level Nite:DisplayObjects.        
        for (Iterator it = popupContextRoot.getChildren().iterator();
            it.hasNext();
            ) {
            Element child = (Element) it.next();

            String attribute =
                ((String) child.getAttributeValue(NConstants.attributeName));
            List attributes = child.getAttributes();

            Iterator ait = attributes.iterator();
            while (ait.hasNext()) {

                Attribute at = (Attribute) ait.next();
                if (at.getName().startsWith("option")) {
                    a.addPopupContext(attribute, at.getValue());
                }
            }
        }

    }

    private void parseDisplayObjects() {
        // Create a simple handler for the implied desktop pane.
        rootHandler = createDesktopPaneHandler();

        // Navigate to the element which contains all the Nite:DisplayObjects.
        Element docRoot = interfaceSpecDoc.getRootElement();

        Element displayRoot = docRoot.getChild("Display", niteNamespace);

        // Now recurse on all the top-level Nite:DisplayObjects.        
        for (Iterator it = displayRoot.getChildren().iterator();
            it.hasNext();
            ) {
            Element child = (Element) it.next();

            NDisplayObjectHandler handler = process(rootHandler, child);
            rootHandler.addChild(handler);

        }
    }

    /**
         * Build a list of NActions based
         * on an XML document.
         * 
         * 
         * @param element	The element to process 
         * 
         * @return The newly created NiteAction
         */
    private NiteAction processAction(Element element) {

        NiteAction action = new NiteAction(this);
        // Set up the id of this action
        action.setId((String) element.getAttributeValue(NConstants.id));
        //set up the description of the action
        action.setDescription(
            (String) element.getAttributeValue(NConstants.description));
        action.addComponentID(
            (String) element.getAttributeValue(NConstants.dialoguebox));
        action.addInputComponentID(
            (String) element.getAttributeValue(NConstants.source));
        action.setActionType(
            (String) element.getAttributeValue(NConstants.type));
            action.setAttributeName((String) element.getAttributeValue(NConstants.attributeName));
           

        return action;
    }

    /**
     *  Extract  the Target specification from the jdom element */
    private void processTarget(Element target, NiteAction action) {

        //get the content for this target
        String content = target.getAttributeValue(NConstants.type);
        String number = target.getAttributeValue(NConstants.arity);

        //Not yet sure how the target thingies are meant to work. Are they simply type checking?
        //action.addTarget(mutation);
    }

    /**
         * Extract the InputComponent specification from the jdom element, and
         * store the ID of the component which will be used to get input. The
         * component itself will be found at a later stage in the processing
         * once the components have been generated */

    private void processInputComponent(Element target, NiteAction action) {

        String type = target.getAttributeValue(NConstants.DisplayType);
        String componentID = target.getAttributeValue(NConstants.ComponentID);

        action.addInputComponentID(componentID);

    }

    /**
     * Recursive method for building a tree of NDisplayObjectHandlers based
     * on an XML document.
     * 
     * @param parent	The parent NDisplayObjectHandler for this step.
     * @param element	The element to process at this step.
     * 
     * @return The newly created NDisplayObjectHandler for this step.
     */
    private NDisplayObjectHandler process(
        NDisplayObjectHandler parent,
        Element element) {
        NDisplayObjectHandler current = null;
        // Get the textual content and attributes from the element.
        String content = element.getText();
        Map props = new HashMap();

	
        for (Iterator it = element.getAttributes().iterator(); it.hasNext();) {
            Attribute attr = (Attribute) it.next();
            props.put(attr.getName().trim(), attr.getValue());
        }

        if (element.getName().equals("ActionReference")) {
            current = new NActionReferenceHandler();

            current.init(content, props);
        } else { // Get the type of the element, and build the appropriate handler.
            String type = element.getAttributeValue("type");

            current = HandlerFactory.getHandler(type);
            current.setClock(clock);

            // Initialise the handler.
            current.init(content, props);
            current.setParent(parent);

            // add this handler to the Map of display objects which is
            // used for fast retrieval of displayobjects by ID when
            // linking up actions to display objects later
            displayobjects.put(current.getID(), current);

            // Iterate over the child elements and, recursive apply
            // this method to each of them, with the newly created handler
            // as the parent.
            int index = 0;

            for (Iterator it = element.getChildren().iterator(); it.hasNext(); ) {
                index++;
                Element child = (Element) it.next();

                NDisplayObjectHandler childHandler = process(current, child);
                //IMPORTANT -  editing the user's source doucment. NOT the interface spec
                Element el = findElementForID( sourceDocument.getRootElement(),
                        childHandler.getSourceID());
                if (el != null) {
                    //FIX ME - THIS NEEDS TO BE EXTENDED FOR NOM TOO
                    JDomObjectModelElement jel = new JDomObjectModelElement(el);
                    jel.setChildIndex(index);
		jel.setDisplayedAttribute(child.getAttributeValue("displayedAttribute"));
                    jel.setParent(new JDomObjectModelElement(el.getParent()));
                    childHandler.setElement(jel);
                }
                current.addChild(childHandler);
            }
        }

        return current;

    }

    /**
     * Create a NDisplayObjectHandler which wraps the JDesktopPane.
     * 
     * @return NDisplayObjectHandler
     */
    private JComponentHandler createDesktopPaneHandler() {

        // Create a simple handler to wrap the desktop pane.
        // Everything gets added to that.
        JComponentHandler h = new JComponentHandler() {
            protected void createPeer() {
                this.component = desktopPane;

            }
            public void registerAction(String s, NiteAction a) {
                System.err.println(
                    "Warning, attempt to register an action with a desktop pane");
            }

        };

        h.init("", Collections.EMPTY_MAP);

        return h;
    
        
       
    }

    /**
     * Returns the metadata.
     * @return NiteMetaData
     */
    public NMetaData getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata.
     * @param metadata The metadata to set
     */
    public void setMetadata(NMetaData metadata) {
        this.metadata = metadata;
    }

}
