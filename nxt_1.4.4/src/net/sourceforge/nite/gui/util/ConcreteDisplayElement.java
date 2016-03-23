/**
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

/**
 * See also AbstractDisplayElement, AbstractCallableToolConfig and NXTConfig class.
 *
 * It's common for elements in tools to have a specific element they
 * are displaying or creating and a type which is either an enumerated
 * attribute or a pointer into a type ontology. This is the simplest possible
 * implementation of the AbstractDisplayElement interface.
 *
 * @author Jonathan Kilgour, UEdin
 */
public class ConcreteDisplayElement implements AbstractDisplayElement {
    private String elementName=null;
    private String enumeratedAttribute=null;
    private String typeDefault=null;
    private String typeRoot=null;
    private String typeRole=null;
    private String typeGloss=null;
    private String elementNameLong=null;
    private String elementNameShort=null;

    /** Convenience class for displays of specific elements. Simply
     * holds incormation about an element with a type that's either an
     * enumerated attribute or a pointer into a type ontology. */
    public ConcreteDisplayElement(String elname, String attr, String deft, String root, 
				  String role, String gloss, String longname, String shortname) {
	this.elementName=elname;
	this.enumeratedAttribute=attr;
	this.typeDefault=deft;
	this.typeRoot=root;
	this.typeRole=role;
	this.typeGloss=gloss;
	this.elementNameLong=longname;
	this.elementNameShort=shortname;
    }

    /** Return the name of the element this display is predominantly
     * designed for displaying or creating. */
    public String getElementName() {
	return elementName;
    }

    /** Return the full name of the element this display is
     * predominantly designed for displaying or creating: can be
     * multiple words e.g. "Dialogue Act". */
    public String getElementNameLong() {
	return elementNameLong;
    }

    /** Return the short name of the element this display is predominantly
     * designed for displaying or creating: e.g. "DA" */
    public String getElementNameShort() {
	return elementNameShort;
    }

    /** true if the type is an enumerated attribute; false if the type
     * is a pointer to an ontology. */
    public boolean typeIsEnumeratedVariable() {
	return (enumeratedAttribute!=null);
    }

    /** Return the name of the enumerated attribute that is used for
     * the type (only required to be set if typeIsEnumeratedVariable()
     * is true). */
    public String getEnumeratedTypeAttribute() {
	return enumeratedAttribute;
    }

    /** Return the role of the pointer from the base element into the
     * type ontology */
    public String getTypeRole() {
	return typeRole;
    }

    /** Return the name of the root element of the type ontology */
    public String getTypeOntologyRoot() {
	if (enumeratedAttribute!=null) { return null; }
	return typeRoot;
    }

    /** Return the name of the attribute on elements of the type
     * ontology that give a 'gloss' of their meaning */
    public String getTypeGloss() {
	return typeGloss;
    }

    /** If the type is an enumerated attribute, this is the String
     * value of the default; if the type is an ontology this is the ID
     * of the default type element. */
    public String getTypeDefault() {
	return typeDefault;
    }
    
}
