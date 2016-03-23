package net.sourceforge.nite.tools.dacoder;

import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.util.Debug;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.nom.*;
import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.query.*;
import net.sourceforge.nite.tools.linker.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.logging.*;
import java.io.*;


/**
 * Dialogue Act Annotation Tool
 *
 * This tool extends the AbstractCallableTool, offering functionality
 * for annotating dialogue acts and relations between them on a
 * transcribed corpus. A lot of the most important functionality is
 * derived directly from the
 * {@link net.sourceforge.nite.gui.util.AbstractCallableTool
 * superclass}, so don't forget to read that documentation!
 * Documentation on the additions in this tool will be added in due
 * time... whenever I find the time, basically.
 * @author Dennis Reidsma, UTwente
 * 
 **/
public class DACoder extends AbstractCallableTool implements NOMView {

   /*==================================================================================
                  CONSTRUCTION
     ==================================================================================*/
    protected void initConfig() {
        config = new DACoderConfig();
    }
    /**
     * The main method for a subclassing tool is probably always the same: simply call
     * the constructor and pass any arguments.
     */
    public static void main(String[] args) {
        DACoder mainProg = new DACoder(args);
    }
    /**
     * Call this superclass-constructor if you make an extension of the DACoder; 
     * then choose your own initialization inspired by what is in the other constructor of DACoder
     */
    public DACoder() {
        
    }
    /**
     * This method starts with parsing the arguments passed to this tool, after which it calls
     * all those useful methods from the superclass that make development of a new tool such an
     * easy process.
     */
    public DACoder(String[] args) {
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
        setupSearch(); //search functinoality can be included by default in all subclassing tools. Search results can be highlighted in the transcription view.
        setupActions();
        setupMenus();
        setupDACodingGui(); //ook uitsplitsen in tweeen en aanroepen met de coordinaten als parameter (en de NTV, corpus,. cofig)
        getNTV().gotoDocumentStart();
        Logger.global.info("DACoder Initialization complete");
        
        
    }

    /**
     * In the DACoder the following codings are loaded as annotator specific:
     * the codings containing the dialogue act elements, the adjacency pair elements.
     * <br>
     * The following methods are available to use here:
     * <br><b>nom.forceAnnotatorCoding(annotatorname, {name of coding})</b>: 
     *     The given coding MUST be loaded annotator specific. If it doesn't exist for this annotator, don't
     *     load data for this coding.
     * <br><b>nom.preferAnnotatorCoding(annotatorname, {name of coding})</b>: 
     *     The given coding will preferably be loaded annotator specific. If it doesn't exist for this annotator, 
     *     load the 'main' data for this coding.
     * <br><b>nom.setDefaultAnnotator(annotatorname)</b>: Prefer the given anntoator for all codings, unless
     *     overridden by one of the above methods.
     * 
     */
    protected void initNomAnnotatorSpecificLoads(NOMWriteCorpus nom) throws NOMException {
        if (getAnnotatorName()!=null) {
            Set specificCodings = new HashSet(); //collect in a set, because they may be stored in the same coding
            DACoderConfig cfg = (DACoderConfig)getConfig();
            NElement dael = nom.getMetaData().getElementByName(cfg.getDAElementName());
            if (dael != null) {
                specificCodings.add(dael.getLayer().getContainer());
            }
            NElement apel = nom.getMetaData().getElementByName(cfg.getAPElementName());
            if (apel != null) {
                specificCodings.add(apel.getLayer().getContainer());
            }
            Iterator it = specificCodings.iterator();
            while (it.hasNext()) {
                nom.forceAnnotatorCoding(getAnnotatorName(), ((NCoding)it.next()).getName());
            }
        }
    }
        
    protected void setupDACodingGui() {
        setupEditorModules();
        setupInputMaps();
    }        
    
//===================== NOMSHARED INTERFACING ===================================

    /**
     * The DACoder should be a NOMView, to be able to lock the corpus and notify changes.
     *
     * AT THE MOMENT IT DOES NOT DO ANYTHING WHEN IT RECEIVES AN EDIT
     */
    public void handleChange(NOMEdit change) {
        System.out.println("External change: " + change);
    }

    
//===================== NEW GLOBAL ACTIONS ===================================

    /**
     * Not operational yet
     */
    public static final String SETUP_KEYS_ACTION = "Change key settings";
    /**
     * New actions: none
     */
    protected void setupActions() {
        super.setupActions();
        ActionMap actMap = getActionMap();
        Action act = null;
        NTranscriptionView ntv = getNTV();
        //no extension
    }
        
    /** 
     * No new menu options in fileM
     */
    protected void setupMenus() {
        super.setupMenus();
        Map menus = getMenuMap();
        JMenu fileM = (JMenu)menus.get("File");
    }

  



//======================== special view ===========================

    public void initTranscriptionViewSettings() {
        super.initTranscriptionViewSettings();
        
        StringInsertDisplayStrategy ds=new StringInsertDisplayStrategy(getNTV()) {
           
            protected String formStartString(NOMElement element) {
		return getNTV().getSegmentText(element) + ": ";		
            }
        };
        ds.setEndString("");
        getNTV().setDisplayStrategy(((DACoderConfig)config).getSegmentationElementName(),ds);
        //display: how to visualize dialogue acts: 
        //blue letters, some extra spacing, slightly larger font and dialog act type
        Style style  = getNTV().addStyle("dact-style",null);
        StyleConstants.setForeground(style,Color.blue);
        StringInsertDisplayStrategy ds2=new StringInsertDisplayStrategy(getNTV(), style) {
            protected String formStartString(NOMElement element) {
		// adding check if segmentation and dact element are the same
		// - concatenate start string - not ideal really! JK 7.8.8
		String inittext="";
		if (((DACoderConfig)config).getSegmentationElementName().equals(((DACoderConfig)getConfig()).getDAElementName())) {
		    inittext = getNTV().getSegmentText(element) + ": ";
		}
		//show type of da...
                String text = "Dialogue-act";
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
                    
                return " " +comm+ " " + inittext + text + ": <";
            }
        };
        ds2.setEndString(">  ");
        getNTV().setDisplayStrategy(((DACoderConfig)getConfig()).getDAElementName(),ds2);

        //selection: set of annotation element types that can be selected 
        Set s = new HashSet();
        s.add(((DACoderConfig)getConfig()).getDAElementName()); 
        getNTV().setSelectableAnnotationTypes(s);
      
    }


    //public Logger performancelogger;
    protected void setupLog(Logger log, int x, int y, int w, int h) {
        super.setupLog(log,x,y,w,h);
        
        //performancelogger = Logger.getLogger("performance");
        /*String path = getMetaData().getCodingPath();
        String name="log";
        long postfix = System.currentTimeMillis();
        try {
            FileHandler fh = new FileHandler(path+"/"+name+postfix);
            performancelogger.addHandler(fh);
        } catch (IOException ex) {
        }*/
        //performancelogger.info("Logging started");
    }


         
    
    
    
/*==================================================================================
                SETUP METHODS
==================================================================================*/
    
   
    /**
     * Initialize editor modules and put them on screen.
     */
    private void setupEditorModules(){
        daPane= new DAEditorModule(this);

        NITEMediaPlayer niteplayer = getMediaPlayer();
        if ((niteplayer != null) && (niteplayer instanceof NMediaPlayer)) {
            NMediaPlayer player = (NMediaPlayer)niteplayer;
            player.addSignalListener(daPane);
        }

	// Set up two display elements to pass to the linker
	// modules. This just makes it easier to avoid passing lots of
	// parameters.
	DACoderConfig daconf = (DACoderConfig)getConfig();
	AbstractDisplayElement daElement = new ConcreteDisplayElement(daconf.getDAElementName(),
			       null, daconf.getDATypeDefault(), daconf.getDATypeRoot(), daconf.getDATypeRole(), 
			       daconf.getDAAGloss(), "Dialogue Act", "DA");
	AbstractDisplayElement apElement = new ConcreteDisplayElement(daconf.getAPElementName(),
			       null, daconf.getDefaultAPType(), daconf.getAPTypeRoot(), 
			       daconf.getNXTConfig().getCorpusSettingValue("aptyperole"), 
                               daconf.getAPGloss(), "Adjacency Pair", "AP");

	String apsr=daconf.getNXTConfig().getCorpusSettingValue("apsourcerole");
	String aptr=daconf.getNXTConfig().getCorpusSettingValue("aptargetrole");

        // jonathan 16.12.04 - make adjacency pairs optional
        if (daconf.showAdjacencyPairWindows()) {
	    // we're linking two daElements together with an apElement..
            apPane= new LinkEditorModule(this, apElement, daElement, daElement, apsr, aptr);
            apPane.setDefaultType(getCorpus().getElementByID(daconf.getDefaultAPType()));
        }
        JInternalFrame daModFrame = new JInternalFrame ("Edit Dialogue Acts", true, false, true, true);
        SwingUtils.getResourceIcon(daModFrame, "/eclipseicons/eview16/editor_view.gif",getClass());
        daModFrame.getContentPane().add(daPane.getPanel());
        daModFrame.setSize(460,260);
        daModFrame.setLocation(520,460);
        daModFrame.setVisible(true);
        getDesktop().add(daModFrame);        
        // jonathan 16.12.04 - make adjacency pairs optional
        if (((DACoderConfig)getConfig()).showAdjacencyPairWindows()) {
	
            JInternalFrame apModFrame = new JInternalFrame ("Edit Adjacency Pairs", true, false, true, true);
            SwingUtils.getResourceIcon(apModFrame, "/eclipseicons/eview16/editor_view.gif",getClass());
            apModFrame.getContentPane().add(apPane.getPanel());
            apModFrame.setSize(460,250);
            apModFrame.setLocation(520,10);
            apModFrame.setVisible(true);
            getDesktop().add(apModFrame);        

	    apDisplayPane= new LinkDisplayModule(this, apElement, daElement, daElement, apsr, aptr);
	    JInternalFrame apDisplayFrame = new JInternalFrame ("Adjacency Pairs", true, false, true, true);
	    SwingUtils.getResourceIcon(apDisplayFrame, "/net/sourceforge/nite/icons/logo/graph16.gif",getClass());
	    apDisplayFrame.getContentPane().add(apDisplayPane.getPanel());
	    apDisplayFrame.setSize(460,200);
	    apDisplayFrame.setLocation(520,260);
	    apDisplayFrame.setVisible(true);
	    apPane.addLinkChangeListener(apDisplayPane);
	    getDesktop().add(apDisplayFrame);
        }
    }

    
    protected InputMap globalImap;
    protected ActionMap globalAmap;
    protected InputMap getglobalImap(){
        return globalImap;
    }
    protected ActionMap getglobalAmap(){
        return globalAmap;
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
            amapAPMod = getapPane().getActionMap();
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
            getapPane().getPanel().setInputMap(JComponent.WHEN_FOCUSED,globalImap);
            getapPane().getPanel().setActionMap(globalAmap);
        }
        logFrame.setInputMap(JComponent.WHEN_FOCUSED,globalImap);
        logFrame.setActionMap(globalAmap);
    }
   
           
  
   


/*=======================
  variables and accessors
  =======================*/
    /**
     * The DAElementPane will be made private, and available through an accessor.
     * This makes it easier to avoid initialization problems. If you use an accessor, the pane
     * doesn't have to be initialized before you create e.g. the delete action.
     */
    DAEditorModule daPane;//[DR: naming!
    /**
     * See {@link project.ami.textlabeler.DAAnnotationTool#ntv ntv} attribute.
     */
    public DAEditorModule getdaPane() {
        return daPane;
    }
    /**
     * The APElementPane will be made private, and available through an accessor.
     * This makes it easier to avoid initialization problems. If you use an accessor, the pane
     * doesn't have to be initialized before you create e.g. the delete action.
     */
    LinkEditorModule apPane;
    /**
     * See {@link project.ami.textlabeler.DAAnnotationTool#ntv ntv} attribute.
     */
    public LinkEditorModule getapPane() {
        return apPane;
    }
    /**
     * The APDisplayPane will be made private, and available through an accessor.
     * This makes it easier to avoid initialization problems. If you use an accessor, the pane
     * doesn't have to be initialized before you create e.g. the delete action.
     */
    LinkDisplayModule apDisplayPane;
    /**
     * See {@link project.ami.textlabeler.DAAnnotationTool#ntv ntv} attribute.
     */
    public LinkDisplayModule getapDisplayPane() {
        return apDisplayPane;
    }
    



/*========================
  Pointless utility methods which are better off forgotten...
  ========================*/

    /**
     * Extremely dependent on a particular corpus structure...
     */
    protected NOMElement getPersonForAgentName(String agentName) {
        //Iterator i = search("($h person)(exists $p participant):($p@"+getMetaData().getObservationAttributeName() +"==\"" + getObservationName() + "\") && ($p>$h) && ($p@"+getMetaData().getAgentAttributeName() +"==\"" + agentName + "\")").iterator();
        //if (i.hasNext()) {
	//  i.next();
        //}
        NOMElement result = null;
        //if (i.hasNext()) {
	//  result = (NOMElement)((List)i.next()).get(0);
        //}
        return result;
    }

    /* allows sub-components to search */
    public List search(String s) {
	return super.search(s);
    }


}
