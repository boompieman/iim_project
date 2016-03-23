/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * University of Edinburgh
 * Neil Mayo
 */
package net.sourceforge.nite.search.rewriter;

import java.util.StringTokenizer;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
//import net.sourceforge.nite.nomread.*;

/**
 * 
 */
public class QueryStringRewriter {

	// symbols in the query string:
	private final String DELIM_COMPLEX = "::";
	private final String DELIM_SIMPLE = ":";
	private final String AND = "&&";
	private final String OR = "||";
	private final String PRE_VAR = "$";

    public static String rewrite(String query) {     
        //System.out.println("\n---\nrewriting query string:\n '"+query+"'");
        //parseQueryString(query);
        //rewriteQueryString(query);
        return query;
    }


	// UNFINISHED
    private static void parseQueryString(String query) {
        //StringTokenizer simpleQueries = new StringTokenizer(query);
        //while (st.hasMoreTokens()) {
        //    System.out.println(st.nextToken());
        //}

        String[] types, conditions;

        // NOTE: does not account for "::" e.g. in a string equality test
        String[] simpleQueries = query.split("::");
        for (int x=0; x<simpleQueries.length; x++) {
            System.out.println(simpleQueries[x]);
            // split into declarations and conditions
            String[] sq = simpleQueries[x].split(":", 2);
            String decl = sq[0], cond = sq[1];
            // split declarations and conditions
            //String[] vars = decl.split(")(", 1);
            StringTokenizer vars = new StringTokenizer(decl, "()");
            while (vars.hasMoreTokens()) {
                String[] var = vars.nextToken().split("\\s");
				String name, type;
				boolean exists = false, forall = false;

				// if the first part is exists/forall, record it
				// then the next is the variable
				// then optionally a type or set of types

				//name = var[0];
				//System.out.print("VAR: "+name);
				//if (var.length > 1) {
				//	type = var[1];
				//	System.out.println("("+type+")");
				//}
			}
        }
    }


	public static void main(String[] args) {
        // our test queries:
        int num = 3;
		String[] queries = new String[num];
        queries[0] = "(exists $e nt)($w word): $e@cat=\"NP\" && $e^$w";
        queries[1] = "($w1 word)($w2 word): text($w1)=\"the\" && $w1<>$w2 :: (exists $p nt): $p@cat eq \"NP\" && $p^$w1 && $p^$w2";
		queries[2] = "($a foo)(forall $b bar): !($b^$a)";

        String query = queries[0];
        if (args.length>0) {
            int i = Integer.parseInt(args[0]);
            if (i>0 && i<=num) {
                query = queries[i-1];
            }
        }

		query = rewrite(query);
		System.out.println("Rewritten query:\n '"+query+"'");
		
	}

}
