/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.awt.Color;


import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;



import net.sourceforge.nite.gui.actions.NiteAction;
import net.sourceforge.nite.nstyle.NConstants;

/**
 * @author judyr
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ScrollPaneHandler extends JComponentHandler {

    /**
     * A JPanel which is used as the viewport view. All children will be added to it, rather than to the scroll pane itself
     * */
    private JPanel internal;
    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
    	
        //create a scroll pane which will show scroll bars horizontally or vertically when its contents would be obscured otherwise
        component =
            new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                internal = new JPanel();
                internal.setLayout(new BoxLayout(internal, BoxLayout.Y_AXIS));
                //internal.setLayout(new PnutsLayout("Cols = 1"));
               ((JScrollPane) component).setViewportView(internal);
               setUpBackgroundColour();

    }
    
    /**
     * This needs it's own method for this, so it can set the internal panel to the background colour rather than the scroll pnae itself
     * */
    public Color setUpBackgroundColour() {
    	 Color textcolour = NConstants.getColour((String) properties.get(NConstants.backgroundColour));
        if (textcolour != null) internal.setBackground(textcolour);
        return textcolour;

    }

    /**
     * When adding a child to a JScrollPane, it is necessary to add it the viewport
     * */
    public void addChild(NDisplayObjectHandler child) {
        if (child instanceof JComponentHandler) {
            JComponentHandler jh = (JComponentHandler) child;
            internal.add(jh.component);
	children.add(jh);
        }else   throw new IllegalArgumentException("Attempted to add child of wrong type to JScrollPane");

    }
     

    
}
