/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;


import java.awt.Component;
import java.awt.Color;


import javax.swing.*;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;


import net.sourceforge.nite.gui.actions.OutputComponent;
import net.sourceforge.nite.nstyle.handler.JComponentHandler;
/**
 * @author judyr
 *
 * Renders ObjectModel element data stored with the tree nodes onto tree cells
 */
public class NTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {

    private ImageIcon leaficon;
    private ImageIcon openicon;
    private ImageIcon closedicon;
    
    public NTreeCellRenderer() {
	super();
    }
   
    public Component getTreeCellRendererComponent(JTree tree, Object value,
						  boolean selected,
						  boolean expanded,
						  boolean leaf, int row,
						  boolean hasFocus) {
        if(value instanceof NTreeNode) {
	    NTreeNode node = (NTreeNode) value;
	    JComponentHandler handler = node.getDisplayComponentHandler();
	    if (handler != null){
		if (handler instanceof OutputComponent){
                                //display this data element
		    return  ( (OutputComponent) handler).displayElement(handler.getElement(), selected);
		}
	    } else {
		Component tcrc = super.getTreeCellRendererComponent(tree, ((NTreeNode)value).getContent(), selected, expanded, leaf, row, hasFocus);
		if (node.isHighlighted()) { 
		    tcrc.setForeground(node.getHighlightColor());
		} else {
		    tcrc.setForeground(node.getTextColor());
		}
		return tcrc;
	    }
        }
	return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
    
    public void setLeafIcon(ImageIcon i){
    	leaficon = i;
    }
    
    public void setOpenIcon(ImageIcon i){
    	openicon = i;
    }
    
    public void setClosedIcon(ImageIcon i){
    	closedicon = i;
    }
}
