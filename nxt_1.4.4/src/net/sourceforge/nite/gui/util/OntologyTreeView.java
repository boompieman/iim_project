package net.sourceforge.nite.gui.util;
import java.util.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Component;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.nxt.*;


/**
CLASS UNDER DEVELOPMENT. DON'T DEPEND ON THIS CLASS!

CLASS UNDER DEVELOPMENT. DON'T DEPEND ON THIS CLASS!

CLASS UNDER DEVELOPMENT. DON'T DEPEND ON THIS CLASS!

CLASS UNDER DEVELOPMENT. DON'T DEPEND ON THIS CLASS!


how to make a general view of ontology, so we can conveniently switch tree or popup without changing behaviour...
misschien omdraaien dan: een popupmenu is iets wat je uit een ontologyTreeModel haalt... en de ontologytreemodel is de brug van nxt naar swing...


 * A tree view with contents filled from an ontology in a NXT corpus.
 * <p>
 * main features:
 * <ul>
 * <li>tree hierarchy reflects ontology tree.
 * <li>User can set node which serves as root -- all descendants below that node are included in the tree.
 * <li>User can select whether to display or hide the 'root node' in the tree. If hide, tree starts with first-level-children
 * <li>User can choose what action will be fired when a tree item is selected
 * <li>User can choose whether intermediate nodes (non-leafs) will also fire the action.
 * <li>Keystrokes can be displayed in the tree

[following text: not ponpupmenuitem but something else...]
 * <li>PopupMenuItems conform to a NOMElementContainer interface, which means that the corresponding NOMElement (a node
 * in the ontology) can be found from the PopupMenuItem and therefore indirectly from the ActionEvent. Given the <code>ActionEvent 
 * ae</code>, you can find the NOMElement from the ontology which was selected in the menu by calling
 * <code>((NOMElementContainer)ae.getSource()).getElement()</code>.
 * </ul>
 
 
 * <p>
 * @author Dennis Reidsma, UTwente
 */
public class OntologyTreeView extends JTree {

/* [DR:] maybe we should rewrite this as a static factory class. Makes it e.g. possible to get the menuITEM(s) 
 * for the popup instead of the menu itself, meaning you can combine several (sub) ontologies in one menu....
 
 this would help combining this class with the ontologypopupmenu class...
 */

    public static final int SHOWKEYS_OFF = 0;
    public static final int SHOWKEYS_LABEL = 1;
    public static final int SHOWKEYS_TOOLTIP = 2;
    
    private int showKeys;
    private HashMap keyMap;
    private String displayAttribute;
    private ValueColourMap colourMap = null;
    
    private OntologyTreeView(NOMCorpus nom, final String displayAttribute,
            String rootID, HashMap keyMap, int showKeys) {
        super();
        ToolTipManager.sharedInstance().registerComponent(this);
        this.keyMap = keyMap;
        this.showKeys = showKeys;
        this.displayAttribute = displayAttribute;
        this.colourMap = colourMap;
        
        NOMTypeElement root = (NOMTypeElement)nom.getElementByID(rootID);
        DefaultMutableTreeNode tree = new DefaultMutableTreeNode(root);
        setModel(new DefaultTreeModel(tree));
        setCellRenderer(new OntologyTreeCellRenderer());
        fillTree(tree); 
    }
    
    /**
     * <p>Sets the colour map that will be used to colour the leaf labels.
     * The colour map should map the ontology elements (instances of NOMElement)
     * to colours. If <code>map</code> is null, the tree view will use the
     * global value colour map (default).</p>
     *
     * @param map a colour map or null
     */
    public void setColourMap(ValueColourMap map) {
        this.colourMap = map;
    }
    
    private void fillTree( DefaultMutableTreeNode root) {
        //get user object
        NOMElement ne = (NOMElement)root.getUserObject();
	if (ne==null) { return; }
        List l = ne.getChildren(); 	
        if ((l==null) || (l.size() == 0)) { //leaf
            //do nothing
        } else { //intermediate node
       	    //add children...
            Iterator childIt = l.iterator();
            if (childIt!=null) {
                while (childIt.hasNext()){
                    DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode((NOMElement)childIt.next());
                    root.add(newTreeNode);
                    fillTree(newTreeNode);
                }	
            }
        }	 
    }

    public String convertValueToText(Object value, boolean selected,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Object obj = ((DefaultMutableTreeNode)value).getUserObject();
	if (obj==null) { return ""; }
        if (!(obj instanceof NOMElement)) {
            return obj.toString();
        }
        NOMElement elem = (NOMElement)obj;
        String label = (String)elem.getAttributeComparableValue(displayAttribute);
        if ((showKeys == SHOWKEYS_LABEL) && (keyMap != null)) {
            String key = (String)keyMap.get(elem);
            if (key != null)
                label = (String)key + " - " + label;
        }
        return label;
    }
    
    /**
    
    
    the object is stored in defaultmutabletreenode.getobject ofzo...
    
     * @param nom the corpus in which the ontology resides
     * @param displayAttribute the name of an attribute of the elements in the ontology which is used
     * for the display text of corresponding elements in the popup menu
     * @param actionCallback the ActionListener that is called when an item in the popupmenu is clicked.
     * @param rootID the element ID of the top-node in the menu, of which all descendants should be added (it's a NOM ID, so it's prefixed with the colour, e.g. "daa#mrda_11a")
     * @param addLeavesForNodes if true, the popup menu will contain leaf-elements for each non-leaf-node. This makes it 
     * possible to select intermediate nodes from the ontology hierarchy instead of only leaves.
     * @param includeRoot if true, the popupmenu will have the rootElement as its first-level menu item
     */
    public static OntologyTreeView getOntologyTreeView(NOMCorpus nom, 
                              final String displayAttribute, 
                              String rootID) {
        return new OntologyTreeView(nom,displayAttribute,rootID,null,SHOWKEYS_OFF);
    }

    /**
     * <p>Creates a new ontology tree view. Keystrokes will be shown depending
     * on the <code>showKeys</code> parameter, which can be one of the following
     * constants:</p>
     *
     * <p><ul>
     * <li><code>SHOWKEYS_OFF</code>: keystrokes won't be shown</li>
     * <li><code>SHOWKEYS_LABEL</code>: keystroke will be shown in the label
     * of a leaf</li>
     * <li><code>SHOWKEYS_TOOLTIP</code>: keystroke will be shown in the tooltip
     * of a leaf</li>
     * </ul></p>
     *
     * <p>The keystrokes must be defined in the <code>keyMap</code> parameter,
     * which should map ontology elements (instances of NOMElement) to the
     * key strokes as they should be displayed (instances of String).</p>
     */
    public static OntologyTreeView getOntologyTreeView(NOMCorpus nom, 
                              final String displayAttribute, 
                              String rootID, HashMap keyMap, int showKeys) {
        return new OntologyTreeView(nom,displayAttribute,rootID,keyMap,showKeys);
    }

    private class OntologyTreeCellRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree,
                Object value, boolean sel, boolean expanded, boolean leaf,
                int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            NOMElement elem = (NOMElement)node.getUserObject();

            if (node.isLeaf()) {
                if (colourMap != null)
                    setTextNonSelectionColor(colourMap.getValueTextColour(elem));
                else
                    setTextNonSelectionColor(ValueColourMap.getColour(elem));
            } else {
                setTextNonSelectionColor(Color.black);
            }
            String text = null;
            if ((showKeys == SHOWKEYS_TOOLTIP) && (keyMap != null)) {
                String key = (String)keyMap.get(elem);
                if (key != null)
                    text = key + " - " + (String)elem.getAttributeComparableValue(displayAttribute);
            }
            setToolTipText(text);
            return super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
        }
    }

}
