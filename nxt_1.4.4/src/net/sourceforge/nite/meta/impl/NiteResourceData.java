/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2007, Jean Carletta, Jonathan Kilgour
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Hashtable;
import java.net.URL;
import java.net.MalformedURLException;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.helpers.*;

import net.sourceforge.nite.util.Debug;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.NiteMetaException;
import net.sourceforge.nite.util.XMLutils;

/**
 * Instantiates NResourceData which handles any resource file associated
 * with a corpus: if present this must define all the information
 * about where on disk codings live, and what competing versions of
 * annotations exist.
 *
 * @author jonathan 
 */
public class NiteResourceData implements net.sourceforge.nite.meta.NResourceData, LexicalHandler {
    private NMetaData metadata;
    private String resource_filename; 
    private boolean url=false;
    private URL resource_url=null;
    private File resource_file=null;
    private String resource_path=null;
    private String original_resource_path=null;
    private XMLReader parser = null;
    protected Hashtable resourcetypes=new Hashtable();
    protected Hashtable resources=new Hashtable();
    protected Hashtable incompatibleResources=new Hashtable();

    protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
    protected static final String LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";
    protected static final String NAMESPACE_PREFIXES_FEATURE_ID = "http://xml.org/sax/features/namespace-prefixes";
    private final static int START=0;
    private final static int END=1;
    private final static int STARTANDEND=2;    

    /** the NiteResourceData constructor takes its parent metadata and its
     * filename. If the filename is relative, it's relative to the
     * metadata, not the working directory. */
    public NiteResourceData(NMetaData meta, String filename) throws NiteMetaException {
	this.metadata=meta;
	setFilename(filename);
	readResourceFile();
    }

    /**
     * Read the resource document into memory.
     */
    private void readResourceFile() throws NiteMetaException {
	String filename = resource_path + File.separator + resource_filename;
	Debug.print("Loading RESOURCE file: " + filename, Debug.IMPORTANT);

	try{
	    parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
	    parser.setFeature(NAMESPACES_FEATURE_ID, true);
	    parser.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, true);
	    parser.setProperty(LEXICAL_HANDLER, this);
            parser.setContentHandler(new MyContentHandler(this));
	} catch (Exception pce) {
	    pce.printStackTrace();
	    throw new NiteMetaException("Error loading RESOURCE file: " + filename);
	}
	try {
	    parser.parse( new InputSource( new FileInputStream(filename) ) );
	} catch (SAXException saxex) {
	    throw new NiteMetaException("SAX Exception (XML Error) loading RESOURCE file: " + filename);
	} catch (IOException ioe) {
	    // perhaps it's a URL...
	    try { 
		parser.parse(new InputSource((new URL(filename)).openStream()));
	    } catch (MalformedURLException muex) {
		throw new NiteMetaException("IO Exception loading RESOURCE URL: " + filename);
	    } catch (SAXException saxex) {
		throw new NiteMetaException("SAX Exception (XML Error) loading RESOURCE URL: " + filename);
	    } catch (IOException ie2) {
		throw new NiteMetaException("IO Exception loading RESOURCE URL: " + filename);
	    }
	}
    }

    /** write an XML element with the given attributes */
    private void writeXMLElement(OutputStreamWriter out, String el, List atts, int indent,				 int eltype) throws java.io.IOException {
	for (int i=0; i<indent; i++) { out.write("  ");	}
	out.write("<");
	if (eltype==END) { out.write("/"); }
	out.write(el);
	if (atts!=null && eltype!=END) {
	    // depend on the attributes just being in pairs!
	    for (Iterator atit=atts.iterator(); atit.hasNext(); ) {
		out.write(" "+atit.next()+"="+XMLutils.escapeAttributeValue((String)atit.next()));
	    }
	}
	if (eltype==STARTANDEND) { out.write("/"); }
	out.write(">\n");
    }   

    /** write a group of dependencies */
    private void writeDependencies(OutputStreamWriter out, List deps) throws java.io.IOException {
	if (deps==null) { return; }
	for (Iterator dit=deps.iterator(); dit.hasNext(); ) {
	    NResourceDependency dep = (NResourceDependency)dit.next();
	    List arr = new ArrayList();
	    arr.add(NiteMetaConstants.dependencyObservationAttr); arr.add(dep.getObservationRegexp());
	    arr.add(NiteMetaConstants.dependencyIdrefAttr); arr.add(dep.getResourceID());
	    writeXMLElement(out, NiteMetaConstants.dependency, arr, 3, STARTANDEND);
	}
    }

    /** find a list of attributes for an NResource (virtual or real) */
    private List getCommonResourceAttributes(NResource res) {
	List arr = new ArrayList();
	arr.add(NiteMetaConstants.resourceIDAttr); arr.add(res.getID());
	arr.add(NiteMetaConstants.resourceIncompatibleAttr); arr.add(res.getIncompatibleID());
	if (res.isDefault()) {
	    arr.add(NiteMetaConstants.resourceDefaultAttr); arr.add("true");
	}
	return arr;
    }

    /** write a single virtual resource */
    private void writeVirtualResource(OutputStreamWriter out, NVirtualResource vres) throws java.io.IOException {
	if (vres==null) { return; }
	List arr = getCommonResourceAttributes(vres);
	writeXMLElement(out, NiteMetaConstants.virtualResource, arr, 2, START);
	writeDependencies(out, vres.getDependencies());
	writeXMLElement(out, NiteMetaConstants.virtualResource, null, 2, END);
    }

    /** write a single real resource */
    private void writeRealResource(OutputStreamWriter out, NRealResource rres) throws java.io.IOException {
	if (rres==null) { return; }
	List arr = getCommonResourceAttributes(rres);
	String description=rres.getDescription();
	if (description!=null) {
	    arr.add(NiteMetaConstants.resourceDescriptionAttr); arr.add(description);
	}
	String rtype = NiteMetaConstants.resourceTypeManual;
	if (rres.getType()==NRealResource.AUTOMATIC) { 
	    rtype = NiteMetaConstants.resourceTypeAutomatic;
	}
	arr.add(NiteMetaConstants.resourceTypeAttr); arr.add(rtype);
	String ann=rres.getAnnotator();
	if (ann!=null) { 
	    arr.add(NiteMetaConstants.resourceAnnotatorAttr); arr.add(ann);
	}
	String path=rres.getPath();
	if (path!=null) { 
	    arr.add(NiteMetaConstants.resourcePathAttr); arr.add(path);
	}
	String cov=rres.getCoverage();
	if (cov!=null) { 
	    arr.add(NiteMetaConstants.resourceCoverageAttr); arr.add(cov);
	}
	String resp=rres.getResponsible();
	if (resp!=null) { 
	    arr.add(NiteMetaConstants.resourceResponsibleAttr); arr.add(resp);
	}
	String qual=rres.getQuality();
	if (qual!=null) { 
	    arr.add(NiteMetaConstants.resourceQualityAttr); arr.add(qual);
	}
	String manu=rres.getCodingManualReference();
	if (manu!=null) { 
	    arr.add(NiteMetaConstants.resourceManualAttr); arr.add(manu);
	}
	writeXMLElement(out, NiteMetaConstants.resource, arr, 2, START);
	writeDependencies(out, rres.getDependencies());
	writeXMLElement(out, NiteMetaConstants.resource, null, 2, END);
    }

    /** write a single group of resources */
    private void writeResourceGroup(OutputStreamWriter out, String rgcoding, 
				    NResourceGroup group) throws java.io.IOException {
	if (group==null) { return; }
	List arr = new ArrayList();
	arr.add(NiteMetaConstants.resourceTypeCodingAttr); arr.add(rgcoding);
	if (group.getPath()!=null) {
	    arr.add(NiteMetaConstants.resourceTypePathAttr); arr.add(group.getPath());
	}
	writeXMLElement(out, NiteMetaConstants.resourceType, arr, 1, START);
	for (Iterator vit=group.getVirtualResources().iterator(); vit.hasNext(); ) {
	    writeVirtualResource(out, (NVirtualResource)vit.next());
	}
	for (Iterator rit=group.getRealResources().iterator(); rit.hasNext(); ) {
	    writeRealResource(out, (NRealResource)rit.next());
	}
	writeXMLElement(out, NiteMetaConstants.resourceType, null, 1, END);
    }

    /** save the resource file to the current filename (by default the file
        it was created from, or set using setFilename). */
    public void writeResourceFile() {
	try {
	    String rf = decidePath(original_resource_path) + File.separator + resource_filename;
	    Debug.print("Saving: " + rf, Debug.IMPORTANT);
	    OutputStream fout= new FileOutputStream(rf);
	    OutputStream bout= new BufferedOutputStream(fout);
	    OutputStreamWriter out = new OutputStreamWriter(bout, "8859_1");
	    
	    out.write("<?xml version=\"1.0\" ");
	    out.write("encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n");  
	    writeXMLElement(out, NiteMetaConstants.resourceRoot, null, 0, START);
	    for (Enumeration rte=resourcetypes.keys(); rte.hasMoreElements(); ) {
		String rcode = (String)rte.nextElement();
		NResourceGroup nrg = (NResourceGroup)resourcetypes.get(rcode);
		writeResourceGroup(out,rcode,nrg);
	    }
	    writeXMLElement(out, NiteMetaConstants.resourceRoot, null, 0, END);
	    out.flush();
	    out.close();
	    bout.close();
	    fout.close();
	} catch (Exception ex) {
	    Debug.print("ERROR writing resource file '" + resource_filename + "'. See stack trace for details.");
	    ex.printStackTrace();
	}
    }

    /** save the metadata to a file with the given filename. */
    public void writeResourceFile(String filename) {
	setFilename(filename);
	writeResourceFile();
    }

    /** set the resource filename for any future save: relative paths
        are assumed to be relative to the metadata directory. NOTE:
        This does NOT update the resource file name saved in the
        metadata file: you shoud really use
        NMetaData.setResourceFilename. */
    public void setFilename(String filename) {
	if (filename==null) { return; }
	String rp = "";
	if (filename.lastIndexOf(File.separator)>=0) {
	    rp=filename.substring(0,filename.lastIndexOf(File.separator));
	    resource_filename = filename.substring(filename.lastIndexOf(File.separator), filename.length());
	} else { 
	    resource_filename=filename;
	}
	resource_path = decidePath(rp);

	// keep this so it's easy to recalculate - if we're relative
	// to the metadata path and that changes - we need to change
	// too
	original_resource_path=rp; 
    }

    /** Return the path to the resource file (absolute) */
    public String getResourceFilePath() {
	return resource_path;
    }
    
    /** resolve IDs/IDREFs in resources - we have finished reading the
     * resource file */
    protected void finalizeResources() {
	for (Enumeration e = resources.keys(); e.hasMoreElements(); ) {
	    NResource res=(NResource)resources.get(e.nextElement());
	    String incid = res.getIncompatibleID();
	    if (incid!=null) {
		NResource res2 = (NResource)resources.get(incid);
		if (res2!=null) {
		    List inres = (List)incompatibleResources.get(res);
		    List inres2 = (List)incompatibleResources.get(res2);
		    if (inres==null) { inres=new ArrayList(); }
		    if (inres2==null) { inres2=new ArrayList(); }
		    inres.add(res2);
		    inres2.add(res);
		    incompatibleResources.put(res, inres);
		    incompatibleResources.put(res2, inres2);
		}
	    }
	    resolveDependencies(res.getDependencies());
	}
    }

    /** grab the actual resources from the dependencies */
    private void resolveDependencies(List unresolved) {
	try {
	    for (Iterator lit=unresolved.iterator(); lit.hasNext(); ) {
		NiteResourceDependency nrd = (NiteResourceDependency)lit.next();
		if (nrd==null) { 
		    Debug.print("Failed to resolve resource. ", Debug.ERROR);
		} else if (nrd.getResource()==null) {
		    nrd.resource=(NResource)resources.get(nrd.getResourceID());
		    if (nrd.resource==null) {
			Debug.print("Failed to resolve resource ID: " + nrd.getResourceID() + ".", Debug.ERROR);
		    }
		}
	    }
	} catch (Exception ex) {
	    Debug.print("Unexpected error resolving resource file dependencies: see stack trace. File loading will probably be affected.", Debug.ERROR);
	    ex.printStackTrace();
	}
    }

    /** find a resource from its ID */
    public NResource getResourceByID(String rid) {
	return (NResource)resources.get(rid);
    }
    
    /** find a list of NRealResource elements that instantiate the named
     * coding. If there are zero, return null. */
    public List getResourcesForCoding(String coding) {
	NResourceGroup bng = (NResourceGroup) resourcetypes.get(coding);
	if (bng==null) { return null; }
	return bng.getRealResources();
    }

    /** find a NResourceGroup elements that instantiate the named
     * coding. If there are zero, return null. */
    public NResourceGroup getResourceGroupForCoding(String coding) {
	return (NResourceGroup) resourcetypes.get(coding);
    }

    /** add a NResourceGroup element for resources that instantiate the named
     * coding.  */
    public void addResourceGroup(NResourceGroup nrg) {
	if (nrg==null || nrg.getCoding()==null) {
	    System.err.println("Failed to add resource group " + nrg);
	}
	resourcetypes.put(nrg.getCoding(), nrg);
    }

    /** find a list of NResource elements that instantiate the named
     * coding and have their default attribute set to true. If there
     * are zero, return null. */
    public List getDefaultedResourcesForCoding(String coding) {
	NResourceGroup bng = (NResourceGroup) resourcetypes.get(coding);
	if (bng==null) { return null; }
	return bng.getDefaultedResources();
    }

    /** find a list of NVirtualResource elements that instantiate the named coding */
    public List getVirtualResourcesForCoding(String coding) {
	NResourceGroup bng = (NResourceGroup) resourcetypes.get(coding);
	if (bng==null) { return null; }
	return bng.getVirtualResources();
    }

    /** Return the full list of real resources for a coding, except
     * that if there are virtual resources, add them to the list and
     * remove any resources they draw from. Return null if none can be
     * found. */
    public List getCoherentResourceGroups(String coding) {
	return null;
    }

    /** Return the full list of real resources, except that if there
     * are virtual resources, add them to the list and remove any
     * resources they draw from. Return null if none can be found. */
    public List getCoherentResourceGroups(String coding, String observation) {
	return null;
    }

    /** delete resource given its ID */
    public void deleteResource(String resourceid) {
	NResource nr = (NResource)resources.get(resourceid);
	if (nr==null) {
	    Debug.print("Resource with ID '"+resourceid+"' can't be deleted - it doesn't exist", Debug.ERROR); 
	} else {
	    NResourceGroup nrg = (NResourceGroup)resourcetypes.get(nr.getCoding());
	    nrg.getRealResources().remove(nr);
	    nrg.getVirtualResources().remove(nr);
	}
	resources.remove(nr);
    }

    /** add a new resource */
    public void addResource(String coding, NResource resource) {
	NResourceGroup bng = (NResourceGroup) resourcetypes.get(coding);
	if (bng==null) { bng=new NiteResourceGroup(coding, ""); } 
	if (resource instanceof NVirtualResource) { bng.addVirtualResource((NVirtualResource)resource); }
	else { bng.addResource(resource); }
	if (resource.isDefault()) {
	    bng.addDefaultedResource(resource);
	}
	resources.put(resource.getID(), resource);
    }

    /** find the path to the resource: each resource has one location
     * on disk which can be affected by: the location of the resource file
     * (if paths are relative); the 'path' attribute of the
     * resource-type element in the resource file; the 'path'
     * attribute of the 'resource' element */
    public String getResourcePath(String resourceid) {
	if (resourceid==null) { return null; }
	return getResourcePath((NResource)resources.get(resourceid));
    }

    /** find the path to the resource: each resource has one location
     * on disk which can be affected by: the location of the resource file
     * (if paths are relative); the 'path' attribute of the
     * resource-type element in the resource file; the 'path'
     * attribute of the 'resource' element */
    public String getResourcePath(NResource resource) {
	try {
	    String mainpath = resource_path;
	    String resourcetypepath = resource.getResourceGroup().getPath();
	    String resourcepath=null;
	    if (resource instanceof NRealResource) {
		resourcepath=((NRealResource)resource).getPath();
	    }
	    // most specific is an absolute path - return it
	    if (URLorAbsolute(resourcepath)) { return resourcepath; }
	    String path = resourcetypepath;
	    if (resourcepath!=null) { 
		if (path==null) { path=resourcepath; } 
		else { path += File.separator + resourcepath; }
	    }
	    // second-most specific is an absolute path - return it
	    if (URLorAbsolute(path)) { return path; }
	    if (path!=null) { mainpath += File.separator + path; }
	    return mainpath;
	} catch (NullPointerException nex) {
	    Debug.print("Failed to find path for resource "+resource+".", Debug.ERROR);
	    nex.printStackTrace();
	    return "";
	}
    }

    /** return true if the string represents an absolute file /
     * directory name or a valid URL */
    protected boolean URLorAbsolute(String path) {
	if (path==null) { return false; }
	try {
	    URL u = new URL(path);
	    return true;
	} catch (MalformedURLException muex) { }
	if (new File(path).isAbsolute()) {
	    return true;
	}
	return false; 
    }
    
    protected String decidePath(String relpath) {
	boolean url=false;
	String properpath="";
	String parpath = metadata.getPath();
	if (relpath==null) { return properpath; }
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
	if (properpath==null || properpath.equals("")) { properpath="."; }
	return properpath;
    }

    /*----------------------------------------------------------------------------*/
    /* This private internal class contains the methods to handle the
     * SAX events that allow us to build the Resource DB */
    /*----------------------------------------------------------------------------*/

    private class MyContentHandler extends DefaultHandler {
	String resourceTypeCoding=null;
	String resourceTypePath=null;
	String resourceTypeDescription=null;
	NiteResourceData nrd=null;
	NResourceGroup ng=null;
	Object res=null;

	public MyContentHandler (NiteResourceData nrd) {
	    this.nrd=nrd;
	}

	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	    used internally. Start of a new XML document. */
	public void startDocument() {

	}

	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	    used internally. End of XML document. */
	public void endDocument() {
	    finalizeResources();
	}
	
	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	    used internally. Signals the start of a parsed XML element. */
	public void startElement(String uri, String localName,
				 String qName, Attributes attributes) {
	    if (qName.equalsIgnoreCase(NiteMetaConstants.resourceType)) {
		resourceTypeCoding=attributes.getValue(NiteMetaConstants.resourceTypeCodingAttr);
		resourceTypePath=attributes.getValue(NiteMetaConstants.resourceTypePathAttr);
		resourceTypeDescription=attributes.getValue(NiteMetaConstants.resourceTypeDescriptionAttr);
		ng=new NiteResourceGroup(resourceTypeCoding, resourceTypePath, resourceTypeDescription);
		NResourceGroup bng = (NResourceGroup) resourcetypes.get(resourceTypeCoding);
		if (bng!=null) {
		    Debug.print("ERROR IN RESOURCE FILE: Multiple resource type elements instantiate the same coding: '"+
				resourceTypeCoding+
				"'. Please amalgamate the groups; otherwise only the last will be visible.", Debug.ERROR);
		}
		resourcetypes.put(resourceTypeCoding, ng);
	    } else if (qName.equalsIgnoreCase(NiteMetaConstants.resource)) {
		if (resourceTypeCoding==null || ng==null) {
		    Debug.print("ERROR IN RESOURCE FILE: Resource '"+
				attributes.getValue(NiteMetaConstants.resourceIDAttr)+
				"' is not in any resourceType. IGNORED.", Debug.ERROR);
		} else {
		    try {
			NiteResource nr = new NiteResource(ng, 
			    attributes.getValue(NiteMetaConstants.resourceIDAttr),
			    attributes.getValue(NiteMetaConstants.resourceDescriptionAttr),
			    attributes.getValue(NiteMetaConstants.resourceTypeAttr),
			    attributes.getValue(NiteMetaConstants.resourceAnnotatorAttr),
			    attributes.getValue(NiteMetaConstants.resourcePathAttr),
			    attributes.getValue(NiteMetaConstants.resourceDefaultAttr));
			nr.setCoverage(attributes.getValue(NiteMetaConstants.resourceCoverageAttr));
			nr.setQuality(attributes.getValue(NiteMetaConstants.resourceQualityAttr));
			nr.setResponsible(attributes.getValue(NiteMetaConstants.resourceResponsibleAttr));
			nr.setCodingManualReference(attributes.getValue(NiteMetaConstants.resourceManualAttr));
			nr.setLastEdit(attributes.getValue(NiteMetaConstants.resourceLastEditAttr));
			nr.setIncompatibleID(attributes.getValue(NiteMetaConstants.resourceIncompatibleAttr));

			ng.addResource(nr);
			resources.put(nr.getID(), nr);
			if (nr.isDefault()) { ng.addDefaultedResource(nr); }
			res=nr;
		    } catch (NiteMetaException nme) {
			nme.printStackTrace();
		    }
		}
	    } else if (qName.equalsIgnoreCase(NiteMetaConstants.virtualResource)) {
		if (resourceTypeCoding==null || ng==null) {
		    Debug.print("ERROR IN RESOURCE FILE: Virtual Resource '"+
				attributes.getValue(NiteMetaConstants.resourceIDAttr)+
				"' is not in any resourceType. IGNORED.", Debug.ERROR);
		} else {
		    String id = attributes.getValue(NiteMetaConstants.resourceIDAttr);
		    if (id==null) { 
			Debug.print("ERROR IN RESOURCE FILE: no ID for resource: '"+
				resourceTypeCoding+"'.", Debug.ERROR);
		    }
		    NiteVirtualResource nvr = new NiteVirtualResource(ng, id, attributes.getValue(NiteMetaConstants.resourceDefaultAttr));
		    nvr.setIncompatibleID(attributes.getValue(NiteMetaConstants.resourceIncompatibleAttr));
		    ng.addVirtualResource(nvr);
		    if (nvr.isDefault()) { ng.addDefaultedResource(nvr); }
		    resources.put(nvr.getID(), nvr);
		    res=nvr;
		}
	    } else if (qName.equalsIgnoreCase(NiteMetaConstants.dependency)) {
		if (res==null) {
		    Debug.print("ERROR IN RESOURCE FILE: Dependency '"+
				attributes.getValue(NiteMetaConstants.dependencyIdrefAttr)+
				"' is not within a resource. IGNORED.", Debug.ERROR);
		} else if (attributes.getValue(NiteMetaConstants.dependencyIdrefAttr)==null) {
		    Debug.print("ERROR IN RESOURCE FILE: Dependency has null '"+
				NiteMetaConstants.dependencyIdrefAttr+
				"' attribute. IGNORED.", Debug.ERROR);
		} else if (res instanceof NiteVirtualResource) {
			((NiteVirtualResource)res).addDependency(attributes.getValue(NiteMetaConstants.dependencyIdrefAttr), attributes.getValue(NiteMetaConstants.dependencyObservationAttr));
		} else if (res instanceof NiteResource) {
			((NiteResource)res).addDependency(attributes.getValue(NiteMetaConstants.dependencyIdrefAttr), attributes.getValue(NiteMetaConstants.dependencyObservationAttr));
		}
	    }
	}
	
	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	    used internally */
	public void ignorableWhitespace(char[] ch, int start, int length) {

	}

	
	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	 * used internally.  Extend the behaviour of DefaultHandler to
	 * allow textual content. No mixed content, so we just place text
	 * in a String field in the element.  */
	public void characters(char[] ch, int start, int length) {

	}
	
	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	    used internally */
	public void endElement(String uri, String localName, String qName) {
	    if (qName.equalsIgnoreCase(NiteMetaConstants.resourceType)) {
		resourceTypeCoding=null;
		resourceTypePath=null;
	    } else if (qName.equalsIgnoreCase(NiteMetaConstants.resource)) {
		res=null;
	    } else if (qName.equalsIgnoreCase(NiteMetaConstants.virtualResource)) {
		res=null;
	    }
	}
	
    }

    /** get a List of NResource elements that are incompatible with this one */
    public List getIncompatibleResources(NResource res) {
	return (List)incompatibleResources.get(res);
    }

    
    /*----------------------------------------------------------------------------*/
    /* These methods implement LexicalHandler. We do this just to store comments. */
    /*----------------------------------------------------------------------------*/
    /** Store comments (part of the LexicalHandler interface) */
    public void comment(char[] ch, int start, int length) {

    }

    /** part of the org.xml.sax.ext.LexicalHandler implementation -
        not for programming use */
    public void endCDATA() {}
    /** part of the org.xml.sax.ext.LexicalHandler implementation -
        not for programming use */
    public void endDTD() {}
    /** part of the org.xml.sax.ext.LexicalHandler implementation -
        not for programming use */
    public void endEntity(String name) {}
    /** part of the org.xml.sax.ext.LexicalHandler implementation -
        not for programming use */
    public void startCDATA() {}
    /** part of the org.xml.sax.ext.LexicalHandler implementation -
        not for programming use */
    public void startDTD(java.lang.String name, java.lang.String publicId,
			 java.lang.String systemId) {}
    /** part of the org.xml.sax.ext.LexicalHandler implementation -
        not for programming use */
    public void startEntity(java.lang.String name) {}

}
