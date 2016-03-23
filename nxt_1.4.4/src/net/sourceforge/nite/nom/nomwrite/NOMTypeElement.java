/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite;

/**
 * NOMTypeElement extends NOMElement and represents an element within
 * a type hierarchy or "ontology". Such elements extend NOMElement so
 * that they can be used in the query language (though in general they
 * will not be returned as search results). Type elements only have
 * one attribute (named in the metadata file) which holds the type name.
 *
 * @author jonathan */
public interface NOMTypeElement extends NOMElement {
    
}
