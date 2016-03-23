package net.sourceforge.nite.tools.linker;

import java.util.logging.*;
import java.io.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collection;
import java.util.Arrays;

import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.*;

import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.util.*;
import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.search.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.nstyle.handler.*;
import net.sourceforge.nite.time.*;
import net.sourceforge.nite.tools.dacoder.DACoder;
import net.sourceforge.nite.tools.discoursecoder.DECoder;
import net.sourceforge.nite.tools.necoder.NELinker;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.*;
import com.jgoodies.looks.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;


/**
 * This Display module displays links between two dialogue elements.
 */
public class LinkDisplayModule implements LinkChangeListener,NTASelectionListener,ActionListener,NOMWriteElementContainer {
    /*==================
      Initialization, connection to main tool
      ====================*/
    /**
     * For reference to NTV, observationname, nomcorpus......
     */
    //DACoder main;
    AbstractCallableTool main;
    AbstractDisplayElement linkelement;
    AbstractDisplayElement sourceelement;
    AbstractDisplayElement targetelement;
    String linkSourceRole;
    String linkTargetRole;

    /**
     * The panel that contains the element edit GUI interface
     */
    private NTextArea linkDisplay = new NTextArea();
    private JScrollPane scroller = new JScrollPane(linkDisplay);
    //private JPanel thePanel = new JPanel();
    private MyNTAChangeListener mcl = new MyNTAChangeListener();
    private Style pstyle, boldstyle;
    private String boldstylename="bold";

    /** This constructor passes all the required parameters for the
     * linker rather than individual calls to the set methods. */
    public LinkDisplayModule(AbstractCallableTool main, AbstractDisplayElement linkd, 
			     AbstractDisplayElement sourced, AbstractDisplayElement targetd,
			     String sourcerole, String targetrole) {
        this.main = main;
	this.linkelement=linkd;
	this.sourceelement=sourced;
	this.targetelement=targetd;
	this.linkSourceRole=sourcerole;
	this.linkTargetRole=targetrole;
        main.getNTV().addNTASelectionListener(this);
        createGUI();
	linkDisplay.addNTASelectionListener(mcl);
        setCurrentElement(null);
	main.getQueryHandler().registerResultHandler(linkDisplay);	
    }

    /**
     * The current link element
     */
    protected NOMWriteElement currentElement = null;
    public void setCurrentElement(NOMWriteElement element) {
        if (element != null) {
            if (!element.getName().equals(linkelement.getElementName())) {
                System.out.println("Wrong type of element in LinkDisplay : " + element.getName());
                return;
            }
        }
        currentElement = element;
    }

    public NOMWriteElement getCurrentElement() {
        return currentElement;
    }  

    /*===================
      GUI
      =====================*/


    public JComponent getPanel() {
        return scroller;
        //return thePanel;
    }


    /**
     * Creates GUI objects, arranges them in formlayout.
     * stores resulting panel in thePanel attribute.
     */
    protected void createGUI() {
	StyleContext sc = new StyleContext();
	pstyle = sc.addStyle("pink", null);	
	StyleConstants.setBackground(pstyle, Color.pink);
	boldstyle = sc.addStyle(boldstylename, null);	
	StyleConstants.setForeground(boldstyle, Color.blue);
	StyleConstants.setBold(boldstyle, true);
	//StyleConstants.setUnderline(pstyle, true);
	linkDisplay.setHighlightingStyle(NTextArea.USER_HIGHLIGHTS,pstyle);
	linkDisplay.addStyle(boldstylename, boldstyle);
        linkChanged();
    }


    /**
     * LinkChangeListener interface implementation. what if linkchange was
     from selection? then we don't want to redisplay everything :)
     this method just redisplays all links.
    */
    public void linkChanged() {
        linkDisplay.clear();
        linkDisplay.setEditable(false);
	// this is a hack to allow selection of the first Link.
	linkDisplay.addElement(new NTextElement(" ",null,null)); 

        Iterator elemIt = main.search("($a "+linkelement.getElementName()+")").iterator();
        if (elemIt.hasNext()){
            elemIt.next();
	    while (elemIt.hasNext()) {
		NOMElement nme = (NOMElement)((List)elemIt.next()).get(0);
                List pointers = nme.getPointers();
                String ssource = "<no source>";
                String starget = "<no target>";
                String stype = "<no type>";
                if (pointers!=null) {
                    Iterator pointersIt = pointers.iterator();
                    while (pointersIt.hasNext()) {
                        NOMPointer p = (NOMPointer)pointersIt.next();
                        //type
                        if (p.getRole().equals(linkelement.getTypeRole())) {
                            //get relation type
			    try {
				NOMElement type = p.getToElement();
				stype = (String)type.getAttributeComparableValue(linkelement.getTypeGloss());
				if ((stype == null) || (stype.equals(""))) {
				    stype = (String)type.getAttributeComparableValue("name");
				}            
			    } catch (Exception ex) { }
                        }
                        //source
                        if (p.getRole().equals(linkSourceRole)) {
                            NOMElement source = p.getToElement();
			    if (source==null) {
				System.out.println("FAILED to get pointer target from element: " + nme.getID());
				//return;
			    } else {
				//get source type
				List sl = source.getPointers();
				if (sl != null) {
				    Iterator slIt = sl.iterator();
				    while (slIt.hasNext()) {
					NOMPointer p2 = (NOMPointer)slIt.next();
					if (p2.getRole().equals(sourceelement.getTypeRole())) {
					    ssource = (String)p2.getToElement().getAttributeComparableValue(sourceelement.getTypeGloss());
					}
				    }
				}
                            }
                        }
                        //target
                        if (p.getRole().equals(linkTargetRole)) {
			    try {
				NOMElement target = p.getToElement();
				//get target type
				List tl = target.getPointers();
				if (tl != null) {
				    Iterator tlIt = tl.iterator();
				    while (tlIt.hasNext()) {
					NOMPointer p2 = (NOMPointer)tlIt.next();
					if (p2.getRole().equals(targetelement.getTypeRole())) {
					    starget = (String)p2.getToElement().getAttributeComparableValue(targetelement.getTypeGloss());
					}
				    }
				}
			    } catch (Exception ex) {
				// e.g. target is null...
			    }
                        }
                    }
                }

		String neatt = main.getConfig().getNXTConfig().getCorpusSettingValue("nelinkattribute");
		if (neatt!=null && neatt.length()!=0) {
		    String val = (String)nme.getAttributeComparableValue(neatt);
		    if (val!=null && val.length()>0) {stype = val; }
		}
		
                String text = ssource + " --" + stype + "-> " + starget;
                NOMObjectModelElement nome= new NOMObjectModelElement(nme);
                NTextElement nte1 = new NTextElement("\u2219 ", boldstylename, nome);
                linkDisplay.addElement(nte1);
                NTextElement nte = new NTextElement(text, null,
						    nme.getStartTime(), nme.getEndTime(), nome);
                //nte.setDataElement((ObjectModelElement)nome);
                linkDisplay.addElement(nte);
		linkDisplay.addElement(new NTextElement("\n ", null, null));
	    }
        }
        linkDisplay.invalidate();
        linkDisplay.repaint();
    }

    /* NTASelectionListener */
    /**
     * Depending on the state of the editormodule , this method reacts
     * appropriately to selection changes (change range, or set
     * current element)
     */
    public void selectionChanged() {
	// now look for changes in the main transcription window
        Set s = main.getNTV().getHighlightedTextElements(NTextArea.SELECTION_HIGHLIGHTS);
        if ((s != null)  && (s.size() > 0)) {
	    linkDisplay.clearHighlights(NTextArea.USER_HIGHLIGHTS);
	    for (Iterator sit=s.iterator(); sit.hasNext(); ) {
		NOMWriteElement nel = (NOMWriteElement)((NOMObjectModelElement)(((NTextElement)sit.next()).getDataElement())).getElement();
		String sname = sourceelement.getElementName();
		String tname = targetelement.getElementName();
		if (nel==null) { continue; }
		if (nel.getName().equals(sname) || nel.getName().equals(tname)) {
		    List ps = nel.getPointersTo();
		    if (ps==null) { continue; }
		    for (Iterator pit=ps.iterator(); pit.hasNext(); ) {
			NOMPointer np = (NOMPointer)pit.next();

			// jonathan - what about source vs target - different colours??

			NOMElement link = np.getFromElement();
			if (link==null) { continue; }
			if (link.getName().equals(linkelement.getElementName())) {
			    linkDisplay.setHighlighted(NTextArea.USER_HIGHLIGHTS, new NOMObjectModelElement(link));
			    this.setCurrentElement((NOMWriteElement)link);
			}
		    }
		}
            }
        }
    }
	
    /* ActionListener */

    public void actionPerformed (ActionEvent ev) {
	
    }

    class MyNTAChangeListener implements NTASelectionListener {
	public void MyNTAChangeListener() {

	}

	public void selectionChanged() {
	    Set s = linkDisplay.getHighlightedTextElements(NTextArea.SELECTION_HIGHLIGHTS);
	    // the user-highlights cause a prob if we click on the ready-highlighted one.
	    linkDisplay.clearHighlights(NTextArea.USER_HIGHLIGHTS);
	    Set us = linkDisplay.getHighlightedTextElements(NTextArea.USER_HIGHLIGHTS);
	    NOMWriteElement ls=null;
	    if ((s != null)  && (s.size() > 0)) {
		NOMWriteElement el = (NOMWriteElement)((NOMObjectModelElement)(((NTextElement)s.iterator().next()).getDataElement())).getElement();
		// jonathan - this needs to be improved!!
		if (main instanceof DECoder) {
		    ((DECoder)main).getlinkPane().setCurrentElement(el);
		} else if (main instanceof DACoder) {
		    ((DACoder)main).getapPane().setCurrentElement(el);
		} else if (main instanceof NELinker) {
		    ((NELinker)main).getLinkEditorPane().setCurrentElement(el);
		}
		ls = el;
	    }

	    if (ls!=null) 
		linkDisplay.setHighlighted(NTextArea.SELECTION_HIGHLIGHTS, new NOMObjectModelElement(ls));
	}
	
    }

}
