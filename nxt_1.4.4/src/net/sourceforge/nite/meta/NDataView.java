/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/**
 * Information about a NITE data view: a collection of displays on the 
 * screen used for a particular purpose.
 *
 * @author jonathan 
 */
public interface NDataView {
    public static final int EDITOR=0;
    public static final int DISPLAY=1;

    /** Returns a textual description of the purpose of this view */
    public String getDescription();
    /** returns one of EDITOR or DISPLAY depending on whether the NOM
        can be changed using this view. */
    public int getType();
    
    /** Find the windows that can be edited - returns a List of
        NWindows */
    public List getWindows();
}
