package net.sourceforge.nite.tools.videolabeler;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;

import org.w3c.dom.Node;

/**
 * <p>A Feeltrace target control panel can be used with a Feeltrace annotation
 * layer (see {@link FeeltraceAnnotationLayer FeeltraceAnnotationLayer}). The
 * main component of the panel is a {@link FeeltraceCircle FeeltraceCircle},
 * in which annotations can be made and which can display existing annotations.
 * A checkbox named "Replay" is added under the circle. If it is checked
 * (default), existing annotations will be displayed in the circle. At the
 * bottom of the panel is a {@link FeeltraceTimeLine FeeltraceTimeLine}, which
 * is a bar with an overview of the annotations over the entire timeline. The
 * timeline will be updated as annotations are made without searching the
 * corpus, which may lead to an inaccurate view. When the user clicks the
 * refresh button at the right of the timeline, the corpus will be queried so
 * the timeline provides an accurate view.</p>
 */
public class FeeltraceTargetControlPanel extends TargetControlPanel {
    
    private FeeltraceAnnotationLayer feeltraceLayer;
    private FeeltraceCircle circle;
    private FeeltraceTimeLine timeline;
    private JCheckBox replayCheck;
    private boolean showLabels = true;
    private boolean clickAnnotation = false;
    private double currentStart = Double.NaN;
    private Point2D currentEmotion = null;

    /**
     * <p>Constructs a new Feeltrace target control panel for the specified
     * layer and agent. The agent parameter may be null if the layer belongs to
     * an interaction coding rather than an agent coding. The layerInfo
     * parameter is the layerinfo element from the configuration file. It may
     * have the following two optional attributes:</p>
     *
     * <ul>
     * <li>showlabels (default true): true if the Feeltrace circle should show
     * labels for some predefined emotions</li>
     * <li>clickannotation (default false): true if the user should click the
     * mouse button to start and end annotating, false if the user should keep
     * the mouse button pressed while annotating</li>
     * </ul>
     *
     * @param frame the annotation frame that contains this control panel
     * @param layer an annotation layer
     * @param layerInfo the layerinfo element from the configuration file
     * @param agent an agent (if the layer belongs to an agent coding) or null
     * (if the layer belongs to an interaction coding)
     */
    public FeeltraceTargetControlPanel(AnnotationFrame frame, AnnotationLayer layer,
            Node layerInfo, NAgent agent) {
        super(frame,layer,layerInfo,agent);
        this.feeltraceLayer = (FeeltraceAnnotationLayer)layer;
        CSLConfig cfg = CSLConfig.getInstance();
        String showlabels = cfg.getAttributeValue(layerInfo,"showlabels");
        if (showlabels != null)
            showLabels = Boolean.valueOf(showlabels).booleanValue();
        String clickannotation = cfg.getAttributeValue(layerInfo,"clickannotation");
        if (clickannotation != null)
            clickAnnotation = Boolean.valueOf(clickannotation).booleanValue();
        circle = new FeeltraceCircle(this);
        setLayout(new BorderLayout());
        add(circle,BorderLayout.CENTER);
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(2,1));
        replayCheck = new JCheckBox(new ToggleReplayAction());
        replayCheck.setSelected(true);
        JPanel barPanel = new JPanel();
        barPanel.setLayout(new BorderLayout());
        timeline = new FeeltraceTimeLine(this);
        barPanel.add(timeline,BorderLayout.CENTER);
        barPanel.add(new JButton(new RefreshAction()),BorderLayout.EAST);
        controlPanel.add(replayCheck);
        controlPanel.add(barPanel);
        add(controlPanel,BorderLayout.SOUTH);
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                circle.setVisible(true);
                timeline.setVisible(true);
            }
            
            public void componentHidden(ComponentEvent e) {
                circle.setVisible(false);
                timeline.setVisible(false);
            }
        });
    }
    
    /**
     * <p>Determines whether the Feeltrace circle should show labels for
     * some predefined emotions.</p>
     *
     * @return true if the Feeltrace circle should show labels, false otherwise
     */
    public boolean showLabels() {
        return showLabels;
    }

    /**
     * <p>Determines whether the user should click the mouse button to start
     * and end annotating, or whether the user should keep the mouse button
     * pressed while annotating.</p>
     *
     * @return true if the user should click to start and end annotating, false
     * if the user should keep the mouse button pressed while annotating
     */
    public boolean clickAnnotation() {
        return clickAnnotation;
    }

    /**
     * <p>Returns the Feeltrace annotation layer.</p>
     *
     * @return the Feeltrace annotation layer
     */
    public FeeltraceAnnotationLayer getFeeltraceLayer() {
        return feeltraceLayer;
    }
    
    /**
     * <p>Calls the superclass method and saves the current time as the
     * start time of the current annotation (used to update the timeline
     * without searching the corpus).</p>
     */
    protected boolean processAnnotationStarted() {
        boolean result = super.processAnnotationStarted();
        if (result)
            currentStart = Document.getInstance().getClock().getSystemTime();
        else
            currentStart = Double.NaN;
        return result;
    }

    /**
     * <p>Calls the superclass method. If a start time and emotion for the
     * current annotation were saved, the timeline will be updated. Note that
     * this may not be accurate, because the annotation may be changed when it
     * is inserted into the corpus, in particular if
     * {@link CSLConfig#makeContinuous() CSLConfig.makeContinuous()} returns
     * true.</p>
     */
    protected boolean processAnnotationEnded() {
        boolean result = super.processAnnotationEnded();
        if (result && currentEmotion != null) {
            timeline.showAnnotation(currentStart,
                    Document.getInstance().getClock().getSystemTime(),
                    currentEmotion);
        }
        currentStart = Double.NaN;
        currentEmotion = null;
        return result;
    }
    
    /**
     * <p>Sets the target of the current annotation. This is handled by the
     * Feeltrace circle, but this method saves the emotion of the current
     * annotation (used to update the timeline without searching the
     * corpus).</p>
     *
     * @param annotation the current annotation
     * @return true if the target was set successfully, false otherwise
     */
    public boolean setTarget(NOMWriteElement annotation) {
        boolean result = circle.setTarget(annotation);
        if (result && !Double.isNaN(currentStart)) {
            currentEmotion = circle.getCurrentEmotion();
        } else {
            currentStart = Double.NaN;
            currentEmotion = null;
        }
        return result;
    }
    
    /**
     * <p>Action for the refresh button. Updates the timeline.</p>
     */
    private class RefreshAction extends AbstractAction {
        public RefreshAction() {
            super("Refresh");
        }
        
        public void actionPerformed(ActionEvent e) {
            timeline.repaint();
        }
    }
    
    /**
     * <p>Action for the replay checkbox. Enables or disables replay in the
     * Feeltrace circle.</p>
     */
    private class ToggleReplayAction extends AbstractAction {
        public ToggleReplayAction() {
            super("Replay");
        }
        
        public void actionPerformed(ActionEvent e) {
            circle.setReplay(replayCheck.isSelected());
        }
    }
}
