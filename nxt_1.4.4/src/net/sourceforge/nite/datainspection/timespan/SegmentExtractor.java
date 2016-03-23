package net.sourceforge.nite.datainspection.timespan;

import net.sourceforge.nite.search.Engine;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteCorpus;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.gui.util.NOMElementStartTimeComparator;
import net.sourceforge.nite.util.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Collections;

/**
 * Given a lot of information about the NOM annotation layers in a NOMCorpus, extracts the 
 * 'segment' lists for a given annotator and returns it as a List. 
 *
 * See design on paper for documentation
 */
public class SegmentExtractor {

        final static Predicate falseP = new Predicate() { 
            public boolean valid(Object obj) {
                return false;
            }
        };
    
    public static ArrayList extractSegments(
                    NOMWriteCorpus corpus,           //this corpus is suposedly already loaded with the correct observation.
                    String coder,               //the name of the annotator whose annotation should be analysed
                    String codingName,          //the name of the Coding in which the segments are to be found
                    String segmentsLayer,       //the name of the Layer in that Coding in which the segments are to be found
                    String segmentElementName,  //the name of the Elements in the Layer in that Coding in which the segments are to be found
                    String agentName            //the name of the agent for which you want to analyse the annotations. If you want to analyse 
                                                //an interaction coding, this parameter should be null
                   ) {
        return extractSegments(corpus,coder,codingName,segmentsLayer,segmentElementName,agentName,falseP);
    }
    /** 
     Note: this extract method presumes a codingname, a segmentlayer name and an element name.
     This is because a layer can contain more than one type of element. If you want to include more or all element types 
     of a layer in your analysis you need to write a new extract method based on this one. */
    public static ArrayList extractSegments(
                    NOMWriteCorpus corpus,           //this corpus is suposedly already loaded with the correct observation.
                    String coder,               //the name of the annotator whose annotation should be analysed
                    String codingName,          //the name of the Coding in which the segments are to be found
                    String segmentsLayer,       //the name of the Layer in that Coding in which the segments are to be found
                    String segmentElementName,  //the name of the Elements in the Layer in that Coding in which the segments are to be found
                    String agentName,           //the name of the agent for which you want to analyse the annotations. If you want to analyse 
                                                //an interaction coding, this parameter should be null
                    Predicate isIgnoreP         //this predicate defines the difference between fore- and background elements. See package documentation.
                   ) {
            ArrayList segments = new ArrayList();
            
            //collect all NOMElements that are to be included
            String agentq = "";
            if (agentName != null) {
                agentq = "&&($a@"+corpus.getMetaData().getAgentAttributeName()+"='"+agentName+"')";
            }
            Iterator elemIt = search(corpus, "($a "+segmentElementName+"):($a@coder='"+coder+"')"+agentq).iterator();
            
            //for each element, create a boundary. checking for duplicates is done automatically, because the
            //boundaries are stored in a hashSet first. Duplicates are identified as being boundaries with 
            //the same time stamp.
            if (elemIt.hasNext()) {
                elemIt.next();  //first element is a list of some general search result variables
                while (elemIt.hasNext()) {
                    NOMWriteElement next = (NOMWriteElement)((List)elemIt.next()).get(0);
                    //don't extract ignore-elements
                    if ((isIgnoreP != null) && (isIgnoreP.valid(next))) continue;
                    //don't allow segments with wrong timings (start > end, or NaN values)
                    if (new Double(next.getStartTime()).toString().equals("NaN")) continue;
                    if (new Double(next.getEndTime()).toString().equals("NaN")) continue;
                    if (next.getStartTime()>next.getEndTime()) continue;
                    segments.add(next);
                }
            }
            //put in array, sort on start time, 
            Collections.sort(segments, new NOMElementStartTimeComparator());
            return segments;
    }

//=========================== SEARCH =============================================


   /** 
    *Search engine to find annotation elements 
    */
    protected static Engine searchEngine= new Engine();
    public static List search(NOMWriteCorpus nom, String query) {
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