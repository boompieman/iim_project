/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.text.Keymap;
import javax.swing.KeyStroke;

import net.sourceforge.nite.gui.actions.InputComponent;
import net.sourceforge.nite.gui.actions.LeftMouseListener;
import net.sourceforge.nite.gui.actions.NiteAction;
import net.sourceforge.nite.gui.actions.OutputComponent;
import net.sourceforge.nite.gui.actions.RightMouseListener;
import net.sourceforge.nite.gui.textviewer.NTextArea;
import net.sourceforge.nite.nxt.ObjectModelElement;


/**
 * @author judyr
 *
 * 
 */
public class TextAreaHandler extends JComponentHandler implements InputComponent , OutputComponent{
    /**
     * The text area which is displayed using this component handler
     * */

   public  NTextArea textArea = new NTextArea();

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
        JScrollPane sp = new JScrollPane(textArea);
        if (getClock() != null) textArea.setClock(getClock());
        component = sp;
    }

    /**
     * Only TextElements can be displayed on text areas. All other
     * component handlers will cause an exception
     * */
    public void addChild(NDisplayObjectHandler child) {
    	// if the child is a text element, it should be inserted into
    	// the TextArea's document
        if (child instanceof TextElementHandler) {
            TextElementHandler teh = (TextElementHandler) child;
            textArea.addElement(teh.getTextElement());
            children.add(teh);
        } 
        // if the child is a TextStyleHandler, the style it specifies
        // should be added to the TextArea's style context
        else if (child instanceof TextStyleHandler) {
            TextStyleHandler t = (TextStyleHandler) child;
            textArea.addStyle(t.name, t.style);
        } else if (child instanceof NActionReferenceHandler) {
	    addActionReference((NActionReferenceHandler)child);
        } else
            throw new IllegalArgumentException("Attempted to add child of wrong type to TextArea");

    }

    /* set up a keybinding / mouse click on the text area to trigger
       an action */
    public void registerAction(String key, NiteAction a){
	super.registerAction(key, a);
	// System.out.println("Registering action " + a.getId() + " to act on key '" + key + "'.");
	if (key != null) {
	    if (key.equals("right_mouse")) {
		textArea.addMouseListener(new RightMouseListener(a));
	    } else if (key.equals("left_mouse")) {
		textArea.addMouseListener(new LeftMouseListener(a));
	    } else {
		Keymap km = textArea.getKeymap();
		if (KeyStroke.getKeyStroke(key)==null) { 
		    System.err.println("Failed to add a Keymap to text component - could not generate keystroke for '" + key + "'");
		    return;
		}
		km.addActionForKeyStroke(KeyStroke.getKeyStroke(key), a);
		textArea.setKeymap(km);
		//	System.err.println("Warning: attempt to register action with key " + key + " will have no effect");
	    }
	}
    }

    public Set getSelectedObjectModelElements(){
	Set elements = textArea.getSelectedElements();
	//  System.out.println("Selected elements in set " + elements.size());
	return  elements;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#displayElement(net.sourceforge.nite.nxt.ObjectModelElement, boolean)
     */
    public JComponent displayElement(ObjectModelElement e, boolean selected) {
	return textArea;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#redisplayElement(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void redisplayElement(ObjectModelElement e) {
	textArea.redisplayElement( e);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#removeDisplayComponent(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void removeDisplayComponent(ObjectModelElement e) {
	textArea.removeDisplayComponent(e);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#insertDisplayElement(net.sourceforge.nite.nxt.ObjectModelElement, net.sourceforge.nite.nxt.ObjectModelElement, int)
     */
    public void insertDisplayElement(ObjectModelElement newElement, ObjectModelElement parent, int position) {
	textArea.insertDisplayElement(newElement, parent, position);
    }
}
   	
   

