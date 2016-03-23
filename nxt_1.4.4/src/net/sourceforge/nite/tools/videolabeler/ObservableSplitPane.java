package net.sourceforge.nite.tools.videolabeler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JSplitPane;

/**
 * <p>An observable split pane can notify registered
 * {@linkplain SplitPaneListener split pane listeners} when the divider of
 * the split pane is moved.</p>
 */
public class ObservableSplitPane extends JSplitPane {
    
    private List splitPaneListeners = new ArrayList();
    
    public ObservableSplitPane(int newOrientation) {
        super(newOrientation);
    }
    
    public void setDividerLocation(double proportionalLocation) {
        super.setDividerLocation(proportionalLocation);
        notifyDividerMoved();
    }
    
    public void setDividerLocation(int location) {
        super.setDividerLocation(location);
        notifyDividerMoved();
    }
    
    public void addSplitPaneListener(SplitPaneListener l) {
        if (!splitPaneListeners.contains(l))
            splitPaneListeners.add(l);
    }
    
    public void removeSplitPaneListener(SplitPaneListener l) {
        splitPaneListeners.remove(l);
    }
    
    private void notifyDividerMoved() {
        Iterator it = splitPaneListeners.iterator();
        while (it.hasNext()) {
            SplitPaneListener l = (SplitPaneListener)it.next();
            l.dividerMoved(this);
        }
    }
}
