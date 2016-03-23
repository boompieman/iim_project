/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JComponent;

import net.sourceforge.nite.gui.actions.NiteAction;
import net.sourceforge.nite.gui.actions.OutputComponent;
import net.sourceforge.nite.nxt.ObjectModelElement;

/**
 * @author judyr
 *
 * 
 */
public class ButtonHandler extends JComponentHandler  implements OutputComponent {

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    
    protected void createPeer() {
           
        component = new JButton(content);
        setUpForegroundColour();
        setUpBackgroundColour();
        setUpFont();
        setUpImage();
        setUpToolTip();
        if (image != null)
             ((JButton) component).setIcon(image);

    }
    
    public void registerAction(String s, NiteAction a){
    	super.registerAction(s,a);
    	((JButton)component).addActionListener(a);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#displayElement(net.sourceforge.nite.nxt.ObjectModelElement, boolean)
     */
    public JComponent displayElement(ObjectModelElement e, boolean selected) {
		//FIX ME - selection is indicated by red text for labels, which is a bit lame
		   if (selected)  ((JButton)component).setForeground(Color.red);
		   else ((JButton)component).setForeground(Color.black);
		   if (e != null){
            
		   String type = e.getDisplayedAttribute();
		   String displayme = e.getAttributeValue(type);
		   if (displayme == null) displayme = e.getTextualContent();
		   ((JButton)component).setText(displayme);
		   }
    
   return component;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#redisplayElement(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void redisplayElement(ObjectModelElement e) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#removeDisplayComponent(net.sourceforge.nite.nxt.ObjectModelElement)
     */
    public void removeDisplayComponent(ObjectModelElement e) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#insertDisplayElement(net.sourceforge.nite.nxt.ObjectModelElement, net.sourceforge.nite.nxt.ObjectModelElement, int)
     */
    public void insertDisplayElement(ObjectModelElement newElement, ObjectModelElement parent, int position) {
        // TODO Auto-generated method stub
        
    }
   
    
    
}
