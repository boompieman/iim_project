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
 * two annotators identified same segments.
 * 
 * Segments detected by two annotators are taken to be the same if they differ in start or end time at most a (configurable) 
 * threshold 'th'. Kappa and alpha are calculated by comparing the labeling for each aligned pair of segments.
 *
 * 
 <h2><a name="segment">Segment alignment based inspection</a></h2>
This section describes an inspection tool that looks at full segments rather than boundaries.
Back to the summary in the nite.datainspection package documentation: Data inspection consists of
<ol>
<li>finding out whether separate annotators identified the same Items (segments, units for labeling), 
<li>finding out whether comparable Items have been assigned the same Values (labels, classes, categories) by the annotators and 
<li>finding out where disagreement lies, i.e. what Values are confused with each other in what situations; what type of Items are most often NOT identified by both annotators at the same time; etc.
<li>(Investigating the nature of the errors that annotators made, and deciding how important these errors are, given the use for which the annotations were created.)
</ol>
The Segment Based inspections are involved with another approach to step one: finding out whether separate annotators identified the same items or events in the data.
In this analysis, the focus is on detection whether two annotators identified the same <i>segments</i> at the same moment. 

The image below shows a (hypothetical) example annotation fragment. The key point of the inspections described in this section is that the pair of blue annotation elements as well as the pair of red ones are both considered to be "the same Item identified by both annotators, and given the same label", with the precision of the timing slightly off. This means that a confusion table of this annotation would show two instances of confusion between the labels 'green' and 'brownish-green', and no confusion between red and green or blue and green, even if there is some overlap in time for these labels. Furthermore this analysis does not take the length of the aligned segments into account: a (dis)agreement between two segments counts equally for two long or two short segments.
<br><br>
<div  class=figure><img src="doc-files/timespan_fragment3.png"/><br>an example 'timespan' annotation for two annotators</div>


<br>
A usual position that people take is something like "In order to align the two annotations, it was decided that two segments referred to the same gesture if they covered the same time span, plus or minus a quarter of a second at the onset or end of the gesture" [MUMIN], with varying thresholds of allowed deviation (e.g. MUMIN: 0.25 sec.; Natasa|gaze: 0.8 sec.)
<h4>Precision</h4>
<p>Varying threshold 'th' gives information about the <i>precision</i> with which two annotators identified the same segments. Note however that 'th' should be low enough compared to the segment lengths in the annotations.
<pre>
         ========
         ===Aligning f vs e
         ---
         - th 0.0
         9 aligned segments 
          For ann1: 2%.
          For ann2: 3%.
         ---
         - th 1.5
         242 aligned segments 
          For ann1: 74%.
          For ann2: 90%.
         ---
         - th 3.0
         248 aligned segments 
          For ann1: 76%.
          For ann2: 92%.
</pre>
<p>The list above gives an example output obtained from the {@link net.sourceforge.nite.datainspection.timespan.SegmentBasedInspection} tool, for the FOA layer of the AMI corpus, with several variations of 'th'. Again, we look at the visualisations of the alignments, because of course we are afraid of running into the same 'th' sensitivity that we had with the boundary alignments. The following image show an alignment produced using the tool {@link net.sourceforge.nite.datainspection.timespan.SegmentBasedInspection}. Actually (but for now you'll have to take my word for it...) it turns out that this alignment is for this annotation a lot better when it operates on segments instead of boundaries (see the boundary based inspection for example alignments). Furthermore it is less sensitive to too high threshold values. In this annotation even a treshold of 3 seconds leads to almost only correct alignments (compare to the maximum adequate threshold of about 0.3 seconds for the boundary alignment).
<div  class=figure><img src="doc-files/segment_alignment_foa_th10.png"/><br>automatic alignment for a good threshold.</div>

<h3>AND NOW SOME REMARKS ABOUT REASONS TO EXTEND THE ALIGNMENT ALGORITHM!</h3>
Why? Why do I feel that the alignment should be made more complicated, when the example above showed that it works quite allright? 
<p>
Remember why you do reliability analysis. The reason to do reliability analysis is not only to reach a conclusion like "the alpha reliability of my annotation is 0.8 so it is good enough". The main reason to do this data inspection is to figure out <i>the strong points and the weak points of your annotations</i>. Hopefully identifying those strong and weak points helps you improve the annotation protocol, or decided what you can and cannot do with the data resulting from your annotation protocol.
<h4>Taking labels into consideration.</h4>
Take a look at the image below, which shows a fragment of two aligned annotations for hand gestures in the AMI corpus. The red fragments are 'no_comm_hand'. The other colours all stand for some communicative hand gesture. Note especially the alignment between the brownish-green fragment (point_p_id_RH) of the first annotator with the red fragment directly below it, next to the brown exclamation mark. The alignment procedure obviously aligned the element with the closest similarily sized element of the other annotator. When we now proceed to analyse the labelings of the aligned segments, this alignment will contribute a 'confusion between point_p_id_RH and no_comm_hand'. What we actually <i>wanted</i> to learn about this annotation is that the two annotators agree perfectly on the occurrence and labeling of the point_p_id_RH gesture, but disagreed more than we expected on the timing of this segment. So... it would have been nice if the alignment algorithm would have aligned the two brownish-green fragments near the exclamation mark (the <i>are</i> within a 'th' distance of each other...). As a second example, see the blue exclamation mark on the right: both annotators find two gestures there (and a lot of no_comm_hand), agree on the labeling of the left one and disagree on the labeling of the right one and disagree a lot on the timing of the left element. The alignment goes wrong, leading to a conclusion that there are two disagreements on labeling and one gesture found only by one annotator.
<div  class=figure><img src="doc-files/ia_alignment_noLabelCheck.png"/><br>An example of an alignment that goes wrong when labels are not considered in the alignment.</div>
<p>
The kind of alignment mistake described above is often unavoidable. When they don't happen too often, they are also not so much of a problem, since the data inspection packages aim at getting an idea of the quality of the corpus, not at getting an absolute truth about correctness of annotations. In some cases however it is quite easy to improve the alignments. In the example mentioned above, the hand gesture annotation of AMI, the no_comm_hand label is a kind of "background label" which is used whenever there is no communicative hand gesture. Since the alignment procedure aims at aligning the segments where two annotators agree on the occurrence of (in this case) a gesture, it seems sensible to leave out these background segments from the analysis. This is supported by the 'isIgnoreP' parameter in the constructor of the {@link net.sourceforge.nite.datainspection.timespan.SegmentBasedInspection} tool. If this parameter is non-null, all segments for which this predicate evaluates true are ignored in the analysis. If we run the alignment for the AMI hand gestures anew while defining "no_comm_hand" as an to-be-ignored label we get the alignment shown below.
<div  class=figure><img src="doc-files/ia_alignment_withIgnore.png"/><br>An example of the same alignment with no_comm_hand as an ignored label.</div>

<!-- another remark here: this background label also 'solves' the issue of having one 'real label' in a sea of 'nothing' where the other annotator has only 'nothing'. -->

<h3>Next step: alpha reliability on labeling of aligned segments</h3>
Supported by tool {@link net.sourceforge.nite.datainspection.timespan.SegmentBasedInspection}; some information needed. You must provide a DistanceMetric and a NOMElementToValueDelegate to the constructor of the SegmentBasedInspection tool. These are used to calculate Krippendorff Alpha on the aligned segments. These values are reported on the System.out. Furthermore confusion tables are shown on screen for every pair of annotators. 

For the AMI hand gesture annotation, the output is as follows (Alpha reported for all aligned hand gestures using the string type label as Value and a BooleanMetric as distance metric):
<pre>
         Deriving matrices for BrigitteGreenwood vs pistek
         ==============================================================
         Alpha = 0.7226277372262774 for threshold 0.5
         ==============================================================
         Alpha = 0.8085808580858086 for threshold 1.5
         ==============================================================
         Alpha = 0.8171091445427728 for threshold 2.5
         
         Deriving matrices for BrigitteGreenwood vs xsobol03
         ==============================================================
         Alpha = 0.7428571428571429 for threshold 0.5
         ==============================================================
         Alpha = 0.7128712871287128 for threshold 1.5
         ==============================================================
         Alpha = 0.7256637168141593 for threshold 2.5
</pre>
<div  class=figure><img src="doc-files/confTablePanel.png"/><br>An example of a confusion table shown by the SegmentBasedInspection tool. Note for example the confusion concerning what hand this participant used to point... (left or both hands, point_me_LH and point_me_BH). The tool allows one to find that particular confusion on the timeline, then click on the timeline to bring up that particular stretch of video on the NXT video player.</div>
 */
public class SegmentBasedInspection {


    public SegmentBasedInspection (
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
        collectAlignments(thMin, thMax, thSteps);
        
        collectClassifications();
        collectMatrices();
        
        //reporting
        checkMainFrame();
        renderSegments();
        renderRelations();
        drawLegend();
        
        //report confusions
        showConfusionTables();
        
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
    /** A Map of Maps of segmentAlignments. Keys: pairs of annotatornames. values: for each two annotators,
    a Map of segmentAlignments keyed by the threshold value with which each alignment was produced. */
    Map segmentAlignments = new HashMap(); 
    /** The pairs of Classifications derived from the SegmentAlignments. Because the classifications
    for one author will be different when comparing with different other annotators, Pairs of 
    classifications (for all used threshold values) is stored here for each Pair of annotator names. 
    Key: pairs of annotator names. Value: Maps of Pairs of classifications, keyed by the threshold value 
    with which the alignments were produced. */
    Map classificationPairs = new HashMap(); 
    
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
    
    /** Collect for all threshold values the alignments for all annotator pairs, using the SegmentAligner. */
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
            ArrayList segmentList1 = (ArrayList)segmentLists.get(ann1);
            ArrayList segmentList2 = (ArrayList)segmentLists.get(ann2);
            
            //init boundaryAlignments map for this pair
            Map alignments = new HashMap();
            segmentAlignments.put(nextPair,alignments);
            
            //for each threshold value
            for (int i = 0; i < thresholdValues.size(); i++) {
                //generate alignments
                SegmentAlignment alignment = SegmentAligner.alignSegments(segmentList1, segmentList2, ((Double)thresholdValues.get(i)).doubleValue(), isIgnoreP);

                alignments.put(thresholdValues.get(i),alignment);
                System.out.println("---\n- th " + ((Double)thresholdValues.get(i)).doubleValue());
                System.out.println(alignment.alignedSegments.size() + " aligned segments ");
                double pct1 = (double)alignment.alignedSegments.size()/(double)(alignment.unalignedSegments1.size() + alignment.alignedSegments.size());
                double pct2 = (double)alignment.alignedSegments.size()/(double)(alignment.unalignedSegments2.size() + alignment.alignedSegments.size());
                System.out.println(" For ann1: " + ((int)(pct1*100)) + "%.\n For ann2: " + ((int)(pct2*100)) + "%.");
            }
        }
    }
    /** Collect for all threshold values the derived classifications for all alignments, using the SegmentAlignmentToClassificationFactory. */
    public void collectClassifications() {
        Iterator pairIt = allAnnotatorPairs.iterator();
        //for all pairs of actual annotators for this observation/layer: 
        while (pairIt.hasNext()) {
            Pair nextPair = (Pair)pairIt.next();
            String ann1 = (String)nextPair.o1;
            String ann2 = (String)nextPair.o2;

            System.out.println("Deriving classifications for " + ann1 + " vs " + ann2);
            
            //init classificationPairs map for this pair
            Map classifications = new HashMap();
            classificationPairs.put(nextPair,classifications);
            
            //get alignments map for this pair
            Map alignments = (Map)segmentAlignments.get(nextPair);
            
            //for each threshold value
            for (int i = 0; i < thresholdValues.size(); i++) {
                //System.out.println("threshold " + ((Double)thresholdValues.get(i)).doubleValue());
                //generate classifications from alignment
                SegmentAlignment alignment = (SegmentAlignment)alignments.get(thresholdValues.get(i));
                Pair classificationPair = SegmentAlignmentToClassificationFactory.makeClassificationsFromAlignments(alignment,segmentToValue);
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

            System.out.println("\nDeriving matrices for " + ann1 + " vs " + ann2);

            //init confusion and coincidencematrices map for this pair
            Map coincidenceMatrices = new HashMap();
            segmentCoincidenceMatrices.put(nextPair,coincidenceMatrices);
            Map confusionMatrices = new HashMap();
            segmentConfusionMatrices.put(nextPair,confusionMatrices);
            
            //get classificationPairs map for this pair
            Map classifications = (Map)classificationPairs.get(nextPair);
            
            //for each threshold value
            for (int i = 0; i < thresholdValues.size(); i++) {
                //generate matrices from classifications 
                Pair classificationPair = (Pair)classifications.get(thresholdValues.get(i));
                Classification c1 = (Classification)classificationPair.o1;
                Classification c2 = (Classification)classificationPair.o2;
                CoincidenceMatrix coincM = new CoincidenceMatrix(c1,c2);
                coincidenceMatrices.put(thresholdValues.get(i),coincM);
                ConfusionMatrix confM = new ConfusionMatrix(c1,c2);
                confusionMatrices.put(thresholdValues.get(i),confM);
                if (coincM.alphaNominal(labelDistanceMetric)>0) {
                    System.out.println("==============================================================");
                }
                System.out.println("Alpha = " + coincM.alphaNominal(labelDistanceMetric) + " for threshold " + ((Double)thresholdValues.get(i)).doubleValue());
                
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
//        ArrayList l = new ArrayList();
//        l.add("xsobol03");
//        l.add("xpolok00");
//        reportPanel.annotatorsToDraw = l;
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
            Map alignments = (Map)segmentAlignments.get(nextPair);
            //for each threshold value
            //for (int i = 0; i < thresholdValues.size(); i++) {
                int i = thresholdValues.size()-1;
                ArrayList relations = new ArrayList();
                SegmentAlignment alignment = (SegmentAlignment)alignments.get(thresholdValues.get(i));
                for (int j = 0; j < alignment.alignedSegments.size(); j++) {
                    WeightedPair p = (WeightedPair)alignment.alignedSegments.get(j);
                    Double time1 = new Double((((NOMWriteElement)p.o1).getEndTime()+((NOMWriteElement)p.o1).getStartTime())/2d);
                    Double time2 = new Double((((NOMWriteElement)p.o2).getEndTime()+((NOMWriteElement)p.o2).getStartTime())/2d);
//                    Double time1 = new Double(((NOMWriteElement)p.o1).getStartTime());
//                    Double time2 = new Double(((NOMWriteElement)p.o2).getStartTime());
                    relations.add(new Pair(time1,time2));
                }
                reportPanel.betweenAnnotatorRelations.put(new Pair(ann1,ann2),relations);
            //}            
        }        
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

