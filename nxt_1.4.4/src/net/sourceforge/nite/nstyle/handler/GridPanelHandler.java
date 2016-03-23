/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;


import net.sourceforge.nite.gui.actions.OutputComponent;
import net.sourceforge.nite.gui.textviewer.GridPanel;
import net.sourceforge.nite.nstyle.NConstants;
import net.sourceforge.nite.nxt.ObjectModelElement;

/**
 * @author judyr
 *
 * 
 */
public class GridPanelHandler extends JComponentHandler implements OutputComponent{

	/**
	 * The grid panel corresponding to this handler
	 * */
	GridPanel pane = null;
	boolean border = false;
    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
    	int cols = 1;
    	
    	if (properties.get(NConstants.Columns) != null){
    	cols = Integer.parseInt((String) properties.get(NConstants.Columns));
    	}
    	if (properties.get(NConstants.Border) != null){
    	if (((String) properties.get(NConstants.Border)).equalsIgnoreCase("true")) border = true;
    	}
    	pane = new GridPanel(cols, border);
        if (getClock() != null) pane.setClock(getClock());
    	component = pane;
    	setUpBackgroundColour();
    }
    
     /**
     * This needs it's own method for this, so it can set the internal panel to the background colour rather than the scroll pnae itself
     * */
    public Color setUpBackgroundColour() {
    	 Color textcolour = NConstants.getColour((String) properties.get(NConstants.backgroundColour));
        if (textcolour != null) pane.setBackgroundColour(textcolour);
        return textcolour;

    }
    
     public void addChild(NDisplayObjectHandler child) {
        if (child instanceof GridElementHandler) {
            
            GridElementHandler gehandler = (GridElementHandler) child;
            if ((gehandler.getStartTime() != 999) && (gehandler.getStartTime() != -999)){
                pane.addEntry(gehandler.rowspan, gehandler.colspan, gehandler.getStartTime(), gehandler.getEndTime(), gehandler.position, gehandler.component);
            }
            else pane.addEntry(gehandler.rowspan, gehandler.colspan, gehandler.position, gehandler.component);
	children.add(gehandler);
	setStartTime(pane.getMinStartTime());
        setEndTime(pane.getMaxEndTime());
	Border etched = BorderFactory.createEtchedBorder();
	if (border) gehandler.component.setBorder(etched);
        }else   throw new IllegalArgumentException("Attempted to add child of wrong type to GridPanel");

    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#displayElement(net.sourceforge.nite.nxt.ObjectModelElement, boolean)
     */
    public JComponent displayElement(ObjectModelElement e, boolean selected) {
        
        return pane;
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
