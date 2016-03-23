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
<h2><a name="timedisc">Timeline Discretization</a></h2>
PROGRAM FOR THIS ANALYSIS NOT YET COMMITTED TO CVS
<p>
Some annotations do not lend themselves well for the event-like interpretation underlying the Segment alignment inspection.
<br>[[EXAMPLE?]]<br>
For such annotations we developed an analysis based on the <i>amount of overlap</i> for different labels. In the (hypothetical) example fragment below, we encounter from left to right the following agreements and disagreements: disagr(red, green), disagr(blue, green), agr(blue,blue), disagr(blue,green), disagr(blue,red), agr(red,red). Note the difference with the segment alingment based inspection. In the segment alignment inspection the first blue segment of the two annotators would be seen as "perfect agreement for identification and label, with some disagreement about timing". In the timeline discretization analysis the short overlap between green and blue at the beginning is counted as a label disagreement.

<br>
<div  class=figure><img src="doc-files/timespan_fragment2.png"/><br>an hypothetical annotation fragment</div>

This analysis is based on a discretization of the timeline. The image below shows this discretization for a certain size 'th'. The small timeline segments are considered the Items in the reliability analysis; the Values are derived from the largest annotation element in this segment. For example, the first few labels for the small segments for the upper annotation are "red, red, blue, blue, blue, blue, ..." etc. The (dis)agreements between the annotators start then with "disagr(red, green), disagr(red, green), disagr(blue, green), agr(blue,blue), agr(blue,blue), agr(blue,blue), disagr(blue,green), ..." etc. Note that longer stretches of a certain (dis)agreement count more in the final outcome than the shorter stretches. Furthermore, setting 'th' too high means that small annotation elements will not be counted anymore.
<br>
<div  class=figure><img src="doc-files/timespan_fragment2_discretized.png"/><br>the annotation fragment discretized</div>

The documentation of the class {@link net.sourceforge.nite.datainspection.timespan.DiscretizedTimelineInspection} explains in detail how such inspection along these lines is supported by the packages, including an explanation how the calculation of kappa and alpha metrices is supported, and the automatic creation of confusion tables. It also explains how varying 'th' has an impact on the outcome of the reliability measures.
 */
public class DiscretizedTimelineInspection {


    public DiscretizedTimelineInspection (
                       String c, /* corpus */
                       String o, /* observation */
                       String codingName,          //the name of the Coding in which the boundaries are to be found
                       String segmentsLayer,       //the name of the Layer in that Coding in which the boundaries are to be found
                       String segmentElementName,  //the name of the Elements in the Layer in that Coding in which the boundaries are to be found
                       String agentName,           //the name of the agent for which you want to analyse the annotations. If you want to analyse 
                                                   //an interaction coding, this parameter should be null
                       NOMElementToTextDelegate segmentToText, 
                                                   //used to determine the color in the segmentation views
                       Predicate isIgnoreP,        //used to determine whether a segment is 'foreground' or 'background' (see package documentation)
                       NOMElementToValueDelegate segmentToValue, 
                                                   //used to get a Value for each Item (see the datainspection package documentation for the use of Values and Items)
                       DistanceMetric labelDistanceMetric,
                                                   //distance metric for calculation of alpha
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

        this.isIgnoreP = isIgnoreP;
        this.segmentToValue = segmentToValue;
        this.segmentToText = segmentToText;
        this.labelDistanceMetric = labelDistanceMetric;

        initializeCorpus();
        
        //calculations
        collectSegments();
        collectClassifications(thMin, thMax, thSteps);
        
        collectMatrices();
        
        //reporting
        //checkMainFrame();
        //renderSegments();
        //renderRelations();
        //drawLegend();
        
        //report confusions
        //showConfusionTables();
        
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
    /** All Pairs of relevant annotator names (those that have annotations) */
    Set allAnnotatorPairs = new HashSet();

    /** key: annotatorname. value: List of segments. sorted on starttime. */
    Map segmentLists = new HashMap(); 

    Predicate isIgnoreP = null;
    NOMElementToValueDelegate segmentToValue = null;
    NOMElementToTextDelegate segmentToText = null;
    DistanceMetric labelDistanceMetric = null;

/*====The alignments and classifications====*/
    
    /** A list of Doubles, all threshold values that should be used for attempting to align segments
    annotations. Alignments (and derived classifications, and reliability reports) will be produced for 
    each threshold value in this list. The list is filled based on the thMin, thMax and thSteps values
    passed to the SegmentBasedInspection constructor. */
    ArrayList thresholdValues = new ArrayList();
    /** The Item lists for all segment sizes. */
    Map itemLists = new HashMap();
    /** The Classifications derived from the segmentlists. 
    Key: annotator name. Value: a map of classifications, keyed by the threshold value 
    with which the timeline was discretized. */
    Map classifications = new HashMap(); 
    
/*====For the reliability calculations====*/

    /** Keys: pairs of annotatornames. values: Maps of CoincidenceMatrix for the derived segment classifications
    for those two annotators, keyed by used threshold value. */
    Map segmentCoincidenceMatrices = new HashMap(); 
    /** Same as segmentCoincidenceMatrices, but for confusions instead of coincidences */
    Map segmentConfusionMatrices = new HashMap(); 
    
    /*===========================================================
                CALCULATIONS: methods
                ================================================*/

    /** Collect the segments for all annotators. Also initializes allAnnotatorPairs,allUsedAnnotators,actualNoOfAnn. */
    public void collectSegments() {
        //collect all coders
        allAnnotators = allAnnotatorsForAllCodings();
        //find segments
        for (int i = 0; i < allAnnotators.size(); i++) { //for each potentially relevant annotator
            String nextName = (String)allAnnotators.get(i);
            ArrayList segmentList = SegmentExtractor.extractSegments(getCorpus(),nextName,codingName,segmentsLayer,segmentElementName,agentName,isIgnoreP);
            if (segmentList.size() > 0) {
                allUsedAnnotators.add(nextName);
                actualNoOfAnn++;
                segmentLists.put(nextName,segmentList);
                System.out.println("Number of segments for "+ nextName + ": " + segmentList.size());
            } else {
                System.out.println("No segments for "+ nextName);
            }
        }
        //collect pairs of actually relevant coders
        collectAnnotatorPairs();
    }

   
    /** Collect for all threshold values the derived classifications for all annotators, using the 
    TimelineDiscretizationClassificationFactory. */
    public void collectClassifications(double thMin,double thMax,int thSteps) {
        //first discretize the timeline
        generateThresholdValues(thMin,thMax,thSteps);
        discretizeTimeline();

        for (int i = 0; i < allUsedAnnotators.size(); i++) { //for each relevant annotator
            String nextName = (String)allUsedAnnotators.get(i);
            System.out.println("Deriving classifications for " + nextName);
            ArrayList segmentList = (ArrayList)segmentLists.get(nextName);
            //for all segmentation sizes
            
            //init classif map for this annotator
            Map classificationsMap = new HashMap();
            classifications.put(nextName,classificationsMap);
            
            //for each threshold value
            for (int t = 0; t < thresholdValues.size(); t++) {
                //System.out.println("threshold " + ((Double)thresholdValues.get(t)).doubleValue());
                //generate classifications from boundarylist
                List itemList = (List)itemLists.get(thresholdValues.get(t));
                Classification c = TimelineDiscretizationClassificationFactory.makeClassification(itemList,segmentList,segmentToValue);
                classificationsMap.put(thresholdValues.get(t),c);
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
                            REPORTING
                ================================================*/

    /** The panel upon which several visualisations will be drawn (segments, alignments, etc... */
    TimespanReportPanel reportPanel;
    /** Horizontal resolution of reportPanel */
    final int milliSecsPerPixel = 200;
    /** Row height of reportPanel */
    int height = 25;

    public void initReportPanel() {    
        JInternalFrame jif = new JInternalFrame("All coders: "+getObservationName(), true, false, true, true);
        reportPanel = new TimespanReportPanel((BufferedImage)null,(int)((getCorpus().getCorpusEndTime()*1000d)/milliSecsPerPixel),height,getClock(),(ArrayList)allUsedAnnotators);
        searchGui.registerResultHandler(reportPanel) ;
        jif.getContentPane().add(new JScrollPane(reportPanel));
        JSlider js = new JSlider(1,1000,10);
        jif.getContentPane().add(js, "South");
        jif.setLocation(0,0);
        jif.setSize(600,40);
        jif.setVisible(true);
        getDesktop().add(jif);
        js.addChangeListener(new MyChangeListener(reportPanel,js));
    }
    public void renderSegments() {
        //
        initReportPanel();
        //for each relevant annotator
        for (int i = 0; i < allUsedAnnotators.size(); i++) { 
            String nextName = (String)allUsedAnnotators.get(i);
            ArrayList segmentList = (ArrayList)segmentLists.get(nextName);
            BufferedImage bim = AnnotatorRenderer.renderSegmentList(segmentList, height, milliSecsPerPixel,segmentToText);
            if (bim == null) {
                System.out.println("NULL IMAGE: "+ nextName);
            } else {
                reportPanel.addSubImage(nextName,bim);
            }
        }
        ArrayList l = new ArrayList();
        l.add("xsobol03");
        l.add("xpolok00");
        reportPanel.annotatorsToDraw = l;
    }


    ValueColourMapLegend legend;
    public void drawLegend() {
        //draw legend...
        legend = new ValueColourMapLegend(ValueColourMap.getGlobalColourMap());
        JInternalFrame jif = new JInternalFrame("Legend", true, false, true, true);
        jif.getContentPane().add(new JScrollPane(legend));
        jif.setLocation(200,200);
        jif.setSize(600,40);
        jif.setVisible(true);
        getDesktop().add(jif);
    }

    /**
     * Show the confusion tables in internal frames on the desktop
     */
    public void showConfusionTables() {
        Iterator pairIt = allAnnotatorPairs.iterator();
        //for all pairs of actual annotators for this observation/layer: 
        while (pairIt.hasNext()) {
            Pair nextPair = (Pair)pairIt.next();
            String ann1 = (String)nextPair.o1;
            String ann2 = (String)nextPair.o2;

            System.out.println("Deriving matrices for " + ann1 + " vs " + ann2);

            
            //get confusion matrices map for this pair
            Map confusionMatrices = (Map)segmentConfusionMatrices.get(nextPair);
            
            //for each threshold value
            for (int i = 0; i < thresholdValues.size(); i++) {
                ConfusionMatrix alignmentConfusionMatrix = (ConfusionMatrix)confusionMatrices.get(thresholdValues.get(i)); 
                getDesktop().add(ConfusionTablePanel.getConfusionPanelFrame("Alignment Conf: "+ann1+" vs "+ann2+", th="+thresholdValues.get(i), alignmentConfusionMatrix));
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

    /** Called by collectSegments. This method collects the information about all <i>potentially</i> relevant annotators. It may 
    be that this method finds annotators that turn out not to have annotations for the relevant layer.
    In the method collectSegments, the variables allUsedAnnotators and actualNoOfAnn will be set based
    on for which annotators collected here segments can be derived. */
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

    /** Called by collectSegments. Collects all pairs of annotators for which a segment list could be derived. */
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


    class NOMElementLengthComparator implements Comparator {
        public int compare (Object o1, Object o2) {
            NOMElement n1 = (NOMElement)o1;
            NOMElement n2 = (NOMElement)o2;
            double l1 = n1.getEndTime()-n1.getStartTime();
            double l2 = n2.getEndTime()-n2.getStartTime();
            return Double.compare(l1,l2);
        }
        
        public boolean equals(Object o) {
            return this==o;
        }
    }
    class MyChangeListener implements ChangeListener {
        TimespanReportPanel pan;
        JSlider js;
        public MyChangeListener (TimespanReportPanel p, JSlider s) {
            pan = p;
            js = s;
        }
        public void stateChanged(ChangeEvent e) {
            pan.setZoom(((double)js.getValue())/100d);
        }
    }    


}

