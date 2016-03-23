/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.mediaviewer;

import net.sourceforge.nite.time.*;
import java.util.*;
import java.awt.Color;

/**
 * This is an in-between level class. It shouldn't be used by
 * application programs: it's created by the Clock implementation to
 * control all the other PlayingTimeHandlers registered with the
 * Clock. We want this separation because the messages passed to
 * PlayingTimeHandlers (they're continuous players like media players)
 * should be significantly different to the behaviour of discrete
 * players. The former should get few messages: things like 'play' and
 * 'pause' from button presses, while the latter get an update every
 * time the system clock changes.
 *
 * @author jonathan
 */
public class NMediaHandler implements PlayingTimeHandler {
    public Set playingHandlers = new HashSet(); // of type PlayingTimeHandler
    Clock clock;

    public NMediaHandler(Clock clock) {
	this.clock=clock;
    }

    /** Accept a new time (generally from another registered
    TimeHandler). Do what you have to do in this TimeHandler to represent
    the fact that the global system time is now 'systemTime' (but don't
    inform the niteclock to pass it on). */
    public void acceptTimeChange(double systemTime) {
	Iterator it = playingHandlers.iterator();
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    th.acceptTimeChange(systemTime);
	}		
    }

    /** Broadcast a new time from a media player, but only to the
     * elements up the tree. */
    public void setTime(double time) {
	//System.out.println("Set time: " + time);
    }

    /** Broadcast a new time from a media player, but only to the
     * elements up the tree. */
    public void setTime(double time, TimeHandler child) {
	//System.out.println("Set time: " + time);
	clock.setSystemTime(time, this);
    }

    /** Accept a new time span from another registered time
      handler. This may involve changing our on-screen appearance to
      introduce some new time highlights, or playing a stretch of
      video, depending on the type of handler. */
    public void acceptTimeSpanChange(double start, double end) {
	//System.out.println("Span passed to media handler - passing on to media players");
	
	Iterator it = playingHandlers.iterator();
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    th.pause();
	    th.acceptTimeChange(start);
	}	
	/*
	it = playingHandlers.iterator();
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    th.acceptTimeSpanChange(start, end);
	}	
	*/
    }
    
    /** Broadcast a new span to all registered TimeHandlers. I don't
     * think we can get this from a media player, so it's not
     * currently implemented */
    public void setTimeSpan(double start, double end) {

    }

    /** Return the Clock that is currently syncronising this
      TimeHandler */
    public Clock getClock() {
	return clock;
    }

    /** Set the Clock to which this TimeHandler is registered */
    public void setClock(Clock clock) {
	this.clock=clock;
	Iterator it = playingHandlers.iterator();
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    th.setClock(clock);
	}	
	//niteclock.registerTimeHandler(this);
    }

    /** Change the Color used for all registered TimeHandlers to
    highlight times - this is not relevant to PlayingTimeHandlers */
    public void setTimeHighlightColor(Color color) {

    }

    /** start playing the continuous stream */
    public void play() {
	Iterator it = playingHandlers.iterator();
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    th.play();
	}
    }


    /** pause the continuous stream */
    public void pause() {
	Iterator it = playingHandlers.iterator();
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    th.pause();
	}
    }

    /** fast forward the continuous stream by the pre-defined amount
     * (defaults to 5 seconds) */
    public void fastForward() {
	Iterator it = playingHandlers.iterator();
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    th.fastForward();
	}
    }

    /** fast forward the continuous stream by the pre-defined amount
     * (defaults to 5 seconds) */
    public void fastForward(double seconds) {
	Iterator it = playingHandlers.iterator();
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    th.fastForward(seconds);
	}
    }

    /** rewind the continuous stream by the pre-defined amount
     * (defaults to 5 seconds) */
    public void rewind() {
	Iterator it = playingHandlers.iterator();
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    th.rewind();
	}
    }

    /** rewind the continuous stream by the pre-defined amount
     * (defaults to 5 seconds) */
    public void rewind(double seconds) {
	Iterator it = playingHandlers.iterator();
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    th.rewind(seconds);
	}
    }


    /** Register a PlayingTimeHandler that will henceforth be
     * controlled from the parent Clock. */ 
    public void registerPlayer(PlayingTimeHandler display) {
	playingHandlers.add(display);
	if (display.getClock()==clock) {
	    display.setClock(clock);
	}
    } 

    /** Deregister a PlayingTimeHandler so that it is no longer
     * controlled by the parent Clock */
    public void deregisterPlayer(TimeHandler display) {
	playingHandlers.remove(display);
    } 

    /** fast forward the continuous stream by the pre-defined amount
     * (defaults to 5 seconds) */
    public void setPlayRate(float rate) {
	Iterator it = playingHandlers.iterator();
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    th.setPlayRate(rate);
	}
    }

    /** get the latest time handled by this TimeHandler - this returns
     * 0.0 because we leave it to the individual media players to
     * register their times with the clock when they have
     * pre-fetched. */
    public double getMaxTime() {
	return 0.0;
    }

    /** return the file name currently being played by this player -
     * not relevant here! */
    public String getFileName() {
	return null;
    }

    public boolean isPlaying() {
        Iterator it = playingHandlers.iterator();
    	while (it.hasNext()) {
    	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
    	    if(th.isPlaying()){
    	        return true;
    	    }
    	}
    	return false;
    }

    /** return the time of a media source */
    public double getTime() {
        Iterator it = playingHandlers.iterator();
	if (it==null || !it.hasNext()) { return clock.getSystemTime(); }
	PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	if (th!=null) { return th.getTime(); }
	else { return clock.getSystemTime(); }
    }

    /** return true if this player has the 'master' button checked,
     * and thus controls time for the application */
    public boolean isMaster() {
	return false;
    }

    /** Make this player control time for the application if the
     * argument is 'true', or stop it from being if 'false'. */
    public void setMaster(boolean mast) {
	
    }

    /** Return the file name being played. */
    public String getName() {
	return null;
    }

    /** return a boolean: true the given time is after the
     * end time of the media file.  */
    public boolean pastEndTime(double ctime) {
	return false;
    }

}

