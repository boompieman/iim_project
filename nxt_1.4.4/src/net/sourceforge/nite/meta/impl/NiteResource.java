/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2007, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import net.sourceforge.nite.util.Debug;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.NiteMetaException;

/**
 * represents a single resource in a resource file, containing all its
 * attributes and dependencies.
 *
 * @author jonathan 
 */
public class NiteResource implements net.sourceforge.nite.meta.NRealResource {
    private String resourceID=null;
    private String resourcePath=null;
    private int resourceType=NRealResource.MANUAL;
    private String resourceDescription=null;
    private String resourceAnnotator=null;
    private NResourceGroup resourceGroup=null;
    private boolean default_resource=false;
    protected List dependencies=new ArrayList();
    private String resourceResponsible=null;
    private String resourceManual=null;
    private String resourceQuality=null;
    private String resourceCoverage=null;
    private String resourceLastEdit=null;
    private String incompatible=null;
  
    /** the NiteResource constructor takes its parent resourceGroup and its
     * attributes.  */
    public NiteResource(NResourceGroup gr, String id, String description, 
			String rtype, String annotator,	String path, String def) throws NiteMetaException {
	resourceGroup=gr;
	resourcePath=path;
	resourceID=id;
	resourceDescription=description;
	resourceAnnotator=annotator;
	if (rtype!=null && (rtype.equalsIgnoreCase("automatic") || rtype.equalsIgnoreCase("a"))) {
	    resourceType=NRealResource.AUTOMATIC;
	}
	if (def!=null && def.equalsIgnoreCase("true")) {
	    default_resource=true;
	}
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

    /** return the name of the coding this resource
     * implements */
    public String getCoding() {
	if (resourceGroup==null) { return null; }
	return resourceGroup.getCoding();
    }

    /** return the ID of this resource */
    public String getID() {
	return resourceID;
    }

    /** return a List of NResourceDependency elements */
    public List getDependencies() {
	return dependencies;
    }

    /** return the value of the 'path' attribute for this
     * resource. This can be absolute or relative, or even a URL */
    public String getPath() {
	return resourcePath;
    }

    /** Set the value of the 'path' attribute for this resource
     * group. This can be absolute or relative, or even a URL -
     * relative paths are relative to the resource file location
     * and/or the resource-type path */
    public void setPath(String path) {
	resourcePath=path;
    }

    /** return the textual description of this resource */
    public String getDescription() {
	return resourceDescription;
    }

    /** set the textual description of this resource */
    public void setDescription(String description) {
	resourceDescription=description;
    }

    /** return the annotator for this resource - null if this is an
     * automatic process. Each separate annotator should have a
     * separate resource  */
    public String getAnnotator() {
	return resourceAnnotator;
    }

    /** set the annotator for this resource - should remain null if
     * this is an automatic process. Each separate annotator should
     * have a separate resource  */
    public void setAnnotator(String annotator) {
	resourceAnnotator=annotator;
    }

    /** return the type of this resource: AUTOMATIC or MANUAL */
    public int getType() {
	return resourceType;
    }

    /** set the type of this resource: AUTOMATIC or MANUAL */
    public void setType(int type) {
	if (type==NRealResource.AUTOMATIC || type==NRealResource.MANUAL) {
	    resourceType=type;
	} else {
	    Debug.print("Failed to set resource type: invalid type - " + type, Debug.IMPORTANT);
	}
    }

    /** return the NResourceGroup to which this NResource belongs */
    public NResourceGroup getResourceGroup() {
	return resourceGroup;
    }

    /** return true if this resource has a 'default' attribute that is
     * set to 'true' */
    public boolean isDefault() {
	return default_resource;
    }

    /** return the responsible person or organisation as a String */
    public String getResponsible() {
	return resourceResponsible;
    }

    /** set the responsible person or organisation as a String */
    public void setResponsible(String responsible) {
	resourceResponsible=responsible;
    }

    /** return the coding manual reference or URL as a String */
    public String getCodingManualReference() {
	return resourceManual;
    }

    /** set the coding manual reference or URL as a String */
    public void setCodingManualReference(String manual) {
	resourceManual=manual;
    }

    /** return the responsible person or organisation as a String */
    public String getQuality() {
	return resourceQuality;
    }

    /** set the responsible person or organisation as a String */
    public void setQuality(String quality) {
	resourceQuality=quality;
    }

    /** return the responsible person or organisation as a String */
    public String getCoverage() {
	return resourceCoverage;
    }

    /** set the responsible person or organisation as a String */
    public void setCoverage(String coverage) {
	resourceCoverage=coverage;
    }

    /** return some details about the last edit to this resource as a String */
    public String getLastEdit() {
	return resourceLastEdit;
    }

    /** return some details about the last edit to this resource as a String */
    public void setLastEdit(String edit) {
	resourceLastEdit=edit;
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
