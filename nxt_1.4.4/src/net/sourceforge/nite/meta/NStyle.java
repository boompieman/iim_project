/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/**
 * Information about a "style": a specification used by the NITE
 * Interface Engine (NIE) or the Observable Track Annotation Board
 * (OTAB).
 *
 * @author jonathan 
 */
public interface NStyle {
    public static final int EDITOR=0;
    public static final int DISPLAY=1;

    public static final int NIE=0;
    public static final int OTAB=1;

    /** returns the name of the style as used in filenames */
    public String getName();
    /** returns the filename extension of the style as used in
        filenames */
    public String getExtension();
    /** returns a textual description of the style that can be used,
        for example, in a GUI list to be chosen from */
    public String getDescription();
    /** returns one of EDITOR or DISPLAY depending on whether the NOM
        can be changed by this stylesheet. */
    public int getType();
    /** returns one of NIE or OTAB depending on which program this is
        a spec for. */
    public int getApplication();

    /** Find the codings that can be used / edited - returns a List of
        Strings */
    public List getCodingNames();

    /** Find the object sets that can be used / edited - returns a List of
        Strings */
    public List getObjectSetNames();

    /** Find the ontologies that can be used - returns a
        List of Strings */
    public List getOntologyNames();

}
