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
 * A class encapsulating details about a variable which is to be declared in a suitable
 * SimpleQuery during a query rewrite. This will be done by SimpleQueryCreator. Records the  
 * variable along with the conditions which use it, and a boolean indicating whether or not this 
 * variable has been declared yet in a new SimpleQuery.
 * 
 * @author Neil Mayo
 */
public class DeclarableVariable {

    /** The name of the variable to be distributed. */
    private String name;
    /** The types of the variable. */
    private List types = new Vector();
    /** The MinimalConditions involving this variable. */
    private List conditions = new Vector();
    /** Whether this variable has been distributed to a new SimpleQuery yet. */
    private boolean hasBeenDistributed = false;
    /** True if the variable is declared 'exists' */
    private boolean exists=false;
    /** True if the variable is declared 'forall' */
    private boolean forall=false;
	
    /**
     * Initialise the object with a variable name and types.
     */
    public DeclarableVariable(String name, List types) {
        this.name = name;
        this.types = types;
    }

    /**
     * Initialise the object with a variable name and types, and
     * whether it's declared exists or forall.
     */
    public DeclarableVariable(String name, List types, boolean exists, boolean forall) {
        this.name = name;
        this.types = types;
	this.exists = exists;
	this.forall = forall;
    }

    /**
     * Adds a MinimalCondition to the list of conditions which use this variable.
     * Also updates the MinimalCondition with a reference to this variable.
     */
    public void addCondition(MinimalCondition mc) { conditions.add(mc); mc.addVariable(this); }

    /**
     * Returns the number of conditions involving this variable.
     */
    public int getCardinality() { return conditions.size(); }

    /**
     * Returns the conditions involving this variable.
     */
    public List getConditions() { return conditions; }

    /**
     * Returns the condition nodes involving this variable.
     */
    public List getBareConditions() {
        List bareConditions = new Vector();
        for (Iterator it = conditions.iterator(); it.hasNext(); ) {
            MinimalCondition mc = (MinimalCondition)it.next();
            bareConditions.add(mc.getCondition());
        }
        return bareConditions; 
    }

    /**
     * Returns a list of the possible types of the variable.
     */
    public List getTypes() { return types; }

    /**
     * Returns the name of the variable.
     */
    public String getName() { return name; }

    /**
     * Returns all the MinimalCondition objects which are of the given cardinality
     * (those which involve n variables).
     */
    public List getConditionsOfCardinality(int n) {
        List conds = new Vector();
        // check each condition and add to the list if it is of the required cardinality
        for (int i=0; i<getCardinality(); i++) {
            MinimalCondition mc = (MinimalCondition)conditions.get(i);
            if (mc.getCardinality()==n) conds.add(mc);
        }
        return conds;
    }

    /**
     * Returns true if all the conditions containing this variable contain 
     * <i>only</i> this variable (are of cardinality 1).
     */
    public boolean isLone() {
        return (getConditionsOfCardinality(1).size() == this.getCardinality());
    }

    public String toString() {
        return name+" "+types;
    }

    /** return true if the variable was declared as 'exists' */
    public boolean isExists() {
	return exists;
    }

    /** return true if the variable was declared as 'forall' */
    public boolean isForall() {
	return forall;
    }

    /** set the variable to be an 'exists' var */
    public void setExists(boolean val) {
	exists=val;
    }
    /** set the variable to be an 'forall' var */
    public void setForall(boolean val) {
	forall=val;
    }
}

