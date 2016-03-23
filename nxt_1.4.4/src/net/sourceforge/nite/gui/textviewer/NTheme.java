/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import java.awt.*;

public class NTheme extends DefaultMetalTheme {
    
    private ColorUIResource primary1;
    private ColorUIResource primary2;
    private ColorUIResource primary3;
    private ColorUIResource secondary1;
    private ColorUIResource secondary2;
    private ColorUIResource secondary3;

    public NTheme (Color p1, Color p2, Color p3,
		   Color s1, Color s2, Color s3) {
	if (p1 != null) { primary1=new ColorUIResource(p1); }
	if (p2 != null) { primary2=new ColorUIResource(p2); }
	if (p3 != null) { primary3=new ColorUIResource(p3); }
	if (s1 != null) { secondary1=new ColorUIResource(s1); }
	if (s2 != null) { secondary2=new ColorUIResource(s2); }
	if (s3 != null) { secondary3=new ColorUIResource(s3); }
    }

    protected ColorUIResource getPrimary1() { 
	if (primary1 != null) {	return primary1; }
	else { return super.getPrimary1(); }
    }
    protected ColorUIResource getPrimary2() { 
	if (primary2 != null) {	return primary2; }
	else { return super.getPrimary2(); }
    }
    protected ColorUIResource getPrimary3() { 
	if (primary3 != null) {	return primary3; }
	else { return super.getPrimary3(); }
    }
    protected ColorUIResource getSecondary1() { 
	if (secondary1 != null) { return secondary1; }
	else { return super.getSecondary1(); }
    }
    protected ColorUIResource getSecondary2() { 
	if (secondary2 != null) { return secondary2; }
	else { return super.getSecondary2(); }
    }
    protected ColorUIResource getSecondary3() { 
	if (secondary3 != null) { return secondary3; }
	else { return super.getSecondary3(); }
    }
    //    protected ColorUIResource getPrimary2() { return primary2; }
    //    protected ColorUIResource getPrimary3() { return primary3; }
}

