/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.link;

/**
 * A NOMView reacts to changes made to the NOM by other modules.
 * Implemented by any screen element that can be affected by a NOM
 * change, for example a transcription view or an annotation board.
 *
 * @author jonathan 
 */
public interface NOMView {
    /** handle a specific change to the corpus */ 
    public void handleChange(NOMEdit edit);
} 
