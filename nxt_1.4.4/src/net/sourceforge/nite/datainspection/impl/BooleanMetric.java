package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;

/**
 * class DiceMetric for distance metrics for alpha
  @author Rieks op den Akker
 */
 
public class BooleanMetric implements DistanceMetric{

/**
 * implements distance of interface DistanceMetric
 * using nominal boolean valued metric 
 * @param f ans are not null Values
 * @return 1.0 when not equal 
 * @return 0.0 when equal
 */
public double distance(Value f, Value s){
	if (f.equals(s)) return 0.0;
	else return 1.0;
}

}