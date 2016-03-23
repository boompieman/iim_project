package net.sourceforge.nite.tools.videolabeler;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.meta.NLayer;

/**
 * <p>An annotation frame consists of two or three parts: an
 * {@linkplain AnnotationArea annotation area} at the top (only if
 * {@link AnnotationLayer#showAnnotationArea() showAnnotationArea} of the
 * specified annotation layer is true), an {@linkplain AnnotationControlPanel
 * annotation control panel} in the middle and a {@linkplain TargetControlPanel
 * target control panel} at the bottom. These two or three parts are separated
 * by splitters. This class creates the components of the annotation frame for
 * the annotations of an agent and layer. The three parts are connected through
 * the annotation listener and annotation selection listener interfaces.</p>
 *
 * <p>When the annotation frame is hidden or if the annotation area is hidden
 * (using the splitter), the frame will call <code>setVisible(false)</code>
 * on the hidden frame components, so the components can handle that event.</p>
 */
public class AnnotationFrame extends JInternalFrame {
    
    public static final int MINIMUM_WIDTH = 150;
    public static final int MINIMUM_HEIGHT = 250;
    
    private NAgent agent;
    private AnnotationLayer layer;
    private AnnotationArea annotationArea = null;
    private AnnotationControlPanel annotationControlPanel;
    private TargetControlPanel targetControlPanel;
    private ObservableSplitPane topSplitter = null;
    private ObservableSplitPane bottomSplitter;
    
    private int resetTopLocation = -1;
	
    /**
     * <p>Constructs a new annotation frame for the specified layer and agent.
     * The agent parameter may be null if the layer belongs to an interaction
     * coding rather than an agent coding.</p>
     *
     * @param agent an agent (if the layer belongs to an agent coding) or null
     * (if the layer belongs to an interaction coding)
     * @param layer an annotation layer
     * @exception Exception if the target control panel could not be created
     */
    public AnnotationFrame(NAgent agent, AnnotationLayer layer) throws Exception {
        super("",true,false,true,true);
        this.agent = agent;
        this.layer = layer;
        setSize(300,300);
        setMinimumSize(new Dimension(MINIMUM_WIDTH,MINIMUM_HEIGHT));
        String title = layer.getNLayer().getName();
        if (agent != null)
            title = agent.getShortName() + " - " + title;
        setTitle(title);
        createGui();
        if (topSplitter != null) {
            topSplitter.addSplitPaneListener(new SplitPaneListener() {
                public void dividerMoved(JSplitPane splitter) {
                    int loc = topSplitter.getDividerLocation();
                    if (loc < 20)
                        annotationArea.setVisible(false);
                    else
                        annotationArea.setVisible(true);
                }
            });
        }
        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameOpened(InternalFrameEvent e) {
                int height = getHeight();
                if (topSplitter != null)
                    topSplitter.setDividerLocation((height-90)/2);
                bottomSplitter.setDividerLocation(90);
            }

            public void internalFrameClosed(InternalFrameEvent e) {
                if (annotationArea != null)
                    annotationArea.setVisible(false);
                annotationControlPanel.setVisible(false);
                targetControlPanel.setVisible(false);
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (topSplitter != null) {
                    int topLoc = topSplitter.getDividerLocation();
                    if (resetTopLocation > -1 && resetTopLocation != topLoc)
                    topSplitter.setDividerLocation(resetTopLocation);
                }
                resetTopLocation = -1;
                bottomSplitter.setDividerLocation(90);
            }
        });
    }
    
    //////////////////////////////////////////////////////////////////////////
    // setSize and setBounds are overridden, so the location of the top divider
    // can be saved (and possibly changed) before the frame is actually resized
    // (which may change the location of the top divider automatically). The
    // saved location is reset after the resize. This happens in the component
    // listener (registered in the constructor).

    public void setSize(int width, int height) {
        resetTopLocation = -1;
        if (topSplitter != null)
            resetTopLocation = topSplitter.getDividerLocation();
        int diff = height - getHeight();
        if (resetTopLocation >= 20)
            resetTopLocation += diff/2;
        super.setSize(width,height);
    }
    
    public void setSize(Dimension d) {
        resetTopLocation = -1;
        if (topSplitter != null)
            resetTopLocation = topSplitter.getDividerLocation();
        int diff = d.height - getHeight();
        if (resetTopLocation >= 20)
            resetTopLocation += diff/2;
        super.setSize(d);
    }
    
    public void setBounds(int x, int y, int width, int height) {
        resetTopLocation = -1;
        if (topSplitter != null)
            resetTopLocation = topSplitter.getDividerLocation();
        int diff = height - getHeight();
        if (resetTopLocation >= 20)
            resetTopLocation += diff/2;
        super.setBounds(x,y,width,height);
    }
    
    public void setBounds(Rectangle r) {
        resetTopLocation = -1;
        if (topSplitter != null)
            resetTopLocation = topSplitter.getDividerLocation();
        int diff = r.height - getHeight();
        if (resetTopLocation >= 20)
            resetTopLocation += diff/2;
        super.setBounds(r);
    }
    
    /**
     * <p>Returns the agent of this annotation frame. If the annotation layer
     * belongs to an interaction coding, this method returns null.</p>
     *
     * @return the agent of this annotation frame or null
     */
    public NAgent getAnnotationAgent() {
        return agent;
    }

    /**
     * <p>Returns the annotation area of this frame. If the annotation layer
     * says that the annotation area should not be shown, this method will
     * return null.</p>
     *
     * @return the annotation area or null
     */
    public AnnotationArea getAnnotationArea() {
        return annotationArea;
    }
    
    /**
     * <p>Returns the annotation control panel of this frame.</p>
     *
     * @return the annotation control panel
     */
    public AnnotationControlPanel getAnnotationControlPanel() {
        return annotationControlPanel;
    }
    
    /**
     * <p>Returns the target control panel. If an error occurred while creating
     * the target control panel, this method will return null.</p>
     *
     * @return the target control panel or null
     */
    public TargetControlPanel getTargetControlPanel() {
        return targetControlPanel;
    }
	
    /**
     * <p>Returns the layer of this annotation frame.</p>
     *
     * @return the layer of this annotation frame
     */
    public AnnotationLayer getAnnotationLayer() {
        return layer;
    }
	
    /**
     * <p>Creates the components of this frame and connects them through
     * listener interfaces.</p>
     *
     * @exception Exception if the target control panel could not be created
     */
    private void createGui() throws Exception {
        if (layer.showAnnotationArea())
            annotationArea = new AnnotationArea(agent,layer,true);
        annotationControlPanel = new AnnotationControlPanel(agent,layer);
        targetControlPanel = layer.createTargetControlPanel(this,agent);
        if (targetControlPanel == null) {
            if (annotationArea != null)
                annotationArea.setVisible(false);
            annotationControlPanel.setVisible(false);
            throw new Exception("Target control panel could not be created");
        }
        if (annotationArea != null) {
            annotationArea.addAnnotationSelectionListener(annotationControlPanel);
            annotationArea.addAnnotationSelectionListener(targetControlPanel);
        }
        targetControlPanel.addAnnotationListener(annotationControlPanel);
        if (annotationArea != null)
            targetControlPanel.addAnnotationListener(annotationArea);

        bottomSplitter = new ObservableSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomSplitter.setLeftComponent(annotationControlPanel);
        bottomSplitter.setRightComponent(targetControlPanel);
        bottomSplitter.setContinuousLayout(true);
        bottomSplitter.setOneTouchExpandable(true);
        bottomSplitter.setResizeWeight(0.0);
        
        if (annotationArea != null) {
            topSplitter = new ObservableSplitPane(JSplitPane.VERTICAL_SPLIT);
            topSplitter.setLeftComponent(new JScrollPane(annotationArea));
            topSplitter.setRightComponent(bottomSplitter);
            topSplitter.setContinuousLayout(true);
            topSplitter.setOneTouchExpandable(true);
            topSplitter.setResizeWeight(0.0);
            getContentPane().add(topSplitter);
        } else {
            getContentPane().add(bottomSplitter);
        }
    }
}
