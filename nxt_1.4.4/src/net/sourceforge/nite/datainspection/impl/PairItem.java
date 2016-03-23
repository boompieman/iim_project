package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;

import net.sourceforge.nite.util.Pair;

import java.util.*;
import java.io.*;

/**
 */
 
public class PairItem implements Item{

    public Pair itemvalue;
    
    /**
     * @return the value Object wrapped by this Item
     */
    public Object getItem(){
       return itemvalue;
    }
    
    
    public PairItem(Pair value){
        this.itemvalue = value;
    }
    
    /**
     * @return true iff this wrapped Pair equals o's Pair
     * @return false if o is not a PairValue or it's Pair is different from this one's
     */
    public boolean equals(Object o){
        try{
            PairItem v = (PairItem)o;
            return v.itemvalue.equals(this.itemvalue);    
        } catch (ClassCastException exc){
            return false;
        }
    }


}