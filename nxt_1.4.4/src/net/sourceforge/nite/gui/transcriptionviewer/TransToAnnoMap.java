/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.transcriptionviewer;

import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nxt.*;
import java.util.Set;
import java.util.List;

/**
 * TransToAnnoMap implementations define the relations between transcription elements (e.g. words) in a
 * corpus and annotation elements defined on the transcription (e.g. dialogue acts or named entities).
 * <p>
 * The single most important class in this package is the 
 * {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView NTranscriptionView} class.
 * It has been designed to display speech transcriptions from a corpus of interactions, display extra
 * information annotated on these transcriptions (e.g. dialogue acts or adjacency pairs)
 * and provide a variety of GUI interface methods for selecting transcriptions and/or
 * annotations.
 * <p>
 * In order to know where in the transcription text a certain annotation element should be displayed
 * and which annotation elements are bound to e.g. a selected word in the transcription, the relation between
 * annotation elements and transcription should be defined. This relation is not always straightforward.
 * Often it is just a parent-child or ancestor-descendant relation, but sometimes the relation is through
 * complex pointer structures.
 * <p>
 * TransToAnnoMap implementations define this relation. A {@link net.sourceforge.nite.gui.transcriptionviewer.DisplayStrategy
 * DisplayStrategy} uses this implementation to determine which Transcription elements (and therefore NTextElements)
 * should be decorated because they represent the transcription elements for an annotation elements.
 * {@link net.sourceforge.nite.gui.transcriptionviewer.TransToAnnoMap#ghgjkhgf TransToAnnoMap.hgfdasdfg}
 * <p>
 * @@@The NTranscriptionView or AnnotationSelectDelegate (Which of the two?) Uses several TransToAnnoMaps to determine 
 * which annotation elements are selected / selectable at a certain moment.
 * <p>
 * @@@DOCUMENT AN EXAMPLE OF THE USE OF THIS CLASS FOR E.G. DIALOGUE ACTS DISPLAY AND SELECTION
 * <p>
 * @author Dennis Reidsma 
 */
public interface TransToAnnoMap {
 
    /**
     * Given ANY annotation element defined on a transcription, either directly or indirectly,
     * return the transcripotion elements to which the element pertains.
     * <p>
     * The effect of this method is very dependent on the application and the structure of the corpus.
     */
    public Set getTransElementsForAnnotationElement(NOMElement nme);

    /**
     * Given an NOMElement, return all potential annotation elements related to it.
     * CAN BE MORE THAN ONE!
     */
    public Set getAnnotationElementsForTransElement(NOMElement nme);   
}