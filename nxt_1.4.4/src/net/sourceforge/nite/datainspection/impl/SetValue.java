package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;
import java.util.*;
import java.io.*;
import java.util.Random;

/**
 * SetValue is a Value for Sets of Objects
 *
 * @see DiceMetrics it defines a distance between two SetValues
 */
 
public class SetValue implements Value{

public Set value;

/**
 * @param value should not be null
 */
public SetValue(Set value){
	this.value = value;
}

public SetValue(String[] value){
	this.value = new HashSet();
	for (int i=0;i<value.length;i++)
		this.value.add(value[i]);		
}

public SetValue(List value){
	this.value = new HashSet();
	for (int i=0;i<value.size();i++)
		this.value.add(value.get(i));		
}


public Object getValue(){
	return value;
}

/**
 * @return true iff this set wrapped  equals o's set
 * @return false if o is not a SetValue or it's set is different from this one's
 */
public boolean equals(Object o){
    //System.out.println("Equals called");
    try {
	    SetValue v = (SetValue)o;
	    //if (v.value.equals(this.value)) {
	        //System.out.println("TEST"+v.value);
	    //}
	    return v.value.equals(this.value);	
    } catch (ClassCastException exc) {
        System.out.println("NO EQUAL: CLASSCASTEXCEPT " + this + "," + o);
	    return false;
    }
}


public String toString(){
	Object element;
	String result = "{";
	for (Iterator iter = value.iterator();iter.hasNext();){
		element = iter.next();
		result += element.toString()+",";
	}
	int len = result.length();
	if (len>1) result = result.substring(0,len-1);	
	return result+"}";
}

}