/* NXT Corpus Manager
 * Copyright (c) 2008, Jean Carletta, Jonathan Kilgour
 * Created by Jonathan Kilgour 24/4/08 
 */
package net.sourceforge.nite.corpus;

/* Manage a set of corpora */
import java.net.URL;
import java.net.MalformedURLException;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import net.sourceforge.nite.util.*;
import net.sourceforge.nite.gui.util.ChooseObservation;
import net.sourceforge.nite.meta.impl.NiteMetaData;
import net.sourceforge.nite.meta.NObservation;
import net.sourceforge.nite.search.*;
import net.sourceforge.nite.query.*;
import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.nom.nomwrite.*;

import com.jgoodies.looks.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import ims.jmanual.*;

/** GUI that's the entry point for management of one or more
 * corpora. No arguments are required as it uses the user's
 * preferences file to populate known corpora */
public class Manager extends JFrame implements ActionListener, Runnable {

    public static final String TITLE = "NXT Corpus Manager v0.1";
    // this is the same as the search GUI uses.

    private Preferences      preferences  = null;
    private PreferenceLoader prefloader   = new PreferenceLoader(System.getProperty("user.home"));
    private List             corpora      = new ArrayList();
    private List             plugins      = null;
    private HashMap          corporaPaths = new HashMap();
    private HashMap          corporaNames = new HashMap();
    private File             corporaDir   = new File( System.getProperty("user.dir") );
    private Corpus           currentCorpus= null;
    private DefaultMutableTreeNode currentTreeNode= null;
    private HashMap          corpusTabs   = new HashMap();
    private HashMap          tabCorpus    = new HashMap();
    private boolean          editable     = true;

    // INTERFACE ELEMENTS
    private JTabbedPane      mainPanel    = new JTabbedPane();
    private JPanel           corpusPanel  = new JPanel();
    private JMenu            corpusMenu   = null;
    private JMenuItem        corpusExit   = null;
    private JMenuItem        corpusOpen   = null;
    private JMenuItem        corpusView   = null;
    private JMenuItem        corpusRemove = null;
    private JMenu            settingsMenu = null;
    private JMenuItem        settingsConfigure = null;
    private JTree            corpusTree   = null;
    private DefaultTreeModel treeModel    = null;
    private JFileChooser     fc           = null;
    private JLabel           statusBar    = null;
    private DefaultMutableTreeNode  root  = new DefaultMutableTreeNode("known corpora");
    JTextField selftypedplugin = null;

    /**
     * Creats a new {@linkplain #Manager()}.
     * @param args parameter isn't used
     */
    public static void main(String[] args) {

	boolean looking     = true;
	boolean version     = false;
	for( int i=0; i<args.length; i++ ){
	    String x = args[i];
	    if(  x.equalsIgnoreCase("version") || x.equalsIgnoreCase("-version") ) {
		version = true;
	    } 
	}

	// print version
	if(version){
	    System.out.print(TITLE);
	    return;
	}

	// show window
	Manager gui = new Manager();    
    }

    /**
     * Opens a new Manager window
     */
    public Manager() {
	setupManager();
	setVisible(true);
    }

    private void setupManager() {
	addWindowListener(new ManagerWindowListener());
    
	//window icon
	setIconImage( loadImage("/net/sourceforge/nite/icons/misc/corpus.gif") );

	// load preferences
	preferences = prefloader.getPreferences();

	corpora          = preferences.corpora;
	corporaNames     = preferences.names;
	plugins          = new ArrayList(preferences.plugins);

	//FormLayout layout = new FormLayout("fill:70dlu:grow, 3dlu, fill:100dlu:grow, 3dlu",
	//				   "fill:50dlu:grow, 3dlu, min, 3dlu");
	FormLayout layout = new FormLayout("max(70dlu;pref), 3dlu, fill:pref:grow, 3dlu",
					   "fill:50dlu:grow, 3dlu, min, 3dlu");
	PanelBuilder builder = new PanelBuilder(layout);
	builder.setDefaultDialogBorder();
	CellConstraints cc = new CellConstraints();
	setupCorpusPanel();
	setupMainPanel();
	setSize(600, 400);
	setTitle(TITLE);
	setJMenuBar(setupMenuBar());
	builder.add(corpusPanel, cc.xy(1,1));
	builder.add(mainPanel, cc.xy(3,1));
	statusBar = new JLabel("Click on a corpus on the left or use Corpus..Open");
	builder.add(statusBar, cc.xyw(1,3,3));
	getContentPane().add(builder.getPanel());
    }

    /** set up the menus and that */
    private JMenuBar setupMenuBar() {
	JMenuBar menubar=new JMenuBar();
	//menu: Corpus
	corpusMenu = new JMenu("Corpus");
	menubar.add(corpusMenu);
	corpusMenu.setMnemonic('C');
	corpusOpen   = new JMenuItem("Open ...", 'O');
	corpusView  = new JMenuItem("HTML view", 'H');
	corpusRemove  = new JMenuItem("Remove", 'R');
	corpusExit   = new JMenuItem("Exit", 'X');
	corpusExit.setIcon( loadIcon("/net/sourceforge/nite/icons/misc/exit.gif") );

	corpusOpen.setActionCommand("open");
	corpusOpen.addActionListener(this);
	corpusOpen.setIcon( loadIcon("/net/sourceforge/nite/icons/misc/open_corpus.gif") );

	corpusView.setActionCommand("view");
	corpusView.addActionListener(this);
	corpusView.setIcon( loadIcon("/eclipseicons/elcl16/linkto_help.gif") );
	corpusRemove.setActionCommand("remove");
	corpusRemove.addActionListener(this);
	//corpusRemove.setIcon( loadIcon("/net/sourceforge/nite/icons/media/high/stop.gif") );
	corpusRemove.setIcon( loadIcon("/eclipseicons/elcl16/close_view.gif") );
	corpusExit.setActionCommand("exit");
	corpusExit.addActionListener(this);


	corpusMenu.removeAll();
	corpusMenu.add(corpusOpen);
	corpusMenu.add(corpusView);
	corpusMenu.add(corpusRemove);
	corpusMenu.add(corpusExit);
	
	corpusView.setEnabled(false);
	corpusRemove.setEnabled(false);

	// menu: Settings
	settingsMenu = new JMenu("Settings");
	menubar.add(settingsMenu);
	settingsMenu.setMnemonic('S');
	settingsConfigure   = new JMenuItem("Configure ...", 'C');
	settingsConfigure.setActionCommand("configure");
	settingsConfigure.addActionListener(this);
	settingsMenu.add(settingsConfigure);
	//settingsConfigure.setIcon( loadIcon("/net/sourceforge/nite/icons/misc/open_corpus.gif") );
	return menubar;
    }

    /** set up the contents of the corpus selector window */
    private void setupCorpusPanel() {
	corpusPanel.setMinimumSize(new Dimension(100, 100));
	List toDelete=new ArrayList();
	for (Iterator corpit=corpora.iterator(); corpit.hasNext(); ) {
	    String corpus = (String)corpit.next();
	    String path = (String)corporaPaths.get(corpus);
	    String name = (String)corporaNames.get(corpus);
	    DefaultMutableTreeNode corpnode=createCorpusTreeNode(corpus, name, path);
	    
	    if (corpnode==null) { 
		toDelete.add(corpus);
	    } else {
		root.add(corpnode);
	    }
	}
	
	// tidy up by deleting those corpora that don't exist 
	for (Iterator delit=toDelete.iterator(); delit.hasNext(); ) {
	    Object o = delit.next();
	    if (o!=null) {
		corpora.remove(o);
		corporaNames.remove(o);
		corporaPaths.remove(o);
	    }
	}
	treeModel=new DefaultTreeModel(root);
	corpusTree = new JTree(treeModel);
	corpusTree.setEditable(true);
	corpusTree.setRootVisible(false);
	corpusTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	Icon leafIcon = loadIcon("/net/sourceforge/nite/icons/misc/corpus_open.gif");
	if (leafIcon != null) {
	    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
	    renderer.setLeafIcon(leafIcon);
	    corpusTree.setCellRenderer(renderer);
	}	
	//corpusPanel.add(new JScrollPane(corpusTree));
	
	corpusPanel.add(corpusTree);
	corpusPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "Known corpora"));
	corpusTree.addTreeSelectionListener(new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent e) {
		    corpusView.setEnabled(false);
		    corpusRemove.setEnabled(false);
		    DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
			corpusTree.getLastSelectedPathComponent();
		    if (node==null) return;
		    Object nodeInfo = node.getUserObject();
		    if (nodeInfo == null || !(nodeInfo instanceof Corpus)) return;
		    currentTreeNode = node;
		    currentCorpus=(Corpus)nodeInfo;
		    //System.out.println("selection " + currentCorpus.corpusFullPath);
		    if (corpusTabs.get(currentCorpus.corpusFullPath)==null) {
			JPanel jpan = createCorpusTab(currentCorpus);
			if (corpusTabs.size()==0) { mainPanel.removeAll(); }
			mainPanel.add(currentCorpus.corpusName, jpan);
			currentCorpus.tab=jpan;
			corpusTabs.put(currentCorpus.corpusFullPath, jpan);
			tabCorpus.put(jpan, currentCorpus);
		    }
		    mainPanel.setSelectedComponent((JComponent)corpusTabs.get(currentCorpus.corpusFullPath));
		    corpusView.setEnabled(true);
		    corpusRemove.setEnabled(true);
		}
	    });
	
	/** here we listen for changes made by clicking twice on a
	 * tree node and editing manually */
	treeModel.addTreeModelListener(new TreeModelListener() {
		public void treeNodesChanged(TreeModelEvent e) {
		    if (e.getChildren()==null) { return; }
		    Object o  = e.getChildren()[0];
		    currentCorpus.corpusName=o.toString();
		    if (currentCorpus.tab!=null) {
			int ind = mainPanel.indexOfComponent(currentCorpus.tab);
			if (ind!=-1) {
			    mainPanel.setTitleAt(ind, o.toString());
			}
		    }
		    corporaNames.put(currentCorpus.corpusFullPath, o.toString());
		    ((DefaultMutableTreeNode)o).setUserObject(currentCorpus);
		}
		public void treeNodesInserted(TreeModelEvent e) { }
		public void treeNodesRemoved(TreeModelEvent e) { }
		public void treeStructureChanged(TreeModelEvent e) { }
	    });
    }

    /** make a tree entry for a single corpus by reading the metadata file */
    private DefaultMutableTreeNode createCorpusTreeNode(String corpus, String name, String path) {
	try {
	    NMetaData meta = new NiteMetaData(corpus);
	    String cname=corpus;
	    if (meta.getCorpusDescription()!=null) { cname=meta.getCorpusDescription(); }
	    if (name!=null && !corpus.contains(name)) { cname=name; }
	    Corpus c = new Corpus(meta, corpus, cname);
	    DefaultMutableTreeNode corpnode = new DefaultMutableTreeNode(c);
	    c.treenode=corpnode;
	    return corpnode;
	} catch (Exception ex) {
	    Debug.print("Failed to find metadata file " + corpus + " deleting from list.");
	    return null;
	}
    }

    /** set up the contents of the main tabbed panel */
    private void setupMainPanel() {
	//mainPanel.setSize(400, 400);
	mainPanel.add("Corpus details", new JPanel());
	mainPanel.addChangeListener(new TabSelectionListener());
    }

    /** set up the contents of the main tabbed panel */
    private JPanel createCorpusTab(Corpus corpus) {
	FormLayout layout = new FormLayout("pref, 2dlu, fill:100dlu:grow, 2dlu",
					   "pref, 2dlu, fill:80dlu:grow, 2dlu");
	PanelBuilder builder = new PanelBuilder(layout);
	builder.setDefaultDialogBorder();
	CellConstraints cc = new CellConstraints();
	//builder.add(new JLabel("Name"), cc.xy(1,1));
	//JTextField nameField = new JTextField(corpus.corpusName);
	//if (!editable) { nameField.setEditable(false); }
	//builder.add(nameField, cc.xy(3,1));
	builder.add(new JLabel("Metadata location"), cc.xy(1,1));
	JTextField locField = new JTextField(corpus.corpusFullPath);
	locField.setEditable(false); // never edit?
	builder.add(locField, cc.xy(3,1));
	builder.add(makeInternalTabs(corpus), cc.xyw(1,3,3));
	return builder.getPanel();
    }

    /** make a set of action tabs for a given corpus */
    private JTabbedPane makeInternalTabs(Corpus corpus) {
	JTabbedPane internalTabs = new JTabbedPane();
	if (plugins==null) { return internalTabs; }
	for (int i=0; i<plugins.size(); i++) {
	    String classname = (String)plugins.get(i);
	    try {
		Object ctool = Class.forName(classname).newInstance();
		if (ctool==null) {
		    Debug.print("Could not retrtieve instance of plugin " + classname, Debug.ERROR);
		    continue;
		}
		if (!(ctool instanceof CorpusTool)) {
		    Debug.print("Plugin " + classname + " does not implement the CorpusTool interface", Debug.ERROR);
		    continue;

		}
		System.out.println("Success for " + classname);
		CorpusTool ct = (CorpusTool)ctool;
		internalTabs.add(ct.getName(), ct.getPanel(corpus.metadata));
	    } catch (Exception ex) {
		Debug.print("Error trying to use plugin: " + classname);
		continue;
	    }
	}
	return internalTabs;
    }


    /** make a panel for selecting plugins */
    private JPanel buildPluginSelector(List plugins) {
	FormLayout layout = new FormLayout("pref, 3dlu, pref",
					   "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu");
	PanelBuilder builder = new PanelBuilder(layout);
	builder.setDefaultDialogBorder();
	CellConstraints cc = new CellConstraints();
	JTextArea desc = new JTextArea("These plugins are the tabs that appear for each corpus in the corpus manager tool. You can add your own plugin by implementing the CorpusTool interface, and typing the fully qualified class name into the text box at the bottom of this form. It's up to you to make sure it's on the CLASSPATH at runtime. ",6,80);
	desc.setLineWrap(true);
	desc.setWrapStyleWord(true);
	desc.setEditable(false);
	builder.add(desc, cc.xyw(1,1,3));
	builder.add(new JLabel("Plugin"), cc.xy(1,3));
	builder.add(new JLabel("Selected"), cc.xy(3,3));
	int ypos=5;
	List otherplugs = new ArrayList(plugins);

	// first iterate through the startup Preferences (so that if
	// we deselect we can get them back!)
	for (int i=0; i<Preferences.plugs.length; i++) {
	    String plugname = Preferences.plugs[i];
	    JTextField classname = new JTextField(plugname);
	    classname.setEditable(false);
	    builder.add(classname, cc.xy(1,ypos));
	    boolean selected = false;
	    if (plugins.contains(plugname)) { 
		selected=true; 
		otherplugs.remove(plugname);
	    }
	    JCheckBox check = new JCheckBox();
	    check.setSelected(selected);
	    check.addActionListener(new MyCheckListener(check, plugname));
	    builder.add(check, cc.xy(3,ypos));
	    ypos+=2;
	}

	// now iterate through the user Preferences in case there are
	// non-standard ones that need to be listed.
	for (int i=0; i<otherplugs.size(); i++) {
	    String plugname = (String)otherplugs.get(i);
	    JTextField classname = new JTextField(plugname);
	    classname.setEditable(false);
	    builder.add(classname, cc.xy(1,ypos));
	    boolean selected = false;
	    if (plugins.contains(plugname)) { selected=true; }
	    JCheckBox check = new JCheckBox();
	    check.setSelected(selected);
	    check.addActionListener(new MyCheckListener(check, plugname));
	    
	    builder.add(check, cc.xy(3,ypos));
	    ypos+=2;
	}

	selftypedplugin=new JTextField("type class");
	selftypedplugin.setEditable(true);
	builder.add(selftypedplugin, cc.xy(1,ypos));
	//JCheckBox check = new JCheckBox();
	//check.setSelected(true);
	//builder.add(check, cc.xy(3,ypos));
	
	return builder.getPanel();
    }

    /** checkbox listener */
    private class MyCheckListener implements ActionListener {
	JCheckBox check;
	String plugin;

	public MyCheckListener(JCheckBox check, String plugin) {
	    this.check=check;
	    this.plugin=plugin;
	}

	public void actionPerformed(ActionEvent e) {
	    if (check.isSelected() && !plugins.contains(plugin)) {
		plugins.add(plugin);
	    } else if (!(check.isSelected()) && plugins.contains(plugin)) {
		plugins.remove(plugin);
	    }
	}
    }

    /** configure the corpus plugins */
    private void configurePlugins() {
	JPanel pluginpanel = buildPluginSelector(plugins);
	JOptionPane optionPane = new JOptionPane(pluginpanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
	JDialog dialog = optionPane.createDialog(this, "Select Corpus Plugins");
	dialog.show();
	Object selectedValue = optionPane.getValue();
	if (selectedValue==null || !(selectedValue instanceof Integer)) { return; }
	int i = ((Integer)selectedValue).intValue();
	
	if (i==JOptionPane.YES_OPTION) { 
	    if (selftypedplugin!=null) {
		if (selftypedplugin.getText()!=null && !(selftypedplugin.getText().equals("type class"))) {
		    plugins.add(selftypedplugin.getText());
		    System.out.println("Adding: " + selftypedplugin.getText());
		}
	    }
	    preferences.plugins=new ArrayList(plugins);
	} else {
	    plugins=new ArrayList(preferences.plugins);
	}
    }

    /////////////////////////////////////////////////////////////////////////////
    // ActionListener
    /**
     * Set of actions started by the menus or buttons.
     * @param event event.getActionCommand() is used to know which action
     * has to execute
     */
    public void actionPerformed(ActionEvent event) {
	String cmd = event.getActionCommand();
	if ( cmd.equals("open") ) {
	    loadCorpus();
	} else if ( cmd.equals("view") ) {
	    if (currentCorpus==null) { return; }
	    NMetaData metadata = currentCorpus.metadata;
	    if (metadata==null || metadata.getPath()==null) { return; }
	    try {
		String dir = metadata.getPath()+File.separator+"corpusdoc";
		// check write-ability of relevant bits
		File metadir = new File(metadata.getPath());
		File defdir = new File(dir);
		File defind = new File(dir+File.separator+"index.html");
		File meta = new File(metadata.getFilename());
		String filename = "file://"+defdir.getAbsoluteFile()+File.separator+"index.html";
		if (defind.exists() && defind.canRead() && (defind.lastModified() > meta.lastModified())) {
		    HTMLViewer viewWindow = new HTMLViewer(filename, "Corpus information");
		    System.out.println("Used existing corpus info in " + dir);
		} else {
		    if (!metadir.canWrite()) {
			dir=System.getProperty("user.home") + File.separator + "corpusdoc";
			defdir = new File(dir);
			filename = "file://"+defdir.getAbsoluteFile()+File.separator+"index.html";
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
	} else if ( cmd.equals("remove") ) {
	    if (currentCorpus==null) { return; }
	    String corpid=currentCorpus.corpusFullPath;
	    corpora.remove(corpid);
	    corporaNames.remove(corpid);
	    corporaPaths.remove(corpid);
	    treeModel.removeNodeFromParent(currentTreeNode);
	    corpusView.setEnabled(false);
	    corpusRemove.setEnabled(false);
	    try {
		mainPanel.remove((JComponent)corpusTabs.get(corpid));
		tabCorpus.remove(corpusTabs.get(corpid));
		corpusTabs.remove(corpid);
	    } catch (Exception ex) { 
		System.err.println("Failed to remove " + corpid);
	    }
	    currentCorpus=null;
	} else if ( cmd.equals("exit") ) {
	    exit();
	} else if ( cmd.equals("configure") ) {
	    configurePlugins();
	}
    }


    /**
     * Closes window and exits program.
     */
    public void exit() {
	close();
	System.exit( 0 );
    }

    /**
     * Closes window.
     */
    public void close() {
	preferences.corpora    = corpora;
	preferences.names      = corporaNames;
	prefloader.savePreferences(preferences);
	setVisible( false );
    }

    /**
     * New query will be executed by a new thread.
     */
    public void run(){
    }

    /** load an image catching exceptions */
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

    /** load an icon an catch exceptions */
    private ImageIcon loadIcon(String name) {
	try {
	    return new ImageIcon( loadImage(name) );
	} catch (Exception ex) {
	    return null;
	}
    }

    /** utility class to gather information about a corpus we know about */
    private class Corpus {
        public String corpusFullPath;
        public String corpusName;
	public NMetaData metadata;
	public DefaultMutableTreeNode treenode=null;
	public JComponent tab=null;
	
        public Corpus(NMetaData meta, String path, String name) {
	    corpusFullPath=path;
	    corpusName=name;
	    metadata=meta;
        }
	
        public String toString() {
            return corpusName;
        }
    }
    
    /** clean exit when window closes */
    private class ManagerWindowListener extends WindowAdapter {
	public void windowClosing(WindowEvent event)     { 
	    exit();
	}
    }

    /** add a new corpus to the list */
    public void loadCorpus() {
	fc = new JFileChooser( corporaDir );
	fc.setFileFilter( new MyFileFilter("xml") );
	int returnVal = fc.showOpenDialog(Manager.this);

	if( returnVal == JFileChooser.APPROVE_OPTION ){
	    File corpusFile = fc.getSelectedFile();
	    if( corpusFile.exists() && corpusFile.canRead() ){
		String filename=null;
		filename = corpusFile.getAbsolutePath();
		DefaultMutableTreeNode mtn = createCorpusTreeNode(filename, null, null);
		if (mtn!=null) { 
		    int insertionpoint=root.getChildCount();
		    treeModel.insertNodeInto((DefaultMutableTreeNode)mtn, root, insertionpoint);
		    corpora.add(filename);
		    //System.out.println("Corpus name: " + mtn.toString());
		    corporaNames.put(filename, mtn.toString());
		    
		    try { corporaDir = corpusFile.getParentFile(); } 
		    catch (Exception e) {
			corporaDir   = new File( System.getProperty("user.dir") );
		    } 
		    corpusTree.setVisible(true);
		    // I think this might be a swing bug - if there
		    // are no corpora present the root node is closed
		    if (insertionpoint==0) {
			corpusTree.expandPath(new TreePath(root.getPath()));
		    }
		} else {
		    statusBar.setText("Open corpus failed: Metadata invalid.");		    
		}
	    } else {
		statusBar.setText("Open corpus failed: Access denied.");
	    }
	}
    }

    /** a simple filter for metadata files: use .xml as the extension by default */
    private class MyFileFilter extends FileFilter {
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

    /** changing tab manually */
    private class TabSelectionListener implements ChangeListener {
	public void stateChanged(ChangeEvent e) {
	    try {
		Corpus corp = (Corpus)tabCorpus.get(mainPanel.getSelectedComponent());
		if (currentCorpus!=corp) {
		    corpusTree.setSelectionPath(new TreePath(corp.treenode.getPath()));
		}
	    } catch (Exception ex) {  } 
	}
    }

}
