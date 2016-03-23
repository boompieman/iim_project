package net.sourceforge.nite.tools.necoder;
import net.sourceforge.nite.tools.necoder.*;
import net.sourceforge.nite.util.*;

import java.util.*;
import java.awt.Color;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.nxt.*;
import javax.swing.text.*;

/** 

@author Craig Nicol, based on NEDisplayStrategy (edited by Jonathan Kilgour)

Display for the NE coder when we allow multiple assignments for the same span

*/
public class NEDisplayStrategyMulti extends AbstractDisplayStrategy {


    /**Documentation as in superclass?**/
    protected final String MULTI_STYLE_NAME="MULTI_STYLE";
    /**Documentation as in superclass?**/
    protected static int idcounter = 0;
    String neTypeRole = "";
    String abbrevAttrib = "";
    String attributeName = "";
    /***********************************CONSTRUCTORS**********************************/
   
    /** 
     * Initialize neTypeRole is the role of the pointer to the NE
     * ontology; abbrevAttrib is the name of the attrib in the ne
     * ontology that isused in the 'brackets'; attributeName is
     * non-null if we should display an attribute of the named entity
     * rather than a pointer to a type ontology (jonathan 25/3/6)
     */  
    public NEDisplayStrategyMulti (NTranscriptionView ntv, String neTypeRole, String abbrevAttrib, String attributeName) {
        init(ntv);
        this.neTypeRole = neTypeRole;
        this.abbrevAttrib = abbrevAttrib;
        // this last
        this.attributeName = attributeName;
    }

 
    /********************************END-OF-CONSTRUCTORS********************************/
   

    /**
     * Display annotation element by changing the text style of all text for all relevant transcription elements.
     * <p>
     * @return true if displayed successfully, i.e. if the text to which given element relates was actually
     * visible in the NTranscriptionView.
     */
    public boolean display(NOMElement element) {
        int firstpos=-1;
        int lastpos=0;
        Set l = getTextElements(element);  
        if ((l == null) || (l.size()<=0)){
            return false;
        }
        Iterator it = l.iterator();
        NTextElement next = null;
        //determine colour/style for element
        List styles = getColouredStyle(element);
        Style style = null;
        if (styles != null && styles.size() > 0) {
            style = (Style)styles.get(0);
            if (style != null) {
                while (it.hasNext()) {
                    next=(NTextElement)it.next();
                    //ntv.insertCopyOfStyle(next,style,MULTI_STYLE_NAME+String.valueOf(idcounter));
                    if ((next.getPosition() < firstpos) || (firstpos == -1)) {
                        firstpos = next.getPosition();
                    }
                    if (next.getPosition()+next.getText().length() > lastpos) {
                        lastpos = next.getPosition()+next.getText().length();
                    }
                    
                    idcounter++;
                    if (idcounter == Integer.MAX_VALUE) {
                        idcounter = Integer.MIN_VALUE;
                    }
                }
            }  
        
            //lastpos = spaceBackwards(lastpos);

            String val=null;
            NOMPointer p = element.getPointerWithRole(neTypeRole) ;
            // changed by jonathan 25/3/06 to allow for attribute type
            // rather than pointer to ontology.
            if (p != null && p.getToElement()!=null) {
                val = (String)p.getToElement().getAttributeComparableValue(abbrevAttrib);
            } else if (attributeName!=null && attributeName.length()>0) {
                val = (String)element.getAttributeComparableValue(attributeName);
            }
            if (val!=null) {
                //use annotation element as dataElement!
                NOMObjectModelElement nome=new NOMObjectModelElement(element);
                //add postfix
                NTextElement endStringElement=
                    new NTextElement(") ",style.getName(),lastpos,element.getStartTime(), element.getEndTime(), nome);
                ntv.insertElement (endStringElement,lastpos);            

                //add prefix
		if (styles.size() > 1) {
		    //Use reverse order so they appear in the same order as the data
		    List ps = new Vector(element.getPointers());
		    ListIterator pit = ps.listIterator();
		    while(pit.hasNext()) {
			NOMPointer nextp = (NOMPointer)pit.next();
			if(!neTypeRole.equals(nextp.getRole())) {
			    pit.remove();
			}
		    }

		    pit = ps.listIterator(styles.size());

		    ListIterator sit = styles.listIterator(styles.size());
		    while(sit.previousIndex() > 0 && pit.previousIndex() > 0) {
			Style s = (Style)sit.previous();
			NOMPointer nextp = (NOMPointer)pit.previous();

			String styleval = (String)nextp.getToElement().getAttributeComparableValue(abbrevAttrib);

			// BUGFIX: Separate attributes so that higlighting works correctly.
			NTextElement bog = new NTextElement(" ", null);
			ntv.insertElement (bog, firstpos);

			NTextElement styleStringElement=
			    new NTextElement(styleval+". ",s.getName(),firstpos,element.getStartTime(), element.getEndTime(), nome);
			ntv.insertElement (styleStringElement,firstpos);

		    }
		}

		// BUGFIX: Separate attributes so that higlighting works correctly.
		NTextElement bog = new NTextElement(" ", null);
		ntv.insertElement (bog, firstpos);

                NTextElement startStringElement=
                    new NTextElement("("+val+". ",style.getName(),firstpos,element.getStartTime(), element.getEndTime(), nome);
                ntv.insertElement (startStringElement,firstpos);
            }
            return true;
        }
        return false;
    }
    /**
     * POS is a position in the transcription view. It is moved to the left as long as there are 
     * spaces directly left of it
     */
    protected int spaceBackwards(int pos) {
        int result = pos;
        try {
            while ((result > 0) && ntv.getDocument().getText(result-1,1).equals(" ")) {
                result--;
            }
        } catch (BadLocationException ex) {
            System.out.println("!");
        }
        return result;
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
                    return ((Style)o).getName().startsWith(MULTI_STYLE_NAME);
                }
            };     
        Iterator it = l.iterator();
        NTextElement next = null;
        while (it.hasNext()) {
            next=(NTextElement)it.next();
            ntv.removeStyleFromChain(next, isMyTextStyle);
        }  
        if (element!=null) {
            NOMObjectModelElement nome= new NOMObjectModelElement(element);
            ntv.removeDisplayComponent((ObjectModelElement)nome);
        } 
    }	   	

    /**
     * Return template style for given annotation element that points to ontology, or null if no pointer to ontology
     */
    protected List getColouredStyle(NOMElement element) {
        List ps = element.getPointers(); //element.getPointerWithRole(neTypeRole) ;
        Vector styles = new Vector(ps.size());
        if (ps.size() == 0) {
            return null;
        }
        Iterator ptrs = ps.iterator();
        while(ptrs.hasNext()) {
            NOMPointer p = (NOMPointer)ptrs.next();
            if ( !neTypeRole.equals(p.getRole()) ) {
		System.err.println("Wrong pointer role: Expecting " + neTypeRole + ", found " + p.getRole() + ".");
                continue;
            }
            if (p != null) {
                styles.add(getTemplateStyle(p.getToElement()));
            }
            if (attributeName!=null && attributeName.length()>0) {
                styles.add(getTemplateStyle((String)element.getAttributeComparableValue(attributeName)));
            }
        }
        return styles;
    }
    
    Map ontologyToStyle = new HashMap();
    /**
     * Return template style for given ontology element or attribute value
     */
    protected Style getTemplateStyle(Object type) {
        if (type==null) { return null; }
        if (ontologyToStyle.get(type) == null) {
            //create new style for this hitherto unencountered type
            Color newColour = ValueColourMap.getColour(type);
            String tidString="";
            if (type instanceof NOMElement) { tidString = ((NOMElement)type).getID(); }
            else if (type instanceof String) { tidString = (String)type; }
            else { tidString = type.toString(); }
            Style newTemplate = ntv.addStyle(MULTI_STYLE_NAME+"_template_"+tidString,null);
            StyleConstants.setForeground(newTemplate, newColour);
            StyleConstants.setBold(newTemplate, true);
            StyleConstants.setFontSize(newTemplate, 12);
            ontologyToStyle.put(type, newTemplate);
        }
        return (Style)ontologyToStyle.get(type);
    }

}
