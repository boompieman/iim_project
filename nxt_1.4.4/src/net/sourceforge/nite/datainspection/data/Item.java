package net.sourceforge.nite.datainspection.data;
import java.util.*;
import java.io.*;

/**
 * an interface for Items of Units.
 * <br>SEE PACKAGE DOCUMENTATION<br>
 * We recommend to override Object.equals(Object o) in every class that implements Item
 @author Rieks op den Akker
 
 */
 
public interface Item{

/**
 * @return the value Object wrapped by this Item
 */
public Object getItem();

}