/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */


package net.sourceforge.nite.gui.textviewer;

import java.util.Comparator;

// Referenced classes of package nite.gui.textviewer:
//            NTextElement

public class NTextElementDataComparator
    implements Comparator
{

    public NTextElementDataComparator()
    {
    }

    public int compare(Object obj, Object obj1)
    {
        NTextElement ntextelement = (NTextElement)obj;
        NTextElement ntextelement1 = (NTextElement)obj1;
        
        return ntextelement.getText().compareTo(ntextelement1.getText());
    }
}
