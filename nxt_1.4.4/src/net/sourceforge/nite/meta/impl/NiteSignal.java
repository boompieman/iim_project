/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import net.sourceforge.nite.meta.NSignal;
import net.sourceforge.nite.meta.NMetaData;
import java.io.File;

/** 
 * A signal as referred to in the metadata
 *
 * @author jonathan
 */

public class NiteSignal implements net.sourceforge.nite.meta.NSignal {
    private int media=NSignal.VIDEO_SIGNAL;
    private int type=NSignal.INTERACTION_SIGNAL;
    private String name;
    private String format;
    private String extension;
    private String modifier="";
    NMetaData meta;
    
    public NiteSignal (NMetaData meta, int media, int type, String name, String format, 
		       String extension, String modifier) {
	this.meta=meta;
	if (media==NSignal.AUDIO_SIGNAL) { this.media=NSignal.AUDIO_SIGNAL; }
	if (type==NSignal.AGENT_SIGNAL) { this.type=NSignal.AGENT_SIGNAL; }
	this.name=name;
	this.format=format;
	this.extension=extension;
	if (modifier!=null) { this.modifier=File.separator + modifier; }
    }

    /** returns the name of the signal - used in file name */
    public String getName() {
	return name;
    }

    /** getType returns either INTERACTION_SIGNAL or AGENT_SIGNAL. */
    public int getType() {
	return type;
    }

    /** getMediaType returns either VIDEO_SIGNAL or AUDIO_SIGNAL. */
    public int getMediaType() {
	return media;
    }

    /** returns the format of the signal. Returns an arbitrary string
        (whatever appears in the format attribute of the signal in the
        metadata file).  */
    public String getFormat() {
	return format;
    }

    /** returns the extension of the signal, without the "." */
    public String getExtension() {
	return extension;
    }

    /** returns the path modifier, replacing 'observation' with the
     * given observation name */
    public String getSignalPathModifier(String observation) {
	return modifier.replaceAll("observation", observation);
    }

    /** returns the full path and filename of the signal, given the
     * current observation and agent names (agent name can be null for
     * interaction signals of course) */
    public String getFilename(String obsname, String agentname) {
	if (type==NSignal.AGENT_SIGNAL) {
	    return meta.getSignalPath() + meta.getSignalPathModifier(obsname) + 
		getSignalPathModifier(obsname) + File.separator + obsname + "." + 
		agentname + "."  + name + "." + extension;
	}
	return meta.getSignalPath() + meta.getSignalPathModifier(obsname) + 
	    getSignalPathModifier(obsname) + File.separator + obsname + "."  + 
	    name + "." + extension;
    }

}

