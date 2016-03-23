package net.sourceforge.nite.datainspection.data;
import net.sourceforge.nite.datainspection.impl.*;
import java.util.*;
import java.io.*;

/**
 * A Classification is data store for a judgement.
 * It is a mapping from the Items judged to a Value given by a judge to the Item 
 * <br>SEE PACKAGE DOCUMENTATION<br>
 * @author Rieks op den Akker
 */
 
public class Classification{

// these two list contain the Items and Values
// in such a way that pairs (item,value) can be found by the same index in the lists
// thus they always have equal size
public List items;
public List values;
// name of this data store
public String name;


/**
 * make empty Classification with a given name
 * @param name the name of this data
 */
public Classification(String name){
	this.name = name;
	items = new ArrayList();
	values = new ArrayList();
}

/**
 * makes a Classification with StringItems and StringValues from
 * two equal length arrays of Strings as items and Strings as values
 * @param name of this classification
 * @param items and values must be equally long and not null
 */
public Classification(String name, String[] items, String[] values){
	this(name);
	set(items,values);
}	


private void set(String[] items, String[] values){
	for (int i=0; i<items.length;i++){
		this.items.add(new StringItem(items[i]));
		this.values.add(new StringValue(values[i]));	
	}	
}

public Item getItem(int n){
	if (n>=items.size()) return null;
	return (Item)items.get(n);
}

public Value getValue(int n){
	if (n>=values.size()) return null;
	return (Value)values.get(n);
}

/**
 * @return the Value for the given Item
 * @return null if the Item does not occur
 */
public Value getValue(Item item){
	Item result;
	for (int i=0; i<items.size();i++){
		result = (Item)items.get(i);
		if (result.equals(item)) 
		  return (Value)values.get(i);
        }
        return null;
} 

public void add(Item item, Value value){
	items.add(item);
	values.add(value);
}

/**
 * @return number of pairs in this Classification
 */
public int size(){
	return items.size();
}

/**
 * @return the List of values as they have been assigned to the items, in their proper order.
 */
public List values(){
	return values;
}

/**
 * @return the List of items
 */
public List items(){
	return items;
}

/*A tab separated string of all values in the classification as they have been assigned to the items, in their proper order.*/
public String valueString(){
	String result = "";
	for (int i=0; i< size();i++){
		result+=getValue(i)+"\t";	
	}
	return result;
}

/*A tab separated string of all items in the classification */
public String itemString(){
	String result = "";
	for (int i=0; i< size();i++){
		result+=getItem(i)+"\t";	
	}
	return result;
}

/**
 * @return the number of times Value val is assigned to an Item in this Classification
 */
public int nrOfValues(Value val){
	int count=0;
	for (int i=0;i<values.size();i++){
		if (val.equals(values.get(i))){
			count++;	
		}
	}
	return count;	
}

/**
 * return the proportion of the Value val assigned to an Item in this classification
 * @return nrOfValues(val) / size()  
 */
public double proportionValue(Value val){
	return (double)nrOfValues(val)/size();	
}

/**
 * @return the List with the distinct Values that occur in this Classification (each Value once)
 */
public List valueList(){
	List result = new ArrayList();
	Value v;
	int n = size();
	for (int i=0;i<n;i++){
		v=(Value)values.get(i);
		if (!result.contains(v))
			result.add(v);
	}
	return result;	
}



/**
 * prints items and values each on a line on SO
 */
public void show(){
	System.out.print("items\t");
	System.out.println(itemString());
	System.out.print("values\t");
	System.out.println(valueString());
}

}