/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import net.sourceforge.nite.search.*;
import net.sourceforge.nite.util.Debug;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;

import java.lang.NumberFormatException;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * The default implementation of the QueryRewriter, implementing our first guess at a set of  
 * rewriting rules.
 * <p>
 * We implement the ParserTreeConstants so we can use them in creating Nodes, and RewriterConstants
 * so we can use the sample queries in testing via the main method.
 * 
 * @author nmayo@inf.ed.ac.uk
 */
public class DefaultQueryRewriter implements QueryRewriter, ParserTreeConstants, RewriterConstants {

    /** A corpus on which the query is executed. We use this to get summary information 
        concerning the nature of the data. */
    protected SearchableCorpus corpus;
    /** A structure mapping each variable name in the query to a DeclarableVariable which
        encapsulates information about that variable. We construct this during the rewrite and
        pass it to the ConditionDistributor. */
    private Hashtable declarableVariables = new Hashtable();
    /** List of MinimalCondition objects encapsulating information about the conditions we
        have broken up for redistribution amongst SimpleQueries. */
    private List minimalConditions = new Vector();
    // Both of these could be abstracted to a class which manages the summary statistics and interdependencies for us.


    /**
     * Usage instructions for the main() test method.
     */
    private static void usage () {
	System.out.println(BREAK_LINE+
			   "Usage: java DefaultQueryRewriter query\n"+
			   "  Note that the query parameter can be a number\n"+
			   "  representing one of the test queries 1 to "+TEST_QUERIES.length+":\n");
	for (int i=0; i<TEST_QUERIES.length; i++) {
	    System.out.println("Q"+(i+1)+": "+TEST_QUERIES[i]+"\n");
	}
	System.out.println(BREAK_LINE);
	System.exit(0);
    }



    /**
     * Create a DefaultQueryRewriter with (optionally) a SearchableCorpus attached.
     * 
     * 
     */
    public DefaultQueryRewriter() {this(null);}
    public DefaultQueryRewriter(SearchableCorpus sc) {corpus = sc;}

    /**
     * Main test method. Instantiates the class without a SearchableCorpus,
     * just running the rewrite() method on a parsed query string. The 
     * NodeQuery query plan is rewritten according to the algorithm.
     */
    public static void main(String[] args) {
        // Check for arguments (a query string or number)
        if (args.length <=0) usage();
        String query = args[0];
        // try and parse the query as the number of a test query:
        try {
            int i = Integer.parseInt(query);
            query = TEST_QUERIES[i-1];            
        } catch(NumberFormatException nfe) {
            // leave the query as a string
        } catch(ArrayIndexOutOfBoundsException aob) {
            System.err.println("Invalid query number!");
            usage();            
        }

        try {
            NodeQuery n = ( new TestParser( new StringReader(query+"\n") ) ).Query();
            // Test our ability to create a query from scratch:
            //n = SimpleQueryCreator.testCreateAQueryByHand();

            Debug.print("\n---\nSimple query breakdown:\n", Debug.DEBUG);
            Debug.print(n.dump(" ORIG "), Debug.DEBUG);
            new DefaultQueryRewriter().rewrite(n);
            //Debug.print("\n---\n\n"+n.dump(" REWR "), Debug.DEBUG);
        } catch (Throwable t) {
            System.err.println("Error parsing query");
            t.printStackTrace(System.out);
        }
        Debug.print("Original Query:\n "+query, Debug.DEBUG);

        // We cannot run the query without a SearchableCorpus. 
        // Adding this will allow us to test the results rewriter.
    }
    

    private Node[] simpleQueries=null;
    Hashtable existvars=null;
    Hashtable forallvars=null;

    /**
     * Rewrite the query plan according to the rules of our algorithm.
     * Ignore exists quantifier, and split query into complex composition
     * of more concise simple queries with, where possible:<br>
     * <ul>
     * <li>one variable binding per sub query
     * <li>tests evaluated on earliset subquery possible
     * <li>component queries sequenced by increasing order of (estimated) match set size
     * </ul>
     * <p>
     * In terms of the dnf vector containing the query plan: 
     * Redistribute sets of ANDed NodeConditions to single 
     * variables where possible, joining them with NodeLogicals.
     * Keep conditions which are joined by implication or OR together.
     * Make all exists booleans false.
     * <p>
     * TO DO: Meanwhile record a mapping for the results output.
     * TO DO: We may need to reorder forall booleans?
     * 
     * @param query the NodeQuery which is to be rewritten
     * @return the rewritten query
     */
    public NodeQuery rewrite(NodeQuery query) {
	// --------------------------------------------------------------------------
        // (A) Break down the query into its component simple queries

	// List of broken-down conditions (atomic or composed) for redistibution amongst SimpleQueries.
        List conditions = new Vector();
        // Mapping of variable name to a list of types.
        Hashtable variables = new Hashtable();
        simpleQueries = query.getChildren(); // made global so the result rewriter can use
	declarableVariables = new Hashtable();
	minimalConditions = new Vector();
	existvars = new Hashtable();
	forallvars = new Hashtable();

        // Read each for its topmost logical node, retrieving the dnf list
        for (int i=0; i<simpleQueries.length; i++) {
            NodeSimpleQuery simpleQuery = (NodeSimpleQuery)simpleQueries[i];
            // (1) get the variable declarations and types:
            variables.putAll(simpleQuery.getVariables());

	    // (2) record quantifiers. JAK 9/1/07
	    for (Enumeration ken=simpleQuery.getVariables().keys(); ken.hasMoreElements(); ) {
		String varname = (String)ken.nextElement();
		existvars.put(varname, new Boolean(simpleQuery.isExists(varname)));
		forallvars.put(varname, new Boolean(simpleQuery.isForAll(varname)));
	    }
	    
            // (3) break down the dnf into anded conditions
	    if (simpleQuery.jjtGetNumChildren()>0) { 
		NodeLogical nl = (NodeLogical)simpleQuery.jjtGetChild(0);

		/// NOTE: We use dnf2 rather than the full dnf, which cannot be split without rewriting
		/// to a different form. The dnf2 contains the original input form but with implication 
		/// rewritten and negations distributed.
		conditions.addAll( //getMinimalConditions(nl.dnf2 )
				  //new DefaultConditionAtomiser().atomiseConditions(nl.dnf2)
				  new DefaultConditionAtomiser().atomiseConditionsInLogicalNode(nl)
				  );
	    } 
        }

	// --------------------------------------------------------------------------
	// (B) Create a DeclarableVariable object for each variable
	Debug.print(BREAK_LINE+" Declarable Variables"+BREAK_LINE, Debug.DEBUG);
        for (Iterator it = variables.keySet().iterator(); it.hasNext(); ) {
            String varName = (String)it.next();
            DeclarableVariable dv = new DeclarableVariable(varName, (List)variables.get(varName), ((Boolean)existvars.get(varName)).booleanValue(), ((Boolean)forallvars.get(varName)).booleanValue());
            Debug.print("VAR:  "+dv, Debug.DEBUG);
            declarableVariables.put(varName, dv);
        }
                

	// --------------------------------------------------------------------------
	// (C) Create a MinimalCondition object for each condition
	Debug.print(BREAK_LINE+" Minimal Conditions"+BREAK_LINE, Debug.DEBUG);
        for (Iterator it = conditions.iterator(); it.hasNext(); ) {
            SimpleNode cond = (SimpleNode)it.next();
            Debug.print(cond.dump("MC:   "), Debug.DEBUG);
            MinimalCondition mc = new MinimalCondition(cond);
            /* Add the MinimalCondition to the list in the DeclarableVariable 
               object representing each variable in the condition.
            */
            for (Iterator names = mc.getVariableNames().iterator(); names.hasNext(); ) {
                DeclarableVariable dv = (DeclarableVariable)declarableVariables.get((String)names.next());
                dv.addCondition(mc);
            }
            minimalConditions.add(mc);
        }


	// --------------------------------------------------------------------------
	// (D) Create a ConditionDistributor which contains summary information for the minimal conditions
        ConditionDistributor cd = new DefaultConditionDistributor(minimalConditions, declarableVariables);
        DeclarationOrderingScheme orderingScheme = new DefaultDeclarationOrderingScheme(corpus);
        NodeQuery retquery = cd.distribute(orderingScheme);
        return retquery;
    }

    /** Create a List of Lists containing all the result tuples,
     * flattening the tree structure */
    private List getTuplesFromResult(List result, List vals, List indices, int added) {
	Iterator it = result.iterator();
 	List varNames = result.isEmpty() ? new ArrayList() : (List)it.next();
	List results = new Vector();
        if( result.isEmpty() ) return results;

	//Debug.print("vars: " + varNames, Debug.DEBUG);
	while(  it.hasNext() ) {
	    int count=added;
	    List match = (List)it.next();
	    //Debug.print("match: " + match, Debug.DEBUG);
	    List newvals = new Vector(vals);
	    for(int j=0; j<varNames.size(); j++) {
		try {
		    NOMElement nel = (NOMElement)match.get(j);
		    // index for addition should be taken from the
		    // index list from our current counter j plus the
		    // number of vars already added.
		    int indexval = j+added;
		    int ind = ((Integer)indices.get(indexval)).intValue();
		    if (newvals.size()<=ind) { 
			for (int i=newvals.size(); i<=ind; i++) newvals.add(nel); 
		    } 
		    newvals.set(ind,nel);
		    count++;
		} catch( ClassCastException e ){ }
	    }

	    if(match.size() > varNames.size()) {
		results.addAll(getTuplesFromResult((List)match.get(match.size()-1), newvals, indices, count));
	    } else {
		results.add(newvals);
	    }
	}	
	return results;
    }

    /** Create a List of vars from a List of NodeSimpleQueries (the
     * original query). Flatten any tree structure */
    private List getFlatVarsFromQuery(Node[] queries) {
 	List varNames = new ArrayList();
        for (int i=0; i<queries.length; i++) {
            NodeSimpleQuery simpleQuery = (NodeSimpleQuery)simpleQueries[i];
            varNames.addAll(simpleQuery.getVarNames());
	} 
	return varNames;
    }

    /** Create a List of vars from a List of NodeSimpleQueries (the
     * original query). Flatten any tree structure */
    private List getTreeVarsFromQuery(Node[] queries) {
 	List varNames = new ArrayList();
        for (int i=0; i<queries.length; i++) {
            NodeSimpleQuery simpleQuery = (NodeSimpleQuery)simpleQueries[i];
            varNames.add(removeQuantified(simpleQuery.getVarNames()));
	} 
	return varNames;
    }

    /** Create a List of vars from a result list. Flattening any tree structure */
    private List getFlatVarsFromResult(List res) {
	List varNames = new Vector();
        if ( res.isEmpty() ) return varNames;
	Iterator it = res.iterator();
	varNames.addAll((List)it.next());
	List match = (List)it.next();
	if(match.size() > varNames.size()) {
	    varNames.addAll(getFlatVarsFromResult((List)match.get(match.size()-1)));
	} 
	return varNames;
    }

    private int findIndex(String val, List list) {
	for (int i=0; i<list.size(); i++) {
	    String n = (String)list.get(i);
	    if (n.equalsIgnoreCase(val)) return i;
	}
	return -1;
    }

    /** find the index of the variables of the original query in the
     * newly made tuple set */
    private List getIndices(List oldvars, List newvars) {
	List vars = new Vector();
	for (Iterator olit=oldvars.iterator(); olit.hasNext(); ) {
	    String v = (String)olit.next();
	    vars.add(new Integer(findIndex(v, newvars)));
	}
	return vars;
    }

    /** use the globals existsvars and forallvars, which tell us which
     * variables are quantified, to decide if a pair of tuples
     * are duplicates */
    private boolean sameTupleQuantified(List tup1, List tup2, List vars) {
	if (tup1==null || tup2==null) { return false; }
	for (int i=0; i<vars.size(); i++) {
	    String v = (String)vars.get(i);
	    // if we're quantified, comparison is irrelevant
	    if (((Boolean)existvars.get(v)).booleanValue() || ((Boolean)forallvars.get(v)).booleanValue()) { continue; }
	    NOMElement el1 = (NOMElement)tup1.get(i);
	    NOMElement el2 = (NOMElement)tup2.get(i);
	    if (el1!=el2) return false; 
	}
	return true;
    }

    /** use the globals existsvars and forallvars, which tell us which
     * variables are quantified, to decide which of or sorted tuples
     * are duplicates */
    private void removeDuplicates(List tuples, List vars) {
	for (int i=tuples.size()-1; i>0; i--) {
	    List tup1 = (List)tuples.get(i);
	    List tup2 = (List)tuples.get(i-1);
	    if (sameTupleQuantified(tup1, tup2, vars)) {
		tuples.remove(i);
	    }
	}
    }

    /** make the results tree to fit the format of the input - recurse
     * for complex input queries. */
    private List produceResultsFromTuples(List tuples, List vartree, int level, int start, int end) {
	List lastvals = null;
	List res = new Vector();
	if (vartree.size()<=level) { return null; }
	List varlist = (List)vartree.get(level);
	res.add(varlist);

	int numvars = 0;
	for (int i=0; i<level; i++) { numvars+=((List)vartree.get(i)).size(); }

	int lastend=start;
	for (int index=start; index<end; index++) {
	    List thisvals = new Vector();
	    List thisres = (List)tuples.get(index);
	    
	    for (int i=numvars; i<numvars+varlist.size(); i++) {
		thisvals.add((NOMElement)thisres.get(i));
	    }
	    if (lastvals==null || thisvals.equals(lastvals)) {
		if (lastvals==null) { lastvals=new Vector(thisvals); }
	    } else {
		List match = new Vector();
		match.addAll(lastvals);
		if (level<vartree.size()) {
		    List r2 = produceResultsFromTuples(tuples, vartree, level+1, lastend, index);
		    if (r2!=null) { match.add(r2); }
		}
		res.add(match);
		lastvals=new Vector(thisvals);
		lastend=index;
	    }

	}

	// tidy up at the end!
	if (lastvals!=null) {
	    List match = new Vector();
	    match.addAll(lastvals);
	    if (level<vartree.size()) {
		List r2 = produceResultsFromTuples(tuples, vartree, level+1, lastend, end);
		if (r2!=null) { match.add(r2); }
	    }
	    res.add(match);
	}
	return res;
    }
    
    /** remove quantified vars so they don't appear in result lists */
    private List removeQuantified(List vars) {
	List retlist = new Vector();
	for (Iterator vit=vars.iterator(); vit.hasNext(); ) {
	    String var = (String)vit.next();
	    if (!((Boolean)existvars.get(var)).booleanValue() &&
		!((Boolean)forallvars.get(var)).booleanValue()) { 
		retlist.add(var);
	    }
	}
	return retlist;
    }

    /**
     * Take the result tree and rewrite it to the form 
     * expected given the original input query.
     */
    public List rewriteResult(List result) {
        List newResult = new ArrayList();
	List rewrittenvars = getFlatVarsFromResult(result);
	// simpleQueries is the chain of simple queries in the original query. 
	// We use it to get the desired format of return variables.
	List oldvars = getFlatVarsFromQuery(simpleQueries);
	List indices = getIndices(oldvars, rewrittenvars);
	List tuples = getTuplesFromResult(result, new ArrayList(), indices, 0);
	Collections.sort(tuples, new TupleComparator()); // sort on IDs
	removeDuplicates(tuples, oldvars); // use quantifiers to remove duplicates
	List oldvartree = getTreeVarsFromQuery(simpleQueries);
	if (oldvartree.size()==1) { // make the simple query case quick
	    newResult.add((List)oldvartree.get(0));
	    for (int i=0; i<tuples.size(); i++ ) {
		List els = (List)tuples.get(i);
		newResult.add(els);
	    }
	    return newResult;
	}
	newResult = produceResultsFromTuples(tuples, oldvartree, 0, 0, tuples.size());
	return newResult;
    }


    /**
     * Utility method which prints details of the minimal conditions 
     * in the DNF vector. UNUSED
     * 
     * @param l the DNF 
     * @param dnf true to print dnf, false to print dnf2
     * @param prefix the prefix for printed lines
     */
    private void printMinimalConditions(List l, boolean dnf, String prefix) {
        int s = l.size();
        for (int h=0; h<s; h++) {
            Object o = l.get(h);
            if (o instanceof NodeCondition) {
                NodeCondition nc = (NodeCondition)o;
                System.out.print(prefix+"CONDITION: "+nc.dump(""));
                if (nc.jjtGetNumChildren()>0) printMinimalConditions(Arrays.asList(nc.getChildren()), dnf, prefix+"  ");
            }
            if (o instanceof Vector) {
                Debug.print(prefix+"VECTOR:    "+(Vector)o, Debug.DEBUG);
                printMinimalConditions((List)o, dnf, prefix+"  ");
            }
            if (o instanceof NodeLogical) {
                List thednf = dnf? ((NodeLogical)o).dnf : ((NodeLogical)o).dnf2;
                printMinimalConditions(thednf, dnf, prefix);
            }
        }
    }

    private class TupleComparator implements Comparator {

	private int compel(NOMElement e1, NOMElement e2) {
	    if (e1==null && e2==null) { return 0; }
	    else if (e1==null)  { return -1; } 
	    else if (e2==null)  { return 1; }	    
	    return e1.getID().compareTo(e2.getID());
	}

	public int compare(Object obj1, Object obj2) {
	    if (obj1==null && obj2==null) { return 0; }
	    else if (obj1==null)  { return -1; } 
	    else if (obj2==null)  { return 1; }
	    try {
		List l1 = (List)obj1;
		List l2 = (List)obj2;
		int max = l1.size();
		if (l2.size()<max) { max=l2.size(); }
		for (int i=0; i<max; i++) {
		    int ret = compel((NOMElement)l1.get(i), (NOMElement)l2.get(i));
		    if (ret!=0) return ret;
		}
		return 0;
	    } catch (ClassCastException cce) { // you get strings in search results for the variables!
		System.err.println("Exception Comparing '" + obj1 + "' to '" + obj2 + "'.");
		return -1;
	    }
	}
   }

}
