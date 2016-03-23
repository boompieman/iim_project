/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import net.sourceforge.nite.search.*;
import java.util.List;

/**
 * An interface for classes that distribute minimal conditions 
 * (gleaned from a query) across new subqueries (SimpleQueries).
 * 
 * @author Neil Mayo
 */
public interface ConditionDistributor {

    /**
     * Distribute minimal conditions and variable declarations across new queries.
     * 
     * @param orderingScheme a declaration ordering scheme to be used in deciding how to distribute conditions and variables
     * @return a NodeQuery representing the new query composed of several subqueries
     */
    public abstract NodeQuery distribute(DeclarationOrderingScheme orderingScheme);


    /** 
     * Get the types associated with a particular variable amongst the conditions.
     * 
     * @param varName the name of the variable whose types we wish to know about
     * @return a List of types bound to the variable
     */
    public abstract List getTypes(String varName);

}
