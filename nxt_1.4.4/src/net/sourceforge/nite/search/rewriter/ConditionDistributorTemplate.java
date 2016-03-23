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
import java.util.Comparator;
import java.util.Collections;

/**
 * This class provides a template algorithm for the condition distribution process,
 * implementing the features that should be common to any rewriting, while leaving 
 * as abstract the core distribution algorithm, which provides a solution to the 
 * constraint-satisfaction problem of distributing the remaining conditions.
 * <p>
 * We know that some variables and conditions must occur together, and for efficiency
 * should occur in particular positions in the ordered subqueries. Concrete
 * implementations of this class should implement <method>distributeRemainingConditions()</method> 
 * in order to provide a pluggable algorithm for distributing the remaining conditions 
 * and variable declarations according to their own heuristics or algorithms.
 * <p>
 * This template implementation of the ConditionDistributor works by incrementally creating
 * declarations for different classes of variable/condition. It creates minimal-variable 
 * SimpleQueries using the following heuristics (note that the rules are applied only to those 
 * variables and conditions which have not already been assigned to a subquery):<br>
 * <ul>
 *   <li><b>Lone variables</b>: Create a subquery for each variable which appears <i>exclusively</i> 
 *       in unary conditions (conditions involving only that variable). These queries should be evaluated last,
 *       as the variables declared within them are not required anywhere else and therefore merely increase the 
 *       size of the resulting match set, which is the cross product of the new variable binding and the elements
 *       previous match set.
 *   <li><b>One-variable conditions</b>: Create a subquery for each variable which is involved in unary conditions. 
 *       These conditions will tend to restrict the number of matches returned for a variable declaration, by 
 *       specifying a property which must hold for all of those variables. Therefore queries involving these
 *       conditions can be evaluated early to restrict the size of variable bindings which are later combined with 
 *       other bindings. This also means that other conditions, involving the same variable in binary relationships, 
 *       have a wider choice of subqueries across which they can be distributed. They can be placed as early or as 
 *       late in the ordering as is required, subject to the declaration of the other variables involved.
 *   <li><b>Single-occurrence variables</b>: Create a subquery for each <i>undeclared</i> variable which 
 *       appears in only one <i>undistributed</i> condition. This condition must appear with the declaration 
 *       of the variable. This heuristic is applied repeatedly (using the remaining <i>undeclared</i> variables  
 *       and <i>undistributed</i> conditions) until it no longer applies.
 * </ul>
 * <p>
 * We are then left with the problem of declaring the remaining variables, distributing the remaining conditions 
 * and ordering the resultant simple queries. This final stage is controlled by 
 * <method>distributeRemainingConditions()</method> which must be implemented in subclasses.
 * 
 * @author nmayo@inf.ed.ac.uk
 */
public abstract class ConditionDistributorTemplate implements ConditionDistributor, ParserTreeConstants, RewriterConstants {

    /** A DeclarationOrderingScheme to be used in distributing conditions and declarations across new subqueries. */
    protected DeclarationOrderingScheme orderingScheme;
    /** A SimpleQueryCreator which is used to create subqueries for variable/condition combinations. */
    protected SimpleQueryCreator queryCreator;

    /** The DeclarableVariables whose declarations should be distributed. */
    protected Hashtable declarableVariables;
    /** The MinimalConditions to be distributed. */
    protected List minimalConditions;

    /** The variables remaining to be assigned - a list of names. */
    protected List undeclaredVariables;
    /** The variables that are quantified - a list of names. */
    protected List quantifiedVariables;
    /** The conditions remaining to be distributed - a list of MinimalConditions. */
    protected List undistributedConditions;

    /** The new NodeQuery representing the rewritten query with redistributed minimal conditions. */
    //private NodeQuery newQuery = new NodeQuery(JJTQUERY);

    /** A list of newly-created SimpleQueries to go in the NodeQuery. */
    protected Vector simpleQueries = new Vector();


    // Lists of SubqueryTemplates which map variable declarations to conditions ready for translation into actual subquery nodes
    protected List oneVarQueries, loneVarQueries, singleOccurrenceVarQueries, floaters;
    protected List subqueryList = new Vector();
    protected List declaredvars = new Vector();

    // We have three groups of SQs based on different factors: 
    // one-var conds (we assume these go at the start)
    // lone-var conds (these must go at end) [and possibly forall vars]
    // floaters (single-occurrence vars) (these can be placed as soon as the other vars involved are declared)
    //    (NB: considering that they add var bindings which are not used again, they should come right before the lone vars??)


    /** Distribute all remaining conditions and variables. We expect
     * the floaters, undeclaredVariables and undistributedConditions
     * Lists to be fully processed and emptied, and the subqueryList
     * to be updated accordingly. */
    protected abstract void processRemainingConditions();

    /** Make sure there is a declaration for every variable before distributing final conditions. */
    protected abstract void declareRemainingVariables();
    /** This method forms the changeable step in the distribution algorithm. Once the template 
	algorithm has done some initial processing and distributed any minimal conditions which 
	have an obvious placement, this method is called to handle the distribution of the remaining 
	conditions and variables. It must be implemented by any concrete ConditionDistributor. 
        There is likely to be an interplay */
    protected abstract void distributeRemainingConditions();
    /**  */
    protected abstract void applyOrdering();
    /** An empty method to act as a hook for any finalising that a subclass may wish to do; once this
        method has been called, the subqueries should be fully specified and ordered, ready for this
        template class to construct the NodeQuery. Subclasses can just override this method to implement 
        their own behaviour. */
    protected void finaliseSubqueries() { }


    /**
     * Initialise the class with a set of MinimalConditions and DeclarableVariables.
     * 
     * @param conds a List of MinimalCondition objects representing SimpleNodes which should be distributed
     * @param vars a Hashtable mapping each variable name to a DeclarableVariable object
     */
    public ConditionDistributorTemplate(List conds, Hashtable vars) {
        // Record the conditions which are to be distributed and the variables we are to declare:
        if (conds==null) this.minimalConditions = new Vector();
        else this.minimalConditions = conds;
        if (vars==null) this.declarableVariables = new Hashtable();
        else this.declarableVariables = vars;
        // Create the structures used in our calculations (lists of info)
	queryCreator = new SimpleQueryCreator(declarableVariables, minimalConditions);
	// Create hashes for the various types of subquery we must track:
	oneVarQueries = new Vector();
	loneVarQueries = new Vector();
	floaters = new Vector();
    }


    /**
     * Distribute the conditions and variable declarations across a
     * new set of SimpleQueries, returning the resultant complex query.
     * This is the template method, describing at a high level the steps
     * of the distribution algorithm.
     * <p>
     * We may wish to add hooks to this algorithm for subclasses to 
     * provide actions between stages.
     * 
     * @param orderingScheme a DeclarationOrderingScheme to use in ordering subqueries
     * @return a new query
     */
    public final NodeQuery distribute(DeclarationOrderingScheme orderingScheme) {
        this.orderingScheme = orderingScheme;

	// Create a new query. Note that subqueryList is now processed
	// into newQuery at the very end now instead of building up
	// newQuery as we go.
	NodeQuery newQuery = new NodeQuery(JJTQUERY);
	// Initialise the structures we will use to keep track of our variables and conditions:
        undeclaredVariables = new Vector(declarableVariables.keySet());
        undistributedConditions = new Vector(minimalConditions);
	quantifiedVariables=new Vector();

	//////////////////////////////////////////////////////////////////
        // (A) Create minimal-variable SimpleQueries (the first step
        // in reconstructing the query plan)
	Debug.print(BREAK_LINE+" Creating skeleton minimal-variable SimpleQueries"+BREAK_LINE, Debug.DEBUG);

	// remove any vars that are quantified from
	// undeclaredVariables. They need special treatment.
	removeQuantifiedVariables(); 
        createLoneVariableQueries();
	distributeOneVarConditions();
        int created = 1;
	while (created>0) {
	    created = declareSingleOccurrenceVariables();
	    //Debug.print("SINGLE OCC: " + created, , Debug.DEBUG);
	}

	//////////////////////////////////////////////////////////////////
        // (B) Get a preferred ordering for the variable declarations
	Debug.print(BREAK_LINE+" Ordering Variables"+BREAK_LINE, Debug.DEBUG);
        Debug.print("Unordered variables: "+declarableVariables.keySet(), Debug.DEBUG);
        List ordering = orderingScheme.getOrdering(new Vector(declarableVariables.values()));
        Debug.print("Ordered variables:   "+ordering, Debug.DEBUG);
	Comparator varOrderComparator = new VariableOrderComparator(ordering);
	Comparator simpleVarOrderComparator = new SimpleVariableOrderComparator(ordering);

	// Sort all the lists
	Collections.sort(oneVarQueries, varOrderComparator);
	Collections.sort(loneVarQueries, varOrderComparator);
	Collections.sort(floaters, varOrderComparator);
        Collections.sort(undeclaredVariables, simpleVarOrderComparator);
        Collections.sort(quantifiedVariables, simpleVarOrderComparator);

	//////////////////////////////////////////////////////////////////
        // (C) Order the SimpleQueries and distribute the remaining
        // conditions go over the SQs, calculating a score based on
        // the vars involved, and order them accordingly we must also
        // take into account the variables declared in each query,
        // which must be declared in the correct order

        // Add the one-var queries to the start of the query, in preferred order
        for (Iterator it = oneVarQueries.iterator(); it.hasNext(); ) {
	    SubqueryTemplate sqt = (SubqueryTemplate)it.next();
            // make a new SimpleQuery and add to the main Query:
	    //NodeSimpleQuery sq = queryCreator.createSimpleQuery(sqt);
            //if (sq!=null) newQuery.jjtAddChild(sq, newQuery.jjtGetNumChildren());
	    // just add to the subquerylist to be created later (we may still edit)
	    subqueryList.add(sqt);
	    declaredvars.addAll(sqt.getVariables());
        }

	// TODO: Write a method which checks the SubqueryTemplates for completeness using isComplete()

        // Let the subclass distribute the remaining conditions. We
        // expect the floaters, undeclaredVariables and
        // undistributedConditions Lists to be fully processed and
        // emptied, and the subqueryList to be updated accordingly.
	processRemainingConditions();

        //declareRemainingVariables();
	//distributeRemainingConditions();
        //finaliseSubqueries();
	
        // Add the lone-var queries to the end of the query, in preferred order
        for (Iterator it = loneVarQueries.iterator(); it.hasNext(); ) {
	    SubqueryTemplate sqt = (SubqueryTemplate)it.next();
            // make a new SimpleQuery and add to the main Query:
	    //NodeSimpleQuery sq = queryCreator.createSimpleQuery(sqt);
            //if (sq!=null) newQuery.jjtAddChild(sq, newQuery.jjtGetNumChildren());
	    // just add to the subquerylist to be created later (we may still edit)
	    subqueryList.add(sqt);
	    declaredvars.addAll(sqt.getVariables());
        }

	// now build the actual query
	for (Iterator sqli=subqueryList.iterator(); sqli.hasNext(); ) {
	    NodeSimpleQuery sq = queryCreator.createSimpleQuery(((SubqueryTemplate)sqli.next()));
            if (sq!=null) newQuery.jjtAddChild(sq, newQuery.jjtGetNumChildren());
	}

	//////////////////////////////////////////////////////////////////
        // DEBUG: Print the results
	Debug.print(BREAK_LINE+" New Query"+BREAK_LINE+newQuery.dump("  "), Debug.DEBUG);
        if (undeclaredVariables.size()>0) Debug.print(BREAK_LINE+" Undeclared Variables"+BREAK_LINE+undeclaredVariables, Debug.DEBUG);
        if (undistributedConditions.size()>0) {
            Debug.print(BREAK_LINE+" Undistributed Conditions"+BREAK_LINE, Debug.DEBUG);
            for (Iterator it = undistributedConditions.iterator(); it.hasNext(); ) {
		Debug.print(""+(MinimalCondition)it.next(), Debug.DEBUG);
	    }
        }
	Debug.print(BREAK_LINE, Debug.DEBUG);


	return newQuery;
    }



    /**
     * Create skeleton subqueries for any remaining undeclared variables, without 
     * appending conditions.
     */
    public void createSkeletonDeclarations() {
        for (Iterator it = undeclaredVariables.iterator(); it.hasNext(); ) {
            String name = (String)it.next();
            //NodeSimpleQuery nsq = queryCreator.createSimpleQuery(dv.getName(), dv.getBareConditions());
            SubqueryTemplate sqt = new SubqueryTemplate(name);
            loneVarQueries.add(sqt);
            // remove the variable from the list:
            undeclaredVariables.remove(name);            
        }
    }

    /** For each variable, check if it's quantified. if so, add it
	to the quantifiedVariables list and remove from undeclaredVariables */
    private void removeQuantifiedVariables() {
        for (Iterator vars = declarableVariables.values().iterator(); vars.hasNext(); ) {
            DeclarableVariable dv = (DeclarableVariable)vars.next();
	    Debug.print("Test quantification: " + dv.getName(), Debug.DEBUG);
            if (dv.isExists() || dv.isForall()) {
		Debug.print("ADDED: " + dv.getName(), Debug.DEBUG);
		quantifiedVariables.add(dv.getName());
                undeclaredVariables.remove(dv.getName());
	    }
	}
    }

    /**
     * Create a declaration-condition mapping for any variable which appears only in 
     * conditions involving no other variable. These simple queries should be placed last 
     * in the sequence of queries.
     * 
     * TODO: append ALL the conditions which involve the lone var - there may be more than one
     */
    private void createLoneVariableQueries() {
	// For each variable, check each condition involving it to see
	// if other variables are involved.
        for (Iterator vars = declarableVariables.values().iterator(); vars.hasNext(); ) {
            DeclarableVariable dv = (DeclarableVariable)vars.next();
            if (dv.isLone()) {
                String var = dv.getName();
                Debug.print("* Lone variable: "+var, Debug.DEBUG);
		SubqueryTemplate sqt = new SubqueryTemplate(dv.getName(), dv.getConditions());
                loneVarQueries.add(sqt);
                // remove the variable from the list:
                undeclaredVariables.remove(dv.getName());
                // remove the conditions from the undistrib list:
                undistributedConditions.removeAll(dv.getConditions());
            }
        }
    }
	

    /**
     * Check for undistributed minimal conditions which only involve one variable,
     * and create a new simple query if the variable has not already 
     * been declared. Note that any further conditions involving only this variable
     * will be distributed to the same subquery at a later stage of distribution. Note
     * also that if there are many one-variable conditions for the same variable, they 
     * will restrict the size of the match set for this subquery, rather than increase 
     * it. For this second reason we may wish to be made aware of how many one-variable 
     * conditions there are with this variable, for ordering purposes.
     * 
     * TODO: append *all* the conditions which involve only this variable, as we know
     * they will all have to go here, and it affects ordering of subqueries..
     * 
     * @return the number of one-var-condition subqueries that were created
     */
    private int distributeOneVarConditions() {
        int count = 0;
        for (ListIterator it = undistributedConditions.listIterator(); it.hasNext(); ) {
            MinimalCondition mc = (MinimalCondition)it.next();
            // is the intersection of the condition's variables with undeclared variables a single variable?
            //List undeclVars = new Vector(mc.getVariableNames());
            //undeclVars.retainAll(undeclaredVariables);
            //if (undeclVars.size()==1) {
	    if (containsAny(mc.getVariableNames(), quantifiedVariables)) {
		continue;
	    }
            if (mc.getCardinality()==1) {
                Debug.print("* One-variable condition: "+mc.getCondition(), Debug.DEBUG);
                count++;
                String var = (String)mc.getVariableNames().get(0);
                //String var = (String)undeclVars.get(0);
                // check if this var occurs elsewhere
                //if () continue;
                // Create a SimpleQuery with this var and condition
		SubqueryTemplate sqt = new SubqueryTemplate(var, mc);
                oneVarQueries.add(sqt);
                // remove the condition from the undistrib list:
                //undistributedConditions.remove(mc);
                it.remove();
                //mc.hasBeenDistributed = true;
                // Remove this variable from the declarations list
                undeclaredVariables.remove(var);
            }
        }
        return count;
    }

    /** return true if any of the members of the second list are in the first */
    protected boolean containsAny(List list1, List list2) {
	if (list1==null || list2==null) { return false; }
	for (Iterator lit=list1.iterator(); lit.hasNext(); ) {
	    if (list2.contains(lit.next())) { return true; }
	}
	return false;
    }


    /**
     * Check for undeclared variables which occur only once among the remaining 
     * undistributed conditions, and create a new simple query for each variable 
     * and the single condition in which it is involved.
     * <p>
     * If an undeclared variable occurs in only one undistributed condition, 
     * it must be declared along with that condition.
     * 
     * 
     * TODO: This method should be updated so that it checks for *other* variables
     * which may occur in *only* the condition which is to be bound to a declaration;
     * these should be declared at the same time.
     * 
     * Alternatively to the iteration of this method, perhaps we should only create SQs for
     * vars which do occur only once in *any* of the conditions, and declare other vars later
     * based on the ordering of SQs. Otherwise we may get undesirable pairings of declarations..
     * 
     * @return the number of single-occurrence-variable subqueries that were created
     */
    private int declareSingleOccurrenceVariables() {
        int count = 0;
	// Check each undeclared variable to see whether it is involved in only one undistributed condition:
        for (ListIterator vars = undeclaredVariables.listIterator(); vars.hasNext(); ) {
            DeclarableVariable dv = (DeclarableVariable)declarableVariables.get((String)vars.next());
            // is the intersection of the variable's conditions with undistributed conditions a single condition?
            List undistribConds = new Vector(dv.getConditions());
            undistribConds.retainAll(undistributedConditions);
	    // if so, create a subquery for this var and condition
            if (undistribConds.size()==1) {
                // Get the single condition including this variable
                MinimalCondition mc = (MinimalCondition)undistribConds.get(0);
                //Debug.print("* : "+mc.getCondition(), Debug.DEBUG);
		Debug.print("* Single-occurrence variable "+dv.getName()+" in: "+mc.getCondition(), Debug.DEBUG);
                count++;
 
                // Get the names of all undeclared variables involved in the condition, whose *only* 
                // undistributed condition is this condition.
                // TODO: In fact, we can save declaring it here just because it hasn't yet been declared, 
                // and create a condition-less subquery for it in a later stage of distribution - this subq
                // can then be placed at an appropriate point in the order and collapsed into another subq's declaration.

                // Get the names of all undeclared variables involved in *only* this condition
                //List declareVars = mc.getVariablesOfCardinality(1);
                //declareVars.retainAll(undeclaredVariables);
                //if (!declareVars.contains(dv.getName())) declareVars.add(dv.getName());

		SubqueryTemplate sqt = new SubqueryTemplate(dv.getName(), mc);
                floaters.add(sqt);
                // remove the variable we just declared:
                vars.remove();
                //mc.hasBeenDistributed = true;
                undistributedConditions.remove(mc);
            }
        }
        return count;
    }


    /** 
     * Get the types associated with a particular variable.
     * 
     * @param varName a String naming the variable
     * @return a List of the types the variable is bound to
     */
    public List getTypes(String varName)	{
        if (varName == null || !declarableVariables.containsValue(varName)) return null;
        return ((DeclarableVariable)declarableVariables.get(varName)).getTypes();
    }



    /**
     * Returns any variable names in the list argument which refer to variables which 
     * have not been declared.
     * 
     * @param varNames a List of names of the variables we wish to check for declaredness
     * @return a List of the names from the input List which refer to undeclared variables
     */
    private List getUndeclaredVariables(List varNames) {
        for (ListIterator names = varNames.listIterator(); names.hasNext(); ) {
            String name = (String)names.next();
            if (!undeclaredVariables.contains(name)) varNames.remove(name);
        }
        return varNames;
    }

    /** This comparator takes a preferred order of variables and
     * orders SubqueryTemplates by their first used variable  */
    private class VariableOrderComparator implements Comparator {
	private List varorder; 

	public VariableOrderComparator(List vars) {
	    super();
	    varorder=vars;
	}

	/** find the placement of the variable name in the stored list
	 * of variables. Return -1 if not found. */
	private int getIndex(String s) {
	    if (varorder==null || s==null) { return -1; }
	    for (int i=0; i<varorder.size(); i++) {
		String comp = (String)varorder.get(i);
		if (comp.equals(s)) { return i; }
	    }
	    return -1;
	}

	public int compare(Object obj1, Object obj2) {
	    if (obj1==null && obj2==null) { return 0; }
	    else if (obj1==null)  { return -1; } 
	    else if (obj2==null)  { return 1; }
	    try {
		SubqueryTemplate sqt1 = (SubqueryTemplate)obj1;
		SubqueryTemplate sqt2 = (SubqueryTemplate)obj2;
		List v1 = sqt1.getVariablesUsed();
		List v2 = sqt2.getVariablesUsed();
		if (v1==null && v2==null) { return 0; }
		else if (v1==null || v1.size()==0) { return -1; }
		else if (v2==null || v2.size()==0) { return 1; }
		// only compare the first variable for now...
		String s1 = (String)v1.get(0);
		String s2 = (String)v2.get(0);
		if (s1==null && s2==null) { return 0; }
		else if (s1==null) { return -1; }
		else if (s2==null) { return 1; }
		int i1 = getIndex(s1);
		int i2 = getIndex(s2);
		return new Integer(i1).compareTo(new Integer(i2));
	    } catch (ClassCastException cce) { 
		//System.err.println("Exception Comparing '" + obj + "' to '" + obj1 + "'.");
		return -1;
	    }
	}
    }

    /** This comparator takes a preferred order of variables and
     * orders String (variable names) according to the list  */
    private class SimpleVariableOrderComparator implements Comparator {
	private List varorder; 

	public SimpleVariableOrderComparator(List vars) {
	    super();
	    varorder=vars;
	}

	/** find the placement of the variable name in the stored list
	 * of variables. Return -1 if not found. */
	private int getIndex(String s) {
	    if (varorder==null || s==null) { return -1; }
	    for (int i=0; i<varorder.size(); i++) {
		String comp = (String)varorder.get(i);
		if (comp.equals(s)) { return i; }
	    }
	    return -1;
	}

	public int compare(Object obj1, Object obj2) {
	    if (obj1==null && obj2==null) { return 0; }
	    else if (obj1==null)  { return -1; } 
	    else if (obj2==null)  { return 1; }
	    try {
		String s1 = (String)obj1;
		String s2 = (String)obj2;
		if (s1==null && s2==null) { return 0; }
		else if (s1==null) { return -1; }
		else if (s2==null) { return 1; }
		int i1 = getIndex(s1);
		int i2 = getIndex(s2);
		return new Integer(i1).compareTo(new Integer(i2));
	    } catch (ClassCastException cce) { 
		//System.err.println("Exception Comparing '" + obj1 + "' to '" + obj2 + "'.");
		return -1;
	    }
	}
    }

}
