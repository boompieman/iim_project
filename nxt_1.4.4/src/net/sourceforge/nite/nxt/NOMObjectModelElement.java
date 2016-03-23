/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nxt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.nite.nstyle.NConstants;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.NOMPointer;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWritePointer;
import org.jdom.Namespace;

/**
 * This is a wrapper for NOMElement so that it implements the
 * ObjectModelElement interface 
 *
 * @author jonathan
 */

public class NOMObjectModelElement implements ObjectModelElement {
    private NOMElement element;
    private NOMElement parent;
    private String namespace;

    /** This is the name of the attribute belonging to this element
        which is displayed onscreen. Apparently **/
    private String displayedAttribute;

    private int index = 0;

    /**
     * Constructor JDomObjectModelElement.
     * @param element
     */
    public NOMObjectModelElement(NOMElement element) {
        this.element = element;
    }


    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#addAttribute(String, String)
     */
    public void addAttribute(String name, String value) {
	try {
	    element.setStringAttribute(name, value);
	} catch (NOMException nex) {
	    nex.printStackTrace();
	}
    }

    /**
     */
    public void deleteElement() {
	try {
	    if ((element==null) || (element.getParentInFile()==null)) {
		System.err.println("Failed to delete element - element is either null (element) or it is a root element");
		return;
	    }
	    element.getParentInFile().deleteChild(element);
	} catch (NOMException nex) {
	    nex.printStackTrace();
	}
    }

    public void addContent(String c) {
	try {
	    element.setText(c);
	} catch (NOMException nex) {
	    nex.printStackTrace();
	}
    }

    public String getTextualContent() {
        return element.getText();
    }

    public String getAttribute(String aname) {
        return element.getAttribute(aname).getStringValue();
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getAttributeValue()
     */
    public String getAttributeValue(String name) {
        return element.getAttribute(name).getStringValue();
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getDisplayedAttribute()
     */
    public String getDisplayedAttribute() {
        return displayedAttribute;
    }

    /**
     * Sets the displayedAttribute.
     * @param displayedAttribute The displayedAttribute to set
     */
    public void setDisplayedAttribute(String displayedAttribute) {
        this.displayedAttribute = displayedAttribute;
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getID()
     */
    public String getID() {
	if (element==null) { return null; }
        return element.getID();
    }

    /**
     * Returns the element.
     * @return NOMElement
     */
    public NOMElement getElement() {
        return element;
    }

    /**
     * Sets the element.
     * @param element The element to set
     */
    public void setElement(NOMElement element) {
        this.element = element;
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#removeChild(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void removeChild(ObjectModelElement e) {
	try {
	    if (e instanceof NOMObjectModelElement) {
		element.removeChild(((NOMObjectModelElement)e).getElement());
	    }
	} catch (NOMException nex) {
	    nex.printStackTrace();
	}

    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#makeElement()
     */
    public ObjectModelElement makeElement() {
	//     return new NOMObjectModelElement(new Element("user-created element"));
	return null;
    }

    /**
     */
    public void addPointer(String role, ObjectModelElement el) {
	try {
	    element.addPointer(new NOMWritePointer(element.getCorpus(), role, 
			element, ((NOMObjectModelElement)el).getElement()));
	} catch (NOMException nex) {
	    nex.printStackTrace();
	}
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#removePointer(java.lang.String, net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void removePointer(String role, ObjectModelElement e) {
	// I thought we needed different args here since we can have
	// multiple pointers with the same role?? Ah.. delete them all.
	for (Iterator pit=element.getPointers().iterator(); pit.hasNext(); ) {
	    NOMPointer np = (NOMPointer)pit.next();
	    if (np.getRole().equalsIgnoreCase(role)) {
		try { element.removePointer(np); }
		catch (NOMException nex) { nex.printStackTrace(); }
	    }
	}
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#addChild(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void addChild(ObjectModelElement e, int pos) {
	try {
	    element.addChild(((NOMObjectModelElement)e).getElement());
	} catch (NOMException nex) {
	    nex.printStackTrace();
	}
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getParent()
     */
    public ObjectModelElement getParent() {
	if (parent==null) {
	    parent=element.getParentInFile();
	}
	return (ObjectModelElement) parent;
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#setParent(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void setParent(ObjectModelElement e) {
	// I don't think this makes sense for NOM
	// parent = (NOMObjectModelElement) e;
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getChildIndex()
     */
    public int getChildIndex() {
        return index;
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#setChildIndex(int)
     */
    public void setChildIndex(int i) {
        index = i;
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getAttributeValuePairs()
     */
    public List getAttributes() {
        return element.getAttributes();

    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getName()
     */
    public String getName() {
        return element.getName();
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getNamespace()
     */
    public String getNamespace() {
	String qname = element.getName();
	int ind = qname.indexOf(":");
	if (ind<=1) { return null; }
	return qname.substring(0,ind);
    }

    /**
     * Method setNamespace.
     * @param string
     */
    public void setNamespace(String string) {
	//        element.setNamespace(Namespace.getNamespace(string));
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#addSibling(net.sourceforge.nite.nxt.ObjectModelElement, int)
     */
    public void addSibling(ObjectModelElement e, int pos) {
	try {
	    NOMElement parent = element.getParentInFile();
	    if (parent != null) {
		parent.addChildAfter(element, ((NOMObjectModelElement)e).getElement());
	    }
	} catch (NOMException nex) {
	    nex.printStackTrace();
	}
    }

    /**
     * @see net.sourceforge.nite.nxt.ObjectModelElement#getChildren()
     */
    public List getChildren() {
	List objectmodelchildren = new ArrayList();
	List children = element.getChildren();
	Iterator it = children.iterator();
	while(it.hasNext()){
	    NOMElement o = (NOMElement) it.next();
	    NOMObjectModelElement nomel = new NOMObjectModelElement(o);
	    objectmodelchildren.add(nomel);
	}
        return objectmodelchildren;
    }

    public ObjectModelElement copy() {
	NOMObjectModelElement ret = new NOMObjectModelElement(this.element);
	ret.setDisplayedAttribute(this.displayedAttribute);
	ret.setElement(this.element);
	return ret;
    }

}
