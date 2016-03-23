package net.sourceforge.nite.tools.videolabeler;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import net.sourceforge.nite.gui.util.ValueColourMap;
import net.sourceforge.nite.util.Debug;
import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.meta.NPointer;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.NOMPointer;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWritePointer;
import net.sourceforge.nite.nstyle.NConstants;
import org.w3c.dom.Node;

/**
 * <p>A label target control panel can be used with a label annotation layer
 * (see {@link LabelAnnotationLayer LabelAnnotationLayer}). It shows buttons
 * for all possible targets of the annotation layer. When a button is clicked
 * the events annotationEnded, annotationStarted and annotationTargetSet are
 * fired in that order.</p>
 */
public class LabelTargetControlPanel extends TargetControlPanel {
    private static final double BUTTON_PROPORTION = 3.0;
    private LabelAnnotationLayer labelLayer;
    private NOMElement oldcurrentTarget = null;
    private Object currentTarget = null;
    private Vector keystrokes = new Vector(); // String objects
    private Object annotationLock = new Object();

    /**
     * <p>Constructs a new label target control panel for the specified layer
     * and agent. The agent parameter may be null if the layer belongs to an
     * interaction coding rather than an agent coding.</p>
     *
     * @param frame the annotation frame that contains this control panel
     * @param layer an annotation layer
     * @param layerInfo the layerinfo element from the configuration file
     * @param agent an agent (if the layer belongs to an agent coding) or null
     * (if the layer belongs to an interaction coding)
     */
    public LabelTargetControlPanel(AnnotationFrame frame, AnnotationLayer layer,
            Node layerInfo, NAgent agent) {
        super(frame,layer,layerInfo,agent);
        labelLayer = (LabelAnnotationLayer)layer;
        createButtons();
        addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                GlobalInputMap map = GlobalInputMap.getInstance();
                while (keystrokes.size() > 0) {
                    String key = (String)keystrokes.remove(0);
                    map.removeKeyStroke(key);
                }
            }
        });
    }
	
    /**
     * <p>Creates a button for each possible target and adds the buttons to
     * this panel.</p>
     */
    private void createButtons() {
        setLayout(new OptimalBoxLayout(BUTTON_PROPORTION,true));
        List targets = labelLayer.getTargets();
        ValueColourMap colourMap = labelLayer.getColourMap();
        Iterator it = targets.iterator();
        while (it.hasNext()) {
	    Object target = it.next();
	    String label=null;
	    if (target instanceof String) { label=(String)target; }
	    else { label = labelLayer.getTargetName((NOMElement)target); }
            if (label != null) {
                TargetAction action = new TargetAction(label,target);
                JButton button = new JButton(action);
                button.setBackground(colourMap.getValueBackColour(target));
                add(button);
            }
        }
    }

    /**
     * <p>Sets the target of the current annotation. This method is called by
     * an annotation listener after the annotationTargetSet event, which is
     * fired when the user clicks a button in this panel.</p>
     *
     * @param annotation the current annotation
     * @return true if the target was set successfully, false otherwise
     */
    public boolean setTarget(NOMWriteElement annotation) {
        Document doc = Document.getInstance();
	if (currentTarget instanceof NOMElement) {
	    NPointer pointer = labelLayer.getPointer();
	    NOMPointer point = new NOMWritePointer(doc.getCorpus(),pointer.getRole(),annotation,(NOMElement)currentTarget);
	    try {
		annotation.addPointer(point);
	    } catch (NOMException ex) {
		Debug.print("ERROR: Could not add pointer to annotation: " + ex.getMessage(), Debug.ERROR);
		return false;
	    }
	} else if (currentTarget instanceof String) {
	    try {
		annotation.setStringAttribute(labelLayer.getEnumeratedAttribute().getName(), (String)currentTarget);
	    } catch (NOMException nex) {
		Debug.print("Failed to set attribute '"+labelLayer.getEnumeratedAttribute()+"' for element '"+annotation.getID()+"' to value '"+currentTarget+"'.", Debug.ERROR);
		return false;
	    }
	} else {
	    return false;
	}
        return true;
    }

    /**
     * <p>Action for the target buttons. Fires the AnnotationListener events
     * when a button is clicked.</p>
     */
    private class TargetAction extends AbstractAction {
        private Object target;

        public TargetAction(String label, Object target) {
            super();
            this.target = target;
            putValue(Action.NAME,label);
	    // forces the keys to be incrtementing numbers..
	    String key = "1";
	    if (target instanceof NOMElement) {
		key = ((NOMElement)target).getKeyStroke();
	    } else if (target instanceof String) {
		
	    }
            GlobalInputMap map = GlobalInputMap.getInstance();
            key = map.addKeyStroke(key,this);
            if (key != null) {
                keystrokes.add(key);
                String showKey = CSLConfig.getInstance().getShowKeyStrokes();
                if (showKey.equals("label"))
                    putValue(Action.NAME,key + " - " + label);
                else if (showKey.equals("tooltip"))
                    putValue(Action.SHORT_DESCRIPTION,key + " - " + label);
            }
        }

        public void actionPerformed(ActionEvent e) {
            synchronized (annotationLock) {
                processAnnotationEnded();
                processAnnotationStarted();
                currentTarget = target;
                processAnnotationTargetSet();
            }
        }
    }
}
