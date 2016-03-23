/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import javax.swing.JComponent;
import javax.swing.JSplitPane;


import net.sourceforge.nite.nstyle.NConstants;

/**
 * @author judyr
 *
 * 
 */
public class SplitPaneHandler extends JComponentHandler {

    JComponent first = null;
    JComponent second = null;

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
        component = new JSplitPane();
        setUpForegroundColour();
        setUpBackgroundColour();
        setUpAlignment();
    }

    /**
     *  Decide whether the split pane is to be horizontal or vertical, according the the spec in the properties componentToData
     * */
    private void setUpAlignment() {
        String align = (String) properties.get(NConstants.splitPaneSplit);
        if (align != null){
        if (align.equalsIgnoreCase("Horizontal"))
            ((JSplitPane) component).setOrientation(
                JSplitPane.HORIZONTAL_SPLIT);
        else
            ((JSplitPane) component).setOrientation(JSplitPane.VERTICAL_SPLIT);
       
        }
         ((JSplitPane) component).setOneTouchExpandable(true);

    }
    /**
    * Children of this component should be added to one of the panes of the splitpane. Remember that
    * left and right correspond to top and bottom in JSplitPane, depending on whether it is vertical or horizontal
    * */
    public void addChild(NDisplayObjectHandler child) {
        if (child instanceof JComponentHandler) {
            JComponentHandler jchild = (JComponentHandler) child;
            if (first == null)
                ((JSplitPane) component).setLeftComponent(
                    first = jchild.component);
            else {
                if (second == null) {
                    ((JSplitPane) component).setRightComponent(
                        second = jchild.component);
                } else
                    throw new RuntimeException("Attempted to add one component too many to a SplitPane");
            }
            children.add(jchild);

        } else
            throw new IllegalArgumentException("Attempted to add child of wrong type to JSplitPane");

    }

 


}
