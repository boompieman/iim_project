/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;




import javax.swing.BoxLayout;
import javax.swing.JPanel;

import net.sourceforge.nite.gui.actions.NiteAction;

/**
 * 
 * 
 * @author Judy Robertson
 */
public class PanelHandler extends JComponentHandler {

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
        
        component = new JPanel();
        component.setLayout( new BoxLayout(component, BoxLayout.Y_AXIS));
        setUpForegroundColour();
        setUpBackgroundColour();
        super.setUpImage();
         
       
                
    }
    
  

}
