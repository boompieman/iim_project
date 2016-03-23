/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004, Jean Carletta, Jonathan Kilgour, Natasa Jovanovic, Dennis Reidsma
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.transcriptionviewer;

import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.event.*;

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
import java.util.Iterator;
import java.util.Comparator;

import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.*;

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
import net.sourceforge.nite.util.*;
import net.sourceforge.nite.nstyle.handler.*;
import net.sourceforge.nite.time.*;


/**
 * NTranscriptionView is a text pane used for customizable display of annotated speech transcripts, also providing
 * flexible selection behaviour.
 * <p>
 * NTranscriptionView is one of the core classes in the transcriptionviewer package. It has been
 * created to display speech transcriptions from a corpus of interactions, display extra
 * information annotated on these transcriptions (e.g. dialogue acts or adjacency pairs)
 * and provide a variety of GUI interface methods for selecting transcriptions and/or
 * annotations. 
 * <p>
 * The documentation of this class is grouped in three subjects:
 * what is displyed; how will it be displayed; what selection mechanisms are available.
 * <b>SOMEBODY PLEASE FINISH THIS DOCUMENTATION FOR ME!</B> I'm fed up with it :p
 * When finishing this documentation, take care to link always to the central methods that 
 * support a certain functionality, because those methods also contain a lot of detailed
 * documentation
 * <h2>What is displayed?</h2>
 *   <br>the transcription layer
 *    {@link #transLayerName}
 *   <br>the segment layer
 *    {@link #segmentationElementName}
 *   <br>the setDisplayedSegments methods
 *    {@link #setDisplayedSegments(Iterator elements)}
 *   <br>the displayAnnotationELements methods (which display only those ann. els for which (part of) 
 *    the transcription elements are displayed).
 *     {@link #displayAnnotationElement(NOMElement element)}
 * <h2>How will it be displayed?</h2>
 *   <br>Transcription: ascending preference: default is text. If attribute name is set, that is used. If delegate is set, that is used.
 *    {@link #transToTextDelegate}
 *   <br>Annotations: Display strategies in combination with TransToAnnoMaps
 *    {@link #setDisplayStrategy(String annotationElementName, DisplayStrategy ds)}
 * <h2>What selection mechanisms are available?</h2>
 *   <br>transcription selection (settings!)
 *   <br>annotation element selection (uses transtoannomaps and some extra settings)
 *   <br>selection changed listeners! 
 *   <br>Access of selection data thorugh getSelectedAnno and Trans Elements
 * <p>
 * <h2>WISHLIST</h2>
 * <ul>
 * <li> The displayed segments are now always sorted on start time. That is obviously not always
 *      the most desirable sorting. Customized sorting could be implemented with some settings
 *      (SORT_ON_STARTTIME, SORT_ON_ENDTIME, DONT_SORT (which keeps the sorting intact that was
 *      imposed by the user when passing the segments to the NTrasncriptionView), possibly more 
 *      methods...)
 * <li>
 * </ul>
 * @author Dennis Reidsma
 * @author Natasa Jovanovic, Jonathan Kilgour, Jean Carletta
 */
public class NTranscriptionView extends NTextArea {

    
    /**
     * Describe all default settings for everything here?
     * (defaulttranstoannomap, default selection settings, etc)
     * <p>People who use such things can add here some trivial extra
     * constructors that take more arguments, such as selection settings parameters.
     * In that case, don't forget to call this parameterfree constructor!
     */
    public NTranscriptionView() {
        super();
        transToAnnoMaps.add(new DefaultTransToAnnoMap(this));
    }
    
/*=================================================================================================

                                    TRANSCRIPTION TEXT DISPLAY
                                    
  =================================================================================================*/
    
    /**
     * See the documentation of the method 
     * {@link #setSegmentationElementName setSegmentationElementName}
     */
    protected String segmentationElementName = "trans";
    /**
     * The <i>segmentation</i> of the speech transcriptions determines how the transcriptions will
     * be grouped and sorted.  
     * <p>
     * Usually the transcription should be displayed in `screen lines'. Each line stands for one 
     * utterance or dialogue contribution or prosodic `sentence fragment' or something like that. 
     * The `screen lines' can be sorted on time, or on dialogue structure, or any other way.
     * [DR: comment: see wishlist on sorting.]
     * <p>
     * Examples of this segmentation can be utterances, dialogue acts, or simple the segments as have been 
     * created in ChannelTrans [ref].
     * <p>
     * The class NTranscriptionView supposes that this grouping is defined by a layer of segmentation 
     * elements that form a non-overlapping segmentation of the speech of the participants (non-overlapping
     * in the sense that each word belongs to at most one segment).
     * The attribute segmentationElementName defines the name of the segmentation elements.
     * In the metadata this is a structural layer with &lt;code name="###"/&gt; having the `word' elements 
     * as its children.
     */
    public void setSegmentationElementName(String newName) {
        if (newName == null) {
            throw new NullPointerException("Null segmentationElementName is not allowed");
        }
        segmentationElementName = newName;
    }
    /**
     * See the documentation of the method
     * {@link #setSegmentationElementName setSegmentationElementName}
     */
    public String getSegmentationElementName() {
        return segmentationElementName;
    }

    /**
     * Returns true iff the given element is a segment element.
     */
    public boolean isSegmentationElement(NOMElement element) {
        try {
            return element.getName().equals(segmentationElementName);
        } catch (Exception ex) {
            System.out.println(" unknown error ");
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * See the documentation of the method 
     * {@link #setTransLayerName setTransLayerName}
     */
    protected String transLayerName = "trans";
    /**
     * The core of the display is formed by the actual transcription text.
     * Transcription text is stored in a specific layer of NOMElements with
     * a certain name (in the metadata: &lt;time-aligned-layer name="###"/&gt;).
     * <p>
     * Since the name "###" can be different for different corpora this name should
     * be stored in the transcriptionViewer.
     * <p>
     * For documentation on the way in which the transcription elements are actually 
     * visualized, please see the overview documentation of this class.
     */
    public void setTransLayerName(String newName) {
        if (newName == null) {
            throw new NullPointerException("Null transElementName is not allowed");
        }
        transLayerName = newName;
    }
    /**
     * See the documentation of the method 
     * {@link #setTransLayerName setTransLayerName}
     */
    public String getTransLayerName() {
        return transLayerName;
    }
    /**
     * Returns true iff the given element is a transcription element.
     */
    public boolean isTranscriptionElement(NOMElement element) {
        try {
            return element.getLayer().getName().equals(transLayerName);
        } catch (NOMException ex) {
            System.out.println(" unknown error ");
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * See the documentation of the method 
     * {@link #setTranscriptionToTextDelegate setTranscriptionToTextDelegate}
     */
    protected TranscriptionToTextDelegate transToTextDelegate = null;

    /**
     * There are three ways to determine the text that should be used to represent a certain
     * element <code>tle</code> from the transcription layer.
     * <p>The one with the highest precedence is the TranscriptionToTextDelegate. If this parameter is set,
     * the text for element <code>tle</code> is determined by a call to 
     * {@link TranscriptionToTextDelegate#getTextForTranscriptionElement getTextForTranscriptionElement}.
     * <br>The one with the second-highest precedence is the parameter {@link #transcriptionAttribute}.
     * If there is no TranscriptionToTextDelegate set, and this parameter is non-null, the text for 
     * element <code>tle</code> is determined by the value of attribute 'transcriptionAttribute' of
     * that element.
     * <br>If both above parameters are not set, the text representation of element <code>tle</code>
     * is taken to be the text content of the element.
     * <p>
     * In order to make the text of a transcription element available independent of these different
     * styles of derivation the method {@link #getTranscriptionText} is used.
     */
    public void setTranscriptionToTextDelegate(TranscriptionToTextDelegate newDelegate) {
        transToTextDelegate = newDelegate;
    }

    /**
     * See the documentation of the method 
     * {@link #setTranscriptionToTextDelegate setTranscriptionToTextDelegate}
     */
    protected String transcriptionAttribute = null;

    /**
     * See the documentation of the method 
     * {@link #setTranscriptionToTextDelegate setTranscriptionToTextDelegate}
     */
    public void setTranscriptionAttribute(String newName) {
        transcriptionAttribute = newName;
    }

    /**
     * See the documentation of the method 
     * {@link #setTranscriptionToTextDelegate setTranscriptionToTextDelegate}
     */
    public String getTranscriptionAttribute() {
        return transcriptionAttribute;
    }

    /**
     * This method returns the transcript text of a certain transcription element. 
     * See {@link #transcriptionAttribute} for information on how that text is derived.
     * <p>
     * This method should be used whenever the text content for a certain transcription element is needed.
     * @param nme The NOMElement containing transcription data. 
     * @throws IllegalArgumentException If called with wrong TYPE of element (not in transcription layer),
     * an IllegalArgumentException is thrown.
     * @throws NoSuchElementException : If the given element does not contain the right attribute
     * {@link #transcriptionAttribute} when that attribute is needed, a NoSuchElementException is thrown.
     */
    public String getTranscriptionText(NOMElement nme) {
        if (!isTranscriptionElement(nme)) {
            throw new IllegalArgumentException("getTranscriptionText should only be called for transcription elements of the right type. Correct layer: " + transLayerName + "; Used type: " + nme.getName());
        }
        if (transToTextDelegate != null) {
            return transToTextDelegate.getTextForTranscriptionElement(nme);
        } else if (transcriptionAttribute == null) {
            return nme.getText();
        } else {
            String result = (String)nme.getAttributeComparableValue(transcriptionAttribute);
            if (result == null) {
                throw new NoSuchElementException("Can't find attribute " 
                                                + transcriptionAttribute 
                                                + " on transcription element");
            }
            return result;
        }
    }

    /** NEW */

    /**
     * See the documentation of the method 
     * {@link #setTranscriptionToTextDelegate setTranscriptionToTextDelegate}
     */
    protected TranscriptionToTextDelegate segmentToTextDelegate = null;

    /**
     * There are three ways to determine the text that should be used to represent a certain
     * element <code>tle</code> from the segment layer.
     * <p>The one with the highest precedence is the TranscriptionToTextDelegate. If this parameter is set,
     * the text for element <code>tle</code> is determined by a call to 
     * {@link TranscriptionToTextDelegate#getTextForElement getTextForSegmentElement}.
     * <br>The one with the second-highest precedence is the parameter {@link #segmentToTextAttribute}.
     * If there is no SegmentToTextDelegate set, and this parameter is non-null, the text for 
     * element <code>tle</code> is determined by the value of attribute 'segmentToTextAttribute' of
     * that element.
     * <br>If both above parameters are not set, the text representation of element <code>tle</code>
     * is taken to be the agent of the element (if it has an agent).
     * <p>
     * In order to make the text of a segment element available independent of these different
     * styles of derivation the method {@link #getSegmentText} is used.
     */
    public void setSegmentToTextDelegate(TranscriptionToTextDelegate newDelegate) {
        segmentToTextDelegate = newDelegate;
    }

    /**
     * See the documentation of the method 
     * {@link #setSegmentToTextDelegate setSegmentToTextDelegate}
     */
    protected String segmentToTextAttribute = null;

    /**
     * See the documentation of the method 
     * {@link #setSegmentToTextDelegate setSegmentToTextDelegate}
     */
    public void setSegmentToTextAttribute(String newName) {
        segmentToTextAttribute = newName;
    }

    /**
     * See the documentation of the method 
     * {@link #setSegmentToTextDelegate setSegmentToTextDelegate}
     */
    //JCC corrected spelling of this string 
    public String getSegmentToTextAttribute() {
        return segmentToTextAttribute;
    }


    /**
     * This method returns the text for displaying the start of a
     * certain segment element - normally 'speaker:' or something
     * similar.
     * See {@link #segmentToTextAttribute} for information on how that text is derived.
     * <p>
     * This method should be used whenever the start-text for a certain segment element is needed.
     * @param seg The NOMElement containing segment data. 
     * @throws IllegalArgumentException If called with wrong TYPE of element (not in segmentation layer),
     * an IllegalArgumentException is thrown.
     * @throws NoSuchElementException : If the given element does not contain the right attribute
     * {@link #segmentToTextAttribute} when that attribute is needed, a NoSuchElementException is thrown.
     */
    public String getSegmentText(NOMElement nme) {
        if (!isSegmentationElement(nme)) {
            throw new IllegalArgumentException("getSegmentText should only be called for segment elements of the right type. Correct element: " + segmentationElementName + "; Used type: " + nme.getName());
        }
        if (segmentToTextDelegate != null) {
            return segmentToTextDelegate.getTextForTranscriptionElement(nme);
        } else if (segmentToTextAttribute == null) {
            return nme.getAgentName();
        } else {
            String result = (String)nme.getAttributeComparableValue(segmentToTextAttribute);
            if (result == null) {
                throw new NoSuchElementException("Can't find attribute " 
                                                + segmentToTextAttribute 
                                                + " on segment element");
            }
            return result;
        }
    }
    
    /**
     * See the documentation of {@link #setBasicStyle}
     */
    Style basicStyle = null;
    /**
     * Set the text style used for transcription text.
     * By default, the transcription text is shown without any special styles.
     * This method allows you to set a style for it. <p>NB: The style must have been added to
     * the NTranscriptionView!
     */
    public void setBasicStyle(Style newStyle) {
        basicStyle = newStyle;
    }
    
    /**
     * If True, segments should be separated with newlines.
     * Default is true. Setting this to false degrades performance.
     */
    protected boolean addNewlines = true;
    /**
     * If true, segments should be separated with newlines.
     */
    public void setAddNewlines(boolean add) {
        addNewlines = add;
    }
    
    /**
     * This method is used to pass the actual speech transcription text to the NTranscriptionView.
     * <p>
     * The segments should belong to the correct segment layer (see {@link #segmentationElementName}),
     * having children or descendants in the {@link #transLayerName transcription} layer.
     * The segments will be ordered on their starttime<b>@@See also @link wishlist!!!!!!!!</b>, 
     * and displayed one by one in the NTranscriptionView by displaying the text of their transcription elements (see 
     * {@link #getTranscriptionText}).
     * <p>
     * The segments will be separated by newlines yet if {@link #addNewlines} is true.
     * If a DisplayStrategy is known for the segment elements it will immediately be invoked for them 
     * (see {@link setDisplayStrategy}).
     * <p><b>See remark in overview about sorting!</b>
     */
    public void setDisplayedSegments(Iterator elements) {
        DisplayStrategy ds = (DisplayStrategy)displayStrategies.get(segmentationElementName);
        int segNo = 0; //see HACK below 
        clear();
        setEditable(false); //!! :-( [DR] why is this necessary? (it is...)
        displayedAnnotationElements.clear();
        textToSegments.clear();
        //sort segments. [DR: I want to get rid of this sorting. Anyone wanting to implement the wishlist point is welcome.
        List sortedElements = new ArrayList();
        while (elements.hasNext()) {
            NOMElement next = (NOMElement)elements.next();
            if (!next.getName().equals(segmentationElementName)) {
                throw new IllegalArgumentException("Not allowed: wrong type of segments. Correct segment type: "+segmentationElementName+" used type: " + next.getName());
            }
            int index = 0;
            double t = next.getStartTime();
            while (index < sortedElements.size()) {
                if (((NOMElement)sortedElements.get(index)).getStartTime() > t) {
                    break;
                }
                index++;
            }
            sortedElements.add(index, next);
        }
        
        //suspend caret listeners, since that would only slow everything down..
        CaretListener[] carets=getCaretListeners();
          for (int i=0;i<carets.length;i++)
            removeCaretListener(carets[i]);
        
        //for each element:
        Iterator sortedIterator = sortedElements.iterator();
        while (sortedIterator.hasNext()) {
            NOMElement nextSegment = (NOMElement)sortedIterator.next();
            segNo++;  //see HACK below
                    
            //get transcription elements and their text
            Set transElements = getTranscriptionDescendants(nextSegment);
            Iterator it = transElements.iterator();
            while (it.hasNext()) {
                NOMElement transel = (NOMElement)it.next();
                String text = getTranscriptionText(transel);
                
                //create textelement, timing derived from nme; content is word; objectmodelelement is nme
                //use basic style, if present.
                String style = "";
                if (basicStyle != null) {
                    style = basicStyle.getName();
                }
                NTextElement nte = new NTextElement(text + " ", style, transel.getStartTime(), transel.getEndTime());
                NOMObjectModelElement nome= new NOMObjectModelElement(transel);
                nte.setDataElement((ObjectModelElement)nome);
                textToSegments.put(nte,nextSegment);
                addElement(nte);
            }
       
       
            //<HACK>    @@@@
                    //  [DR:]
                    //  problem : JTextPane will crash with too long text without \n in it. 
                    //            How do we force the occasional newline...
                    //  solution: simply add a newline if we have seen a number of segments
                    if (addNewlines || (!addNewlines && (segNo > 100)) ) {
                        NTextElement nte = new NTextElement("\n", null, nextSegment.getStartTime(), nextSegment.getEndTime());
                        NOMObjectModelElement nome= new NOMObjectModelElement(nextSegment);
                        nte.setDataElement((ObjectModelElement)nome);
                        textToSegments.put(nte,nextSegment);
                        addElement(nte);
                        segNo = 0;
                    }
            //</HACK>   @@@@

            //display the segment as an annotation element, if a display strategy is known. (e.g. prefixing with speaker,
            //which is quite usual for this kind of thing)
            if (ds != null) {
                displayAnnotationElement(nextSegment);
            }
        }
        
        //reinstall caretlistener
        for (int i=0;i<carets.length;i++) {
            addCaretListener(carets[i]);
        }
    }
    /**
     * See {@link #setDisplayedSegments(Iterator elements)} documentation
     */
    public void setDisplayedSegments(List elements) {
        setDisplayedSegments(elements.iterator());
    }
    /**
     * See {@link #setDisplayedSegments(Iterator elements)} documentation
     */
    public void setDisplayedSegments(Set elements) {
        setDisplayedSegments(elements.iterator());
    }
    /**
     * A map from text elements to segments. Maintiained by "setDisplayedSegments".
     * Used internally for quick mapping between them, to see whether a text element should be highlighted
     * when a certain segment has been selected. Is this, performance wise, a good idea? What is the cost in memory?
     * What is the cost in speed if we NOT do this?
     */
    protected Map textToSegments = new HashMap();


/*=================================================================================================

                                    ANNOTATION ELEMENTS DISPLAY
                                    
  =================================================================================================*/
    
    /**
     * See {@link #displayAnnotationElement(NOMElement element)} documentation
     */
    public void displayAnnotationElements(Set elements) {
        displayAnnotationElements(elements.iterator());
    }
    /**
     * See {@link #displayAnnotationElement(NOMElement element)} documentation
     */
    public void displayAnnotationElements(List elements) {
        displayAnnotationElements(elements.iterator());
    }
    /**
     * See {@link #displayAnnotationElement(NOMElement element)} documentation
     */
    public void displayAnnotationElements(Iterator elements) {
        while (elements.hasNext()) {
            displayAnnotationElement((NOMElement)elements.next());
        }
    }    
    /**
     * The given annotation element is displayed in the appropriate way in the transcription texts.
     * 'The appropriate way' is determined by the display strategies that have been registered for elements of 
     * different types.
     * <p> If already displayed: remove & redisplay!
     * <p>If, for one of the annotation elements, no display strategy is known, the element is not displayed.
     * <p>If, for one of the annotation elements, the Transcription fragment to which it pertains is not
     * displayed in the NTranscriptionView, the annotation element will not be added. If at a later time the relevant 
     * transcription elements are added, you will have to add these annotation elements again.
     * <p>Internally administrates fact that this element has been displayed.
     */
    public void displayAnnotationElement(NOMElement element) {
        //System.out.println("remove?***************");
        if (displayedAnnotationElements.contains(element)) {
            //System.out.println("redisplay. first remove");
            undisplayAnnotationElement(element);
            //System.out.println("removed.");
        }//not so efficient? calls for the displaystrategy twice, in this way...
        DisplayStrategy ds = (DisplayStrategy)displayStrategies.get(element.getName());
        if (ds == null) {
            Logger.global.info("No display strategy known for " + element.getName());
            return;
        }
        if (ds.display(element)) {
            displayedAnnotationElements.add(element);
        } else {
	    Debug.print("display of element failed:" + element.getID());
            //System.out.println("display of element failed:" + element.getID() );
        }
    }    
    /**
     * See {@link #undisplayAnnotationElements(Iterator elements)} documentation
     */
    public void undisplayAnnotationElements(Set elements) {
        undisplayAnnotationElements(elements.iterator());
    }
    /**
     * See {@link #undisplayAnnotationElements(Iterator elements)} documentation
     */
    public void undisplayAnnotationElements(List elements) {
        undisplayAnnotationElements(elements.iterator());
    }
    /**
     * See {@link #undisplayAnnotationElements(Iterator elements)} documentation
     */
    public void undisplayAnnotationElements(Iterator elements) {
        while (elements.hasNext()) {
            undisplayAnnotationElement((NOMElement)elements.next());
        }
    }    
    /**
     * Undisplays element, using displaystrategy.undisplay. Also updates internal administration
     */
    public void undisplayAnnotationElement(NOMElement element) {
        if (!displayedAnnotationElements.contains(element)) {
            System.out.println("Element " + element + " of type " + element.getName() + " was never displayed...");
            return;
        }
        DisplayStrategy ds = (DisplayStrategy)displayStrategies.get(element.getName());
        if (ds != null) {
            ds.undisplay(element);
        }
        displayedAnnotationElements.remove(element);
    }    
    /** 
     * Stores the annotationelements that have already been displayed in the transcript text.
     */
    protected Set displayedAnnotationElements = new HashSet();
    //TreeSet(new NOMElementStartTimeComparator());
    //DR 26.11.04 this was a treeset. But because I used an NOMElementStartTimeComparator here, 
    //a NOMElement would not be found back if it's children had been changed. This meant a.o.
    //that a undisplay would not be called when you change the children and then do a redisplay.
    
    /**
     * Administration of DisplayStrategies.
     * <p>
     * The central focus of the display in text labelling tools will
     * be the text of the transcriptions. The
     * NTranscriptionViewer will show this text plainly, without
     * styles or extra markup or characters. Elements annotated on this
     * text can be shown in two ways: 1) by changing
     * the style of the corresponding text and 2) by adding extra markup
     * characters such as brackets. It seems natural to associate a
     * specific display style with each <b>element type</b> annotated on the
     * text. The actual displaying of elements is performed by delegates:
     * {@link net.sourceforge.nite.gui.transcriptionviewer.DisplayStrategy DisplayStrategy}.
     * <p>
     * setDisplayStrategy(elementname, DisplayStrategy): indicates that the annotation elements with the
     * given name (e.g. ``da'', ``pos'', ``propername'') will be
     * displayed using the given DisplayStrategy object.
     */
    public void setDisplayStrategy(String annotationElementName, DisplayStrategy ds) {
        if (annotationElementName == null) {
            throw new IllegalArgumentException("Can't set display strategy for <null> annotation element name");
        }
        if (ds == null) {
            throw new IllegalArgumentException("Display strategy <null> not allowed");
        }
        displayStrategies.put(annotationElementName, ds);
    }
    /**
     * A mapping between annotation element names and displaystrategies.
     * See {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#setDisplayStrategy setDisplayStrategy}
     * documentation.
     */
    protected Map displayStrategies = new HashMap();
   

/*=================================================================================================

                                    SELECTION
                                    
  =================================================================================================*/

    /**
     * This boolean determines whether you can select speech transcription elements.
     * If this one is false, no speechtext selection will take place, regardless of settings 
     * such as 'allowMultiAgentSelect' or 'wordlevelSelectionType'.
     * <p>default true.
     * <p>See also the general documentation on selection: {@link #newSelection}
     */
    protected boolean allowTranscriptSelect = true;
    /**
     * See {@link #allowTranscriptSelect} documentation.
     * <p>See also the general documentation on selection: {@link #newSelection}
     */
    public void setAllowTranscriptSelect(boolean transSel) {
        allowTranscriptSelect = transSel;
    }
    /**
     * See {@link #allowTranscriptSelect} documentation.
     * <p>See also the general documentation on selection: {@link #newSelection}
     */
    public boolean getAllowTranscriptSelect() {
        return allowTranscriptSelect;
    }
    /**
     * This boolean determines whether you can select annotation elements at all.
     * If this one is false, no annotation-element-selection will take place, regardless of other settings.
     * <p>default false.
     * <p>See also the general documentation on selection: {@link #newSelection}
     */
    protected boolean allowAnnotationSelect = false;
    /**
     * See {@link #allowAnnotationSelect} documentation
     * <p>See also the general documentation on selection: {@link #newSelection}
     */
    public void setAllowAnnotationSelect(boolean annoSel) {
        allowAnnotationSelect = annoSel;
    }
    /**
     * See {@link #allowAnnotationSelect} documentation
     * <p>See also the general documentation on selection: {@link #newSelection}
     */
    public boolean getAllowAnnotationSelect() {
        return allowAnnotationSelect;
    }
    /**
     * If true, user can select a span of text, or set of annotation elements, that contains stuff of 
     * more than one agent. If false, then not.
     * <p>default false.
     * <p>See also the general documentation on selection: {@link #newSelection}
     */
    protected boolean allowMultiAgentSelect = false;
    /**
     * See {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#allowMultiAgentSelect allowMultiAgentSelect}
     * documentation.
     * <p>See also the general documentation on selection: {@link #newSelection}
     */
    public void setAllowMultiAgentSelect(boolean multiagent) {
        allowMultiAgentSelect = multiagent;
    }
    /**
     * See {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#allowMultiAgentSelect allowMultiAgentSelect}
     * documentation.
     * <p>See also the general documentation on selection: {@link #newSelection}
     */
    public boolean getAllowMultiAgentSelect(){
        return allowMultiAgentSelect;
    }    
     

    
    /**
     * Given a click or drag, defined by (dot,mark), update the selection.
     * What has been selected: transcript text, annotation elements, depends
     * on the diverse selection settings. This method also takes care of appropriate
     * highlighting.
     * <p>
     * <h3>NTranscriptionView selection mechanisms</h3>
     *
     
   <b>Somebody finish this documentation for me :-) </b>
   wsetgjiuytfrdcgvnjhuygrdsxgvnjkljhgvfcszdgvnjmk  
     LINK TO APPROPRIATE OTHER PARTS (setselectiontypes enzo)
     */
    public void newSelection(int dot, int mark) {
        // we have a new selection, so let's remove all the old ones
        Highlighter h = getHighlighter();
        h.removeAllHighlights();
        clearHighlights(SELECTION_HIGHLIGHTS);
        selectedTransElements.clear();
        selectedTransSegments.clear();
        selectedAnnoElements.clear();
        //determine the set of selected elements. (NTextElements!)
        //may contain transcript text as well as text representation for annotation elements
        //the transcript text may later be used directly, or
        //interpreted as annotation elements.
        Set s = null;
        if (dot == mark) { //only one element
            s = new HashSet();
            s.add(defaultDoc.getElementAtPosition(dot));
        } else {           //all elements between start and end
            if (dot < mark) {
                s = defaultDoc.getElementsBetweenPositions(dot, mark); //by the way, why does this method fail when dot==mark? OK, because the last parameter is maxIndex + 1, he consideres between dot and mark-1 :-)
            } else if (dot > mark) {
                s = defaultDoc.getElementsBetweenPositions(mark, dot);
            }
        }
        if (    (defaultDoc.getElementAtPosition(dot) != null) 
             && (defaultDoc.getElementAtPosition(dot).getDataElement() != null)
             && (((NOMObjectModelElement)defaultDoc.getElementAtPosition(dot).getDataElement()).getElement() != null)) {
            if (allowTranscriptSelect) {
                //agent is determined by the 'last' selected item.
                //@@ho! null excepts!
                final String agent = ((NOMObjectModelElement)defaultDoc.getElementAtPosition(dot).getDataElement()).getElement().getAgentName();
                TreeSet sorted = new TreeSet( new PositionComparator() );
                sorted.addAll( s );
        
                //filter on transcriptelements if selecting text
                //filter on agentname
                Predicate p = new Predicate() {
                    public boolean valid(Object o) {
			try {
			    NOMElement nme =  ((NOMObjectModelElement)((NTextElement)o).getDataElement()).getElement();
			    if (allowMultiAgentSelect) {
				return isTranscriptionElement(nme);
			    } else {
				if (nme.getAgentName()==null) {
				    return isTranscriptionElement(nme);
				} else {
				    return nme.getAgentName().equals(agent) && isTranscriptionElement(nme);
				}
			    }
			} catch (Exception ex) { 
			    return false;
			}
                    }
                };
                Iterator selectedTextElsIt = new IteratorFilter(sorted.iterator(),p);
        
                //ONE_WORD: only select last word
                switch (wordlevelSelectionType) {
                    case ONE_WORD:
                        //find first element that is 'allowed'
                        if (selectedTextElsIt.hasNext()) {
                            NTextElement nte = (NTextElement)selectedTextElsIt.next(); 
                            setHighlighted(SELECTION_HIGHLIGHTS, nte);
                            selectedTransElements.add(((NOMObjectModelElement)nte.getDataElement()).getElement());
                            notifyNTASelectionListeners();
            
                            return; // if a text selected: don't try annotation as well.
                        }
                        break;
        
                    case ONE_SEGMENT:
                    case MULTIPLE_SEGMENTS:
                        //ONE_SEGMENT: only select last segment. determine segment from word.
                        //MULTIPLE_SEGMENTS: extend all to full segments
                        //System.out.println("select segment");
                        NTextElement startel = null;
                        if (selectedTextElsIt.hasNext()) {
                            startel = (NTextElement)selectedTextElsIt.next();;
                        } else { //maybe it was single click on text representation of segment
                            startel = defaultDoc.getElementAtPosition(dot);
                            NOMElement nme =  ((NOMObjectModelElement)startel.getDataElement()).getElement();
                            if ((!nme.getAgentName().equals(agent) && !allowMultiAgentSelect) || !nme.getName().equals(segmentationElementName)) {
                                break; //its not a valid word, and now it'salso not a segment unit, so: don't select any segment.
                            }
                        }
                        while (true) {
                            //find segment for startel:
                            NOMElement segment = (NOMElement)textToSegments.get(startel);
                            if (segment == null) {
                                //System.out.println("no segment....");
                                //no segment -> not a trans text element -> maybe this is the bracket for a segment-element?
                                //In that case the corresponding TEXT elements should be selected, since the selection strategy is
                                //now text based and not annotation element based.
                                NOMObjectModelElement datamodel = (NOMObjectModelElement)startel.getDataElement();
                                if (datamodel != null) {
                                    NOMElement data = datamodel.getElement();
                                    if (data.getName().equals(segmentationElementName)) {
                                        segment = data;
                                    }
                                }
                            } 
                            if (segment == null) {
                                //System.out.println("still no segment");
                            } else if (!segment.getName().equals(segmentationElementName)) {
                                //System.out.println("not a segment....");
                            } else {    
                                //System.out.println("highlighting segment: " + segment + " " + segment.getName());
                                //highlight all text elements for segment:
                                highlightTransElementsForSegment(segment);
                                selectedTransSegments.add(segment);
                            }    
                            if (wordlevelSelectionType == ONE_SEGMENT) { //ONE_SEGMENT: stop after first segment is highlighted
                                notifyNTASelectionListeners();
            
                                return; // if a text selected: don't try annotation as well.;
                            }  
                            if (selectedTextElsIt.hasNext()) { //MULTIPLE_SEGMENTS: continue until no more elements to check.
                                startel = (NTextElement)selectedTextElsIt.next();
                            } else {
                                notifyNTASelectionListeners();
        
                                return; // if a text selected: don't try annotation as well.;
                            }
                        }
    
                    case CROSS_SEGMENT_PHRASE:            
                        //CROSS_SEGMENT_PHRASE  
                        //(the simplest version :-), just select all full text elements
                        if (!selectedTextElsIt.hasNext()) {
                            //Logger.global.info("a");
                            break;
                        }
                        while (selectedTextElsIt.hasNext()) {
                            NTextElement nextel = (NTextElement) selectedTextElsIt.next();
                            //  System.out.println(" new selection " + startel.getText());
                            setHighlighted(SELECTION_HIGHLIGHTS, nextel);
                            selectedTransElements.add(((NOMObjectModelElement)nextel.getDataElement()).getElement());
                        }
                            //Logger.global.info("b");
                        notifyNTASelectionListeners();
                        return; //after text select: no annotatino sleect
        
                    case IN_SEGMENT_PHRASE:
                        //IN_SEGMENT_PHRASE:
                        //select all that are in same segment as startel
                        NOMElement segment = null;
                        if (!selectedTextElsIt.hasNext()) {
                            break;
                        }
                        while (selectedTextElsIt.hasNext()) {
                            NTextElement nextel = (NTextElement) selectedTextElsIt.next();
                            if (segment==null) {
                                segment = (NOMElement)textToSegments.get(nextel);
                            }
                            
                            if ((segment != null) && (segment == textToSegments.get(nextel))) {
                                setHighlighted(SELECTION_HIGHLIGHTS, nextel);
                                selectedTransElements.add(((NOMObjectModelElement)nextel.getDataElement()).getElement());
                            }
                        }
                        notifyNTASelectionListeners();
                        return;//after text select: no annotatino sleect
                }
            }
            if (allowAnnotationSelect) {
                //31) segment selection: if segment is not necessarily selectable, but text IS selectable, then clicking segment thing might mean 'select corresponding text'

                //agent is determined by the 'last' selected item.
                //@@ho! null excepts!
                final String agent = ((NOMObjectModelElement)defaultDoc.getElementAtPosition(dot).getDataElement()).getElement().getAgentName();
                TreeSet sorted = new TreeSet( new PositionComparator() );
                sorted.addAll( s );
        
                //filter on NON transcriptelements if NOT selecting text
                //filter on agentname
                Predicate p = new Predicate() {
                    public boolean valid(Object o) {
                        NOMElement nme =  ((NOMObjectModelElement)((NTextElement)o).getDataElement()).getElement();
                        if (allowMultiAgentSelect) {
        
                            return (!isTranscriptionElement(nme) || selectTranscriptionAncestors);
                        } else {
            
                            return (nme.getAgentName()==null || nme.getAgentName().equals(agent)) && (!isTranscriptionElement(nme) || selectTranscriptionAncestors);
                        }
                    }
                };
                Iterator selectedTextElsIt = new IteratorFilter(sorted.iterator(),p);
                            
                //getAnnotationElements...
                if ((annotationSelectionGranularity == SINGLE_ANNOTATION) || (annotationSelectionGranularity == MULTIPLE_ANNOTATIONS)) {
                    while (selectedTextElsIt.hasNext()) {
                        NTextElement nextTextEl = (NTextElement)selectedTextElsIt.next();
                        NOMElement nextNOMEl = ((NOMObjectModelElement)nextTextEl.getDataElement()).getElement();
                        Set potentiallySelectedAnnoElements = new TreeSet();
                        //find which anno elements belong to this text element.
                        if (isTranscriptionElement(nextNOMEl) && selectTranscriptionAncestors) {
                            //if text is trans: find through transToAnnoMaps 
                            Iterator ttaMaps = transToAnnoMaps.iterator();
                            while (ttaMaps.hasNext()) {
                                TransToAnnoMap nextMap = (TransToAnnoMap)ttaMaps.next();
                                Set s2 = nextMap.getAnnotationElementsForTransElement(nextNOMEl);
                                potentiallySelectedAnnoElements.addAll(s2);
                            }
                        } else {
                            if (selectDirectTextRepresentations) {
                                //if text is anno: simple, add to set
                                potentiallySelectedAnnoElements.add(nextNOMEl);
                            }
                        }
                        //filter predicate: only those elements can be selected that are in selecatble annotation elements;
                        Predicate canBeSelectedP = new Predicate() {
                            public boolean valid(Object obj) {
                                return selectableAnnotationTypes.contains(((NOMElement)obj).getName());
                            }
                        };
                        //Iterator it iterates over all Annotation elements that may have been selected
                        Iterator it = new IteratorFilter(potentiallySelectedAnnoElements.iterator(), canBeSelectedP);
                        while (it.hasNext()) {
                            NOMElement nextSelectableElement = (NOMElement)it.next();
                            selectedAnnoElements.add(nextSelectableElement);
                            //so. how do we find out highlighting for this element?
                            setHighlighted(SELECTION_HIGHLIGHTS, new NOMObjectModelElement(nextSelectableElement));
                            //we chosen one anno element for being selected, so if only one can be selected at a time we're done
                            if (annotationSelectionGranularity == SINGLE_ANNOTATION) {
                                notifyNTASelectionListeners();
        
                                return;
                            }
                        }
                    }
                    notifyNTASelectionListeners();
            
                    return;
                }
                    
                //maybe it would help to separate click and drag?                 
            }
        }
    }



/*
        =====================================
                ANNOTATION ELEMENT SELECTION
        =====================================
*/    

    /**
     * See {@link #annotationSelectionGranularity} documentation.
     */
    public static final int SINGLE_ANNOTATION    = 1;
    /**
     * See {@link #annotationSelectionGranularity} documentation.
     */
    public static final int MULTIPLE_ANNOTATIONS = 2;
    /**
     * Determines what units are selectable on the ANNOTATIONS:
     * <ul>
     * <li>SINGLE_ANNOTATION
     * <li>MULTIPLE_ANNOTATIONS
     * </ul>
     * Default: SINGLE_ANNOTATION
     * <p>Interacts with other selection settings to define selection behaviour.
     * See also {@link #setSelectableAnnotationTypes}
     * See also {@link #setSelectDirectTextRepresentations}
     * See also {@link #setSelectTranscriptionAncestors}
     */
    protected int annotationSelectionGranularity = 1;
    /**
     * See {@link #annotationSelectionGranularity} documentation.
     */
    public void setAnnotationSelectionGranularity(int granularity) {
        annotationSelectionGranularity = granularity;
    }

    /**
     * See {@link #setSelectableAnnotationTypes} documentation.
     */
    protected Set selectableAnnotationTypes = null;
    /**
     * This set determines which annotation elements are selectable. If it is empty, any element that
     * should potentially be selected based on the current user drag/click will be selected.
     * Otherwise only those elements with a type in this set will be selected.
     */
    public void setSelectableAnnotationTypes(Set newset) {
        selectableAnnotationTypes = newset;
    }
    /**
     * Returns a copy of the selectableAnnotationTypes set.
     * See {@link #setSelectableAnnotationTypes} documentation. 
     */
    public Set getSelectableAnnotationTypes() {
        Set result = new TreeSet();
        result.addAll(selectableAnnotationTypes);
        return result;
    }

    /**
     * See {@link #setSelectDirectTextRepresentations} documentation.
     */
    protected boolean selectDirectTextRepresentations = true;
    /**
     * If false, it is never possible to select annotation elements through clicking their own
     * text representation.
     */
    public void setSelectDirectTextRepresentations(boolean newval) {
        selectDirectTextRepresentations = newval;
    }
    /**
     * See {@link #setSelectDirectTextRepresentations} documentation.
     */
    public boolean getSelectDirectTextRepresentations() {
        return selectDirectTextRepresentations;
    }
    /**
     * See {@link #setSelectTranscriptionAncestors} documentation.
     */
    protected boolean selectTranscriptionAncestors = false;
    /**
     * If false, it is never possible to select annotation elements through clicking om
     * the text representation of their corresponding transcription elements.
     */
    public void setSelectTranscriptionAncestors(boolean newval) {
        selectTranscriptionAncestors = newval;
    }
    /**
     * See {@link #setSelectTranscriptionAncestors} documentation.
     */
    public boolean getSelectTranscriptionAncestors() {
        return selectTranscriptionAncestors;
    }
    
    //per default constructor will add default map
    protected Set transToAnnoMaps = new TreeSet();
    /**
     * Return the TransToAnnoMaps used for determining which annotation elements are selectable 
     * through clicking on the text of a transcription element.
     */
    public Set getTransToAnnoMaps() {
        Set result = new TreeSet();
        result.addAll(transToAnnoMaps);
        return result;
    }
    /**
     * See {@link #getTransToAnnoMaps} documentation.
     */
    public void clearTransToAnnoMaps() {
        transToAnnoMaps.clear();
    }
    /**
     * See {@link #getTransToAnnoMaps} documentation.
     */
    public void addTransToAnnoMap(TransToAnnoMap newmap) {
        transToAnnoMaps.add(newmap);
    }
    

    /**
     * The set of annotation elements that are selected.
     */
    protected Set selectedAnnoElements = new HashSet();
    /**
     * See {@link #selectedAnnoElements} documentation.
     */
    public Set getSelectedAnnoElements() {
        return selectedAnnoElements;
    }
    
/*
        =====================================
                SPEECH TEXT SELECTION
        =====================================
*/    
   
    /**
     * See {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#wordlevelSelectionType wordlevelSelectionType}
     * documentation.
     */
    public static final int FIRST_TEXT_SELECT_TYPE           = 1;
    /**
     * See {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#wordlevelSelectionType wordlevelSelectionType}
     * documentation.
     */
    public static final int ONE_WORD             = 1;
    /**
     * See {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#wordlevelSelectionType wordlevelSelectionType}
     * documentation.
     */
    public static final int ONE_SEGMENT          = 2;
    /**
     * See {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#wordlevelSelectionType wordlevelSelectionType}
     * documentation.
     */
    public static final int MULTIPLE_SEGMENTS    = 3;
    /**
     * See {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#wordlevelSelectionType wordlevelSelectionType}
     * documentation.
     */
    public static final int IN_SEGMENT_PHRASE    = 4;
    /**
     * See {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#wordlevelSelectionType wordlevelSelectionType}
     * documentation.
     */
    public static final int CROSS_SEGMENT_PHRASE = 5;
    /**
     * See {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#wordlevelSelectionType wordlevelSelectionType}
     * documentation.
     */
    public static final int LAST_TEXT_SELECT_TYPE= 5;
    /**
     * Determines what units are selectable on the speech transcriptions:
     * <ul>
     * <li>ONE_WORD
     * <li>ONE_SEGMENT
     * <li>MULTIPLE_SEGMENTS
     * <li>IN_SEGMENT_PHRASE
     * <li>CROSS_SEGMENT_PHRASE
     * </ul>
     * Default: ONE_WORD
       <!--@@@@@@@@@@@@@@@@@@@Disjunct in segment? (gebroken woorden...... hij _belt_ straks _op_.... ) is eigenlijk "anything goes...."-->
     */
    protected int wordlevelSelectionType = 1;
    /**
     * See {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#wordlevelSelectionType wordlevelSelectionType}
     * documentation.
     * <p>
     * Throws exception if out of range (see max & min types)
     */
    public void setWordlevelSelectionType(int newType) {
        if ((newType > LAST_TEXT_SELECT_TYPE) || (newType < FIRST_TEXT_SELECT_TYPE)) {
            throw new IllegalArgumentException("Worldlevel selectiontype out of range");
        }
        wordlevelSelectionType = newType;
    }
    
    protected Color textSelectColor = Color.red;//MetalLookAndFeel.getTextHighlightColor();
    protected Color annoSelectColor = Color.blue;//MetalLookAndFeel.getTextHighlightColor();
    protected Set selectedTransElements = new LinkedHashSet(); //linked, because that one stays ordered on insertion order :-)
    protected Set selectedTransSegments = new LinkedHashSet(); //linked, because that one stays ordered on insertion order :-)
    
    /**
     * Returns the set of speech transcription nomelements that are currently selected
     Works always, if selection type is trans-text-selection
     */
    public Set getSelectedTransElements() {
        return selectedTransElements;
    }
    /**
     * Returns the set of speech transcription SEGMENTS!!! that are currently selected
     * returns elements from segmenttationLayer
     * works only if selection type is actually transtext+ (segment or multisegment)
     * <p>
     * @@@@@@do we want to extend with exception if wrong state?
     */
    public Set getSelectedTransSegments() {
        return selectedTransSegments;
    }

    /**
     * Compares two NTextElements on their character position in the text.
     * <p>
     * two null elements are equal, a null element is always smaller than a non-null element.
     * <p>
     * Used among for selection, to sort something.... (figure out and document!)
     * should we move this outside to its own class?
     */
    private class PositionComparator implements Comparator {
        public int compare( Object o1, Object o2 ) {
            NTextElement e1 = (NTextElement)o1;
            NTextElement e2 = (NTextElement)o2;

            if( e1 == null && e2 == null ) {
                return 0;
            } else if( e1 == null ) {
                return -1;
            } else if( e2 == null ) {
                return 1;
            } else if( e1.equals( e2 )) {
                return 0;
            } else {
                int pos1 = e1.getPosition();
                int pos2 = e2.getPosition();

                if (pos1== pos2) {
                        return 0;
                } else if (pos1 < pos2) {
                        return -1;
                } else { //pos2 > pos1
                        return 1;
                }
            }
        }
    }

    /**
     * Selection: select the given segment.
     * segment must be from segmetnation layer.
     * will select all text elements for the SPEECH TRANSCRIPTION ELEMENTS pertaining to this segment
     */
    protected void highlightTransElementsForSegment(NOMElement segment) {
        if (!segment.getName().equals(segmentationElementName)) {
            throw new IllegalArgumentException("not a segment! " + segment.getName());
        }
        Iterator iter = getTranscriptionDescendants(segment).iterator(); //an iterator of the transcription NOMElements
        while (iter.hasNext()) {
            NOMElement nextTransEl = (NOMElement)iter.next();
            //System.out.println("highlighting another trans child of segment: " + nextTransEl.getName());
            setHighlighted(SELECTION_HIGHLIGHTS, 
                new NOMObjectModelElement(nextTransEl));
            selectedTransElements.add(nextTransEl);
        }
    }
/*=================================================================================================

                                    UTILITY
                                    
  =================================================================================================*/

    /**
     * Given ANY element,
     * return the NOMElement descendants that are transcripotion elements
     * (descendants in parent - child hierarchy)
     */    
    public Set getTranscriptionDescendants(NOMElement nwe) {
        Set result = new LinkedHashSet();
        if (isTranscriptionElement(nwe)) {
            result.add(nwe);
	    // Jonathan 17.2.05 - allow recursive transcrption layer 
	    boolean recurse=false;
	    try { recurse=nwe.getLayer().getRecursive();  } 
	    catch (Exception nex) { }
            if (!recurse) { return result; }
        }
        if (nwe.getChildren() != null) {
            Iterator it = nwe.getChildren().iterator();
            while (it.hasNext()) {
                NOMElement next = (NOMElement)it.next();
		result.addAll(getTranscriptionDescendants(next));
            }
        }
        return result;
    }

     
  


}
