package net.sourceforge.nite.tools.videolabeler;

import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.util.Vector;
import javax.swing.JDesktopPane;

import net.sourceforge.nite.meta.NAgent;

/**
 * <p>A view frame factory is a global singleton object. Before it is used it
 * should be created with
 * {@link #createInstance(javax.swing.JDesktopPane, java.awt.Rectangle) ViewFrameFactory.createInstance()}.
 * From then on the same factory can be retrieved with
 * {@link #getInstance() ViewFrameFactory.getInstance()}.</p>
 *
 * <p>A view frame factory creates view frames for certain selected annotation
 * layers. A view frame is an instance of {@link ViewFrame ViewFrame}. This
 * factory does not only create view frames, but it also serves as a repository
 * of the current view frames and it is responsible for laying out the view
 * frames in a designated area of a desktop pane.</p>
 *
 * <p>The factory implements the {@link SelectionListener SelectionListener}
 * interface. The user should be able to select and deselect the layers for
 * which view frames should be shown. Whenever the user selects or deselects an
 * annotation layer, the method
 * {@link #layerSelected(net.sourceforge.nite.tools.videolabeler.AnnotationLayer, boolean) layerSelected()}
 * should be called, so that the view frame for the selected/deselected layer
 * is created/removed and the view frames are layed out on the desktop.</p>
 */
public class ViewFrameFactory implements SelectionListener {
    private Vector viewFrames = new Vector();
    private Vector selectedLayers = new Vector();
    private JDesktopPane desktop;
    private Rectangle area;

    /**
     * Constructs a new view frame factory. See createInstance()
     */
    private ViewFrameFactory(JDesktopPane desktop, Rectangle area) {
        this.desktop = desktop;
        this.area = area;
    }

    private static ViewFrameFactory instance = null;

    /**
     * <p>Initialises the singleton view frame factory. The factory will display
     * the view frames on the specified desktop. The frames will be layed out in
     * the specified area of the desktop. The factory uses a
     * {@link FrameArranger FrameArranger} to lay out the view frames.</p>
     *
     * <p>If the singleton view frame factory has already been initialised, this
     * method does not create a new factory, but simple returns the existing
     * singleton factory.</p>
     *
     * @param desktop the desktop where the view frames will be displayed
     * @param area the area of the desktop in which the view frames will be
     * layed out
     * @return the singleton view frame factory
     */
    public static ViewFrameFactory createInstance(JDesktopPane desktop, Rectangle area) {
        if (instance == null)
            instance = new ViewFrameFactory(desktop,area);
        return instance;
    }

    /**
     * <p>Returns the singleton view frame factory. The factory should have been
     * initialised first with
     * {@link #createInstance(javax.swing.JDesktopPane, java.awt.Rectangle) createInstance}.
     * If the factory has not been initialised yet, this method returns
     * null.</p>
     *
     * @return the singleton view frame factory or null
     */
    public static ViewFrameFactory getInstance() {
        return instance;
    }

    /**
     * <p>Sets the area of the desktop in which the view frames are layed out.
     * This method will set the area and lay out the current view frames in the
     * specified area.</p>
     *
     * @param area an area of the desktop
     */
    public void setArea(Rectangle area) {
        this.area = area;
        layoutFrames();
    }

    /**
     * Lays out the current view frames in the set desktop area.
     */
    private void layoutFrames() {
        int nframes = viewFrames.size();
        if (nframes == 0) return;
        FrameArranger arranger = new FrameArranger(nframes,area,10,150,800,100,800);
        for (int i = 0; i < viewFrames.size(); i++) {
            ViewFrame frame = (ViewFrame)viewFrames.get(i);
            Rectangle bounds = arranger.getBoundsForFrame(i);
            frame.setBounds(bounds);
            frame.show();
        }
    }

    /**
     * Adds a layer to the selected layers, creates the view frame for that
     * layer and lays out the current view frames.
     */
    private void addLayer(AnnotationLayer layer) {
        selectedLayers.add(layer);
        ViewFrame frame = new ViewFrame(layer,search);
        viewFrames.add(frame);
        desktop.add(frame);
        layoutFrames();
    }

    /**
     * Removes a layer from the selected layers, removes the view frame for that
     * layer and lays out the current view frames.
     */
    private void removeLayer(AnnotationLayer layer) {
        selectedLayers.remove(layer);
        int i = 0;
        while (i < viewFrames.size()) {
            ViewFrame frame = (ViewFrame)viewFrames.get(i);
            if (frame.getViewLayer() == layer) {
                try {
                    frame.setClosed(true);
                    viewFrames.remove(i);
                } catch (PropertyVetoException ex) {
                    System.out.println("ERROR: Could not close view frame: " + ex.getMessage());
                }
            } else {
                i++;
            }
        }
        layoutFrames();
    }

    /**
     * <p>This method does nothing, because view frames are only associated with
     * a layer, not with an agent.</p>
     */
    public void agentSelected(NAgent agent, boolean selected) {}

    /* added by jonathan 21.4.05 */
    protected net.sourceforge.nite.search.GUI search=null;
    /** set the search gui with which individial NTextAreas can be registered */
    protected void setSearch(net.sourceforge.nite.search.GUI search) {
	this.search=search;
    }

    /**
     * <p>Selects or deselects a layer. If a layer is selected, the view frame
     * for that layer is created on the desktop. If a layer is deselected, the
     * view frame for that layer is removed from the desktop. In either case
     * the new set of selected view frames are layed out in the designated
     * desktop area.</p>
     *
     * @param layer the layer that is selected or deselected
     * @param selected true if the layer is selected, false if it is deselected
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
