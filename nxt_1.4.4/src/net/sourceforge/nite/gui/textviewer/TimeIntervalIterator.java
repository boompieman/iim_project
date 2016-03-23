/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import java.util.Set;

/**
 * Utility to allow stateful interation across a timeline,
 * testing, for each time visited, which objects are in context
 * at that time.
 * 
 * @author judy
 */
public interface TimeIntervalIterator {

	/**
	 * Set the iterator to a specified time. This may result in
	 * the set of objects returned by {@see #getMatchingObjects()}
	 * being changed; this can be tested with {@see #hasObjectSetChanged()}.
	 * 
	 * @param time The time to which the iterator should seek.
	 */
	void setTime(double time);

	/**
	 * Set the iterator to a specified span. This may result in
	 * the set of objects returned by {@see #getMatchingObjects()}
	 * being changed; this can be tested with {@see #hasObjectSetChanged()}??
	 * 
	 * @param time The time to which the iterator should seek.
	 */
	void setTimes(double stime, double etime);

	/**
	 * Get the time to which the iterator has been set using
	 * {@see #setTime(double)}.
	 * 
	 * @return The time.
	 */
	double getTime();
	
	/**
	 * Get the set of Objects (as supplied to the underlying
	 * {@see TimeIntervalMapper}) which match the currently set time.
	 * 
	 * @return <code>Set</code> of matching objects.
	 * 
	 * @throws IllegalStateException if no time has been set.
	 */
	Set getMatchingObjects();
	
	/**
	 * Tests whether the last call to {@see #setTime(double)} caused
	 * the set of matching objects to change.
	 * 
	 * @return <code>true</code> iff the set has changed.
	 */
	boolean hasObjectSetChanged();

}
