/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;


import net.sourceforge.nite.nstyle.NConstants;

/**
 * @author judyr
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class GridElementHandler extends JComponentHandler {
	
	int rowspan = 1;
	int colspan = 1;
	String position = "";

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     * As the GridPanelElement doesn't actually have a visible peer, this method sets
     */
    protected void createPeer() {
    	
    	colspan = Integer.parseInt((String) properties.get(NConstants.ColSpan));
    	
    	rowspan = Integer.parseInt((String) properties.get(NConstants.RowSpan));
    	position = (String) properties.get(NConstants.position); 
        setUpTimes();
        
    }
    
    /**
     * this should just make the component from the child into it's own component for display on the grid pane
     * */
     public void addChild(NDisplayObjectHandler child) {
     	
     	if (child instanceof JComponentHandler) {
            JComponentHandler jchandler = (JComponentHandler) child;
            component = jchandler.component;
            children.add(jchandler);
            setStartTime(child.getStartTime());
            setEndTime(child.getEndTime());
        }
     	
     }
      

   

}
