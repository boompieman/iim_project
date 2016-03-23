/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import net.sourceforge.nite.util.Debug;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.NiteMetaConstants;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
// import org.apache.xerces.parsers.DOMParser;
import org.apache.xpath.XPathAPI;
import org.xml.sax.InputSource;
import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeIterator;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import org.apache.xml.serialize.*;
//import com.sun.resolver.tools.CatalogResolver;

/**
 * Handles the metadata associated with a corpus: during validation
 * this structure stores important information about the parsed
 * metadata file.
 *
 */
public class NiteMetaData implements net.sourceforge.nite.meta.NMetaData {
    private String metadata_filename=null;
    private File metadata_file=null;
    private URL metadata_url=null;
    private boolean url=false;
    private String metadata_path=null;
    private String corpus_id=null;
    private String description=null;
    private int corpus_type = STANDOFF_CORPUS;
    private int link_type = LTXML1_LINKS;
    private String resource_file=null;
    private ArrayList observations = null;
    private ArrayList dataviews = null;
    private ArrayList styles = null;
    private ArrayList interfaces = null;
    private ArrayList annotationspecs = null;
    private ArrayList signals = null;
    private ArrayList agents = null;
    private ArrayList codings = null;
    private ArrayList programs = null;
    private ArrayList all_elements = null;
    private ArrayList signalrefs = null;
    private NResourceData resourcefile = null;
    private ArrayList ontologies = null;
    private ArrayList objectsets = null;
    private ArrayList corpusresources = null;
    private ArrayList observation_variables = null;
    private Hashtable elements = new Hashtable();
    private String relsignalpath=null;
    private String signalpath=null;
    private String signalpathmodifier=null;
    private String relontologypath=null;
    private String ontologypath=null;
    private String relobjectsetpath=null;
    private String objectsetpath=null;
    private String relcorpusresourcepath=null;
    private String corpusresourcepath=null;
    private String programpath=null;
    private String relprogrampath=null;
    private String codingspath=null;
    private String relcodingspath=null;
    private String intercodingspath=null;
    private String relintercodingspath=null;
    private String agentcodingspath=null;
    private String relagentcodingspath=null;
    private String relstylepath=null;
    private String stylepath=null;
    private String stylesheetpath=null;
    private String annotationspecpath=null;
    private String reservedIDAttribute=null;
    private String reservedCommentAttribute=null;
    private String reservedStartTimeAttribute=null;
    private String reservedEndTimeAttribute=null;
    private String reservedAgentAttribute=null;
    private String reservedObservationAttribute=null;
    private String reservedResourceAttribute=null;
    private String reservedGVMAttribute=null;
    private String reservedKeyAttribute=null;
    private String reservedChildElement=null;
    private String reservedPointerElement=null;
    private String reservedExternalPointerElement=null;
    private String reservedStreamElement=null;
    private String reservedTextElement=null;
    private String cvsProtocol=null;
    private String cvsServer=null;
    private String cvsModule=null;
    private String cvsRepository=null;
    private String cvsConnection=null;
    private Document doc = null;
    Hashtable layerhash = new Hashtable();
    private Hashtable pointers_from = new Hashtable();
    private Hashtable pointers_to = new Hashtable();
    boolean validation=true;

    /** Constructor */
    public NiteMetaData(String filename) throws NiteMetaException {
	metadata_filename = filename;
	url=false;
	Debug.print("Metadata file: "+filename);
	try {
	    metadata_url=new URL(metadata_filename);
	    metadata_path=metadata_filename.substring(0,metadata_filename.lastIndexOf(File.separator));
	    url=true;
	} catch (MalformedURLException muex) { 
	    //System.out.println("Metadata is ill formed: " + metadata_filename);
	}
	if (!url) {
	    metadata_file = new File(metadata_filename);
	    metadata_path=metadata_file.getParent();
	    // the only case we don't cover above is if we're relative and have no parent - here
	    if (metadata_filename!=null && metadata_path==null) 
		metadata_path=".";
	    //System.err.println("Metadata path is " + metadata_path + " from " + metadata_filename);
	}
	initializeControlFile();
	finalizeLayerHierarchy(); 
	// printMetaStructure();
    }


    private void printMetaStructure () {
	/* This is just for debugging purposes ... */
	ArrayList al = (ArrayList)getCodings();
	Iterator it = al.iterator();
	NCoding nc;
	while (it.hasNext())  {
	    nc = (NCoding) it.next();
	    System.out.println("CODING: " + nc.getName());
	    NLayer toplayer = (NLayer)nc.getTopLayer();
	    while (toplayer!=null) {
		System.out.println("  LAYER: " + toplayer.getName());
		ArrayList eles = (ArrayList)toplayer.getContentElements();
		Iterator elit = eles.iterator();
		while (elit.hasNext())  {
		    NiteElement nel = (NiteElement) elit.next();
		    System.out.println("    ELEMENT: " + nel.getName());
		    ArrayList atts = (ArrayList)nel.getAttributes();
		    Iterator elit2 = atts.iterator();
		    while (elit2.hasNext())  {
			NiteAttribute att = (NiteAttribute) elit2.next();
			System.out.println("      ATTRIBUTE: " + att.getName());
			ArrayList vals = (ArrayList)att.getEnumeratedValues();
			Iterator elit3 = vals.iterator();
			while (elit3.hasNext())  {
			    String val = (String) elit3.next();
			    System.out.println("        VALUE: " + val);
			}
		    }

		    ArrayList points = (ArrayList)nel.getPointers();
		    elit2 = points.iterator();
		    while (elit2.hasNext())  {
			NitePointer att = (NitePointer) elit2.next();
			System.out.println("      POINTER: " + att.getRole());
		    }
		}
		toplayer=toplayer.getChildLayer();
	    }
	}
    }

    /** save the metadata to the current filename (by default the file
        it was created from, or set using setFilename). */
    public void writeMetaData() {
	try {
	    FileOutputStream os = new FileOutputStream(metadata_filename);
	    XMLSerializer ser = new XMLSerializer();
	    OutputFormat of = new OutputFormat();
	    of.setIndenting(true);
	    ser.setOutputFormat(of);
	    ser.setOutputByteStream((OutputStream) os);
	    ser.serialize(doc);
	    if (resourcefile!=null) {
		resourcefile.writeResourceFile();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /** save the metadata to a file with the given filename. */
    public void writeMetaData(String filename) {
	try {
	    setFilename(filename);
	    writeMetaData();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * initialize the document so that queries can be performed on it
     */
    private void initializeControlFile() throws NiteMetaException {
	// Set up a DOM tree to query.
	InputSource in = null;

	//	Debug.print("Trying to read in file " + metadata_filename);
	
	try{
	    if (url && metadata_url!=null) {
		in = new InputSource(metadata_url.openStream());
	    } else {
		in = new InputSource(new FileInputStream(metadata_filename));
	    }
	} catch (FileNotFoundException e) {
	    System.err.println("ERROR: File "+ metadata_filename + " not found");
	    //	    e.printStackTrace();
	    // System.exit(0); 
	    throw new NiteMetaException("File '" + metadata_filename + "' not found.");
	} catch (IOException e) {
	    System.err.println("ERROR: metadata URI: "+ metadata_filename + " not found");
	    //	    e.printStackTrace();
	    // System.exit(0); 
	    throw new NiteMetaException("Metadata URI '" + metadata_filename + "' not found.");
	}
	DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
	dfactory.setNamespaceAware(true);
	//	dfactory.setValidating(true);
	try{
	    if (in != null){
		DocumentBuilder db=dfactory.newDocumentBuilder();
		//CatalogResolver resolver=new CatalogResolver();
		//db.setEntityResolver(resolver);
		//		db.setEntityResolver(new NiteEntityResolver());
		doc = db.parse(in);
	    }
	}catch (ParserConfigurationException pce){
	    throw new NiteMetaException("NITE MetaData document could not be built from file: " + metadata_filename);
	} catch ( org.xml.sax.SAXException sax){
	    sax.printStackTrace();
	    throw new NiteMetaException("NITE MetaData SAX exception using file: " +
					metadata_filename);
	} catch (IOException e){
	    e.printStackTrace();

	    throw new NiteMetaException("NITE MetaData IO exception using file: " + 
					metadata_filename);

	}


	if (doc == null) {
	    throw new NiteMetaException("NITE MetaData file could not be parsed. File name: " + metadata_filename);
	}

	// get the corpus type: this is always done at the start as
	// some of the other functions depend on it
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.typexpath);
	    if (n==null) {
		Debug.print("Warning: corpus type not explicit. Defaulting to standoff", Debug.DEBUG);
	    } else {
		String type = n.getNodeValue();
		if (type.equalsIgnoreCase(NiteMetaConstants.corpusTypeSimple)) {
		    corpus_type=SIMPLE_CORPUS;
		} 
	    }
	} catch (TransformerException e) {
	    throw new NiteMetaException("NITE MetaData Error: could not get node from path " + NiteMetaConstants.typexpath);
	}

	// get the link type: can't load data without knowing the link syntax
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.linksyntaxxpath);
	    if (n==null) {
		String val = NiteMetaConstants.corpusLinksLTXML1;
		Debug.print("Warning: link syntax not explicit. Defaulting to LTXML1 links", Debug.DEBUG);
		Element el  = (Element)XPathAPI.selectSingleNode(doc, NiteMetaConstants.corpus_path);
		if (el!=null) {  el.setAttribute(NiteMetaConstants.links, val); }
	    } else {
		String type = n.getNodeValue();
		if (type.equalsIgnoreCase(NiteMetaConstants.corpusLinksXPointer)) {
		    link_type=XPOINTER_LINKS;
		}
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}

	// get the resource file if present. This should contain the
	// location of all annotation files.
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.resourcesxpath);
	    if (n!=null) { 
		resource_file = n.getNodeValue(); 
		// special case - make a corpus-resource type file with a fixed structure
		resourcefile = new NiteResourceData(this, resource_file); 
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}	
	
	// get the corpus ID
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.idxpath);
	    if (n!=null) { corpus_id = n.getNodeValue(); }
	} catch (TransformerException e) {
	    throw new NiteMetaException("NITE MetaData Error: could not get node from path " + NiteMetaConstants.idxpath);
	}

	// get the corpus description
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.descriptionxpath);
	    if (n!=null) { this.description = n.getNodeValue(); }
	} catch (TransformerException e) {
	    throw new NiteMetaException("NITE MetaData Error: could not get node from path " + NiteMetaConstants.descriptionxpath);
	}

	// get the reserved attribute & element names
	Node n=null;
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedChild);
	    if (n != null) {
		reservedChildElement = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedPointer);
	    if (n != null) {
		reservedPointerElement = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedExternalPointer);
	    if (n != null) {
		reservedExternalPointerElement = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}

	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedStream);
	    if (n != null) {
		reservedStreamElement = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}

	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedText);
	    if (n != null) {
		reservedTextElement = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedID);
	    if (n != null) {
		reservedIDAttribute = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedComment);
	    if (n != null) {
		reservedCommentAttribute = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedStartTime);
	    if (n != null) {
		reservedStartTimeAttribute = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedEndTime);
	    if (n != null) {
		reservedEndTimeAttribute = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedAgent);
	    if (n!=null) {
		reservedAgentAttribute = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedObservation);
	    if (n!=null) {
		reservedObservationAttribute = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedResource);
	    if (n!=null) {
		reservedResourceAttribute = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedGVM);
	    if (n!=null) {
		reservedGVMAttribute = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedKey);
	    if (n!=null) {
		reservedKeyAttribute = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.cvsprotocolpath);
	    if (n!=null) {
		cvsProtocol = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.cvsserverpath);
	    if (n!=null) {
		cvsServer = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.cvsmodulepath);
	    if (n!=null) {
		cvsModule = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.cvsrepositorypath);
	    if (n!=null) {
		cvsRepository = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.cvsconnectionpath);
	    if (n!=null) {
		cvsConnection = n.getNodeValue();
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
    }


    /**
     * finalize the layer hierarchy so that the points-to links
     * become real children.
     */
    private void finalizeLayerHierarchy() {
	//	Debug.print("Finalizing hierarchy");
	ArrayList cdings = (ArrayList)getCodings();
	ArrayList onts = (ArrayList)getOntologies();
	ArrayList obss = (ArrayList)getObjectSets();
	ArrayList corps = (ArrayList)getCorpusResources();
	//First set children and parents
	Enumeration layerkeys = layerhash.keys();
	while (layerkeys.hasMoreElements()) {
	    String lkey = (String) layerkeys.nextElement();
	    NiteLayer blayer = (NiteLayer)layerhash.get(lkey);
	    if (blayer==null) {
		System.err.println("Can't retrieve layer from hash!! " + lkey);
		return;
	    }
	    if (blayer.getChildLayerName() != null && !blayer.getChildLayerName().equals("")) {
		//		Debug.print("Layer " + lkey + " points to " + blayer.getChildLayerName());
		NiteLayer child = findLayerWithName(blayer.getChildLayerName());
		if (blayer.getRecursive()) {
		    blayer.addParentLayer(blayer);
		}
		if (child != null) {
		    blayer.setChildLayer(child);
		    child.addParentLayer(blayer);
		} else {
		    System.err.println("Failed to resolve child-layer for layer " + blayer.getName() + " which draws children from " + blayer.getChildLayerName() + ".");
		}
	    }
	}

	// Now set top and bottom layers for each coding
	layerkeys = layerhash.keys();
	while (layerkeys.hasMoreElements()) {
	    String lkey = (String) layerkeys.nextElement();
	    NiteLayer blayer = (NiteLayer)layerhash.get(lkey);
	    //	    NiteCoding nc = (NiteCoding) blayer.getCoding();
	    //	    NiteLayer player = (NiteLayer)blayer.getParentLayerInCoding((NCoding)nc);
	    Object nc = blayer.getContainer();
	    NiteLayer player = (NiteLayer)blayer.getParentLayerInCoding(nc);
	    if (player==null) {
		if (nc instanceof NiteCoding) {
		    ((NiteCoding)nc).setTopLayer(blayer);
		}
	    }
	    NiteLayer clayer = (NiteLayer)blayer.getChildLayer();
	    if (clayer != null) {
		if (clayer.getContainer()!=nc) {
		    if (nc instanceof NiteCoding) {
			//Debug.print("Layers " + clayer.getName() + " and " + blayer.getName() + " have different codings (" + ((NCoding)clayer.getContainer()).getName() + ", " + ((NiteCoding)nc).getName() + ").");
			((NiteCoding)nc).setBottomLayer(blayer);
		    }
		}
	    } else {
		//Debug.print("Layer " + blayer.getName() + " has no child layer. ");
		if (nc instanceof NiteCoding) {
		    ((NiteCoding)nc).setBottomLayer(blayer);
		}
	    }
	}
    }


    /** returns a "styled display" (either an annotation board or a
        stylesheet-produced interface) with a given name. */
    public NStyle findStyleWithName(String name) {
	NStyle restyle=null;
	if (styles==null) { return null; }
	Iterator sit=styles.iterator();
	while (sit.hasNext()) {
	    NiteStyle st = (NiteStyle)sit.next();
	    if (st.getName().equalsIgnoreCase(name)) {
		return (NStyle)st;
	    }
	}
	return restyle;
    }

    /** returns a signal with the given name. */
    public NSignal findSignalWithName(String name) {
	NSignal resignal=null;
	List sigs=getSignals();
	if (sigs==null) { return null; }	
	Iterator sit=sigs.iterator();
	while (sit.hasNext()) {
	    NiteSignal st = (NiteSignal)sit.next();
	    if (st.getName().equalsIgnoreCase(name)) {
		return (NSignal)st;
	    }
	}
	return resignal;
    }

    /**
     * return a NiteLayer with this name or null if there are none.
     */
    public NiteLayer findLayerWithName(String name) {
	NiteLayer reslayer=null;
	Enumeration layerkeys = layerhash.keys();
	while (layerkeys.hasMoreElements()) {
	    String lkey = (String) layerkeys.nextElement();
	    NiteLayer blayer = (NiteLayer)layerhash.get(lkey);
	    if (blayer.getName().equalsIgnoreCase(name)) {
		return blayer;
	    }
	}
	return reslayer;
    }

    /** returns the description of the corpus
     */
    public String getCorpusDescription() { 
	return description;
    }

    /** sets the description of the corpus  */
    public void setCorpusDescription(String description) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.descriptionxpath);
	    if (n!=null) { n.setNodeValue(description); }
	    else { System.err.println("Failed to find node " + NiteMetaConstants.descriptionxpath); }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	this.description=description;
    }

    /** returns the ID of the corpus
     */
    public String getCorpusID() { 
	return corpus_id;
    }

    /** Set the identifier of the corpus */
    public void setCorpusID(String id) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.idxpath);
	    if (n!=null) { n.setNodeValue(id); }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	corpus_id=id;
    }

    /** returns STANDOFF_CORPUS by default, or SIMPLE_CORPUS if that's
     *  specified on the top level metadata tag.
     */
    public int getCorpusType() { 
	return corpus_type;
    }

    /** returns LTXML1_LINKS or XPOINTER_LINKS depending on the link type
     *  specified on the top level metadata tag.
     */
    public int getLinkType() { 
	return link_type;
    }

    /** Set the type of links this metadata has (or will be serialized
        with). Must be either LTXML1_LINKS or XPOINTER_LINKS */
    public void setLinkType(int linktype) {
	String val = NiteMetaConstants.corpusLinksLTXML1;
	if (linktype == XPOINTER_LINKS) { val=NiteMetaConstants.corpusLinksXPointer;}
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.linksyntaxxpath);
	    if (n!=null) { 
		n.setNodeValue(val); 
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	link_type=linktype;
    }

    /** Set validation on (true) or off (false). Validation applies to
        the metadata file itself (though only as far as comparing
        declared observation variables with actual ones) and the data
        itself. */
    public void setValidation(boolean validate) {
	validation=validate;
    }

    /** Returns true if validation is on (default) or false if it's
        off. Validation applies to the metadata file itself (though
        only as far as comparing declared observation variables with
        actual ones) and the data itself. */
    public boolean isValidating() {
	return validation;
    }

    /** returns an ArrayList of "NiteAgent"s */
    public List getAgents() {
	if (agents==null) {
	    agents=new ArrayList();
	    NodeIterator nl=null;
	    try {
		nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.agentxpath);
	    } catch (TransformerException ex) {
		ex.printStackTrace();
	    }

	    Element e;
	    while ((e = (Element)nl.nextNode())!= null)  {
		agents.add(new NiteAgent(e.getAttribute(NiteMetaConstants.objectName), e.getAttribute(NiteMetaConstants.description)));
	    }
	}
	return (List)agents;
    }

    /** returns a List of "NCallableProgram"s - programs that can be
     * called on this corpus */
    public List getPrograms() {
	if (programs==null) {
	    programs=new ArrayList();
	    NodeIterator nl=null;
	    try {
		nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.programxpath);
	    } catch (TransformerException ex) {
		ex.printStackTrace();
	    }

	    Element e;
	    Element e2;
	    while ((e = (Element)nl.nextNode())!= null)  {
		String name = e.getAttribute(NiteMetaConstants.objectName);
		String description = e.getAttribute(NiteMetaConstants.description);
		NiteCallableProgram prog = new NiteCallableProgram(name, description);
		NodeIterator nl2=null;
		try {  
		    /* Get the required arguments */
		    nl2 = XPathAPI.selectNodeIterator(e, NiteMetaConstants.requiredargumentxpath );
		    while ((e2 = (Element)nl2.nextNode())!= null) {
			String argname = e2.getAttribute(NiteMetaConstants.objectName);
			String type = e2.getAttribute(NiteMetaConstants.objectType);
			String defaultVal=null;
			if (e2.hasAttribute(NiteMetaConstants.objectDefault)) {
			    defaultVal = e2.getAttribute(NiteMetaConstants.objectDefault);
			}
			prog.addRequiredArgument(argname, type, defaultVal);
		    }

		    /* Get the optional arguments */
		    nl2 = XPathAPI.selectNodeIterator(e, NiteMetaConstants.optionalargumentxpath );
		    while ((e2 = (Element)nl2.nextNode())!= null) {
			String argname = e2.getAttribute(NiteMetaConstants.objectName);
			String type = e2.getAttribute(NiteMetaConstants.objectType);
			String defaultVal = e2.getAttribute(NiteMetaConstants.objectDefault);
			prog.addOptionalArgument(argname, type, defaultVal);
		    }

		} catch (TransformerException ex) { ex.printStackTrace(); }
		programs.add(prog);
	    }
	}
	return (List) programs;
    }

    /** returns a List of "NDataViews"s */
    public List getDataViews() {
	List ll = getSignals();
	if (dataviews==null) {
	    dataviews=new ArrayList();
	    NodeIterator nl=null;
	    try {
		nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.viewxpath);
	    } catch (TransformerException ex) {
		ex.printStackTrace();
	    }

	    Element e;
	    Element e2;
	    while ((e = (Element)nl.nextNode())!= null)  {
		ArrayList windows=new ArrayList();
		NodeIterator nl2=null;
		try {  /* Get the styled windows */
		    nl2 = XPathAPI.selectNodeIterator(e, NiteMetaConstants.styledwindowxpath );
		    while ((e2 = (Element)nl2.nextNode())!= null) {
			boolean sound=true;
			String sstring = e2.getAttribute(NiteMetaConstants.sound);
			if (sstring.equalsIgnoreCase(NiteMetaConstants.off)) {
			    sound=false;
			}
			String myname=e2.getAttribute(NiteMetaConstants.objectNameRef);
			NiteWindow nw = new NiteWindow(myname, NWindow.STYLE, sound);
			//Debug.print("Adding a styled window with name " + myname);
			windows.add((Object)nw);
		    }

		    /* get the video displays */
		    nl2=null;
		    nl2 = XPathAPI.selectNodeIterator(e, NiteMetaConstants.videowindowxpath );
		    while ((e2 = (Element)nl2.nextNode())!= null) {
			boolean sound=true;
			String sstring = e2.getAttribute(NiteMetaConstants.sound);
			if (sstring.equalsIgnoreCase(NiteMetaConstants.off)) {
			    sound=false;
			}
			NiteWindow nw = new NiteWindow(e2.getAttribute(NiteMetaConstants.objectNameRef), NWindow.VIDEO, sound);
			windows.add((Object)nw);
		    }

		    /* get the audio displays */
		    nl2=null;
		    nl2 = XPathAPI.selectNodeIterator(e, NiteMetaConstants.audiowindowxpath );
		    while ((e2 = (Element)nl2.nextNode())!= null) {
			boolean sound=true;
			String sstring = e2.getAttribute(NiteMetaConstants.sound);
			if (sstring.equalsIgnoreCase(NiteMetaConstants.off)) {
			    sound=false;
			}
			NiteWindow nw = new NiteWindow(e2.getAttribute(NiteMetaConstants.objectNameRef), NWindow.AUDIO, sound);
			windows.add((Object)nw);
		    }
		    
		} catch (TransformerException ex) { ex.printStackTrace(); }

		int type=NDataView.DISPLAY;
		String typestring = e.getAttribute(NiteMetaConstants.objectType);
		if (typestring.equalsIgnoreCase(NiteMetaConstants.editor)) {
		    type=NDataView.EDITOR;
		}
		NiteDataView ndv=new NiteDataView(e.getAttribute(NiteMetaConstants.description), type, windows);
		dataviews.add((Object)ndv);
		// Debug.print("Added a data view with description " + ndv.getDescription());
		
	    }
	}
	return (List)dataviews;
    }


    /** returns an ArrayList of "NiteSignal"s */
    public List getSignals() {
	if (signals==null) {
	    // No signals are listed: search first for interaction
	    // signals, and then for agent signals.
	    signals=new ArrayList();
	    NodeIterator nl=null;
	    try {
		nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.interactionsignalxpath );
	    } catch (TransformerException ex) {
		ex.printStackTrace();
	    }
	    
	    Element e;
	    while ((e = (Element)nl.nextNode())!= null) {         
		int media_type=NSignal.AUDIO_SIGNAL;
		String media = e.getAttribute(NiteMetaConstants.objectType);
		if (media.equalsIgnoreCase(NiteMetaConstants.video)) {
		    media_type=NSignal.VIDEO_SIGNAL;
		}
		NiteSignal signal = new NiteSignal(this, media_type, 
			       NSignal.INTERACTION_SIGNAL,
			       e.getAttribute(NiteMetaConstants.objectName),
			       e.getAttribute(NiteMetaConstants.objectFormat),
			       e.getAttribute(NiteMetaConstants.objectExtension),
			       e.getAttribute(NiteMetaConstants.pathModifier));
		signals.add((Object)signal);
	    }

	    // Done the interaction signals; now for the agent signals
	    nl=null;
	    try {
		nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.agentsignalxpath );
	    } catch (TransformerException ex) {
		ex.printStackTrace();
	    }

	    while ((e = (Element)nl.nextNode())!= null) {         
		int media_type=NSignal.AUDIO_SIGNAL;
		String media = e.getAttribute(NiteMetaConstants.objectType);
		if (media.equalsIgnoreCase(NiteMetaConstants.video)) {
		    media_type=NSignal.VIDEO_SIGNAL;
		}
		//		Debug.print("Added an agent signal");
		NiteSignal signal = new NiteSignal(this, media_type, 
			       NSignal.AGENT_SIGNAL,
			       e.getAttribute(NiteMetaConstants.objectName),
			       e.getAttribute(NiteMetaConstants.objectFormat),
			       e.getAttribute(NiteMetaConstants.objectExtension),
			       e.getAttribute(NiteMetaConstants.pathModifier));
		signals.add((Object)signal);
	    }
	}
	return (List)signals;
    }

    /** Returns a list of NAttributes since the data structure is exactly
	the same. Observation variables define the names and types of
	variables that are associated with each observation. */
    public List getObservationVariables() {
	if (observation_variables==null) {
	    observation_variables=new ArrayList();
	    NodeIterator nl=null;
	    try {
		nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.observationvariablexpath);
	    } catch (TransformerException ex) {
		ex.printStackTrace();
	    }

	    Element e;

	    while ((e = (Element)nl.nextNode())!= null)  {
		ArrayList vals = null;
		String atttype = e.getAttribute(NiteMetaConstants.objectType);
		int attrtype = -1;
		if (atttype.equals(NiteMetaConstants.string)) {
		    attrtype=NAttribute.STRING_ATTRIBUTE;
		} if (atttype.equals(NiteMetaConstants.number)) {
		    attrtype=NAttribute.NUMBER_ATTRIBUTE;		
		} else if (atttype.equals(NiteMetaConstants.enumerated)) {
		    attrtype=NAttribute.ENUMERATED_ATTRIBUTE;
		    vals = getValues(e);
		}
		NiteAttribute nattr = new NiteAttribute(e.getAttribute(NiteMetaConstants.objectName), attrtype, vals);
		// Debug.print("Found an observation variable: " + e.getAttribute(NiteMetaConstants.objectName));
		observation_variables.add(nattr);
	    }
	}
	return (List)observation_variables;
    }


    /** returns an ArrayList of "NiteObservation"s */
    public List getObservations() {
	if (observations==null) {
	    observations=new ArrayList();
	    NodeIterator nl=null;
	    try {
		nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.observationxpath);
	    } catch (TransformerException ex) {
		ex.printStackTrace();
	    }

	    Element e;
	    Element e2;
	    NodeIterator nl2=null;
	    while ((e = (Element)nl.nextNode())!= null)  {
		ArrayList usercodings = new ArrayList();
		nl2=null;
		try {
		    nl2 = XPathAPI.selectNodeIterator(e, NiteMetaConstants.usercodingxpath );
		} catch (TransformerException ex) {
		    ex.printStackTrace();
		}

		while ((e2 = (Element)nl2.nextNode())!= null) {
		    int status = NUserCoding.CODING_UNSTARTED;
		    String codst = e2.getAttribute(NiteMetaConstants.status);
		    if (codst.equalsIgnoreCase(NiteMetaConstants.statusDraft)) { 
			status=NUserCoding.CODING_DRAFT; 
		    } else if (codst.equalsIgnoreCase(NiteMetaConstants.statusFinished)) { 
			status=NUserCoding.CODING_FINISHED; 
		    } else if (codst.equalsIgnoreCase(NiteMetaConstants.statusChecked)) { 
			status=NUserCoding.CODING_CHECKED; 
		    }
		    //  Debug.print("Translated " + codst + " to " + status);
		    NiteUserCoding nuc = new NiteUserCoding(
					    e2.getAttribute(NiteMetaConstants.objectName), 
					    e2.getAttribute(NiteMetaConstants.agent), 
					    e2.getAttribute(NiteMetaConstants.coder), 
					    e2.getAttribute(NiteMetaConstants.checker), status,
					    e2.getAttribute(NiteMetaConstants.date), e2 );
		    usercodings.add((Object)nuc);
		}

		ArrayList variables=getObservationVariableValues(e);

		observations.add(new NiteObservation(this, e.getAttribute(NiteMetaConstants.objectName), e.getAttribute(NiteMetaConstants.description),
						     variables, usercodings, doc, e));
	    }
	}
	return (List)observations;
    }

    /** returns the named observation or null if it doesn't exist */
    public NObservation getObservationWithName(String obsname) {
	NObservation returnval = null;
	List obs = getObservations();
	Iterator obs_it = obs.iterator();
	while (obs_it.hasNext()) {
	    NObservation next = (NObservation) obs_it.next();
	    if (obsname.equalsIgnoreCase(next.getShortName()) == true) {
		returnval = next;
		break;
	    }
	}
	if (returnval==null) {
	    System.err.println("Observation named " + obsname + " not found");
	}
	return returnval;	
    }

    /** Find the list of values of observation variables. This just
        returns a list of String values, but crucially these are in
        the order of the defined observation variables */
    private ArrayList getObservationVariableValues(Element e) {
	// First get the list of defined variables.
	ArrayList vars = (ArrayList)getObservationVariables();

	NodeIterator nl2=null;

	// Now make a list of pairs of variable / values actually present
	try {
	    nl2 = XPathAPI.selectNodeIterator(e, NiteMetaConstants.variablexpath );
	} catch (TransformerException ex) {
	    ex.printStackTrace();
	}
	
	HashMap hm=new HashMap();
	Element e2;
	while ((e2 = (Element)nl2.nextNode())!= null) {
	    String name= e2.getAttribute(NiteMetaConstants.objectName);
	    if (name==null) { continue; }
	    if (vars==null || (findAttribute(vars,name)==null)) {
		Debug.print("WARNING: Ignoring observation variable " + name + " on observation " + e.getAttribute(NiteMetaConstants.objectName) + ". You need to declare all the observation variables used.", Debug.WARNING);
		continue;
	    }
	    hm.put((Object)e2.getAttribute(NiteMetaConstants.objectName),
		   (Object)e2.getAttribute(NiteMetaConstants.value));
	}

	ArrayList variables = new ArrayList();
	
	// go through the declared variables and stick the String values
	// in a list structure. 
	if (vars != null) {
	    Iterator vit = vars.iterator();
	    while (vit.hasNext()) {
		NAttribute nat = (NAttribute) vit.next();
		String val=(String)hm.get((Object)nat.getName());
		// Debug.print("Observation " + e.getAttribute(NiteMetaConstants.objectName) + " has attribute " + nat.getName() + " set to " + val + ".");
		variables.add((Object)val);
	    }
	}

	return variables;
    }

    /** Find the named NAttribute in a list of them or return null */
    private NAttribute findAttribute (List atlist, String name) {
	if (atlist == null) { return null; }
	Iterator vit = atlist.iterator();
	while (vit.hasNext()) {
	    NAttribute nat = (NAttribute) vit.next();
	    if (name.equals(nat.getName())) { return nat; }
	}
	return null;
    }


    /** find an observation with the given name */
    public NObservation findObservationWithName(String name) {
	List oblist=getObservations();
	if (oblist==null) { return null; }
	Iterator oit = oblist.iterator();
	while (oit.hasNext()) {
	    NObservation ob=(NObservation)oit.next();
	    if (ob.getShortName().equals(name)) { 
		return ob; 
	    }
	}
	return null;
    }

    /** find a user coding with the given name */
    public NUserCoding findUserCoding(NObservation observation, String name, String agent) {
	if (observation==null) { return null; }
	return observation.findCoding(name, agent);
    }

    /** add an observation to the corpus. Please note that adding an
        observation programatically will not change any associated NOM
        in any way - you will need to make a call to NOMCorpus.loadData
        with the appropriate arguments to make this observation ready
        for new annotations. */
    public void addObservation(NObservation observation) {
	if (observation==null) { return; }
	Node ch=null;
	try {
	    ch = XPathAPI.selectSingleNode(doc, NiteMetaConstants.observationsxpath);
	} catch (TransformerException ex) {
	    ex.printStackTrace();
	}

	// make sure the "observations" parent node is present in the document
	if (ch==null) {
	    Debug.print("Node with path " + NiteMetaConstants.observationsxpath + " not present", Debug.WARNING);
	    Element corp=null;
	    try {
		corp = (Element)XPathAPI.selectSingleNode(doc, NiteMetaConstants.corpus_path);
	    } catch (TransformerException ex) {
		ex.printStackTrace();
	    }

	    ch=doc.createElement(NiteMetaConstants.observations_element);
	    corp.appendChild(ch);
	}

	try {
	    Element obsnode = (Element)observation.getNode();
	    if (obsnode==null) {
		obsnode=doc.createElement(NiteMetaConstants.observation_element);
		obsnode.setAttribute(NiteMetaConstants.objectName, observation.getShortName());
		if (observation.getVariables()!=null) {		
		    Element varsnode=doc.createElement(NiteMetaConstants.variables_element);
		    obsnode.appendChild(varsnode);
		    Iterator varit=getObservationVariables().iterator();
		    Iterator valit=observation.getVariables().iterator();
		    while(varit.hasNext()) {
			String val=null;
			if (valit.hasNext()) { val=(String)valit.next(); }
			NAttribute nat=(NAttribute)varit.next();
			Element varnode=doc.createElement(NiteMetaConstants.variable_element);
			varnode.setAttribute(NiteMetaConstants.objectName, nat.getName());
			varnode.setAttribute(NiteMetaConstants.value, val);
			varsnode.appendChild(varnode);
		    }
		}
		observation.setNode((Node)obsnode);
		observation.setDocument(doc);
	    }
	    ch.appendChild(obsnode);
	} catch (DOMException dex) {
	    dex.printStackTrace();
	}
    }
	

    /** returns an ArrayList of "NiteStyle"s */
    public List getStyles() {
	if (styles==null) {
	    styles=new ArrayList();
	    NodeIterator nl=null;
	    try {
		nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.stylexpath);
	    } catch (TransformerException ex) {
		ex.printStackTrace();
	    }

	    // ITERATE over the Stylesheets
	    Element e;
	    NodeIterator ni2=null;
	    while ((e=(Element)nl.nextNode())!= null)  {
		ArrayList codings = new ArrayList();
		ArrayList objectsets = new ArrayList();
		ArrayList ontologies = new ArrayList();
		ArrayList corpusresources = new ArrayList();

		//		Debug.print("Style " + e.getAttribute(NiteMetaConstants.fileName) + "; description: " + e.getAttribute(NiteMetaConstants.description) + ". Search for signals using path " + NiteMetaConstants.usessignalxpath);
		// For each style, iterate over the codings it uses...
		try {
		    ni2 = XPathAPI.selectNodeIterator(e, NiteMetaConstants.codingrefxpath);
		} catch (TransformerException ex) {
		    ex.printStackTrace();
		}
		Element e2;
		while ((e2=(Element)ni2.nextNode())!= null)  {
		    String name = e2.getAttribute(NiteMetaConstants.objectName);
		    codings.add(name);
		}

		// ... the objectsets it uses...
		try {
		    ni2 = XPathAPI.selectNodeIterator(e, NiteMetaConstants.objectsetrefxpath);
		} catch (TransformerException ex) {
		    ex.printStackTrace();
		}
		while ((e2=(Element)ni2.nextNode())!= null)  {
		    String name = e2.getAttribute(NiteMetaConstants.objectName);
		    objectsets.add(name);
		}

		// ... the ontologies it uses...
		try {
		    ni2 = XPathAPI.selectNodeIterator(e, NiteMetaConstants.ontologyrefxpath);
		} catch (TransformerException ex) {
		    ex.printStackTrace();
		}
		while ((e2=(Element)ni2.nextNode())!= null)  {
		    String name = e2.getAttribute(NiteMetaConstants.objectName);
		    ontologies.add(name);
		}
		
		int stype = NStyle.DISPLAY;
		String type = e.getAttribute(NiteMetaConstants.objectType);
		if (type.equalsIgnoreCase(NiteMetaConstants.editor)) {
		    stype = NStyle.EDITOR;
		}

		int application=NStyle.OTAB;
		String app = e.getAttribute(NiteMetaConstants.application);
		if (app.equalsIgnoreCase(NiteMetaConstants.nie)) {
		    application = NStyle.NIE;
		}

		NiteStyle style = new NiteStyle(stype, application,
			       e.getAttribute(NiteMetaConstants.objectName),
			       e.getAttribute(NiteMetaConstants.extension),
			       e.getAttribute(NiteMetaConstants.description),
			       codings, objectsets, ontologies);
		styles.add((Object)style);
	    }
	}
	return (List)styles;
    }

    /** returns an "NStyle" with the given name, or null*/
    public NStyle getStyleWithName(String name) {
	List styles = getStyles();
	if (styles==null) { return null; }
	Iterator sit = styles.iterator();
	while (sit.hasNext()) {
	    NStyle nst=(NStyle)sit.next();
	    if (nst.getName().equalsIgnoreCase(name)) {
		return nst;
	    }
	}
	return null;
    }


    /** returns the metadata filename
     */
    public String getFilename() { 
	return metadata_filename;
    }

    /** returns the path to the metadata (can be a URL prefix or a directory path)
     */
    public String getPath() { 
	return metadata_path;
    }

    /** returns the Document (DOM view of the metadata file)
     */
    protected Document getDocument() { 
	return doc;
    }

    /** set the metadata filename for any future save (pass the full
        path or it will be assumed to be relative to the working
        directory) */
    public void setFilename(String filename) throws NiteMetaException {
	// take a copy of the old stuff in case we fail.
	String old_metafilename=metadata_filename;
	boolean old_url=url;
	URL old_metadata_url=metadata_url;
	File old_metadata_file = metadata_file;
	String old_metadata_path=metadata_path;

	metadata_filename=filename;
	metadata_file=null;
	url=false;
	try {
	    metadata_url=new URL(metadata_filename);
	    metadata_path=metadata_filename.substring(0,metadata_filename.lastIndexOf(File.separator));
	    url=true;
	} catch (MalformedURLException muex) { }
	if (!url) {
	    metadata_file = new File(metadata_filename);
	    metadata_path = metadata_file.getParent();
	}

	// make the directories if needed
	if (metadata_path == null || metadata_path.length()==0) { metadata_path="."; }
	File pathfile = new File(metadata_path);
	if (!pathfile.exists()) {
	    if (pathfile.mkdirs()) {
		Debug.print("Made directory for metadata: " + metadata_path, Debug.DEBUG);
	    } else {
		metadata_filename=old_metafilename;
		url=old_url;
		metadata_url=old_metadata_url;
		metadata_file=old_metadata_file;
		metadata_path=old_metadata_path;
		throw new NiteMetaException("Failed to make directory for metadata: " + metadata_path + ". Reverting to old metadata location!");
	    }
	}
	// set the paths to null so we re-calculate from the new location
	ontologypath=null;
	objectsetpath=null;
	corpusresourcepath=null;
	codingspath=null;
    }

    /** get the name of the reserved element for NITE children in this corpus */
    public String getChildElementName() {
	if (reservedChildElement==null) { 
	    return NiteMetaConstants.defaultReservedChild; 
	}
	return reservedChildElement;
    }

    /** get the name of the reserved element for NITE pointers in this corpus */
    public String getPointerElementName() {
	if (reservedPointerElement==null) { 
	    return NiteMetaConstants.defaultReservedPointer; 
	}
	return reservedPointerElement;
    }

    /** get the name of the reserved element for external NITE
     * pointers in this corpus (this is only used for elements in
     * EXTERNAL_POINTER_LAYER type layers  */
    public String getExternalPointerElementName() {
	//Debug.print("HHHH");
	if (reservedExternalPointerElement==null) { 
	    return NiteMetaConstants.defaultReservedExternalPointer; 
	}
	return reservedExternalPointerElement;
    }

    /** change the name of an external pointer element for serialization - the
        default value is "nite:external_pointer" */
    public void setExternalPointerElementName(String pointer_name) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedExternalPointer);
	    if (n != null) { n.setNodeValue(pointer_name); }
	    else { // add the element
		Node rel=addReservedElementsNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedExternalPointerName);
		ch.setAttribute(NiteMetaConstants.objectName, pointer_name);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedExternalPointerElement=pointer_name;
    }

    /** change the name of a pointer element for serialization - the
        default value is "nite:pointer" */
    public void setPointerElementName(String pointer_name) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedPointer);
	    if (n != null) { n.setNodeValue(pointer_name); }
	    else { // add the element
		Node rel=addReservedElementsNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedPointerName);
		ch.setAttribute(NiteMetaConstants.objectName, pointer_name);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedPointerElement=pointer_name;
    }

    /** change the name of a child element for serialization - the
        default value is "nite:child" */
    public void setChildElementName(String child_name) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedChild);
	    if (n != null) { n.setNodeValue(child_name); }
	    else { // add the element
		Node rel=addReservedElementsNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedChildName);
		ch.setAttribute(NiteMetaConstants.objectName, child_name);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedChildElement=child_name;
    }

    /** get the name of the reserved element for NITE streams in this corpus */
    public String getStreamElementName() {
	if (reservedStreamElement==null) { 
	    return NiteMetaConstants.defaultReservedStream; 
	}
	return reservedStreamElement;
    }

    /* return the existing reserved-elements node or make one */
    private Node addReservedElementsNode() {
	Node n=null;
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedElements);
	    if (n == null) {
		Element corp = (Element)XPathAPI.selectSingleNode(doc, NiteMetaConstants.corpus_path);
		Element ch=doc.createElement(NiteMetaConstants.reservedElementsName);
		// Add after reserved attributes, or before the first node
		Node ras = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedAttributes);
		if (ras!=null) {
		    corp.insertBefore(ch,ras.getNextSibling());
		} else {
		    corp.insertBefore(ch,corp.getFirstChild());
		}
		n=(Node)ch;
	    }
	    return n;
	} catch (TransformerException e) { return n;}	
    }

    /* return the existing reserved-attributes node or make one */
    private Node addReservedAttributesNode() {
	Node n=null;
	try {
	    n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedAttributes);
	    if (n == null) {
		Element corp = (Element)XPathAPI.selectSingleNode(doc, NiteMetaConstants.corpus_path);
		Element ch=doc.createElement(NiteMetaConstants.reservedAttributesName);
		corp.insertBefore(ch,corp.getFirstChild());
		n=(Node)ch;
	    }
	    return n;
	} catch (TransformerException e) { return n;}	
    }

    /** set the name of the reserved element for NITE streams in this corpus */
    public void setStreamElementName(String element_name) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedStream);
	    if (n != null) { n.setNodeValue(element_name); }
	    else {
		// No reserved pointer element declared - add an element
		Node rel=addReservedElementsNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedStreamName);
		ch.setAttribute(NiteMetaConstants.objectName, element_name);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedStreamElement=element_name;
    }

    /** get the name of the reserved element for text in this corpus */
    public String getTextElementName() {
	if (reservedTextElement==null) { 
	    return NiteMetaConstants.defaultReservedText; 
	}
	return reservedTextElement;
    }

    /** get the name of the reserved attribute for start times in this
        corpus */
    public String getStartTimeAttributeName() {
	if (reservedStartTimeAttribute==null) { 
	    return NiteMetaConstants.defaultReservedStart; 
	}
	return reservedStartTimeAttribute;
    }

    /** get the name of the reserved attribute for end times in this
        corpus */
    public String getEndTimeAttributeName() {
	if (reservedEndTimeAttribute==null) { 
	    return NiteMetaConstants.defaultReservedEnd; 
	}
	return reservedEndTimeAttribute;
    }

    /** set the name of the reserved attribute for start times in this
        corpus */
    public void setStartTimeAttributeName(String start) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedStartTime);
	    if (n != null) { n.setNodeValue(start); }
	    else { // add the element
		Node rel=addReservedAttributesNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedStartTimeName);
		ch.setAttribute(NiteMetaConstants.objectName, start);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedStartTimeAttribute=start;
    }

    /** set the name of the reserved attribute for end times in this
        corpus */
    public void setEndTimeAttributeName(String end) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedEndTime);
	    if (n != null) { n.setNodeValue(end); }
	    else { // add the element
		Node rel=addReservedAttributesNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedEndTimeName);
		ch.setAttribute(NiteMetaConstants.objectName, end);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedEndTimeAttribute=end;
    }

    /** get the name of the reserved attribute for IDs in this corpus */
    public String getIDAttributeName() {
	if (reservedIDAttribute==null) { 
	    return NiteMetaConstants.defaultReservedID; 
	}
	return reservedIDAttribute;
    }

    /** set the name of the reserved attribute for IDs in this corpus */
    public void setIDAttributeName(String id) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedID);
	    if (n != null) { n.setNodeValue(id); }
	    else { // add the element
		Node rel=addReservedAttributesNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedIDName);
		ch.setAttribute(NiteMetaConstants.objectName, id);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedIDAttribute=id;
    }

    /** get the name of the reserved attribute for comments in this corpus */
    public String getCommentAttributeName() {
	if (reservedCommentAttribute==null) { 
	    return NiteMetaConstants.defaultReservedComment; 
	}
	return reservedCommentAttribute;
    }

    /** set the name of the reserved attribute for comments in this corpus */
    public void setCommentAttributeName(String comment) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedComment);
	    if (n != null) { n.setNodeValue(comment); }
	    else { // add the element
		Node rel=addReservedAttributesNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedCommentName);
		ch.setAttribute(NiteMetaConstants.objectName, comment);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedCommentAttribute=comment;
    }

    /** Return the name of the reserved agent attribute for the corpus
        - this attribute will not be expected on the input files but
        will be added to any elements that have agents on import. If
        the value is null (i.e. there is no 'agentname' element in the
        metadata file), no agent attributes are added. This is a
        convenience function to allow access to agents from the query
        language. */
    public String getAgentAttributeName() {
	/*
	if (reservedAgentAttribute==null) { 
	    return NiteMetaConstants.defaultReservedAgent; 
	}
	*/
	return reservedAgentAttribute;
    }

    /** set the name of the reserved attribute for agents in this corpus */
    public void setAgentAttributeName(String agent) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedAgent);
	    if (n != null) { n.setNodeValue(agent); }
	    else { // add the element
		Node rel=addReservedAttributesNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedAgentName);
		ch.setAttribute(NiteMetaConstants.objectName, agent);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedAgentAttribute=agent;
    }

    /** Return the name of the reserved observation attribute for the
        corpus - this attribute will not be expected on the input
        files but will be added to all elements on import. If the
        value is null (i.e. there is no 'observationname' element in
        the metadata file), no observation attributes are added. This
        is a convenience function to allow access to observations from
        the query language. */
    public String getObservationAttributeName() {
	return reservedObservationAttribute;
    }

    /** set the name of the reserved attribute for observations in this corpus */
    public void setObservationAttributeName(String observation) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedObservation);
	    if (n != null) { n.setNodeValue(observation); }
	    else { // add the element
		Node rel=addReservedAttributesNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedObservationName);
		ch.setAttribute(NiteMetaConstants.objectName, observation);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedObservationAttribute=observation;
    }

    /** Return the name of the reserved resource attribute for the
        corpus - this attribute will not be expected on the input
        files but will be added to all elements on import. If the
        value is null (i.e. there is no 'resourcename' element in
        the metadata file), no resource attributes are added. This
        is a convenience function to allow access to resources from
        the query language. */
    public String getResourceAttributeName() {
	return reservedResourceAttribute;
    }

    /** set the name of the reserved attribute for resources in this corpus */
    public void setResourceAttributeName(String res) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedResource);
	    if (n != null) { n.setNodeValue(res); }
	    else { // add the element
		Node rel=addReservedAttributesNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedResourceName);
		ch.setAttribute(NiteMetaConstants.objectName, res);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedResourceAttribute=res;
    }


    /** get the name of the reserved attribute for Graphical Visual
        Markup in this corpus */
    public String getKeyStrokeAttributeName() {
	if (reservedKeyAttribute==null) { 
	    return NiteMetaConstants.defaultReservedKey; 
	}
	return reservedKeyAttribute;
    }

    /** set the name of the reserved attribute for KeyStrokes in this corpus */
    public void setKeyStrokeAttributeName(String key) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedKey);
	    if (n != null) { n.setNodeValue(key); }
	    else { // add the element
		Node rel=addReservedAttributesNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedKeyName);
		ch.setAttribute(NiteMetaConstants.objectName, key);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedKeyAttribute=key;
    }

    /** get the name of the reserved attribute for Graphical Visual
        Markup in this corpus */
    public String getGVMAttributeName() {
	if (reservedGVMAttribute==null) { 
	    return NiteMetaConstants.defaultReservedGVM; 
	}
	return reservedGVMAttribute;
    }

    /** set the name of the reserved attribute for Graphical Visual
        Markup in this corpus */
    public void setGVMAttributeName(String GVM) {
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.reservedGVM);
	    if (n != null) { n.setNodeValue(GVM); }
	    else { // add the element
		Node rel=addReservedAttributesNode();
		Element ch=doc.createElement(NiteMetaConstants.reservedGVMName);
		ch.setAttribute(NiteMetaConstants.objectName, GVM);
		((Element)rel).appendChild(ch);
	    }
	} catch (TransformerException e) { }
	reservedGVMAttribute=GVM;
    }


    /** set all the distinguished element and attribute values to
        their defaults. */
    public void setElementsAndAttributesToDefaults() {
	setPointerElementName(NiteMetaConstants.defaultReservedPointer);
	setChildElementName(NiteMetaConstants.defaultReservedChild);
	setStreamElementName(NiteMetaConstants.defaultReservedStream);
	setStartTimeAttributeName(NiteMetaConstants.defaultReservedStart);
	setEndTimeAttributeName(NiteMetaConstants.defaultReservedEnd);
	setIDAttributeName(NiteMetaConstants.defaultReservedID);
	setCommentAttributeName(NiteMetaConstants.defaultReservedComment);
	setAgentAttributeName(NiteMetaConstants.defaultReservedAgent);
    }


    /* CVS */
    /** get the protocol used by CVS (or null if there's no CVS information) */
    public String getCVSProtocol() {
	return cvsProtocol;
    }

    /** get the CVS server (or null if there's no CVS information) */
    public String getCVSServer() {
	return cvsServer;
    }

    /** get the module / base directory used by CVS (or null if
     * there's no CVS information) */
    public String getCVSModule() {
	return cvsModule;
    }

    /** get the repository used by CVS (or null if
     * there's no CVS information) */
    public String getCVSRepository() {
	return cvsRepository;
    }

    /** get the connection method used by CVS (or null if
     * there's no CVS information) */
    public int getCVSConnectionMethod() {
	if (cvsConnection!=null && cvsConnection.equals(NiteMetaConstants.ssh)) {
	    return NiteMetaConstants.SSH;
	}
	return NiteMetaConstants.RSH;
    }

    /** returns the directory path where styles are stored for the
        corpus, as it is in the metadata file - relative to the
        metadata file.  */
    public String getRelativeStylePath(){
	if (relstylepath==null) {
	    try {
		Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.stylepathxpath);
		if (n==null) { return null; }
		relstylepath = n.getNodeValue();
	    } catch (TransformerException e) {
		e.printStackTrace();
	    }
	}
	return relstylepath;
    }

    /** returns the directory path where styles are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relstylepath is absolute) */
    public String getStylePath(){
	if (stylepath==null) {
	    stylepath=decidePath(getRelativeStylePath());
	}
	return stylepath;
    }


    /** change the path where the "styles" are loaded from - styles
        are stylesheets and annotation board displays. */
    public void setStylePath(String path){
	try {
	    Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.stylepathxpath);
	    n.setNodeValue(path);
	} catch (TransformerException e) {
	    e.printStackTrace();
	} 
	relstylepath=path;
	stylepath=null;
	stylepath=getStylePath();
    }


    public String getAnnotationSpecPath(){
	if (annotationspecpath==null) {
	    try {
		Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.annotationspecpathxpath);
		annotationspecpath = n.getNodeValue();
	    } catch (TransformerException e) {
		e.printStackTrace();
	    }
	}
	return annotationspecpath;
    }


    /** returns the directory path where codings are stored for the
        corpus, as it is in the metadata file - relative to the
        metadata file.  */
    public String getRelativeCodingPath(){
	if (relcodingspath==null) {
	    try {
		Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.codingspathxpath);
		if (n==null) { return null; }
		relcodingspath = n.getNodeValue();
	    } catch (TransformerException e) {
		e.printStackTrace();
	    }
	}
	return relcodingspath;
    }

    /** returns the directory path where codings are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relcodingspath is absolute) */
    public String getCodingPath(){
	if (codingspath==null) {
	    codingspath=decidePath(getRelativeCodingPath());
	}
	return codingspath;
    }

    /** returns the directory path where interaction codings are
        stored for the corpus, as it is in the metadata file -
        relative to the metadata file.  */
    public String getRelativeInteractionCodingPath(){
	if (relintercodingspath==null) {
	    try {
		Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.intercodingpathxpath);
		if (n==null) { return null; }
		relintercodingspath = n.getNodeValue();
	    } catch (TransformerException e) {
		e.printStackTrace();
	    }
	}
	return relintercodingspath;
    }

    /** returns the directory path where interaction codings are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relcodingspath is absolute) */
    public String getInteractionCodingPath(){
	if (intercodingspath==null) {
	    intercodingspath=decidePath(getRelativeInteractionCodingPath());
	}
	return intercodingspath;
    }

    /** returns the directory path where agent codings are stored for
        the corpus, as it is in the metadata file - relative to the
        metadata file.  */
    public String getRelativeAgentCodingPath(){
	if (relagentcodingspath==null) {
	    try {
		Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.agentcodingpathxpath);
		if (n==null) { return null; }
		relagentcodingspath = n.getNodeValue();
	    } catch (TransformerException e) {
		e.printStackTrace();
	    }
	}
	return relagentcodingspath;
    }

    /** returns the directory path where agent codings are stored for
        the corpus - relative to the working directory in which java
        is running (or absolute if relcodingspath is absolute) */
    public String getAgentCodingPath(){
	if (agentcodingspath==null) {
	    agentcodingspath=decidePath(getRelativeAgentCodingPath());
	}
	return agentcodingspath;
    }


    /** change the path where the codings are loaded and serialized
        from & to */
    public void setCodingPath(String path)  {
	try {
	    Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.codingspathxpath);
	    if (n==null) { 
		System.err.println("WARNING: Attempting to set the Coding Path when no codings are present");
		return;
	    } else {
		n.setNodeValue(path);
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	} 
	relcodingspath=path;
	codingspath=null;
	codingspath=getCodingPath();
    }

    /** returns the directory path where signals are stored for the
        corpus, as it is in the metadata file - relative to the
        metadata file.  */
    public String getRelativeSignalPath(){
	if (relsignalpath==null) {
	    try {
		Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.signalpathxpath);
		if (n==null) { return null; }
		relsignalpath = n.getNodeValue();
	    } catch (TransformerException e) {
		e.printStackTrace();
	    }
	}
	return relsignalpath;
    }

    /** returns the directory path where signals are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relsignalpath is absolute) */
    public String getSignalPath(){
	if (signalpath==null) {
	    signalpath=decidePath(getRelativeSignalPath());
	}
	return signalpath;
    }

    /** returns the modifier to the signal path, replacing the string
     * 'observation' with the provided Observation's name. This allows
     * a certain amount of flexibility in the placement of signal
     * files. Set using pathmodifier="obsrevation" which means signals
     * are assumed to be in subdirectories named exactly the same as
     * the observationname. */
    public String getSignalPathModifier(NObservation obs) {
	if (obs!=null) {
	    return getSignalPathModifier(obs.getShortName());
	} 
	return getSignalPathModifier("");	
    }

    /** returns the modifier to the signal path, replacing the string
     * 'observation' with the provided Observation's name. This allows
     * a certain amount of flexibility in the placement of signal
     * files. Set using pathmodifier="obsrevation" which means signals
     * are assumed to be in subdirectories named exactly the same as
     * the observationname. */
    public String getSignalPathModifier(String obs) {
	if (signalpathmodifier==null) {
	    try {
		Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.signalmodifierxpath);
		if (n==null) { return ""; }
		signalpathmodifier = n.getNodeValue();
		if (signalpathmodifier!=null && obs!=null) {
		    signalpathmodifier = signalpathmodifier.replaceAll("observation", obs);
		}
		if (signalpathmodifier!=null) {
		    signalpathmodifier = File.separator + signalpathmodifier;
		}
	    } catch (TransformerException e) {
		e.printStackTrace();
	    }
	}
	if (signalpathmodifier==null) { signalpathmodifier=""; }
	return signalpathmodifier;	
    }

    /** change the path where the resource file is serialized to */
    public void setResourceFilename(String resource_filename) {
	if (resourcefile!=null) {
	    resourcefile.setFilename(resource_filename);
	}
	try {
	    Node n = XPathAPI.selectSingleNode(doc, NiteMetaConstants.resourcesxpath);
	    if (n==null || resourcefile==null) {
		Debug.print("WARNING: Attempting to set resource filename when there is no resource file loaded!", Debug.WARNING);
		return;
	    } 
	    //System.out.println("Set resource file name to: " + resource_filename);
	    n.setNodeValue(resource_filename);
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
    }

    /** returns the directory path where signals are stored for the
        corpus, as it is in the metadata file - relative to the
        metadata file.  */
    public String getRelativeOntologyPath(){
	if (relontologypath==null) {
	    try {
		Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.ontologypathxpath);
		if (n==null) { return null; }
		relontologypath = n.getNodeValue();
	    } catch (TransformerException e) {
		e.printStackTrace();
	    }
	}
	return relontologypath;
    }

    /** returns the directory path where ontologies are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relontologypath is absolute) */
    public String getOntologyPath(){
	if (ontologypath==null) {
	    ontologypath=decidePath(getRelativeOntologyPath());
	}
	return ontologypath;
    }

    /** change the path where the ontologies are loaded and serialized
        from & to */
    public void setOntologyPath(String path){
	try {
	    Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.ontologypathxpath);
	    if (n==null) { 
		System.err.println("WARNING: Attempting to set ontology path when there are no ontologies loaded!");
		return;
	    } else {
		n.setNodeValue(path);
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	relontologypath=path;
	ontologypath=null;
	ontologypath=getOntologyPath();
    }

    /** returns an ArrayList of "NiteOntology"s */
    public List getOntologies() {
	if (ontologies==null) {
	    ontologies=new ArrayList();
	    NodeIterator nl=null;
	    try {
		nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.ontologyxpath);
	    } catch (TransformerException ex) {
		ex.printStackTrace();
	    }

	    Element e;
	    while ((e = (Element)nl.nextNode())!= null)  {
		String elname=e.getAttribute(NiteMetaConstants.elementName);
		String col=e.getAttribute(NiteMetaConstants.displayColour);
		String atname=e.getAttribute(NiteMetaConstants.attributeName);
		NOntology non = new NiteOntology(
			  e.getAttribute(NiteMetaConstants.objectName), 
			  e.getAttribute(NiteMetaConstants.description), 
			  e.getAttribute(NiteMetaConstants.fileName),
			  elname, atname);
		NiteElement nele = new NiteElement(elname, null, null, non, col, e.getAttribute(NiteMetaConstants.textContent));
		nele.addAttribute(new NiteAttribute(atname, NAttribute.STRING_ATTRIBUTE, null));
		ArrayList attrs = getAttributes(e);
		for (Iterator ait=attrs.iterator(); ait.hasNext(); ) {
		    nele.addAttribute((NiteAttribute)ait.next());
		}
		elements.put((Object)elname, (Object)nele);
		ontologies.add(non);
	    }
	}
	return (List)ontologies;
    }

    /** returns an ArrayList of "NiteObjectSet"s */
    public List getObjectSets() {
	if (objectsets==null) {
	    objectsets=new ArrayList();
	    NodeIterator nl=null;
	    try {
		nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.objectsetxpath);
	    } catch (TransformerException ex) {
		ex.printStackTrace();
	    }

	    Element e;
	    while ((e = (Element)nl.nextNode())!= null)  {
		NiteObjectSet nos = new NiteObjectSet(
			    e.getAttribute(NiteMetaConstants.objectName), 
			    e.getAttribute(NiteMetaConstants.description), 
			    e.getAttribute(NiteMetaConstants.fileName));
		objectsets.add(nos);

		// Object sets can contain a bunch of "code"s like a layer
		ArrayList els = getElements(e,nos);
		nos.setContentElements(els);
		// layerhash.put(e2.getAttribute(NiteMetaConstants.objectName), nitel);
		// nos.setLayer(nitel);
	    }
	}
	return (List)objectsets;
    }

    /** returns the directory path where object sets are stored for the
        corpus, as it is in the metadata file - relative to the
        metadata file.  */
    public String getRelativeObjectSetPath(){
	if (relobjectsetpath==null) {
	    try {
		Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.objectsetpathxpath);
		if (n==null) { return null; }
		relobjectsetpath = n.getNodeValue();
	    } catch (TransformerException e) {
		e.printStackTrace();
	    }
	}
	return relobjectsetpath;
    }

    /** returns the directory path where object sets are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relobjectsetpath is absolute) */
    public String getObjectSetPath(){
	if (objectsetpath==null) {
	    objectsetpath=decidePath(getRelativeObjectSetPath());
	}
	return objectsetpath;
    }


    /** change the path where the object sets are loaded and serialized
        from & to */
    public void setObjectSetPath(String path) {
	try {
	    Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.objectsetpathxpath);
	    if (n==null) { 
		System.err.println("WARNING: Attempting to set the Object Set Path when no object sets are present");
		return;
	    } else {
		n.setNodeValue(path);
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	relobjectsetpath=path;
	objectsetpath=null;
	objectsetpath=getObjectSetPath();
    }

    /** returns a single NResourceData element or null if there's no
     * resource file */
    public NResourceData getResourceData() {
	return resourcefile;
    }

    /** returns an ArrayList of "NiteCorpusResource"s */
    public List getCorpusResources() {
	if (corpusresources==null) {
	    corpusresources=new ArrayList();
	    NodeIterator nl=null;
	    try {
		nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.corpusresourcexpath);
	    } catch (TransformerException ex) {
		ex.printStackTrace();
	    }

	    Element e;
	    while ((e = (Element)nl.nextNode())!= null)  {
		NiteCorpusResource ncr = new NiteCorpusResource(
			    e.getAttribute(NiteMetaConstants.objectName), 
			    e.getAttribute(NiteMetaConstants.description), 
			    e.getAttribute(NiteMetaConstants.fileName));
		corpusresources.add(ncr);
		// Corpus Resources can contain a bunch of "layer"s 
		// exactly like a coding. So we use the same code..
		NCorpusResourceCoding coding = getCorpusResourceCoding(e, ncr.getName(), NCoding.INTERACTION_CODING, null);
		ncr.setCoding(coding);
	    }
	}
	return (List)corpusresources;
    }


    /** returns the directory path where object sets are stored for the
        corpus, as it is in the metadata file - relative to the
        metadata file.  */
    public String getRelativeCorpusResourcePath(){
	if (relcorpusresourcepath==null) {
	    try {
		Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.corpusresourcepathxpath);
		if (n==null) { return null; }
		relcorpusresourcepath = n.getNodeValue();
	    } catch (TransformerException e) {
		e.printStackTrace();
	    }
	}
	return relcorpusresourcepath;
    }

    /** returns the directory path where object sets are stored for the
        corpus - relative to the working directory in which java is
        running (or absolute if relcorpusresourcepath is absolute) */
    public String getCorpusResourcePath(){
	if (corpusresourcepath==null) {
	    corpusresourcepath=decidePath(getRelativeCorpusResourcePath());
	}
	return corpusresourcepath;
    }

    protected String decidePath(String relpath) {
	boolean url=false;
	String properpath="";
	String parpath = metadata_path;
	if (relpath==null) { System.err.println("null relpath; return '"+properpath+"'"); return properpath; }
	try {
	    URL u = new URL(relpath);
	    url=true;
	} catch (MalformedURLException muex) { }
	if (!(new File(relpath).isAbsolute()) && !url) {
	    if (parpath != null) {
		properpath=parpath + File.separator;
	    }
	}
	properpath += relpath;	
	//System.out.println("Return: " + properpath);
	return properpath;
    }


    /** change the path where the object sets are loaded and serialized
        from & to */
    public void setCorpusResourcePath(String path) {
	try {
	    Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.corpusresourcepathxpath);
	    if (n==null) { 
		System.err.println("WARNING: Attempting to set the Object Set Path when no object sets are present");
		return;
	    } else {
		n.setNodeValue(path);
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	relcorpusresourcepath=path;
	corpusresourcepath=null;
	corpusresourcepath=getCorpusResourcePath();
    }

    public List getCodings(){
	if (codings==null) {
	    codings=new ArrayList();
	    if (corpus_type==SIMPLE_CORPUS) {
		try {
		    Node n=XPathAPI.selectSingleNode(doc, NiteMetaConstants.codingsxpath);
		    NCoding nc = getCoding((Element)n, "coding", NCoding.INTERACTION_CODING, null);
		    codings.add(nc);
		} catch (TransformerException ex) {
		    ex.printStackTrace();
		}
	    } else {

		NodeIterator nl=null;
		try {
		    nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.intercodingxpath);
		} catch (TransformerException ex) {
		    ex.printStackTrace();
		}
		
		// ITERATE over the Interaction codings...
		Element e;
		while ((e=(Element)nl.nextNode())!= null)  {
		    NCoding nc = getCoding(e, e.getAttribute(NiteMetaConstants.objectName), NCoding.INTERACTION_CODING, e.getAttribute(NiteMetaConstants.objectPath));
		    codings.add(nc);
		}

		nl=null;
		try {
		    nl = XPathAPI.selectNodeIterator(doc, NiteMetaConstants.agentcodingxpath);
		} catch (TransformerException ex) {
		    ex.printStackTrace();
		}
		
		// ... and iterate over the Agent codings...
		while ((e=(Element)nl.nextNode())!= null)  {
		    NCoding nc = getCoding(e, e.getAttribute(NiteMetaConstants.objectName), NCoding.AGENT_CODING, e.getAttribute(NiteMetaConstants.objectPath));
		    codings.add(nc);
		}

	    }		
	}
	return (List)codings;
    }

    /** get the NCoding with the given name or null if it doesn't exist */ 
    public NCoding getCodingByName(String name) {
	List codes = (ArrayList)getCodings();
	Iterator cit = codes.iterator();
	while (cit.hasNext()) {
	    NiteCoding tcod = (NiteCoding) cit.next();
	    if (tcod.getName().equals(name)) { return tcod; }
	}
	return null;
    }

    /** get the NFile with the given name or null if it doesn't exist */ 
    public NFile getNFileByName(String name) {
	NFile nf = getCodingByName(name);
	if (nf!=null) { return nf; }
	nf = getCorpusResourceByName(name);
	if (nf!=null) { return nf; }
	nf = getOntologyByName(name);
	if (nf!=null) { return nf; }
	nf = getObjectSetByName(name);
	return nf;
    }

    /** get the NLayer with the given name or null if it doesn't
     * exist. Rather inefficient, but shouldn't be a problem unless
     * the metadata is huge! */ 
    public NLayer getLayerByName(String name) {
	ArrayList codes = (ArrayList)getCodings();
	Iterator cit = codes.iterator();
	while (cit.hasNext()) {
	    NiteCoding tcod = (NiteCoding) cit.next();
	    //Debug.print ("Coding: '" + tcod.getName() + "' has " +tcod.getLayers().size() + " layers"); 
	    
	    for (Iterator lit=tcod.getLayers().iterator(); lit.hasNext(); ) {
		NLayer lay = (NLayer) lit.next();
		//Debug.print ("Compare: '" + lay.getName() + "' with '" + name + "'"); 
		if (lay.getName().equalsIgnoreCase(name)) {
		    return lay;
		}
	    }
	}
	ArrayList corps = (ArrayList)getCorpusResources();
	Iterator crit = corps.iterator();
	while (crit.hasNext()) {
	    NiteCorpusResource tcod = (NiteCorpusResource) crit.next();
	    for (Iterator lit=tcod.getLayers().iterator(); lit.hasNext(); ) {
		NLayer lay = (NLayer) lit.next();
		if (lay.getName().equalsIgnoreCase(name)) {
		    return lay;
		}
	    }
	}
	return null;
    }


    /** get the NObjectSet with the given name or null if it doesn't exist */ 
    public NObjectSet getObjectSetByName(String name) {
	ArrayList corps = (ArrayList)getObjectSets();
	for (Iterator crit = corps.iterator(); crit.hasNext(); ) {
	    NiteObjectSet tcod = (NiteObjectSet) crit.next();
	    if (tcod.getName().equalsIgnoreCase(name)) {
		return tcod;
	    }
	}
	return null;
    }

    /** get the NOntology with the given name or null if it doesn't exist */ 
    public NOntology getOntologyByName(String name) {
	ArrayList corps = (ArrayList)getOntologies();
	for (Iterator crit = corps.iterator(); crit.hasNext(); ) {
	    NiteOntology tcod = (NiteOntology) crit.next();
	    if (tcod.getName().equalsIgnoreCase(name)) {
		return tcod;
	    }
	}
	return null;
    }

    /** get the NCorpusResource with the given name or null if it doesn't exist */ 
    public NCorpusResource getCorpusResourceByName(String name) {
	ArrayList corps = (ArrayList)getCorpusResources();
	for (Iterator crit = corps.iterator(); crit.hasNext(); ) {
	    NiteCorpusResource tcod = (NiteCorpusResource) crit.next();
	    if (tcod.getName().equalsIgnoreCase(name)) {
		return tcod;
	    }
	}
	return null;
    }

    /** returns a List of all the "NElement"s in all the codings in
        the corpus */
    public List getAllElements(){
	if (all_elements==null) {
	    all_elements=new ArrayList();
	    for (Iterator cit=getCodings().iterator(); cit.hasNext(); ) {
		for (Iterator lit=((NCoding)cit.next()).getLayers().iterator(); lit.hasNext(); ) {
		    for (Iterator eit=((NLayer)lit.next()).getContentElements().iterator(); eit.hasNext(); ) {
			all_elements.add((NElement)eit.next());
		    }
		}
	    }
	}
	return all_elements;
    }

    private void setLayers(Element element, NCoding ncoding) {
	int layercount=0;
	//	Debug.print("Looking for layers in coding: " + name);	
	NodeIterator nodeit=null;
	try {
	    nodeit = XPathAPI.selectNodeIterator(element, NiteMetaConstants.layerxpath);
	} catch (TransformerException ex) {
	    ex.printStackTrace();
	}
	// ITERATE over the layers
	NiteLayer nl=null;
	NiteLayer lastlayer=null;
	Element e;
	while ((e=(Element)nodeit.nextNode())!= null)  {
	    String layertype = e.getTagName();
	    int laytype = -1;
	    boolean inherits = true;
	    if (layertype.equals(NiteMetaConstants.structlayer)) {
		laytype=NLayer.STRUCTURAL_LAYER;
	    } else if (layertype.equals(NiteMetaConstants.timedlayer)) {
		laytype=NLayer.TIMED_LAYER;
	    } else if (layertype.equals(NiteMetaConstants.featlayer)) {
		laytype=NLayer.FEATURAL_LAYER;
		inherits = false;
	    } else if (layertype.equals(NiteMetaConstants.externallayer)) {
		laytype=NLayer.EXTERNAL_POINTER_LAYER;
		inherits=false;
		if (e.getAttribute(NiteMetaConstants.layertype).equalsIgnoreCase(NiteMetaConstants.structural)) {
		    inherits=true;
		}
	    }

	    if (e.getAttribute(NiteMetaConstants.inheritstime).equalsIgnoreCase(NiteMetaConstants.inheritstimefalse)) {
		inherits=false;
	    }
	    
	    String childlayer = findChildLayer(e);
	    String recchildlayer = findRecursiveChildLayer(e);

	    nl = new NiteLayer(e.getAttribute(NiteMetaConstants.objectName), 
			       laytype, ncoding, 
			       childlayer,
			       recchildlayer,
			       e.getAttribute(NiteMetaConstants.program),
			       e.getAttribute(NiteMetaConstants.contenttype),
			       inherits);
	    
	    // external pointer layers are a special case...
	    if (laytype==NLayer.EXTERNAL_POINTER_LAYER) {
		getProgramArguments(e,nl);
		NiteElement nel = makeSpecialElement(e, e.getAttribute(NiteMetaConstants.elementName), nl);
		ArrayList ellist = new ArrayList();
		ellist.add(nel);
		elements.put((Object)e.getAttribute(NiteMetaConstants.elementName), (Object)nel);
		nl.setContentElements(ellist);
	    } else {
		ArrayList els = getElements(e,nl);
		nl.setContentElements(els);
	    }
	    layerhash.put(e.getAttribute(NiteMetaConstants.objectName), nl);
	    //	    Debug.print("Added entry to layerhash: " + e.getAttribute(NiteMetaConstants.objectName) + "; " + nl);
	    layercount += 1;
	    lastlayer=nl;
	}


    }

    private NCorpusResourceCoding getCorpusResourceCoding(Element element, String name, int type, String path) {
	NiteCorpusResourceCoding nc = new NiteCorpusResourceCoding(this, name, type, null, null, path);
	setLayers(element, nc);
	return nc;
    }

    private NCoding getCoding(Element element, String name, int type, String path) {
	NiteCoding nc = new NiteCoding(this, name, type, null, null, path);;
	setLayers(element, nc);	
	return nc;
    }

    /* Find the layer we take children from: allow the old syntax but
     * prefer the new! */
    private String findChildLayer(Element e) {
	if (e==null) { return null; }
	String r = e.getAttribute(NiteMetaConstants.drawschildren);
	if (r==null || r.length()==0) {
	    r = e.getAttribute(NiteMetaConstants.pointsto);
	}
	return r;
    }

    /* Find the layer we recursively take children from: allow the old
     * syntax but prefer the new! */
    private String findRecursiveChildLayer(Element e) {
	if (e==null) { return null; }
	String r = e.getAttribute(NiteMetaConstants.recursedrawschildren);
	if (r==null || r.length()==0) {
	    r = e.getAttribute(NiteMetaConstants.recursepointsto);
	}
    return r;
    }

    /** extract the arguments to be passed to the external program
     * associated with the external pointer of this layer: only ever
     * called if this is an EXTERNAL_POINTER_LAYER */
    private NiteElement makeSpecialElement(Element xmlele, String elname, NiteLayer layer) {
	ArrayList points = getPointers(xmlele);
	// make a normal element with no attributes or pointers (not much use then!)
	NiteElement nele = new NiteElement(elname, null, null, layer, null, null);

	// It'll have one child or pointer for each of the pointers
	// declared (depending on what the layer-type of its parent is)
	if (points==null) {
	    System.err.println("NO Pointers declared on external pointer layer '" + layer.getName() +  "'!"); 
	} else {
	    if (layer.inheritsTime()) { //children
		NitePointer p1 = (NitePointer)points.get(0);
		// we set the child layer for this layer, and it'll act
		// like a normal parent/child relationship
		layer.setChildLayerName(p1.getTarget());
	    } else {  // pointers
		nele.setPointers(points);
	    }
	}

	// plus one special pointer to external data.
	nele.addExternalPointer(xmlele.getAttribute(NiteMetaConstants.extpointerrole));
	
	return nele;
    }


    /** extract the arguments to be passed to the external program
     * associated with the external pointer of this layer: only ever
     * called if this is an EXTERNAL_POINTER_LAYER */
    private void getProgramArguments(Element xmlele, NLayer layer) {
	NodeIterator nodeit=null;
	try {
	    nodeit = XPathAPI.selectNodeIterator(xmlele, NiteMetaConstants.argumentxpath);
	} catch (TransformerException ex) {
	    ex.printStackTrace();
	}
	Element e;
	while ((e=(Element)nodeit.nextNode())!= null)  {
	    layer.addProgramArgument(e.getAttribute(NiteMetaConstants.value),
				  e.getAttribute(NiteMetaConstants.objectDefault));
	}
    }

    private ArrayList getElements(Element xmlele, Object container) {
	ArrayList eles=new ArrayList();
	NodeIterator nodeit=null;
	try {
	    nodeit = XPathAPI.selectNodeIterator(xmlele, NiteMetaConstants.elementxpath);
	} catch (TransformerException ex) {
	    ex.printStackTrace();
	}
	Element e;
	while ((e=(Element)nodeit.nextNode())!= null)  {
	    ArrayList attrs = getAttributes(e);
	    ArrayList points = getPointers(e);
	    NiteElement nele = new NiteElement(e.getAttribute(NiteMetaConstants.objectName), attrs, points, container, e.getAttribute(NiteMetaConstants.displayColour), e.getAttribute(NiteMetaConstants.textContent));
	    eles.add(nele);
	    elements.put((Object)e.getAttribute(NiteMetaConstants.objectName),
			 (Object)nele);
	    if (container instanceof NLayer && points!=null) {
		for (Iterator pit = points.iterator(); pit.hasNext(); ) {
		    NPointer np = (NPointer) pit.next();
		    storePointer((NLayer)container, np.getTarget());
		}
	    }
	}
	return eles;
    }

    /** retrieve information about all the valid attributes declared
     * under the given Element */
    private ArrayList getAttributes(Element xmlele) {
	ArrayList attrs=new ArrayList();
	NodeIterator nodeit=null;

	try {
	    nodeit = XPathAPI.selectNodeIterator(xmlele, NiteMetaConstants.attributexpath);
	} catch (TransformerException ex) {
	    ex.printStackTrace();
	}
	Element e;
	while ((e=(Element)nodeit.nextNode())!= null)  {
	    String atttype = e.getAttribute(NiteMetaConstants.valuetype);
	    ArrayList vals = getValues(e);
	    int attrtype = -1;
	    if (atttype.equals(NiteMetaConstants.string)) {
		attrtype=NAttribute.STRING_ATTRIBUTE;
	    } if (atttype.equals(NiteMetaConstants.number)) {
		attrtype=NAttribute.NUMBER_ATTRIBUTE;		
	    } else if (atttype.equals(NiteMetaConstants.enumerated)) {
		attrtype=NAttribute.ENUMERATED_ATTRIBUTE;
	    }
	    NiteAttribute nattr = new NiteAttribute(e.getAttribute(NiteMetaConstants.objectName), attrtype, vals);
	    attrs.add(nattr);
	}
	return attrs;
    }

    private ArrayList getPointers(Element xmlele) {
	ArrayList points=new ArrayList();
	NodeIterator nodeit=null;

	try {
	    nodeit = XPathAPI.selectNodeIterator(xmlele, NiteMetaConstants.pointerxpath);
	} catch (TransformerException ex) {
	    ex.printStackTrace();
	}
	Element e;
	while ((e=(Element)nodeit.nextNode())!= null)  {
	    NitePointer npoint = new NitePointer(e.getAttribute(NiteMetaConstants.objectRole), e.getAttribute(NiteMetaConstants.objectTarget), e.getAttribute(NiteMetaConstants.objectNumber));
	    points.add(npoint);
	}
	return points;
    }

    private ArrayList getValues(Element xmlele) {
	ArrayList vals=new ArrayList();
	NodeIterator nodeit=null;
	try {
	    nodeit = XPathAPI.selectNodeIterator(xmlele, NiteMetaConstants.valuexpath);
	} catch (TransformerException ex) {
	    ex.printStackTrace();
	}
	Element e;
	while ((e=(Element)nodeit.nextNode())!= null)  {
	    //	    String val = e.getNodeValue();
	    String val = getString(e);
	    vals.add(val);
	}
	return vals;
    }

    private String getString(Element xmlele) {
	String str=null;
	NodeIterator nodeit=null;
	try {
	    nodeit = XPathAPI.selectNodeIterator(xmlele, "text()");
	} catch (TransformerException ex) {
	    ex.printStackTrace();
	}
	Node e;
	while ((e=(Node)nodeit.nextNode())!= null)  {
	    if (str==null) { str = e.getNodeValue(); }
	    else { str = str + e.getNodeValue(); }
	}
	return str;
    }

    /** Find the element called "element_name". */ 
    public NElement getElementByName(String element_name) {
	if (elements==null) { return null; }
	return (NElement) elements.get((Object) element_name);
    }

    /** Find the layers of a given layer type. The type must be one of
        FEATURAL_LAYER, STRUCTURAL_LAYER or TIMED_LAYER (see "NLayer") */
    public List getLayersByType(int type) {
	ArrayList retlist=new ArrayList();
	if (layerhash==null) { return (List)retlist; }
	Enumeration layerkeys = layerhash.keys();
	while (layerkeys.hasMoreElements()) {
	    String lkey = (String) layerkeys.nextElement();
	    NiteLayer blayer = (NiteLayer)layerhash.get(lkey);
	    if (blayer.getLayerType()==type) {
		retlist.add((Object)blayer);
	    }
	}
	return (List)retlist;
    }

    /** change an attribute value on a user coding in the metadata. */
    public void setUserCodingAttribute(NObservation obs, NUserCoding nuc, 
				       String attname, String value) {
	try {
	    Element e =(Element)XPathAPI.selectSingleNode(doc, NiteMetaConstants.observationxpath + "[@" + NiteMetaConstants.objectName + "='" + obs.getShortName() + "']");
	    if (e==null) {
		System.err.println("Failed to find observation in document: " + obs.getShortName());
	    } else {
		String xpath = NiteMetaConstants.usercodingxpath + 
		    "[@" + NiteMetaConstants.objectName + "='" + nuc.getCodingName() + "'"; 
		if (nuc.getAgentName()!=null && !nuc.getAgentName().equals("")) {
		    xpath = xpath + "and @" + NiteMetaConstants.agent + "='" + nuc.getAgentName() + "'";
		}
		xpath = xpath + "]";
		Element e2=(Element)XPathAPI.selectSingleNode(e, xpath);
		if (e2==null) {
		    System.err.println("Failed to find user coding in observation " + obs.getShortName() + " called " + nuc.getCodingName() + ".");
		    Debug.print("Path: " + xpath, Debug.DEBUG);
		} else {
		    Debug.print("Found and set query: " + xpath, Debug.DEBUG);
		    e2.setAttribute(attname, value);
		}
	    }
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
    }

    /** find a metadata route between layers and return as a List of Codings */
    public List findPathBetween(NLayer top, NLayer bottom) {
	List path=null;
	if (top==null) { return path; }
	try {
	    NCoding topcod = (NCoding) top.getContainer();
	    NCoding botcod = null;
	    if (bottom!=null) { 
		botcod = (NCoding) bottom.getContainer();
	    }
	    path = new ArrayList();
	    path.add(topcod);
	    if (topcod==botcod) { 
		//Debug.print("SAME"); 
		return path; 
	    }
	    LinkedHashSet npath = new LinkedHashSet();
	    npath.add(topcod);
	    NLayer mlay = top.getChildLayer();
	    NLayer lastlay=null;
	    while (mlay!=null && mlay!=bottom) {
		if (mlay==lastlay) {
		    break;
		}
		npath.add((NCoding)mlay.getContainer());
		lastlay=mlay;
		mlay=mlay.getChildLayer();
	    }
	    if (mlay==bottom || (mlay==null && bottom==null)) {	
		//npath.add((NCoding)mlay.getContainer()); 
		return new ArrayList(npath); 
	    }
	} catch (ClassCastException cce) {
	    return path;
	}
	return path;
    }

    /** Keep more information than previously about pointers, so we
     * can find out what layers validly point to each other (this
     * helps in lazy loading) */
    protected void storePointer(NLayer lay1, String lay2) {
	if (lay1==null || lay2==null) { return; }
	Object ob = pointers_from.get(lay1);
	Set fro=null;
	if (ob==null) { fro = new HashSet(); }
	else { fro = (Set)ob; }
	fro.add(lay2);
	pointers_from.put(lay1, fro);

	ob = pointers_to.get(lay2);
	Set toe=null;
	if (ob==null) { toe = new HashSet(); }
	else { toe = (Set)ob; }
	toe.add(lay1);
	pointers_to.put(lay2, toe);
	//Debug.print("Store link between " + lay1.getName() + " and " + lay2); 
    }

    /** Keep more information than previously about pointers, so we
     * can find out what layers validly point to each other (this
     * helps in lazy loading) */
    protected void storePointer(NLayer lay1, NLayer lay2) {
	Object ob = pointers_from.get(lay1);
	Set fro=null;
	if (ob==null) { fro = new HashSet(); }
	else { fro = (Set)ob; }
	fro.add(lay2);
	pointers_from.put(lay1, fro);

	ob = pointers_to.get(lay2);
	Set toe=null;
	if (ob==null) { toe = new HashSet(); }
	else { toe = (Set)ob; }
	toe.add(lay2);
	pointers_to.put(lay1, toe);
    }

    /** get the Set of NLayers that the elements in this layer can
     * point to 
    public Set getValidPointersFrom (NLayer lay) {
	return (Set)pointers_from.get(lay);
    }
    */

    /** get the Set of NLayers that can point to the elements in this layer *
    public Set getValidPointersTo (NLayer lay) {
	return (Set)pointers_to.get(lay);
    }
    */

    /** get the Set of String NLayer names that the elements in this layer can
     * point to */
    public Set getValidPointersFrom (NLayer lay) {
	return (Set)pointers_from.get(lay);
    }

    /** get the Set of NLayers that can point to the elements in this named layer */
    public Set getValidPointersTo (String lay) {
	return (Set)pointers_to.get(lay);
    }

}
