/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import net.sourceforge.nite.search.*;
import net.sourceforge.nite.util.Debug;
import java.util.List;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;

/**
 * This implementation first creates skeleton subqueries without conditions, for any undeclared variables; 
 * orders them by the summed frequencies of the variables declared in each; then distributes the remaining 
 * conditions by placing them as early as possible in the sequence of subqueries. If it is impossible to 
 * place a condition C in a subquery which a) declares a variable used in the condition; b) has no condition; 
 * and c) along with its predecessors declares all the variables required by C; then two subqueries must be 
 * collapsed into one. This is a simple way of avoiding a potential circular dependency between the 

 and declarations in the correct order, 
 * 
 * @author nmayo@inf.ed.ac.uk
 */
public class DefaultConditionDistributor extends ConditionDistributorTemplate {

    /**
     * Initialise the class with a set of minimal conditions. These may be atomic
     * conditions or groupings of conditions.
     * 
     * @param conds a List of MinimalCondition objects representing SimpleNodes which should be distributed
     * @param vars a Hashtable mapping each variable name to a List of types
     */
    public DefaultConditionDistributor(List conds, Hashtable vars) {
        super(conds, vars);
    }



    // This method should be used to ensure there are declarations for all variables,
    // if that is required before distributeRemainingConditions() is called
    protected void declareRemainingVariables() { /* UNIMPLEMENTED */ }



    /**
     * Distribute the conditions and variable declarations across a
     * new set of SimpleQueries, returning the resultant complex query.
     * 
     * @param orderingScheme a DeclarationOrderingScheme to use in ordering subqueries
     * @return a new query
     */
    public void distributeRemainingConditions() {
        // (C) Order the SimpleQueries and distribute the remaining conditions
        // go over the SQs, calculating a score based on the vars involved, and order them accordingly
        // we must also take into account the variables declared in each query, which must be declared in the correct order
        /*
	  for (Iterator it = simpleQueries.iterator(); it.hasNext(); ) {
	  NodeSimpleQuery sq = (NodeSimpleQuery)it.next();
	  // add the new SimpleQuery to the main Query:
	  newQuery.jjtAddChild(sq, newQuery.jjtGetNumChildren());
	  }
        */
    }

    // This is called after distributeRemainingConditions() - apply
    // any further ordering we wish to the subqueries; however the
    // ordering and distribution are likely to be intertwined, and
    // thus ordering will probably be done within the
    // distributeRemainingConditions() method.
    protected void applyOrdering() { /* UNIMPLEMENTED */ }

    // Override this if there is anything incomplete about the subqueries
    //protected void finaliseSubqueries() { }

    /** Distribute all remaining conditions and variables. We expect
     * the floaters, undeclaredVariables and undistributedConditions
     * Lists to be fully processed and emptied, and the subqueryList
     * to be updated accordingly. */
    protected void processRemainingConditions() {
	// Add the Single-occurrence variable queries to the end of
	// the query, in preferred order.
	int size = floaters.size(); 

	Debug.print("Declared: " + declaredvars+"; quantified: " + quantifiedVariables, Debug.DEBUG);

	while (size>0) {
	    int lastsize=size;
	    // Look for subqueries where all vars used plus all vars
	    // declared within the subquery are sufficient for
	    // evaluation. Catch the pathological case where already
	    // declaredvars is a superset of the ones declared in this
	    // query. In this case split the subquery and add the
	    // conditions to the undistributed list.
	    for (int i=0; i<size; i++) {
		SubqueryTemplate sqt = (SubqueryTemplate)floaters.get(i);
		if (!containsAny(sqt.getVariablesUsed(), quantifiedVariables)) {
		    if (allVarsDeclared(declaredvars,sqt)) {
			if (declaredvars.containsAll(sqt.getVariables())) {
			    Debug.print("Split conditions for " + sqt.getConditions(), Debug.DEBUG);
			    for (Iterator cit=sqt.getConditions().iterator(); cit.hasNext(); ) {
				MinimalCondition cc = new MinimalCondition((SimpleNode)cit.next());
				undistributedConditions.add(cc);
			    }
			} else {
			    sqt.removeVariableList(declaredvars);
			    subqueryList.add(sqt);
			    declaredvars.addAll(sqt.getVariables());
			    Debug.print("FIRST LOOP ADD " + sqt.getVariables(), Debug.DEBUG);
			}
			floaters.remove(i);
			size--;
			break;
		    }
		}
	    }
	    if (lastsize==size && lastsize>0) {
		// This time around we can include one or even more
		// undeclared variables
		for (int i=0; i<size; i++) {
		    SubqueryTemplate sqt = (SubqueryTemplate)floaters.get(i);
		    if (!containsAny(sqt.getVariablesUsed(), quantifiedVariables)) {
			Debug.print(sqt.getVariablesUsed()+" contains no quantified vars ("+quantifiedVariables+")", Debug.DEBUG);
			if (declareAllVars(declaredvars,sqt,undeclaredVariables)) {
			    // make a new SimpleQuery and add to the main Query:
			    //NodeSimpleQuery sq = queryCreator.createSimpleQuery(sqt);
			    //if (sq!=null) newQuery.jjtAddChild(sq, newQuery.jjtGetNumChildren());
			    // just add to the subquerylist to be created later (we may still edit)
			    sqt.removeVariableList(declaredvars);
			    subqueryList.add(sqt);
			    declaredvars.addAll(sqt.getVariables());
			    Debug.print("SECOND LOOP ADD " + sqt.getVariables(), Debug.DEBUG);
			    floaters.remove(i);
			    size--;
			    break;
			}
		    }
		}
	    }
	    if (lastsize==size && lastsize>0) {
		// admit defeat - the only reason is that we use
		// quantified variables, so break up the simple query
		// into its constituents and add at the end.
		Debug.print("Failed to place " + lastsize + " floating condition(s) due to quantifiers. Splitting for inclusion later.", Debug.DEBUG);
		for (int i=size-1; i>=0; i--) {
		    SubqueryTemplate sqt = (SubqueryTemplate)floaters.get(i);
		    if (containsAny(sqt.getVariablesUsed(), quantifiedVariables)) {
			for (Iterator cit=sqt.getConditions().iterator(); cit.hasNext(); ) {
			    MinimalCondition cc = new MinimalCondition((SimpleNode)cit.next());
			    undistributedConditions.add(cc);
			}
			sqt.getVariables().removeAll(declaredvars);
			sqt.getVariables().removeAll(quantifiedVariables);
			undeclaredVariables.addAll(sqt.getVariables());
			floaters.remove(i);
			size--;
		    }
		}
		//System.exit(1);
	    }
        }

	Debug.print("Declared: " + declaredvars+"; quantified: " + quantifiedVariables, Debug.DEBUG);

	// Now place any remaining variables where they are first
	// used. If there are variables that are not used so far, make
	// a new subquery and stick it at the end.
	if (undeclaredVariables.size()>0) {
	    for (Iterator uvi=undeclaredVariables.iterator(); uvi.hasNext(); ) {
		String var = (String)uvi.next();
		boolean assigned=false;
		for (Iterator sqli=subqueryList.iterator(); sqli.hasNext(); ) {
		    SubqueryTemplate sqt = (SubqueryTemplate)sqli.next();
		    if (sqt.getVariablesUsed().contains(var)) {
			sqt.addVariable(var);
			assigned=true;
			break;
		    }
		}
		if (assigned==false) {
		    // I guess we assume there are some unplaced conditions to go there?
		    Debug.print("Failed to find place to assign variable. Making new subquery for " + var, Debug.DEBUG);
		    SubqueryTemplate sqt = new SubqueryTemplate(var);
		    subqueryList.add(sqt);
		}
	    }
	}

	undeclaredVariables.clear();

	// If there are quantified variables, make a new subquery for them at the end
	if (quantifiedVariables.size()>0) {
	    SubqueryTemplate sqt = new SubqueryTemplate(quantifiedVariables, new Vector());
	    declaredvars.addAll(quantifiedVariables);
	    subqueryList.add(sqt);
	}

	Debug.print("Declared: " + declaredvars+"; quantified: " + quantifiedVariables, Debug.DEBUG);


	// Place any remaining conditions as soon as they are valid
	if (undistributedConditions.size()>0) {
	    for (Iterator uci=undistributedConditions.iterator(); uci.hasNext(); ) {
		MinimalCondition mc = (MinimalCondition)uci.next();
		// keep running total of declared variables..
		List declvars=new Vector();
		boolean assigned=false;
		for (Iterator sqli=subqueryList.iterator(); sqli.hasNext(); ) {
		    SubqueryTemplate sqt = (SubqueryTemplate)sqli.next();
		    declvars.addAll(sqt.getVariables());
		    //Debug.print("Mini declvars: " + declvars + "; query vars: " + mc.getVariableNames(), Debug.DEBUG);
		    if (declvars.containsAll(mc.getVariableNames())) {
			sqt.addCondition(mc);
			assigned=true;
			break;
		    } 
		}
		if (assigned==false) {
		    Debug.print("Failed to find a place for condition: " + mc + " where all the variables are declared! (" + declvars + ") Exiting. ", Debug.ERROR);
		    System.exit(1);
		}
	    }
	}	
    }

    /** 
     * Return true if the existing list of variables plus any
     * declared in the given query provide all we need to evaluate
     * (i.e. can we place the thing now).
     */
    private boolean allVarsDeclared(List previouslyDeclaredVars, SubqueryTemplate newQuery) {
	List existingvars = new ArrayList(previouslyDeclaredVars);
	List usedvars = newQuery.getVariablesUsed();
	existingvars.addAll(newQuery.getVariables());
	return existingvars.containsAll(usedvars);
    }

    /** 
     * Return true if we can now insert this query given the
     * previously declared variables and adding to the alreqady
     * declared vars in this query using the list of undeclared
     * vars. If we can, add the appropriate undeclared vars to the
     * subquery and remove them from undeclaredvars
     */
    private boolean declareAllVars(List previouslyDeclaredVars, SubqueryTemplate newQuery,
				    List undeclaredVars) {
	List existingvars = new ArrayList(previouslyDeclaredVars);
	List usedvars = new ArrayList(newQuery.getVariablesUsed());
	usedvars.removeAll(existingvars);
	usedvars.removeAll(newQuery.getVariables());
	Debug.print("Need vars: " + usedvars + " from " + undeclaredVars + "; previously declared: " + previouslyDeclaredVars, Debug.DEBUG);
	if (usedvars.size()>0) {
	    newQuery.addVariableList(usedvars);
	    undeclaredVars.removeAll(usedvars);
	    return true;
	}
	return false;
    }


}
