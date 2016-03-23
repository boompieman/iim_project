/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sourceforge.nite.gui.actions.NActionReference;
import net.sourceforge.nite.nstyle.NConstants;

import net.sourceforge.nite.nxt.ObjectModelElement;
import net.sourceforge.nite.time.Clock;

/**
 * 
 * 
 * @author Judy Robertson
 */
public abstract class NDisplayObjectHandler {

    /** The string content for the NDisplayObjectHandler. */
    protected String content = null;

    /** The properties for configuring this object.  */
    protected Map properties = null;

    /** The parent of this node. */
    private NDisplayObjectHandler parent = null;
    
    /** The children of this node **/
    protected List children = new ArrayList();
    
    /**
     * The underlying xml element which is corresponds to this java object
     */
    protected ObjectModelElement element;
    /**
     * A display object can have none or more associated actions
     * */
   protected List actionReferences = new ArrayList();

    /**
        * The start time of this component. An unset time has the value -999
        */
    private double startTime = 999;

    /**
    The end time of this component. An unset time has the value -999  */
    private double endTime = -999;

    /**
     * A clock for synchronising to other time handlers
     */
    private Clock niteclock;
    
    /**
     * A unique ID assciated with the component handled by this 
     * */
    private String ID;
    
    private String sourceID;
    
    private String displayAttribute;

    /**
     * Initialises this <code>NDisplayObjectHandler</code> with
     * the supplied set of properties. Initialisation typically
     * involves creating an underlying object such as a
     * {@see javax.swing.JComponent}.
     * 
     * @param content     The textual content for this object.          
     * @param properties  A set of named properties.
     */
    public void init(String content, Map properties) {
        this.content = content;
        this.properties = properties;
    if (properties != null){
    
        // Delegate to subclass.
       assignID();
       assignSourceID();
    }
        createPeer();
    }
    
    protected void assignID(){
            if (properties != null){
          
    	String id = (String) properties.get(NConstants.id);
    	setID(id);
            }
    	
    }
    
    protected void assignSourceID(){
	if (properties != null) {
            String id = (String) properties.get(NConstants.sourceID);
	    setSourceID(id);
	}
    }

    protected abstract void createPeer();

    /**
     * In an implementation-specific manner, add the supplied child
     * <code>NDisplayObjectHandler</code> to this one. 
     * 
     * @param child The child to add.
     * 
     * @throws IllegalArgumentException if the supplied child was not
     *      of the correct type.
     */
    public abstract void addChild(NDisplayObjectHandler child);

    /**
     * Sets the parent of this <code>NDisplayObjectHandler</code> to the
     * specified object.
     * 
     * @param parent The object which will become the parent of this one.
     */
    public void setParent(NDisplayObjectHandler parent) {
        this.parent = parent;
    }

    /**
     * Gets the parent of this <code>NDisplayObjectHandler</code>.
     * 
     * @return The parent <code>NDisplayObjectHandler</code>.
     */
    public NDisplayObjectHandler getParent() {
        return parent;
    }

    /**
     * Returns the endTime.
     * @return double
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * Returns the startTime.
     * @return double
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * Sets the endTime.
     * @param endTime The endTime to set
     */
    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    /**
     * Sets the startTime.
     * @param startTime The startTime to set
     */
    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the niteclock.
     * @return Clock
     */
    public Clock getClock() {
        return niteclock;
    }

    /**
     * Sets the niteclock.
     * @param niteclock The niteclock to set
     */
    public void setClock(Clock niteclock) {
        this.niteclock = niteclock;
    }

    public void setUpTimes() {
        Double endtime = null;

        String s = (String) properties.get(NConstants.nomEndTime);
        if (s != null) {
            try {
                endtime = Double.valueOf(s);
                setEndTime(endtime.doubleValue());
            } catch (NumberFormatException e) {
                System.out.println("A number format exception");
                
            }

        }
        Double starttime = null;

        String st = (String) properties.get(NConstants.nomStartTime);
        if (st != null) {
            try {
                starttime = Double.valueOf(st);
                setStartTime(starttime.doubleValue());
            } catch (NumberFormatException e) {
                System.out.println("A number format exception");
                
            }

        }
    }
    
    public void addActionReference(NActionReference n){
    	actionReferences.add(n);
    }

    public List getActionReferences(){
    	return actionReferences;
    }

    /**
     * Returns the children.
     * @return List
     */
    public List getChildren() {
        return children;
    }

    /**
     * Sets the children.
     * @param children The children to set
     */
    public void setChildren(List children) {
        this.children = children;
    }



    /**
     * Returns the iD.
     * @return String
     */
    public String getID() {
        return ID;
    }

    /**
     * Sets the iD.
     * @param iD The iD to set
     */
    public void setID(String iD) {
        ID = iD;
    }

    /**
     * Returns the element.
     * @return Element
     */
    public ObjectModelElement getElement() {
        return element;
    }

    /**
     * Sets the element.
     * @param element The element to set
     */
    public void setElement(ObjectModelElement element) {
	if (properties != null) {
	    displayAttribute  =  (String) properties.get("displayAttribute");
	}
        this.element = element;
	if (element != null) this.element.setDisplayedAttribute(displayAttribute);
    }

    /**
     * Method getSourceID.
     * @return String
     */
    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String s){
	sourceID = s;        
    }
}

