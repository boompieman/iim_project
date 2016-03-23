/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.transcriptionviewer;

import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nxt.*;
import java.util.Set;


/**
 * DisplayStrategy implementations are used as delegates to display information annotated 
 * on speech transcriptions in a NTranscriptionView.
 * <p>
 * The single most important class in this package is the 
 * {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView NTranscriptionView} class.
 * It has been designed to display speech transcriptions from a corpus of interactions, display extra
 * information annotated on these transcriptions (e.g. dialogue acts or adjacency pairs)
 * and provide a variety of GUI interface methods for selecting transcriptions and/or
 * annotations.
 * <p>
 * When the basic transcription text has been displayed in the NTranscriptionView, implementations 
 * of this DisplayStrategy interface are called to alter the displayed text in order to visualize 
 * the extra annotated information. To do this three methods are defined in the interface:
 * <ol>
 * <li>{@link net.sourceforge.nite.gui.transcriptionviewer.DisplayStrategy#init init(NTranscriptionView ntv)}<br>
 *     Upon creation this method should be
 *     called so the DisplayStrategy can perform any necessary initialization.
 * <li>{@link net.sourceforge.nite.gui.transcriptionviewer.DisplayStrategy#display display(NOMElement element)}<br>
 *     This method is called to make the DisplayStrategy display the given element. This
 *     may for instance involve changing the font or colour of the text to which this annotation
 *     element pertains (or e.g. placing brackets around it).
 * <li>{@link net.sourceforge.nite.gui.transcriptionviewer.DisplayStrategy#undisplay undisplay(NOMElement element)}<br>
 *     This method provides functionality to <i>undo</i> the display of annotation elements, i.e.
 *     remove the relevant styles and inserted text.
 * </ol>
 * <p>
 * A DisplayStrategy uses a TransToAnnoMap to determine which Transcription elements (and therefore NTextElements)
 * should be decorated because they represent the transcription elements for an annotation elements
 * ({@link net.sourceforge.nite.gui.transcriptionviewer.DisplayStrategy#setTransToAnnoMap setTransToAnnoMap}).
 * <p>
 * @author Dennis Reidsma 
 * @author Natasa Jovanovic
 * @author Dennis Hofs
 */
public interface DisplayStrategy 
{
 
    /**
     * When a new DisplayStrategy is created, this method should be
     * called so the DisplayStrategy can perform any necessary initialization.
     */
    public void init (NTranscriptionView ntv);
  
    /** 
     * This method is called to make the DisplayStrategy display the given element. This
     * may for instance involve changing the font or colour of the text to which this annotation
     * element pertains (or e.g. placing brackets around it).
     * <p>
     * @return true  if displayed successful,
     * @return false if the relevant piece of transcript text was not in the textarea and the
     * annotation element was <b>not</b> displayed
     */
    public boolean display (NOMElement element);
  
    /**
     * This method provides functionality to <i>undo</i> the display of annotation elements, i.e.
     * remove the relevant styles and inserted text.
     */
    public void undisplay (NOMElement element);
  
    /**
     * @@@Document
     */
    public void setTransToAnnoMap(TransToAnnoMap newmap);
   
}