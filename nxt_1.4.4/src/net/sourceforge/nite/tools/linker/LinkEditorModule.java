package net.sourceforge.nite.tools.linker;
import java.util.logging.*;
import java.io.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.*;
import com.jgoodies.looks.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import net.sourceforge.nite.tools.necoder.NECoderConfig;
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.util.*;
import net.sourceforge.nite.search.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.nom.link.NOMView;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.nstyle.handler.*;
import net.sourceforge.nite.time.*;


/** A display to edit a single link between other elements. Requires
 * links to displays of the elements it is changing and ability to
 * communicate changes to any external display that may be interested
 * (like a LinkDisplayModule). */
public class LinkEditorModule implements NTASelectionListener,ActionListener,NOMWriteElementContainer {

    AbstractCallableTool main;
    AbstractDisplayElement linkdisplay;
    AbstractDisplayElement sourcedisplay;
    AbstractDisplayElement targetdisplay;
    String linkSourceRole;
    String linkTargetRole;
    NOMView mainview=null;

    /** 
     * The name of the link elements (added Jonathan Kilgour 27/4/06)
     */ 
    protected String linkNameLong = "Adjacency Pair";
    protected String linkNameShort = "AP";

    JPopupMenu jmenu;
    //OntologyPopupMenu ontPopupMenu;

    Style yellowStyle;
    Style orangeStyle;
    protected void initStyles() {
	yellowStyle = StyleContext.getDefaultStyleContext().addStyle("YELLOW_AP",null);
	yellowStyle.addAttribute(StyleConstants.ColorConstants.Background, Color.yellow);
	orangeStyle = StyleContext.getDefaultStyleContext().addStyle("ORANGE_AP",null);
	orangeStyle.addAttribute(StyleConstants.ColorConstants.Background, Color.orange);
    }
    
    /*==================
      Initialization, connection to main tool
      ====================*/
    public LinkEditorModule(AbstractCallableTool main, AbstractDisplayElement linkd,
			    AbstractDisplayElement sourced, AbstractDisplayElement targetd,
			    String source, String target) {
        this.main = main;
	this.sourcedisplay=sourced;
	this.targetdisplay=targetd;
	this.linkdisplay=linkd;
	linkSourceRole=source;
	linkTargetRole=target;
	linkNameLong=linkdisplay.getElementNameLong();
	if (linkNameLong==null) { linkNameLong=linkdisplay.getElementName(); }
	linkNameShort=linkdisplay.getElementNameShort();
	if (linkNameShort==null) { linkNameShort=linkNameLong; }

	String neatt = main.getConfig().getNXTConfig().getCorpusSettingValue("nelinkattribute");
	if (neatt==null || neatt.length()==0) {
	    jmenu=new OntologyPopupMenu(main.getCorpus(), linkdisplay.getTypeGloss(), this, linkdisplay.getTypeOntologyRoot(), false, false);
	} else {
	    jmenu = new EnumeratedPopupMenu(main.getCorpus(), main.getConfig().getNXTConfig().getCorpusSettingValue("nelinkelementname"), neatt, this);
	}
        initStyles();
        main.getNTV().addNTASelectionListener(this);
        createGUI();
        setCurrentElement(null);
    }

    /**
     * The current AP element
     */
    protected NOMWriteElement currentElement = null;


    /** set the NOM view that is in charge - i.e. the one that sets
     * locks and doesn't get notified of any changes. */
    protected void setNOMView(NOMView nv) {
	mainview=nv;
    }

/*===============
    ACTIONS
=====================*/

    /**
     * The ActionMap that stores 
     */
    private ActionMap actMap = null;
    /**
     */
    public static final String NEW_LINK_ACTION = "NEW_LINK_ACTION";
    public static final String DELETE_LINK_ACTION = "DELETE_LINK_ACTION";
    public static final String CHANGE_LINK_TYPE_ACTION = "CHANGE_LINK_TYPE_ACTION";
    public static final String CHANGE_LINK_SOURCE_ACTION = "CHANGE_LINK_SOURCE_ACTION";
    public static final String CHANGE_LINK_TARGET_ACTION = "CHANGE_LINK_TARGET_ACTION";
    public static final String CANCEL_ACTION = "CANCEL LINK ACTION";
    public static final String SET_LINK_COMMENT_ACTION = "SET_LINK_COMMENT_ACTION";
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
        actMap.put(SET_LINK_COMMENT_ACTION, act);
        
        act = new AbstractAction("New " + linkNameShort) {
            public void actionPerformed(ActionEvent ev) {
                if (neutralizeState()) {
                    createNewElement();
                }
            }
        };
        if (getClass().getResource("eclipseicons/etool16/new_page.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("eclipseicons/etool16/new_page.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }
        act.putValue(Action.SHORT_DESCRIPTION,"Create new " + linkNameLong);
        actMap.put(NEW_LINK_ACTION, act);

        act = new AbstractAction("Delete") {
            public void actionPerformed(ActionEvent ev) {
                if (neutralizeState()) {
                    deleteElement();
                }
            }
        };
        if (getClass().getResource("eclipseicons/etool16/delete_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("eclipseicons/etool16/delete_edit.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }
        act.putValue(Action.SHORT_DESCRIPTION,"Delete current " + linkNameLong);
        actMap.put(DELETE_LINK_ACTION, act);

        
        act = new AbstractAction("Type...") {
            public void actionPerformed(ActionEvent ev) {
                if (neutralizeState()) {
                    Logger.global.info("Please select type from popup menu...");
                    Object o = ev.getSource();
                    if (o != null) {
                        showLinkTypePopupMenu(((Component)o).getX(), ((Component)o).getY());
                    } else {
                        showLinkTypePopupMenu();
                    }
                }
            }
        };
        if (getClass().getResource("eclipseicons/elcl16/tree_mode.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("eclipseicons/elcl16/tree_mode.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }
        act.putValue(Action.SHORT_DESCRIPTION,"Change " + linkNameShort + " relation type...");
        actMap.put(CHANGE_LINK_TYPE_ACTION, act);

        act = new AbstractAction("Source...") {
            public void actionPerformed(ActionEvent ev) {
                startWaitingForSource();
            }
        };
        act.putValue(Action.SHORT_DESCRIPTION,"Select source in text...");
        actMap.put(CHANGE_LINK_SOURCE_ACTION, act);
        
        act = new AbstractAction("Target...") {
            public void actionPerformed(ActionEvent ev) {
                startWaitingForTarget();
            }
        };
        act.putValue(Action.SHORT_DESCRIPTION,"Select target in text...");
        actMap.put(CHANGE_LINK_TARGET_ACTION, act);
        
        act = new AbstractAction("Cancel...") {
            public void actionPerformed(ActionEvent ev) {
                neutralizeState();
            }
        };
        act.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke("ESCAPE"));
        actMap.put(CANCEL_ACTION, act);
    }
    
    
/*===================
CORE FUNCTIONALITY
=====================*/
    public void setDefaultType(NOMElement nme) {
        defaultType = nme;
    }


    /**        
     * create new, then use setcurrent on new.
     */
    public void createNewElement() {
        NOMWriteElement newElement = null;
        try {
            newElement = new NOMWriteAnnotation(main.getCorpus(),linkdisplay.getElementName(), main.getObservationName(), (String)null);
            newElement.getShared().addToCorpus(mainview);
            Logger.global.info("New " + linkNameLong + " created");
        } catch (NOMException ex) {
            ex.printStackTrace();
            Logger.global.severe("Serious error. can't create new " + linkNameLong + ". Refer to stacktrace for details");
        }
		setCurrentElement(newElement);	
		if (defaultType != null) {
		    setType(defaultType);
		}
		startWaitingForSourceAndTarget();
    }
    /**        
     * delete, then setcurrent null.
     */
    public void deleteElement() {
        try {
            ((NOMElement)currentElement.getParents().get(0)).getShared().deleteChild(mainview,currentElement);
            //@@@apDisplay.removeDisplayComponent(new NOMObjectModelElement(currentElement));
            Logger.global.info("Element deleted");
        } catch (NOMException ex) {
            ex.printStackTrace();
            Logger.global.severe("Serious error. can't delete " + linkNameLong + ". Refer to stacktrace for details");
        }
		setCurrentElement(null);	
    }
    public void setType(NOMElement newType) {
        NOMWriteElement nwe = null;
        try {
            nwe = (NOMWriteElement)newType;
        } catch (ClassCastException ex)  {
            Logger.global.severe("Can't change type: element is non-write");
            return;
        }
        //remove old pointer thePane.currentElement.
        List l = currentElement.getPointers();
        List removePointers = new ArrayList();
        if (l != null) {
            Iterator it = l.iterator();
            while (it.hasNext()) {
                NOMPointer p = (NOMPointer)it.next();
                if (p.getRole().equals(linkdisplay.getTypeRole())) {
                    removePointers.add(p);
                }
            }
            try {
                for (int i =0;i<removePointers.size();i++) {
                    NOMPointer p = (NOMPointer)removePointers.get(i);
                    currentElement.getShared().removePointer(mainview,p);
                }
            } catch (NOMException ex) {
                Logger.global.severe("Can't set target. Read stacktrace for more info");
                ex.printStackTrace();
            }
        }
        //add new pointer
        NOMWritePointer p = new NOMWritePointer(main.getCorpus(), linkdisplay.getTypeRole(), currentElement, newType);
        try {
            currentElement.getShared().addPointer(mainview,p);
        } catch (NOMException ex) {
            Logger.global.severe("Can't set type. Read stacktrace for more info");
            ex.printStackTrace();
        }
        
        refreshGUI();
	String stype = (String)newType.getAttributeComparableValue(linkdisplay.getTypeGloss());
	if ((stype == null) || (stype.equals(""))) {
	    stype = (String)newType.getAttributeComparableValue("name");
	}            

        Logger.global.info("Type changed to " + stype );
    }

    /**
     * Change the link type of the current element - this
     * version takes a String and assumes that getDAAttributeName is
     * non-null (i.e. we're using an enumerated attribute rather than
     * a type ontology). JK 22/3/06.
     */
    public void setType(String newType) {
	try {
	    String neatt = main.getConfig().getNXTConfig().getCorpusSettingValue("nelinkattribute");
	    currentElement.setStringAttribute(neatt, newType);
        } catch (Exception ex) {
            Logger.global.severe("Can't set type. Read stacktrace for more info");
            ex.printStackTrace();
        }
        refreshGUI();
        Logger.global.info("Link type changed to " + newType);
    }

    public void setSource(NOMElement newSource) {
        NOMWriteElement nwe = null;
        try {
            nwe = (NOMWriteElement)newSource;
        } catch (ClassCastException ex)  {
            Logger.global.severe("Can't change source: element is non-write");
            return;
        }
        //remove old pointer 
        List l = currentElement.getPointers();
        List removePointers = new ArrayList();
        if (l != null) {
            Iterator it = l.iterator();
            while (it.hasNext()) {
                NOMPointer p = (NOMPointer)it.next();
                if (p.getRole().equals(linkSourceRole)) {
                    removePointers.add(p);
                }
            }
            try {
                for (int i =0;i<removePointers.size();i++) {
                    NOMPointer p = (NOMPointer)removePointers.get(i);
                    currentElement.getShared().removePointer(mainview,p);
                }
            } catch (NOMException ex) {
                Logger.global.severe("Can't set target. Read stacktrace for more info");
                ex.printStackTrace();
            }
        }
        //add new pointer
        NOMWritePointer p = new NOMWritePointer(main.getCorpus(), linkSourceRole, currentElement, newSource);
        try {
            currentElement.getShared().addPointer(mainview,p);
        } catch (NOMException ex) {
            Logger.global.severe("Can't set source Read stacktrace for more info");
            ex.printStackTrace();
        }
        
        refreshGUI();
        Logger.global.info("Source changed");
    }
    public void setTarget(NOMElement newTarget) {
        NOMWriteElement nwe = null;
        try {
            nwe = (NOMWriteElement)newTarget;
        } catch (ClassCastException ex)  {
            Logger.global.severe("Can't change target: element is non-write");
            return;
        }
        //remove old pointer 
        List l = currentElement.getPointers();
        List removePointers = new ArrayList();
        if (l != null) {
            Iterator it = l.iterator();
            while (it.hasNext()) {
                NOMPointer p = (NOMPointer)it.next();
                if (p.getRole().equals(linkTargetRole)) {
                    removePointers.add(p);
                }
            }
            try {
                for (int i =0;i<removePointers.size();i++) {
                    NOMPointer p = (NOMPointer)removePointers.get(i);
                    currentElement.getShared().removePointer(mainview,p);
                }
            } catch (NOMException ex) {
                Logger.global.severe("Can't set target. Read stacktrace for more info");
                ex.printStackTrace();
            }
        }
        //add new pointer
        NOMWritePointer p = new NOMWritePointer(main.getCorpus(), linkTargetRole, currentElement, newTarget);
        try {
            currentElement.getShared().addPointer(mainview,p);
        } catch (NOMException ex) {
            Logger.global.severe("Can't set target. Read stacktrace for more info");
            ex.printStackTrace();
        }
        
        refreshGUI();
        Logger.global.info("Target changed");
    }
    /** 
     * checks if at least source, target and type are filled in.
     * Pre: expects adjacencypair input.
     */
    protected boolean minimallyComplete(NOMElement link) {
        boolean typeFilled = false;
        boolean sourceFilled = false;
        boolean targetFilled = false;
        List pointers = link.getPointers();
        if (pointers==null) {
            return false;
        }
        Iterator pointersIt = pointers.iterator();
        while (pointersIt.hasNext()) {
            NOMPointer p = (NOMPointer)pointersIt.next();
            //type
            if (p.getRole().equals(linkdisplay.getTypeRole())) {
                typeFilled = true;
            }
            //source
            if (p.getRole().equals(linkSourceRole)) {
                sourceFilled = true;
            }
            //target
            if (p.getRole().equals(linkTargetRole)) {
                targetFilled = true;
            }
        }
        return (typeFilled && sourceFilled && targetFilled);
    } 
    
    

/*===================
        SOMEWHERE BETWEEN GUI AND CORE: states
=====================*/
    
    protected static final int NORMAL = 0;
    protected static final int WAITING_FOR_SOURCE = 1;
    protected static final int WAITING_FOR_TARGET = 2;
    protected static final int WAITING_FOR_SOURCE_AND_TARGET = 3;
    protected int state = NORMAL;

    /**
     * The editor has an internal state to signify the state of interaction.
     * e.g. 'waitingforsource' means that the system is waiting for the user to select 
     * a source dialog act.
     *
     * This method  tries to neutralize a previous state, e.g. in response to 'escape'
     */
    protected boolean neutralizeState() {
        switch (state) {
            case NORMAL:
                return true;
            case WAITING_FOR_SOURCE:
                Logger.global.info("Selection of source cancelled");
                state = NORMAL;
                return true;
            case WAITING_FOR_TARGET:
                Logger.global.info("Selection of target cancelled");
                state = NORMAL;
                return true;
            case WAITING_FOR_SOURCE_AND_TARGET:
                Logger.global.info("Selection of source and target cancelled");
                state = NORMAL;
                return true;
        }
        Logger.global.info("State error, set to normal");
        state = NORMAL;
        return true;
    }
    /**
     * Puts editor in state such that user select of dact will be set as source...
     */
    protected void startWaitingForSource() {
        if (!neutralizeState()) {
            return;
        }
        state = WAITING_FOR_SOURCE;
        Logger.global.info("Please select source");
    }
    /**
     * Puts editor in state such that user select of dact will be set as target...
     */
    protected void startWaitingForTarget() {
        if (state != WAITING_FOR_SOURCE_AND_TARGET) {
            if (!neutralizeState()) {
                return;
            }
        }
        state = WAITING_FOR_TARGET;
        Logger.global.info("Please select target");
    }
    /**
     * Puts editor in state such that user select of dact will be set as source, then target...
     */
    protected void startWaitingForSourceAndTarget() {
        if (!neutralizeState()) {
            return;
        }
        state = WAITING_FOR_SOURCE_AND_TARGET;
        Logger.global.info("Please select source");
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
 * COMPLETELY DIFFERENT
 */
    //source
    protected JLabel sourceL = null;
    protected JTextArea sourceText = null;
    protected JButton setSourceB = null;
    //type
    protected JButton setTypeB = null;
    protected JLabel typeL = null;
    protected NOMElement defaultType = null;
    //target
    protected JLabel targetL = null;
    protected JTextArea targetText = null;
    protected JButton setTargetB = null;
    //cancelling settarget/source...
    protected JButton cancelB = null;
    //new element...
    protected JButton newAPB = null;
    //delete element...
    protected JButton deleteAPB = null;
    //change comment
    protected JButton setCommentB = null;

    /**
     * Creates GUI objects, arranges them in formlayout.
     * stores resulting panel in thePanel attribute.
     */
    protected void createGUI() {
        ActionMap amap = getActionMap();
        FormLayout layout = new FormLayout(
            "7dlu, right:pref, 3dlu, center:70dlu:grow, 3dlu, pref", // columns
            "p, 3dlu, center:min:grow, 5dlu, p, 5dlu, center:min:grow,3dlu,p,3dlu,p,3dlu,p");      // rows
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        
            builder.addSeparator(linkNameLong,   cc.xyw(1,  1, 6));
        
        sourceL = new JLabel("<no source>");
            builder.add(sourceL, cc.xy(6,  3,"left,bottom"));
        sourceText = new JTextArea();
        sourceText.setLineWrap(true);
        sourceText.setWrapStyleWord(true);
        sourceText.setEditable(false);
        JScrollPane sourceScroll = new JScrollPane(sourceText);
            builder.add(sourceScroll, cc.xy(4,  3,"fill,fill"));
        setSourceB = new JButton(amap.get(CHANGE_LINK_SOURCE_ACTION));
            builder.add(setSourceB, cc.xy(2,  3,"fill,top"));
        targetL = new JLabel("<no target>");
            builder.add(targetL, cc.xy(6,  5,"left,bottom"));
        targetText = new JTextArea();
        targetText.setLineWrap(true);
        targetText.setWrapStyleWord(true);
        targetText.setEditable(false);
        JScrollPane targetScroll = new JScrollPane(targetText);
            builder.add(targetScroll, cc.xy(4,  5,"fill,fill"));
        setTargetB = new JButton(amap.get(CHANGE_LINK_TARGET_ACTION));
            builder.add(setTargetB, cc.xy(2,  5,"fill,top"));
        typeL = new JLabel("<no relation type>");
            builder.add(typeL, cc.xyw(4,  7, 3, "left,center"));
        setTypeB = new JButton(amap.get(CHANGE_LINK_TYPE_ACTION));
            builder.add(setTypeB, cc.xy(2,  7));
        
        setCommentB = new JButton(amap.get(SET_LINK_COMMENT_ACTION));
            builder.add(setCommentB, cc.xy(2,  9,"fill,center"));

            builder.addSeparator("",   cc.xyw(1,  11, 6));
        newAPB = new JButton(amap.get(NEW_LINK_ACTION));
            builder.add(newAPB, cc.xy(2,  13,"left,center"));
        deleteAPB = new JButton(amap.get(DELETE_LINK_ACTION));
            builder.add(deleteAPB, cc.xy(4,  13,"right,center"));

        thePanel = builder.getPanel();                
    }

    /**
     * No curr el, so clear all fields
     */
    protected void clearGUI() {
        sourceText.setText("");
        sourceL.setText("<no source>");
        targetText.setText("");
        targetL.setText("<no target>");
        typeL.setText("<no relation type>");
        setSourceB.setEnabled(false);
        setTypeB.setEnabled(false);
        setTargetB.setEnabled(false);
        setCommentB.setEnabled(false);
        deleteAPB.setEnabled(false);
    }
    /**
     * refresh gui after change of currentElement (called automatically).
     * if curr null: clear. 
     * else enable all buttons, fill fields.
     */
    protected void refreshGUI() {
        if (currentElement == null) {
            clearGUI();
            main.getNTV().clearHighlights(NTextArea.SELECTION_HIGHLIGHTS);
            notifyLinkChangeListeners();
            return;
        }
        //enable the buttons
        setSourceB.setEnabled(true);
        setTypeB.setEnabled(true);
        setTargetB.setEnabled(true);
        deleteAPB.setEnabled(true);
        setCommentB.setEnabled(true);
        
        String comm = currentElement.getComment();
        if ( comm != null && !comm.equals("")) {
            setCommentB.setText("Edit Comment");
        } else {
            setCommentB.setText("Set Comment");
        }
        
        sourceL.setText("<no da type>");
        targetL.setText("<no da type>");
        typeL.setText("<no relation type>");
        sourceText.setText("");
        targetText.setText("");
        //get the pointers, extract source, target and type info from the relevant pointers
        List pointers = currentElement.getPointers();
        //[DR: experimental line of code...
        main.getNTV().clearHighlights(NTextArea.USER_HIGHLIGHTS);
        if (pointers!=null) {
            Iterator pointersIt = pointers.iterator();
            while (pointersIt.hasNext()) {
                NOMPointer p = (NOMPointer)pointersIt.next();
                //type
		//System.out.println("Pointer: " + p + "; role: " + p.getRole());
		if (p==null) continue;
                if (p.getRole().equals(linkdisplay.getTypeRole())) {
                    //get relation type
                    NOMElement type = p.getToElement();
                    String s = (String)type.getAttributeComparableValue(linkdisplay.getTypeGloss());
                    if ((s == null) || (s.equals(""))) {
                        s = (String)type.getAttributeComparableValue("name");
                    }            
                    typeL.setText(s);
                }
                //source
                if (p.getRole().equals(linkSourceRole)) {
                    //get source text
                    NOMElement source = p.getToElement();
		    if (source!=null) {
			//[DR: experimental line of code...
			main.getNTV().setHighlighted(NTextArea.USER_HIGHLIGHTS, new NOMObjectModelElement(source), yellowStyle);
			String text = "";
			Iterator it = main.getNTV().getTranscriptionDescendants(source).iterator();
			while (it.hasNext()) {
			    text += main.getNTV().getTranscriptionText((NOMElement)it.next()) + " ";
			}
			if (source.getAgentName()!=null) {
			    text = source.getAgentName()+": "+text;
			}
			sourceText.setText(text);
			//get source type
			List sl = source.getPointers();
			if (sl != null) {
			    Iterator slIt = sl.iterator();
			    while (slIt.hasNext()) {
				NOMPointer p2 = (NOMPointer)slIt.next();
				if (p2.getRole().equals(sourcedisplay.getTypeRole())) {
				    sourceL.setText((String)p2.getToElement().getAttributeComparableValue(sourcedisplay.getTypeGloss()));
				}
			    }
			}
		    }
                }
                //target
                if (p.getRole().equals(linkTargetRole)) {
                    //get target text
                    NOMElement target = p.getToElement();
		    if (target!=null) {
			//[DR: experimental line of code...
			main.getNTV().setHighlighted(NTextArea.USER_HIGHLIGHTS, new NOMObjectModelElement(target),orangeStyle);
			String text = "";
			Iterator it = main.getNTV().getTranscriptionDescendants(target).iterator();
			while (it.hasNext()) {
			    text += main.getNTV().getTranscriptionText((NOMElement)it.next()) + " ";
			}
			if (target.getAgentName()!=null) {
			    text = target.getAgentName()+": "+text;
			} 
			targetText.setText(text);
			//get target type
			List tl = target.getPointers();
			if (tl != null) {
			    Iterator tlIt = tl.iterator();
			    while (tlIt.hasNext()) {
				NOMPointer p2 = (NOMPointer)tlIt.next();
				if (p2.getRole().equals(targetdisplay.getTypeRole())) {
				    targetL.setText((String)p2.getToElement().getAttributeComparableValue(targetdisplay.getTypeGloss()));
				}
			    }
			}
		    }
                }
            }
	}
	String neatt = main.getConfig().getNXTConfig().getCorpusSettingValue("nelinkattribute");
	if (neatt!=null && neatt.length()!=0) {
	    typeL.setText((String)currentElement.getAttributeComparableValue(neatt));
	}
        notifyLinkChangeListeners();
    }

/** mostly different*/
    public void setCurrentElement(NOMWriteElement element) {
        //check element name
        if ((element != null) && !element.getName().equals(linkdisplay.getElementName())) {
            Logger.global.warning("The system tries to display an element in the " + linkNameLong + " display panel that is not an " + linkNameLong + ". Action ignored.");
            return;
        }
        //check if old element was completely filled
        if ((currentElement != null) && !minimallyComplete(currentElement)) {
            //Logger.global.warning("The previous " + linkNameLong + " contained empty parameters! Don't forget to fill them in later...");
        }
        currentElement = element;
        
        
        refreshGUI();
    }
    public NOMWriteElement getCurrentElement() {
        return currentElement;
    }

    /**
     * Show the Ontology popup menu that will change the link type of the current
     */
    public void showLinkTypePopupMenu() {
        Point p = main.getNTV().getCaret().getMagicCaretPosition() ;
        //ontPopupMenu.show(main.getNTV(),(int)p.getX(),(int)p.getY()+20);
        jmenu.show(main.getNTV(),(int)p.getX(),(int)p.getY()+20);
    }
    /**
     * Show the Ontology popup menu that will change the link type of the current
     */
    protected void showLinkTypePopupMenu(int x, int y) {
        //ontPopupMenu.show(thePanel,x,y);
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
     * Processes selection change. Only relevant if in one of waiting states, for now...
     */
    public void selectionChanged() {
        switch(state) {
            case WAITING_FOR_SOURCE:
                Set newSelect = main.getNTV().getSelectedAnnoElements();
                if (newSelect.size() > 0) {
                    state=NORMAL;
                    setSource((NOMElement)newSelect.iterator().next());
                }
                break;
            case WAITING_FOR_TARGET:
                 newSelect = main.getNTV().getSelectedAnnoElements();
                if (newSelect.size() > 0) {
                    state=NORMAL;
                    setTarget((NOMElement)newSelect.iterator().next());
                }
                break;
            case WAITING_FOR_SOURCE_AND_TARGET:
                 newSelect = main.getNTV().getSelectedAnnoElements();
                if (newSelect.size() > 0) {
                    setSource((NOMElement)newSelect.iterator().next());
                    startWaitingForTarget();
                }
                break;
        }
    }
    
    
/*===============
    Listeners for modified LINKs (e.g. a listbox filled with those LINK's...)
=====================*/

	protected EventListenerList listenerList = new EventListenerList(); 
	public void addLinkChangeListener (LinkChangeListener newListener) {
		listenerList.remove(LinkChangeListener.class, newListener); 
		listenerList.add(LinkChangeListener.class, newListener); 
	}
	public void removeLinkChangeListener (LinkChangeListener listener){
		listenerList.remove(LinkChangeListener.class, listener);
	}
	protected void notifyLinkChangeListeners(){
		// Process the listeners last to first, notifying 
		// those that are interested in this event 
		Object[] listeners = listenerList.getListenerList(); 
		for (int i = listeners.length-2; i>=0; i-=2) { 
        	((LinkChangeListener)listeners[i+1]).linkChanged();
		}
	}

    /**
     * Change the full name of the element type to be displayed and
     * edited in this panel (default: "Adjacency Pair")
     */
    private void setLongName(String name) {
	linkNameLong=name;
    }

    /**
     * Change the abbreviated name of the element type to be displayed and
     * edited in this panel (default: "LINK")
     */
    private void setShortName(String name) {
	linkNameShort=name;
    }
}
