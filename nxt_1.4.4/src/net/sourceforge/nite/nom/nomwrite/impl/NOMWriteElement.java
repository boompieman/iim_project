/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.util.Debug;
import net.sourceforge.nite.util.XMLutils;
import org.xml.sax.Attributes;

/**
 * This abstract class is extended by the three different types of
 * element in a NOM Corpus: Annotations, Objects (part of an object
 * set) and TypeElements (part of a type hierarchy).
 *
 * @author jonathan 
 */
public abstract class NOMWriteElement implements NOMElement {
    private int type=NLayer.FEATURAL_LAYER;
    private int order=UNORDERED;
    private int children_count=0;
    private String id=null;
    private String gvm=null;
    private String key=null;
    private String who=null;
    private String comment=null;
    private NAgent meta_agent=null;
    private String text_content;
    private boolean stream=false;
    private double start=NOMElement.UNTIMED;
    private double end=NOMElement.UNTIMED;
    private String name=null;
    private String colour=null;
    private String observation=null;
    private NResource resource=null;
    private ArrayList attributes=null;
    private ArrayList comments=null;
    private ArrayList children=null;
    private ArrayList local_children=null;
    private ArrayList nite_children=null; // Temporary home for remote children
    private ArrayList parents=null;
    private ArrayList pointers=null;
    private NOMWriteElement real_parent=null;
    protected NOMCorpus corpus=null;
    protected NMetaData meta=null;
    private NElement nel=null;
    private boolean is_leaf = false;
    private boolean is_comment = false;
    private String external_pointer_href=null;
    private boolean pointers_resolved=false;

    /*--------------*/
    /* CONSTRUCTORS */
    /*--------------*/

    /** This constructor is only used by the process that builds the
        NOM from files: it uses the org.xml.sax.Attributes to code the
        attributes. Note that because we know it's an
        internal-building call, we know we should be using the
        corpus's NOMMaker rather than direct constructor calls for
        pointers and attributes. */
    protected NOMWriteElement(NOMCorpus corpus, String name, 
		      Attributes attributes, String colour, NResource resource,
		      boolean stream) throws NOMException {
	this.name=name;
	this.colour=colour;
	this.corpus=corpus;
	this.resource=resource;
	this.meta=corpus.getMetaData();
	this.stream=stream;
	if (stream && (!name.equals(meta.getStreamElementName()))) {
	    boolean bad=true;
	    nel = meta.getElementByName(name);
	    if (nel==null) {
		//	System.out.println("Null metadata element for " + name);
	    }
	    if (nel!=null && nel.getContainerType()!=NElement.CODING) {
		bad=false;
	    }
	    if (bad) {
		Debug.print("WARNING: Stream element \"" + name + "\" does not have the declared NITE stream element name \"" + meta.getStreamElementName() + "\".", Debug.WARNING);
	    }
	    // this.name=meta.getStreamElementName();
	}
	if (corpus == null) {
	    throw new NOMException("Error creating element '" + name + "'. Null corpus");
	}
	if (attributes == null) {
	    throw new NOMException("Error creating element '" + name + "'. No ID attribute given.");
	}
	this.id=attributes.getValue(meta.getIDAttributeName());
	if (stream==false) { 
	    corpus.registerID(this.id, colour); 
	}
	this.who=attributes.getValue(meta.getAgentAttributeName());
	this.gvm=attributes.getValue(meta.getGVMAttributeName());
	this.key=attributes.getValue(meta.getKeyStrokeAttributeName());
	this.comment=attributes.getValue(meta.getCommentAttributeName());
	if ((stream==false) && (!name.equals(meta.getTextElementName()))) {
	    nel = meta.getElementByName(name);
	    if (nel==null) {
		throw new NOMException("Error creating element '" + name + "'. Element not found in metadata");
	    }
	    if (nel.getContainerType()==NElement.CODING) {
		NLayer elay = nel.getLayer();
		if (elay==null) {
		    throw new NOMException("Error creating element '" + name + "'. Element does not appear in any layer in the metadata file!");
		}
		this.type=elay.getLayerType();
	    
		if (elay.getChildLayer() == null) {
		    is_leaf = true;
		} 
	    } else {
		this.type=NLayer.FEATURAL_LAYER;
		is_leaf=true;
	    }
	    
	    //	    System.out.println("Adding an element of type " + name + " which is " + this.type);
	    
	    if (this.type == NLayer.TIMED_LAYER || this.type==NLayer.STRUCTURAL_LAYER) {
		String sts=attributes.getValue(meta.getStartTimeAttributeName());
		String ets=attributes.getValue(meta.getEndTimeAttributeName());
		if (sts!=null) {
		    try {
			this.start=Double.valueOf(sts).doubleValue();
		    } catch (Exception exc) {
			this.end=NOMElement.UNTIMED;
			// System.err.println("Failed to parse time in timed element!");
		    }
		}
		if (ets!=null) {
		    try {
			this.end=Double.valueOf(ets).doubleValue();
		    } catch (Exception exc) {
			this.end=NOMElement.UNTIMED;
			//  System.err.println("Failed to parse time in timed element!");
		    }
		}
	    }
	    this.attributes=processAttributes(attributes, nel);
	} else {
	    this.attributes=processAttributes(attributes);
	}
	// There is one attribute we always want to ignore on input -
	// we stick it out when serializing where necessary.
	try {
	    removeAttribute(NOMWriteCorpus.LINKTYPEATTR);
	} catch (Exception e) {	}

	meta_agent=getAgent();
	if (meta.getObservationAttributeName()!=null && colour.indexOf(".")>-1) {
	    String observation=colour.substring(0,colour.indexOf("."));
	    setStringAttribute(meta.getObservationAttributeName(), observation);
	}
	if (meta.getResourceAttributeName()!=null && resource!=null) {
	    setStringAttribute(meta.getResourceAttributeName(), resource.getID());
	}
	if (comment!=null) { // make available to query etc
	    setStringAttribute(meta.getCommentAttributeName(), comment);
	}
	corpus.notifyChange(new DefaultEdit(this, NOMEdit.ADD_ELEMENT, null));
	//	corpus.addChangedColour(colour);
	//	System.out.println("New Element: " + this.getID() + "; type: " + name + " and colour: " + colour);

    }

    /** This constructor is called when new elements are added
        programatically. This version takes an ID and colour and
        checks neither! Use with caution */
    protected NOMWriteElement(NOMCorpus corpus, String name, String colour, NResource resource,
			   boolean stream, String id ) throws NOMException {
	this.name=name;
	this.colour=colour;
	this.corpus=corpus;
	this.resource=resource;
	this.meta=corpus.getMetaData();
	this.stream=stream;
	if (stream && (!name.equals(meta.getStreamElementName()))) {
	    Debug.print("WARNING: Stream element \"" + name + "\" does not have the declared NITE stream element name \"" + meta.getStreamElementName() + "\".", Debug.WARNING);
	    //  this.name=meta.getStreamElementName();
	}
	if (corpus == null) {
	    throw new NOMException("Error creating element '" + name + "'. Null corpus");
	}
	if (id == null) {
	    throw new NOMException("Error creating element '" + name + "'. Null ID given.");
	}
	this.id=id;
	if ((stream==false) && (!name.equals(meta.getTextElementName()))) {
	    NElement nel = meta.getElementByName(name);
	    if (nel==null) {
		throw new NOMException("Error creating element '" + name + "'. Element not found in metadata");
	    }
	    if (nel.getContainerType()==NElement.CODING) {
		NLayer elay = nel.getLayer();
		if (elay==null) {
		    throw new NOMException("Error creating element '" + name + "'. Element does not appear in any layer in the metadata file!");
		}
		this.type=elay.getLayerType();
	    
		if (elay.getChildLayer() == null) {
		    is_leaf = true;
		} 
	    } else {
		this.type=NLayer.FEATURAL_LAYER;
		is_leaf=true;
	    }
	    
	}

	meta_agent=getAgent();
	if (meta.getObservationAttributeName()!=null && colour.indexOf(".")>-1) {
	    String observation=colour.substring(0,colour.indexOf("."));
	    setStringAttribute(meta.getObservationAttributeName(), observation);
	}

	if (meta.getResourceAttributeName()!=null && resource!=null) {
	    setStringAttribute(meta.getResourceAttributeName(), resource.getID());
	}
	corpus.notifyChange(new DefaultEdit(this, NOMEdit.ADD_ELEMENT, null));
	//	corpus.addChangedColour(colour); 
    }

    /** take out what's common so we're not too repetetive */
    private void commonConstruction(NOMCorpus corpus, String name, String observation, String agent, String mid, NResource resource) throws NOMException {
	this.name=name;
	this.corpus=corpus;
	this.observation=observation;
	this.meta=corpus.getMetaData();
	this.resource=resource;
	this.stream=false;
	this.who=agent;
	this.id=mid;
	String mycolour=null;

	if (corpus == null) {
	    throw new NOMException("Error creating element '" + name + "'. Null corpus");
	}
	nel=meta.getElementByName(name);
	if (nel==null) {
	    throw new NOMException("Error creating element '" + name + "'. Element not found in metadata");
	}

	if (this instanceof NOMWriteTypeElement) {
	    this.type=NLayer.FEATURAL_LAYER;
	    is_leaf=true;
	    mycolour=((NOntology)nel.getContainer()).getName();
	    //throw new NOMException ("Error creating element '" + name + "' - this is an Ontology element which cannot be added programatically!");
	} else if (this instanceof NOMWriteObject) {
	    this.type=NLayer.FEATURAL_LAYER;
	    is_leaf=true;
	    mycolour=((NObjectSet)nel.getContainer()).getName();
	} else {
	    NLayer nlay=nel.getLayer();
	    if (nlay==null) {
		throw new NOMException("Error creating element '" + name + "'. Element does not appear in any layer in the metadata file!");
	    }
	    this.type=nlay.getLayerType();
	    NCoding container=(NCoding)nlay.getContainer();

	    /* Generate colour and ID. Colour generation should I guess be
	       generated in a more generic manner! */
	    mycolour=observation;
	    if (container.getType()==NCoding.AGENT_CODING) {
		if (agent==null || agent.equals("")) {
		    throw new NOMException("Error creating element '" + name + "'. Element is in an agent coding, but no agent was given");
		}
		mycolour=mycolour + "." + agent;
		//		System.out.println(((NCoding)container).getName() + " is an agent coding. ");
	    }
	    
	    if (this instanceof NOMWriteResourceElement) {
		mycolour = container.getName();
	    } else {
		if (observation==null || observation.equals("")) {
		    throw new NOMException("Error creating element '" + name + "'. Element is in an agent or interaction coding, but no observation was given.");
		}	    
		mycolour = mycolour + "." + container.getName();
	    }
	    //System.out.println("Colour: " + mycolour);
	}

	this.colour=mycolour;
	
	if (this.id==null) {
	    this.id=corpus.generateID(mycolour, resource);
	}

	if (this.id == null) {
	    throw new NOMException("Error creating element '" + name + "'. Null ID attribute generated!.");
	} 

	this.meta_agent=getAgent();
	if (meta.getObservationAttributeName()!=null) {
	    setStringAttribute(meta.getObservationAttributeName(), observation);
	}
	if (meta.getResourceAttributeName()!=null && resource!=null) {
	    setStringAttribute(meta.getResourceAttributeName(), resource.getID());
	}
	corpus.notifyChange(new DefaultEdit(this, NOMEdit.ADD_ELEMENT, null));
	//	corpus.addChangedColour(colour); 
    }

    /** The colour of the element is derived from the
        metadata and the ID is derived from that. IDs generated will
        be unique with respect to the corpus subset that is currently
        loaded. */
    protected NOMWriteElement(NOMCorpus corpus, String name,
			      String observation, String agent, NResource resource) throws NOMException {
	commonConstruction(corpus, name, observation, agent, null, resource);
    }

    /** The colour of the element is derived from the
        metadata and the ID is derived from that. IDs generated will
        be unique with respect to the corpus subset that is currently
        loaded. */
    protected NOMWriteElement(NOMCorpus corpus, String name,
			      String observation, String agent) throws NOMException {
	commonConstruction(corpus, name, observation, agent, null, null);
    }

    /** The colour of the element is derived from the
        metadata and the ID is derived from that. IDs generated will
        be unique with respect to the corpus subset that is currently
        loaded. */
    protected NOMWriteElement(NOMCorpus corpus, String name,
			      String observation, NAgent agent, NResource resource) throws NOMException {
	if (agent==null) {
	    commonConstruction(corpus, name, observation, null, null, resource);
	} else {
	    commonConstruction(corpus, name, observation, agent.getShortName(), null, resource);
	}
    }

    /** The colour of the element is derived from the
        metadata and the ID is derived from that. IDs generated will
        be unique with respect to the corpus subset that is currently
        loaded. */
    protected NOMWriteElement(NOMCorpus corpus, String name,
			      String observation, NAgent agent) throws NOMException {
	if (agent==null) {
	    commonConstruction(corpus, name, observation, null, null, null);
	} else {
	    commonConstruction(corpus, name, observation, agent.getShortName(), null, null);
	}
    }

    /** Create an annotation element where the colour of the element
        is derived from the metadata and the ID is provided by the
        caller. */
    protected NOMWriteElement(NOMCorpus corpus, String name,
			      String observation, String agent, String id, NResource resource) throws NOMException {
	commonConstruction(corpus, name, observation, agent, id, resource);	
    }

    /** This constructor creates a comment element */
    protected NOMWriteElement (NOMCorpus corpus, String comment, String colour, NResource resource) throws NOMException {
	is_comment=true;
	text_content=comment;
	this.colour=colour;
	this.corpus=corpus;
	this.resource=resource;
	if (corpus == null) {
	    throw new NOMException("Error creating comment element. Null corpus");
	}
	//	corpus.addChangedColour(colour);
    }

    /* utility to check edits are OK */
    private void checkEditSafe() throws NOMException {
	if (corpus.isEditSafe()==false) { 
	    throw new NOMException("Corpus cannot be edited without lock when it is being shared amongst more than one NOMView"); 
	}	
    }

    /*---------------------*/
    /* Handling Attributes */
    /*---------------------*/

    /** Set the value of a named string attribute; create the
        attribute if it doesn't exist. */
    public void setStringAttribute(String name, String value) throws NOMException {
	checkEditSafe();
	if (name.equals(meta.getIDAttributeName()) ||
	    name.equals(meta.getStartTimeAttributeName()) ||
	    name.equals(meta.getEndTimeAttributeName())) {
	    throw new NOMException("Attempt to set a reserved attribute '" + name + "' using SetStringAttribute method. Element: '" + this.name + "' (ID: " + this.id + ").");
	}

	NOMWriteAttribute nattr = (NOMWriteAttribute) this.getAttribute(name);
	if (nattr==null) {
	    nattr = new NOMWriteAttribute(NOMAttribute.NOMATTR_STRING, name,
					  value, null);
	    nattr.setElement(this);
	    if (attributes==null) { attributes=new ArrayList(); }
	    attributes.add((Object)nattr);
	} else {
	    nattr.setStringValueUnnotified(value);
	}
	corpus.notifyChange(new DefaultEdit(this, NOMEdit.SET_ATTRIBUTE, (Object)name));
    }


    /** Set the value of a named Double attribute; Create the
        attribute if it doesn't exist. */
    public void setDoubleAttribute(String name, Double value) throws NOMException {
	checkEditSafe();
	if (name.equals(meta.getIDAttributeName()) ||
	    name.equals(meta.getStartTimeAttributeName()) ||
	    name.equals(meta.getEndTimeAttributeName())) {
	    throw new NOMException("Attempt to set a reserved attribute '" + name + "' using SetDoubleAttribute method. Element: '" + this.name + "' (ID: " + this.id + ").");
	}

	NOMWriteAttribute nattr = (NOMWriteAttribute) this.getAttribute(name);
	if (nattr==null) {
	    nattr = new NOMWriteAttribute(NOMAttribute.NOMATTR_NUMBER, name,
					  null, value);
	    nattr.setElement(this);
	    if (attributes==null) { attributes=new ArrayList(); }
	    attributes.add((Object)nattr);
	} else {
	    nattr.setDoubleValueUnnotified(value);
	}
	corpus.notifyChange(new DefaultEdit(this, NOMEdit.SET_ATTRIBUTE, (Object)name));
    }


    /** Remove an attribute completely from an element */
    public void removeAttribute(String name) throws NOMException {
	checkEditSafe();
	if (name.equals(meta.getIDAttributeName()) ||
	    name.equals(meta.getStartTimeAttributeName()) ||
	    name.equals(meta.getEndTimeAttributeName())) {
	    throw new NOMException("Attempt to remove a reserved attribute '" + name + "' from element '" + this.name + "' (ID: " + this.id + ").");
	}
	
	NOMWriteAttribute nattr = (NOMWriteAttribute) this.getAttribute(name);
	if (nattr!=null) {
	    attributes.remove((Object)nattr);
	}
	corpus.notifyChange(new DefaultEdit(this, NOMEdit.SET_ATTRIBUTE, (Object)name));
    }

    
    /*-------------------*/
    /* Handling Pointers */
    /*-------------------*/

    /** returns a List of NOMPointers */
    public List getPointers() {
	if (!pointers_resolved && corpus instanceof NOMWriteCorpus) { 
	    ((NOMWriteCorpus)corpus).resolvePointers(this, pointers); 
	}
	return pointers;
    }

    /** returns a List of NOMPointers that point to this element*/
    public List getPointersTo() {
	return corpus.getPointersTo(this);
    }

    /** returns the first NOMPointer which has a matching role */
    public NOMPointer getPointerWithRole(String rolename) {
	if (pointers == null) { return null; }
	for (Iterator pit = pointers.iterator(); pit.hasNext(); ) {
	    NOMPointer point = (NOMPointer) pit.next();
	    if (point.getRole().equalsIgnoreCase(rolename)) {
		return point;
	    }
	}
	return null;
    }

    /** returns the value of any external pointer from this element. */
    public String getExternalPointerValue() {
	return external_pointer_href;
    }

    /** add a pointer to the element */
    public void addPointer(NOMPointer pointer) throws NOMException {
	checkEditSafe();
	if (pointers==null) { pointers=new ArrayList(); }
	pointers.add((Object)pointer);
	corpus.notifyChange(new DefaultEdit(this, NOMEdit.ADD_POINTER, (Object)pointer));
    }

    /** remove a pointer from the element */
    public void removePointer(NOMPointer pointer) throws NOMException {
	checkEditSafe();
	pointers.remove(pointer);
	corpus.notifyChange(new DefaultEdit(pointer.getFromElement(), NOMEdit.DELETE_POINTER, (Object)pointer));
    }


    /*-------------------------------*/
    /* Handling Children and Parents */
    /*-------------------------------*/

    /** the idea of this method is that when an element is added as a
        child of an element, we make sure it's actually got a 'real'
        (file) parent. */
    private void safeAddParent(NOMWriteElement child) throws NOMException {
	if (child.getParents()==null) {
	    NOMWriteElement realparent = (NOMWriteElement)corpus.getRootWithColour(child.getColour(), child.getResource());
	    if (realparent != null) { realparent.addLastChild(child); }
	}	
	child.addParent((NOMElement)this); 
    }

    /** adds the NOMElement as the first child */
    public void addFirstChild(NOMElement child) throws NOMException {
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedChildren(this, colour);
	}
	checkEditSafe();
	if (children==null) { children=new ArrayList(); }
	children.add(0,(Object)child);
	children_count++;
	updateTimes();
	if (child.getColour().equals(colour)) { 
	    ((NOMWriteElement)child).setParent((NOMElement)this); 
	    if (local_children==null) { local_children=new ArrayList(); }
	    local_children.add(0,(Object)child);
	} else { 
	    safeAddParent((NOMWriteElement)child);	    
	}
	corpus.notifyChange(new DefaultEdit(this, NOMEdit.ADD_CHILD, (Object)child));
    }

    /** structural validity check */
    private boolean checkValidChild(NOMElement child) throws NOMException {
	NLayer thisLayer,kl,childLayer=null;
	if (!corpus.isValidating()) { return true; }
	childLayer = child.getLayer();
	if (stream) { 
	    try {
		String codname = colour.substring(colour.lastIndexOf(".")+1, colour.length());
		NCoding ncod=meta.getCodingByName(codname);
		kl=ncod.getTopLayer();
	    } catch (Exception ex) {
		throw new NOMException("Error checking validity of child for root element of colour "  + colour);
	    }

	} else { 
	    thisLayer = this.getLayer(); 
	    kl=thisLayer.getChildLayer();
	    if (thisLayer==null || childLayer==null) {
		//  System.out.println("Element: " + name + " has no layer ");
		return false; 
	    }
	    if (thisLayer.getRecursive() && thisLayer==childLayer) {
		return true;
	    }
	}
	if (kl!=null && kl==childLayer) { return true; }
	return false;
    }

    /** adds the NOMElement as the last child */
    public void addLastChild(NOMElement child) throws NOMException {
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedChildren(this, colour);
	}
	checkEditSafe();

	/*
	if (!checkValidChild(child)) {
	    throw new NOMException("ERROR: Trying to add an invalid child element (" + child.getName()  + " " + child.getID() + ") to element '" + name + "'.");
	    //	    System.err.println("WARNING: Trying to add an invalid child element (" + child.getName()  +   ") to element '" + name + "'.");
	    //	    return;
	}
	*/

	if (children==null) { children=new ArrayList(); }
	children.add((Object)child);
	children_count++;
	updateTimes();
	if (child.getColour().equals(colour)) { 
	    ((NOMWriteElement)child).setParent((NOMElement)this); 
	    if (local_children==null) { local_children=new ArrayList(); }
	    local_children.add((Object)child);
	} else { 
	    safeAddParent((NOMWriteElement)child);	    
	}
	corpus.notifyChange(new DefaultEdit(this, NOMEdit.ADD_CHILD, (Object)child));
    }


    /** adds the NOMElement newchild immediately before the given
        existing child, or reports an exception if the child cannot be
        found */
    public void addChildBefore(NOMElement oldchild, NOMElement newchild) throws NOMException {
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedChildren(this, colour);
	}
	checkEditSafe();
	int chin=0;
	Iterator chit=children.iterator();
	while (chit.hasNext()) {
	    if (oldchild==(NOMElement)chit.next()) {
		children.add(chin, (Object)newchild);
		children_count++;
		updateTimes();
		if (newchild.getColour().equals(colour)) { 
		    ((NOMWriteElement)newchild).setParent((NOMElement)this); 
		    if (local_children==null) { local_children=new ArrayList(); }
		    local_children.add((Object)newchild);
		} else { 
		    safeAddParent((NOMWriteElement)newchild);
		}
		corpus.notifyChange(new DefaultEdit(this, NOMEdit.ADD_CHILD, (Object)newchild));
		return;
	    }
	    chin++;
	}
	throw new NOMException("addChildBefore method could not locate existing child!");
    }

    /** adds the NOMElement newchild immediately after the given
        existing child, or reports an exception if the child cannot be
        found */
    public void addChildAfter(NOMElement oldchild, NOMElement newchild) throws NOMException {
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedChildren(this, colour);
	}
	checkEditSafe();
	int chin=1;
	Iterator chit=children.iterator();
	while (chit.hasNext()) {
	    if (oldchild==(NOMElement)chit.next()) {
		children.add(chin, (Object)newchild);
		children_count++;
		updateTimes();
		if (newchild.getColour().equals(colour)) { 
		    ((NOMWriteElement)newchild).setParent((NOMElement)this); 
		    if (local_children==null) { local_children=new ArrayList(); }
		    local_children.add((Object)newchild);
		} else { 
		    safeAddParent((NOMWriteElement)newchild);
		}
		corpus.notifyChange(new DefaultEdit(this, NOMEdit.ADD_CHILD, (Object)newchild));
		return;
	    }
	    chin++;
	}
	throw new NOMException("addChildAfter method could not locate existing child!");
    }

    /** adds the NOMElement newchild in place of the given child
     * list, making the current children chilren of the newly
     * added node. */
    public void addChildAboveChildren(NOMElement newchild, List old_children) throws NOMException {
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedChildren(this, colour);
	}
	checkEditSafe();

	
	NOMElement firstchild = (NOMElement) old_children.get(0);
	addChildBefore(firstchild, newchild);
	
	// find the place to put element
	Iterator chit=old_children.iterator();
	while (chit.hasNext()) {
	    NOMElement kid = (NOMElement)chit.next();
	    removeChild(kid);
	    newchild.addChild(kid);
	}	
    }

    /** Insert an new parent node, making me and my siblings children
     * of our the new parent and making our current parent the parent
     * of the new parent */
    public void insertParent(NOMElement newparent, NOMElement oldparent) throws NOMException {
	if (!parents.contains(oldparent)) {
	    throw new NOMException("insertParent: element '" + oldparent.getID() + "' is not a parent of this element (" + id + "): cannot be removed");
	}
	List klist = new ArrayList(oldparent.getChildren());
	oldparent.addChildAboveChildren(newparent, klist);
    }

    /** Remove the parent node, making me and my siblings children of
     * our grandparent. Possibly need to check validity... */
    public void removeParentAndAdjust(NOMElement par) throws NOMException {
	NOMElement grand = par.getParentInFile();
	if (!parents.contains(par)) {
	    throw new NOMException("removeParent: element '" + par.getID() + "' is not a parent of this element (" + id + "): cannot be removed");
	}
	for (Iterator kit=par.getChildren().iterator(); kit.hasNext(); ) {
	    NOMElement k = (NOMElement)kit.next();
	    grand.addChildBefore(par, k);
	}
	grand.deleteChild(par);
    }


    /** Remove a nite:parent from an element without actually deleting
     *  the element referred to. */
    private void removeParent(NOMElement par)  {
	parents.remove(par);
    }

    /** Remove a nite:child from an element without actually deleting
     *  the element referred to. Throw an error if we're trying to
     *  remove the child from its serialized parent.
     */
    public void removeChild(NOMElement child) throws NOMException {
	checkEditSafe();
	if (child==null) { return; }
	if (!children.contains(child)) {
	    return;
	    //throw new NOMException("removeChild: element '" + child.getID() + "' is not a child of this element (" + id + "): cannot be removed");
	}
	if (child.getColour().equals(colour)) {
	    local_children.remove(child);
	    //	    throw new NOMException("Cannot remove a child with the same colour as parent! You need to call deleteChild for that!");
	}
	children.remove(child);
	((NOMWriteElement)child).removeParent(this);
	children_count--;
	updateTimes();
	corpus.notifyChange((NOMEdit)new DefaultEdit(this, NOMEdit.REMOVE_CHILD, (Object)child));
    }

    /** removes a child element from the element without checking
        whether the removal causes the corpus to become invalid */
    private void removeChildNoCheck(NOMElement child) {
	if (child.getColour()!=null && child.getColour().equals(colour)) { 
	    if (local_children!=null) {
		local_children.remove(child); 
	    }
	}
	children.remove(child);
	((NOMWriteElement)child).removeParent(this);
	children_count--;
	updateTimes();
	try { corpus.notifyChange((NOMEdit)new DefaultEdit(this, NOMEdit.REMOVE_CHILD, (Object)child));	} 
	catch (NOMException nex) { }
    }

    /** Delete an element from the corpus completely. Removes the
     * child from all its parents and deletes all pointers to this
     * element. */
    public void deleteChild(NOMElement child) throws NOMException {
	checkEditSafe();
	if (child.getPointers()!=null) {
	    Iterator poit = child.getPointers().iterator();
	    while (poit.hasNext()) {
		NOMPointer point=(NOMPointer)poit.next();
		corpus.removePointerIndex(point);
	    }
	}
	if (child.getChildren()!=null) {
	    Iterator kit = child.getChildren().iterator();
	    while (kit.hasNext()) {
		NOMWriteElement kid=(NOMWriteElement)kit.next();
		kid.removeParent(child);
	    }
	}
	ArrayList pars = new ArrayList(child.getParents());
	Iterator pit = pars.iterator();
	while (pit.hasNext()) {
	    NOMWriteElement par=(NOMWriteElement)pit.next();
	    par.removeChildNoCheck(child);
	    par.updateTimes();
	}
	// this is the bit where pointers to the deleted element get removed 
	corpus.notifyChange((NOMEdit)new DefaultEdit(child.getParentInFile(), NOMEdit.DELETE_ELEMENT, (Object)child));
    }

    /** adds a child to this element. Since no order is specified, add
        the child to the end of the list of children */
    public void addChild(NOMElement child) throws NOMException {
	addLastChild(child);
    }

    /** This is only called when a nite child link is resolved and the
	resulting element is added into the array in the appropriate
	place. Should not be used except when loading a corpus. */
    protected void addChildOrder(NOMElement child, int order) {
	if (children==null) { children=new ArrayList(); }
	if (children.size()<order) { order=children.size(); }

	children.add(order, (Object)child);
	if (child.getColour().equals(colour)) { 
	    ((NOMWriteElement)child).setParent((NOMElement)this); 
	    if (local_children==null) { local_children=new ArrayList(); }
	    local_children.add((Object)child);
	} else { 
	    try {
		safeAddParent((NOMWriteElement)child);
	    } catch (NOMException nex) { }
	}
	//	corpus.addChangedColour(colour); ??
    }

    /** adds an XML comment storing the position and preceding element */
    protected void addComment(NOMElement comment) throws NOMException {
	if (comments==null) { comments=new ArrayList(); }
	NOMElement prev=null;
	int position=0;
	if (children!=null) { 
	    prev=(NOMElement)children.get(children.size()-1);
	    position=children.size();
	}
	comments.add(new XMLComment(comment, prev, null, position));
    }

    /** Sets the parent of this element which is of the same colour as
        this element. An element can only have one parent of the same
        colour as itself. This method is not usually called from an
        application program, since adding a child automatically adds
        parents. */
    private void setParent(NOMElement parent) {
	real_parent=(NOMWriteElement)parent;
	if (parents==null) { parents=new ArrayList(); }	
	if (!parents.contains((Object)parent)) {
	    parents.add((Object)parent);
	}
	//	corpus.addChangedColour(colour);
    }

    /** adds a parent of this element. Elements may have any number of
        parents, though only one can have the same "colour" as the
        child. This method is not usually called from an
        application program, since adding a child automatically adds
        parents. */
    private void addParent(NOMElement parent) {
	if (parents==null) { parents=new ArrayList(); }	
	if (!parents.contains((Object)parent)) {
	    parents.add((Object)parent);
	    //	    corpus.addChangedColour(colour);
	}
    }


    /*---------------------------------------------*/
    /* Adding to corpus without refrence to parent */
    /*---------------------------------------------*/    

    /** add an element to the corpus with no information about
        location. This should only be used with elements that are in
        the top level of a coding. If the element is in a timed layer
        and has start and end times, the placement of the addition in
        the "stream" is derived from those. Otherwise the element is
        added at the end of the stream. */
    public void addToCorpus() throws NOMException {
	if (nel==null) {
	    throw new NOMException("Cannot add element '" + name + "' to corpus without metadata information");
	}
	checkEditSafe();

	NOMElement nel=corpus.getElementByID(this.getColour() + "#" + this.getID());
	if (nel!=null && nel.getParentInFile()!=null) {
	    //	    System.err.println("Element " + this.getID() + " already exists in the corpus");	    
	    return;
	}

	if (this instanceof NOMWriteTypeElement) {
	    NOMWriteElement root = (NOMWriteElement) corpus.getRootWithColour(this.colour, this.resource);
	    if (root==null) {
		root = ((NOMWriteCorpus)corpus).createRootIfValid(this.colour);
		if (root==null) {
		    throw new NOMException("Element to be added ('" + name + "') has no colour ('" + colour + "') or else no root exists with this colour!");
		}
	    }
	    root.addLastChild((NOMElement)this);
	    // throw new NOMException("Error During element addition - element '" + name + "' is an Ontology element which cannot be added programatically!");
	} else if (this instanceof NOMWriteObject) {
	    NOMWriteElement root = (NOMWriteElement) corpus.getRootWithColour(this.colour, this.resource);
	    if (root==null) {
		root = ((NOMWriteCorpus)corpus).createRootIfValid(this.colour);
		if (root==null) {		
		    throw new NOMException("Element to be added ('" + name + "') has no colour ('" + colour + "') or else no root exists with this colour!");
		}
	    }
	    root.addLastChild((NOMElement)this);
	} else {
	    NiteLayer nl = (NiteLayer) nel.getLayer();
	    if (!(nl.isTopLayerInCoding())) {
		throw new NOMException("Cannot add element '" + name + "' to corpus without parent information: it does not appear in the top layer of a coding");	    
	    }
	    NOMWriteElement root = (NOMWriteElement) corpus.getRootWithColour(this.colour, this.resource);
	    //System.out.println("Add to corpus root: " + resource);
	    if (root==null) {
		root = ((NOMWriteCorpus)corpus).createRootIfValid(this.colour);
		if (root==null) {		
		    throw new NOMException("Element to be added ('" + name + "') has no colour ('" + colour + "') or else no root exists with this colour!");
		}
	    }
	    
	    if (this.type==NLayer.TIMED_LAYER && this.start!=NOMElement.UNTIMED && 
		this.end!=NOMElement.UNTIMED) {
		int korder=0;
		List chList = root.getChildren();
		if (chList != null && chList.iterator()!=null) {
		    for (Iterator kit=chList.iterator(); kit.hasNext(); ) {
			NOMElement kid = (NOMElement)kit.next();
			if (kid.getStartTime() > this.start) { break; }
			korder++;
		    }
		}
		root.addChildOrder(this, korder);
	    } else {
		root.addLastChild((NOMElement)this);
	    }
	}
    }

    /** add the element as a sibling of the given element. Of course
        the siblings must be in the same layer and the ordering must
        not violate any of the precedence constraints */
    public void addBeforeSibling(NOMElement sibling) throws NOMException {
	checkEditSafe();
	if (sibling==null) {
	    throw new NOMException("Sibling passed to 'addBeforeSibling' is null");
	}
	if (sibling.isStreamElement()) {
	    throw new NOMException("Sibling passed to 'addBeforeSibling' is a root element: invalid call");
	}
	NOMWriteElement par = (NOMWriteElement)sibling.getParentInFile();
	if (par==null) {
	    throw new NOMException("Sibling passed to 'addBeforeSibling' (" + sibling.getID() + ") has no file parent element: either use addToCorpus, or explicitly add it as a child of an element before using this call");
	}
	par.addChildBefore(sibling, this);
    }

    /** add the element as a sibling of the given element. Of course
        the siblings must be in the same layer and the ordering must
        not violate any of the precedence constraints */
    public void addAfterSibling(NOMElement sibling) throws NOMException {
	checkEditSafe();
	if (sibling==null) {
	    throw new NOMException("Sibling passed to 'addAfterSibling' is null");
	}
	if (sibling.isStreamElement()) {
	    throw new NOMException("Sibling passed to 'addAfterSibling' is a root element: invalid call");
	}
	NOMWriteElement par = (NOMWriteElement)sibling.getParentInFile();
	if (par==null) {
	    throw new NOMException("Sibling passed to 'addAfterSibling' (" + sibling.getID() + ") has no file parent element: either use addToCorpus, or explicitly add it as a child of an element before using this call");
	}
	par.addChildAfter(sibling, this);
    }


    /*-----------------------*/
    /* Simple access methods */
    /*-----------------------*/

    /** Simply return the Corpus that this element is a part of */
    public NOMCorpus getCorpus() {
	return corpus;
    }

    /** returns the name of the element */
    public String getName() {
	return name;
    }

    /** returns the ID of the element */
    public String getID() {
	return id;
    }

    /** returns the Graphical Visual Markup string for the element */
    public String getGVM() {
	return gvm;
    }

    /** returns the keystroke associated with this element (as a string) */
    public String getKeyStroke() {
	return key;
    }

    /** returns the contents of the reserved comment String (or null if not set) */
    public String getComment() {
	return comment;
    }

    /** returns the name of the agent responsible for the
        element. Note that this is either the value of the attribute
        defined under "reserved-attributes" in the metadata, or (more
        commonly) it's derived from the colour of the element (so you
        don't need to explicitly name the agent for anything that's in
        an agent-coding) */
    public String getAgentName() {
	if (who==null) { meta_agent=getAgent(); }
	return who;
    }

    /** Returns the agent responsible for the element (or null if
        it's not in an agent coding). */
    public NAgent getAgent() {
	if (meta_agent==null) {
	    if (this instanceof NOMAnnotation) {
		NElement nell = meta.getElementByName(name);
		if (nell==null) { return null; }
		NLayer elay = nell.getLayer();
		if (elay!=null) {
		    NCoding ncod=(NCoding)elay.getContainer();
		    if (ncod!=null && ncod.getType()==NCoding.AGENT_CODING) {
			for (Iterator ait=meta.getAgents().iterator(); ait.hasNext(); ) {
			    NAgent ag=(NAgent)ait.next();
			    if (this.colour.indexOf("." + ag.getShortName() + ".") >0) {
				meta_agent=ag;
				if (who==null) { who=ag.getShortName(); }
				if (meta.getAgentAttributeName()!=null) {
				    try {
				    setStringAttribute(meta.getAgentAttributeName(), who);
				    } catch (NOMException nex) { }
				} 
			    } 
			}
		    } 
		}  
	    } 
	}
	return meta_agent;
    }

    /** returns the "colour" of the element: we use 'colour' in an
        NXT-specific way: it's precisely the filename the element will
        be serailized into, without its the '.xml' extension: thus it
        comprises observation name; '.'; the agent name followed by
        '.' (if an agent coding); the coding name.
    */
    public String getColour() {
	return colour;
    }

    /** returns the NResource of the element: this is null unless a
     * resource file is used for this corpus, in which case it can
     * affect the location of the file on disk etc.
     */
    public NResource getResource() {
	return resource;
    }

    /** sets the NResource for this element - use with caution as this
     * will change the serialization of this element. */
    public void setResource(NResource resource) {
	this.resource=resource;
    }

    /** returns the name of the observation to which the element belongs */
    public String getObservation() {
	if (observation==null) {
	    if (colour!=null && colour.indexOf(".")>0) {
		observation = colour.substring(0,colour.indexOf("."));
	    }
	}
	return observation;
    }

    /** returns a List of XMLComments */
    protected List getComments() {
	return comments;
    }

    /** returns a List of NOMElements: the children of this element
     * (not including comment elements)  */
    public List getChildren() {
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedChildren(this, colour);
	}
	return children;
    }

    /** This version of getChildren includes any comments that are
     * present in the correct order within the child stream. NOTE:
     * Order of comments is not always respected with respect to
     * out-of-file links, but will always be maintained for in-file
     * children. */
    public List getChildrenWithInterleavedComments() {
	// we must make sure that the child layer is loaded because if
	// it's out-of-file and unloaded, the child links will not
	// have been resolved, and you could lose children.
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedChildren(this, colour);
	}
	if (children==null && comments==null) { return null; }
	if (children!=null && comments==null) { return children; }

	List retlist=new ArrayList();
	int kidnum=0;
	for (Iterator comit=comments.iterator(); comit.hasNext(); ) {
	    NOMWriteElement.XMLComment com = (NOMWriteElement.XMLComment)comit.next();
	    NOMElement next = com.getNextElement();
	    NOMElement prev = com.getPreviousElement();
	    int pos = com.getPosition();
	    if (children!=null && next!=null && children.contains(next)) {
		if (children.indexOf(next)>kidnum+1) {
		    for (int i=kidnum; i<children.size(); i++) {
			NOMWriteElement kid = (NOMWriteElement)children.get(i);
			if (kid.getID().equals(next.getID())) {
			    break;
			}
			kidnum++;
			retlist.add(kid);
		    }
		}
		retlist.add(com.getCommentElement());
	    } else if (children!=null && prev!=null && children.contains(prev)) {
		if (children.indexOf(prev)>=kidnum) {
		    for (int i=kidnum; i<children.size(); i++) {
			NOMWriteElement kid = (NOMWriteElement)children.get(i);
			kidnum++;
			retlist.add(kid);
			if (kid.getID().equals(prev.getID())) {
			    break;
			}
		    }
		}
		retlist.add(com.getCommentElement());
	    } else if (children!=null && pos>kidnum) {
		for (int i=kidnum; i<children.size(); i++) {
		    if (i>=pos) { break; }
		    NOMWriteElement kid = (NOMWriteElement)children.get(i);
		    kidnum++;
		    retlist.add(kid);
		}
		retlist.add(com.getCommentElement());		
	    } else {
		retlist.add(com.getCommentElement());		
	    }
	}

	// tidy up any spare children
	if (children!=null && kidnum<children.size()-1) {
	    for (int i=kidnum; i<children.size(); i++) {
		NOMWriteElement kid = (NOMWriteElement)children.get(i);
		retlist.add(kid);
	    }	    
	}
	return retlist;
	
    }

    /** returns a List of children children with the same colour
        (NOMElements) */
    private List getLocalChildren() {
	return local_children;
    }

    /** returns true if this element is a leaf (according to the metadata) */
    public boolean isLeaf() {
	return is_leaf;
    }

    /** returns true if this element is a comment */
    public boolean isComment() {
	return is_comment;
    }

    /** returns true if this elements has had its pointers resolved */
    protected boolean pointersResolved() {
	return pointers_resolved;
    }

    /** set the value of pointers_resolved (should only used by the corpus class) */
    protected void setPointersResolved(boolean val) {
	pointers_resolved=val;
    }

    /*-----------------------*/
    /* Simple edit methods */
    /*-----------------------*/

    /** sets the name of the element - not commonly used! */
    public void setName(String name) {
	this.name=name;
    }

    /** sets the ID of the element - only used internally! */
    protected void setID(String id) {
	//	System.out.println("Set ID to " + id + " from " + this.id);
	this.id=id;
    }

    /** sets the GVM string for this element */
    public void setGVM(String name) {
	this.gvm=name;
    }

    /** sets the keystroke string associated with this element */
    public void setKeyStroke(String name) {
	this.key=name;
	try {
	    corpus.notifyChange(new DefaultEdit(this, NOMEdit.SET_ATTRIBUTE, meta.getKeyStrokeAttributeName()));
	} catch (NOMException nex) { }
    }

    /** sets the comment attribute for this element */
    public void setComment(String comment) {
	this.comment=comment;
	try {
	    corpus.notifyChange(new DefaultEdit(this, NOMEdit.SET_ATTRIBUTE, meta.getCommentAttributeName()));
	} catch (NOMException nex) { }
    }

    /** sets the textual content of an element. */
    public void setText(String chars) throws NOMException {
	checkEditSafe();
	if (children==null) {
	    text_content=chars;
	    //	    System.out.println("Added character content to element " + name + " id: " + id + ": '" + chars + "'");
	} else {
	    //	    System.err.println("NOM ERROR: Trying to add text content to an element with children. \nMixed content is not allowed in the NOM");
	}
	corpus.notifyChange(new DefaultEdit(this, NOMEdit.SET_TEXT, (Object)chars));
    }

    /** appends to the textual content of an element. */
    public void appendText(String chars) throws NOMException {
	checkEditSafe();
	if (text_content==null) { text_content=""; } 
	//	else { text_content += "\n"; }
	text_content+=chars;
	corpus.notifyChange(new DefaultEdit(this, NOMEdit.SET_TEXT, (Object)text_content));
    }

    /** gets the textual content of an element. */
    public String getText() {
	return text_content;
    }

    /** returns a List of NOMElements */
    public List getParents() {
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedParentLayers(this);
	}
	return parents;
    }

    /* Return the first ancestor we find with the given name or null
       if there are none */
    public NOMElement findAncestorNamed(String name) {
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedAncestorLayers(this, name);	    
	}
	return findAncestorNamed(this, name);
    }

    /* Return the List of NOMElements that are ancestors of this one in
     * the same file (may include stream element, and may return
     * null) */
    public List findAncestorsInFile() {
	if (real_parent==null) { return null; }
	List ret = new ArrayList();
	ret.add(real_parent);
	List parlist = real_parent.findAncestorsInFile();
	if (parlist!=null) { ret.addAll(parlist); }
	return ret;
    }

    /* Return the first ancestor we find in the given NLayer or null
       if there are none */
    public NOMElement findAncestorInLayer(NLayer layer) {
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedAncestorLayers(this, name);	    
	}
	if (layer==null || layer.getContentElements()==null) { return null; }
	return findAncestorNamed(this, getNames(layer.getContentElements()));
    }


    /* Return the first ancestor we find with the given name or null
       if there are none */
    private NOMElement findAncestorNamed(NOMElement nel, String name) {
	if (nel.getParents()==null) { return null; }
	for (Iterator pit=new ArrayList(nel.getParents()).iterator(); pit.hasNext(); ) {
	    NOMElement par = (NOMElement)pit.next();
	    if (par.getName().equals(name)) { return par; }
	    NOMElement anc=findAncestorNamed(par, name);
	    if (anc!=null) { return anc; }
	}
	return null;
    }

    /* Return the first ancestor we find within the given names list or null
       if there are none */
    private NOMElement findAncestorNamed(NOMElement nel, List names) {
	if (nel.getParents()==null) { return null; }
	for (Iterator pit=new ArrayList(nel.getParents()).iterator(); pit.hasNext(); ) {
	    NOMElement par = (NOMElement)pit.next();
	    if (names.contains(par.getName())) { return par;  }
	    NOMElement anc=findAncestorNamed(par, names);
	    if (anc!=null) { return anc; }
	}
	return null;
    }

    /* Return the set of ancestors we find with the given name or the
     * empty set if none exist */
    private Set findAncestorsNamed(NOMElement nel, String name) {
	Set ret = new HashSet();
	if (nel.getParents()==null) { return ret; }
	for (Iterator pit=new ArrayList(nel.getParents()).iterator(); pit.hasNext(); ) {
	    NOMElement par = (NOMElement)pit.next();
	    if (par.getName().equals(name)) { ret.add(par); }
	    Set r2 = findAncestorsNamed(par, name);
	    ret.addAll(r2);
	}
	return ret;
    }

    /* Return the set of ancestors we find with the given name or the
     * empty set if none exist */
    private Set findAncestorsNamed(NOMElement nel, List names) {
	Set ret = new HashSet();
	if (nel.getParents()==null) { return ret; }
	for (Iterator pit=new ArrayList(nel.getParents()).iterator(); pit.hasNext(); ) {
	    NOMElement par = (NOMElement)pit.next();
	    if (names.contains(par.getName())) { ret.add(par); }
	    Set r2 = findAncestorsNamed(par, names);
	    ret.addAll(r2);
	}
	return ret;
    }

    /* Return all ancestors of the given name or the empty Set if
     * there are none */
    public Set findAncestorsNamed(String name) {
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedAncestorLayers(this, name);	    
	}
	return findAncestorsNamed(this, name);
    }

    /* Return the nearest NOMElement in the same file that is an
     * ancestor of this element and of the given NOMElement: returns
     * null if there is no such element, and may return the stream
     * element. */
    public NOMElement findCommonAncestorInFile(NOMElement other) {
	if (other==null) { return null; }
	List ancs = findAncestorsInFile();
	List ancs2 = other.findAncestorsInFile();
	if (ancs==null || ancs2==null) { return null; }
	for (Iterator anit=ancs.iterator(); anit.hasNext(); ) {
	    NOMElement anc = (NOMElement) anit.next();
	    if (ancs2.contains(anc)) { return anc; }
	}
	return null;
    }

    /** return a list of names from a list of NElements */
    private List getNames(List els) {
	List names = new ArrayList();
	for (Iterator elit=els.iterator(); elit.hasNext(); ) {
	    names.add(((NElement)elit.next()).getName());
	}
	return names;
    }

    /* Return all ancestors in the given NLayer or the empty Set if
     * there are none */
    public Set findAncestorsInLayer(NLayer layer) {
	if (corpus.isLazyLoading()) {
	    ((NOMWriteCorpus)corpus).loadRequestedAncestorLayers(this, name);	    
	}
	if (layer==null || layer.getContentElements()==null) { return new HashSet(); }
	return findAncestorsNamed(this, getNames(layer.getContentElements()));	
    }

    /* Return all descendants in the given layer or the empty List if
     * there are none */
    private List findDescendantsInLayer(NOMElement p, NLayer layer) {
	List rset = new ArrayList();
	try {
	    if (p.isStreamElement()==false && p.getLayer()==layer) {
		rset.add(p);
	    } 
	    List kids=p.getChildren();
	    if (kids==null) { return rset; }
	    for (Iterator kit=kids.iterator(); kit.hasNext(); ) {
		NOMElement k = (NOMElement)kit.next();
		rset.addAll(findDescendantsInLayer(k,layer));
	    }
	} catch (Exception ex) {
	    return rset;
	}
	return rset;
    }

    /* Return all descendants with the given name or the empty List if
     * there are none */
    private List findDescendantsNamed(NOMElement p, String name) {
	List rset = new ArrayList();
	try {
	    if (p.getName().equalsIgnoreCase(name)) {
		rset.add(p);
	    } 
	    List kids=p.getChildren();
	    if (kids==null) { return rset; }
	    for (Iterator kit=kids.iterator(); kit.hasNext(); ) {
		NOMElement k = (NOMElement)kit.next();
		rset.addAll(findDescendantsNamed(k,name));
	    }
	} catch (Exception ex) {
	    return rset;
	}
	return rset;
    }


    /* Return all descendants in the given NLayer or the empty List if
     * there are none */
    public List findDescendantsInLayer(NLayer layer) {
	if (layer==null || layer.getContentElements()==null) { return new ArrayList(); }
	return findDescendantsInLayer(this, layer);	
    }


    /* Return all descendants with the given name or the empty List if
     * there are none */
    public List findDescendantsNamed(String name) {
	return findDescendantsNamed(this, name);	
    }


    /** returns the NOMElement belonging to the same colour (i.e. that
     * will be serialized into the same file). */
    public NOMElement getParentInFile() {
	return (NOMElement) real_parent;
    }

    /** returns a List of NOMAttributes */
    public List getAttributes() {
	return attributes;
    }

    /** returns the named attribute if it exists */
    public NOMAttribute getAttribute(String attribute_name) {
	if (attributes != null) {
	    Iterator ai=attributes.iterator();
	    while (ai.hasNext()) {
		NOMAttribute nat = (NOMAttribute)ai.next();
		if (nat.getName().equals(attribute_name)) {
		    return nat;
		}
	    }
	}
	return null;
    }

    /** add an attribute to the element */
    public void addAttribute(NOMAttribute attribute) throws NOMException {
	checkEditSafe();
	if (attribute==null) { return; }
	if (attributes==null) { attributes=new ArrayList(); }
	if (getAttribute(attribute.getName())!=null) {
	    NOMAttribute existing_att = getAttribute(attribute.getName());
	    try {
		existing_att.setComparableValue(attribute.getComparableValue());
	    } catch (NOMException ex) {
	    }
	} else {
	    attributes.add((Object)attribute);
	    ((NOMWriteAttribute)attribute).setElement(this);
	}
	corpus.notifyChange(new DefaultEdit(this, NOMEdit.SET_ATTRIBUTE, (Object)attribute.getName()));
    }

    /** returns a List of nite_children - these should not be of
        interest once the NOM is fully loaded. */
    public List getNiteChildren() {
	return nite_children;
    }

    /** Only used when building a corpus - this is used when the
        nite:child element has not yet been resolved - it's a
        placeholder for one or more real children */
    protected void addNiteChild(String nite_child_href) {
	if (nite_children==null) { nite_children=new ArrayList(); }
	nite_children.add((Object) new NiteChild(nite_child_href, children_count));
	children_count++;
	//	corpus.addChangedColour(colour);
    }

    /** Only used when building a corpus - remove nite children once
        they have been resolved */
    protected void removeNiteChildren() {
	nite_children=null;
    }
 
    /** specifies whether the children are temporally ordered,
     * implicitly ordered or unordered by returning one of UNORDERED,
     * IMPLICIT_ORDER or TEMPORAL_ORDER */
    public int getChildOrder() {
	return order;
    }

    /** returns true if this is a "stream" element - i.e. one which is
        purely there as a container for other elements for the
        purposes of serialization. */
    public Boolean isStreamElementOld() {
	return new Boolean(stream);
    }

    /** returns true if this is a "stream" element - i.e. one which is
        purely there as a container for other elements for the
        purposes of serialization. */
    public boolean isStreamElement() {
	return stream;
    }

    /** Set the "stream"ness of the element - a stream element is one
        which is purely there as a container for other elements for
        the purposes of serialization. */
    public void setStreamElement(boolean stream) {
	this.stream=stream;
	if (this.stream && (!name.equals(meta.getStreamElementName()))) {
	    Debug.print("WARNING: Stream element \"" + name + "\" does not have the declared NITE stream element name \"" + meta.getStreamElementName() + "\".", Debug.WARNING);
	    // this.name=meta.getStreamElementName();
	}
    }

    /** returns true if there is an element following this one in the
        corpus if it were to be serialized */
    public boolean hasNextElement() {
	if (local_children!=null) {
	    return true;
	} else {
	    if (real_parent==null) { return false; }
	    List kids = real_parent.getLocalChildren();
	    int ind = kids.indexOf(this);
	    if (kids.size()>ind+1) {
		return true;
	    } else {
		return real_parent.hasNextElementDoneKids();
	    }
	}
    }

    /** returns the element following this one in the corpus if it
	were to be serialized */
    public NOMElement getNextElement() {
	if (local_children!=null) {
	    return (NOMElement)local_children.get(0);
	} else {
	    if (real_parent==null) { return null; }
	    List kids = real_parent.getLocalChildren();
	    int ind = kids.indexOf(this);
	    if (kids.size()>ind+1) {
		return (NOMElement)kids.get(ind+1);
	    } else {
		return real_parent.getNextElementDoneKids();
	    }
	}
    }

    private boolean hasNextElementDoneKids() {
	if (real_parent==null) { return false; }
	List kids = real_parent.getLocalChildren();
	int ind = kids.indexOf(this);
	if (kids.size()>ind+1) {
	    return true;
	} else {
	    return real_parent.hasNextElementDoneKids();
	}
    }

    private NOMElement getNextElementDoneKids() {
	if (real_parent==null) { return null; }
	List kids = real_parent.getLocalChildren();
	int ind = kids.indexOf(this);
	if (kids.size()>ind+1) {
	    return (NOMElement)kids.get(ind+1);
	} else {
	    return real_parent.getNextElementDoneKids();
	}
    }


    /*----------------------------*/
    /* Get, set and inherit times */
    /*----------------------------*/

    /** Get the start time of this element. No distinction is made
        here between real and inherited times. If this is not a timed
        element or has not been assigned a start time, return
        NOMElement.UNTIMED. */
    public double getStartTime() {
	if (stream) return start;
	// if we have a time in 'start', it's correct as we propagate all changes.
	// jonathan 2.12.05
	if (!Double.isNaN(start)) { return start; }
	try {
	    NLayer nl = this.getLayer();
	    if (nl==null) { return start; }
	    if (Double.isNaN(start) && corpus.isLazyLoading() && nl!=null && nl.inheritsTime()) {
		((NOMWriteCorpus)corpus).loadRequestedChildTimeLayers(this);
	    }
	} catch (NOMException nex) { }
	return start;
    }

    /** Get the end time of this element. No distinction is made here
        between real and inherited times. If this is not a timed
        element or has not been assigned an end time, return
        NOMElement.UNTIMED. */
    public double getEndTime() {
	if (stream) return end;
	// if we have a time in 'end', it's correct as we propagate all changes.
	// jonathan 2.12.05
	if (!Double.isNaN(end)) { return end; }
	try {
	    NLayer nl = this.getLayer();
	    if (nl==null) { return end; }
	    if (Double.isNaN(end) && corpus.isLazyLoading() && nl.inheritsTime()) {
		((NOMWriteCorpus)corpus).loadRequestedChildTimeLayers(this);
	    }
	} catch (NOMException nex) { }
	return end;
    }

    /** Set the start time of this element. Only elements in a timed
        layer can have their start and end times set directly; all
        structural times are strictly inherited. */
    public void setStartTime(double st) throws NOMException {
	checkEditSafe();
	if (corpus.isValidating() && this.type != NLayer.TIMED_LAYER) {
	    throw new NOMException("It's illegal to set the start time of an element in a non time-aligned layer. Element: " + name + " ("  + id + "). This type=" + type + " not equal to " + NLayer.TIMED_LAYER);
	} else {
	    if (corpus.isValidating() && this.end < st) {
		throw new NOMException("It's illegal to set the start time of an element to be greater than the end time. Turn off validation if you want to force this to be permitted. Element: " + name + " ("  + id + "). ");
	    } else {
		this.start=st;
	    }
	    corpus.notifyChange(new DefaultEdit(this, NOMEdit.SET_START_TIME, null));
	    updateTimesOfParents();
	}
    }

    /** Set the end time of this element. Only elements in a timed
        layer can have their start and end times set directly; all
        structural times are strictly inherited. */
    public void setEndTime(double et) throws NOMException {
	checkEditSafe();
	if (corpus.isValidating() && this.type != NLayer.TIMED_LAYER) {
	    throw new NOMException("It's illegal to set the end time of an element in a non time-aligned layer. Element: " + name + " ("  + id + ").");
	} else {
	    if (corpus.isValidating() && this.start > et) {
		throw new NOMException("It's illegal to set the end time of an element to be before its start time. Turn off validation if you want to force this to be permitted. Element: " + name + " ("  + id + ")." );		
	    } else {
		this.end=et;
	    }
	    corpus.notifyChange(new DefaultEdit(this, NOMEdit.SET_END_TIME, null));
	    updateTimesOfParents();
	}
    }


    /** returns the integer TIME_ALIGNED, STRUCTURAL or FEATURAL: the
     * type of the element as far as timing is concerned. */
    public int getTimeType() {
	return type;
    }

    /** Inherit start and end times from timed child nodes. */
    public void inheritTimes() {
	List kids=children;
	int kidcount=0;
	NOMWriteElement last=null;

	if (is_comment) { return; }
	if (type!=NLayer.STRUCTURAL_LAYER) { return; }
	if (kids!=null) {
	    Iterator kit = kids.iterator();
	    NOMWriteElement kida=null;
	    while (kit.hasNext()) {
		Object kid = kit.next();
		if (kid instanceof NOMWriteElement) {
		    kida = (NOMWriteElement) kid;
		    if (kida.isComment()) { kida=last; continue; }
		    if (kidcount==0) {
		    //		    System.out.println("Inherit start from: " + kida.getName() + kida.getID());
		    // recursive layers cause problems!!
			if (kida.getStartTime()==NOMElement.UNTIMED) {
			    kida.inheritTimes();
			}
			this.start=kida.getStartTime();
			kidcount++;
		    }
		}
		last=kida;
	    }
	    if (kida != null) {
		// recursive layers cause problems!!
		if (kida.getEndTime()==NOMElement.UNTIMED) {
		    kida.inheritTimes();
		}
		this.end=kida.getEndTime();
		//		System.out.println("Inherit end time from: " + kida.getName() + kida.getID());
	    }
	}
    }

    protected void updateTimesOfParents() {
	if (parents==null) { return; } 
	Iterator pit = parents.iterator();
	while (pit.hasNext()) {
	    NOMWriteElement par=(NOMWriteElement)pit.next();
	    if (par!=this) {
		par.updateTimes();
	    }
	}
    }

    /** Called when a change to the start or end time of a timed unit
        by the user requires propagation */
    public void updateTimes() {
	if (corpus.getBatchMode()) { return; }
	List kids=children;
	int kidcount=0;
	NOMWriteElement last=null;

	if (is_comment) { return; }
	if (type!=NLayer.STRUCTURAL_LAYER) { return; }
	if (nel==null|| nel.getLayer()==null || nel.getLayer().inheritsTime()==false) { return; }
	if (kids!=null && kids.size()>0) {
	    Iterator kit = kids.iterator();
	    NOMWriteElement kida=null;
	    while (kit.hasNext()) {
		Object kid = kit.next();
		if (kid instanceof NOMWriteElement) {
		    kida = (NOMWriteElement) kid;
		    //if (kida.isComment()) { kida=last; continue; }
		    if (kidcount==0) {
			if (start != kida.getStartTime()) {
			    this.start=kida.getStartTime();
			    try { corpus.notifyChange(new DefaultEdit(this, NOMEdit.SET_START_TIME, null)); }
			    catch (NOMException nex) { }
			    updateTimesOfParents();
			}
			kidcount++;
		    }
		}
		last=kida;
	    }
	    if (kida != null) {
		if (end != kida.getEndTime()) {
		    this.end=kida.getEndTime();
		    try { corpus.notifyChange(new DefaultEdit(this, NOMEdit.SET_END_TIME, null)); }
		    catch (NOMException nex) { }
		    updateTimesOfParents();
		}
	    }
	}
    }

    /*---------------------------*/
    /* Private utility functions */
    /*---------------------------*/
    
    /** This version of processAttributes relies on the element having
     * been located in the metadata. We get the type of the attributes
     * from there 
     */
    private ArrayList processAttributes(Attributes attrs, NElement nel) {
	ArrayList retlist=new ArrayList();
	for (int i=0; i<attrs.getLength(); i++) {
	    String qname=attrs.getQName(i);
	    if (!qname.equals(meta.getIDAttributeName()) &&
		!qname.equals(meta.getStartTimeAttributeName()) &&
		!qname.equals(meta.getEndTimeAttributeName()) &&
		!qname.equals(meta.getGVMAttributeName()) &&
		!qname.equals(meta.getKeyStrokeAttributeName()) &&
		!qname.equals(meta.getCommentAttributeName()) &&
		!qname.equals(meta.getAgentAttributeName())) {
		NAttribute nat=nel.getAttributeByName(qname);
		int atype=NAttribute.STRING_ATTRIBUTE;
		Double dval=null;
		String sval=null;
		if (nat==null) {
		    Debug.print("WARNING: Element '" + nel.getName() +
				       "' has attribute '" + qname +
				       "' not listed in metadata! " +
				       " Defaulting to string type.", Debug.WARNING);
		    atype=NAttribute.STRING_ATTRIBUTE;
		    sval=attrs.getValue(i);
		} else if (nat.getType()==NAttribute.NUMBER_ATTRIBUTE) {
		    atype=nat.getType();
		    try {
			dval=Double.valueOf(attrs.getValue(i));
		    } catch (Exception exc) {
			Debug.print("WARNING: Element '" + nel.getName()+
					   "' attribute '" + qname +
					   "' value is not numerical. " +
					   "Setting to string! ", Debug.WARNING);
			atype=NAttribute.STRING_ATTRIBUTE;
			dval=null;
			sval=attrs.getValue(i);
		    }
		} else {
		    atype=NAttribute.STRING_ATTRIBUTE;
		    sval=attrs.getValue(i);
		}
		NOMAttribute nattr = corpus.getMaker().make_attribute(atype, qname, sval, dval );
		nattr.setElement(this);
		retlist.add((Object)nattr);
	    }
	}
	return retlist;
    }

    /** This version of processAttributes assumes all attributes are strings 
     */
    private ArrayList processAttributes(Attributes attrs) {
	ArrayList retlist=new ArrayList();
	for (int i=0; i<attrs.getLength(); i++) {
	    String qname=attrs.getQName(i);
	    if (!qname.equals(meta.getIDAttributeName()) &&
		!qname.equals(meta.getStartTimeAttributeName()) &&
		!qname.equals(meta.getEndTimeAttributeName()) &&
		!qname.equals(meta.getGVMAttributeName()) &&
		!qname.equals(meta.getKeyStrokeAttributeName()) &&
		!qname.equals(meta.getCommentAttributeName()) &&
		!qname.equals(meta.getAgentAttributeName())) {
		int atype=NAttribute.STRING_ATTRIBUTE;
		Double dval=null;
		String sval=attrs.getValue(i);
		NOMAttribute nattr = corpus.getMaker().make_attribute(atype, qname, sval, dval );
		nattr.setElement(this);
		retlist.add((Object)nattr);
	    }
	}
	return retlist;
    }

    private String replaceChars(String in, String old, String news) {
	if (in==null) { return null; }
	if (in.lastIndexOf(old)>-1) {
	    int ind=-1;
	    while ((ind=in.indexOf(old))>-1) {
		String star="";	if (ind!=0) { star=in.substring(0,ind); }
		in = star + news + in.substring(ind+1,in.length());
	    }
	}
	return in;
    }
    
    private String single_escape(String in) {
	return "'" + replaceChars(in,"'","&apos;") + "'";
    }

    private String escape(String in) {
	return XMLutils.escapeAttributeValue(in);
    }

    private String escape(double in) {
	if (in==NOMElement.UNTIMED || (new Double(in)).isNaN() ) { return "\"\""; }
	return '"' + Double.toString(in) + '"';
    }

    private String escape(Double in) {
	if (in==null || in.isNaN()) { return "\"\""; }
	return '"' + in.toString() + '"';
    }

    /*---------------*/
    /* Serialization */
    /*---------------*/

    /** Returns the metadata layer to which this element belongs. Note
        that this is only non-null where the element is an instance of
        NOMWriteAnnotation (i.e. not in an object set or
        ontology). Used when adding elements and in serialization */
    public NLayer getLayer()  throws NOMException {
	if (nel==null) { nel = meta.getElementByName(name); }
	if (nel==null) {
	    NOMException nex = new NOMException("Cannot find layer of element '" + name + "' from corpus: no metadata information");
	    nex.printStackTrace();
	    throw nex;
	}
	return nel.getLayer();
    }

    /** Returns the metadata element for this element. */
    public NElement getMetadataElement()  {
	if (nel==null) { nel = meta.getElementByName(name); }
	return nel;
    }

    /** Used in serialization - return a string containing the XML for
        the start of this element including attributes. */
    public String startElementString() {
	String rstr = "<" + name + " " + meta.getIDAttributeName() + "=" +
	    escape(id);

	NElement nell = meta.getElementByName(name);
	if (nell != null && this instanceof NOMAnnotation) {
	    NLayer elay = nell.getLayer();
	    if (elay!=null) {
		// Note that we always serialize times for timed
		// layers, but we can also do so for structural layers
		// if the corpus tells us to.
		if ((elay.getLayerType() == NLayer.TIMED_LAYER) ||
		    ((elay.getLayerType() == NLayer.STRUCTURAL_LAYER) &&
		     (corpus.serializeInheritedTimes() || children==null || children.size()==0 ||
		      (new Double(((NOMElement)children.get(0)).getStartTime()).isNaN()) ) ) )	{
		    if (start!=NOMElement.UNTIMED && !(new Double(start)).isNaN()) {
			rstr += " " + meta.getStartTimeAttributeName() + "=" + escape(start);
		    }
		    if (end!=NOMElement.UNTIMED && !(new Double(end)).isNaN()) {
			rstr += " " + meta.getEndTimeAttributeName() + "=" + escape(end);
		    }
		}
	    }
	}
	if (who != null) { // who is set for all elements belonging to an agent coding.
	    //	    rstr += " " + meta.getAgentAttributeName() + "=" + escape(who);
	}
	if (gvm != null) {
	    rstr += " " + meta.getGVMAttributeName() + "=" + escape(gvm);
	}
	if (key != null) {
	    rstr += " " + meta.getKeyStrokeAttributeName() + "=" + escape(key);
	}
	if (comment != null) {
	    rstr += " " + meta.getCommentAttributeName() + "=" + escape(comment);
	}
	
	if (attributes != null) {
	    Iterator ai=attributes.iterator();
	    while (ai.hasNext()) {
		NOMAttribute nat = (NOMAttribute)ai.next();
		if (nat.getName().equals(meta.getAgentAttributeName()) ||
		    nat.getName().equals(meta.getObservationAttributeName()) ||
		    nat.getName().equals(meta.getCommentAttributeName()) ||
		    nat.getName().equals(meta.getResourceAttributeName())) { 
		    continue; 
		}
		if (nat.getType() == NOMAttribute.NOMATTR_NUMBER) {
		    rstr +=  " " + nat.getName() + "=" + escape(nat.getDoubleValue());
		} else {
		    rstr += " " + nat.getName() + "=" + escape(nat.getStringValue());
		}
	    }
	}
	if (children==null && pointers==null && 
	    text_content==null && external_pointer_href==null) { 
	    rstr += "/"; 
	}
	return  rstr + ">";
    }

    /** Used in serialization - return a string containing the XML for
        the end of this element. */
    public String endElementString() {
	if (children==null && pointers==null && text_content==null && external_pointer_href==null) {
	    return "";
	} else {
	    return "</" + name + ">";
	}
    }

    /** returns a string containing a full Link to this element
        including filename */
    public String getLink() {
	String idstr = id;
	if (meta.getLinkType()==NMetaData.XPOINTER_LINKS) { idstr=single_escape(id); }
	return this.colour + ".xml" + corpus.getLinkFileSeparator() +
	    corpus.getLinkBeforeID() + idstr + corpus.getLinkAfterID();
    }

    /** returns a string containing a Link to this element (without
        the filename) */
    public String getIDLink() {
	String idstr = id;
	if (meta.getLinkType()==NMetaData.XPOINTER_LINKS) { idstr=single_escape(id); }
	return corpus.getLinkBeforeID() + idstr + corpus.getLinkAfterID();
    }

    /** returns this element's previous sibling of the same colour */
    public NOMElement getPreviousSibling() {
	if (real_parent==null) { return null; }
	List kids = real_parent.getChildren();
	Iterator kit = kids.iterator();
	NOMElement last = null;
	while (kit.hasNext()) {
	    NOMElement current = (NOMElement)kit.next();
	    if (current==this) {
		return last;
	    }
	    last=current;
	}
	return null;
    }

    /** returns this element's following sibling of the same colour */
    public NOMElement getNextSibling() {
	if (real_parent==null) { return null; }
	List kids = real_parent.getChildren();
	Iterator kit = kids.iterator();
	while (kit.hasNext()) {
	    NOMElement current = (NOMElement)kit.next();
	    if (current==this) {
		if (kit.hasNext()) {
		    return (NOMElement)kit.next();
		} else { return null; }
	    }
	}
	return null;
    }

    /** get the actual recursive depth of this element in its
     * recursive layer where 0 means no parents in this layer; 1 means
     * one etc. */
    public int getRecursiveDepth() {
	int depth=0;
	try {
	    NLayer nl = getLayer();
	    if (!nl.getRecursive()) { return depth; }
	    NOMElement mel = real_parent;
	    while (mel!=null && !mel.isStreamElement() && 
		   mel.getLayer()!=null && mel.getLayer()==nl) {
		depth++;
		mel=mel.getParentInFile();
	    }
	} catch (NOMException nex) { }
	return depth;
    }

    /** Set the href value of the external pointer from this
     * element. Only allowed in elements oin layers of type
     * EXTERNAL_POINTER_LAYER  */
    public void addExternalPointer(String role, String value) throws NOMException {
	if (nel.getContainerType()!=NElement.CODING) {
	    throw new NOMException("Illegal setting of external pointer on element '" + name + "'. ");
	}
	NLayer elay = nel.getLayer();
	if (elay.getLayerType() != NLayer.EXTERNAL_POINTER_LAYER) {
	    throw new NOMException("Can'r set external pointer on element '" + name + "' which is not a part of an External Pointer Layer. ");
	}

	external_pointer_href=value;
    }

    /** get the actual recursive height of this element in its
     * recursive layer where 0 means no children in this layer; 1 means
     * kids to a depth of 1 etc. */
    public int getRecursiveHeight() {
	int height=0;
	try {
	    NLayer nl = getLayer();
	    if (!nl.getRecursive()) { return height; }
	    if (children==null) { return height; }
	    for (Iterator kit=children.iterator(); kit.hasNext(); ) {
		NOMElement k = (NOMElement)kit.next();
		NLayer kl = k.getLayer();
		int kdepth=0;
		if (kl==nl) {
		    kdepth = k.getRecursiveHeight();
		    if (kdepth>=height) { height=kdepth+1; }
		}
	    }
	} catch (NOMException nex) { }
	return height;
    }


    /** This class is for nite:child elements before the xlink has
        been resolved */
    protected class NiteChild {
	String xlink_href=null;
	String start_of_range;
	String end_of_range;
	int order=-1;
	boolean range=false;
	
	public NiteChild (String href, int order) {
	    xlink_href=href;
	    this.order=order;
	    if (href==null) {
		Debug.print("ERROR: NXT child or pointer with no " + corpus.getHrefAttr() + " attribute: cannot be resolved. Element ID: " + id, Debug.ERROR);
		//System.exit(0);
		return;
	    }
	    
	    int rangeindex = href.lastIndexOf(corpus.getRangeSeparator());
	    int startrange = href.lastIndexOf(corpus.getLinkFileSeparator());
	    //	    System.out.println("HREF: " + href) ;
	    if (rangeindex!=-1 && startrange!=-1) {
		if (startrange > rangeindex) { // happens when we have '..' in observation name!
		    rangeindex=-1;
		}
	    }
	    if (rangeindex!=-1) {
		range=true;
		start_of_range=href.substring(0, rangeindex);
		String file=start_of_range.substring(0, start_of_range.indexOf(corpus.getLinkFileSeparator()));
		end_of_range=file + corpus.getLinkFileSeparator() +
		    href.substring(rangeindex+2);
	    }
	}
	
	public int getOrder() {
	    return order;
	}

	public String getLink() {
	    return xlink_href;
	}

	public void setRange(boolean isrange) {
	    range=isrange;
	}

	public boolean isRange() {
	    return range;
	}

	public String getRangeStart() {
	    return start_of_range;
	}

	public String getRangeEnd() {
	    return end_of_range;
	}

	public void setRangeStart(String start) {
	    start_of_range=start;
	}

	public void setRangeEnd(String end) {
	    end_of_range=end;
	}

    }


    /**
     * Returns the value of an attribute specified by his name as
     * {@link Comparable}.
     * The interface {@linkplain Comparable} is usefull to compare values. Notice
     * that in NOM an attribute value is primary a {@linkplain String} and if
     * possible interpreted as {@linkplain Double}. Because both,
     * {@linkplain String} and {@linkplain Double}, implement {@link Comparable}
     * attribute values could be tried to parse as {@linkplain Double} when
     * setting and not when getting it. This will speed up reading, like i.e.
     * needed for searching.
     * @param name the name of the attribute
     * @return the value of an attribute as {@link Comparable}
     */
    public Comparable getAttributeComparableValue(String name) {
	NOMWriteAttribute nattr = (NOMWriteAttribute) this.getAttribute(name);
	if (nattr==null) { return null; }
	return nattr.getComparableValue();
    }
    
    /**
     * Returns the xlink:href to the element this element is stored.
     * The xlink:href could be something like "file.xml#id".
     * @return the xlink:href to the element this element is stored
     */
    public String getXLink() {
	return getLink();
    }
    
    /** return a shared view of this element which simply provides
        utility functions for editing the element without thinking
        about locking and unlocking the corpus. */
    public SharedEl getShared() {
	return new SharedElement(this);
    }
    
    /** This inner class provides some utility functions for shared
        NOM users */
    public class SharedElement implements SharedEl {
	NOMWriteElement nwa;
	public SharedElement (NOMWriteElement nwa) {
	    this.nwa=nwa;
	}

	/** Set the value of a named string attribute. NOM-sharing version. */
	public void setStringAttribute(NOMView view, String name, String value) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.setStringAttribute(name, value);
		getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }
	}

	/** Set the value of a named Double attribute. NOM-sharing version. */
	public void setDoubleAttribute(NOMView view, String name, Double value) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.setDoubleAttribute(name, value);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }
	}
	
	/** Remove an attribute from the element completely. NOM-sharing
	    version. */
	public void removeAttribute(NOMView view, String name) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.removeAttribute(name);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }
	}
	
	/** add an attribute to the element. NOM-sharing version */
	public void addAttribute(NOMView view, NOMAttribute attribute) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.removeAttribute(attribute.getName());
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }
	}
	
	/** add a pointer to the element. NOM-sharing version. */
	public void addPointer(NOMView view, NOMPointer pointer) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.addPointer(pointer);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }
	}
	
	/** remove a pointer from the element. NOM-sharing version. */
	public void removePointer(NOMView view, NOMPointer pointer) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.removePointer(pointer);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}
	
	/** adds the NOMElement as the first child. NOM-sharing version. */
	public void addFirstChild(NOMView view, NOMElement child) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.addFirstChild(child);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}
	
	/** adds the NOMElement as the last child. NOM-sharing version. */
	public void addLastChild(NOMView view, NOMElement child) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.addLastChild(child);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}
	
	/** adds the NOMElement newchild immediately before the given
	    existing child, or reports an exception if the child cannot be
	    found. NOM-sharing version. */
	public void addChildBefore(NOMView view, NOMElement oldchild, NOMElement newchild) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.addChildBefore(oldchild, newchild);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}
	
	/** adds the NOMElement newchild immediately after the given
	    existing child, or reports an exception if the child cannot be
	    found. NOM-sharing version. */
	public void addChildAfter(NOMView view, NOMElement oldchild, NOMElement newchild) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.addChildAfter(oldchild, newchild);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}
	
	/** Remove a nite:child from an element without actually deleting
	 *  the element referred to. Throw an error if we're trying to
	 *  remove the child from its serialized parent. NOM-sharing version. */
	public void removeChild(NOMView view, NOMElement child) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.removeChild(child);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}
	
	/** Delete an element. Remove the child from all its
	 * parents. NOM-sharing version. */
	public void deleteChild(NOMView view, NOMElement child) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.deleteChild(child);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}
	
	/** adds a child to this element. Since no order is specified, add
	    the child to the end of the list of children. NOM-sharing version. */
	public void addChild(NOMView view, NOMElement child) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.addChild(child);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}
	
	/** adds the NOMElement newchild in place of the given child
	 * list, making the current children chilren of the newly
	 * added node. NOM-sharing version. */
	public void addChildAboveChildren(NOMView view, NOMElement newchild, List old_children) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.addChildAboveChildren(newchild, old_children);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}

	/** Remove the parent node, making me and my siblings children of
	 * our grandparent. Possibly need to check validity... */
	public void removeParentAndAdjust(NOMView view, NOMElement par) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.removeParentAndAdjust(par);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}

	/** Insert an new parent node, making me and my siblings children
	 * of our the new parent and making our current parent the parent
	 * of the new parent */
	public void insertParent(NOMView view, NOMElement newparent, NOMElement oldparent) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.insertParent(newparent, oldparent);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}

	/** sets the textual content of an element. NOM-sharing version. */
	public void setText(NOMView view, String chars) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.setText(chars);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}
	
	/** Set the start time of this element. Only elements in a timed
	    layer can have their start and end times set directly; all
	    structural times are strictly inherited. NOM-sharing version. */
	public void setStartTime(NOMView view, double st) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.setStartTime(st);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}
	
	/** Set the end time of this element. Only elements in a timed
	    layer can have their start and end times set directly; all
	    structural times are strictly inherited. NOM-sharing version. */
	public void setEndTime(NOMView view, double et) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.setEndTime(et);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    } 
	}
	
	
	/** add an element to the corpus with no information about
	    location. This should only be used with elements that are in
	    the top level of a coding. If the element is in a timed layer
	    and has start and end times, the placement of the addition in
	    the "stream" is derived from those. Otherwise the element is
	    added at the end of the stream. NOM-sharing version*/
	public void addToCorpus(NOMView view) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.addToCorpus();
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }
	}
	
	/** add the element as a sibling of the given element. Of course
	    the siblings must be in the same layer and the ordering must
	    not violate any of the precedence constraints. NOM-sharing
	    version. */
	public void addBeforeSibling(NOMView view, NOMElement sibling) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.addBeforeSibling(sibling);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }
	}
	
	/** add the element as a sibling of the given element. Of course
	    the siblings must be in the same layer and the ordering must
	    not violate any of the precedence constraints. NOM-sharing
	    version. */
	public void addAfterSibling(NOMView view, NOMElement sibling) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.addAfterSibling(sibling);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }
	}

	/** Set the href value of the external pointer from this
	 * element. Only allowed in elements on layers of type
	 * EXTERNAL_POINTER_LAYER. NOM-sharing version  */
	public void addExternalPointer(NOMView view, String role, String value) throws NOMException {
	    if (nwa.getCorpus().lock(view)) {
		nwa.addExternalPointer(role, value);
		nwa.getCorpus().unlock(view);
	    } else {
		throw new NOMException("Corpus is already locked - cannot edit!");
	    }	    
	}

    }

    public class XMLComment {
	NOMElement comment;
	NOMElement previous;
	NOMElement nextel;
	int position;
	
	public XMLComment(NOMElement comment, NOMElement before, NOMElement after,
			  int position) {
	    this.comment=comment;
	    this.previous=before;
	    this.nextel=after;
	    this.position=position;
	}

	public NOMElement getCommentElement() {
	    return comment;
	}

	public NOMElement getPreviousElement() {
	    return previous;
	}

	public NOMElement getNextElement() {
	    return nextel;
	}
	
	public int getPosition() {
	    return position;
	}

	public void setAfterElement(NOMElement after) {
	    this.nextel=after;
	}
    }

} 
