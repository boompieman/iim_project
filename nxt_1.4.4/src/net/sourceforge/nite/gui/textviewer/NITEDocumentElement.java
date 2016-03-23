/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;
import javax.swing.text.*;
public class NITEDocumentElement{
    AttributeSet  a;
    NTextElement nel;
    
    NITEDocumentElement(AttributeSet d, NTextElement te){
	a = d;
	nel = te;
    }

    NTextElement getNTextElement(){
	return nel;
    }
    
    AttributeSet getDocElement(){
	return a;
    }
}
