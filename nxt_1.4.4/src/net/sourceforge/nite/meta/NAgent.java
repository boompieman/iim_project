/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

/**
 * Information about an Agent.
 *
 * @author jonathan 
 */
public interface NAgent {
    /** The identifier for this agent as used in filenames
        etc. Usually something like "g" for giver. */
    public String getShortName();
    /** The full description of the Agent for display purposes. Could
	be a string like "giver". */
    public String getFullName(); 
}
