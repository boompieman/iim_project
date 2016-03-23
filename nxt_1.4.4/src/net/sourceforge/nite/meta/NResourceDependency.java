/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2007, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/**
 * Handles a single dependency between resources.
 *
 * @author jonathan 
 */
public interface NResourceDependency {
    /** return the ID of the resource depended on */
    public String getResourceID();

    /** return the actual NResource depended on */
    public NResource getResource();

    /** return the regexp string for observations */
    public String getObservationRegexp();

    /** return the observations - this is a List of NObservations */
    public List getObservations();
}
