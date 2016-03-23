/**
 * 
 */
package net.sourceforge.nite.gui.timelineviewer;

import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteCorpus;

/**
 * Provides a method that {@link TimeGrid} can call to 
 * create new NOMElements when clicking on empty space in the grid.
 * 
 * @author Craig Nicol
 *
 */

public interface TimelineNOMElementCreator {
	/* Add a new NOMElement */
	public NOMElement createNewElement(float start, float end, String label, String layer, int depth);
}
