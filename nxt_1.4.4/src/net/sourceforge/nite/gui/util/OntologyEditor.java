/**
 * Here's a simple Prefuse display that shows an NXT Ontology as a
 * tree, and allows some editing of the elements using the new
 * ElementDisplay utility. It doesn't show off Prefuse's power for
 * animation etc, so is only really a first step.
 *
 * You need prefuse.jar on your classpath along with the standard NXT
 * classpath to compile and run (prefuse may be added to the NXT
 * distribution for the next release). Call like this: 
 *
 * java -c Data/AMI/ami.xml -e ne-type
 *
 * This code is based on AggregateDemo from the Prefuse demo set
 * Jonathan Kilgour 14/4/08
 */
package net.sourceforge.nite.gui.util;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.TreeLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.DragControl;
import prefuse.controls.HoverActionControl;
import prefuse.controls.ControlAdapter;
import prefuse.data.Tree;
import prefuse.data.Table;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.render.ShapeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualTree;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import net.sourceforge.nite.search.*;
import net.sourceforge.nite.gui.util.ElementDisplay;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;

/**
 * Demo application showing an ontology (from NXT) as a Prefuse graph.
 */
public class OntologyEditor extends Display implements NOMView {

    public static final String TREE = "ontology";
    public static final String NODES = "ontology.nodes";
    public static final String EDGES = "ontology.edges";
    // the data 
    private String metadatafile=null;
    private String ontology_element=null;
    private Tree tree;
    private TreeLayout treelayout=null;
    private Table m_nodes;
    boolean modelchanged=false;
    NOMCorpus nom;
    NOMElement root;
    ElementDisplay ed=null;
    JFrame frame=null;
    
    public OntologyEditor(String meta, String elname) {
        // initialize display and data
        super(new Visualization());
	NiteMetaData metadata=null;
	try {
	    metadata = new NiteMetaData(meta);
	} catch (NiteMetaException nme) {
	    nme.printStackTrace();
	    System.exit(1);
	}
	commonConstructor(metadata, elname);
    }

    public OntologyEditor(NiteMetaData metadata, String elname) {
        super(new Visualization());
	commonConstructor(metadata, elname);
    }

    private void commonConstructor(NiteMetaData metadata, String elname) {
        initOntology(metadata, elname);
        
        // set up the renderers
	LabelRenderer r = new LabelRenderer("name");
	r.setRoundedCorner(8, 8);
	DefaultRendererFactory drf = new DefaultRendererFactory(r);
        m_vis.setRendererFactory(drf);
        
        // set up the visual operators
        // first set up all the color actions
        ColorAction nStroke = new ColorAction(NODES, VisualItem.STROKECOLOR);
        nStroke.setDefaultColor(ColorLib.gray(100));
        nStroke.add("_hover", ColorLib.gray(50));
        
        ColorAction nFill = new ColorAction(NODES, VisualItem.FILLCOLOR);
        nFill.setDefaultColor(ColorLib.rgb(190,190,255));
        nFill.add("_hover", ColorLib.rgb(230,230,255));
        
        ColorAction nEdges = new ColorAction(EDGES, VisualItem.STROKECOLOR);
        nEdges.setDefaultColor(ColorLib.gray(100));

        ColorAction text = new ColorAction(NODES,
                VisualItem.TEXTCOLOR, ColorLib.gray(0));
        text.add("_hover", ColorLib.rgb(150,20,20));
        
        int[] palette = new int[] {
            ColorLib.rgba(255,200,200,150),
            ColorLib.rgba(200,255,200,150),
            ColorLib.rgba(200,200,255,150)
        };

        // bundle the color actions
        ActionList colors = new ActionList();
        colors.add(nStroke);
        colors.add(nFill);
        colors.add(nEdges);
        colors.add(text);
        
        // now create the main layout routine
        ActionList layout = new ActionList(Activity.INFINITY);
        layout.add(colors);
	treelayout=new NodeLinkTreeLayout(TREE);
        layout.add(treelayout);
        layout.add(new RepaintAction());
        m_vis.putAction("color", colors);
        m_vis.putAction("layout", layout);
        
        // set up the display
        setSize(500,500);
        pan(250, 250);
        setHighQuality(true);
        addControlListener(new DragControl());
        addControlListener(new ZoomControl());
        addControlListener(new PanControl());
        addControlListener(new MyMouseControl());

        // assign the colors
        //m_vis.run("color");
        // set things running
        m_vis.run("layout");
    }
    
    private void initOntology(NiteMetaData metadata, String element) {
	try {
	    nom = new NOMWriteCorpus(metadata, System.err);
	    nom.registerViewer(this);
	    NElement el=metadata.getElementByName(element);
	    if (el==null || el.getContainerType()!=NElement.ONTOLOGY) {
		System.err.println("Element " + element + " is not an ontology element.");
	    }
	    nom.loadData(((NObservation)metadata.getObservations().get(0)));
	    Engine searchEngine = new Engine();
	    List reslist = searchEngine.search((SearchableCorpus)nom, "($e "+element+")(forall $e2 "+element+"): $e^$e2");
	    if (reslist==null || reslist.size()<2) {
		System.err.println("Found no " + element + " elements in corpus.");
		System.exit(1);
	    }
	    root = (NOMElement)((List)reslist.get(1)).get(0);
	    makeTree();
	} catch (NOMException nex) {
	    nex.printStackTrace();
	    System.exit(1);
	} catch (Throwable thr) {
	    thr.printStackTrace();
	    System.exit(1);
	}
    }

    /** new tree */
    private void makeTree() {
	tree = new Tree();
	m_nodes = tree.getNodeTable();
	m_nodes.addColumn("name", String.class);
	m_nodes.addColumn("element", NOMElement.class);
	treeWalk(null, root);
	completeTree();
    }

    /** 'walk the tree' of an ontology creating a display */
    private void treeWalk(Node parent, NOMElement el) {
	Node n1=null;
	if (parent!=null) { n1=tree.addChild(parent); }
	else { n1 = tree.addRoot(); }
	n1.set("element", el);
	n1.setString("name", (String)el.getAttributeComparableValue("name"));
	List kids = el.getChildren();
	if (kids!=null) {
	    for (Iterator kit=kids.iterator(); kit.hasNext(); ) {
		NOMElement nel = (NOMElement)kit.next();
		treeWalk(n1, nel);
	    }
	}
    }

    /** finished the tree, set some properties and realize */
    private void completeTree() {
	Table t = tree.getEdgeTable();
	Table t2 = tree.getNodeTable();
        m_vis.removeGroup(TREE);
        VisualTree vg = m_vis.addTree(TREE, tree);
	//treelayout.setLayoutRoot(tree.getRoot());
        m_vis.setInteractive(EDGES, null, false);
        m_vis.setValue(NODES, null, VisualItem.SHAPE,
		       new Integer(Constants.SHAPE_ELLIPSE));
        
	if (!tree.isValidTree()) {
	    System.err.println("Invalid tree! Attmpting to continue anyway. ");
	}
    }
	
    private static void usage () {
	System.err.println("Usage: java OntologyEditor -c <nxt-metadata> -e <nxt-ontology-element> ");
	System.exit(0);
    }
    
    public static void main(String[] args) {
	String metafile=null;
	String ontel=null;
	if (args.length < 4) { usage(); }
	for (int i=0; i<args.length; i++) {
	    String flag=args[i];
	    if (flag.equals("-corpus") || flag.equals("-c")) {
		i++; if (i>=args.length) { usage(); }
		metafile=args[i];
	    } else if (flag.equals("-element") || flag.equals("-e")) {
		i++; if (i>=args.length) { usage(); }
		ontel=args[i];
	    } else {
		System.err.println("ERROR: Unknown option - " + flag);
		usage();
	    }
	}
        OntologyEditor o = new OntologyEditor(metafile,ontel);
	JFrame frameo = o.demo(metafile, ontel);
        frameo.setVisible(true);
    }
    
    public JFrame demo(String meta, String el) {
        OntologyEditor ontology = new OntologyEditor(meta, el);
        JFrame frameo = new JFrame("N X T - o n t o l o g y");
        frameo.getContentPane().add(ontology);
        frameo.pack();
	frameo.setVisible(true);
        frameo.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	frameo.addWindowListener(new ontologyWindowListener());
        return frameo;
    }

    public JFrame demo(NiteMetaData meta, String el) {
        OntologyEditor ontology = new OntologyEditor(meta, el);
        JFrame frameo = new JFrame("N X T - o n t o l o g y");
        frameo.getContentPane().add(ontology);
        frameo.pack();
	frameo.setVisible(true);
        frameo.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	frameo.addWindowListener(new ontologyWindowListener());
        return frameo;
    }

    public void setModelChanged(boolean ch) {
	modelchanged=ch;
	System.out.println("Window closing changed: " + ch);
	if (modelchanged) {
	    try { nom.serializeCorpusChanged(); } catch (NOMException ex) { 
		System.err.println("Failed to save changes to NOM!");
	    }
	    //repaint
	    makeTree();
	    modelchanged=false;
	}
    }

    public boolean isModelChanged() {
	return modelchanged;
    }
    
    class MyMouseControl extends ControlAdapter {
	public void  itemEntered(VisualItem item, java.awt.event.MouseEvent e) {
	    String name = (String)item.getSourceTuple().get("name");
	    NOMElement el = (NOMElement)item.getSourceTuple().get("element");
	}

	public void  itemClicked(VisualItem item, java.awt.event.MouseEvent e) {
	    String name = (String)item.getSourceTuple().get("name");
	    NOMElement el = (NOMElement)item.getSourceTuple().get("element");
	    if (el!=null) {
		if (ed==null) {
		    ed = new ElementDisplay(el);
		    frame = new JFrame("Edit NXT element");
		    frame.setLocationRelativeTo(e.getComponent());
		    frame.getContentPane().add(ed.getPanel());
		    frame.addWindowListener(new elementWindowListener(ed));
		    frame.pack();
		    frame.setVisible(true);
		} else {
		    ed.changeElement(el);
		    frame.setVisible(true);
		}
	    }
	}
    }

    /** element edit windows get this listener */
    class elementWindowListener extends WindowAdapter {
	ElementDisplay ed;
	public elementWindowListener(ElementDisplay ed) {
	    this.ed=ed;
	}

	public void windowClosing(WindowEvent e) {
	    ed.checkExit();
	    setModelChanged(ed.isChanged());
	}
    }

    /** main window gets this listener */
    class ontologyWindowListener extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
	    if (isModelChanged()) {
		try {
		    nom.serializeCorpusChanged();
		} catch (Exception ex) { }
	    }
	    e.getWindow().dispose();
	}
    }

    /** Implementation of NOMView interface - just redraw for every edit!! */
    public void handleChange(NOMEdit edit) {
	System.out.println("Change to NOM");
	setModelChanged(true);
    }
    
} // end of class OntologyEditor


