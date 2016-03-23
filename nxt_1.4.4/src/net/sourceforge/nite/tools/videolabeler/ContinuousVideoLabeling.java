package net.sourceforge.nite.tools.videolabeler;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.beans.PropertyVetoException;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import java.io.IOException;
import java.io.File;
import org.xml.sax.SAXException;

import net.sourceforge.nite.gui.mediaviewer.NITEMediaPlayer;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.meta.NAgent;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.time.DefaultClock;
import net.sourceforge.nite.time.ClockFace;
import net.sourceforge.nite.search.*;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

public class ContinuousVideoLabeling {
    private JFrame mainFrame;
    private JDesktopPane desktop = null;
    private Rectangle videoBounds = new Rectangle();
    private Vector annotationSelectionListeners = new Vector();
    private Vector viewSelectionListeners = new Vector();
    private HashMap annotateAgentMenuItems = new HashMap();
    private HashMap annotateLayerMenuItems = new HashMap();
    private HashMap viewLayerMenuItems = new HashMap();
    private HashMap viewAgentMenuItems = new HashMap();

    static String annotatorName=null;
    public String getAnnotatorName() {
        return annotatorName;
    }
    static String corpusSettingsName=null;
    public String getCorpusSettingsName() {
        return corpusSettingsName;
    }
    static String guiSettingsName=null;
    public String getGUISettingsName() {
        return guiSettingsName;
    }
    
    public ContinuousVideoLabeling(String c, String o, String configFileName) {
        //DR: added metadata argument for specifying config file.
    	//update config !!!
        if (guiSettingsName!=null) {
            CSLConfig.getInstance().setGUISettings(guiSettingsName);
        }
        if (corpusSettingsName!=null) {
            CSLConfig.getInstance().setCorpusSettings(corpusSettingsName);
        }    	
    	if (configFileName != null) {
    	    try {
    	        //config name: if it is a relative path, we assume that it is a path relative to the metadatafile.
    	        if (!(new File(configFileName).isAbsolute())) {
    	            configFileName = new File(c).getParent()+"/"+configFileName;
    	        }
    	        CSLConfig.getInstance().loadConfig(configFileName);
    	    } catch (IOException ex) {
    	        System.out.println("Can't load config from file " + configFileName + " specified in metadata " + c + ", exiting. StackTrace:");
    	        ex.printStackTrace();
                System.exit(0);
    	    } catch (SAXException ex) {
    	        System.out.println("Can't load config from file " + configFileName + " specified in metadata " + c + ", exiting. StackTrace:");
    	        ex.printStackTrace();
                System.exit(0);
    	    }
    	} else { // load with a null config file (request config from user)
            try {
                CSLConfig.getInstance().loadConfig(configFileName);
            } catch (Exception ex) {
                System.out.println("Can't load config file. Exiting. StackTrace:");
                ex.printStackTrace();
                System.exit(0);
            }
        }    	
        CSLConfig.getInstance().setMetaDataFile(c);
        setupGeneralInterface();
        try {
            Document.createInstance(c,o,annotatorName);
        } catch (Exception ex) {
            System.out.println("ERROR: Could not load observation \"" + o + "\" from metadata file \"" + c + "\"");
            return;
        }
        setupSearch();
        setupMenus();
        setupVideo();
        //DR: a segmentreplayer can be used to control playback restricted to some timed segments. Useful if you only want to 
        //label some dimension, e.g. emotion trace, for selected segments (e.g. 'isinterestingforemotion')
        setupSegmentReplayer();
        setupFactories();
        mainFrame.setVisible(true);

        if (loneagent != null) {
        	loneagent.doClick();
        }
        if (lonelayer != null) {
        	lonelayer.doClick();
        }

    }

    /**
     * setup: general interface stuff, menus etc
     */
    private void setupGeneralInterface() {
        mainFrame = new JFrame("Continuous Signal Labeler");
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new ExitAction().actionPerformed(null);
            }
        });

        initLnF(); // I prefer the inner frames to look different :-)

        //desktop initialisation
        desktop = new JDesktopPane();
        desktop.setSize(new Dimension(1020, 730));
        desktop.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                layoutComponents();
            }
        });
        SelfSelectingFrames.setFramesSelfSelecting(desktop,true);
        GlobalInputMap map = GlobalInputMap.getInstance();
        desktop.setActionMap(map.getActionMap());
        desktop.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,map.getInputMap());
        mainFrame.getContentPane().add(desktop);

        //do show 
        mainFrame.setSize(new Dimension(1020, 730));
        mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
    }
    
    int i = 0;
    
    /**
     * The search window which is used, not internally, but externally for the user.
     */
    protected net.sourceforge.nite.search.GUI searchGui;
    /**
     * See {@linkplain #searchGui}
     */
    protected void setupSearch() {
        Document doc = Document.getInstance();
        searchGui = new net.sourceforge.nite.search.GUI(doc.getCorpus());
    }    

    /** 
     * Initializes the look and feel to something more elegant. Others might disagree and override this method...
     */
    protected void initLnF() {
    
	System.out.println("CVL init");
    
        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        System.setProperty("sun.awt.noerasebackground","true");        
        try {
            PlasticLookAndFeel.setCurrentTheme(new DesertBlue());
            Options.setPopupDropShadowEnabled(true);
            Options.setUseNarrowButtons(true);
            Options.setUseSystemFonts(true);
            Options.setDefaultIconSize(new Dimension (16,16));
            Plastic3DLookAndFeel olaLF = new Plastic3DLookAndFeel();
            UIManager.setLookAndFeel(olaLF);
            //update(c);
        } catch (Exception ex) {
            System.out.println("Error in LookaNdFeelSupport!");ex.printStackTrace();
        }
    }


	private JCheckBoxMenuItem loneagent = null;
	private JCheckBoxMenuItem lonelayer = null;
 
    private void setupMenus() {
        //menu bar construction
        JMenuBar menubar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.add(new SaveAction());
        file.add(new ExitAction());
        menubar.add(file);
        
        // add annotate menu
        JMenu annotate = new JMenu("Annotate");
        // add agents
        JMenuItem labelItem = new JMenuItem("Select an agent");
        labelItem.setEnabled(false);
        annotate.add(labelItem);
        Document doc = Document.getInstance();
        Iterator it = doc.getAgents().iterator();
        while (it.hasNext()) {
            NAgent agent = (NAgent)it.next();
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new AnnotateAgentAction(agent));
            annotate.add(item);
            annotateAgentMenuItems.put(agent,item);
            if(doc.getAgents().size() == 1) {
				loneagent = item;
            }
        }
        annotate.addSeparator();
        // add layers
        labelItem = new JMenuItem("Select a layer");
        labelItem.setEnabled(false);
        annotate.add(labelItem);
        it = doc.getLayers().iterator();
        while (it.hasNext()) {
            AnnotationLayer layer = (AnnotationLayer)it.next();
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new AnnotateLayerAction(layer));
            annotate.add(item);
            annotateLayerMenuItems.put(layer,item);
            if(doc.getLayers().size() == 1) {
				lonelayer = item;
            }
        }
        menubar.add(annotate);

        // add view menu
        JMenu view = new JMenu("View");
        // add layers
        labelItem = new JMenuItem("Select a layer");
        labelItem.setEnabled(false);
        view.add(labelItem);
        it = doc.getLayers().iterator();
        while (it.hasNext()) {
            AnnotationLayer layer = (AnnotationLayer)it.next();
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new ViewLayerAction(layer));
            view.add(item);
            viewLayerMenuItems.put(layer,item);
        }

	// search added by jonathan (similar to necoder code by Dennis)
        if (searchGui != null) {
            //Search action
            Action act = new AbstractAction("Search") {
                public void actionPerformed(ActionEvent ev) {
                    searchGui.popupSearchWindow();
                }
            };
            act.putValue(Action.SHORT_DESCRIPTION,"Search the corpus.");
            act.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK ));
            act.putValue(Action.MNEMONIC_KEY,new Integer(java.awt.event.KeyEvent.VK_S));
	    view.addSeparator();
	    view.add(act);
            //actMap.put(SEARCH_ACTION, act);
        }

        menubar.add(view);

        //add help menu
        String helpHS = CSLConfig.getInstance().getHelpSetName();
        HelpSet hs = null;
        ClassLoader cl = getClass().getClassLoader();
	JMenu help = new JMenu("Help");
        try {
            URL hsURL = HelpSet.findHelpSet(cl, helpHS);
            hs = new HelpSet(null, hsURL);
            HelpBroker hb = hs.createHelpBroker();
            final ActionListener al = new CSH.DisplayHelpFromSource( hb );
            //Help action
            Action act = new AbstractAction("Help") {
                public void actionPerformed(ActionEvent ev) {
                    al.actionPerformed(ev);
                }
            };
            act.putValue(Action.SHORT_DESCRIPTION,"Show help pages.");
            act.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
            act.putValue(Action.MNEMONIC_KEY,new Integer(java.awt.event.KeyEvent.VK_H));
            help.add(act);
        } catch (Exception ee) {
            System.out.println("ERROR: HelpSet " + ee.getMessage());
            System.out.println("ERROR: HelpSet "+ helpHS +" not found");
        }   

	menubar.add(help);

        mainFrame.setJMenuBar(menubar);
        
    }

    /**
     * setup: media player
     */
    private void setupVideo() {
        //DR: 2005.05.18 removed all old code here, now simply make clockface visible. videos can be shown by selecting them from dropdownbox. NMediaPlayer is now no longer used
/*        Document doc = Document.getInstance();
        JInternalFrame clockFace = doc.getClock().getDisplay();
        clockFace.setLocation(10,400);
        desktop.add(clockFace);*/
        setupMediaPlayer(10,400,380,180);
        /*NMediaPlayer video = new NMediaPlayer(doc.getMetaData(),doc.getObservation().getShortName(),(DefaultClock)doc.getClock());
        video.setLocation(10,10);
        video.setClosable(false);
        video.setSize(clockFace.getWidth(),380);
        desktop.add(video);
        videoBounds = new Rectangle(10,10,video.getWidth(),
                clockFace.getY() + clockFace.getHeight() - video.getY());*/
    }
    
    /**
     * Creates a NITEMediaPlayer including signal selection list for the
     * current corpus/observation and adds it to the current desktop at the given 
     * coordinates.
     * <p>Pre: Corpus should be loaded; desktop should be created.
     * <p>Post: The videoplayer is created and visible and can be retrieved using 
     * getMediaPlayer();
     */
    protected void setupMediaPlayer(int x, int y, int width, int height) {
        System.out.println("=======================================");
        Document doc = Document.getInstance();
	
	    doc.getClock().ensureVisible(new Rectangle(x,y,width,height), desktop);

        System.out.println("=======================================");
    }

    protected ClockFace clockFace = null;
    /** 
     * Returns the clockface of this class. A clockface is the 'play control panel'
     * for the video and audio, and in this tool for the virtual meeting animation as well.
     * Before you initialize this, you must have at least a mainframe, a desktop and an initialized 
     * corpus.
     */
    public ClockFace getClockFace() {
        //return clockFace;
        Document doc = Document.getInstance();
	    return (ClockFace)doc.getClock().getDisplay();
    }
	
	protected SegmentReplayer replayer;
	protected void setupSegmentReplayer() {
	    //check config: segmentreplayer in use?
	    if (!CSLConfig.getInstance().getUseSegmentReplayer()) {
	        return;
	    }
	    replayer = new SegmentReplayer(getClockFace());
	    //hack:
	    //check config: what's the name of the elements?
	    String segname = CSLConfig.getInstance().getNXTConfig().getCorpusSettingValue("replaysegmentname");
	    List segments = new ArrayList();
	    //force load :-(
	    Iterator it = search("($a "+segname+")").iterator();
	    if (!it.hasNext()) {
	        return;
	    }
	    it.next();
	    while (it.hasNext()) {
	        segments.add(((List)it.next()).get(0));
	    }
	    replayer.setAvailableSegments(segments);
	    JInternalFrame jif = new JInternalFrame("a", true,false,false,true);
	    jif.getContentPane().add(replayer.getGuiPanel());
	    jif.setVisible(true);
	    jif.setLocation(0,0);
	    jif.setSize(100,100);
	    desktop.add(jif);
	}
	
    private void setupFactories() {
        AnnotationFrameFactory aff = AnnotationFrameFactory.createInstance(desktop,new Rectangle(0,0,0,0));
	aff.setSearch(searchGui);
        annotationSelectionListeners.add(aff);
        ViewFrameFactory vff = ViewFrameFactory.createInstance(desktop,new Rectangle(0,0,0,0));
	vff.setSearch(searchGui);
        viewSelectionListeners.add(vff);
        TimelineFactory tlf = TimelineFactory.createInstance(desktop,new Rectangle(0,0,0,0), Document.getInstance().getCorpus());
        tlf.setSearch(searchGui);
        tlf.setClock(Document.getInstance().getClock());
        viewSelectionListeners.add(tlf);     
    }

    private void layoutComponents() {
        AnnotationFrameFactory aff = AnnotationFrameFactory.getInstance();
        if (aff != null) {
            int left = videoBounds.x + videoBounds.width + 10;
            int top = 10;
            int width = desktop.getWidth() - left - 10;
            int height = desktop.getHeight() - 50;
            aff.setArea(new Rectangle(left,top,width,height));
        }
        ViewFrameFactory vff = ViewFrameFactory.getInstance();
        if (vff != null) {
            int left = 10;
            int top = videoBounds.y + videoBounds.height + 10;
            int width = desktop.getWidth() - 20;
            int height = (desktop.getHeight() - top - 40) / 2;
            vff.setArea(new Rectangle(left,top,width,height));
        }
        TimelineFactory tlf = TimelineFactory.getInstance();
        if (tlf != null) {
            int left = 10;
            int top = videoBounds.y + videoBounds.height + 10;
            int width = desktop.getWidth() - 20;
            int height = (desktop.getHeight() - top - 40) / 2;
            top += height; // put it below vff
            tlf.setArea(new Rectangle(left,top,width,height));
        }
    }

    /**
     *  Saves the Corpus
     */
    public class SaveAction extends AbstractAction {
        public SaveAction() {
            super("Save corpus");
            putValue(Action.SHORT_DESCRIPTION,"Save corpus.");
            putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK ));
            GlobalInputMap.getInstance().addKeyStroke(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK ),this);
        }
        public void actionPerformed(ActionEvent ev) {
            try {
                Document.getInstance().save();
            } catch (NOMException ex) {
                System.out.println("ERROR: Could not save corpus: " + ex.getMessage());
            }
        }
    }

    /** 
     * A very simple "exit" action 
     */
    public class ExitAction extends AbstractAction {
        public ExitAction() {
            super("Exit");
        }
        public void actionPerformed(ActionEvent ev) {
            Document doc = Document.getInstance();
            if (doc.checkSave()) {
                DefaultClock clock = (DefaultClock)doc.getClock();
		if (clock.getMediaHandler()!=null) {
		    Object[] handlers = clock.getMediaHandler().playingHandlers.toArray();
		    for (int i = 0; i < handlers.length; i++) {
			if (handlers[i] instanceof NITEMediaPlayer) {
			    ((NITEMediaPlayer)handlers[i]).close();
			}
		    }
		}
		System.exit(0);
	    }
        }
    }

    public class AnnotateAgentAction extends AbstractAction {
        private NAgent agent;

        public AnnotateAgentAction(NAgent agent) {
            super(agent.getShortName());
            this.agent = agent;
        }

        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem menu = (JCheckBoxMenuItem)annotateAgentMenuItems.get(agent);
            Iterator it = annotationSelectionListeners.iterator();
            while (it.hasNext()) {
                SelectionListener l = (SelectionListener)it.next();
                l.agentSelected(agent,menu.isSelected());
            }
        }
    }

    public class AnnotateLayerAction extends AbstractAction {
        private AnnotationLayer layer;

        public AnnotateLayerAction(AnnotationLayer layer) {
            super(layer.getNLayer().getName());
            this.layer = layer;
        }

        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem menu = (JCheckBoxMenuItem)annotateLayerMenuItems.get(layer);
            Iterator it = annotationSelectionListeners.iterator();
            while (it.hasNext()) {
                SelectionListener l = (SelectionListener)it.next();
                l.layerSelected(layer,menu.isSelected());
            }
        }
    }
	
    public class ViewLayerAction extends AbstractAction {
        private AnnotationLayer layer;

        public ViewLayerAction(AnnotationLayer layer) {
            super(layer.getNLayer().getName());
            this.layer = layer;
        }

        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem menu = (JCheckBoxMenuItem)viewLayerMenuItems.get(layer);
            Iterator it = viewSelectionListeners.iterator();
            while (it.hasNext()) {
                SelectionListener l = (SelectionListener)it.next();
                l.layerSelected(layer,menu.isSelected());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length % 2 != 0) {
            usage();
            return;
        }
        String corpus = null;
        String observation = null;
        String configFileName = null;
        for (int i = 0; i < args.length/2; i++) {
            String arg = args[i*2];
            String val = args[i*2+1];
            if (arg.equals("-corpus")) {
                if (corpus == null)
                    corpus = val;
                else {
                    System.out.println("ERROR: found command-line argument \"-corpus\" twice");
                    usage();
                    return;
                }
            } else if (arg.equals("-observation")) {
                if (observation == null)
                    observation = val;
                else {
                    System.out.println("ERROR: found command-line argument \"-observation\" twice");
                    usage();
                    return;
                }
            } else if (arg.equals("-annotator")) {
                if (annotatorName == null)
                    annotatorName = val;
                else {
                    System.out.println("ERROR: found command-line argument \"-annotator\" twice");
                    usage();
                    return;
                }
            } else if (arg.equals("-config")) {
                if (configFileName == null)
                    configFileName = val;
                else {
                    System.out.println("ERROR: found command-line argument \"-config\" twice");
                    usage();
                    return;
                }
            }  else if (arg.equals("-corpus-settings")) {
                if (corpusSettingsName == null)
                    corpusSettingsName = val;
                else {
                    System.out.println("ERROR: found command-line argument \"-corpus-settings\" twice");
                    usage();
                    return;
                }
            }  else if (arg.equals("-gui-settings")) {
                if (guiSettingsName == null)
                    guiSettingsName = val;
                else {
                    System.out.println("ERROR: found command-line argument \"-gui-settings\" twice");
                    usage();
                    return;
                }
            } else {
                System.out.println("WARNING: unknown command-line argument: " + arg);
            }
        }
        
        ContinuousVideoLabeling mainProg = new ContinuousVideoLabeling(corpus,observation, configFileName);
    }

    private static void usage() {
        System.err.println("Usage: java net.sourceforge.nite.tools.videolabeler.ContinuousVideoLabeling " +
                "-corpus <<metadata-filename>> -observation <<observation-name>> [-config <<configuration-filename>> -corpus-settings <<corpus-settings-node-id>> -gui-settings <<gui-settings-node-id>>] [-annotator <<annotator-name>>]");
    }

//=========================== SEARCH =============================================


   /** 
    *Search engine to find annotation elements 
    */
    protected Engine searchEngine= new Engine();
    protected List search(String query) {
        Document doc = Document.getInstance();
        List result = null;
    	try {
    	    result = searchEngine.search(doc.getCorpus(), query); 
    	} catch (Throwable e) {
    	    e.printStackTrace();
    	}
    	if (result == null || result.size() == 1) {
    	    System.err.println("NO DATA OF ELEMENTS IN CORPUS! ");
    	}
    	return result;
    }
}
