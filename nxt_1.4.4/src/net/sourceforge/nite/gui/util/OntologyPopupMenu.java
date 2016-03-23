package net.sourceforge.nite.gui.util;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.nxt.*;


/**
 * A popupmenu with contents filled from an ontology in a NXT corpus.
 * <p>
 * main features:
 * <ul>
 * <li>Popupmenu hierarchy reflects ontology tree.
 * <li>User can set node which serves as root -- all descendants below that node are included in the popup menu.
 * <li>User can select whether to display or hide the 'root node' in the popup menu. If hide, menu starts with first-level-children
 * <li>User can choose what action will be fired when a menu item is selected
 * <li>User can choose whether intermediate nodes (non-leafs) will also fire the action.
 * <li>PopupMenuItems conform to a NOMElementContainer interface, which means that the corresponding NOMElement (a node
 * in the ontology) can be found from the PopupMenuItem and therefore indirectly from the ActionEvent. Given the <code>ActionEvent 
 * ae</code>, you can find the NOMElement from the ontology which was selected in the menu by calling
 * <code>((NOMElementContainer)ae.getSource()).getElement()</code>.
 * </ul>
 * <p>
 * @author Dennis Reidsma, UTwente
 */
public class OntologyPopupMenu extends JPopupMenu {

/* [DR:] maybe we should rewrite this as a static factory class. Makes it e.g. possible to get the menuITEM(s) 
 * for the popup instead of the menu itself, meaning you can combine several (sub) ontologies in one menu....
 */
 
    /**
     * @param nom the corpus in which the ontology resides
     * @param displayAttribute the name of an attribute of the elements in the ontology which is used
     * for the display text of corresponding elements in the popup menu
     * @param actionCallback the ActionListener that is called when an item in the popupmenu is clicked.
     * @param rootID the element ID of the top-node in the menu, of which all descendants should be added (it's a NOM ID, so it's prefixed with the colour, e.g. "daa#mrda_11a")
     * @param addLeavesForNodes if true, the popup menu will contain leaf-elements for each non-leaf-node. This makes it 
     * possible to select intermediate nodes from the ontology hierarchy instead of only leaves.
     * @param includeRoot if true, the popupmenu will have the rootElement as its first-level menu item
     */
    public OntologyPopupMenu( NOMCorpus nom, 
                              //String ontologyName, 
                              String displayAttribute, 
                              ActionListener actionCallback,
                              String rootID,
                              boolean addLeavesForNodes,
                              boolean includeRoot) {
        NOMTypeElement root = (NOMTypeElement)nom.getElementByID(rootID);
        if (root == null) {
            System.out.println("Can't find root '" + rootID + "'");
        }
        fillPopupMenu(this, root, displayAttribute, actionCallback, addLeavesForNodes, includeRoot); 
    }
  
    private void fillPopupMenu( JComponent parent,
                                NOMTypeElement root,
                                String displayAttribute, 
                                ActionListener actionCallback,
                                boolean addLeavesForNodes,
                                boolean includeRoot) {
        //determine possible displaytext for this node or child
	if (root==null) { return; }
        String displayText = "";
        if (root.getAttribute(displayAttribute)!=null) {
            displayText = (String)root.getAttributeComparableValue(displayAttribute);
        } else {
            displayText = (String)root.getAttributeComparableValue("name");
        }

        List l = root.getChildren(); 	
    
        if ((l==null) || (l.size() == 0)) { //leaf
            //create menu item
            JMenuItem mItem = new OntologyPopupMenuLeaf(displayText,root);
            mItem.addActionListener(actionCallback);
            parent.add(mItem);
        } else { //intermediate node
            JComponent newParent = parent;
            if (includeRoot) { //create submenu node for parent element; make this the new parent for the child nodes.
                //create submenunode
                newParent = new OntologyPopupMenuNode(displayText, root);
                parent.add(newParent);
                //check if leaf for this node is requested
                if (addLeavesForNodes) { 
                    ((JMenu)newParent).addActionListener(actionCallback); //this doesn't have any effect on windows :(
                    JMenuItem mItem = new OntologyPopupMenuLeaf(displayText, root); //create extra leaf
                    mItem.addActionListener(actionCallback);
                    newParent.add(mItem);       //add leaf to new submenu
                }
       	    }
       	    
       	    //add children...
            Iterator childIt = l.iterator();
            if (childIt!=null) {
                while (childIt.hasNext()){
                    fillPopupMenu(newParent, (NOMTypeElement)childIt.next(), displayAttribute, actionCallback, addLeavesForNodes, true);
                }	
            }
        }	 
    }
 
    public interface NOMElementContainer {
        public NOMElement getElement();
    }
    public class OntologyPopupMenuLeaf extends JMenuItem implements NOMElementContainer {
        NOMElement theElement;
        public OntologyPopupMenuLeaf(String name, NOMElement element) {
            super(name);
            theElement = element;
        }
        public NOMElement getElement() {
            return theElement;
        }
    }
    public class OntologyPopupMenuNode extends JMenu implements NOMElementContainer {
        NOMElement theElement;
        public OntologyPopupMenuNode(String name, NOMElement element) {
            super(name);
            theElement = element;
        }
        public NOMElement getElement() {
            return theElement;
        }
    }
            
}
