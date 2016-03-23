/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * IMS, University of Stuttgart // HCRC, University of Edinburgh
 * Holger Voormann, Halyna Seniv
 * edited by Jonathan Kilgour.
 */
package net.sourceforge.nite.search;

/* This class was created so that the popup search window would
 * implement query.QueryHandler: ths allows search results to be
 * highlighted in other displays.
 */

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

//import net.sourceforge.nite.web.DemoExample;

import net.sourceforge.nite.util.Debug;
import net.sourceforge.nite.util.CorpusViewer;
import net.sourceforge.nite.util.HTMLViewer;
import net.sourceforge.nite.gui.util.ChooseObservation;
import net.sourceforge.nite.meta.impl.NiteMetaData;
import net.sourceforge.nite.meta.NObservation;
import net.sourceforge.nite.query.*;
import net.sourceforge.nite.corpus.*;
import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.nom.nomwrite.NOMCorpus;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.NOMAttribute;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteCorpus;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;

import ims.jmanual.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;

/**
 * Simple grahic user interface for {@linkplain Engine}.
 * GUI offers a window with a text form to type in the query, a
 * submit button, a result panel and some other functions.
 * @author Holger Voormann, Halyna Seniv */
public class GUI
    extends JFrame
    implements WindowListener, ActionListener, CaretListener, KeyListener,
	       Runnable, HelpViewerListener, QueryHandler
{
    // the top three are additions by Jonathan Kilgour
    private boolean mainWindow=true; // true if we're the controller
    private Vector queryHandlers = new Vector(); // of type QueryHandler
    private HashMap treehash= new HashMap(); // store the match elements.

    // added by Elaine Farrow
    private final java.util.List/*<SimpleQueryResultHandler>*/ handlers = new ArrayList/*<SimpleQueryResultHandler>*/();

    //  private static final boolean DEBUGGING = true;
    private static final boolean DEBUGGING = false;

    private static final String DAT_SERIALISATION = ".nxtSearch";
    public static final String TITLE = "NXT Search Version 0.26";

    private static final String ERROR_INTERRUPT_SAVE = "Quering was interrupted - results is not complete. Save anyway?";
    private static final String ERROR_OVERWRITE_SAVE = "The file already exists. Overwrite?";
    private static final String ERROR_CANNOT_WRITE   = "The file is inaccessible,\nmaybe because the file\nis opened by another application.\n\nTry again?\n";
    private static final String ERROR_OUT_OF_MEMORY  = "Sorry, by too many matching results an out of memory error occured.\nPlease use a smaller corpus or/and a stricter query.\n";
    //---------------------------------------------------------------------------
    private boolean nom = true;
    //---------------------------------------------------------------------------

    private JTabbedPane      mainPanel    = new JTabbedPane();
    private JTextArea        qin          = new JTextArea(12, 42);
    //  private JTextArea        qout         = new JTextArea(8, 40);

    //  private JTree            qout         = new JTree(top);
    private JSplitPane       out          = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private DefaultMutableTreeNode top    = new DefaultMutableTreeNode("loading...");
    private JTree            outTree      = new JTree(top);

    //  private ResultVisualisationComponent outFocus = new SimpleResultVisualisationComponent(this);
    private ResultVisualisationComponent outFocus = new TableResultVisualisationComponent(this);

    private String           lastQuery    = "";
    private SearchableCorpus corpus       = null;
    private String           loadedCorpusPath = null;

    private boolean          corpusLoaded = false;
    private boolean          corpusClosed = false;
    private Engine           searchEngine = new Engine();
    private ProgressWindow   progressWindow;

    //  private net.sourceforge.nite.web.DemoExample webDemo = null;

    private static final int MAX          = 8;
    private Preferences      preferences  = null;
    private PreferenceLoader prefloader   = new PreferenceLoader(System.getProperty("user.home"));
    private java.util.List   corpora      = new ArrayList();
    private HashMap          corporaPaths = new HashMap();
    private HashMap          corporaNames = new HashMap();


    private ArrayList        bookmarksNames    = new ArrayList();
    private ArrayList        bookmarksQueries  = new ArrayList();
    private boolean          autoload     = false;

    // Set the following to true if this is a client application
    // (specifically so we don't get caught by the preferences problems!)
    // jonathan 30.11.04
    private boolean          client     = false; 
    private java.util.List   allResults   = null;
    private JMenu            corpusMenu   = null;
    private JMenu            bookmarksMenu  = null;
    private JMenuItem        helpCorpus  = null;
    private JMenuItem        querySubmit  = null;
    private JMenuItem        corpusExit   = null;
    private JMenuItem        corpusOpen   = null;
    private JMenuItem        corpusReload = null;
    private JMenuItem        corpusClose  = null;
    private JCheckBoxMenuItem autoloadCorpus = null;
    private JButton          submit       = null;
    private JFileChooser     fc           = null;
    private ImageIcon        iconCorpus     = null;
    private ImageIcon        iconCorpusOpen = null;
    private JLabel           statusBar      = null;
    private JMenuItem        addBookmark    = null;
    private JMenu            deleteBookmark = null;
    private JMenuItem save = new JMenuItem("Save ...", 'S');
    private JMenuItem exportAsExcel = new JMenuItem("Export As Excel ...", 'E');
    private String           corpusName     = "Results";

    private File             helpDir;
    private File             corporaDir;

    public boolean showTime = true;

    /**
     * Creats a new {@linkplain #GUI()}.
     * @param args parameter isn't used
     */
    public static void main(String[] args) {

	//setLookAndFeel
	//	try {
	//		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
	//	} catch( Exception e ){} //skip

	String installDir = "";

	boolean looking     = true;
	boolean nextInstall = false;
	boolean version     = false;
	for( int i=0; i<args.length; i++ ){
	    String x = args[i];
	    if(looking){

		//looking
		looking = false;
		if(  x.equalsIgnoreCase("install")
		     || x.equalsIgnoreCase("-install") ){
		    nextInstall = true;
		} else if(  x.equalsIgnoreCase("version")
			    || x.equalsIgnoreCase("-version") ){
		    version = true;
		} else {
		    looking = true;
		}

	    } else {

		//setting
		if( nextInstall ){
		    nextInstall = false;
		    installDir = x;
		}

	    }
	}

	// print version
	if(version){
	    System.out.print(TITLE);
	    return;
	}

	// show window
	GUI gui = new GUI(installDir);    
    }

    /**
     * Sets up a search window as a slave where we have already loaded a
     * corpus. Doesn't open a window (use popupSearchWindow to do that).  
     * Added by Jonathan Kilgour 23/1/04.
     */
    public GUI(SearchableCorpus nom) {
	client=true; // make sure we dont autoload!
	mainWindow=false;
	setupGUI("");
	corpus=nom;
	corpusLoaded=true;
	submit.setEnabled(true);       //search button
	querySubmit.setEnabled(true);  //menu: query - search enabled
	helpCorpus.setEnabled(true);   //menu: corpus help enabled
	corpusMenu.setEnabled(false); //can't change corpus
    }

    
    /**
     * Opens a new Search window
     */
    public GUI(String installDir) {
	setupGUI(installDir);
	setVisible(true);
    }

    private void setupGUI(String installDir) {
	addWindowListener(this);
	addKeyListener(this);
    
	//window icon
	setIconImage( loadImage("/net/sourceforge/nite/icons/misc/nxt_search.gif") );

	// load images
	iconCorpus     = loadIcon("/net/sourceforge/nite/icons/misc/corpus.gif");
	iconCorpusOpen = loadIcon("/net/sourceforge/nite/icons/misc/corpus_open.gif");

	// load preferences
	preferences = prefloader.getPreferences();
	corpora          = preferences.corpora;
	corporaNames     = preferences.names;
	bookmarksNames   = preferences.bookmarksNames;
	bookmarksQueries = preferences.bookmarksQueries;
	autoload         = preferences.autoloaded;


	//System.out.println("Preferences:\n corpora: " +  corpora);
	//System.out.println("Preferences:\n autoload: " +  autoload);

	//menubar
	JMenuBar menubar = new JMenuBar();
	setJMenuBar(menubar);
	//-------------------------------------
	//vorgezogene Deklarationen wegen autoload
	statusBar = new JLabel();
	getContentPane().add( statusBar, BorderLayout.SOUTH);
	querySubmit = new JMenuItem("Search", 'S');
	querySubmit.setActionCommand("submit");
	querySubmit.setEnabled(false);
	querySubmit.addActionListener(this);

	//-------------------------------------
	submit = new JButton( "Search" );
	submit.setActionCommand("submit");
	submit.addActionListener(this);
	submit.setEnabled(false);
	//-------------------------------------
	//menu: Corpus
	corpusMenu = new JMenu("Corpus");
	corpusMenu.setMnemonic('C');
	// Switch added by Jonathan Kilgour
	if (mainWindow) { menubar.add(corpusMenu); }
	corpusOpen   = new JMenuItem("Open ...", 'O');
	corpusReload = new JMenuItem("Reload", 'R');
	corpusClose  = new JMenuItem("Close", 'C');
	corpusExit   = new JMenuItem("Exit", 'X');
	corpusExit.setIcon( loadIcon("/net/sourceforge/nite/icons/misc/exit.gif") );

	corpusOpen.setActionCommand("open");
	corpusOpen.addActionListener(this);
	corpusOpen.setIcon( loadIcon("/net/sourceforge/nite/icons/misc/open_corpus.gif") );

	corpusReload.setActionCommand("reload");
	corpusReload.addActionListener(this);
	corpusReload.setIcon( loadIcon("/net/sourceforge/nite/icons/misc/reload_corpus.gif") );

	corpusClose.setActionCommand("close");
	corpusClose.addActionListener(this);
	corpusClose.setIcon( loadIcon("/net/sourceforge/nite/icons/misc/close_corpus.gif") );
	corpusExit.setActionCommand("exit");
	corpusExit.addActionListener(this);

	autoloadCorpus = new JCheckBoxMenuItem("Autoload");
	autoloadCorpus.setState(autoload);
	//    autoloadCorpus.setIcon( loadIcon("/net/sourceforge/nite/icons/misc/autoload.gif") );
	//autoloadCorpus.setActionCommand("autoload");
	//autoloadCorpus.addActionListener(this);

	//    generateJMenuItems("init");

	loadCorpus(null);

	//menu: Query
	JMenu queryMenu = new JMenu("Query");
	queryMenu.setMnemonic('Q');
	menubar.add(queryMenu);
	//Query - Submit
	queryMenu.add(querySubmit);

	//menu: Result
	JMenu resultMenu = new JMenu("Result");
	resultMenu.setMnemonic('R');
	menubar.add(resultMenu);
	//Result - Save
	save.setActionCommand("save");
	save.addActionListener(this);
	save.setEnabled(false);
	resultMenu.add(save);
	//Result - Export As Excel
	exportAsExcel.setActionCommand("exportAsExcel");
	exportAsExcel.addActionListener(this);
	exportAsExcel.setEnabled(false);
	resultMenu.add(exportAsExcel);

	//menu: Debug
	if( DEBUGGING ){
	    JMenu debugMenu = new JMenu("Debug");
	    debugMenu.setMnemonic('D');
	    menubar.add(debugMenu);
	    //Debug - Show Parse Tree
	    JMenuItem debugTree = new JMenuItem("Show Parse Tree", 'T');
	    debugTree.setActionCommand("debugTree");
	    debugTree.addActionListener(this);
	    debugMenu.add(debugTree);
	    //Debug - Show DNF
	    JMenuItem debugDNF = new JMenuItem("Show DNF", 'D');
	    debugDNF.setActionCommand("dnf");
	    debugDNF.addActionListener(this);
	    debugMenu.add(debugDNF);
	    //Debug - Web Demo
	    JMenuItem debugWeb = new JMenuItem("Web Demo", 'W');
	    debugWeb.setActionCommand("web");
	    debugWeb.addActionListener(this);
	    debugMenu.add(debugWeb);
	}

	// bookmarks
	bookmarksMenu = new JMenu("Bookmarks");
	bookmarksMenu.setMnemonic('B');
	menubar.add(bookmarksMenu);
	addBookmark = new JMenuItem("Add Bookmark ...", 'A');
	addBookmark.setActionCommand("addBookmark");
	addBookmark.addActionListener(this);
	bookmarksMenu.add(addBookmark);
	if ( bookmarksNames.size() > 0 ) {
	    deleteBookmark = makeSubMenu("Delete Bookmark", "delete", 'D');
	    bookmarksMenu.add(deleteBookmark);
	    bookmarksMenu.addSeparator();
	    for( int i=0; i<bookmarksNames.size(); i++ ) {
		bookmarksMenu.add( makeMenuItem( (String)bookmarksQueries.get(i), "bookmark"+i, this) );
	    }
	}

	//set directories (help and corpora)
	helpDir    = new File( System.getProperty("user.dir") );
	try {
	    helpDir = new File( installDir + File.separator + "lib" );
	} catch( Exception e ){}  //skip
	// new default corpus dir: start in the last directory you opened a corpus from
	try {
	    corporaDir = new File((String)corpora.get(0)).getParentFile();
	} catch (Exception e) {
	    corporaDir = new File( System.getProperty("user.dir") );
	}

	// JAK deleted this default - I think it is completely unused.
	/*
	try {
	    corporaDir = new File( installDir + File.separator + "corpora" );
	} catch( Exception e ){}  //skip
	*/

	//help
	JMenu helpMenu = new JMenu("Help");
	helpMenu.setMnemonic('H');
	menubar.add(helpMenu);
	//JManual: User's Manual
	try {
	    //JManual - help system
	    HelpViewer helpview = new HelpViewer(
						 "helpset",    //name main file (without .hs)
						 "NXT Search - User's Manual", //window title
						 "titlepage",  //homeID
						 this,        //listener
						 this);       //parent
	    //         helpview.setBounds(config.getHelpWindowX(),
	    //                            config.getHelpWindowY(),
	    //                            config.getHelpWindowWidth(),
	    //                            config.getHelpWindowHeight());
	    //         helpview.setSeparatorX(config.getHelpWindowSeparatorX());
	    //help menu
	    JMenuItem helpJMI = new JMenuItem("Query Language Help", 'Q');
	    helpJMI.setIcon( loadIcon("/net/sourceforge/nite/icons/misc/help.gif") );
	    helpJMI.setActionCommand("Help");
	    helpJMI.addActionListener(helpview.getActionListener());
	    helpJMI.setEnabled(true);
	    helpMenu.add(helpJMI);
	} catch( HelpViewerException e ){
	    statusBar.setText("Loading help system failed.");
	}
	//corpus-specific help
	helpCorpus = new JMenuItem("Corpus help", 'C');
	helpCorpus.setIcon( loadIcon("/eclipseicons/elcl16/linkto_help.gif") );
	helpCorpus.setActionCommand("corpus");
	helpCorpus.addActionListener(this);
	helpMenu.add(helpCorpus);

	//about
	JMenuItem helpAbout = new JMenuItem("About", 'A');
	helpAbout.setIcon( loadIcon("/net/sourceforge/nite/icons/misc/about.gif") );
	helpAbout.setActionCommand("about");
	helpAbout.addActionListener(this);
	helpMenu.addSeparator();
	helpMenu.add(helpAbout);

	JPanel      queryPanel   = new JPanel( new BorderLayout() );
	JPanel      resultsPanel = new JPanel( new BorderLayout() );
	//queryPanel
	queryPanel.setBorder( new EmptyBorder(4, 4, 0, 4) );
	JPanel queryButtonsPanel = new JPanel(
					      new FlowLayout(FlowLayout.RIGHT, 0, 6) );
	//qin - Query INput field
	qin.addCaretListener(this);
	qin.addKeyListener(this);
	qin.setLineWrap(true);
	//submit button
	queryButtonsPanel.add( submit );

	//resultsPanel
	//    qout.setLineWrap(true);
	//    qout.setOpaque(false);


	//assemble queryPanel
	queryPanel.add(queryButtonsPanel, "South");
	queryPanel.add(new JScrollPane(qin), "Center");
	//assemble resultsPanel
	outTree.getSelectionModel().setSelectionMode(  // allows one se-
						     TreeSelectionModel.SINGLE_TREE_SELECTION);   // lection at a time
	outTree.addTreeSelectionListener(
					 new TreeSelectionListener(){
					     public void valueChanged(TreeSelectionEvent e) {
						 DefaultMutableTreeNode node =
						     (DefaultMutableTreeNode)outTree.getLastSelectedPathComponent();

						 //show selected node
						 //if (node == null) return;
						 //outFocus.showElement( node.getUserObject() );

						 //show all nodes belonging to the selected item or the subtree of it
						 java.util.List elementList = new ArrayList();
						 if( node == null ) return;
						 for( Enumeration enumer = node.depthFirstEnumeration();
						      enumer.hasMoreElements(); ){
						     Object element = enumer.nextElement();
						     if(  ( element != null )
							  && ((DefaultMutableTreeNode)element).isLeaf() ){
							 elementList.add( ((DefaultMutableTreeNode)element).getUserObject() );
						     }
						 }
						 outFocus.showElements(elementList);

						 if (treehash==null) { return; }
						 java.util.List results = null;
						 // added by jonathan to highlight search results.
						 Object o = treehash.get(node);
						 if (o==null) { return; }
						 if (o instanceof NOMElement) {
						     NOMElement ne = (NOMElement) o;
						     for (Iterator qhit=queryHandlers.iterator(); qhit.hasNext(); ) {
							 QueryResultHandler qh = (QueryResultHandler) qhit.next();
							 if (qh!=null && ne!=null) {
							     qh.acceptQueryResult(ne);
							 }
						     }
						     results = Collections.singletonList(o);
						 } else if (o instanceof java.util.List) {
						     // remove the sublists and keep only the elements
						     java.util.List l = new ArrayList((java.util.List) o);
						     for (Iterator it = l.iterator(); it.hasNext();) {
							 if (! (it.next() instanceof NOMElement)) {
							     it.remove();
							 }
						     }
						     for (Iterator qhit=queryHandlers.iterator(); qhit.hasNext(); ) {
							 QueryResultHandler qh = (QueryResultHandler) qhit.next();
							 if (qh!=null && l!=null) {
							     qh.acceptQueryResults(l);
							 }
						     }
						     results = (java.util.List) o;
						 }
						 if (results==null) { return; }
						 for (Iterator it = handlers.iterator(); it.hasNext();) {
						     ((SimpleQueryResultHandler) it.next()).acceptResults(results);
						 }

					     }
					 } );
	out.setPreferredSize( new Dimension( 8, 8 ) );
	out.setTopComponent( new JScrollPane(outTree) );
	out.setBottomComponent( outFocus );
	out.setOneTouchExpandable(true);
	out.setDividerLocation(178);
	//outTree only view ___________________________________________________________!
	resultsPanel.add(out, "Center");
	//resultsPanel.add( new JScrollPane(outTree), "Center");

	//assemble mainPanel
	mainPanel.addTab("Query", queryPanel);
	mainPanel.addTab("Result", resultsPanel);
	getContentPane().add( mainPanel, "Center" );

	//disable resultsPanel
	mainPanel.setEnabledAt( 1, false );

	//show window
	pack();
	setLocation(100, 200);
	//    setVisible(true);

	//set corpus
	closeCorpus();
	// now checks if we're a client program where the NOM is passed in - don't autoload...
	// jonathan 30.11.04
	if( autoload && !client && preferences.corpusLoaded && !corpora.isEmpty() ){
	    loadCorpus((String)corpora.get(0));
	}

	//qin.setText("($a word)($b): $a@pos == $b@pos");
	//qin.setText("($a)($b): $a > $b");
	//qin.setText("($a)($b): $a <> $b");
	//qin.setText("($a)($b): $a >\"type\" $b");
	//qin.setText("($a): start($a) == \"0\"");
	//qin.setText("($a)($b): start($b) == end($a)");
	//qin.setText("($a): timed($a)");
	//qin.setText("($a): $a@orth ~ /.*s.*/");
	//qin.setText("($a)(exists $b word)($c): $a@orth ~ /.*s.*/");
	//qin.setText("($a)($b)($c): $a@orth -> $b@orth -> $c@orth");
	//qin.setText("($a word) :: ($b syntax)");
	//qin.setText("($a syntax)($b gesture): $a == $b");
	//qin.setText("($a word): $a@orth ~ /t.*/ :: ($b word): $b@orth ~ /.*e/");
	//qin.setText("($a word): $a@orth ~ /t.*/ :: ($b word): $b@orth ~ /.*e/ and $a==$b");
	//qin.setText("($a word): $a@orth ~ /t.*/ :: ($b word): $b@orth ~ /.*e/ and $a==$b :: ($c): $c@orth==\"the\"");
	//qin.setText("($a)($b)($c)($d): $a^$b and $b^$c and $c^$d");
	//qin.setText("($a)($b):$a^$b :: ($c):  $b^$c :: ($d): $c^$d");
	//qin.setText("($a word)::($b syntax):$a ^ $b");
	//qin.setText("($a): id($a)='w_6' or id($a)='w_7' or id($a)='w_8'");
	////////////////////////////////////////////////////////////////
    }

    private JMenuItem makeMenuItem(String label, String action, ActionListener al)
    {
	JMenuItem ret = new JMenuItem(label);
	ret.setActionCommand(action);
	ret.addActionListener(al);
	return ret;
    }

    private JMenu makeSubMenu(String label, String action, char mnemonic)
    {
	JMenu ret = new JMenu(label);
	ret.setMnemonic(mnemonic);
	for (int i=0; i<bookmarksNames.size(); i++) {
	    JMenuItem bookmark = makeMenuItem((String)bookmarksQueries.get(i), action, this);
	    bookmark.setName((String)bookmarksNames.get(i));
	    ret.add(bookmark);
	}
	return ret;
    }


    /////////////////////////////////////////////////////////////////////////////
    // WindowListener

    /** NOP (methode needed for implementing WindowListener).
     * @param event parameter isn't used */
    public void windowActivated(WindowEvent event)   {}

    /** NOP (methode needed for implementing WindowListener).
     * @param event parameter isn't used */
    public void windowClosed(WindowEvent event)      {}

    /**
     * Processes {@linkplain #exit()}.
     * @param event parameter isn't used
     */
    public void windowClosing(WindowEvent event)     { 
	// mainWindow check added by Jonathan Kilgour
	if (mainWindow) { exit(); }
	else { close(); }
    }

    /** NOP (methode needed for implementing WindowListener).
     * @param event parameter isn't used */
    public void windowDeactivated(WindowEvent event) {}

    /** NOP (methode needed for implementing WindowListener).
     * @param event parameter isn't used */
    public void windowDeiconified(WindowEvent event) {}

    /** NOP (methode needed for implementing WindowListener).
     * @param event parameter isn't used */
    public void windowIconified(WindowEvent event)   {}

    /** NOP (methode needed for implementing WindowListener).
     * @param event parameter isn't used */
    public void windowOpened(WindowEvent event)      {}

    /////////////////////////////////////////////////////////////////////////////
    // ActionListener
    /**
     * Set of actions started by the menus or buttons.
     * @param event event.getActionCommand() is used to know which action
     * has to execute
     */
    public void actionPerformed(ActionEvent event)
    {
	String cmd = event.getActionCommand();
	//Debug.print("Search action: " + cmd, Debug.IMPORTANT);
	if ( cmd.equals("submit") ) {
	    if ( qin.getText().trim().length() == 0 ) {
		JOptionPane.showMessageDialog( this,
					       "Please specify a query",
					       "Syntax Error",
					       JOptionPane.ERROR_MESSAGE );
	    } else {
		progressWindow = new ProgressWindow(this, "Searching...", this, searchEngine, searchEngine);
		progressWindow.show();
	    }
	} else if ( cmd.equals("exit") ) {
	    exit();
	} else if ( cmd.equals("dnf") ) {
	    try {
		String dnf = searchEngine.showDNF( qin.getText() );
		JOptionPane.showMessageDialog( this,
					       dnf,
					       "DNF",
					       JOptionPane.INFORMATION_MESSAGE );
	    } catch (Throwable e) {
		JOptionPane.showMessageDialog( this,
					       e.getMessage(),
					       "Syntax Error",
					       JOptionPane.ERROR_MESSAGE );
	    }

	    // web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-w
	    // eb-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-we
	    // b-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web-web
	} else if ( cmd.equals("web") ) {
	    try {
		java.util.List x = searchEngine.search(corpus, qin.getText(), -1);
		System.out.println(""+Engine.resultToXML(x));
	    } catch( Throwable e ) { e.printStackTrace(); }
	    //
	    //      NOMWriteElement x = null;
	    //      NOMWriteElement y = null;
	    //      for (Iterator i = corpus.getElements(); i.hasNext(); )
	    //      {
	    //        NOMWriteElement item = (NOMWriteElement)i.next();
	    //        if( item.getID().equals("g_2") ){
	    //          System.out.println(">>> "+item.getLink() );
	    //          for (Iterator j = item.getParents().iterator(); j.hasNext(); ){
	    //            NOMWriteElement item2 = (NOMWriteElement)j.next();
	    //            System.out.println(" p "+item2.getLink() );
	    //            if( item2.getParents() != null ){
	    //              for (Iterator k = item2.getParents().iterator(); k.hasNext(); ){
	    //                NOMWriteElement item3 = (NOMWriteElement)k.next();
	    //                System.out.println(" pp "+item3.getLink() );
	    //              }
	    //            }
	    //          }
	    //        }
	    //        if( item.getID().equals("g_1") ){ x = item; }
	    //        if( item.getID().equals("w_1") ){ y = item; }
	    //      }
	    //      System.out.println("a^b "+corpus.testDominates(x, y));
	} else if ( cmd.equals("debugTree") ) {
	    try {
		String tree = searchEngine.showParseTree( qin.getText(), "" );
		JOptionPane.showMessageDialog( this,
					       tree,
					       "Query Parse Tree",
					       JOptionPane.INFORMATION_MESSAGE );
	    } catch (Throwable e) {
		JOptionPane.showMessageDialog( this,
					       e.getMessage(),
					       "Syntax Error",
					       JOptionPane.ERROR_MESSAGE );
	    }
	} else if ( cmd.equals("exit") ) {
	    prefloader.savePreferences(preferences);
	    exit();
	} else if ( cmd.equals("save") ) {
	    int confirmValue = 1;
	    String resultAsXML = null;
	    if (allResults != null) resultAsXML = Engine.resultToXML(allResults);
	    if (resultAsXML != null) {
		if ( searchEngine.isInterrupted()) {
		    confirmValue = JOptionPane.showConfirmDialog(this, ERROR_INTERRUPT_SAVE, "Confirm", JOptionPane.YES_NO_OPTION);
		    if ( confirmValue == JOptionPane.NO_OPTION ) { return; }
		}

		fc = new JFileChooser( preferences.getSaveDir() );
		fc.setFileFilter(new MyFileFilter("xml"));
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    File file = fc.getSelectedFile();
		    if ( file.exists() ) {
			confirmValue = JOptionPane.showConfirmDialog(this, ERROR_OVERWRITE_SAVE, "Confirm", JOptionPane.YES_NO_OPTION);
			if ( confirmValue == JOptionPane.NO_OPTION) { return; }
		    }
		    FileFilter ff = fc.getFileFilter();
		    try {
			BufferedWriter xml = null;
			if ( ff.getDescription().equals("xml files") && !file.getName().endsWith(".xml") ) {
			    File xmlFile = new File(file.getAbsolutePath() + ".xml");
			    xml = new BufferedWriter( new FileWriter(xmlFile) );
			} else {
			    xml = new BufferedWriter( new FileWriter(file) );
			}
			xml.write(resultAsXML);
			xml.close();
			preferences.saveDir = file.getParentFile();
		    } catch(IOException e) {
			e.printStackTrace(System.out);
		    }
		}
	    }
	} else if ( cmd.equals("exportAsExcel") ) {
	    try {
		//1. interrupted -> export anyway?
		int confirmValue = 1;
		if( searchEngine.isInterrupted() ){
		    confirmValue = JOptionPane.showConfirmDialog( this,
								  ERROR_INTERRUPT_SAVE,
								  "Confirm",
								  JOptionPane.YES_NO_OPTION );
		    if( confirmValue == JOptionPane.NO_OPTION ){ return; }
		}

		//2. file chooser: select target
		fc = new JFileChooser( preferences.getSaveDir() );
		fc.setFileFilter(new MyFileFilter("xls"));
		int returnVal = fc.showSaveDialog(this);
		if( returnVal != JFileChooser.APPROVE_OPTION ){ return; }

		//3. already exist -> overwrite?
		File file = fc.getSelectedFile();
		//check: correct ending
		String fileName = file.getName();
		String fileAppendix = fileName.substring( fileName.lastIndexOf('.')+1 );
		//wrong ending -> +".xls"
		if( !fileAppendix.equalsIgnoreCase("xls") ){
		    file = new File( file.getParentFile(), fileName+".xls" );
		}
		if( file.exists() ){
		    //a) not writeable -> show error message
		    if( !file.canWrite() ){
			JOptionPane.showMessageDialog( this,
						       ERROR_CANNOT_WRITE,
						       "Can not Write",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    //b) writeable -> confirm
		    confirmValue = JOptionPane.showConfirmDialog( this,
								  ERROR_OVERWRITE_SAVE,
								  "Confirm",
								  JOptionPane.YES_NO_OPTION );
		    if( confirmValue == JOptionPane.NO_OPTION ){ return; }
		}

		//4. pass 1: collecting all attributes
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet( corpusName );
		boolean timedElements = false;
		boolean texedElements = false;
		java.util.List attributeList = new ArrayList();
		for( Enumeration e = top.depthFirstEnumeration(); e.hasMoreElements(); ) {
		    Object item = e.nextElement();
		    try {
			NOMElement itemElement = (NOMElement)((DefaultMutableTreeNode)item).getUserObject();
			if(  !timedElements
			     && !Double.isNaN( itemElement.getStartTime() )
			     && !Double.isNaN( itemElement.getEndTime()   ) ){
			    timedElements = true;
			}
			if(  !texedElements
			     && (itemElement.getText() != null)
			     && !itemElement.getText().equals("") ){
			    texedElements = true;
			}
			for( Iterator elIt = itemElement.getAttributes().iterator(); elIt.hasNext(); ){
			    NOMAttribute attr = (NOMAttribute)elIt.next();
			    String attrName = attr.getName();
			    if(  !attributeList.contains(attrName)
				 && !attrName.startsWith("xmlns:") ){
				attributeList.add(attrName);
			    }
			}
		    } catch( ClassCastException ex ){} //skip
		}
		//writing column headers
		HSSFRow headerRow = sheet.createRow( (short)0 );
		//style for header cells
		HSSFCellStyle headerCellStyle = workbook.createCellStyle();
		HSSFFont headerFont = workbook.createFont();
		headerFont.setBoldweight( HSSFFont.BOLDWEIGHT_BOLD );
		headerCellStyle.setFont( headerFont );
		headerCellStyle.setBorderBottom( HSSFCellStyle.BORDER_THIN );
		headerCellStyle.setBorderRight(  HSSFCellStyle.BORDER_THIN );
		//style for header cells
		HSSFCellStyle blackHeaderCellStyle = workbook.createCellStyle();
		HSSFFont blackHeaderFont = workbook.createFont();
		blackHeaderFont.setBoldweight( HSSFFont.BOLDWEIGHT_BOLD );
		blackHeaderFont.setColor( HSSFColor.WHITE.index );
		blackHeaderCellStyle.setFont( blackHeaderFont );
		blackHeaderCellStyle.setBorderBottom( HSSFCellStyle.BORDER_THIN );
		blackHeaderCellStyle.setBorderRight(  HSSFCellStyle.BORDER_THIN );
		blackHeaderCellStyle.setFillForegroundColor( HSSFColor.BLACK.index );
		blackHeaderCellStyle.setFillPattern( HSSFCellStyle.SOLID_FOREGROUND );
		//left upper cell
		HSSFCell currentCell = headerRow.createCell( (short)0 );
		//        currentCell.setCellValue( "name()" );
		currentCell.setCellStyle( headerCellStyle );
		//XLINK
		currentCell = headerRow.createCell( (short)0 );
		currentCell.setCellValue( "XLINK" );
		currentCell.setCellStyle( blackHeaderCellStyle );
		//NAME
		currentCell = headerRow.createCell( (short)1 );
		currentCell.setCellValue( "NAME" );
		currentCell.setCellStyle( blackHeaderCellStyle );
		//header: attributes
		for( int i = 0; i < attributeList.size(); i++ ){
		    currentCell = headerRow.createCell( (short)(i+2) );
		    currentCell.setCellValue( "@" + attributeList.get(i).toString() );
		    currentCell.setCellStyle( blackHeaderCellStyle );
		}
		//time: start() and end()
		if( timedElements ){
		    //start()
		    currentCell = headerRow.createCell( (short)(attributeList.size()+2) );
		    currentCell.setCellValue( "start()" );
		    currentCell.setCellStyle( blackHeaderCellStyle );
		    //end()
		    currentCell = headerRow.createCell( (short)(attributeList.size()+3) );
		    currentCell.setCellValue( "end()" );
		    currentCell.setCellStyle( blackHeaderCellStyle );
		}
		//text()
		if( texedElements ){
		    //start()
		    currentCell = headerRow.createCell(
						       (short)( attributeList.size() + ( timedElements ? 4 : 2 ) ) );
		    currentCell.setCellValue( "text()" );
		    currentCell.setCellStyle( blackHeaderCellStyle );
		}

		//5. pass 2: generating output
		short rowIndex = (short)1;
		//style for an empty cell
		HSSFCellStyle emptyCellStyle = workbook.createCellStyle();
		emptyCellStyle.setFillForegroundColor( HSSFColor.GREY_25_PERCENT.index );
		emptyCellStyle.setFillPattern( HSSFCellStyle.SOLID_FOREGROUND );
		emptyCellStyle.setBorderBottom( HSSFCellStyle.BORDER_THIN );
		emptyCellStyle.setBorderRight( HSSFCellStyle.BORDER_THIN );
		//style for an normal cell
		HSSFCellStyle normalCellStyle = workbook.createCellStyle();
		normalCellStyle.setBorderBottom( HSSFCellStyle.BORDER_THIN );
		normalCellStyle.setBorderRight( HSSFCellStyle.BORDER_THIN );
		//style for start() and end()
		HSSFCellStyle emptyTimedCellStyle = workbook.createCellStyle();
		emptyTimedCellStyle.setFillForegroundColor( HSSFColor.SEA_GREEN.index );
		emptyTimedCellStyle.setFillPattern( HSSFCellStyle.SOLID_FOREGROUND );
		emptyTimedCellStyle.setBorderBottom( HSSFCellStyle.BORDER_THIN );
		emptyTimedCellStyle.setBorderRight( HSSFCellStyle.BORDER_THIN );
		//style for an normal timed cell
		HSSFCellStyle normalTimedCellStyle = workbook.createCellStyle();
		normalTimedCellStyle.setFillForegroundColor( HSSFColor.LIGHT_GREEN.index );
		normalTimedCellStyle.setFillPattern( HSSFCellStyle.SOLID_FOREGROUND );
		normalTimedCellStyle.setBorderBottom( HSSFCellStyle.BORDER_THIN );
		normalTimedCellStyle.setBorderRight( HSSFCellStyle.BORDER_THIN );
		//style for text()
		HSSFCellStyle emptyTextCellStyle = workbook.createCellStyle();
		emptyTextCellStyle.setFillForegroundColor( HSSFColor.LIGHT_BLUE.index );
		emptyTextCellStyle.setFillPattern( HSSFCellStyle.SOLID_FOREGROUND );
		emptyTextCellStyle.setBorderBottom( HSSFCellStyle.BORDER_THIN );
		emptyTextCellStyle.setBorderRight( HSSFCellStyle.BORDER_THIN );
		//style for an normal text cell
		HSSFCellStyle normalTextCellStyle = workbook.createCellStyle();
		normalTextCellStyle.setFillForegroundColor( HSSFColor.PALE_BLUE.index );
		normalTextCellStyle.setFillPattern( HSSFCellStyle.SOLID_FOREGROUND );
		normalTextCellStyle.setBorderBottom( HSSFCellStyle.BORDER_THIN );
		normalTextCellStyle.setBorderRight( HSSFCellStyle.BORDER_THIN );
		//row loop
		for( Enumeration e = top.depthFirstEnumeration(); e.hasMoreElements(); ) {
		    Object item = e.nextElement();
		    try {
			NOMElement currentElement =
			    (NOMElement)((DefaultMutableTreeNode)item).getUserObject();
			HSSFRow  currentRow  = sheet.createRow(rowIndex);
			//xlink
			currentCell = currentRow.createCell( (short)0 );
			currentCell.setCellValue( currentElement.getXLink() );
			currentCell.setCellStyle( headerCellStyle );
			//element name
			currentCell = currentRow.createCell( (short)1 );
			currentCell.setCellValue( currentElement.getName() );
			currentCell.setCellStyle( headerCellStyle );
			//column loop
			for( int i = 0; i < attributeList.size(); i++ ){
			    currentCell = currentRow.createCell( (short)(i+2) );
			    NOMAttribute currentAttribute = currentElement.getAttribute(
											attributeList.get(i).toString() );
			    if( currentAttribute != null ){
				currentCell.setCellType( HSSFCell.CELL_TYPE_STRING );

				// The setEncoding method was removed in Aug 2008 as 
				// it now handles Unicode without forcing the encoding.
				// JK 1/10/10
				//currentCell.setEncoding( HSSFCell.ENCODING_UTF_16 );
				currentCell.setCellValue( currentAttribute.getStringValue() );
				currentCell.setCellStyle( normalCellStyle );
			    } else {
				currentCell.setCellStyle( emptyCellStyle );
			    }
			}
			//time informations
			if( timedElements ){
			    //start()
			    currentCell = currentRow.createCell( (short)(attributeList.size()+2) );
			    if( !Double.isNaN( currentElement.getStartTime() ) ){
				currentCell.setCellValue( currentElement.getStartTime() );
				currentCell.setCellStyle( normalTimedCellStyle );
			    } else {
				currentCell.setCellStyle( emptyTimedCellStyle );
			    }
			    //end()
			    currentCell = currentRow.createCell( (short)(attributeList.size()+3) );
			    if( !Double.isNaN( currentElement.getEndTime() ) ){
				currentCell.setCellValue( currentElement.getEndTime() );
				currentCell.setCellStyle( normalTimedCellStyle );
			    } else {
				currentCell.setCellStyle( emptyTimedCellStyle );
			    }
			}
			if( texedElements ){
			    currentCell = currentRow.createCell(
								(short)( attributeList.size() + (timedElements ? 4 : 2) ) );
			    if(  (currentElement.getText() != null)
				 && !currentElement.getText().equals("") ){
				currentCell.setCellValue( currentElement.getText() );
				currentCell.setCellStyle( normalTextCellStyle );
			    } else {
				currentCell.setCellStyle( emptyTextCellStyle );
			    }
			}
			rowIndex++;
		    } catch( ClassCastException ex ){}
		}

		//6. write into file
		boolean success = false;
		while( !success ){
		    try {
			FileOutputStream fileOut = new FileOutputStream(file);
			workbook.write(fileOut);
			fileOut.close();
			preferences.saveDir = file.getParentFile();
			success = true;
		    } catch( Exception ex ){
			//cannot wirte (maybe file is locked) -> try again?
			confirmValue = JOptionPane.showConfirmDialog( this,
								      ERROR_CANNOT_WRITE,
								      "Confirm",
								      JOptionPane.YES_NO_OPTION );
			if( confirmValue == JOptionPane.NO_OPTION ){ success = true; }
		    }
		}

		////////
	    } catch( OutOfMemoryError e ){
		JOptionPane.showMessageDialog( this,
					       ERROR_OUT_OF_MEMORY,
					       "Out of Memory Error",
					       JOptionPane.ERROR_MESSAGE );
	    }



	} else if ( cmd.equals("open") ) {
	    loadCorpus();
	} else if ( cmd.equals("reload") ) {
	    closeCorpus();
	    if( !corpora.isEmpty() ){ 
		loadCorpus( (String)corpora.get(0) ); 
	    }
	} else if ( cmd.equals("close") ){
	    closeCorpus();
	} else if ( cmd.equals("selectCorpus") ) {
	    loadCorpus( ((JMenuItem)event.getSource()).getName() );
	} else if ( cmd.equals("addBookmark") ) {
	    if ( qin.getText().trim().length()>0 ) {
		BookmarkDialog dialog = new BookmarkDialog(this, "Add Bookmark", qin.getText(), true);
		dialog.setLocationRelativeTo(this);
		dialog.show();
		if ( dialog.submitted ) {
		    if ( bookmarksNames.size()==0 ) {
			deleteBookmark = makeSubMenu("Delete Bookmark", "delete", 'D');
			bookmarksMenu.add(deleteBookmark);
			bookmarksMenu.addSeparator();
		    }
		    bookmarksQueries.add(dialog.getName());
		    bookmarksNames.add(qin.getText());
		    bookmarksMenu.add( makeMenuItem( dialog.getName(), "bookmark"+(bookmarksNames.size()-1), this) );
		    JMenuItem bookmark = makeMenuItem( dialog.getName(), "delete", this);
		    bookmark.setName(qin.getText());
		    deleteBookmark.add(bookmark);
		}
		// this isn't really a good thing to do, but the search window
		// isn't always exited correctly so bookmarks often get
		// lost. Jonathan 4/8/6
		prefloader.savePreferences(preferences);
	    }
	} else if( cmd.startsWith("bookmark") ) {
	    try {
		int i = Integer.parseInt( cmd.substring(8) );
		mainPanel.setSelectedIndex(0);
		qin.setText( (String)bookmarksNames.get(i) );
	    } catch(NumberFormatException e){}
	} else if ( cmd.equals("delete") ) {
	    JMenuItem bookmark = (JMenuItem)event.getSource();
	    int index = bookmarksNames.indexOf(bookmark.getName());
	    if ( bookmarksNames.size() > 0 ) {
		bookmarksMenu.remove(index+3);
		deleteBookmark.remove(bookmark);
		bookmarksNames.remove(bookmark.getName());
		bookmarksQueries.remove(index);
		if ( bookmarksNames.size() == 0){
		    //better solution???
		    bookmarksMenu.removeAll();
		    bookmarksMenu.add(addBookmark);
		} else {
		    // generate bookmarks new
		    bookmarksMenu.removeAll();
		    bookmarksMenu.add(addBookmark);
		    JMenu deleteBookmark = makeSubMenu("Delete Bookmark", "delete", 'D');
		    bookmarksMenu.add(deleteBookmark);
		    bookmarksMenu.addSeparator();
		    for( int i=0; i<bookmarksNames.size(); i++ ) {
			bookmarksMenu.add( makeMenuItem( (String)bookmarksQueries.get(i), "bookmark"+i, this) );
		    }
		}
		// this isn't really a good thing to do, but the search window
		// isn't always exited correctly so bookmarks often get
		// lost. Jonathan 4/8/6
		prefloader.savePreferences(preferences);
	    }
	} else if ( cmd.equals("corpus") ) { // new addition by JAK 19/3/08
	    if (corpus==null || !(corpus instanceof NOMCorpus)) { return; }
	    NMetaData metadata = ((NOMCorpus)corpus).getMetaData();
	    if (metadata==null || metadata.getPath()==null) { return; }
	    try {
		String dir = metadata.getPath()+File.separator+"corpusdoc";
		// check write-ability of relevant bits
		File metadir = new File(metadata.getPath());
		File defdir = new File(dir);
		File defind = new File(dir+File.separator+"index.html");
		File meta = new File(metadata.getFilename());
		String filename = "file:///"+defdir.getAbsoluteFile()+File.separator+"index.html";
		if (defind.exists() && defind.canRead() && (defind.lastModified() > meta.lastModified())) {
		    HTMLViewer viewWindow = new HTMLViewer(filename, "Corpus information");
		    System.out.println("Used existing corpus info in " + dir);
		} else {
		    if (!metadir.canWrite()) {
			dir=System.getProperty("user.home") + File.separator + "corpusdoc";
			defdir = new File(dir);
			filename = "file:///"+defdir.getAbsoluteFile()+File.separator+"index.html";
			defind = new File(dir+File.separator+"index.html");
		    }
		    // check again so that home-dir people get re-use
		    if (!defind.exists() || (defind.lastModified() < meta.lastModified())) {
			CorpusViewer cv = new CorpusViewer(metadata, CorpusViewer.HTML, dir, false);
			cv.visualise();
			System.out.println("New corpus info in " + dir);
		    } else { 
			System.out.println("Used existing corpus info in " + dir);
		    }
		    HTMLViewer viewWindow = new HTMLViewer(filename, "Corpus information");
		}
	    } catch (Exception ex) {
		ex.printStackTrace();
		System.err.println("Failed to create HTML view of corpus.");
	    }
	} else if ( cmd.equals("about") ) {
	    About aboutWindow = new About( this, loadIcon("/net/sourceforge/nite/icons/misc/nite_nxt_search.jpg") );
	} else {
	    System.out.println("ERROR - UNKNOWN COMMAND " + cmd );
	}
    }

    public boolean isCorpusLoaded() { return corpus != null; }
    public void loadCorpus()
    {
	// 1.) create new file chooser (location: corpora directory)
	fc = new JFileChooser( corporaDir );
	fc.setFileFilter( new MyFileFilter("xml") );
	int returnVal = fc.showOpenDialog(GUI.this);

	// 2.) open selected corpus
	if( returnVal == JFileChooser.APPROVE_OPTION ){
	    File corpusFile = fc.getSelectedFile();
	    if( corpusFile.exists() && corpusFile.canRead() ){
		loadCorpus( corpusFile.getPath() );
		if (corpusFile.getParentFile()!=null) {
		    corporaDir = corpusFile.getParentFile();
		}
	    } else {
		statusBar.setText("Open corpus failed: Access denied.");
	    }
	}
    }

    public void closeCorpus(){ 
	if (corpus instanceof NOMCorpus) {
	    ((NOMCorpus)corpus).clearData();
	}
	loadCorpus(null); 
    }

    public void loadCorpus(String corpusPath) {
	corpus = null;
	setTitle(TITLE);
	boolean successfulLoaded = false;
	if( corpusPath != null ){  //otherwise initiale corpus menu
	    statusBar.setText("Loading corpus ...");

	    try {
		File corpusFile = new File(corpusPath);
		if( !corpusFile.exists() || !corpusFile.canRead() ){
		    throw new Exception("Can not open corpus file");
		}
		//loading NOM or JNOM
		if( nom ){
		    NiteMetaData meta = new NiteMetaData(corpusPath);
		    corpus = new NOMWriteCorpus( meta );
	    
		    // If you have a large corpus it's crazy to load it all -
		    // JAK changing so it's a single observation; demo corpora
		    // should work as they did as they only have one
		    // observation.
		    ChooseObservation co = new ChooseObservation(meta);
		    String observationname=co.popupDialog();
		    NObservation obs = meta.findObservationWithName(observationname);
		    ((NOMWriteCorpus)corpus).loadData(obs);
		} else {
		    //          corpus = new JNOMCorpus();
		    //          ((JNOMCorpus)corpus).load(corpusFile.toURL());
		}
		successfulLoaded = true;

		//set windows title and status text
		corpusName =
		    isCorpusFile(corpusFile.getName()) ?
		    corpusFile.getName().substring(0, corpusFile.getName().indexOf('.')) :
		    corpusFile.getName();
		statusBar.setText("Corpus " + corpusName + " loaded.");
		setTitle( TITLE + " - " + corpusName );

		//update fast open corpora list (corpora & corporaPaths)
		corpusPath = corpusFile.getCanonicalPath();
		if( corpora.contains( corpusPath ) ){
		    corpora.remove( corpusPath );
		} else {
		    corporaNames.put( corpusPath, corpusName );
		}
		corpora.add( 0, corpusPath );
		while( corpora.size() > MAX ){
		    Object x = corpora.remove( corpora.size()-1 );
		    corporaNames.remove(x);
		}

	    } catch (Exception e) {
		statusBar.setText("Failed while loading: " + e.getMessage());
	    }
	}

	//enable/disable buttons and menu items
	submit.setEnabled(successfulLoaded);       //search button
	corpusClose.setEnabled(successfulLoaded);  //menu: corpus - close enabled
	corpusReload.setEnabled(successfulLoaded); //menu: corpus - reload enabled
	querySubmit.setEnabled(successfulLoaded);  //menu: query - search enabled
	if (helpCorpus!=null)
	    helpCorpus.setEnabled(successfulLoaded);   //menu: corpus help
	//update or initiale corpus menu
	corpusMenu.removeAll();
	corpusMenu.add(corpusOpen);
	corpusMenu.add(corpusReload);
	corpusMenu.add(corpusClose);
	corpusMenu.addSeparator();
	corpusMenu.add(autoloadCorpus);

	//fast open corpus menu items
	if( !corpora.isEmpty() ){
	    corpusMenu.addSeparator();
	    for( int i = 0; i < corpora.size(); i++ ) {
		String path = (String)corpora.get(i);
		JMenuItem item = new JMenuItem( (i+1) + ". " + corporaNames.get(path),
						( successfulLoaded && (i == 0) ) ?
						iconCorpusOpen :
						iconCorpus );
		item.setName(path);
		item.addActionListener(this);
		item.setActionCommand("selectCorpus");
		item.setMnemonic( item.getText().charAt(0) );
		corpusMenu.add(item);
	    }
	}

	//exit
	corpusMenu.addSeparator();
	corpusMenu.add(corpusExit);




	/*
	  int count = 1;
          int index = 0;
          if ( param.equals("select") ) {
	  try {
	  corpusMenu.removeAll();
	  } catch (Exception e) {
	  e.printStackTrace(System.out);
	  }
          }
          corpusMenu.insert(corpusOpen, 0);
          corpusMenu.insert(corpusClose, 1);
          corpusMenu.insertSeparator(2);
          index = corpusMenu.getMenuComponentCount();
          if ( corpora.size() > 0 ) {
	  corpusMenu.insert(autoloadCorpus, 3);
	  corpusMenu.insertSeparator(4);
	  String name = (String)corporaPaths.get(corpora.get(0));
	  String corpusName = isCorpusFile(name) ? name.substring(0, name.indexOf('.')) : name;
	  JMenuItem current = null;
	  if ( (param.equals("init") && autoload) || param.equals("select") ) {
	  current = new JMenuItem(count + ". " + corpusName, iconCorpusOpen);
	  } else {
	  current = new JMenuItem(count + ". " + corpusName, iconCorpus);
	  }
	  current.setName((String)corpora.get(0));
	  corpusMenu.add(current);
	  current.addActionListener(this);
	  current.setActionCommand("selectCorpus");
	  current.setMnemonic(current.getText().charAt(0));
	  if ((autoload && !client) || param.equals("select") ) {
	  try {
	  statusBar.setText("Loading the corpus ...");
	  if( nom ) {
	  File f = new File( (String)corpora.get(0) );
	  //            corpus = new NOMWriteCorpus(
	  corpus = new NOMWriteCorpus( new NiteMetaData( f.getPath() ) );
	  ((NOMWriteCorpus)corpus).loadData();
	  } else {
	  URL url = ( new File( (String)corpora.get(0) ) ).toURL();
	  corpus = new JNOMCorpus();
	  ((NOMCorpus)corpus).load(url);
	  }
	  corpusLoaded = true;
	  submit.setEnabled( corpusLoaded );
	  save.setEnabled(false);

	  querySubmit.setEnabled( corpusLoaded );
	  if (helpCorpus!=null)
	     helpCorpus.setEnabled( corpusLoaded );
	  statusBar.setText("Corpus " + corpusName + " loaded.");
	  } catch( Exception e ) {
	  System.out.println( e.toString() );
	  }
	  }
	  count++;
	  for (int i = 1; i < corpora.size(); i++) {
	  name = (String)corporaPaths.get(corpora.get(i));
	  corpusName = isCorpusFile(name) ? name.substring(0, name.indexOf('.')) : name;
	  JMenuItem item = new JMenuItem(count + ". " + corpusName, iconCorpus);
	  corpusMenu.add(item);
	  item.setName((String)corpora.get(i));
	  item.addActionListener(this);
	  item.setActionCommand("selectCorpus");
	  item.setMnemonic(item.getText().charAt(0));
	  count++;
	  }
	  corpusMenu.addSeparator();
	  int newIndex = corpusMenu.getMenuComponentCount();
	  corpusMenu.insert(corpusExit, newIndex);
          } else
	  corpusMenu.insert(corpusExit, index);
	*/
    }

    /////////////////////////////////////////////////////////////////////////////
    // CaretListener

    /**
     * If query input is being changed deactivate result panel.
     * @param e parameter isn't used
     */
    public void caretUpdate(CaretEvent e)
    {
	String simplifiedQin = simplifyString(qin.getText());
	if (  !lastQuery.equals(simplifiedQin) || simplifiedQin.equals("") ) {
	    mainPanel.setEnabledAt( 1, false );
	} else {
	    mainPanel.setEnabledAt( 1, true );
	}
    }

    /////////////////////////////////////////////////////////////////////////////
    // KeyListener

    public void keyTyped(KeyEvent event){}

    /**
     * Subit query if CTRL+RETURN is typed.
     * @param event the keyevent
     */
    public void keyReleased(KeyEvent event)
    {
	if(  (event.getKeyCode() == KeyEvent.VK_ENTER)
	     && event.isControlDown()
	     && submit.isEnabled() ){
	    actionPerformed( new ActionEvent(this, 0, "submit") );
	}

    }

    public void keyPressed(KeyEvent event){}

    /////////////////////////////////////////////////////////////////////////////

    private String simplifyString(String s)
    {
	StringBuffer ret = new StringBuffer();
	char c;
	boolean isLastWhitespace = true,
            isWhitespace;
	for (int i=0; i<s.length(); i++) {
	    c = s.charAt(i);
	    isWhitespace = Character.isWhitespace(c);
	    if ( !(isWhitespace && isLastWhitespace) ) {
		if (isWhitespace) {
		    ret.append(' ');
		} else if (  ( (c == '(') || (c == ')') )
			     && !isLastWhitespace ) {
		    ret.append(' ');
		    ret.append(c);
		} else {
		    ret.append(c);
		}
	    }
	    isLastWhitespace = isWhitespace || (c == '(') || (c == ')');
	}
	return ret.toString().trim();
    }

    /**
     * Closes window and exits program.
     */
    public void exit()
    {
	close();
	//dispose();
	System.exit( 0 );
    }

    /**
     * Closes window.
     */
    public void close()
    {
	prefloader.savePreferences(preferences);
	setVisible( false );
    }


    /** save changes to preferences file */
    public void savePreferences() {
	prefloader.savePreferences(preferences);
    }

    /**
     * New query will be executed by a new thread.
     */
    public void run()
    {
	try {
	    long start = Calendar.getInstance().getTime().getTime();
	    statusBar.setText("Processing the query '" + qin.getText() + "'...");

	    StringBuffer result = new StringBuffer();
	    allResults = searchEngine.search( corpus, qin.getText() );

	    mainPanel.setEnabledAt( 1, true );
	    mainPanel.setSelectedIndex( 1 );
	    lastQuery = simplifyString( qin.getText() );
	    long duration = Calendar.getInstance().getTime().getTime() - start;

	    statusBar.setText( ( !searchEngine.isInterrupted() ?
				 "Processing took " :
				 "Interrupted after " )
			       + (duration/1000)
			       + "."
			       + (duration%1000)
			       + " seconds." );

	    // clear focus visualisation
	    outFocus.initialise();
      
	    // clear tree
	    top.removeAllChildren();
	    ((DefaultTreeModel)outTree.getModel()).reload(); // otherwise the old tree will be shown

	    // clear result map
	    treehash.clear();

	    // matchlist
	    if( allResults.size()>=1 ) {
		top.setUserObject( "<matchlist size=\"" + (allResults.size()-1) + "\">" );
		save.setEnabled(true);
		exportAsExcel.setEnabled(true);
		// build tree
		resultToTree(allResults, top);
	    } else {
		top.setUserObject("<matchlist size=\"0\">");
	    }

	    // expand first level
	    outTree.expandPath( new TreePath(top) );

	    ((DefaultTreeModel)outTree.getModel()).reload(); //refresh (to much clipped right)
	    save.setEnabled(true);
	    exportAsExcel.setEnabled(true);

	} catch( OutOfMemoryError e ){
	    if ( (progressWindow != null) && progressWindow.isVisible() ) {
		progressWindow.stop();
	    }
	    JOptionPane.showMessageDialog( this,
					   "Sorry, too many matching results cause an error. Use a smaller corpus or/and a stricter query.",
					   "Out of Memory Error",
					   JOptionPane.ERROR_MESSAGE );

	} catch( Throwable e ){
	    if ( (progressWindow != null) && progressWindow.isVisible() ) {
		progressWindow.stop();
	    }

	    JOptionPane.showMessageDialog( this,
					   e.getMessage(),
					   "Syntax Error",
					   JOptionPane.ERROR_MESSAGE );
	    try {
		ParseException parseException = (ParseException)e;
		int x = parseException.currentToken.beginColumn;
		int y = parseException.currentToken.beginLine;
		int pos = qin.getDocument().getDefaultRootElement().getElement(y-1).getStartOffset()
		    + (x-1);
		qin.grabFocus();
		qin.setCaretPosition( pos );
	    } catch( Exception castException){} //skip
	    if( DEBUGGING ){ e.printStackTrace(); }
	}


	// close progress window
	if ( progressWindow != null ) {
	    progressWindow.setVisible(false);
	    progressWindow.dispose();
	}
    }

    private boolean isCorpusFile(String corpusName) {
	return corpusName.substring(corpusName.indexOf('.')+1, corpusName.length()).equals("corpus");
    }

    private void closeCorpusOld(){
	String corpusName = null;
	int count         = 1;
	int index         = 0;
	corpusClosed      = true;
	try {
	    corpusMenu.removeAll();
	} catch (Exception e) {
	    e.printStackTrace(System.out);
	}
	corpusMenu.insert(corpusOpen, 0);
	corpusMenu.insert(corpusClose, 1);
	corpusMenu.insertSeparator(2);
	corpusMenu.insert(autoloadCorpus, 3);
	corpusMenu.insertSeparator(4);
	for (int i = 0; i < corpora.size(); i++) {
	    String name = (String)corporaPaths.get(corpora.get(i));
	    corpusName = isCorpusFile(name) ? name.substring(0, name.indexOf('.')) : name;
	    JMenuItem item = new JMenuItem(count + ". " + corpusName, iconCorpus);
	    corpusMenu.add(item);
	    item.setName((String)corpora.get(i));
	    item.addActionListener(this);
	    item.setActionCommand("selectCorpus");
	    item.setMnemonic(item.getText().charAt(0));
	    count++;
	}
	if (corpora.size() > 0 ) corpusMenu.addSeparator();
	index = corpusMenu.getMenuComponentCount();
	corpusMenu.insert(corpusExit, index);
    }

    private void resultToTree(java.util.List matchList, DefaultMutableTreeNode parent)
    {
	int n=1;
	boolean first = true;
	java.util.List variables = null;
	for( Iterator i=matchList.iterator(); i.hasNext(); ) {
	    java.util.List list = (java.util.List)i.next();
	    if(first) {
		variables = list;
		first = false;
	    } else {
		DefaultMutableTreeNode match = new DefaultMutableTreeNode("<match n=\""+ n++ +"\">");
		parent.add(match);
		treehash.put(match, list);
		int nn = 0;
		for( Iterator j=list.iterator(); j.hasNext(); ) {
		    Object item = j.next();
		    try {
			String xLink = "";

			// either NOMElement or NOMWriteElement element, else subMatchList
			try { xLink = ((NOMElement)item).getXLink(); }
			catch( ClassCastException e ) {
			    NOMWriteElement x = (NOMWriteElement)item;
			    xLink = x.getLink();

			}
			String title = "<nite:pointer role=\""
			    + variables.get(nn++).toString()
			    + "\" xlink:href=\""
			    + xLink
			    + "\"/>";
			DefaultMutableTreeNode itemNode = new MyTreeNode(item, title);
			match.add( itemNode );
	    
			// added by Jonathan Kilgour - store results for display
			treehash.put(itemNode, item);

			// subMatchList
		    } catch(ClassCastException e) {
			try {
			    java.util.List subMatchList = (java.util.List)item;
			    DefaultMutableTreeNode subMatchListNode =
				new DefaultMutableTreeNode(
							   "<matchlist type=\"sub\" size=\"" + (subMatchList.size()-1) + "\">" );
			    match.add(subMatchListNode);
			    treehash.put(subMatchListNode, subMatchList);
			    resultToTree(subMatchList, subMatchListNode);
			} catch(ClassCastException f) {}
		    }
		}
	    }
	}
    }

    /**
     * Returns all layers of the loaded corpus as a list of Strings.
     * @return all layers of the loaded corpus as a list of Strings
     */
    public java.util.List getLayers()
    {
	java.util.List ret = new ArrayList();
	if( !isCorpusLoaded() ) return ret;
	for( Iterator i=corpus.getElements(); i.hasNext(); ) {
	    try {
		NOMElement e = (NOMElement)i.next();
		if( !ret.contains( e.getName() ) ) {
		    ret.add( e.getName() );
		}
	    } catch( ClassCastException exception ){} //skip
	}
	return ret;
    }

    /**
     * Returns how many sublayers there are in the specified layer.
     * @param layer the layer containing the sublayers
     * @return how many sublayers there are in the specified layer
     */
    public int getNumberOfSublayers(String layer)
    {
	int ret = -1;
	for( Iterator i=corpus.getElements(); i.hasNext(); ) {
	    NOMElement element = (NOMElement)i.next();
	    if( element.getName().equals(layer) ) {
		int sublayer = getSublayer( element );
		if( sublayer > ret ) { ret = sublayer; }
	    }
	}
	return ret+1;
    }

    /**
     * Returns the number of the sublayer of the specified element, where 0 is
     * the root sublayer. All elements of a flat layer are sublayer 0.
     * @param element the element in the sublayer
     * @return the number of the sublayer of the specified element
     */
    public int getSublayer(NOMElement element)
    {
	int ret = -1;
	String layer = element.getName();
	java.util.List parents = new ArrayList();
	parents.add(element);
	// how many parent levels there are in this layer?
	while( !parents.isEmpty() ) {
	    java.util.List nextParents = new ArrayList();
	    // collect all parent elements of the same level
	    for( Iterator i=parents.iterator(); i.hasNext(); ) {
		NOMElement x = (NOMElement)i.next();
		java.util.List xParents = x.getParents() == null ?
		    new ArrayList() :
		    x.getParents();
		for( Iterator j= xParents.iterator(); j.hasNext(); ) {
		    NOMElement y = (NOMElement)j.next();
		    if( y.getName().equals(layer) ) {
			nextParents.add(y);
		    }
		}
	    }
	    ret++;
	    parents = nextParents;

	}
	return ret;
    }

    /**
     * Returns the serch engine.
     * @return the serch engine
     */
    public Engine getEngine() { return searchEngine; }

    class MyFileFilter extends FileFilter
    {
	String type;

	public MyFileFilter(String type) { this.type = type; }

	// accept all directories and .xml files
	public boolean accept(File f)
	{
	    if (f.isDirectory()) { return true; }

	    // get extension
	    String extension = null;
	    String s = f.getName();
	    int i = s.lastIndexOf('.');
	    if (i > 0 &&  i < s.length() - 1) {
		extension = s.substring(i+1).toLowerCase();
	    }

	    return (extension == null) ?
		false :
		(extension.equals(type) ? true : false);
	}
	// the description of this filter
	public String getDescription() { return type + " files"; }

    }

    /////////////////////////////////////////////////////////////////////////////
    // BookmarkDialog
    class BookmarkDialog extends JDialog implements ActionListener
    {
	boolean submitted = false;
	String bookmarkName;
	JTextField nameField = new JTextField(15);

	public BookmarkDialog(Frame owner, String title, String query,
			      boolean modal) {
	    super(owner, title, modal);
	    setSize(300, 400);
	    JPanel pane1 = new JPanel();
	    JPanel pane2 = new JPanel();
	    JLabel   label = new JLabel("Name:");
	    JButton cancel = new JButton("Cancel");
	    JButton submit = new JButton("Ok");
	    getContentPane().setLayout(new GridLayout(2, 2, 15, 10));
	    pane1.setLayout(new FlowLayout(5));
	    pane2.setLayout(new GridLayout(1, 1, 50, 0));
	    getRootPane().setDefaultButton(submit);
	    nameField.setText(query.length() < 16 ? query : query.substring(0, 16));
	    nameField.selectAll();
	    cancel.setActionCommand("cancel");
	    cancel.addActionListener(this);
	    submit.setActionCommand("ok");
	    submit.addActionListener(this);
	    pane1.add(label);
	    pane1.add(nameField);
	    pane2.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
	    pane2.add(submit);
	    pane2.add(cancel);
	    getContentPane().add(pane1);
	    getContentPane().add(pane2);
	    pack();
	}

	public String getName() {
	    return bookmarkName;
	}

	public void setName(String name) {
	    bookmarkName = name;
	}

	public boolean isSubmitted() {
	    return submitted;
	}

	public void setSubmitted(boolean submitted) {
	    this.submitted = submitted;
	}

	public void actionPerformed(ActionEvent e) {
	    String cmd = e.getActionCommand();
	    if (cmd.equals("cancel")) {
		stop();
	    }
	    else if (cmd.equals("ok")) {
		setName(nameField.getText());
		setSubmitted(true);
		this.setVisible(false);
	    }
	}

	public void stop() {
	    setVisible(false);
	    dispose();
	}

    }

    class MyTreeNode
	extends DefaultMutableTreeNode
    {
	private String title;
	public MyTreeNode(Object userObject, String title)
	{
	    super(userObject);
	    this.title = title;
	}
	public String toString(){ return title; }
    }

    private Image loadImage(String name) {
	try {
	    // JAK 4/7/7 Without the check, a thread can start and
	    // mysteriously crash saying there's an uncaught exception
	    if (getClass().getResource(name) != null) { 
		return getToolkit().getImage(getClass().getResource(name));
	    } else {
		Debug.print("WARNING: Failed to load image: " + name, Debug.IMPORTANT);
		return null;
	    }
	} catch(Exception e) { 
	    Debug.print("WARNING: Failed to load image: " + name, Debug.IMPORTANT);
	    return null; 
	}
    }

    private ImageIcon loadIcon(String name) {
	try {
	    return new ImageIcon( loadImage(name) );
	} catch (Exception ex) {
	    return null;
	}
    }

    //set query from JManual
    public void querySubmitted(String query)
    {
	mainPanel.setSelectedIndex(0);
	qin.setText(query);
	qin.grabFocus();
    }

    // Jonathan added these four methods to implement the QueryHandler
    // interface

    /** add an interface element that will be informed of results. */
    public void registerResultHandler(QueryResultHandler display) {
	if (display instanceof SimpleQueryResultHandler) {
	    registerHandler((SimpleQueryResultHandler) display);
	} else if (display instanceof QueryResultHandler) {
	    queryHandlers.addElement((Object)display);	
	}
    } 
    
    /** remove an interface element that will be informed of results  */
    public void deregisterResultHandler(QueryResultHandler display) {
	if (display instanceof SimpleQueryResultHandler) {
	    deregisterHandler((SimpleQueryResultHandler) display);
	} else if (display instanceof QueryResultHandler) {
	    queryHandlers.removeElement((Object)display);
	}
    } 

    /**
     * Perform the query and display the results on-screen
     */
    public void performQuery(String newQuery) {
	try {
	    java.util.List elist = searchEngine.search(corpus, newQuery);
	} catch (Throwable e) {
	    e.printStackTrace();
	}
    }

    public void popupSearchWindow() {
	setVisible(true);
    }
    
    /** Forces a notify of the full resultslist to all resulthandlers. Added Dennis Reidsma 22-11-2006 */
    public void notifyFullResultList() {
        for (Iterator qhit=queryHandlers.iterator(); qhit.hasNext(); ) {
            QueryResultHandler qh = (QueryResultHandler) qhit.next();
            if (qh!=null && allResults!=null) {
                qh.acceptQueryResults(allResults);
            }
        }
    }
   
    // Elaine Farrow added these methods
    public void registerHandler(SimpleQueryResultHandler handler) {
        handlers.add(handler);
    }

    public void deregisterHandler(SimpleQueryResultHandler handler) {
        handlers.remove(handler);
    }
}
