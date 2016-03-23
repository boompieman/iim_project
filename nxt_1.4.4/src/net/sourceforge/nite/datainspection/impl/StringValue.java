package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;
import java.util.*;
import java.io.*;

/**
 * StringValues of Units 
 *
 * @see BooleanMetrics it defines a boolean valued distance between two Values
 @author Rieks op den Akker
 */
 
public class StringValue implements Value{

public String value;

public StringValue(String value){
	this.value = value;
}

public Object getValue(){
	return value;
}

/**
 * @return true iff this set wrapped  equals o's set
 * @return false if o is not a SetValue or it's set is different from this one's
 */
public boolean equals(Object o){
    try{
	StringValue v = (StringValue)o;
	return v.value.equals(this.value);	
    }catch(ClassCastException exc){
	return false;
    }
}

public String toString(){
	return value;
}


}