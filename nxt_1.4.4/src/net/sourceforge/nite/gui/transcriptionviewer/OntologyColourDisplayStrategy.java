package net.sourceforge.nite.gui.transcriptionviewer;
import java.util.*;
import java.awt.Color;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.util.*;
import javax.swing.text.*;

/** 
SLIGHTLY GENERIC
 * A trick: given an ontology, an element is displayed in the colour determined by its first pointer to
 an element in that ontology.
 This displaystrategy can be shared for many annotation element types.
 
 Basic: the colour is determined `randomly' from the ontology.
 
 Extension: maybe we want to have some conttrol over the colours?
 
 for now, using this for more than one ontology will cause extreme confusion wrt what ontology.
 within ontology, colour means ONE elemtn....
 */
public class OntologyColourDisplayStrategy extends AbstractDisplayStrategy {
	
   /**Documentation as in superclass?**/
    protected final String COULOUR_STYLE_NAME="ONTOLOGY_COLOUR_STYLE";
   /**Documentation as in superclass?**/
    protected static int idcounter = 0;
String role = "";

 /***********************************CONSTRUCTORS**********************************/
   
    /** 
     * Initialize a OntologyColourDisplayStrategy that uses the given template style for display.
     */  
    public OntologyColourDisplayStrategy (NTranscriptionView ntv, String role) {
        init(ntv);
        this.role = role;
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
                //determine colour/style for element
        Style style = getColouredStyle(element);
        if (style != null) {
            while (it.hasNext()) {
                next=(NTextElement)it.next();
                    ntv.insertCopyOfStyle(next,style,COULOUR_STYLE_NAME+String.valueOf(idcounter));
                    
                idcounter++;
                if (idcounter == Integer.MAX_VALUE) {
                    idcounter = Integer.MIN_VALUE;
                }
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
                return ((Style)o).getName().startsWith(COULOUR_STYLE_NAME);
            }
        };     
        Iterator it = l.iterator();
        NTextElement next = null;
        while (it.hasNext()) {
            next=(NTextElement)it.next();
            ntv.removeStyleFromChain(next, isMyTextStyle);
        }  
    }	   	

    /**
     * Return template style for given annotation element that points to ontology, or null if no pointer to ontology
     */
    protected Style getColouredStyle(NOMElement element) {
        NOMPointer p = element.getPointerWithRole(role) ;
        if (p != null) {
            return getTemplateStyle(p.getToElement());
        }
        return null;
    }
    
    Map ontologyToStyle = new HashMap();
    /**
     * Return template style for given ontology element
     */
    protected Style getTemplateStyle(NOMElement type) {
        if (ontologyToStyle.get(type) == null) {
            //create new style for this hitherto unencountered type
            Color newColour = ValueColourMap.getColour(type);
            Style newTemplate = ntv.addStyle(COULOUR_STYLE_NAME+"_template_"+type.getID(),null);
            StyleConstants.setForeground(newTemplate, newColour);
            StyleConstants.setBold(newTemplate, true);
            StyleConstants.setFontSize(newTemplate, 12);
            ontologyToStyle.put(type, newTemplate);
        }
        return (Style)ontologyToStyle.get(type);
    }
}