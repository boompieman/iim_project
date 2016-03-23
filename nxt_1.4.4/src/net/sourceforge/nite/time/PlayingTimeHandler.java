/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
// TimeHandler.java
package net.sourceforge.nite.time;

/** NITE Playing Time Handler. Implemented by any class on which we
 *  can play, rewind, fast forward etc.
 */
public interface PlayingTimeHandler extends TimeHandler {
    /** start playing the continuous stream */
    public void play();
    /** pause the continuous stream */
    public void pause();
    /** fast forward the continuous stream by the specified amount */
    public void fastForward(double seconds);
    public void rewind(double seconds);
    /** fast forward the continuous stream by the specified amount */
    public void fastForward();
    /** rewind the continuous stream by the pre-defined amount
     * (defaults to 5 seconds) */
    public void rewind();
    /** set the rate of play so we can sound like pinky and perky. 1 is normal. */
    public void setPlayRate(float rate);
    /** return the file name currently being played by this player */
    public String getFileName();
    /** true if we're playing */
    public boolean isPlaying();
    /** return the time of this media source */
    public double getTime();
    /** return true if this player has the 'master' button checked,
     * and thus controls time for the application */
    public boolean isMaster();
    /** Make this player control time for the application if the
     * argument is 'true', or stop it from being if 'false'. */
    public void setMaster(boolean mast);
    /** return a boolean: true the given time is after the
     * end time of the media file.  */
    public boolean pastEndTime(double ctime);
}
