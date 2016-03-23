/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.awt.Dimension;
import javax.swing.JInternalFrame;



/**
 * 
 * 
 * @author Judy Robertson
 */
public class InternalFrameHandler extends JComponentHandler {

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
        super.assignID();
        super.assignSourceID();
        component = new JInternalFrame();
        setUpForegroundColour();
        setUpBackgroundColour();
       	setUpImage();
       	if (image != null) ((JInternalFrame)component).setFrameIcon(image);
       	if (content != null) ((JInternalFrame)component).setTitle(content);
        component.setSize(new Dimension(600, 600));
        component.setLocation(300,0);
       	((JInternalFrame) component).setResizable(true);
       	((JInternalFrame) component).setMaximizable(true);
        component.setVisible(true);
        
    }


    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.JComponentHandler#addChild(net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler)
     */
    public void addChild(NDisplayObjectHandler child) {
        if (child instanceof JComponentHandler) {
            JComponentHandler compChild = (JComponentHandler) child;
            
            JInternalFrame frame = (JInternalFrame) component;
            frame.getContentPane().add(compChild.getJComponent());
              children.add(compChild);
        } else {
            throw new IllegalArgumentException("Illegal child.");   
        }
        
        
    }
     

    

}
