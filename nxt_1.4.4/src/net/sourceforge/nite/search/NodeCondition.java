package net.sourceforge.nite.search;

//import net.sourceforge.nite.nomread.*;
import java.util.List;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/* Generated By:JJTree: Do not edit this line. NodeCondition.java */

/**
 * Generated by the
 * <a href="http://www.webgain.com/products/java_cc/" target="_blank">JavaCC</a>
 * tool JJTree.
 */
public class NodeCondition
    extends SimpleNode
{
    public NodeCondition(int id) { super(id); }
    public NodeCondition(Parser p, int id) { super(p, id); }

    /* Extended manual ... */

    private List types;
    public NodeCondition(String variable, List types)
    {
        super(0);
        aType = TYPE_IS;
        this.types = types;
        setAVar(variable);
    }

    public boolean isGroup = false,
        isNegated = false;


    /**
     * 
     * Called by Parser and NodeLogical.negateDNF() via NodeLogical.makeDNF() via Parser
     */
    public void negate()
    {
        if (isGroup) {
            /// NM 2006 record the original negated state of the group
            hasBeenNegated = !hasBeenNegated;
            ((NodeLogical)jjtGetChild(0)).negateDNF();
            ((NodeLogical)jjtGetChild(0)).negateDNF2();
            //System.out.println("[ GROUP HAS BEEN NEGATED "+(++callsToNegate)+" ]");
        } else {
            isNegated = !isNegated;
        }
     }
    /// NM 2006: added this so we can keep track of which logical relationships in dnf2 have been inverted
    private int callsToNegate = 0;
    public boolean hasBeenNegated = false;

    public static final int //attribute tests
        TEST  = 0,
        EQ    = 1,
        EQV   = 2,
        NE    = 3,
        NEV   = 4,
        GE    = 5,
        LE    = 6,
        GT    = 7,
        LT    = 8,
        REGEX = 9,
        NOTREGEX = 10,
        //equality
        EQUAL                   = 11,
        INEQUAL                 = 12,
        //structural relations
        DOMINANCE               = 21,
        DOMINANCE_WITH_DISTANCE = 22,
        PRECEDENCE              = 23,
        POINTER                 = 24,
        POINTER_WITH_ROLE       = 25,
        SUBDOM                  = 26,
        SUBDOM_WITH_ROLE        = 27,
        //temporal relations
        TIMED                   = 30,
        OVERLAPS_LEFT           = 31,
        LEFT_ALIGNED_WITH       = 32,
        RIGHT_ALIGNED_WITH      = 33,
        INCLUDES                = 34,
        SAME_EXTENT_AS          = 35,
        OVERLAPS_WITH           = 36,
        CONTACT_WITH            = 37,
        PRECEDES                = 38,
        //the type (= name of element) is
        TYPE_IS                 = 39;

    private int type = TEST;
    private String role;
    private Comparable valueA, valueB;
    private int a = 0, b = 0;  //tuple-position of var
    private boolean isAValue = false,
        isBValue = false;
    private int distance;

    public static final int VALUE     = 0,
        ATTRIBUTE = 1,
        TEXT      = 2,
        START     = 3,
        END       = 4,
        DURATION  = 5,
        CENTER    = 6,
        ID        = 7;

    private int aAttrType = ATTRIBUTE,
        bAttrType = ATTRIBUTE;
    private Pattern pattern = null;

    public void setRole(String name) { role  = name.substring(1, name.length()-1 ); }
    public String setAVar(String name) { aVariable = name; return name; }
    public String setBVar(String name) { bVariable = name; return name; }
    public String getAVar() { return aVariable; }
    public String getBVar() { return bVariable; }



    public void setPattern(String p)   { pattern = Pattern.compile( p.substring(1, p.length()-1 )); }

    public void setDistance(String distance)
    {
        this.distance = Integer.parseInt(distance);
    }
    public int getDistance(){ return distance; }

    private String getAtr(String varAtr)
    {
        return varAtr.substring( varAtr.indexOf('@')+1 );
    }
    private String getNode(String varAtr)
    {
        return varAtr.substring( 0, varAtr.indexOf('@') );
    }

    public void setTuple(String[] variableNames) {
        for (int i=0; i<variableNames.length; i++) {
            if ( variableNames[i].equals(aVariable) ) { a = i; }
            if ( variableNames[i].equals(bVariable) ) { b = i; }
        }
    }
    public int  getType() { return type; }
    public void setType(int type) {
        this.type = type;
        if (isAValue) {
            switch (type) {
            case EQ:  this.type = EQV;
                break;
            }
        }
    }


    private Comparable aComparable = null;
    private Comparable bComparable = null;
    private String     aVariable   = "";
    private String     bVariable   = "";
    private String     aAttribute  = "";
    private String     bAttribute  = "";
    private int        aType       = -1;
    private int        bType       = -1;

    public String setA(int type, String value) { return set(true,  type, value); }
    public String setB(int type, String value) { return set(false, type, value); }
    private String set(boolean isA, int type, String value) {

        String ret = null;

        if( isA ){ aType = type; } else { bType = type; }

        // value
        if( type == VALUE ){
            // "valueText" -> valueText
            ret = value.substring(1, value.length()-1 );
            Comparable c;
            try {
                c = (Comparable)(new Double(ret));
            } catch (NumberFormatException e) {
                c = (Comparable)ret;
	    }
            if( isA ){ aComparable = c; } else { bComparable = c; }
            // attribute
        } else if( type == ATTRIBUTE ) {
            // varible part: <$x>@attribue
            String x = getNode(value);
            ret = x;
            if( isA ){ setAVar(x); } else { setBVar(x); }
            // attribute part: $variable@<x>
            x = getAtr(value);
            if( isA ){ aAttribute = x; } else { bAttribute = x; }

            // text, start, end, id, timed
        } else {
            ret = value;
            if( isA ){ setAVar(value); } else { setBVar(value); }
        }

        return ret;
    }

    /** utility by JAK 20/3/08 - get an attribute from a Searchable
     * corpus in a way that will make it as comparable as possible
     * with the provided value (providedVal) */
    private Comparable getCompatibleAttribute(SearchableCorpus corpus, Object element, String attribute, Comparable providedVal) {
	Comparable ret=null;
	if (providedVal!=null) {
	    if (providedVal instanceof Double) {
		ret = corpus.getAttributeDoubleValue(element, attribute);
	    } else {
		ret = corpus.getAttributeStringValue(element, attribute);
	    }
	} 
	if (ret==null) { // get whatever the metadata-declared type is 
	    ret = corpus.getAttributeComparableValue(element, attribute);
	}
	return ret;
    }

    public boolean ask(Object[] tuple, SearchableCorpus corpus)
    {
        switch( aType ){
        case TYPE_IS:   return types.contains( corpus.getNameOfElement(tuple[a]) );
        case VALUE:     break;
        case ATTRIBUTE: aComparable = getCompatibleAttribute(corpus, tuple[a], aAttribute, bComparable);
            break;
        case START:     aComparable = corpus.getStartComparableValue( tuple[a] );
            break;
        case END:       aComparable = corpus.getEndComparableValue( tuple[a] );
            break;
        case DURATION:  aComparable = corpus.getDurationComparableValue( tuple[a] );
            break;
        case CENTER:    aComparable = corpus.getCenterComparableValue( tuple[a] );
            break;
        case TEXT:      aComparable = corpus.getText( tuple[a] );
            break;
        case ID:        aComparable = corpus.getIdComparableValue( tuple[a] );
            break;
        }
        switch( bType ){
        case VALUE:     break;
        case ATTRIBUTE: bComparable = getCompatibleAttribute(corpus, tuple[b], bAttribute, aComparable);
            break;
        case START:     bComparable = corpus.getStartComparableValue( tuple[b] );
            break;
        case END:       bComparable = corpus.getEndComparableValue( tuple[b] );
            break;
        case DURATION:  bComparable = corpus.getDurationComparableValue( tuple[b] );
            break;
        case CENTER:    bComparable = corpus.getCenterComparableValue( tuple[b] );
            break;
        case TEXT:      bComparable = corpus.getText( tuple[b] );
            break;
        case ID:        bComparable = corpus.getIdComparableValue( tuple[b] );
            break;
        }
        boolean ret;
        switch (type) {
        case EQ:    if ( (aComparable != null) && (bComparable != null) ) {
            ret = aComparable.equals(bComparable);
        } else {
            ret = false;
        }
            break;
        case NE:    if ( (aComparable != null) && (bComparable != null) ) {
            ret = !aComparable.equals(bComparable);
        } else {
            ret = false;
        }
            break;
        case EQV:   ret = aComparable.equals(bComparable);
            break;
        case GE:    try {
            ret = aComparable.compareTo(bComparable) >= 0 ;
        } catch (RuntimeException e) {
            ret = false;
        }
            break;
        case LE:    try {
            ret = aComparable.compareTo(bComparable) <= 0 ;
        } catch (RuntimeException e) {
            ret = false;
        }
            break;
        case GT:    try {
            ret = aComparable.compareTo(bComparable) > 0 ;
        } catch (RuntimeException e) {
            ret = false;
        }
            break;
        case LT:    try {
            ret = aComparable.compareTo(bComparable) < 0 ;
        } catch (RuntimeException e) {
            ret = false;
        }
            break;
        case REGEX: try {
            Matcher m = pattern.matcher( aComparable.toString() );
            ret = m.matches() ;
        } catch (RuntimeException e) {
            ret = false;
        }
            break;
        case NOTREGEX: try {
            Matcher m = pattern.matcher( aComparable.toString() );
            ret = !m.matches() ;
        } catch (RuntimeException e) {
            ret = false;
        }
            break;

            //equality
        case EQUAL:                   ret = corpus.testIsEqual(tuple[a], tuple[b]);
            break;
        case INEQUAL:                 ret = corpus.testIsInequal(tuple[a], tuple[b]);
            break;

            //strucural relations
        case DOMINANCE:               ret = corpus.testDominates(tuple[a], tuple[b]);
            break;
        case DOMINANCE_WITH_DISTANCE: ret = corpus.testDominates(tuple[a], tuple[b], distance);
            break;
        case PRECEDENCE:              ret = corpus.testPrecedes(tuple[a], tuple[b]);
            break;
        case POINTER:                 ret = corpus.testHasPointer(tuple[a], tuple[b]);
            break;
        case POINTER_WITH_ROLE:       ret = corpus.testHasPointer(tuple[a], tuple[b], role);
            break;
        case SUBDOM:                  ret = corpus.testDominatesSubgraph(tuple[a], tuple[b]);
            break;
        case SUBDOM_WITH_ROLE:        ret = corpus.testDominatesSubgraph(tuple[a], tuple[b], role);
            break;

            //temporal relations
        case TIMED:                   ret = corpus.testTimed(tuple[a]);
        case OVERLAPS_LEFT:           ret = corpus.testOverlapsLeft(tuple[a], tuple[b]);
            break;
        case LEFT_ALIGNED_WITH:       ret = corpus.testLeftAlignedWith(tuple[a], tuple[b]);
            break;
        case RIGHT_ALIGNED_WITH:      ret = corpus.testRightAlignedWith(tuple[a], tuple[b]);
            break;
        case INCLUDES:                ret = corpus.testIncludes(tuple[a], tuple[b]);
            break;
        case SAME_EXTENT_AS:          ret = corpus.testSameExtend(tuple[a], tuple[b]);
            break;
        case OVERLAPS_WITH:           ret = corpus.testOverlapsWith(tuple[a], tuple[b]);
            break;
        case CONTACT_WITH:            ret = corpus.testContactWith(tuple[a], tuple[b]);
            break;
        case PRECEDES:                ret = corpus.testPrecedesTemporal(tuple[a], tuple[b]);
            break;

        default:                      ret = (aComparable != null);
        }
        return isNegated ? !ret : ret;
    }


    /**
     * Override the SimpleNode method to also show whether the Condition is negated,
     * whether it is a group condition, and to elaborate the type of relation and its variables.
     * 
     * @author nmayo@inf.ed.ac.uk
     */
    public String toString() {
        String ret = (isNegated?"NOT ":"")+(isGroup?"Group ":"")+ParserTreeConstants.jjtNodeName[id];
        // TODO: If one var, write {var} {type} {varComparable}
        // If two vars, write {varA} {type} {varB}        
        ret += " ("+typeList[type]+" ";
        // add details of variable a
        if (!aVariable.equals("")) {
            ret += aVariable;
            if (!aAttribute.equals("")) ret+= "@"+aAttribute;
            if (type==REGEX) ret += " '"+pattern+"'";
        }
        if (aComparable!=null) ret += " "+aComparable;
        // add details of variable b
        if (!bVariable.equals("")) {
            if (!aVariable.equals("")) ret += ", ";
            ret += bVariable;
            if (!bAttribute.equals("")) ret+= "@"+bAttribute;
        }
        if (bComparable != null) ret+= " "+bComparable;

        // add types info if it exists (what is this?)
        if (types != null) {
            ret += " - types ";
            for (Iterator f=types.iterator(); f.hasNext(); ) {
                //ret += aVariable+" ";
                ret += typeList[Integer.parseInt(f.next().toString())];
                //ret += bVariable+" ";
                if ( f.hasNext() ) { ret += ", "; };
            }
        }
        ret += ")";
        // the image is an (incomplete) representation of the relation
        //if (image!=null) ret += "  ["+image+"]";
        return ret;
    }

    /// NM 2006: I've enumerated the types here for use in dump()
    private String[] typeList = new String[40];
    {
        typeList[0] = "TEST";
        typeList[1] = "EQ";
        typeList[2] = "EQV";
        typeList[3] = "NE";
        typeList[4] = "NEV";
        typeList[5] = "GE";
        typeList[6] = "LE";
        typeList[7] = "GT";
        typeList[8] = "LT";
        typeList[9] = "REGEX";
        typeList[10] = "NOTREGEX";
        //equality
        typeList[11] = "EQUAL";
        typeList[12] = "INEQUAL";
        //structural relations
        typeList[21] = "DOMINANCE";
        typeList[22] = "DOMINANCE_WITH_DISTANCE";
        typeList[23] = "PRECEDENCE";
        typeList[24] = "POINTER";
        typeList[25] = "POINTER_WITH_ROLE";
        typeList[26] = "SUBDOM";
        typeList[27] = "SUBDOM_WITH_ROLE";
        //temporal relations
        typeList[30] = "TIMED";
        typeList[31] = "OVERLAPS_LEFT";
        typeList[32] = "LEFT_ALIGNED_WITH";
        typeList[33] = "RIGHT_ALIGNED_WITH";
        typeList[34] = "INCLUDES";
        typeList[35] = "SAME_EXTENT_AS";
        typeList[36] = "OVERLAPS_WITH";
        typeList[37] = "CONTACT_WITH";
        typeList[38] = "PRECEDES";
        //the type (= name of element) is
        typeList[39] = "TYPE_IS";
    }

}
