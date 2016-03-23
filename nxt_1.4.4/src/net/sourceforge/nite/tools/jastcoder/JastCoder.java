package net.sourceforge.nite.tools.jastcoder;
import net.sourceforge.nite.tools.necoder.*;
import net.sourceforge.nite.util.*;

import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.nom.*;
import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.time.*;
import net.sourceforge.nite.meta.*;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.*;
import java.text.DecimalFormat;

/**
 * A demonstration implementation of the AbstractCallableTool.
 * Is a Named Entity coder...
 *
 * The JastCoder is an NECoder that changes the clock to see if
 * we can fix the synchronisation issues affecting our MPEG
 * videos.
 *
 * @author Dennis Reidsma, UTwente, Craig Nicol
 */
public class JastCoder extends AbstractCallableTool implements NOMView {
   /*==================================================================================
                  CONSTRUCTION
     ==================================================================================*/
    protected void initConfig() {
        config = new NECoderConfig();
    }

    /**
     * Same for every subclass! Document in AbstractCallableTool
     */
    public static void main(String[] args) {
        
    
    	JastCoder mainProg = new JastCoder(args);
    }
    /**
     * Constructor is more or less the same for all abstractcallabletools.
     * First store input vars, then init corpus,
     * then call a number of predefined initializationmethods.
     * Each new tool redefines this to get correct combination of elements.
     */
    public JastCoder(String[] args) {
        parseArguments(args);
        initConfig();
    	initializeCorpus(getCorpusName(),getObservationName());
    	setupMainFrame("Jast Named Entity Coder");
        initLnF(); // I prefer the inner frames to look different :-)
    	setupDesktop();
        setupLog(Logger.global, 530, 530, 465, 90);

	((DefaultClock)getClock()).setMaximumDrift(10.0);

        setupMediaPlayer(695,15,380,180);
        setupTranscriptionView(15,15,500,600);
        
            selector = new DefaultTransToAnnoMap(getNTV()); //ugly hack

        setupSearch();
        setupActions();
        setupMenus();
        setupNECodingGui();
        setupEditorModules();
        Logger.global.info("Initialization complete");
    }

    /**
     * In the NECoder the following codings are loaded as annotator specific:
     * the codings containing the named-entity elements.
     * <br>
     * The following methods are available to use here:
     * <br><b>nom.forceAnnotatorCoding(annotatorname, {name of coding})</b>: 
     *     The given coding MUST be loaded annotator specific. If it doesn't exist for this annotator, don't
     *     load data for this coding.
     * <br><b>nom.preferAnnotatorCoding(annotatorname, {name of coding})</b>: 
     *     The given coding will preferably be loaded annotator specific. If it doesn't exist for this annotator, 
     *     load the 'main' data for this coding.
     * <br><b>nom.setDefaultAnnotator(annotatorname)</b>: Prefer the given anntoator for all codings, unless
     *     overridden by one of the above methods.
     * 
     */
    protected void initNomAnnotatorSpecificLoads(NOMWriteCorpus nom) throws NOMException {

        if (getAnnotatorName()!=null) {
            Set specificCodings = new HashSet(); //collect in a set, because they may be stored in the same coding
            NECoderConfig cfg = (NECoderConfig)getConfig();
	    NMetaData metadata = nom.getMetaData();
            NElement neel = metadata.getElementByName(cfg.getNEElementName());
            if (neel != null) {
                specificCodings.add(neel.getLayer().getContainer());
		System.out.println("Add a specific coding:  " + ((NCoding)neel.getLayer().getContainer()).getName());		
            } else {
		System.out.println("Failed to find " + cfg.getNEElementName() + " in " + metadata);
	    }
            Iterator it = specificCodings.iterator();
            while (it.hasNext()) {
		String codename = ((NCoding)it.next()).getName();
		System.out.println("Adding " + codename + " as annotator specific");
                nom.forceAnnotatorCoding(getAnnotatorName(), codename);
            }
        }
    }

/*=======================
  variables and accessors
  =======================*/
    /**
     * The DAElementPane will be made private, and available through an accessor.
     * This makes it easier to avoid initialization problems. If you use an accessor, the pane
     * doesn't have to be initialized before you create e.g. the delete action.
     */
    JastEditorModule jePane;
    /**
     * See {@link project.ami.textlabeler.DAAnnotationTool#ntv ntv} attribute.
     */
    public JastEditorModule getjePane() {
        return jePane;
    }
    

//===================== NOMSHARED INTERFACING ===================================

    /**
     * The JastCoder should be a NOMView, to be able to lock the corpus and notify changes.
     *
     * AT THE MOMENT IT DOES NOT DO ANYTHING WHEN IT RECEIVES AN EDIT
     */
    public void handleChange(NOMEdit change) {
        System.out.println("External change: " + change);
    }

    

/*==================================================================================
                SETUP METHODS
==================================================================================*/
    
   
    /**
     * Initialize editor modules and put them on screen.
     */
    private void setupEditorModules(){
        jePane= new JastEditorModule(this);

        NITEMediaPlayer niteplayer = getMediaPlayer();
        if ((niteplayer != null) && (niteplayer instanceof NMediaPlayer)) {
            NMediaPlayer player = (NMediaPlayer)niteplayer;
            player.addSignalListener(jePane);
        }

        JInternalFrame jeModFrame = new JInternalFrame ("Edit Referring Expressions", true, false, true, true);
        SwingUtils.getResourceIcon(jeModFrame, "/eclipseicons/eview16/editor_view.gif",getClass());
        jeModFrame.getContentPane().add(jePane.getPanel());
        jeModFrame.setSize(460,260);
        jeModFrame.setLocation(520,460);
        jeModFrame.setVisible(true);
        getDesktop().add(jeModFrame);        
    }
    

    /** expand all nodes in the ontology tree - should go in
     * OntologyTreeView but that is still under construction (jonathan
     * 7.3.5) */
    private void expandTree(JTree jt) {
	for (int i=0; i<jt.getRowCount(); i++) {
	    jt.expandRow(i);
	}
    }

    private JTree jt;
          
    /** 
     * This method does the setup of the parts that do the actual work of this tool.
     * In this case, this means building a small button panel, which creates named entities 
     * from the current selection. 
     * If a piece of text is selected that overlaps with an exisiting named entity, it is changed to reflect that entity and
     * that entity is set as current on the panel. If you change the type of NE and a current is set, its type is changed.
     * If you press a type button and no current is set, a new NE is created and initialized with correct type.
     * The whole stuff should also be connected to key presses (1..0, or more keys if more NE types),
     initialized from the NamedEntity Ontology (the name and root of which is set in the NECoderConfig)
     */
    protected void setupNECodingGui() {
        JPanel pan = new JPanel();
        JInternalFrame jif = new JInternalFrame("NEGUI", true, false, true, true);
        //final JTree jt = OntologyTreeView.getOntologyTreeView( getCorpus(), ((NECoderConfig)config).getNEDisplayAttribute(),((NECoderConfig)config).getNETypeRoot());

        setupInputMaps();
	HashMap hm = addToInputMaps((NOMTypeElement)getCorpus().getElementByID(((NECoderConfig)config).getNETypeRoot()));

	String dat = ((NECoderConfig)config).getNEAttributeName();
	if (dat==null || dat.length()==0) {
	    jt = OntologyTreeView.getOntologyTreeView( getCorpus(), ((NECoderConfig)config).getNEDisplayAttribute(),((NECoderConfig)config).getNETypeRoot(), hm, OntologyTreeView.SHOWKEYS_LABEL);
	} else {
	    jt = EnumerationTreeView.getEnumerationTreeView(getCorpus(), ((NECoderConfig)config).getNEElementName(),((NECoderConfig)config).getNEAttributeName(), hm, OntologyTreeView.SHOWKEYS_LABEL);
	}
        TreeSelectionModel tsm = new DefaultTreeSelectionModel();
        tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jt.setSelectionModel(tsm);
	expandTree(jt);

        TreeSelectionListener selectionCallback = new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e){
                if (e.getNewLeadSelectionPath()==null) {
                    return;
                }
                DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)e.getNewLeadSelectionPath().getLastPathComponent();
                if (dmtn.isLeaf()) {
                    createNewNE(dmtn.getUserObject());
                }
            }
        };
        jt.addTreeSelectionListener(selectionCallback);
        
        getNTV().addNTASelectionListener(new NTASelectionListener() {
            public void selectionChanged() {
                jt.clearSelection();
            }
        });
        pan.add(jt);
        
        jif.getContentPane().add(jt);
        jif.setVisible(true);                    
        jif.setSize(150, 495);
        jif.setLocation(530,15);
        getDesktop().add(jif);
        
    }
    
    protected InputMap globalImap;
    protected ActionMap globalAmap;
    protected InputMap getglobalImap(){
        return globalImap;
    }
    protected ActionMap getglobalAmap(){
        return globalAmap;
    }

    /**
     * This method should be diffferent for every tool version.
     * Get relevant actions from relevant editor modules, put them in central action map, create appropriate inputmaps...
     */    
    private void setupInputMaps() {
    	globalImap  = new ComponentInputMap(getNTV());
    	globalAmap = new ActionMap();
    	
    	globalAmap.put(DELETE_NE_ACTION, getActionMap().get(DELETE_NE_ACTION));
    	globalImap.put(KeyStroke.getKeyStroke("DELETE"), DELETE_NE_ACTION);

        getNTV().setInputMap(JComponent.WHEN_FOCUSED,globalImap);
        getNTV().setActionMap(globalAmap);

        getDesktop().setActionMap(globalAmap);
        getDesktop().setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, globalImap);
    }
    
    /** look for keyboard shortcuts to add */
    private HashMap addToInputMaps(NOMTypeElement nel) {
	HashMap retmap = new HashMap();
	if (nel==null) { return retmap; }
	String key = nel.getKeyStroke();
	if (key!=null) {
	    try {
		KeyStroke ks = KeyStroke.getKeyStroke(key);
		//System.err.println("Got keystroke: " + key);		
                TargetAction action = new TargetAction(key,nel);
		globalAmap.put(key, action);
		globalImap.put(ks,key);
		key = key.replaceFirst("typed ", "");
		retmap.put(nel,key);
	    } catch (Exception ex) {
		System.err.println("Failed to parse keystroke: " + key);
		ex.printStackTrace();
	    }
	}
	List kids = nel.getChildren();
	if (kids!=null) {
	    for(Iterator kit = kids.iterator(); kit.hasNext(); ) {
		retmap.putAll(addToInputMaps((NOMTypeElement)kit.next()));
	    }
	}
	return retmap;
    }

    private boolean containsToPointer(List l, NOMPointer p)
    {
	NOMPointer n;
	for (Iterator i = l.iterator(); i.hasNext();) {
	    n = (NOMPointer)i.next();
	    if(n.getToElement() == p.getToElement()) {
		return true;
	    }
	}
	return false;
    }

    /** Add or edit an element where we can nest. Need to first work
     * out whether the new element covers exactly the same extent as
     * an existing one. In that case we edit and otherwise we add a
     * new element, calculating any changes required to the parent
     * element as we go. Note that we can be added as a parent of
     * existing NEs, a child of an existing NE, or indeed both.
     * newType can either be a type element in which case we're
     * pointing to the ontology or a string in which case we're
     * setting an attribute (jak 25/3/6) */
    private void addNested(Set selection, String elname, Object newType) throws NOMException {
	NOMElement editThis=null;
	Set deleteThese = new HashSet(); // overlaps
	Set children = new HashSet(); // subsets (children)
	Set parents = new HashSet(); // supersets

	NLayer lay = getMetaData().getLayerByName(getNTV().getTransLayerName());
	Set allparents = findAllNamedParents(selection.iterator(), elname, null);

	for (Iterator pit = allparents.iterator(); pit.hasNext(); ) {
	    NOMElement par = (NOMElement)pit.next();
	    boolean parent_not_selection=false;
	    boolean selection_not_parent=false;
	    List kids = par.findDescendantsInLayer(lay);
	    // check if parent contains elements not in selection.
	    for (Iterator kit = kids.iterator(); kit.hasNext(); ) {
		NOMElement kid = (NOMElement) kit.next();
		if (!selection.contains(kid)) { parent_not_selection=true; }
	    }
	   
	    // check if selection contains elements not in parent
	    for (Iterator sit = selection.iterator(); sit.hasNext(); ) {
		NOMElement sel = (NOMElement)sit.next();
		if (!kids.contains(sel)) { selection_not_parent=true; }
	    }

	    if (parent_not_selection && selection_not_parent) {
		deleteThese.add(par);
	    } else if (parent_not_selection) {
		parents.add(par);
	    } else if (selection_not_parent) {
		children.add(par);
	    } else { // equality!
		editThis=par;
	    }
	}

	// edit, don't delete and re-create...
	if (editThis!=null) {
	    try {
		if (newType instanceof NOMElement) {
		    List pts = editThis.getPointers();

		    //if (pt==null) {
		    NOMPointer pt = new NOMWritePointer(getCorpus(), ((NECoderConfig)config).getNETypePointerRole(), editThis, (NOMElement)newType);
		    //TODO: Find duplicates 
		    if (containsToPointer(pts, pt)) {
			Logger.global.info("Add cancelled, " + ((NOMElement)newType).getName() + " already exists.");
			return;
		    } else {
			editThis.addPointer(pt);
			//} else {
			//pt.setToElement((NOMElement)newType);
			//}
		    }
		} else if (newType instanceof String) {
		    editThis.setStringAttribute(((NECoderConfig)config).getNEAttributeName(), (String)newType);
		}
	    } catch (NOMException ex) {
		Logger.global.severe("Can't set type. Read stacktrace for more info");
		ex.printStackTrace();
	    }		

	    getNTV().displayAnnotationElement(editThis);
	    return;
	}

	// delete bad overlaps
	for (Iterator delit=deleteThese.iterator(); delit.hasNext(); ) {
	    NOMElement del = (NOMElement)delit.next();
	    getNTV().undisplayAnnotationElement(del);
	    NOMdelete(del);
	}

	// add a new element and work out where to put it!
	NOMElement first = (NOMElement)selection.iterator().next();
	NOMElement nel= new NOMWriteAnnotation(getCorpus(),((NECoderConfig)config).getNEElementName(),getObservationName(), first.getAgentName());
	
	try {
	    if (newType instanceof NOMElement) {
		NOMPointer pt = new NOMWritePointer(getCorpus(), ((NECoderConfig)config).getNETypePointerRole(), nel, (NOMElement)newType);
		nel.addPointer(pt);
	    } else if (newType instanceof String) {
		nel.setStringAttribute(((NECoderConfig)config).getNEAttributeName(), (String)newType);
	    }
	} catch (NOMException ex) {
	    Logger.global.severe("Can't set type. Read stacktrace for more info");
		ex.printStackTrace();
	}

	// remove parents of parents (since these will not be changed by our addition)
	List removes = new ArrayList();
	for (Iterator pit=parents.iterator(); pit.hasNext();) {
	    NOMElement par = (NOMElement) pit.next();
	    NOMElement parpar = par.getParentInFile();
	    if (!parpar.isStreamElement() && parents.contains(parpar)) {
		removes.add(parpar);
	    }
	}
	for (Iterator rit=removes.iterator(); rit.hasNext(); ) {
	    parents.remove(rit.next());
	}

	// remove children of children (since these will not be changed by our addition)
	removes = new ArrayList();
	for (Iterator kit=children.iterator(); kit.hasNext();) {
	    NOMElement kid = (NOMElement) kit.next();
	    NOMElement par = kid.getParentInFile();
	    if (!par.isStreamElement() && children.contains(par)) { 
		removes.add(kid); 
	    }
	}
	for (Iterator rit=removes.iterator(); rit.hasNext(); ) {
	    children.remove(rit.next());
	}

	NOMElement lastAdded=null;

	for (Iterator kit=children.iterator(); kit.hasNext(); ) {
	    NOMElement k=(NOMElement)kit.next();
	    k.getParentInFile().removeChild(k);
	}

	// now we're ready to add. First collect children.
	// We do this by adding to the child list any transcription-level
	// elements that are not contained by any of the existing
	// child elements.
	List realkids = new ArrayList();
	for (Iterator sit = selection.iterator(); sit.hasNext(); ) {
	    NOMElement sel = (NOMElement)sit.next();
	    boolean containedByChild=false;
	    for (Iterator chit=children.iterator(); chit.hasNext(); ) {
		NOMElement kk=(NOMElement)chit.next();
		List kkids = kk.findDescendantsInLayer(lay);
		if (kkids.contains(sel)) {
		    if (lastAdded==null || lastAdded!=kk) {
			realkids.add(kk);
			lastAdded=kk;
		    }
		    containedByChild=true;
		    continue;
		}
		if (containedByChild) { continue; }
	    }
	    if (!containedByChild) {
		realkids.add(sel);
		lastAdded=sel;
	    }
	}


	// now add. If we have a parent, just add the new element in
	// place of its gathered children.
	if (parents.size()==1) {
	    NOMElement par = (NOMElement) parents.iterator().next();
	    par.addChildAboveChildren(nel, realkids);
	} else {
	    // otherwise add our new children, removing from their old parents
	    for (Iterator sit = realkids.iterator(); sit.hasNext(); ) {
		NOMElement sel = (NOMElement)sit.next();
		NOMElement selp = sel.findAncestorInLayer(nel.getLayer());
		if (selp!=null) { 
		    selp.removeChild(sel); 
		}
		nel.addChild(sel);
	    }
	}
	
	nel.addToCorpus();
	Logger.global.info("NE created");
	getNTV().displayAnnotationElement(nel);
    }

    /** find all the parents in the named entity layer for the
     * selected elements */
    private Set findAllNamedParents(Iterator selit, String elname, NOMElement nel) throws NOMException  {
	Set removeParents = new LinkedHashSet();

	while (selit.hasNext()) {
	    NOMWriteElement nextChild = (NOMWriteElement)selit.next();
	    //is there already a named entity on that element?
	    Set parents = nextChild.findAncestorsNamed(((NECoderConfig)config).getNEElementName()); 
	    if (parents != null) {
		//remember to remove old named entity
		removeParents.addAll(parents);
	    }
	    /* removed - causing problems...
	    if (nel!=null) {
		nel.addChild(nextChild);
	    }
	    */
	}
	return removeParents;
    }


    protected void createNewNE() {
        createNewNE(null);
    }

    /**
     * Create a new NE act from the current text selection in the transcription view
     */
    protected void createNewNE(Object newType) {
        try {
            Set transSelection = getNTV().getSelectedTransElements();
            Set annoSelection = getNTV().getSelectedAnnoElements();
            //Logger.global.info("annoSelection == null ? " + (annoSelection == null));
            if ((transSelection == null) || (transSelection.size() == 0)) {
                if ((annoSelection == null) || (annoSelection.size() == 0)) {
                    Logger.global.info("Please select some text first!");
                    return;
                } else {
                    // annoSelection is a set of NOMElement's
                    Logger.global.info(annoSelection.size() + " annotation(s) selected. " + ((NOMElement)annoSelection.toArray()[0]).getChildren().size() + " children found.");
                    transSelection = new HashSet(((NOMElement)annoSelection.toArray()[0]).getChildren());
                }
            } else {
                // There's a valid transSelection, so we don't need the annoSelection.
                annoSelection = null;
            }
               
            if (allowNestedNamedEntities()) { 
                addNested(transSelection, ((NECoderConfig)config).getNEElementName(), newType);
            } else {
                addUnnested(transSelection, ((NECoderConfig)config).getNEElementName(), newType);
            }
            
            // Select newly created annotation.
            if (annoSelection == null) {
                int dot = getNTV().getSelectionStartPosition() - 2; // 1 for space, 2 to hit annotation
                if (dot >= 0) {
                    getNTV().newSelection(dot, dot);
                } else {
                    Logger.global.info("Could not select annotation.");
                }
            }
        } catch (NOMException ex) {
            ex.printStackTrace();
        }
    }

    //TODO: Add pointer to existing expression in addUnnested
    /** it's a relatively simple process to add if we're un-nestable -
        newType can either be a type element in which case we're
        pointing to the ontology or a string in which case we're
        setting an attribute (jak 25/3/6) */
    private void addUnnested(Set transSelection, String elname, Object newType) throws NOMException {
	NOMElement first = (NOMElement)transSelection.iterator().next();
	Set removeParents = new HashSet();
	NOMElement newNE= new NOMWriteAnnotation(getCorpus(),((NECoderConfig)config).getNEElementName(),getObservationName(), first.getAgentName());
	removeParents = findAllNamedParents(transSelection.iterator(), 
					    ((NECoderConfig)config).getNEElementName(), newNE);
	/* remove some previous named entities from this element */
	Iterator it = removeParents.iterator(); 
	while (it.hasNext()) {
	    NOMElement parent = (NOMElement)it.next();
	    System.out.println("Element " + first.getID() + " has parent: " + parent.getID());
	    getNTV().undisplayAnnotationElement(parent);
	    parent.getParentInFile().deleteChild(parent);
	}

	for (Iterator selit=transSelection.iterator(); selit.hasNext(); ) {
	    NOMElement nel = (NOMElement)selit.next();
	    newNE.addChild(nel);
	}

	newNE.addToCorpus();
                              
	if (newType != null) {
	    if (newType instanceof NOMElement) {
		NOMWritePointer p = new NOMWritePointer(getCorpus(), ((NECoderConfig)config).getNETypePointerRole(), newNE, (NOMElement)newType);
		try {
		    newNE.addPointer(p);
		} catch (NOMException ex) {
		    Logger.global.severe("Can't set type. Read stacktrace for more info");
		    ex.printStackTrace();
		}
	    } else if (newType instanceof String) {
		try {
		    newNE.setStringAttribute(((NECoderConfig)config).getNEAttributeName(), (String)newType);
		} catch (NOMException ex) {
		    Logger.global.severe("Can't set type. Read stacktrace for more info");
		    ex.printStackTrace();
		}
	    }
	}
	Logger.global.info("NE created");
	getNTV().displayAnnotationElement(newNE);
    }
    
/*====================================================================
    VERY IMPORTANT: THE SETTINGS OF THE TRANSCRIPTIONVIEW    
====================================================================*/

    /**
     * In this tool: segment name == XXX, transcript layer = XXX, 
     In other tools those things might be set interactively with user?
     */
    public void initTranscriptionViewSettings() {
        super.initTranscriptionViewSettings();
	getNTV().setTranscriptionToTextDelegate(new AMITranscriptionToTextDelegate());

        StringInsertDisplayStrategy ds=new StringInsertDisplayStrategy(getNTV()) {
            protected String formStartString(NOMElement element) {
                String spaces = "";
                String agentName = element.getAgentName();
                if (agentName!=null) {
		    if (agentName.equals("p1")) {
			spaces = " ";
		    } else if (agentName.equals("p2")) {
			spaces = "  ";
		    } else if (agentName.equals("p3")) {
			spaces = "   ";
		    }
		} else {
		    return "";
		}
                //if (getPersonForAgentName(agentName) != null) {
                //    return spaces + spaces + spaces + spaces + spaces + spaces + getPersonForAgentName(agentName).getAttributeComparableValue("name") + ": ";
                //} else {
		int starttime = (int)element.getStartTime();
		int startminutes = starttime / 60;
		double startseconds = element.getStartTime() - (startminutes*60);
		//TODO: improve formatting
		DecimalFormat fmt = new DecimalFormat("00.000");
		return spaces + spaces + spaces + spaces + spaces + spaces + agentName + " (" + startminutes + ":" + fmt.format(startseconds) + "): ";
                //}
            }
        };
        ds.setEndString("");
        getNTV().setDisplayStrategy(config.getSegmentationElementName(),ds);
        
        String elName = ((NECoderConfig)config).getNEElementName();
        String roleName = ((NECoderConfig)config).getNETypePointerRole();
        String abbrevAttr = ((NECoderConfig)config).getNEAbbrevAttrib();
        String attrName = ((NECoderConfig)config).getNEAttributeName();
        getNTV().setDisplayStrategy(elName, new JastDisplayStrategy(getNTV(), roleName, abbrevAttr, attrName));

        getNTV().setSelectDirectTextRepresentations(true);
        getNTV().setSelectTranscriptionAncestors(true);
        Set types = new HashSet();
        types.add(((NECoderConfig)config).getNEElementName());
        getNTV().setSelectableAnnotationTypes(types);
        getNTV().setAllowTranscriptSelect(true);
        getNTV().setAllowAnnotationSelect(true);
    }

    /**
     * Delete a NE
     */
    public static final String DELETE_NE_ACTION = "Delete label";
    /**
     * New actions: delete NE
     */
    protected void setupActions() {
        super.setupActions();
        ActionMap actMap = getActionMap();
        Action act = null;
        NTranscriptionView ntv = getNTV();
        
        //delete action
        act = new AbstractAction("Delete NE") {
            public void actionPerformed(ActionEvent ev) {
                deleteNE();
            }
        };
        if (getClass().getResource("/eclipseicons/etool16/delete_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/delete_edit.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }
        act.putValue(Action.SHORT_DESCRIPTION,"Delete Named Entity.");
        actMap.put(DELETE_NE_ACTION, act);
    }
    TransToAnnoMap selector;

    /**
     * Delete NE from current selection, determining NE through selector.
     */
    protected void deleteNE() {
        Set selected = getNTV().getSelectedTransElements();
        Set todelete = new HashSet();
        Iterator it = selected.iterator();
        while (it.hasNext()) {
            NOMElement next = (NOMElement)it.next();
            //
            //ugly hack::::::::
            NTextElement del = (NTextElement)getNTV().getTextElements(new NOMObjectModelElement(next)).iterator().next();
            //
            NOMElement delnme = ((NOMObjectModelElement)del.getDataElement()).getElement();
            if (delnme.getName().equals(((NECoderConfig)config).getNEElementName())) {
                todelete.add(delnme);
            } else {
                Set l = selector.getAnnotationElementsForTransElement(delnme);
                todelete.addAll(l);
            }
        }
        todelete.addAll(getNTV().getSelectedAnnoElements());
        it = todelete.iterator();
        while (it.hasNext()) {
            NOMWriteElement next = (NOMWriteElement)it.next();
            try {
                getNTV().undisplayAnnotationElement(next);
		NOMdelete(next);
            } catch (NOMException ex) {
                Logger.global.info("ERROR can't remove element");
            }
        }
    }        

    /** delete the element, passing any kids up to parent where appropriate. */
    private void NOMdelete(NOMElement nel) throws NOMException {
	boolean parent_gets_leaf_kids=true;
	NOMElement par = nel.getParentInFile();
	if (par.isStreamElement()) {
	    parent_gets_leaf_kids=false;
	}
	List kids = nel.getChildren();
	if (kids!=null) {
	    NLayer oklayer = getMetaData().getLayerByName(getNTV().getTransLayerName());
	    if (oklayer!=null) {
		for (Iterator kit=kids.iterator(); kit.hasNext(); ) {
		    NOMElement child = (NOMElement)kit.next();
		    boolean add_child_to_parent=true;
		    if (!parent_gets_leaf_kids) { // check how leafy this child is
			if (!oklayer.getContentElements().contains(child.getName())) {
			    add_child_to_parent=false; // it's too leafy
			}
		    }
		    if (add_child_to_parent) {
			par.addChildBefore(nel, child);
		    }
		} 
	    }
	}
	nel.getParentInFile().deleteChild(nel);
    }

    /** True if named entities can nest */
    public boolean allowNestedNamedEntities() {
	return ((NECoderConfig)config).getAllowNestedNamedEntities();
    }


    /**
     * <p>Action for the ontology tree elements. Fires the AnnotationListener events
     * when a button is clicked.</p>
     */
    private class TargetAction extends AbstractAction {
        private NOMElement target;

        public TargetAction(String label, NOMElement target) {
            super();
            this.target = target;
        }

        public void actionPerformed(ActionEvent e) {
	    createNewNE(target);
        }
    }

}
   
