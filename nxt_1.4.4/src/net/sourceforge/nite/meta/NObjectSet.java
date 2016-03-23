/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/**
 * Information about an Object Set. e.g. a set of referents. An object
 * set is a flat list of elements.
 *
 * @author jonathan */
public interface NObjectSet extends NFile {
    /** returns a description of the ObjectSet. */
    public String getDescription(); 
    /** ObjectSets are rather like a layer in that they have a
        flat set of elements. */
    public List getContentElements();
}
