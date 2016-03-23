/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.link.NOMView;

/**
 * Extends the nomread version with methods for editing pointers
 *
 * @author jonathan 
 */
public interface NOMPointer  {
    /** returns the role of the pointer */
    public String getRole();

    /** returns the element which is the source of the pointer */
    public NOMElement getFromElement();

    /** returns the element which is the destination of the pointer */
    public NOMElement getToElement();

    /** returns the element which is the destination of the pointer as
        a string (the original href string from the loaded corpus) */
    public String getToElementString();

    /** returns the contents of the reserved comment String (or null if not set) */
    public String getComment();

    /** return a string representing an XLink to the pointed-to
        element */
    public String getLink();

    /** set the role of this pointer */
    public void setRole(String role);

    /** set the element to which this pointer points */
    public void setToElement(NOMElement target) throws NOMException;

    /** return a shared view of this pointer which simply provides a
        single utility function for editing the pointer without
        thinking about locking and unlocking the corpus. */
    public SharedPoint getShared();

    /** set the contents of the reserved comment attribute */
    public void setComment(String comment);

    /** This inner interface provides a single utility function for
        shared NOM users - can be ignored if NOM is not shared amongst
        multiple applications that can write to the NOM. */
    public interface SharedPoint {
	/** set the element to which this pointer points */
	public void setToElement(NOMView view, NOMElement target) throws NOMException;
    }
} 
