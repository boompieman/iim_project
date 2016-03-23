/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/**
 * Information about a coding file.
 *
 * @author jonathan 
 */
public interface NCoding extends NFile {
    public static final int INTERACTION_CODING=0;
    public static final int AGENT_CODING=1;

    /** returns the directory path of the coding - defaults to the
     * path on the 'codings' element */
    public String getPath();
    /** sets the directory path of the coding - if relative, then
     * it'll be relative to the metadata */
    public void setPath(String path);
    /** returns either INTERACTION_CODING or AGENT_CODING */
    public int getType();
    /** returns the top layer in the coding */
    public NLayer getTopLayer();
    /** returns the bottom layer in the coding */
    public NLayer getBottomLayer();
    /** Returns a List of "NLayer"s belonging to this coding, in
        arbitrary order */
    public List getLayers();
}
