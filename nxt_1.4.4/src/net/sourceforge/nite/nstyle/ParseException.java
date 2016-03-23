/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle;

/** 
 * Handle a parse error on the input stream and quit processing
 */

public class ParseException extends Exception {
    public ParseException() { 
	super(); 
	System.out.println("Error Parsing Interface Specification: \n");
	//	System.exit();
    }

    public ParseException(String s) { 
	super(s); 
	System.out.println("Error Parsing Interface Specification: \n  "  + s);
	//	System.exit();
    }
}
