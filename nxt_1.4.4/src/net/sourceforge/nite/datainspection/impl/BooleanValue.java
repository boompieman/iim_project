package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;

/**
 */
 
public class BooleanValue implements Value{
    
    public Boolean value;
    
    /**
     * @param value should not be null
     */
    public BooleanValue(Boolean value){
    	this.value = value;
    }
    public BooleanValue(boolean value){
    	this.value = new Boolean(value);
    }
    
    public Object getValue(){
    	return value;
    }
    
    /**
     */
    public boolean equals(Object o){
        try {
    	    BooleanValue v = (BooleanValue)o;
    	    return v.value.equals(this.value);	
        } catch (ClassCastException exc) {
    	    return false;
        }
    }

    public String toString() {
        return value.toString();
    }    
}