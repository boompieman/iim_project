/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import net.sourceforge.nite.search.*;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

/**
 * This implementation of ConditionAtomiser uses information internal to the NodeLogical 
 * in order to infer the points at which the constituent conditions of the query plan may 
 * be broken up. Logical groupings of OR-ed conditions are kept together as a compound 
 * condition, while top-level conjunctions (AND-ings) of elements ('elements' being OR-groupings or 
 * plain conditions) can be split up.
 */
public class DefaultConditionAtomiser implements ConditionAtomiser {

    /**
     * Break down a logical node (NodeLogical) into minimal conditions,
     * using the information stored in the node's logicals List.
     * 
     * @param nl the NodeLogical which is to be atomised
     * @return a List of minimal conditions (as NodeConditions and NodeLogicals)
     */
    public List atomiseConditionsInLogicalNode(NodeLogical nl) {
        // Create a structure to hold our atomised conditions:
        Vector atoms = new Vector();
        // Get the logicals used to join the conditions in this NodeLogical (i.e. without the initial OR)
        List joiningLogicals = nl.logicals.size() > 1 ? 
            nl.logicals.subList(1, nl.logicals.size()) : 
            new Vector();
        // If there is an OR anywhere in the logicals (except the first which we have omitted), we must keep the whole lot together
        if (joiningLogicals.contains(nl.OR)) atoms.add(nl);
        // Otherwise we can split into individual ANDed conditions
        else {
            int num = nl.jjtGetNumChildren();
            // For each 
            for (int i=0; i<num; ++i) {
                NodeCondition nc = (NodeCondition)nl.jjtGetChild(i);
                // If the condition is a group node, add atomisation of the conditions which it groups
                if (nc.isGroup) atoms.addAll( atomiseConditionsInLogicalNode((NodeLogical)nc.jjtGetChild(0)) );
                // .. otherwise just add the condition node
                else atoms.add(nc);
            }
        }
        return atoms;
    }


}
