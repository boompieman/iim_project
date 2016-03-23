/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

import java.util.Comparator;
import net.sourceforge.nite.nxt.*;
//import net.sourceforge.nite.nom.nomread.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.nom.*;
import net.sourceforge.nite.time.*;

/**
 * Compares two NOMElements on their starttime.
 * Exception if invoked on object without timing.
 * <br>If time is the same, but o1!= o2, o1 is smaller than o2.
 * <br>Degenerate cases: null < not timed < timed
 * <br>Equal: iff o1==o2
 * <b>WARNING: THIS CLASS MAY MOVE TO A MORE APPROPRIATE PACKAGE</b>
 * @author Dennis Reidsma, UTwente
 */
public class NOMElementStartTimeComparator implements Comparator {
    //-1 if o1 < o2
    public int compare(Object o1, Object o2) {
        if( o1 == null && o2 == null ) {
            return 0;
        } else if( o1 == null ) {
            return -1;
        } else if( o2 == null ) {
            return 1;
        } 
        if (o1==o2)
            return 0;
            
        double t1 = ((NOMElement)o1).getStartTime();
        double t2 = ((NOMElement)o2).getStartTime();
        
        if ( t1 == NOMElement.UNTIMED && t2 == NOMElement.UNTIMED ) {
            return 0;
        } else if ( t1 == NOMElement.UNTIMED ) {
            return -1;
        } else if ( t2 == NOMElement.UNTIMED ) {
            return 1;
        }
        
        if (t1 <= t2) 
            return -1;
        else 
            return 1;
    }
    
}