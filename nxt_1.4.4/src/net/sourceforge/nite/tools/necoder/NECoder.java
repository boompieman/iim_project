package net.sourceforge.nite.tools.necoder;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeSelectionModel;

import net.sourceforge.nite.gui.textviewer.NTASelectionListener;
import net.sourceforge.nite.gui.textviewer.NTextElement;
import net.sourceforge.nite.gui.transcriptionviewer.DefaultTransToAnnoMap;
import net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView;
import net.sourceforge.nite.gui.transcriptionviewer.StringInsertDisplayStrategy;
import net.sourceforge.nite.gui.transcriptionviewer.TransToAnnoMap;
import net.sourceforge.nite.gui.util.AbstractCallableTool;
import net.sourceforge.nite.gui.util.EnumerationTreeView;
import net.sourceforge.nite.gui.util.OntologyTreeView;
import net.sourceforge.nite.gui.util.SwingUtils;
import net.sourceforge.nite.util.Debug;
import net.sourceforge.nite.meta.NCoding;
import net.sourceforge.nite.meta.NElement;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.NOMPointer;
import net.sourceforge.nite.nom.nomwrite.NOMTypeElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteAnnotation;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteCorpus;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWritePointer;
import net.sourceforge.nite.nxt.NOMObjectModelElement;

/**
 * A demonstration implementation of the AbstractCallableTool.
 * Is a Named Entity coder...
 *
 * @author Dennis Reidsma, UTwente
 */
public class NECoder extends AbstractCallableTool {
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
        
    
    	NECoder mainProg = new NECoder(args);
    }
    /**
     * Constructor is more or less the same for all abstractcallabletools.
     * First store input vars, then init corpus,
     * then call a number of predefined initializationmethods.
     * Each new tool redefines this to get correct combination of elements.
     */
    public NECoder(String[] args) {
        parseArguments(args);
        initConfig();
        initLnF(); // I prefer the inner frames to look different :-)
    	initializeCorpus(getCorpusName(),getObservationName());
    	setupMainFrame("Named Entity Coder");
    	setupDesktop();
        setupLog(Logger.global, 530, 530, 465, 90);
        setupMediaPlayer(695,15,380,180);
        setupTranscriptionView(15,15,500,600);
        
	selector = new DefaultTransToAnnoMap(getNTV()); //ugly hack

        setupSearch();
        setupActions();
        setupMenus();
        setupNECodingGui();
	getNTV().gotoDocumentStart();
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
		Debug.print("Add a specific coding:  " + ((NCoding)neel.getLayer().getContainer()).getName(), Debug.PROGRAMMER);		
            } else {
		Debug.print("Failed to find " + cfg.getNEElementName() + " in " + metadata, Debug.WARNING);
	    }
            Iterator it = specificCodings.iterator();
            while (it.hasNext()) {
		String codename = ((NCoding)it.next()).getName();
		Debug.print("Adding " + codename + " as annotator specific", Debug.DEBUG);
                nom.forceAnnotatorCoding(getAnnotatorName(), codename);
            }
        }
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
        JScrollPane treepane;

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
	if (((NECoderConfig)config).expandOntologyTree()) {
	    expandTree(jt);
	}

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
        
        //Debug.print("Adding Scrollbar.");
        treepane = new JScrollPane(jt);
        jif.getContentPane().add(treepane);
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

    /** utility funtion by Craig Nicol to check if the list of
     * pointers already contains one that points to the same
     * element */
    private boolean containsToPointer(List l, NOMPointer p) {
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

	// edit this one, or if we allow multiple pointers, add a new one on top
	if (editThis!=null) {
	    try {
		if (newType instanceof NOMElement) {
		    // OK - now check if we should add or edit
		    if (((NECoderConfig)config).getNEMultiplePointers()) {
			List pts = editThis.getPointers();
		    
			NOMPointer pt = new NOMWritePointer(getCorpus(), ((NECoderConfig)config).getNETypePointerRole(), editThis, (NOMElement)newType);
			//TODO: Find duplicates 
			if (containsToPointer(pts, pt)) {
			    Logger.global.info("Add cancelled, " + ((NOMElement)newType).getName() + " already exists.");
			    return;
			} else {
			    editThis.addPointer(pt);
			}

		    } else {
			NOMPointer pt = editThis.getPointerWithRole(((NECoderConfig)config).getNETypePointerRole());
			if (pt==null) {
			    pt = new NOMWritePointer(getCorpus(), ((NECoderConfig)config).getNETypePointerRole(), editThis, (NOMElement)newType);
			    editThis.addPointer(pt);
			} else {
			    pt.setToElement((NOMElement)newType);
			}
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
            if ((transSelection == null) || (transSelection.size() == 0)) {
                Logger.global.info("Please select some text first!");
                return;
            }
               
	    if (allowNestedNamedEntities()) {
		addNested(transSelection, ((NECoderConfig)config).getNEElementName(), newType);
	    } else {
		addUnnested(transSelection, ((NECoderConfig)config).getNEElementName(), newType);
	    }
        } catch (NOMException ex) {
            ex.printStackTrace();
        }
    }

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
	    //Debug.print("Element " + first.getID() + " has parent: " + parent.getID());
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
        
        StringInsertDisplayStrategy ds=new StringInsertDisplayStrategy(getNTV()) {
            protected String formStartString(NOMElement element) {
		return getNTV().getSegmentText(element) + ": ";
		/*
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
		return spaces + spaces + spaces + spaces + spaces + spaces + agentName + ": ";
		*/
            }
        };
        ds.setEndString("");
        getNTV().setDisplayStrategy(config.getSegmentationElementName(),ds);
        
        String elName = ((NECoderConfig)config).getNEElementName();
        String roleName = ((NECoderConfig)config).getNETypePointerRole();
        String abbrevAttr = ((NECoderConfig)config).getNEAbbrevAttrib();
        String attrName = ((NECoderConfig)config).getNEAttributeName();
	boolean attset=false; 
	String dat = ((NECoderConfig)config).getNEAttributeName();
	if (dat!=null && dat.length()!=0) { attset=true; }
	// check we haven't set the type to be an enumerated attibute
	// *and* the multiple pointers. That would be folly!
	boolean multi=false;
	if (((NECoderConfig)config).getNEMultiplePointers()) {
	    if (!attset) {
		Logger.global.info("Multiple Pointers Allowed");	    
		getNTV().setDisplayStrategy(elName, new NEDisplayStrategyMulti(getNTV(), roleName, abbrevAttr, attrName));
		multi=true;
	    } else {
		Logger.global.info("MULTIPLE POINTERS SETTING IGNORED - using an enumerated attribute for the ne type means we can't have multiple values!");
	    }
	}
	if (!multi) {
	    getNTV().setDisplayStrategy(elName, new NEDisplayStrategy(getNTV(), roleName, abbrevAttr, attrName));
	}

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
   
