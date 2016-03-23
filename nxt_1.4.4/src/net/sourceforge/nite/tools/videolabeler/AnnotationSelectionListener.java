package net.sourceforge.nite.tools.videolabeler;

import net.sourceforge.nite.nxt.NOMObjectModelElement;

/**
 * <p>An annotation selection listener is notified when an annotation is
 * selected in the annotation area (see {@link AnnotationArea AnnotationArea})
 * of an annotation frame (see {@link AnnotationFrame AnnotationFrame}). This
 * means that the selected annotation should become the current annotation of
 * the annotation frame. It should cancel any unfinished annotation made by
 * the target control panel (see
 * {@link TargetControlPanel TargetControlPanel}).</p>
 */
public interface AnnotationSelectionListener {

    /**
     * <p>Called when an existing annotation has been selected.</p>
     *
     * @param annotation the selected annotation
     */
    public void annotationSelected(NOMObjectModelElement annotation);
}
