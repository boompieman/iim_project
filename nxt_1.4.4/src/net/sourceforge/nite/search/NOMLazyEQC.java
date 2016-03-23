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
 * of elements of any given name in the NOM at the moment, allowing
 * elements to be loaded into the corpus as a result of the call if
 * lazy loading is on.
 */
public class NOMLazyEQC implements ElementQuantityCalculator {
    NOMCorpus nom=null;

    public NOMLazyEQC(NOMCorpus nom) {
	this.nom=nom;
    }

    public int getNumber(String elname) {
	if (nom==null) { return 0; }
	List num = nom.getElementsByName(elname);
	//System.out.println("Counting " + elname + ": " + num.size());
	if (num==null) { return 0; }
	return num.size();
    }
}
