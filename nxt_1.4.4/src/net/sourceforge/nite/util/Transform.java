/* @author Job Zwiers  
 * @version  0, revision $Revision: 1.1 $,
 * $Date: 2004/12/10 16:08:43 $    
 * @since version 0       
 */

// Last modification by: $Author: reidsma $
// $Log: Transform.java,v $
// Revision 1.1  2004/12/10 16:08:43  reidsma
// DR: Upmigration of AMI code into sourceforge
//
// Revision 1.1  2003/05/09 12:08:09  zwiers
// First version
//



package net.sourceforge.nite.util;
import java.util.*;

/**
 * Transforms are objects that define a "mapping" from Objects to Object.
 * They are used, for instance, by FilterSet, to define mappings on Sets.
 * The transform could modify the Object "in place", or not.
 * In both cases the modified Object must be returned.
 * <br>
 * Adapted from the parlevink package (HMI group, University of Twente)
 */
 
public interface Transform
{
   public Object  transform(Object obj);

}