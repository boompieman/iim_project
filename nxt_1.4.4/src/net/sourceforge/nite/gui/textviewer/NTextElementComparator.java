/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import java.util.Comparator;

// Referenced classes of package nite.gui.textviewer:
//            NTextElement

public class NTextElementComparator
    implements Comparator
{
    boolean debug=false;

    public NTextElementComparator()
    {
    }
    public NTextElementComparator(boolean debug)
    {
	this.debug=debug;
    }

    public int compare(Object obj, Object obj1)
    {
	if (obj==obj1) { return 0; }
	else { return 1; }
	/*
        NTextElement ntextelement = (NTextElement)obj;
        NTextElement ntextelement1 = (NTextElement)obj1;
        return (new Double(ntextelement.getStartTime())).compareTo(new Double(ntextelement1.getStartTime()));
	*/
    }
}
