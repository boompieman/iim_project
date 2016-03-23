/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/**
 * A NFile is an interface that is useful as a catch-all for all kinds
 * of file-level container - it is instantiated by NCoding, NOntology
 * etc.
 *
 * @author jonathan 
 */
public interface NFile {
    /** returns the name of this NFile as used for pointing to it,
     * This is not necessarily the same as the filename. */
    public String getName();

    /** returns the filename of this NFile. */
    public String getFileName();
}