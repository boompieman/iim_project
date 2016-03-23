/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004, Jean Carletta, Jonathan Kilgour, Natasa Jovanovic, Dennis Reidsma
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.util;

import net.sourceforge.nite.nom.nomwrite.*;


/**
 * Implementations of this interface will derive the text representation for any NOMElement.
 * <p>
 * Implementations of this interface are very application or corpus specific.
 * <p>
 * @author Dennis Reidsma, UTwente
 */
public interface NOMElementToTextDelegate {
    /**
     * Return a string representation for the given element (e.g. the words for an element from a transcription layer).
     */
    public String getTextForNOMElement(NOMElement nme);
}