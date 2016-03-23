package net.sourceforge.nite.tools.comparison.nonspanning;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.lang.Double;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.text.Style;
import java.awt.FlowLayout;

import net.sourceforge.nite.gui.mediaviewer.NITEVideoPlayer;
import net.sourceforge.nite.gui.textviewer.NTextArea;
import net.sourceforge.nite.gui.textviewer.NTextElement;
import net.sourceforge.nite.gui.textviewer.NTree;
import net.sourceforge.nite.gui.textviewer.NTreeNode;
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.util.ChooseObservation;
import net.sourceforge.nite.gui.util.EnumerationTreeView;
import net.sourceforge.nite.gui.util.OntologyTreeView;
import net.sourceforge.nite.gui.util.SwingUtils;
import net.sourceforge.nite.meta.NCoding;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.meta.NSignal;
import net.sourceforge.nite.meta.impl.NiteMetaData;
import net.sourceforge.nite.meta.impl.NiteMetaException;
import net.sourceforge.nite.meta.impl.NiteObservation;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomread.impl.NOMReadCorpus;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.NOMTypeElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteCorpus;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.nstyle.handler.TextAreaHandler;
import net.sourceforge.nite.search.Engine;
import net.sourceforge.nite.time.Clock;
import net.sourceforge.nite.time.DefaultClock;
import net.sourceforge.nite.time.ClockFace;
import net.sourceforge.nite.tools.necoder.NECoderConfig;
import net.sourceforge.nite.util.IteratorTransform;
import net.sourceforge.nite.util.Transform;
import net.sourceforge.nite.util.Debug;
import net.sourceforge.nite.util.SearchResultTimeComparator;
import net.sourceforge.nite.gui.util.AbstractCallableTool;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.nom.nomwrite.*;

import org.xml.sax.SAXException;

//TODO: Derive points-to-word-layer elements from the comparisonlayer tree
//(e.g. given extsumm-layer, detect that dact elements are to be displayed)

/**
 * Displays Non-Spanning annotations by 2 separate annotators.
 * <p>
 * A Non-Spanning annotation is one of a set of annotations
 * within a coding layer that points to dialogue where the
 * annotations do not cover the whole dialogue.
 * <p>
 * See {@link #main} method for command-line arguments.
 * <p>
 * Important attributes from nxtConfig:
 * <p>
 * <dl>
 * <dt>neelementname</dt> <dd>name of element to compare</dd>
 * <dt>neontology</dt> <dd>used to display tree gui</dd>
 * <dt>neroot</dt> <dd>where to start drawing the tree gui</dd>
 * <dt>netyperole</dt> <dd>pointer type into ontology (used to find annotator text)</dd>
 * <dt>abbrevattribute</dt> <dd>attribute of netyperole pointer used for annotator text</dd>
 * <dt>nenameattribute</dt> <dd>text to diplay if no valid pointer</dd>
 * <dt>transcriptionlayername</dt> <dd>where the dialogue is</dd>
 * <dt>transcriptiondelegateclassname</dt>
 * <dt>transcriptionattribute</dt>
 * <dt>segmentationelementname</dt> <dd>one line of dialogue</dd>
 * <dt>nsannotatorlayer</dt> <dd>layer containing elements to compare</dd>
 * <dt>nscommonlayer</dt> <dd>common layer name</dd>
 * </dl>
 * <p>
 * Remaining tasks:
 * <ul>
 * <li>TODO: - annotator path (optional)</li>
 * <li>TODO: - turn on/off text styling (for items inside brackets)</li>
 * <li>TODO: Next/Previous annotation navigation buttons</li>
 * </ul>
 * <p>
 * Based heavily on JastDisplay by Craig Nicol, which is
 * based on MonitorDisplay by 
 *       Jonathan Kilgour, Jean Carletta March 2003
 * and also on NECoder and AbstractCallableTool
 * 
 * @author Craig Nicol September 2006
 * 
 * @see #main
 * @see NonSpanningCoderConfig
 * @see MergedViewDisplayStrategy
 * @see net.sourceforge.nite.tools.necoder
 * @see net.sourceforge.nite.tools.necoder.NECoder
 */
public class NonSpanningComparisonDisplay {
	private Clock niteclock;
	private ClockFace vclock;
	NOMWriteCorpus nom;
	NiteMetaData controlData;
	JInternalFrame iframe;
	JInternalFrame iframe2;
	String corpusname;
	String observationname;
	String configFilename;
	String guisettings;
	String corpussettings;
	NonSpanningCoderConfig config;
	JFrame frame;
	String exportdir = ".";
	
	//String comparisonlayername = "extsumm-layer"; //"ne-layer";
	//String commonlayername = "da-layer"; //"words-layer"; //"segment-layer";
	List othercodingnames = new ArrayList(); //{"segments"};
	String annotatorattr = "coder";
	String autoname = null;
	String manualname = null;
	String pathtoannotators = null;
	int showannotators = 3;
	
	MergedViewDisplayStrategy mvds;
	NTranscriptionView ntv;
	NTree ntree; // Named Entity List
	JScrollPane legendpane;
	JScrollPane treepane;
	JDesktopPane desktop;
	NOMWriteElement changed;
	NTextArea legend = null;
	
	List annelements; // For navigation
	List ann1elements;
	List ann2elements;
	boolean timedannotations = false;
	JComboBox navigationType;
	// If we were using Java 1.5, there would be Map<NOMElement, int>
	Map mapElementStart = new HashMap();
	Map mapElementEnd = new HashMap();

	private net.sourceforge.nite.search.GUI search=null;
	
	/** 
	 *Search engine to find annotation elements 
	 */
	protected Engine searchEngine= new Engine();
	public List search(String query) {
		List result = null;
		try {
			result = searchEngine.search(nom, query); 
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (result == null || result.size() == 1) {
			System.err.println("NO DATA OF ELEMENTS IN CORPUS! ");
		}
		return result;
        }

   protected NTranscriptionView getNTV() 
     {
	return ntv;
     }
   
   
	private Comparator mycomp = new SearchResultTimeComparator();

    /* This is disabled since annotator specific loads are incompatable with LoadReliability */
    protected void initNomAnnotatorSpecificLoads(NOMWriteCorpus nom) throws NOMException {
        return;
    }
	
	// Find the first element that starts after a certain time.
	private NOMElement findNextAnnotation(double time, Iterator it) {
		if (!it.hasNext()) { System.err.println("findNext Error: At Last Element.\n"); return null; }
		
		NOMElement el = null;
		List li;
		
		do {
			li = (List)(it.next());
			el = (NOMElement)(li.get(0));
		} while(it.hasNext() && el.getStartTime() <= time);
		
		return el;
	}
	
	// Find the first element that starts after a certain time
	private NOMElement findNextAnnotation(double time) {
		return findNextAnnotation(time, annelements.iterator());
	}

    public String getAnnotatorName() {
        return "1: " + autoname + ", 2: " + manualname; 
    }
	
	// Find the last element that ends before a certain time
	private NOMElement findPreviousAnnotation(double time, ListIterator it) {
		if (!it.hasPrevious()) { System.err.println("findPrevious Error: At First Element.\n"); return null; }
		
		NOMElement el = null;
		List li;
		
		do {
			li = (List)(it.previous());
			el = (NOMElement)(li.get(0));
		} while(it.hasPrevious() && el.getEndTime() > time);
		
		return el;
	}
	// Find the first element that ends before a certain time
	private NOMElement findPreviousAnnotation(double time) {
		return findPreviousAnnotation(time, annelements.listIterator(annelements.size()));
	}
	
	// Find the first element that starts after a certain position.
	private NOMElement findNextAnnotation(int pos, Iterator it) {
		if (!it.hasNext()) { System.err.println("findNext Error: At Last Element.\n"); return null; }
		
		NOMElement el = null;
		List li;
		
		do {
			li = (List)(it.next());
			el = (NOMElement)(li.get(0));
		} while(it.hasNext() && getElementStart(el) <= pos);
		
		return el;
	}
	
	// Find the first element that starts after this position
	private NOMElement findNextAnnotation(int pos) {
		return findNextAnnotation(pos, annelements.iterator());
	}
	
	// Find the last element that ends before this position
	private NOMElement findPreviousAnnotation(int pos, ListIterator it) {
		if (!it.hasPrevious()) { System.err.println("findPrevious Error: At First Element.\n"); return null; }
		
		NOMElement el = null;
		List li;
		
		do {
			li = (List)(it.previous());
			el = (NOMElement)(li.get(0));
		} while(it.hasPrevious() && getElementEnd(el) >= pos);
		
		return el;
	}
	// Find the first element that ends before this position
	private NOMElement findPreviousAnnotation(int pos) {
		return findPreviousAnnotation(pos, annelements.listIterator(annelements.size()));
	}
	
	public NonSpanningComparisonDisplay(String c, String o, String cf, String gs, String cs, String an, String mn, int s) {
		corpusname = c;
		observationname = o;
		configFilename = cf;
		guisettings = gs;
		corpussettings = cs;
		autoname = an;
		manualname = mn;
		config = new NonSpanningCoderConfig();
		showannotators = s;
		
		loadConfigSettings();
		
		//Debug.print("Config settings loaded", Debug.IMPORTANT);
		//Debug.print("Common layer is " + config.getCommonLayer());
		
		try {
			controlData = new NiteMetaData(corpusname);
		} catch (NiteMetaException nme) {
			nme.printStackTrace();
		}
		
		if (observationname==null) {
			ChooseObservation co = new ChooseObservation(controlData);
			observationname=co.popupDialog();
		} 
		
		NLayer toplay = controlData.getLayerByName(config.getAnnotatorLayer());
		NLayer commlay = null;
		if (config.getCommonLayer()!=null) {
			commlay = controlData.getLayerByName(config.getCommonLayer());
			if (commlay==null) {
				System.err.println("Can't find common layer (" + config.getCommonLayer() + ") in the metadata.\n"); 
				System.exit(1);
			}
		}

		
		if (toplay==null) {
			System.err.println("Can't find annotator layer (" + config.getAnnotatorLayer() + ") in the metadata.\n"); 
			System.exit(1);
		}
		
		nom = new NOMReadCorpus(controlData); //multi annotator
		niteclock = new DefaultClock(controlData, observationname);
		if (controlData.getCorpusType() == NMetaData.STANDOFF_CORPUS) {
		    //System.out.println("This is a standoff corpus: NOM being loaded");
			try {
				//nom = new NOMWriteCorpus(controlData); // single annotator
				NiteObservation obs = observationWithName(observationname);
				//nom.loadData(obs); // single annotator
				
				//* START multiple annotators
				Debug.print("Loading annotators data...", Debug.DEBUG);
				ArrayList obslist = new ArrayList();
				obslist.add(obs);
				
				nom.loadData(obslist, null); // this is just so we can lazy load later
				nom.loadReliability(toplay, commlay, annotatorattr, pathtoannotators, obslist);

				/* because multi-annotator loads now cope OK with lazy 
				 * loading, this is no longer needed - JAK 28.3.07
				
				// Must do it this way to avoid ID clashes
				ArrayList codings = new ArrayList();
				othercodingnames.addAll(config.getExtraCodings());
				Debug.print("Adding " + othercodingnames.size() + " extra codings:", Debug.DEBUG);
				for (int i=0; i < othercodingnames.size(); i++) {
					Debug.print("Finding coding " + i + ": " + othercodingnames.get(i) + "...", Debug.DEBUG);
					NCoding nc = controlData.getCodingByName((String)othercodingnames.get(i));
					if (nc==null) { 
						System.err.println("Failed to find coding: " + othercodingnames.get(i));
						continue; 
					}
					Debug.print("Adding coding " + nc.getName() + "...", Debug.DEBUG);
					codings.add(nc);
				}
				nom.loadData(obslist, codings);
                                */

				// END multiple annotators */
				Debug.print("Finished loading data.", Debug.DEBUG);
				search=new net.sourceforge.nite.search.GUI(nom);
				
				Debug.print("search loaded...", Debug.PROGRAMMER);
				
				setupInterface(nom);
				if (getNTV()!=null) {
				    search.registerResultHandler(getNTV()) ;
				}    
			} catch (NOMException nex) {
				nex.printStackTrace();
			}
		} else {
			System.out.println("This is a standalone or simple corpus: no NOM has been loaded");
		}
	}
	
	private void setupInterface(NOMWriteCorpus nom) {
		frame = new JFrame("Non-Spanning Annotations Comparison Display");
		JMenuBar menubar = new JMenuBar();
		JMenu file = new JMenu("File");
		//file.add(new SaveAction());
		file.add(new ExitAction());
		menubar.add(file);
		/* Make the search menu too! */
		JMenu search = new JMenu("Search");
		search.add(new SearchAction());
		menubar.add(search);
		JMenu view = new JMenu("View");
		view.add(new ViewAction(0, "None"));
		view.add(new ViewAction(1, autoname));
		view.add(new ViewAction(2, manualname));
		view.add(new ViewAction(3, "Both"));
		menubar.add(view);
		frame.setJMenuBar(menubar);
		desktop = new JDesktopPane();
		
		Debug.print("addTranscriptionView()...", Debug.PROGRAMMER);
		//TODO: find out why it mysteriously stops here
		addTranscriptionView(0, 0, 540, 460);
		Debug.print("addNavigation()...", Debug.PROGRAMMER);
		addNavigation(0, 460, 540, 100); 
		Debug.print("addOntology()...", Debug.PROGRAMMER);
		addOntology(540, 0, 240, 460);
		Debug.print("addLegend()...", Debug.PROGRAMMER);
		addLegend(540, 460, 240, 100, autoname, manualname);
		
		vclock = (ClockFace)(niteclock.getDisplay());
		vclock.setClock(niteclock);
		vclock.setLocation(new Point(0, 560));
		//video.setSize(new Dimension(450, 400));
		vclock.setID(3);
		
		desktop.add(vclock);
		
		JPanel panel = new JPanel();
		desktop.setSize(new Dimension(1200, 850));
		frame.getContentPane().add(desktop);
		frame.setSize(new Dimension(1200, 850));
		frame.setVisible(true);
	}
	
	private void clearTextArea(TextAreaHandler nta) {
		
	}
	
	
	private void loadConfigSettings() {
		//Load NEConfig
		ntv = new NTranscriptionView();
		
		if (guisettings != null) {
			config.setGUISettings(guisettings);
		}
		
		if (corpussettings != null) {
			config.setCorpusSettings(corpussettings);
		}
		
		if (configFilename != null) {
			try {
				//config name: if it is a relative path, we assume that it is a path relative to the metadatafile.
				if (!(new File(configFilename).isAbsolute())) {
					String par = new File(corpusname).getParent();
					if (par!=null) {
						configFilename = par+"/"+configFilename;
					} 
				}
				config.loadConfig(configFilename);
			} catch (IOException ex) {
				Debug.print("Can't load config from file " + configFilename + " specified in metadata " + corpusname + ", exiting. StackTrace:", Debug.WARNING);
				ex.printStackTrace();
				System.exit(0);
			} catch (SAXException ex) {
				Debug.print("Can't load config from file " + configFilename + " specified in metadata " + corpusname + ", exiting. StackTrace:", Debug.WARNING);
				ex.printStackTrace();
				System.exit(0);
			}
		} else { // load with a null config file (request config from user)
			try {
				config.loadConfig(configFilename);
			} catch (Exception ex) {
				System.err.println("Can't load config file. Exiting. StackTrace:");
				ex.printStackTrace();
				System.exit(0);
			}
		}
		config.setMetaDataFile(corpusname);	
	}
	/**
	 * Creates a NTranscriptionView and adds it to the desktop at the given coordinates.
	 * <p>Pre: Corpus should be loaded; desktop should be created.
	 * <p>Post: The transcriptionView is created and visible and can be retrieved using 
	 * getNTV();
	 * This abstractcallabletool assumes that you want to have at most one transcriptionview.
	 * The contents and settings of the transcriptionview are determined by the methods
	 * initTranscriptionViewSettings() and refreshTranscriptionView(). Those are the two methods
	 * that you would most likely override to make sure that YOUR tool has exactly the right 
	 * transcription display.
	 */
	protected void setupTranscriptionView(int x, int y, int width, int height) {
		ntv=new NTranscriptionView();
		ntv.setClock(niteclock);
		JInternalFrame ntvFrame = new JInternalFrame("Transcription", true, false, true, true);
		SwingUtils.getResourceIcon(ntvFrame, "/eclipseicons/obj16/text_edit.gif", getClass());
		JScrollPane scroller= new JScrollPane(ntv);
		ntvFrame.getContentPane().add(scroller);
		ntvFrame.setVisible(true);                    
		ntvFrame.setSize(width, height);
		ntvFrame.setLocation(x,y);
		desktop.add(ntvFrame);
		
		Debug.print("initialise transcription view...", Debug.PROGRAMMER);
		initTranscriptionViewSettings();
		Debug.print("refresh transcription view...", Debug.PROGRAMMER);
		refreshTranscriptionView();
	}    
	
	/**
	 * Initializes the settings of the transcriptionView. This method does NOT fill the contents of the view.
	 * The contents are filled in 
	 * {@link project.ami.nxtutils.AbstractCallableTool#refreshTranscriptionView refreshTranscriptionView}.
	 * This methods will be overriden by the implementations.
	 * <p>
	 * <b>WHAT SHOULD BE INITIALIZED?</b>
	 
	 See also new config classes
	 */
	public void initTranscriptionViewSettings() {
		// From AbstractCallableTool
		
		//display: word layer properties
		getNTV().setTransLayerName(config.getTranscriptionLayerName());
		getNTV().setTranscriptionToTextDelegate(config.getTranscriptionToTextDelegate());
		getNTV().setTranscriptionAttribute(config.getTranscriptionAttribute());
		
		//display: segment layer properties
		getNTV().setSegmentationElementName(config.getSegmentationElementName()); 
		
		//selection: selection strategies and properties
		getNTV().setWordlevelSelectionType(config.getWordlevelSelectionType());
		getNTV().setAllowTranscriptSelect(config.getAllowTranscriptSelect());
		getNTV().setAllowAnnotationSelect(config.getAllowAnnotationSelect());
		getNTV().setAllowMultiAgentSelect(config.getAllowMultiAgentSelect());
		
		Debug.print("Load string ds...", Debug.PROGRAMMER);
		// Based on NECoder
		StringInsertDisplayStrategy ds=new StringInsertDisplayStrategy(getNTV()) {
			protected String formStartString(NOMElement element) {
				String spaces = "";
				String agentName = element.getAgentName();
				return agentName + ": ";
			}
		};
		
		ds.setEndString("");
		getNTV().setDisplayStrategy(config.getSegmentationElementName(),ds);
		
		
		Debug.print("Load mvds...", Debug.PROGRAMMER);
		String elName = ((NECoderConfig)config).getNEElementName();
		String roleName = ((NECoderConfig)config).getNETypePointerRole();
		String abbrevAttr = ((NECoderConfig)config).getNEAbbrevAttrib();
		String attrName = ((NECoderConfig)config).getNEAttributeName();
		
		// If elName in annotator layer, pass null to mcds	   
		NLayer annlay = controlData.getLayerByName(config.getAnnotatorLayer());
		
		if (annlay == controlData.getElementByName(elName).getLayer()) {
			annlay = null;
		}
		
		
		mvds = new MergedViewDisplayStrategy(getNTV(), roleName, abbrevAttr, attrName, annlay, annotatorattr, autoname, manualname);
		mvds.styletext(false);
		//mvds.setlogging(true);
		mvds.setlogging(false);
		//Debug.setDebug(false);
		if ((showannotators & 0x01) == 0) {
			mvds.hideautoannotator();
		}
		if ((showannotators & 0x02) == 0) {
			mvds.hidemanualannotator();
		}
		
		Debug.print("Set mvds...", Debug.PROGRAMMER);
		getNTV().setDisplayStrategy(elName, mvds);
		
		getNTV().setSelectDirectTextRepresentations(true);
		getNTV().setSelectTranscriptionAncestors(true);
		Set types = new HashSet();
		types.add(((NECoderConfig)config).getNEElementName());
		getNTV().setSelectableAnnotationTypes(types);
		getNTV().setAllowTranscriptSelect(true);
		getNTV().setAllowAnnotationSelect(true);
		getNTV().setAnnotationSelectionGranularity(NTranscriptionView.SINGLE_ANNOTATION);
		
	}
	
	/**
	 * Refresh the transcriptionView. This method does NOT change the settings of the view,
	 * it merely clears the contents and redisplays the transcription segments and the annotation
	 * elements. The view settings are changed in 
	 * {@link project.ami.nxtutils.AbstractCallableTool#initTranscriptionViewSettings initTranscriptionViewSettings}.
	 
	 * May be overriden, but default behaviour is OK for most. Default: display all segments of type config.getSegmentationElementName()
	 * and display all annotation element of all types in config.getDisplayedAnnotationNames().
	 */
	public void refreshTranscriptionView() {
		Transform t = new Transform() { 
			public Object transform(Object o) { 
				return ((List)o).get(0); 
			} 
		}; 
		//display segments
		Debug.print("Displaying segments: " + config.getSegmentationElementName(), 
			    Debug.PROGRAMMER);
		Iterator elemIt = search("($a " + config.getSegmentationElementName() + ")").iterator();
		if (elemIt.hasNext()){
			elemIt.next();
			Iterator transformedIt = new IteratorTransform(elemIt, t);     	
			getNTV().setDisplayedSegments(transformedIt);
		}  
		//display annotations on the transcription
		Debug.print("Displaying annotations...", Debug.PROGRAMMER);
		Iterator it = config.getDisplayedAnnotationNames().iterator();
		String elements = "";
		boolean first = true;
		while (it.hasNext()) {
			if (first) {
				first = false;
			} else {
				elements += "|";
			}
			elements += (String)it.next();
		}
		if (!elements.equals("")) {
			elemIt = search("($a " + elements +")").iterator();
			if (elemIt.hasNext()) {
				elemIt.next();  //first element is a list of some general search result variables
				Iterator transformedIt = new IteratorTransform(elemIt, t);     	
				getNTV().displayAnnotationElements(transformedIt);
			}
		}
		Debug.print("TranscriptionView refreshed.", Debug.PROGRAMMER);
	}
	
	private void addTranscriptionView(int x, int y, int width, int height) {
		
		//Read NEConfig
		String elName = config.getNEElementName();
		String roleName = config.getNETypePointerRole();
		String abbrevAttr = config.getNEAbbrevAttrib();
		String attrName = config.getNEAttributeName();
		NLayer annlay = controlData.getLayerByName(config.getAnnotatorLayer());
		ntv.setDisplayStrategy(elName, new MergedViewDisplayStrategy(ntv, roleName, abbrevAttr, attrName, annlay, annotatorattr, autoname, manualname));
		
		//Create Transcription view (x:0, y:320, width:540, height:240)
		ntv.setSelectDirectTextRepresentations(true);
		ntv.setSelectTranscriptionAncestors(true);
		Set types = new HashSet();
		types.add(config.getNEElementName());
		ntv.setSelectableAnnotationTypes(types);
		ntv.setAllowTranscriptSelect(true);
		ntv.setAllowAnnotationSelect(true);
		setupTranscriptionView(x, y, width, height);
	}

	
	private void addNavigation(int x, int y, int width, int height) {
		//legend = new NTextArea(); // Might be a frame - to get buttons

		annelements = new ArrayList();
		ann1elements = new ArrayList();
		ann2elements = new ArrayList();
		
		refreshNavigationList();
		
		iframe = new JInternalFrame("Navigation", true, false, true, true);
		iframe.setSize(new Dimension(width, height));
		iframe.setLocation(new Point(x, y));
		
		/* top (or left) of the desktop area, containing the text */
		iframe.getContentPane().setLayout(new FlowLayout());
		iframe.getContentPane().add(new JButton(new PrevAnnAction()));
		iframe.getContentPane().add(new JButton(new NextAnnAction()));
		String[] names = {"all annotators", autoname, manualname};
		navigationType = new JComboBox(names);
		iframe.getContentPane().add(navigationType);
		iframe.setVisible(true);
		desktop.add(iframe);
	}
	
	private void refreshNavigationList() {
		Comparator c;
		
		annelements.clear();
		ann1elements.clear();
		ann2elements.clear();
		
		annelements = search("($e " + config.getNEElementName() + ")");
		
		Debug.print("Annotation Elements found: " + annelements.size(), Debug.DEBUG);    
		Iterator it = annelements.iterator();
		if(it.hasNext()) { it.next(); it.remove(); } // Skip the header
		
		List firstlist = (List)annelements.get(0);
		NOMElement first = (NOMElement)firstlist.get(0);
		Debug.print("Start: " + first.getStartTime(), Debug.PROGRAMMER);
		//if ( first.getStartTime() == NOMElement.UNTIMED ) {
		if (Double.isNaN(first.getStartTime())) {
			Debug.print("WARNING: Untimed Annotation Elements found. This will be slower.", Debug.WARNING);
			timedannotations = false;
			c = new SearchResultPositionComparator();
		} else {
		    Debug.print("Annotations are timed.", Debug.PROGRAMMER);
			timedannotations = true;
			c = mycomp;
		}
		
		while(it.hasNext()) {
			List li = (List)(it.next());
			NOMElement el = (NOMElement)(li.get(0));
			Style s = mvds.getAnnotatorStyle(el);
			if(s == null) {
				it.remove();
			} else if (s == mvds.getdualstyle()) {
				ann1elements.add(li);
				ann2elements.add(li);
			} else if (s == mvds.getann1style()) {
				ann1elements.add(li);
			} else if (s == mvds.getann2style()) {
				ann2elements.add(li);
			} 
		}
		Debug.print("Sorting Elements...\n", Debug.PROGRAMMER);
		
		Collections.sort(annelements, c);
		Collections.sort(ann1elements, c);
		Collections.sort(ann2elements, c);
		
		if (annelements.size() == 0) {
			System.err.println("ERROR: No Valid Annotation Elements found.");
		}
		
		
	}
	
	private void showTree(NOMElement source_element, NTreeNode tree_node) {
		
		if (source_element == null) {
			return;
		}
		NTreeNode newnode =
			new NTreeNode(source_element.getAttribute("name").getStringValue());
		tree_node.addNode(newnode);
		if (source_element.getChildren() == null) {
			return;
		}
		for (Iterator kit = source_element.getChildren().iterator();
		kit.hasNext();
		) {
			NOMElement kid = (NOMElement) kit.next();
			showTree(kid, newnode);
		}
	}
	
	/**
	 * We want to load one observation worth of data; return the
	 * observation with the name
	 * @param observation.  Choke if there isn't one.  */
	private NiteObservation observationWithName(String observation) {
		boolean found = false;
		NiteObservation returnval = null;
		List obs = controlData.getObservations();
		Iterator obs_it = obs.iterator();
		while ((obs_it.hasNext()) && (found == false)) {
			NiteObservation next = (NiteObservation) obs_it.next();
			if (observation.equals(next.getShortName()) == true) {
				found = true;
				returnval = next;
			};
		}
		if (found == false) {
			System.err.println(
					"Observation named "
					+ observation
					+ " doesn't exist:  exiting...");
			System.exit(0);
		}
		return returnval;
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
	 * This method should be different for every tool version.
	 * Get relevant actions from relevant editor modules, put them in central action map, create appropriate inputmaps...
	 */    
	private void setupInputMaps() {
		globalImap  = new ComponentInputMap(getNTV());
		globalAmap = new ActionMap();
		
		//globalAmap.put(DELETE_NE_ACTION, getActionMap().get(DELETE_NE_ACTION));
		//globalImap.put(KeyStroke.getKeyStroke("DELETE"), DELETE_NE_ACTION);
		
		getNTV().setInputMap(JComponent.WHEN_FOCUSED,globalImap);
		getNTV().setActionMap(globalAmap);
		
		desktop.setActionMap(globalAmap);
		desktop.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, globalImap);
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
	
	/** expand all nodes in the tree - should go in
	 * OntologyTreeView but that is still under construction (jonathan
	 * 7.3.5) */
	private void expandTree(JTree jt) {
		for (int i=0; i<jt.getRowCount(); i++) {
			jt.expandRow(i);
		}
	}
	
	private JTree jt;
	
	/** 
	 * Show the screen objects
	 */
	protected void addOntology(int x, int y, int width, int height) {
		// TODO: How do we handle null ontology? - return null?
		JPanel pan = new JPanel();
		JInternalFrame jif = new JInternalFrame("Ontology Tree", true, false, true, true);
		//final JTree jt = OntologyTreeView.getOntologyTreeView( getCorpus(), ((NECoderConfig)config).getNEDisplayAttribute(),((NECoderConfig)config).getNETypeRoot());
		
		setupInputMaps();
		HashMap hm = addToInputMaps((NOMTypeElement)nom.getElementByID(((NECoderConfig)config).getNETypeRoot()));
		
		NECoderConfig ncc = (NECoderConfig)config;
		String dat = ncc.getNEAttributeName();
		
		if (dat==null || dat.length()==0) {
			jt = OntologyTreeView.getOntologyTreeView( nom, ncc.getNEDisplayAttribute(), ncc.getNETypeRoot(), hm, OntologyTreeView.SHOWKEYS_LABEL);
		} else {
			jt = EnumerationTreeView.getEnumerationTreeView(nom, ncc.getNEElementName(), ncc.getNEAttributeName(), hm, OntologyTreeView.SHOWKEYS_LABEL);
		}
		if(jt == null)
		{
			return;
		}
		
		TreeSelectionModel tsm = new DefaultTreeSelectionModel();
		tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		jt.setSelectionModel(tsm);
		expandTree(jt);
		
		pan.add(jt);
		
		treepane = new JScrollPane(jt);
		jif.getContentPane().add(treepane);
		//jif.getContentPane().add(jt);
		jif.setVisible(true);                    
		jif.setSize(width, height);
		jif.setLocation(x,y);
		desktop.add(jif);
		
	}
	
	private void addLegend(int x, int y, int width, int height, String ann1name, String ann2name)
	{
		// Create an internal ds not linked to any elements so we can recover styles
		legend = new NTranscriptionView();
		
		MergedViewDisplayStrategy mvds = new MergedViewDisplayStrategy((NTranscriptionView)(legend), "", "", "", null, "", ann1name, ann2name);
		//legend = new NTextArea();
		
		iframe =
			new JInternalFrame("Annotator Legend", true, false, true, true);
		iframe.setSize(new Dimension(width, height));
		iframe.setLocation(new Point(x, y));

		String ann1displayname = ann1name;
		String ann2displayname = ann2name;
		
		if ((showannotators & 0x01) == 0) {
			ann1displayname = ann1name + " (hidden)";
		}
		if ((showannotators & 0x02) == 0) {
			ann2displayname = ann2name + " (hidden)";
		}
		
		legend.addElement(new NTextElement(ann1displayname, mvds.getann1style().getName()));
		legend.addElement(new NTextElement("\n", null));
		legend.addElement(new NTextElement(ann2displayname, mvds.getann2style().getName()));
		
		/* top (or left) of the desktop area, containing the text */
		legendpane = new JScrollPane(legend);
		iframe.getContentPane().add(legendpane);
		iframe.setVisible(true);
		desktop.add(iframe);
	}
	
	private void refreshLegend() {
		// Create an internal ds not linked to any elements so we can recover styles
		legend.clear(); // = new NTranscriptionView();
		
		MergedViewDisplayStrategy mvds = new MergedViewDisplayStrategy((NTranscriptionView)(legend), "", "", "", null, "", autoname, manualname);
		
		String ann1displayname = autoname;
		String ann2displayname = manualname;
		
		if ((showannotators & 0x01) == 0) {
			ann1displayname = autoname + " (hidden)";
		}
		if ((showannotators & 0x02) == 0) {
			ann2displayname = manualname + " (hidden)";
		}
		
		legend.addElement(new NTextElement(ann1displayname, mvds.getann1style().getName()));
		legend.addElement(new NTextElement("\n", null));
		legend.addElement(new NTextElement(ann2displayname, mvds.getann2style().getName()));
		
	}
	//TODO: Optget this
	
	/**
	 * Called to start the application.
	 * Required command line arguments are:
	 *<ul>
	 *<li> <i>-c</i> | -corpus corpus </li>
	 *<li> <i>-o</i> | -observation observation </li>
	 *<li> <i>-cf</i> | -config config-filename </li>
	 *<li> <i>-gs</i> | -gui-settings gui-settings </li>
	 *<li> <i>-cs</i> | -corpus-settings corpus-settings </li>
	 *<li> <i>-a1</i> | -ann1-name annotator-name-1 </li>
	 *<li> <i>-a2</i> | -ann2-name annotator-name-2 </li>
	 *<li> <i>-s</i> | -show none|1|2|both </li> 
	 *</ul>
	 *
	 */
	public static void main(String[] args) {
		String corpus=null;
		String observation=null;
		String configfile=null;
		String guisettings=null;
		String corpussettings=null;
		String autoname=null;
		String manualname=null;
		int showannotators=3;
		
		if (args.length < 2) { usage(); }
		for (int i=0; i<args.length; i++) {
			String flag=args[i];
			if (flag.equals("-corpus") || flag.equals("-c")) {
				i++; if (i>=args.length) { usage(); }
				corpus=args[i];
			} else if (flag.equals("-observation") || flag.equals("-o")) {
				i++; if (i>=args.length) { usage(); }
				observation=args[i];
			} else if (flag.equals("-config") || flag.equals("-cf")) {
				i++; if (i>=args.length) { usage(); }
				configfile=args[i];
			} else if (flag.equals("-gui-settings") || flag.equals("-gs")) {
				i++; if (i>=args.length) { usage(); }
				guisettings=args[i];
			} else if (flag.equals("-corpus-settings") || flag.equals("-cs")) {
				i++; if (i>=args.length) { usage(); }
				corpussettings=args[i];
			} else if (flag.equals("-ann1-name") || flag.equals("-a1")) {
				i++; if (i>=args.length) { usage(); }
				autoname=args[i];
			} else if (flag.equals("-ann2-name") || flag.equals("-a2")) {
				i++; if (i>=args.length) { usage(); }
				manualname=args[i];
			} else if (flag.equals("-show") || flag.equals("-s")) {
				i++; if (i>=args.length) { usage(); }
				String ann = args[i];
				if (ann.equals("both")) {
					showannotators = 3;
				} else if (ann.equals("2")) {
					showannotators = 2;
				} else if (ann.equals("1")) {
					showannotators = 1;
				} else {
					showannotators = 0;
				}
			} else {
				usage();
			}
		}
		if (corpus == null || configfile == null || autoname == null || manualname == null) { System.err.println("Invalid arguments: " + args); usage(); }
		
		NonSpanningComparisonDisplay ne = new NonSpanningComparisonDisplay(corpus, observation, configfile, guisettings, corpussettings, autoname, manualname, showannotators);
	}
	
	protected static void usage() {
		System.err.println("Usage: java NonSpanningComparisonDisplay -c metadata-filename -o observation-name -cf config-filename -gs gui-settings -cs corpus-settings -a1 auto-annotator-name -a2 manual-annotator-name");
		System.exit(0);
	}
	
	/** A very simple "exit" action */
	public class ExitAction extends AbstractAction {
		public ExitAction() {
			super("Exit");
		}
		public void actionPerformed(ActionEvent ev) {
			System.exit(0);
		}
	}
	
	/** A "search" action - pops up a search GUI */
	public class SearchAction extends AbstractAction {
		public SearchAction() {
			super("Search...");
		}
		public void actionPerformed(ActionEvent ev) {
			search.popupSearchWindow();
		}
	}
	
	public class ViewAction extends AbstractAction {
		//TODO: could store NavigationList here if we want to sacrifice memory for speed
		private int annshow;
		public ViewAction(int show, String text) {
			super("View " + text);
			annshow = show;
		}
		public void actionPerformed(ActionEvent ev) {
			showannotators = annshow;
			//initTranscriptionViewSettings();
			mvds.setannotatordisplay(showannotators);
			refreshTranscriptionView();
			refreshNavigationList();
			refreshLegend();
		}
		
	}
	
	/**
	 * A utility method that returns the NTextElements (screen elements in a NTranscriptionView)
	 * for the given annotation element. 
	 * <p>
	 * Uses a call to getTranscriptionDescendants to determine the transcription NOMElements,
	 * then finds the corresponding NTextElements.
	 */
	protected Set getTextElements(NOMElement element) {
		TransToAnnoMap transToAnnoMap = new DefaultTransToAnnoMap(getNTV());
		Set result = new LinkedHashSet();
		Set transcriptionDesc = transToAnnoMap.getTransElementsForAnnotationElement(element); 
		Iterator allDescsIt = transcriptionDesc.iterator();
		while (allDescsIt.hasNext()) {
			NOMElement nme = (NOMElement)allDescsIt.next();
			NOMObjectModelElement nome = new NOMObjectModelElement(nme);
			Set s = ntv.getTextElements(nome);
			if (s != null)
				result.addAll(s);
		}
		return result;
	}

	protected void mapElementAdd(NOMElement e) {
		int firstpos = -1;
		int lastpos = -1;

		Set s = getTextElements(e);
		
		Iterator i = s.iterator();
		NTextElement t;
		
		while (i.hasNext()) {
			t = (NTextElement)i.next();
			if ((t.getPosition() < firstpos) || (firstpos < 0)) {
				firstpos = t.getPosition();
			}
			if ( (t.getPosition()+t.getText().length() > lastpos) ) {
				lastpos = t.getPosition() + t.getText().length();
			}
		}
		
		mapElementStart.put(e, new Integer(firstpos));
		mapElementEnd.put(e, new Integer(lastpos));
		
	}
	
	protected int getElementStart(NOMElement e) {
		if(!mapElementStart.containsKey(e)) {
			mapElementAdd(e);
		}
		return ((Integer)(mapElementStart.get(e))).intValue();
	}
	
	protected int getElementEnd(NOMElement e) {
		if(!mapElementEnd.containsKey(e)) {
			mapElementAdd(e);
		}
		return ((Integer)(mapElementEnd.get(e))).intValue();
	}
	
	// Based on SearchResultTimeComparator
	public class SearchResultPositionComparator implements Comparator {
		
		public int compare(Object obj, Object obj1) {
			if (obj==null && obj1==null) { return 0; }
			else if (obj==null)  { return -1; } 
			else if (obj1==null)  { return 1; }
			try {
				// these are search results so it's a list of elements and we just know
				// to choose the first result
				List l1 = (List)obj;
				List l2 = (List)obj1;
				NOMElement nelement1 = (NOMElement) l1.get(0);
				NOMElement nelement2 = (NOMElement) l2.get(0);
				int s1 = getElementStart(nelement1);
				int s2 = getElementEnd(nelement2);
				return (new Integer(s1)).compareTo(new Integer(s2));
			} catch (ClassCastException cce) { 
				// This happens if you fail to remove the first list
				// element from a search because you get strings in search
				// results for the variables!
				return -1;
			}
		}
	}
	
	protected boolean selectElement(NOMElement e) {
		int firstpos = -1;
		int lastpos = 0;
		Set s = getTextElements(e);
		
		Iterator i = s.iterator();
		NTextElement t;
		
		while (i.hasNext()) {
			t = (NTextElement)i.next();
			if ((t.getPosition() < firstpos) || (firstpos < 0)) {
				firstpos = t.getPosition();
			}
			if (t.getPosition() + t.getText().length() > lastpos) {
				lastpos = t.getPosition() + t.getText().length();
			}
		}
		
		if (firstpos < 0) {
			return false;
		} else {
			getNTV().newSelection(firstpos, lastpos);
			return true;
		}
	}
	
	private List selectAnnotationList() {
		String annname = (String)navigationType.getSelectedItem();
		if (annname.equals(autoname)) {
			return ann1elements;
		} else if (annname.equals(manualname)) {
			return ann2elements;
		} else {
			return annelements;
		}
		
	}
	
	/** A "next annotation" action */
	public class NextAnnAction extends AbstractAction {
		public NextAnnAction() {
			super("Next Annotation >>");
		}
		public void actionPerformed(ActionEvent ev) {
			Debug.print("Next Annotation...\n", Debug.PROGRAMMER);
			if (selectAnnotationList().size() == 0) {
				Debug.print("No annotations.\n", Debug.PROGRAMMER);
				return;
			}
			Iterator annsit = selectAnnotationList().iterator();
			double t = getNTV().getClock().getSystemTime();
			Set s = getNTV().getSelectedTransElements();
			//System.out.println("Current set: "+s.size()+" elements.");
			Debug.print("Current set: "+s.size()+" elements.", Debug.PROGRAMMER);
			NOMElement n = findNextAnnotation(0, annsit);
			
			// TODO: The problem with the timed approach is that when multiple speakers are active, the next annotation in time might be ABOVE the selection in the transcription. How do we solve this?
			if (s.size() > 0) {
				NOMElement f = (NOMElement)(s.iterator().next());
				if (timedannotations) {
				    Debug.print("First element: " + f.getID() + " [" + f.getStartTime() + ", " + f.getEndTime() + "].", Debug.PROGRAMMER); 
					n = findNextAnnotation(f.getEndTime(), annsit);
				} else {
				    Debug.print("First element: " + f.getID() + " <" + getElementStart(f) + ", " + getElementEnd(f) + ">.", Debug.PROGRAMMER);
					n = findNextAnnotation(getElementEnd(f), annsit);
				}
			} else {
				n = findNextAnnotation(t, annsit); 
			}
			
			Debug.print("Current time: " + t + ". Next ann = " + n.getID() + " [" + n.getStartTime() + ", " + n.getEndTime() + "] <" + getElementStart(n) + ", " + getElementEnd(n) + ">.", Debug.PROGRAMMER);
			
			if(!selectElement(n)) {
				Debug.print("Cannot select element.", Debug.WARNING);
			}
		}
	}
	
	/** A "previous annotation" action */
	public class PrevAnnAction extends AbstractAction {
		public PrevAnnAction() {
			super("<< Previous Annotation");
		}
		public void actionPerformed(ActionEvent ev) {
			Debug.print("Previous Annotation...\n", Debug.PROGRAMMER);
			if (selectAnnotationList().size() == 0) {
				Debug.print("No annotations.\n", Debug.PROGRAMMER);
				return;
			}
			List anns = selectAnnotationList();
			ListIterator annsit = anns.listIterator(anns.size());
			double t = getNTV().getClock().getSystemTime();
			Set s = getNTV().getSelectedTransElements();
			Debug.print("Current set: "+s.size()+" elements.", Debug.PROGRAMMER);
			NOMElement n = findPreviousAnnotation(getNTV().getDocument().getLength() + 1, annsit);
			
			// TODO: The problem with the timed approach is that when multiple speakers are active, the next annotation in time might be ABOVE the selection in the transcription. How do we solve this?
			if (s.size() > 0) {
				NOMElement f = (NOMElement)(s.iterator().next());
				Debug.print("First element: " + f.getID() + " [" + f.getStartTime() + ", " + f.getEndTime() + "].", Debug.PROGRAMMER); 
				if (timedannotations) {
					n = findPreviousAnnotation(f.getStartTime(), annsit);
				} else {
					n = findPreviousAnnotation(getElementStart(f), annsit);
				}
			} else {
				n = findPreviousAnnotation(t, annsit); 
			}
			
			Debug.print("Current time: " + t + ". Prev ann = " + n.getID() + " [" + n.getStartTime() + ", " + n.getEndTime() + "].", Debug.PROGRAMMER);
			
			if(!selectElement(n)) {
				Debug.print("Cannot select element.", Debug.WARNING);
			}
		}
	}
	
	/**
	 * <p>Action for the tree elements, when a button is clicked.</p>
	 */
	private class TargetAction extends AbstractAction {
		private NOMElement target;
		
		public TargetAction(String label, NOMElement target) {
			super();
			this.target = target;
		}
		
		public void actionPerformed(ActionEvent e) {
		    Debug.print(target.getID(), Debug.PROGRAMMER);
		}
	}
	
}
