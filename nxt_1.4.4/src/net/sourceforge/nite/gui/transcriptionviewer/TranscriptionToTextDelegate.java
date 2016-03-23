/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004, Jean Carletta, Jonathan Kilgour, Natasa Jovanovic, Dennis Reidsma
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.transcriptionviewer;

import net.sourceforge.nite.nom.nomwrite.*;


/**
 * Implementations of this interface will derive the text representation for elements
 * from the transcription layer. In most case the two basic methos for obtaining that text
 * available in NTranscriptionView (see {@link NTranscriptionView#setTranscriptionToTextDelegate})
 * should be sufficient. Sometimes however the transcription layer is so complicated (see e.g.
 * the ICSI corpus), having many different element types in it, that it is easier to implement
 * a special delegate for this problem.
 * <p>
 * Implementations of this interface are very application or corpus specific.
 * <p>
 * @author Dennis Reidsma, UTwente
 */
public interface TranscriptionToTextDelegate {
    /**
     * Return a string representation for the given element from a transcription layer.
     */
    public String getTextForTranscriptionElement(NOMElement nme);
}