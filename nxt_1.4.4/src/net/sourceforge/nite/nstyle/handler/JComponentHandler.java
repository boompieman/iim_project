/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.awt.Color;
import java.awt.Font;
import java.util.StringTokenizer;

import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.KeyStroke;

import net.sourceforge.nite.gui.actions.NActionReference;
import net.sourceforge.nite.gui.actions.NiteAction;
import net.sourceforge.nite.gui.textviewer.NiteKeyListener;
import net.sourceforge.nite.nstyle.NConstants;


/**
 * @author Judy Robertson
 */
public abstract class JComponentHandler extends NDisplayObjectHandler {

    /** The peer component object. */
    protected JComponent component = null;
    /** An image to be diplayed by the peer component */
    protected ImageIcon image = null;

    protected abstract void createPeer();
   
   /**
    * This is used to register an action listener for the action;
    * well, in fact it's overridden by every class that extends this,
    * so it's not really!
    */
   public   void registerAction(String binding, NiteAction a){
       /*
        if (binding != null) {
	    if (component instanceof JTextComponent) {
		JTextComponent th = (JTextComponent)component;
		Keymap km = th.getKeymap();
		if (KeyStroke.getKeyStroke(binding)==null) { 
		    System.err.println("Failed to add a Keymap to text component - could not generate keystroke for '" + binding + "'");
		    return;
		}
		km.addActionForKeyStroke(KeyStroke.getKeyStroke(binding), a);
		th.setKeymap(km);
	    } else {
		System.out.println(component);
		NiteKeyListener listener = new NiteKeyListener(a, binding);
		component.addKeyListener(listener);
	    }
	}
       */
	/*
       InputMap im = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
       im.put(KeyStroke.getKeyStroke(binding),a);
       component.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, im);

       ActionMap am = component.getActionMap();
       am.put(KeyStroke.getKeyStroke(binding), a);
       component.setActionMap(am);

       System.out.println("Component " + component);
       KeyStroke[] keys = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).allKeys();
       for (int i= 0; i < keys.length; i ++){
	   KeyStroke key = (KeyStroke) keys[i];
	   System.out.println(key);
       }
       */
       if (binding != null){
	   if (! binding.equals("right_mouse") ){
	       NiteKeyListener listener = new NiteKeyListener(a, binding);
	       component.addKeyListener(listener);
	   }
       }
   }
   	
    
    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#addChild(net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler)
     */
    public void addChild(NDisplayObjectHandler child) {
	children.add(child);
        if (child instanceof JComponentHandler) {
            JComponentHandler compHandlerChild = (JComponentHandler) child;
            JComponent childComponent = compHandlerChild.getJComponent();
            component.add(childComponent);
        } else if (child instanceof NActionReferenceHandler){
	    addActionReference((NActionReferenceHandler)child);
        } else {
            throw new IllegalArgumentException("Incompatible child.");
        }
    }


    /** adds a reference to an action for this component, so that the
        action is fired when the appropriate key press / mouse click
        occurs */
    public void addActionReference(NActionReferenceHandler ref){
	NActionReference nar = ref.getActionref();
	// add this action reference to list of action references for this component
	addActionReference(nar);
	// set up the InputMap for this component. InputMap binds key
	// strokes to action names
	nar.getInputMap().setParent(component.getInputMap(JComponent.WHEN_FOCUSED));
	component.setInputMap(JComponent.WHEN_FOCUSED, nar.getInputMap());
    }
    
    public JComponent getJComponent() {
        return component;
    }

    /**
     * Use the font information specified in the properties componentToData to 
     * set the font on the JComponent
     * */
    //FIX ME - Need to specify to the user which fonts should be allowed
    public Font setUpFont() {
	if (properties != null){
	    String name = "Arial";
	    int size = 10;
	    int style = Font.PLAIN;
	    //find the font size specified in the properties map
	    String sizeString = (String) properties.get(NConstants.fontSize);
	    if (sizeString != null)
		size = Integer.parseInt(sizeString);
	    
	    //find the name of the font specified in the properties map
	    String fontString = (String) properties.get(NConstants.font);
	    if (fontString != null)
		name = fontString;
	    
	    //find the name of the font specified in the properties map
	    String fontstyle = (String) properties.get(NConstants.fontStyle);
	    
	    if (fontstyle != null) {
		if (fontstyle.equalsIgnoreCase(NConstants.bold)) {
		    style = Font.BOLD;
		} else if (fontstyle.equalsIgnoreCase(NConstants.italic)) {
		    style = Font.ITALIC;
		}
	    }
	    
	    Font font = new Font(name, style, size);
	    component.setFont(font);
	    return font;
	} else return null;
    }

    /**
     * Use the foreground colour information specified in the
     * properties componentToData to set the colours on the JComponent
     * */
    public Color setUpForegroundColour() {
	if (properties != null){
	    Color textcolour = NConstants.getColour((String) properties.get(NConstants.foregroundColour));
	    if (textcolour != null) component.setForeground(textcolour);
	    return textcolour;
	} else return null;
    }

    /**
     * Use the background colour information specified in the
     * properties componentToData to set the colours on the JComponent
     * */
    public Color setUpBackgroundColour() {
	if (properties != null){
	    Color textcolour = NConstants.getColour((String) properties.get(NConstants.backgroundColour));
	    if (textcolour != null) component.setBackground(textcolour);
	    return textcolour;
	} else return null;
    } 

    /**
     * Use the imagepath specified in the properties componentToData to 
     * set up the image for the JComponent
     * */
    public void setUpImage() {
	if (properties != null){
	    String imagepath = (String) properties.get(NConstants.ImagePath);
	    if (imagepath != null) {
        	//we need to process the image path to make it suitable for unix. Assume that it will be in windows format in the stylsheet
        	String neutralImagePath = imagepath.replace('\\', System.getProperty("file.separator").charAt(0));
		setImage(image = new ImageIcon(neutralImagePath));
	    }
	}
    }
    
    /**
     * Use the string specified in the properties componentToData to 
     * set up tooltip text
     * */
    public void setUpToolTip() {
	if (properties != null){
	    String tip = (String) properties.get(NConstants.toolTip);
	    if (tip != null) {
		component.setToolTipText(tip);
	    }
        }
    }

    /**
     * Returns the image.
     * @return ImageIcon
     */
    public ImageIcon getImage() {
        return image;
    }

    /**
     * Sets the image.
     * @param image The image to set
     */
    public void setImage(ImageIcon image) {
        this.image = image;
    }


    
   



}
