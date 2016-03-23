/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;

import java.util.List;

/**
 * Information about a Corpus Resource. e.g. information about agents. 
 * A corpus resource can be any XML file
 *
 * @author jonathan */
public interface NCorpusResource extends NFile {
    /** returns a description of the CorpusResource. */
    public String getDescription(); 
    /** CorpusResources contain a bunch of layers */
    public List getLayers();
    /** get the top layer of this resource - returns an NLayer */
    public NLayer getTopLayer();
    /** get the bottom layer of this resource - returns an NLayer */
    public NLayer getBottomLayer();
}
