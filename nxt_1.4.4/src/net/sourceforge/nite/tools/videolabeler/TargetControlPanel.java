package net.sourceforge.nite.tools.videolabeler;

import java.util.Iterator;
import java.util.Vector;
import javax.swing.JPanel;

import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.nxt.NOMObjectModelElement;

import org.w3c.dom.Node;

/**
 * <p>A target control panel is a panel that allows the user to make
 * annotations in a certain layer. If the layer belongs to an agent coding
 * rather than an interaction coding, an agent should be specified as well. The
 * panel is part of an annotation frame (see
 * {@link AnnotationFrame AnnotationFrame}). It interacts with an annotation
 * listener. An annotation listener is registered with this panel by calling
 * {@link #addAnnotationListener(net.sourceforge.nite.tools.videolabeler.AnnotationListener) addAnnotationListener()}.</p>
 *
 * <p>According to user input the panel should notify the annotation listener
 * of the following three events: annotationStarted, annotationTargetSet and
 * annotationEnded, in that order. A subclass can notify the registered
 * annotation listeners by calling processAnnotationStarted(),
 * processAnnotationTargetSet() and processAnnotationEnded().</p>
 *
 * <p>After the annotationTargetSet event, an annotation listener can call the
 * {@link #setTarget(net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement) setTarget()}
 * method of this control panel, which should be implemented by a subclass.</p>
 *
 * <p>A target control panel is registered as an annotation selection listener
 * with the annotation area in the same annotation frame. Subclasses can
 * override
 * {@link #annotationSelected(net.sourceforge.nite.nxt.NOMObjectModelElement) annotationSelected()}
 * to handle the event when an existing annotation is selected in the annotation
 * area.</p>
 *
 * <p>If something needs to be done before the annotation frame that contains
 * the control panel, is closed, register a
 * {@link java.awt.event.ComponentListener ComponentListener} and listen to componentHidden.
 * For instance, any actions that were added to the global input map, should
 * be removed.</p>
 */
public abstract class TargetControlPanel extends JPanel implements AnnotationSelectionListener {
    private Vector annotationListeners = new Vector();
    protected NAgent agent;
    protected AnnotationLayer layer;
    protected Node layerInfo;
    protected AnnotationFrame frame;

    /**
     * <p>Constructs a new target control panel for the specified layer and
     * agent. The agent parameter may be null if the layer belongs to an
     * interaction coding rather than an agent coding.</p>
     *
     * @param frame the annotation frame that contains this control panel
     * @param layer an annotation layer
     * @param layerInfo the layerinfo element from the configuration file
     * @param agent an agent (if the layer belongs to an agent coding) or null
     * (if the layer belongs to an interaction coding)
     */
    public TargetControlPanel(AnnotationFrame frame, AnnotationLayer layer,
            Node layerInfo, NAgent agent) {
        super();
        this.frame = frame;
        this.agent = agent;
        this.layer = layer;
        this.layerInfo = layerInfo;
    }
    
    /**
     * <p>Returns the agent of this control panel, if the annotation layer
     * belongs to an agent coding. If the annotation layer belongs to an
     * interaction coding, this method returns null.</p>
     *
     * @return an agent or null
     */
    public NAgent getAgent() {
        return agent;
    }

    /**
     * <p>Returns the annotation layer for this control panel.</p>
     *
     * @return an annotation layer
     */
    public AnnotationLayer getLayer() {
        return layer;
    }

    /**
     * <p>Registers an annotation listener with this control panel.</p>
     *
     * @param l an annotation listener
     */
    public void addAnnotationListener(AnnotationListener l) {
        annotationListeners.add(l);
    }

    /**
     * <p>Unregisters an annotation listener from this control panel.</p>
     *
     * @param l an annotation listener
     */
    public void removeAnnotationListener(AnnotationListener l) {
        annotationListeners.remove(l);
    }

    /**
     * <p>A subclass should call this method when the start of a new annotation
     * is marked. This method notifies annotation listeners. It returns true
     * if at least one of the annotation listeners returned true.</p>
     */
    protected boolean processAnnotationStarted() {
        Iterator it = annotationListeners.iterator();
        boolean result = false;
        while (it.hasNext()) {
            AnnotationListener l = (AnnotationListener)it.next();
            result |= l.annotationStarted(this);
        }
        return result;
    }

    /**
     * <p>A subclass should call this method when the target of the current
     * annotation is set. This method notifies annotation listeners. It returns
     * true if at least of one the annotation listeners returned true.</p>
     */
    protected boolean processAnnotationTargetSet() {
        Iterator it = annotationListeners.iterator();
        boolean result = false;
        while (it.hasNext()) {
            AnnotationListener l = (AnnotationListener)it.next();
            result |= l.annotationTargetSet(this);
        }
        return result;
    }

    /**
     * <p>A subclass should call this method when the end of the current
     * annotation is marked. This method notifies annotation listeners. It
     * returns true if at least one of the annotation listeners returned
     * true.</p>
     */
    protected boolean processAnnotationEnded() {
        Iterator it = annotationListeners.iterator();
        boolean result = false;
        while (it.hasNext()) {
            AnnotationListener l = (AnnotationListener)it.next();
            result |= l.annotationEnded(this);
        }
        return result;
    }
	
    /**
     * <p>Called when an existing annotation is selected in the annotation
     * area of the same annotation frame. This method does nothing, but may
     * be overridden.</p>
     *
     * @param annotation the selected annotation
     */
    public void annotationSelected(NOMObjectModelElement annotation) {
    }
	
    /**
     * <p>Sets the target of the current annotation. This method is called by
     * an annotation listener after the annotationTargetSet event.</p>
     *
     * @param annotation the current annotation
     * @return true if the target was set successfully, false otherwise
     */
    public abstract boolean setTarget(NOMWriteElement annotation);
}
