package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;

import java.util.*;
import java.io.*;

/**
 * class DiceMetricsForStringTwoSetValues for distance metrics for alpha
 *
 * distance(v1,v2) = 1.0 if (v1.datype != v2.datype)
 * distance(v1,v2) = lambda * distance(v1.aset,v2.aset) + (1-lambda)*distance(v1.bset,v2.bset)
 *
 * user can set the lambda value but it should be in [0,1]
 */
 
public class DiceMetricsForStringTwoSetValues implements DistanceMetric{


public double lambda = 0.5;

/**
 * @param lambda weighting factor  for the distances between the a-sets and the b-sets part of the
 * StringTwoSets values used in computing the distance between two of these type of Values
 */  
public void setLambda(double lambda){
	this.lambda = lambda;
}

/**
 * @return lambda
 */
public double lambda(){
	return lambda;
}


/**
 * Implements distance of interface DistanceMetric
 *
 * distance(v1,v2) = 1.0 if (v1.datype != v2.datype)
 * distance(v1,v2) = lambda * distance(v1.aset,v2.aset) + (1-lambda)*distance(v1.bset,v2.bset)
 *
 * User can set the lambda value but it should be in [0,1]

 * using Dice coefficient on two given sets
 * @param f and s must be StringTwoSetValues
 * @return distance(f,s) as defined above
 * @return 1.0 if the actual types of argument value sis not a StringTwoSetValue
 */
public double distance(Value f, Value s){
    try{
	StringTwoSetValue fv = (StringTwoSetValue)f;
	StringTwoSetValue sv = (StringTwoSetValue)s;
	if (!(fv.getDATypeValue().equals(sv.getDATypeValue()))) return 1.0;
	DistanceMetric dm = new DiceMetric();
	double aset_dist = dm.distance(fv.getASetValue(),sv.getASetValue());
	double bset_dist = dm.distance(fv.getBSetValue(),sv.getBSetValue());
	return lambda * aset_dist + (1.0-lambda) * bset_dist;	
    }catch(ClassCastException exc){
	return 1.0;
    }

}

}