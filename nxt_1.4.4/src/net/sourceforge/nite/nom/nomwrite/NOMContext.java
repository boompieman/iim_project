/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite;

import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.nom.NOMException;

import java.util.List;
import java.util.Set;
import java.util.Iterator;

/**
 * NOMContext holds information about the currently loaded files. It
 * is mainly useful when resource files are being used, but can also
 * show reliability syle loads using the older metadata approach.
 *
 * @author jonathan 
 */
public interface NOMContext {
    public static final int UNLOADED = 0;
    public static final int METADATA = 1;
    public static final int METADATA_MULTI = 2;
    public static final int RESOURCE = 3;
    public static final int RESOURCE_MULTI = 4;

    /* return the observation names for which there is some data
     * loaded */
    public List getLoadedObservationNames();

    /* return the load type given observation and NFile (where NFile
     * can be NCoding, NCorpusResource, NOntology or
     * NObjectSet). Returns one of UNLOADED meaning nothing has been
     * loaded for this NFile; METADATA meaning the NFile is
     * singly-loaded using the metadata path; METADATA_MULTI meaning a
     * multi-annotator load has been applied using the loadReliability
     * approach; RESOURCE meaning the NFile is singly loaded using a
     * resource path; RESOURCE_MULTI meaning it is multiply loaded
     * using resources. */
    public int getLoadType(String observation, NFile nfile);

    /** only returns a non-null List if the load type is
     * METADATA_MULTI - if so, returns a List of String elements: the
     * annotators IDs */
    public List getAnnotators(String observation, NFile nfile);

    /** only returns a non-null List if the load type RESOURCE - if
     * so, returns the NResource itself */
    public NResource getResource(String observation, NFile nfile);

    /** Only returns a non-null Set if the type is RESOURCE or
     * RESOURCE_MULTI - return all resources loaded for this
     * combination of observation and NFile as a Set of NResource
     * elements. */
    public Set getResources(String observation, NFile nfile);

    /** Only returns a non-null List if the type is RESOURCE or
     * RESOURCE_MULTI - return all resources loaded for this
     * combination of observation and NFile as a List of Strings: the
     * result of calling getDescription on each NResource. */
    public List getResourceDescriptions(String observation, NFile nfile);

    /** Only returns a non-null List if the type is RESOURCE or
     * RESOURCE_MULTI - return all resources loaded for this
     * combination of observation and NFile as a List of Strings: the
     * result of calling getID on each NResource. */
    public List getResourceIDs(String observation, NFile nfile);

    /** return a List of NFile elements that have some loaded data
     * corresponding to them for the given observation name. This will
     * not contain any corpus level files loaded unless the observation
     * String is null. */
    public List getLoadedNFilesForObservation(String observation);

    /** return a List of NFile elements that have no loaded data
     * corresponding to them for the given observation name. This will
     * not contain any unloaded corpus level files unless the observation
     * String is null. */
    public List getUnloadedNFilesForObservation(String observation);

    /** return a String containing the context in plain text -
     * formatted for human reading */
    public String getTextualContext();

    /** return an XML formatted String containing the context */
    public String getXMLContext();

    /** clear information about loaded data */
    public void clear();

    /** clear information about loaded data for the given observation */
    public void clear(String observation);

    /** add a loaded file to the context store - caller must work out
     * type and should pass resource only if it's a resource load and
     * annotator only if it's an old-style multi-annotator load. */
    public void addFile(String observation, NFile nfile, String filename, int type,
			   NResource resource, String annotator);
}
