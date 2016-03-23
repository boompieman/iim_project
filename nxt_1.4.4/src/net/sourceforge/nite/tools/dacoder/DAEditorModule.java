package net.sourceforge.nite.tools.dacoder;

import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.util.*;
import net.sourceforge.nite.nom.link.*;

import java.util.logging.*;
import java.io.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collection;
import java.util.Arrays;

import javax.swing.plaf.basic.BasicComboBoxRenderer;

import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import net.sourceforge.nite.search.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.nstyle.handler.*;
import net.sourceforge.nite.time.*;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.*;
import com.jgoodies.looks.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;


/**
 * This Editor Module is the first step in the 'plug-in' like restructuring of ou
 * annotation tools.
 * The editor module provides a graphical interface (JComponent... e.g. JPanel) for
 * editing elements of a certain type (Dialogue acts).
 * The module also provides a number of editing functions for external access, such as
 * may be createNewElement or deleteCurrentElement.
 * And last but not least the module provides an ActionMap of actions, which can be connected 
 * to keypresses in main tool, or displayed as a toolbar...
 */
public class DAEditorModule implements NTASelectionListener,ActionListener,SignalListener,NOMWriteElementContainer {
    public static final int NORMAL=0;
    public static final int WAITING=1; //waiting for WHAT? (range)
    private int INTERNAL_STATE=NORMAL; //should be state of edit pane!!!!!!!!!!!
    JPopupMenu jmenu;
    String ignoreaddresseeattribute = "";
    /** 
     * The name of the elements edited by this module (added Jonathan Kilgour 27/4/06)
     */ 
    protected String elementNameLong = "Dialogue Act";
    protected String elementNameShort = "DA";

/*==================
Initialization, connection to main tool
====================*/
    //main param vervangen door config, ntv en corpus?
    public DAEditorModule(DACoder main) {
        this.main = main;
        
	// If we have the daattributename set in the config file, use
	// a simple enumerated val as the popup, otherwise use the
	// more complicated OntologyPopup. JAK 22/3/06
	String dat = ((DACoderConfig)main.getConfig()).getDAAttributeName();
	if (dat==null || dat.length()==0) {
	    // Ontology
	    jmenu = new OntologyPopupMenu(main.getCorpus(),((DACoderConfig)main.getConfig()).getDAAGloss(),this, ((DACoderConfig)main.getConfig()).getDATypeRoot(), false, false);
	    
	} else {
	    // Enumerated attribute
	    jmenu = new EnumeratedPopupMenu(main.getCorpus(), ((DACoderConfig)main.getConfig()).getDAElementName(), ((DACoderConfig)main.getConfig()).getDAAttributeName(), this);
	}
	ignoreaddresseeattribute = ((DACoderConfig)main.getConfig()).getAddresseeIgnoreAttribute();
        main.getNTV().addNTASelectionListener(this);
        createGUI();
        setCurrentElement(null);
    }


    /** Alternative constructor that sets the long and short names of
     * the discourse entities to be edited in this window (defaults:
     * "Dialogue Act"; "DA"). */
    public DAEditorModule(DACoder main, String longname, String shortname) {
        this.main = main;
	// If we have the daattributename set in the config file, use
	// a simple enumerated val as the popup, otherwise use the
	// more complicated OntologyPopup. JAK 22/3/06
	String dat = ((DACoderConfig)main.getConfig()).getDAAttributeName();
	if (dat==null || dat.length()==0) {
	    // Ontology
	    jmenu = new OntologyPopupMenu(main.getCorpus(),((DACoderConfig)main.getConfig()).getDAAGloss(),this, ((DACoderConfig)main.getConfig()).getDATypeRoot(), false, false);
	    
	} else {
	    // Enumerated attribute
	    jmenu = new EnumeratedPopupMenu(main.getCorpus(), ((DACoderConfig)main.getConfig()).getDAElementName(), ((DACoderConfig)main.getConfig()).getDAAttributeName(), this);
	}
	ignoreaddresseeattribute = ((DACoderConfig)main.getConfig()).getAddresseeIgnoreAttribute();
        main.getNTV().addNTASelectionListener(this);

	setLongName(longname);
	setShortName(shortname);
        createGUI();
        setCurrentElement(null);
    }

    /**
     * For reference to NTV, observationname, nomcorpus......
     */
    DACoder main;

    /**
     * The current Dialogue act element
     */
    protected NOMWriteElement currentElement = null;

    /**
     * The current media signal (used to lay out the addressee check boxes)
     */
    protected String currentSignal = null;

/*===============
    ACTIONS
=====================*/

    /**
     * The ActionMap that stores 
     */
    private ActionMap actMap = null;
    /**
     * New DA action: Create a new dialog act and display it in edit pane
     */
    public static final String NEW_DA_ACTION = "NEW_DA_ACTION";
    /**
     * set comment action
     */
    public static final String SET_DA_COMMENT_ACTION = "SET_DA_COMMENT_ACTION";
    /**
     *Change type
     */
    public static final String CHANGE_DA_TYPE_ACTION = "CHANGE_DA_TYPE_ACTION";
    /**
     * Delete DA action: Delete the current dialog act in the editPane after asking for confirmation.
     */
    public static final String DELETE_DA_ACTION = "DELETE_DA_ACTION";
    /**
     * Delete DA action without confirm: Immediately delete the current dialog act in the editPane.
     */
    public static final String DELETE_DA_ACTION_NO_CONFIRM = "DELETE_DA_ACTION_NO_CONFIRM";
    /**
     * Change text range for DA.
     */
    public static final String CHANGE_DA_RANGE_ACTION = "CHANGE_DA_RANGE_ACTION";
    /**
     * Check addressee of a DA.
     */
    public static final String CHECK_ADDRESSEE_ACTION = "CHECK_ADDRESSEE_ACTION";
    /**
     * Check all participants as addressees of a DA.
     */
    public static final String CHECK_ALL_ADDRESSEES_ACTION = "CHECK_ALL_ADDRESSEES_ACTION";
    /**
     * Uncheck all addressees of a DA.
     */
    public static final String UNCHECK_ALL_ADDRESSEES_ACTION = "UNCHECK_ALL_ADDRESSEES_ACTION";
    /**
     * Check reflexivity checkbox.
     */
    public static final String CHECK_REFLEXIVITY_ACTION = "CHECK_REFLEXIVITY_ACTION";
    /**
     * Initializes (if still needed) and returns the ActionMap for this module.
     * The action names are stored in public variables in this class.
     */
    public ActionMap getActionMap() {
        if (actMap == null) {
            initActionMap();
        }
        return actMap;
    }
    /**
     * Initializes the actionmap.
     */
    protected void initActionMap() {
        actMap = new ActionMap();
        
        //set comment
        Action act = new SetCommentAction("Set Comment", this) {
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                refreshGUI();
                main.getNTV().displayAnnotationElement(currentElement);
            }
        };
        if (getClass().getResource("/eclipseicons/obj16/text_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/obj16/text_edit.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }
        act.putValue(Action.SHORT_DESCRIPTION,"Set a comment.");
        actMap.put(SET_DA_COMMENT_ACTION, act);
        
        //delete da action
        act = new AbstractAction("Delete...") {
            public void actionPerformed(ActionEvent ev) {
                deleteCurrentDA();
            }
        };
        if (getClass().getResource("/eclipseicons/etool16/delete_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/delete_edit.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }
        act.putValue(Action.SHORT_DESCRIPTION,"Delete current " + elementNameLong + " (asks for confirmation).");
        actMap.put(DELETE_DA_ACTION, act);

        //delete da action no confirm
        act = new AbstractAction("Delete!") {
            public void actionPerformed(ActionEvent ev) {
                deleteCurrentDANoConfirm();
            }
        };
        if (getClass().getResource("/eclipseicons/clcl16/remove.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/clcl16/remove.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }
        act.putValue(Action.SHORT_DESCRIPTION,"Delete current " + elementNameLong + " (no confirmation).");
        actMap.put(DELETE_DA_ACTION_NO_CONFIRM, act);
        
        //new da action 
        act = new AbstractAction("New " + elementNameShort) {
            public void actionPerformed(ActionEvent ev) {
                createNewDA();
            }
        };
        if (getClass().getResource("/eclipseicons/etool16/new_page.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/new_page.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }
        act.putValue(Action.SHORT_DESCRIPTION,"Create a new " + elementNameLong + " from the current selection.");
        actMap.put(NEW_DA_ACTION, act);
        
        //change da range action 
        act = new AbstractAction("Range...") {
            public void actionPerformed(ActionEvent ev) {//een functie als "startwaitingfornewrange"
                if (currentElement!=null) {
                    INTERNAL_STATE=WAITING;
                    Logger.global.info("Please select new text range for this " + elementNameLong);
                }
            }
        };
        if (getClass().getResource("/eclipseicons/etool16/segment_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/segment_edit.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }
        act.putValue(Action.SHORT_DESCRIPTION,"Change text range for " + elementNameLong);
        actMap.put(CHANGE_DA_RANGE_ACTION, act);
        
        //change da type
        act = new AbstractAction("Type...") {
            public void actionPerformed(ActionEvent ev) {
                if (currentElement!=null) {
                    Object o = ev.getSource();
                    if (o != null) {
                        showDATypePopupMenu(((Component)o).getX(), ((Component)o).getY());
                    } else {
                        showDATypePopupMenu();
                    }
                    Logger.global.info("Please select new type for this " + elementNameLong);
                }
            }
        };
        if (getClass().getResource("/eclipseicons/elcl16/tree_mode.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/elcl16/tree_mode.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }
        act.putValue(Action.SHORT_DESCRIPTION,"Change type for " + elementNameLong);
        actMap.put(CHANGE_DA_TYPE_ACTION, act);
        
        // check addressee
        act = new AbstractAction() {
            public void actionPerformed(ActionEvent ev) {
                if (currentElement != null)
                    setAddressees();
            }
        };
        act.putValue(Action.SHORT_DESCRIPTION,"Select an addressee for " + elementNameLong);
        actMap.put(CHECK_ADDRESSEE_ACTION, act);
        
        // check all addressees
        act = new AbstractAction("All") {
            public void actionPerformed(ActionEvent ev) {
                if (currentElement != null)
                    setAllAddressees(true);
            }
        };
        act.putValue(Action.SHORT_DESCRIPTION,"Select all participants as addressees for " + elementNameLong);
        actMap.put(CHECK_ALL_ADDRESSEES_ACTION, act);

        // uncheck all addressees
        act = new AbstractAction("None") {
            public void actionPerformed(ActionEvent ev) {
                if (currentElement != null)
                    setAllAddressees(false);
            }
        };
        act.putValue(Action.SHORT_DESCRIPTION,"Deselect all addressees for " + elementNameLong);
        actMap.put(UNCHECK_ALL_ADDRESSEES_ACTION, act);
        
        // check reflexivity
        act = new AbstractAction() {
            public void actionPerformed(ActionEvent ev) {
                if (currentElement != null)
                    setReflexivity();
            }
        };
        act.putValue(Action.SHORT_DESCRIPTION,"Set reflexivity for " + elementNameLong);
        actMap.put(CHECK_REFLEXIVITY_ACTION, act);
    }
    
    
/*===================
CORE FUNCTIONALITY
=====================*/

    /**
     * Deletes the current " + elementNameLong + " in the edit pane, without asking for confirmation.
     * If there are elements that point to the dialogue act to be deleted,
     * these elements will be deleted too. Currently the code assumes that only
     * adjacency pairs point to dialogue acts!
     *
     * This method will notify NOMViews of the change. This notification may involve the removing of some pointers as well!
     */
    protected void deleteCurrentDANoConfirm() {
        try{
            if (currentElement!=null){
                // find elements that point to current dialogue act, they will be deleted too
                List pointers = main.getCorpus().getPointersTo(currentElement);
                if (pointers != null) {
	                Iterator it = pointers.iterator();
	                while (it.hasNext()) {
	                    NOMPointer pointer = (NOMPointer)it.next();
	                    NOMElement src = pointer.getFromElement();
	                    if (src.getName().equals(((DACoderConfig)main.getConfig()).getAPElementName())) {
	                        NOMElement parent = (NOMElement)src.getParents().get(0);
		                    parent.getShared().deleteChild(main,src);
		                    if (main.getapPane()!= null) {
    	   	                    main.getapPane().setCurrentElement(null);
    		                    Logger.global.info("Deleted link pointing to deleted " + elementNameLong + ": " + src.getID());
    	   	                }
		                } else {
		                 	src.getShared().removePointer(main,pointer);
		                 	Logger.global.info("Removed pointer to deleted " + elementNameLong);
		                }
	                }
	            }
            	
                main.getNTV().undisplayAnnotationElement(currentElement);
                //if there is another display, e.g. a list, it should be refreshed as well!
                NOMWriteElement parent = (NOMWriteElement)currentElement.getParents().get(0);
                parent.getShared().deleteChild(main,currentElement);
                setCurrentElement(null);
                Logger.global.info(elementNameLong + " deleted");
            }   
        } catch (NOMException ex) {
            ex.printStackTrace();
        }	
    }	

    /**
     * Deletes the current dialogue act in the edit pane, after asking for confirmation.
     * Passes the actual delete request to the deleteNoConfirm action, which also notifies the NOMViews.
     */
    protected void deleteCurrentDA() {
        if (currentElement!=null){
            if (JOptionPane.showConfirmDialog(null, "Delete element " + currentElement.getID() + "?", "Confirm delete element", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {	
                deleteCurrentDANoConfirm();
            }
        } 
    }	
    
    /**
     * Create a new dialogue act from the current text element selection in the transcription view
     * and set it as current in the edit pane
     */
    protected void createNewDA() {
        try {
            Set transSelection = main.getNTV().getSelectedTransElements();
            if ((transSelection == null) || (transSelection.size() == 0)) {
                Logger.global.info("Please select some text first!");
                return;
            }
               
            NOMElement first = (NOMElement)transSelection.iterator().next();
               
            NOMWriteElement newDA= new NOMWriteAnnotation(main.getCorpus(),((DACoderConfig)main.getConfig()).getDAElementName(),main.getObservationName(), first.getAgentName());
            Iterator selectedTransIt = transSelection.iterator();
            while (selectedTransIt.hasNext()) {
                NOMWriteElement nextChild = (NOMWriteElement)selectedTransIt.next();
                boolean ok = nextChild.findAncestorNamed(((DACoderConfig)main.getConfig()).getDAElementName())==null; 
                if (!ok) { //this transcription element already is part of a dialog act!!
                    if ((newDA.getChildren() != null) && (newDA.getChildren().size() > 0)) {
                        break;
                    }
                } else {
                    newDA.addChild(nextChild);
                }
            }
                  
            if ((newDA.getChildren() != null) && (newDA.getChildren().size() > 0)) { //so, valid children were selected...
                newDA.getShared().addToCorpus(main);
                Logger.global.info(elementNameLong + " created");
                setCurrentElement(newDA);
                showDATypePopupMenu();
                main.getNTV().displayAnnotationElement(newDA);
            } else {
                Logger.global.info("Please select some UNLABELED text first!");
                return;
            }
        } catch (NOMException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Change the dialogue act type of the current element
     */
    public void setType(NOMElement newType) {
        //remove old pointer thePane.currentElement.
        try {        
            List l = currentElement.getPointers();
            List removePointers = new ArrayList();
            if (l != null) {
                Iterator it = l.iterator();
                while (it.hasNext()) {
                    NOMPointer p = (NOMPointer)it.next();
                    if (p.getRole().equals(((DACoderConfig)main.getConfig()).getDATypeRole())) {
                        removePointers.add(p);
                    }
                }
                for (int i =0;i<removePointers.size();i++) {
                    NOMPointer p = (NOMPointer)removePointers.get(i);
                    currentElement.getShared().removePointer(main,p);
                }
            }
            //add new pointer
            NOMWritePointer p = new NOMWritePointer(main.getCorpus(), ((DACoderConfig)main.getConfig()).getDATypeRole(), currentElement, newType);

            currentElement.getShared().addPointer(main,p);
        } catch (NOMException ex) {
            Logger.global.severe("Can't set type. Read stacktrace for more info");
            ex.printStackTrace();
        }
        
        main.getNTV().displayAnnotationElement(currentElement);
        refreshGUI();
        Logger.global.info("Type changed to " + newType.getAttributeComparableValue(((DACoderConfig)main.getConfig()).getDAAGloss()));
    }

    /**
     * Change the dialogue act type of the current element - this
     * version takes a String and assumes that getDAAttributeName is
     * non-null (i.e. we're using an enumerated attribute rather than
     * a type ontology). JK 22/3/06.
     */
    public void setType(String newType) {
	try {
	    currentElement.setStringAttribute(((DACoderConfig)main.getConfig()).getDAAttributeName(), newType);
        } catch (NOMException ex) {
            Logger.global.severe("Can't set type. Read stacktrace for more info");
            ex.printStackTrace();
        }
        main.getNTV().displayAnnotationElement(currentElement);
        refreshGUI();
        Logger.global.info("Type changed to " + currentElement.getAttributeComparableValue(((DACoderConfig)main.getConfig()).getDAAttributeName()));
    }

    protected void setRangeFromSelection() {
        Set transSelection = main.getNTV().getSelectedTransElements();
        if (transSelection.size() == 0) {
            INTERNAL_STATE=NORMAL;
            Logger.global.info("Incorrect text range. Cancelled");
            return;
        }
        final String ag = currentElement.getAgentName();
        Predicate correctAgent = new Predicate() {
            public boolean valid(Object o) {
                return ((NOMElement)o).getAgentName().equals(ag);
            }
        };
        Iterator newTextChildsIt = new IteratorFilter(transSelection.iterator(), correctAgent);
        if (!newTextChildsIt.hasNext()) {
            INTERNAL_STATE=NORMAL;
            Logger.global.info("Incorrect text range (wrong agent?). Cancelled");
            return;
        }
        try{	
            ArrayList newChildrenList= new ArrayList();
            while (newTextChildsIt.hasNext()){
                NOMWriteElement nextChild = (NOMWriteElement)newTextChildsIt.next();
                NOMElement dact = nextChild.findAncestorNamed(((DACoderConfig)main.getConfig()).getDAElementName());
                boolean ok = (dact==null) || (dact == currentElement); //overlap w/ existing dacts?
                if (!ok){
                    Logger.global.info("Overlap with existing " + elementNameLong + " removed...");
                    if(newChildrenList.size() > 0) {
                        break;
                    }
                } else {
                    newChildrenList.add(nextChild);
                }
            }
            if (newChildrenList.size()==0) {
                INTERNAL_STATE=NORMAL;
                Logger.global.info("Range not changed: impossible new range. Cancelled.");
                return;   
            } 	
            List oldChildren=currentElement.getChildren();
            if (oldChildren != null) {
                for (int i = oldChildren.size()-1; i >= 0; i--) {
                    NOMWriteElement oldchild = (NOMWriteElement)oldChildren.get(i);
                    currentElement.getShared().removeChild(main,oldchild);
                }
            }
            Iterator itSel=newChildrenList.iterator();
            while (itSel.hasNext()){
                NOMElement next= (NOMElement)itSel.next();
                currentElement.getShared().addChild(main,next);
            } 

            main.getNTV().displayAnnotationElement(currentElement);
            
            INTERNAL_STATE=NORMAL;
            Logger.global.info("Range changed.");
            refreshGUI();

        } catch (NOMException ex) {
            ex.printStackTrace();
        }	
    }
    
    /**
     * Reads the addressee check boxes and sets the addressee attribute of the
     * dialogue act.
     */
    private void setAddressees() {
        String addrStr = "";
        Iterator it = addresseeChecks.keySet().iterator();
        while (it.hasNext()) {
            String agent = (String)it.next();
            JCheckBox check = (JCheckBox)addresseeChecks.get(agent);
            if (check.isSelected()) {
                if (addrStr.length() > 0) addrStr += ",";
                addrStr += agent;
            }
        }
        try {
            currentElement.getShared().setStringAttribute(main,"addressee",addrStr);
            updateCheckAllAddresseesButton();
	        Logger.global.info("Addressee attribute set to: " + addrStr);
	     } catch (NOMException ex) {
            Logger.global.info("Error setting addressee attribute: " + ex.getMessage());
            refreshGUI();
        }
    }
    
    /**
     * Updates the All/None button for selecting addressees. If all selectable
     * participants have been selected as addressees, the button is set to
     * None. Otherwise it is set to All.
     */
    private void updateCheckAllAddresseesButton() {
        boolean allChecked = true;
        Iterator it = addresseeChecks.values().iterator();
        while (allChecked && it.hasNext()) {
            JCheckBox check = (JCheckBox)it.next();
            if (check.isEnabled() && !check.isSelected())
                allChecked = false;
        }
        if (allChecked)
            addrAllB.setAction(getActionMap().get(UNCHECK_ALL_ADDRESSEES_ACTION));
        else
            addrAllB.setAction(getActionMap().get(CHECK_ALL_ADDRESSEES_ACTION));
    }

    /**
     * If check is true, all participants are selected as addressees for the
     * dialogue act.
     * If check is false, all addressees are deselected.
     */
    private void setAllAddressees(boolean check) {
        Iterator it = addresseeChecks.values().iterator();
        while (it.hasNext()) {
            JCheckBox checkBox = (JCheckBox)it.next();
            if (checkBox.isEnabled())
                checkBox.setSelected(check);
        }
        setAddressees();
    }
    /** deactivate addresseechecks
    */
    protected void greyOutAddressees() {
        //System.out.println("greyaddr");
        Iterator it = addresseeChecks.values().iterator();
        while (it.hasNext()) {
            JCheckBox checkBox = (JCheckBox)it.next();
            checkBox.setEnabled(false);
        }

    }
    /**
     * Reads the reflexivity check box and sets the reflexivity attribute of
     * the dialogue act.
     */
    private void setReflexivity() {
        String reflexive = Boolean.toString(reflexivityCheck.isSelected());
        try {
            currentElement.getShared().setStringAttribute(main,"reflexivity",reflexive);
            Logger.global.info("Reflexivity attribute set to: " + reflexive);
        } catch (NOMException ex) {
            Logger.global.info("Error setting reflexivity attribute: " + ex.getMessage());
            refreshGUI();
        }
    }

/*===================
        GUI
=====================*/

    /**
     * The panel that contains the element edit GUI interface
     */
    private JPanel thePanel = null;
    public JComponent getPanel() {
        return thePanel;
    }

/*
 * The graphical components that make up the GUI panel.
 * The only elements that are STORED are those that have to be accessed again later.
 */
    //agent
    protected JLabel agentL = null;
    //main DA-type
    protected JLabel daTypeL = null;
    protected JButton setDATypeB = null;
    //fragment text
    protected JTextArea daText = null;
    protected JButton setRangeB = null;
    //addressee...
    protected AgentConfiguration agentConfig = null;
    protected Hashtable addresseeChecks = null;
    protected JPanel addresseePanel = null;
    protected JButton addrAllB = null;
    //reflexivity
    protected JCheckBox reflexivityCheck = null;
    //cancelling 
    //new element...
    protected JButton newDAB = null;
    //change comment
    protected JButton setCommentB = null;
    //delete element...
    protected JButton deleteDAB = null;
    //maps for key actions...
    protected InputMap imap = null;
    
    /**
     * Creates GUI objects, arranges them in formlayout.
     * stores resulting panel in thePanel attribute.
     */
    protected void createGUI() {
        ActionMap amap = getActionMap();
        FormLayout layout = new FormLayout(
            "left:pref, 3dlu, fill:100dlu:grow, 3dlu, pref", // columns
            "p, 3dlu, p, 3dlu, p, 3dlu, center:min:grow, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");      // rows
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        
            builder.addSeparator(elementNameLong,   cc.xyw(1,  1, 5));
            
            builder.addLabel("Agent:", cc.xy(1,  3));
        agentL = new JLabel("<none>");
            builder.add(agentL, cc.xy(3,  3));
            
            builder.addLabel(elementNameShort + "type:", cc.xy(1,  5));
        daTypeL = new JLabel("<none>");
            builder.add(daTypeL, cc.xy(3,  5));
        setDATypeB = new JButton(amap.get(CHANGE_DA_TYPE_ACTION));
            builder.add(setDATypeB, cc.xy(5,  5));
            
            builder.addLabel(elementNameShort + "text:", cc.xy(1,  7));
        daText = new JTextArea();
        daText.setLineWrap(true);
        daText.setWrapStyleWord(true);
        daText.setEditable(false);
        JScrollPane daTextScroll = new JScrollPane(daText);
            builder.add(daTextScroll, cc.xy(3,  7,"fill,fill"));
        setRangeB = new JButton(amap.get(CHANGE_DA_RANGE_ACTION));
            builder.add(setRangeB, cc.xy(5,  7,"fill,bottom"));

        agentConfig = new AgentConfiguration(main.getCorpus(),main.getObservationName());
        builder.addLabel("Addressee:", cc.xy(1,9));
        addresseeChecks = new Hashtable();
        addresseePanel = new JPanel();
        List agents = main.getMetaData().getAgents();
        Iterator it = agents.iterator();
        while (it.hasNext()) {
            NAgent agent = (NAgent)it.next();
            JCheckBox check = new JCheckBox(amap.get(CHECK_ADDRESSEE_ACTION));
            addresseeChecks.put(agent.getShortName(),check);
        }
        layoutAddresseeChecks();
        builder.add(addresseePanel, cc.xy(3,9));
        addrAllB = new JButton(amap.get(CHECK_ALL_ADDRESSEES_ACTION));
        builder.add(addrAllB, cc.xy(5,9));
        
        builder.addLabel("Reflexivity:", cc.xy(1,11));
        reflexivityCheck = new JCheckBox(amap.get(CHECK_REFLEXIVITY_ACTION));
        builder.add(reflexivityCheck, cc.xy(3,11));
        
        setCommentB = new JButton(amap.get(SET_DA_COMMENT_ACTION));
            builder.add(setCommentB, cc.xy(5,  13,"fill,center"));

        builder.addSeparator("",   cc.xyw(1,  15, 5));
            
        newDAB = new JButton(amap.get(NEW_DA_ACTION));
            builder.add(newDAB, cc.xyw(1,  17,2,"left,center"));
        deleteDAB = new JButton(amap.get(DELETE_DA_ACTION_NO_CONFIRM));
            builder.add(deleteDAB, cc.xy(5,  17,"fill,center"));

        
        thePanel = builder.getPanel();                
    }
    
    /**
     * No curr el, so clear all fields
     */
    protected void clearGUI() {
        agentL.setText("<none>");
        daTypeL.setText("<none>");
        setDATypeB.setEnabled(false);
        daText.setText("");
        setRangeB.setEnabled(false);
        setCommentB.setEnabled(false);
        deleteDAB.setEnabled(false);
        Iterator it = addresseeChecks.keySet().iterator();
        while (it.hasNext()) {
            String agent = (String)it.next();
            JCheckBox check = (JCheckBox)addresseeChecks.get(agent);
            check.setSelected(false);
            check.setEnabled(false);
            check.setText(agent);
            NOMElement person = main.getPersonForAgentName(agent);
            String name = agent;
            if (person != null) {
                String pname = (String)person.getAttributeComparableValue("name");
                if (pname != null)
                    name = pname;
            }
            check.setToolTipText(name);
        }
        addrAllB.setAction(getActionMap().get(CHECK_ALL_ADDRESSEES_ACTION));
        addrAllB.setEnabled(false);
        reflexivityCheck.setSelected(false);
        reflexivityCheck.setEnabled(false);
    }
    /**
     * refresh gui after change of currentElement (called automatically).
     * if curr null: clear. 
     * else enable all buttons, fill fields.
     *
     * if current element is of an 'ignoreaddressee' type, the addressee buttons are de-activated.
     */
    protected void refreshGUI() {
        if (currentElement == null) {
            clearGUI();
            return;
        }
        //NOMElement person = main.getPersonForAgentName(currentElement.getAgentName());
//        agentL.setText((String)person.getAttributeComparableValue("name"));
        agentL.setText(currentElement.getAgentName());
        //get type
        daTypeL.setText("<none>");
        NOMElement type = null;
	String dat = ((DACoderConfig)main.getConfig()).getDAAttributeName();
	if (dat==null || dat.length()==0) {
	    List l = currentElement.getPointers();
	    if (l != null) {
		Iterator it = l.iterator();
		while (it.hasNext()) {
		    NOMPointer p = (NOMPointer)it.next();
		    if (p.getRole().equals(((DACoderConfig)main.getConfig()).getDATypeRole())) {
			type = p.getToElement();
			daTypeL.setText((String)p.getToElement().getAttributeComparableValue(((DACoderConfig)main.getConfig()).getDAAGloss()));
		    }
		}
	    }
	} else {
	    daTypeL.setText((String)currentElement.getAttributeComparableValue(dat));
	}
        //get text
        String text = "";
        Iterator it = main.getNTV().getTranscriptionDescendants(currentElement).iterator();
        while (it.hasNext()) {
            text += main.getNTV().getTranscriptionText((NOMElement)it.next()) + " ";
        }
        //dit kan ook via getdisplaystrategy.gettext of zoiets!!!
        daText.setText(text);
        //enable buttons        
        setDATypeB.setEnabled(true);
        setRangeB.setEnabled(true);
        setCommentB.setEnabled(true);
        deleteDAB.setEnabled(true);

        String comm = currentElement.getComment();
        if ( comm != null && !comm.equals("")) {
            setCommentB.setText("Edit Comment");
        } else {
            setCommentB.setText("Set Comment");
        }
                
        // get addressees
        String addrStr = (String)currentElement.getAttributeComparableValue("addressee");
        String[] addressees = new String[0];
        if (addrStr != null)
            addressees = addrStr.split(",");
        String speaker = currentElement.getAgentName();
        it = addresseeChecks.keySet().iterator();
        while (it.hasNext()) {
            String agent = (String)it.next();
            JCheckBox check = (JCheckBox)addresseeChecks.get(agent);
            check.setSelected(false);
            check.setEnabled(speaker != null && !speaker.equals(agent));
        }
        for (int i = 0; i < addressees.length; i++) {
            JCheckBox check = (JCheckBox)addresseeChecks.get(addressees[i]);
	    if (check!=null) {
		check.setSelected(true);
	    }
        }
        updateCheckAllAddresseesButton();
        addrAllB.setEnabled(true);
        
        //System.out.println("grey?");
        //check if addressees should be greyed out
        if (!ignoreaddresseeattribute.equals("")&&type!= null) {
            //System.out.println("attr:"+ignoreaddresseeattribute);
            //System.out.println("value:"+(String)type.getAttributeComparableValue(ignoreaddresseeattribute));
            if (type.getAttributeComparableValue(ignoreaddresseeattribute)!=null) {
                if (((String)type.getAttributeComparableValue(ignoreaddresseeattribute)).toLowerCase().equals("true")) {
                    greyOutAddressees();
                }
            } 
        }
        
        // get reflexivity
        String reflexiveStr = (String)currentElement.getAttributeComparableValue("reflexivity");
        boolean reflexive = (reflexiveStr == null ? false : Boolean.valueOf(reflexiveStr).booleanValue());
        reflexivityCheck.setSelected(reflexive);
        reflexivityCheck.setEnabled(true);
    }

    public void setCurrentElement(NOMWriteElement element) {
        if (element != null) {
            if (!element.getName().equals(((DACoderConfig)main.getConfig()).getDAElementName())) {
                System.out.println("Wrong type of element to edit in DAEditPane : " + element.getName());
                return;
            }
        }
        currentElement = element;
        refreshGUI();
    }
    public NOMWriteElement getCurrentElement() {
        return currentElement;
    }  

    /**
     * Show the Ontology popup menu that will change the DA type of the current
     * dialogue act in the daEditPane
     */
    protected void showDATypePopupMenu() {
        Point p = main.getNTV().getCaret().getMagicCaretPosition() ;
	jmenu.show(main.getNTV(),(int)p.getX(),(int)p.getY()+20);
    }
    /**
     * Show the Ontology popup menu that will change the DA type of the current
     * dialogue act in the daEditPane
     */
    protected void showDATypePopupMenu(int x, int y) {
	jmenu.show(thePanel,x,y);
    }
    /**
     * Called for ontology popup menu. Change type...
     */
    public void actionPerformed (ActionEvent ev) {
        if (currentElement==null) {
            return;
        }
        Object src = ev.getSource();
        if (src instanceof OntologyPopupMenu.NOMElementContainer) {
            NOMElement type = ((OntologyPopupMenu.NOMElementContainer)src).getElement();
            //check elementname of type ? for now only one thing can fire actionperformed...
            setType(type);
        } else {
            setType(((JMenuItem)src).getText());
	}
    }
    /**
     * Depending on the state of the editormodule ,
     * this method reacts appropriately to selection changes (change range, or set current element
     */
    public void selectionChanged() {
        //waiting for range select?
        if (INTERNAL_STATE==WAITING) {
            setRangeFromSelection();
            return;
        }
        //otherwise: it's just a 'set current'.
        Set selectedAnnotationElements=main.getNTV().getSelectedAnnoElements();
        /* selected dialogue act? set as current element */
        setCurrentElement(null);
        if (!selectedAnnotationElements.isEmpty()) {
            Iterator it=selectedAnnotationElements.iterator();
            while (it.hasNext()){
                NOMWriteElement selectElem=(NOMWriteElement)it.next();
                if (selectElem.getName().equals(((DACoderConfig)main.getConfig()).getDAElementName())) {
                    setCurrentElement(selectElem);
                }
            } 	
        }
    }
    
    public void layoutAddresseeChecks() {
        addresseePanel.removeAll();
        Dimension dim = agentConfig.getDimension(currentSignal);
        addresseePanel.setLayout(new GridLayout(dim.height,dim.width));
        for (int y = 0; y < dim.height; y++) {
            for (int x = 0; x < dim.width; x++) {
                String agent = agentConfig.getAgentAt(currentSignal,x,y);
                JCheckBox check = null;
                if (agent != null)
                    check = (JCheckBox)addresseeChecks.get(agent);
                if (check == null)
                    addresseePanel.add(new JPanel());
                else
                    addresseePanel.add(check);
            }
        }
        if (thePanel != null)
            thePanel.validate();
    }

    public void signalChanged(String name) {
        this.currentSignal = name;
        layoutAddresseeChecks();
    }

    /**
     * Change the full name of the element type to be displayed and
     * edited in this panel (default: "Dialogue Act")
     */
    private void setLongName(String name) {
	elementNameLong=name;
    }

    /**
     * Change the abbreviated name of the element type to be displayed and
     * edited in this panel (default: "DA")
     */
    private void setShortName(String name) {
	elementNameShort=name;
    }

}
