/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.time;

import java.awt.Color;

/**
 * Represents a display that keeps itself synchronized with the
 * current system time, and can reset current system time via its
 * timeHandler.  This is intended to cover either displays generated
 * using the stylesheet engine or the NIS generic display.
 * 
 * @author jeanc, jonathan, judy
 */
public interface TimeHandler {
    /** Accept a new time (generally from another registered
    TimeHandler). Do what you have to do in this TimeHandler to represent
    the fact that the global system time is now 'systemTime' (but don't
    inform the niteclock to pass it on). */
    public void acceptTimeChange(double systemTime);

    /** Broadcast a new time. Implement by sending a setSystemTime
      call to the current Clock. */
    public void setTime(double time);

    /** Accept a new time span from another registered time
      handler. This may involve changing our on-screen appearance to
      introduce some new time highlights, or playing a stretch of
      video, depending on the type of handler. */
    public void acceptTimeSpanChange(double start, double end);
    
    /** Broadcast a new span to all registered TimeHandlers. */
    public void setTimeSpan(double start, double end);

    /** Return the Clock that is currently syncronising this
      TimeHandler */
    public Clock getClock();

    /** Set the Clock to which this TimeHandler is registered */
    public void setClock(Clock clock);

    /** Change the Color used for all registered TimeHandlers to
    highlight times */
    public void setTimeHighlightColor(Color color); 

    /** find the largest end time handled by this TimeHandler. This is
    only really applicable to text areas. */
    public double getMaxTime();

}




