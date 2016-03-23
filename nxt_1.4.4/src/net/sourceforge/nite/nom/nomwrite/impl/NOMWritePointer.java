/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite.impl;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.link.*;

/**
 * A simple implementation of a pointer in a NOM corpus.
 *
 * @author jonathan 
 */
public class NOMWritePointer implements NOMPointer {
    private String role=null;
    private NOMElement source=null;
    private NOMElement target=null;
    private String target_string=null;
    private String comment=null;
    private NOMCorpus corpus;

    public NOMWritePointer (NOMCorpus corpus, String role, NOMElement source, String targetstr) {
	this.corpus=corpus;
	this.role=role;
	this.source=source;
	this.target_string=targetstr;
	if (targetstr==null) {
	    System.err.println("ERROR: nite:pointer with no href attribute. Cannot be resolved.");
	    //System.exit(0);
	}
    }

    public NOMWritePointer (NOMCorpus corpus, String role, NOMElement source, NOMElement target) {
	this.corpus=corpus;
	this.role=role;
	this.source=source;
	this.target=target;
	if (target==null) {
	    System.err.println("ERROR: nite:pointer ponting to null element.");
	    //System.exit(0);
	}
    }

    /** returns the role of the pointer */
    public String getRole() {
	return role;
    }

    /** returns the element which is the source of the pointer */
    public NOMElement getFromElement() {
	return source;
    }

    /** returns the element which is the destination of the pointer */
    public NOMElement getToElement() {
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedPointer(source, role);
	}
	return target;
    }

    /** Simply return the Corpus that this pointer is a part of */
    public NOMCorpus getCorpus() {
	return corpus;
    }

    /** set the role of this pointer */
    public void setRole(String role) {
	this.role=role;
    }

    /** set the element to which this pointer points */
    public void setToElement(NOMElement targ) throws NOMException {
	if (targ==null) return;
	if (corpus.isEditSafe()==false) { 
	    throw new NOMException("Corpus cannot be edited without lock when it is being shared amongst more than one NOMView"); 
	}	
	this.target=targ;
	corpus.notifyChange(new DefaultEdit(source, NOMEdit.EDIT_POINTER, (Object)target));
    }

    /** returns the element which is the destination of the pointer as
        a string (the original href string from the loaded corpus) */
    public String getToElementString() {
	return target_string;
    }

    private String single_escape(String in) {
	return "'" + in + "'";
    }

    /** Return the full link to the pointed to element */
    public String getLink() {
	if (target==null) { return target_string; } 
	else {
	    return target.getColour() + ".xml" + corpus.getLinkFileSeparator() +
		corpus.getLinkBeforeID() + 
		//		single_escape(target.getID()) + 
		target.getID() + 
		corpus.getLinkAfterID();
	}
    }

    /** return a shared view of this pointer which simply provides a
        utility function for editing the pointer without thinking
        about locking and unlocking the corpus. */
    public SharedPoint getShared() {
	return new SharedPointer(this);
    }

    /** returns the contents of the reserved comment String (or null if not set) */
    public String getComment() {
	return comment;
    }

    /** set the contents of the reserved comment attribute */
    public void setComment(String comment) {
	this.comment=comment;
    }
    
    /** This inner class provides a utility function for shared
        NOM users */
    public class SharedPointer implements SharedPoint {
	NOMWritePointer nwa;
	public SharedPointer (NOMWritePointer nwa) {
	    this.nwa=nwa;
	}

	/** set the element to which this pointer points */
	public void setToElement(NOMView view, NOMElement target) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.setToElement(target);
		getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }
	}

    }
} 
