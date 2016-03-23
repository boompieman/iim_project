/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004, Jean Carletta, Jonathan Kilgour, Natasa Jovanovic, Dennis Reidsma
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.util;

import java.io.PrintStream;

/**
 * A simple utility for controlling printed messages. Five levels of
 * information are defined and users can set the PrintStream on which
 * messages are printed. The levels of output are:<ul>
 * <li>0: No messages</li>
 * <li>1: Error messages only</li>
 * <li>2: Important information</li>
 * <li>3: Warnings</li>
 * <li>4: Debug information</li></ul>
 *
 * Each level prints all the types in lower levels plus its own type
 * of message. The default level is 3.
 *
 * @author Jonathan Kilgour
 */
public class Debug {
    public static final int NO_MESSAGES=0;
    public static final int ERROR=1;
    public static final int IMPORTANT=2;
    public static final int WARNING=3;
    public static final int DEBUG=4;
    public static final int PROGRAMMER=5;

    protected static int debug = 3; // current state
    
    /** default PrintStream is System.err but can be set using setStream */
    private static PrintStream STREAM=System.err;
        
    /** Switch debugging to default level (WARNINGS) if true or
     * completely off if false */
    public static void setDebug(boolean level) {
	if (level==true) { debug=WARNING; }
	else { debug=NO_MESSAGES; }
    }

    /** Switch debugging to the given level */
    public static void setDebug(int level) {
	if (level>PROGRAMMER) { level=PROGRAMMER; }
	if (level<NO_MESSAGES) { level=NO_MESSAGES; }
	debug=level;
    }

    /** Return true if debugging is on, false otherwise */
    public static boolean isDebugging() {
	return (debug>NO_MESSAGES) ? true : false;
    }

    /** Return true if debugging is on, false otherwise */
    public static int getDebugLevel() {
	return debug;
    }

    /** Switch the output PrintStream */
    public static void setStream(PrintStream str) {
	STREAM=str;
    }
    
    /** Switch the output PrintStream */
    public static PrintStream getStream() {
	return STREAM;
    }
    
    /**
     * Print a debug message to the appropriate output stream - if the
     * level given is at least equal to out current debug level.
     *  
     * @param message	the debug message.
     * @param level	the debug level of this message.
     */
    public static void print(String message, int level) {
	if (level<=debug) { STREAM.println(message); }
    }

    /**
     * Print a debug message to the appropriate output stream - this
     * version has no debug level, so we assume it's at WARNING level.
     *  
     * @param message	the debug message.
     */
    public static void print(String message) {
	if (WARNING<=debug) { STREAM.println(message); }
    }
}
