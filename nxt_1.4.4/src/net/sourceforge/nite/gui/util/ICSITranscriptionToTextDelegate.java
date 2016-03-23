/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004, Jean Carletta, Jonathan Kilgour, Natasa Jovanovic, Dennis Reidsma
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.nom.nomwrite.*;

import java.util.Iterator;
import java.util.List;


/**
 * A very simple delegate made especially for the ICSI corpus. See interface documentation for moreinfo.
 * <p>
 * @author Dennis Reidsma, UTwente
 */
public class ICSITranscriptionToTextDelegate implements TranscriptionToTextDelegate {
    private final String WORD="w";

    /**
     * Return a string representation for the given element from a transcription layer.
     */
    public String getTextForTranscriptionElement(NOMElement nme) {
	String name = nme.getName();

	if (name.equalsIgnoreCase(WORD)) {
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
	    return "[" + name + "]";
	}
    }
}
