/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
// Decompiled by Jad v1.5.7g. Copyright 2000 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/SiliconValley/Bridge/8617/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NTextElementTimeComparator.java

package net.sourceforge.nite.gui.textviewer;

import java.util.Comparator;

// Referenced classes of package nite.gui.textviewer:
//            NTextElement

public class NTextElementTimeComparator
    implements Comparator
{

    public NTextElementTimeComparator()
    {
    }

    public int compare(Object obj, Object obj1)
    {
        NTextElement ntextelement = (NTextElement)obj;
        NTextElement ntextelement1 = (NTextElement)obj1;
        return (new Double(ntextelement.getStartTime())).compareTo(new Double(ntextelement1.getStartTime()));
    }
}
