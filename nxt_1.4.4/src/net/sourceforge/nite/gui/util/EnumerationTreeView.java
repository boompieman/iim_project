package net.sourceforge.nite.gui.util;
import java.util.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Component;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.nxt.*;


/*
 * A tree view with contents filled from an enumerated attribute
 *
 * @author Jonathan Kilgour, based on OntologyTreeView by Dennis Reidsma, UTwente
 */
public class EnumerationTreeView extends JTree {

    public static final int SHOWKEYS_OFF = 0;
    public static final int SHOWKEYS_LABEL = 1;
    public static final int SHOWKEYS_TOOLTIP = 2;
    
    private int showKeys;
    private HashMap keyMap;
    private String elementName;
    private String attributeName;
    private ValueColourMap colourMap = null;
    private DefaultMutableTreeNode tree;
    
    private EnumerationTreeView(NOMCorpus nom, String elementName, String attributeName,
            HashMap keyMap, int showKeys) {
        super();
        ToolTipManager.sharedInstance().registerComponent(this);
        this.keyMap = keyMap;
        this.showKeys = showKeys;
        this.attributeName = attributeName;
        this.elementName = elementName;
        this.colourMap = colourMap;
        
	
        tree = new DefaultMutableTreeNode("values");
        setModel(new DefaultTreeModel(tree));
        setCellRenderer(new EnumerationTreeCellRenderer());

	NElement el = nom.getMetaData().getElementByName(elementName);
	if (el==null) { return; }
	NAttribute at = el.getAttributeByName(attributeName);
	if (at==null || at.getType()!=NAttribute.ENUMERATED_ATTRIBUTE) { return; }
	for (Iterator vit=at.getEnumeratedValues().iterator(); vit.hasNext(); ) {
	    DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode((String)vit.next());
	    tree.add(newTreeNode);
	}

    }
    
    /**
     * <p>Sets the colour map that will be used to colour the leaf labels.
     * The colour map should map the enumerated attribute values (Strings)
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
        if (obj instanceof String) {
	    return (String)obj;
        }
	return obj.toString();
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
    public static EnumerationTreeView getEnumerationTreeView(NOMCorpus nom, 
                              final String elementName, 
                              final String attributeName) {
        return new EnumerationTreeView(nom,elementName,attributeName,null,SHOWKEYS_OFF);
    }

    /**
     * <p>Creates a new enumeration tree view. Keystrokes will be shown depending
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
     * which should map enumeration elements (instances of NOMElement) to the
     * key strokes as they should be displayed (instances of String).</p>
     */
    public static EnumerationTreeView getEnumerationTreeView(NOMCorpus nom, 
                              final String elementName, 
			      final String attributeName,
			      HashMap keyMap, int showKeys) {
        return new EnumerationTreeView(nom,elementName,attributeName,keyMap,showKeys);
    }


    private class EnumerationTreeCellRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree,
                Object value, boolean sel, boolean expanded, boolean leaf,
                int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
	    String val = (String)node.getUserObject();
            if (node.isLeaf()) {
                if (colourMap != null)
                    setTextNonSelectionColor(colourMap.getValueTextColour(val));
                else
                    setTextNonSelectionColor(ValueColourMap.getColour(val));
            } else {
                setTextNonSelectionColor(Color.black);
            }
            String text = null;
            if ((showKeys == SHOWKEYS_TOOLTIP) && (keyMap != null)) {
                String key = (String)keyMap.get(val);
                if (key != null)
                    text = key + " - " + val;
            }
            setToolTipText(text);
            return super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
        }
    }

}
