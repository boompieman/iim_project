/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import net.sourceforge.nite.meta.NLayer;
/** 
 * An pointer as referred to in the metadata
 *
 * @author jonathan
 */

public class NitePointer implements net.sourceforge.nite.meta.NPointer {
    private String role;
    private String target;
    private String number;
    private NiteLayer layer=null;
    
    public NitePointer (String role, String target, String number) {
	this.role=role;
	this.target=target;
	this.number=number;
    }

    /** returns the role of this pointer */
    public String getRole() {
	return role;
    }

    /** returns the target of the pointer as a String. This will
     * either be the name of a layer in a coding or corpus resource,
     * or the name of an ontology or object set.*/
    public String getTarget() {
	return target;
    }

    /** returns the 'number' of this pointer - normally either '1' or '+'
        meaning 'exactly 1' and 'one or more' respectively */
    public String getNumber() {
	return number;
    }

    /** returns the target layer of the pointer as a NLayer. */
    public NLayer getTargetLayer() {
	return (NLayer)layer;
    }

    /** set the target layer of the pointer - should only be used by
        the metadata parsing process. */
    protected void setTargetLayer(NLayer layer) {
	this.layer=(NiteLayer)layer;
    }

}

