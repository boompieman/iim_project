/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.awt.Dimension;

import javax.swing.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.NOMCorpus;
import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.meta.impl.NiteMetaException;

/**
 * A utility class that pops up a corpus save panel allowing one to
 * set lots of options.  
 *
 * @author Jonathan Kilgour 15/4/2003
 */
public class CorpusSaveOptions extends JDialog {
    static final String META = "meta";
    static final String CORPUSID = "corpusid";
    static final String CORPUSDESC = "corpusdesc";
    static final String CODING = "coding";
    static final String ONTOLOGY = "ontology";
    static final String OBJECTSET = "objectset";
    static final String EXPORTDIR = "exportdir";
    static final String XP = "xpointer";
    static final String LT = "ltxml1";

    private JTextField corpusidfield;
    private JTextField corpusdescfield;
    private JTextField meta_location;
    private JTextField coding_location;
    private JTextField ontology_location;
    private JTextField objectset_location;
    private JTextField export_location;
    private JCheckBox range_check;
    private JCheckBox inherit_check;
    private JCheckBox export_check;
    private String corpusname;
    private String exportdir=".";
    private boolean exportinfo=false;
    private ButtonGroup group=new ButtonGroup();

    private String message = "";
    private JOptionPane pane;
    private NOMCorpus nom;
    private NMetaData controlData;
    private JFrame parent;

    public CorpusSaveOptions(NOMCorpus corpus, JFrame parent) {
        this.nom=corpus;
	controlData=nom.getMetaData();
	corpusname=controlData.getFilename();
	this.parent=parent;
	popupDialog();
	//	super(parent, true); modal
    }

    public CorpusSaveOptions(NOMCorpus corpus) {
        this.nom=corpus;
	controlData=nom.getMetaData();
	corpusname=controlData.getFilename();
	popupDialog();
	setModal(true);
    }

    private void popupDialog() {
	JTabbedPane tp = setupTabbedPane();
	setSize(new Dimension(550, 250));
	getContentPane().add(tp);
	int ret= JOptionPane.showConfirmDialog(parent,tp,"Save corpus...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	if (ret==JOptionPane.OK_OPTION) {
	    try { // make sure we have all the settings!
		String loc = meta_location.getText();
		controlData.setFilename(loc);	    
		corpusname=loc;
		loc = coding_location.getText();
		controlData.setCodingPath(makeAbsolute(loc));	    
		loc = ontology_location.getText();
		if (loc!=null) controlData.setOntologyPath(makeAbsolute(loc));	    
		loc = objectset_location.getText();
		if (loc!=null) controlData.setObjectSetPath(makeAbsolute(loc));	    
		controlData.writeMetaData();
		nom.serializeCorpus();	    
	    } catch (NOMException e) {
		e.printStackTrace();
	    } catch (NiteMetaException nme) {
		nme.printStackTrace();
	    }
	}
    }


    class linkStyleListener implements ActionListener {
	public void actionPerformed(ActionEvent ex) {
	    String item = group.getSelection().getActionCommand();
	    //   System.out.println("Selected: " + item);
	    if (item.equals(XP)) {
		controlData.setLinkType(NMetaData.XPOINTER_LINKS);
	    } else if (item.equals(LT)) {
		controlData.setLinkType(NMetaData.LTXML1_LINKS);
	    }
	}
    }

    class metaListener implements ActionListener {
	public void actionPerformed(ActionEvent ex) {
	    String command = ex.getActionCommand();
	    //	    System.out.println("Set some meta stuff: " + command); 
	    if (command.equals(CORPUSID)) {
		String loc = corpusidfield.getText();
		controlData.setCorpusID(loc);
	    } else if (command.equals(CORPUSDESC)) {
		String loc = corpusdescfield.getText();
		controlData.setCorpusDescription(loc);
	    }
	}
    }

    class rangeListener implements ActionListener {
	public void actionPerformed(ActionEvent ex) {
	    boolean rang = range_check.isSelected();
	    //  System.out.println("Selected: " + rang);
	    nom.setSerializeMaximalRanges(rang);
	}
    }

    class inheritListener implements ActionListener {
	public void actionPerformed(ActionEvent ex) {
	    boolean inherit = inherit_check.isSelected();
	    //  System.out.println("Selected: " + rang);
	    nom.setSerializeInheritedTimes(inherit);
	}
    }

    class exportCustomListener implements ActionListener {
	public void actionPerformed(ActionEvent ex) {
	    exportinfo = export_check.isSelected();
	    //  System.out.println("Selected: " + rang);
	    //  nom.setSerializeInheritedTimes(inherit);
	}
    }

    class locationListener implements ActionListener {
	public void actionPerformed(ActionEvent ex) {
	    String command=ex.getActionCommand();
	    if (command.equals(CODING)) {
		String loc = coding_location.getText();
		controlData.setCodingPath(makeAbsolute(loc));	    
	    } else if (command.equals(ONTOLOGY)) {
		String loc = ontology_location.getText();
		String l2 = makeAbsolute(loc);
		//System.out.println("Made file: " + loc + "absolute: " + l2);
		controlData.setOntologyPath(l2);
	    } else if (command.equals(OBJECTSET)) {
		String loc = objectset_location.getText();
		controlData.setObjectSetPath(makeAbsolute(loc));	    
	    } else if (command.equals(META)) {
		String loc = meta_location.getText();
		try {
		    controlData.setFilename(loc);	    
		    corpusname=loc;
		} catch (NiteMetaException nme) {
		    nme.printStackTrace();
		}
	    } else if (command.equals(EXPORTDIR)) {
		exportdir = export_location.getText();
	    } 
	}
    }

    private String makeAbsolute(String orig) {
	return (new File(orig)).getAbsolutePath();
    }

    class browserListener implements ActionListener {
	public void actionPerformed(ActionEvent ex) {
	    JFileChooser choose=new JFileChooser();
	    choose.setApproveButtonText("Select");
	    choose.setApproveButtonToolTipText("Use the selected directory");
	    choose.setCurrentDirectory(new File("."));
	    if (!ex.getActionCommand().equals(META)) {
		choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    }
	    int option=choose.showOpenDialog(parent);
	    if (option==JFileChooser.APPROVE_OPTION) {
		String command=ex.getActionCommand();
		String thdirname=(new File("")).getAbsolutePath();
		String dirname="";
		if (choose.getSelectedFile()!=null) {
		    dirname=choose.getSelectedFile().getAbsolutePath();
		    if (dirname.indexOf(thdirname) >= 0) {
			dirname=dirname.substring(dirname.indexOf(thdirname) + thdirname.length() + 1);
		    } 
		} 
		if (command.equals(CODING)) {
		    coding_location.setText(dirname);
		    controlData.setCodingPath(makeAbsolute(dirname));	    
		} else if (command.equals(ONTOLOGY)) {
		    ontology_location.setText(dirname);
		    controlData.setOntologyPath(makeAbsolute(dirname)); 
		} else if (command.equals(OBJECTSET)) {
		    objectset_location.setText(dirname);
		    controlData.setObjectSetPath(makeAbsolute(dirname));
		} else if (command.equals(META)) {
		    meta_location.setText(dirname);
		    corpusname=dirname;
		} else if (command.equals(EXPORTDIR)) {
		    export_location.setText(dirname);
		    exportdir=dirname;
		} 
	    }
	}
    }

    private JTabbedPane setupTabbedPane() {
	JTabbedPane tp = new JTabbedPane();
	ActionListener ll = new locationListener();
	ActionListener bl = new browserListener();
	ActionListener ml = new metaListener();


	/*-----------------*/
	/* CORPUS LOCATION */
	/*-----------------*/
	//	ActionListener ll = new locationListener();
	//	ActionListener bl = new browserListener();

	JPanel mp = new JPanel();
	GridBagLayout gbl  = new GridBagLayout();
	mp.setLayout(gbl);
	JPanel jp2 = new JPanel();
	jp2.setLayout(new GridLayout(3,3));

	jp2.add(new JLabel("Codings: "));
	String codings=null;
	String cp = controlData.getCodingPath();
	if (cp!=null && cp.length()!=0) {
	    try { codings = new File(cp).getCanonicalPath(); }
	    catch (Exception ex) {}
	}
	coding_location=new JTextField(codings, 20);
	coding_location.setActionCommand(CODING);
	coding_location.addActionListener(ll);
	jp2.add(coding_location);
	JButton browse_coding=new JButton("Browse...");
	jp2.add(browse_coding);
	browse_coding.setActionCommand(CODING);
	browse_coding.addActionListener(bl);
	jp2.setVisible(true);

	jp2.add(new JLabel("Ontologies: "));
	String ontologies=null;
	String op = controlData.getOntologyPath();
	if (op!=null && op.length()!=0) {
	    try { ontologies = new File(op).getCanonicalPath(); }
	    catch (Exception ex) {}
	}
	ontology_location=new JTextField(ontologies, 20);
	ontology_location.setActionCommand(ONTOLOGY);
	ontology_location.addActionListener(ll);
	jp2.add(ontology_location);
	JButton browse_ontology=new JButton("Browse...");
	jp2.add(browse_ontology);
	browse_ontology.setActionCommand(ONTOLOGY);
	browse_ontology.addActionListener(bl);
	jp2.setVisible(true);

	jp2.add(new JLabel("Object Sets: "));
	String objectsets=null;
	String ob = controlData.getObjectSetPath();
	if (ob!=null && ob.length()!=0) {
	    try { objectsets = new File(ob).getCanonicalPath(); }
	    catch (Exception ex) {}
	}
	objectset_location=new JTextField(objectsets, 20);
	objectset_location.setActionCommand(OBJECTSET);
	objectset_location.addActionListener(ll);
	jp2.add(objectset_location);
	JButton browse_objectset=new JButton("Browse...");
	jp2.add(browse_objectset);
	browse_objectset.setActionCommand(OBJECTSET);
	browse_objectset.addActionListener(bl);

	GridBagConstraints c = new GridBagConstraints();
	c.anchor=GridBagConstraints.NORTH;
	c.gridwidth = GridBagConstraints.REMAINDER; //end row
	gbl.setConstraints(jp2, c);
	mp.add(jp2);
	JLabel jl = new JLabel("NOTE: if you change these values, you may also want to change the location of the metadata file");
	c.anchor=GridBagConstraints.SOUTH;
	gbl.setConstraints(jl, c);
	mp.add(jl);
	JLabel jl2 = new JLabel("by clicking the metadata tab above.");
	gbl.setConstraints(jl2, c);
	mp.add(jl2);

	tp.addTab("Save corpus to...", null, mp, "select the location of saved corpus files");

	/*-----------------*/
	/* METADATA        */
	/*-----------------*/

	JPanel jp3 = new JPanel();
	jp3.setLayout(new GridLayout(3,3));

	jp3.add(new JLabel("Corpus ID: "));
	corpusidfield = new JTextField(controlData.getCorpusID(), 20);
	corpusidfield.setActionCommand(CORPUSID);
	corpusidfield.addActionListener(ml);
	jp3.add(corpusidfield);
	JLabel bm=new JLabel("");
	jp3.add(bm);

	jp3.add(new JLabel("Corpus description: "));
	corpusdescfield = new JTextField(controlData.getCorpusDescription(), 20);
	corpusdescfield.setActionCommand(CORPUSDESC);
	corpusdescfield.addActionListener(ml);
	jp3.add(corpusdescfield);
	bm=new JLabel("");
	jp3.add(bm);

	jp3.add(new JLabel("Metadata location: "));
	meta_location=new JTextField(corpusname, 20);
	meta_location.setActionCommand(META);
	meta_location.addActionListener(ll);
	jp3.add(meta_location);
	JButton browse_meta=new JButton("Browse...");
	jp3.add(browse_meta);
	//	JButton bm=new JButton("");
	//	jp3.add(bm);
	browse_meta.setActionCommand(META);
	browse_meta.addActionListener(bl);

	tp.addTab("Metadata", null, jp3, "basic metadata settings");


	/*-----------------*/
	/* LINK TYPE       */
	/*-----------------*/
	int links = controlData.getLinkType();
	boolean ltselect=false;
	boolean xpselect=false;
	if (links==NMetaData.LTXML1_LINKS) { ltselect=true; }
	else if (links==NMetaData.XPOINTER_LINKS) { xpselect=true; }
	ActionListener al = new linkStyleListener();
	JRadioButton lt=new JRadioButton("LTXML version 1 links", ltselect);
	lt.setActionCommand(LT);
	lt.addActionListener(al);
	JRadioButton xp=new JRadioButton("XLink / XPointer links", xpselect);
	xp.setActionCommand(XP);
	xp.addActionListener(al);
	group.add(lt);
	group.add(xp);
	JPanel jf2 = new JPanel();
	tp.addTab("Link style", null, jf2, "select the link style");
	jf2.setLayout(new GridLayout(3,1));
	jf2.add(new JLabel("Select the style of links used to refer to external elements in the corpus"));
	jf2.add(lt);
	jf2.add(xp);
	//	jf2.setVisible(true);


	/*-----------------*/
	/* MAXIMAL RANGES  */
	/*-----------------*/
	boolean ranges = nom.serializeMaximalRanges();
	ActionListener rl = new rangeListener();
	range_check=new JCheckBox("Serialize maximal ranges", ranges);
	range_check.addActionListener(rl);

	JPanel jp = new JPanel();
	tp.addTab("Link ranges", null, jp, "turn ranges on & off");
	jp.setLayout(new GridLayout(3,1));
	jp.add(new JLabel("If selected, maximal ranges are used in nite:child elements;"));
	jp.add(new JLabel("If deselected, ranges are not used."));
	jp.add(range_check);


	/*-----------------*/
	/* INHERITED TIMES */
	/*-----------------*/
	boolean save_inherited = nom.serializeInheritedTimes();
	ActionListener il = new inheritListener();
	inherit_check=new JCheckBox("Serialize inherited times", save_inherited);
	inherit_check.addActionListener(il);

	JPanel jp4 = new JPanel();
	tp.addTab("Times", null, jp4, "save inherited times");
	jp4.setLayout(new GridLayout(2,1));
	jp4.add(new JLabel("Check the checkbox to save inherited times to disk."));
	jp4.add(inherit_check);

	return tp;
    }

}

