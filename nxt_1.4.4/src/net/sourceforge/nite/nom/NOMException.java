/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom;

/** 
 * An error in the NOM process
 */
public class NOMException extends Exception {
    public NOMException() { 
	super(); 
	System.out.println("NOM Error: \n");
	//	System.exit();
    }

    public NOMException(String s) { 
	super(s); 
	System.out.println("NOM Error: \n  "  + s);
	//	System.exit();
    }
}
