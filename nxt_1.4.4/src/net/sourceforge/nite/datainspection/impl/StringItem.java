package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;
import java.util.*;
import java.io.*;

/**
 * StringItems of Units 
 * @author Rieks op den Akker
 */
 
public class StringItem implements Item{

// the String wrapped in this object
public String itemvalue;

/**
 * @return the value Object wrapped by this Item
 */
public Object getItem(){
   return itemvalue;
}


public StringItem(String value){
	this.itemvalue = value;
}

/**
 * @return true iff this set wrapped  equals o's set
 * @return false if o is not a SetValue or it's set is different from this one's
 */
public boolean equals(Object o){
    try{
	StringItem v = (StringItem)o;
	return v.itemvalue.equals(this.itemvalue);	
    }catch(ClassCastException exc){
	return false;
}
}


}