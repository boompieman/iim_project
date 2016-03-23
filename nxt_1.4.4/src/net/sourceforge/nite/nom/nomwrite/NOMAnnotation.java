/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite;

/**
 * NOMAnnotation extends NOMElement and represents an individual
 * annotation on the data. Annotatoions do not extend NOMElement in
 * any way, but are contrasted to NOMObjects and NOMTypeElement,
 * though all are types of NOMElement and as such can be treated
 * similarly by the query engine.
 *
 * @author jonathan */
public interface NOMAnnotation extends NOMElement {
    
}
