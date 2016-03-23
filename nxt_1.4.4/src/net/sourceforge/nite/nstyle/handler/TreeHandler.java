/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.util.Set;


import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;

import net.sourceforge.nite.gui.actions.InputComponent;
import net.sourceforge.nite.gui.actions.LeftMouseListener;
import net.sourceforge.nite.gui.actions.NiteAction;
import net.sourceforge.nite.gui.actions.OutputComponent;
import net.sourceforge.nite.gui.actions.RightMouseListener;
import net.sourceforge.nite.gui.textviewer.NTree;
import net.sourceforge.nite.gui.textviewer.NTreeCellRenderer;
import net.sourceforge.nite.gui.textviewer.NTreeNode;
import net.sourceforge.nite.gui.textviewer.NiteKeyListener;
import net.sourceforge.nite.nstyle.NConstants;
import net.sourceforge.nite.nxt.ObjectModelElement;

import java.io.File;

/**
 * @author judyr
 *
 */
public class TreeHandler
    extends JComponentHandler
    implements InputComponent, OutputComponent {
    private String dirimagepath =
        "Data" + File.separator + "Images" + File.separator;

    private NTree tree = null;
    private NTreeNode root = null;

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
        String leaficon = (String) properties.get(NConstants.LeafImage);
        String openicon = (String) properties.get(NConstants.OpenImage);
        String closedicon = (String) properties.get(NConstants.ClosedImage);
        String expandedicon = (String) properties.get(NConstants.ExpandedImage);
        String collapsedicon =
            (String) properties.get(NConstants.CollapsedImage);

        if (leaficon != null)
            UIManager.put(
                "Tree.leafIcon",
                new ImageIcon(dirimagepath + leaficon));
        if (openicon != null)
            UIManager.put(
                "Tree.openIcon",
                new ImageIcon(dirimagepath + openicon));
        if (closedicon != null)
            UIManager.put(
                "Tree.closedIcon",
                new ImageIcon(dirimagepath + closedicon));

        if (expandedicon != null)
            UIManager.put(
                "Tree.expandedIcon",
                new ImageIcon(dirimagepath + expandedicon));
        if (collapsedicon != null)
            UIManager.put(
                "Tree.collapsedIcon",
                new ImageIcon(dirimagepath + collapsedicon));

        setUpToolTip();

        NTreeCellRenderer renderer = new NTreeCellRenderer();
        tree = new NTree();
        //show tool tips on this tree
        ToolTipManager.sharedInstance().registerComponent(tree);

        tree.putClientProperty("JTree.lineStyle", "None");
        tree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.setCellRenderer(renderer);

        JScrollPane scroller =
            new JScrollPane(
                tree,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	component = scroller;
    }

    public void initializeTree(NTreeNode rootnode) {
        tree.setRoot(rootnode);
        if (getClock() != null) tree.setClock(getClock());
        tree.setUpTimeMapper(rootnode);
        tree.setUpDataElements(rootnode);
    }

    /**
     * The children of a Tree must be TreeNodes. */
    public void addChild(NDisplayObjectHandler child) {
        if (child instanceof TreeNodeHandler) {
            TreeNodeHandler childNode = (TreeNodeHandler) child;
            if (root == null) {
		root = childNode.getTreeNode();
                initializeTree(root);
                children.add(childNode);
            } else {
                throw new RuntimeException("A tree may have only one root node.");
            }
        } else if (child instanceof NActionReferenceHandler) {
            addActionReference((NActionReferenceHandler) child);
        } else {
            throw new IllegalArgumentException("Attempted to add child of wrong type to Tree");
	}
    }

    public Set getSelectedObjectModelElements() {
        Set elements = tree.getSelectedElements();
        return elements;
    }

    /**
     * Find the tree nodes which display this object model element and redraw
     * them given the  data in the specified element
     * @see net.sourceforge.nite.gui.actions.OutputComponent#redisplayElement(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void redisplayElement(ObjectModelElement e) {
        tree.redisplayElement(e);
    }

    /**
     * Find the nodes which represent this element and delete them (and their
     * children) from the tree.
     * @see net.sourceforge.nite.gui.actions.OutputComponent#removeDisplayComponent(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void removeDisplayComponent(ObjectModelElement e) {
        tree.removeDisplayComponent(e);
    }

    /**
     * Find the tree nodes which represent the specified parent. Find which node
     * represents the positionth child of them and add a new tree node to
     * represent the specified node there
     * @see net.sourceforge.nite.gui.actions.OutputComponent#insertDisplayElement(net.sourceforge.nite.nxt.ObjectModelElement, net.sourceforge.nite.nxt.ObjectModelElement, int)
     */
    public void insertDisplayElement(
        ObjectModelElement newElement,
        ObjectModelElement parent,
        int position) {
        tree.insertDisplayElement(newElement, parent, position);
    }

    /**
     * @see net.sourceforge.nite.gui.actions.OutputComponent#displayElement(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public JComponent displayElement(ObjectModelElement e, boolean selected) {
        //the redisplay is actually implemented in the tree cell renderer and will be picked up when the object model elements are updated
        return tree;
    }

    /////////////////////////////////////////////////////////////////////////////////
    // FIX ME - InputMap does not appear to intercept key events which
    // means that I am going to use a keylistener instead Java
    // tutorial advises InputMaps, but they have unpredictable
    // behaviour - try testing it with the InputMapTest example

    /**
     *  This is used to register an action listener for the action.
     **/

    /* */
    public void registerAction(String binding, NiteAction a) {
        if (binding != null) {
            if (binding.equals("right_mouse")) {
                tree.addMouseListener(new RightMouseListener(a));
            } else if (binding.equals("left_mouse")) {
                tree.addMouseListener(new LeftMouseListener(a));
            } else {
		System.out.println(tree);
		NiteKeyListener listener = new NiteKeyListener(a, binding);
		tree.addKeyListener(listener);
	    }
	    /*
	      tree.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(binding), a);
	      tree.getActionMap().put(KeyStroke.getKeyStroke(binding), a);
	    */
	}
    }

}

