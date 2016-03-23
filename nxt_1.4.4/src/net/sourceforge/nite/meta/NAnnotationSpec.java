/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/**
 * Information about an annotation board specification (used by the OTAB to 
 * display an annotation view).
 * getType returns either DISPLAY or EDITOR.
 *
 * @author jonathan 
 */
public interface NAnnotationSpec {
    public static final int EDITOR=0;
    public static final int DISPLAY=1;

    /** returns the file name in which the annotation-board
        description is stored */
    public String getFileName();
    /** returns some descriptive text about the annotation-board for
        the purposes of GUI list of choices for example. */
    public String getDescription();

    /** returns one of EDITOR or DISPLAY depending on whether the NOM
        can be changed by this annotation board. */
    public int getType();

    /** Find the codings that can be edited - returns n List of
        Strings */
    public List getEditableCodingNames();

    /** Find the codings that can be used but not edited - returns a
        List of Strings */
    public List getUsedCodingNames();

    /** Find the object sets that can be edited - returns n List of
        Strings */
    public List getEditableObjectSetNames();

    /** Find the ontologies that can be used but not edited - returns a
        List of Strings */
    public List getUsedOntologyNames();

    /** Find the codings that can be used but not edited - returns a
        List of Strings */
    public List getUsedObjectSetNames();

    /** Find the names of the signals that should be displayed with
        this stylesheet - returns a List of Strings */
    public List getUsedSignals();
}
