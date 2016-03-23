/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2007, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import net.sourceforge.nite.meta.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Handles a resource-type element in a resource file. These just
 * group resources and virtual resources into sets that instantiate
 * the same metadata coding.
 *
 * @author jonathan 
 */
public class NiteResourceGroup implements NResourceGroup {
    List resources=new ArrayList();
    List virtual_resources=new ArrayList();
    List defaulted_resources=null;
    String coding=null;
    String path=null;
    String description=null;

    /** Constructor for NiteResourceGroup takes two Strings: the
     * coding instantiated by all its children and the path (if the
     * path is relative it's relative to the resource file
     * location). */
    public NiteResourceGroup(String coding, String path) {
	this.coding=coding;
	this.path=path;
    }

    /** Constructor for NiteResourceGroup takes three Strings: the
     * coding instantiated by all its children; the path (if the
     * path is relative it's relative to the resource file
     * location); and the description of this group  */
    public NiteResourceGroup(String coding, String path, String description) {
	this.coding=coding;
	this.path=path;
	this.description=description;
    }

    /** add a resource to the group - this could be real or virtual */
    public void addResource(NResource nr) {
	if (nr instanceof NRealResource) { addRealResource((NRealResource)nr); }
	else if (nr instanceof NVirtualResource) { addVirtualResource((NVirtualResource)nr); }
    }

    /** add a real resource to the group */
    public void addRealResource(NRealResource nr) {
	resources.add(nr);
    }

    /** add a virtual resource to the group */
    public void addVirtualResource(NVirtualResource nr) {
	virtual_resources.add(nr);
    }

    /** add a defaulted resource to the group - defaults can be
     * virtual or real */
    public void addDefaultedResource(NResource resource) {
	if (!(resource instanceof NVirtualResource) && !(resource instanceof NRealResource)) {
	    System.err.println("ATTEMPT TO ADD A NON_RESOURCE TO LIST OF DEFAULTED RESOURCES - FAILED. ");
	    return;
	} 
	if (defaulted_resources==null) { defaulted_resources=new ArrayList(); }
	defaulted_resources.add(resource);
    }

    /** retrieve all the (non-virtual) resources that implement the
     * particular coding instantiated by this group  */
    public List getRealResources() {
	return resources;
    }

    /** retrieve all the virtual resources that implement the
     * particular coding instantiated by this group  */
    public List getVirtualResources() {
	return virtual_resources;
    }

    /** retrieve all the resources that implement the particular
     * coding instantiated by this group and have their default
     * attribute set to true. Note: this can return a mixed list of
     * virtual and non-virtual resources (if the resource file writer
     * is mental).  */
    public List getDefaultedResources() {
	return defaulted_resources;
    }

    /** get the name of the metadata coding that each resource in this
     * group instantiates */
    public String getCoding() {
	return coding;
    }

    /** remove a resource from the group - only insisting on
     * implementation of the generic form where the NResource can be
     * NRealResource or NVirtualResource. */
    public void deleteResource(NResource nr) {
	if (nr instanceof NRealResource) { resources.remove((NRealResource)nr); }
	else if (nr instanceof NVirtualResource) { virtual_resources.remove((NVirtualResource)nr); }
    }

    /** return the value of the 'path' attribute for this resource
     * group. This can be absolute or relative, or even a URL */
    public String getPath() {
	return path;
    }

    /** Set the value of the 'path' attribute for this resource
     * group. This can be absolute or relative, or even a URL -
     * relative paths are relative to the resource file location
     * and can be overridden by specific resource paths */
    public void setPath(String path) {
	this.path=path;
    }

    /** return the generic description of the resource group. */
    public String getDescription() {
	return description;
    }

    /** Set the value of the 'description' attribute for this resource
     * group: a short textual description. */
    public void setDescription(String description) {
	this.description=description;
    }

}
