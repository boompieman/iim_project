/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2006, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.search;

/**
 * A grand name for something pretty simple in essence: an
 * ElementQuantityCalculator returns the number of elements of a
 * particular type in a corpus. 
 */
public interface ElementQuantityCalculator {
    public int getNumber(String elname);
}
