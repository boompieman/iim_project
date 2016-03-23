package net.sourceforge.nite.tools.videolabeler;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

import net.sourceforge.nite.gui.util.NOMWriteElementContainer;
import net.sourceforge.nite.gui.util.SetCommentAction;
import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteAnnotation;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.nxt.NOMObjectModelElement;

/**
 * <p>An annotation control panel displays the current annotation and contains
 * two buttons that allow the user to delete the current annotation or to edit
 * a comment. Initially the current annotation is unassigned.</p>
 *
 * <p>The panel is part of an annotation frame (see
 * {@link AnnotationFrame AnnotationFrame}), which also contains an annotation
 * area (see {@link AnnotationArea AnnotationArea}) and a target control panel
 * (see {@link TargetControlPanel TargetControlPanel}). The panel is an
 * annotation listener and an annotation selection listener, so it can keep
 * track of the current annotation.</p>
 *
 * <p>The current annotation is changed either when the user starts a new
 * annotation in the target control panel (notification through the annotation
 * listener interface) or an existing annotation is selected (notification
 * through the annotation selection listener interface).</p>
 *
 * <p>When the panel receives an annotationTargetSet event through the
 * annotation listener interface, it calls the setTarget() method of the
 * specified target control panel and then it retrieves the target label
 * through the annotation layer that is specified at construction. The label
 * will be displayed in the panel.</p>
 *
 * <p>When the panel receives an annotationEnded event, it inserts the
 * annotation into the corpus, making sure that the annotations remain
 * non-overlapping. Note that the incomplete annotation has already been
 * added to the corpus after the annotationStarted event.</p>
 */
public class AnnotationControlPanel extends JPanel implements
    AnnotationListener, AnnotationSelectionListener, NOMWriteElementContainer {

    private JLabel startLabel;
    private JLabel targetLabel;
    private JLabel endLabel;
    private JLabel commentLabel;
    private Action deleteAction;
    private Action finishAction;
    private Action setCommentAction;

    private NOMWriteElement currentAnnotation = null; 
    private double currentStartTime = Double.NaN;
    private String currentLabel = null;
    private double currentEndTime = Double.NaN;
    private boolean finishEnabled = true;
    private boolean deleteEnabled = true;
    private static final int TIME_FORMAT_SECONDS = 1;
    private static final int TIME_FORMAT_MINUTES = 2;
    
    private int timeformat = TIME_FORMAT_SECONDS;

    private NAgent agent;
    private AnnotationLayer layer;
	
    /**
     * <p>Constructs a new annotation control panel for the specified
     * annotation layer and agent. The agent parameter may be null if the layer
     * belongs to an interaction coding rather than an agent coding</p>
     *
     * @param agent an agent (if the layer belongs to an agent coding) or null
     * (if the layer belongs to an interaction coding)
     * @param layer an annotation layer
     */
    public AnnotationControlPanel(NAgent agent, AnnotationLayer layer) {
        super();
        this.agent = agent;
        this.layer = layer;
        createGui();
        refreshView();
    }

    /**
     * <p>Creates the GUI components for this panel.</p>
     */
    private void createGui() {
        Box selectionBox = Box.createHorizontalBox();
        Box textLabelBox = Box.createVerticalBox();
        JLabel startTextLabel = new JLabel("Start: ");
        JLabel targetTextLabel = new JLabel("Target: ");
        JLabel endTextLabel = new JLabel("End: ");
        JLabel commentTextLabel = new JLabel("Comment: ");
        Box labelBox = Box.createVerticalBox();
        startLabel = new JLabel();
        targetLabel = new JLabel();
        endLabel = new JLabel();
        commentLabel = new JLabel();
        textLabelBox.add(startTextLabel);
        textLabelBox.add(targetTextLabel);
        textLabelBox.add(endTextLabel);
        textLabelBox.add(commentTextLabel);
        labelBox.add(startLabel);
        labelBox.add(targetLabel);
        labelBox.add(endLabel);
        labelBox.add(commentLabel);
        selectionBox.add(textLabelBox);
        selectionBox.add(labelBox);
        setLayout(new BorderLayout());
        add(selectionBox,BorderLayout.WEST);
        deleteAction = new DeleteAction();
        deleteAction.setEnabled(false);
        setCommentAction = new SetCommentAction("Edit Comment", this);
        setCommentAction.setEnabled(false);
        finishAction = new FinishAction();
        finishAction.setEnabled(false);
        JPanel buttonpanel = new JPanel();
        JButton deleteButton = new JButton(deleteAction);
        JButton setCommentButton = new JButton(setCommentAction);
        JButton finishButton = new JButton(finishAction);
        buttonpanel.add(deleteButton);
        buttonpanel.add(setCommentButton);
        buttonpanel.add(finishButton);
        add(buttonpanel,BorderLayout.SOUTH);
	String timeformatstring = CSLConfig.getInstance().getPreferredTimeDisplay();
	if (timeformatstring.equals("minutes")) 
	    timeformat=TIME_FORMAT_MINUTES;
    }
    
    /**
     * <p>Enables or disables the finish action. If <code>enabled</code> is
     * true, this control panel will automatically enable/disable the finish
     * action depending on the currently selected annotation. If
     * <code>enabled</code> is false, the finish action will always be
     * disabled.</p>
     *
     * @param enabled true if the finish action should automatically be
     * enabled/disabled depending on the currently selected annotation, false
     * if it should always be disabled
     */
    public void setFinishActionEnabled(boolean enabled) {
        finishEnabled = enabled;
    }


    /** 
     * define the format of the time display 
     */
    public void setTimeDisplay(int displayType) {
	if (displayType==TIME_FORMAT_MINUTES || displayType==TIME_FORMAT_SECONDS) {
	    timeformat=displayType;
	}
    }

    NumberFormat nf = NumberFormat.getNumberInstance();
    DecimalFormat nnFormat = new DecimalFormat("00");

    /**
     * Takes a number of seconds and returns a string in the format of h:mm:ss
     */
    private String formatTime(double seconds) {
	if (timeformat==TIME_FORMAT_MINUTES) {
	    double s = seconds%60;
	    int m = (int)((seconds/60)%60);
	    int h = (int)(seconds/3600);
	    nf.setMaximumFractionDigits(2);
	    nf.setMinimumIntegerDigits(2);
	    return h + ":" + nnFormat.format(m) + ":" + nf.format(s);
	} else {
	    return nf.format(seconds);
	}
    }    


    /**
     * <p>Enables or disables the delete action. If <code>enabled</code> is
     * true, this control panel will automatically enable/disable the delete
     * action depending on the currently selected annotation. If
     * <code>enabled</code> is false, the delete action will always be
     * disabled.</p>
     *
     * @param enabled true if the delete action should automatically be
     * enabled/disabled depending on the currently selected annotation, false
     * if it should always be disabled
     */
    public void setDeleteActionEnabled(boolean enabled) {
        deleteEnabled = enabled;
    }

    /**
     * <p>Updates the GUI components for the current annotation.</p>
     */
    private void refreshView() {
        Document doc = Document.getInstance();
        //NumberFormat nf = NumberFormat.getNumberInstance();
        //nf.setMaximumFractionDigits(2);
        if (currentAnnotation != null) {
            if (Double.isNaN(currentStartTime))
                startLabel.setText("--");
            else
                startLabel.setText(formatTime(currentStartTime));
            if (currentLabel == null)
                targetLabel.setText("--");
            else
                targetLabel.setText(currentLabel);
            if (Double.isNaN(currentEndTime))
                endLabel.setText("--");
            else
                endLabel.setText(formatTime(currentEndTime));
            if (currentAnnotation.getComment() == null)
                commentLabel.setText("--");
            else
                commentLabel.setText(currentAnnotation.getComment());
        } else {
            startLabel.setText("");
            targetLabel.setText("");
            endLabel.setText("");
            commentLabel.setText("");
        }
        deleteAction.setEnabled(deleteEnabled && currentAnnotation != null);
        setCommentAction.setEnabled(currentAnnotation != null);
        finishAction.setEnabled(finishEnabled && currentAnnotation != null &&
                Double.isNaN(currentEndTime));
    }

    ///////////////////////////////////////////////////////////////////////
    // AnnotationListener methods

    /**
     * <p>Creates a new annotation and makes it the current annotation. The
     * current time is set as the start time of the new annotation. This control
     * panel is updated for the new annotation. If the new annotation cannot be
     * created, an error is printed to standard output and this method returns
     * false. In that case the current annotation will be unassigned.</p>
     *
     * @param panel the target control panel from which the annotation was
     * started
     * @return true if the new annotation was created successfully, false
     * otherwise
     */
    public boolean annotationStarted(TargetControlPanel panel) {
        Document doc = Document.getInstance();
        currentAnnotation = null;
        currentStartTime = doc.getClock().getSystemTime();
        currentEndTime = Double.NaN;
        currentLabel = null;
        try {
            currentAnnotation = doc.createAnnotation(
                    layer.getCodeElement().getName(),currentStartTime,agent);
        } catch (NOMException ex) {
            System.out.println("ERROR: Could not create annotation: " + ex.getMessage());
            currentAnnotation = null;
            currentStartTime = Double.NaN;
            currentEndTime = Double.NaN;
            currentLabel = null;
            refreshView();
            return false;
        }
        refreshView();
        return true;
    }

    /**
     * <p>Calls setTarget() of the specified target control panel, so the
     * target of the current annotation is set. Then updates this control
     * panel, so it shows the label of the current annotation's target. If
     * the current annotation is unassigned, this method has no effect and
     * returns false. If an error occurs while setting the target, this method
     * prints an error to standard output and returns false.</p>
     */
    public boolean annotationTargetSet(TargetControlPanel panel) {
        if (currentAnnotation == null)
            return false;
        boolean result = panel.setTarget(currentAnnotation);
        if (!result)
            System.out.println("ERROR: Could not set target of current annotation");
        currentLabel = layer.getLabel(currentAnnotation);
        double endTime = currentAnnotation.getEndTime();
        if (Double.isNaN(endTime))
            endTime = currentAnnotation.getStartTime();
        Document.getInstance().notifyObservers(currentAnnotation.getStartTime(),
                endTime);
        refreshView();
        return result;
    }

    /**
     * See annotationEnded(TargetControlPanel panel).
     */
    private boolean annotationEnded() {
        double currentTime = Document.getInstance().getClock().getSystemTime();
        if ((currentAnnotation == null) || !Double.isNaN(currentEndTime)) {
            currentAnnotation = null;
            currentStartTime = Double.NaN;
            currentLabel = null;
            currentEndTime = Double.NaN;
            return false;
        }
        if (currentTime < currentStartTime) {
            NOMObjectModelElement nome = new NOMObjectModelElement(currentAnnotation);
            nome.deleteElement();
            currentAnnotation = null;
            currentStartTime = Double.NaN;
            currentLabel = null;
            currentEndTime = Double.NaN;
            return false;
        }
        boolean result = true;
        Document doc = Document.getInstance();
        try {
            currentAnnotation.setEndTime(currentTime);
            doc.insertAnnotation(layer,currentAnnotation);
        } catch (Throwable ex) {
            System.out.println("ERROR: Could not insert annotation into corpus: " + ex.getMessage());
            result = false;
        }
        if (result == false) {
            try {
                currentAnnotation.setEndTime(Double.NaN);
            } catch (Throwable ex) {}
            double endTime = currentAnnotation.getEndTime();
            if (Double.isNaN(endTime))
                endTime = currentAnnotation.getStartTime();
            doc.notifyObservers(currentAnnotation.getStartTime(),endTime);
        }
        currentAnnotation = null;
        currentStartTime = Double.NaN;
        currentLabel = null;
        currentEndTime = Double.NaN;
        refreshView();
        return result;
    }

    /**
     * <p>Sets the current time as the end time of the current annotation and
     * inserts the annotation into the {@link Document Document}. If the current
     * annotation is unassigned, if it already has an end time, or if the end
     * time is not greater than the start time, this method has no effect and
     * returns false. If an error occurs while setting the end time or while
     * inserting the annotation, an error is printed to standard output and this
     * method returns false. After calling this method, the current annotation
     * will be unassigned.</p>
     */
    public boolean annotationEnded(TargetControlPanel panel) {
        return annotationEnded();
    }
    
    ///////////////////////////////////////////////////////////////////////
    // AnnotationSelectionListener method

    /**
     * <p>Sets the selected annotation as the current annotation and updates
     * this control panel.</p>
     */
    public void annotationSelected(NOMObjectModelElement annotation) {
        if (annotation != null) {
            currentAnnotation = (NOMWriteElement)annotation.getElement();
            currentStartTime = currentAnnotation.getStartTime();
            currentLabel = layer.getLabel(currentAnnotation);
            currentEndTime = currentAnnotation.getEndTime();
        } else {
            currentAnnotation = null;
            currentStartTime = Double.NaN;
            currentLabel = null;
            currentEndTime = Double.NaN;
        }
        refreshView();
    }
	
    ///////////////////////////////////////////////////////////////////////
    // NOMWriteElementContainer method
    
    /**
     * <p>Returns the current element. Called after "set comment".</p>
     */
    public NOMWriteElement getCurrentElement() {
        return currentAnnotation;
    }

    /**
     * <p>This action deletes the current annotation from the corpus and
     * updates the control panel. After this, the current annotation will be
     * unassigned.</p>
     */
    private class DeleteAction extends AbstractAction {
        public DeleteAction() {
            super("Delete");
        }

        public void actionPerformed(ActionEvent e) {
            if (currentAnnotation != null) {
                Document doc = Document.getInstance();
                try {
                    NOMObjectModelElement nome = new NOMObjectModelElement(currentAnnotation);
                    doc.deleteAnnotation(layer,nome);
                } catch (Throwable ex) {
                    System.out.println("ERROR: Could not delete annotation: " + ex.getMessage());
                }
                currentAnnotation = null;
                currentStartTime = Double.NaN;
                currentLabel = null;
                currentEndTime = Double.NaN;
                refreshView();
            }
        }
    }
    
    /**
     * <p>This action finishes the current annotation by setting its end time
     * to the current time.</p>
     */
    private class FinishAction extends AbstractAction {
        public FinishAction() {
            super("Finish");
        }
        
        public void actionPerformed(ActionEvent e) {
            annotationEnded();
        }
    }
}
