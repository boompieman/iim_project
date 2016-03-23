/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * IMS, University of Stuttgart
 * Holger Voormann
 */
package net.sourceforge.nite.search;

import net.sourceforge.nite.search.rewriter.QueryRewriter;

/**
 * Every corpus which implement the interface SearchableCorpus could be read
 * by NXT Search.
 * <br>For more information and web demo of NXT Search visit
 * <a href ="http://www.ims.uni-stuttgart.de/projekte/nite/>
 * http://www.ims.uni-stuttgart.de/projekte/nite/</a>.
 */
public interface SearchableCorpus
{

    /////////////////////////////////////////////////////////////////////////////
    // iterators

    /**
     * Returns an {@linkplain java.util.Iterator} which visits each element in
     * the corpora exactly once.
     * @return an {@linkplain java.util.Iterator} which visits each element in
     * the corpora exactly once
     */
    public java.util.Iterator getElements();

    /**
     * Returns an {@linkplain java.util.Iterator} which visits each element with
     * the specified type (= name of element) in the corpora exactly once.
     * It is allowed to visit also elements with other types but then there is no
     * search space reduction and the query processing may be less efficient.
     * It is not allowed to visit an element two times.
     * @param types list of types
     * @return an {@linkplain java.util.Iterator} which visits each element with
     * the specified type (= name of element) in the corpora exactly once
     */
    public java.util.Iterator getElements(java.util.List types);

    /**
     * Returns an {@linkplain java.util.Iterator} which visits each element
     * dominated by the specified element in the corpora exactly once.
     * It is allowed to visit also elements not dominated by the specified
     * element but then there is no search space reduction and the query
     * processing may be less efficient. It is not allowed to visit an element
     * two times.
     * @param rootElement the element which should dominate the requested
     * elements
     * @return an {@linkplain java.util.Iterator} which visits each element
     * dominated by the specified element in the corpora exactly once
     */
    public java.util.Iterator getElementsDominatedBy(Object rootElement);

    /**
     * Returns an {@linkplain java.util.Iterator} which visits each element
     * dominating the specified element in the corpora exactly once.
     * It is allowed to visit also elements not dominating the specified
     * element but then there is no search space reduction and the query
     * processing may be less efficient. It is not allowed to visit an element
     * two times.
     * @param childElement the element whisch should be dominated by the requested
     * elements
     * @return an {@linkplain java.util.Iterator} which visits each element
     * dominating the specified element in the corpora exactly once
     */
    public java.util.Iterator getElementsDominating(Object childElement);

    /**
     * Returns an {@linkplain java.util.Iterator} which visits each element which
     * has a pointer from the specified element in the corpora exactly once.
     * It is allowed to visit also elements not having a pointer from the
     * specified element but then there is no search space reduction and the
     * query processing may be less efficient. It is not allowed to visit an
     * element two times.
     * @param startElement the element where a pointer pointing to the requested
     * elements starts
     * @return an {@linkplain java.util.Iterator} which visits each element which
     * has a pointer from the specified element in the corpora exactly once
     */
    public java.util.Iterator getElementsPointedBy(Object startElement);

    /**
     * Returns an {@linkplain java.util.Iterator} which visits each element of
     * the specified subgraphs in the corpora exactly once.
     * It is allowed to visit also elements not being in the specified subgraphs
     * but then there is no search space reduction and the query processing may
     * be less efficient. It is not allowed to visit an element two times.
     * @param pointingElement the element pointing to the subgraphs
     * @return an {@linkplain java.util.Iterator} which visits each element of
     * the specified subgraphs in the corpora exactly once.
     */
    public java.util.Iterator getElementsOfSubgraph(Object pointingElement);


    /////////////////////////////////////////////////////////////////////////////
    // attribute, text, name, time (start, end, duration, center), id

    /**
     * Returns the value of an attribute of an element as
     * {@linkplain Comparable}.
     * @param element the element with the attribute, that will be returned
     * @param name the name of the attribute
     * @return the value of an attribute of an element as {@linkplain Comparable}
     */
    public java.lang.Comparable getAttributeComparableValue(Object element, String name);

    /// next two methods adde by JK to attempt typing and aid comparison accuracy
    /**
     * Returns the value of an attribute of an element as a
     * {@linkplain Double} if possible (null if not).
     * @param element the element with the requested attribute
     * @param name the name of the attribute
     * @return the value of an attribute of an element as {@linkplain Double}
     */
    public Double getAttributeDoubleValue(Object element, String name);

    /**
     * Returns the value of an attribute of an element as a
     * {@linkplain String}.
     * @param element the element with the requested attribute
     * @param name the name of the attribute
     * @return the value of an attribute of an element as {@linkplain String}
     */
    public String getAttributeStringValue(Object element, String name);


    /**
     * Returns the value of the text content as {@linkplain Comparable}.
     * @param element the element containing the text, that will be returned
     * @return the value of the text content as {@linkplain Comparable}
     */
    public java.lang.Comparable getText(Object element);

    /**
     * Returns the name resp. type of an element.
     * @param element the element with the name, that will be returned
     * @return the name resp. type of an element
     */
    public String getNameOfElement(Object element);

    /**
     * Returns the start time as a {@linkplain java.lang.Comparable} value.
     * @param elemen the element with the start time, that will be returned
     * @return the start time as a {@linkplain java.lang.Comparable} value
     */
    public java.lang.Comparable getStartComparableValue(Object element);

    /**
     * Returns the end time as a {@linkplain java.lang.Comparable} value.
     * @param elemen the element with the end time, that will be returned
     * @return the end time as a {@linkplain java.lang.Comparable} value
     */
    public java.lang.Comparable getEndComparableValue(Object element);

    /**
     * Returns the temporal duration as a {@linkplain java.lang.Comparable} value.
     * @param elemen the element with the temporal duration, that will be returned
     * @return the temporal duration as a {@linkplain java.lang.Comparable} value
     */
    public java.lang.Comparable getDurationComparableValue(Object element);

    /**
     * Returns the center of start and end time as a
     * {@linkplain java.lang.Comparable} value.
     * @param elemen the element with the center of start and end time, that will
     * be returned
     * @return the center of start and end time as a
     * {@linkplain java.lang.Comparable} value
     */
    public java.lang.Comparable getCenterComparableValue(Object element);

    /**
     * Returns the ID of an element as a {@linkplain java.lang.Comparable} value.
     * @param element the element with the ID, that will be returned
     * @return the ID of an element as a {@linkplain java.lang.Comparable} value
     */
    public java.lang.Comparable getIdComparableValue(Object element);


    /////////////////////////////////////////////////////////////////////////////
    // equality

    /**
     * Returns true if element A is the same element as element B.
     * @param a element A
     * @param b element B
     * @return true if element A is the same element as element B
     */
    public boolean testIsEqual   (Object a, Object b);

    /**
     * Returns true if element A is not the same element as element B.
     * @param a element A
     * @param b element B
     * @return true if element A is not the same element as element B
     */
    public boolean testIsInequal (Object a, Object b);


    /////////////////////////////////////////////////////////////////////////////
    // strucural relations

    /**
     * Returns true if element A dominates element B.
     * Notice that an element also dominates itself.
     * @param a element A
     * @param b element B
     * @return true if element A dominates element B
     */
    public boolean testDominates         (Object a, Object b);

    /**
     * Returns true if element A dominates element B with the specified
     * distance.
     * Notice that with distance=0 this metode is equale to
     * {@linkplain #testIsEqual(java.lang.Object, java.lang.Object)}. Also
     * distance < 0 is possible, means element B dominates element B.
     * @param a element A
     * @param b element B
     * @param distance distance between element A and element B
     * @return true if element A dominates element B with the specified
     * distance
     */
    public boolean testDominates         (Object a, Object b, int distance);

    /**
     * Returns true if element A precedes element B.
     * @param a element A
     * @param b element B
     * @return true if element A precedes element B
     */
    public boolean testPrecedes          (Object a, Object b);

    /**
     * Returns true if there is a pointer from the first to the second element.
     * @param from start element of the pointer
     * @param to target element of the pointer
     * @return true if there is a pointer from the first to the second element
     */
    public boolean testHasPointer        (Object from, Object to);

    /**
     * Returns true if there is a pointer from the first to the second element
     * with the specified role.
     * @param from start element of the pointer
     * @param to target element of the pointer
     * @param role the role of the pointer
     * @return true if there is a pointer from the first to the second element
     * with the specified role
     */
    public boolean testHasPointer        (Object from, Object to, String role);

    /**
     * Returns true if there is a pointer from the first element to another
     * element which is dominated by the second element.
     * This methode may be usefull for type hierarchies.
     * @param from start element of the pointer
     * @param to element which dominates target element of the pointer
     * @return true if there is a pointer from the first element to another
     * element which is dominated by the second element
     */
    public boolean testDominatesSubgraph (Object from, Object to);

    /**
     * Returns true if there is a pointer with a specified role from the first
     * element to another element which is dominated by the second element.
     * This methode may be usefull for type hierarchies.
     * @param from start element of the pointer
     * @param to element which dominates target element of the pointer
     * @param role the role of the pointer
     * @return true if there is a pointer with a specified role from the first
     * element to another element which is dominated by the second element
     */
    public boolean testDominatesSubgraph (Object from, Object to, String role);


    /////////////////////////////////////////////////////////////////////////////
    // temporal relations

    /**
     * Returns true if the element A is timed.
     * Timed means either the element has explicit start and end time or all its
     * children are timed.
     * @param a element A
     * @return true if the element A is timed
     */
    public boolean testTimed (Object a);

    /**
     * Returns true if element A overlaps left element B.
     * Means a_start <= b_start and a_end > b_start and a_end <= b_end.
     * @param a element A
     * @param b element B
     * @return true if element A overlaps left element B
     */
    public boolean testOverlapsLeft (Object a, Object b);

    /**
     * Returns true if element A is left aligned with element B.
     * Element A and B are starting at the same time, so a_start is equale to
     * b_start.
     * @param a element A
     * @param b element B
     * @return true if element A is left aligned with element B
     */
    public boolean testLeftAlignedWith (Object a, Object b);

    /**
     * Returns true if element A is right aligned with element B.
     * Element A and B are stoping at the same time, so a_end is equale to b_end.
     * @param a element A
     * @param b element B
     * @return true if element A is right aligned with element B
     */
    public boolean testRightAlignedWith (Object a, Object b);

    /**
     * Returns true if element A temporally includes element B.
     * Means a_start <= b_start and a_end >= b_end.
     * @param a element A
     * @param b element B
     * @return true if element A temporally includes element B.
     */
    public boolean testIncludes (Object a, Object b);

    /**
     * Returns true if element A and element B have the same duration.
     * Means a_start == b_start and a_end == b_end.
     * @param a element A
     * @param b element B
     * @return true if element A and element B have the same duration
     */
    public boolean testSameExtend (Object a, Object b);

    /**
     * Returns true if element A overlaps element B.
     * Means a_end > b_start and b_end > a_start.
     * @param a element A
     * @param b element B
     * @return true if element A overlaps element B
     */
    public boolean testOverlapsWith (Object a, Object b);

    /**
     * Returns true if element A ends at the time element B starts.
     * Means a_end == b_start.
     * @param a element A
     * @param b element B
     * @return true if element A ends at the time element B starts
     */
    public boolean testContactWith (Object a, Object b);

    /**
     * Returns true if element A temporally precedes element B.
     * Means a_end <= b_start.
     * @param a element A
     * @param b element B
     * @return true if element A temporally precedes element B
     */
    public boolean testPrecedesTemporal (Object a, Object b);

    /** set to true to enable the new query rewrite functionality that
     * can increase the speed of your queries */
    public void setQueryRewriting(boolean val);

    /** true means we have enabled the new query rewrite functionality
     * that can increase the speed of your queries. false (the
     * default) means we haven't */
    public boolean isQueryRewriting();

    /** Enable the query rewrite functionality and select a rewriter
     * to use (if the argument is null, query rewriting will not be
     * enabled). */
    public void setQueryRewriter(QueryRewriter writer);

    /** Return the query rewriter that should be used (or null if it
     * is not set) */
    public QueryRewriter getQueryRewriter();

}
