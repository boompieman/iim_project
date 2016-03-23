package net.sourceforge.nite.tools.videolabeler;

/**
 * <p>A layout listener is notified whenever the size of a component changes,
 * so that the size and location of other components can be updated.</p>
 */
public interface LayoutListener {

    /**
     * <p>Called when the size of a component changes.</p>
     */
    public void componentSizeChanged();
}
