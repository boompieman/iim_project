package net.sourceforge.nite.gui.util;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.meta.*;

/**
 * A popupmenu with contents filled from an enumerated attribute in a NXT corpus.
 * <p>
 * main features:
 * <ul>
 * <li>User can choose what action will be fired when a menu item is selected
 * <li>Elements are associated with simple Strings and cannot nest. (see OntologyPopupMenu for nestable hierarchy of values
 * </ul>
 * <p>
 * @author Jonathan Kilgour, UEdin, based on OntologyPopupMeny by Dennis Reidsma, UTwente
 */
public class EnumeratedPopupMenu extends JPopupMenu {
 
    /**
     * @param nom the corpus in which the ontology resides
     * @param displayAttribute the name of an attribute of the elements in the ontology which is used
     * for the display text of corresponding elements in the popup menu
     * @param actionCallback the ActionListener that is called when an item in the popupmenu is clicked.
     * @param includeRoot if true, the popupmenu will have the rootElement as its first-level menu item
     */
    public EnumeratedPopupMenu( NOMCorpus nom, 
                              String elementName, 
                              String attributeName, 
                              ActionListener actionCallback) {
	NElement el = nom.getMetaData().getElementByName(elementName);
	if (el==null) { return; }
	NAttribute at = el.getAttributeByName(attributeName);
	if (at==null || at.getType()!=NAttribute.ENUMERATED_ATTRIBUTE) { return; }
	for (Iterator vit=at.getEnumeratedValues().iterator(); vit.hasNext(); ) {
            JMenuItem mItem = new JMenuItem((String)vit.next());
	    mItem.addActionListener(actionCallback);
	    this.add(mItem);
	}
    }
  
}
