package net.sourceforge.nite.datainspection.timespan;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;
import net.sourceforge.nite.datainspection.impl.*;
import net.sourceforge.nite.datainspection.view.*;


//NXT imports
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.util.*;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.nom.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.nom.nomread.impl.*;
import net.sourceforge.nite.time.*;
import net.sourceforge.nite.search.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.query .*;

//general java imports
import java.net.URL;
import java.awt.image.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.*;
import java.awt.*;
import java.io.*;
import org.xml.sax.SAXException;

//look and feels
import java.awt.Toolkit;
import javax.swing.*;
import com.jgoodies.looks.plastic.*;
import com.jgoodies.looks.plastic.theme.*;
import com.jgoodies.looks.*;



/**
 * Inspection tool for timeline segmentations (gapped or non gapped) that investigates whether
 * two annotators identified same segment boundaries.
 *
 * DON'T USE THIS TOOL! See the package documentation for a reason why not...
 * 
 * This version uses the classifications produced by the DiscretizationBasedBoundaryToClassificationFactory
 * (yes, I know, the name is awful...)
 */
public class BoundaryBasedInspection2 {


    public BoundaryBasedInspection2 (
                       String c, /* corpus */
                       String o, /* observation */
                       String codingName,          //the name of the Coding in which the boundaries are to be found
                       String segmentsLayer,       //the name of the Layer in that Coding in which the boundaries are to be found
                       String segmentElementName,  //the name of the Elements in the Layer in that Coding in which the boundaries are to be found
                       String agentName,           //the name of the agent for which you want to analyse the annotations. If you want to analyse 
                                                   //an interaction coding, this parameter should be null
                       double thMin,               //thMin, thMax and thSteps together determine which threshold variations will be used to attempt 
                       double thMax,               //to align two boundary annotations.
                       int    thSteps
                      ) {
        //store params
        this.corpusName = c;
        this.observationName = o;
        this.codingName=codingName;
        this.segmentsLayer=segmentsLayer;
        this.segmentElementName=segmentElementName;
        this.agentName=agentName;

        initializeCorpus();
        
        //calculations
        collectBoundaries();
        collectClassifications(thMin, thMax, thSteps);
        collectMatrices();
        
        //reporting
        
        System.out.println("Done");
    }


    /*===========================================================
                CALCULATIONS: data structures
                ================================================*/

/*====General stuff====*/

    /** all annotaters in the corpus */
    List allAnnotators;
    /** all annotaters that have annotations of the requested layer/observation/agent */
    List allUsedAnnotators = new ArrayList();
    /** number of annotators that have annotations of the requested layer/observation/agent */
    int actualNoOfAnn =0;
    /** key: annotatorname. value: List of boundaries. sorted on starttime. */
    Map boundaryLists = new HashMap(); 
    /** All Pairs of relevant annotator names (those that have annotations) */
    Set allAnnotatorPairs = new HashSet();

/*====The classifications====*/
    
    /** A list of Doubles, the list of sizes of segments in which the timeline is to be discretized. Classifications
    and reliability reports will be produced for each threshold value in this list. The list is filled based on the 
    thMin, thMax and thSteps values passed to the BoundaryBasedInspection2 constructor. */
    ArrayList thresholdValues = new ArrayList();
    /** The Item lists for all segment sizes. */
    Map itemLists = new HashMap();
    /** The Classifications derived from the boundarylists. 
    Key: annotator name. Value: a map of classifications, keyed by the threshold value 
    with which the timeline was discretized. */
    Map classifications = new HashMap(); 
    
/*====For the reliability calculations====*/

    /** Keys: pairs of annotatornames. values: Maps of CoincidenceMatrix for the derived Boundary classifications
    for those two annotators, keyed by used threshold value. */
    Map boundaryCoincidenceMatrices = new HashMap(); 
    /** Same as boundaryCoincidenceMatrices, but for confusions instead of coincidences */
    Map boundaryConfusionMatrices = new HashMap(); 
    
    /*===========================================================
                CALCULATIONS: methods
                ================================================*/

    /** Collect the derived Boundary annotations for all annotators. Also initializes allAnnotatorPairs,allUsedAnnotators,actualNoOfAnn. */
    public void collectBoundaries() {
        //collect all coders
        allAnnotators = allAnnotatorsForAllCodings();
        //find boundaries
        for (int i = 0; i < allAnnotators.size(); i++) { //for each potentially relevant annotator
            String nextName = (String)allAnnotators.get(i);
            ArrayList boundaryList = BoundaryExtractor.extractBoundaries(getCorpus(),nextName,codingName,segmentsLayer,segmentElementName,agentName);
            if (boundaryList.size() > 0) {
                allUsedAnnotators.add(nextName);
                actualNoOfAnn++;
                boundaryLists.put(nextName,boundaryList);
                System.out.println("Number of boundaries for "+ nextName + ": " + boundaryList.size());
            } else {
                System.out.println("No boundaries for "+ nextName);
            }
        }
        //collect pairs of actually relevant coders
        collectAnnotatorPairs();
    }
    
    /** Collect for all threshold values the derived classifications for all annotators, using the 
    DiscretizationBasedBoundaryToClassificationFactory. */
    public void collectClassifications(double thMin,double thMax,int thSteps) {
        //first discretize the timeline
        generateThresholdValues(thMin,thMax,thSteps);
        discretizeTimeline();

        for (int i = 0; i < allUsedAnnotators.size(); i++) { //for each relevant annotator
            String nextName = (String)allUsedAnnotators.get(i);
            System.out.println("Deriving classifications for " + nextName);
            ArrayList boundaryList = (ArrayList)boundaryLists.get(nextName);
            //for all segmentation sizes
            
            //init classif map for this annotator
            Map classificationsMap = new HashMap();
            classifications.put(nextName,classificationsMap);
            
            //for each threshold value
            for (int t = 0; t < thresholdValues.size(); t++) {
                //System.out.println("threshold " + ((Double)thresholdValues.get(t)).doubleValue());
                //generate classifications from boundarylist
                List itemList = (List)itemLists.get(thresholdValues.get(t));
                Classification c = DiscretizationBasedBoundaryToClassificationFactory.makeClassificationsFromBoundaryList(boundaryList,itemList);
                classificationsMap.put(thresholdValues.get(t),c);
                System.out.println("Nr (false,true): (" + c.nrOfValues(new BooleanValue(false)) + "," + c.nrOfValues(new BooleanValue(true))+ ") for threshold " + ((Double)thresholdValues.get(t)).doubleValue());
            }
        }
    }

    /** Collect for all Classification pairs the confusion and coincidence matrices used for calculation of reliability and
    inspection of confusions. */
    public void collectMatrices() {
        Iterator pairIt = allAnnotatorPairs.iterator();
        //for all pairs of actual annotators for this observation/layer: 
        while (pairIt.hasNext()) {
            Pair nextPair = (Pair)pairIt.next();
            String ann1 = (String)nextPair.o1;
            String ann2 = (String)nextPair.o2;

            System.out.println("====\n=Deriving matrices for " + ann1 + " vs " + ann2);
            
            //get classificationPairs map for this pair
            Map classifications1 = (Map)classifications.get(ann1);
            Map classifications2 = (Map)classifications.get(ann2);
            
            //for each threshold value
            for (int i = 0; i < thresholdValues.size(); i++) {
                //generate matrices from classifications 
                Classification c1 = (Classification)classifications1.get(thresholdValues.get(i));
                Classification c2 = (Classification)classifications2.get(thresholdValues.get(i));
                CoincidenceMatrix coincM = new CoincidenceMatrix(c1,c2);
                ConfusionMatrix confM = new ConfusionMatrix(c1,c2);
                //if (coincM.alphaNominal(new BooleanMetric())>0) {
                    //System.out.println("==============================================================");
                //}
                //
                System.out.println("-Alpha = " + coincM.alphaNominal(new BooleanMetric()) + " for seg size " + ((Double)thresholdValues.get(i)).doubleValue());
                //System.out.println("-Matrix:\n" + confM.toString());
                //System.out.println("-Confusion matrix: (f,f) / (t,t): " + confM.entry(0,0) + " / " + confM.entry(1,1) + "=" + (confM.entry(0,0)/confM.entry(1,1)));
            }
        }
    }
    
    /*===========================================================
      Attributes to store Corpus and layer info and GUI elements
                ================================================*/
    
    /** corpus */
    public String corpusName;
    /** observation */
    public String observationName;
    /** the name of the Coding in which the boundaries are to be found */
    public String codingName;
    /** the name of the Layer in that Coding in which the boundaries are to be found */
    public String segmentsLayer;
    /** the name of the Elements in the Layer in that Coding in which the boundaries are to be found */
    public String segmentElementName; 
    /** the name of the agent for which you want to analyse the annotations. If you want to analyse 
        an interaction coding, this parameter should be null */
    public String agentName;

    public String getCorpusName() {
        return corpusName;
    }
    public String getObservationName() {
        return observationName;
    }

    /* The corpus */
    NOMWriteCorpus nom;
    public NOMWriteCorpus getCorpus() {
        return nom;
    }
    /* The corpus meta data */
    NiteMetaData metadata;
    public NiteMetaData getMetaData() {
        return metadata;
    }
    /* Video and synchronization clock */
    Clock niteclock;
    public Clock getClock() {
        return niteclock;
    }
    /* GUI */
    JFrame mainframe = null;
    protected JFrame getMainFrame() {
        return mainframe;
    }                
    JDesktopPane desktop;
    protected JDesktopPane getDesktop() {
        return desktop;
    }        
    protected ClockFace clockFace = null;
    /**
     * The search window which is used, not internall, but externally for the user.
     */
    protected net.sourceforge.nite.search.GUI searchGui;
    protected JMenuBar mainMenuBar; //any reason to store this?
    /** 
     * Returns the clockface of this class. A clockface is the 'play control panel'
     * for the video and audio, and in this tool for the virtual meeting animation as well.
     * Before you initialize this, you must have at least a mainframe, a desktop and an initialized 
     * corpus.
     */
    public ClockFace getClockFace() {
        //return clockFace;
        return (ClockFace)niteclock.getDisplay();
    }


    /*===========================================================
                INIT: load corpus etc
                ================================================*/

    /**
     * Loads the corpus (given the name and observationname etc).
     * DEPENDENT ON INFO SUCH AS WHAT LAYERS...
     * Post: metadata defined, nom defined, corpusname defined, observationname defined, clock defined
     */
    protected final void initializeCorpus() {
    	try {
    	    metadata = new NiteMetaData(getCorpusName());
    	    System.out.println("Metadata loaded");
    	} catch (NiteMetaException nme) {
    	    nme.printStackTrace();
    	    throw new RuntimeException("Can't initialize metadata");
    	}
    	niteclock = new DefaultClock(metadata, getObservationName());
    	//niteclock.registerTimeHandler(this);
    	if (metadata.getCorpusType() == NMetaData.STANDOFF_CORPUS) {
    	    try {
        		nom = new NOMReadCorpus(metadata);
        		NObservation obs = metadata.getObservationWithName(getObservationName());
        		nom.setLazyLoading(false);
        		NLayer toplay  = metadata.getLayerByName(segmentsLayer); 
        		NLayer commlay = null;
        		String attributename="coder";
        		ArrayList one_obs_list = new ArrayList();
        	    one_obs_list.add(obs);
        	    nom.loadReliability(toplay, commlay, attributename, null, one_obs_list);
	            System.out.println("Corpus '" + getCorpusName() + "' loaded for observation '" + getObservationName() + "'.");
    	    } catch (NOMException nex) {
        		nex.printStackTrace();
    	        throw new RuntimeException("Error loading corpus");
    	    }
    	} else {
    	    throw new RuntimeException("This is a standalone or simple corpus: couldn't load NOM");
    	}
    }        

//=========================== some methods for finding the relevant annotators =============================================

    /** Called by collectBoundaries. This method collects the information about all <i>potentially</i> relevant annotators. It may 
    be that this method finds annotators that turn out not to have annotations for the relevant layer.
    In the method collectBoundaries, the variables allUsedAnnotators and actualNoOfAnn will be set based
    on for which annotators collected here boundaries can be derived. */
    private List allAnnotatorsForAllCodings() {
    	Set annames=new HashSet();
    	List codings = new ArrayList();
    	codings.add(getMetaData().getCodingByName(codingName));
    	for (Iterator cit=codings.iterator(); cit.hasNext(); ) {
    	    NCoding cod = (NCoding) cit.next();
    	    List ml = findCoderDirectories(cod.getPath());
    	    if (ml!=null) {
    		    annames.addAll(ml);
    	    }
    	}
    	return(new ArrayList(new Vector(annames)));
    }

    /** Called by collectBoundaries. Collects all pairs of annotators for which a boundary list could be derived. */
    private void collectAnnotatorPairs() {
        allAnnotatorPairs = new HashSet();
        for (int i = 0; i < allUsedAnnotators.size(); i++) {
            for (int j = i+1; j < allUsedAnnotators.size(); j++) {
                String ann1 = (String)allUsedAnnotators.get(i);
                String ann2 = (String)allUsedAnnotators.get(j);
                Pair nextPair = new Pair(ann1,ann2);
                allAnnotatorPairs.add(nextPair);
            }
        }
        System.out.println("Number of annotator pairs to be investigated: " + allAnnotatorPairs.size());
    }        


    public void generateThresholdValues(double thMin, double thMax, int thSteps) {
        double stepValue = (thMax-thMin)/(double)thSteps;
        for (int i = 0; i <= thSteps; i++) {
            thresholdValues.add(new Double(thMin+stepValue*(double)i));
        }
        System.out.println("Number of threshold values to be investigated: " + thresholdValues.size());
    }

    public void discretizeTimeline() {
        for (int i = 0; i < thresholdValues.size(); i++) {
            List itemList = DiscretizationBasedBoundaryToClassificationFactory.generateDiscretizedItems(getCorpus().getCorpusEndTime(), ((Double)thresholdValues.get(i)).doubleValue());
            itemLists.put(thresholdValues.get(i),itemList);
        }
    }

    /** given a path, find all the subdirectories with some xml files
     * in them and add those to the list (assuming the directory names
     * are in fact coder names). */ 
    private List findCoderDirectories(String path) {
    	ArrayList rl = new ArrayList();
    	File cd = new File(path);
    	if (!cd.isDirectory()) {
    	    return rl;
    	}
    	File[] files = cd.listFiles(new myfilter());
    	if (files.length==0) { return null; }
    	for (int i=0; i<files.length; i++) {
    	    rl.add(files[i].getName());
    	}
    	return rl;
    }

    class myfilter implements FileFilter {
    	public boolean accept (File f) {
    	    if (!f.isDirectory()) { return false; }
    	    File[] xlist = f.listFiles(new xmlfilter());
    	    if (xlist.length>0) { return true; }
    	    return false;
    	}
    }

    class xmlfilter implements FilenameFilter {
    	String xex=".xml";

    	public boolean accept (File dir, String name) {
    	    if ((name.length() - name.indexOf(xex)) == xex.length()) { return true; }
    	    return false;
    	}
    }    

//=========================== GUI INIT (frame, mediaplayer, desktop) =============================================

    /**
     * Setup main window. 
     * If mainframe already initialized, return
     */
    protected void checkMainFrame() {
        if (mainframe!=null)return;
        
        //Look and feel
        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        System.setProperty("sun.awt.noerasebackground","true");        
        try {
            PlasticLookAndFeel.setCurrentTheme(new DesertBlue());
            Options.setPopupDropShadowEnabled(true);
    		Options.setUseNarrowButtons(true);
    		Options.setUseSystemFonts(true);
            Options.setDefaultIconSize(new Dimension (16,16));
    		Plastic3DLookAndFeel olaLF = new Plastic3DLookAndFeel();
    		UIManager.setLookAndFeel(olaLF);
        } catch (Exception ex) {
            System.out.println("Error in LookaNdFeelSupport!");ex.printStackTrace();
        }
        
        //main frame
    	mainframe = new JFrame("Data reporter");

    	//add windows listener to the main frame in order to perform appropriate exit action
    	mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	
        //maximize
      	mainframe.setExtendedState(Frame.MAXIMIZED_BOTH);

    	mainframe.setSize(new Dimension(1000, 700));
    	mainframe.setVisible(true);
    	
    	//desktop
        desktop = new JDesktopPane();
    	desktop.setSize(new Dimension(1000, 700));
    	getMainFrame().getContentPane().add(desktop);   
    	niteclock.ensureVisible(new Rectangle(0,0,100,100), desktop); 	
    	
    	//search window..
        searchGui = new net.sourceforge.nite.search.GUI(nom);
        
        //Search action in menu
        AbstractAction act = new AbstractAction("Search") {
            public void actionPerformed(ActionEvent ev) {
                searchGui.popupSearchWindow();
            }
        };
        act.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK ));
        act.putValue(Action.MNEMONIC_KEY,new Integer(java.awt.event.KeyEvent.VK_S));

    	mainMenuBar = new JMenuBar();
    	getMainFrame().setJMenuBar(mainMenuBar);
        
        JMenu viewM = new JMenu("View");
    	mainMenuBar.add(viewM);
        viewM.add(new JMenuItem(act));
    }

//=========================== SEARCH =============================================


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

}