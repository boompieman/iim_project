/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

/**
 * Information about a Pointer.
 *
 * @author jonathan 
 */
public interface NPointer {
    /** returns the role of this pointer */
    public String getRole();
    /** returns the target of the pointer as a String. This will
     * either be the name of a layer in a coding or corpus resource,
     * or the name of an ontology or object set.*/
    public String getTarget(); 
    /** returns the 'number' of this pointer - normally either '1' or '+'
        meaning 'exactly 1' and 'one or more' respectively */
    public String getNumber(); 
    /** returns the target layer of the pointer as a NLayer. */
    public NLayer getTargetLayer();
}
