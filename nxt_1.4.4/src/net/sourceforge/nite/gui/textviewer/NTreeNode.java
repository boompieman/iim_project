/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import org.jdom.Element;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Color;
import java.awt.Font;

import net.sourceforge.nite.nstyle.*;
import net.sourceforge.nite.nstyle.handler.JComponentHandler;
import net.sourceforge.nite.nxt.ObjectModelElement;


/**
 * @author judyr
 */
public class NTreeNode extends DefaultMutableTreeNode {
    private NTree parentTree=null;
    private NTreeNode parent=null;
    protected ObjectModelElement dataElement;
    public Font font = null;
    public Color textcolour = Color.black;
    public Color highcolour = Color.red;
    public boolean timehighlit = false;
    private JComponent displaycomponent;
    private JComponentHandler displayComponentHandler;
    private boolean highlighted = false;
    private double starttime =999;
    private double endtime =-999;
    private double maxEndTime = -999;
    private double minStartTime = 999;
    private String dirpath = "Data\\Images\\";
    private String content;

    /**
     * Create a simple tree node with no content.
     */
    public NTreeNode() {
        super();
    }

    /** Create a simple NTreeNode with the given string as its text */
    public NTreeNode(String s) {
        content = s;
    }
    
    /**
     * Constructor for NTreeNode.
     * @param userObject
     * @param allowsChildren
     */
    public NTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    /* EDITS */

    /**
     * Add the specified node as a child of this node, thus expanding the tree
     * Update indices of the parent NTree as necessary.
     * */
    public void addNode(NTreeNode n){
	n.setParentNTreeNode(this);
	updateTreeIndices(NTree.ADD, n);
        add(n);
    }

    /**
     * Remove the specified child node from this one and update indices of
     * the parent NTree as necessary. */
    public void remove(NTreeNode child) {
	updateTreeIndices(NTree.REMOVE, child);
	super.remove(child);
	child=null;
    }

    /** if this node is connected to an NTree, update the indices */
    private void updateTreeIndices(int type, NTreeNode c) {
	if (parentTree!=null) {
	    parentTree.updateIndices(type, c);
	} else {
	    if (parent!=null) { parent.updateTreeIndices(type,c); }
	}
    }
    
    /** set the parent this node */
    protected void setParentNTreeNode(NTreeNode par){
        parent = par;   
    }

    /** get the parent this node */
    protected NTreeNode getParentNTreeNode(){
        return parent;   
    }

    /** set the parent NTree for this node (normally only set for the
     * root) */
    protected void setParentNTree(NTree pare){
        parentTree = pare;   
    }

    /** get the parent NTree for this node */
    protected NTree getParentNTree(){
        return parentTree;   
    }

    /** set the JComponent that displays this node */
    public void setComponent(JComponent com){
        displaycomponent = com;   
    }

    /** return the text content of the JDOM Element */
    private String getText(Element e) {
        String temp = "";
        String type = e.getAttributeValue(NConstants.objectType);
	temp = e.getTextTrim();
        return temp;
    }

    /** return the currently displayed text for this node */
    public String toString() {
        return content;
    }

    /** return the start time of the given JDOM element (not required by user programs) */
    protected Double getStartTime(Element obj) {
        Double starttime = null;

        String s = obj.getAttributeValue(NConstants.nomStartTime);
        if (s == null) {
            return null;
        }

        try {
            starttime = Double.valueOf(s);
        } catch (NumberFormatException e) {
            System.out.println("A number format exception");
            return null;
        }

        return starttime;
    }

    /** return the end time of the given JDOM element (not required by user programs) */
    public Double getEndTime(Element obj) {
        Double endtime = null;

        String s = obj.getAttributeValue(NConstants.nomEndTime);
        if (s == null) {
            return null;
        }

        try {
            endtime = Double.valueOf(s);
        } catch (NumberFormatException e) {
            System.out.println("A number format exception");
            return null;
        }

        return endtime;
    }


    /**
     * Sets up colour and font
     * @param The label which is to be decorated
     * @param The jdom element specifying display properties
     * @return the label with updated display properties
     */
    private JLabel setDisplayProperties(JLabel comp, Element e){
    	ImageIcon icon = null;
	Font font = null;
	String imagepath = e.getAttributeValue(NConstants.ImagePath);
	if (imagepath != null) {
	    icon = new ImageIcon(dirpath + imagepath);
	}
	if (e.getAttributeValue(NConstants.foregroundColour) != null) {
	    textcolour =
		NConstants.getColour(e.getAttributeValue(
					 NConstants.foregroundColour));
	    System.out.println(comp.getText() + " " + 
		       e.getAttributeValue(NConstants.foregroundColour));
	}
	
	String name = "Arial";
	int size = 12;
	int style = Font.PLAIN;
	if (e.getAttributeValue(NConstants.fontSize)
	    != null) {
	    size = Integer.parseInt(e.getAttributeValue(NConstants.fontSize));
	}
	if (e.getAttributeValue(NConstants.font) != null) {
	    name =e.getAttributeValue(NConstants.font);
	    
	}
	String fontstyle = e.getAttributeValue(NConstants.fontStyle);
	if (fontstyle != null) {
	    if (fontstyle
		.equalsIgnoreCase(NConstants.bold)) {
		style = Font.BOLD;
	    } else if (fontstyle.equalsIgnoreCase(NConstants.italic)) {
		style = Font.ITALIC;
	    }
	}
	font = new Font(name, style, size);
	if (font != null) comp.setFont(font);
	if (textcolour != null) comp.setForeground(textcolour);
	if (icon != null) comp.setIcon(icon);
    	
    	return comp;
    }
   

    /**
     * Returns the endtime.
     * @return double
     */
    public double getEndtime() {
        return endtime;
    }

    /**
     * Returns the starttime.
     * @return double
     */
    public double getStarttime() {
        return starttime;
    }

    /**
     * Sets the endtime.
     * @param endtime The endtime to set
     */
    public void setEndtime(double endtime) {
        this.endtime = endtime;
    }

    /**
     * Sets the starttime.
     * @param starttime The starttime to set
     */
    public void setStarttime(double starttime) {
        this.starttime = starttime;
    }

    
    /**
     * Returns the textual content of this node.
     * @return String
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the text content of this node.
     * @param content The content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the displaycomponent.
     * @return JComponent
     */
    public JComponent getDisplaycomponent() {
        return displaycomponent;
    }

    /**
     * Sets the displaycomponent.
     * @param displaycomponent The displaycomponent to set
     */
    public void setDisplaycomponent(JComponent displaycomponent) {
        this.displaycomponent = displaycomponent;
    }


    /**
     * Sets the highlighted-ness of the node.
     */
    public void setHighlighted(boolean highed) {
        this.highlighted = highed;
    }

    /**
     * Sets the highlighted-ness of the node.
     */
    public void setHighlighted(boolean highed, Color col) {
	this.highcolour=col;
        this.highlighted = highed;
    }

    /**
     * Returns the highlighted-ness of the node.
     */
    public boolean isHighlighted() {
	return this.highlighted;
    }

    /**
     * Returns true if the node is timed and false otherwise.
     */
    public boolean isTimed() {
        return starttime != 999 && endtime != -999;
    }

    /**
     * Returns the dataElement.
     * @return ObjectModelElement
     */
    public ObjectModelElement getDataElement() {
        return dataElement;
    }

    /**
     * Sets the dataElement.
     * @param dataElement The dataElement to set
     */
    public void setDataElement(ObjectModelElement dataElement) {
        this.dataElement = dataElement;
    }

    /**
     * Returns the displayComponentHandler.
     * @return JComponentHandler
     */
    public JComponentHandler getDisplayComponentHandler() {
        return displayComponentHandler;
    }

    /**
     * Sets the displayComponentHandler.
     * @param displayComponentHandler The displayComponentHandler to set
     */
    public void setDisplayComponentHandler(JComponentHandler displayComponentHandler) {
        this.displayComponentHandler = displayComponentHandler;
    }

    /**
     * Returns the highlighting colour
     */
    public Color getHighlightColor() {
        return highcolour;
    }

    /**
     * Set the highlighting colour
     */
    public void setHighlightColor(Color col) {
        highcolour=col;
    }

    /**
     * Returns the normal text colour
     */
    public Color getTextColor() {
        return textcolour;
    }

    /**
     * Set the normal text colour
     */
    public void setTextColor(Color col) {
        textcolour=col;
    }

}
