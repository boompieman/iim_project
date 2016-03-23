/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import net.sourceforge.nite.meta.NCallableProgram;
import java.util.List;
import java.util.ArrayList;

/**
 * Describes an external program that can be called on this corpus
 *
 * @author jonathan 
 */
public class NiteCallableProgram implements NCallableProgram {
    private String name=null;
    private String description=null;
    private List requiredArgs=null;
    private List optionalArgs=null;

    public NiteCallableProgram(String name, String description) {
	this.name=name;
	this.description=description;
    }

    /** returns the file name of the program */
    public String getName() {
	return name;
    }

    /** returns a short description of the program */
    public String getDescription() {
	return description;
    }

    /** Returns a List of required "Argument"s belonging to this
	coding, in arbitrary order */
    public List getRequiredArguments() {
	return requiredArgs;
    }

    /** Returns a List of optional "Argument"s belonging to this
        coding, in arbitrary order */
    public List getOptionalArguments() {
	return requiredArgs;
    }

    public void addRequiredArgument(String flag, String type, String defaultValue) {
	if (requiredArgs==null) {
	    requiredArgs = new ArrayList();
	}
	requiredArgs.add(new Argument(flag, type, defaultValue));
    }

    public void addOptionalArgument(String flag, String type, String defaultValue) {
	if (optionalArgs==null) {
	    optionalArgs = new ArrayList();
	}
	optionalArgs.add(new Argument(flag, type, defaultValue));
    }

    public class Argument implements NCallableProgram.Argument {
	private static final String obstype = "observation";
	private static final String corpustype = "corpus";
	private static final String annotatortype = "annotator";

	private int type = UNKNOWN;
	private String flag = null;
	private String defaultValue = null;

	public Argument(String flag, String type, String defaultValue) {
	    this.flag=flag;
	    this.defaultValue=defaultValue;
	    if (type.equalsIgnoreCase(obstype)) { this.type=OBSERVATION_NAME; }
	    else if (type.equalsIgnoreCase(corpustype)) { this.type=CORPUS_NAME; }
	    else if (type.equalsIgnoreCase(annotatortype)) { this.type=ANNOTATOR_NAME; }
	}

	/** get the flag for this argument should not include the '-' */
	public String getFlagName() {
	    return flag;
	}

	/** get the default value if one is available */
	public String getDefaultValue() {
	    return defaultValue;
	}

	/** get the type of the argument - returns CORPUS_NAME or
	 * OBSERVATION_NAME if that is listed in the metadata, or
	 * UNKNOWN if not. */
	public int getType() {
	    return type;
	}
    }
}
