/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite.impl;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Point;
import java.awt.GridLayout;

import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.helpers.*;

import net.sourceforge.nite.nom.*;
import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.search.*;
import net.sourceforge.nite.search.rewriter.*;
import net.sourceforge.nite.gui.util.PopupMessage;
import net.sourceforge.nite.util.Debug;
import net.sourceforge.nite.util.XMLutils;

/**
 * NOMCorpus is the top-level class & represents a multi-rooted
 * directed graph: the NOM structure. The constructor must pass a
 * pre-loaded NMetaData structure which is used in many of the methods.
 *
 * @author Jonathan Kilgour, Holger Voormann (SearchableCorpus
 *  implementation etc.)
 */
public class NOMWriteCorpus implements NOMCorpus, LexicalHandler, NOMControl, SearchableCorpus {
    protected static final int CODING = 0;
    protected static final int ONTOLOGY = 1;
    protected static final int OBJECTSET = 2;
    protected static final int CORPUSRESOURCE = 3;
    protected static final String NXT_QUERY_REWRITE_PROPERTY = "NXT_QUERY_REWRITE";
    protected static final String NXT_QUERY_REWRITE_ENVVAR = "NXT_QUERY_REWRITE";
    protected static final String NXT_DEBUG_PROPERTY = "NXT_DEBUG";
    protected static final String NXT_DEBUG_ENVVAR = "NXT_DEBUG";
    protected static final String NXT_LAZY_PROPERTY = "NXT_LAZY_LOAD";
    protected static final String NXT_LAZY_ENVVAR = "NXT_LAZY_LOAD";
    protected static final String NXT_TEXT_PERCOLATE_PROPERTY = "NXT_TEXT_PERCOLATE";
    protected static final String NXT_TEXT_PERCOLATE_ENVVAR = "NXT_TEXT_PERCOLATE";
    protected static final String NXT_RESOURCES_PROPERTY = "NXT_RESOURCES_ALWAYS_ASK";
    protected static final String NXT_RESOURCES_ENVVAR = "NXT_RESOURCES_ALWAYS_ASK";
    protected static final String NXT_RESOURCES_TO_LOAD_PROPERTY = "NXT_RESOURCES";
    protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
    protected static final String LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";
    protected static final String NAMESPACE_PREFIXES_FEATURE_ID
	= "http://xml.org/sax/features/namespace-prefixes";

    protected static final String NITE_NAMESPACE_NAME = "xmlns:nite";
    protected static final String NITE_NAMESPACE = "http://nite.sourceforge.net/";
    protected static final String XLINK_NAMESPACE_NAME = "xmlns:xlink";
    protected static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";
    protected static final String SCHEMA_NAMESPACE_NAME = "xmlns:xsi";
    protected static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    protected static final String SCHEMA_LOCATION_NAME = "xsi:noNamespaceSchemaLocation";

    protected static final String LINKTYPEATTR = "xlink:type";
    protected static final int TEXT_NODE = 1;
    protected static final int ELEMENT_NODE = 2;
    protected static final String INDENT = "   ";
    protected static final String ROLE_ATTRIBUTE_NAME = "role";
    protected static final String LTXML1_LINK_FILE_SEPARATOR = "#";
    protected static final String LTXML1_RANGE_SEPARATOR = "..";
    protected static final String LTXML1_LINK_BEFORE_ID = "id(";
    protected static final String LTXML1_LINK_AFTER_ID = ")";
    protected static final String LTXML1_RANGE_END = "";
    protected static final String LTXML1_LINK_END = "";
    protected static final String LTXML1_EXTRA_ATTRIBUTE = "";
    protected static final String LTXML1_HREF_ATTRIBUTE = "href";
    protected static final String XPOINTER_LINK_FILE_SEPARATOR = "#xpointer(";
    protected static final String XPOINTER_RANGE_SEPARATOR = "/range-to(";
    protected static final String XPOINTER_LINK_BEFORE_ID = "id(";
    protected static final String XPOINTER_LINK_AFTER_ID = ")";
    protected static final String XPOINTER_RANGE_END = ")";
    protected static final String XPOINTER_LINK_END = ")";
    protected static final String XPOINTER_EXTRA_ATTRIBUTE = " " + LINKTYPEATTR + "=\"simple\"";
    protected static final String XPOINTER_HREF_ATTRIBUTE = "xlink:href";

    private double corpus_start_time=NOMElement.UNTIMED;
    private double corpus_end_time=NOMElement.UNTIMED;
    private boolean cleaning_up = false;
    protected boolean lazy_loading = true;
    private boolean rewrite = false; // do we auto-rewrite queries for efficiency?
    private boolean forceresourceask = false; // do we always ask when multiple resources are available
    private boolean percolatetext = false; // do we percolate textual content up the tree?
    private String globalAnnotator = null; // if set load/save this annotator's data.
    private Set unloadedFiles = new HashSet();
    private Set loadedFiles = new HashSet();
    private Hashtable loadedNOMFiles = new Hashtable();
    private Hashtable loadedResources = new Hashtable();
    private Hashtable changedPreferences = new Hashtable();
    private Hashtable forcedResources = new Hashtable();
    private Hashtable preferredResources = new Hashtable();
    private Hashtable createdResources = new Hashtable();
    private Set loadedParentLayers = new HashSet();
    private Set loadedChildLayers = new HashSet();
    private Set loadedToTimeColours = new HashSet();
    private boolean serialize_maximal_ranges = true;
    private boolean serialize_inherited_times = false;
    private boolean serialize_forced_stream_element_names = false;
    private boolean validating = true;
    private boolean loadingfromfile = false;
    private boolean loadrequest = false;
    private boolean saveerror=false;
    protected NiteMetaData metadata;
    private String current_colour;
    private NResource current_resource=null;
    private XMLReader parser = null;
    private int content_nodes=0;
    private ArrayList rootlist=null;
    private Stack element_stack = new Stack();
    protected Hashtable element_hash = new Hashtable();
    private ArrayList element_list = new ArrayList();
    private Hashtable element_name_hash = new Hashtable();
    private Hashtable pointer_hash = new Hashtable();
    private HashMap idhash = new HashMap();
    private HashSet colours_changed=new HashSet();
    private HashSet resources_changed=new HashSet();
    private HashSet registered_views=new HashSet();
    private String linkfileseparator=LTXML1_LINK_FILE_SEPARATOR;
    private String linkrangeseparator=LTXML1_RANGE_SEPARATOR;
    private String linkrangeend=LTXML1_RANGE_END;
    private String linkend=LTXML1_LINK_END;
    private String linkbeforeid=LTXML1_LINK_BEFORE_ID;
    private String linkafterid=LTXML1_LINK_AFTER_ID;
    private String linkextraattr=LTXML1_EXTRA_ATTRIBUTE;
    private String linkhrefattr=LTXML1_HREF_ATTRIBUTE;
    private String schemalocation=null;
    boolean removing_pointers=false;
    boolean batch_load=false; // set to true when don't want time checking
    private NOMView locker=null;
    boolean metaless_mode=false; 
    private int loading=CODING;
    private int saving=CODING;
    private HashSet loaded_observations=new HashSet();
    private List specialLoads=new ArrayList(); // annotator-specific data
    private boolean corpus_shared=false;
    //private PrintStream LOG=System.out;
    private PrintStream ERR=System.err;
    private NOMMaker nommaker = new Maker();
    /** These are lists of AnnotatorCodings which have been specified
     * as preferred or forced whenever loadData is called */
    private List preferredCodings = new ArrayList();
    private List forcedCodings = new ArrayList();
    private QueryRewriter rewriter=null;
    private List globalResourceList=new ArrayList();
    private boolean finishedresources=false;
    private JFrame dialog;
    private JDialog dlog;
    private NResource globalResource=null;
    private boolean onlysaveifchanged=true;
    protected NOMContext context=null;

    //    private extra_attribute="";

    /** Construct a NOM corpus ready to load / edit data.
        This constructor also does some basic loading of
        non-observation specific data like ontologies and object-sets
        here. */
    public NOMWriteCorpus (NiteMetaData meta) {
	init(meta);
    }


    /** Construct a NOM corpus ready to load / edit data.
        This constructor also does some basic loading of
        non-observation specific data like ontologies and object-sets
        here. */
    public NOMWriteCorpus (NiteMetaData meta, PrintStream log) {
	Debug.setStream(log);
	init(meta);
    }

    /** the common behaviour of the constructors */
    private void init(NiteMetaData meta) {
	metadata=meta;

	if (meta==null) { 
	    //	    metaless_mode=true;
	    ERR.println("FATAL ERROR: Cannot initialize a NOM without reference to a metadata file.");
	    System.exit(0);
	}
	// First set up the link type so we can read the data in correctly...
	if (meta.getLinkType()==NMetaData.XPOINTER_LINKS) {
	    linkfileseparator=XPOINTER_LINK_FILE_SEPARATOR;
	    linkrangeseparator=XPOINTER_RANGE_SEPARATOR;
	    linkrangeend=XPOINTER_RANGE_END;
	    linkend=XPOINTER_LINK_END;
	    linkbeforeid=XPOINTER_LINK_BEFORE_ID;
	    linkafterid=XPOINTER_LINK_AFTER_ID;
	    linkextraattr=XPOINTER_EXTRA_ATTRIBUTE;
	    linkhrefattr=XPOINTER_HREF_ATTRIBUTE;
	}
	context=new NOMWriteContext(this);
	checkProperties();
	initializeCorpus();
	clearChanges();
    }

    /** set up the corpus to respect any properties set. Please note -
     * because System.getenv is not Java 1.4 compliant, environment
     * variables are no longer allowed as a way of achieving these
     * settings. I agree this is rubbish, but we'd rather not insist
     * on Java 1.5 yet. */
    private void checkProperties() {
	// NXT_DEBUG specifies the level of debug message we get.
	int debug = Debug.getDebugLevel();
	String debugval = System.getProperty(NXT_DEBUG_PROPERTY);		
	if (debugval==null) { debugval = System.getenv(NXT_DEBUG_ENVVAR); }
	if (debugval!=null) {
	    // try to extract as an integer
	    try {
		debug=Integer.parseInt(debugval);
	    } catch (NumberFormatException nex) {
		if (debugval.equalsIgnoreCase("false")) { debug=Debug.NO_MESSAGES; }
		else { debug=Debug.WARNING; }
	    }
	    Debug.setDebug(debug);
	}

	// NXT_QUERY_REWRITE specifies whether we rewrite queries...
	boolean rewritebool=rewrite;
	String rewriteval = System.getProperty(NXT_QUERY_REWRITE_PROPERTY);		
	if (rewriteval==null) { rewriteval = System.getenv(NXT_QUERY_REWRITE_ENVVAR); }
	if (rewriteval!=null && !(rewriteval.equalsIgnoreCase("false"))) { rewritebool=true; }
	else if (rewriteval!=null && rewriteval.equalsIgnoreCase("false")) { rewritebool=false; }
	this.setQueryRewriting(rewritebool);
	Debug.print( rewrite ? "Query Rewriting ON " :
				"No query rewriting (use property"+NXT_QUERY_REWRITE_PROPERTY+" to switch)", Debug.DEBUG);

	// NXT_LAZY_LOAD tells us whether we should apply lazy loading
	boolean lazybool=lazy_loading;
	String lazyval = System.getProperty(NXT_LAZY_PROPERTY);
	if (lazyval==null) { rewriteval = System.getenv(NXT_LAZY_ENVVAR); }
	if (lazyval!=null && !(lazyval.equalsIgnoreCase("false"))) { lazybool=true; }
	else if (lazyval!=null && lazyval.equalsIgnoreCase("false")) { lazybool=false; }
	lazy_loading=lazybool;
	Debug.print( lazy_loading ? "Lazy loading ON (use property"+NXT_LAZY_PROPERTY+" to switch)" :
				"Lazy loading OFF", Debug.DEBUG);


	// NXT_RESOURCES_PROPERTY specifies whether we ask the user
	// when a choice of resources is available....
	boolean resourcebool=forceresourceask;
	String resourceval = System.getProperty(NXT_RESOURCES_PROPERTY);		
	if (resourceval==null) { resourceval = System.getenv(NXT_RESOURCES_ENVVAR); }
	if (resourceval!=null && !(resourceval.equalsIgnoreCase("false"))) { resourcebool=true; }
	else if (rewriteval!=null && rewriteval.equalsIgnoreCase("false")) { resourcebool=false; }
	this.setResourceLoadAsk(resourcebool);
	Debug.print( resourcebool ? "Always ask user to choose when multiple resources available: ON " :
				"Always ask user to choose when multiple resources available: OFF (use property "+NXT_RESOURCES_PROPERTY+" to switch)", Debug.DEBUG);


	// NXT_TEXT_PERCOLATE specifies whether we percolate (all) text content upwards...
	Boolean percbool=percolatetext;
	String percval = System.getProperty(NXT_TEXT_PERCOLATE_PROPERTY);		
	if (percval==null) { percval = System.getenv(NXT_TEXT_PERCOLATE_ENVVAR); }
	if (percval!=null && !(percval.equalsIgnoreCase("false"))) { percbool=true; }
	else if (percval!=null && percval.equalsIgnoreCase("false")) { percbool=false; }
	this.setTextPercolate(percbool);
	Debug.print( percbool ? "Text Percolation ON " :
				"No text percolation (use property"+NXT_TEXT_PERCOLATE_PROPERTY+" to switch)", Debug.DEBUG);


	String ress = System.getProperty(NXT_RESOURCES_TO_LOAD_PROPERTY);
	applyResourcesString(ress);
	if (ress==null) {
	    Debug.print("No resources listed on command line (use property "+NXT_RESOURCES_TO_LOAD_PROPERTY+" to specify)", Debug.DEBUG);	    
	}

    }
    
    /*-----------------------*/
    /* LOADING DATA INTO NOM */
    /*-----------------------*/

    /* Nothing to do here as all data including corpus-level
     * ontologies are now lazy-loaded. */
    private void initializeCorpus() {

    }

    protected void setBatchMode(boolean val) {
	batch_load=val;
    }

    /** used internally to indicate when the process is in batch mode
        i.e. we're currently loading a set of files. */
    public boolean getBatchMode() {
	return batch_load;
    }

    /** Returns true if data is currently being loaded from file. */
    public boolean isLoadingFromFile() {
	return loadingfromfile;
    }

    /** Deprecated. Since we're not sure how to deal with things
	without colour. Load data from a set of files into the
	NOMCorpus. Incremental loading of data is the default, so a new
	call to loadData will not zero-out the data loaded in a
	previous call. */
    private void loadData(String[] files) throws NOMException {
	if (files==null) return;
	setBatchMode(true);
	for (int i=0; i<files.length; i++) {
	    try {
		loadDataFromFile(files[i], "", null);
	    } catch (IOException e) { //not sure what to do here!!
		Debug.print("Failed to Load data from file: " + files[i], Debug.ERROR);
		e.printStackTrace();
	    }
	}
	setBatchMode(false);
	cleanupCorpus(false);
    }

    /** Load data for a specific set of observations into the
	NOMCorpus. Incremental loading of data is the default, so a new
	call to loadData will not zero-out the data loaded in a
	previous call. If the list of codings is non-null, it will be
	expected to be a list of NCodings that is the maximal set to be
	loaded whether lazy loading is on or off. */
    public void loadData(List observations,
			 List codings) throws NOMException {
	loadrequest=true;
	if (observations==null) { // load the whole corpus!!
	    observations=metadata.getObservations();
	}
	Iterator oit = observations.iterator();
	setBatchMode(true);
	while (oit.hasNext()) {
	    NiteObservation observation = (NiteObservation) oit.next();
	    if (!loaded_observations.contains(observation)) {
		loadObservationData(observation, codings);
		loaded_observations.add(observation);
	    }
	}
	setBatchMode(false);
	cleanupCorpus(false);
    }

    /** Load data for a single observation into the
	NOMCorpus. Incremental loading of data is the default, so a new
	call to loadData will not zero-out the data loaded in a
	previous call. */
    public void loadData(NObservation observation) throws NOMException {
	if (!lazy_loading) {
	    ensureCorpusLevelDataLoaded();
	}
	if (!loaded_observations.contains(observation)) {
	    loadrequest=true;
	    setBatchMode(true);
	    loadObservationData((NiteObservation)observation);
	    loaded_observations.add(observation);
	    setBatchMode(false);
	    cleanupCorpus(false);
	}
    }

    /** Set the preferred annotator for *all* codings that is used on
     * subsequent loadData calls. This will be overridden by any
     * codings that are forced to a specific annotator using
     * 'forceAnnotatorCoding', or even preferred using
     * 'preferAnnotatorCoding'. Note that this is the preferred
     * annotator only, and if there is no annotator data for any
     * coding but gold-standard data is present, that will be loaded
     * instead. */
    public void setDefaultAnnotator(String annotator) {
	globalAnnotator=annotator;
    }

    /** set a preference for all dependencies of this resource */
    private void preferDependencies(NResource resource) throws NOMException {
	if (resource==null) { return; }
	List deps = resource.getDependencies();
	if (deps!=null) {
	    for (Iterator dit=deps.iterator(); dit.hasNext(); ) {
		NResourceDependency dep = (NResourceDependency)dit.next();
		preferResourceLoad(dep.getResourceID());
	    }
	}
    }

    /** Force one coding to be loaded for a specific annotator when
     * loadData is called. This loads from the annotator's directory
     * even if it's empty, and there is gold-standard data
     * available. */
    public void forceResourceLoad(String resourceid) throws NOMException {
	NResourceData nrd = metadata.getResourceData();
	if (nrd==null) { 
	    throw new NOMException("Cannot force resource coding '" + resourceid + "' as no resource file is loaded. Check the metadata file.");
	}
	NResource nres = nrd.getResourceByID(resourceid);
	if (nres==null) {
	    throw new NOMException("Cannot find resource named '" + resourceid + "' in the resource file.");
	}

	// only set and recurse through dependents if we were not already forced
	List ress = (List)forcedResources.get(nres.getCoding());
	if (ress==null || !ress.contains(nres)) {
	    addForcedResource(nres);
	}
    }


    /** Prefer one particular virtual or real resource over any
     * competing ones for the same coding. This causes all other
     * resources mentioned as dependents of this one to be preferred
     * too. */
    public void preferResourceLoad(String resourceid) throws NOMException {
	NResourceData nrd = metadata.getResourceData();
	if (nrd==null) { 
	    throw new NOMException("Cannot force resource coding '" + resourceid + "' as no resource file is loaded. Check the metadata file.");
	}
	NResource nres = nrd.getResourceByID(resourceid);
	if (nres==null) {
	    throw new NOMException("Cannot find resource named '" + resourceid + "' in the resource file.");
	}
	// only set and recurse through dependents if we were not already preferred
	List ress = (List)preferredResources.get(nres.getCoding());
	if (ress==null || !ress.contains(nres)) {
	    addPreferredResource(nres);
	}
    }

    /** we have a string passed as a property NXT_RESOURCES - try to
     * decode it and force the appropriate resources */
    private void applyResourcesString(String resource_descriptions) {
	if (resource_descriptions==null) { return; }
	String [] ress = resource_descriptions.split(",");
	for (int j=0; j<ress.length; j++) {
	    try {
		forceResourceLoad(ress[j]);
	    } catch (Exception ex) {
		Debug.print("Failed to force resource load " + ress[j] + "! Ignored.", Debug.ERROR); 
	    }
	}
    }


    /** set the changed resource-preferences flag for the given
     * resource for all currently loaded observations (newly loaded
     * observations will be loaded with these prefs anyway) */
    private void noteChangedDependencies(NResource resource) {
	changedPreferences.put(metadata.getNFileByName(resource.getCoding()), "true");
    }

    /** set the changed resource-preferences flag for the given
     * resource for all currently loaded observations (newly loaded
     * observations will be loaded with these prefs anyway */
    private void removeChangedDependencies(NResource resource) {
	changedPreferences.remove(metadata.getNFileByName(resource.getCoding()));
    }

    // Simply check if a resource is already loaded 
    private boolean isLoaded(NResource resource) {
	if (loadedResources==null || loadedResources.values()==null) { return false; }
	for (Iterator rit=loadedResources.values().iterator(); rit.hasNext(); ) {
	    Object rn = rit.next();
	    if (rn instanceof List) {
		if (((List)rn).contains(resource)) { return true; }
	    }
	}
	return false;
    }

    // Simply check if a resource is already loaded 
    private boolean isLoaded(NOMFile nf, NResource resource) {
	if (loadedResources==null || loadedResources.values()==null) { return false; }
	List lr = (List)loadedResources.get(nf);
	if (lr!=null && lr.contains(resource)) { return true; }
	return false;
    }

    // Check if any resource is already loaded for a NOMFile
    private boolean anyLoaded(NOMFile nf) {
	if (loadedResources==null || loadedResources.values()==null) { return false; }
	if (nf==null || nf.getColour()==null) { return false; }
	for (Enumeration fen=loadedResources.keys(); fen.hasMoreElements(); ) {
	    Object fn = fen.nextElement();
	    if (fn instanceof NOMFile) {
		if (((NOMFile)fn).getColour().equals(nf.getColour())) { 
		    List rl = (List)loadedResources.get(fn);
		    if (rl!=null && rl.size()>0) {
			return true; 
		    }
		}
	    }
	}
	return false;
    }

    // Check if one of a List of NResource elements is loaded. Return
    // true if any of them are */
    private boolean anyLoaded(List rl) {
	if (rl==null) { return false; }
	for (Iterator rit=rl.iterator(); rit.hasNext(); ) {
	    try {
		NResource res = (NResource) rit.next();
		if (res!=null && isLoaded(res)) { 
		    return true; 
		}
	    } catch (Exception ex) { }
	}
	return false;
    }

    /** add a resource to the list of preferred resources for a
     * particular colour and prefer its dependencies */
    private void addPreferredResource(NResource resource) throws NOMException {
	if (resource==null) { return; }
	// should first check if it's already loaded...
	if (isLoaded(resource)) { return; }
	List pref = (List)preferredResources.get(resource.getCoding());
	if (pref==null) { pref = new ArrayList(); }
	if (!pref.contains(resource)) {
	    pref.add(resource);
	}
	preferredResources.put(resource.getCoding(), pref);
	//Debug.print("Coding: " + resource.getCoding() + " has " + pref.size() + " resources:", Debug.DEBUG);
	for (Iterator pit=pref.iterator(); pit.hasNext();) {
	    NResource res = (NResource)pit.next();
	    //Debug.print("  "+ res.getID(), Debug.DEBUG);
	}
	if (!(resource instanceof NVirtualResource)) {
	    preferDependencies(resource);
	}
	noteChangedDependencies(resource);
    }

    /** add a resource to the list of forced resources for a
     * particular colour and prefer its dependencies */
    private void addForcedResource(NResource resource) throws NOMException {
	if (resource==null) { return; }
	if (isLoaded(resource)) { return; }
	List forced = (List)forcedResources.get(resource.getCoding());
	if (forced==null) { forced = new ArrayList(); }
	forced.add(resource);
	forcedResources.put(resource.getCoding(), forced);
	preferDependencies(resource);
	noteChangedDependencies(resource);
    }

    /** get user input to select resource(s) */
    private JPanel buildResourceChoicePanelFromList(List resources, String elementname) {
	JPanel dpanel = new JPanel(new GridLayout(resources.size()+3,1));
	String lab = "NXT needs help in selecting which resource to use";
	if (elementname!=null) { lab += " for creating '"+elementname+"' elements."; }
	JLabel label = new JLabel(lab);
	dpanel.add(label);
	dpanel.add(label);
	ButtonGroup group = new ButtonGroup();

	globalResource=null;

	if (resources!=null) {
	    for (Iterator vit=resources.iterator(); vit.hasNext(); ) {
		NResource nvr=(NResource)vit.next();
		String desc = getResourceDescription(nvr);
		JRadioButton jrb = new JRadioButton(desc);
		jrb.addActionListener(new RealResourceCheckboxActionListener(nvr));
		group.add(jrb);
		dpanel.add(jrb);
	    }
	}

	JButton okay = new JButton("OK");
	okay.addActionListener(new OKActionListener());
	//dpanel.add(group);
	dpanel.add(okay);

	return dpanel;
    }

    /** get user input to select resource(s) */
    private NRealResource getRealResourceFromChoice(List resources, String elementname) {
	JPanel resourcechoice=buildResourceChoicePanelFromList(resources, elementname);
	dlog = new JDialog((JFrame)null, "Select a resource");
	dlog.setModal(true);
	finishedresources=false;
	WindowListener l = new WindowAdapter() {
		public void windowClosing(WindowEvent e) { finishedResources(); }
	    };
	dlog.addWindowListener(l);	
	dlog.getContentPane().add(resourcechoice);
	dlog.pack();
	dlog.setLocation(new Point(200, 200));
	dlog.show();
	return (NRealResource)globalResource;
    }

    /** get user input to select resource(s) */
    private NResource getResourceFromChoice(List resources, String elementname) {
	JPanel resourcechoice=buildResourceChoicePanelFromList(resources, elementname);
	dlog = new JDialog((JFrame)null, "Select a resource");
	dlog.setModal(true);
	finishedresources=false;
	WindowListener l = new WindowAdapter() {
		public void windowClosing(WindowEvent e) { finishedResources(); }
	    };
	dlog.addWindowListener(l);	
	dlog.getContentPane().add(resourcechoice);
	dlog.pack();
	dlog.setLocation(new Point(200, 200));
	dlog.show();
	return (NResource)globalResource;
    }

    /** If an element of a particular type and observation is being
     * created by a user action (i.e. not through loading a file),
     * this will decide if a resource should be associated with the
     * element, and if so, which resource */
    public NResource selectResourceForCreatedElement(String elementname, String observation) {
	NFile nfile = metadata.getElementByName(elementname).getFile();
	NOMFile tnf = new NOMFile(metadata, nfile, metadata.getObservationWithName(observation), null);
	loadRequestedColour(tnf.getColour());
	List nfs = (List)loadedNOMFiles.get(tnf.getColour());
	if (nfs==null) { 
	    Debug.print("Error trying to create element "+elementname+" but no metadata information available", Debug.ERROR);
	    return null; 
	}
	List resources = (List)loadedResources.get(nfs.get(0));
	if (resources==null || resources.size()==0) {
	    return null;
	}
	if (resources.size()>1) {
	    NRealResource rr = (NRealResource)createdResources.get(nfile);
	    if (rr!=null) { return rr; }
	    rr = getRealResourceFromChoice(resources, elementname);
	    createdResources.put(nfile, rr);
	    return rr; // could be null
	    //Debug.print("Multiple resources available for created element "+elementname+": choosing randomly!", Debug.ERROR);
	}
	return (NResource)resources.get(0);
    }

    /** Force one particular virtual or real resource over any
     * competing ones for the same coding. This causes all other
     * resources mentioned as dependents of this one to be preferred
     * too. Note that if there is no resource file, the old-style
     * convention is followed where each annotator gets a subdirectory
     * of the path specified in the metadata. If a resource file is
     * present but no resource is specified for the coding /
     * annotator, the appropriate resources are created using
     * essentially the same convention. */
    public void forceAnnotatorCoding(String annotator, String coding) throws NOMException {
	if (coding==null || annotator==null) {
	    System.err.println("Called forceAnnotatorCoding with annotator: " + annotator + " and coding: " + coding + ".\n Returning having done nothing!");
	    return;
	}
	NCoding ncod = metadata.getCodingByName(coding);
	if (ncod==null) {
	    throw new NOMException("Cannot find coding named '" + coding + "' in the metadata file.");
	}
	if (metadata.getResourceData()==null) {
	    forcedCodings.add(new AnnotatorCoding(annotator, ncod));
	} else {
	    NResourceData nrd = metadata.getResourceData();
	    List ress = nrd.getResourcesForCoding(coding);
	    NResourceGroup nrg=null;
	    NResource f=null;
	    if (ress==null) {
		// OK - the default is that the coding path is the same as the 
		// coding name, but if the metadata specifies a path, we use that 
		// instead (making it relative to resource file instead of metadata)
		String mypath=coding;
		String path = ncod.getPath();
		if (path!=null && !path.equals("") && !path.equals(metadata.getCodingPath())) { mypath=path; }
		if (mypath.contains(nrd.getResourceFilePath())) {
		    mypath = mypath.replaceFirst(nrd.getResourceFilePath(), "");
		    if (mypath.startsWith("/")) {
			mypath = mypath.replaceFirst("/", "");
		    }
		}
		nrg = new NiteResourceGroup(coding, mypath);
		nrd.addResourceGroup(nrg);
		//throw new NOMException("Cannot find annotator-specific resource in resource file for coding named '" + coding + "' and annotator '"+annotator+"'.");
	    } else {
		for (Iterator rit=ress.iterator(); rit.hasNext(); ) {
		    NRealResource t = (NRealResource)rit.next();
		    if (nrg==null) { nrg=t.getResourceGroup(); }
		    String a = t.getAnnotator();
		    if (a!=null && a.equalsIgnoreCase(annotator)) {
			f=t;
			break;
		    }
		}
	    }
	    if (f==null) {
		try {
		    f = new NiteResource(nrg, coding+"_"+annotator, annotator +"'s "+coding+" annotation", "manual", annotator, annotator, null);
		    nrd.addResource(coding, f);
		    System.out.println("Added new resource: " + f.getCoding());
		} catch (NiteMetaException nme) {
		    throw new NOMException("Cannot find annotator-specific resource in resource file for coding named '" + coding + "' and annotator '"+annotator+"'. \n Also failed to add it to the metadata!");
		}
	    }
	    if (f==null) {
		System.err.println("ERROR: failed to force annotator: " + annotator + " for coding: " + coding + ".\n Returning having done nothing!");
		return;
	    }
	    // only set and recurse through dependents if we were not already forced
	    List resses = null;
	    if (forcedResources!=null) { 
		//resses=(List)forcedResources.get(f.getCoding()); 
		resses=(List)forcedResources.get(coding); 
	    }
	    if (resses==null || !resses.contains(f)) {
		addForcedResource(f);
	    }
	}
    }

    /** Prefer one coding to be loaded for a specific annotator when
     * loadData is called. This means if there's no annotator data for
     * the coding (in fact its enclosing coding-file) we take any
     * 'gold-standard' data instead. If a resource file is present, we
     * use it. */
    public void preferAnnotatorCoding(String annotator, String coding) throws NOMException {
	NCoding ncod = metadata.getCodingByName(coding);
	if (ncod==null) {
	    throw new NOMException("Cannot find coding named '" + coding + "' in the metadata file.");
	}
	if (metadata.getResourceData()==null) {
	    preferredCodings.add(new AnnotatorCoding(annotator, ncod));	
	} else {
	    NResourceData nrd = metadata.getResourceData();
	    List ress = nrd.getResourcesForCoding(coding);
	    if (ress==null) {
		throw new NOMException("Cannot find annotator-specific resource in resource file for coding named '" + coding + "' and annotator '"+annotator+"'.");
	    }
	    NResource f=null;
	    for (Iterator rit=ress.iterator(); rit.hasNext(); ) {
		NRealResource t = (NRealResource)rit.next();
		String a = t.getAnnotator();
		if (a!=null && a.equalsIgnoreCase(annotator)) {
		    f=t;
		    break;
		}
	    }
	    if (f==null) {
		throw new NOMException("Cannot find annotator-specific resource in resource file for coding named '" + coding + "' and annotator '"+annotator+"'.");
	    }
	    List resses = (List)preferredResources.get(f.getCoding());
	    if (resses==null || !resses.contains(f)) {
		Debug.print("Adding preferred resource");
		addPreferredResource(f);
	    }
	}
    }

    /** Load all data for the corpus into the NOMCorpus. Incremental
     * loading of data is the default, so a new call to loadData will
     * not zero-out the data loaded in a previous call. */
    public void loadData( ) throws NOMException {
	//	clearData();
	loadrequest=true;
	List observations=metadata.getObservations();
	Iterator oit = observations.iterator();
	setBatchMode(true);
	while (oit.hasNext()) {
	    NiteObservation observation = (NiteObservation) oit.next();
	    loadObservationData(observation);
	    loaded_observations.add(observation);
	}
	completeLoad();
	setBatchMode(false);
	cleanupCorpus(false);
    }

    /** If the boolean argument is false, this version just makes sure
     * we don't add to the list of changed files. */
    protected void cleanupCorpus(boolean arg) {
	boolean lf = loadingfromfile;
	loadingfromfile=!arg;
	cleanupCorpus();
	loadingfromfile=lf;
    }

    /** Generally called after loading data, this method resolves
        parent / child links and pointers, and propagates any timing
        information as appropriate. */
    protected void cleanupCorpus() {
	cleaning_up=true;
	boolean ls = lazy_loading;
	lazy_loading=false;
	// first check the load and resolve pointers and nite children
	if (rootlist != null) {
	    Iterator rootit = new ArrayList(rootlist).iterator();
	    while (rootit.hasNext()) {
		NOMWriteElement rel = (NOMWriteElement) rootit.next();
		resolveTree(rel, rel.getColour());
	    }
	}
	// propagate timings
	propagateTimes();
	// It is wrong to clear changes here now that files can be
	//loaded at any time!  clearChanges();
	cleaning_up=false;
	lazy_loading=ls;
    }

    /** finds the first matching real resource from a virtual resource
     * that matches the observation from the NOMFile */
    private NResource findMatchingResourceChild(NVirtualResource nvr, NOMFile nomfile) {
	if (nvr==null || nvr.getDependencies()==null || nomfile==null) { return null; }
	String observation = nomfile.getObservation().getShortName();
	for (Iterator dit=nvr.getDependencies().iterator(); dit.hasNext(); ) {
	    NResourceDependency ndep = (NResourceDependency)dit.next();
	    try {
		if (Pattern.matches(ndep.getObservationRegexp(), observation)) {
		    return ndep.getResource();
		}
	    } catch (java.util.regex.PatternSyntaxException pex) {
		Debug.print("Regular expression '"+ndep.getObservationRegexp()+"' in resource file failed to parse! Nothing matched." , Debug.ERROR);
	    }
	}
	return null;
    }

    /** finds the first matching real resource from a virtual resource
     * that matches the observation from the NOMFile and has files
     * that match. */
    private NResource findMatchingResourceChildWithFiles(NVirtualResource nvr, NOMFile nomfile) {
	if (nvr==null || nvr.getDependencies()==null || nomfile==null) { return null; }
	String observation = nomfile.getObservation().getShortName();
	for (Iterator dit=nvr.getDependencies().iterator(); dit.hasNext(); ) {
	    NResourceDependency ndep = (NResourceDependency)dit.next();
	    try {
		if (Pattern.matches(ndep.getObservationRegexp(), observation) &&
		    hasMatchingFiles((NRealResource)ndep.getResource(), nomfile)) {
		    return ndep.getResource();
		}
	    } catch (java.util.regex.PatternSyntaxException pex) {
		Debug.print("Regular expression '"+ndep.getObservationRegexp()+"' in resource file failed to parse! Nothing matched." , Debug.ERROR);
	    } catch (ClassCastException cce) { }
	}
	return null;
    }

    private String getResourceDescription(NResource resource) {
	if (resource instanceof NVirtualResource) {
	    return "Virtual: " + resource.getID();
	} else {
	    NRealResource nrr = (NRealResource)resource;
	    String desc = nrr.getDescription();
	    if (desc!=null) {
		if (nrr.getAnnotator()!=null) { desc+=" by " + nrr.getAnnotator(); }
		else { desc += " ("+nrr.getID()+")"; }
	    } else {
		if (nrr.getType()==NRealResource.AUTOMATIC) {
		    desc = "Automatic annotation " + nrr.getID();
		} else {
		    desc = "Manual annotation ";
		    if (nrr.getAnnotator()!=null) { desc+="by " + nrr.getAnnotator(); } 
		    else { desc+=nrr.getID(); }
		}
	    }
	    return "Real: " + desc;
	}
    }

    /** get user input to select resource(s) */
    private JPanel buildResourceChoicePanel(NResourceData resourcedata, String coding) {
	List nvrs = resourcedata.getVirtualResourcesForCoding(coding);
	List rvrs = resourcedata.getResourcesForCoding(coding);
	

	JPanel dpanel = new JPanel(new GridLayout(nvrs.size()+rvrs.size()+4,1));
	JLabel label = new JLabel("NXT needs help in selecting which resource to load for coding '"+coding+"'.");
	dpanel.add(label);
	label = new JLabel("Leave all un-checked to load from metadata default location.");
	dpanel.add(label);
	
	globalResourceList=new ArrayList();	

	// go through virtual resources
	if (nvrs!=null) {
	    for (Iterator vit=nvrs.iterator(); vit.hasNext(); ) {
		NVirtualResource nvr=(NVirtualResource)vit.next();
		String desc = getResourceDescription(nvr);
		JCheckBox jcb = new JCheckBox(desc);
		jcb.addActionListener(new ResourceCheckboxActionListener(nvr));
		dpanel.add(jcb);
	    }
	}

	// go through real resources
	if (rvrs!=null) {
	    for (Iterator vit=rvrs.iterator(); vit.hasNext(); ) {
		NRealResource nvr=(NRealResource)vit.next();
		String desc = getResourceDescription(nvr);
		JCheckBox jcb = new JCheckBox(desc);
		jcb.addActionListener(new ResourceCheckboxActionListener(nvr));
		dpanel.add(jcb);
	    }
	}

	JButton okay = new JButton("OK");
	okay.addActionListener(new OKActionListener());
	dpanel.add(okay);
	return dpanel;
    }

    private class OKActionListener implements ActionListener {
	public void actionPerformed(ActionEvent ae) {
	    finishedResources();
	}
    }    

    private class ResourceCheckboxActionListener implements ActionListener {
	NResource resource;

	public ResourceCheckboxActionListener(NResource resource) {
	    super();
	    this.resource=resource;
	}

	public void actionPerformed(ActionEvent ae) {
	    if (((JCheckBox)ae.getSource()).isSelected()) {
		globalResourceList.add(resource);
	    } else {
		globalResourceList.remove(resource);
	    }
	}
    }

    private class RealResourceCheckboxActionListener implements ActionListener {
	NResource resource;

	public RealResourceCheckboxActionListener(NResource resource) {
	    super();
	    this.resource=resource;
	}

	public void actionPerformed(ActionEvent ae) {
	    //System.out.println("Setting global resource to: " + resource.getID());
	    globalResource = resource;
	}
    }

    /** finished choosing resources by closing window or hitting OK */
    private void finishedResources() {
	if (dialog!=null) { 
	    dialog.setVisible(false); 
	    dialog.dispose();
	}	
	if (dlog!=null) {
	    dlog.setVisible(false);
	    dlog.dispose();
	}
	dialog=null;
	finishedresources=true;
    }

    /** get user input to select resource(s) */
    private List getUserSelectedResourcesForCoding(NResourceData resourcedata, String coding) {
	JPanel resourcechoice=buildResourceChoicePanel(resourcedata, coding);
	dlog = new JDialog((JFrame)null, "Select resource(s)");
	dlog.setModal(true);
	finishedresources=false;
	WindowListener l = new WindowAdapter() {
		public void windowClosing(WindowEvent e) { finishedResources(); }
	    };
	dlog.addWindowListener(l);	
	dlog.getContentPane().add(resourcechoice);
	dlog.pack();
	dlog.setLocation(new Point(200, 200));
	dlog.show();
	return globalResourceList;
    }

    /** returns true if the resource passed actually contains files
     * for the particular observation referred to by the NOMFile */
    private boolean hasMatchingFiles(NRealResource nrr, NOMFile nomfile) {
	try {
	    String path = metadata.getResourceData().getResourcePath(nrr);
	    NFile nf = nomfile.getNFile();
	    String observation = "";
	    if (nomfile.getObservation()!=null) {
		observation=nomfile.getObservation().getShortName();
	    }
	    List fncheck = new ArrayList();
	    if (nf instanceof NCoding) {
		NCoding coding = (NCoding)nf;
		if (coding.getType()==NCoding.INTERACTION_CODING) {
		    fncheck.add(observation+"."+coding.getName()+".xml");
		} else {
		    for (Iterator agit = metadata.getAgents().iterator(); agit.hasNext(); ) {
			NAgent age = (NAgent)agit.next();
			fncheck.add(observation+"."+age.getShortName()+"."+coding.getName()+".xml");
		    }
		}
	    } else if (nf!=null) {
		fncheck.add(nf.getName()+".xml");
	    }
	    for (Iterator fit=fncheck.iterator(); fit.hasNext(); ) {
		String fn = path+File.separator+(String)fit.next();
		//Debug.print("Looking for file: " + fn, Debug.ERROR);
		File fc = new File(fn);
		if (fc.exists()) { return true; }
	    }
	} catch (Exception ex) {
	    //ex.printStackTrace();
	    return false;
	}
	return false;
    }

    /** remove from a List of NResources any that are listed as having
     * been loaded */
    private List removeLoadedResources(List res, NOMFile nf) {
	if (res==null || res.size()==0) { return res; }
	/* This doesn't work but I'm not sure why - JAK.
	List ld = (List)loadedResources.get(nf);
	if (ld==null) { return res; }
	List resres=new ArrayList();
	for (Iterator rit=res.iterator(); rit.hasNext(); ) {
	    Object r = rit.next();
	    if (!ld.contains(r)) { resres.add(r); }
	}
	return resres;
	*/
	List resres=new ArrayList();
	for (Iterator rit=res.iterator(); rit.hasNext(); ) {
	    NResource nr = (NResource)rit.next();
	    if (!isLoaded(nf,nr)) { resres.add(nr); }
	}
	return resres;
    }

    /** selects a resource that instantiates the coding given all the
     * specified preferences. If null is returned we'll try metadata locations. */
    private List selectResource(NOMFile nomfile, NResourceData nrd) {
	if (nrd==null) { return null; }
	NFile nc = nomfile.getNFile();
	boolean vrselected = false;
	List nr=null;

	if (forceresourceask) {
	    List ress = metadata.getResourceData().getResourcesForCoding(nc.getName());
	    if (ress==null) return nr;
	    if (ress.size()==1) {
		nr=new ArrayList();
		nr.add(ress.get(0));
		return nr;
	    }
	    nr = getUserSelectedResourcesForCoding(nrd, nc.getName());
	    Debug.print("User selection requested: " + nr, Debug.DEBUG);
	    if (nr!=null) { return nr; }
	}

	nr = (List)forcedResources.get(nc.getName());	
	if (nr==null) {
	    nr = (List)preferredResources.get(nc.getName());
	    if (nr==null) {
		nr = metadata.getResourceData().getDefaultedResourcesForCoding(nc.getName());
		if (nr==null) {
		    nr = new ArrayList();
		    List nvrs = nrd.getVirtualResourcesForCoding(nc.getName());
		    if (nvrs!=null) {
			for (Iterator vit=nvrs.iterator(); vit.hasNext(); ) {
			    NVirtualResource nvr=(NVirtualResource)vit.next();
			    NResource rr = findMatchingResourceChildWithFiles(nvr,nomfile);
			    if (rr!=null) { 
				nr.add(rr);
				vrselected=true;
				//break; 
			    }
			}
		    } 

		    if (nr.size()==0) {
			List nrrs = nrd.getResourcesForCoding(nc.getName());
			
			if (nrrs!=null) {
			    for (Iterator rit=nrrs.iterator(); rit.hasNext(); ) {
				NRealResource nrr=(NRealResource)rit.next();
				if (hasMatchingFiles(nrr,nomfile)) {
				    nr.add(nrr);
				    //break;
				} else {
				    Debug.print("Resource " + nrr.getID() + " for coding " + nc.getName() + " has no matching files on its path: " + metadata.getResourceData().getResourcePath(nrr) + "\n   so it's not being considered as a potential source of files.", Debug.DEBUG);
				}
			    }
			}		    
		    }
		    
		    // Don't consider loading already-loaded resources.
		    nr = removeLoadedResources(nr, nomfile);
		    
		    // It's only here we should ask users which
		    // resource they want (when there are multiple to
		    // choose from) if there are multiple forced or
		    // preferred, or even defaulted, that should lead
		    // to a reliability-style multiple load. That
		    // shouldn't happen here.
		    if (nr.size()>1) {
			nr = getUserSelectedResourcesForCoding(nrd, nc.getName());
			Debug.print("User selection requested");
		    }
		} 
	    }
	}
	return (List)nr;
    }

    private List getOverlap(List l1, List l2) {
	List ret = null;
	try {
	    for (Iterator l1it=l1.iterator(); l1it.hasNext(); ) {
		Object el1=l1it.next();
		if (l2.contains(el1)) {
		    if (ret==null) { ret=new ArrayList(); }
		    ret.add(el1);
		}
	    }
	} catch(Exception ex) { }
	return ret;
    }

    /** note any clashes between resources we're about to load and ask
     * the user to resolve them */
    private List resolveConflicts(List resources, NResourceData nrd, NOMFile nomfile) {
	if (resources==null || resources.size()==1) { return resources; }
	List resres = new ArrayList();
	List dealtwith = new ArrayList();
	for (int i=0; i<resources.size(); i++) {
	    NResource nr = (NResource)resources.get(i);
	    if (dealtwith.contains(nr)) { continue; }
	    List conflicts = nrd.getIncompatibleResources(nr);
	    if (i<resources.size()-1) {
		List remaining = resources.subList(i+1,resources.size());
		List overlaps = getOverlap(conflicts, remaining);
		if (overlaps!=null) {
		    overlaps.add(nr);
		    dealtwith.addAll(overlaps);
		    NResource r = getResourceFromChoice(overlaps, nomfile.getNFile().getName());
		    resres.add(r);
		} else {
		    resres.add(nr);
		}
	    } else {
		resres.add(nr);
	    }
	}
	return resres;
    }

    /** convenience method for loading taking observation and coding and NFile */
    private void loadDataFromNFile(NObservation observation, NCoding coding, NFile nf) {

    }

    /** This method works with resource files, or when no resource
     * file is present. When file is present, this checks all
     * instantiating resources for a particular requested observation
     * / coding pair. If there is more than one conflicting result and
     * no preference has been specified using forceResourceLoad or
     * preferResourceLoad this may end up popping up a choice
     * dialog. If multiple resources are preferred or forced, this
     * method loads them all.*/
    private void loadDataFromNOMFile(NOMFile nomfile) throws NOMException {
	NResourceData nrd = metadata.getResourceData();
	List resources = null;
	//NRealResource resource=null;

	// First check we haven't already loaded files of this colour
	if (anyLoaded(nomfile)) {
	    Debug.print("Resources already loaded for " + nomfile.getColour(), Debug.DEBUG);
	    return;
	}

	try {
	    resources = selectResource(nomfile, nrd);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	NFile nf = nomfile.getNFile();
	NObservation observation = nomfile.getObservation();

	if (resources==null || resources.size()==0) { // load from metadata locations
	    if (nf instanceof NCoding && !(nf instanceof NCorpusResourceCoding)) {
		NCoding coding=(NCoding)nf;
		//NOMException nex = new NOMException("debug");
		//nex.printStackTrace();
		Debug.print("Load non-resource coding: " + coding.getName(), Debug.DEBUG);
		if (coding.getType()==NCoding.INTERACTION_CODING) {
		    String colour=observation.getShortName()+"."+coding.getName();
		    String filename=chooseFileNameInteraction(observation, coding, colour);
		    loadDataFromFileCatchingExceptions(filename, colour, null,
						       observation.getShortName(), nf, null,
						       NOMContext.METADATA);
		} else if (coding.getType()==NCoding.AGENT_CODING) {
		    for (Iterator agit = metadata.getAgents().iterator(); agit.hasNext(); ) {
			NAgent age = (NAgent)agit.next();
			String colour = observation.getShortName() + "." + age.getShortName() + "." + coding.getName();
			String filename=chooseFileNameAgent(observation, coding, colour, metadata.getAgents());
			loadDataFromFileCatchingExceptions(filename, colour, null,
							  observation.getShortName(), nf, null,
							   NOMContext.METADATA);	
		    }
		}
	    } else {
		String filename="";
		if (nf instanceof NCorpusResourceCoding) {
		    filename=metadata.getCorpusResourcePath() + File.separator;
		} else if (nf instanceof NOntology) {
		    filename=metadata.getOntologyPath() + File.separator;
		} else if (nf instanceof NObjectSet) {
		    filename=metadata.getObjectSetPath() + File.separator;
		}
		filename+=nf.getName() + ".xml";
		loadDataFromFileCatchingExceptions(filename, nf.getName(), null,
						   null, nf, null,
						   NOMContext.METADATA);
	    }

	} else { // iterate through selected resources (normally one
		 // but multiple for reliability loads)
	    NRealResource resource=null;
	    List actuallyLoaded = new ArrayList();
	    resources = removeLoadedResources(resources, nomfile);
	    resources = resolveConflicts(resources, nrd, nomfile);
	    for (Iterator rit=resources.iterator(); rit.hasNext(); ) {
		NResource nr = (NResource)rit.next();
		if (anyLoaded(nrd.getIncompatibleResources(nr))) {
		    Debug.print("WARNING: NXT has been asked to load resource " + nr.getID() + " which is incompatible with an already-loaded resource. \n This resource will not be loaded and this could have unexpected results! ", Debug.IMPORTANT);
		    continue;
		}
		Debug.print("Loading Resource: " + nr.getID(), Debug.DEBUG);
		//NOMException nex=new NOMException("load resource: " + nr.getID());
		//nex.printStackTrace();
		removeChangedDependencies(nr);
		if (nr instanceof NVirtualResource) {
		    resource = (NRealResource)findMatchingResourceChildWithFiles((NVirtualResource)nr,nomfile);
		} else {
		    resource = (NRealResource)nr;
		}
		removeChangedDependencies(resource);
		String resourcepath=null;
		if (resource!=null) { resourcepath = nrd.getResourcePath(resource); }
		if (nf instanceof NCoding  && !(nf instanceof NCorpusResourceCoding)) {
		    NCoding coding=(NCoding)nf;
		    if (coding.getType()==NCoding.INTERACTION_CODING) {
			String colour=observation.getShortName()+"."+coding.getName();
			String filename=resourcepath+File.separator+colour+".xml";
			loadDataFromFileCatchingExceptions(filename, colour, resource,
						       observation.getShortName(), nf, null,
						       NOMContext.RESOURCE);
		    } else if (coding.getType()==NCoding.AGENT_CODING) {
			for (Iterator agit = metadata.getAgents().iterator(); agit.hasNext(); ) {
			    NAgent age = (NAgent)agit.next();
			    String colour = observation.getShortName() + "." + age.getShortName() + "." + coding.getName();
			    String filename=resourcepath+File.separator+colour+".xml";
			    loadDataFromFileCatchingExceptions(filename, colour, resource,
						       observation.getShortName(), nf, null,
						       NOMContext.RESOURCE);
			}
		    }
		} else {
		    String filename = resourcepath + File.separator + nf.getName() + ".xml";
		    loadDataFromFileCatchingExceptions(filename, nf.getName(), resource,
						       null, nf, null,
						       NOMContext.RESOURCE);
		}
		actuallyLoaded.add(resource);
		preferDependencies(resource);
	    }
	    loadedResources.put(nomfile, actuallyLoaded);
	}
    }

    /** Load data from a specified file name */
    //    protected void loadDataFromFileCatchingExceptions(String filename, String colour, NResource resource) throws NOMException {
    /** Load data from a specified file name */
    protected void loadDataFromFileCatchingExceptions(String filename, String colour, 
						      NResource resource, String observation, 
						      NFile nfile, String annotator, int type) 
	throws NOMException {
	if (loadedFiles.contains(filename)) { 
	    Debug.print("WARNING: NXT asked to load file " + filename + " when it is already loaded.", Debug.DEBUG);
	    if (resource!=null) { 
		Debug.print("       This could signal a problem with your resources file.", Debug.DEBUG);
	    }
	    return; 
	}
	try {
	    loadDataFromFile(filename, colour, resource);
	    context.addFile(observation, nfile, filename, type, resource, annotator);
	    loadedFiles.add(filename);
	} catch (FileNotFoundException exc) {
	    File tf = new File(filename);
	    if (tf.exists() && tf.isFile()) { 
		throw new NOMException("File Error: A DTD or other file reference from your XML file '" + filename + "' could not be resolved.");
	    }
	    Debug.print("File" + filename + " not found. Adding root node.", Debug.DEBUG);
	    loadingfromfile=true;
	    NOMElement newel = addRootElement(NOMAnnotation.class, colour, resource);
	    newel.setStringAttribute(NITE_NAMESPACE_NAME, NITE_NAMESPACE);
	    if (metadata.getLinkType()==NMetaData.XPOINTER_LINKS) {
		newel.setStringAttribute(XLINK_NAMESPACE_NAME, XLINK_NAMESPACE);
	    }
	    loadingfromfile=false;
	} catch (IOException exc) {
	    exc.printStackTrace();
	    throw new NOMException("IO error loading observation file: " + filename);
	}
    }

    /** Load data from a specified file name */
    protected void loadDataFromFile(String filename, String colour, NResource resource) throws NOMException, IOException {
	current_colour=colour;
	current_resource=resource;
	Debug.print("Loading data from file: " + filename, Debug.IMPORTANT);
	//NOMException nex = new NOMException("Stacktracing");
	//nex.printStackTrace();
	// make sure we're in batch mode while loading file, but store old value...
	boolean batchval = batch_load;
	batch_load=true;

	try{
	    parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
	    parser.setFeature(NAMESPACES_FEATURE_ID, true);
	    parser.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, true);
	    parser.setProperty(LEXICAL_HANDLER, this);
            parser.setContentHandler(new MyContentHandler());
	} catch (Exception pce) {
	    pce.printStackTrace();
	    current_resource=null;
	    batch_load=batchval;
	    throw new NOMException("Error loading file: " + filename);
	}
	try {
	    loadingfromfile=true;
	    // throws IO exception if file not found etc.
	    // Holger suggested the InputSource change...
	    // parser.parse(filename); 
	    parser.parse( new InputSource( new FileInputStream(filename) ) );
	    loadingfromfile=false;
	    //loadedResources.add
	} catch (SAXException saxex) {
	    loadingfromfile=false;
	    //saxex.printStackTrace();
	    current_resource=null;
	    batch_load=batchval;
	    throw new NOMException("SAX Exception (XML Error) loading file: " + filename);
	} catch (IOException ioe) {
	    // perhaps it's a URL...
	    try { 
		parser.parse(new InputSource((new URL(filename)).openStream()));
		loadingfromfile=false;
	    } catch (MalformedURLException muex) {
		loadingfromfile=false;		
		current_resource=null;
		batch_load=batchval;
		throw ioe;
	    } catch (SAXException saxex) {
		loadingfromfile=false;
		current_resource=null;
		batch_load=batchval;
		throw new NOMException("SAX Exception (XML Error) loading URL: " + filename);
	    }
	}
	current_resource=null;
	batch_load=batchval;
    }

    /** Uses the metadata information to calculate which files should
     *  be loaded for a given observation and loads that data.
     */
    private void loadObservationData(NiteObservation observation) throws NOMException {
	loadObservationData(observation, (List)null);
    }

    /** Uses the codings list (or metadata if that's null) to
     *  calculate which files should be loaded for a given observation
     *  and loads that data.
     */
    private void loadObservationData(NiteObservation observation, List codings) throws NOMException {
	if (observation==null) {
	    throw new NOMException("Failed to load null observation!");
	}
	if (metadata.getCorpusType()==NMetaData.SIMPLE_CORPUS) {
	    String fn = metadata.getCodingPath() + File.separator + observation.getShortName() + ".xml";
	    try {
		loadDataFromFile(fn, observation.getShortName(), null);
	    } catch (FileNotFoundException exc) {
		File tf = new File(fn);
		if (tf.exists() && tf.isFile()) { 
		    throw new NOMException("File Error: A DTD or other file reference from your XML file '" + fn + "' could not be resolved.");
		}
		Debug.print("File " + fn + " not found. Adding root node.", Debug.DEBUG);
		addRootElement(NOMAnnotation.class, current_colour, current_resource);
	    } catch (IOException exc) {
		exc.printStackTrace();
		throw new NOMException("IO error loading observation file: " + fn);
	    }

	} else {
	    List cods = codings;
	    if (cods==null) {
		cods = metadata.getCodings();
	    }
	    Iterator cit = cods.iterator();
	    NCoding nc;
	    
	    while (cit.hasNext())  {
		nc = (NCoding) cit.next();
		loadCode(nc, observation, null);
	    }
	}
    }

    /** this just helps me out by adding an extra attribute to every
     * loaded element. *
     protected void setExtraAttribute(String nat) {
     extra_attribute=nat;
     }

     /** no longer add the extra attribute *
     protected void unsetExtraAttribute() {
     extra_attribute="";
     }
    */


    /** select the name of the file to be loaded. The purpose of this
     * method is simply to work out whether to load a particular
     * annotator's data or the data in the gold-standard place.  */
    private SpecialLoad findSpecialLoad(NObservation obs, NCoding coding) {
	SpecialLoad sl = null;
	if (specialLoads==null) { return sl; }
	for (Iterator lit = specialLoads.iterator(); lit.hasNext(); ) {
	    SpecialLoad sl2 = (SpecialLoad)lit.next();
	    if (sl2.getObservation()==obs &&
		sl2.getCoding()==coding) {
		return sl2;
	    }
	}
	return sl;
    }

    /** find the annotator in a List of AnnotatorCodings that matches
     * the given NCoding, or return null */
    private String findAnnotator(List alist, NCoding coding) {
	if (alist==null || coding==null) { return null; }
	for (Iterator ait=alist.iterator(); ait.hasNext(); ) {
	    AnnotatorCoding ac = (AnnotatorCoding) ait.next();
	    if (coding==ac.getCoding()) {
		return ac.getAnnotator();
	    }
	}
	return null;
    }

    /** select the name of the file to be loaded for an interaction
     * coding. The purpose of this method is simply to work out
     * whether to load a particular annotator's data or the data in
     * the gold-standard place.  */
    private String chooseFileNameInteraction(NObservation obs, NCoding coding, 
					     String colour) {
	String path = coding.getPath();
	if (path==null) { path="."; }
	String mypath = path + File.separator + colour + ".xml";
	SpecialLoad oldLoad = findSpecialLoad(obs, coding);
	boolean forced=false;
	String myAnnotator = globalAnnotator;
	String forcedAnnotator = findAnnotator(forcedCodings, coding);
	String preferredAnnotator = findAnnotator(preferredCodings, coding);
	if (forcedAnnotator!=null) { forced=true; myAnnotator=forcedAnnotator; }
	else if (preferredAnnotator!=null) { myAnnotator=preferredAnnotator; }

	if (myAnnotator!=null) {
	    String anpath = path + File.separator + myAnnotator + 
		File.separator + colour + ".xml";
	    File f = new File(anpath);
	    if (oldLoad!=null || f.exists() || forced) { 
		mypath=anpath;
		//System.out.println("Add special load for colour: " + colour + " old load: " + oldLoad + "; File: " + anpath + "; exists: " +  f.exists() + "; forced: " + forced);
		if (oldLoad==null) {
		    specialLoads.add(new SpecialLoad(obs, coding, myAnnotator));
		}
	    } else {
		// if neither exists, load as this coder's data so we
		// can start a new coding.
	        f = new File(mypath);
		//System.out.println("Anpath: " + anpath + "; mypath: " + mypath + "; oldLoad: " + oldLoad + "; f exists: " + f.exists());
		if (oldLoad!=null || !f.exists()) { 
		    mypath=anpath;
		    //System.out.println("Add special load for colour: " + colour + "; oldLoad: " + oldLoad);
		    if (oldLoad==null) {
			specialLoads.add(new SpecialLoad(obs, coding, myAnnotator));
		    }
		}
	    }
	}
	return mypath;
    }

    /* check if any files exist, plugging in the agents one at a time */
    private boolean existsAgentFiles(String pre, List agents, String post) {
	if (agents==null) { return false; }
	for (Iterator ait = agents.iterator(); ait.hasNext(); ) {
	    String fn = pre + ((NAgent)ait.next()).getShortName() + post;
	    File f = new File(fn);
	    if (f.exists()) { return true; }
	}
	return false;
    }

    /** Unfortunately we need a completely different routine for agent
     * codings because the existence checks are different. We don't
     * want to check the individual file's existence, instead if *any*
     * agent's file is present in a preferred annotator directory we
     * want to force all to be loaded from there;  */
    private String chooseFileNameAgent(NObservation obs, NCoding coding, 
				       String colour, List agents) {
	String path = coding.getPath();
	String mypath = path + File.separator + colour + ".xml";
	SpecialLoad oldLoad = findSpecialLoad(obs, coding);
	boolean forced=false;
	String myAnnotator = globalAnnotator;
	String forcedAnnotator = findAnnotator(forcedCodings, coding);
	String preferredAnnotator = findAnnotator(preferredCodings, coding);
	if (forcedAnnotator!=null) { forced=true; myAnnotator=forcedAnnotator; }
	else if (preferredAnnotator!=null) { myAnnotator=preferredAnnotator; }

	if (myAnnotator!=null) {
	    String anpath = path + File.separator + myAnnotator + File.separator + colour + ".xml";
	    boolean exists = existsAgentFiles(path + File.separator + myAnnotator + File.separator + obs.getShortName() + ".", agents, "." + coding.getName() + ".xml");
	    if (oldLoad!=null || exists || forced) { 
		mypath=anpath;
		//System.out.println("Add special load for colour: " + colour + " old load: " + oldLoad + "; File: " + anpath + "; exists: " +  exists + "; forced: " + forced);
		if (oldLoad==null) {
		    specialLoads.add(new SpecialLoad(obs, coding, myAnnotator));
		}
	    } else {
		// if neither exists, load as this coder's data so we
		// can start a new coding.
		exists = existsAgentFiles(path + File.separator + obs.getShortName() + ".", agents, "." + coding.getName() + ".xml");
		if (oldLoad!=null || !exists) { 
		    mypath=anpath;
		    //System.out.println("Add special load for colour: " + colour + "; oldLoad: " + oldLoad);
		    if (oldLoad==null) {
			specialLoads.add(new SpecialLoad(obs, coding, myAnnotator));
		    }
		}
	    }
	}
	return mypath;
    }

    /** load a set of files given observation, coding and optional
     * agent, or just list the files as unloaded if lazy loading is
     * on. */
    protected boolean loadCode(NFile nc, NObservation observation, NAgent agent) throws NOMException {
	//NOMFile nomfile = new NOMFile(metadata, nc, observation, null);

	NOMFile nomfile = new NOMFile(metadata, nc, observation, agent);
	//Debug.print("loadCode - lazy loading is " + lazy_loading + "; coding is: " + nc.getName(), Debug.DEBUG);
	if (lazy_loading) {
	    return false;
	} 
	if (loadedNOMFiles.get(nomfile.getColour())==null || 
	    changedPreferences.get(nomfile.getNFile())!=null) {
	    List loaded = (List)loadedNOMFiles.get(nomfile.getColour());
	    //if (loaded==null || !loaded.contains(nomfile.getColour())) {
	    loadDataFromNOMFile(nomfile);
	    if (loaded==null) { loaded = new ArrayList(); }
	    loaded.add(nomfile);
	    loadedNOMFiles.put(nomfile.getColour(), loaded);
	    //Debug.print("loadedNOMFiles for " + nomfile.getColour() + ": " + loaded);
	    return true;
	    //}
	}
	return false;
    }

    /** Force the load of a particular element set. */
    protected boolean ensureLoadCode(NFile nc, NObservation observation, NAgent agent) throws NOMException {
	boolean loaded = false;
	//if (nc==null || observation==null) { return loaded; }
	if (nc==null) { return loaded; }
	boolean l = lazy_loading;
	lazy_loading=false;
	int ld = loading;
	if (nc instanceof NCorpusResourceCoding) { loading=CORPUSRESOURCE; }
	else if (nc instanceof NOntology) { loading=ONTOLOGY; }
	else if (nc instanceof NObjectSet) { loading=OBJECTSET; }
	else { loading=CODING; }

	//Debug.print("File is " + nc);

	try {
	    loaded=loadCode(nc, observation, agent);
	} catch (NOMException nex) {
	    lazy_loading=l;
	    throw nex;
	}
	lazy_loading=l;
	loading=ld;
	return loaded;
    }

    /** Load data for the purpose of comparing different coders'
     * data. This is a write-able implementation so it's invalid to do
     * this (we don't want anyone attempting to serialize this stuff).
     */
    public void loadReliability(NLayer top, NLayer top_common, String coder_attribute_name, String path, List observations) throws NOMException {
	throw new NOMException("This is a read/write NOM implementation: reliability comparisons can only be made on a read-only corpus as serialization will not be valid.");
    }

    /** Load data for the purpose of comparing different coders'
     * data. This is a write-able implementation so it's invalid to do
     * this (we don't want anyone attempting to serialize this stuff).
     */
    public void loadReliability(NLayer top, NLayer top_common, String coder_attribute_name, String path, List observations, List extra_layers) throws NOMException {
	throw new NOMException("This is a read/write NOM implementation: reliability comparisons can only be made on a read-only corpus as serialization will not be valid.");
    }

    /*----------------*/
    /* CLEARING DATA  */
    /*----------------*/

    /** Deletes all data in the NOM */
    public void clearData() {
	rootlist=null;
	element_stack=new Stack();
	element_hash = new Hashtable();
	element_list = new ArrayList();
	element_name_hash = new Hashtable();
	pointer_hash = new Hashtable();
	loaded_observations=new HashSet();
	unloadedFiles = new HashSet();
	loadedFiles = new HashSet();
	loadedParentLayers = new HashSet();
	loadedChildLayers = new HashSet();
	loadedToTimeColours = new HashSet();
	loadedNOMFiles = new Hashtable();
	loadedResources = new Hashtable();
	changedPreferences = new Hashtable();
	forcedResources = new Hashtable();
	preferredResources = new Hashtable();
	clearChanges();
	clearIDs();
	//System.gc();
	// these loads may seem odd, but it gets us back to the state
	// when we have just used the constructor
	initializeCorpus();
    }

    /** Removes any currently loaded data relating to the given observation */
    public void clearDataForObservation(NObservation ob) {
	if (ob==null) { return; }
	String obname = ob.getShortName();
	if (loaded_observations.contains((NiteObservation)ob)) {
	    // for now take the easiest option if we're the only obs loaded.
	    if (loaded_observations.size()==1) { clearData(); }
	    else {
		int elcount=0;
		for (Iterator elit=new ArrayList(element_list).iterator(); elit.hasNext(); ) {
		    NOMWriteElement nel = (NOMWriteElement)elit.next();
		    if (obname.equals(nel.getObservation())) {
			element_list.remove(nel);
			element_hash.remove(nel.getID());
			List els = (List)element_name_hash.get(nel.getName());
			if (els!=null) {
			    els.remove(nel);
			    element_name_hash.put(nel.getName(), els);
			}
			elcount++;
		    }
		}

		// reset changed colours to remove any that have been
		// removed from memory.
		Iterator cbit=new ArrayList(colours_changed).iterator();
		while (cbit.hasNext()) {
		    String colour = (String) cbit.next();
		    NOMFile nf = new NOMFile(metadata, colour);
		    if (nf.getObservation()==ob) {
			colours_changed.remove(colour);
			//System.out.println("Removed colour: " + colour + " from changed ones");
		    }
		    List nfs = (List)loadedNOMFiles.get(colour);
		    if (nfs!=null) {
			for (Iterator nit=nfs.iterator(); nit.hasNext(); ) {
			    NOMFile nomfile = (NOMFile)nit.next();
			    loadedResources.remove(nomfile);
			}
			loadedNOMFiles.remove(colour);
		    }
		}		

		// remove any unloadedFiles that come from the removed observation
		for (Iterator uit=new ArrayList(unloadedFiles).iterator(); uit.hasNext();) {
		    NOMFile nf = (NOMFile)uit.next();
		    if (nf.getObservation()==ob) {
			unloadedFiles.remove(nf);
		    }		    
		}

		// remove any loadedFiles that come from the removed observation
		for (Iterator uit=new ArrayList(loadedFiles).iterator(); uit.hasNext();) {
		    String nf = (String)uit.next();
		    if (nf.indexOf(obname)>-1) {
			loadedFiles.remove(nf);
		    }		    
		}

		// need to encourage Java to garbage collect - just
		// null out the root elements?
		if (rootlist!=null) {
		    for (Iterator rit=new ArrayList(rootlist).iterator(); rit.hasNext(); ) {
			NOMElement root=(NOMElement)rit.next();
			if (obname.equals(root.getObservation())) {
			    rootlist.remove(root);
			    root=null;
			}
		    }
		}

		// last index to tidy up: pointers_to. The only
		// elements that can have remaining pointers from the
		// removed observation are the corpus-resource type
		// ones. Annoyingly we have to go through each one's
		// list one by one.
		for (Iterator elit=element_list.iterator(); elit.hasNext(); ) {
		    NOMWriteElement nel = (NOMWriteElement)elit.next();
		    NElement nme = nel.getMetadataElement();
		    if (nme!=null && nme.getContainerType()!=NElement.CODING) {
			Set pt = (Set)pointer_hash.get(nel);
			if (pt!=null) {
			    for (Iterator pit=new ArrayList(pt).iterator(); pit.hasNext(); ) {
				NOMPointer p = (NOMPointer)pit.next();
				if (p==null) { continue; }
				NOMElement el=p.getFromElement();
				if (el==null) { continue; }
				if (el.getObservation()==obname) {
				    pt.remove(p);
				}
			    }
			    pointer_hash.put(nel, pt);
			}
		    }
		}

		// finally remember not to report this observation as
		// loaded any more.
		loaded_observations.remove(ob);
	    }
	}
    }

    /** Removes any currently loaded data relating to the named observation */
    public void clearDataForObservation(String ob) {
	clearDataForObservation(metadata.getObservationWithName(ob));
    }

    /** Clear the list of "colours" which will need to be saved to file. */
    private void clearChanges() {
	colours_changed.clear();
	resources_changed.clear();
    }

    /** Clear the ID hash and start again. */
    private void clearIDs() {
	idhash.clear();
    }

    
    /*-------------------------------------*/
    /* TOP LEVEL ACCESS TO NOM ELEMENTS    */
    /*-------------------------------------*/

    /** returns a List of NOMElements: the top level "stream" elements */
    public List getRootElements() {
	/* If you want to load everything regardless of lazy loading, reinstate this!
	   if (lazy_loading) {
	   completeLoad();
	   }
	*/
	return (List)rootlist;
    }

    /** returns the root NOMElement which has the given colour: we use
        'colour' in an NXT-specific way: it's precisely the filename
        the element will be serailized into, without its the '.xml'
        extension: thus it comprises observation name; '.'; the agent
        name followed by '.' (if an agent coding); the coding name. */
    public NOMElement getRootWithColour(String colour) {
	if (lazy_loading) { loadRequestedColour(colour); }
	if (rootlist==null) return null;
	for (Iterator rit=rootlist.iterator(); rit.hasNext(); ) {
	    NOMElement root=(NOMElement)rit.next();
	    if (colour.equals(root.getColour())) {
		return root;
	    }
	}
	return null;
    }

    /** returns the root NOMElement which has the given colour and
        resource: we use 'colour' in an NXT-specific way: it's
        precisely the filename the element will be serailized into,
        without its the '.xml' extension: thus it comprises
        observation name; '.'; the agent name followed by '.' (if an
        agent coding); the coding name. */
    public NOMElement getRootWithColour(String colour, NResource resource) {
	if (lazy_loading) { loadRequestedColour(colour); }
	if (rootlist==null) return null;
	for (Iterator rit=rootlist.iterator(); rit.hasNext(); ) {
	    NOMElement root=(NOMElement)rit.next();
	    if (colour.equals(root.getColour()) && (resource==null || resource==root.getResource())) {
		return root;
	    }
	}
	return null;
    }

    /* IDs should be globally unique in NITE corpora, though colour is
       used here */
    protected NOMWriteElement findElementWithID(String id, String colour) throws NOMException {
	if (lazy_loading) { loadRequestedColour(colour); }
	String key = colour + "#" + id;
	if (metadata.getCorpusType()==NMetaData.SIMPLE_CORPUS) {
	    key = id;
	}
	NOMWriteElement nre = (NOMWriteElement)element_hash.get(key);
	if (nre == null && !lazy_loading) {
	    if (isValidating() && !cleaning_up) {
		throw new NOMException("It's illegal to refer to a non-existant element. Element: " + colour + " ("  + id + ") doesn't exist. (turn off validation to get round this)");
	    } else if (!cleaning_up) {
		Debug.print("WARNING: Failed to find element with id " + id + " and colour " + colour + "!!", Debug.WARNING);
	    }
	}
	return nre;
    }

    /** Return a list of NOMWriteElements which have the given element
	name. */
    public List getElementsByName(String name) {
	if (lazy_loading) { loadRequestedElementName(name); }
	return (List)element_name_hash.get(name);	    
    }

    /* Return the element with the given 'colour' (i.e. filename
     * without .xml extension) and ID */
    public NOMElement getElementByID(String colour, String id) {
	if (colour==null) { return getElementByID(id); }
	return getElementByID(colour + LTXML1_LINK_FILE_SEPARATOR + id);
    }

    /** Return a NOMWriteElement which has the given element ID: you
     * can either pass an unadorned ID in which case NXT searches for
     * the element in all already-loaded files, or you can specify the
     * 'full' ID like this: colour#id (e.g. q4nc4.f.moves#move.3 would refer to
     * element 'move.3' in the file q4nc4.f.moves.xml) */
    public NOMElement getElementByID(String id) {
	if (element_hash==null || id==null) { return null; }
	if (id.indexOf(LTXML1_LINK_FILE_SEPARATOR)>0) {
	    if (lazy_loading) { loadRequestedColour(id); }
	    return (NOMWriteElement)element_hash.get(id);
	} else if (rootlist!=null) {
	    for (Iterator rit = rootlist.iterator(); rit.hasNext(); ) {
		NOMElement r = (NOMElement) rit.next();
		if (r==null || r.getColour()==null) { continue; }
		//System.out.println("Looking for " + r.getColour() + "; " + id);
		NOMElement n = getElementByID(r.getColour(), id);
		if (n!=null) { return n; }
	    }
	}
	return null;
    }

    /**
     * Add a new root element of the given type and colour.
     * 
     * @param type
     * the type.
     * @param colour
     * the colour.
     * @return the root element.
     * @throws NOMException
     * if something is wrong.
     */
    private NOMElement addRootElement(Class type, String colour, NResource resource)
	throws NOMException {
	NOMElement el = null;
	String rootName = metadata.getStreamElementName();
	if (NOMTypeElement.class.equals(type)) {
	    el = nommaker.make_type_element(this, rootName, colour, resource, true, colour);
	} else if (NOMObject.class.equals(type)) {
	    el = nommaker.make_object(this, rootName, colour, resource, true, colour);
	} else if (NOMResourceElement.class.equals(type)) {
	    el = nommaker.make_resource_element(this, rootName, colour, resource, true, colour);
	} else if (NOMAnnotation.class.equals(type)) {
	    el = nommaker.make_annotation(this, rootName, colour, resource, true, colour);
	} else {
	    Debug.print("Unsupported element type in addRootElement(): "
			+ type.getName(), Debug.WARNING);
	    return null;
	}
	addRootElement(el);
	// Remove the colour from the list of changed colours: we don't want to
	// save it if this is the only change.
	removeChangedColour(colour);
	removeUnloaded(colour+".xml");
	return el;
    }

    private void addRootElement(Object el) {
	if (rootlist==null) {
	    rootlist=new ArrayList();
	}
	rootlist.add(el);
    }

    /** this will be called when there has been no loadData, but
     * addToCorpus (or similar) has been invoked. This may be true
     * when you want to start an observation from scratch. */
    protected NOMWriteElement createRootIfValid(String colour) {
	try {
	    return (NOMWriteElement)addRootElement(NOMObject.class, colour, current_resource);
	} catch (NOMException nex) { 
	    Debug.print("Failed to create root element with colour: " + colour, Debug.ERROR);
	    return null;
	}
    }


    /** Return the deepest nesting of elements in this recursive layer
        (if the layer is not recursive, returns 1 or 0) */
    public int getMaxDepth(NLayer layer) {
	int depth=0;
	if (layer==null) { return depth; }
	if (!layer.getRecursive()) {
	    for (Iterator cit = layer.getContentElements().iterator(); cit.hasNext(); ) {
		NElement nel = (NElement) cit.next();
		if (nel!=null && getElementsByName(nel.getName())!=null) { return 1; }
	    }
	} else {
	    Object cont = layer.getContainer();
	    if (cont instanceof NCoding) {
		for (Iterator oit=loaded_observations.iterator(); oit.hasNext(); ) {
		    NObservation nob = (NObservation)oit.next();
		    if (((NCoding)cont).getType()==NCoding.AGENT_CODING) {
			for (Iterator ait=metadata.getAgents().iterator(); ait.hasNext(); ) { 
			    NAgent ag = (NAgent)ait.next();
			    String col=nob.getShortName() + "." + ag.getShortName() + "." + ((NCoding)cont).getName();			    
			    int nd=getMaxDepthColour(col, layer);
			    if (nd>depth) { depth=nd; }
			}
			
		    } else {
			String col=nob.getShortName() + "." + ((NCoding)cont).getName();
			int nd=getMaxDepthColour(col, layer);
			if (nd>depth) { depth=nd; }
		    }
		}
	    }
	}
	return depth;
    }

    /** Get the max depth for a particular colour by getting the root
        and recursing - we use 'colour' in an NXT-specific way: it's
        precisely the filename the element will be serailized into,
        without its the '.xml' extension: thus it comprises
        observation name; '.'; the agent name followed by '.' (if an
        agent coding); the coding name. */
    private int getMaxDepthColour(String colour, NLayer layer) {
	int deep=0;
	NOMElement roo = getRootWithColour(colour);
	List roots = getLayerRoots(roo, layer);
	if (roots==null) { return deep; }
	for (Iterator kit=roots.iterator(); kit.hasNext(); ) {
	    NOMElement ch = (NOMElement)kit.next();
	    int md = getDepthInLayer(ch);
	    if (md>deep) { deep=md; }
	}
	return deep;
    }

    /** get the top level of a recursive layer within (possibly) a
        multiple -layer file */
    private List getLayerRoots(NOMElement el, NLayer layer) {
	ArrayList ret= new ArrayList();
	if (el==null) { return ret; }
	try {
	    if (el.isStreamElement()==false && el.getLayer()==layer) { ret.add(el);}
	    else {
		List kids = el.getChildren();
		if (kids==null) { return ret; }
		for (Iterator kit = kids.iterator(); kit.hasNext(); ) {
		    NOMElement kid = (NOMElement)kit.next();
		    List kl = getLayerRoots(kid,layer);
		    if (kl.size()!=0) { ret.addAll(kl); }
		}
	    }
	} catch (NOMException nex) { }
	return ret;
    }


    /** Get max depth of a particular element within its own layer */
    private int getDepthInLayer(NOMElement el) {
	int elcount=1;
	if (el.getChildren()==null) { return elcount; }
	try{
	    for (Iterator kkit=el.getChildren().iterator(); kkit.hasNext(); ) {
		NOMElement child = (NOMElement)kkit.next();
		if (child.getLayer()==el.getLayer()) {
		    int thisone = 1+getDepthInLayer(child);
		    if (thisone>elcount) { elcount=thisone; }
		}
	    }
	} catch (NOMException nex) { }
	return elcount;
    }


    /*------------------------------------*/
    /* SET AND GET CORPUS-WIDE PROPERTIES */
    /*------------------------------------*/

    /** returns true if the corpus is validating (i.e. if it is
        checking against the metadata whether changes are valid). The
        default value for validation is true */
    public boolean isValidating() {
	if (metadata != null) { return metadata.isValidating(); }
	return validating;
    }

    /** Set validation for the corpus. The default value for
        validation is true. */
    public void setValidation(boolean validate) {
	if (metadata != null) { metadata.setValidation(validate); }
	validating=validate;
    }

    /** returns the metadata associated with this NOM */
    public NMetaData getMetaData() {
	return (NMetaData) metadata;
    }

    /** returns a List of NObservation elements - each one the name of an
     * observation that has been asked to be loaded (how much, if any
     * of the observation data actually loaded depends on lazy
     * loading). */
    public List getLoadedObservations() {
	return new ArrayList(loaded_observations);
    }

    /** Set to true to make future serialization calls serialize with
        stream element names conforming to meta.getStreamElementName().
	Default is that stream elements will be output as they are input.
    */
    public void setForceStreamElementNames(boolean bool) {
	serialize_forced_stream_element_names=bool;
    }

    /** If this method is used with a non-null argument, we make sure
        the schema instance namespace is output on every stream-like
        element on serialization along with this as the
        noNamespaceSchemaLocation */
    public void setSchemaLocation(String location) {
	schemalocation=location;
    }

    /** Set to true (default) to lazy-load any future calls to load data;
     * false means everything in future load calls is loaded up-front. */
    public void setLazyLoading(boolean bool) {
	// If we switch lazy loading off and we have already loaded
	// some data, we now need to load all the remaining data
	if (lazy_loading && !bool && loadrequest) {
	    completeLoad();
	}
	lazy_loading=bool;
    }

    /** part of a complete load - make sure all corpus-level data (ontologies etc) are loaded into the NOM. Return true if this causes any files to be loaded. */
    private boolean ensureCorpusLevelDataLoaded() {
	boolean loaded=false;
	try {
	    List ontologies = metadata.getOntologies();
	    if (ontologies!=null) {
		for (Iterator oit=ontologies.iterator(); oit.hasNext(); ) {
		    NOntology ont = (NOntology)oit.next();
		    Debug.print("Loading ontology: " + ont.getElementName());
		    //NFile nfile = metadata.getElementByName(ont.getElementName()).getFile();
		    if (ensureLoadCode(ont, null, null)) { loaded=true; }
		}
	    }

	    List objectsets = metadata.getObjectSets();
	    if (objectsets!=null) {
		for (Iterator oit=objectsets.iterator(); oit.hasNext(); ) {
		    NObjectSet ont = (NObjectSet)oit.next();
		    if (ensureLoadCode(ont, null, null)) { loaded=true; }
		}
	    }

	    List corpusresources = metadata.getCorpusResources();
	    if (corpusresources!=null) {
		for (Iterator oit=corpusresources.iterator(); oit.hasNext(); ) {
		    NCorpusResource cr = (NCorpusResource)oit.next();
		    if (ensureLoadCode(cr, null, null)) { loaded=true; }
		}
	    }
	} catch (Exception ex) { }
	return loaded;
    }

    /** finish loading *all* files we know about from the corpus: this
     * only makes sense if lazy loading is switched on, otherwise it
     * will do nothing. */
    public void completeLoad() {
	boolean loaded=false;
	loaded=ensureCorpusLevelDataLoaded();
	try {
	    for (Iterator oit=loaded_observations.iterator(); oit.hasNext(); ) {
		NObservation obs=(NObservation)oit.next();
		for (Iterator elit=metadata.getAllElements().iterator(); elit.hasNext(); ) {
		    NElement el = (NElement)elit.next();
		    //Debug.print("Loading element: " + el.getName());
		    NFile nfile = el.getFile();
		    if (ensureLoadCode(nfile, obs, null)) { loaded=true; }
		}
	    }
	} catch (Exception ex) { }
	if (loaded) { cleanupCorpus(false); }	
    }

    /** Set to true (default) to lazy-load any future calls to load data;
     * false means everything in future load calls is loaded up-front. */
    public boolean isLazyLoading() {
	return lazy_loading;
    }

    /** Set to true to make future serialization calls serialize with
        inherited times on structural elements. Set to false (default)
        to only serialize start and end times on timed elemets. */
    public void setSerializeInheritedTimes(boolean bool) {
	serialize_inherited_times=bool;
    }
    
    /** True if we should allow inherited times to be serialized */
    public boolean serializeInheritedTimes() {
	return serialize_inherited_times;
    }

    /** True if we should  serialize ranges */
    public boolean serializeMaximalRanges() {
	return serialize_maximal_ranges;
    }

    /** Set to true (default) to make future serialization calls
        serialize with ranges where possible. Set to false to
        explicitly list all nite children. */
    public void setSerializeMaximalRanges(boolean bool) {
	serialize_maximal_ranges=bool;
    }

    /** Link syntax information: get the String that separates a
        filename from an ID */
    public String getLinkFileSeparator() {
	return linkfileseparator;
    }

    /** Link syntax information: get the String that appears before an ID */
    public String getLinkBeforeID() {
	return linkbeforeid;
    }

    /** Link syntax information: get the String that appears after an ID */
    public String getLinkAfterID() {
	return linkafterid;
    }

    /** Link syntax information: get the String that appears between
        IDs in a range */
    public String getRangeSeparator() {
	return linkrangeseparator;
    }

    /** Link syntax information: get the name of the 'href' attribute */    
    public String getHrefAttr() {
	return linkhrefattr;
    }

    private void setLinkSyntax() {
	if (metadata.getLinkType()==NMetaData.XPOINTER_LINKS) {
	    linkfileseparator=XPOINTER_LINK_FILE_SEPARATOR;
	    linkrangeseparator=XPOINTER_RANGE_SEPARATOR;
	    linkrangeend=XPOINTER_RANGE_END;
	    linkend=XPOINTER_LINK_END;
	    linkbeforeid=XPOINTER_LINK_BEFORE_ID;
	    linkafterid=XPOINTER_LINK_AFTER_ID;
	    linkextraattr=XPOINTER_EXTRA_ATTRIBUTE;
	    linkhrefattr=XPOINTER_HREF_ATTRIBUTE;
	} else {
	    linkfileseparator=LTXML1_LINK_FILE_SEPARATOR;
	    linkrangeseparator=LTXML1_RANGE_SEPARATOR;
	    linkrangeend=LTXML1_RANGE_END;
	    linkend=LTXML1_LINK_END;
	    linkbeforeid=LTXML1_LINK_BEFORE_ID;
	    linkafterid=LTXML1_LINK_AFTER_ID;
	    linkextraattr=LTXML1_EXTRA_ATTRIBUTE;
	    linkhrefattr=LTXML1_HREF_ATTRIBUTE;
	}
    }


    /*----------------------*/
    /* PROPAGATION OF TIMES */
    /*----------------------*/

    /** propagate times from timed elements up to their structural
        parents. */
    private void propagateTimes() {
	// first find all the timed element names
	List timedlayers =  metadata.getLayersByType(NLayer.TIMED_LAYER);
	Iterator tlit = timedlayers.iterator();
	while (tlit.hasNext()) {
	    propagateUpFrom((NLayer)tlit.next());
	}
    }

    private void propagateUpFrom(NLayer timed_layer) {
	List parels = timed_layer.getContentElements();
	Iterator elit = parels.iterator();
	
	Set allparents = new HashSet();
	// for all elements in the timed layer
	while (elit.hasNext()) {
	    NElement nelement = (NElement)elit.next();
	    //	    Debug.print("Find all " + elname + "'s");
	    // find the instances of the element in the NOM
	    ArrayList al = (ArrayList) element_name_hash.get(nelement.getName());
	    if (al!=null) {
		Iterator alit = al.iterator();
		while (alit.hasNext()) {
		    NOMWriteElement nre = (NOMWriteElement) alit.next();
		    if (!Double.isNaN(nre.getStartTime()) || 
			!Double.isNaN(nre.getEndTime()) ) {
			allparents.addAll(nre.getParents());
		    }
		}
	    }
	}
	for (Iterator pit=allparents.iterator(); pit.hasNext(); ) {
	    NOMWriteElement nre = (NOMWriteElement) pit.next();
	    nre.updateTimes();	    
	}
    }

    /*-------------------*/
    /* ITERATOR OVER NOM */
    /*-------------------*/

    /** Provides an iterator which visits each element in the NOM
	exactly once. We guarantee to traverse each "document" in
	document order, where "document" refers to a file that is read
	in or a pseudo-file that is created internally when data is
	loaded for a particular purpose. These "documents" are not
	considered to be ordered. */
    public Iterator NOMWalker() {
	return new CorpusIterator();
    }

    /** This internal class provides an iterator over the NOM. We make
        a number of restrictions in the attempt to make this reasonably
        efficient:
	1. The "remove" method is not implemented. 
	2. Edits to the underlying structure that occur during iteration 
	are not reflected in the iterator. 
    */
    class CorpusIterator implements Iterator {
	//private int docnr = 0;
	//private boolean first=true;
	//	private Enumeration enum  = ids[docnr];
	Iterator myit;
	//private NOMElement current_element= (NOMElement)rootlist.get(docnr);
	
	/** constructor */
	public CorpusIterator() {
	    try {
		myit=element_list.iterator();
	    } catch (Exception e) { 
		//		throw new NOMException("Cannot make iterator!");
	    }
	}

	public boolean hasNext() {
	    if (myit==null) { return false; }
	    return myit.hasNext();
	}

	public Object next() throws NoSuchElementException {
	    if (myit==null) { throw new NoSuchElementException(); }
	    return myit.next();
	}

	/*
	  public boolean hasNext() {
	  if (first==true) {
	  if (current_element instanceof NOMWriteTypeElement) { return true; }
	  }
	  if ( current_element.hasNextElement() ) {
	  return true;
	  } else {
	  boolean hasMore = false;
	  for (int i=docnr+1; i<rootlist.size(); i++) {
	  NOMElement nx = (NOMElement)rootlist.get(i);
	  if ( nx instanceof NOMWriteTypeElement || 
	  nx.getChildren()!=null ) {
	  hasMore = true;
	  break;
	  }
	  }
	  return hasMore;
	  }
	  }
	
	  public Object next() throws NoSuchElementException {
	  if (first==true) {
	  first=false;
	  if (current_element instanceof NOMWriteTypeElement) { 
	  return current_element; 
	  }
	  }
	  if ( current_element.hasNextElement() ) {
	  current_element=current_element.getNextElement();
	  return current_element;
	  } else {
	  boolean hasMore = false;
	  for (int i=docnr+1; i<rootlist.size(); i++) {
	  NOMElement nx = (NOMElement)rootlist.get(i);
	  if (nx instanceof NOMWriteTypeElement) {
	  current_element=nx;
	  docnr=i;
	  return current_element;
	  } else if ( nx.getChildren()!=null ) {
	  current_element=(NOMElement)nx.getChildren().get(0);
	  docnr=i;
	  return current_element;
	  }
	  }
	  }
	  throw new NoSuchElementException();
	  }
	*/
	
	/** Unimplemented - in an attempt to make this efficient */
	public void remove() {
	    throw new UnsupportedOperationException();
	};

    }



    /*----------------*/
    /* SERIALIZE DATA */
    /*----------------*/

    /** Serialize all loaded files */
    public void serializeCorpus() throws NOMException {
	onlysaveifchanged=false;
	setLinkSyntax();
	saveerror=false;
	Iterator obit=loaded_observations.iterator();
	while (obit.hasNext()) {
	    NiteObservation obs = (NiteObservation) obit.next();
	    saveObservationData(obs);
	}
	saveObjectSets();
	saveOntologies();
	saveCorpusResources();
	if (!saveerror) {
	    clearChanges();
	} else {
	    throw new NOMException("SAVE failed! Check file permissions.");
	}
    }

    /** Serialize all files which have been changed. */
    public void serializeCorpusChanged() throws NOMException {
	setLinkSyntax();
	saveerror=false;
	onlysaveifchanged=true;
	
	Iterator cbit=colours_changed.iterator();
	//System.out.println("SAVE COLOURS: " + colours_changed.size() + "; " + cbit.hasNext());

	// If we're serializing only the changed files, there are
	// potential problems with ranges in non-saved higher layers.
	// To solve this we need to save layers above the saved ones.	

	List resourceSaves = new ArrayList();

	// for each colour, note all the NOMFiles that are loaded.
	while (cbit.hasNext()) {
	    String colour = (String) cbit.next();
	    NOMFile nf2 = new NOMFile(metadata, colour);
	    String indexcolour = nf2.getIndexColour();
	    //Debug.print("SAVE COLOUR: " + indexcolour + " (" + nf2.getColour() + ")");
	    if (loadedNOMFiles.get(indexcolour)==null) { loadRequestedColour(colour); }
	    saveCommon(nf2);
	}

	// do them all for now
	if (resourceSaves.size()>0) {
	    saveCorpusResources();
	    saveObjectSets();
	    saveOntologies();
	}
	if (!saveerror) {
	    clearChanges();
	} else {
	    throw new NOMException("SAVE failed! Check file permissions.");
	}
    }

    /** attempt to factor out some of the things that are common to
     * all saves: we only save what we have loaded; we need to allow
     * for both resource and non-resource directory specification. */
    private void saveCommon(NOMFile nomfile) throws NOMException {
	List nfs = (List)loadedNOMFiles.get(nomfile.getIndexColour());
	String colour = nomfile.getColour();
	if (nfs==null) { 
	    //Debug.print("No nom files for " + nomfile.getIndexColour(), Debug.WARNING); 
	    return; 
	}
	//Debug.print("Nom files for " + nomfile.getIndexColour() + ": " + nfs);
	// for each NOMFile check whether metadata resources exist
	// - if so, save them using resource paths, otherwise use
	// standard metadata paths.
	for (Iterator nit=nfs.iterator(); nit.hasNext(); ) {
	    NOMFile nf = (NOMFile)nit.next(); 
	    nf.agent=nomfile.getAgent();
	    List resources = (List)loadedResources.get(nf);
	    if (resources==null) { // no metadata resources for this coding - use metadata
		//Debug.print("NO Resources");
		String fn=null;
		if (nf.getType()==NOMFile.RESOURCE) {
		    fn = getCorpusResourceFilename(nf.getNFile());
		} else {
		    fn = getCodingFilename(nf.getObservation(), (NCoding)nf.getNFile(), nf.getAgent());
		}
		saveDataToFile(fn, colour, null);
	    } else { // use resource paths (and potentially save multiple files)..
		//Debug.print("Resources");
		for (Iterator rit=resources.iterator(); rit.hasNext(); ) {
		    NRealResource nr = (NRealResource)rit.next();
		    //Debug.print("Resource "+nr.getID()+ " Changed: " + resources_changed.contains(nr));
		    if (!onlysaveifchanged || resources_changed.contains(nr)) {
			String fn=metadata.getResourceData().getResourcePath(nr)+File.separator+colour+".xml";
			saveDataToFile(fn, colour, nr);
		    }
		}
	    }
	}
    }

    /** Serialize all loaded files for the given list of observations */
    public void serializeCorpus(List observations) throws NOMException {
	// this avoids loading remote files during save - there's never any point!
	boolean laz = lazy_loading;
	lazy_loading=false;
	onlysaveifchanged=false;
	setLinkSyntax();
	saveerror=false;
	if (observations==null) { serializeCorpus(); return; }
	Iterator obit=observations.iterator();
	while (obit.hasNext()) {
	    NiteObservation obs = (NiteObservation) obit.next();
	    saveObservationData(obs);
	}
	saveObjectSets();
	saveOntologies();
	saveCorpusResources();
	lazy_loading=laz;
	if (!saveerror) {
	    clearChanges();
	} else {
	    throw new NOMException("SAVE failed! Check file permissions.");
	}
    }

    /** Return the actual file to which this data should be serialized
     * (including any annotator-specific subdirectory). */
    public String getCodingFilename(NObservation no, NCoding co, NAgent ag) {
	NOMFile nf = new NOMFile(metadata, co, no, ag);
	String fn = nf.getFullFilename();
	// we may have loaded this for a particular annotator. If
	// so, save it right back there...
	SpecialLoad sl = findSpecialLoad(no, co);
	if (sl!=null) {
	    fn = co.getPath() + File.separator + sl.getAnnotator() +
		File.separator + nf.getColour() + ".xml";
	}
	return fn;
    }

    /** Return the actual file to which this corpus resource data
     * should be serialized (we have already established that we're
     * using metadata paths rather than resource file paths here).  */
    private String getCorpusResourceFilename(NFile res) {
	String path="";
	if (res instanceof NCorpusResource) {
	    path = metadata.getCorpusResourcePath();
	} else if (res instanceof NOntology) {
	    path = metadata.getOntologyPath();
	} else if (res instanceof NObjectSet) {
	    path = metadata.getObjectSetPath();
	} 
	return path + File.separator + res.getFileName() + ".xml";
    }

    
    /** saves all ontologies */
    private void saveOntologies() {
	List Ontologies=metadata.getOntologies();
	Iterator oit = Ontologies.iterator();
	saving=ONTOLOGY;
	while (oit.hasNext()) {
	    NiteOntology ontology = (NiteOntology) oit.next();
	    try {
		saveCommon(new NOMFile(metadata, ontology, null, null));
	    } catch (NOMException ex) {
		Debug.print("Error saving ontology: " + ontology.getName());
	    }
	}
	saving=CODING;
    }

    /** saves all object sets */
    private void saveObjectSets() {
	// Load in objectsets
	List objectsets=metadata.getObjectSets();
	Iterator oit = objectsets.iterator();
	saving=OBJECTSET;
	while (oit.hasNext()) {
	    NiteObjectSet objectset = (NiteObjectSet) oit.next();
	    try { saveCommon(new NOMFile(metadata, objectset, null, null));  } 
	    catch (NOMException ex) { Debug.print("Error saving object set: " + objectset.getName()); }
	}	
	saving=CODING;
    }

    /** saves all corpus resources */
    private void saveCorpusResources() {
	List corpusresources=metadata.getCorpusResources();
	Iterator oit = corpusresources.iterator();
	saving=CORPUSRESOURCE;
	while (oit.hasNext()) {
	    NiteCorpusResource corpusresource = (NiteCorpusResource) oit.next();
	    try { saveCommon(new NOMFile(metadata, corpusresource, null, null)); }
	    catch (NOMException ex) { Debug.print("Error saving corpus resource: " + corpusresource.getName()); }
	}
	saving=CODING;
    }


    /** Uses the metadata information to calculate which files should
     *  be saved for a given observation and saves that data.
     */
    private void saveObservationData(NiteObservation observation) throws NOMException {

	if (metadata.getCorpusType()==NMetaData.SIMPLE_CORPUS) {
	    String fn = metadata.getCodingPath() + File.separator + observation.getShortName()+ ".xml";
	    Debug.print("Save observation file: " + fn, Debug.IMPORTANT);
	    saveDataToFile(fn, observation.getShortName(), null);
	    return;
	} 

	List cods = metadata.getCodings();
	Iterator cit = cods.iterator();
	NCoding nc;

	while (cit.hasNext())  {
	    nc = (NCoding) cit.next();
	    NOMFile nomfile = new NOMFile(metadata, nc, observation, null);
	    saveCommonCoding(observation, nc);
	}
    }

    private void saveCommonCoding(NiteObservation observation, NCoding nc) {
	try {
	    if (nc.getType()==NCoding.INTERACTION_CODING) {
		saveCommon(new NOMFile(metadata, nc, observation, null));
	    } else {
		Iterator agit = metadata.getAgents().iterator();
		while (agit.hasNext()) {
		    saveCommon(new NOMFile(metadata, nc, observation, ((NAgent)agit.next())));
		}
	    }
	} catch (NOMException nex) {
	    nex.printStackTrace();
	}
    }

    /** This is a version that includes resource so we get
     * the correct root element  */
    private void saveDataToFile(String filename, String colour, NResource resource) {
	//	filename = filename + "2"; // for debugging!
	//Debug.print("Save data to file: " + filename);
	loadingfromfile=true;
	try {

	    // find the root to write
	    NOMWriteElement base=null;
	    for (Iterator rit=rootlist.iterator(); rit.hasNext(); ) {
		NOMWriteElement readele = (NOMWriteElement) rit.next();
		if (readele.getColour().equals(colour) && (resource==null || resource==readele.getResource())) {
		    base=readele; 
		    break;
		}
	    }

	    if (base==null) { 
		Debug.print("DEBUG: Not writing file " + filename + ". Can't find root element. Colour is "+colour+"; resource is "+resource, Debug.DEBUG);
		loadingfromfile=false;
		return;
	    }

	    // don't write empty files.
	    if (base.getChildren()==null) {
		Debug.print("DEBUG: Not writing file " + filename + " as it has no content ", Debug.DEBUG);
		loadingfromfile=false;
		return;
	    }

	    File f=new File(filename);
	    String dirname=f.getParent();
	    if (dirname!=null) {
		File dir=new File(dirname);
		if (!dir.exists()) {
		    if (!dir.mkdirs()) {
			Debug.print("ERROR: Failed to make directory for file " + filename, Debug.ERROR);
		    }
		}
	    }
	    
	    Debug.print("Saving: " + filename, Debug.IMPORTANT);

	    OutputStream fout= new FileOutputStream(filename);
	    OutputStream bout= new BufferedOutputStream(fout);
	    OutputStreamWriter out = new OutputStreamWriter(bout, "8859_1");

	    out.write("<?xml version=\"1.0\" ");
	    out.write("encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n");  
	    if (serialize_maximal_ranges==true) {
		writeStructureWithRanges(out, 0, base, colour, TEXT_NODE);
	    } else {
		writeStructureFromElement(out, 0, base, colour, TEXT_NODE);
	    }
	    out.flush();
	    out.close();
	    bout.close();
	    fout.close();
	} catch (Exception e) {
	    loadingfromfile=true;
	    PopupMessage pe = new PopupMessage("SAVE FILE FAILED: '" + filename + "'. Check permissions and try saving again before exit!");
	    pe.popup();
	    saveerror=true;
	    e.printStackTrace();
	}
	loadingfromfile=false;
    }


    /** Write the structure to a file with no ranges */
    public void writeStructureFromElement(OutputStreamWriter out, int level,
					   NOMWriteElement element, String colour,
					   int previous) {
	try {
	    if (element.isStreamElement()) {
		if (element.getAttribute(NITE_NAMESPACE_NAME)==null ||
		    element.getAttribute(NITE_NAMESPACE_NAME).equals("")) {
		    element.setStringAttribute(NITE_NAMESPACE_NAME, NITE_NAMESPACE);
		}
		if (metadata.getLinkType()==NMetaData.XPOINTER_LINKS) {
		    if (element.getAttribute(XLINK_NAMESPACE_NAME)==null ||
			element.getAttribute(XLINK_NAMESPACE_NAME).equals("")) {
			element.setStringAttribute(XLINK_NAMESPACE_NAME, XLINK_NAMESPACE);
		    }
		}
		if (serialize_forced_stream_element_names && 
		    !(element instanceof NOMTypeElement)) {
		    // Debug.print("Forcing stream to: " + metadata.getStreamElementName());
		    element.setName(metadata.getStreamElementName());
		} 
		if (schemalocation != null) {
		    if (element.getAttribute(SCHEMA_NAMESPACE_NAME)==null ||
			element.getAttribute(SCHEMA_NAMESPACE_NAME).equals("")) {
			element.setStringAttribute(SCHEMA_NAMESPACE_NAME, SCHEMA_NAMESPACE);
		    }
		    element.setStringAttribute(SCHEMA_LOCATION_NAME, schemalocation);
		}

	    }

	    if (element.isComment()) {
		out.write("\n");
		for (int i=0; i<level; i++) { out.write(INDENT); }
		out.write("<!-- " + XMLutils.escapeText(element.getText()) + " -->");
		return;
	    }

	    if (previous==ELEMENT_NODE) {
		out.write("\n");
		for (int i=0; i<level; i++) { out.write(INDENT); }
	    }
	    out.write(element.startElementString());
	    previous=ELEMENT_NODE;

	    // POINTERS
	    List points = element.getPointers();
	    if (points!=null) { 
		Iterator pit = points.iterator();
		while (pit.hasNext()) {
		    NOMWritePointer point = (NOMWritePointer)pit.next();
		    out.write("\n");
		    for (int i=0; i<level+1; i++) { out.write(INDENT); }
		    String rolestr="";
		    if (point.getRole() != null) {
			rolestr = " " + ROLE_ATTRIBUTE_NAME + "=" +
			    XMLutils.escapeAttributeValue(point.getRole());
		    }
		    String commstr="";
		    if (point.getComment() != null) {
			commstr = " " + metadata.getCommentAttributeName() + "=" +
			    XMLutils.escapeAttributeValue(point.getComment());
		    }
		    out.write("<" + metadata.getPointerElementName() +
			      rolestr + " " + commstr + " " + linkhrefattr + "=\"" +
			      point.getLink() + linkend + '"' + 
			      linkextraattr + "/>");
		}
	    }

	    // EXTERNAL POINTER 
	    if (element.getExternalPointerValue()!=null) {
		NElement metael = element.getMetadataElement();
		if (metael!=null) {
		    out.write("\n");
		    for (int i=0; i<level+1; i++) { out.write(INDENT); }
		    out.write("<" + metadata.getExternalPointerElementName() + " " + 
			      ROLE_ATTRIBUTE_NAME + "=" + XMLutils.escapeAttributeValue(metael.getExternalPointerRole())
			      + " " + linkhrefattr + "=\"" + element.getExternalPointerValue() 
			      + '"' +  "/>");
		}
	    }

	    // CHILDREN
	    //List kids = getChildrenWithInterleavedComments(element);
	    List kids = element.getChildrenWithInterleavedComments();
	    if (kids!=null) { 
		Iterator kit = kids.iterator();
		while (kit.hasNext()) {
		    Object kid = kit.next();
		    if (kid instanceof NOMWriteElement) {
			NOMWriteElement nre1 = (NOMWriteElement) kid;
			if (nre1.getColour().equals(colour)) {
			    writeStructureFromElement(out, level+1, nre1, colour, previous);
			} else {
			    out.write("\n");
			    for (int i=0; i<level+1; i++) { out.write(INDENT); }
			    out.write("<" + metadata.getChildElementName() +
				      " " + linkhrefattr + "=" + '"' +
				      nre1.getLink());
			    out.write(linkend + '"' + linkextraattr + "/>");
			}
			previous=ELEMENT_NODE;
		    }
		}
	    } else {
		if (element.getText() != null) {
		    // Debug.print("Text child: " + element.getText());
		    out.write(XMLutils.escapeText(element.getText()));
		    previous=TEXT_NODE;
		}
	    }
	    if ((kids!=null || points!=null || element.getExternalPointerValue()!=null) && previous==ELEMENT_NODE) {
		out.write("\n");
		for (int i=0; i<level; i++) { out.write(INDENT); }
	    }
	    out.write(element.endElementString());
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /** Just like writeStructureFromElement but tries to output ranges
        where possible */
    private void writeStructureWithRanges(OutputStreamWriter out, int level,
					  NOMWriteElement element, String colour,
					  int previous) {
	try {
	    if (element.isStreamElement()) {
		if (element.getAttribute(NITE_NAMESPACE_NAME)==null ||
		    element.getAttribute(NITE_NAMESPACE_NAME).equals("")) {
		    element.setStringAttribute(NITE_NAMESPACE_NAME, NITE_NAMESPACE);
		}
		if (metadata.getLinkType()==NMetaData.XPOINTER_LINKS) {
		    if (element.getAttribute(XLINK_NAMESPACE_NAME)==null ||
			element.getAttribute(XLINK_NAMESPACE_NAME).equals("")) {
			element.setStringAttribute(XLINK_NAMESPACE_NAME, XLINK_NAMESPACE);
		    }
		}
		if (serialize_forced_stream_element_names) {
		    //Debug.print("Forcing stream to: " + metadata.getStreamElementName());
		    element.setName(metadata.getStreamElementName());
		} 
		if (schemalocation != null) {
		    if (element.getAttribute(SCHEMA_NAMESPACE_NAME)==null ||
			element.getAttribute(SCHEMA_NAMESPACE_NAME).equals("")) {
			element.setStringAttribute(SCHEMA_NAMESPACE_NAME, SCHEMA_NAMESPACE);
		    }
		    element.setStringAttribute(SCHEMA_LOCATION_NAME, schemalocation);
		}
	    }

	    if (element.isComment()) {
		out.write("\n");
		for (int i=0; i<level; i++) { out.write(INDENT); }
		out.write("<!-- " + XMLutils.escapeText(element.getText()) + " -->");
		return;
	    }

	    if (previous==ELEMENT_NODE) {
		out.write("\n");
		for (int i=0; i<level; i++) { out.write(INDENT); }
	    }
	    out.write(element.startElementString());
	    previous=ELEMENT_NODE;

	    // POINTERS
	    List points = element.getPointers();
	    if (points!=null) { 
		Iterator pit = points.iterator();
		while (pit.hasNext()) {
		    NOMWritePointer point = (NOMWritePointer)pit.next();
		    out.write("\n");
		    for (int i=0; i<level+1; i++) { out.write(INDENT); }
		    String rolestr="";
		    if (point.getRole() != null) {
			rolestr = " " + ROLE_ATTRIBUTE_NAME + "=" +
			    XMLutils.escapeAttributeValue(point.getRole());
		    }
		    String commstr="";
		    if (point.getComment() != null) {
			commstr = " " + metadata.getCommentAttributeName() + "=" +
			    XMLutils.escapeAttributeValue(point.getComment());
		    }
		    out.write("<" + metadata.getPointerElementName() +
			      rolestr + " " + commstr + " " + linkhrefattr + "=\"" +
			      point.getLink() + linkend + '"' + 
			      linkextraattr + "/>");
		}
	    }

	    // EXTERNAL POINTER 
	    if (element.getExternalPointerValue()!=null) {
		NElement metael = element.getMetadataElement();
		if (metael!=null) {
		    out.write("\n");
		    for (int i=0; i<level+1; i++) { out.write(INDENT); }
		    out.write("<" + metadata.getExternalPointerElementName() + " " + 
			      ROLE_ATTRIBUTE_NAME + "=" + XMLutils.escapeAttributeValue(metael.getExternalPointerRole())
			      + " " + linkhrefattr + "=\"" + element.getExternalPointerValue() 
			      + '"' +  "/>");
		}
	    }


	    // CHILDREN
	    //List kids = getChildrenWithInterleavedComments(element);
	    List kids = element.getChildrenWithInterleavedComments();
	    if (kids!=null) { 
		//		Debug.print("Element " + element.getID() + " has " + kids.size() + " children.");
		Iterator kit = kids.iterator();
		NOMWriteElement last = null;
		boolean inrange=false;
		int rangecount=0;
		while (kit.hasNext()) {
		    Object kid = kit.next();
		    if (kid instanceof NOMWriteElement) {
			NOMWriteElement nre1 = (NOMWriteElement) kid;
			if (nre1.getColour().equals(colour)) {
			    if (inrange) { // end any open ranges.
				if (rangecount>0) { 
				    out.write(linkrangeseparator + last.getIDLink() + linkrangeend ); 
				} 
				inrange=false;
				out.write(linkend + '"' + linkextraattr + "/>");
				rangecount=0;
			    }
			    writeStructureWithRanges(out, level+1, nre1, colour, previous);
			} else {
			    if (inrange==true) {
				NOMWriteElement pre = (NOMWriteElement)nre1.getPreviousSibling();
				if (pre!=null && pre==last) {
				    rangecount++;
				} else {
				    if (rangecount>0) { 
					out.write(linkrangeseparator + last.getIDLink() + linkrangeend); 
				    } 
				    inrange=false;
				    out.write(linkend + '"' + linkextraattr + "/>");
				    rangecount=0;
				}
			    }

			    if (inrange==false) {
				out.write("\n");
				for (int i=0; i<level+1; i++) { out.write(INDENT); }
				out.write("<" + metadata.getChildElementName() +
					  " " + linkhrefattr + "=" + '"' +
					  nre1.getLink());
				inrange=true;
			    }

			    last = nre1;
			}
			previous=ELEMENT_NODE;
		    }
		}
		if (inrange) { // end any open ranges.
		    if (rangecount>0) { 
			out.write(linkrangeseparator + last.getIDLink() + linkrangeend); 
		    } 
		    inrange=false;
		    out.write(linkend + '"' + linkextraattr + "/>");
		    rangecount=0;
		}
	    } else {
		if (element.getText() != null) {
		    // Debug.print("Text child: " + element.getText());
		    out.write(XMLutils.escapeText(element.getText()));
		    previous=TEXT_NODE;
		}
	    }
	    if ((kids!=null || points!=null || element.getExternalPointerValue()!=null) && previous==ELEMENT_NODE) {
		out.write("\n");
		for (int i=0; i<level; i++) { out.write(INDENT); }
	    }
	    out.write(element.endElementString());
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    

    /*-----------------------------------------------*/
    /* RESOLUTION OF LINKS FOR POINTERS AND CHILDREN */
    /*-----------------------------------------------*/

    /** Traverse each element in the individual subtree and resolve
	pointers and nite children */
    private void resolveTree(NOMWriteElement el, String colour) {
	List kids = el.getChildren();
	List ncs = el.getNiteChildren();
	List points = el.getPointers();
	resolveChildren(el,ncs);
	resolvePointers(el,points);
	//Debug.print("Element " + el.getName() + " has nite children " + ncs + " and pointers " + points + " to be resolved.");
	
	if (kids != null) {
	    Iterator kit = kids.iterator();
	    while (kit.hasNext()) {
		Object kid = kit.next();
		if (kid instanceof NOMWriteElement) {
		    resolveTree((NOMWriteElement)kid, colour);
		}
	    }
	}
    }

    /** Resolve the nite children and make them actual children of this
	node. We need to keep a running total of the "extra" resolved
	children (caused by ranges) so we can put the following nite
	children in the right place. */
    private void resolveChildren(NOMWriteElement el, List kids) {
	if (kids==null) { return; }
	Iterator kit = kids.iterator();
	int globaladdorder=0;
	int resolvecount=0;

	//Debug.print("Resolving children of " + el.getID() + ". There are " + kids.size() + " of them.");
	while (kit.hasNext()) {
	    NOMWriteElement.NiteChild nc=(NOMWriteElement.NiteChild)kit.next();
	    String href = nc.getLink();
	    int order = nc.getOrder();
	    //Debug.print("Attempt to resolve child: " + href);
	    //	    NOMWriteElement nre = resolveXLink(href);
	    List relinks =  resolveXLink(nc);
	    if (relinks!=null && relinks.size()>0 && relinks.get(0)!=null) { 
		resolvecount++;
	    }
	    if (relinks==null) { continue; }
	    Iterator eles = relinks.iterator();
	    int thisaddorder=0;

	    // Note links in the NiteChild - a convenience...
	    //	    if (relinks.size()>1) { nc.setElements(relinks); }
	    while (eles.hasNext()) {
		NOMWriteElement nre = (NOMWriteElement)eles.next();
		if (nre == null) {
		    if (!lazy_loading && !cleaning_up) { // too many error messages otherwise!
			Debug.print("Failed to resolve xlink " + href, Debug.ERROR);
		    }
		} else {
		    //Debug.print("Resolving a child of " + el.getID() + ": " + nre.getID());
		    el.addChildOrder(nre, order+globaladdorder+thisaddorder);
		    // nre.addParent(el);
		    thisaddorder+=1;
		}
	    }
	    if (thisaddorder>1) { 
		globaladdorder+=thisaddorder-1; 
	    };
	}
	if (resolvecount>=kids.size()) {
	    el.removeNiteChildren();
	}
    }

    /** Resolve the nite pointers and make them actual pointers of this
	node */
    protected void resolvePointers(NOMWriteElement el, List points) {
	if (points==null) { 
	    el.setPointersResolved(true);
	    return; 
	}
	Iterator pit = new ArrayList(points).iterator();
	List addpoints = new ArrayList();
	while (pit.hasNext()) {
	    NOMWritePointer point = (NOMWritePointer)pit.next();
	    if (point.getToElement()!=null) { continue; }

	    // note we make a NiteChild for convenience only! That's what resolveXLink takes
	    List pdest = resolveXLink(el.new NiteChild(point.getToElementString(), 0));
	    boolean first = true;
	    //Debug.print("Attempt to resolve pointer from " + point.getFromElement().getID() + " with role: " + point.getRole() + " to element " + point.getToElementString() + ". Results: " + pdest, Debug.DEBUG);
	    if (pdest==null || pdest.size()==0 || pdest.get(0)==null) { 
		continue;
	    }
	    Iterator eles = pdest.iterator();
	    while (eles.hasNext()) {
		NOMWriteElement nre = (NOMWriteElement)eles.next();
		if (nre == null) {
		    if (!lazy_loading && !cleaning_up) { // too many error messages otherwise!
			Debug.print("Failed to resolve xlink " + point.getToElementString(), Debug.ERROR);
		    }
		} else {
		    try {
			if (first) { // fill in the existing pointer
			    point.setToElement(nre);
			    addPointerIndex(point);
			    first=false;
			} else { // add a new pointer - we're in a range
			    NOMPointer np=nommaker.make_pointer(this,point.getRole(),el,nre);
			    addpoints.add(np);
			}
		    } catch (NOMException nex) {
			nex.printStackTrace();
		    }
		}
	    }

	    /* Original code for single href
	       NOMWriteElement nre = resolveSimpleXLink(point.getToElementString());
	       try { 
	       point.setToElement(nre);
	       addPointerIndex(point);
	       } catch (NOMException nex) {

	       }
	    */
	}

	if (addpoints.size()>0) {
	    for (Iterator apit=addpoints.iterator(); apit.hasNext(); ) {
		NOMPointer np = (NOMPointer)apit.next();
		try { 
		    el.addPointer(np);
		    addPointerIndex(np); // needed?
		} catch (NOMException nex) {
		}
	    }
	}

	el.setPointersResolved(true);

    }

    /* A couple of utility functions that resolve links */
    /** Resolve an individual xlink expression which points to exactly
	one NOM element. Note that the format of the link depends on the
	metadata link syntax setting. */
    public NOMElement resolveLink(String xlink) {
	return (NOMElement) resolveSimpleXLink(xlink);
    }

    /** Resolve an individual xlink expression which points to exactly
	one NOM element - the second argument explicitly names the link
	type involved. It can be one of XPOINTER_LINKS or LTXML1_LINKS
	(defined in the NMetaData class) */
    public NOMElement resolveLink(String xlink, int linktype) {
	if (linktype!=NMetaData.XPOINTER_LINKS && 
	    linktype!=NMetaData.LTXML1_LINKS) { 
	    Debug.print("ERROR! You have specified an invalid link type to method resolveLink!", Debug.ERROR);
	    return null;
	}
	int curlink=metadata.getLinkType();
	metadata.setLinkType(linktype);
	NOMElement retvalue = resolveLink(xlink);
	metadata.setLinkType(curlink);
	return retvalue;
    }

    /** Resolve an individual xlink expression which points to exactly
	one NOM element. */
    private NOMWriteElement resolveSimpleXLink(String xlink) {
	String colour=null;
	String id=null;
	if (xlink==null) { return null; }
	int hashindex = xlink.lastIndexOf(linkfileseparator);
	if (hashindex==-1) {
	    Debug.print("Cannot resolve xlink href " + xlink, Debug.ERROR);
	    return null;
	}
	colour=xlink.substring(0, hashindex);
	id=xlink.substring(hashindex+1);
	int dotindex = colour.lastIndexOf(".");
	if (dotindex != -1) {
	    colour = colour.substring(0,dotindex);
	}
	int stindex = id.indexOf(linkbeforeid) + linkbeforeid.length();
	if (stindex>=linkbeforeid.length()) { id=id.substring(stindex); }
	int eindex = id.indexOf(linkafterid);
	if (eindex>0) { id=id.substring(0,eindex); }
	// Get rid of any quotes
	if (id.startsWith("'")) { id=id.substring(1); }
	if (id.endsWith("'")) { id=id.substring(0, id.length()-1); }
	// Debug.print("XLink colour: " + colour + "; element id: " + id);
	try {
	    return findElementWithID(id, colour);
	} catch (NOMException nex) {
	    nex.printStackTrace();
	    return null;
	}
    }

    /* Resolve a link in NITE format. This could resolve to multiple
       remote elements since we allow ranges. */
    private List resolveXLink(NOMWriteElement.NiteChild child) {
	ArrayList elelist = null;
	
	if (child.isRange()) {
	    elelist=(ArrayList)
		resolveRange(resolveSimpleXLink(child.getRangeStart()),
			     resolveSimpleXLink(child.getRangeEnd()));
	    if (elelist==null && !lazy_loading && !cleaning_up) {
		Debug.print("Failed to resolve range " + child.getLink() + ".", Debug.ERROR);
	    }
	} else {
	    elelist=new ArrayList();
	    elelist.add(resolveSimpleXLink(child.getLink()));
	}
	return elelist;
    }

    /** find the actual elements represented by the range, remembering
     * to take account of recursive layers. */
    private List resolveRange(NOMWriteElement from, NOMWriteElement to) {
	ArrayList ellist=new ArrayList();

	if (from==null || to==null) { return null; }
	NOMElement parent = from.findCommonAncestorInFile(to);
	if (parent==null) { return null; }
	try {
	    NLayer nlay = from.getLayer();
	    //if (!nlay.getRecursive()) {
	    return ResolveRange(from, to);
	    /*
	      } else {
	      RecursiveRange rr = new RecursiveRange(parent, from, to);
	      return rr.getElementList();
	      }
	    */
	} catch (NOMException nex) { return null; }
    }

    /** I decided I needed some state for this job, and a little extra
     * thought to avoid loading unnecessary files.. so I put it in an
     * inner class.. jonathan 13.9.05

     * ... and then I realised we don't allow ranges between elements
     * that are at a different level (even within a recursive layer),
     * so we don't need this at all! jonathan 1.11.05 */
    private class RecursiveRange {
	boolean started=false;
	boolean ended=false;
	NOMElement parent=null;
	NOMElement startel=null;
	NOMElement endel=null;
	NLayer layer=null;
	List ellist=new ArrayList();

	public RecursiveRange(NOMElement p, NOMElement s, NOMElement e) {
	    parent=p;
	    startel=s;
	    endel=e;
	}

	private void getElements(NOMElement el) throws NOMException {
	    if (el==startel) { started=true; }
	    if (started) { ellist.add(el); }
	    if (el==endel) { ended=true; return; }
	    // this can potentially load below the recursive layer
	    List kids = el.getChildren(); 
	    if (kids==null) { return; }
	    for (Iterator kit=kids.iterator(); kit.hasNext(); ) {
		NOMElement kid = (NOMElement)kit.next();
		// note that even if this child element is not the
		// right layer, the next one could be!
		if (kid.getLayer()!=layer) { continue; }
		getElements(kid);
		if (ended) { return; }
	    }
	}

	public List getElementList() {
	    if (parent==null || startel==null || endel==null) return null;
	    try {
		layer=startel.getLayer();
		getElements(parent);
	    } catch (NOMException nex) { }
	    return ellist;
	}
    }

    private List ResolveRange(NOMWriteElement from, NOMWriteElement to) {
	ArrayList ellist=new ArrayList();

	if (from==null || to==null) { return null; }
	//System.out.println("RESOLVE " + from.getID());
	NOMWriteElement parent = (NOMWriteElement)from.getParentInFile();
	Iterator chiterator = parent.getChildren().iterator();
	boolean started=false;
	NOMWriteElement nre = null;
	while (chiterator.hasNext()) {
	    nre = (NOMWriteElement)chiterator.next();
	    if (nre==from) { started=true;  }
	    if (started==true) { 
		ellist.add(nre);  
	    }
	    if (nre==to) { break; }
	}
	return ellist;
    }


    /*--------------------------------------------------------------------*/
    /* These methods handle the SAX events that allow us to build the NOM */
    /*--------------------------------------------------------------------*/

    /* this class just makes the SAX handler more separate. */
    class MyContentHandler extends DefaultHandler {

	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	    used internally. Start of a new XML document. */
	public void startDocument() {
	    //Debug.print("Start the document with colour " + current_colour);
	    content_nodes=0;
	    element_stack=new Stack();
	}
	

	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	    used internally. Signals the start of a parsed XML element. */
	public void startElement(String uri, String localName,
				 String qName, Attributes attributes) {
	    
	    NOMWriteElement curel = null;
	    if (!element_stack.empty()) {
		curel=(NOMWriteElement) element_stack.peek();
	    }
	    
	    NOMElement newel=null;
	    try {
		
		if (qName.equals(metadata.getChildElementName())) {
		    if (curel != null) {
			// Add a child element whose link is yet to be resolved.
			curel.addNiteChild(attributes.getValue(linkhrefattr));
		    }
		} else if (qName.equals(metadata.getPointerElementName())) {
		    //	    Debug.print("Start Nite Pointer Element " + qName);
		    NOMPointer np = nommaker.make_pointer(NOMWriteCorpus.this,
							  attributes.getValue(ROLE_ATTRIBUTE_NAME), curel,
							  attributes.getValue(linkhrefattr) );
		    np.setComment(attributes.getValue(metadata.getCommentAttributeName()));
		    if (curel != null) {
			try { curel.addPointer(np); }
			catch (NOMException nex) { }
		    }
		} else if (qName.equals(metadata.getExternalPointerElementName())) {
		    //System.out.println("Handle external pointer");
		    if (curel!=null) {
			curel.addExternalPointer(attributes.getValue(ROLE_ATTRIBUTE_NAME),
						 attributes.getValue(linkhrefattr));
			//System.out.println("External pointer value is: " + curel.getExternalPointerValue());
		    }
		} else if (qName.equals(metadata.getTextElementName())) {
		    newel = nommaker.make_annotation(NOMWriteCorpus.this, qName, attributes, current_colour, current_resource, false);
		    if (curel != null) {
			curel.addChild(newel);
			// newel.setParent(curel);
		    }
		    element_stack.push(newel);
		} else {
		    //String id=attributes.getValue(metadata.getIDAttributeName());
		    //Debug.print("start normal element " + qName + "; " + id);
		    boolean stream=false;
		    if (content_nodes==0) { stream=true; }
		    if (loading==CODING) {
			newel = nommaker.make_annotation(NOMWriteCorpus.this, qName, attributes, current_colour, current_resource, stream);
			if (curel != null) { curel.addChild(newel); }
			checkTimes(newel);
		    } else if (loading==OBJECTSET) {
			newel = nommaker.make_object(NOMWriteCorpus.this, qName, attributes, current_colour, current_resource, stream);
			if (curel != null) { curel.addChild(newel); }
		    } else if (loading==ONTOLOGY) {
			newel = nommaker.make_type_element(NOMWriteCorpus.this, qName, attributes, current_colour, current_resource, stream);
			if (curel != null) { curel.addChild(newel); }
		    } else if (loading==CORPUSRESOURCE) {
			newel = nommaker.make_resource_element(NOMWriteCorpus.this, qName, attributes, current_colour, current_resource, stream);
			if (curel != null) { curel.addChild(newel); }
		    }

		    element_stack.push(newel);
		    if (stream) {
			addRootElement(newel);
		    }	
		    content_nodes+=1;
		}
	    } catch (NOMException nex) {
		// if newel is set and we get here, there's been an
		// error adding it. We've decided it's too serious to
		// try to continue, so we're crashing out.
		/*
		  if (newel!=null) {
		  try {
		  notifyChange((NOMEdit)new DefaultEdit(curel, NOMEdit.DELETE_ELEMENT, newel));
		  } catch (NOMException nex2) { }
		  // just so we have something to pop!
		  element_stack.push(null);
		  newel=null;
		  }
		*/
		//		nex.printStackTrace();
		System.exit(0);
	    }
	}
	
	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	    used internally */
	public void ignorableWhitespace(char[] ch, int start, int length) {
	    //	String content = new String(ch, start, length);
	    //	Debug.print("Ignorable whitespace!");
	}

	
	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	 * used internally.  Extend the behaviour of DefaultHandler to
	 * allow textual content. No mixed content, so we just place text
	 * in a String field in the element.  */
	public void characters(char[] ch, int start, int length) {
	    String content = new String(ch, start, length);
	    //	Debug.print("Real characters: " + content);
	    if (!element_stack.empty()) {
		NOMWriteElement curel=(NOMWriteElement) element_stack.peek();
		// find out if the element is allowed text content
		NElement mel=curel.getMetadataElement();
		if (mel==null || !mel.textContentPermitted()) { 
		    // throw exception?? 
		    String stripped = content.trim();
		    if (stripped.length()>0) {
			Debug.print("Text content not added to non-text element: '" + curel.getName() + "'", Debug.ERROR);
		    }
		    return; 
		}

		// I think trimming is wrong. for example, you get a
		// new chunk for entities and we don't wan't to trim
		// whitespace around them. jonathan 4.11.4
		//content=content.trim();
		if (content==null || content.equals("")) { return; }
		try { curel.appendText(content); }
		catch (NOMException nex) { }
	    }
	}
	
	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	    used internally */
	public void endElement(String uri, String localName, String qName) {
	    if (!qName.equals(metadata.getChildElementName()) && 
		!qName.equals(metadata.getPointerElementName()) &&
		!qName.equals(metadata.getExternalPointerElementName())) {
		if (!element_stack.empty()) {
		    NOMElement nre = (NOMElement)element_stack.pop();
		    if (nre.getText()!=null) {
			try {
			    nre.setText(nre.getText().trim());
			} catch(NOMException nex) { }
		    }
		} else {
		    Debug.print("POP FROM EMPTY STACK!", Debug.ERROR);
		}
	    }
	}
	
    }
    
    /*----------------------------------------------------------------------------*/
    /* These methods implement LexicalHandler. We do this just to store comments. */
    /*----------------------------------------------------------------------------*/
    /** Store comments (part of the LexicalHandler interface) */
    public void comment(char[] ch, int start, int length) {
	String content = new String(ch, start, length);
	if (!element_stack.empty()) {
	    content=content.trim();
	    if (content==null || content.equals("")) { return; }
	    NOMWriteElement curel=(NOMWriteElement) element_stack.peek();
	    try {
		NOMAnnotation commel= nommaker.make_annotation((NOMCorpus)this, content, current_colour, current_resource);
		curel.addComment(commel);
	    } catch (NOMException nex) {

	    }
	    //	    Debug.print("Added comment: " + content);
	}
    }

    /** part of the org.xml.sax.ext.LexicalHandler implementation -
        not for programming use */
    public void endCDATA() {}
    /** part of the org.xml.sax.ext.LexicalHandler implementation -
        not for programming use */
    public void endDTD() {}
    /** part of the org.xml.sax.ext.LexicalHandler implementation -
        not for programming use */
    public void endEntity(String name) {}
    /** part of the org.xml.sax.ext.LexicalHandler implementation -
        not for programming use */
    public void startCDATA() {}
    /** part of the org.xml.sax.ext.LexicalHandler implementation -
        not for programming use */
    public void startDTD(java.lang.String name, java.lang.String publicId,
			 java.lang.String systemId) {}
    /** part of the org.xml.sax.ext.LexicalHandler implementation -
        not for programming use */
    public void startEntity(java.lang.String name) {}


    /*---------------------------*/
    /* NOMControl implementation */
    /*---------------------------*/
    /** Add a NOMView to the list of viewers that get notifed of changes. */
    public void registerViewer(NOMView display) {
	registered_views.add(display);
	if (registered_views.size()>1) {
	    corpus_shared=true;
	}
    }

    /** Remove a NOMView from the list of viewers that get notifed of changes. */
    public void deregisterViewer(NOMView display) {
	registered_views.remove(display);
	if (registered_views.size()<2) {
	    corpus_shared=false;
	}
    }
    
    /** returns true if there are two or more registered viewers of
        this NOM */
    protected boolean isShared() {
	return corpus_shared;
    }

    /** Return true if the corpus can be edited safely - for internal
        use. The corpus is always safe to edit if it is not shared; if
        the corpus is shared, edits are permitted only if a process
        has locked the corpus. */
    public boolean isEditSafe () {
	if (batch_load) return true;
	if (!corpus_shared) return true;
	if (locker!=null) return true;
	return false;
    }

    /** Notify all NOMViews that an (unspecified) edit has ocurred */
    public void notifyChange() {
	for (Iterator vit=registered_views.iterator(); vit.hasNext(); ) {
	    NOMView view=(NOMView)vit.next();
	    if (view!=null) { view.handleChange(null); }
	}
    }

    /** Notify all NOMViews that a specific NOMEdit has ocurred */
    public void notifyChange(NOMEdit edit) throws NOMException {
	if (locker!=null) {
	    for (Iterator vit=registered_views.iterator(); vit.hasNext(); ) {
		NOMView view=(NOMView)vit.next();
		if (view==null) continue; 
		if (view!=locker) {
		    view.handleChange(edit); 
		}
	    } 
	}
	updateIndices(edit);
    }

    /** Notify all NOMViews except the one passed as an argument that
        a NOMEdit has ocurred */
    public void notifyChange(NOMEdit edit, NOMView view) throws NOMException {
	for (Iterator vit=registered_views.iterator(); vit.hasNext(); ) {
	    NOMView view2=(NOMView)vit.next();
	    if ((view2!=null) && (view2!=view)) { view.handleChange(edit); }
	}
	updateIndices(edit);
    }

    /** lock the corpus for edits - this is only necessary if more
        than one application will be writing to the same NOM
        simultaneously. */
    public boolean lock (NOMView view) {
	if (locker==null) {
	    locker=view;
	    return true;
	} else {
	    return false;
	}
    }

    /** unlock the corpus */
    public boolean unlock(NOMView view) {
	if (locker==view) {
	    locker=null;
	    return true;
	} else return false;
    }

    /*----------------------------*/
    /* internal handling of edits */
    /*----------------------------*/
    
    /** returns true if the corpus has unsaved edits */
    public boolean edited() {
	if (colours_changed==null || colours_changed.size()==0) { return false; }
	return true;
    }

    /** Keep our own house in order with respect to edits on the NOM */
    private void updateIndices(NOMEdit edit) throws NOMException {
	NOMWriteElement ele = (NOMWriteElement) edit.getElement();
	if (edit.getType()==NOMEdit.DELETE_ELEMENT) {
	    NOMElement child = (NOMElement)edit.getObject();
	    deletePointersTo(child);
	    String key= child.getColour() + "#" + child.getID();
	    if (metadata.getCorpusType()==NMetaData.SIMPLE_CORPUS) { key=ele.getID(); }
	    element_hash.remove(key);
	    if (element_list.contains(ele)) {
		element_list.remove(element_list.indexOf(ele));
	    }
	    List ellist = (List)element_name_hash.get(child.getName());
	    if (ellist!=null) { ellist.remove(child); }
	    element_name_hash.put(child.getName(), ellist);
	} else if (edit.getType()==NOMEdit.ADD_ELEMENT) {
	    //System.out.println("Adding element!\n");
	    String elname = ele.getName();
	    if (!elname.equals(metadata.getTextElementName()) &&
		!elname.equals(metadata.getChildElementName()) &&
		!elname.equals(metadata.getPointerElementName()) &&
		!elname.equals(metadata.getExternalPointerElementName())) {
		String id=ele.getID();
		if (id==null || id.equals("")) {
		    id=generateID(ele.getColour(), ele.getResource());
		    ele.setID(id);
		    //   Debug.print("Element has no ID attribute (" + metadata.getIDAttributeName() + ")");
		    Debug.print("Element has no ID attribute (generated " + id + ").");
		}
		//		else {
		// element_hash indexes elements by ID
		String key= ele.getColour() + "#" + ele.getID();
		if (metadata.getCorpusType()==NMetaData.SIMPLE_CORPUS) { key=ele.getID(); }
		if (element_hash.get(key)!=null) {
		    // treat stream elements as a special case as they
		    // shouldn't be referred to anywhere.
		    if (ele.isStreamElement()) {
			if (id==null) {
			    ele.setID(generateID(ele.getColour(), ele.getResource()));
			}
		    } else {
			if (element_hash.get(key)==ele) { return; }		
			NOMException nnn = new NOMException("ID CLASH EXCEPTION! File: " + ele.getColour() + ".xml; ID: " + ele.getID());
			nnn.printStackTrace();
			throw nnn;
		    }
		}
		element_hash.put(key, ele);
		if (!ele.isStreamElement() || ele instanceof NOMWriteTypeElement) {
		    //Debug.print("Added element "+ele.getID()+" which is "+ele,Debug.DEBUG);
		    if (loadingfromfile) { element_list.add(ele); }
		    else {
			if (!ele.hasNextElement()) {
			    element_list.add(ele);
			} else {
			    NOMElement afel = ele.getNextElement();
			    element_list.add(element_list.indexOf(afel)-1, ele);
			}
		    }
		}
	    }
	    checkTimes(ele);
	    // element_name_hash indexes elements by element name
	    if (element_name_hash.get(elname) != null) {
		ArrayList al = (ArrayList) element_name_hash.get(elname);
		al.add(ele);
		// element_name_hash.remove(elname); // necessary??
		element_name_hash.put(elname, al);
	    } else {
		ArrayList al = new ArrayList();
		al.add(ele);
		element_name_hash.put(elname, al);
	    }
	    //	    }
	} else if (edit.getType()==NOMEdit.ADD_CHILD) {
	    // Nothing to do here! No elements are actually added.	    
	} else if (edit.getType()==NOMEdit.REMOVE_CHILD) {
	    // Nothing to do here! No elements are actually deleted.
	} else if (edit.getType()==NOMEdit.DELETE_POINTER) {
	    NOMPointer point = (NOMPointer)edit.getObject();
	    if (point!=null) { removePointerIndex(point); }
	} else if (edit.getType()==NOMEdit.ADD_POINTER) {
	    NOMPointer point = (NOMPointer)edit.getObject();
	    if (point!=null) { addPointerIndex(point); }
	} else if (edit.getType()==NOMEdit.SET_ATTRIBUTE || 
		   edit.getType()==NOMEdit.SET_START_TIME ||
		   edit.getType()==NOMEdit.SET_END_TIME ||
		   edit.getType()==NOMEdit.SET_TEXT) {
	    // Nothing more to do here - no indices are affected.
	    checkTimes(ele);
	}

	if (!loadingfromfile && ele!=null) {
	    //System.out.println ("add changed colour: " + ele.getColour());
	    addChangedColour(ele.getColour());
	    if (ele.getResource()!=null) {
		resources_changed.add(ele.getResource());
		//System.out.println("Added changed resource: " + ele.getResource().getID());
		//NOMException nex = new NOMException("TEST");
		//nex.printStackTrace();
	    }
	} 
    }

    /** Add to the list of "colours" which will need to be saved. */
    private void addChangedColour(String colour) {
	colours_changed.add(colour);
    }

    /** Remove an entry from the list of "colours" to be saved. */
    private void removeChangedColour(String colour) {
	colours_changed.remove(colour);
    }

    /** notes that this element's "colour" has been changed and also
        adds all of this node's parents' colours to the list - used
        when serializing */
    private void addChangedColours(NOMElement element) {
	List parents = element.getParents();
	if (parents==null) { return; }
	Iterator pit = parents.iterator();
	while (pit.hasNext()) {
	    NOMElement nel = (NOMElement)pit.next();
	    //System.out.println("Adding colour: " + nel.getColour());
	    //System.err.println("ADD CHANGED COLOUR: " + nel.getColour());
	    colours_changed.add(nel.getColour());
	}
    }

    /** Return the reverse index of pointers to the given element */
    public List getPointersTo(NOMElement to_element) {
	if (to_element==null) { return null; }
	if (lazy_loading) { loadRequestedPointersTo(to_element); }
	HashSet hs = (HashSet) pointer_hash.get(to_element);
	if (hs==null) { return null; }
	return (List) new ArrayList(hs);
    }

    /** Add a reverse index of the element pointed at. When it's
        deleted, we'll need to delete pointers to it. */
    private void addPointerIndex(NOMPointer point) {
	NOMElement to = point.getToElement();
	if (to!=null) {
	    HashSet hs = (HashSet) pointer_hash.get(to);
	    if (hs==null) { hs = new HashSet(); }
	    hs.add( point);
	    pointer_hash.put(to,  hs);
	}
    }

    /** Remove a pointer from our global index (the index is required
        so we can delete appropriate pointers to elements that are
        themselves deleted. */
    public void removePointerIndex(NOMPointer point) {
	if (removing_pointers) { return; }
	NOMElement to = point.getToElement();
	//	Debug.print("REMOVING!@ ");
	if (to==null) { return; }
	HashSet hs = (HashSet) pointer_hash.get(to);
	if (hs!=null) { 
	    hs.remove( point);
	    pointer_hash.put(to,  hs);
	}
    }

    /** An element has been deleted: we need to delete any pointers to
        the element. */
    private void deletePointersTo(NOMElement to) {
	String toname = to.getLink();
	//Debug.print("Deleting pointers to " + toname);
	HashSet hs = (HashSet) pointer_hash.get(to);
	if (hs==null) { return; }
	Iterator hit=hs.iterator();
	removing_pointers=true;
	while (hit.hasNext()) {
	    NOMPointer point= (NOMPointer)hit.next();
	    NOMElement fromel = point.getFromElement();
	    if (fromel!=null) {
		try { fromel.removePointer(point); }
		catch (NOMException nex) { }
		//Debug.print("REMOVING POINTER to " + toname);
		continue;
	    }
	}
	removing_pointers=false;
	pointer_hash.remove(to);
    }


    private void updateRanges(NOMElement removed) {
	
    }

    
    /*-------------------*/
    /* UTILITY FUNCTIONS */
    /*-------------------*/

    /** generates an Identifier that's globally unique - used when
        creating elements - we use 'colour' in an NXT-specific way:
        it's precisely the filename the element will be serailized
        into, without its the '.xml' extension: thus it comprises
        observation name; '.'; the agent name followed by '.' (if an
        agent coding); the coding name. */
    public String generateID(String colour) {
	return generateID(colour, null);
    }

    /** generates an Identifier that's globally unique - used when
        creating elements - we use 'colour' in an NXT-specific way:
        it's precisely the filename the element will be serailized
        into, without its the '.xml' extension: thus it comprises
        observation name; '.'; the agent name followed by '.' (if an
        agent coding); the coding name. */
    public String generateID(String colour, NResource resource) {
	if (lazy_loading) {
	    loadRequestedColour(colour);
	}
	Integer id=(Integer)idhash.get(colour);
	int ident=1;
	if (id!=null) { ident=id.intValue(); }
	idhash.put(colour, (new Integer(ident+1)));
	NOMFile nf = new NOMFile(metadata, colour);
	// add annotator to id if we're annotator specific - then we
	// can load multi-coder without ID clashes.
	//Debug.print("Generated ident: " + ident + " for colour " + colour); 
	//NOMException nex = new  NOMException("AA");
	//nex.printStackTrace();
	if (nf.getNFile() instanceof NCoding) {
	    SpecialLoad sl = findSpecialLoad(nf.getObservation(), (NCoding)nf.getNFile());
	    if (sl != null) { 
		if (sl.getAnnotator()!=null) {
		    return colour + "." + sl.getAnnotator() + "." + ident;
		}
	    }
	}
	if (resource!=null) {
	    return colour + "." + resource.getID() + "." + ident;	    
	}
	return colour + "." + ident;
    }


    /** registers an Identifier as having been used and if necessary,
        notes an Integer in the ID hash for quick generation of IDs.
        Should only be used internally to the
        net.sourceforge.net.sourceforge.net.sourceforge.nite.nom.nomwrite.impl
        package. 'colour' is the filename the element will be
        serialized into without the .xml extension. */
    public void registerID(String id, String colour) {
	if (id==null || colour==null) { return; }
	if (id.indexOf(colour)>=0) {
	    //Debug.print("Registering ID " + id + ". Colour: " + colour);
	    String segment=id.substring(id.lastIndexOf(colour)+colour.length()+1);
	    Integer num=null;
	    if (segment==null) { return; }
	    try { num=Integer.decode(segment); }
	    catch (NumberFormatException exc) {
		if (segment.indexOf(".")>=0) { // we have an annotator name??
		    String seg2 = segment.substring(segment.indexOf(".")+1, segment.length());
		    //System.out.println("DECODE: " + seg2);
		    try { num=Integer.decode(seg2); }
		    catch (NumberFormatException exc2) { }
		} else { // try without a dot...
		    String seg2=id.substring(id.lastIndexOf(colour)+colour.length());
		    try { num=Integer.decode(seg2); }
		    catch (NumberFormatException exc2) { }
		}
	    }
	    if (num == null) {
		//		Debug.print("Register ID: failed to get number from segment " + segment + "; ID:" + id + "(colour: " + colour + ").");
	    } else {
		//Debug.print("Got a number from " + id + ": " + num.toString());
		if (idhash.get(colour)==null) {
		    //   Debug.print("No known index for colour " + colour + ". Setting to " + num.toString());
		    idhash.put(colour, (new Integer(num.intValue()+1)));
		} else {
		    Integer existingnum=(Integer)idhash.get(colour);
		    if (num.intValue() >= existingnum.intValue()) {
			// Debug.print("Replacing " + existingnum.toString() + " with " + num.toString() + " for colour " + colour);
			idhash.put(colour, (new Integer(num.intValue()+1)));
		    }
		}
	    }
	} else {
	    //	    Debug.print(colour + " is not a substring of " + id);
	}
    }


    /** A method to show the structure of the multi-rooted XML in the NOM
     */
    public void printStructure() {
	Iterator rit = rootlist.iterator();
	while (rit.hasNext()) {
	    NOMWriteElement readele = (NOMWriteElement) rit.next();
	    //	    Debug.print(readele.getName() + " element " + readele.getID() + " is a root.");
	    printStructureFromRoot(0, readele, readele.getColour());
	}	
    }

    /** print from one element. Call like:
          nom.printStructureFromRoot(0, element, element.getColour());
    */
    public void printStructureFromRoot(int level, NOMWriteElement ele, String elcolour) {
	List kids = ele.getChildren();
	List pars = ele.getParents();
	List pointers = ele.getPointers();

	if (ele.isComment()) {
	    for (int i=0; i<level; i++) { System.out.print(INDENT); }
	    System.out.println("Comment: " + ele.getText() );
	    return;
	}
	//	for (int i=0; i<level; i++) { System.out.print("  "); }
	int ksize=0; int parsize=0; int pointsize=0;
	if (kids != null) { ksize = kids.size(); }
	if (pars != null) { parsize = pars.size(); }
	if (pointers != null) { pointsize = pointers.size(); }
	System.out.println(ele.getName() + " (" + ele.getID() + ", " + ele.getColour() + ") has " + ksize + " children, " + parsize + " parents and " + pointsize + " pointers." + " Start time: " + ele.getStartTime() + ". End time: " + ele.getEndTime());

	if (pointsize>0) {
	    for (int i=0; i<level; i++) { System.out.print("    "); }
	    System.out.print("POINTERS: ");
	    Iterator pit = pointers.iterator();
	    while (pit.hasNext()) {
		NOMWritePointer poin = (NOMWritePointer) pit.next();
		NOMWriteElement nre1 = (NOMWriteElement)poin.getToElement();
		if (nre1 == null) {
		    System.out.print("Unresolved Pointer: " + poin.getToElementString() + "!!");
		} else {
		    System.out.print(nre1.getName() + " " + nre1.getID() + "; ");
		}
	    }
	    System.out.println("");
	}

	if (ksize>0) {
	    Iterator kit = kids.iterator();
	    while (kit.hasNext()) {
		Object kid = kit.next();
		if (kid instanceof NOMWriteElement) {
		    NOMWriteElement nre1 = (NOMWriteElement) kid;
		    for (int i=0; i<level; i++) { System.out.print("    "); }
		    if (!nre1.getColour().equals(elcolour)) {
			System.out.print("NITE CHILD: ");
			System.out.println(nre1.getName() + " " + nre1.getID() + " (" + nre1.getColour() + "); ");
		    } else {
			System.out.print("FILE ELEMENT: ");
			printStructureFromRoot(level+1, nre1, elcolour);
		    }
		} 
	    }
	} else {
	    if (ele.getText() != null) {
		System.out.print(ele.getText());
	    }
	}
	
    }


    /*----------------------------------*/
    /* SEARCHABLE CORPUS IMPLEMENTATION */
    /*----------------------------------*/

    /////////////////////////////////////////////////////////////////////////////
    // iterator


    /**
     * Returns an Iterator which visits each element in the NOM exactly
     * once: this version loads any data that has not already been loaded.
     * @return an Iterator which visits each element in the NOM exactly once
     */
    public java.util.Iterator getElements() {
	if (lazy_loading) {
	    completeLoad();
	}
	return new CorpusIterator();	
    }

    /**
     * Returns an Iterator which visits each element that has already
     * been loaded into the NOM exactly once: this version does not
     * check whether there is data still to be loaded.
     * @return an Iterator which visits each element in the NOM exactly once
     */
    public java.util.Iterator getElementsLoaded() {
	return new CorpusIterator();	
    }

    /**
     * Returns the value of the text content as {@linkplain Comparable}.
     * @param element the element containing the text, that will be returned
     * @return the value of the text content as {@linkplain Comparable}
     */
    public java.lang.Comparable getText(Object element)
    {
	try {
	    return ((NOMElement)element).getText();
	} catch( Exception e ){ return null; }
    }

    /////////////////////////////////////////////////////////////////////////////
    // attribute

    /**
     * Returns the value of an attribute of an element as
     * {@linkplain Comparable}.
     * @param element the element with the requested attribute
     * @param name the name of the attribute
     * @return the value of an attribute of an element as {@linkplain Comparable}
     */
    public java.lang.Comparable getAttributeComparableValue(Object element, String name) {
	try {
	    return ((NOMElement)element).getAttribute(name).getComparableValue();
	} catch( Exception e ){ return null; }
    }

    /**
     * Returns the value of an attribute of an element as a
     * {@linkplain Double} if possible (null if not).
     * @param element the element with the requested attribute
     * @param name the name of the attribute
     * @return the value of an attribute of an element as {@linkplain Double}
     */
    public Double getAttributeDoubleValue(Object element, String name) {
	try {
	    return ((NOMElement)element).getAttribute(name).getDoubleValue();
	} catch( Exception e ){ return null; }
    }

    /**
     * Returns the value of an attribute of an element as a
     * {@linkplain String}.
     * @param element the element with the requested attribute
     * @param name the name of the attribute
     * @return the value of an attribute of an element as {@linkplain String}
     */
    public String getAttributeStringValue(Object element, String name) {
	try {
	    return ((NOMElement)element).getAttribute(name).getStringValue();
	} catch( Exception e ){ return null; }
    }


    /////////////////////////////////////////////////////////////////////////////
    // equality

    /**
     * Returns true if element A is the same element as element B.
     * @param a element A
     * @param b element B
     * @return true if element A is the same element as element B
     */
    public boolean testIsEqual   (Object a, Object b) {
	return a.equals(b);
    }

    /**
     * Returns true if element A is not the same element as element B.
     * @param a element A
     * @param b element B
     * @return true if element A is not the same element as element B
     */
    public boolean testIsInequal (Object a, Object b) {
	return !a.equals(b);
    }


    /////////////////////////////////////////////////////////////////////////////
    // strucural relations

    /* This version is friendly wrt lazy loading but still uses the
     * original algorithm */
    /**
     * Returns true if element A dominates element B.
     * Notice that an element also dominates itself.
     * @param a element A
     * @param b element B
     * @return true if element A dominates element B
     */
    public boolean testDominates (Object a, Object b) {
	boolean ll = lazy_loading;
	try {
	    lazy_loading=false;
	    NOMElement na = (NOMElement) a;
	    NOMElement nb = (NOMElement) b;
	    //System.out.println("Test dominate: " + na.getName() + "; " + nb.getName());
	    //loadRequestedAncestorLayers(na, nb.getName());
	    loadRequestedAncestorLayers(nb, na.getName());
	    // save processing if the path doesn't exist. 
	    if (na.getLayer()!=nb.getLayer() && metadata.findPathBetween(na.getLayer(), nb.getLayer())==null) { 
		Debug.print("No path between element " + na.getName() + " and "+nb.getName());
		return false; 
	    }
	    //Debug.print("Path between element " + na.getName() + " and "+nb.getName()+" or layers are the same");
	    
	    boolean res = originalTestDominates(a,b);
	    lazy_loading=ll;
	    return res;
	} catch (Exception cce) { // ClassCast or NOM Exception;
	    lazy_loading=ll;
	    return originalTestDominates(a,b);
	}
    }

    /**
     * Returns true if element A dominates element B.
     * Notice that an element also dominates itself.
     * @param a element A
     * @param b element B
     * @return true if element A dominates element B
     */
    public boolean originalTestDominates (Object a, Object b) {
	try {
	    List parents = new Vector();
	    parents.add( b );
	    boolean ret = false;
	    while ( !parents.isEmpty() ) {
		if ( parents.contains(a) ) {
		    ret = true;
		    break;
		} else {
		    NOMElement x = (NOMElement)parents.get(0);
		    parents.remove(0);
		    if (x.getParents()!=null) {
			parents.addAll( x.getParents() );
		    }
		}
	    }
	    return ret;
	} 
	catch (ClassCastException e) { return false; }
	catch (NullPointerException e) { return false; }
    }

    /**
     * Returns true if element A dominates element B with the specified
     * distance.
     * Notice that with distance=0 this metode is equale to
     * {@linkplain #testIsEqual(java.lang.Object, java.lang.Object)}. Also
     * distance < 0 is possible, means element B dominates element B.
     * @param a element A
     * @param b element B
     * @param distance distance between element A and element B
     * @return true if element A dominates element B with the specified
     * distance
     */
    public boolean testDominates (Object a, Object b, int distance) {
	try {
	    if ( distance<0 ) {
		return testDominates(b, a, -distance);
	    } else {
		List parents = new Vector();
		parents.add( b );
		for (int i=0; i<distance; i++) {
		    List help = new Vector();
		    while ( !parents.isEmpty() ) {
			NOMElement x = (NOMElement)parents.get(0);
			parents.remove(0);
			if (x.getParents()!=null) {
			    help.addAll( x.getParents() );
			}
		    }
		    parents = help;
		}
		return parents.contains(a);
	    }
	} 
	catch (ClassCastException e) { return false; }
	catch (NullPointerException e) { return false; }
    }

    /**
     * Returns true if element A precedes element B.
     * @param a element A
     * @param b element B
     * @return true if element A precedes element B
     */
    public boolean testPrecedes (Object a, Object b) {
	try {
	    List aParents = new Vector(),
		newAParents = new Vector(),
		bParents = new Vector(),
		newBParents = new Vector();
	    if (a==b) { return false; }
	    newAParents.add( a );
	    newBParents.add( b );
	    boolean ret = false;
	    NOMElement commonParent = null;
	    while ( !newAParents.isEmpty() || !newBParents.isEmpty() ) {
		// left side: element a parents
		if ( !newAParents.isEmpty() ) {
		    NOMElement x = (NOMElement)newAParents.remove(0);
		    if ( bParents.contains(x) ) {
			if (x==b) { return false; }
			commonParent = x;
			break;
		    } else {
			aParents.add(x);
			if (x.getParents()!=null) {
			    newAParents.addAll( x.getParents() );
			}
		    }
		}
		// right side: element b parents
		if ( !newBParents.isEmpty() ) {
		    NOMElement x = (NOMElement)newBParents.remove(0);
		    if ( aParents.contains(x) ) {
			if (x==a) { return false; }
			commonParent = x;
			break;
		    } else {
			bParents.add(x);
			if (x.getParents()!=null) {
			    newBParents.addAll( x.getParents() );
			}
		    }
		}
	    }
	    // branch a before branch b?
	    if (commonParent == null) {
		return false;
	    } else {
		List children = commonParent.getChildren();
		int  indexOfA = 0,
		    indexOfB = 0;
		for (int i=0; i<children.size(); i++) {
		    Object x = children.get(i);
		    if ( aParents.contains(x) ) { indexOfA = i; }
		    if ( bParents.contains(x) ) { indexOfB = i; }
		}
		return indexOfA < indexOfB ?
		    true :
		    false;
	    }
	} 
	catch (ClassCastException e) { return false; }
	catch (NullPointerException e) { return false; }
    }

    /**
     * Returns true if there is a pointer from the first to the second element.
     * @param from start element of the pointer
     * @param to target element of the pointer
     * @return true if there is a pointer from the first to the second element
     */
    public boolean testHasPointer (Object from, Object to) {
	boolean ret = false;
	List pointers = ((NOMElement)from).getPointers();
	if (pointers==null) return false;
	for (int i=0; i<pointers.size(); i++) {
	    try {
		if ( ((NOMPointer)pointers.get(i)).getToElement().equals(to) ) {
		    ret = true;
		    break;
		}
	    } 
	    catch (NullPointerException e) { continue; }
	    catch (ClassCastException e) { continue; }
	}
	return ret;
    }

    /**
     * Returns true if there is a pointer from the first to the second element
     * with the specified role.
     * @param from start element of the pointer
     * @param to target element of the pointer
     * @param role the role of the pointer
     * @return true if there is a pointer from the first to the second element
     * with the specified role
     */
    public boolean testHasPointer (Object from, Object to, String role) {
	boolean ret = false;
	List pointers = ((NOMElement)from).getPointers();
	for (int i=0; i<pointers.size(); i++) {
	    try {
		if (  ((NOMPointer)pointers.get(i)).getToElement().equals(to)
		      && ((NOMPointer)pointers.get(i)).getRole().equals(role) ) {
		    ret = true;
		    break;
		}
	    }
	    catch (ClassCastException e) { continue; }
	    catch (NullPointerException e) { continue; }
	}
	return ret;
    }


    /**
     * Returns true if there is a pointer from the first element to another
     * element which is dominated by the second element.
     * This methode may be usefull for type hierarchies.
     * @param from start element of the pointer
     * @param to element which dominates target element of the pointer
     * @return true if there is a pointer from the first element to another
     * element which is dominated by the second element
     */
    public boolean testDominatesSubgraph (Object a, Object b) {
	boolean ret = false;
	List pointers = ((NOMElement)a).getPointers();
	for (int i=0; i<pointers.size(); i++) {
	    try {
		if ( testDominates( b, ((NOMPointer)pointers.get(i)).getToElement() ) ) {
		    ret = true;
		    break;
		}
	    }
	    catch (ClassCastException e) { continue; }
	    catch (NullPointerException e) { continue; }
	}
	return ret;
    }

    /**
     * Returns true if there is a pointer with a specified role from the first
     * element to another element which is dominated by the second element.
     * This methode may be usefull for type hierarchies.
     * @param from start element of the pointer
     * @param to element which dominates target element of the pointer
     * @param role the role of the pointer
     * @return true if there is a pointer with a specified role from the first
     * element to another element which is dominated by the second element
     */
    public boolean testDominatesSubgraph (Object a, Object b, String role) {
	boolean ret = false;
	List pointers = ((NOMElement)a).getPointers();
	for (int i=0; i<pointers.size(); i++) {
	    try {
		if (  ((NOMPointer)pointers.get(i)).getRole().equals(role)
		      && testDominates( b, ((NOMPointer)pointers.get(i)).getToElement() ) ) {
		    ret = true;
		    break;
		}
	    } 
	    catch (ClassCastException e) { continue; }
	    catch (NullPointerException e) { continue; }	
	}
	return ret;
    }


    /////////////////////////////////////////////////////////////////////////////
    // temporal relations

    public boolean testSameExtend (Object a, Object b) {
	try {
	    NOMElement x = (NOMElement)a;
	    NOMElement y = (NOMElement)b;
	    if (x.getStartTime()==y.getStartTime() && x.getEndTime()==y.getEndTime()) {
		return true;
	    }
	    return false;
	} catch (Exception ex) { 
	    return false;
	}
    }

    /**
     * Returns true if the element A is timed.
     * Timed means either the element has explicit start and end time or all its
     * children are timed.
     * @param a element A
     * @return true if the element A is timed
     */
    public boolean testTimed (Object a) {
	try {
	    NOMElement x = (NOMElement)a;
	    // note that we could go to the metadata to find out if
	    // the element could be timed or not, but it's faster to
	    // just test the values. This also catches those elements
	    // that can take a time but haven't been given one yet.
	    // e.g. if (!(x instanceof NOMWriteAnnotation)) { return false; }
	    if ( ( x.getStartTime() != NOMElement.UNTIMED )
		 && ( x.getEndTime() != NOMElement.UNTIMED ) ) {
		return true;
	    } else {
		return false;
	    }
	} 
	catch (ClassCastException e) { return false; }
	catch (NullPointerException e) { return false; }
    }

    /**
     * Returns true if element A overlaps left element B.
     * Means a_start <= b_start and a_end > b_start and a_end <= b_end.
     * @param a element A
     * @param b element B
     * @return true if element A overlaps left element B
     */
    public boolean testOverlapsLeft (Object a, Object b) {
	try {
	    if (!temporalRelationValid((NOMElement)a, (NOMElement)b)) { return false; }
	    double aStart = ((NOMElement)a).getStartTime();
	    double aEnd   = ((NOMElement)a).getEndTime();
	    double bStart = ((NOMElement)b).getStartTime();
	    double bEnd   = ((NOMElement)b).getEndTime();
	    if (  (aStart != NOMElement.UNTIMED)
		  && (aEnd   != NOMElement.UNTIMED)
		  && (bStart != NOMElement.UNTIMED)
		  && (bEnd   != NOMElement.UNTIMED)
		  && (aStart <= bStart)
		  && (aEnd   > bStart)
		  && (aEnd   <= bEnd) ) {
		return true;
	    } else {
		return false;
	    }
	} 
	catch (ClassCastException e) { return false; }
	catch (NullPointerException e) { return false; }
    }

    /**
     * Returns true if element A is left aligned with element B.
     * Element A and B are starting at the same time, so a_start is equale to
     * b_start.
     * @param a element A
     * @param b element B
     * @return true if element A is left aligned with element B
     */
    public boolean testLeftAlignedWith (Object a, Object b) {
	try {
	    if (!temporalRelationValid((NOMElement)a, (NOMElement)b)) { return false; }
	    double aStart = ((NOMElement)a).getStartTime();
	    double bStart = ((NOMElement)b).getStartTime();
	    if (  (aStart != NOMElement.UNTIMED)
		  && (bStart != NOMElement.UNTIMED)
		  && (aStart == bStart) ) {
		return true;
	    } else {
		return false;
	    }
	} 
	catch (ClassCastException e) { return false; }
	catch (NullPointerException e) { return false; }
    }

    /**
     * Returns true if element A is right aligned with element B.
     * Element A and B are stoping at the same time, so a_end is equale to b_end.
     * @param a element A
     * @param b element B
     * @return true if element A is right aligned with element B
     */
    public boolean testRightAlignedWith (Object a, Object b) {
	try {
	    if (!temporalRelationValid((NOMElement)a, (NOMElement)b)) { return false; }
	    double aEnd = ((NOMElement)a).getEndTime();
	    double bEnd = ((NOMElement)b).getEndTime();
	    if (  (aEnd != NOMElement.UNTIMED)
		  && (bEnd != NOMElement.UNTIMED)
		  && (aEnd == bEnd) ) {
		return true;
	    } else {
		return false;
	    }
	} 
	catch (ClassCastException e) { return false; }
	catch (NullPointerException e) { return false; }
    }

    /**
     * Returns true if element A temporally includes element B.
     * Means a_start <= b_start and a_end >= b_end.
     * @param a element A
     * @param b element B
     * @return true if element A temporally includes element B.
     */
    public boolean testIncludes (Object a, Object b) {
	try {
	    if (!temporalRelationValid((NOMElement)a, (NOMElement)b)) { return false; }
	    double aStart = ((NOMElement)a).getStartTime();
	    double aEnd   = ((NOMElement)a).getEndTime();
	    double bStart = ((NOMElement)b).getStartTime();
	    double bEnd   = ((NOMElement)b).getEndTime();
	    if (  (aStart != NOMElement.UNTIMED)
		  && (aEnd   != NOMElement.UNTIMED)
		  && (bStart != NOMElement.UNTIMED)
		  && (bEnd   != NOMElement.UNTIMED)
		  && (aStart <= bStart)
		  && (aEnd   >= bEnd) ) {
		return true;
	    } else {
		return false;
	    }
	} 
	catch (ClassCastException e) { return false; }
	catch (NullPointerException e) { return false; }
    }

    /**
     * Returns true if element A and element B have the same duration.
     * Means a_start == b_start and a_end == b_end.
     * @param a element A
     * @param b element B
     * @return true if element A and element B have the same duration
     */
    public boolean testSameDuration (Object a, Object b) {
	try {
	    // not sure about this one - should we be cross-observation??
	    if (!temporalRelationValid((NOMElement)a, (NOMElement)b)) { return false; }
	    double aStart = ((NOMElement)a).getStartTime();
	    double aEnd   = ((NOMElement)a).getEndTime();
	    double bStart = ((NOMElement)b).getStartTime();
	    double bEnd   = ((NOMElement)b).getEndTime();
	    if (  (aStart != NOMElement.UNTIMED)
		  && (aEnd   != NOMElement.UNTIMED)
		  && (bStart != NOMElement.UNTIMED)
		  && (bEnd   != NOMElement.UNTIMED)
		  && (aStart == bStart)
		  && (aEnd   == bEnd) ) {
		return true;
	    } else {
		return false;
	    }
	} 
	catch (ClassCastException e) { return false; }
	catch (NullPointerException e) { return false; }
    }

    /**
     * Returns true if element A overlaps element B.
     * Means a_end > b_start and b_end > a_start.
     * @param a element A
     * @param b element B
     * @return true if element A overlaps element B
     */
    public boolean testOverlapsWith (Object a, Object b) {
	try {
	    if (!temporalRelationValid((NOMElement)a, (NOMElement)b)) { return false; }
	    double aStart = ((NOMElement)a).getStartTime();
	    double aEnd   = ((NOMElement)a).getEndTime();
	    double bStart = ((NOMElement)b).getStartTime();
	    double bEnd   = ((NOMElement)b).getEndTime();
	    if (  (aStart != NOMElement.UNTIMED)
		  && (aEnd   != NOMElement.UNTIMED)
		  && (bStart != NOMElement.UNTIMED)
		  && (bEnd   != NOMElement.UNTIMED)
		  && (aEnd > bStart)
		  && (bEnd > aStart) ) {
		return true;
	    } else {
		return false;
	    }
	} 
	catch (ClassCastException e) { return false; }
	catch (NullPointerException e) { return false; }
    }

    /**
     * Returns true if element A ends at the time element B starts.
     * Means a_end == b_start.
     * @param a element A
     * @param b element B
     * @return true if element A ends at the time element B starts
     */
    public boolean testContactWith (Object a, Object b) {
	try {
	    if (!temporalRelationValid((NOMElement)a, (NOMElement)b)) { return false; }
	    double aEnd   = ((NOMElement)a).getEndTime();
	    double bStart = ((NOMElement)b).getStartTime();
	    if (  (aEnd   != NOMElement.UNTIMED)
		  && (bStart != NOMElement.UNTIMED)
		  && (aEnd == bStart) ) {
		return true;
	    } else {
		return false;
	    }
	} 
	catch (ClassCastException e) { return false; }
	catch (NullPointerException e) { return false; }
    }

    /**
     * Returns true if element A temporally precedes element B.
     * Means a_end <= b_start.
     * @param a element A
     * @param b element B
     * @return true if element A temporally precedes element B
     */
    public boolean testPrecedesTemporal (Object a, Object b) {
	try {
	    if (!temporalRelationValid((NOMElement)a, (NOMElement)b)) { return false; }
	    double aEnd   = ((NOMElement)a).getEndTime();
	    double bStart = ((NOMElement)b).getStartTime();
	    if (  (aEnd   != NOMElement.UNTIMED)
		  && (bStart != NOMElement.UNTIMED)
		  && (aEnd <= bStart) ) {
		return true;
	    } else {
		return false;
	    }
	} 
	catch (ClassCastException e) { return false; }
	catch (NullPointerException e) { return false; }
    }

    // Added code from Holger 10/2/3

    /**
     * Returns the name/type of the specified element.
     * If the specified element isn't a {@linkplain
     * net.sourceforge.nite.nom.nomwrite.NOMElement} null will be returned.
     * @param element the element with the name, that will be returned
     * @return the name/type of the specified element
     */
    public String getNameOfElement(Object element)
    {
	try { return ((NOMElement)element).getName();
	} catch( Exception e ){ return null; }
    }

    /**
     * Returns the start time as a {@linkplain java.lang.Comparable} value.
     * If the specified element isn't a {@linkplain
     * net.sourceforge.nite.nom.nomwrite.NOMElement} null will be returned.
     * @param elemen the element with the start time, that will be returned
     * @return the start time as a {@linkplain java.lang.Comparable} value
     */
    public java.lang.Comparable getStartComparableValue(Object element)
    {
	try { return new Double( ((NOMElement)element).getStartTime() );
	} catch( Exception e ){ return null; }
    }

    /**
     * Returns the start time as a {@linkplain java.lang.Comparable} value.
     * If the specified element isn't a {@linkplain
     * net.sourceforge.nite.nom.nomwrite.NOMElement} null will be returned.
     * @param elemen the element with the start time, that will be returned
     * @return the start time as a {@linkplain java.lang.Comparable} value
     */
    public java.lang.Comparable getEndComparableValue(Object element)
    {
	try { return new Double( ((NOMElement)element).getEndTime() );
	} catch( Exception e ){ return null; }
    }


    /**
     * Returns the temporal duration as a {@linkplain java.lang.Comparable} value.
     * If the specified element isn't a {@linkplain
     * net.sourceforge.nite.nom.nomwrite.NOMElement} null will be returned.
     * @param elemen the element with the temporal duration, that will be returned
     * @return the temporal duration as a {@linkplain java.lang.Comparable} value
     */
    public java.lang.Comparable getDurationComparableValue(Object element)
    {
	try { return new Double( ((NOMElement)element).getEndTime()
				 - ((NOMElement)element).getStartTime() );
	} catch( Exception e ){ return null; }
    }

    /**
     * Returns the center of start and end time as a
     * {@linkplain java.lang.Comparable} value.
     * If the specified element isn't a {@linkplain
     * net.sourceforge.nite.nom.nomwrite.NOMElement} null will be returned.
     * @param elemen the element with the center of start and end time, that will
     * be returned
     * @return the center of start and end time as a
     * {@linkplain java.lang.Comparable} value
     */
    public java.lang.Comparable getCenterComparableValue(Object element)
    {
	try { return new Double( ( ((NOMElement)element).getEndTime()
				   + ((NOMElement)element).getStartTime() ) * 0.5 );
	} catch( Exception e ){ return null; }
    }

    /**
     * Returns the ID of an element as a {@linkplain java.lang.Comparable} value.
     * If the specified element isn't a {@linkplain
     * net.sourceforge.nite.nom.nomwrite.NOMElement} null will be returned.
     * @param element the element with the ID, that will be returned
     * @return the ID of an element as a {@linkplain java.lang.Comparable} value
     */
    public java.lang.Comparable getIdComparableValue(Object element)
    {
	try { return ((NOMElement)element).getID();
	} catch( Exception e ){ return null; }
    }

    /////////////////////////////////////////////////////////////////////////////
    // Added code from Holger 5/3/2003

    /**
     * Returns an {@linkplain java.util.Iterator} which visits each element
     * dominated by the specified element in the corpora exactly once.
     * The algorithm is based on the idea that there are no multiple paths
     * between two different elements.
     * @param rootElement the element which should dominate the requested
     * elements
     * @return an {@linkplain java.util.Iterator} which visits each element
     * dominated by the specified element in the corpora exactly once
     */
    public java.util.Iterator getElementsDominatedBy(Object rootElement)
    {
	return new CorpusIteratorDominatedBy(rootElement);
    }

    class CorpusIteratorDominatedBy
	implements Iterator
    {
	private List children = new ArrayList();
	/** constructor */
	public CorpusIteratorDominatedBy(Object rootElement)
	{
	    try {
		//first dominated element is the element itself
		children.add( (NOMElement)rootElement );
	    } catch( ClassCastException e ){} //skip
	}

	public boolean hasNext(){ return !children.isEmpty(); }

	public Object next() throws NoSuchElementException
	{
	    try{
		NOMElement ret = (NOMElement)children.remove(0);
		try {
		    children.addAll( ret.getChildren() );
		} catch( Exception e ){} //skip
		return ret;
	    } catch( Exception e ){ throw new NoSuchElementException(); }
	}

	/** Unimplemented - in an attempt to make this efficient */
	public void remove(){ throw new UnsupportedOperationException(); }
    }

    /**
     * Returns an {@linkplain java.util.Iterator} which visits each element
     * dominating the specified element in the corpora exactly once.
     * @param childElement the element whisch should be dominated by the requested
     * elements
     * @return an {@linkplain java.util.Iterator} which visits each element
     * dominating the specified element in the corpora exactly once
     */
    public java.util.Iterator getElementsDominating(Object childElement)
    {
	return new CorpusIteratorDominating(childElement);
    }

    class CorpusIteratorDominating
	implements Iterator
    {
	private List parents = new ArrayList();
	/** constructor */
	public CorpusIteratorDominating(Object childElement)
	{
	    try {
		parents.add( (NOMElement)childElement );
	    } catch( ClassCastException e ){} //skip
	}

	public boolean hasNext(){ 
	    Debug.print("Iterator parents empty: " + parents.isEmpty());
	    return !parents.isEmpty(); 
	}

	public Object next() throws NoSuchElementException
	{
	    try{
		NOMElement ret = (NOMElement)parents.remove(0);
		try {

		    //          parents.addAll( ret.getParents() );

		    //instead simple to add all parents, it must be tested
		    //if the parent is realy an visible corpus element
		    //Debug.print("in next - getting parents of: " + ret.getID() + "(" + ret.getName() + "). Number of elements in list: " + parents.size(), Debug.ERROR);
		    //NOMException nex = new NOMException("debug");
		    //nex.printStackTrace();
		    List l = ret.getParents();
		    for( Iterator i = l.iterator(); i.hasNext(); ){
			try {
			    NOMElement x = (NOMElement)i.next();
			    //element_list.contains(x) makes it slow :-((
			    if( element_list.contains(x) ){ 
				parents.add(x); 
				//Debug.print("Adding parent " + x.getName() + "; " + parents.size(), Debug.ERROR);
			    } else {
				//Debug.print("NOT adding parent " + x.getName(), Debug.ERROR);
			    }
			} catch( Exception e ){} //skip
		    }

		} catch( Exception e ){} //skip
		return ret;
	    } catch( Exception e ){ throw new NoSuchElementException(); }
	}

	/** Unimplemented - in an attempt to make this efficient */
	public void remove(){ throw new UnsupportedOperationException(); }
    }

    /**
     * Returns an {@linkplain java.util.Iterator} which visits each element which
     * has a pointer from the specified element in the corpora exactly once.
     * @param startElement the element where a pointer pointing to the requested
     * elements starts
     * @return an {@linkplain java.util.Iterator} which visits each element which
     * has a pointer from the specified element in the corpora exactly once
     */
    public java.util.Iterator getElementsPointedBy(Object startElement)
    {
	return new CorpusIteratorPointedBy(startElement);
    }

    class CorpusIteratorPointedBy
	implements Iterator
    {
	private List targets = new ArrayList();
	/** constructor */
	public CorpusIteratorPointedBy(Object startElement)
	{
	    try {
		NOMWriteElement el = (NOMWriteElement)startElement;
		if (!el.pointersResolved()) {
		    resolvePointers(el, el.getPointers());
		}
		List pointers = new ArrayList(el.getPointers());
		for( Iterator i = pointers.iterator(); i.hasNext(); ){
		    Object x = i.next();
		    if( !targets.contains(x) ){
			targets.add( ((NOMPointer)x).getToElement() );
		    }
		}
	    } catch( NullPointerException e ){ //skip
	    } catch( ClassCastException e ){} //skip
	}

	public boolean hasNext(){ return !targets.isEmpty(); }

	public Object next() throws NoSuchElementException
	{
	    try{
		return targets.remove(0);
	    } catch( Exception e ){ throw new NoSuchElementException(); }
	}

	/** Unimplemented - in an attempt to make this efficient */
	public void remove(){ throw new UnsupportedOperationException(); }
    }

    /**
     * Returns an {@linkplain java.util.Iterator} which visits each element with
     * the specified type (= name of element) in the corpora exactly once.
     * @param types list of types
     * @return an {@linkplain java.util.Iterator} which visits each element with
     * the specified type (= name of element) in the corpora exactly once
     */
    public Iterator getElements(List names)
    {
	List iterators = new ArrayList();
	for( int i=0; i<names.size(); i++ ){
	    try {
		iterators.add( getElementsByName( (String)names.get(i) ).iterator() );
	    } catch( NullPointerException e ){} //skip
	}
	return new CompoundIterator(iterators);
    }

    /**
     * CompoundIterator do not test elements if an element is visited by more
     * than one iterator. These means that the given iterators must be
     * complementary.
     */
    class CompoundIterator
	implements Iterator
    {
	private List iterators;
	private Iterator currentIterator = null;
	private int currentIndex = 0;

	public CompoundIterator(List iterators)
	{
	    this.iterators = iterators;
	    if( iterators.size() > 0 ){ currentIterator = (Iterator)iterators.get(0); }
	}

	public Object next() throws NoSuchElementException
	{
	    try{
		return currentIterator.next();
	    }catch( Exception e ){
		if( currentIndex < (iterators.size()-1) ){
		    currentIndex++;
		    currentIterator = (Iterator)iterators.get(currentIndex);
		    return next();
		} else {
		    throw new NoSuchElementException();
		}
	    }
	}

	public boolean hasNext()
	{
	    try {
		if( currentIterator.hasNext() ){
		    return true;
		} else {
		    for( int i=currentIndex+1; i<iterators.size(); i++ ){
			if( ((Iterator)iterators.get(i)).hasNext() ){ return true; }
		    }
		    return false;
		}
	    } catch( NullPointerException e ){ return false; }
	}


	/** Unimplemented - in an attempt to make this efficient */
	public void remove(){ throw new UnsupportedOperationException(); }

    }

    /**
     * Returns an {@linkplain java.util.Iterator} which visits each element of
     * the specified subgraphs in the corpora exactly once.
     * @param pointingElement the element pointing to the subgraphs
     * @return an {@linkplain java.util.Iterator} which visits each element of
     * the specified subgraphs in the corpora exactly once.
     */
    public java.util.Iterator getElementsOfSubgraph(Object pointingElement)
    {
	return new CorpusIteratorOfSubgraph(pointingElement);
    }

    class CorpusIteratorOfSubgraph
	implements Iterator
    {
	private List parents = new ArrayList();
	/** constructor */
	public CorpusIteratorOfSubgraph(Object pointingElement)
	{
	    try {
		List pointersToSubgraphs = ((NOMElement)pointingElement).getPointers();
		for( Iterator i = pointersToSubgraphs.iterator(); i.hasNext(); ){
		    parents.add( ((NOMPointer)i.next()).getToElement() );
		}
	    } catch( Exception e ){} //skip
	}

	public boolean hasNext(){ return !parents.isEmpty(); }

	//!!!not yet implemented: check if elements are visited only one
	public Object next() throws NoSuchElementException
	{
	    try{
		NOMElement ret = (NOMElement)parents.remove(0);
		try {
		    List l = ret.getParents();
		    for( Iterator i = l.iterator(); i.hasNext(); ){
			try {
			    NOMElement x = (NOMElement)i.next();
			    if( element_list.contains(x) ){ parents.add(x); }
			} catch( Exception e ){} //skip
		    }
		    //          parents.addAll( ret.getParents() );
		} catch( Exception e ){} //skip
		return ret;
	    } catch( Exception e ){ throw new NoSuchElementException(); }
	}

	/** Unimplemented - in an attempt to make this efficient */
	public void remove(){ throw new UnsupportedOperationException(); }
    }


    /** check the start and end times of a new element in case it
        changes the start and end times of the corpus */
    private void checkTimes(NOMElement element) {
	//	Debug.print("Check Times for : " + element.getID() + ". Start time is: " + element.getStartTime() + ". Current start time: " + corpus_start_time); 
	if ((new Double(corpus_start_time)).isNaN()) {
	    corpus_start_time=element.getStartTime();
	    corpus_end_time=element.getEndTime();
	} else {
	    if (corpus_start_time > element.getStartTime()) {
		corpus_start_time=element.getStartTime();		
	    }
	    if (corpus_end_time < element.getEndTime()) {
		corpus_end_time=element.getEndTime();		
	    }
	}
    }

    /** returns the earliest start time of any element in the corpus
        (or UNTIMED if there is no timed element) */
    public double getCorpusStartTime() {
	return corpus_start_time;
    }

    /** returns the latest end time of any element in the corpus
        (or UNTIMED if there is no timed element) */
    public double getCorpusEndTime() {
	return corpus_end_time;
    }

    /** returns the duration of the corpus (last end time - earliest start time)
        (or UNTIMED if there is are no timed elements) */
    public double getCorpusDuration() {
	if (corpus_start_time==NOMElement.UNTIMED || 
	    corpus_end_time==NOMElement.UNTIMED) {
	    return NOMElement.UNTIMED;
	}
	return corpus_end_time - corpus_start_time;
    }

    /** Return the log PrintStream */
    public PrintStream getLogStream () {
	return Debug.getStream();
    }

    /** Set the log PrintStream */
    public void setLogStream(PrintStream ps) {
	Debug.setStream(ps);
    }

    /** Return the error PrintStream */
    public PrintStream getErrorStream () {
	return ERR;
    }

    /** Set the error PrintStream */
    public void setErrorStream(PrintStream ps) {
	ERR=ps;
    }

    /** add a derived attribute to all the relevant elements in the
        entire corpus. The name of the attribute is given and the
        value is derived from a different attribute value plus the
        offset given.*/
    public void addDerivedAttribute(String oldatt, String newatt, double offset) {
	for (Iterator eit=this.getElements(); eit.hasNext(); ) {
	    NOMWriteElement nel = (NOMWriteElement)eit.next();
	    Double newval = null;
	    if (oldatt.equalsIgnoreCase(metadata.getStartTimeAttributeName())) {
		double st = nel.getStartTime();
		if (Double.isNaN(st)) continue;
		newval = new Double(st + offset);
	    } else if (oldatt.equalsIgnoreCase(metadata.getEndTimeAttributeName())) {
		double en = nel.getEndTime();
		if (Double.isNaN(en)) continue; 
		newval = new Double(en + offset);
	    } else {
		NOMAttribute nat = nel.getAttribute(oldatt);
		if (nat==null) continue;
		Double val = nat.getDoubleValue();
		if (val==null) continue;
		newval = new Double(val.doubleValue() + offset);
	    }

	    try {
		nel.addAttribute(nommaker.make_attribute(newatt, newval));
	    } catch (NOMException nex) {
		nex.printStackTrace();
	    }
	}
    }

    /** add derived 'duration' attributes to all the timed elements in the
        corpus. The name of the duration attribute is given.*/
    public void addDurations(String attname) {
	for (Iterator eit=this.getElements(); eit.hasNext(); ) {
	    NOMWriteElement nel = (NOMWriteElement)eit.next();
	    Double newval = null;
	    double st = nel.getStartTime();
	    if (Double.isNaN(st)) continue;
	    double en = nel.getEndTime();
	    if (Double.isNaN(en)) continue; 
	    newval = new Double(en - st);
	    try {
		nel.addAttribute(nommaker.make_attribute(attname, newval));
	    } catch (NOMException nex) {
		nex.printStackTrace();
	    }
	}
    }

    /** This is used by my initial nomread implementation to make sure
     * the created elements are all nomread elements rather than
     * mixing the implementations */
    protected void setMaker(NOMMaker maker) {
	this.nommaker=maker;
    }

    /** This is used by internal corpus-building routines so we make
     * sure we always use the right constructors. */
    public NOMMaker getMaker() {
	return nommaker;
    }

    class Maker implements NOMMaker {
	public NOMAnnotation make_annotation(NOMCorpus corpus, String name, 
					     Attributes attributes, String colour, NResource resource, 
					     boolean stream) throws NOMException {
	    return new NOMWriteAnnotation(corpus,name,attributes,colour,resource,stream);
	}
	public NOMAnnotation make_annotation(NOMCorpus corpus, String name, 
					     String colour, NResource resource, boolean stream, String id) throws NOMException {
	    return new NOMWriteAnnotation(corpus,name,colour,resource,stream,id);
	}
	public NOMAnnotation make_annotation(NOMCorpus corpus, String name,
					     String observation, String agent, NResource resource) throws NOMException {
	    return new NOMWriteAnnotation(corpus,name,observation,agent,resource);
	}
	/** This creates a comment element */
	public NOMAnnotation make_annotation(NOMCorpus corpus, String comment, 
					     String colour, NResource resource) throws NOMException {
	    return new NOMWriteAnnotation(corpus,comment,colour,resource);
	}

	public NOMObject make_object(NOMCorpus corpus, String name, 
				     Attributes attributes, String colour, NResource resource, 
				     boolean stream) throws NOMException {
	    return new NOMWriteObject(corpus,name,attributes,colour,resource,stream);
	}
	public NOMObject make_object(NOMCorpus corpus, String name, 
				     String colour, NResource resource, boolean stream, String id) throws NOMException {
	    return new NOMWriteObject(corpus,name,colour,resource,stream,id);
	}

	public NOMResourceElement make_resource_element(NOMCorpus corpus, String name, 
							Attributes attributes, String colour, NResource resource, 
							boolean stream) throws NOMException {
	    return new NOMWriteResourceElement(corpus,name,attributes,colour,resource,stream);
	}
	public NOMResourceElement make_resource_element(NOMCorpus corpus, String name, 
							String colour, NResource resource, boolean stream, String id) throws NOMException {
	    return new NOMWriteResourceElement(corpus,name,colour,resource,stream,id);
	}
	/** This creates a comment element */
	public NOMResourceElement make_resource_element(NOMCorpus corpus, String comment, 
							String colour, NResource resource) throws NOMException {
	    return new NOMWriteResourceElement(corpus,comment,colour,resource);
	}

	public NOMTypeElement make_type_element(NOMCorpus corpus, String name, 
						Attributes attributes, String colour, NResource resource, 
						boolean stream) throws NOMException {
	    return new NOMWriteTypeElement(corpus,name,attributes,colour,resource,stream);
	}
	public NOMTypeElement make_type_element(NOMCorpus corpus, String name, 
						String colour, NResource resource, boolean stream, String id) throws NOMException {
	    return new NOMWriteTypeElement(corpus,name,colour,resource,stream,id);
	}

	public NOMAttribute make_attribute(int type, String name, 
					   String string_value, Double double_value) {
	    return new NOMWriteAttribute(type,name,string_value,double_value);
	}
	/** create a numeric attribute */
	public NOMAttribute make_attribute(String name, Double double_value) {
	    return new NOMWriteAttribute(name,double_value);
	}
	/** create a string attribute */
	public NOMAttribute make_attribute(String name, String string_value) {
	    return new NOMWriteAttribute(name,string_value);
	}

	public NOMPointer make_pointer (NOMCorpus corpus, String role, 
					NOMElement source, String targetstr) {
	    return new NOMWritePointer(corpus,role,source,targetstr);
	}
	public NOMPointer make_pointer (NOMCorpus corpus, String role, 
					NOMElement source, NOMElement target) {
	    return new NOMWritePointer(corpus,role,source,target);
	}

    }


    /*-----------------------------------*/
    /* Lazy Loading utilities            */
    /*-----------------------------------*/

    /** load any files that may that may contain elements of the given name */
    private void loadRequestedElementName(String element_name) {
	if (cleaning_up) { return; }
	//Debug.print("Loading requested element " + element_name + " for observations " + loaded_observations, Debug.DEBUG);
	boolean loaded=false;
	try {
	    NFile nfile = metadata.getElementByName(element_name).getFile();
	    //NCoding elcoding = (NCoding)((NLayer)metadata.getElementByName(element_name).getContainer()).getContainer();
	    for (Iterator oit=loaded_observations.iterator(); oit.hasNext(); ) {
		if (ensureLoadCode(nfile, (NObservation)oit.next(), null)) { loaded=true; }
	    }
	} catch (Exception ex) { 
	    Debug.print("Element " + element_name + " unknown.", Debug.IMPORTANT);
	}
	if (loaded) { cleanupCorpus(false); }
    }

    /** load any files that may that may contain elements of the given name */
    private void loadRequestedColour(String element_id) {
	if (cleaning_up) { return; }
	//if (unloadedFiles.size()==0) { return; }
	//Debug.print("Loading requested colour " + element_id + ".", Debug.DEBUG);
	boolean loaded=false;
	try {
	    NOMFile fl = new NOMFile(metadata, element_id);
	    loaded = ensureLoadCode(fl.getNFile(), fl.getObservation(), null);
	} catch (Exception ex) { }
	if (loaded) { cleanupCorpus(false); }
    }

    /** load any files that may that may contain pointers to this element */
    protected void loadRequestedPointersTo(NOMElement element) {
	if (cleaning_up) { return; }
	//if (unloadedFiles.size()==0) { return; }
	//Debug.print("Loading requested pointers to " + element.getName() + ".", Debug.DEBUG);
	if (element.isStreamElement()) { return; }
	NOMFile fl = new NOMFile(metadata, element.getColour());
	NObservation ob = fl.getObservation();
	boolean loaded=false;
	try {
	    Set myset = metadata.getValidPointersTo(element.getLayer().getName());
	    for (Iterator pit = myset.iterator(); pit.hasNext(); ) {
		NLayer nlay = (NLayer)pit.next();
		NFile pcoding = (NFile)nlay.getContainer();
		if (ensureLoadCode(pcoding, ob, null)) { loaded=true; }
	    }
	} catch (Exception ex) { 
	    Debug.print("Pointer to "+element.getName()+" caused an exception.", Debug.DEBUG);
	}
	if (loaded) { cleanupCorpus(false); }
    }

    /** load any files that may that may contain pointers from this
     * element with the given role */
    protected void loadRequestedPointer(NOMElement element, String role) {
	if (cleaning_up) { return; }
	//if (unloadedFiles.size()==0) { return; }
	//Debug.print("Loading requested pointer " + element.getName() + " with role " + role, Debug.DEBUG);
	if (element==null || element.isStreamElement()) { return; }
	NOMFile fl = new NOMFile(metadata, element.getColour());
	NObservation ob = fl.getObservation();
	boolean loaded=false;
	try {
	    NElement nel = metadata.getElementByName(element.getName());
	    NLayer target=null;
	    NFile targetfile=null;
	    for (Iterator pit=nel.getPointers().iterator(); pit.hasNext(); ) {
		NPointer p = (NPointer)pit.next();
		if (p.getRole().equals(role)) {
		    target = metadata.getLayerByName(p.getTarget());
		    if (target==null) {
			targetfile = metadata.getOntologyByName(p.getTarget());
			if (targetfile==null) {
			    targetfile = metadata.getObjectSetByName(p.getTarget());
			}
			if (targetfile==null) {
			    targetfile = metadata.getCorpusResourceByName(p.getTarget());
			}
		    }
		    break;
		}
	    }
	    if (targetfile==null) {
		targetfile = (NFile)target.getContainer();
	    }
	    if (ensureLoadCode(targetfile, ob, null)) { loaded=true; }
	} catch (Exception ex) {
	    Debug.print("Pointer from "+element.getName()+" with role "+role+" caused an exception.", Debug.DEBUG);
	    //ex.printStackTrace();
	}
	if (loaded) { cleanupCorpus(false); }
    }


    /** load all children of this node */
    protected void loadRequestedChildren(NOMElement element, String elcol) {
	boolean loaded = false; 
	if (element.isStreamElement()) { return; }
	NLayer ellay=null;
	try {
	    ellay=(NLayer)metadata.getElementByName(element.getName()).getContainer();
	    if (loadedChildLayers.contains(ellay)) { return; }
	} catch (Exception ex) { }
	try {
	    //Debug.print("Loading requested children of " + element.getName() + "(" + elcol +").", Debug.DEBUG);
	    NOMFile fl = new NOMFile(metadata, element.getColour());
	    NObservation ob = fl.getObservation();
	    NFile kcoding = (NFile)ellay.getChildLayer().getContainer();
	    if (ensureLoadCode(kcoding, ob, null)) { loaded=true; }
	} catch (Exception ex) { 
	    // ex.printStackTrace();
	}
	if (loaded) { 
	    cleanupCorpus(false); 
	    if (ellay!=null) { loadedChildLayers.add(ellay); }
	}
    }

    /** load any files that may that may contain descendants of this
     * element that might affect its start or end times */
    protected void loadRequestedChildTimeLayers(NOMElement element) {
	if (cleaning_up) { return; }
	//if (unloadedFiles.size()==0) { return; }
	boolean loaded=false;
	//Debug.print("Loading requested child time layers of " + element.getName() + ".", Debug.DEBUG);
	String elcol = element.getColour();
	if (loadedToTimeColours.contains(elcol)) { return; }
	try {
	    NLayer ellay = element.getLayer();
	    NCoding elcoding = (NCoding)ellay.getContainer();
	    NCoding kcoding = (NCoding)ellay.getChildLayer().getContainer();
	    NOMFile ef = new NOMFile(metadata, elcol);
	    loadToTimeAlignedLayer(elcoding, kcoding, ef);
	    loadedToTimeColours.add(elcol);
	} catch (Exception ex) { }
    }

    /** load any files from the current layer down to the
     * first-encountered time aligned layer. */
    private void loadToTimeAlignedLayer(NCoding pcode, NCoding kcode, NOMFile elfile) {
	boolean loaded=false;
	try {
	    if (kcode==null || pcode==null) { return; }
	    if (pcode.getBottomLayer().getLayerType()==NLayer.TIMED_LAYER) { return; }
	    if (kcode==pcode) { 
		loadToTimeAlignedLayer(kcode, (NCoding) kcode.getBottomLayer().getChildLayer().getContainer(), elfile); 
		return;
	    }
	    // use any agent of the original element.
	    loaded = ensureLoadCode(kcode, elfile.getObservation(), null);
	} catch (Exception ex) { }
	if (loaded) { cleanupCorpus(false); }
	if (kcode.getBottomLayer().getLayerType()!=NLayer.TIMED_LAYER) { 
	    loadToTimeAlignedLayer(kcode, (NCoding) kcode.getBottomLayer().getChildLayer().getContainer(), elfile); 	    
	}
    }

    /** load any files that may that may contain parents of this element */
    protected void loadRequestedParentLayers(NOMElement element) {
	if (cleaning_up) { return; }
	//if (unloadedFiles.size()==0) { return; }
	if (element.isStreamElement()) { return; }
	//NOMException nex = new NOMException("debug");
	//nex.printStackTrace();
	String elcol = element.getColour();
	NLayer ellay = null;
	try {
	    ellay = element.getLayer();
	} catch (Exception ex) { }
	//Debug.print("Loading requested parents of " + element.getName() + "; colour " + ellay.getName() + "; already loaded: " + loadedParentLayers.contains(ellay), Debug.DEBUG);
	if (ellay==null || loadedParentLayers.contains(ellay)) { return; }
	NOMFile fl = new NOMFile(metadata, element.getColour());
	NFile nf = fl.getNFile();
	NObservation ob = fl.getObservation();
	boolean loaded=false;
	try {
	    // this is a bug fix: we don't want to fill in the fact we
	    // have loaded all parents of a coding when in fact we
	    // have only loaded the parents of the bottom layer!
	    //	    if (nf instanceof NCoding) { 
	    //ellay = ((NCoding)nf).getTopLayer();
	    //}
	    List plays = ellay.getParentLayers();
	    NCoding elcoding = (NCoding)ellay.getContainer();
	    NOMFile ef = new NOMFile(metadata, element.getColour());
	    for (Iterator pit=plays.iterator(); pit.hasNext(); ) { 
		NLayer par = (NLayer) pit.next();
		NFile pcoding = (NFile)par.getContainer();
		if (par==ellay) { continue; }
		//System.err.println("Ensure loaded coding: " + pcoding.getName() + " parent of " + ellay.getName());
		if (ensureLoadCode(pcoding, ob, null)) { loaded=true; }
	    }
	} catch (Exception ex) { }
	if (loaded) { 
	    cleanupCorpus(false); 
	}
	loadedParentLayers.add(ellay);
    }

    /** load any files that may that may contain ancestors of this
     * element with the given name, plus any in-between files.  */
    protected void loadRequestedAncestorLayers(NOMElement element, String ancestorName) {
	if (cleaning_up) { return; }
	//if (unloadedFiles.size()==0) { return; }
	if (element.isStreamElement()) { return; }
	//Debug.print("Loading requested ancestors of " + element.getName() + ".", Debug.DEBUG);
	boolean loaded=false;
	try {
	    NLayer ellay = element.getLayer();
	    NLayer anlay = (NLayer)metadata.getElementByName(ancestorName).getContainer();
	    List path = metadata.findPathBetween(anlay, ellay);
	    //Debug.print("Path between " + anlay.getName() + " and " + ellay.getName() + ": " + path, Debug.DEBUG);	    
	    if (path==null || path.size()<=0) { return; }
	    NCoding elcoding = (NCoding)ellay.getContainer();
	    NOMFile ef = new NOMFile(metadata, element.getColour());

	    for (Iterator pit=path.iterator(); pit.hasNext(); ) {
		NFile pcoding = (NFile) pit.next();
		if (pcoding==elcoding) { continue; }
		//Debug.print("Ensure loaded " + pcoding.getName() + ".", Debug.DEBUG);
		if (ensureLoadCode(pcoding, ef.getObservation(), null)) { loaded=true; }

		/*
		NLayer par = (NLayer) pit.next();
		if (par==ellay) { continue; }
		//String newcolour = element.getColour();
		NFile pcoding = (NFile)par.getContainer();
		Debug.print("Ensure loaded " + pcoding.getName() + ".", Debug.DEBUG);
		loaded = ensureLoadCode(pcoding, ef.getObservation(), null);
		*/
	    }
	} catch (Exception ex) { ex.printStackTrace(); }
	if (loaded) { cleanupCorpus(false); }
    }

    /** force the loading of a lazy file and remove it from the global list */
    private void forceLoad(NOMFile lf) throws NOMException {
	boolean l=lazy_loading; 
	try {
	    lazy_loading=false;
	    setBatchMode(true);
	    loadCode(lf.getNFile(), lf.getObservation(), lf.getAgent());
	    setBatchMode(false);
	    unloadedFiles.remove(lf);
	} catch (NOMException nex) {
	    lazy_loading=l;
	    throw nex;
	}
	lazy_loading=l;
    }

    /** remove a file from the list of unloaded ones */
    protected void removeUnloaded(String fn) {
	NOMFile torem=null;
	for (Iterator fit = unloadedFiles.iterator(); fit.hasNext(); ) {
	    NOMFile lf = (NOMFile) fit.next();
	    if (lf.getFilename().equals(fn)) {
		torem=lf;
		break;
	    }
	}
	if (torem!=null) {
	    unloadedFiles.remove(torem);
	}
    }

    /** Simply stores a preference (or demand) for a particular
     * annotator's data to be loaded for a coding. Note that the
     * SpecialLoad class is used to store *actually loaded* codings */
    class AnnotatorCoding {
	String annotator;
	NCoding coding;

	public AnnotatorCoding(String ann, NCoding cod) {
	    annotator=ann;
	    coding = cod;
	}

	public String getAnnotator() {
	    return annotator;
	}

	public NCoding getCoding() {
	    return coding;
	}

    }

    /** take details of a load that is to a non-standard path
     * (i.e. for a specific annotator). This class is for an actual
     * loaded file; the AnnotatorCoding class simply stores a
     * preference (or demand) for a particular annotator's data to be
     * loaded for a coding. */
    class SpecialLoad {
	NObservation observation;
	NCoding coding;
	String annotator;

	public SpecialLoad(NObservation ob, NCoding cod, String ann) {
	    observation=ob;
	    coding = cod;
	    annotator=ann;
	}

	public String getAnnotator(NObservation ob, NCoding cod) {
	    if (ob.getShortName().equals(observation.getShortName()) &&
		cod.getName().equals(coding.getName())) {
		return annotator;
	    }
	    return null;
	}
	public String getAnnotator() {
	    return annotator;
	}
	public NObservation getObservation() {
	    return observation;
	}
	public NCoding getCoding() {
	    return coding;
	}

    }

    /** a utility function used by many of the comparators to stop us
	temporal relations between elements in different
	observations. jonathan 24/4/06 */
    private boolean temporalRelationValid(NOMElement a, NOMElement b) {
	String oba = a.getObservation();
	String obb = b.getObservation();
	// no temporal relation to corpus resources
	if (oba==null || obb==null) { return false; } 
	return oba.equals(obb);
    }

    /** a utility function used by many of the comparators to stop us
	structural relations between elements in different
	observations. jonathan 24/4/06 */
    private boolean structuralRelationValid(NOMElement a, NOMElement b) {
	String oba = a.getObservation();
	String obb = b.getObservation();
	// can have structural relation within corpus resources but
	// not between annotations and corpus-resources
	if (oba==null && obb==null) { return true; } 
	if (oba==null || obb==null) { return false; }
	return oba.equals(obb);
    }

    /** a utility function used by many of the comparators to stop us
	pointer relations between elements in different
	observations. jonathan 24/4/06 */
    private boolean pointerRelationValid(NOMElement a, NOMElement b) {
	String oba = a.getObservation();
	String obb = b.getObservation();
	// pointers to and from corpus resources are OK
	if (oba==null || obb==null) { return true; } 
	// otherwise make sure we stick to one observation
	return oba.equals(obb);
    }

    /** set to true to percolate text content from leaf nodes to higher in the tree */
    public void setTextPercolate(boolean val) {
	percolatetext=val;
    }

    /** true means elements above text-containing elements inherit the
     * text content; false (default) means no percolation of text
     * takes place. */
    public boolean isTextPercolating() {
	return percolatetext;
    }

    /** set to true to enable the new query rewrite functionality that
     * can increase the speed of your queries */
    public void setQueryRewriting(boolean val) {
	rewrite=val;
	if (val && rewriter==null) { rewriter=new DefaultQueryRewriter(this); }
    }

    /** set to true to force user question whenever a choice of
     * resources is available. */
    public void setResourceLoadAsk(boolean val) {
	forceresourceask=val;
    }

    /** true means we have enabled the new query rewrite functionality
     * that can increase the speed of your queries. false (the
     * default) means we haven't */
    public boolean isQueryRewriting() {
	return rewrite;
    }

    /** Enable the query rewrite functionality and select a rewriter
     * to use (if the argument is null, query rewriting will not be
     * enabled). */
    public void setQueryRewriter(QueryRewriter writer) {
	rewriter=writer;
	if (rewriter!=null) { rewrite=true; }
    }

    /** Return the query rewriter that should be used (or null if it
     * is not set) */
    public QueryRewriter getQueryRewriter() {
	return rewriter;
    }

    /** Return the current 'context': the locations and types of files
     * loaded at this point in time. */
    public NOMContext getContext() {
	return context;
    }

}

