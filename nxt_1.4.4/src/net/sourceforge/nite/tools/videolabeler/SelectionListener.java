package net.sourceforge.nite.tools.videolabeler;

import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.meta.NLayer;

/**
 * <p>A selection listener is notified whenever an agent or layer is selected
 * or deselected.</p>
 */
public interface SelectionListener {

    /**
     * <p>Called when an agent is selected or deselected.</p>
     *
     * @param agent the agent that is selected or deselected
     * @param selected true if the agent is selected, false if it is deselected
     */
    public void agentSelected(NAgent agent, boolean selected);

    /**
     * <p>Called when a layer is selected or deselected.</p>
     *
     * @param layer the layer that is selected or deselected
     * @param selected true if the layer is selected, false if it is deselected
     */
    public void layerSelected(AnnotationLayer layer, boolean selected);
}
