/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.time;

import java.awt.Container;
import java.awt.Rectangle;
import javax.swing.JInternalFrame;

/**
 * Represents the clock that keeps the current time for the entire
 * synchronized set of display windows. 
 *
 * @author jeanc, jonathan
 */
public interface Clock {
    /** Register a TimeHandler that will henceforth be able to set and
     * get times from this Clock */ 
    public void registerTimeHandler(TimeHandler display);

    /** Deregister a TimeHandler so that it can no longer set and get
     * times from this Clock */
    public void deregisterTimeHandler(TimeHandler display);

    /** Receive a change in the system time and pass it on to all
     * registered TimeHandlers */
    public void setSystemTime(double newTime);

    /** Receive a change in the system time and pass it on to all
     * registered TimeHandlers other than the handler passed as an
     * argument*/
    public void setSystemTime(double newTime, TimeHandler handler);


    /** Receive notification of a new span and pass the new span to
     * all registered TimeHandlers. This version plays the segment
     * through in real time; if you don't want that behaviour, use the
     * version with a third (boolean) argument to turn off the playing
     * behaviour. */
    public void setTimeSpan(double start, double end);

    /** Receive notification of a new span and pass the new span to
     * all registered TimeHandlers; the third argument specifies
     * whether the segment is 'played' in real time by the clock. */
    public void setTimeSpan(double start, double end, boolean play);

    /** Receive notification of a new span and pass the new span to
     * all registered TimeHandlers other than the one passed as an
     * argument. This version plays the segment through in real time;
     * if you don't want that behaviour, use the version with a fourth
     * (boolean) argument to turn off the playing behaviour. */
    public void setTimeSpan(double start, double end, TimeHandler h);

    /** Receive notification of a new span and pass the new span to
     * all registered TimeHandlers other than the one passed as an
     * argument; the third argument specifies whether the segment is
     * 'played' in real time by the clock. */
    public void setTimeSpan(double start, double end, TimeHandler h, boolean play);

    /** Return the current system time */
    public double getSystemTime();

    /** Return the visual display of this clock (as a control strip) */
    public JInternalFrame getDisplay();

    /** Called by registered displays to allow the slider to be set correctly. */
    public void registerMaxTime(int max, PlayingTimeHandler display);

    /** 
     * set the number of seconds the fast-forward and rewind buttons jump
     */
    public void setWindSkip(double seconds);

    /** 
     * get the number of seconds the fast-forward and rewind buttons jump
     */
    public double getWindSkip();

    /** ensure visibility of interface */
    public void ensureVisible(Container desk);

    /** ensure visibility of interface, providing location */
    public void ensureVisible(Rectangle rect, Container desk);

    /** pass a play message to any ClockFace ui component, causing any
     * media files to start playing */
    public void play();

    /** pass a pause message to any ClockFace ui component, causing any
     * media files to be paused */
    public void pause();

    /** set the PlayingTimeHandler that controls time for this Clock */
    public void setMasterPlayer(PlayingTimeHandler pth);

} 
