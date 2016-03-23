package net.sourceforge.nite.tools.videolabeler;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import net.sourceforge.nite.gui.util.OntologyTreeView;
import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.meta.NPointer;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.NOMPointer;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWritePointer;
import org.w3c.dom.Node;

/**
 * <p>A tree target control panel can be used with a label annotation layer
 * (see {@link LabelAnnotationLayer LabelAnnotationLayer}). The targets must
 * be elements from an ontology! The panel shows a tree view of the ontology
 * where the leafs are the possible targets of the annotation layer. When a
 * leaf is selected the events annotationEnded, annotationStarted and
 * annotationTargetSet are fired in that order.</p>
 */
public class TreeTargetControlPanel extends TargetControlPanel {
    private OntologyTreeView tree;
    private LabelAnnotationLayer labelLayer;
    private NOMElement currentTarget = null;
    private HashMap keyMap = new HashMap(); // NOMElement (target) to String (keystroke)
    private ActionMap actionMap = new ActionMap(); // NOMElement (target) to Action
    private TreeSelectionListener selListener = new OntologyTreeSelectionListener();
    private Object annotationLock = new Object();

    /**
     * <p>Constructs a new tree target control panel for the specified layer
     * and agent. The agent parameter may be null if the layer belongs to an
     * interaction coding rather than an agent coding.</p>
     *
     * @param frame the annotation frame that contains this control panel
     * @param layer an annotation layer
     * @param layerInfo the layerinfo element from the configuration file
     * @param agent an agent (if the layer belongs to an agent coding) or null
     * (if the layer belongs to an interaction coding)
     */
    public TreeTargetControlPanel(AnnotationFrame frame, AnnotationLayer layer,
            Node layerInfo, NAgent agent) {
        super(frame,layer,layerInfo,agent);
        labelLayer = (LabelAnnotationLayer)layer;
        createActions();
        createTreeView();
        addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                GlobalInputMap map = GlobalInputMap.getInstance();
                Iterator it = keyMap.values().iterator();
                while (it.hasNext()) {
                    String key = (String)it.next();
                    map.removeKeyStroke(key);
                }
                keyMap.clear();
            }
        });
    }
    
    /**
     * <p>Creates the actions for all annotation targets. Actions are put in
     * the actionMap. Keystrokes are put in the keyMap.</p>
     */
    private void createActions() {
        List targets = labelLayer.getTargets();
        Iterator it = targets.iterator();
        while (it.hasNext()) {
            NOMElement target = (NOMElement)it.next();
            String label = labelLayer.getTargetName(target);
            if (label != null) {
                TargetAction action = new TargetAction(label,target);
                actionMap.put(target,action);
            }
        }
    }
	
    /**
     * <p>Creates the tree view and adds it to this panel.</p>
     */
    private void createTreeView() {
        setLayout(new BorderLayout());
        Document doc = Document.getInstance();
        String showKeysString = CSLConfig.getInstance().getShowKeyStrokes();
        int showKeys = OntologyTreeView.SHOWKEYS_OFF;
        if (showKeysString.equals("label"))
            showKeys = OntologyTreeView.SHOWKEYS_LABEL;
        else if (showKeysString.equals("tooltip"))
            showKeys = OntologyTreeView.SHOWKEYS_TOOLTIP;
        tree = OntologyTreeView.getOntologyTreeView(doc.getCorpus(),
                labelLayer.getLabelAttribute(),labelLayer.getTargetRootID(),
                keyMap,showKeys);
        tree.setColourMap(labelLayer.getColourMap());
        TreePath root = new TreePath(tree.getModel().getRoot());
        tree.expandPath(root);

        tree.addTreeSelectionListener(selListener);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        add(new JScrollPane(tree),BorderLayout.CENTER);
    }

    /**
     * <p>Sets the target of the current annotation. This method is called by
     * an annotation listener after the annotationTargetSet event, which is
     * fired when the user selects a leaf in the tree.</p>
     *
     * @param annotation the current annotation
     * @return true if the target was set successfully, false otherwise
     */
    public boolean setTarget(NOMWriteElement annotation) {
        Document doc = Document.getInstance();
        NPointer pointer = labelLayer.getPointer();
        NOMPointer point = new NOMWritePointer(doc.getCorpus(),pointer.getRole(),annotation,currentTarget);
        try {
            annotation.addPointer(point);
        } catch (NOMException ex) {
            System.out.println("ERROR: Could not add pointer to annotation: " + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * <p>Action for the annotation targets. Fires the AnnotationListener events
     * and synchronises the tree view with the current target.</p>
     */
    private class TargetAction extends AbstractAction {
        private NOMElement target;

        /**
         * <p>Constructs a new target action for the specified annotation
         * target. A possible keystroke is added to the keyMap.</p>
         */
        public TargetAction(String label, NOMElement target) {
            super();
            this.target = target;
            putValue(Action.NAME,label);
            String key = target.getKeyStroke();
            GlobalInputMap map = GlobalInputMap.getInstance();
            key = map.addKeyStroke(key,this);
            if (key != null) {
                keyMap.put(target,key);
            }
        }
        
        /**
         * <p>Performs this target action. The parameter <code>e</code> is not
         * used and may be null.</p>
         */
        public void actionPerformed(ActionEvent e) {
            synchronized (annotationLock) {
                processAnnotationEnded();
                processAnnotationStarted();
                tree.removeTreeSelectionListener(selListener);
                DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
                DefaultMutableTreeNode leaf = root.getFirstLeaf();
                boolean found = false;
                while ((leaf != null) && !found) {
                    if (leaf.getUserObject() == target) {
                        TreePath path = new TreePath(leaf.getPath());
                        tree.setSelectionPath(path);
                        tree.scrollPathToVisible(path);
                        found = true;
                    } else {
                        leaf = leaf.getNextLeaf();
                    }
                }
                tree.addTreeSelectionListener(selListener);
                currentTarget = target;
                processAnnotationTargetSet();
            }
        }
    }
    
    /**
     * <p>Selection listener for the ontology tree. When a leaf is selected,
     * the corresponding target action is performed.</p>
     */
    private class OntologyTreeSelectionListener implements TreeSelectionListener {
        
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getNewLeadSelectionPath().getLastPathComponent();
            if (node.isLeaf() && (node.getUserObject() instanceof NOMElement)) {
                NOMElement elem = (NOMElement)node.getUserObject();
                Action action = actionMap.get(elem);
                action.actionPerformed(null);
            }
        }
    }
}
