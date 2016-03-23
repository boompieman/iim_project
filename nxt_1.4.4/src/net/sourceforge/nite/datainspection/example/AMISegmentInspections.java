package net.sourceforge.nite.datainspection.example;
import net.sourceforge.nite.datainspection.timespan.DiscretizedTimelineInspection;
import net.sourceforge.nite.datainspection.timespan.SegmentBasedInspection;
import net.sourceforge.nite.datainspection.timespan.QuickScan;
import net.sourceforge.nite.datainspection.impl.BooleanMetric;
import net.sourceforge.nite.datainspection.impl.StringValue;
import net.sourceforge.nite.datainspection.data.NOMElementToValueDelegate;
import net.sourceforge.nite.datainspection.data.Value;

import net.sourceforge.nite.util.Predicate;
import net.sourceforge.nite.util.NOMElementToTextDelegate;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;


public class AMISegmentInspections {
        final static Predicate trueP = new Predicate() { 
            public boolean valid(Object obj) {
                return true;
            }
        };
        final static Predicate falseP = new Predicate() { 
            public boolean valid(Object obj) {
                return false;
            }
        };
        final static Predicate handIgnoreP = new Predicate() { 
            public boolean valid(Object obj) {
                NOMWriteElement nwe = (NOMWriteElement)obj;
                //IA_hand ignore if type is no_comm_hand or off_camera
                String type = (String)nwe.getAttributeComparableValue("type");
                if (type.equals("off_camera") || type.equals("no_comm_hand")) return true;
                return false;
            }
        };
    
    static final NOMElementToTextDelegate foalabelsToString = new NOMElementToTextDelegate() { /* NOMElementToTextDelegate, for views and for tables and for confusions... a delegate from 'segment' to a textual description of it. */
            public String getTextForNOMElement(NOMElement nme){
                String type = (String)nme.getAttributeComparableValue("type");
                if (type.equals("person"))      return "person: " + (String)nme.getAttributeComparableValue("role"); 
                if (type.equals("place"))       return "place: " + (String)nme.getAttributeComparableValue("place"); 
                if (type.equals("unspecified")) return "unspecified"; 
                return "error";
            }
    };
    static final NOMElementToTextDelegate hglabelsToString = new NOMElementToTextDelegate() {
            public String getTextForNOMElement(NOMElement nme){
                return (String)nme.getAttributeComparableValue("type");
            }
    };
    static final NOMElementToValueDelegate hglabelsToValue = new NOMElementToValueDelegate() {
            public Value getGapValue() {
                return new StringValue("<<GAP>>");
            }
            public Value getValueForNOMElement(NOMElement nme){
                return new StringValue((String)nme.getAttributeComparableValue("type"));
            }
    };
    static final NOMElementToValueDelegate foalabelsToValue = new NOMElementToValueDelegate() {
            public Value getGapValue() {
                return new StringValue("<<GAP>>");
            }
            public Value getValueForNOMElement(NOMElement nme){
                return new StringValue(foalabelsToString.getTextForNOMElement(nme));
            }
    };
    
    public static void main(String[] args) {
        if (args.length <=0) {
            System.out.println("Usage: AMISegmentInspection <corpus>");
            System.exit(0);
        }
        //one test... eventually you want all cross coded meetings, all agents, all relevant layers to be called like this...
        SegmentBasedInspection inspection = 
           new SegmentBasedInspection(
                       args[0], /* corpus */
                       "IS1008a",           /* observation */
                       "hand",              //the name of the Coding in which the boundaries are to be found
                       "hand-layer",        //the name of the Layer in that Coding in which the boundaries are to be found
                       "hand",              //the name of the Elements in the Layer in that Coding in which the boundaries are to be found
                       "A",                 //the name of the agent for which you want to analyse the annotations. If you want to analyse 
                                            //an interaction coding, this parameter should be null
                       hglabelsToString,
                       handIgnoreP,         //used to determine whether a segment is 'foreground' or 'background' (see package documentation)
                       hglabelsToValue, 
                                            //used to get a Value for each Item (see the datainspection package documentation for the use of Values and Items)
                       new BooleanMetric(),
                       0.5,               //thMin, thMax and thSteps together determine which threshold variations will be used to attempt 
                       2.5,               //to align two boundary annotations.
                       3
                            );
//        DiscretizedTimelineInspection inspection = 
//           new DiscretizedTimelineInspection(
//                       args[0], /* corpus */
//                       "IS1008a",           /* observation */
//                       "hand",              //the name of the Coding in which the boundaries are to be found
//                       "hand-layer",        //the name of the Layer in that Coding in which the boundaries are to be found
//                       "hand",              //the name of the Elements in the Layer in that Coding in which the boundaries are to be found
//                       "A",                 //the name of the agent for which you want to analyse the annotations. If you want to analyse 
//                                            //an interaction coding, this parameter should be null
//                       hglabelsToString,
//                       handIgnoreP,         //used to determine whether a segment is 'foreground' or 'background' (see package documentation)
//                       hglabelsToValue, 
//                                            //used to get a Value for each Item (see the datainspection package documentation for the use of Values and Items)
//                       new BooleanMetric(),
//                       0.1d,//0.5               //thMin, thMax and thSteps together determine which threshold variations will be used to attempt 
//                       0.5d,//2.5               //to align two boundary annotations.
//                       5//2
//                            );


//        DiscretizedTimelineInspection inspection = 
//           new DiscretizedTimelineInspection(
//                       args[0], /* corpus */
//                       "IS1001a",           /* observation */
//                       "foa",              //the name of the Coding in which the boundaries are to be found
//                       "foa-layer",        //the name of the Layer in that Coding in which the boundaries are to be found
//                       "foa",              //the name of the Elements in the Layer in that Coding in which the boundaries are to be found
//                       "A",                 //the name of the agent for which you want to analyse the annotations. If you want to analyse 
//                                            //an interaction coding, this parameter should be null
//                       foalabelsToString,
//                       falseP,         //used to determine whether a segment is 'foreground' or 'background' (see package documentation)
//                       foalabelsToValue, 
//                                            //used to get a Value for each Item (see the datainspection package documentation for the use of Values and Items)
//                       new BooleanMetric(),
//                       0.1d,               //thMin, thMax and thSteps together determine which threshold variations will be used to attempt 
//                       0.5d,               //to align two boundary annotations.
//                       5
//                            );


    }

}