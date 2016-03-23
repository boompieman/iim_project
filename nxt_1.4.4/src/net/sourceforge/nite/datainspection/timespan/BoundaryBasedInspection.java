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
 * two annotators identified the same segment boundaries.
 * 
 * Boundaries detected by two annotators are taken to be the same if they are at most a (configurable) 
 * threshold 'th' apart. Kappa and alpha are calculated by giving a pair of such aligned boundaries
 * the label 'true' for both annotators, and giving unaligned boundaries the label 'true' for the 
 * annotator who detected the boundary and 'false' for the other.
 *
 *
<h2><a name="boundary">Boundary based inspection</a></h2>
<h3>Introduction</h3>
Recall the summary in the nite.datainspection package documentation: Data inspection consists of
<ol>
<li>finding out whether separate annotators identified the same Items (segments, units for labeling), 
<li>finding out whether comparable Items have been assigned the same Values (labels, classes, categories) by the annotators and 
<li>finding out where disagreement lies, i.e. what Values are confused with each other in what situations; what type of Items are most often NOT identified by both annotators at the same time; etc.
<li>(Investigating the nature of the errors that annotators made, and deciding how important these errors are, given the use for which the annotations were created.)
</ol>
The Boundary Based inspections are involved with one possible approach to step one: finding out whether separate annotators identified the same items or events in the data.
In this analysis, the focus is on detection whether two annotators identified a <i>change point</i> or <i>boundary</i> at the same moment, i.e. a start or end of a segment. This is a sensible approach to take if the detection of boundaries is explicitly defined as a separate step in the annotation instructions (e.g. "The task consists of two parts: first, defining "cuts" (segmentation points)
in the video of a person at places where you see a distinct change in the mental state of this person, and second, to fill in a form that describes each segment that is thus created.").  The two images below show how a segmentation of the timeline can be interpreted in terms of boundaries rather than segments.
<br><br>
<div class=figure><img  src="doc-files/timespan_fragment.png"/><br>an example 'timespan' annotation for two annotators</div>
<div class=figure><img  src="doc-files/boundary_fragment.png"/><br>an example derived 'boundary' annotation for two annotators</div>
<br>
The second image represents the lists of {@link net.sourceforge.nite.datainspection.timespan.Boundary Boundaries} extracted by the {@link net.sourceforge.nite.datainspection.timespan.BoundaryExtractor} for the two annotators. To interpret this in terms of 'whether the annotators identified the same boundaries', the relation between those derived boundary annotations must be analysed. The image below shows some relevant information. The red lines mark where two annotators found the same boundary; the blue dots mark boundaries found by only one annotator.
<br><br>
<div  class=figure><img  src="doc-files/boundary_alignment.png"/><br>the alignment of the derived boundaries.</div>
<br>

<h3>Boundary agreement: aligning boundaries</h3>
The alignment of the derived boundaries shown earlier can to a certain extent be calculated automatically. Precision of the alignment is determined by the threshold 'th': boundaries Ba and Bb annotated by annotators A and B can be seen as 'the same boundary' when they are at most 'th' time apart. The <i>percentage</i> of boundaries that have been aligned is a measure for how much two annotators agreed on the identification of boundaries. For the example alignment above:
<pre>
        Aligned: 14
        Unaligned A: 0 (100% aligned)
        Unaligned B: 4 (78% aligned)
</pre>
This information is reported by the class {@link net.sourceforge.nite.datainspection.timespan.BoundaryBasedInspection}. 
<h4>Precision</h4>
<p>Varying threshold 'th' gives information about the <i>precision</i> with which two annotators identified the same boundaries. Note however that 'th' should be low enough compared to the segment lengths in the annotations.
<pre>
        ================
        ===Aligning f vs e
        ---
        - th 0.0
        50 aligned boundaries 
         For ann1: 15%.
         For ann2: 18%.
        ---
        - th 0.2
        209 aligned boundaries 
         For ann1: 63%.
         For ann2: 78%.
        ---
        - th 0.4
        228 aligned boundaries 
         For ann1: 69%.
         For ann2: 85%.
        ---
        - th 0.6
        241 aligned boundaries 
         For ann1: 73%.
         For ann2: 90%.
        ---
        - th 0.8
        243 aligned boundaries 
         For ann1: 74%.
         For ann2: 91%.
        ---
        - th 1.0
        249 aligned boundaries 
         For ann1: 76%.
         For ann2: 93%.
</pre>
<p>The list above gives an example output obtained from the {@link net.sourceforge.nite.datainspection.timespan.BoundaryBasedInspection} tool, for the FOA layer of the AMI corpus, with several variations of 'th'. <b>For this specific annotation, 50% of the identified segments were shorter than 1 second.</b> This means that the higher alignment percentages in the list are at least hard to interpret, and in the worst case not really meaningful.
To gain more insight in these numbers, one should therefore have a look at the visualisation of the boundary alignments for different values of 'th', to see whether the alignments make sense or not. Also, one should relate the range of 'th' that is tried out to the distribution of segment lengths in the annotation.
<p>
The following three images, obtained for the same AMI FOA annotation, show how setting 'th' too low will cause the alignment percentages to be unfairly low, and setting 'th' too high will lead to unfairly high alignment percentages. These images can be produced using the tool {@link net.sourceforge.nite.datainspection.timespan.BoundaryBasedInspection}.
<div class=figure><img  src="doc-files/boundary_alignment_foa_th00.png"/><br>automatic alignment for a too low threshold.</div>
<div class=figure><img  src="doc-files/boundary_alignment_foa_th04.png"/><br>more adequate automatic alignment.</div>
<div class=figure><img width=200 height=50 src="doc-files/boundary_alignment_foa_th10.png"/><br>automatic alignment for a too high threshold: the threshold is higher than the average segment length.</div>
<p>
In the end, if you have been able to determine a sensible value for 'th', this analysis can lead to an answer to the question "what percentage of boundaries have been found by both annotators?" (In this particular case, the answer should be "about 65% for annotator 1 and about 85% for annotator 2").
Furthermore, the range of values for 'th' for which an acceptable alignment is found gives information about the precision with which annotators have annotated the data. In this case, this seems to be somewhere between 0.2 and 0.4 seconds. 

<p>(Side remark. Note that this has an implication for your use of the data. If the precision is on a few tenths of seconds, you should not focus on learning frame-perfect automatic boundary recognition. Furthermore, your evaluation of your machine learning results should keep this margin when assessing whether a detected boundary is 'good'.)

<h4>Trying to calculate alpha agreement for the alignment</h4>
Often, people are interested more in the <i>chance corrected</i> agreement than in the <i>percentual agreement</i>. One way to calculate such chance agreement for the alignment is to take every boundary detected by at least one annotator and take it as an Item. The Value then is True for the annotator who found the boundary, and False for the annotator who did not find the boundary, or True for both if they both found this boundary, as indicated by the alignment. However, this analysis cannot represent the fact that two annotators agree a lot on all the places where <i>no</i> boundary should be annotated, and therefore leads to ridiculously low alpha values, often even very negative. For the FOA annotations mentioned above, the resulting alpha values using this method will turn out to be somewhere between -0.2 and -0.8 for 

<!--<p>[[INCLUDE EXAMPLES OF THE ALPHA OBTAINED FOR DIFFERENT TRESHOLDS ON AMI FOA OR IA to support last point]] (they turn out to be between -0.1 and -0.9)-->
<!-- One solution, commented out for now because I have no intention of making it anytime soon, is to generate a lot of classifications of random boundaries with the same distribution, and then calculate the expected percentage of agreement... -->

<h3>Boundary agreement: timeline discretization</h3>
<!-- Class: {@link net.sourceforge.nite.datainspection.timespan.BoundaryBasedInspection2}.<p>-->

A possible solution to the problem that the 'alignment analysis' does not take into consideration the amount by which annotators agree that there is <i>no boundary</i> is to discretize the time line into short, equal-sized segments (length 'l'), and look for each annotator whether (s)he did note a boundary within that segment. The segment size 'l' is a measure of how much precision is required: a low 'l' means that boundaries are only seen as 'detected by both annotators' when the boundaries are very close together. This leads to a derived Classification where Items are each a short span on the timeline ({@link net.sourceforge.nite.datainspection.impl.TimespanItem}) and Values are True or False ({@link net.sourceforge.nite.datainspection.impl.BooleanValue}) depending on whether the given annotator has noted a boundary in that segment. These classifications can then be used to calculate {@link net.sourceforge.nite.datainspection.calc.CoincidenceMatrix CoincidenceMatrices} and standard reliability measures such as kappa or alpha.
<p>
<i>This is not a good idea!</i>
<p>The relative distributions of the labels true and false are extremely dependent on 'l': if 'l' gets smaller, the number of 'false' Values rises dramatically, whereas the number of 'true' Values is stable. This affects the outcome of the alpha and kappa values: they become higher when 'l' becomes smaller. On the other hand, smaller values for 'l' mean that less boundaries end up aligned (because a higher precision is required when 'l' is smaller), which means that kappa and alpha go down. And these two effects of course interact.
<p>
Conclusion: don't use that particular analysis (even though it was implemented in the class {@link net.sourceforge.nite.datainspection.timespan.BoundaryBasedInspection2}).

<h3>Boundary based inspection: Conclusion</h3>
All in all, the class {@link net.sourceforge.nite.datainspection.timespan.BoundaryBasedInspection} and its supporting classes provide support for determining a sensible threshold for boundary alignment as well as for performing this alignment. In the end, the percentual agreement on the occurrence of boundaries gives an indication of how often annotators find the same boundaries, and the determined threshold value gives an indication of how precise the timing of the annotations is.
 */
public class BoundaryBasedInspection {

    public BoundaryBasedInspection (
                       String c, /* corpus */
                       String o, /* observation */
                       String codingName,          //the name of the Coding in which the boundaries are to be found
                       String segmentsLayer,       //the name of the Layer in that Coding in which the boundaries are to be found
                       String commonLayer,         //the name of the common layer shared by all annotators, can be null
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
        this.commonLayer=commonLayer;
        this.segmentElementName=segmentElementName;
        this.agentName=agentName;

        initializeCorpus();
        
        //calculations
        collectBoundaries();
        collectAlignments(thMin, thMax, thSteps);
        
        //the next two steps are not really sensible to do: see package documentation
        //why you cannot use these alignments of _boundaries_ to derive an alpha agreement.
        //collectClassifications();
        //collectMatrices();
        
        //reporting
        checkMainFrame();
        renderBoundaries();
        renderRelations();
        System.out.println("Done");
    }

    public BoundaryBasedInspection (
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

        this(c, o, codingName, segmentsLayer, "", segmentElementName, agentName, thMin, thMax, thSteps);
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

    /** key: annotatorname. value: List of boundaries. sorted on starttime. */
    Map boundaryLists = new HashMap(); 

/*====The alignments and classifications====*/
    
    /** A list of Doubles, all threshold values that should be used for attempting to align boundary
    annotations. Alignments (and derived classifications, and reliability reports) will be produced for 
    each threshold value in this list. The list is filled based on the thMin, thMax and thSteps values
    passed to the BoundaryBasedInspection constructor. */
    ArrayList thresholdValues = new ArrayList();
    /** A Map of Maps of boundaryAlignments. Keys: pairs of annotatornames. values: for each two annotators,
    a Map of boundaryAlignments keyed by the threshold value with which each alignment was produced. */
    Map boundaryAlignments = new HashMap(); 
    /** The pairs of Classifications derived from the boundaryAlignments. Because the classifications
    for one author will be different when comparing with different other annotators, Pairs of 
    classifications (for all used threshold values) is stored here for each Pair of annotator names. 
    Key: pairs of annotator names. Value: Maps of Pairs of classifications, keyed by the threshold value 
    with which the alignments were produced. */
    Map classificationPairs = new HashMap(); 
    
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
    
    /** Collect for all threshold values the alignments for all annotator pairs, using the BoundaryAligner. */
    public void collectAlignments(double thMin, double thMax, int thSteps) {
        //first generate the threshold values...
        generateThresholdValues(thMin, thMax, thSteps);
        //then generate all alignments for all threshold values for all annotator pairs
       
        Iterator pairIt = allAnnotatorPairs.iterator();
        //for all pairs of actual annotators for this observation/layer: 
        while (pairIt.hasNext()) {
            Pair nextPair = (Pair)pairIt.next();
            String ann1 = (String)nextPair.o1;
            String ann2 = (String)nextPair.o2;
            System.out.println("========\n===Aligning " + ann1 + " vs " + ann2);
            ArrayList boundaryList1 = (ArrayList)boundaryLists.get(ann1);
            ArrayList boundaryList2 = (ArrayList)boundaryLists.get(ann2);
            
            //init boundaryAlignments map for this pair
            Map alignments = new HashMap();
            boundaryAlignments.put(nextPair,alignments);
            
            //for each threshold value
            for (int i = 0; i < thresholdValues.size(); i++) {
                //generate alignments
                BoundaryAlignment alignment = BoundaryAligner.alignBoundaries(boundaryList1, boundaryList2, ((Double)thresholdValues.get(i)).doubleValue());
                alignments.put(thresholdValues.get(i),alignment);
                System.out.println("---\n- th " + ((Double)thresholdValues.get(i)).doubleValue());
                System.out.println(alignment.alignedBoundaries.size() + " aligned boundaries ");
                double pct1 = (double)alignment.alignedBoundaries.size()/(double)(alignment.unalignedBoundaries1.size() + alignment.alignedBoundaries.size());
                double pct2 = (double)alignment.alignedBoundaries.size()/(double)(alignment.unalignedBoundaries2.size() + alignment.alignedBoundaries.size());
                System.out.println(" For ann1: " + ((int)(pct1*100)) + "%.\n For ann2: " + ((int)(pct2*100)) + "%.");
            }
        }
    }

    /** Collect for all threshold values the derived classifications for all alignments, using the BoundaryAlignmentToClassificationFactory. */
    public void collectClassifications() {
        Iterator pairIt = allAnnotatorPairs.iterator();
        //for all pairs of actual annotators for this observation/layer: 
        while (pairIt.hasNext()) {
            Pair nextPair = (Pair)pairIt.next();
            String ann1 = (String)nextPair.o1;
            String ann2 = (String)nextPair.o2;

            //System.out.println("Deriving classifications for " + ann1 + " vs " + ann2);
            
            //init classificationPairs map for this pair
            Map classifications = new HashMap();
            classificationPairs.put(nextPair,classifications);
            
            //get alignments map for this pair
            Map alignments = (Map)boundaryAlignments.get(nextPair);
            
            //for each threshold value
            for (int i = 0; i < thresholdValues.size(); i++) {
                //System.out.println("threshold " + ((Double)thresholdValues.get(i)).doubleValue());
                //generate classifications from alignment
                BoundaryAlignment alignment = (BoundaryAlignment)alignments.get(thresholdValues.get(i));
                Pair classificationPair = BoundaryAlignmentToClassificationFactory.makeClassificationsFromAlignments(alignment);
                classifications.put(thresholdValues.get(i),classificationPair);
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

            //System.out.println("Deriving matrices for " + ann1 + " vs " + ann2);
            
            //get classificationPairs map for this pair
            Map classifications = (Map)classificationPairs.get(nextPair);
            
            //for each threshold value
            for (int i = 0; i < thresholdValues.size(); i++) {
                //generate matrices from classifications 
                Pair classificationPair = (Pair)classifications.get(thresholdValues.get(i));
                Classification c1 = (Classification)classificationPair.o1;
                Classification c2 = (Classification)classificationPair.o2;
                CoincidenceMatrix coincM = new CoincidenceMatrix(c1,c2);
                ConfusionMatrix confM = new ConfusionMatrix(c1,c2);
                //if (coincM.alphaNominal(new BooleanMetric())>0) {
                    //System.out.println("==============================================================");
                //}
                //System.out.println("Alpha = " + coincM.alphaNominal(new BooleanMetric()) + " for threshold " + ((Double)thresholdValues.get(i)).doubleValue());
                
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
    public void renderBoundaries() {
        //
        initReportPanel();
        //for each relevant annotator
        for (int i = 0; i < allUsedAnnotators.size(); i++) { 
            String nextName = (String)allUsedAnnotators.get(i);
            ArrayList boundaryList = (ArrayList)boundaryLists.get(nextName);
            reportPanel.addSubImage(nextName,AnnotatorRenderer.renderBoundaryList(boundaryList, height, milliSecsPerPixel));
        }
        ArrayList l = new ArrayList();
        l.add("f");
        l.add("e");
        reportPanel.annotatorsToDraw = l;
    }
    public void renderRelations() {
        //for each pair, create the relation list based on the alignments
        Iterator pairIt = allAnnotatorPairs.iterator();
        //for all pairs of actual annotators for this observation/layer: 
        while (pairIt.hasNext()) {
            Pair nextPair = (Pair)pairIt.next();
            String ann1 = (String)nextPair.o1;
            String ann2 = (String)nextPair.o2;

            //get alignments map for this pair
            Map alignments = (Map)boundaryAlignments.get(nextPair);
            //for each threshold value
            //for (int i = 0; i < thresholdValues.size(); i++) {
                int i = thresholdValues.size()-1;
                ArrayList relations = new ArrayList();
                BoundaryAlignment alignment = (BoundaryAlignment)alignments.get(thresholdValues.get(i));
                for (int j = 0; j < alignment.alignedBoundaries.size(); j++) {
                    Pair p = (Pair)alignment.alignedBoundaries.get(j);
                    Double time1 = new Double(((Boundary)p.o1).time);
                    Double time2 = new Double(((Boundary)p.o2).time);
                    relations.add(new Pair(time1,time2));
                }
                reportPanel.betweenAnnotatorRelations.put(new Pair(ann1,ann2),relations);
            //}            
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
    /** the name of the common layer shared by all annotators, can be null */
    public String commonLayer;
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
        		NLayer commlay = metadata.getLayerByName(commonLayer);
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