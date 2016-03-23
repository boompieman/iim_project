package net.sourceforge.nite.tools.comparison.dualtranscription;

/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004, Jean Carletta, Jonathan Kilgour, Natasa Jovanovic, Dennis Reidsma
 * See the README file in this distribution for licence.
 */

import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.meta.*;

import java.util.Iterator;
import java.util.List;
import java.lang.String;

/**
 * A delegate to extract only text from the specified annotator
 * <p>
 * @author Craig Nicol, UEdin
 */
public class DualTranscriptionToTextDelegate implements TranscriptionToTextDelegate {
	private final String WORD="w";
	
	private String aname="";
	private String aattr="";
	private NLayer alayer=null;
	
	public DualTranscriptionToTextDelegate(String annotatorname, String annotatorattr, NLayer annotatorlayer) {
		super();
		aname = annotatorname;
		aattr = annotatorattr;
		alayer = annotatorlayer;
	}
	
	public DualTranscriptionToTextDelegate(String annotatorname, String annotatorattr) {
		super();
		aname = annotatorname;
		aattr = annotatorattr;
	}
	
	
	/**
	 * Return a string representation for the given element from a transcription layer.
	 */
	public String getTextForTranscriptionElement(NOMElement nme) {
		if (alayer == null) {
            String name = nme.getName();
			NOMAttribute annotator = nme.getAttribute(this.aattr);
            
			if (name.equalsIgnoreCase(WORD) && annotator != null && annotator.getStringValue().equals(this.aname)) {
				List kids=nme.getChildren();
				String content="";
				if (kids==null) {
					content = nme.getText();
					if (content==null) { return ""; }
					return content.trim();
				} else {
					return "";
				}
			} else {
				/*
				 return "[" + (String)nme.getAttributeComparableValue(DESC) + "]";
				 } else {
				 */
				return "";
			}
		} else {
			return "IMPL:alayer";
		}
	}
}

