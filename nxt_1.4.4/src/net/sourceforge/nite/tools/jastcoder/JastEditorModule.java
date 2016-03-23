package net.sourceforge.nite.tools.jastcoder;

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
import net.sourceforge.nite.tools.necoder.*;

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
public class JastEditorModule implements NTASelectionListener,ActionListener,SignalListener,NOMWriteElementContainer {
    public static final int NORMAL=0;
    public static final int WAITING=1; //waiting for WHAT? (range)
    private int INTERNAL_STATE=NORMAL; //should be state of edit pane!!!!!!!!!!!
    JPopupMenu jmenu;
    String ignoreaddresseeattribute = "";
    /** 
     * The name of the elements edited by this module (added Jonathan Kilgour 27/4/06)
     */ 
    protected String elementNameLong = "Referring Expression";
    protected String elementNameShort = "RE";

/*==================
Initialization, connection to main tool
====================*/
    //main param vervangen door config, ntv en corpus?
    public JastEditorModule(JastCoder main) {
        this.main = main;
        
	ignoreaddresseeattribute = "";
        main.getNTV().addNTASelectionListener(this);
        createGUI();
        setCurrentElement(null);
    }


    /** Alternative constructor that sets the long and short names of
     * the discourse entities to be edited in this window (defaults:
     * "Referring Expression"; "RE"). */
    public JastEditorModule(JastCoder main, String longname, String shortname) {
        this.main = main;

	setLongName(longname);
	setShortName(shortname);
        createGUI();
        setCurrentElement(null);
    }

    /**
     * For reference to NTV, observationname, nomcorpus......
     */
    JastCoder main;

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
     * Check ispart checkbox.
     */
    public static final String CHECK_ISPART_ACTION = "CHECK_ISPART_ACTION";

     /**
     * Change accessability value.
     */
    public static final String CHANGE_ACCESS_ACTION = "CHANGE_ACCESS_ACTION";
 
    /**
     * Check deixis checkbox.
     */
    public static final String CHECK_DEIXIS_ACTION = "CHECK_DEIXIS_ACTION";

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

        // check ispart
        act = new AbstractAction() {
            public void actionPerformed(ActionEvent ev) {
                if (currentElement != null)
                    setIspart();
            }
        };
        act.putValue(Action.SHORT_DESCRIPTION,"Set ispart for " + elementNameLong);
        actMap.put(CHECK_ISPART_ACTION, act);

        // check deixis
        act = new AbstractAction() {
            public void actionPerformed(ActionEvent ev) {
                if (currentElement != null)
                    setDeixis();
            }
        };
        act.putValue(Action.SHORT_DESCRIPTION,"Set deixis for " + elementNameLong);
        actMap.put(CHECK_DEIXIS_ACTION, act);

        // check accessibility
        act = new AbstractAction() {
            public void actionPerformed(ActionEvent ev) {
                if (currentElement != null)
                    setAccessibility();
            }
        };
        act.putValue(Action.SHORT_DESCRIPTION,"Set accessibility for " + elementNameLong);
        actMap.put(CHANGE_ACCESS_ACTION, act);
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
	                    if (src.getName().equals(((NECoderConfig)main.getConfig()).getNEElementName())) {
	                        NOMElement parent = (NOMElement)src.getParents().get(0);
		                    parent.getShared().deleteChild(main,src);
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
                NOMElement dact = nextChild.findAncestorNamed(((NECoderConfig)main.getConfig()).getNEElementName());
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
     * Reads the ispart check box and sets the ispart attribute of
     * the referring expression.
     */
    private void setIspart() {
        String ispart = Boolean.toString(ispartCheck.isSelected());
        try {
            currentElement.getShared().setStringAttribute(main,"ispart",ispart);
            Logger.global.info("ispart attribute set to: " + ispart);
        } catch (NOMException ex) {
            Logger.global.info("Error setting ispart attribute: " + ex.getMessage());
            refreshGUI();
        }
    }

     /**
     * Reads the deixis check box and sets the deixis attribute of
     * the referring expression.
     */
    private void setDeixis() {
        String deixis = Boolean.toString(deixisCheck.isSelected());
        try {
            currentElement.getShared().setStringAttribute(main,"deixis",deixis);
            Logger.global.info("Deixis attribute set to: " + deixis);
        } catch (NOMException ex) {
            Logger.global.info("Error setting deixis attribute: " + ex.getMessage());
            refreshGUI();
        }
    }
 
     /**
     * Reads the accessibility combo box and sets the accessibility attribute of
     * the referring expression.
     */
    private void setAccessibility() {
        String access = (String)(accessibilityList.getSelectedItem());
        try {
            currentElement.getShared().setStringAttribute(main,"accessibility",access);
            Logger.global.info("accessibility attribute set to: " + access);
        } catch (NOMException ex) {
            Logger.global.info("Error setting accessibility attribute: " + ex.getMessage());
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
    //deixis
    protected JCheckBox deixisCheck = null;
    protected JLabel deixisLabel = null;
    //ispart
    protected JCheckBox ispartCheck = null;
    protected JLabel ispartLabel = null;
    //Accessibility
    protected JComboBox accessibilityList = null;
    protected JLabel accessibilityLabel = null;
    //maps for key actions...
    protected InputMap imap = null;
    
    /**
     * Creates GUI objects, arranges them in formlayout.
     * stores resulting panel in thePanel attribute.
     */
    protected void createGUI() {
        ActionMap amap = getActionMap();
        FormLayout layout = new FormLayout(
            "left:pref, 3dlu, fill:10dlu:grow, 3dlu, pref", // columns
            "p, p, p, 3dlu, p, 3dlu, p, 3dlu, p");      // rows
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator(elementNameLong,   cc.xyw(1,  1, 5));
        
	ispartLabel = new JLabel("Part of Object:");
        builder.add(ispartLabel, cc.xy(1,3));
        ispartCheck = new JCheckBox(amap.get(CHECK_ISPART_ACTION));
        builder.add(ispartCheck, cc.xy(3,3));

	deixisLabel = new JLabel("Deixis:");
        builder.add(deixisLabel, cc.xy(1,5));
        deixisCheck = new JCheckBox(amap.get(CHECK_DEIXIS_ACTION));
        builder.add(deixisCheck, cc.xy(3,5));
        
	accessibilityLabel = new JLabel("Accessibility:");
        builder.add(accessibilityLabel, cc.xy(1,7));
	//TODO: read these from a config file?
        String[] access = {"0", "1", "1.5", "2", "2.5", "3", "4"};
        accessibilityList = new JComboBox(access);
        accessibilityList.setAction(amap.get(CHANGE_ACCESS_ACTION));
        builder.add(accessibilityList, cc.xy(3,7));

        thePanel = builder.getPanel();                
    }
    
    /**
     * No curr el, so clear all fields
     */
    protected void clearGUI() {
        ispartCheck.setSelected(false);
        ispartCheck.setEnabled(false);
	ispartLabel.setEnabled(false);

        deixisCheck.setSelected(false);
        deixisCheck.setEnabled(false);
	deixisLabel.setEnabled(false);

        accessibilityList.setSelectedIndex(0);
        accessibilityList.setEnabled(false);
	accessibilityLabel.setEnabled(false);
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

        // get ispart
        String ispartStr = (String)currentElement.getAttributeComparableValue("ispart");
        boolean ispart = (ispartStr == null ? false : Boolean.valueOf(ispartStr).booleanValue());
        ispartCheck.setSelected(ispart);
        ispartCheck.setEnabled(true);
	ispartLabel.setEnabled(true);

        // get deixis
        String deixisStr = (String)currentElement.getAttributeComparableValue("deixis");
        boolean deixis = (deixisStr == null ? false : Boolean.valueOf(deixisStr).booleanValue());
        deixisCheck.setSelected(deixis);
        deixisCheck.setEnabled(true);
	deixisLabel.setEnabled(true);

        // get accessibility
        String accessStr = (String)currentElement.getAttributeComparableValue("accessibility");
        //int access = (accessStr == null ? 0 : Integer.valueOf(accessStr).intValue());
        accessibilityList.setSelectedItem(accessStr);
        accessibilityList.setEnabled(true);
	accessibilityLabel.setEnabled(true);
        
    }

    public void setCurrentElement(NOMWriteElement element) {
        if (element != null) {
            if (!element.getName().equals(((NECoderConfig)main.getConfig()).getNEElementName())) {
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
        } else {
            return;
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
                if (selectElem.getName().equals(((NECoderConfig)main.getConfig()).getNEElementName())) {
                    setCurrentElement(selectElem);
                }
            } 	
        }
    }

    public void signalChanged(String name) {
        this.currentSignal = name;
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
