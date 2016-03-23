/* @author Job Zwiers  
 * @version  0, revision $Revision: 1.1 $,
 * $Date: 2004/12/10 16:08:43 $    
 * @since version 0       
 */

// Last modification by: $Author: reidsma $
// $Log: Predicate.java,v $
// Revision 1.1  2004/12/10 16:08:43  reidsma
// DR: Upmigration of AMI code into sourceforge
//
// Revision 1.1  2003/05/09 12:08:02  zwiers
// First version
//



package net.sourceforge.nite.util;
import java.util.*;


/**
 *
 * Predicates are Objects that implement a boolean test on Objects,
 * in the form of their "valid" method. If some Object obj satifies
 * some predicate pred, then pred.valid(obj) should yield "true".
 * <br>
 * Adapted from the parlevink package (HMI group, University of Twente)
 */
 
public interface Predicate
{
   public boolean valid(Object obj);

}