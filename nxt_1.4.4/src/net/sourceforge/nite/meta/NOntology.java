/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta;



/**
 * Information about an Ontology. An example is an ontology of gesture
 * types. Only one element name is permitted in an ontology (but it is
 * recursive. Only one significant attribute is permitted on ontology
 * elements.
 *
 * @author jonathan */
public interface NOntology extends NFile {
    /** returns a description of the ontology. */
    public String getDescription(); 
    /** returns the element name of this ontology. */
    public String getElementName();
    /** returns the significant attribute name for this ontology. */
    public String getAttributeName();
}
