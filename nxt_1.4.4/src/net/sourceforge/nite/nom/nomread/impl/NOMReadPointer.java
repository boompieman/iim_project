/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomread.impl;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.link.NOMView;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWritePointer;

/**
 * Extends the nomwrite version restricting methods for editing pointers
 *
 * @author jonathan 
 */
public class NOMReadPointer extends NOMWritePointer {

    public NOMReadPointer (NOMCorpus corpus, String role, NOMElement source, String targetstr) {
	super(corpus, role, source, targetstr);
    }

    public NOMReadPointer (NOMCorpus corpus, String role, NOMElement source, NOMElement target) {
	super(corpus, role, source, target);
    }

    /** set the element to which this pointer points */
    public void setToElement(NOMElement target) throws NOMException {
	if (getCorpus().isLoadingFromFile()) { super.setToElement(target); }
	else {
	    throw new NOMException("Read-only corpus: cannot set pointer's destination.");
	}
    }

    /** return null since this is a read-only corpus. */
    public SharedPoint getShared() {
	return null;
    }

    /** set the contents of the reserved comment attribute - do
     * nothing since we're read-only. */
    public void setComment(String comment) {

    }

} 
