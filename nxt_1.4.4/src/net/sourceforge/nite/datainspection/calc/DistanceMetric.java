package net.sourceforge.nite.datainspection.calc;
import net.sourceforge.nite.datainspection.data.*;
import java.util.*;
import java.io.*;

/**
 * an interface for distance metrics for alpha.
 * <BR>SEE PACKAGE DOCUMENTATION<BR>
 * The metric for distance defines the distances between two Values.
 * It should satisfy:
 * <ul>
 *  <li>distance(v,v) = 0;  
 *  <li>distance(a,b) = distance(b,a)
 *  <li>triangularity: distance(a,b) + distance(b,c) >= distance (a,c) (ie: "the direct way is not longer than via some intermediate point")
 * </ul>
 *
 * Usually: distance == 1 - similarity
 * @author Rieks op den Akker
 */
 
public interface DistanceMetric{

/**
 * @return the distance between f and s on a scale [0,1] where 0 means they are equal and 1 means they are completely different
 */
public double distance(Value f, Value s);

}