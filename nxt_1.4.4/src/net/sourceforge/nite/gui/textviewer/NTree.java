/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.LinkedHashSet;

import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;


import java.awt.Color;

import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nxt.ObjectModelElement;
import net.sourceforge.nite.nxt.NOMObjectModelElement;
import net.sourceforge.nite.time.Clock;
import net.sourceforge.nite.time.TimeHandler;
import net.sourceforge.nite.query.QueryResultHandler;

/**
 * A simple extension of JTree that allows a data element to be
 * associated with each tree node so that time highlighting and query
 * highlighting can be achieved.
 *
 * @author judyr, edited by jonathan
 * */
public class NTree extends JTree implements TimeHandler, QueryResultHandler {

    private Clock niteclock;
    private NTreeNode currentnode;
    private NTreeNode root;
    private Set previousSet = null;
    private Set currentSet = null;
    private Color ccolor = new Color(200, 255, 200); // colour of time highlight
    private Color qcolor = new Color(200, 100, 100); // colour of query highlight
    public static final int ADD=1;
    public static final int REMOVE=2;

    // need a mapping between the nodes and the data which they represent
    private Map nodesToData = new HashMap();

    // also need a mapping between ObjectModelElement IDs and which nodes
    // represent them on screen
    private Map dataToNodes = new HashMap();

    // these handle indexing between nodes and their timings
    private TimeIntervalMapper timemap;
    private TimeIntervalIterator currentTimeIterator = null;


    /**
     * Constructor for NTree.
     */
    public NTree() {
        super();
        timemap = new TimeIntervalMapper();
	this.getModel().addTreeModelListener(new NTree.IndexingChangeListener());
    }

    /**
     * Constructor for NTree.
     * @param root
     */
    public NTree(NTreeNode nroot) {
        super(nroot);
	setRoot(nroot);
	this.getModel().addTreeModelListener(new NTree.IndexingChangeListener());
    }

    public void setRoot(NTreeNode nroot) {
	root=nroot;
	setModel(new DefaultTreeModel(root));
	this.getModel().addTreeModelListener(new NTree.IndexingChangeListener());
        timemap = new TimeIntervalMapper();
	nodesToData = new HashMap();
	dataToNodes = new HashMap();
	visitAllNodes(root);
	visitAllNodesForData(root);
	nroot. setParentNTree(this);
    }

    // this does a full pass of the tree, creating indices
    private void indexNodeData(ObjectModelElement e, NTreeNode node) {
	if (e != null && node != null){
	    // System.err.println("Indexing " + e.getID());
	    //index the data by the node
	    Set l = (Set) nodesToData.get(node);
	    if (l == null) {
		l = new TreeSet(new ObjectModelComparator());
		l.add(e);
		nodesToData.put(node, l);
	    } else {
		l.add(e);
		nodesToData.put(node, l);
	    }

	    // !!! occasional problem during lazy loading 
	    String id="";
	    try { id = e.getID(); } catch (Exception ex) { return; }
	    
	    //index the node by the data
	    Set l1 = (Set) dataToNodes.get(id);
	    if (l1 == null) {
		l1 = new TreeSet(new NTreeNodeComparator());
		l1.add(node);
		dataToNodes.put(id, l1);
	    } else {
		l1.add(node);
		dataToNodes.put(id, l1);
	    }
	}
    }

    /** Take references to deleted nodes out of maps
     */
    private void removeNodeData(ObjectModelElement e, NTreeNode node) {
        //remove the data by the node
        Set l = (Set) nodesToData.get(node);
        //if the node has some data already, remove only the specified data
        if (l != null) {
            l.remove(e);
            nodesToData.put(node, l);
        } else {
            //but if there are no bits of data left, remove the node
            //completely from the positionToElement
            nodesToData.remove(node);
        }
	
        //remove the node by the data
        Set l1 = (Set) nodesToData.get(node);
	//if the element has some nodes already, remove only
	//the specified node
	String id=e.getID();
	if (l1 != null) {
	    l1.remove(node);
	    dataToNodes.put(id, l1);
	} else {
	    //but if there are no nodes left, remove the data
	    //element completely from the positionToElement
	    dataToNodes.remove(id);
	}
    }

    /**
     * Initialises the TimeIntervalMapper data structure which
     * maintains a mapping between display components and times.
     * */
    public void setUpTimeMapper(NTreeNode root) {
        visitAllNodes(root);
    }

    public void setUpTimeMapper() {
	if (root!=null) { visitAllNodes(root); }
    }

    public void setUpDataElements(NTreeNode root) {
        visitAllNodesForData(root);
    }

    public void setUpDataElements() {
	if (root!=null) { visitAllNodesForData(root); }
    }

    //      Traverses all nodes in the tree,setting up data mapping
    private void visitAllNodesForData(NTreeNode node) {
	ObjectModelElement dn=node.getDataElement();
	if (dn!=null) { 
	    indexNodeData(dn, node);
	}	
        if (node.getChildCount() > 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                NTreeNode n = (NTreeNode) e.nextElement();
                visitAllNodesForData(n);
            }
        }
    }

    //Traverses all nodes in the tree, transferring timing information
    //to the TimeIntervalMapper
    private void visitAllNodes(NTreeNode node) {
        //if the node has timing information, its display component
        //should be added to the TimeIntervalMapper
        if (node.isTimed()) {
            timemap.addObject(node, node.getStarttime(), node.getEndtime());
        }

        if (node.getChildCount() > 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                NTreeNode n = (NTreeNode) e.nextElement();
                visitAllNodes(n);
            }
        }
    }

    /** keep indices up to date: called when any NTreeNode within this
     * tree is edited */
    protected void updateIndices(int type, NTreeNode node) {
	if (type==ADD) {
	    visitAllNodes(node);
	    visitAllNodesForData(node);
	} else if (type==REMOVE) { // can't see a good reason we need to know the times to remove!
	    NOMObjectModelElement ob = (NOMObjectModelElement)node.getDataElement();
	    if (ob != null) {
		NOMElement el = ob.getElement();
		timemap.removeObject(node, el.getStartTime(), el.getEndTime());
		removeNodeData(node.getDataElement(), node); // possibly recurse?
	    }
	}
    }

    /**
     * Causes the display components which correspond to the specified
     * time to be highlighted This uses the TimeIntervalInterator
     * provided by TimeIntervalMapper to find all the display
     * components for the specified time. A record of the previous
     * iterator is maintained so that it is possible to turn off the
     * highlighting on components which are no longer in the time
     * frame
     * @see net.sourceforge.nite.time.TimeHandler#acceptTimeChange(double) */
    public void acceptTimeChange(double systemTime) {

        if (currentTimeIterator == null)
            currentTimeIterator = timemap.getTimeIntervalIterator();
        currentTimeIterator.setTime(systemTime);

        //retrieve the display components which are currently in time scope, and highlight them
        if (currentTimeIterator != null) {
            currentSet = currentTimeIterator.getMatchingObjects();

            Iterator it = currentSet.iterator();
            while (it.hasNext()) {
                NTreeNode node = (NTreeNode) it.next();
                JComponent display = node.getDisplaycomponent();
                //if the display component for this node is a grid pane,let the grid pane take care of displaying it
                if (display instanceof GridPanel) {
                    ((GridPanel) display).acceptTimeChange(systemTime);
                } else {
                    NTimedLabel label =
                        (NTimedLabel) node.getDisplaycomponent();
                    label.setTimeHighlit(true, ccolor);
                }

                TreePath path =
                    new TreePath(
                        ((DefaultTreeModel) getModel()).getPathToRoot(node));

		// For some reason the isVisible test only works for
		// non-leaf nodes, so for certain displays scrolling
		// falls behind. jonathan 3.11.05

                //if (!isVisible(path)) {
		//}
		scrollPathToVisible(path);
                repaint();

            }

        }

        //if the set of objects which are in time scope has changed since last clock update, 
        // find the objects which have changed and remove the highlighting using Set operations

        if (previousSet != null) {
            Set temp = new HashSet(previousSet);
            temp.removeAll(currentSet);

            Iterator it = temp.iterator();
            while (it.hasNext()) {
                NTreeNode node = (NTreeNode) it.next();

                JComponent display = node.getDisplaycomponent();
                if (!(display instanceof GridPanel)) {
                    ((NTimedLabel) display).setTimeHighlit(false);
                }

            }
        }

        // keep a record of the objects which are highlighted now so
        // that they can be unhighlighted later
        previousSet = currentSet;
    }

    public void setTimeHighlightColor(Color color) {
	ccolor=color;
    }

    /**
     * @see net.sourceforge.nite.time.TimeHandler#setTime(double)
     */
    public void setTime(double time) {
        niteclock.setSystemTime(time);
    }

    /**
     * Causes the display components which are in time scope between
     * the specified start and end times to be highlighted 
     * NOT YET IMPLEMENTED
     */
    public void acceptTimeSpanChange(double start, double end) {

    }

    /**
     * @see net.sourceforge.nite.time.TimeHandler#setTimeSpan(double, double)
     */
    public void setTimeSpan(double start, double end) {
        niteclock.setTimeSpan(start, end);
    }

    public void setClock(Clock c) {
        niteclock = c;
        niteclock.registerTimeHandler(this);
    }

    /**
     * @see net.sourceforge.nite.time.TimeHandler#getClock()
     */
    public Clock getClock() {
        return niteclock;
    }

    /** allow the user to set the selected element */
    public void setSelected(ObjectModelElement el) {
	//	setSelectionPath(((DefaultTreeModel) getModel()).getPathToRoot());
	if (el==null) { return; }
        Set nodes = (Set) dataToNodes.get(el.getID());
	if (nodes==null) { 
	    // System.out.println("No tree nodes are associated with OME " + el.getID());
	    if (dataToNodes.keySet()==null || dataToNodes.keySet().size()==0) { 
		System.err.println("No data map! ");
	    }	    
	    return;
	}
	for (Iterator it=nodes.iterator(); it.hasNext(); ) {
            NTreeNode n = (NTreeNode) it.next();
            TreePath path =
                new TreePath(((DefaultTreeModel) getModel()).getPathToRoot(n));
	    setSelectionPath(path);
            if (!isVisible(path)) {
                scrollPathToVisible(path);
            }
            repaint();
        }
    }

    public void clearHighlights() {
	for (Iterator it=nodesToData.keySet().iterator(); it.hasNext(); ) {
            NTreeNode n = (NTreeNode) it.next();
            TreePath path =
                new TreePath(((DefaultTreeModel) getModel()).getPathToRoot(n));
	    n.setHighlighted(false);
        }
	repaint();
    }

    /* we need this because ObjectModelElements the user makes are not
       the same as the ones in the keys of the mapping! This kind of
       shows it's being done in the wrong way! */
    public Set getDataNodes(ObjectModelElement el) {
 	String id=el.getID();
// 	for (Iterator dit=dataToNodes.keySet().iterator(); dit.hasNext(); ) {
// 	    ObjectModelElement ome = (ObjectModelElement)dit.next();
// 	    if (ome.getID().equals(id)) {
// 		//		System.out.println("HELLO\n\n");
// 		return (Set)dataToNodes.get(ome);
// 	    }
// 	}
// 	return null;
      return (Set)dataToNodes.get(id);
    }

    /** allow the user to add a highlight to an element */
    public void setHighlighted(ObjectModelElement el, Color c) {
	//	setSelectionPath(((DefaultTreeModel) getModel()).getPathToRoot());
	if (el==null) { return; }
	Set nodes = (Set) dataToNodes.get(el.getID());
	//        Set nodes = getDataNodes(el);
	if (nodes==null) { 
	    if (dataToNodes.keySet()==null || dataToNodes.keySet().size()==0) { 
		System.err.println("No data map! ");
	    }	    
	    return;
	}
	for (Iterator it=nodes.iterator(); it.hasNext(); ) {
            NTreeNode n = (NTreeNode) it.next();
            TreePath path =
                new TreePath(((DefaultTreeModel) getModel()).getPathToRoot(n));
	    n.setHighlighted(true, c);
            if (!isVisible(path)) {
                scrollPathToVisible(path);
            }
        }
	repaint();
	
    }

    /**
     * Method getSelectedElements.
     * @return Set
     */
    // NOTE THIS HAS BEEN CONSTRAINED SO THAT ONLY THE DIRECTLY
    // SELECTED ELEMENT IS EDITED RATHER THAN THE WHOLE TREE PATH
    public Set getSelectedElements() {
        Set set = new TreeSet(new NTreeNodeComparator());
        TreePath[] paths = getSelectionPaths();
	if (paths==null) return set;
        //iterate over all selected branches of the tree
        for (int i = 0; i < paths.length; i++) {
	    //	    System.out.println("Element selected");
            TreePath path = (TreePath) paths[i];
            Object[] pathArray = path.getPath();
            NTreeNode directSelection = (NTreeNode) path.getLastPathComponent();
	    // the nodes to data thing seems broken! Jonathan 7/4/3
	    Set s = (Set) nodesToData.get(directSelection);
	    //merge the sets
	    if (s != null) {
		set.addAll(s);
	    } 
        }
        return set;
    }


    /**
     * Method getSelectedElementsOrdered.
     * @return the list ordered in the way the connected nodes appear in the tree
     */
    // NOTE THIS HAS BEEN CONSTRAINED SO THAT ONLY THE DIRECTLY
    // SELECTED ELEMENT IS EDITED RATHER THAN THE WHOLE TREE PATH
    public List getSelectedElementsOrdered() {
        List res = new ArrayList();
        TreePath[] paths = getSelectionPaths();
	if (paths==null) return res;
        //iterate over all selected branches of the tree
        for (int i = 0; i < paths.length; i++) {
	    //	    System.out.println("Element selected");
            TreePath path = (TreePath) paths[i];
            Object[] pathArray = path.getPath();
            NTreeNode directSelection = (NTreeNode) path.getLastPathComponent();
	    // the nodes to data thing seems broken! Jonathan 7/4/3
	    Set s = (Set) nodesToData.get(directSelection);
	    //merge the sets
	    if (s != null) {
		res.addAll(s);
	    } 
        }
        return res;
    }

    public Set getSelectedTreeNodes() {
        Set set = new TreeSet(new NTreeNodeComparator());
        TreePath[] paths = getSelectionPaths();
	if (paths==null) return set;
        //iterate over all selected branches of the tree
        for (int i = 0; i < paths.length; i++) {
	    //	    System.out.println("Element selected");
            TreePath path = (TreePath) paths[i];
            Object[] pathArray = path.getPath();
            NTreeNode directSelection = (NTreeNode) path.getLastPathComponent();
	    set.add(directSelection);
        }
        return set;
    }

    public Set getSelectedTreeNodesOrdered() {
	// LinkedHashSet set = new LinkedHashSet(new NTreeNodeComparator());
        LinkedHashSet set = new LinkedHashSet();
        TreePath[] paths = getSelectionPaths();
	if (paths==null) return set;
        //iterate over all selected branches of the tree
        for (int i = 0; i < paths.length; i++) {
	    //	    System.out.println("Element selected");
            TreePath path = (TreePath) paths[i];
            Object[] pathArray = path.getPath();
            NTreeNode directSelection = (NTreeNode) path.getLastPathComponent();
	    set.add(directSelection);
        }
        return set;
    }

    /**
     * Find the tree nodes which display this object model element and redraw
     * them given the  data in the specified element
     */
    public void redisplayElement(ObjectModelElement e) {
        Set nodes = (Set) dataToNodes.get(e.getID());
        Iterator it = nodes.iterator();
        while (it.hasNext()) {

            NTreeNode n = (NTreeNode) it.next();
            n.setDataElement(e);
            //also need to update indexing . FIX ME - potential problem with duplicate entries
            indexNodeData(e, n);
            //cause the display to be updated
            TreePath path =
                new TreePath(((DefaultTreeModel) getModel()).getPathToRoot(n));
            if (!isVisible(path)) {
                scrollPathToVisible(path);
            }
            repaint();
        }

    }

    /**
     * Find the nodes which represent this element and delete them (and their
     * children) from the tree.
     */
    public void removeDisplayComponent(ObjectModelElement e) {
        Set nodes = (Set) dataToNodes.get(e.getID());
        Iterator it = nodes.iterator();
        while (it.hasNext()) {

            NTreeNode n = (NTreeNode) it.next();
            n.removeFromParent();
            //also need to update indexing . FIX ME - potential problem with duplicate entries
            removeNodeData(e, n);
            //cause the display to be updated
            TreePath path =
                new TreePath(
                    ((DefaultTreeModel) getModel()).getPathToRoot(
                        n.getParent()));
            if (!isVisible(path)) {
                scrollPathToVisible(path);
            }
            repaint();
        }

    }

    /**
     * Find the tree nodes which represent the specified parent. Find which node
     * represents the positionth child of them and add a new tree node to
     * represent the specified node there
     */
    public void insertDisplayElement(
        ObjectModelElement newElement,
        ObjectModelElement parent,
        int position) {
	Set nodes = (Set) dataToNodes.get(parent.getID());
	Iterator it = nodes.iterator();
	while (it.hasNext()) {
	    
	    NTreeNode n = (NTreeNode) it.next();
	    //now add the new element as the positionth child of this node
	    NTreeNode newNode = new NTreeNode();
	    newNode.setDataElement(newElement);
	    n.insert(newNode, position);
	    TreePath path =
		new TreePath(((DefaultTreeModel) getModel()).getPathToRoot(n));
	    if (!isVisible(path)) {
		scrollPathToVisible(path);
	    }
	    repaint();
	}
    }


    /** accept a query result as a list of NOMElements */
    public void acceptQueryResults(List results) {
	clearHighlights();
	if (results==null) { return; }
	for (Iterator rit=results.iterator(); rit.hasNext(); ) {
	    acceptQueryResultRecursive((NOMElement)rit.next());
	}
    }

    /** display query result recursively */
    private void acceptQueryResultRecursive(NOMElement result) {
	if (result==null) { return; }
	//	System.out.println("Highlight " + result.getID());
	setHighlighted(new NOMObjectModelElement(result), qcolor);
	//setSelected(new NOMObjectModelElement(result));
	if (result.getChildren()!=null) {
	    for (Iterator kit=result.getChildren().iterator(); kit.hasNext(); ) {
		acceptQueryResultRecursive((NOMElement)kit.next());
	    }
	}
    }

    /** accept a query result as an individual element */
    public void acceptQueryResult(NOMElement result) {
	if (result==null) { return; }
	clearHighlights();
	acceptQueryResultRecursive(result);
    }

    /** set the colour of the highlighting for queries */
    public void setQueryHighlightColor(Color color) {
	qcolor=color;
    }

  /** Clears up the nodes/data indices and remakes them **/
  public void reIndex() {
    // System.err.println("Reindexing");
    nodesToData.clear();
    dataToNodes.clear();
    // FIXME: really need clear the mapper rather than make a new one
    timemap = new TimeIntervalMapper();
    visitAllNodes(root);
    visitAllNodesForData(root);
  }


  class IndexingChangeListener implements TreeModelListener {
    public void IndexingChangeListener() {
    }
    /** Invoked after a node (or a set of siblings) has changed in some
     * way. Only the nodes themselves, but not their siblings have changed -- so we only need to update the corresponding**/
    public void treeNodesChanged(TreeModelEvent e) {
      reIndex();
    }
    
    /** Invoked after nodes have been inserted into the tree. **/
    public void treeNodesInserted(TreeModelEvent e) {
      reIndex();
    }
    
    /** Invoked after nodes have been removed from the tree. **/
    public void treeNodesRemoved(TreeModelEvent e) {
      reIndex();
    }
    
    /** Invoked after the tree has drastically changed structure from a
     * given node down. **/
    public void treeStructureChanged(TreeModelEvent e) {
      reIndex();
    }    
  }

    /** get the largest end time of any NTreeNode so far added
     * to this NTree. Unimplemented - returns 0.0 */
    public double getMaxTime() {
	return 0.0;
    }

}



