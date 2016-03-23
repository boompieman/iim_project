package net.sourceforge.nite.datainspection.timespan;

import net.sourceforge.nite.datainspection.data.Classification;
import net.sourceforge.nite.datainspection.data.Value;
import net.sourceforge.nite.datainspection.data.Item;
import net.sourceforge.nite.datainspection.data.NOMElementToValueDelegate;
import net.sourceforge.nite.datainspection.impl.BooleanValue;
import net.sourceforge.nite.datainspection.impl.TimespanItem;
import net.sourceforge.nite.util.Pair;
import net.sourceforge.nite.nom.nomwrite.NOMElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class makes classifications from an annotation by discretizing the timeline into items
 * and assigning Values to each item based on the NomElementToValueDelegate applied to the annotation
 * element with the largest time overlap with the item.
 * <p>
 * This class works in two step:
 * <br>1) make a list of items ('time spans') that are to be used for all annotators
 * <br>2) given the list of Items, and a segmentlist, and a NomElementToValueDelegate, generate a Classification.
 *
 */
public class TimelineDiscretizationClassificationFactory {
    
    /** Make a list of items ('time spans') that are to be used for all annotators by
    discretizing the timeline from 0 to endtime (usually obtained from getCorpusEndTime)
    in segments of 'th' duration. */
    public static List generateDiscretizedItems(double endtime, double th) {
        ArrayList result = new ArrayList();
        double s = 0;
        while (s < endtime) {
            result.add(new TimespanItem(s, s+th));
            s+=th;
        }
        return result;
    }
    
    
    /** Given the list of Items generated by generateDiscretizedItems, and a segmentlist, 
    and a NomElementToValueDelegate, generate a Classification. The segmentlist is assumed to be sorted
    on start time. */
    public static Classification makeClassification(
                    List items, 
                    List segmentList,
                    NOMElementToValueDelegate nomElementToValue
                   ) {
            Classification c = new Classification("");
            //prepare to investigate the (sorted) segments
            Iterator segmentIt = segmentList.iterator();
            NOMElement nextSegment = null;
            if (segmentIt.hasNext()) {
                nextSegment = (NOMElement)segmentIt.next();
                //for each item in the list, generate a value, and extend the classification.
                for (int i = 0; i < items.size(); i++) {
                    //get next item
                    TimespanItem item = (TimespanItem)items.get(i);
                    //skip to next segment if this segment is no longer covering at least the first 50% of the item
                    while (nextSegment.getEndTime() < (item.start+item.end)/2d) { //uses 'while' instead of 'if', since it may be possible that there are several too-small-elements that all don't cover enough of the 'th' long item. Note though, if this happens, 'th' was probably badly chosen.
                        if (!segmentIt.hasNext())break;
                        nextSegment = (NOMElement)segmentIt.next();
                    }
                    //check if current segment covers at least 50% of the item. If so, create a Item,Value pair from this segment and add it to the classification
                    if (  (   (Math.min(nextSegment.getEndTime(), item.end)-Math.max(nextSegment.getStartTime(), item.start)) 
                            * 2 ) 
                          > (item.end-item.start) ) {
                        Value val = nomElementToValue.getValueForNOMElement(nextSegment);
                        c.add(item,val);
                    } else {
                        Value val = nomElementToValue.getGapValue();
                        c.add(item,val);
                    }
                }
            }
            return c;
    }

}