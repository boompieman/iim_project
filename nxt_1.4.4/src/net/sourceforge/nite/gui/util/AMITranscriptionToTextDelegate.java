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
 * A very simple delegate made especially for the AMI corpus. See
 * interface documentation for moreinfo.  <p>
 * @author Dennis Reidsma, UTwente
 */
public class AMITranscriptionToTextDelegate implements TranscriptionToTextDelegate {
    private final String WORD="w";
    private final String ASRWORD="asrword";
    private final String ASRSIL="asrsil";
    private final String VOCALSOUND="vocalsound";
    private final String TYPE="type";

    /**
     * Return a string representation for the given element from a transcription layer.
     */
    public String getTextForTranscriptionElement(NOMElement nme) {
	String name = nme.getName();

	if (name.equalsIgnoreCase(WORD) || name.equalsIgnoreCase(ASRWORD)) {
	    List kids=nme.getChildren();
	    String content="";
	    if (kids==null) {
		content = nme.getText();
		if (content==null) { return ""; }
		return content.trim();
	    } else {
 		return "";
	    }
	} else if (name.equalsIgnoreCase(ASRSIL)) {
	    return "";
	} else if (name.equalsIgnoreCase(VOCALSOUND)) {
	    return "[" + (String)nme.getAttributeComparableValue(TYPE) + "]";
	} else {
	    return "[" + name + "]";
	}
    }
}
