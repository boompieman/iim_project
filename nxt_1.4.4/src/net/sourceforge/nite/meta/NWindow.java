/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

/**
 * Information about a NITE window: a user interface element that must
 * be popped up.
 *
 * @author jonathan */
public interface NWindow {
    public static final int VIDEO=0;
    public static final int AUDIO=1;
    public static final int STYLE=2;

    /** Returns the identifier of the display */
    public String getName();

    /** Returns true if sound should be on for this display. */
    public boolean sound();
    
    /** Returns the type of window to be opened: VIDEO, AUDIO or STYLE */
    public int getType();

    /** Returns the object this identifier points to - will be an
     *  NStyle or an NSignal.  
    public Object getObject();

    Not sure we need this - maybe just resolve when the view is used.
    */

}
