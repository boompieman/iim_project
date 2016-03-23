/**
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

/**
 * See also AbstractCallableToolConfig and NXTConfig class.
 *
 * It's common for elements in tools to have a specific element they
 * are displaying or creating and a type which is either an enumerated
 * attribute or a pointer into a type ontology.  This interface makes
 * it easier for the tools to fit together: e.g. the linker element
 * can now care less about what it's linking together.
 *
 * @author Jonathan Kilgour, UEdin
 */
public interface AbstractDisplayElement {
    /** Return the name of the element this display is predominantly
     * designed for displaying or creating. */
    public String getElementName();
    /** true if the type is an enumerated attribute; false if the type
     * is a pointer to an ontology. */
    public boolean typeIsEnumeratedVariable();
    /** Return the name of the enumerated attribute that is used for
     * the type (only required to be set if typeIsEnumeratedVariable()
     * is true). */
    public String getEnumeratedTypeAttribute();
    /** Return the role of the pointer from the base element into the
     * type ontology */
    public String getTypeRole();
    /** Return the name of the root element of the type ontology */
    public String getTypeOntologyRoot();
    /** Return the name of the attribute on elements of the type
     * ontology that give a 'gloss' of their meaning */
    public String getTypeGloss();
    /** Return the full name of the element this display is
     * predominantly designed for displaying or creating: can be
     * multiple words e.g. "Dialogue Act". */
    public String getElementNameLong();
    /** Return the short name of the element this display is predominantly
     * designed for displaying or creating: e.g. "DA" */
    public String getElementNameShort();
    /** If the type is an enumerated attribute, this is the String
     * value of the default; if the type is an ontology this is the ID
     * of the default type element. */
    public String getTypeDefault();
}

