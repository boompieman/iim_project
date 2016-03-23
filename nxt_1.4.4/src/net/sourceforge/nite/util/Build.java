/**
 * NITE XML Toolkit
 *
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.util;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Hashtable;
import java.util.regex.*;
import java.io.*;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.helpers.*;
import org.jdom.*;
import org.jdom.output.*;

import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;

/* Problem - I reluctantly admit that I should expand the regular
 * expressions in this program rather than letting ant do it. That's
 * because we potentially want an existence check for each
 * instantiation of each regular expression. For example if a we've
 * asked for a particular annotator in 'gold-standard' mode, but
 * there's no data from that annotator for this coding on a particular
 * observation, we want the real gold-standard version instead if one
 * exists. Got that? The program will be more complicated, and the
 * point of having an intermediate 'ant' stage is reduced... */

/** Build a view of a corpus using a build description file (a short
 * XML file listing the aspects of the corpus you are interested in).
 * The output is an 'ant' file that is does the zip file generation.*/
public class Build  {
    private static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
    /* Hard-wire the elements and attribute names in the build file */
    private static final String EL_ROOT="build";
    private static final String ATT_METADATA="metadata";
    private static final String ATT_NAME="name";
    private static final String ATT_DESCRIPTION="description";
    private static final String ATT_TYPE="type";
    private static final String ATTVAL_ON="on";
    private static final String ATTVAL_OFF="off";
    private static final String ATT_CORPUS_RESOURCES="corpus_resources";
    private static final String ATT_ONTOLOGIES="ontologies";
    private static final String ATT_OBJECT_SETS="object_sets";
    private static final String EL_DEFAULT="default-annotator";
    private static final String EL_CODING="coding-file";
    private static final String ATT_ANNOTATOR="annotator";
    private static final String ATT_RESOURCE="resource";
    private static final String EL_OBSERVATION="observation";
    private static final String ATTVAL_MULTI="multi-coder";
    private static final String ATTVAL_GOLD="gold";
    private static final String EL_EXTRA="extras";
    private static final String ATT_INCLUDES="includes";
    private static final String ATT_LOCALDIR="dir";
    private static final String ATT_ZIPPATH="path";

    private static final int CORPUS_RESOURCES=0;
    private static final int ONTOLOGIES=1;
    private static final int OBJECT_SETS=2;    

    // we need to save an altered copy of the metadata file.
    private String tempMetadataFile="metadata.xml";
    private XMLReader parser = null;
    private List observations=new ArrayList();
    private List codingFiles=new ArrayList();
    private List extraFiles=new ArrayList();
    private String description=null;
    private String metadataFile=null;
    private String buildName="build";
    private String buildType="multi-coder";
    private String defaultAnnotator=null;
    private NMetaData metadata=null;
    private NResourceData resourcedata=null;
    private boolean include_corpus_resources=true;
    private boolean include_ontologies=true;
    private boolean include_object_sets=true;
    private boolean saveresourcefile=false;

    private static void usage() {
	System.err.println("Usage: java Build <<build-spec>>");
	System.exit(0);
    }

    /** java Build <<build-spec>>.  */
    public static void main(String[] args) {
        String buildfile=null;
	
        if (args.length != 1) { usage(); }
	buildfile=args[0];
        if (buildfile == null) { usage(); }
	Build b = new Build(buildfile);
    }
    
    /** return the part of the path that is common to all (or null) */
    private String findCommonPath(List paths) {
	String cp = null;
	for (Iterator pit=paths.iterator(); pit.hasNext(); ) {
	    String pat = (String)pit.next();
	    File tf = new File(pat);
	    String tp = "";
	    try { 
		tp = tf.getCanonicalPath();
	    } catch (IOException ex) {
		if (pat.indexOf(File.separator)==0) {
		    tp = File.separator;
		}
	    }
	    if (cp==null) {
		cp=tp;
	    } else {
		String t = "";
		int max = cp.length();
		if (tp.length()<cp.length()) { max=tp.length(); }
		for (int i=0; i<max; i++) {
		    String ns = t+tp.charAt(i);
		    if (cp.indexOf(ns)==0) {
			t=ns;
		    } else {
			break;
		    }
		}
		cp=t;
	    }
	}
	return cp;
    }

    /** return the part of the path that is common to all (or null) */
    private String findCommonPathToSeparator(List paths) {
	String common_full = findCommonPath(paths);
	if (common_full==null) return null;
	int ind = common_full.lastIndexOf(File.separator);
	if (ind<0) {
	    System.err.println("No separator in " + common_full);
	    return null;
	}
	return common_full.substring(0,ind+1);
    }

    public Build(String buildfile) {
	try { 
	    parseBuild(buildfile);
	    //System.out.println("Loading metadata: " + metadataFile);
	    metadata = new NiteMetaData(metadataFile);
	    String metadir = new File(metadataFile).getParent();
	    resourcedata = metadata.getResourceData();
	    List pathList = new ArrayList();
	    pathList.add(metadataFile);
	    for (Iterator cit=codingFiles.iterator(); cit.hasNext(); ) {
		CodingFile cf = (CodingFile)cit.next();
		NCoding nc = metadata.getCodingByName(cf.getName());
		if (nc==null) {
		    System.err.println("Can't find coding '" + cf.getName() + "' in metadata. Ignoring");
		} else {
		    if (cf.getResource()!=null && resourcedata!=null) {
			NResource res = resourcedata.getResourceByID(cf.getResource());
			if (res!=null && res instanceof NiteResource) {
			    pathList.add(resourcedata.getResourcePath(res));
			    //System.out.println("Added path: " + resourcedata.getResourcePath(res));
			}
		    } else {
			pathList.add(nc.getPath());
			//System.out.println("Added no-resource path: " + nc.getPath());
		    }
		}
	    }
	    //String common = findCommonPath(pathList);
	    String common = findCommonPathToSeparator(pathList);
	    System.out.println("Common path is: " + common);
	    int len = 0;
	    if (common!=null) { len=common.length(); }
	    Hashtable codingsdone = new Hashtable();
	    for (Iterator cit=codingFiles.iterator(); cit.hasNext(); ) {
		CodingFile cf = (CodingFile)cit.next();
		NCoding nc = metadata.getCodingByName(cf.getName());
		if (nc==null) { continue; }
		System.out.println("Coding '" + cf.getName() + "' has path '" + nc.getPath() +  "'.");
		File f = new File(nc.getPath());
		String p = f.getCanonicalPath();
		if (cf.getResource()!=null && resourcedata!=null) {
		    NResource res = resourcedata.getResourceByID(cf.getResource());
		    if (res!=null && res instanceof NiteResource) {
			p = new File(resourcedata.getResourcePath(res)).getCanonicalPath();
			// this just causes the resource file to be saved..
			//((NiteResource)res).setPath(((NiteResource)res).getPath());
			saveresourcefile=true;
		    }
		}
		System.out.println("Path is " + p);
		cf.setLocalPath(p);
		if (codingsdone.get(nc)==null) {
		    String zp = p.substring(len, p.length());
		    codingsdone.put(nc,zp);
		    //nc.setPath(zp);    // set the path in the new metadata file..
		    cf.setZipPath(zp); // .. and store it in the CodingFile
		} else {
		    cf.setZipPath((String)codingsdone.get(nc));
		}
	    }
	    for (Enumeration coden=codingsdone.keys(); coden.hasMoreElements(); ) {
		NCoding nc = (NCoding)coden.nextElement();
		String path = (String)codingsdone.get(nc);
		nc.setPath(path);
	    }
	    // tempMetadataFile = buildName + "_" + tempMetadataFile;
	    tempMetadataFile = metadataFile.substring(metadataFile.lastIndexOf(File.separator)+1, metadataFile.length());
	    writeAntFile();
	    if (saveresourcefile) {
		metadata.setResourceFilename("./resource.xml");
	    }
	    metadata.writeMetaData("." + File.separator + tempMetadataFile);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /** just factor out some common stuff for corpus resources,
        ontologies etc.  The path to the corpus data will always be
        below the metadata and we have just decided to take the last
        part of the path in the original metadata file */
    private Element makeCorpusDataElement (int type, String localpath, String includes, String relpath) {
	Element zfs = new Element("zipfileset");
	// can't really use File.separator as we could have either on either platform.
	int chop=-1;
	if (relpath.lastIndexOf("/") >= 0) {
	    chop=relpath.lastIndexOf("/");
	} else if (relpath.lastIndexOf("\\") >= 0) {
	    chop=relpath.lastIndexOf("\\");	    
	}
	if (chop>=0) {
	    relpath = relpath.substring(chop+1, relpath.length());
	}
	if (relpath.indexOf(".")==0) {
	    relpath=".";
	}

	String forcepath=relpath;
	if (type==CORPUS_RESOURCES) {
	    metadata.setCorpusResourcePath(relpath);
	} else if (type==ONTOLOGIES) {
	    metadata.setOntologyPath(relpath);
	} else if (type==OBJECT_SETS) {
	    metadata.setObjectSetPath(relpath);
	}
	zfs.setAttribute("prefix", forcepath);
	zfs.setAttribute("dir", localpath);
	zfs.setAttribute("includes", includes);
	return zfs;
    }

    /** write the ant file. no, really? */
    private void writeAntFile() {
	Element rootElement = new Element("project");
	rootElement.setAttribute("name", buildName);
	rootElement.setAttribute("default", "zip");
	Document newDoc = new Document(rootElement);
	Element target = new Element("target");
	target.setAttribute("name", "zip");
	target.setAttribute("description", "produces zip file");
	rootElement.addContent(target);
	Element zip = new Element("zip");
	zip.setAttribute("destfile", "data.zip");
	target.addContent(zip);
	// metadata
	Element zfs = new Element("zipfileset");
	zfs.setAttribute("prefix", ".");
	zfs.setAttribute("dir", ".");
	String incs = tempMetadataFile;
	if (saveresourcefile) {
	    incs += ",resource.xml";
	}
	zfs.setAttribute("includes", incs);
	zip.addContent(zfs);

	// do the 'extras' first
	for (Iterator exit=extraFiles.iterator(); exit.hasNext(); ) {
	    ExtraFile ef = (ExtraFile) exit.next();
	    zfs = new Element("zipfileset");
	    zfs.setAttribute("prefix", ef.getLocalPath());
	    zfs.setAttribute("dir", ef.getZipPath());
	    zfs.setAttribute("includes", ef.getIncludes());
	    zip.addContent(zfs);	    
	}

	// now do the 'background' data: corpus resources; ontologies; object sets
	// first get the path to the metadata... 
	// then keep the relative bit
	File mf = new File(metadataFile);
	String mfp = mf.getParent();
	
	
	if (include_corpus_resources && metadata.getRelativeCorpusResourcePath()!=null) {
	    try {
		File lf = new File(mfp + File.separator + metadata.getRelativeCorpusResourcePath());
		String lp = lf.getCanonicalPath();
		zfs = makeCorpusDataElement(CORPUS_RESOURCES, lp, "*.xml", metadata.getRelativeCorpusResourcePath());
		zip.addContent(zfs);	    
	    } catch (java.io.IOException ex) {
		System.err.println("Failed to output corpus resource spec due to path error!");
	    }
	}

	if (include_ontologies && metadata.getRelativeOntologyPath()!=null) {
	    try {
		File lf = new File(mfp + File.separator + metadata.getRelativeOntologyPath());
		String lp = lf.getCanonicalPath();
		zfs = makeCorpusDataElement(ONTOLOGIES, lp, "*.xml", metadata.getRelativeOntologyPath());
		zip.addContent(zfs);	    
	    } catch (java.io.IOException ex) {
		System.err.println("Failed to output ontology spec due to path error!");
	    }
	}

	if (include_object_sets && metadata.getRelativeObjectSetPath()!=null) {
	    try {
		File lf = new File(mfp + File.separator + metadata.getRelativeObjectSetPath());
		String lp = lf.getCanonicalPath();
		zfs = makeCorpusDataElement(OBJECT_SETS, lp, "*.xml", metadata.getRelativeObjectSetPath());
		zip.addContent(zfs);	    
	    } catch (java.io.IOException ex) {
		System.err.println("Failed to output object set spec due to path error!");
	    }
	}
	
	for (Iterator cfit=codingFiles.iterator(); cfit.hasNext(); ) {
	    zfs = new Element("zipfileset");
	    CodingFile cf = (CodingFile) cfit.next(); 

	    String localpath = cf.getLocalPath();
	    String zippath = cf.getZipPath();
	    if (zippath==null || localpath==null) { 
		System.err.println("ERROR: Can't find path for coding " + cf.getName() + " (" + localpath + ", " + zippath + "). ");
		continue; 
	    }
	    // if gold and no annotator, only take the files in the
	    // main dir (no changes).

	    // if 'gold' add the cf.getAnnotator() to the local path.
	    // We should probably check for each expansion of the
	    // regular expression, whether the subdir exists...
	    if (buildType.equalsIgnoreCase(ATTVAL_GOLD) && cf.getAnnotator()!=null) {
		localpath += File.separator + cf.getAnnotator();
	    }

            // if 'multi' and an annotator exists, only that annotator
            // is included (add to local *and* zip paths). Again we
            // should check existence, but that requires evaluating
            // the regular expressions.
	    if (buildType.equalsIgnoreCase(ATTVAL_MULTI) && cf.getAnnotator()!=null) {
		localpath += File.separator + cf.getAnnotator();
		zippath += File.separator + cf.getAnnotator();
	    }

	    boolean subdirIncludes = false;

	    // if multi and no annotator, just recursively include all
	    // directories (no dir extension on either)
	    if (buildType.equalsIgnoreCase(ATTVAL_MULTI) && cf.getAnnotator()==null) {
		subdirIncludes=true;

	    }

	    //System.err.println("Zip path: " + zippath + "; localpath: " + localpath);

	    String includes="";
	    zfs.setAttribute("prefix", zippath);
	    zfs.setAttribute("dir", localpath);
	    for (Iterator oit=observations.iterator(); oit.hasNext(); ) {
		Observation o = (Observation)oit.next();
		if (includes.length()>0) { includes += ", "; }
		String inc = o.getName() + "." + cf.getName() + ".xml";
		includes += inc;
		if (subdirIncludes) {
		    includes += ", */" + inc;
		}	    
	    }
	    zfs.setAttribute("includes", includes);
	    zip.addContent(zfs);
	}
	    

	XMLOutputter outputter = new XMLOutputter();

	// modern jdom jars have Format as a separate class - the one
	// we're currently using has it as an internal class of
	// XMLOutputter so not sure if we can use it.
	//outputter.setFormat(XMLOutputter$Format.getPrettyFormat());
	try {
	    String nfn = "ant_" + buildName + ".xml";
	    System.out.println("Writing ant file " + nfn + ". use 'ant -f " + nfn + "' to create zip file"); 
	    outputter.output(newDoc, new FileOutputStream(new File(nfn)));
	}
	catch (IOException e) {
	    System.err.println(e);
	}
    }

    /** parse the build spec */
    protected void parseBuild(String filename) throws IOException {
	System.out.println("Loading data from file: " + filename);
	try{
	    parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
            parser.setContentHandler(new MyContentHandler());
	} catch (Exception pce) {
	    pce.printStackTrace();
	}
	try {
	    parser.parse( new InputSource( new FileInputStream(filename) ) );
	} catch (SAXException saxex) {
	    saxex.printStackTrace();
	}
    }


    /* this class just makes the SAX handler more separate. */
    class MyContentHandler extends DefaultHandler {

	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	    used internally. Start of a new XML document. */
	public void startDocument() {

	}
	

	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	    used internally. Signals the start of a parsed XML element. */
	public void startElement(String uri, String localName,
				 String qName, Attributes attributes) {
	    
	    if (qName.equals(EL_ROOT)) {
		String b=attributes.getValue(ATT_NAME);
		description=attributes.getValue(ATT_DESCRIPTION);
		metadataFile=attributes.getValue(ATT_METADATA);
		if (b!=null && b.length()!=0) {
		    buildName=b;
		    //System.out.println("Set build name: " + buildName);
		}
		String typ=attributes.getValue(ATT_TYPE);
		if (typ!=null && typ.length()>0) {
		    if (typ.equals(ATTVAL_MULTI) || typ.equals(ATTVAL_GOLD)) {
			buildType=typ;
		    } else {
			System.err.println("INVALID BUILD TYPE! Defaulting to " + buildType);
		    }
		}

		String cr=attributes.getValue(ATT_CORPUS_RESOURCES);
		if (cr!=null && cr.length()>0) {
		    if (cr.equals(ATTVAL_OFF)) {
			include_corpus_resources=false;
		    } else if (!cr.equals(ATTVAL_ON)) {
			System.err.println("INVALID CORPUS RESOURCE PARAMETER! Defaulting to 'on' (should be '" + ATTVAL_OFF + "' or '" + ATTVAL_ON + "').");			
		    }
		}

		cr=attributes.getValue(ATT_OBJECT_SETS);
		if (cr!=null && cr.length()>0) {
		    if (cr.equals(ATTVAL_OFF)) {
			include_object_sets=false;
		    } else if (!cr.equals(ATTVAL_ON)) {
			System.err.println("INVALID OBJECT SET PARAMETER! Defaulting to 'on' (should be '" + ATTVAL_OFF + "' or '" + ATTVAL_ON + "'.");			
		    }
		}

		cr=attributes.getValue(ATT_ONTOLOGIES);
		if (cr!=null && cr.length()>0) {
		    if (cr.equals(ATTVAL_OFF)) {
			include_ontologies=false;
		    } else if (!cr.equals(ATTVAL_ON)) {
			System.err.println("INVALID ONTOLOGY PARAMETER! Defaulting to 'on' (should be '" + ATTVAL_OFF + "' or '" + ATTVAL_ON + "'.");			
		    }
		}

	    } else if (qName.equals(EL_OBSERVATION)) {
		String b=attributes.getValue(ATT_NAME);
		if (b!=null && b.length()!=0) {
		    observations.add(new Observation(b));
		}
	    } else if (qName.equals(EL_CODING)) {
		String b=attributes.getValue(ATT_NAME);
		if (b!=null && b.length()!=0) {
		    codingFiles.add(new CodingFile(b, attributes.getValue(ATT_ANNOTATOR),
						   attributes.getValue(ATT_RESOURCE)));
		}		
	    } else if (qName.equals(EL_EXTRA)) {
		extraFiles.add(new ExtraFile(attributes.getValue(ATT_LOCALDIR),
					     attributes.getValue(ATT_ZIPPATH),
					     attributes.getValue(ATT_INCLUDES)));
	    } else if (qName.equals(EL_DEFAULT)) {
		String b=attributes.getValue(ATT_NAME);
		if (b!=null && b.length()!=0) {
		    defaultAnnotator=b;
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
	 * in the appropriate place.  */
	public void characters(char[] ch, int start, int length) {
	    String content = new String(ch, start, length);
	}
	
	/** part of the org.xml.sax.helpers.DefaultHandler interface -
	    used internally */
	public void endElement(String uri, String localName, String qName) {

	}
	
    }


    /** an individual observation (or a regular expression) in the
     * build spec */
    class Observation {
	String name = null;

	public Observation(String s) {
	    name = s;
	}
	
	public String getName() {
	    return name;
	}
    }

    /** an individual coding-file in the build spec */
    class CodingFile {
	String name = null;
	String annotator = null;
	String resource = null;
	String zipPath = null;
	String localPath = null;

	public CodingFile(String n, String ann, String resource) {
	    name = n;
	    if (ann!=null && ann.length()!=0) {
		annotator=ann;
	    } else if (defaultAnnotator!=null) {
		annotator=defaultAnnotator;
	    }
	    if (resource!=null && resource.length()!=0) {
		this.resource=resource;
	    }
	}
	
	public String getName() {
	    return name;
	}

	public String getAnnotator() {
	    return annotator;
	}

	public String getResource() {
	    return resource;
	}

	public void setZipPath(String pat) {
	    zipPath=pat;
	}

	public String getZipPath() {
	    return zipPath;
	}

	public void setLocalPath(String pat) {
	    localPath=pat;
	}

	public String getLocalPath() {
	    return localPath;
	}
    }

    /** an 'extra' in the build spec */
    class ExtraFile {
	String zipPath = null;
	String localPath = null;
	String includes = null;

	public ExtraFile(String p, String lp, String inc) {
	    localPath=lp;
	    zipPath=p;
	    includes=inc;
	}
	
	public String getIncludes() {
	    return includes;
	}

	public void setZipPath(String pat) {
	    zipPath=pat;
	}

	public String getZipPath() {
	    return zipPath;
	}

	public void setLocalPath(String pat) {
	    localPath=pat;
	}

	public String getLocalPath() {
	    return localPath;
	}
    }
}
