/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */


package net.sourceforge.nite.gui.textviewer;

import java.util.Comparator;




public class NTreeNodeComparator
    implements Comparator
{

    public NTreeNodeComparator()
    {
    }

    public int compare(Object obj, Object obj1)
    {
	if (obj==obj1) { return 0; }
	else { return -1; }

	/*
        NTreeNode n = (NTreeNode)obj;
        NTreeNode n1 = (NTreeNode)obj1;
       
        return n.getDataElement().getID().compareTo(n1.getDataElement().getID());
	*/
    }
}
