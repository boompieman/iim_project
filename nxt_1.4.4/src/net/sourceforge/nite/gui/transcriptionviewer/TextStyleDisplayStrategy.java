/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.transcriptionviewer;

import java.util.*;
import java.awt.Color;
import net.sourceforge.nite.util.*;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.nxt.*;
import javax.swing.text.*;

/** 
 * This DisplayStrategy implementation shows annotation elements in the transcription text
 * by changing the style (font, color, etc) of the text.
 * You can set this style by calling one of the many-parameter-constructors or by creating a 
 * <i>template style</i> that is to be used as example.
 * <p>
 * Uses {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#insertCopyOfStyle NTranscriptionView.insertCopyOfStyle} 
 * and {@link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#removeStyleFromChain NTranscriptionView.removeStyleFromChain}
 * for maintaining the styles.
 * <p>
 * See also @link net.sourceforge.nite.gui.transcriptionviewer.DisplayStrategy DisplayStrategy
 * See also @link net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView#setDisplayStrategy NTranscriptionView.setDisplayStrategy
 * <p>
 * @author Dennis Reidsma 
 * @author Natasa Jovanovic
 * @author Dennis Hofs
 */
public class TextStyleDisplayStrategy extends AbstractDisplayStrategy {
	
   /**Documentation !!!**/
    protected final String TEXT_STYLE_NAME="TEXT_STYLE ";
   /**Documentation !!!**/
    protected static int idcounter = 0;
   /**Documentation !!!**/
    protected Style style;
    
 /***********************************CONSTRUCTORS**********************************/
   
    /** 
     * Initialize a TextStyleDisplayStrategy that uses the given template style for display.
     */  
    public TextStyleDisplayStrategy (NTranscriptionView ntv, Style style) {
        init(ntv);
        this.style=style;
    }
   
   /**
    * Initialize a TextStyleDisplayStrategy, creating a template style with the given parameters.
    * <p>
    * @param fontSize (if fontSize==0 font size is unchanged )
    * @param fontStyle "italic","bold","underline" (if fontStyle==null fontStyle is to be unchanged
    * @param c (if Color==null -> unchanged)
    * @param isBackground Determines if colour is foreground or background colour (if color==null, isBackround is set to false)
    */     
   public TextStyleDisplayStrategy (NTranscriptionView ntv, int fontSize, String fontStyle, Color c, boolean isBackground) {
        init(ntv);
       this.ntv=ntv;
       if (c==null) {
           isBackground=false;
       }
       createTemplateStyle(fontSize, fontStyle, c, isBackground);
   }
 
/********************************END-OF-CONSTRUCTORS********************************/


    /**
     * Display annotation element by changing the text style of all text for all relevant transcription elements.
     * <p>
     * @return true if displayed successfully, i.e. if the text to which given element relates was actually
     * visible in the NTranscriptionView.
     */
    public boolean display(NOMElement element) {
        Set l = getTextElements(element);  
        if ((l == null) || (l.size()<=0)){
            return false;
        }
        Iterator it = l.iterator();
        NTextElement next = null;
        while (it.hasNext()) {
            next=(NTextElement)it.next();
            ntv.insertCopyOfStyle(next,style,TEXT_STYLE_NAME+String.valueOf(idcounter));
            idcounter++;
            if (idcounter == Integer.MAX_VALUE) {
                idcounter = Integer.MIN_VALUE;
            }
        }  
        return true;
    }

    /**
     * Undisplay annotation elements by removing the relevant styles from the style chain for the 
     * appropriate transcription elements.
     */
    public void undisplay(NOMElement element) {
        Set l = getTextElements(element);
        if ((l == null) || (l.size()<=0))
            return;
        Predicate isMyTextStyle = new Predicate() {
            public boolean valid(Object o) {
                return ((Style)o).getName().startsWith(TEXT_STYLE_NAME);
            }
        };     
        Iterator it = l.iterator();
        NTextElement next = null;
        while (it.hasNext()) {
            next=(NTextElement)it.next();
            ntv.removeStyleFromChain(next, isMyTextStyle);
        }  
    }	   	

/*******************************

        INTERNAL UTILITY METHODS
        
        *******************************************************/


    /** 
     * Creates a template style used to display annotation elements, given the parameters
     * that were originally passed to the convenience constructor.
     */
    private void createTemplateStyle(int fontSize, String fontStyle, Color fontColor, boolean isBackground) {
        style = StyleContext.getDefaultStyleContext().addStyle(TEXT_STYLE_NAME,null);
        if (fontColor!=null && isBackground) {
            style.addAttribute(StyleConstants.ColorConstants.Background,fontColor);
        } else if (fontColor!=null) {
            style.addAttribute(StyleConstants.ColorConstants.Foreground,fontColor);    
        }
        if (fontSize!=0) {
            StyleConstants.setFontSize(style,fontSize);
        }
        if (fontStyle!=null) {
            if (fontStyle.equalsIgnoreCase("bold")) {
                StyleConstants.setBold(style,true);
            } else if (fontStyle.equalsIgnoreCase("italic")) {
                StyleConstants.setItalic(style,true);
            } else if (fontStyle.equalsIgnoreCase("underline")) {
                StyleConstants.setUnderline(style,true);
            }
        }
    }   
 
}