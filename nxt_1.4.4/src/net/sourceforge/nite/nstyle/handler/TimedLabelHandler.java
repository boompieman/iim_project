/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;


import javax.swing.JComponent;
import javax.swing.JLabel;


import net.sourceforge.nite.gui.actions.OutputComponent;
import net.sourceforge.nite.gui.textviewer.NTimedLabel;
import net.sourceforge.nite.nxt.ObjectModelElement;


/**
 * @author judyr
 *
 * 
 */
public class TimedLabelHandler extends JComponentHandler implements OutputComponent {

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
        component = new NTimedLabel(content);
        
        setUpForegroundColour();
        setUpBackgroundColour();
        setUpFont();
        setUpImage();
        setUpTimes();
        
        
        setUpToolTip();
        if (image != null)
             ((JLabel) component).setIcon(image);

    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.gui.actions.OutputComponent#displayElement(net.sourceforge.nite.nxt.ObjectModelElement, boolean)
     */
    public JComponent displayElement(ObjectModelElement e, boolean selected) {
        // TODO Auto-generated method stub
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
