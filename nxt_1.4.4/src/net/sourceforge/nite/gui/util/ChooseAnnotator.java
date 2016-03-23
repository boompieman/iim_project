/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.BorderLayout;
import java.io.*;
import java.util.*;

import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.meta.NCoding;
import net.sourceforge.nite.meta.NResourceData;
import net.sourceforge.nite.meta.NResource;
import net.sourceforge.nite.meta.NRealResource;

/** This class is a utility to pop up a dialog that lets the user
 select an Annotator from the list or type a new name Intended use is: <br>
   ChooseAnnotator ca = new ChooseAnnotator(metadata); <br>
   annotatorname=ca.popupDialog();
 */
public class ChooseAnnotator extends JDialog {
    JList list;
    JTextField annfield;
    NMetaData meta;
    String selection=null;
    protected static final String NXT_ANNOTATOR_PROPERTY = "NXT_ANNOTATOR_CODINGS";
    List codings = null;

    public ChooseAnnotator(NMetaData metadata) {
	meta=metadata;
	try {
	    String codinglist = System.getProperty(NXT_ANNOTATOR_PROPERTY);
	    if (codinglist!=null) {
		codings=new ArrayList();
		String[]cds=codinglist.split(";");
		for (int i=0; i<cds.length; i++) {
		    NCoding cod = meta.getCodingByName(cds[i]);
		    if (cod!=null) { codings.add(cod); }
		}
	    }
	} catch (Exception ex) { }
    }

    /** pop up an interface where the user chooses an
        annotator. Return the name of the annotator. */
    private String popupDialog(JList list) {
	JScrollPane jsp = new JScrollPane(list);
	//getContentPane().add(jsp);


	JPanel p1 = new JPanel();
	p1.setBorder(new EtchedBorder());
	p1.setLayout(new BorderLayout());
	p1.add(jsp, BorderLayout.NORTH);
	JLabel label = new JLabel("New annotator: ");
	annfield = new JTextField(20);
	JPanel p2 = new JPanel();
	p2.add(label);
	p2.add(annfield);
	p1.add(p2, BorderLayout.SOUTH);
	
	//getContentPane().add(label);
	getContentPane().add(p1);
	int ret= JOptionPane.showConfirmDialog(null,p1,"Choose annotator...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	if (ret==JOptionPane.OK_OPTION) {
	    String typed = annfield.getText();
	    
	    if (typed!=null && typed.length()>0) { return typed; }
	    return (String)list.getSelectedValue();
	    
	} else {
	    return null;
	}
    }    

    /** pop up an interface where the user chooses an annotator from
        the entire list of annotators on all codings. Return the name
        of the annotator. */
    public String popupDialog() {
	JList list = null;
	if (codings!=null && codings.size()>0) { 
	    list = setupList(codings);
	} else {
	    list = setupList(meta.getCodings());
	}
	return popupDialog(list);
    }

    /** pop up an interface where the user chooses an annotator from
        the entire list of annotators for the given codings (the List
        should contain NCoding elements). Return the name of the
        annotator. */
    public String popupDialog(List codinglist) {
	JList list = setupList(codinglist);
	return popupDialog(list);
    }
    
    private JList setupList(List codings) {
	Set annames=new HashSet();
	for (Iterator cit=codings.iterator(); cit.hasNext(); ) {
	    NCoding cod = (NCoding) cit.next();
	    List ml = null;
	    if (meta.getResourceData()!=null) {
		ml = findAnnotatorResources(meta.getResourceData(), cod);
	    } else {
		ml = findCoderDirectories(cod.getPath());
	    }
	    if (ml!=null) {
		annames.addAll(ml);
	    }
	}
	Vector v = new Vector(annames);
	Collections.sort(v);
	return(new JList(v));
    }


    /** given a coding and a set of resources, find all the resources
     * with an annotator noted against them, and add those annotators
     * to the list. */ 
    private List findAnnotatorResources(NResourceData resources, NCoding coding) {
	List ress = resources.getResourcesForCoding(coding.getName());
	if (ress==null) { return null; }
	List retlist = new ArrayList();
	for (Iterator rit=ress.iterator(); rit.hasNext(); ) {
	    try {
		NRealResource resource = (NRealResource)rit.next();
		if (resource.getAnnotator()!=null) { retlist.add(resource.getAnnotator()); }
	    } catch (Exception ex) {
		// only valid to check for annotator if resource is real, not virtual...
	    }
	}
	return retlist;
    }

    /** given a path, find all the subdirectories with some xml files
     * in them and add those to the list (assuming the directory names
     * are in fact coder names). */ 
    private List findCoderDirectories(String path) {
	ArrayList rl = new ArrayList();
	File cd = new File(path);
	if (!cd.isDirectory()) {
	    return rl;
	}
	File[] files = cd.listFiles(new myfilter());
	if (files.length==0) { return null; }
	for (int i=0; i<files.length; i++) {
	    rl.add(files[i].getName());
	}
	return rl;
    }

    class myfilter implements FileFilter {
	public boolean accept (File f) {
	    if (!f.isDirectory()) { return false; }
	    File[] xlist = f.listFiles(new xmlfilter());
	    if (xlist.length>0) { return true; }
	    return false;
	}
    }

    class xmlfilter implements FilenameFilter {
	String xex=".xml";

	public boolean accept (File dir, String name) {
	    if ((name.length() - name.indexOf(xex)) == xex.length()) { return true; }
	    return false;
	}
    }
    
}
