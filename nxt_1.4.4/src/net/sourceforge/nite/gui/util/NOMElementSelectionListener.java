/**
 * 
 */
package net.sourceforge.nite.gui.util;

import java.util.List;

/**
 * @author Craig Nicol
 *
 */
public interface NOMElementSelectionListener {
	/*
	 * Supplies a List of NOMElements representing the new selection
	 */
	public void selectionChanged(List newSelection); 
}
