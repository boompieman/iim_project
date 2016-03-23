package net.sourceforge.nite.tools.discoursecoder;

import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.tools.dacoder.*;
import net.sourceforge.nite.tools.linker.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.nom.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.gui.mediaviewer.*;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.swing.text.*;
import javax.swing.*;

/**
 * Discourse Entity Annotation Tool
 *
 * This tool extends the dialogue act annotation tool DACoder, though
 * in fact the inheritance should perhaps be the other way round
 * really (and it may be so later on). The main purpose of the split
 * is to show that this is a general purpose tool that can code more
 * than just dialogue acts.
 *
 * @author Jonathan Kilgour, UEdin (DACoder by Dennis Reidsma, UTwente)
 * 
 **/
public class DECoder extends DACoder {

    DAEditorModule daPane;
    LinkEditorModule apPane;
    LinkDisplayModule apDisplayPane;

    // these should be settable in the configuration file
    private String linkNameShort="Relation";
    private String linkNameLong="Relationship";
    private String elementNameShort="DE";
    private String elementNameLong="Discourse Entity";

    /**
     * The main method for a subclassing tool is probably always the same: simply call
     * the constructor and pass any arguments.
     */
    public static void main(String[] args) {
        DECoder mainProg = new DECoder(args);
    }

    /**
     * This method starts with parsing the arguments passed to this tool, after which it calls
     * all those useful methods from the superclass that make development of a new tool such an
     * easy process.
     */
    public DECoder(String[] args) {
        /* mostly necessary: */
        parseArguments(args); //AbstractCallableTool.parseArguments parses the usual arguments: corpus, observation, annotator, usecvs.
        initConfig(); //cfreate the tool specific configuration class
        initLnF(); // Look and feel. Can be ignored/left out if you happen to like the default metal look-and-feel
        initializeCorpus(getCorpusName(),getObservationName()); //initialize the corpus, given the settings passed as arguments
        setupMainFrame(getConfig().getApplicationName()); //setup a main frame 
        setupDesktop(); //setup a desktop on which all the windows (media viewer, transcription view, etc) are added
        
        //* pretty much optional: *
        setupLog(Logger.global, 10, 615, 500, 90); //a logging window, useful for giving feedback to the user
        setupMediaPlayer(695,15,380,180); //a mediaplayer: necessary if you want video or audio players synchronized to the transcription view
        setupTranscriptionView(10,10,500,600); //one of the most important elements for many tools: a view of the transcription. Highly customizable in the methods initTranscriptionView and refreshTranscriptionView.
        setupSearch(); 
        setupActions();
        setupMenus();
        setupDECodingGui(); 
	getNTV().gotoDocumentStart();
        Logger.global.info("DECoder Initialization complete");
    }

    protected void setupDECodingGui() {
        setupEditorModules();
        setupInputMaps();
    }        

    public void initTranscriptionViewSettings() {
        super.initTranscriptionViewSettings();

        StringInsertDisplayStrategy ds=new StringInsertDisplayStrategy(getNTV()) {

            protected String formStartString(NOMElement element) {
                String name = element.getAgentName();
		String attrname=(String)element.getAttributeComparableValue("speaker");
		if (attrname != null) { return attrname + ": "; } 
		else if (name!=null) { return name + ": "; } 
		else { return ""; }
            }

        };
        ds.setEndString("");
        getNTV().setDisplayStrategy(((DACoderConfig)config).getSegmentationElementName(),ds);
      
        //display: how to visualize discourse elements: 
        //blue letters, some extra spacing, slightly larger font and dialog act type
        Style style = getNTV().addStyle("de-style",null);
        StyleConstants.setForeground(style,Color.blue);
        StringInsertDisplayStrategy ds2=new StringInsertDisplayStrategy(getNTV(), style) {
            protected String formStartString(NOMElement element) {
              //show type of da...
                String text = elementNameLong;
		String dat = ((DACoderConfig)getConfig()).getDAAttributeName();
		if (dat==null || dat.length()==0) {
		    List tl = element.getPointers();
		    if (tl != null) {
			Iterator tlIt = tl.iterator();
			while (tlIt.hasNext()) {
			    NOMPointer p2 = (NOMPointer)tlIt.next();
			    if (p2.getRole().equals(((DACoderConfig)getConfig()).getDATypeRole())) {
				text = ((String)p2.getToElement().getAttributeComparableValue(((DACoderConfig)getConfig()).getDAAGloss()));
			    }
			}
		    }
		} else {
		    text = (String)element.getAttributeComparableValue(dat);
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
        getNTV().setDisplayStrategy(((DACoderConfig)getConfig()).getDAElementName(),ds2);

        //selection: set of annotation element types that can be selected 
        Set s = new HashSet();
        s.add(((DACoderConfig)getConfig()).getDAElementName()); 
        getNTV().setSelectableAnnotationTypes(s);
      
    }


    /**
     * Initialize editor modules and put them on screen.
     */
    private void setupEditorModules(){
        daPane= new DAEditorModule(this, elementNameLong, elementNameShort);

	// Set up two display elements to pass to the linker
	// modules. This just makes it easier to avoid passing lots of
	// parameters.
	DACoderConfig daconf = (DACoderConfig)getConfig();
	AbstractDisplayElement daElement = new ConcreteDisplayElement(daconf.getDAElementName(),
			       null, daconf.getDATypeDefault(), daconf.getDATypeRoot(), daconf.getDATypeRole(), 
			       daconf.getDAAGloss(), elementNameLong, elementNameShort);
	AbstractDisplayElement apElement = new ConcreteDisplayElement(daconf.getAPElementName(),
			       null, daconf.getDefaultAPType(), daconf.getAPTypeRoot(), 
			       daconf.getNXTConfig().getCorpusSettingValue("aptyperole"), 
                               daconf.getAPGloss(), linkNameLong, linkNameShort);


        NITEMediaPlayer niteplayer = getMediaPlayer();
        if ((niteplayer != null) && (niteplayer instanceof NMediaPlayer)) {
            NMediaPlayer player = (NMediaPlayer)niteplayer;
            player.addSignalListener(daPane);
        }

        if (((DACoderConfig)getConfig()).showAdjacencyPairWindows()) {
	    // we're linking two daElements together with an apElement..
            apPane= new LinkEditorModule(this, apElement, daElement, daElement, 
				    daconf.getNXTConfig().getCorpusSettingValue("apsourcerole"),
				    daconf.getNXTConfig().getCorpusSettingValue("aptargetrole"));
            apPane.setDefaultType(getCorpus().getElementByID(daconf.getDefaultAPType()));
        }
        JInternalFrame daModFrame = new JInternalFrame ("Edit " + elementNameLong, true, false, true, true);
        SwingUtils.getResourceIcon(daModFrame, "/eclipseicons/eview16/editor_view.gif",getClass());
        daModFrame.getContentPane().add(daPane.getPanel());
        daModFrame.setSize(460,260);
        daModFrame.setLocation(520,460);
        daModFrame.setVisible(true);
        getDesktop().add(daModFrame);        
        // jonathan 16.12.04 - make adjacency pairs optional
        if (((DACoderConfig)getConfig()).showAdjacencyPairWindows()) {
	
            JInternalFrame apModFrame = new JInternalFrame ("Edit " + linkNameLong, true, false, true, true);
            SwingUtils.getResourceIcon(apModFrame, "/eclipseicons/eview16/editor_view.gif",getClass());
            apModFrame.getContentPane().add(apPane.getPanel());
            apModFrame.setSize(460,250);
            apModFrame.setLocation(520,10);
            apModFrame.setVisible(true);
            getDesktop().add(apModFrame);        

	    apDisplayPane= new LinkDisplayModule(this, apElement, daElement, daElement,
				    daconf.getNXTConfig().getCorpusSettingValue("apsourcerole"),
				    daconf.getNXTConfig().getCorpusSettingValue("aptargetrole"));
	    JInternalFrame apDisplayFrame = new JInternalFrame (linkNameShort + " Display", true, false, true, true);
	    SwingUtils.getResourceIcon(apDisplayFrame, "/net/sourceforge/nite/icons/logo/graph16.gif",getClass());
	    apDisplayFrame.getContentPane().add(apDisplayPane.getPanel());
	    apDisplayFrame.setSize(460,200);
	    apDisplayFrame.setLocation(520,260);
	    apDisplayFrame.setVisible(true);
	    apPane.addLinkChangeListener(apDisplayPane);
	    getDesktop().add(apDisplayFrame);
        }
    }

    /**
     * See {@link project.ami.textlabeler.DAAnnotationTool#ntv ntv} attribute.
     */
    public DAEditorModule getdaPane() {
        return daPane;
    }

    /**
     * This method should be diffferent for every tool version.
     * Get relevant actions from relevant editor modules, put them in central action map, create appropriate inputmaps...
     */    
    protected void setupInputMaps() {
        ActionMap amapDAMod = getdaPane().getActionMap();
        ActionMap amapAPMod = null;

        // jonathan 16.12.04 - make adjacency pairs optional
        if (((DACoderConfig)getConfig()).showAdjacencyPairWindows()) {
            amapAPMod = getlinkPane().getActionMap();
        }

            globalImap  = new ComponentInputMap(getNTV());
            globalAmap = new ActionMap();
            
            globalAmap.put(daPane.CHANGE_DA_RANGE_ACTION, amapDAMod.get(daPane.CHANGE_DA_RANGE_ACTION));
            globalImap.put(KeyStroke.getKeyStroke("typed r"), daPane.CHANGE_DA_RANGE_ACTION);
            globalImap.put(KeyStroke.getKeyStroke("typed R"), daPane.CHANGE_DA_RANGE_ACTION);

            globalAmap.put(daPane.NEW_DA_ACTION, amapDAMod.get(daPane.NEW_DA_ACTION));
            globalImap.put(KeyStroke.getKeyStroke("typed d"), daPane.NEW_DA_ACTION);
            globalImap.put(KeyStroke.getKeyStroke("typed D"), daPane.NEW_DA_ACTION);

            globalAmap.put(daPane.DELETE_DA_ACTION, amapDAMod.get(daPane.DELETE_DA_ACTION));
            globalImap.put(KeyStroke.getKeyStroke("DELETE"), daPane.DELETE_DA_ACTION);

            globalAmap.put(daPane.DELETE_DA_ACTION_NO_CONFIRM, amapDAMod.get(daPane.DELETE_DA_ACTION_NO_CONFIRM));
            globalImap.put(KeyStroke.getKeyStroke("shift DELETE"), daPane.DELETE_DA_ACTION_NO_CONFIRM);


        // jonathan 16.12.04 - make adjacency pairs optional
        if (((DACoderConfig)getConfig()).showAdjacencyPairWindows()) {
            globalAmap.put(apPane.NEW_LINK_ACTION, amapAPMod.get(apPane.NEW_LINK_ACTION));
            globalImap.put(KeyStroke.getKeyStroke("typed a"), apPane.NEW_LINK_ACTION);
            globalImap.put(KeyStroke.getKeyStroke("typed A"), apPane.NEW_LINK_ACTION);

            globalAmap.put(apPane.CHANGE_LINK_SOURCE_ACTION, amapAPMod.get(apPane.CHANGE_LINK_SOURCE_ACTION));
            globalImap.put(KeyStroke.getKeyStroke("typed s"), apPane.CHANGE_LINK_SOURCE_ACTION);
            globalImap.put(KeyStroke.getKeyStroke("typed S"), apPane.CHANGE_LINK_SOURCE_ACTION);

            globalAmap.put(apPane.CHANGE_LINK_TARGET_ACTION, amapAPMod.get(apPane.CHANGE_LINK_TARGET_ACTION));
            globalImap.put(KeyStroke.getKeyStroke("typed t"), apPane.CHANGE_LINK_TARGET_ACTION);
            globalImap.put(KeyStroke.getKeyStroke("typed T"), apPane.CHANGE_LINK_TARGET_ACTION);
            
            globalAmap.put(apPane.CANCEL_ACTION, amapAPMod.get(apPane.CANCEL_ACTION));
            globalImap.put(KeyStroke.getKeyStroke("ESCAPE"), apPane.CANCEL_ACTION);
        }

                getDesktop().setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,globalImap);
                getDesktop().setActionMap(globalAmap);

        getNTV().setInputMap(JComponent.WHEN_FOCUSED,globalImap);
        getNTV().setActionMap(globalAmap);
        getdaPane().getPanel().setInputMap(JComponent.WHEN_FOCUSED,globalImap);
        getdaPane().getPanel().setActionMap(globalAmap);

        // jonathan 16.12.04 - make adjacency pairs optional
        if (((DACoderConfig)getConfig()).showAdjacencyPairWindows()) {
            getlinkPane().getPanel().setInputMap(JComponent.WHEN_FOCUSED,globalImap);
            getlinkPane().getPanel().setActionMap(globalAmap);
        }
        logFrame.setInputMap(JComponent.WHEN_FOCUSED,globalImap);
        logFrame.setActionMap(globalAmap);
    }
   

    /**
     * See {@link project.ami.textlabeler.DAAnnotationTool#ntv ntv} attribute.
     */
    public LinkEditorModule getlinkPane() {
        return apPane;
    }

}

