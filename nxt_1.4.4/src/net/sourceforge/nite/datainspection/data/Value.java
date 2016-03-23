package net.sourceforge.nite.datainspection.data;
import java.util.*;
import java.io.*;

/**
 * an interface for Values of Units.
 * <br>SEE PACKAGE DOCUMENTATION<br>
 * known implementation can be BooleanValues, SetValues
 * DistanceMetrics defines distance between two Values
 
 * We strongly recommend to override boolean method Object.equals(Object o) for all implementing Value classes
 @author Rieks op den Akker
 */
 
public interface Value{

/**
 * @return the value Object wrapped by this Value
 */
public Object getValue();

}