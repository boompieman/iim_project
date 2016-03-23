/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2006, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.search;

import net.sourceforge.nite.nom.nomwrite.*;
import java.util.List;

/**
 * This implementation of ElementQuantityCalculator returns the number
 * of elements of any given name in the NOM at the moment, making sure
 * we don't load anything into the corpus as a result of the call.
 */
public class NOMExistingEQC implements ElementQuantityCalculator {
    NOMCorpus nom=null;
    public NOMExistingEQC(NOMCorpus nom) {
	this.nom=nom;
    }

    public int getNumber(String elname) {
	if (nom==null) { return 0; }
	boolean ret = nom.isLazyLoading();
	nom.setLazyLoading(false);
	List num = nom.getElementsByName(elname);
	nom.setLazyLoading(ret);
	if (num==null) { return 0; }
	return num.size();
    }
}
