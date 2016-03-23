/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

/**
 * Information about the location and type of a signal.
 *
 * @author jonathan 
 */
public interface NSignal {
    public static final int VIDEO_SIGNAL=1;
    public static final int AUDIO_SIGNAL=2;
    public static final int AGENT_SIGNAL=1;
    public static final int INTERACTION_SIGNAL=2;

    /** returns the name of the signal - used in file name */
    public String getName();
    /** getType returns either INTERACTION_SIGNAL or AGENT_SIGNAL. */
    public int getType();
    /** getMediaType returns either VIDEO_SIGNAL or AUDIO_SIGNAL. */
    public int getMediaType();
    /** returns the format of the signal. Returns an arbitrary string
        (whatever appears in the format attribute of the signal in the
        metadata file).  */
    public String getFormat();
    /** returns the extension of the signal, without the "." */
    public String getExtension();
    /** returns the full path and filename of the signal, given the
     * current observation and agent names */
    public String getFilename(String obsname, String agentname);
    /** returns the path modifier, replacing 'observation' with the
     * given observation name */
    public String getSignalPathModifier(String observation);
}
