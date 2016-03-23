/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;

import javax.swing.JComponent;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import javax.swing.border.Border;

import net.sourceforge.nite.gui.actions.NiteAction;
import net.sourceforge.nite.gui.actions.InputComponent;
import net.sourceforge.nite.gui.actions.OutputComponent;
import net.sourceforge.nite.gui.actions.RightMouseListener;
import net.sourceforge.nite.gui.textviewer.ObjectModelComparator;
import net.sourceforge.nite.nxt.ObjectModelElement;

/**
 * @author judyr
 *
 * 
 */
public class ListHandler
    extends JComponentHandler
    implements InputComponent, OutputComponent {
    /**
     * The data which will be displayed on this list
     * */
    DefaultListModel model = new DefaultListModel();
    /**
     * The list itself
     * */
    JList list = new JList(model);

    /**
     * 
     * Maps items in the list ot ObjectModelElements
     */
    Map componentToData = new HashMap();

    /**
     * 
     *Maps object model elements to List items (actually to their component handlers)
     */
    Map dataToComponent = new HashMap();

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
        assignID();
        assignSourceID();
        component = new JScrollPane(list);
        Border etched = BorderFactory.createEtchedBorder();

        list.setBorder(etched);
        list.setCellRenderer(new ButtonCellRenderer());
        list.getKeyListeners();

    }
    /**
     * Only Labels can be displayed on lists. All other component handlers will cause an exception
     * */
    public void addChild(NDisplayObjectHandler child) {
        if (child instanceof LabelHandler) {
            LabelHandler lh = (LabelHandler) child;
            model.addElement(lh);
            indexElement(lh);
            children.add(lh);
        } else if (child instanceof ButtonHandler) {
            ButtonHandler bh = (ButtonHandler) child;
            //this is a hack around the fact that labels can't set the bground colour. Use buttons with invisible borders
             ((JButton) bh.getJComponent()).setBorderPainted(false);

            ((DefaultListModel) list.getModel()).addElement(bh);

            indexElement(bh);

            children.add(bh);
        } else if (child instanceof NActionReferenceHandler) {

            addActionReference((NActionReferenceHandler) child);

        } else
            throw new IllegalArgumentException("Attempted to add child of wrong type to List");

    }

    /**
     * Store the object model element associated with this object in the componentToData
     * @param The component handler to index by
     */
    private void indexElement(JComponentHandler j) {
        //if the item in the list knows about its object model element, index by that
        if (j.getElement() != null) {
            componentToData.put(j, j.getElement());
            dataToComponent.put(j.getElement(), j);

        }
        //otherwise, index by the element stored with this list
        else if (getElement() != null) {
            componentToData.put(j, getElement());
            dataToComponent.put(getElement(), j);

        }

    }
    /**
     * It is necessary to define a ListCellRenderer to display text and icon entries in the list. 
     * This is very easy to do as the only legal entries in the List are labels and buttons
     */
    class ButtonCellRenderer implements ListCellRenderer {

        // This is the only method defined by ListCellRenderer.
        // We just return the label which is stored in the listmodel

        public Component getListCellRendererComponent(JList list, Object value,
        // value to display
        int index, // cell index
        boolean isSelected, // is the cell selected
        boolean cellHasFocus) // the list and the cell have the focus
        {
            JComponentHandler handler = (JComponentHandler) value;
            JComponent comp = handler.getJComponent();
            if (isSelected){
                 if (comp instanceof JButton )  {
                         ((JButton) comp).setBackground(Color.green); 
            return comp;
                 }else if (comp instanceof JLabel){
                         ((JLabel) comp).setForeground(Color.red);
                         return comp;
                 }
            } return comp;
            
           
            /**if (handler instanceof OutputComponent) {
                //display this data element
                return ((OutputComponent) handler).displayElement(
                    handler.getElement(),
                    isSelected);

            }**/

            
        
    }
    }

    /**
     * Register an action with the list.
     * @param key specifies which key binding or mouse click will cause actions to
     * be performed on this component
     * @see net.sourceforge.nite.nstyle.handler.JComponentHandler#registerAction(String, NiteAction)
     */
    public void registerAction(String key, NiteAction a) {
        if (key != null) {
            if (key.equals("right_mouse")) {
                list.addMouseListener(new RightMouseListener(a));
            }
        }
    }
    /**
     * @see net.sourceforge.nite.gui.actions.InputComponent#getInputData()
     * 
     */

    public Set getSelectedObjectModelElements() {
        //get the handler corresponding to the currently selected item of this list
        Set set = new TreeSet(new ObjectModelComparator());
        Object[] objects = list.getSelectedValues();
        for (int i = 0; i < objects.length; i++) {

            JComponentHandler sel = (JComponentHandler) objects[i];

            //fix me - need to wrap the jdom element as an object model element when you do setElement in
            //NDisplayObject
            ObjectModelElement ome =
                (ObjectModelElement) componentToData.get(sel);
            set.add(ome);
        }
        return set;

    }

    /**
     * Returns the list.
     * @return JList
     */
    public JList getList() {
        return list;
    }
    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#displayElement(net.sourceforge.nite.nxt.ObjectModelElement, boolean)
     */
    public JComponent displayElement(ObjectModelElement e, boolean selected) {

        return list;
    }
    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#redisplayElement(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void redisplayElement(ObjectModelElement e) {
        //Find this list element which displays this objectmodelelement
        JComponentHandler c = (JComponentHandler) dataToComponent.get(e);
        //redraw it
        c.getJComponent().invalidate();
        c.getJComponent().repaint();

    }
    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#removeDisplayComponent(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void removeDisplayComponent(ObjectModelElement e) {
        //find the list element which displays this object model element
        JComponentHandler c = (JComponentHandler) dataToComponent.get(e);
        ((DefaultListModel) list.getModel()).removeElement(c);

    }
    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#insertDisplayElement(net.sourceforge.nite.nxt.ObjectModelElement, net.sourceforge.nite.nxt.ObjectModelElement, int)
     */
    public void insertDisplayElement(
        ObjectModelElement newElement,
        ObjectModelElement parent,
        int position) {
        //The position parameter usually indicates the position in a children list of the parent that the new element should be inserted
        // In this case, as the list is a flat structure, we will simply insert after the parent
        //find which component we are inserting it at
        JComponent c = (JComponent) dataToComponent.get(parent);
        int pos = ((DefaultListModel) list.getModel()).indexOf(c);
        //create a new component for the new element and index it
        LabelHandler newComp = new LabelHandler();
        newComp.createPeer();
        newComp.setElement(newElement);
        ((DefaultListModel) list.getModel()).insertElementAt(newComp, pos);

    }

}
