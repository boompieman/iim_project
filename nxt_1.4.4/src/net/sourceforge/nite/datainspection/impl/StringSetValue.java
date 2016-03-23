package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;
import java.util.*;
import java.io.*;

/**
 * StringSetValue
 * Composite Value with one StringValue and one SetValue
 * These composite values are used for AP agreement measuring (see Natasa paper for SigDial'05, Lisbon) 
 *
 * @see DiceMetricsForStringSetValues defined for the distance between two StringSetValues
 */
 
public class StringSetValue implements Value{

public SetValue aset;
public StringValue da;

/**
 * @param da the string part of this Value
 * @param aset the set part of this Value
 * @throws NullPointerException if one or more arguments are null
 */  
public StringSetValue(StringValue da, SetValue aset) throws NullPointerException{
   if (da==null||aset==null){
   	 throw new NullPointerException("StringSetValue NULL ARGUMENT");
   }
	this.da = da;
	this.aset = aset;
}

public StringValue getStringValue(){
	return da;
}

public SetValue getSetValue(){
	return aset;
}



/**
 * @return a List with the two value parts [string,set] 
 */
public Object getValue(){
	List values = new ArrayList();
	values.add(da);
	values.add(aset);
	return values;
}

/**
 * @return true iff this set wrapped  equals o's set
 * @return false if o is not a SetValue or it's set is different from this one's
 */
public boolean equals(Object o){
    try{
	StringSetValue v = (StringSetValue)o;
	if (!(v.getStringValue().equals(this.getStringValue()))) return false;
	return (v.getSetValue().equals(this.getSetValue()));	
    }catch(ClassCastException exc){
	return false;
    }
}


public String toString(){
	String result = "<";
	result += this.getStringValue().toString()+",";
	result += this.getSetValue().toString()+">";
	return result;
}


/**
 * uses the dedicated DiceMetricsForStringTwoSetValues for computing the distance
 * @return the distance between this and the given other value
 */
public double distance(StringSetValue other){
	DistanceMetric metric = new  DiceMetricsForStringSetValues();
	return metric.distance(this,other);
}


// for testing this class only
public static void main(String args[]){
	StringValue sv1 = new StringValue("aap");
	StringValue sv2 = new StringValue("aap");	
	StringValue sv3 = new StringValue("noot");
	String[] arr1 = {"1","0","2"};
	String[] arr2 = {"1","0","2"};
	String[] arr3 = {"1","2","1"};
	String[] arr4 = {"1","0","2","3"};
	String[] arr5 = {"0"};
	String[] arr6 = {"0","1"};
	SetValue sval1 = new SetValue(arr1);
	SetValue sval2 = new SetValue(arr2);
	SetValue sval3 = new SetValue(arr3);
	SetValue sval4 = new SetValue(arr4);
        SetValue sval5 = new SetValue(arr5);
        SetValue sval6 = new SetValue(arr6);
        StringSetValue s2sv1 = new StringSetValue(sv1,sval2);
        StringSetValue s2sv2 = new StringSetValue(sv1,sval2);
        StringSetValue s2sv3 = new StringSetValue(sv2,sval1);
        StringSetValue s2sv4 = new StringSetValue(sv3,sval1);
        StringSetValue s2sv5 = new StringSetValue(sv1,sval5);
        StringSetValue s2sv6 = new StringSetValue(sv1,sval6);
        double dist1 = s2sv1.distance(s2sv1);
        System.out.println("distance between "+s2sv1+ " and "+s2sv2+ " = "+dist1);
        double dist2 = s2sv5.distance(s2sv6);
        System.out.println("distance between "+s2sv5+ " and "+s2sv6+ " = "+dist2);
        double dist3 = s2sv1.distance(s2sv4);
        System.out.println("distance between "+s2sv1+ " and "+s2sv4+ " = "+dist3);
        
        StringSetValue ssv1 = new StringSetValue(sv1,sval1);
        StringSetValue ssv2 = new StringSetValue(sv1,sval2);
        StringSetValue ssv3 = new StringSetValue(sv2,sval1);
        StringSetValue ssv4 = new StringSetValue(sv3,sval1);
        StringSetValue ssv5 = new StringSetValue(sv1,sval5);
        StringSetValue ssv6 = new StringSetValue(sv1,sval6);
        
        double dist4 = ssv1.distance(ssv2);
        System.out.println("distance between "+ssv1+ " and "+ssv2+ " = "+dist4);
        double dist5 = ssv5.distance(ssv6);
        System.out.println("distance between "+ssv5+ " and "+ssv6+ " = "+dist5);
        double dist6 = ssv1.distance(ssv4);
        System.out.println("distance between "+ssv1+ " and "+ssv4+ " = "+dist6);
}

}