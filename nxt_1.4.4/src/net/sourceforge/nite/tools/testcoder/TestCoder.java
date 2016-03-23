package net.sourceforge.nite.tools.testcoder;
import net.sourceforge.nite.util.*;

import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.nom.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;


import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.*;

/**
 * A demonstration implementation of the AbstractCallableTool.
 * Used to showcase all selection methods.
 *
 * @author Dennis Reidsma, UTwente
 */
public class TestCoder extends AbstractCallableTool {
   /*==================================================================================
                  CONSTRUCTION
     ==================================================================================*/
    protected void initConfig() {
        config = new TestCoderConfig();
    }

    /**
     * Same for every subclass! Document in AbstractCallableTool
     */
    public static void main(String[] args) {
    
    
    	TestCoder mainProg = new TestCoder(args);
    }
    /**
     * Constructor is more or less the same for all abstractcallabletools.
     * First store input vars, then init corpus,
     * then call a number of predefined initializationmethods.
     * Each new tool redefines this to get correct combination of elements.
     */
    public TestCoder(String[] args) {
        parseArguments(args);
        initConfig();
    	initializeCorpus(getCorpusName(),getObservationName());
    	setupMainFrame("Test Coder");
        initLnF(); // I prefer the inner frames to look different :-)
    	setupDesktop();
        //setupLog(Logger.global, 530, 530, 465, 90);
        setupMediaPlayer(695,15,380,180);
        setupTranscriptionView(15,15,500,600);
        

        setupSearch();
        setupActions();
        setupMenus();
        buildTestPanel();
        Logger.global.info("Initialization complete");
    }
    
    
/*====================================================================
    VERY IMPORTANT: THE SETTINGS OF THE TRANSCRIPTIONVIEW    
====================================================================*/

    /**
     * In this tool: segment name == XXX, transcript layer = XXX, 
     In other tools those things might be set interactively with user?
     */
    public void initTranscriptionViewSettings() {
        super.initTranscriptionViewSettings();
        
        StringInsertDisplayStrategy ds=new StringInsertDisplayStrategy(getNTV()) {
            protected String formStartString(NOMElement element) {
                String spaces = "";
                String agentName = element.getAgentName();
                if (agentName.equals("p1")) {
                    spaces = " ";
                } else if (agentName.equals("p2")) {
                    spaces = "  ";
                } else if (agentName.equals("p3")) {
                    spaces = "   ";
                }
                //if (getPersonForAgentName(agentName) != null) {
                //    return spaces + spaces + spaces + spaces + spaces + spaces + getPersonForAgentName(agentName).getAttributeComparableValue("name") + ": ";
                //} else {
                    return spaces + spaces + spaces + spaces + spaces + spaces + agentName + ": ";
                //}
            }
        };
        ds.setEndString("");
        getNTV().setDisplayStrategy(config.getSegmentationElementName(),ds);
       
       Style style  = getNTV().addStyle("dact-style",null);
        StyleConstants.setForeground(style,Color.blue);
        StringInsertDisplayStrategy ds2=new StringInsertDisplayStrategy(getNTV(), style) {
            protected String formStartString(NOMElement element) {
              //show type of da...
                String text = "Dialogue-act";
                List tl = element.getPointers();
                if (tl != null) {
                    Iterator tlIt = tl.iterator();
                    while (tlIt.hasNext()) {
                        NOMPointer p2 = (NOMPointer)tlIt.next();
                        if (p2.getRole().equals("da-aspect")) {
                            text = (String)p2.getToElement().getAttributeComparableValue("gloss");
                        }
                    }
                }
                String comm = element.getComment();
                if (comm == null) {
                    comm = "";
                } else if (!comm.equals("")) {
                    comm="***";
                }
                    
                return " " +comm+ " " + text + ": <";
            }
        };
        ds2.setEndString(">  ");
        getNTV().setDisplayStrategy("dact",ds2);
        
        getNTV().setSelectDirectTextRepresentations(true);
        getNTV().setSelectTranscriptionAncestors(true);
        Set types = new HashSet();
        types.add("dact");
        getNTV().setSelectableAnnotationTypes(types);
    }


    /**
     * This method builds the test panel that you can use to test al selection strategies etc.
     * It should be extended whenever the NTV gets new capabilities... For example, the 
     * 'select annotation by clicking on transcript' is not includedyet!
     */
    private void buildTestPanel() {
        //JInternalFrame jif = new JInternalFrame("Selection strategies", true, false, true, true);
        JInternalFrame jif = new JInternalFrame("Selection strategies");
        JPanel pan = new JPanel();
        JRadioButton b;
        JPanel p;
        p = new JPanel();
        p.setBorder(BorderFactory.createEtchedBorder());
        ButtonGroup bg1 = new ButtonGroup();
        b = new JRadioButton(new AbstractAction("multiagent") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setAllowMultiAgentSelect(true);
                                    }});
        bg1.add(b);
        p.add(b);
        b=new JRadioButton(new AbstractAction("NO multiagent") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setAllowMultiAgentSelect(false);
                                    }});
        bg1.add(b);
        p.add(b);
        pan.add(p);
        p = new JPanel();
        p.setBorder(BorderFactory.createEtchedBorder());
         JCheckBox tsb = new JCheckBox(new AbstractAction("text select") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setAllowTranscriptSelect(!getNTV().getAllowTranscriptSelect());
                                    }});
                                    tsb.setSelected(getNTV().getAllowTranscriptSelect());
        p.add(tsb);
        JCheckBox asb=new JCheckBox(new AbstractAction("annotationSelect") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setAllowAnnotationSelect(!getNTV().getAllowAnnotationSelect());
                                    }});
        asb.setSelected(getNTV().getAllowAnnotationSelect());

        p.add(asb);
        pan.add(p);
        p = new JPanel();
        p.setBorder(BorderFactory.createEtchedBorder());
        ButtonGroup bg3 = new ButtonGroup();
        b= new JRadioButton(new AbstractAction("oneword") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setWordlevelSelectionType(NTranscriptionView.ONE_WORD);
                                    }});
        p.add(b);
        bg3.add(b);
        b= new JRadioButton(new AbstractAction("segment") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setWordlevelSelectionType(NTranscriptionView.ONE_SEGMENT);
                                    }});
        p.add(b);
        bg3.add(b);
        b= new JRadioButton(new AbstractAction("multiseg") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setWordlevelSelectionType(NTranscriptionView.MULTIPLE_SEGMENTS);
                                    }});
        p.add(b);
        bg3.add(b);
        b= new JRadioButton(new AbstractAction("cross seg") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setWordlevelSelectionType(NTranscriptionView.CROSS_SEGMENT_PHRASE);
                                    }});
        p.add(b);
        bg3.add(b);
        b= new JRadioButton(new AbstractAction("in seg") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setWordlevelSelectionType(NTranscriptionView.IN_SEGMENT_PHRASE);
                                    }});
        p.add(b);
        bg3.add(b);
        pan.add(p);

        p = new JPanel();
        p.setBorder(BorderFactory.createEtchedBorder());
        ButtonGroup bg4 = new ButtonGroup();
        b= new JRadioButton(new AbstractAction("one annotation element") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setAnnotationSelectionGranularity(NTranscriptionView.SINGLE_ANNOTATION);
                                    }});
        p.add(b);
        bg4.add(b);
        b= new JRadioButton(new AbstractAction("multi annotation elements") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setAnnotationSelectionGranularity(NTranscriptionView.MULTIPLE_ANNOTATIONS);
                                    }});
        p.add(b);
        bg4.add(b);
        
        pan.add(p);

        p = new JPanel();
        p.setBorder(BorderFactory.createEtchedBorder());
        JCheckBox cb1 = new JCheckBox(new AbstractAction("Select direct text rep of annotation elements") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setSelectDirectTextRepresentations(!getNTV().getSelectDirectTextRepresentations());
                                    }});
                                    cb1.setSelected(getNTV().getSelectDirectTextRepresentations());
        p.add(cb1);
        JCheckBox cb2=new JCheckBox(new AbstractAction("Select annotation by clicking on transcript") {
                                    public void actionPerformed(ActionEvent e) {
                                        getNTV().setSelectTranscriptionAncestors(!getNTV().getSelectTranscriptionAncestors());
                                    }});
                                    cb2.setSelected(getNTV().getSelectTranscriptionAncestors());

        p.add(cb2);
        pan.add(p);
        
        jif.getContentPane().add(pan);
        jif.setVisible(true);                    
        jif.setSize(710,210);
        jif.setLocation(10,10);
        getDesktop().add(jif);
    }

    /** 
     * We don't want annotator specific codings in this test tool.
     */
    protected void initNomAnnotatorSpecificLoads(NOMWriteCorpus nom) throws NOMException {

    }

}
