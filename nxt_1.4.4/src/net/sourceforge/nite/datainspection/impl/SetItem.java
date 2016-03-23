package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;
import java.util.*;
/**
an item in the calc-package interpretation.
consists of a set of elements; equals is defined by the two sets containing the same (pointer-equal) items.
*/
class SetItem implements Item {

    // the Set wrapped in this object
    public Set itemvalue;
    
    /**
     * @return the value Object wrapped by this Item
     */
    public Object getItem(){
       return itemvalue;
    }
    
    
    public SetItem(Set value){
    	this.itemvalue = value;
    }
    
    /**
     * @return true iff this set wrapped  equals o's set
     * @return false if o is not a SetValue or it's set is different from this one's
     */
    public boolean equals(Object o){
        try{
            SetItem v = (SetItem)o;
            return v.itemvalue.equals(this.itemvalue);	
        }catch(ClassCastException exc){
            return false;
        }
    }

}