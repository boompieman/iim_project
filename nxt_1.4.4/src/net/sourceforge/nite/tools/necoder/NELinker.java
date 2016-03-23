package net.sourceforge.nite.tools.necoder;

import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.nom.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.tools.linker.*;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/**
 * A Named entity coder that adds linking between named entities using
 * the newly added tools.linker package.  
 *
 * @author Jonathan Kilgour, UEdin
 */
public class NELinker extends NECoder {
    private LinkEditorModule linkEditorPane;
    private LinkDisplayModule linkDisplayPane;

    /**
     * Same for every subclass! Document in AbstractCallableTool
     */
    public static void main(String[] args) {
    	NELinker mainProg = new NELinker(args);
    }

    /**
     * Constructor is more or less the same for all abstractcallabletools.
     * First store input vars, then init corpus,
     * then call a number of predefined initializationmethods.
     * Each new tool redefines this to get correct combination of elements.
     */
    public NELinker(String[] args) {
	super(args);
	setupLinker();
    }

    private void setupLinker() {
	System.out.println("Link");
	NECoderConfig neconf = (NECoderConfig)getConfig();
	AbstractDisplayElement neElement = new ConcreteDisplayElement(neconf.getNEElementName(),
			       neconf.getNEAttributeName(), neconf.getNETypeDefault(),
			       neconf.getNETypeRoot(), neconf.getNETypePointerRole(), 
                               neconf.getNEAbbrevAttrib(), "Named Entity", "NE");
	AbstractDisplayElement linkElement = new ConcreteDisplayElement(
			       neconf.getNXTConfig().getCorpusSettingValue("nelinkelementname"), 
			       neconf.getNXTConfig().getCorpusSettingValue("nelinkattribute"), 
			       neconf.getNXTConfig().getCorpusSettingValue("nelinkontology") + "#" +
			       neconf.getNXTConfig().getCorpusSettingValue("nelinktypedefault"), 
			       neconf.getNXTConfig().getCorpusSettingValue("nelinkontology") + "#" +
			       neconf.getNXTConfig().getCorpusSettingValue("nelinkroot"), 
			       neconf.getNXTConfig().getCorpusSettingValue("nelinktyperole"), 
			       neconf.getNXTConfig().getCorpusSettingValue("nelinktypegloss"), 
			       "Relation", "Rel");


	linkDisplayPane= new LinkDisplayModule(this, linkElement, neElement, neElement,
				    neconf.getNXTConfig().getCorpusSettingValue("nelinksourcerole"),
				    neconf.getNXTConfig().getCorpusSettingValue("nelinktargetrole"));
	linkEditorPane=new LinkEditorModule(this, linkElement, neElement, neElement, 
				    neconf.getNXTConfig().getCorpusSettingValue("nelinksourcerole"),
				    neconf.getNXTConfig().getCorpusSettingValue("nelinktargetrole"));	

	linkEditorPane.setDefaultType(getCorpus().getElementByID(linkElement.getTypeDefault()));
	JInternalFrame linkEditorFrame = new JInternalFrame ("Edit Relations", true, false, true, true);
	SwingUtils.getResourceIcon(linkEditorFrame, "/eclipseicons/eview16/editor_view.gif",getClass());
	linkEditorFrame.getContentPane().add(linkEditorPane.getPanel());
	linkEditorFrame.setSize(460,250);
	linkEditorFrame.setLocation(520,10);
	linkEditorFrame.setVisible(true);
	getDesktop().add(linkEditorFrame);        


	JInternalFrame linkDisplayFrame = new JInternalFrame ("Relation Display", true, false, true, true);
	SwingUtils.getResourceIcon(linkDisplayFrame, "/net/sourceforge/nite/icons/logo/graph16.gif",getClass());
	linkDisplayFrame.getContentPane().add(linkDisplayPane.getPanel());
	linkDisplayFrame.setSize(460,300);
	linkDisplayFrame.setLocation(520,260);
	linkDisplayFrame.setVisible(true);
	linkEditorPane.addLinkChangeListener(linkDisplayPane);
	getDesktop().add(linkDisplayFrame);
	
    }

    public LinkEditorModule getLinkEditorPane() {
	return linkEditorPane;
    }

    /** subclass NECoder, adding an annotator specific layer for link */
    protected void initNomAnnotatorSpecificLoads(NOMWriteCorpus nom) throws NOMException {
	super.initNomAnnotatorSpecificLoads(nom);
	if (getAnnotatorName()!=null) {
	    try {
		NECoderConfig cfg = (NECoderConfig)getConfig();
		NElement neel = nom.getMetaData().getElementByName(getConfig().getNXTConfig().getCorpusSettingValue("nelinkelementname"));
		nom.forceAnnotatorCoding(getAnnotatorName(), ((NCoding)neel.getLayer().getContainer()).getName());
	    } catch (Exception ex) { }
	}
	
    }

}
