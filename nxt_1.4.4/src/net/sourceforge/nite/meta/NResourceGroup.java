/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2007, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;


/**
 * Handles a resource-type element in a resource file. These just
 * group resources and virtual resources into sets that instantiate
 * the same metadata coding.
 *
 * @author jonathan 
 */
public interface NResourceGroup {
    /** add a resource to the group - this could be real or virtual */
    public void addResource(NResource nr);

    /** add a real resource to the group */
    public void addRealResource(NRealResource nr);

    /** add a virtual resource to the group */
    public void addVirtualResource(NVirtualResource nr);

    /** add a defaulted resource to the group */
    public void addDefaultedResource(NResource resource);

    /** retrieve all the (non-virtual) resources that implement the
     * particular coding instantiated by this group  */
    public List getRealResources();

    /** retrieve all the resources that implement the particular
     * coding instantiated by this group and have their default
     * attribute set to true. Note: this can return a mixed list of
     * virtual and non-virtual resources (if the resource file writer
     * is mental).  */
    public List getDefaultedResources();

    /** retrieve all the virtual resources that implement the
     * particular coding instantiated by this group  */
    public List getVirtualResources();

    /** get the name of the metadata coding that each resource in this
     * group instantiates */
    public String getCoding();

    /** remove a resource from the group - only insisting on
     * implementation of the generic form where the NResource can be
     * NRealResource or NVirtualResource. */
    public void deleteResource(NResource nr);

    /** return the value of the 'path' attribute for this resource
     * group. This can be absolute or relative, or even a URL */
    public String getPath();

    /** Set the value of the 'path' attribute for this resource
     * group. This can be absolute or relative, or even a URL -
     * relative paths are relative to the resource file location
     * and can be overridden by specific resource paths */
    public void setPath(String path);

    /** return the generic description of the resource group. */
    public String getDescription();

    /** Set the value of the 'description' attribute for this resource
     * group: a short textual description. */
    public void setDescription(String description);

}
