/**
 * 
 */
package net.sourceforge.nite.gui.timelineviewer;

import net.sourceforge.nite.nom.nomwrite.NOMElement;

/**
 * ElementToLayerDepthDelegate is used by {@link NiteTimeline} to determine
 * which sub-layer to place an element on. This interface is provided for
 * authors to create their own rules.
 * 
 * @author Craig Nicol
 *
 */
public interface ElementToLayerDepthDelegate {
	/**
	 * @param element
	 * @return the depth of element in the current layer, or 0 to suppress display
	 * @see NiteTimeline#getElementToLayerDelegate()
	 */
	public int getElementLayerDepth(NOMElement element);
}
