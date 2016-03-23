/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.transcriptionviewer;

import java.util.*;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.nxt.*;

/**
 * This class provides a basic implementation of the DisplayStrategy interface.
 * Most implementations will subclass AbstractDisplayStrategy, since it provides
 * one or two useful things.
 * <p>
 * See the documentation of the {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView NTranscriptionView} 
 * class for some utility methods to modify the style of text elements. Most important are InsertCopyOfStyle and RemoveStyleFromChain.
 * 
 * @author Dennis Reidsma 
 * @author Natasa Jovanovic
 * @author Dennis Hofs
 */
public abstract class AbstractDisplayStrategy implements DisplayStrategy {

    /**
     * The NTranscriptionView on which this delegate should display the annotation elements is stored internally.
     */
    protected NTranscriptionView ntv;
    
   
    /**
     * Initialization: store NTranscriptionView and create a template italic style.
     */
    public void init (NTranscriptionView ntv) {
    	this.ntv=ntv;
    	transToAnnoMap = new DefaultTransToAnnoMap(ntv);
    }

    /**
  @@@ Document
     */
    protected TransToAnnoMap transToAnnoMap;
    /**
     * @@@Document
     */
    public void setTransToAnnoMap(TransToAnnoMap newmap) {
        transToAnnoMap = newmap;
    }


    /**
     * A utility method that returns the NTextElements (screen elements in a NTranscriptionView)
     * for the given annotation element. 
     * <p>
     * Uses a call to getTranscriptionDescendants to determine the transcription NOMElements,
     * then finds the corresponding NTextElements.
     */
    protected Set getTextElements(NOMElement element) {
        Set result = new LinkedHashSet();
        Set transcriptionDesc = transToAnnoMap.getTransElementsForAnnotationElement(element); 
        Iterator allDescsIt = transcriptionDesc.iterator();
        while (allDescsIt.hasNext()) {
            NOMElement nme = (NOMElement)allDescsIt.next();
            NOMObjectModelElement nome = new NOMObjectModelElement(nme);
            Set s = ntv.getTextElements(nome);
            if (s != null)
                result.addAll(s);
        }
        return result;
    }


}
    
    
   
