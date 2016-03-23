package net.sourceforge.nite.gui.abstractviewer;

import java.util.*;

import javax.swing.JComponent;

import net.sourceforge.nite.gui.util.ElementToTextDelegate;

public abstract class NiteAbstractViewer {
	private ElementToTextDelegate eet = new ElementToTextDelegate();
	
	public ElementToTextDelegate getElementToTextDelegate() {
		return eet;
	}
	
	public void setElementToTextDelegate(ElementToTextDelegate eetd) {
		eet = eetd;
	}
	/********************************************************************************
     * 
     * Set elements to display
     * 
     ********************************************************************************/
    
    /**
     * This method accepts an Iterator over NOMElements and inserts them
     * into the current view.
     * @param elements The Iterator pointing to the NOMElements to be inserted.
     */
    abstract public void setDisplayedElements(Iterator elements);
    
    /**
     * See {@link #setDisplayedElements(Iterator elements)} documentation
     */
    public void setDisplayedElements(List elements) {
        setDisplayedElements(elements.iterator());
    }
    /**
     * See {@link #setDisplayedElements(Iterator elements)} documentation
     */
    public void setDisplayedElements(Set elements) {
        setDisplayedElements(elements.iterator());
    }
    
    /********************************************************************
     * 
     * General Configuration options
     * 
     */
    
    /** Does the current viewer allow more than one element to be selected
     * NOTE: This might be better in the selection interface...
     */
    public boolean allowsMultipleSelection() {
    	return false;
    }
}
