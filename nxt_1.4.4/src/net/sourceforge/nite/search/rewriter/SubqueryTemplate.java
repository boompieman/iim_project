/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import net.sourceforge.nite.search.*;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

/**
 * A class recording the information which will eventually be used to create a subquery Node 
 * for the new query. Merely records a list of the variables which must be declared, along
 * with a list of the conditions which should be evaluated.
 */
public class SubqueryTemplate {

    /** The variable names to be declared in this subquery. */
    private List variables = new Vector();
    /** The Node representations of the conditions to be conjoined and evaluated. */
    private List conditions = new Vector();
    /** 
     * The variable names which are used in the conditions evaluated in this subquery. 
     * Note that this is a superset of the variables <i>declared</i> in this subquery,
     * though not necessarily a proper superset (i.e. the sets may be identical). 
     */
    private List variablesUsed = new Vector();


    public SubqueryTemplate() {}
    public SubqueryTemplate(String var) {
	this.addVariable(var);
    }
    public SubqueryTemplate(String var, MinimalCondition mc) {
	//this(var, new Vector().add(mc));
	this(var);
	this.addCondition(mc);
    }
    public SubqueryTemplate(List vars, MinimalCondition mc) {
	this.addVariableList(vars);
	if (mc!=null) this.addCondition(mc);
    }
    public SubqueryTemplate(String var, List conds) {
	this(var);
	this.addConditionList(conds);
    }
    public SubqueryTemplate(List vars, List conds) {
	this.addVariableList(vars);	
	this.addConditionList(conds);
    }


    /** Add a variable name to the variable list. */
    protected void addVariable(String name) {
        variables.add(name);
    }
    protected void addVariableList(List names) {
	for (Iterator it = names.iterator(); it.hasNext(); ) {
	    this.addVariable((String)it.next());
	}
    }

    /** Remove a variable name from the variable list. */
    protected void removeVariable(String name) {
        variables.remove(name);
    }
    /** Remove a list of variables from the variable list. */
    protected void removeVariableList(List names) {
	for (Iterator it = names.iterator(); it.hasNext(); ) {
	    this.removeVariable((String)it.next());
	}
    }

    /** Add a Node to the condition list. */
    //protected void addCondition(Node node) {
    //    conditions.add(node);
    //}

    /** Add a Node to the condition list, and record the variables which are used in it. */
    protected void addCondition(MinimalCondition mc) {
        conditions.add(mc.getCondition());
        variablesUsed.addAll(mc.getVariableNames());
    }
    protected void addConditionList(List mcs) {
	for (Iterator it = mcs.iterator(); it.hasNext(); ) {
	    this.addCondition((MinimalCondition)it.next());
	}
    }


    /** 
     * Collapse another subquery into this one by transferring all its
     * variables and conditions. This method should be used when a pair
     * of subqueries do not allow for the consistent distribution of
     * conditions due to the order of variable declarations and a circular 
     * set of variable dependencies between the conditions. The 
     * second subquery should be discarded.
     * 
     * @param otherSubquery the SubqueryTemplate which is to be collapsed into this one
     */
    protected void collapseSubquery(SubqueryTemplate otherSubquery) {
        this.variables.addAll(otherSubquery.getVariables());
        this.conditions.addAll(otherSubquery.getConditions());
    }

    /** Getter method for the variable names list. */
    protected List getVariables() {return variables;}
    /** Getter method for the condition nodes list. */
    protected List getConditions() {return conditions;}

    /** 
     * Returns the list of all the variables which are used in the conditions, and which must 
     * therefore have been declared within this subquery or those preceding it.
     */
    protected List getVariablesUsed() {
        return variablesUsed;
    }


    /** Whether this query is complete and ready for creation; in other words has both variables and conditions. */
    protected boolean isComplete() {
        return (variables.size()>0 && conditions.size()>=0);
    }


    //public String toString() {
    //return "MC "+condition.toString();
    //}

}
