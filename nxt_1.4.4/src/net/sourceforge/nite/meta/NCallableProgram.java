/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/**
 * Describes an external program that can be called on this corpus
 *
 * @author jonathan 
 */
public interface NCallableProgram {
    /** returns the file name of the program */
    public String getName();
    /** returns a short description of the program */
    public String getDescription();

    /** Returns a List of required "Argument"s belonging to this
	coding, in arbitrary order */
    public List getRequiredArguments();
    /** Returns a List of optional "Argument"s belonging to this
        coding, in arbitrary order */
    public List getOptionalArguments();

    public interface Argument {
	public static final int UNKNOWN=-1;
	public static final int CORPUS_NAME=0;
	public static final int OBSERVATION_NAME=1;
	public static final int ANNOTATOR_NAME=2;

	/** get the flag for this argument should not include the '-' */
	public String getFlagName();
	/** get the default value if one is available */
	public String getDefaultValue();
	/** get the type of the argument - returns CORPUS_NAME,
	 * OBSERVATION_NAME or ANNOTATOR_NAME if that is listed in the
	 * metadata, or UNKNOWN if not. */
	public int getType();
    }
}
