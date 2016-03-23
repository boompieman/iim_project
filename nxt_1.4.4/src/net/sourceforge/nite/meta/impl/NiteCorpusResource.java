/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import java.util.List;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.meta.NCorpusResourceCoding;

/** 
 * An CorpusResource as referred to in the metadata
 *
 * @author jonathan
 */

public class NiteCorpusResource implements net.sourceforge.nite.meta.NCorpusResource {
    private String name;
    private String description;
    private String filename;
    private NCorpusResourceCoding coding;
    
    public NiteCorpusResource (String name, String description, String filename) {
	this.name=name;
	this.description=description;
	this.filename=filename;
    }

    /** returns the name of this CorpusResource as used for pointing to it */
    public String getName() {
	return name;
    }

    /** returns a description of the CorpusResource. */
    public String getDescription() {
	return description;
    }

    /** returns the filename of this CorpusResource */
    public String getFileName() {
	return filename;
    }

    /** get the list of layers valid in this resource - returns an
        ArrayList of NLayers */
    public List getLayers() {
	return coding.getLayers();
    }

    /** get the top layer of this resource - returns an NLayer */
    public NLayer getTopLayer() {
	return coding.getTopLayer();
    }
    /** get the bottom layer of this resource - returns an NLayer */
    public NLayer getBottomLayer() {
	return coding.getBottomLayer();
    }

    /** set the list of layers valid in this corpus resource - takes an
        ArrayList of NLayers */
    protected void setCoding(NCorpusResourceCoding coding) {
	this.coding=coding;
    }

}

