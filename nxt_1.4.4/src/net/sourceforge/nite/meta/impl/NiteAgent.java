/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

/** 
 * An agent as referred to in the metadata
 *
 * @author jonathan
 */

public class NiteAgent implements net.sourceforge.nite.meta.NAgent {
    private String name;
    private String fullname;
    
    public NiteAgent (String name, String fullname) {
	this.name=name;
	this.fullname=fullname;
    }

    /** The identifier for this agent as used in filenames
        etc. Could be something like "g" for the giver. */
    public String getShortName() {
	return name;
    }

    /** The fullname of the agent. */
    public String getFullName() {
	return fullname;
    }

}

