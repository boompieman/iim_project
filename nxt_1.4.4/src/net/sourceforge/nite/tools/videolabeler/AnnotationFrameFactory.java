package net.sourceforge.nite.tools.videolabeler;

import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JDesktopPane;

import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.meta.NCoding;

/**
 * <p>This factory creates annotation frames and keeps track of the currently
 * opened annotation frames. It is a selection listener, which means that it
 * can be notified when the user selects or deselects a layer or an agent. For
 * each selected layer that belongs to an agent coding, this factory makes sure
 * that there is one annotation frame for each of the selected agents. For
 * each selected layer that belongs to an interaction coding, this factory
 * makes sure that there is one annotation frame exactly.
 *
 * <p>The factory is a singleton object, which is created with
 * createInstance().</p>
 */
public class AnnotationFrameFactory implements SelectionListener {
    private Vector annotationFrames = new Vector();
    private Vector selectedAgents = new Vector();
    private Vector selectedLayers = new Vector();
    private JDesktopPane desktop;
    private Rectangle area;

    /**
     * <p>Constructs a new annotation frame factory.</p>
     *
     * @param desktop the desktop on which the annotation frames will be shown
     * @param area the area on the desktop where the annotation frames will be
     * layed out
     */
    private AnnotationFrameFactory(JDesktopPane desktop, Rectangle area) {
        this.desktop = desktop;
        this.area = area;
    }

    // the singleton factory
    private static AnnotationFrameFactory instance = null;

    /**
     * <p>Creates and returns the singleton annotation frame factory. If this
     * method has been called before, this method will return the existing
     * factory. The annotation frames will be layed out in the specified area
     * of the specified desktop pane.</p>
     *
     * @param desktop the desktop pane to which the annotation frames will be
     * added
     * @param area the area in the desktop pane in which the frames will be
     * layed out
     * @return the annotation frame factory
     */
    public static AnnotationFrameFactory createInstance(JDesktopPane desktop, Rectangle area) {
        if (instance == null)
            instance = new AnnotationFrameFactory(desktop,area);
        return instance;
    }

    /**
     * <p>Returns the singleton annotation frame factory. If createInstance()
     * has not been called yet, this method returns null.</p>
     *
     * @return the singleton annotation frame factory or null
     */
    public static AnnotationFrameFactory getInstance() {
        return instance;
    }

    /**
     * <p>Sets the area of the desktop pane in which the annotation frames
     * should be layed out. The current annotation frames are layed out in
     * the new area.</p>
     *
     * @param area the area in which the annotation frames are layed out
     */
    public void setArea(Rectangle area) {
        this.area = area;
        layoutFrames();
    }

    /**
     * <p>Lays out the current annotation frames in the designated area of the
     * desktop pane. This method makes use of a FrameArranger.</p>
     */
    private void layoutFrames() {
        int nframes = annotationFrames.size();
        if (nframes == 0) return;
        FrameArranger arranger = new FrameArranger(nframes,area,10,
                AnnotationFrame.MINIMUM_WIDTH,400,
                AnnotationFrame.MINIMUM_HEIGHT,500);
        for (int i = 0; i < annotationFrames.size(); i++) {
            AnnotationFrame frame = (AnnotationFrame)annotationFrames.get(i);
            Rectangle bounds = arranger.getBoundsForFrame(i);
            frame.setBounds(bounds);
            frame.show();
        }
    }

    /**
     * <p>Adds annotation frames for each combination of the specified agent
     * and a selected layer that belongs to an agent coding.</p>
     */
    private void addAgent(NAgent agent) {
        selectedAgents.add(agent);
        Iterator it = selectedLayers.iterator();
        while (it.hasNext()) {
            AnnotationLayer layer = (AnnotationLayer)it.next();
            NCoding coding = (NCoding)layer.getNLayer().getContainer();
            if (coding.getType() == NCoding.AGENT_CODING) {
                try {
                    AnnotationFrame frame = new AnnotationFrame(agent,layer);
                    annotationFrames.add(frame);
                    AnnotationArea area = frame.getAnnotationArea();
		    if (search!=null && area!=null) {
			search.registerResultHandler(area);
		    }
                    desktop.add(frame);
                } catch (Exception ex) {}
            }
        }
        layoutFrames();
    }

    /* added by jonathan 21.4.05 */
    protected net.sourceforge.nite.search.GUI search=null;
    /** set the search gui with which individial NTextAreas can be registered */
    protected void setSearch(net.sourceforge.nite.search.GUI search) {
	this.search=search;
    }

    /**
     * <p>Removes the annotation frames for the specified agent.</p>
     */
    private void removeAgent(NAgent agent) {
        selectedAgents.remove(agent);
        int i = 0;
        while (i < annotationFrames.size()) {
            AnnotationFrame frame = (AnnotationFrame)annotationFrames.get(i);
            if (frame.getAnnotationAgent() == agent) {
                try {
                    frame.setClosed(true);
                    AnnotationArea area = frame.getAnnotationArea();
		    if (search!=null && area!=null) {
			search.deregisterResultHandler(area);
		    }
                    annotationFrames.remove(i);
                } catch (PropertyVetoException ex) {
                    System.out.println("ERROR: Could not close annotation frame: " + ex.getMessage());
                }
            } else {
                i++;
            }
        }
        layoutFrames();
    }

    /**
     * <p>Adds the annotation frames for the specified layer. If the layer
     * belongs to an agent coding, this method adds frames for each combination
     * of the specified layer and the selected agents. If the layer belongs
     * to an interaction coding, this method adds one frame for the
     * specified layer.</p>
     */
    private void addLayer(AnnotationLayer layer) {
        selectedLayers.add(layer);
        NCoding coding = (NCoding)layer.getNLayer().getContainer();
        if (coding.getType() == NCoding.AGENT_CODING) {
            Iterator it = selectedAgents.iterator();
            while (it.hasNext()) {
                NAgent agent = (NAgent)it.next();
                try {
                    AnnotationFrame frame = new AnnotationFrame(agent,layer);
                    annotationFrames.add(frame);
                    AnnotationArea area = frame.getAnnotationArea();
		    if (search!=null && area!=null) {
			search.registerResultHandler(area);
		    }
                    desktop.add(frame);
                } catch (Exception ex) {}
            }
        } else {
            try {
                AnnotationFrame frame = new AnnotationFrame(null,layer);
                annotationFrames.add(frame);
                AnnotationArea area = frame.getAnnotationArea();
		if (search!=null && area!=null) {
		    search.registerResultHandler(area);
		}
                desktop.add(frame);
            } catch (Exception ex) {}
        }
        layoutFrames();
    }

    /**
     * <p>Removes the annotation frames for the specified layer.</p>
     */
    private void removeLayer(AnnotationLayer layer) {
        selectedLayers.remove(layer);
        int i = 0;
        while (i < annotationFrames.size()) {
            AnnotationFrame frame = (AnnotationFrame)annotationFrames.get(i);
            if (frame.getAnnotationLayer() == layer) {
                try {
                    AnnotationArea area = frame.getAnnotationArea();
		    if (search!=null && area!=null) {
			search.deregisterResultHandler(area);
		    }
                    frame.setClosed(true);
                    annotationFrames.remove(i);
                } catch (PropertyVetoException ex) {
                    System.out.println("ERROR: Could not close annotation frame: " + ex.getMessage());
                }
            } else {
                i++;
            }
        }
        layoutFrames();
    }

    ///////////////////////////////////////////////////////////////////////
    // SelectionListener methods

    /**
     * <p>Adds or removes annotation frames for the specified agent.</p>
     */
    public void agentSelected(NAgent agent, boolean selected) {
        if (selected) {
            if (!selectedAgents.contains(agent))
                addAgent(agent);
        } else {
            removeAgent(agent);
        }
    }

    /**
     * <p>Adds or removes annotation frames for the specified layer.</p>
     */
    public void layerSelected(AnnotationLayer layer, boolean selected) {
        if (selected) {
            if (!selectedLayers.contains(layer))
                addLayer(layer);
        } else {
            removeLayer(layer);
        }
    }
}
