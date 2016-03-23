/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

/**
 * Information about a NITE window: a user interface element that must
 * be popped up.
 *
 * @author jonathan */
public class NiteWindow implements net.sourceforge.nite.meta.NWindow {
    String name=null;
    int type=STYLE;
    boolean sound=true;

    public NiteWindow(String name, int type, boolean sound) {
	this.name=name;
	this.type=type;
	this.sound=sound;
    }

    /** Returns the identifier of the display */
    public String getName() {
	return name;
    }

    /** Returns true if sound should be on for this display. */
    public boolean sound() {
	return sound;
    }
    
    /** Returns the type of window to be opened: VIDEO, AUDIO or STYLE */
    public int getType() {
	return type;
    }

    /** Returns the object this identifier points to - will be an
     *  NStyle or an NSignal.  
    public Object getObject();

    Not sure we need this - maybe just resolve when the view is used.
    */

}
