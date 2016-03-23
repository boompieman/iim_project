/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2007, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import net.sourceforge.nite.meta.*;

/**
 * A virtual resource is a grouping of other resources, for example a
 * gold-standard set that can be used by multiple automatic
 * processes. Methods are provided for querying its dependencies.
 *
 * @author jonathan 
 */
public class NiteVirtualResource implements NVirtualResource {
    private NResourceGroup group = null;
    private String id = null;
    private boolean default_resource=false;
    protected List dependencies=new ArrayList();
    private String incompatible=null;

    public NiteVirtualResource(NResourceGroup nrg, String id, String def) {
	group=nrg;
	this.id=id;
	if (def!=null && def.equalsIgnoreCase("true")) {
	    default_resource=true;
	}
    }

    /** return the name of the coding this virtual resource
     * implements */
    public String getCoding() {
	if (group==null) { return null; }
	return group.getCoding();
    }

    /** return the ID of this virtual resource */
    public String getID() {
	return id;
    }

    /** add a dependency with resource and observation (regexp) as Strings */
    public void addDependency(String res, String observation) {
	NiteResourceDependency nrd = new NiteResourceDependency(res, observation);
	dependencies.add(nrd);
    }

    /** add a dependency with resource resolved and observation (regexp) as a String */
    public void addDependency(NResource res, String observation) {
	NiteResourceDependency nrd = new NiteResourceDependency(res, observation);
	dependencies.add(nrd);
    }

    /** return a List of NResourceDependency elements */
    public List getDependencies() {
	return dependencies;
    }

    /** returns true if this resource can trace a dependency on the
     * given resource for any observation (i.e. it has a direct or
     * indirect dependency) */
    public boolean dependsOn(NResource depresource) {
	if (depresource==null || dependencies==null) { return false; }
	for (Iterator resdeps=dependencies.iterator(); resdeps.hasNext(); ) {
	    NiteResourceDependency nrd = (NiteResourceDependency)resdeps.next();
	    NResource res = nrd.getResource();
	    if (res==depresource || res.dependsOn(depresource)) { 
		return true; 
	    }
	}
	return false;
    }

    /** return the NResourceGroup to which this NResource belongs */
    public NResourceGroup getResourceGroup() {
	return group;
    }

    /** return true if this resource has a 'default' attribute that is
     * set to 'true' */
    public boolean isDefault() {
	return default_resource;
    }

    /** return the textual description of this resource */
    public String getDescription() {
       return "A virtual resource. Contains other resources.";
    }

    /** return the value of the 'notloadedwith' attribute - i.e. an ID
     * of a resource that should never be loaded with this one. */
    public String getIncompatibleID() {
	return incompatible;
    }

    /** set the value of the 'notloadedwith' attribute - i.e. an ID
     * of a resource that should never be loaded with this one. */
    public void setIncompatibleID(String inc) {
	incompatible=inc;
    }
       
}
