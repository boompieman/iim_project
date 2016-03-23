/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2007, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/**
 * Handles any resource file associated with a corpus: if present this
 * must define all the information about where on disk codings live,
 * and what competing versions of annotations exist.
 *
 * @author jonathan 
 */
public interface NResourceData {

    /** find a resource from its ID */
    public NResource getResourceByID(String rid);
    
    /** find a list of NResource elements that instantiate the named
     * coding. If there are zero, return null. */
    public List getResourcesForCoding(String coding);

    /** find a list of NResource elements that instantiate the named
     * coding and have their default attribute set to true. If there
     * are zero, return null. */
    public List getDefaultedResourcesForCoding(String coding);

    /** find a list of NVirtualResource elements that instantiate the named coding */
    public List getVirtualResourcesForCoding(String coding);

    /** Return the full list of real resources for a coding, except
     * that if there are virtual resources, add them to the list and
     * remove any resources they draw from. Return null if none can be
     * found. */
    public List getCoherentResourceGroups(String coding);

    /** Return the full list of real resources, except that if there
     * are virtual resources, add them to the list and remove any
     * resources they draw from. Return null if none can be found. */
    public List getCoherentResourceGroups(String coding, String observation);

    /** delete resource given its ID */
    public void deleteResource(String resourceid);

    /** add a new resource */
    public void addResource(String coding, NResource resource);

    /** find the path to the resource: each resource has one location
     * on disk which can be affected by: the location of the resource
     * file (if paths are relative); the 'path' attribute of the
     * resource-type element in the resource file; the 'path'
     * attribute of the 'resource' element */
    public String getResourcePath(String resourceid);

    /** find the path to the resource: each resource has one location
     * on disk which can be affected by: the location of the resource
     * file (if paths are relative); the 'path' attribute of the
     * resource-type element in the resource file; the 'path'
     * attribute of the 'resource' element */
    public String getResourcePath(NResource resource);

    /** save the resource file to a file with the given filename. */
    public void writeResourceFile(String filename);

    /** save the resource file to the current filename (by default the file
        it was created from, or set using setFilename). */
    public void writeResourceFile();

    /** set the resource filename for any future save: relative paths
        are assumed to be relative to the metadata directory. NOTE:
        This does NOT update the resource file name saved in the
        metadata file: you shoud really use
        NMetaData.setResourceFilename. */
    public void setFilename(String filename);


    /** Return the path to the resource file (absolute) */
    public String getResourceFilePath();

    /** get a List of NResource elements that are incompatible with this one */
    public List getIncompatibleResources(NResource res);

    /** find a NResourceGroup elements that instantiate the named
     * coding. If there are zero, return null. */
    public NResourceGroup getResourceGroupForCoding(String coding);

    /** add a NResourceGroup element for resources that instantiate the named
     * coding.  */
    public void addResourceGroup(NResourceGroup nrg);
    
}
