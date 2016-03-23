/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nxt;

import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;

import net.sourceforge.nite.nstyle.*;
import net.sourceforge.nite.time.*;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.util.VersionChecker;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.util.Debug;
import net.sourceforge.nite.search.GUI.*;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteCorpus;
import net.sourceforge.nite.nom.NOMException;

import org.jdom.input.*;

/**
 * A simple top-level interface for NITE
 * Call like this:
 *    java GUI -corpus Data/meta/mock.xml
 * @author Jonathan Kilgour, May 2003
 **/
public class GUI extends JFrame implements ActionListener { 
    private final static String SEARCH = "Search one observation";
    private final static String VIEW = "Generic corpus display";
    NiteMetaData meta;
    String corpusname;
    HashMap programmap = new HashMap();
    HashMap viewmap = new HashMap();
    DefaultClock niteclock;
    JList jlist;
    JLabel statusbar;
    net.sourceforge.nite.nstyle.NStyle nstyle;

    public GUI(String corp) {
	corpusname = corp;

	try {
	    if (corpusname!=null) {
		/* First load the metadata */
		meta = new NiteMetaData(corpusname);
	    }
	    /* Check Java version */
	    VersionChecker vc = new VersionChecker();
	    int vres = vc.checkVersion();
	    if (vres==VersionChecker.VERSION_ERROR) { System.exit(1); }

	    /* Now set up the interface */
	    setupInterface();
	    populateInterface();
	} catch (NiteMetaException nme) {
	    nme.printStackTrace();
	}
    }
    
    /* just the bits of the interface that change */
    private void populateInterface() {
	String label="";
	if (meta==null) {
	    label = "Open a metadata file using the 'File' menu";	    
	} else {
	    label = "select a program from the list and click 'Run'";
	    if (meta.getCorpusDescription()!=null) { label = meta.getCorpusDescription() + ": " + label; }
	}
	statusbar.setText(label);

	List progs = new ArrayList();
	if (meta!=null) {
	    progs.add(SEARCH);
	    progs.add(VIEW);
	}
	List exprogs = listPrograms();
	List views = listViews();
	progs.addAll(exprogs);
	progs.addAll(views);
	jlist.setListData(progs.toArray());
    }

    /* set up once and for all the bits that stay the same */
    private void setupInterface() {
	setTitle("NITE XML Toolkit");

	JMenuBar menubar = new JMenuBar();
	JMenu file = new JMenu("File");
	file.add(new OpenMetadataAction());
	file.add(new ExitAction());
	menubar.add(file);
	setJMenuBar(menubar);

	statusbar = new JLabel("");
	this.getContentPane().add(statusbar, BorderLayout.SOUTH);
	jlist = new JList();
	
	JPanel p1 = new JPanel();
	p1.setBorder(new EtchedBorder());
	p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
	p1.add(new JScrollPane(jlist), BorderLayout.CENTER);
	JButton gobut = new JButton("Run");
	gobut.addActionListener(this);
	p1.add(gobut, BorderLayout.SOUTH);
	this.getContentPane().add(p1);
	setSize(500, 300);
	setVisible(true);
    }

    /* return a list of callable programs as described in the metadata
     * file for this corpus */
    private List listPrograms() {
	ArrayList proglist = new ArrayList();
	if (meta==null) { return proglist; }
	List progs = meta.getPrograms();
	boolean first=true;
	if (progs != null) {
	    for (Iterator pit = progs.iterator(); pit.hasNext(); ) {
		NCallableProgram ncp = (NCallableProgram) pit.next();
		String name = ncp.getName() + " \t (" + ncp.getDescription() + " program)";
		proglist.add(name);
		programmap.put(name, ncp);
	    }
	}
	return (List)proglist;
    }

    /* return a list of views as described in the metadata file for
     * this corpus */
    private List listViews() {
	ArrayList viewlist = new ArrayList();
	if (meta==null) { return viewlist; }
	List views = meta.getDataViews();
	boolean first=true;
	if (views != null) {
	    for (Iterator pit = views.iterator(); pit.hasNext(); ) {
		NDataView ndv = (NDataView) pit.next();
		String name = ndv.getDescription() + "(NITE stylesheet display)";
		viewlist.add(name);
		viewmap.put(name, ndv);
	    }
	}
	return (List)viewlist;
    }

    /** This handles all the button-presses etc for the interface */
    public void actionPerformed(ActionEvent ae) {
	String act = (String) jlist.getSelectedValue();
	if (act==null) { return; }
	if (act.equals(SEARCH)) {
	    search();
	} else if (act.equals(VIEW)) {
            // JC: 14 Sep 06, GenericDisplay will now take a fontsize, but the only way to make use
            // of that until we change this code (which defaults to 12) is to have them add a callable-program
            // to their metadata with the fontsize they want, or as a required argument that prompts entry.
            // I'm not sure whether we should just force configuration in that way, taking out the automatic
            // access to generic display here, or we should make this code ask in some way.  Configuration
            // for a data set also could involve setting the query that limits the windows coming up (which
            // is another useful thing about having it as a callable-program).
	    GenericDisplay gv = new GenericDisplay(corpusname,null,null,false);
	} else if (programmap.get(act)!=null) {
	    NCallableProgram ncp = (NCallableProgram)programmap.get(act);
	    execute(ncp);
	} else if (viewmap.get(act)!=null) {
	    NDataView ndv = (NDataView)viewmap.get(act);
	    runview(ndv);
	}
    }

    private void callAudio(File f){
	Debug.print("call audio", Debug.DEBUG);
	if(f.exists()){ 
	    NITEAudioPlayer n = new NITEAudioPlayer(f, niteclock);
	    nstyle.getDesktop().add(n);
	}
    }

    private void callVideo(File f){
	Debug.print("Call video", Debug.DEBUG);
	if (f.exists()){
	    NITEVideoPlayer p = new NITEVideoPlayer(f, niteclock);
	    nstyle.getDesktop().add(p);
	}
    }

    /* Run a stylesheet display: this code is based on Judy's
     * MainDisplay code */
    private void runview(NDataView ndv) {
	File ofile=null, ifile; 
	//start the nite clock 
	if (niteclock == null) {
	    niteclock = new DefaultClock();
	}
	ChooseObservation cob = new ChooseObservation(meta);
	String oname = cob.popupDialog();

	for (Iterator wit=ndv.getWindows().iterator(); wit.hasNext(); ) {
	    NWindow nwin = (NWindow)wit.next();
	    if (nwin.getType()==NWindow.STYLE) { // produce a styled display
		net.sourceforge.nite.meta.NStyle nss = meta.getStyleWithName(nwin.getName());
		if (nss==null) {
		    JOptionPane.showMessageDialog(this, "Failed to locate styled display: " + nwin.getName(), "Stylesheet Error", JOptionPane.ERROR_MESSAGE);
		    System.exit(0);
		} 
		String ifname = meta.getStylePath() + File.separator + nss.getName() + nss.getExtension();
		ifile = new File(ifname);
		String fname = meta.getCodingPath() + File.separator + oname + ".xml";
		ofile = new File (fname);
		if (nss.getApplication()==net.sourceforge.nite.meta.NStyle.NIE) {
		    try{
			nstyle = new net.sourceforge.nite.nstyle.NStyle(niteclock);
			// start the stylesheet processing
			nstyle.transform(new SAXBuilder().build(ofile), ifname);
		    } catch(Exception n){
			n.printStackTrace();
		    }
		} else {
		    Debug.print("This application only handles NIE calls, not other applications sucha s OTAB. Ignoring window: " + nwin.getName(), Debug.ERROR);
		}
		
	    } else if (nwin.getType()==NWindow.VIDEO || nwin.getType()==NWindow.AUDIO) {
		NSignal s = meta.findSignalWithName(nwin.getName());
		if (s==null) {
		    JOptionPane.showMessageDialog(this, "Failed to locate signal: " + nwin.getName() + ".", "Error", JOptionPane.ERROR_MESSAGE);
		}  else {
		    //Debug.print("Found a signal matching the name " + nwin.getName());
		    // First Check if it's interaction or agent signal
		    if (s.getType() == NSignal.AGENT_SIGNAL) {
				// Agent signal: open one signal per agent
			Iterator agit = meta.getAgents().iterator();
			while (agit.hasNext()) {
			    NiteAgent ag = (NiteAgent)agit.next();
			    String mypath = s.getFilename(oname, ag.getShortName());
			    //Debug.print("Looking for file: " + mypath);
			    if (nwin.getType() == NWindow.VIDEO) {
				callVideo(new File(mypath));
			    }else if (nwin.getType() == NWindow.AUDIO) {
				callAudio(new File(mypath));
			    }
			}
		    } else {
			String fullpath = s.getFilename(oname, (String)null);
			//Debug.print("Looking for: " + fullpath);
			// if it's a video file, call the video player
			if (nwin.getType() == NWindow.VIDEO) {
			    callVideo(new File(fullpath));
			} else if (nwin.getType() == NWindow.AUDIO) {
			    callAudio(new File(fullpath));
			}
		    }
		}
	    }
	}
    }

    /* popup the built-in search GUI */
    private void search () {
	NOMWriteCorpus nom = new NOMWriteCorpus(meta);
	ChooseObservation cob = new ChooseObservation(meta);
	String value = cob.popupDialog();
	if (value != null) {
	    try {
		nom.loadData(meta.getObservationWithName(value));
		net.sourceforge.nite.search.GUI prog = new net.sourceforge.nite.search.GUI(nom) {
			public void windowClosing(WindowEvent event) {  exit(); }
		    };
		prog.popupSearchWindow();
	    } catch (NOMException nex) {
		Debug.print("Failed to load observation " + value, Debug.ERROR);
		System.exit(1);
	    }
	}
    }
	
    /* call a callable program - supplying the arguments as required */
    private void execute (NCallableProgram ncp) {
	try {
	    String execstring="java " + ncp.getName() + " ";
	    Class prog = ClassLoader.getSystemClassLoader().loadClass(ncp.getName());
	    ArrayList arglist = new ArrayList();
	    int index=0;
	    // There are pros and cons of running via the Runnable
	    // interface - its slower, but exits from sub-processes
	    // don't cause main process to bomb!
	    for (Iterator rit=ncp.getRequiredArguments().iterator(); rit.hasNext(); ) {
		NCallableProgram.Argument arg = (NCallableProgram.Argument) rit.next();
		String flag = "";
		if (arg.getFlagName() != null && !arg.getFlagName().equals("")) {
		    flag="-";
		}
		flag += arg.getFlagName();
		execstring += flag + " ";
		if (!flag.equals("") && !flag.equals(" ")) { arglist.add(flag); }
		String value="";
		if (arg.getType() == NCallableProgram.Argument.CORPUS_NAME) {
		    value=corpusname;
		} else if (arg.getType() == NCallableProgram.Argument.OBSERVATION_NAME) {
		    ChooseObservation cob = new ChooseObservation(meta);
		    value = cob.popupDialog();
		} else if (arg.getType() == NCallableProgram.Argument.ANNOTATOR_NAME) {
		    ChooseAnnotator ca = new ChooseAnnotator(meta);
		    value = ca.popupDialog();
		} else if (arg.getDefaultValue()!=null) {
		    value = arg.getDefaultValue();
		} else {
		    value = getRequiredValue(arg.getFlagName());
		}
		if (value==null) {
		    Debug.print("Required argument not provided. Exiting. ", Debug.ERROR);
		    System.exit(0);
		}
		if (!value.equals("") && !value.equals(" ")) { arglist.add(value); }
		execstring += value + " ";
	    }
	    Debug.print("Executing: " + execstring, Debug.DEBUG);
	    String [] args = new String[ arglist.size() ];
	    for (int i=0; i<arglist.size(); i++) {
		args[i] = (String) arglist.get(i);
	    }

	    // This command runs as a process and doesn't suffer the
	    // problems of System.exit() in the threads.
	    //	    Runtime.getRuntime().exec(execstring);
	    Method main_method=null;
	    Method[] meths = prog.getMethods();
	    for (int i=0; i<meths.length; i++ ) {
		Method meth = meths[i];
		if (meth.getName().equals("main")) {
		    main_method=meth;
		    break;
		}
	    }
	    if (main_method==null) {
		Debug.print("Error: no main method to call in program " + ncp.getName(), Debug.ERROR);
	    } else {
		for (int j=0; j<args.length; j++) {
		    Debug.print(args[j] + " ", Debug.DEBUG);
		}
		Debug.print(". ", Debug.DEBUG);
	    }		
	    RunProg rp = new RunProg(main_method, args);
	    try {
		new Thread(rp).start();
	    } catch (ThreadDeath td1) {
		// nowt??
	    }
	} catch (java.lang.ClassNotFoundException cnfe) {
	    JOptionPane.showMessageDialog(this, "Failed to find program on your classpath!!", "Classpath Error", JOptionPane.ERROR_MESSAGE);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /** 
     * pop up a dialog to get an argument value 
     */
    private String getRequiredValue(String flag) {
	return (String) JOptionPane.showInputDialog(this, "Program requires argument '" 
						    + flag + "'. Type value:", 
						    "Please enter argument value",
						    JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Called to start the  application.
     */
    public static void main(String args[]){
	String corpus=null;
	if (args.length > 2) { usage(); }
	for (int i=0; i<args.length; i++) {
	    String flag=args[i];
	    if (flag.equals("-corpus") || flag.equals("-c")) {
		i++; if (i>=args.length) { usage(); }
		corpus=args[i];
	    } else {
		usage();
	    }
	}
	//if (corpus == null) { usage(); }
	GUI m = new GUI(corpus);
    }

    private static void usage () {
	Debug.print("Usage: java GUI -corpus <path-to-metadata>", Debug.ERROR);
	System.exit(0);
    }

    private class RunProg implements Runnable {
	Method meth;
	String [] args; 

	public RunProg(Method meth, String[] args) {
	    this.meth=meth;
	    this.args=args;
	}
	
	public void run() {
	    try {
		Object [] rr = { args };
		meth.invoke(null, rr);
	    } catch (Throwable e) {
		e.printStackTrace();
	    }
	}

    }

    /** A very simple "exit" action */
    public class ExitAction extends AbstractAction {
	public ExitAction() {
	    super("Exit");
	}
	public void actionPerformed(ActionEvent ev) {
	    System.exit(0);
	}
    }

    /** Change metadata file  */
    public class OpenMetadataAction extends AbstractAction {
	public OpenMetadataAction() {
	    super("Open new metadata file...");
	}
	public void actionPerformed(ActionEvent ev) {
	    JFileChooser fc = new JFileChooser(".");
	    fc.setFileFilter( new MyFileFilter("xml") );
	    int returnVal = fc.showOpenDialog(GUI.this);
	    File corpusFile = fc.getSelectedFile();
	    if( corpusFile.exists() && corpusFile.canRead() ){
		try {
		    corpusname = corpusFile.getPath();
		    meta = new NiteMetaData(corpusname);
		    populateInterface();
		} catch (NiteMetaException nme) {
		    statusbar.setText("Open corpus failed. Metadata loading error.");
		}
	    } else {
		statusbar.setText("Open corpus failed: Access denied.");
	    }
	}
    }

    /** This is Holger Voormann's code from search.GUI (didn't want to
     * make it public) */
    class MyFileFilter extends FileFilter {
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

}

