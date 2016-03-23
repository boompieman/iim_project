/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import javax.swing.JTabbedPane;

import net.sourceforge.nite.gui.actions.NiteAction;

/**
 * @author judyr
 *
 * 
 */
public class TabbedPaneHandler extends JComponentHandler {

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
    	component = new JTabbedPane();
    	setUpForegroundColour();
        setUpBackgroundColour();
    	
    }

    /**
     * Children of this component should be added using @see JTabbedPane.addTab
     * */
    public void addChild(NDisplayObjectHandler child) {
    	if (child instanceof JComponentHandler) {
            JComponentHandler jchild = (JComponentHandler) child;
            ((JTabbedPane)component).addTab(jchild.content, jchild.getImage(), jchild.component);
            children.add(jchild);
        }else throw new IllegalArgumentException("Attempted to add child of wrong type to TabbedPane");
        
        
	

    }
     
    
}
