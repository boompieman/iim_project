/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import net.sourceforge.nite.search.*;
import java.util.List;

/**
 * An interface for a class that accepts a query plan and 
 * can rewrite the contents according to a set of rewriting rules 
 * or heuristics, and also rewrite the results to match the form
 * expected by the provider of the original query.
 * 
 * @author nmayo@inf.ed.ac.uk
 */
public interface QueryRewriter {

    /**
     * Rewrite the given query plan according to a set of rules.
     *
     * @param query the NodeQuery which is to be rewritten
     * @return a new NodeQuery representing the rewritten query plan
     */
    public abstract NodeQuery rewrite(NodeQuery query);


    /**
     * Rewrite the result to match the expected result form 
     * of the original input query. Subclasses must implement 
     * this to provide a mapping from the results structure 
     * of the rewritten query onto the results structure of 
     * the original query.
     * 
     * @param result the results of the query
     */
    public abstract List rewriteResult(List result);

}
