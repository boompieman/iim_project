/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite;

import java.util.List;
import java.util.Set;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.meta.*;

/**
 * Extends the nomread version with methods for adding & deleting
 * elements & attributes
 *
 * @author jonathan 
 */
public interface NOMElement {
    public static final double UNTIMED=Double.NaN;

    public static final int FEATURAL_LAYER=0;
    public static final int STRUCTURAL_LAYER=1;
    public static final int TIME_ALIGNED_LAYER=2;
    
    public static final int UNORDERED=0;
    public static final int IMPLICIT_ORDER=1;
    public static final int TEMPORAL_ORDER=2;

    /** returns the name of the element (not namespace qualified) */
    public String getName();

    /** returns the "colour" of the element (this essentially
        determines the serialization of elements) */
    public String getColour();

    /** returns the NResource of the element: this is null unless a
     * resource file is used for this corpus, in which case it can
     * affect the location of the file on disk etc.
     */
    public NResource getResource();

    /** sets the NResource for this element - use with caution as this
     * will change the serialization of this element. */
    public void setResource(NResource resource);

    /** returns the textual content of the element */
    public String getText();

    /** returns the ID of the element */
    public String getID();

    /** returns the name of the observation to which the element belongs */
    public String getObservation();

    /** returns the Graphical Visual Markup string for the element:
     * this is used by the GRAM tool only for encoding information
     * about on-video overlays */
    public String getGVM();

    /** returns the keystroke associated with this element (as a string) */
    public String getKeyStroke();

    /** returns a List of NOMElements which are immediate descendents
     * of this element (excluding comment elements) */
    public List getChildren();

    /** This version of getChildren includes any comments that are
     * present in the correct order within the child stream. */
    public List getChildrenWithInterleavedComments();

    /** returns a List of NOMElements which directly dominate this element*/
    public List getParents();

    /** returns a List of NOMPointers pointing from this element*/
    public List getPointers();

    /** returns a List of NOMPointers that point to this element */
    public List getPointersTo();

    /** returns a List of NOMAttributes of this element */
    public List getAttributes();

    //    public NOMAttribute getAttribute(String attribute_name);

    /** returns the start time of the element */
    public double getStartTime();

    /** returns the end time of the element */
    public double getEndTime();

    /** returns the integer TIME_ALIGNED_LAYER, STRUCTURAL_LAYER or
     * FEATURAL_LAYER: the type of the element as far as timing is
     * concerned. */
    public int getTimeType();

    /** specifies whether the children are temporally ordered,
     * implicitly ordered or unordered by returning one of UNORDERED,
     * IMPLICIT_ORDER or TEMPORAL_ORDER */
    public int getChildOrder();

    /** A Boolean which is true if this element is a 'stream' type
        element */
    public boolean isStreamElement();

    /**
     * Returns the value of an attribute specified by his name as
     * {@link Comparable}.  The interface {@linkplain Comparable} is
     * useful for comparing values. Because both, {@linkplain String}
     * and {@linkplain Double}, implement {@link Comparable}, this is
     * a useful shorthand, and may speed up reading for example in the
     * search engine.
     * @param name the name of the attribute
     * @return the value of an attribute as {@link Comparable}
     */
    public Comparable getAttributeComparableValue(String name);

    /**
     * Returns the xlink:href to the element this element is stored.
     * The xlink:href could be something like "file.xml#id".
     * @return the xlink:href to the element this element is stored
     */
    public String getXLink();

    /** returns the contents of the reserved comment String (or null if not set) */
    public String getComment();

    /** returns true if this element is a comment */
    public boolean isComment();

    /** get the actual recursive depth of this element in its
     * recursive layer where 0 means no parents in this layer; 1 means
     * one etc. */
    public int getRecursiveDepth();

    /** get the actual recursive height of this element in its
     * recursive layer where 0 means no children in this layer; 1 means
     * kids to a depth of 1 etc. */
    public int getRecursiveHeight();

    /** Returns the metadata element for this element. */
    public NElement getMetadataElement() throws NOMException;

    /** Returns the metadata layer to which this element belongs. Used
        when adding elements and in serialization */
    public NLayer getLayer()  throws NOMException;

    /** Returns the agent responsible for the element (or null if
        it's not in an agent coding). */
    public NAgent getAgent();

    /** returns the name of the agent responsible for the
        element (or null if there is none) */
    public String getAgentName();
    
    /** return the NOMAttribute on this element that has the given
     * name (or null if no such attribute exists). */
    public NOMAttribute getAttribute(String attribute_name);

    /** Simply return the Corpus that this element is a part of */
    public NOMCorpus getCorpus();

    /** return a string representing an XLink to this element */
    public String getLink();

    /** returns the NOMElement belonging to the same colour */
    public NOMElement getParentInFile();

    /* Return the first ancestor we find with the given name or null
       if there are none */
    public NOMElement findAncestorNamed(String name);

    /* Return the first ancestor we find in the given Layer or null
       if there are none */
    public NOMElement findAncestorInLayer(NLayer layer);

    /* Return the List of NOMElements that are ancestors of this one in
     * the same file (may include stream element, and may return
     * null) */
    public List findAncestorsInFile();

    /* Return all ancestors of the given name or the empty Set if
     * there are none */
    public Set findAncestorsNamed(String name);

    /* Return all ancestors in the given NLayer or the empty Set if
     * there are none */
    public Set findAncestorsInLayer(NLayer layer);

    /* Return the nearest NOMElement in the same file that is an
     * ancestor of this element and of the given NOMElement: returns
     * null if there is no such element, and may return the stream
     * element. */
    public NOMElement findCommonAncestorInFile(NOMElement other);

    /* Return all descendants in the given NLayer or the empty List if
     * there are none */
    public List findDescendantsInLayer(NLayer layer);

    /* Return all descendants with the given name or the empty List if
     * there are none */
    public List findDescendantsNamed(String name);

    /** returns true if there is an element following this one in the
        corpus if it were to be serialized */
    public boolean hasNextElement();

    /** returns the element following this one in the corpus if it
	were to be serialized */
    public NOMElement getNextElement();

    /** returns this element's following sibling of the same colour */
    public NOMElement getNextSibling();

    /** returns this element's previous sibling of the same colour */
    public NOMElement getPreviousSibling();

    /*-------------------------------------------------------------*/
    /* WRITE ONLY METHODS. These generally throw an exception if the
     * NOM is in fact read-only. */
    /*-------------------------------------------------------------*/

    /** Set the value of a named string attribute */
    public void setStringAttribute(String name, String value) throws NOMException;

    /** Set the value of a named Double attribute */
    public void setDoubleAttribute(String name, Double value) throws NOMException;

    /** add an attribute to the element or if it exists already, just set it */
    public void addAttribute(NOMAttribute attribute) throws NOMException;

    /** Remove an attribute completely from an element */
    public void removeAttribute(String name) throws NOMException;

    /** add a pointer to the element */
    public void addPointer(NOMPointer pointer) throws NOMException;

    /** remove a pointer from the element */
    public void removePointer(NOMPointer pointer) throws NOMException;

    /** returns the first NOMPointer which has a matching role */
    public NOMPointer getPointerWithRole(String rolename);

    /** returns the value of any external pointer from this element. */
    public String getExternalPointerValue();

    /** Set the start time of this element. Only elements in a timed
        layer can have their start and end times set directly; all
        structural times are strictly inherited. */
    public void setStartTime(double time) throws NOMException;

    /** Set the end time of this element. Only elements in a timed
        layer can have their start and end times set directly; all
        structural times are strictly inherited. */
    public void setEndTime(double time) throws NOMException;

    /** Set the "stream"ness of the element - a stream element is one
        which is purely there as a container for other elements for
        the purposes of serialization. */
    public void setStreamElement(boolean stream);

    /** adds the NOMElement as the first child */
    public void addFirstChild(NOMElement child) throws NOMException;

    /** adds the NOMElement as the last child */
    public void addLastChild(NOMElement child) throws NOMException;

    /** adds the NOMElement newchild immediately before the given
        existing child, or reports an exception if the child cannot be
        found */
    public void addChildBefore(NOMElement oldchild, NOMElement newchild) throws NOMException;

    /** adds the NOMElement newchild immediately after the given
        existing child, or reports an exception if the child cannot be
        found */
    public void addChildAfter(NOMElement oldchild, NOMElement newchild) throws NOMException;

    /** Remove a nite:child from an element without actually deleting
     *  the element referred to. Throw an error if we're trying to
     *  remove the child from its serialized parent. */
    public void removeChild(NOMElement child) throws NOMException;

    /** Delete an element. Remove the child from all its
     * parents. Ranges need to be handled separately (in a different
     * thread?) */
    public void deleteChild(NOMElement child) throws NOMException;

    /** sets the textual content of an element. */
    public void setText(String chars) throws NOMException;

    /** appends to the textual content of an element. */
    public void appendText(String chars) throws NOMException;

    /** sets the name of the element - not commonly used! */
    public void setName(String name);

    /** sets the Graphical Visual Markup string for this element */
    public void setGVM(String name);

    /** sets the keystroke string associated with this element */
    public void setKeyStroke(String name);

    /** add an element to the corpus with no information about
        location. This should only be used with elements that are in
        the top level of a coding. If the element is in a timed layer
        and has start and end times, the placement of the addition in
        the "stream" is derived from those. Otherwise the element is
        added at the end of the stream. */
    public void addToCorpus() throws NOMException;

    /** add the element as a sibling of the given element. Of course
        the siblings must be in the same layer and the ordering must
        not violate any of the precedence constraints */
    public void addBeforeSibling(NOMElement sibling) throws NOMException;

    /** add the element as a sibling of the given element. Of course
        the siblings must be in the same layer and the ordering must
        not violate any of the precedence constraints */
    public void addAfterSibling(NOMElement sibling) throws NOMException;

    /** adds a child to this element. Since no order is specified, add
	the child to the end of the list of children. */
    public void addChild(NOMElement child) throws NOMException;

    /** Adds the NOMElement newchild in place of the given child
     * list, making the current children children of the newly
     * added node. */
    public void addChildAboveChildren(NOMElement newchild, List old_children) throws NOMException;

    /** Remove the parent node, making me and my siblings children of
     * our grandparent. Possibly need to check validity... */
    public void removeParentAndAdjust(NOMElement par) throws NOMException;

    /** Insert n new parent node, making me and my siblings children
     * of the new parent and making our current parent the parent
     * of the new parent */
    public void insertParent(NOMElement newparent, NOMElement oldparent) throws NOMException;

    /** return a shared view of this element which simply provides
        utility functions for editing the element without thinking
        about locking and unlocking the corpus. */
    public SharedEl getShared();

    /** set the contents of the reserved comment attribute */
    public void setComment(String comment);

    /** Set the href value of the external pointer from this
     * element. Only allowed in elements oin layers of type
     * EXTERNAL_POINTER_LAYER  */
    public void addExternalPointer(String role, String value) throws NOMException;

    /** This inner interface provides some utility functions for shared
        NOM users */
    public interface SharedEl {

	/** Set the value of a named string attribute. NOM-sharing version. */
	public void setStringAttribute(NOMView view, String name, String value) throws NOMException;

	/** Set the value of a named Double attribute. NOM-sharing version. */
	public void setDoubleAttribute(NOMView view, String name, Double value) throws NOMException;
	
	/** Remove an attribute from the element completely. NOM-sharing
	    version. */
	public void removeAttribute(NOMView view, String name) throws NOMException;
	
	/** add an attribute to the element. NOM-sharing version */
	public void addAttribute(NOMView view, NOMAttribute attribute) throws NOMException;
	
	/** add a pointer to the element. NOM-sharing version. */
	public void addPointer(NOMView view, NOMPointer pointer) throws NOMException;
	
	/** remove a pointer from the element. NOM-sharing version. */
	public void removePointer(NOMView view, NOMPointer pointer) throws NOMException;
	
	/** adds the NOMElement as the first child. NOM-sharing version. */
	public void addFirstChild(NOMView view, NOMElement child) throws NOMException;
	
	/** adds the NOMElement as the last child. NOM-sharing version. */
	public void addLastChild(NOMView view, NOMElement child) throws NOMException;
	
	/** adds the NOMElement newchild immediately before the given
	    existing child, or reports an exception if the child cannot be
	    found. NOM-sharing version. */
	public void addChildBefore(NOMView view, NOMElement oldchild, NOMElement newchild) throws NOMException;
	
	/** adds the NOMElement newchild immediately after the given
	    existing child, or reports an exception if the child cannot be
	    found. NOM-sharing version. */
	public void addChildAfter(NOMView view, NOMElement oldchild, NOMElement newchild) throws NOMException;
	
	/** Adds the NOMElement newchild in place of the given child
	 * list, making the current children children of the newly
	 * added node. NOM-sharing version. */
	public void addChildAboveChildren(NOMView view, NOMElement newchild, List old_children) throws NOMException;

	/** Remove the parent node, making me and my siblings children of
	 * our grandparent. NOM share version */
	public void removeParentAndAdjust(NOMView view, NOMElement par) throws NOMException;

	/** Insert a new parent node, making me and my siblings
	 * children of the new parent and making our current parent
	 * the parent of the new parent */
	public void insertParent(NOMView view, NOMElement newparent, NOMElement oldparent) throws NOMException;

	/** Remove a nite:child from an element without actually deleting
	 *  the element referred to. Throw an error if we're trying to
	 *  remove the child from its serialized parent. NOM-sharing version. */
	public void removeChild(NOMView view, NOMElement child) throws NOMException;
	
	/** Delete an element. Remove the child from all its
	 * parents. NOM-sharing version. */
	public void deleteChild(NOMView view, NOMElement child) throws NOMException;
	
	/** adds a child to this element. Since no order is specified, add
	    the child to the end of the list of children. NOM-sharing version. */
	public void addChild(NOMView view, NOMElement child) throws NOMException;
	
	/** sets the textual content of an element. NOM-sharing version. */
	public void setText(NOMView view, String chars) throws NOMException;
	
	/** Set the start time of this element. Only elements in a timed
	    layer can have their start and end times set directly; all
	    structural times are strictly inherited. NOM-sharing version. */
	public void setStartTime(NOMView view, double st) throws NOMException;
	
	/** Set the end time of this element. Only elements in a timed
	    layer can have their start and end times set directly; all
	    structural times are strictly inherited. NOM-sharing version. */
	public void setEndTime(NOMView view, double et) throws NOMException;
	
	/** add an element to the corpus with no information about
	    location. This should only be used with elements that are in
	    the top level of a coding. If the element is in a timed layer
	    and has start and end times, the placement of the addition in
	    the "stream" is derived from those. Otherwise the element is
	    added at the end of the stream. NOM-sharing version*/
	public void addToCorpus(NOMView view) throws NOMException;
	
	/** add the element as a sibling of the given element. Of course
	    the siblings must be in the same layer and the ordering must
	    not violate any of the precedence constraints. NOM-sharing
	    version. */
	public void addBeforeSibling(NOMView view, NOMElement sibling) throws NOMException;
	
	/** add the element as a sibling of the given element. Of course
	    the siblings must be in the same layer and the ordering must
	    not violate any of the precedence constraints. NOM-sharing
	    version. */
	public void addAfterSibling(NOMView view, NOMElement sibling) throws NOMException;

	/** Set the href value of the external pointer from this
	 * element. Only allowed in elements on layers of type
	 * EXTERNAL_POINTER_LAYER. NOM-sharing version  */
	public void addExternalPointer(NOMView view, String role, String value) throws NOMException;

    }
} 
