/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;
import net.sourceforge.nite.nxt.ObjectModelElement;

/** 
 * implemented by interfaces which wish to take control of redisplay
 * themselves, rather than leaving redisplay to the limited set of XML
 * actions as pre-defined.
 *
 * @author jonathan
 */
public interface Displayer {
    /** redisplay the entire interface */
    public void redisplay();
    /** redisplay the parts of the interface affected by changes to
        the given ObjectModelElement. */
    public void redisplay(ObjectModelElement ome);
}
