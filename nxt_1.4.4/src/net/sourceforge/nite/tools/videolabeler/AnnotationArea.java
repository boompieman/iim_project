package net.sourceforge.nite.tools.videolabeler;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Vector;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;

import net.sourceforge.nite.gui.textviewer.NTextArea;
import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.nxt.NOMObjectModelElement;
import net.sourceforge.nite.time.Clock;

/**
 * <p>An annotation area can display the annotations of a certain layer. If
 * the layer belongs to an agent coding rather than an interaction coding, an
 * agent should be specified as well. The display format of the annotations is
 * determined by the element formatter of the annotation layer that is
 * specified at construction.</p>
 *
 * <p>Before an annotation area is created, the global singleton document
 * should be created (see {@link Document Document}).</p>
 *
 * <p>This class is an observer for the document, so the annotation area can
 * be refreshed whenever the document changes. This possibly costly operation
 * is only performed when the annotation area is visible.</p>
 *
 * <p>The user can select one or more annotations by clicking or clicking and
 * dragging. If annotation selection listeners
 * (see {@link AnnotationSelectionListener AnnotationSelectionListener}) are
 * registered with this annotation area, they are notified when the user
 * selects one annotation.</p>
 *
 * <p>An annotation area is an annotation listener. When a new annotation
 * is started, the selected annotations will be deselected.</p>
 */
public class AnnotationArea extends NTextArea implements Observer, AnnotationListener {
    private Vector selectionListeners = new Vector();
    private boolean markSelections;
    private NAgent agent = null;
    private AnnotationLayer layer;
	
    /**
     * <p>Constructs a new annotation area for the specified layer and agent.
     * The agent parameter may be null if the layer belongs to an interaction
     * coding rather than an agent coding. The annotation area will immediately
     * show the annotations for the specified layer and agent (if not
     * null).</p>
     *
     * @param agent an agent (if the layer belongs to an agent coding) or null
     * (if the layer belongs to an interaction coding)
     * @param layer the layer for which annotations will be shown
     * @param markselect true if the user can select annotations, false
     * otherwise
     */
    public AnnotationArea(NAgent agent, AnnotationLayer layer, boolean markselect) {
        super();
        this.agent = agent;
        this.layer = layer;
        this.markSelections = markselect;
        Document doc = Document.getInstance();
        setClock(doc.getClock());
        initTextArea();
        refreshView();
        doc.addObserver(this);
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                handleComponentShown(e);
            }

            public void componentHidden(ComponentEvent e) {
                handleComponentHidden(e);
            }
        });
    }
    
    /**
     * <p>Called when the annotation area is made visible. This method registers
     * the annotation area as an observer with the document. The area is also
     * refreshed for any changes that were made while the area was hidden.</p>
     */
    private void handleComponentShown(ComponentEvent e) {
        Document doc = Document.getInstance();
        doc.addObserver(this);
        refreshView();
    }
    
    /**
     * <p>Called when the annotation area is made invisible. This method
     * removes the annotation area as an observer from the document.</p>
     */
    private void handleComponentHidden(ComponentEvent e) {
        Document doc = Document.getInstance();
        doc.deleteObserver(this);
        getClock().deregisterTimeHandler(this);
    }

    /**
     * Initialises the text area.
     */
    private void initTextArea() {
        setCaret(new DefaultCaret() {
            public void mouseClicked(MouseEvent e) {
                processSelectionRelease();
                super.mouseReleased(e);
            }    	        
        });
        getClock().registerTimeHandler(this);
        setStyles(layer.getStyleMap());
    }

    /**
     * Sets the styles with the style names.
     */
    private void setStyles(HashMap styles){
        Iterator it = styles.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            Style style = (Style)styles.get(key);
            addStyle(key,style);
        }
    }

    /**
     * Called when the user makes a selection. If one annotation is selected,
     * the registered annotation listeners are notified.
     */
    private void processSelectionRelease() {
        if (!markSelections)
            return;
        Set selectedElements = getSelectedElements();
        Iterator i = selectedElements.iterator();
        if (selectedElements.size() == 1) {
            NOMObjectModelElement element = (NOMObjectModelElement) i.next();
            notifyAnnotationSelected(element);
        }
    }
	
    /**
     * Clears the annotation area and displays all current annotations obtained
     * from the global singleton document.
     */
    private void refreshView() {
        getClock().deregisterTimeHandler(this);
        clear();
        initTextArea();
        String query = "($a " + layer.getCodeElement().getName() + ")";
        Document doc = Document.getInstance();
        try {
            List elems = doc.searchAnnotations(query);
            Iterator it = elems.iterator();
            while (it.hasNext()) {
                NOMWriteElement elem = (NOMWriteElement)it.next();
                if ((agent == null) || (agent == elem.getAgent()))
                    layer.getElementFormatter().showElement(elem,this);
            }
        } catch (Throwable ex) {
            System.out.println("ERROR: could not retrieve annotations: " + ex.getMessage());
        }
        getClock().registerTimeHandler(this);
        repaint();
    }

    /**
     * Notifies the registered annotation listeners that the specified
     * annotation has been selected.
     */
    private void notifyAnnotationSelected(NOMObjectModelElement annotation) {
        Iterator it = selectionListeners.iterator();
        while (it.hasNext()) {
            AnnotationSelectionListener l = (AnnotationSelectionListener)it.next();
            l.annotationSelected(annotation);
        }
    }

    /**
     * <p>Registers an annotation selection listener with this annotation area.
     * The listener will be notified whenever one annotation is selected.</p>
     *
     * @param l an annotation selection listener
     */
    public void addAnnotationSelectionListener(AnnotationSelectionListener l) {
        selectionListeners.add(l);
    }

    /**
     * <p>Unregisters an annotation selection listener from this annotation
     * area.</p>
     *
     * @param l an annotation selection listener
     */
    public void removeAnnotationSelectionListener(AnnotationSelectionListener l) {
        selectionListeners.remove(l);
    }

    ///////////////////////////////////////////////////////////////////////
    // Observer interface

    /**
     * <p>Updates the annotation area for the changed document.</p>
     */
    public void update(Observable o, Object arg) {
        refreshView();
    }

    ///////////////////////////////////////////////////////////////////////
    // AnnotationListener methods

    /**
     * <p>Deselects the selected annotations.</p>
     */
    public boolean annotationStarted(TargetControlPanel panel) {
        clearHighlights(NTextArea.SELECTION_HIGHLIGHTS);
        return true;
    }

    public boolean annotationTargetSet(TargetControlPanel panel) {
        return true;
    }

    public boolean annotationEnded(TargetControlPanel panel) {
        return true;
    }
}
