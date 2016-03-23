/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */


package net.sourceforge.nite.gui.textviewer;

import java.util.Comparator;

import net.sourceforge.nite.nxt.ObjectModelElement;



public class ObjectModelComparator
    implements Comparator
{

    public ObjectModelComparator()
    {
    }

    public int compare(Object obj, Object obj1)
    {
	/*	if (obj==obj1) return 1;
		else return -1;
	*/
	//	return 1;
        ObjectModelElement ome = (ObjectModelElement)obj;
        ObjectModelElement ome1 = (ObjectModelElement)obj1;
	if (ome==null && ome1==null) return 0;
	if (ome==null) return -1;
	if (ome1==null) return 1;
        return ome.getID().compareTo(ome1.getID());
    }
}
