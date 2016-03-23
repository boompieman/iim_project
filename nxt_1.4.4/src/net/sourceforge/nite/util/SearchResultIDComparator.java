package net.sourceforge.nite.util;

import java.util.*;
import net.sourceforge.nite.nom.nomwrite.NOMElement;

/** This implements the comparator and orders search results
 * alphanumerically by ID. Normally called like this:
 * <br>
 * <code>private Engine searchEngine = new net.sourceforge.nite.search.Engine();<br>
 *  reslist = searchEngine.search(nom, "($a segment)");<br>
 *  reslist.remove(0); <br>
 *  Collections.sort(reslist, new SearchResultIDComparator());</code><br>
 * The first element of a search result list is always a list of
 * variable names and that's why we normally remove it before passing
 * to the Comparator. Note that this comparator does traverse along
 * the result and into any subqueries to distinguish results where the
 * first element is the same.
 *
 * @author Jonathan Kilgour, UEdin
 */
public class SearchResultIDComparator implements Comparator {
    
    public int compare(Object obj, Object obj1) {
	if (obj==null && obj1==null) { return 0; }
	else if (obj==null)  { return -1; } 
	else if (obj1==null)  { return 1; }
	try {
	    // these are search results so it's a list of elements and we just know
	    // to choose the first result
	    List l1 = (List)obj;
	    List l2 = (List)obj1;
	    for (int i=0; i<l1.size(); i++) {
		if (l1.get(i) instanceof List) { 
		    return compare(((List)l1.get(i)).remove(0), ((List)l2.get(i)).remove(0));
		}
		NOMElement nelement1 = (NOMElement) l1.get(i);
		NOMElement nelement2 = (NOMElement) l2.get(i);
		String i1 = nelement1.getID();
		String i2 = nelement2.getID();
		int r=i1.compareTo(i2);
		if (r!=0) { return r; }
	    }
	    return 0;
	} catch (Exception cce) { 
	    // This happens if you fail to remove the first list
	    // element from a search because you get strings in search
	    // results for the variables!
	    return -1;
	}
    }
}

