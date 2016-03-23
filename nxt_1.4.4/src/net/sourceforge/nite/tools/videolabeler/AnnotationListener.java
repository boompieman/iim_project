package net.sourceforge.nite.tools.videolabeler;

/**
 * <p>An annotation listener is notified when the start or end of an annotation
 * is marked or when the target of the current annotation is set. These events
 * are fired by a target control panel (see
 * {@link TargetControlPanel TargetControlPanel}) and handled by an
 * annotation control panel (see
 * {@link AnnotationControlPanel AnnotationControlPanel}).</p>
 *
 * <p>A target control panel that fires the events should fire them in this
 * order: annotationStarted, annotationTargetSet, annotationEnded.</p>
 */
public interface AnnotationListener {

    /**
     * <p>Called when the start of a new annotation is marked. When this event
     * occurs, the new annotation should be set as the current annotation of
     * the specified target control panel's annotation frame and the current
     * time should be set as the start time of the new annotation.</p>
     *
     * @param panel the target control panel that fired the event
     * @return true if the new annotation was created successfully, false
     * otherwise
     */
    public boolean annotationStarted(TargetControlPanel panel);
	
    /**
     * <p>Called when the target of the current annotation is set. When this
     * event occurs, the method setTarget() of the specified target control
     * panel should be called with the current annotation.</p>
     *
     * @param panel the target control panel that fired the event
     * @return true if the target of the current annotation was set
     * successfully, false otherwise
     */
    public boolean annotationTargetSet(TargetControlPanel panel);

    /**
     * <p>Called when the end of the current annotation is marked. When this
     * event occurs, the current time should be set as the end time of the
     * current annotation and the annotation should be inserted into the
     * {@link Document Document}.</p>
     *
     * @param panel the target control panel that fired the event
     * @return true if the annotation was added to the corpus successfully,
     * false otherwise
     */
    public boolean annotationEnded(TargetControlPanel panel);
}
