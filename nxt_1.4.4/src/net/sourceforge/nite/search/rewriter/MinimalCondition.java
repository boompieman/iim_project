/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import net.sourceforge.nite.search.*;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A class encapsulating details about a condition (NodeLogical or NodeCondition)
 * which is to be assigned to a new SimpleQuery during a query rewrite. This will be 
 * done by the ConditionDistributor. Records the condition along with the variables 
 * involved in it, and a boolean indicating whether or not this condition has been 
 * distributed to a new query.
 */
public class MinimalCondition {

	/** The condition to be distributed. */
    private SimpleNode condition;
	/** The variable names involved in this condition. */
    private HashMap variables = new HashMap();
    /** Whether this condition has been distributed to a new SimpleQuery yet. */
    protected boolean hasBeenDistributed = false;


    /**
     * Initialise the object with a condition. This may be an atomic
     * NodeCondition or a grouping of conditions under a NodeLogical.
     * 
     * @param cond a SimpleNode representing the condition to be encapsulated
     */
    public MinimalCondition(SimpleNode cond) {
        this.condition = cond;
        // get the vars involved in this condition
		readVariables(condition);
    }


    /**
     * Inspects the structure of the condition node recursively 
     * to see which variables are used within the expression, 
     * and adds their names to the list.
     * 
     * @param cond the condition node whose variables are to be read
     */
    private void readVariables(Node cond) {
        // find all the subconditions and get their var info
        if (cond instanceof NodeLogical) {
            // do nothing
        } else if (cond instanceof NodeCondition) {
            NodeCondition nc = (NodeCondition)cond;
            /// NOTE We may have to get a CVar also if ternary operators are ever implemented
            addVariable( nc.getAVar() );
            addVariable( nc.getBVar() );
        }
        int kids = cond.jjtGetNumChildren();
        for (int i=0; i<kids; i++) readVariables(cond.jjtGetChild(i));
    }

    /**
     * Adds a variable name to the list of variables in this condition.
     *
     * @param var the name of the variable to add
     */
    public void addVariable(String var) { 
        if (!var.equals("")) variables.put(var, null);
    }

    /**
     * Adds a DeclarableVariable to the list of DeclarableVariables in this condition.
     *
     * @param dv a DeclarableVariable object to add
     */
    public void addVariable(DeclarableVariable dv) { 
        variables.put(dv.getName(), dv);
    }


    /**
     * Returns the number of variables involved in this condition.
     * 
     * @return the number of variables used in this condition
     */
    public int getCardinality() { return variables.size(); }


    /**
     * Returns the names of the variables used in this condition.
     * 
     * @return a List of variable names
     */
    public List getVariableNames() {
        //return new Vector(variables);
        return new Vector(variables.keySet());
    }

    /**
     * Returns the condition node represented by this MinimalCondition.
     * 
     * @return the condition node
     */
    public SimpleNode getCondition() { return condition; }


//      public List getUndeclaredVariableNames() {
//          Vector vars = new Vector();
//          for (Iterator it = variables.values().iterator(); it.hasNext(); ) {
//              DeclarableVariable dv = (DeclarableVariable)it.next();
//              if (dv.hasBeenDeclared(...)) 
//          }
//          return vars;
//      }


    /**
     * Returns the names of all the DeclarableVariable objects which are of the 
     * given cardinality (those which appear in n conditions).
     * 
     * @param n the cardinality required of the variables
     * @return a List of names
     */
    public List getVariablesOfCardinality(int n) {
        List vars = new Vector();
        // check each variable and add to the list if it is of the required cardinality
        for (Iterator it = variables.values().iterator(); it.hasNext(); ) {
            DeclarableVariable dv = (DeclarableVariable)it.next();
            if (dv.getCardinality()==n) vars.add(dv.getName());
        }
        return vars;
    }


    public String toString() {
        return "MC "+condition.toString();
    }


}
