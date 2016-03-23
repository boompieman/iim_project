/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Factory which dispenses instances of 
 * 	{@see	 net.sourceforge.nite.nstyle.NDisplayObjectHandler}
 * based on a supplied name.
 * 
 * @author Judy Robertson
 */
public class HandlerFactory {

    /** Name of classpath-relative props file. */
    private static final String PROPS_FILE = "handlers.properties";

    /** Internal componentToData from object names to handler classes. */
    private static Properties map = new Properties(); 
    
    /*
     * Static initialiser to populate the map.
     */
    static {
        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(PROPS_FILE);
        try {
            map.load(in);
        } catch (IOException e) {
            //e.printStackTrace();
            throw new RuntimeException("Could not load NDisplayObjectHandler properties.", e);
        }        
    }

    /**
     * Return a {@see NDisplayObjectHandler} of the correct type for
     * the supplied name.
     * 
     * @param name The name of the type of handler required.
     * 
     * @return An appropriate <code>NDisplayObjectHandler</code>.
     */
    public static NDisplayObjectHandler getHandler(String name) {
        
        // Get the name of the implementing class.
        String handlerClassName = map.getProperty(name);
        if (handlerClassName == null) {
            throw new RuntimeException(
                    "Unknown NDisplayObjectHandler: " + name);            
        }       
        
        // Get the class from the ClassLoader.
        Class handlerClass = null;
        try {
            handlerClass = Class.forName(handlerClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Couldn't load NDisplayObjectHandler: " + 
                    handlerClassName,
                    e);
        }
        
        // Instantiate the handler.
        NDisplayObjectHandler handlerObj = null;
        try {
            handlerObj = (NDisplayObjectHandler) handlerClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Unsuitable NDisplayObjectHandler: " + 
                    handlerClassName,
                    e);                   
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Unable to access NDisplayObjectHandler: " + 
                    handlerClassName,
                    e);              
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Incompatible type for NDisplayObjectHandler: " + 
                    handlerClassName,
                    e);                 
        }

        // Return the reflectively created object.        
        return handlerObj;
    }
}
    
   
        
