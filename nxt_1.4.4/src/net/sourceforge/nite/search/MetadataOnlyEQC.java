/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2006, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.search;

import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.meta.*;
import java.util.List;

/**
 * This implementation of ElementQuantityCalculator returns an
 * approximation of the relative number of elements of any given name
 * by simply looking at the metadata structure.
 */
public class MetadataOnlyEQC implements ElementQuantityCalculator {
    private static int SMALL=10;
    private static int MEDIUM=100;
    private static int LARGE=1000;

    NMetaData metadata=null;

    public MetadataOnlyEQC(NOMCorpus nom) {
	metadata=nom.getMetaData();
    }

    public int getNumber(String elname) {
	NElement nel = metadata.getElementByName(elname);
	if (nel==null || nel.getContainer()==null) { return 0; }
	// corpus resources - shouldn't be many of these!
	if (nel.getContainerType()!=NElement.CODING) { return SMALL; }
	NLayer nlay = nel.getLayer();
	if (nlay==null) { return 0; }
	int retnum = SMALL;
	if (nlay.getLayerType()==NLayer.STRUCTURAL_LAYER) {
	    retnum=MEDIUM;
	} else if (nlay.getLayerType()==NLayer.TIMED_LAYER) {
	    retnum=LARGE;
	}
	if (nlay.getRecursive()) { retnum = retnum * 2; }
	return retnum;
    }
}
