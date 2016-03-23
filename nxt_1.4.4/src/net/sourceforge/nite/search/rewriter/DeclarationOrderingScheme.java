/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import net.sourceforge.nite.search.*;
import java.util.List;
import java.util.Hashtable;

/**
 * An interface for classes that take a list of variable declarations 
 * and provide a preferred ordering for them based on properties of the variables
 * and/or their types.
 * <p>
 * NOTE: We could make a similar class which provides an ordering for a list of
 * conditions/SimpleQueries, taking into account properties of the relations involved.
 * <p>
 * NOTE: As we are just providing an ordering, we could alternatively provide a Comparator
 * rather than this class...
 * <p>
 * NOTE: The ordering for subqueries declaring variables with equivalent scores may
 * be overridden to make the distribution of conditions easier, but how do we represent this?
 * 
 * @author nmayo
 */
public interface DeclarationOrderingScheme {

    /**
     * Calculates scores for each variable and orders variable names 
     * according to these scores.
     * 
     * @param declarableVariables a List of DeclarableVariable objects to order
     * @return a list of variable names in a preferred order of declaration
     */
    public abstract List getOrdering(List declarableVariables);

    /**
	 * Calculate a score for the given list of variable types. This could be 
	 * a rank, a frequency count or any other number. The score will be used 
	 * to order variable declarations.
	 * 
     * @param types a List of types to which a variable is bound
     * @return the score for the query
     */
	public abstract int calculateVariableScore(List types);

    /**
	 * Calculate a score for the given SimpleQuery, based upon the variables
	 * included in it. This could be a rank, a frequency count or any other number. 
     * The score will be used to order SimpleQueries variable declarations.
	 * <p>
     * UNIMPLEMENTED
     * 
     * @param query the SimpleQuery
     * @return the score for the query
     */
	//public abstract int calculateQueryScore(SimpleQuery query);


}
