/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2007, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/**
 * A single resource in a resource file, provides access methods for
 * its ID, coding and dependencies. Subclassed by NRealResource and
 * NVirtualResource
 *
 * @author jonathan 
 */
public interface NResource {

    /** add a dependency with resource and observation (regexp) as Strings */
    public void addDependency(String res, String observation);

    /** add a dependency with resource resolved and observation (regexp) as a String */
    public void addDependency(NResource res, String observation);

    /** return the name of the coding this resource implements */
    public String getCoding();

    /** return the ID of this resource */
    public String getID();

    /** return a List of NResourceDependency elements */
    public List getDependencies();

    /** return the NResourceGroup to which this NResource belongs */
    public NResourceGroup getResourceGroup();

    /** return true if this resource has a 'default' attribute that is
     * set to 'true' */
    public boolean isDefault();

    /** return the textual description of this resource */
    public String getDescription();

    /** return the value of the 'notloadedwith' attribute - i.e. an ID
     * of a resource that should never be loaded with this one. */
    public String getIncompatibleID();

    /** set the value of the 'notloadedwith' attribute - i.e. an ID
     * of a resource that should never be loaded with this one. */
    public void setIncompatibleID(String inc);

    /** returns true if this resource can trace a dependency on the
     * given resource for any observation (i.e. it has a direct or
     * indirect dependency) */
    public boolean dependsOn(NResource depresource);

}
