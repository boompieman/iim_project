package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;
import java.util.*;
import java.io.*;

/**
 * class DiceMetricsForStringSetValues for distance metrics for alpha
 *
 * distance(v1,v2) = 1.0 if (v1.string != v2.string)
 * distance(v1,v2) = distance(v1.set,v2.set)
 *
 */
 
public class DiceMetricsForStringSetValues implements DistanceMetric{



/**
 * Implements distance of interface DistanceMetric
 *
 * distance(v1,v2) = 1.0 if (v1.string != v2.string)
 * distance(v1,v2) = distance(v1.set,v2.set)
 *
 * using Dice coefficient on two given sets
 * @param f and s must be StringSetValues
 * @return distance(f,s) as defined above
 * @return 1.0 if the actual types of argument value is not a StringSetValue
 */
public double distance(Value f, Value s){
    try{
	StringSetValue fv = (StringSetValue)f;
	StringSetValue sv = (StringSetValue)s;
	if (!(fv.getStringValue().equals(sv.getStringValue()))) return 1.0;
	DistanceMetric dm = new DiceMetric();
	return dm.distance(fv.getSetValue(),sv.getSetValue());	
    }catch(ClassCastException exc){
	return 1.0;
    }

}

}