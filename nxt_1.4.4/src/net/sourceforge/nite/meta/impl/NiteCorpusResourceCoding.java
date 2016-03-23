/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2007, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import net.sourceforge.nite.meta.NCorpusResourceCoding;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.meta.NMetaData;

/**
 * A trivial extension of NCoding used so we can distinguish elements
 * and layers in corpus resource files from those in normal annotation
 * files.
 *
 * @author jonathan 
 */
public class NiteCorpusResourceCoding extends NiteCoding implements NCorpusResourceCoding {

    public NiteCorpusResourceCoding (NMetaData meta, String name, int type, NLayer top, NLayer bottom, String path) {
	super(meta, name, type, top, bottom, path);
    }    
}
