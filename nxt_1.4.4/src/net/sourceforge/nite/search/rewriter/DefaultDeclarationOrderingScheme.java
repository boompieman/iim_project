/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import net.sourceforge.nite.search.*;

//import java.lang.NumberFormatException;
//import java.io.StringReader;
//import java.util.StringTokenizer;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;
import java.util.Comparator;
import java.lang.Comparable;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;


/**
 * This implementation of DeclarationOrderingScheme uses corpus statistics 
 * concerning the frequency of declared types to calculate scores for declarations.
 * <p>
 * NOTE: We should probably create an abstract class CorpusDeclarationOrderingScheme
 * for general schemes based on corpus statistics, which this implements.
 * 
 * @author nmayo@inf.ed.ac.uk
 */
public class DefaultDeclarationOrderingScheme implements DeclarationOrderingScheme {

    /** A reference to a corpus which we use to get information about our data. */
    private SearchableCorpus corpus;
    /** A frequency calculator to use on the corpus. */
    private ElementQuantityCalculator eqc;

    /**
     * Create a DeclarationOrderingScheme with a reference to a corpus
     * whose properties are used in calculating the ordering.
     * 
     * @param corpus the SearchableCorpus to use in calculating scores
     */
    public DefaultDeclarationOrderingScheme(SearchableCorpus corpus) {
	this.corpus = corpus;
        if (corpus instanceof NOMCorpus) {
            eqc = new NOMLazyEQC((NOMCorpus)corpus);
	    // Counts test
            //System.out.println("CORPUS QUANTITIES\n"+
            //                   "turns:  "+eqc.getNumber("turn")+"\n"+
            //                   "parses: "+eqc.getNumber("parse")+"\n"+
            //                   "nts:    "+eqc.getNumber("nt")+"\n"+
            //                   "sils:   "+eqc.getNumber("sil")+"\n"+
            //                   "words:  "+eqc.getNumber("word")+"\n"
            //                   );
        }
    }


    /**
     * Orders variable declarations based on their scores, from low score to high.
     * 
     * @param declarableVariables a list of names mapped to type Lists
     * @return a list of names in a preferred order of declaration
     */
    public List getOrdering(List declarableVariables) {
        // Check if we have a usable ElementQuantityCalculator
        if (eqc==null) {
            System.err.println("Warning: cannot perform an ordering on a corpus which is not of type NOMCorpus. "+
			       "Using default ordering.");
            return declarableVariables;
        }
        List ordering = new Vector();
        List declarationScores = new Vector();
        // Calculate a score for each variable, and create a Declaration object for it
	for (Iterator it=declarableVariables.iterator(); it.hasNext(); ) {
	    DeclarableVariable var = (DeclarableVariable)it.next();
            declarationScores.add(new Declaration(var.getName(), calculateVariableScore(var.getTypes())));
	}
        // Sort the Declarations according to score (using a DeclarationComparator)
        Collections.sort(declarationScores, new DeclarationComparator());
        // Record the variable names in the order they appear in the sorted list of Declarations
	for (Iterator it=declarationScores.iterator(); it.hasNext(); ) {
            ordering.add( ((Declaration)it.next()).varName );
        }
	return ordering;
    }



    /**
     * Calculate a score for the given list of variable types. This implementation
     * uses frequency counts from a NOMCorpus.
     * 
     * @return the sum of the frequency counts for the provided types
     */
    public int calculateVariableScore(List types) {
	int score = 0;
	// add up the number of occurrences of each type in the corpus
	for (Iterator it=types.iterator(); it.hasNext(); ) {
	    String type = (String)it.next();
	    score+=eqc.getNumber(type);
            // NOTE: If the calculator is doing a count anyway, perhaps we should get it to count 
            // types with particular attribute values, if this information is available.
            // However this crosses into the territory of partially evaluating query...
	}
	return score;
    }






    /**
     * Comparator class for comparing Declaration objects based on their score. 
     * Orders variables from low score to high.
     */
    private class DeclarationComparator implements Comparator {
	public int compare(Object o1, Object o2) throws ClassCastException {
            // NB: we should check the types of the objects...
            return ((Comparable)o1).compareTo(o2);
	}
    }

    /**
     * Comparable class representing a score for a variable declaration.
     */
    private class Declaration implements Comparable {
	public int score;
	public String varName;

	public Declaration(String varName, int score) {
            this.varName = varName;
            this.score = score;
	}

	public int compareTo(Object o) throws ClassCastException {
            // compare declaration scores...
            Declaration d = (Declaration)o;
            return (score == d.score) ? 0 : (score < d.score) ? -1 : 1;
        }

    }


}
