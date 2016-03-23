/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import net.sourceforge.nite.search.*;
import java.util.List;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * A class which creates SimpleQueries from DeclarableVariables and MinimalConditions.
 * <p>
 * NOTE: Currently the class requires instantiation with a hash of DeclarableVariables 
 * (with the variable names as keys), and a list of MinimalConditions. It may be desirable
 * to operate without knowledge of these objects.
 * 
 */
public class SimpleQueryCreator implements ParserTreeConstants {

    /** The DeclarableVariables whose declarations should be distributed. */
    private Hashtable declarableVariables;
    /** The MinimalConditions to be distributed. */
    private List minimalConditions;


    /**
     * Create a new SimpleQueryCreator with a set of variables and a set of conditions.
     * 
     * @param variables Hashtable mapping names to DeclarableVariables
     * @param conditions list of MinimalConditions
     */
    public SimpleQueryCreator(Hashtable variables, List conditions) {
        declarableVariables = variables;
        minimalConditions = conditions;
    }


    /**
     * Create a SimpleQuery with the variables and conditions in the SubqueryTemplate.
     * 
     * @param sqt the SubqueryTemplate which contains the lists of variables and conditions which are to be incorporated
     * @return the new NodeSimpleQuery
     */
    protected NodeSimpleQuery createSimpleQuery(SubqueryTemplate sqt) {
        // we need to unwrap the DVs and MCs from the template lists
	if (!sqt.isComplete()) return null;
        return createSimpleQuery(sqt.getVariables(), sqt.getConditions());
    }


    /**
     * Convenience alias for creating a SimpleQuery with a single variable declaration.
     * 
     * @param singleVarToDeclare the name of the variable to be declared in this subquery
     * @param condition the condition node to be evaluated in this subquery
     * @return the new NodeSimpleQuery
     */
    protected NodeSimpleQuery createSimpleQuery(String singleVarToDeclare, SimpleNode condition) {
        List lv = new Vector();
        lv.add(singleVarToDeclare);
        List lc = new Vector();
        lc.add(condition);
        return createSimpleQuery(lv, lc);
    }


    /**
     * Convenience alias for creating a SimpleQuery with a single variable declaration.
     * 
     * @param singleVarToDeclare the name of the variable to be declared in this subquery
     * @param conditions a list of the condition nodes to be evaluated in this subquery
     * @return the new NodeSimpleQuery
     */
    protected NodeSimpleQuery createSimpleQuery(String singleVarToDeclare, List conditions) {
        List l = new Vector();
        l.add(singleVarToDeclare);
        return createSimpleQuery(l, conditions);
    }

    /**
     * Convenience alias for creating a SimpleQuery with a single condition.
     * 
     * @param varsToDeclare a list of names of the variables to be declared in this subquery
     * @param condition the condition node to be evaluated in this subquery
     * @return the new NodeSimpleQuery
     */
    protected NodeSimpleQuery createSimpleQuery(List varsToDeclare, SimpleNode condition) {
        List l = new Vector();
        l.add(condition);
        return createSimpleQuery(varsToDeclare, l);
    }

    /**
     * Create a SimpleQuery which declares the given variables and contains 
     * a conjunction of the given conditions.
     * 
     * @param varsToDeclare a list of names of the variables to be declared in this subquery
     * @param conditions a list of the condition nodes to be evaluated in this subquery
     * @return the new NodeSimpleQuery
     */
    protected NodeSimpleQuery createSimpleQuery(List varsToDeclare, List conditions) {
        //System.err.println("CREATING SIMPLE QUERY FROM "+condition+" vars "+varsToDeclare);
        // ---------------------------------------------------------------------
        // (1) Create a simple query 
        NodeSimpleQuery sq = new NodeSimpleQuery(JJTSIMPLEQUERY);
        for (int i=0; i<varsToDeclare.size(); i++) {
            String name = (String)varsToDeclare.get(i);
	    DeclarableVariable dv = (DeclarableVariable)declarableVariables.get(name);
            List types = dv.getTypes();
            //addVar(String name, boolean isExists, boolean isForAll);
            sq.addVar(name, dv.isExists(), dv.isForall());
            for (int j=0; j<types.size(); j++) sq.addType((String)types.get(j));
        }

        // ---------------------------------------------------------------------
        // (2) Create a top level NodeLogical for the SimpleQuery
        NodeLogical nl;
        // Use an existing NodeLogical
        if (conditions.size()==1 && conditions.get(0) instanceof NodeLogical) {
            nl = (NodeLogical)conditions.get(0);
	    // Or create a new one and add a NodeCondition
        } else {
            nl = new NodeLogical(JJTLOGICAL);
            for (int i=0; i<conditions.size(); i++) {
                SimpleNode condition = (SimpleNode)conditions.get(i);
                addConditionToLogicalNode(condition, nl);
            }
            // Make the DNF for the NodeLogical
            nl.makeDNF();
        }
        // (3) Add the NodeLogical to the NodeSimpleQuery
        sq.jjtAddChild(nl, 0);
        return sq;
    }


    /**
     * Add a minimal condition (SimpleNode) to the given NodeLogical. If the condition 
     * is a NodeCondition we add it directly; if it is a NodeLogical we must create a new 
     * test NodeCondition which contains the NodeLogical.
     * 
     * @param cond the SimpleNode representing the condition to be added to the logical node
     * @param nl  the NodeLogical to which to add the condition
     */
    private void addConditionToLogicalNode(SimpleNode cond, NodeLogical nl) {
        // Turn the condition into a NodeCondition:
        NodeCondition condition;
        if (cond instanceof NodeCondition) condition = (NodeCondition)cond;
        else {
            condition = new NodeCondition(JJTCONDITION);
            condition.setType(condition.TEST);
            condition.jjtAddChild((NodeLogical)cond, 0);
        }
        int numKids = nl.jjtGetNumChildren();
        nl.jjtAddChild(condition, numKids);
        if (numKids==0) nl.addOrElement(condition);
        else nl.addAndElement(condition);
    }




    /**
     * Here we just attempt to create a query by hand, to see how we 
     * might construct a new query representing the rewrite. We construct
     * the query:
     * <p>
     * (exists $e nt)($w word): $e@cat='NP' && $e^$w
     * 
     */
    protected static NodeQuery testCreateAQueryByHand() {
        // ---------------------------------------------------------------------
	// (0) Create a query 
        NodeQuery myQuery = new NodeQuery(JJTQUERY);
        
        // ---------------------------------------------------------------------
	// (1) Create a simple query 
        NodeSimpleQuery sq = new NodeSimpleQuery(JJTSIMPLEQUERY);
        //addVar(String name, boolean isExists, boolean isForAll);
        sq.addVar("$e", true, false);
        sq.addType("nt");
        sq.addVar("$w", false, false);
        sq.addType("word");

        // ---------------------------------------------------------------------
	// (2) Create a NodeLogical 
        NodeLogical nl = new NodeLogical(JJTLOGICAL);

        // ---------------------------------------------------------------------
        // (3) Add NodeConditions
        NodeCondition nc;

        // Condition 1: $e@cat='NP'
        nc = new NodeCondition(JJTCONDITION);
        nc.setType(nc.EQ);
        nc.setA( nc.ATTRIBUTE, "$e@cat");
        nc.setB( nc.VALUE, "\"NP\""); // note the quotes still around the compare string; this has been parsed from the input string
        nl.jjtAddChild(nc, 0);
        nl.addOrElement(nc);
        //System.err.println(nc.dump("NC1 "));

        // AND
        // Condition 2: $e^$w
        nc = new NodeCondition(JJTCONDITION);
        nc.setType(nc.DOMINANCE);
        nc.setAVar("$e");
        nc.setBVar("$w");
        nl.jjtAddChild(nc, 1);
        nl.addAndElement(nc);
        //System.err.println(nc.dump("NC2 "));


        // ---------------------------------------------------------------------
        // (4) Make the DNF for the NodeLogical
        nl.makeDNF();
        //System.err.println(nl.dump("NL "));

        // (show the nf vectors)
        //System.err.println("\n==============================\n  NORMAL FORMS                \n==============================");
        //System.err.println("\nDNF:\n  "+nl.dumpDNFVector(nl.dnf, ""));
        //System.err.println("\nDNF2:\n  "+nl.dumpDNFVector(nl.dnf2, ""));
        
        // ---------------------------------------------------------------------
        // (5) Add the NodeLogical to the NodeSimpleQuery
        sq.jjtAddChild(nl, 0);
        
        // ---------------------------------------------------------------------
        // (6) Add the NodeSimpleQuery to the main NodeQuery
        myQuery.jjtAddChild(sq, 0);
        
        //System.err.println(myQuery.dump("SQ "));
        return myQuery;
    }

}
