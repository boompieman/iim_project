/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2004, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomread.impl;

import java.io.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Vector;
import java.util.LinkedHashSet;
import java.util.Hashtable;
import java.util.HashSet;

import org.xml.sax.Attributes;
import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.meta.NObservation;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteCorpus;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.util.Debug;


/**
 * Our first implementation of NOMReadCorpus is actually an extension
 * of NOMWriteCorpus because we're a bit lazy that way. We just
 * override the editing calls to disallow them. We allow serializing
 * of the corpus, only edits are disallowed.
 *
 * @author jonathan 
 */
public class NOMReadCorpus extends NOMWriteCorpus {
    NOMMaker nommaker = new Maker();
    // convenience globals for when we're doing reliability studies:
    private String coderAttribute = null;
    protected String attname = null;
    String coderValue = null;
    protected boolean reliabilityMode=false;
    // Hash of Lists indexed by colour (elements are Lists of annotators for the colour)
    private Hashtable multiloadedfiles=new Hashtable();

    /** Construct a NOM corpus ready to load data: this NOM will not
        be editable. The argument is the ready-loaded metadata. */
    public NOMReadCorpus (NiteMetaData meta) {
	super(meta);
	setMaker(nommaker);
    }

    /** Construct a NOM corpus ready to load data: this NOM will not
        be editable. The arguments are the ready-loaded metadata and a
        printstream where all the status information will go (default
        is stdout). */
    public NOMReadCorpus (NiteMetaData meta, PrintStream log) {
	super(meta,log);
	setMaker(nommaker);
    }
    
    /** Return true if the corpus can be edited safely - always return
     * false as this is a read-only corpus. */
    public boolean isEditSafe () {
	//System.out.println("Checking safety of edit where nbatch mode = " + isLoadingFromFile() + ". Safety: " + super.isEditSafe());
	if (isLoadingFromFile()) { return super.isEditSafe(); } else { return false; }
    }

    /** lock the corpus for edits - always return false as this is a
     * read-only corpus. */
    public boolean lock (NOMView view) {
	if (isLoadingFromFile()) { return super.lock(view); } else { return false; }
    }

    /** unlock the corpus - always return false as teh corpus cannot
     * be edited. */
    public boolean unlock(NOMView view) {
	if (isLoadingFromFile()) { return super.unlock(view); } else { return false; }
    }
    
    /** remove the pointer - since we do not allow edits, just do nothing */
    public void removePointerIndex(NOMPointer point) {

    }


    /** Load data for the purpose of comparing different coders'
     * data. The layer 'top' is where we start the per-coder
     * information and 'top_common' is the layer at which we expect
     * everything to be common between coders. 'coder_attribute_name'
     * is ised as the name of the attribute that gets the name of the
     * coder and 'path' is where the coder data is. We assume the data
     * will use standard NXT-filenames but be held in a directory per
     * coder (the name of the coder is assumed to be the name of the
     * directory) under 'path'. Note that if 'path' is null, the
     * annotator-specific directories are assumed to be under the
     * standard location of each coding. If 'observation' is null we
     * attempt to load all the data, otherwise we only attempt to load
     * the listed observations.  loadReliability is now compatible
     * with lazy loading, *but* you need to make sure you make all
     * calls to loadReliability before any code that might cause data
     * to be loaded via lazy loading.
     */
    public void loadReliability(NLayer top, NLayer top_common, String coder_attribute_name, String path, List observations) throws NOMException {
	loadReliability(top, top_common, coder_attribute_name, path, observations, null);
    }

    /** Load data for the purpose of comparing different coders'
     * data. This is the same as the other call except for the final
     * argument which is a list of Strings: names of layers that
     * should be loaded in 'gold-standard' mode */ 
    public void loadReliability(NLayer top, NLayer top_common, String coder_attribute_name, String path, List observations, List other_layers) throws NOMException {
	reliabilityMode=true;
	List mypath = metadata.findPathBetween(top, top_common);
	//if (top==null || top_common==null) {
	if (top==null) {
	    throw new NOMException("Layer passed to loadReliability is null.");
	}
	if (top==top_common) {
	    throw new NOMException("Top layer and common layer passed to loadReliability are the same");
	}
	if (mypath==null) {
	    throw new NOMException("There is no path between layer '" + top.getName() + "' and layer '" + top_common.getName() + " in the metadata!");
	}
	coderAttribute=coder_attribute_name;
	attname=coder_attribute_name;
	List coders = null;
	if (path!=null) { coders = findCoderDirectories(path); }
	boolean laz = isLazyLoading();
	try {
	    //setLazyLoading(false); THIS causes a complete load if we have already loaded data!
	    lazy_loading=false;
	    setBatchMode(true);
	    if (observations==null) { observations=metadata.getObservations(); }
	    LinkedHashSet loaded=new LinkedHashSet();
	    for (Iterator oit=observations.iterator(); oit.hasNext(); ) {
		NObservation ob=(NObservation)oit.next();
		for (Iterator pit = mypath.iterator(); pit.hasNext(); ) {
		    NCoding nco = (NCoding) pit.next();
		    //if (path==null) {
			path=nco.getPath();
			coders=findCoderDirectories(path);
		    //}
		    //Debug.print("Coding: " + nco.getName(), Debug.ERROR);
		    for (Iterator cit = coders.iterator(); cit.hasNext(); ) {
			String coder = (String) cit.next();
			coderValue = coder;
			if (nco.getType()==NCoding.INTERACTION_CODING || metadata.getAgents()==null) {
			    String shortfilename=ob.getShortName() + "." + nco.getName() + ".xml";
			    String toremovefilename=path + File.separator + shortfilename;
			    String filename = path + File.separator + coder + File.separator + shortfilename;
			    File myf = new File(filename);
			    Debug.print("try to load: " + filename, Debug.DEBUG);
			    if (myf.isFile()) {
				try {
				    // the trick is to alter the colour of multi-coder 
				    // layers so the IDs don't clash.
				    multiLoadDataFromFile(filename, ob.getShortName() + "." + 
							  nco.getName(), coder, ob.getShortName(), nco);
				    removeUnloaded(shortfilename);
				    loaded.add(ob);
				} catch (IOException ioe) { }
			    } 
			} else {
			    for (Iterator ait=metadata.getAgents().iterator(); ait.hasNext(); ) {
				NAgent na = (NAgent)ait.next();
				String shortfilename=ob.getShortName() + "." + na.getShortName() + "." + nco.getName() + ".xml";
				String toremovefilename=path + File.separator + shortfilename;
				String filename = path + File.separator + coder + File.separator + shortfilename;
				File myf = new File(filename);
				Debug.print("try to load: " + filename, Debug.DEBUG);
				if (myf.isFile()) {
				    try {
					multiLoadDataFromFile(filename, ob.getShortName() + "." + na.getShortName() + "." + nco.getName(), coder, ob.getShortName(), nco);
					removeUnloaded(shortfilename);
					loaded.add(ob);
				    } catch (IOException ioe) { }
				}
			    }
			}
		    }
		}
	    }
	    
	    /* now we need to load from here down. We really need to think
	     * about pointers here but I'm not sure I will bother right
	     * now. */
	    if (top_common!=null) {
		NCoding common = (NCoding)top_common.getContainer();
		while (common!=null) {
		    if (mypath.contains(common)) { continue; }
		    for (Iterator oit=loaded.iterator(); oit.hasNext();) {
			NObservation nob = (NObservation)oit.next();
			Debug.print("Load code reliability: " + common.getName(), Debug.DEBUG);
			loadCode(common, nob, null);
		    }
		    NLayer next = common.getBottomLayer().getChildLayer();
		    if (next==null) { break; }
		    common=(NCoding)next.getContainer();
		}
	    }

	    /* and finally we load the other layers */
	    if (other_layers!=null) {
		// First get a set of codings
		HashSet codings = new HashSet();
		for (Iterator lit=other_layers.iterator(); lit.hasNext(); ) {
		    String nl = (String)lit.next();
		    NLayer l = metadata.findLayerWithName(nl);
		    if (l!=null) { 
			if (l.getContainer() instanceof NCoding) {
			    codings.add((NCoding)l.getContainer());
			}
		    } else { // be kind and allow coding names too
			NCoding nc = metadata.getCodingByName(nl);
			if (nc!=null) { 
			    codings.add(nc);
			} else {
			    Debug.print("Layer / coding called '"+nl+"' not found in metadata; data not loaded",Debug.ERROR);
			}
		    }
		}
		for (Iterator cit=codings.iterator(); cit.hasNext(); ) {
		    NCoding nc = (NCoding)cit.next();
		    for (Iterator oit=loaded.iterator(); oit.hasNext();) {
			NObservation nob = (NObservation)oit.next();
			Debug.print("Load code reliability: " + nc.getName(), Debug.DEBUG);
			loadCode(nc, nob, null);
		    }
		}
		    
	    }
	    setBatchMode(false);	
	    cleanupCorpus(false);
	    //setLazyLoading(laz);
	    lazy_loading=laz;
	    coderAttribute=null;
	} catch (NOMException nex) {
	    //setLazyLoading(laz);
	    lazy_loading=laz;
	    throw nex;
	}
    }    

    /** This is just a wrapper for loadDataFromFile when we know we
     * are potentially loading multiple copies. At the moment I change
     * the colour rather than the ID of each multiply-loaded element
     * and keep a note so we can resolve links later on... */
    private void multiLoadDataFromFile(String filename, String colour, String coder, String observation, NFile nfile) throws NOMException, IOException {
	//Debug.print("Adding "+colour+", "+coder+" to multiloads.", Debug.DEBUG);
	if (!multiloadedfiles.containsKey(colour)) {
	    multiloadedfiles.put(colour, new Vector());
	}
	((List)multiloadedfiles.get(colour)).add(coder);
	loadDataFromFileCatchingExceptions(filename, colour+"."+coder, null, observation, nfile, coder, NOMContext.METADATA_MULTI);
	//loadDataFromFile(filename, colour+"."+coder, null);
    }

    /** We subclass the resolution of individual elements so
     * we can deal with multi-loaded cases. */
    protected NOMWriteElement findElementWithID(String id, String colour) throws NOMException {
	String key = colour + "#" + id;
	NOMWriteElement nre=null;
	if (metadata.getCorpusType()==NMetaData.SIMPLE_CORPUS) {
	    key = id;
	}
	//Debug.print("Looking for el '"+id+"' in colour: "+colour+": "+multiloadedfiles.get(colour), Debug.DEBUG);
	if (multiloadedfiles.containsKey(colour)) {
	    //Debug.print("Multiloaded!", Debug.DEBUG);
	    for (Iterator fit=((List)multiloadedfiles.get(colour)).iterator(); fit.hasNext(); ) {
		String coder = (String)fit.next();
		nre = (NOMWriteElement)element_hash.get(colour+"."+coder+"#"+id);
		if (nre!=null) { break; }
	    }
	} else {
	    nre = (NOMWriteElement)element_hash.get(key);
	}
	if (nre == null && !lazy_loading) {
	    if (isValidating()) {
		throw new NOMException("It's illegal to refer to a non-existant element. Element: " + colour + " ("  + id + ") doesn't exist. (turn off validation to get round this)");
	    } else {
		Debug.print("WARNING: Failed to find element with id " + id + " and colour " + colour + "!!", Debug.WARNING);
	    }
	}
	return nre;
    }
    

    /** given a path, find all the subdirectories with some xml files
     * in them and add those to the list (assuming the directory names
     * are in fact coder names). */ 
    private List findCoderDirectories(String path) throws NOMException {
	File cd = new File(path);
	if (!cd.isDirectory()) {
	    throw new NOMException("Path '" + path + "' is not a directory!");
	}
	File[] files = cd.listFiles(new myfilter());
	if (files.length==0) { return null; }
	ArrayList rl = new ArrayList();
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

    class Maker implements NOMMaker {
	public NOMAnnotation make_annotation(NOMCorpus corpus, String name, 
			     Attributes attributes, String colour, NResource resource,
	      boolean stream) throws NOMException {
	    NOMAnnotation na = new NOMReadAnnotation(corpus,name,attributes,colour,resource,stream);
	    if (coderAttribute!=null) {
		na.addAttribute(new NOMReadAttribute(coderAttribute, coderValue));
	    }
	    return na;
	}
	public NOMAnnotation make_annotation(NOMCorpus corpus, String name, 
	      String colour, NResource resource, boolean stream, String id) throws NOMException {
	    NOMAnnotation na = new NOMReadAnnotation(corpus,name,colour,resource,stream,id);
	    if (coderAttribute!=null) {
		na.addAttribute(new NOMReadAttribute(coderAttribute, coderValue));
	    }
	    return na;
	}
	public NOMAnnotation make_annotation(NOMCorpus corpus, String name,
	      String observation, String agent, NResource resource) throws NOMException {
	    NOMAnnotation na = new NOMReadAnnotation(corpus,name,observation,agent);
	    if (coderAttribute!=null) {
		na.addAttribute(new NOMReadAttribute(coderAttribute, coderValue));
	    }
	    return na;
	}
	/** This creates a comment element */
	public NOMAnnotation make_annotation(NOMCorpus corpus, String comment, 
					     String colour, NResource resource) throws NOMException {
	    return new NOMReadAnnotation(corpus,comment,colour,resource);
	}

	public NOMObject make_object(NOMCorpus corpus, String name, 
	      Attributes attributes, String colour, NResource resource, 
	      boolean stream) throws NOMException {
	    return new NOMReadObject(corpus,name,attributes,colour,resource,stream);
	}
	public NOMObject make_object(NOMCorpus corpus, String name, 
	      String colour, NResource resource, boolean stream, String id) throws NOMException {
	    return new NOMReadObject(corpus,name,colour,resource,stream,id);
	}

	public NOMResourceElement make_resource_element(NOMCorpus corpus, String name, 
	      Attributes attributes, String colour, NResource resource, 
	      boolean stream) throws NOMException {
	    return new NOMReadResourceElement(corpus,name,attributes,colour,resource,stream);
	}
	public NOMResourceElement make_resource_element(NOMCorpus corpus, String name, 
	      String colour, NResource resource, boolean stream, String id) throws NOMException {
	    return new NOMReadResourceElement(corpus,name,colour,resource,stream,id);
	}
	/** This creates a comment element */
	public NOMResourceElement make_resource_element(NOMCorpus corpus, String comment, 
					     String colour, NResource resource) throws NOMException {
	    return new NOMReadResourceElement(corpus,comment,colour,resource);
	}

	public NOMTypeElement make_type_element(NOMCorpus corpus, String name, 
	      Attributes attributes, String colour, NResource resource, 
	      boolean stream) throws NOMException {
	    return new NOMReadTypeElement(corpus,name,attributes,colour,resource,stream);
	}

	public NOMTypeElement make_type_element(NOMCorpus corpus, String name, 
	      String colour, NResource resource, boolean stream, String id) throws NOMException {
	    return new NOMReadTypeElement(corpus,name,colour,resource,stream,id);
	}

	public NOMAttribute make_attribute(int type, String name, 
					   String string_value, Double double_value) {
	    return new NOMReadAttribute(type,name,string_value,double_value);
	}
	/** create a numeric attribute */
	public NOMAttribute make_attribute(String name, Double double_value) {
	    return new NOMReadAttribute(name,double_value);
	}
	/** create a string attribute */
	public NOMAttribute make_attribute(String name, String string_value) {
	    return new NOMReadAttribute(name,string_value);
	}

	public NOMPointer make_pointer (NOMCorpus corpus, String role, 
					NOMElement source, String targetstr) {
	    return new NOMReadPointer(corpus,role,source,targetstr);
	}
	public NOMPointer make_pointer (NOMCorpus corpus, String role, 
					NOMElement source, NOMElement target) {
	    return new NOMReadPointer(corpus,role,source,target);
	}

    }

} 
