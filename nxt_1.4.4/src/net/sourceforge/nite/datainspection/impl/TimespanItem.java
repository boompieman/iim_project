package net.sourceforge.nite.datainspection.impl;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;


/**
 * An item standing for a segment of the timeline.
 */
public class TimespanItem implements Item{

    public double start=0;
    public double end=0;
    
    public Object getItem() {
        return null;
    }
    
    public TimespanItem(double s, double e){
        start=s;
        end=e;
    }
    
    public boolean equals(Object o){
        try{
            TimespanItem v = (TimespanItem)o;
            return (v.start == start) && (v.end == end);    
        } catch (ClassCastException exc){
            return false;
        }
    }

}