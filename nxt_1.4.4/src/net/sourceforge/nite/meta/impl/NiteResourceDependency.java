/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2007, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import net.sourceforge.nite.meta.*;
import java.util.List;

/**
 * Handles a single dependency between resources.
 *
 * @author jonathan 
 */
public class NiteResourceDependency implements NResourceDependency {
    private String resourceid;
    private String observationregexp;
    protected NResource resource=null;
    protected List observations;
    
    public NiteResourceDependency(String resid, String obs) {
	resourceid=resid;
	observationregexp=obs;
    }

    public NiteResourceDependency(NResource res, String obs) {
	resourceid=res.getID();
	resource=res;
	observationregexp=obs;
    }

    /** return the ID of the resource depended on */
    public String getResourceID() {
	return resourceid;
    }

    /** return the actual NResource depended on */
    public NResource getResource() {
	return resource;
    }

    /** return the regexp string for observations */
    public String getObservationRegexp() {
	return observationregexp;
    }

    /** return the observations - this is a List of NObservations */
    public List getObservations() {
	return observations;
    }
}
