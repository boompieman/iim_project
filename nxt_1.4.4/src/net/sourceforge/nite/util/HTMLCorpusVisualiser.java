package net.sourceforge.nite.util;

import net.sourceforge.nite.meta.*;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;

/** 
 * Produce a set of HTML files as a description of the provided corpus.
 *
 * @author Jonathan Kilgour, UEdin
 */
public class HTMLCorpusVisualiser implements CorpusVisualiser {
    NMetaData metadata;
    String outputdir;
    boolean showcontent=false;
    String indexfilename=null;

    /** main constructor - can be called from other programs this way. Valid parameters are
     * <ul>
     * <li>meta NMetaData - metadata file from existing corpus</li>
     * <li>outputdir String - name the output directory into which
     * results are written (may be null for GRAPHICAL mode
     * output).</li>
     * <li>showcontent boolean - if true (default is false) show, or
     * link to, actual corpus coverage data</li> </ul>
     */
    public HTMLCorpusVisualiser(NMetaData meta, String outputdir, boolean showcontent) {
	metadata=meta;
	this.outputdir=outputdir;
	this.showcontent=showcontent;
	System.err.println("OUTPUT DIRECTORY FOR CORPUS HELP IS " + outputdir);
    }
    

    /** perform visualisation and make ready for viewing */
    public void visualiseCorpus() {
	writeCSSFile(outputdir + File.separator + "corpus.css");
	indexfilename=outputdir + File.separator + "index.html";
	System.err.println("OUTPUT css to " + outputdir + File.separator + "corpus.css");
	System.err.println("OUTPUT html to " + outputdir + File.separator + "index.html");
	HTMLFile index = new HTMLFile(metadata.getCorpusDescription(), indexfilename);
	String indexlayerfilename=outputdir + File.separator + "index_layers.html";
	HTMLFile indexlayer = new HTMLFile(metadata.getCorpusDescription(), indexlayerfilename);
	indexlayer.addBodyText("<h2>Annotation Layers</h2><ul>\n");
	List codings = metadata.getCodings();
	List codingelements = new ArrayList();
	// gather reserved attributes for all elements (resource name only)
	String allextraatts=null;
	if (metadata.getResourceAttributeName()!=null) { 
	    allextraatts="<b>"+metadata.getResourceAttributeName()+"</b> (string): ID of the resource from which the element was loaded";
	}
	// add reserved attributes for annotation elements
	String annextraatts=null;
	if (metadata.getObservationAttributeName()!=null) {
	    if (allextraatts!=null) { annextraatts = allextraatts + "<br>\n"; }
	    else { annextraatts=""; }
	    annextraatts+="<b>"+metadata.getObservationAttributeName()+"</b> (string): ID of the observation the element belongs to";
	}
	if (metadata.getAgentAttributeName()!=null) {
	    if (annextraatts!=null) { annextraatts+="<br>\n"; }
	    else { annextraatts=""; }
	    annextraatts+="<b>"+metadata.getAgentAttributeName()+"</b> (string): ID of the agent the element belongs to";
	}
	
	if (codings==null) { index.addBodyText("<p><b>NO Annotations</b></p></ul>\n"); }
	else {
	    for (Iterator cit=codings.iterator(); cit.hasNext(); ) {
		NCoding coding=(NCoding)cit.next();
		if (!(coding instanceof NCorpusResourceCoding)) {
		    String type="Agent";
		    if (coding.getType()==NCoding.INTERACTION_CODING) { type="Interaction"; }
		    try {
			int first=1;
			for (Iterator lit=coding.getLayers().iterator(); lit.hasNext();) {
			    NLayer nl = (NLayer)lit.next();
			    String indtext = "<li>" + nl.getName() + " ("+ type + " layer) elements: \n";
			    for (Iterator elit=nl.getContentElements().iterator(); elit.hasNext(); ) {
				NElement el = (NElement)elit.next();
				writeAnnotationElementFile(coding, nl, el, NElement.CODING, annextraatts);
				codingelements.add(el.getName());
				if (first==1) { first=0; } else { indtext+=", "; }
				indtext+="<a href=\"annot_"+el.getName()+".html\">"+el.getName()+"</a>\n";
			    }
			    indtext += "</li>\n";
			    indexlayer.addBodyText(indtext);
			}
		    } catch (Exception ex) { }
		}
	    }

	}
	indexlayer.addBodyText("</ul>\n");
	indexlayer.writeFile();

	List corpuselements=new ArrayList();
	if (metadata.getCorpusResources()!=null) {
	    for (Iterator cit=metadata.getCorpusResources().iterator(); cit.hasNext(); ) {
		NCorpusResource coding=(NCorpusResource)cit.next();
		for (Iterator lit=coding.getLayers().iterator(); lit.hasNext();) {
		    NLayer nl = (NLayer)lit.next();
		    for (Iterator elit=nl.getContentElements().iterator(); elit.hasNext(); ) {
			NElement el = (NElement)elit.next();
			writeAnnotationElementFile(coding, nl, el, NElement.CORPUSRESOURCE,allextraatts);
			corpuselements.add(el.getName());
		    }
		}
	    }		
	}

	if (metadata.getOntologies()!=null) {
	    for (Iterator cit=metadata.getOntologies().iterator(); cit.hasNext(); ) {
		NOntology ontology=(NOntology)cit.next();
		writeOntologyFile(ontology,allextraatts);
		corpuselements.add(ontology.getElementName());
	    }		
	}

	if (metadata.getObjectSets()!=null) {
	    for (Iterator cit=metadata.getObjectSets().iterator(); cit.hasNext(); ) {
		NObjectSet objectset=(NObjectSet)cit.next();
		for (Iterator elit=objectset.getContentElements().iterator(); elit.hasNext(); ) {
		    NElement el = (NElement)elit.next();
		    writeAnnotationElementFile(objectset, null, el, NElement.OBJECTSET, allextraatts);
		    corpuselements.add(el.getName());
		}
	    }		
	}

	index.addBodyText("<dl><dt>Annotation elements</dt><dd>\n");
	Collections.sort(codingelements); // natural order i.e. alphabetical
	int first=1;
	for (Iterator elit=codingelements.iterator(); elit.hasNext(); ) {
	    if (first==1) { first=0; } else { index.addBodyText(", "); }
	    String elname = (String)elit.next();
	    index.addBodyText("<a href=\"annot_"+elname+".html\">"+elname+"</a>");
	}
	index.addBodyText("</dd></dl>\n");
	index.addBodyText("<dl><dt>Corpus-level elements</dt><dd>\n");
	Collections.sort(corpuselements); // natural order i.e. alphabetical
	first=1;
	for (Iterator elit=corpuselements.iterator(); elit.hasNext(); ) {
	    if (first==1) { first=0; } else { index.addBodyText(", "); }
	    String elname = (String)elit.next();
	    index.addBodyText("<a href=\"corpus_"+elname+".html\">"+elname+"</a>");
	}
	index.addBodyText("</dd></dl>\n");
	if (metadata.getAgents()!=null) {
	    index.addBodyText("<dl><dt>Agents</dt><dd>\n");
	    first=1;
	    for (Iterator ait=metadata.getAgents().iterator(); ait.hasNext(); ) {
		NAgent agent = (NAgent)ait.next();
		if (first==1) { first=0; } else { index.addBodyText(", "); }
		index.addBodyText(agent.getShortName());
	    }
	    index.addBodyText("</dd></dl>\n");
	}
	if (metadata.getObservations()!=null) {
	    index.addBodyText("<dl><dt>Observations</dt><dd>\n");
	    first=1;
	    for (Iterator oit=metadata.getObservations().iterator(); oit.hasNext(); ) {
		NObservation observation = (NObservation)oit.next();
		if (first==1) { first=0; } else { index.addBodyText(", "); }
		index.addBodyText(observation.getShortName());
	    }
	    index.addBodyText("</dd></dl>\n");
	}
	index.writeFile();
    }

    /** utility... */
    private String listLayerElements(NLayer layer, String prefix) {
	String rettext="";
	try {
	    if (layer==null) { return rettext; }
	    int first=1;
	    for (Iterator elit=layer.getContentElements().iterator(); elit.hasNext(); ) {
		NElement el = (NElement)elit.next();
		if (first==1) { first=0; } else { rettext+=", "; }
		rettext+="<a href=\""+prefix+"_"+el.getName()+".html\">"+el.getName()+"</a>\n";
	    }
	} catch (Exception ex) { System.err.println("Exception in layer retrieval"); }
	return rettext;
    }

    /** utility... */
    private String listNFileElements(NFile nfile, String prefix) {
	String rettext="";
	if (nfile==null) { return rettext; }
	int first=1;
	if (nfile instanceof NOntology) {
	    rettext+="<a href=\""+prefix+"_"+((NOntology)nfile).getElementName()+".html\">"+((NOntology)nfile).getElementName()+"</a>\n";
	} else if (nfile instanceof NObjectSet) {
	    for (Iterator elit=((NObjectSet)nfile).getContentElements().iterator(); elit.hasNext(); ) {
		NElement el = (NElement)elit.next();
		if (first==1) { first=0; } else { rettext+=", "; }
		rettext+="<a href=\""+prefix+"_"+el.getName()+".html\">"+el.getName()+"</a>\n";
	    }
	} else if (nfile instanceof NCorpusResource) {
	    return listLayerElements(((NCorpusResource)nfile).getTopLayer(), prefix);
	}
	return rettext;
    }

    /** write a file for an ontology */
    public void writeOntologyFile(NOntology ontology, String extraatts) {
	String annfilename=outputdir + File.separator + "corpus_"+ontology.getElementName()+".html";
	HTMLFile annfile = new HTMLFile(metadata.getCorpusDescription()+" ontology "+ontology.getName(), annfilename);
	annfile.addBodyText("<p>Ontologies have a single element-name. Elements are arranged in a tree structure.</p>");
	annfile.addBodyText("<dl>");
	if (ontology.getDescription()!=null) {
	    annfile.addBodyText("<dt>Ontology description</dt><dd>"+ontology.getDescription()+"</dd>\n");
	}
	annfile.addBodyText("<dt>Ontology element name</dt><dd>"+ontology.getElementName()+"</dd>\n");
	annfile.addBodyText("<dt>Attribute of this element</dt><dd><b>"+ontology.getAttributeName()+"</b> (string)</dd>");
	if (extraatts!=null) { 
	    annfile.addBodyText("<br>\n"+extraatts);
	}
	annfile.addBodyText("</dl>\n");
	annfile.writeFile();
    }

    /** write a file for an individual annotation element */
    public void writeAnnotationElementFile(NFile coding, NLayer layer, NElement element, int eltype, String extraatts) {
	String prefix = "annot";
	if (eltype==NElement.CORPUSRESOURCE || eltype==NElement.ONTOLOGY || eltype==NElement.OBJECTSET) {
	    prefix="corpus";
	}
	String annfilename=outputdir + File.separator + prefix +"_"+element.getName()+".html";
	HTMLFile annfile = new HTMLFile(metadata.getCorpusDescription()+" element "+element.getName(), annfilename);
	annfile.addBodyText("<dl>");
	
	annfile.addBodyText("<dt>Attributes of this element</dt><dd>");
	String indtext="";
	if (element.getAttributes()!=null) {
	    int firsta=1;
	    for (Iterator ait=element.getAttributes().iterator(); ait.hasNext(); ) {
		if (firsta==1) { firsta=0; } else { indtext+="<br>\n"; }
		NAttribute att = (NAttribute)ait.next();
		String type = "string";
		String enumstr = null;
		if (att.getType()==NAttribute.NUMBER_ATTRIBUTE) { type="number"; }
		else if (att.getType()==NAttribute.ENUMERATED_ATTRIBUTE) { 
		    type="enumerated"; 
		    enumstr=""; int firstenum=1;
		    for (Iterator enit=att.getEnumeratedValues().iterator(); enit.hasNext(); ) {
			if (firstenum==1) { firstenum=0; } else { enumstr+=", "; }
			enumstr += (String)enit.next();
		    }
		}
		indtext += "<b>" + att.getName() + "</b> (" + type;
		if (enumstr!=null) { indtext+="; <i>vals</i>: " + enumstr; }
		indtext += ")";
	    }
	}
	if (extraatts!=null) { 
	    if (!indtext.equals("")) { indtext+="<br>\n"; }
	    indtext+=extraatts;
	}
	annfile.addBodyText(indtext+"</dd>\n");

	annfile.addBodyText("<dt>Pointers from this element</dt><dd>");
	indtext="";
	if (element.getPointers()!=null) {
	    int firstp=1;
	    for (Iterator pit=element.getPointers().iterator(); pit.hasNext(); ) {
		NPointer pointer = (NPointer)pit.next(); 
		if (firstp==1) { firstp=0; } else { indtext+="<br>\n"; }
		NLayer target=metadata.getLayerByName(pointer.getTarget());
		if (target!=null) {
		    indtext+="<i>Role</i>: " + pointer.getRole() + "; <i>valid target elements</i>: " + listLayerElements(target, prefix);
		} else {
		    NFile targetfile = metadata.getOntologyByName(pointer.getTarget());
		    if (targetfile==null) {
			targetfile = metadata.getObjectSetByName(pointer.getTarget());
		    }
		    if (targetfile==null) {
			targetfile = metadata.getCorpusResourceByName(pointer.getTarget());
		    }
		    indtext+="<i>Role</i>: " + pointer.getRole() + "; <i>valid target elements</i>: " + listNFileElements(targetfile, "corpus");
		}
	    }
	}
	annfile.addBodyText(indtext+"</dd>\n");
	annfile.addBodyText("<dt>Valid child elements</dt><dd>");
	indtext="";
	if (layer!=null && layer.getChildLayer()!=null) {
	    indtext+=listLayerElements(layer.getChildLayer(), prefix);
	}
	annfile.addBodyText(indtext+"</dd>\n");
	annfile.addBodyText("<dt>Valid parent elements</dt><dd>");
	indtext="";
	if (layer!=null && layer.getParentLayers()!=null) {
	    int first=1;
	    for (Iterator lit=((List)layer.getParentLayers()).iterator(); lit.hasNext(); ) {
		if (first==1) { first=0; } else { indtext+=", "; }
		indtext+=listLayerElements((NLayer)lit.next(), prefix);
	    }	    
	}
	annfile.addBodyText(indtext+"</dd>\n");
	if (layer!=null && prefix.equals("annot")) {
	    annfile.addBodyText("<dt>Layer</dt><dd><a href=\"index_layers.html\">"+layer.getName()+"</a></dd>");
	}
	String annotationtype="one file per observation";
	if (prefix.equals("annot") && coding!=null && ((NCoding)coding).getType()==NCoding.AGENT_CODING) {
	    annotationtype="one file per agent per observation";
	}
	if (prefix.equals("corpus")) { annotationtype="one file per corpus"; }
	annfile.addBodyText("<dt>Annotation type</dt><dd>"+annotationtype+"</dd>\n");
	annfile.addBodyText("</dl>");
	annfile.writeFile();
    }
    
    /** return a handle to the top-level visualisation object: an HTML
     * file / text file / graphical object depending on the mode of
     * the visualisation. */
    public Object getVisualisationObject() {
	return indexfilename;
    }

    /** this class contains all the common stuff to be written */
    private class HTMLFile {
	String title;
	String filename;
	String body="";

	public HTMLFile(String title, String filename) {
	    this.title=title;
	    this.filename=filename;
	}

	public HTMLFile(String title, String filename, String body) {
	    this.title=title;
	    this.filename=filename;
	    this.body=body;
	}

	public void setBodyText(String text) {
	    this.body=text;
	}

	public void addBodyText(String text) {
	    this.body+=text;
	}

	private String getHeader(String title) {
	    return "<html>\n<head>\n<title>"+title+"</title>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"corpus.css\" />	\n\n</head>\n\n<body>\n<div id=\"header\">\n <h1>"+title+"</h1>\n</div>\n\n<div id=\"topmenu\">\n <a href=\"index.html\" class=\"active\">Home</a> | <a href=\"index_layers.html\">Index of Layers</a> \n</div>\n";
	}

	private String getFooter() {
	    return "\n</body>\n</html>\n";
	}

	public void writeFile() {
	    try {
		File f = new File(filename);
		if (f.getParent()!=null) {
		    File dir = new File(f.getParent());
		    if (!dir.exists()) { 
			if (!dir.mkdirs()) { 
			    System.err.println("Failed to make directories for HTML file " + filename + ".");
			    return;
			}
		    }
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(filename));
		out.write(getHeader(title));
		out.write("<div id=\"content\">"+body+"</div>\n");
		out.write(getFooter());
		out.close();
	    } catch (Exception ex) {
		System.err.println("Failed to write HTML file " + filename + ".");
	    }
	}
    }

    public void writeCSSFile(String filename) {
	try {
	    File f = new File(filename);
	    if (f.getParent()!=null) {
		File dir = new File(f.getParent());
		if (!dir.exists()) { 
		    if (!dir.mkdirs()) { 
			System.err.println("Failed to make directories for CSS file " + filename + ".");
			return;
		    }
		}
	    }
	    BufferedWriter out = new BufferedWriter(new FileWriter(filename));
	    out.write("body {\n	margin: 0;\n	padding: 0;\n}\n\n#content {\n	width: 400px;\n	font-family: Tahoma, sans-serif;\n	font-size: 10px;\n}\n\n#header {\n	font-family: Arial, sans-serif;\n	text-align: center;\n	border-bottom: 1px dashed #697;\n}\n\ni {\n	font-weight: lighter;\n}\n\ndt {\n	font-weight: bold;\n	text-align: left;\n}\n\ndd {\n	padding: 2px 5px 2px 2px;\n}\n\n#topmenu {\n	font-family: Arial, sans-serif;\n        text-align: center;\n}\n\n#content h2 {\n	margin: 2px 2px 0 2px;\n	padding: 3px 0 3px 10px;\n	font-family: Arial, sans-serif;\n	font-size: 14px;\n	color: navyblue;\n}\n\n#content p {\n	margin: 1px 1px 0 1px;\n}\n\n#content a:hover {\n	background-color: #eee;\n}\n");
	    out.close();
	} catch (Exception ex) {
	    System.err.println("Failed to write HTML file " + filename + ".");
	}
    }

}
