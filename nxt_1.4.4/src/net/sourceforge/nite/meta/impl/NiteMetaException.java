/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

/** 
 * An error in the MetaData reading process
 */
public class NiteMetaException extends Exception {
    public NiteMetaException() { 
	super(); 
	System.out.println("Nite MetaData Error: \n");
    }

    public NiteMetaException(String s) { 
	super(s); 
	System.out.println("Nite MetaData Error: \n  "  + s);
    }
}
