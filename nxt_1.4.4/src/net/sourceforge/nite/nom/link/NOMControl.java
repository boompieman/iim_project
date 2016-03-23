/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.link;

import net.sourceforge.nite.nom.NOMException;

/**
 * Keeps the registered NOMViews in sync with the central
 * NOM by informing them of any edits made.
 *
 * @author jonathan
 */
public interface NOMControl {
    /** Add a NOMView to the list of viewers that get notifed of changes. */
    public void registerViewer(NOMView display);
    /** Remove a NOMView from the list of viewers that get notifed of changes. */
    public void deregisterViewer(NOMView display);
    /** Notify all NOMViews that an (unspecified) edit has ocurred */
    public void notifyChange();
    /** Notify all NOMViews that a specific NOMEdit has ocurred */
    public void notifyChange(NOMEdit edit) throws NOMException;
    /** Notify all NOMViews except the one passed as an argument that
        a NOMEdit has ocurred */
    public void notifyChange(NOMEdit edit, NOMView view) throws NOMException;
} 
