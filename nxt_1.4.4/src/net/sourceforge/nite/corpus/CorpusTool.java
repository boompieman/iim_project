/* NXT Corpus Manager
 * Copyright (c) 2008, Jean Carletta, Jonathan Kilgour
 * Created by Jonathan Kilgour 29/4/08 
 */
package net.sourceforge.nite.corpus;

import net.sourceforge.nite.meta.NMetaData;
import javax.swing.JPanel;

/** Interface to be instantated by all tools that can perform editing
 * or management style tasks at a corpus-wide level. NOT intended for
 * annotation tools! Each CorpusTool must publish a JPanel containing
 * its interface and declare whether any changes need to be saved as
 * well as providing a method to do its own saving. */
public interface CorpusTool {
    /** provide the interface for this tool */
    public JPanel getPanel(NMetaData metadata);
    /** return true if outstanding changes are still to be committed */
    public boolean requiresSave();
    /** apply the changes to the corpus */
    public void applyChanges();
    /** get a handy tip to associate with this app */
    public String getToolTip();
    /** get a short textual description of this app */
    public String getDescription();
    /** get the (one word) name of this app for naming a tab */
    public String getName();
}
