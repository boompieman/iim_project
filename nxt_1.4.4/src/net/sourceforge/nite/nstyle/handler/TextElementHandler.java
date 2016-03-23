/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;



import net.sourceforge.nite.gui.actions.NiteAction;
import net.sourceforge.nite.gui.textviewer.NTextElement;
import net.sourceforge.nite.nstyle.NConstants;
import net.sourceforge.nite.nxt.ObjectModelElement;


/**
 * @author judyr
 *
 */
public class TextElementHandler extends JComponentHandler {
	NTextElement textElement = null;
	

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    
    protected void createPeer() {
            
                  
    	String style = (String) properties.get(NConstants.style);
    	
    	 StringBuffer buff = new StringBuffer(content);
         String tabstring = (String) properties.get(NConstants.tabStop);
    	
    	 if (tabstring != null && !(tabstring.equals(""))) {
                    //information about the tab stops
                    int tab = new Integer(tabstring).intValue();
                    
                    //insert the correct number of tab characters at the start of the content string
                    for (int i = 0; i < tab; i++) {
                        buff.insert(0, "\t");
                    }
                }
    	
    	textElement = new NTextElement(buff.toString(), style);
        setUpTimes();
        textElement.setStartTime(getStartTime());
        textElement.setEndTime(getEndTime());
      
    	
    }
    
    public void setElement(ObjectModelElement e){
            super.setElement(e);
            //add a record of the data element to the text element
            if (getElement() != null && textElement != null){
                   textElement.setDataElement(getElement());
            }
    }
    
    
     /**
     * TextElements are leaf nodes, so they should never have any children. This method does nothing
     * */
     public void addChild(NDisplayObjectHandler child){
     }


    /**
     * Returns the textElement.
     * @return NTextElement
     */
    public NTextElement getTextElement() {
        return textElement;
    }

    /**
     * Sets the textElement.
     * @param textElement The textElement to set
     */
    public void setTextElement(NTextElement textElement) {
        this.textElement = textElement;
    }

}
