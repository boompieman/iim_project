/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import net.sourceforge.nite.search.*;

/**
 * A utility class providing constants for use across the rewriter package.
 * Currently provides test query strings and a divider for debugging output.
 */
public interface RewriterConstants {

	/** A dividing line for debugging output. */
	public final String BREAK_LINE = "\n============================================================\n";

	/** Default queries used in testing. */
    public String[] TEST_QUERIES = {
        // 
        "(exists $e nt)($w word): $e@cat=\"NP\" && $e^$w",
        // 
        "($w1 word)($w2 word): text($w1)=\"the\" && $w1<>$w2 :: (exists $p nt): $p@cat eq \"NP\" && $p^$w1 && $p^$w2",
        // 
		"($a word)(forall $b turn): !($b^$a)",

        // Some advanced examples taken from swbd-queries on website:
        // 
		"($n nt)(forall $up nt): "+
		"(($n@cat == 'NP') or ($n@cat == 'WHNP')) and "+
		"(not (($n@subcat ~ /.*ADV.*/) or ($n@subcat ~ /.*LOC.*/) or "+
		"($n@subcat ~ /.*DIR.*/) or ($n@subcat ~ /.*UNF.*/))) "+
		"and ((($n != $up) and($up ^ $n)) ->"+
		"((not ($up@cat == 'EDITED')) and "+
		"(not (($up@cat == 'ADVP') and "+
		"(($up@subcat ~ /.*LOC.*/) or ($up@subcat ~ /.*DIR.*/))))))"
		,

        // 
        "($w word)(exists $n nt)(exists $m markable)(forall $up nt): "+
		"($w@pos = 'PRP$') and "+
		"($n ^ $w) and "+
		"($m >'at' $n) and "+
		"(($up != $n) -> (not (($n ^ $up) and ($up ^ $w))))"
		,

        // A query used extensively in testing the distribution of variable declarations and minimal conditions
        "($w1 word)($w2 word|sil): text($w1)=\"the\" && $w1<>$w2 "+
		":: (exists $p nt): $p@cat eq \"NP\" && $p^$w1 && $p^$w2 "+
		":: ($w3 sil)($w4 word): $w2<>$w3 && $w1<>$w4 && $w2<>$w4"+
        ":: ($h sil): text($h)=\"test\""

    };

}
