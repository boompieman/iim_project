package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;

import java.util.*;
import java.io.*;

/**
 * StringTwoSetValue
 * Composite Value with one StringValue and two SetValues
 * These composite values are used for AP agreement measuring (see Natasa paper for SigDial'05, Lisbon) 
 *
 * @see DiceMetricsForStringTwoSetValues is defined for the distance between two StringTwoSetValues
 */
 
public class StringTwoSetValue implements Value{

public SetValue aset;
public SetValue bset;
public StringValue da;

/**
 * @param da the dialogue act
 * @param aset the a-set of dialogue acts
 * @param bset the b-set of dialogue acts
 * @throws NullPointerException if one or more arguments are null
 */  
public StringTwoSetValue(StringValue da, SetValue aset, SetValue bset) throws NullPointerException{
   if (da==null||aset==null||bset==null){
   	 throw new NullPointerException("StringTwoSetValue NULL ARGUMENT");
   }
	this.da = da;
	this.aset = aset;
	this.bset = bset;
}

public StringValue getDATypeValue(){
	return da;
}

public SetValue getASetValue(){
	return aset;
}

public SetValue getBSetValue(){
	return bset;
}

/**
 * @return a List with the three values [datype,aset,bset]
 */
public Object getValue(){
	List values = new ArrayList();
	values.add(da);
	values.add(aset);
	values.add(bset);
	return values;
}

/**
 * @return true iff this set wrapped  equals o's set
 * @return false if o is not a SetValue or it's set is different from this one's
 */
public boolean equals(Object o){
    try{
	StringTwoSetValue v = (StringTwoSetValue)o;
	if (!(v.getDATypeValue().equals(this.getDATypeValue()))) return false;
	if (!(v.getASetValue().equals(this.getASetValue()))) return false;
	return (v.getBSetValue().equals(this.getBSetValue()));	
    }catch(ClassCastException exc){
	return false;
    }
}


public String toString(){
	String result = "<";
	result += this.getDATypeValue().toString()+",";
	result += this.getASetValue().toString()+"|";
	result += this.getBSetValue().toString()+">";
	return result;
}


/**
 * uses the dedicated DiceMetricsForStringTwoSetValues for computing the distance
 * @return the distance between this and the given other value
 */
public double distance(StringTwoSetValue other){
	DistanceMetric metric = new  DiceMetricsForStringTwoSetValues();
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
        StringTwoSetValue s2sv1 = new StringTwoSetValue(sv1,sval1,sval2);
        StringTwoSetValue s2sv2 = new StringTwoSetValue(sv1,sval1,sval2);
        StringTwoSetValue s2sv3 = new StringTwoSetValue(sv2,sval1,sval2);
        StringTwoSetValue s2sv4 = new StringTwoSetValue(sv3,sval1,sval2);
        StringTwoSetValue s2sv5 = new StringTwoSetValue(sv1,sval5,sval2);
        StringTwoSetValue s2sv6 = new StringTwoSetValue(sv1,sval6,sval2);
        double dist1 = s2sv1.distance(s2sv1);
        System.out.println("distance between "+s2sv1+ " and "+s2sv2+ " = "+dist1);
        double dist2 = s2sv5.distance(s2sv6);
        System.out.println("distance between "+s2sv5+ " and "+s2sv6+ " = "+dist2);
        double dist3 = s2sv1.distance(s2sv4);
        System.out.println("distance between "+s2sv1+ " and "+s2sv4+ " = "+dist3);
}

}