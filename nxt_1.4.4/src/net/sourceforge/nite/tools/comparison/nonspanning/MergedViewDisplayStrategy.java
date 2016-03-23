package net.sourceforge.nite.tools.comparison.nonspanning;

import java.awt.Color;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import net.sourceforge.nite.gui.textviewer.NTextElement;
import net.sourceforge.nite.gui.transcriptionviewer.AbstractDisplayStrategy;
import net.sourceforge.nite.gui.transcriptionviewer.NTranscriptionView;
import net.sourceforge.nite.gui.util.ValueColourMap;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.nom.nomwrite.NOMAttribute;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.NOMPointer;
import net.sourceforge.nite.nxt.NOMObjectModelElement;
import net.sourceforge.nite.nxt.ObjectModelElement;
import net.sourceforge.nite.util.Predicate;
import net.sourceforge.nite.util.Debug;

/** 
 
 EXTREMELY SPECIFIC
 
 */
public class MergedViewDisplayStrategy extends AbstractDisplayStrategy {
	
	/**Documentation as in superclass?**/
	protected final String NE_STYLE_NAME="NE_MV_STYLE"; //Merged View
	
	protected final String ANNO_STYLE_NAME = NE_STYLE_NAME + "_ANNOTATOR_";
	protected final Style autoTemplate;
	protected final Style manualTemplate;
	protected final Style dualTemplate;
	
	/**Documentation as in superclass?**/
	protected static int idcounter = 0;
	protected String neTypeRole = "";
	protected String abbrevAttrib = "";
	protected String attributeName = "";
	
	protected NLayer annotatorlayer = null;
	protected String annotatorattr = "";
	protected String autoannotatorname = "";
	protected String manualannotatorname = "";
	
	/* Which annotators to display */
	protected int annotatordisplay = 0;
	protected final int ANN_DISPLAY_AUTO = 0x1;
	protected final int ANN_DISPLAY_MAN = 0x2;
	
	/* see styletext() for info. Defaults to true. */
	boolean styleinlinetext = true;
	
	/* see setlogging() for info. Defaults to true. */
	boolean logging = true;
	
	/***********************************CONSTRUCTORS**********************************/
	
	/** 
	 * Initialize neTypeRole is the role of the pointer to the NE
	 * ontology; abbrevAttrib is the name of the attrib in the ne
	 * ontology that isused in the 'brackets'; attributeName is
	 * non-null if we should display an attribute of the named entity
	 * rather than a pointer to a type ontology (jonathan 25/3/6)
	 */  
	public MergedViewDisplayStrategy (NTranscriptionView ntv, String neTypeRole, String abbrevAttrib, String attributeName, NLayer annotatorlayer, String annotatorattr, String autoannotatorname, String manualannotatorname) {
		init(ntv);
		this.neTypeRole = neTypeRole;
		this.abbrevAttrib = abbrevAttrib;
		// this last
		this.attributeName = attributeName;
		
		this.annotatorlayer = annotatorlayer;
		this.annotatorattr = annotatorattr;
		this.autoannotatorname = autoannotatorname;
		if (autoannotatorname.length() > 0)
			annotatordisplay |= ANN_DISPLAY_AUTO;
		this.manualannotatorname = manualannotatorname;
		if (manualannotatorname.length() > 0)
			annotatordisplay |= ANN_DISPLAY_MAN;
		
		// Generate two new annotator styles to append to the NE styles
		autoTemplate = ntv.addStyle(ANNO_STYLE_NAME+"_template_auto",null);
		StyleConstants.setUnderline(autoTemplate, true);     
		manualTemplate = ntv.addStyle(ANNO_STYLE_NAME+"_template_manual",null);
		StyleConstants.setItalic(manualTemplate, true);
		dualTemplate = ntv.addStyle(ANNO_STYLE_NAME+"_template_dual",null);
		StyleConstants.setUnderline(dualTemplate, true);     
		StyleConstants.setItalic(dualTemplate, true);
		
		// Add style for uncoloured elements
		Color newColour = Color.black;
		String tidString="DEFAULT";
		Style newTemplate = ntv.addStyle(NE_STYLE_NAME+"_template_"+tidString,null);
		StyleConstants.setForeground(newTemplate, newColour);
		StyleConstants.setBold(newTemplate, true);
		StyleConstants.setFontSize(newTemplate, 12);
		ontologyToStyle.put("", newTemplate);
	}
	
	
	/********************************END-OF-CONSTRUCTORS********************************/
	
	public Style getann1style() {
		return autoTemplate;
	}
	
	public Style getann2style() {
		return manualTemplate;
	}
	
	public Style getdualstyle() {
		return dualTemplate;
	}
	
	public void showautoannotator() {
		annotatordisplay |= ANN_DISPLAY_AUTO;
	}
	
	public void hideautoannotator() {
		annotatordisplay &= ~ANN_DISPLAY_AUTO;
	}

	public void showmanualannotator() {
		annotatordisplay |= ANN_DISPLAY_MAN;
	}

	public void hidemanualannotator() {
		annotatordisplay &= ~ANN_DISPLAY_MAN;
	}
	
	public boolean getdisplayautoannotator() {
		return (annotatordisplay & ANN_DISPLAY_AUTO) > 0;
	}
	
	public boolean getdisplaymanualannotator() {
		return (annotatordisplay & ANN_DISPLAY_MAN) > 0;
	}
	
	public void setannotatordisplay(int anndisplay) {
		annotatordisplay = anndisplay;
	}
	/**
	 * Do we want to style the text pointed to by the annotation element?
	 * Set this before displaying any text.
	 **/
	public void styletext(boolean flag) {
		styleinlinetext = flag;
	}
	
	/**
	 * Do we want to notify user of display failure?
	 * Set this before displaying any text.
	 **/
	public void setlogging(boolean flag) {
		logging = flag;
	}

	/**
	 * Display annotation element by changing the text style of all text for all relevant transcription elements.
	 * <p>
	 * @return true if displayed successfully, i.e. if the text to which given element relates was actually
	 * visible in the NTranscriptionView.
	 */
	public boolean display(NOMElement element) {
		if (annotatordisplay == 0) {
			// Nothing to display
			return true;
		}
		int firstpos=-1;
		int lastpos=0;
		Set l = getTextElements(element);  
		if ((l == null) || (l.size()<=0)){
			if(logging) {
			    Debug.print("Error: No text for current entity", Debug.DEBUG);
				return false;
			} else {
				return true;
			}
		}
		Iterator it = l.iterator();
		NTextElement next = null;
		//determine colour/style for element
		Style ent_style = getColouredStyle(element);
		Style ann_style = getAnnotatorStyle(element);
		
		String ent_style_name;
		if (ent_style == null) {
			ent_style_name = "ENT_STYLE";
		} else {
			ent_style_name = ent_style.getName();
		}
		
		String ann_style_name;
		if (ann_style == null) {
			ann_style_name = "ANN_STYLE";
		} else {
			ann_style_name = ann_style.getName();
		}

		Style bracket_style = ntv.addStyle(ann_style_name+"_"+ent_style_name, null);
		
		if (ent_style != null) {
			Enumeration e = ent_style.getAttributeNames();
			while (e.hasMoreElements()) { //this is annoying. addAttributes(Style) will also copy the NAME attribute, meaning that the wrong style will be added later on because the name attribute is no longer correct :(
				Object nextKey = e.nextElement();
				if (!(nextKey.equals(Style.NameAttribute)||(nextKey.equals(Style.ResolveAttribute)))) {
					bracket_style.addAttribute(nextKey, ent_style.getAttribute(nextKey));
				}
			}
		}
	    
		if (ann_style != null) {
			Enumeration e = ann_style.getAttributeNames();
			while (e.hasMoreElements()) { //this is annoying. addAttributes(Style) will also copy the NAME attribute, meaning that the wrong style will be added later on because the name attribute is no longer correct :(
				Object nextKey = e.nextElement();
				if (!(nextKey.equals(Style.NameAttribute)||(nextKey.equals(Style.ResolveAttribute)))) {
					bracket_style.addAttribute(nextKey, ann_style.getAttribute(nextKey));
				}
			}
		}

	    //idcounter++;
		//if (idcounter == Integer.MAX_VALUE) {
		//	idcounter = Integer.MIN_VALUE;
		//}

		//System.out.println("ent_style: " + ent_style + ", ann_style: " + ann_style);
		if (ann_style == null) {
			// No annotator - don't display
			if(logging) {
			    Debug.print("ERROR: Annotator name not found.", Debug.WARNING);
				return false;
			} else {
				return true;
			}
		}
		if (ann_style != null) {
			while (it.hasNext()) {
				next=(NTextElement)it.next();
				if (styleinlinetext) {
					ntv.insertCopyOfStyle(next,ann_style,NE_STYLE_NAME+String.valueOf(idcounter));
				}
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
		
		String val="";
		NOMPointer p = element.getPointerWithRole(neTypeRole) ;
		// changed by jonathan 25/3/06 to allow for attribute type
		// rather than pointer to ontology.
		if (p != null) {
			val = (String)p.getToElement().getAttributeComparableValue(abbrevAttrib);
		} else if (attributeName!=null && attributeName.length()>0) {
			val = (String)element.getAttributeComparableValue(attributeName);
		}
		if (p!=null || val!=null) {
			//use annotation element as dataElement!
			NOMObjectModelElement nome=new NOMObjectModelElement(element);
			//add postfix
			//System.out.println("INSERT for element " + nome.getID());
			NTextElement endStringElement=
				new NTextElement(") ",bracket_style.getName(),lastpos,element.getStartTime(), element.getEndTime(), nome);
			
			// jean wanted me to change this! Jonathan 9/3/5
			//new NTextElement("<"+val+"> ",ent_style.getName(),lastpos,element.getStartTime(), element.getEndTime());
			//endStringElement.setDataElement(nome);
			ntv.insertElement (endStringElement,lastpos);            
			//ntv.insertCopyOfStyle(endStringElement,ann_style,ANNO_STYLE_NAME+"_"+NE_STYLE_NAME+String.valueOf(idcounter));
			
			//add prefix
			NTextElement startStringElement=
				new NTextElement("("+val+". ",bracket_style.getName(),firstpos,element.getStartTime(), element.getEndTime(), nome);
			//new NTextElement("<"+val+"> ",ent_style.getName(),firstpos,element.getStartTime(), element.getEndTime(), nome);
			//startStringElement.setDataElement(nome);
			ntv.insertElement (startStringElement,firstpos);
			//ntv.insertCopyOfStyle(startStringElement,ann_style,ANNO_STYLE_NAME+"_"+NE_STYLE_NAME+String.valueOf(idcounter));
		}
		return true;
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
		//TODO: Complete this to handle auto/manual transcription
		Debug.print("ERROR: MergedViewDisplayStrategy::undisplay() incomplete.", Debug.WARNING);
		
		Set l = getTextElements(element);
		if ((l == null) || (l.size()<=0))
			return;
		Predicate isMyTextStyle = new Predicate() {
			public boolean valid(Object o) {
				return ((Style)o).getName().startsWith(NE_STYLE_NAME);
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
	protected Style getColouredStyle(NOMElement element) {
		Style parent = null; // getAnnotatorStyle(element);
		
		NOMPointer p = element.getPointerWithRole(neTypeRole) ;
		if (p != null) {
			return getTemplateStyle(p.getToElement(), parent);
		}
		if (attributeName!=null && attributeName.length()>0) {
			return getTemplateStyle((String)element.getAttributeComparableValue(attributeName), parent);
		}
		if (p == null) {
			return getTemplateStyle(null, null); //default style
		}
		return null;
	}
	
	/**
	 * Return template style for given annotator for an annotation element that points to ontology, or null if no annotator or annotator not known
	 */
	public Style getAnnotatorStyle(NOMElement element) {
		NOMAttribute a;
		if (annotatorlayer == null) {
			a = element.getAttribute(this.annotatorattr);
			// System.out.println("a: " + a.getName() + "=" + a.getStringValue());
			
			Debug.print("First element: " + (transToAnnoMap.getTransElementsForAnnotationElement(element)).iterator().next(), Debug.PROGRAMMER);
			
			if (a == null) {
				return null;
			}
			
			if ( ((annotatordisplay & ANN_DISPLAY_AUTO) != 0) && a.getStringValue().equals(this.autoannotatorname)) {
				return autoTemplate;
			} else if ( ((annotatordisplay & ANN_DISPLAY_MAN) != 0) && a.getStringValue().equals(this.manualannotatorname) ) {
				return manualTemplate;
			}
		} else {
			boolean foundauto = false;
			boolean foundmanual = false;
			Set s = element.findAncestorsInLayer(annotatorlayer);
			Iterator i = s.iterator();
			while (i.hasNext()) {
				NOMElement p = (NOMElement)(i.next());
				if(p == null) {
					Debug.print("ERROR: No ancestor found for " + element.getName() + " in layer " + annotatorlayer.getName() + ".", Debug.WARNING);
					return null;
				}
				a = p.getAttribute(this.annotatorattr);
				if ( ((annotatordisplay & ANN_DISPLAY_AUTO) != 0) && a.getStringValue().equals(this.autoannotatorname)) {
					foundauto = true;
				} else if ( ((annotatordisplay & ANN_DISPLAY_MAN) != 0) && a.getStringValue().equals(this.manualannotatorname)) {
					foundmanual = true;
				}
			}
			
			if (foundauto && foundmanual) { return dualTemplate; }
			if (foundauto) { return autoTemplate; } 
			if (foundmanual) { return manualTemplate; }
		}
		
		return null;
	}
	
	Map ontologyToStyle = new HashMap();
	/**
	 * Return template style for given ontology element or attribute value
	 */
	protected Style getTemplateStyle(Object type, Style parent) {
		if (type==null) { type=""; }
		if (ontologyToStyle.get(type) == null) {
			//create new style for this hitherto unencountered type
			Color newColour = ValueColourMap.getColour(type);
			String tidString="";
			if (type instanceof NOMElement) { tidString = ((NOMElement)type).getID(); }
			else if (type instanceof String) { tidString = (String)type; }
			else { tidString = type.toString(); }
			String pname = "";
			if (parent != null) {
				pname = "_" + parent.getName();
			}
			Style newTemplate = ntv.addStyle(NE_STYLE_NAME+"_template_"+tidString+pname,parent);
			StyleConstants.setForeground(newTemplate, newColour);
			StyleConstants.setBold(newTemplate, true);
			StyleConstants.setFontSize(newTemplate, 12);
			ontologyToStyle.put(type, newTemplate);
		}
		return (Style)ontologyToStyle.get(type);
	}
	
}
