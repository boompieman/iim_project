/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import net.sourceforge.nite.search.*;
import java.util.List;


/**
 * An interface for classes that take a query plan (DNF vector) from
 * a top-level NodeLogical) and break it down into conditions and 
 * phrases representing disjunctions of conditions, which can then
 * be distributed across multiple sub-queries without destroying the 
 * sense of the original query.
 * <p>
 * In practical terms, this means maintaining any structures which 
 * involve disjunction, and separating out any conjunctions of conditions.
 * By conditions are meant both individual (truly) atomic conditions, and 
 * logical conditions which relate several lower-level conditions.
 * 
 * @author nmayo@inf.ed.ac.uk
 */
public interface ConditionAtomiser {

    /**
     * Breaks down a DNF Vector into a List of minimal conditions, in other words
     * atomic or compound condition Nodes which are conjoined in the input query plan
     * and can therefore be evaluated independently. The returned list should contain 
     * only atomic NodeConditions, and NodeLogicals which group conditions.
     * 
     * @param queryPlan a DNF Vector representing a query plan
     * @return a List of minimal conditions
     */
    //public abstract List atomiseConditions(List queryPlan);


    /**
     * Breaks down a logical node into a List of minimal conditions.
     * 
     * 
     * @param queryPlan a DNF2 Vector representing a query plan
     * @return a List of minimal conditions
     */
    public abstract List atomiseConditionsInLogicalNode(NodeLogical nl);

}
