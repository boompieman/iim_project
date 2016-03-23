package net.sourceforge.nite.tools.videolabeler;

import javax.swing.JSplitPane;

/**
 * <p>A split pane listener can be registered with an {@link ObservableSplitPane
 * ObservableSplitPane} and be notified when the divider of the split pane is
 * moved.</p>
 */
public interface SplitPaneListener {
    
    /**
     * <p>Called when the divider of the split pane is moved.</p>
     *
     * @param splitter the split pane whose divider moved
     */
    public void dividerMoved(JSplitPane splitter);
}
