package net.sourceforge.nite.gui.util;

import java.util.*;
import java.io.File;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;

import net.sourceforge.nite.search.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.gui.textviewer.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.nstyle.handler.*;
import net.sourceforge.nite.time.*;

import net.sourceforge.nite.nom.NOMException;

import java.util.Comparator;
import java.util.Collections;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
/**
 * A simple display that will work on any data set.  It just renders each
 * coding as a separate window, one line per element indented by level in
 * the tree, with NAME:content(att1:val)(att2:val)...
 * If the element is timed, it gets synchronized with the signals.  It
 * includes a search menu with highlighting of results on the main display.
 *
 * The default behaviour is to turn off lazy loading and load
 * everything. However, if a query is passed as a command line
 * argument, lazy loading is left on, and only those files directly
 * loaded by the query are displayed. Future searches (and even the
 * display process itself) may cause more data to be loaded but it
 * will not be displayed.
 *
 * Problems:
 *
 * It works by grabbing all the document roots and doing a left-right,
 * depth-first traversal, which means that in some corpus designs there
 * are nodes that get visited more than once.  We need to restrict visitation
 * by colour, although this arrangement does have the advantage of not
 * treating nite:child and child differently.
 * 
 * This boots up as many signal players as there are signals. It's up
 * to the user to close/mute all but one if they want to make sense of it.
 * 
 * It would be better to use NTrees than text areas for the display (don't
 * need to mess around with indentation and get collapsible nodes), but 
 * NTree doesn't currently implement the QueryResultHandler interface.
 *
 * @author Jonathan Kilgour, Jean Carletta November 2003
 **/

public class GenericDisplay implements WindowListener {
    private Clock niteclock;
    private NITEVideoPlayer video;
    NOMWriteCorpus nom;
    NiteMetaData controlData;
    JInternalFrame iframe;
    JInternalFrame iframe2;
    String corpusname;
    String observationname;
    String querystring;
    int fontsize;
    JFrame frame;
    String exportdir = ".";
    JScrollPane pane;
    JDesktopPane desktop;
    NOMWriteElement changed;
    private net.sourceforge.nite.search.GUI search=null;
    List textAreas = new ArrayList();
    boolean comments=false;
    
    private Engine searchEngine = new Engine();
    
    public GenericDisplay(String c, String o, String q, int f) {
	commonStartup(c,o,q, f, false);
    }

    public GenericDisplay(String c, String o, String q, boolean com) {
	commonStartup(c,o,q,12, com);
    }
    public GenericDisplay(String c, String o, String q, int f, boolean com) {
	commonStartup(c,o,q,f, com);
    }

    private void commonStartup(String c, String o, String q, int f, boolean com) {
	corpusname = c;
	observationname = o;
	querystring = q;
	comments=com;
	fontsize = f;
	try {
	    controlData = new NiteMetaData(corpusname);
	} catch (NiteMetaException nme) {
	    nme.printStackTrace();
	}
	
	if (observationname==null) {
	    ChooseObservation co = new ChooseObservation(controlData);
	    observationname=co.popupDialog();
	} 
	
	if (observationname==null) {
	    ChooseObservation co = new ChooseObservation(controlData);
	    observationname=co.popupDialog();
	    if (observationname==null) { System.exit(0); }
	}

	niteclock = new DefaultClock(controlData, observationname);
	try {
	    nom = new NOMWriteCorpus(controlData);
	    if (querystring==null) {
		nom.setLazyLoading(false);
	    }
	    NiteObservation obs = observationWithName(observationname);
	    nom.loadData(obs);
	    search=new net.sourceforge.nite.search.GUI(nom);
	    // execute the query to load some data before dis
	    if (querystring!=null) {
		search.performQuery(querystring);
	    }
	    setupInterface(nom, fontsize);
	    /*
	    for (Iterator txit=textAreas.iterator(); txit.hasNext(); ) {
		NTextArea nta = (NTextArea) txit.next();
		System.out.println("Scroll to start");
		nta.gotoDocumentStart();
	    }
	    */
	} catch (NOMException nex) {
	    nex.printStackTrace();
	}
    }
    
    private void setupInterface(NOMWriteCorpus nom, int fontsize) {
	frame = new JFrame("NXT Generic Corpus Display");
	frame.addWindowListener(this);
	JMenuBar menubar = new JMenuBar();
	JMenu file = new JMenu("File");
	//file.add(new SaveAction());
	file.add(new ExitAction());
	menubar.add(file);
	/* Make the search menu too! */
	JMenu search = new JMenu("Search");
	search.add(new SearchAction());
	menubar.add(search);
	frame.setJMenuBar(menubar);
	desktop = new JDesktopPane();

        // NITE expects text styles to be set up in advance.  Make
        // one for representing out of document links.  This just
	// creates the style; it still has to be added to every
        // text area that will need it.
	TextStyleHandler linkstylehandler = new TextStyleHandler();
	linkstylehandler.init("", null);
	linkstylehandler.setName("link");
	linkstylehandler.makeNewStyle();
	linkstylehandler.setStyle(linkstylehandler.getColours(linkstylehandler.getStyle(), "black", "black"));
        StyleConstants.setFontSize(linkstylehandler.getStyle(), fontsize);
	StyleConstants.setBold(linkstylehandler.getStyle(), false);
	StyleConstants.setItalic(linkstylehandler.getStyle(), true);

        // Also make one with the correct fontsize for normal stuff.
        // It's the same but without italics.
	TextStyleHandler normalstylehandler = new TextStyleHandler();
	normalstylehandler.init("", null);
	normalstylehandler.setName("normal");
	normalstylehandler.makeNewStyle();
	normalstylehandler.setStyle(normalstylehandler.getColours(normalstylehandler.getStyle(), "black", "black"));
        StyleConstants.setFontSize(normalstylehandler.getStyle(), fontsize);
	StyleConstants.setBold(normalstylehandler.getStyle(), false);
	StyleConstants.setItalic(normalstylehandler.getStyle(), false);

        // the root elements of the NOM are the roots of all the
        // trees in the corpus.  This includes codings, ontologies,
        // and object sets.  We want to add a window for each one.
	
        List rootlist = new ArrayList(nom.getRootElements());
        Iterator roots_it = rootlist.iterator();
        NOMElement root = null;
        int x = 0;
        int y = 0;
	int xinc=20;
	int yinc=20;
        while (roots_it.hasNext()) {
	    root = (NOMElement) roots_it.next();
	    try {
		if (addGenericCodingDisplay(root, x, y, normalstylehandler, linkstylehandler)) {
		    x = x + xinc;
		    y = y + yinc;
		}
	    } catch (Throwable e) {
		e.printStackTrace();
		System.exit(0);
	    }
	    if (y>700) {
		y=0;
		x=50;
	    }
	};
   
	//also add a player for every signal
        x = 0;
        y = 0;
	xinc=20;
	yinc=20;
	NITEMediaPlayer player = null;
	for (Iterator it = controlData.getSignals().iterator(); it.hasNext();) {
	    NSignal sig = (NSignal) it.next();
	    
	    if (sig.getType()==NSignal.AGENT_SIGNAL) {
		List agents = controlData.getAgents();
		for (Iterator ait=agents.iterator(); ait.hasNext(); ) {
		    NAgent nag = (NAgent)ait.next();
		    String filename = sig.getFilename(observationname, nag.getShortName());
		    // perhaps all this could be taken out as a method
		    // so we didn't have to repeat below, but we rely
		    // on update of globals for positioning so it
		    // would still be messy.
		    if (sig.getMediaType() == NSignal.VIDEO_SIGNAL) {
			player = new NITEVideoPlayer(new File(filename), 
						     (DefaultClock) niteclock, sig.getName());
		    } else if (sig.getMediaType() == NSignal.AUDIO_SIGNAL) {
			player = new NITEAudioPlayer(new File(filename),
						     (DefaultClock) niteclock, sig.getName());
		    }
		    
		    if (player!=null) {
			player.setLocation(new Point(440 + x, y));
			// what does setID do?
			player.setID(3);
			try {
			    desktop.add(player);
			} catch (IllegalArgumentException iae) { // bad placement - try again!
			    x=20;
			    y=0;
			    xinc=0;
			    player.setLocation(new Point(440 + x, y));
			    desktop.add(player);
			}
			x = x + xinc;
			y = y + yinc;
		    }

		}
	    } else {
		String filename = sig.getFilename(observationname, (String)null);

		if (sig.getMediaType() == NSignal.VIDEO_SIGNAL) {
		    player = new NITEVideoPlayer(new File(filename), 
						 (DefaultClock) niteclock, sig.getName());
		} else if (sig.getMediaType() == NSignal.AUDIO_SIGNAL) {
		    player = new NITEAudioPlayer(new File(filename),
						 (DefaultClock) niteclock, sig.getName());
		}
	    
		if (player!=null) {
		    player.setLocation(new Point(440 + x, y));
		    // what does setID do?
		    player.setID(3);
		    try {
			desktop.add(player);
		    } catch (IllegalArgumentException iae) { // bad placement - try again!
			x=20;
			y=0;
			xinc=0;
			player.setLocation(new Point(440 + x, y));
			desktop.add(player);
		    }
		    x = x + xinc;
		    y = y + yinc;
		}
	    }

	}

	niteclock.getDisplay().setLocation(new Point(400, 450));
	//niteclock.getDisplay().setSize(new Dimension(450, 100));
	try {
	    desktop.add(niteclock.getDisplay());
	} catch (IllegalArgumentException iae) { // bad location!
	    niteclock.getDisplay().setLocation(new Point(150,15));
	    desktop.add(niteclock.getDisplay());
	}

	JPanel panel = new JPanel();
	desktop.setSize(new Dimension(900, 650));
	frame.getContentPane().add(desktop);
	frame.setSize(new Dimension(900, 650));
	frame.setVisible(true);
    }
    

/** a generic way of displaying some coding that just shows the tree structure
    using indentation in an NTextArea, with name:(att1:val)(att2:val).. for
    each element.  

    We can't do anything clever about placement or sizing, so don't try, just overlap the
    windows so that people will notice.

    If we just treewalk through children doing a complete left-right, depth-first
    traversal, some nodes could end up being displayed in more than one window, 
    because in this data model, an element in one coding/document can have a child
    in another. For this reason, we need to check at each node whether we're still
    in the same coding/document.  It's underdocumented at the moment, but "colour"
    is a public property that is unique per document - so this check is performed
    by noting the colour of the root node and checking the property as the nodes
    are traversed.  This is a part of the NITE design that needs revisiting; we
    thought originally that we would only need to use "colour" internally (for 
    serialization)and so the methods for it are probably insufficient for some 
    purposes.
**/
    private boolean addGenericCodingDisplay(NOMElement root, int x, int y, TextStyleHandler normalstylehandler, TextStyleHandler linkstylehandler) {
        List childlist = root.getChildren();
	if (comments) {
	    childlist = root.getChildrenWithInterleavedComments();
	}
        if (childlist != null) {
	    NOMElement first=(NOMElement)childlist.get(0);
	    // set up for displaying the non-empty document
	    String title=null;
	    try { // try to make a sensible title for the window
		title=first.getLayer().getName(); 
		if (first.getAgentName()!=null) { title = title + ": " + first.getAgentName(); }
	    } catch (Exception ex) { }
	    if (title==null) {
		try {
		    NElement nel = controlData.getElementByName(first.getName());
		    Object container = nel.getContainer();
		    title="";
		    if (nel.getContainerType()==NElement.ONTOLOGY) {
			title = "Ontology: " + ((NOntology)container).getName();
		    } else if (nel.getContainerType()==NElement.OBJECTSET) {
			title = "Object Set: " + ((NObjectSet)container).getName();
		    } else if (nel.getContainerType()==NElement.CORPUSRESOURCE) {
			title = "Corpus Resource: " + ((NCorpusResource)container).getName();
		    }
		} catch (Exception ex) { }
	    }

	    if (title==null) { title="Generic Display"; }
	    iframe = new JInternalFrame(title, true, false, true, true);
	    iframe.setSize(new Dimension(220, 320));
	    iframe.setLocation(new Point(x, y));

	    NTextArea nta = new NTextArea();
	    nta.setClock(niteclock);
	    nta.addStyle("link", linkstylehandler.getStyle());	    
            nta.addStyle("normal", normalstylehandler.getStyle());
	    niteclock.registerTimeHandler((TimeHandler) nta);
	    if (search != null) { search.registerResultHandler(nta); }
	    textAreas.add(nta);
	    nta.addMouseListener(new MyMouseListener(nta));
	    
	    // tree walk the document, rendering elements
            // as we go.  We don't want to show the root, so we need
            // to iterate over the first level children here.

            Iterator child_it = childlist.iterator();
            while (child_it.hasNext()) {
                NOMElement child = (NOMElement)child_it.next();
                showElementAndRecurse(child,0, nta, root.getColour());
            };            
	   // stick it up
	   pane = new JScrollPane(nta);
	   iframe.getContentPane().add(pane);
	   iframe.setVisible(true);
	   desktop.add(iframe);
	   nta.gotoDocumentStart();
	   return true;
	} else {
	    System.out.println("Didn't find any content for document " + root.getColour() + ".");
	}
	return false;
    }

    private String levelString (int level) {
	String retval = "";
        for (int i=1;i<=(level*10);++i) {
	    retval += " ";
	};
	return retval;
    }

    
    /** display shows one line per element, name: (att1: attval)(att2: attval)...
        with rudimentary display of pointers and out-of-document children (an extra
        display line with child (or pointer, with role) and the id of the referenced element)
     */
    private void showElementAndRecurse(NOMElement nwe, int level, NTextArea nta, String rootcolour) {
	if (nwe == null) {
	   return;
	}

	if (nwe.isComment()) {
	    String text = levelString(level) + "comment: " + nwe.getText() + "\n";
            NTextElement nte =  new NTextElement(text, "link", null);
	    nta.addElement(nte);
	    return;
	}
	
        // we're rendering something; make an object model element for it.
        // This is the abstraction by which display objects deal with the
        // NOM; the interface works for both NOM and JDOM, and makes it so
        // that the display objects can work for either kind of data.
        NOMObjectModelElement nome = new NOMObjectModelElement(nwe);
        // if the element is in a different document from what we're
        // rendering make a rudimentary remark about the relationship.
	if (!(nwe.getColour().equals(rootcolour))) {
	    String text = levelString(level) + "child: " + nwe.getID() + "\n";
            // make a new text element with the given text.
            NTextElement nte =  new NTextElement(text, "link", nome);
            // add the text element to the display's text area
	    nta.addElement(nte);
	    return;
	};

        String text = levelString(level) + nwe.getName() + "[" + nwe.getID() + "]" + ": ";
        // iterate over attributes adding representation to the string
        String content = nwe.getText();
        if (content != null) {
            text = text + content + " ";
	};
        List attlist = nwe.getAttributes();
        Iterator att_it = attlist.iterator();
        while (att_it.hasNext()) {
            NOMAttribute att = (NOMAttribute)att_it.next();
            String attval = null;
            if (att.getType() == NOMAttribute.NOMATTR_NUMBER) {
                 attval = att.getDoubleValue().toString();
	    } else {
                attval = att.getStringValue();
	    }
            text = text + "(" + att.getName() + ": " + attval + ")";
	};
        //text = text + "\n";
        NTextElement nte = null;
        if (nwe.getTimeType() == NOMElement.UNTIMED) {
	    nte =  new NTextElement(text, "normal", null, null);
        } else {
            nte =  new NTextElement(text, "normal", nwe.getStartTime(), nwe.getEndTime());
	};
	nte.setDataElement((ObjectModelElement) nome);
	nta.addElement(nte);
	nta.addElement(new NTextElement("\n","normal"));

        // show any pointers
        if (nwe.getPointers() != null) {
           for (Iterator kit = nwe.getPointers().iterator(); kit.hasNext();) {
               NOMPointer kid = (NOMPointer) kit.next();
	       String pttext="";
	       // graceful fail check (SourceForge bug 936361) - jonathan
	       NOMObjectModelElement ptnome = null;
	       if (kid.getToElement()==null) {
		   pttext = levelString(level + 1) + "pointer: " + kid.getRole() + "-> UNRESOLVED POINTER!! \n";
	       } else {
		   pttext = levelString(level + 1) + "pointer: " + kid.getRole() + "->" + kid.getToElement().getID() + "\n";
		   ptnome = new NOMObjectModelElement(kid.getToElement());
	       }
               NTextElement ptnte =  new NTextElement(pttext, "link", ptnome);
	       nta.addElement(ptnte);
	   }
	}
        //recurse on children
        List childlist = nwe.getChildren();
	if (comments) {
	    childlist = nwe.getChildrenWithInterleavedComments();
	}
	if (childlist == null) { return; }	
	for (Iterator kit = childlist.iterator(); kit.hasNext();) {
           NOMElement kid = (NOMElement) kit.next();
           showElementAndRecurse(kid,level+1,nta,rootcolour);
	}
    }

    private String getAttrOrEmptyString(NOMWriteElement nwe, String attname) {
	if (null != nwe.getAttribute(attname)) {
	    return nwe.getAttribute(attname).getStringValue();
	} else {
            return "";
        }
    }

    /**
     * We want to load one observation worth of data; return the
     * observation with the name
     * @param observation.  Choke if there isn't one.  */
    private NiteObservation observationWithName(String observation) {
	boolean found = false;
	NiteObservation returnval = null;
	List obs = controlData.getObservations();
	Iterator obs_it = obs.iterator();
	while ((obs_it.hasNext()) && (found == false)) {
	    NiteObservation next = (NiteObservation) obs_it.next();
	    if (observation.equals(next.getShortName()) == true) {
		found = true;
		returnval = next;
	    };
	}
	if (found == false) {
	    System.out.println(
			       "Observation named "
			       + observation
			       + " doesn't exist:  exiting...");
	    System.exit(0);
	}
	return returnval;
    }
    
    /**
     * Called to start the  application.
     * Legal command line arguments are:
     *<ul>
     * <li> -corpus  corpus </li>
     * <li> -observation observation </li>
     * <li> -query query </li>
     * <li> -fontsize fontsize </li>
     * <li> -comments </li>
     *</ul>
     *
     * The corpus argument is the name of the metadata file; the
     * observation names the observation to load and the optional
     * query argument specifies a query that is run before the display
     * starts up. Only the data loaded as a direct result of that
     * query will be displayed (along with any corpus-level
     * files). Finally, if the -comments argument is present, comments
     * will be displayed.
     */
    public static void main(String[] args) {
	String corpus=null;
	String observation=null;
	String query=null;
	String fontsizestr=null; //contains string representation of a number
	boolean comments=false;
	
	if (args.length < 2 || args.length > 8) { usage(); }
	for (int i=0; i<args.length; i++) {
	    String flag=args[i];
	    if (flag.equals("-corpus") || flag.equals("-c")) {
		i++; if (i>=args.length) { usage(); }
		corpus=args[i];
	    } else if (flag.equals("-query") || flag.equals("-q")) {
		i++; if (i>=args.length) { usage(); }
		query=args[i];
	    } else if (flag.equals("-observation") || flag.equals("-o")) {
		i++; if (i>=args.length) { usage(); }
		observation=args[i];
	    } else if (flag.equals("-fontsize") || flag.equals("-f")) {
		i++; if (i>=args.length) { usage(); }
		fontsizestr=args[i];
	    } else if (flag.equals("-comments") || flag.equals("-com")) {
		comments=true;
	    } else {
		usage();
	    }
	}
	if (corpus == null) { usage(); }
        int fontsize = 12;
        if (fontsizestr != null) {
           try {
		Integer fs = Integer.valueOf(fontsizestr);
		fontsize = fs.intValue();
		} catch (NumberFormatException e) {
		   System.err.println(
			"Can't interpret " + fontsize + " as an integer to use as font size - using 12 point.");
		}
	}	
	GenericDisplay m = new GenericDisplay(corpus, observation, query, fontsize, comments);
    }
    
    private static void usage() {
	System.err.println("Usage: java GenericDisplay -c metadata-filename [ -o observation-name ]  [ -f font-size] [ -q query ]");
	System.exit(0);
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

    /** A "search" action - pops up a search GUI */
    public class SearchAction extends AbstractAction {
	public SearchAction() {
	    super("Search...");
	}
	public void actionPerformed(ActionEvent ev) {
	    search.popupSearchWindow();
	}
    }

    /** internal method to pass on highlights */
    private void setSelectedAll(NTextArea nta, Set highlights) {
	for (Iterator tait=textAreas.iterator(); tait.hasNext(); ) {
	    NTextArea nt = (NTextArea)tait.next();
	    if (nt!=nta) { nt.clearHighlights(NTextArea.SELECTION_HIGHLIGHTS); }
	}
	for (Iterator tait=textAreas.iterator(); tait.hasNext(); ) {
	    NTextArea nt = (NTextArea)tait.next();
	    if (nt!=nta) {
		for (Iterator hit=highlights.iterator(); hit.hasNext(); ) {
	        nt.setHighlighted(NTextArea.SELECTION_HIGHLIGHTS,(ObjectModelElement)hit.next());
		}
	    }
	}
    }

    /* handle the clicks on NTextAreas: highlight element on all other displays */
    public class MyMouseListener implements MouseListener {
	private NTextArea nta;

	public MyMouseListener(NTextArea nta) {
	    this.nta=nta;
	}

	public void mouseClicked(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mousePressed(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) {
	    int b = e.getButton();
	    // Do nothing if ctrl is on so we don't clash with media playing!
	    String mods = e.getMouseModifiersText(e.getModifiers());
	    if (mods.indexOf("Ctrl")>=0) { return; }
	    
	    if (b==MouseEvent.BUTTON1) { 
		Set sel = nta.getSelectedElements();
		if (sel==null || sel.size()==0) { return; } 
		setSelectedAll(nta, sel);
	    }
	}

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
	System.exit(0);
    }
    
    /** NOP (method needed for implementing WindowListener).
     * @param event parameter isn't used */
    public void windowDeactivated(WindowEvent event) {}
    
    /** NOP (method needed for implementing WindowListener).
     * @param event parameter isn't used */
    public void windowDeiconified(WindowEvent event) {}
    
    /** NOP (method needed for implementing WindowListener).
     * @param event parameter isn't used */
    public void windowIconified(WindowEvent event)   {}
    
    /** NOP (method needed for implementing WindowListener).
     * @param event parameter isn't used */
    public void windowOpened(WindowEvent event)      {}

}
