/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import net.sourceforge.nite.gui.textviewer.NTreeNode;
import net.sourceforge.nite.nxt.ObjectModelElement;

/**
 * @author judyr
 */
public  class TreeNodeHandler extends NDisplayObjectHandler {
    private NTreeNode node = null;

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
        node = new NTreeNode( );
    	node.setContent(content);
    	setUpTimes();
    }
    
    public void setElement(ObjectModelElement e){
	super.setElement(e);
	//add a record of the data element to the tree node
	if (getElement() != null ){
	    node.setDataElement(e);
	}
    }
    
    /**
     * The children of a TreeNode can be JComponents; TimedLabels get
     * special treatment */
    public void addChild(NDisplayObjectHandler child) {
        if (child instanceof TreeNodeHandler) {
            TreeNodeHandler childNode = (TreeNodeHandler) child;
            node.addNode(childNode.getTreeNode());
            children.add(childNode);
        } else if (child instanceof TimedLabelHandler) {
            TimedLabelHandler tlh = (TimedLabelHandler) child;
            this.setStartTime(tlh.getStartTime());
            this.setEndTime(tlh.getEndTime());
            node.setStarttime(tlh.getStartTime());
            node.setEndtime(tlh.getEndTime());
	    node.setComponent(tlh.getJComponent());
	    node.setDisplayComponentHandler(tlh);
            children.add(tlh);
        } else if (child instanceof GridPanelHandler){
	    GridPanelHandler jch = (GridPanelHandler)   child;
            node.setComponent(jch.getJComponent());
            if ((jch.getStartTime() != 999) && (jch.getEndTime() != -999)){
		node.setStarttime(jch.getStartTime());
		node.setEndtime(jch.getEndTime());
            }
            node.setDisplayComponentHandler(jch);
	    children.add(jch);
        } else if (child instanceof JComponentHandler){
            JComponentHandler ch = (JComponentHandler) child;
            node.setComponent(ch.getJComponent());
            children.add(ch);
            node.setDisplayComponentHandler(ch);
            //this node will be displaying another component, so should assume the data of that element
            node.setDataElement(ch.getElement());
	} else {
            throw new IllegalArgumentException("Attempted to add child of wrong type to TreeNode");
        }
    }
    
    public NTreeNode getTreeNode() {
        return node;   
    }

}
