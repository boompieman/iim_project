package net.sourceforge.nite.tools.videolabeler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import net.sourceforge.nite.gui.util.AgentConfiguration;
import net.sourceforge.nite.gui.util.ValueColourMap;
import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.meta.NPointer;
import net.sourceforge.nite.meta.NSignal;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.NOMPointer;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWritePointer;
import net.sourceforge.nite.nstyle.NConstants;
import org.w3c.dom.Node;

/**
 * <p>A label target control panel can be used with a label annotation layer
 * (see {@link LabelAnnotationLayer LabelAnnotationLayer}).</p>
 */
public class AgentTargetControlPanel extends TargetControlPanel {
    private static final double BUTTON_PROPORTION = 3.0;
    private HashMap targetButtons = new HashMap();
    private LabelAnnotationLayer labelLayer;
    private NOMElement currentTarget = null;
    private AgentConfiguration agentConfig;
    private JComboBox signalCombo;
    private JPanel buttonPanel;
    private String currentSignal = null;

    public AgentTargetControlPanel(AnnotationFrame frame, AnnotationLayer layer,
            Node layerInfo, NAgent agent) {
        super(frame,layer,layerInfo,agent);
        Document doc = Document.getInstance();
        agentConfig = new AgentConfiguration(doc.getCorpus(),doc.getObservation().getShortName());
        labelLayer = (LabelAnnotationLayer)layer;
        createGUI();
    }
	
    private void createGUI() {
        signalCombo = new JComboBox();
        Document doc = Document.getInstance();
        List signals = doc.getSignals();
        Iterator it = signals.iterator();
        while (it.hasNext()) {
            NSignal sig = (NSignal)it.next();
            signalCombo.addItem(sig.getName());
        }
        signalCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                signalSelected();
            }
        });
        currentSignal = (String)signalCombo.getSelectedItem();
        setLayout(new BorderLayout());
        add(signalCombo,BorderLayout.NORTH);
        buttonPanel = new JPanel();
        add(buttonPanel,BorderLayout.CENTER);
        createButtons();
        layoutButtons();
    }

    private void createButtons() {
        List targets = labelLayer.getTargets();
        ValueColourMap colourMap = labelLayer.getColourMap();
        Iterator it = targets.iterator();
        while (it.hasNext()) {
            NOMElement target = (NOMElement)it.next();
            String label = labelLayer.getTargetName(target);
            if (label != null) {
                TargetAction action = new TargetAction(label,target);
                JButton button = new JButton(action);
                button.setBackground(colourMap.getValueBackColour(target));
                targetButtons.put(label,button);
            }
        }
    }

    /**
     * <p>Converts an agent name to the name of an agent target. It is assumed
     * that the agents are named "p<i>x</i>" and the agent targets are named
     * "Person <i>x</i>".</p>
     *
     * @param agentName the name of an agent ("p<i>x</i>")
     * @return the name of the agent target ("Person <i>x</i>")
     */
    public String agentToAgentTarget(String agentName) {
        String agentIndex = agentName.substring(1);
        return "Person " + agentIndex;
    }

    private void layoutButtons() {
        buttonPanel.removeAll();
        Dimension dim = agentConfig.getDimension(currentSignal);
        buttonPanel.setLayout(new GridLayout(dim.height,dim.width));
        for (int y = 0; y < dim.height; y++) {
            for (int x = 0; x < dim.width; x++) {
                String agentName = agentConfig.getAgentAt(currentSignal,x,y);
                if (agentName == null) {
                    buttonPanel.add(new JPanel());
                } else if (agentName.equals(agent.getShortName())) {
                    JButton noTargetButton = (JButton)targetButtons.get("No target");
                    buttonPanel.add(noTargetButton);
                } else {
                    String gazeTarget = agentToAgentTarget(agentName);
                    JButton button = (JButton)targetButtons.get(gazeTarget);
                    buttonPanel.add(button);
                }
            }
        }
        validate();
    }

    public boolean setTarget(NOMWriteElement annotation) {
        Document doc = Document.getInstance();
        NPointer pointer = ((LabelAnnotationLayer)layer).getPointer();
            NOMPointer point = new NOMWritePointer(doc.getCorpus(),pointer.getRole(),annotation,currentTarget);
            try {
                annotation.addPointer(point);
            } catch (NOMException ex) {
                System.out.println("ERROR: Could not add pointer to annotation: " + ex.getMessage());
                return false;
            }
        return true;
    }

    private void signalSelected() {
        currentSignal = (String)signalCombo.getSelectedItem();
        layoutButtons();
    }

    private class TargetAction extends AbstractAction {
        private NOMElement target;

        public TargetAction(String label, NOMElement target) {
            super();
            this.target = target;
            putValue(Action.NAME,label);
        }

        public void actionPerformed(ActionEvent e) {
            processAnnotationEnded();
            processAnnotationStarted();
            currentTarget = target;
            processAnnotationTargetSet();
        }
    }
}
