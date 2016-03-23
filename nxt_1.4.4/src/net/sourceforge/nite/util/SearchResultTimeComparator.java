package net.sourceforge.nite.util;

import java.util.*;
import net.sourceforge.nite.nom.nomwrite.NOMElement;

/** This is (was) a commonly used subclass in sample programs which
 * implements the comparator and orders search results by the start
 * time of the first bound variable in the query. Normally called like this:
 * <br>
 * <code>private Engine searchEngine = new net.sourceforge.nite.search.Engine();<br>
 *  reslist = searchEngine.search(nom, "($a segment)");<br>
 *  reslist.remove(0); <br>
 *  Collections.sort(reslist, new SearchResultTimeComparator());</code><br>
 * The first element of a search result list is always a list of
 * variable names and that's why we normally remove it before passing
 * to the Comparator.
 *
 * @author Jonathan Kilgour, UEdin
 */
public class SearchResultTimeComparator implements Comparator {
    
    public int compare(Object obj, Object obj1) {
	if (obj==null && obj1==null) { return 0; }
	else if (obj==null)  { return -1; } 
	else if (obj1==null)  { return 1; }
	try {
	    // these are search results so it's a list of elements and we just know
	    // to choose the first result
	    List l1 = (List)obj;
	    List l2 = (List)obj1;
	    NOMElement nelement1 = (NOMElement) l1.get(0);
	    NOMElement nelement2 = (NOMElement) l2.get(0);
	    double s1 = nelement1.getStartTime();
	    double s2 = nelement2.getStartTime();
	    return (new Double(s1)).compareTo(new Double(s2));
	} catch (ClassCastException cce) { 
	    // This happens if you fail to remove the first list
	    // element from a search because you get strings in search
	    // results for the variables!
	    return -1;
	}
    }
}

