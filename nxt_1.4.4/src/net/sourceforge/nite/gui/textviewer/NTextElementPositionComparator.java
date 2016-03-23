/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
// Decompiled by Jad v1.5.7g. Copyright 2000 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/SiliconValley/Bridge/8617/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NTextElementComparator.java

package net.sourceforge.nite.gui.textviewer;

import java.util.Comparator;

// Referenced classes of package nite.gui.textviewer:
//            NTextElement
public class NTextElementPositionComparator
    implements Comparator
{

    public NTextElementPositionComparator()
    {
    }

    /** Compare objects assuming that both are text elements. If both
     * are non-null, the element with a smaller position is assument
     * to precede the one with the larger position. The null element
     * is assumed to precede anything. **/
    public int compare(Object obj1, Object obj2)
    {
	
	NTextElement e1 = (NTextElement)obj1;
	NTextElement e2 = (NTextElement)obj2;
	
	if( e1 == null && e2 == null ) {
	    return 0;
	} else if( e1 == null ) {
	    return -1;
	} else if( e2 == null ) {
	    return 1;
	} else if( e1.equals( e2 )) {
	    return 0;
	} else {
	    int pos1 = e1.getPosition();
	    int pos2 = e2.getPosition();
	    
	    if (pos1== pos2) {
		return 0;
	    } else if (pos1 < pos2) {
		return -1;
	    } else { //pos2 > pos1
		return 1;
	    }
	}
    }

}
