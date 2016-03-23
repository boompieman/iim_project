/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import java.awt.Color;


import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;

import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * This class is used to display timing information. It stories timing
 * information about the content displayed on it, and uses this
 * information to decide whether to hightlight it on receiving a time
 * change from the clock It is intended for use in a larger data
 * display structure such as a table or a tree
 *
 * @author judyr
 * 
 */
public class NTimedLabel extends JLabel  {

private Color originalBackground;
private boolean timehighlit;

private NTreeNode node;
    
    public NTimedLabel(String text, Icon icon, boolean hasBorder, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
       if (hasBorder){
			//use a colour from the look and feel colour scheme for the border
			Color c = MetalLookAndFeel.getControlDarkShadow();
			setBorder(BorderFactory.createLineBorder(c));
		}
		originalBackground = this.getBackground();
      

    }
    
    public NTimedLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
       
			originalBackground = this.getBackground();
       

    }
    
    
    

    /**
     * Constructor for NTimedLabel.
     * @param text
     * @param horizontalAlignment
     */
    public NTimedLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
       
    }
    
    

   

    /**
     * Constructor for NTimedLabel.
     * @param image
     * @param horizontalAlignment
     */
    public NTimedLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
    }

    /**
     * Constructor for NTimedLabel.
     * @param image
     */
    public NTimedLabel(Icon image) {
        super(image);
    }

    /**
     * Constructor for NTimedLabel.
     */
    public NTimedLabel() {
        super();
    }

    /**
     * Constructor NTimedLabel.
     * @param string
     */
    public NTimedLabel(String string) {
    	super(string);
    }


    public void setTimeHighlit(boolean b){
	timehighlit = b;
   	
   	if (b) {
	    setBackground(MetalLookAndFeel.getFocusColor());
	    setOpaque(true);
   	}
   	else {
	    setBackground(originalBackground);
	    setOpaque(false);
   	}
    }

    public void setTimeHighlit(boolean b, Color c){
	timehighlit = b;
   	
   	if (b) {
	    setBackground(c);
	    setOpaque(true);
   	}
   	else {
	    setBackground(originalBackground);
	    setOpaque(false);
   	}
    }

    public boolean getTimeHighlit (){
	return timehighlit;	
    }
   

}
