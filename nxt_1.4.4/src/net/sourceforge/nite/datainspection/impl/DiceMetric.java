package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;

import java.util.*;
import java.io.*;

/**
 * class DiceMetric for distance metrics on SetValues
 * @see Value
 * @see SetValue
 */
 
public class DiceMetric implements DistanceMetric{

/**
 * implements distance of interface DistanceMetric
 * using Dice coefficient on two given sets
 * @param f and s must be SetValues not null
 * @return 1- (the Dice coefficient of f and s)
 * @return 0  when both SetValues wrap empty sets
 */
public double distance(Value f, Value s){
	SetValue fs = (SetValue)f;
	SetValue ss = (SetValue)s;
	int numerator = 2*nrOfCommonElements((Set)fs.getValue(),(Set)ss.getValue());
	int denominator = fs.value.size()+ss.value.size();
	if (denominator==0) return 0.0;
	return 1-((double)numerator)/((double)denominator);
}

/**
 * @return the number of elements in the product set of f and s
 */
public int nrOfCommonElements(Set f, Set s){
	int commons = 0;
	for (Iterator iter = f.iterator(); iter.hasNext();)
		if (s.contains(iter.next())) commons++;
	return commons;
}

}