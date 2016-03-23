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
import net.sourceforge.nite.nom.nomread.impl.*;
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
 * A simple display for multi-annotator data that will work on any
 * data set.  It just renders each coding as a separate window, one
 * line per element indented by level in the tree, with
 * NAME:content(att1:val)(att2:val)...  If the element is timed, it
 * gets synchronized with the signals.  It includes a search menu with
 * highlighting of results on the main display.
 *
 * @author Jonathan Kilgour, Jean Carletta November 2003
 **/

public class MultiAnnotatorDisplay implements WindowListener {
    private Clock niteclock;
    private NITEVideoPlayer video;
    NOMReadCorpus nom;
    NiteMetaData meta;
    JInternalFrame iframe;
    JInternalFrame iframe2;
    String corpusname;
    String toplayername;
    String commonlayername;
    String attributename;
    String observationname;
    JFrame frame;
    String exportdir = ".";
    JScrollPane pane;
    JDesktopPane desktop;
    private net.sourceforge.nite.search.GUI search=null;
    List textAreas = new ArrayList();
    
    private Engine searchEngine = new Engine();
    
    public MultiAnnotatorDisplay(String c, String o, String top, String comm, String att) {
	corpusname = c;
	observationname = o;
	toplayername = top;
	commonlayername = comm;
	attributename = att;
	
	try {
	    meta = new NiteMetaData(corpusname);
	} catch (NiteMetaException nme) {
	    nme.printStackTrace();
	}
	
	if (observationname==null) {
	    ChooseObservation co = new ChooseObservation(meta);
	    observationname=co.popupDialog();
	} 

	NLayer toplay = meta.getLayerByName(toplayername);
	NLayer commlay = null;
	if (commonlayername!=null) {
	    commlay = meta.getLayerByName(commonlayername);
	    if (commlay==null) {
		System.err.println("Can't find common layer (" + commonlayername + ") in the metadata.\n"); 
		System.exit(1);
	    }
	}
	//if (toplay==null || commlay==null) {
	if (toplay==null) {
	    System.err.println("Can't find top layer (" + toplayername + ") in the metadata.\n"); 
	    System.exit(1);
	}

	niteclock = new DefaultClock(meta, observationname);
	try {
	    nom = new NOMReadCorpus(meta);
	    nom.setLazyLoading(false);
	    NiteObservation obs = observationWithName(observationname);
	    ArrayList one_obs_list = new ArrayList();
	    one_obs_list.add(obs);
	    System.out.println("Loading...");
	    nom.loadReliability(toplay, commlay, attributename, null, one_obs_list);
	    System.out.println("Finished loading.");
	    search=new net.sourceforge.nite.search.GUI(nom);
	    System.out.println("Setup interface");
	    setupInterface(nom);
	    System.out.println("Setup interface DONE");
	} catch (NOMException nex) {
	    nex.printStackTrace();
	}
    }
    
    private void setupInterface(NOMReadCorpus nom) {
	frame = new JFrame("NXT Multi-Annotator Display");
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
        StyleConstants.setFontSize(linkstylehandler.getStyle(), 12);
	StyleConstants.setBold(linkstylehandler.getStyle(), false);
	StyleConstants.setItalic(linkstylehandler.getStyle(), true);

        // the root elements of the NOM are the roots of all the
        // trees in the corpus.  This includes codings, ontologies,
        // and object sets.  We want to add a window for each one.
	
        List rootlist = nom.getRootElements();
        Iterator roots_it = rootlist.iterator();
        NOMElement root = null;
        int x = 0;
        int y = 0;
	int xinc=20;
	int yinc=20;
        while (roots_it.hasNext()) {
	    root = (NOMElement) roots_it.next();
	    try {
		if (addGenericCodingDisplay(root, x, y, linkstylehandler)) {
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

	System.out.println("Do signals");
   
        x = 0;
        y = 0;
	xinc=20;
	yinc=20;
	NITEMediaPlayer player = null;
	for (Iterator it = meta.getSignals().iterator(); it.hasNext();) {
	    NSignal sig = (NSignal) it.next();
	    
	    if (sig.getType()==NSignal.AGENT_SIGNAL) {
		List agents = meta.getAgents();
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

	System.out.println("Done signals");

	JInternalFrame clockframe = niteclock.getDisplay();
	clockframe.setLocation(new Point(400, 450));
	//clockframe.setSize(new Dimension(450, 100));
	try {
	    desktop.add(clockframe);
	} catch (IllegalArgumentException iae) { // bad location!
	    clockframe.setLocation(new Point(150,15));
	    desktop.add(clockframe);
	}

	System.out.println("Done Clock");

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
    private boolean addGenericCodingDisplay(final NOMElement root, int x, int y, TextStyleHandler linkstylehandler) {
        List childlist = root.getChildren();
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
		    NElement nel = meta.getElementByName(first.getName());
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

	    NTextArea nta = new NTextArea() {
	    public boolean isResultRelevant(NOMElement result) {
		return comparable(result, root);
	    }

	    /** check if the result should be followed to its root in
	     * this window. The algorithm checks the annotator value
	     * of the root and result elements and doesn't follow the
	     * trail if they're defined and different. This has the effect that
	     * elements with the same ID but different annotators
	     * won't be confused */
	    private boolean comparable(NOMElement res, NOMElement root) {
		String ann1 = (String)res.getAttributeComparableValue(attributename);
		String ann2 = (String)root.getAttributeComparableValue(attributename);
		if (ann1==null || ann2==null) { return true; }
		if (ann1.equals(ann2)) { return true; }
		return false; 
	    }
        };

	    nta.setClock(niteclock);
	    nta.addStyle("link", linkstylehandler.getStyle());
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
	    nte =  new NTextElement(text, null, null, null);
        } else {
            nte =  new NTextElement(text, null, nwe.getStartTime(), nwe.getEndTime());
	};
	nte.setDataElement((ObjectModelElement) nome);
	nta.addElement(nte);
	nta.addElement(new NTextElement("\n",null));

        // show any pointers
        if (nwe.getPointers() != null) {
           for (Iterator kit = nwe.getPointers().iterator(); kit.hasNext();) {
               NOMPointer kid = (NOMPointer) kit.next();
	       String pttext="";
	       // graceful fail check (SourceForge bug 936361) - jonathan
	       if (kid.getToElement()==null) {
		   pttext = levelString(level + 1) + "pointer: " + kid.getRole() + "-> UNRESOLVED POINTER!! \n";
	       } else {
		   pttext = levelString(level + 1) + "pointer: " + kid.getRole() + "->" + kid.getToElement().getID() + "\n";
	       }
               NOMObjectModelElement ptnome = new NOMObjectModelElement(kid.getToElement());
               NTextElement ptnte =  new NTextElement(pttext, "link", ptnome);
	       nta.addElement(ptnte);
	   }
	}
        //recurse on children
	if (nwe.getChildren() == null) {
	   return;
	}
	for (Iterator kit = nwe.getChildren().iterator(); kit.hasNext();) {
           NOMElement kid = (NOMElement) kit.next();
           showElementAndRecurse(kid,level+1,nta,rootcolour);

	}
    }

    /**
     * We want to load one observation worth of data; return the
     * observation with the name
     * @param observation.  Choke if there isn't one.  */
    private NiteObservation observationWithName(String observation) {
	boolean found = false;
	NiteObservation returnval = null;
	List obs = meta.getObservations();
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
     *<li> -corpus  corpus </li>
     * <li> -observation observation </li>
     * <li> -toplayer layer-name (name of the top annotator-specific layer)</li>
     * <li> -commonlayer layer-name (name of the top common layer)</li>
     * <li> -attrinute agent-attribute-name </li>
     *</ul>
     *
     */
    public static void main(String[] args) {
	String corpus=null;
	String observation=null;
	String toplayer=null;
	String commonlayer=null;
	String attribute="coder";
	
	if (args.length < 2 || args.length > 10) { usage(); }
	for (int i=0; i<args.length; i++) {
	    String flag=args[i];
	    if (flag.equals("-corpus") || flag.equals("-c")) {
		i++; if (i>=args.length) { usage(); }
		corpus=args[i];
	    } else if (flag.equals("-observation") || flag.equals("-o")) {
		i++; if (i>=args.length) { usage(); }
		observation=args[i];
	    } else if (flag.equals("-toplayer") || flag.equals("-tl")) {
		i++; if (i>=args.length) { usage(); }
		toplayer=args[i];
	    } else if (flag.equals("-commonlayer") || flag.equals("-cl")) {
		i++; if (i>=args.length) { usage(); }
		commonlayer=args[i];
	    } else if (flag.equals("-attribute") || flag.equals("-a")) {
		i++; if (i>=args.length) { usage(); }
		attribute=args[i];
	    } else {
		usage();
	    }
	}
	if (corpus == null || toplayer==null) { usage(); }
	
	MultiAnnotatorDisplay m = new MultiAnnotatorDisplay(corpus, observation, 
							    toplayer, commonlayer,
							    attribute);
    }
    
    private static void usage() {
	System.err.println("Usage: java MultiAnnotatorDisplay -c metadata-filename -o observation-name -tl top-layer -cl common-layer -a annotator-attribute");
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
