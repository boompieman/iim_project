/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import java.util.List;
import java.util.ArrayList;

/**
 * Information about a NITE data view: a collection of displays on the 
 * screen used for a particular purpose.
 *
 * @author jonathan 
 */
public class NiteDataView implements net.sourceforge.nite.meta.NDataView {
    int type=EDITOR;
    String description=null;
    ArrayList windows=null;
    
    public NiteDataView(String description, int type, List windows) {
	this.type=type;
	this.description=description;
	this.windows=(ArrayList)windows;
    }

    /** Returns a textual description of the purpose of this view */
    public String getDescription() {
	return description;
    }

    /** returns one of EDITOR or DISPLAY depending on whether the NOM
        can be changed using this view. */
    public int getType() {
	return type;
    }

    /** Find the windows that can be edited - returns a List of
        NWindows */
    public List getWindows() {
	return windows;
    }
}
