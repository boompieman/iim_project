/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.transcriptionviewer;

import java.util.*;
import java.awt.Color;
import javax.swing.*;
import javax.swing.text.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.nxt.*;


/**
 * This DisplayStrategy implementation shows annotation elements in the transcription text
 * by inserting text before the first and/or after the last NTextElement that is related to the
 * annotation element.
 * <p>
 * The prefix and postfix strings can be customized. The actual strings for pre and postfix are
 * determined in the utility methods 
 * {@link net.sourceforge.nite.gui.transcriptionviewer.StringInsertDisplayStrategy#formStartString formStartString(NOMElement element)}
 * and {@link net.sourceforge.nite.gui.transcriptionviewer.StringInsertDisplayStrategy#formEndString formEndString(NOMElement element)}.
 * This makes it easy to extend this class with more specific visualization in a public subclass or an inner class.
 * <p>
 * The style of these strings can also be customized. The style may be dependent on the annotation element. This is 
 * achieved by overriding the method 
 * {@link net.sourceforge.nite.gui.transcriptionviewer.StringInsertDisplayStrategy#getStyle getStyle(NOMElement element)}.
 * <p>
 * See also {@link net.sourceforge.nite.gui.transcriptionviewer.DisplayStrategy DisplayStrategy}
 * See also {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#setDisplayStrategy NTranscriptionView.setDisplayStrategy}
 * <p>
 * <p> EXAMPLES!!!
 * 
 * @author Dennis Reidsma 
 * @author Natasa Jovanovic
 * @author Dennis Hofs
 */
public class StringInsertDisplayStrategy extends AbstractDisplayStrategy
 /* [DR:] we might consider building a few static factory methods for easy creation of StringInsertDisplayStrategy
    objects that only use e.g. brackets, or always include agentname, or some attrib value, or attrib value of given pointer...
    Could also make all these values accesssible in utility methods, so you can override formStartBracket with calls to these methods :-)
    Oh and some basic indentation call (getspaces(5) is always funny :-)
 */
{
    
    /** 
     * The prefix string, displayed just before the first text element for an annotation element.
     * <p>
     * Default: "("
     */
    protected String startString = "(";
    
    /** 
     * The prefix string, displayed just before the first text element for an annotation element.
     * <p>
     * Default: ")\n"
     */
    protected String endString = ")\n";
   
   /**Documentation !!!**/
    public static final String BRACKET_STYLE = "BRACKET_STYLE"; 
   /**Documentation !!!**/
    protected int idcounter = 0;
   /**Documentation !!!**/
    protected Style style;

/*******************************

        CONSTRUCTORS
        
        *******************************************************/
   
   
    /**  
     * Default behaviour of this class: display annotation elements by placing relevant text 
     * between brackets (...), which are coloured red.
     */
    public StringInsertDisplayStrategy(NTranscriptionView ntv) {
        init(ntv);
        createDefaultStyle();
    }
    
    /**  
     * Default behaviour of this class: display annotation elements by placing relevant text 
     * between brackets (...), which get the given style.
     */
    public StringInsertDisplayStrategy(NTranscriptionView ntv,Style newStyle) {
        init(ntv);
        style = newStyle;
    }


    
    /**
     * Changes the value of the prefix string
     */
    public void setStartString(String newStartString) {
        startString = newStartString;
    }

    /**
     * Changes the value of the postfix string
     */
    public void setEndString(String newEndString) {
        endString = newEndString;
    }

/*******************************

        MAIN OVERRIDE METHODS
        
        *******************************************************/

    /**
     * Form the prefix string for the given annotation NOMElement. Override this method if you
     * want to change the visualization of annotation elements to another string representation.
     */
    protected String formStartString(NOMElement element) {
       return startString;
    }      
    /**
     * Form the postfix string for the given annotation NOMElement. Override this method if you
     * want to change the visualization of annotation elements to another string representation.
     */
    protected String formEndString(NOMElement element) {
       return endString;
    }      
    /**
     * Get the style for the pre and postfix string for the given element. Useful to override this 
     * if it should be dependent on e.g. agent name! (different color per agent?)
     */
    protected Style getStyle(NOMElement element) {
       return style;
    }      

    /**
     * The display method of this class consists of tracking down the relevant NTextElements in the 
     * NTranscriptionView, placing the prefix just before the first displayed transcription element and 
     * placing the postfix just after the last displayed transcription element.
     * <p>
     * The newly inserted NTextElements will have the annotation element as dataElement.
     */
    public boolean display (NOMElement element) {
        Set l = getTextElements(element); //can we guaratee an order here?
        if ((l == null) || (l.size()<=0)){
            return false;
        }
        
        //determine minimum and maximum position. Don't depend on order of elements yet :-(
        int min = -1;
        int max = -1;
        String startS = formStartString(element);
        String endS = formEndString(element);
        Iterator it = l.iterator();
        while (it.hasNext()) {
            NTextElement nextElem= (NTextElement)it.next();
            if ((nextElem.getPosition() < min) || (min == -1)) {
                min=nextElem.getPosition();
            }        
            if ((nextElem.getPosition() + nextElem.getText().length()  > max) || (max == -1)) {
                max=nextElem.getPosition() + nextElem.getText().length();
            }        
        }
        //use annotation element as dataElement!
        NOMObjectModelElement nome=new NOMObjectModelElement(element);
        //add prefix
        NTextElement startStringElement=
            new NTextElement(startS,style.getName(),min,element.getStartTime(), element.getEndTime());
        startStringElement.setDataElement(nome);
        ntv.insertElement (startStringElement,min);
        //add postfix
        NTextElement endStringElement=
            new NTextElement(endS,style.getName(),max + startS.length(),element.getStartTime(), element.getEndTime());
        endStringElement.setDataElement(nome);
        ntv.insertElement (endStringElement,max + startS.length());
        return true;
   }   

   /**
    * This method is used to undisplay the annotation element. Since the display was only creation of
    * two strings that both have the annotation element as data element, removing them is easy...
    */
    public void undisplay(NOMElement element) {
        if (element!=null) {
            NOMObjectModelElement nome= new NOMObjectModelElement(element);
            ntv.removeDisplayComponent((ObjectModelElement)nome);
        } 
    }     
   
/*******************************

        INTERNAL UTILITY METHODS
        
        *******************************************************/

    /**
     * Create a default style for the pre and postfix strings and store it.
     * In this case the default style is just using red text colouring.
     */
    protected void createDefaultStyle() {
        style = ntv.addStyle(BRACKET_STYLE,null);
        StyleConstants.setForeground(style,Color.red);
    }
}      
        
 
    
    
