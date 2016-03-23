/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.export;


import java.util.*;
import java.io.*;

import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.nom.nomwrite.*;


/**
 * @author Jonathan Kilgour, Nov 2002
 *
 * This class attempts to export a loaded NOM into an Anvil
 * specification file plus one Anvil data file for each NOM
 * observation. 
 **/

public class NomToAnvil { 

    public static final String SPEC_FILE="spec.xml";
    protected static final String INDENT = "   ";
    protected static final String ELEMENT_NAME_ATTRIBUTE = "element_name";
    public static final String WAVE_TRACK="<track-spec name=\"wave\" type=\"waveform\" height=\"1.5\" />";
    NOMWriteCorpus nom;
    NiteMetaData meta;
    List roots;

    String corpusname;
    int level = 0;
    int enum_count=0;

    int elements=0;
    boolean display;
    boolean command_line;
    String current_track=null;
    Hashtable idhash=new Hashtable();
    Hashtable metaIndex=new Hashtable();
    Hashtable layer_indices=new Hashtable();
    Hashtable layer_recursion=new Hashtable();
    Hashtable layer_info=new Hashtable();
    Hashtable layer_data=new Hashtable();
    Hashtable codhash=new Hashtable();
    HashSet atthash=new HashSet();

    /** Pass the corpus name, the directory where output goes and a
        boolean - true if we can request information on the command
        line; false if not. This version loads the corpus! */
    public NomToAnvil(String c, String output_dir, boolean commandline){
	corpusname = c;
	command_line=commandline;
	try {
	    meta = new NiteMetaData(corpusname);
	} catch (NiteMetaException nme) {
	    nme.printStackTrace();
	    System.exit(0);
	}
	
	if (meta.getCorpusType()==NMetaData.STANDOFF_CORPUS) {
	    System.out.println("This is a standoff corpus: NOM being loaded");
	    try {
		nom = new NOMWriteCorpus(meta);
		nom.loadData();
	    } catch (net.sourceforge.nite.nom.NOMException nex) {
		nex.printStackTrace();
		System.exit(0);
	    }
	} else {
	    System.out.println("This is a standalone or simple corpus: no NOM has been loaded");
	    System.exit(0);
	}

	// NOM and metadata loaded. Attempt the translation now.
	process(nom, output_dir, command_line);
    }

    /** This constructor works on a ready-loaded NOM
     */
    public NomToAnvil(NOMCorpus nom, String output_dir, boolean commandline) {
	process(nom, output_dir, commandline);
    }    

    private void process (NOMCorpus nom, String output_dir, boolean commandline) {
	command_line=commandline;
	meta = (NiteMetaData) nom.getMetaData();
	// First set up our element hash so we know how to cross-reference,
	// and get information about recursive layers.

	// The main loop is over the observations we have loaded
	List observations=meta.getObservations();
	roots = nom.getRootElements();
	Iterator oit = observations.iterator();
	while (oit.hasNext()) {
	    NiteObservation observation = (NiteObservation) oit.next();
	    firstPassObservation(observation);
	}
	Enumeration keys = layer_recursion.keys();
	while (keys.hasMoreElements()) {
	    String key = (String)keys.nextElement();
	    //	    System.out.println("Layer: " + key + " recurses to level: " + ((Integer)layer_recursion.get((Object)key)).toString());
	}

	try {
	    FileOutputStream fout= new FileOutputStream(output_dir + File.separator + 
						    SPEC_FILE);
	    BufferedOutputStream bout= new BufferedOutputStream(fout);
	    OutputStreamWriter out = new OutputStreamWriter(bout, "8859_1");
	    
	    out.write("<?xml version=\"1.0\" ");
	    out.write("encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n");  
	    out.write("<annotation-spec>\n" + INDENT + "<head>\n");
	    level=2;
	    writeSpecAttributes(out);
	    out.write(INDENT + "</head>\n\n" + INDENT + "<body>\n");
	    out.write(INDENT + INDENT + WAVE_TRACK + "\n");
	    List codings=meta.getCodings();
	    Iterator cit=codings.iterator();
	    while (cit.hasNext()) {
		NCoding coding=(NCoding)cit.next();
		String codingdef = writeSpecBodyCoding(coding);
		if (codingdef==null || codingdef.equals("")) { continue; }
		boolean prim=false;
		NLayer lay = coding.getTopLayer();
		while (lay!=null) {
		    if (lay.getLayerType()==NLayer.TIMED_LAYER) { prim=true; break; }
		    if (lay.getChildLayer() == null) { break; }
		    if (lay.getChildLayer().getContainer()!=lay.getContainer()) { break; }
		    lay = lay.getChildLayer();
		}
		
		if (prim) { out.write(codingdef);
		} else { codhash.put(coding, codingdef); }
	    }
	    
	    Enumeration ckeys = codhash.keys();
	    while (ckeys.hasMoreElements()) {
		NCoding key = (NCoding)ckeys.nextElement();
		out.write((String)codhash.get((Object)key));
	    }
	    
	    out.write(INDENT + "</body>\n</annotation-spec>\n");
	    out.flush();
	    out.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	// NOW WRITE THE DATA!

	oit = observations.iterator();
	while (oit.hasNext()) {
	    NiteObservation observation = (NiteObservation) oit.next();
	    writeObservation(observation, output_dir);
	}
    }

    private NOMElement findRootWithColour(String colour) {
	Iterator rit = roots.iterator();
	while (rit.hasNext()) {
	    NOMElement roo = (NOMElement)rit.next();
	    if (colour.equals(roo.getColour())) {
		return roo;
	    }
	}
	return null;
    }

    /*--------------------*/
    /* First pass stuff   */
    /*--------------------*/

    /** Uses the metadata information to calculate which files should
     *  be processed for a given observation. Then make a first pass
     *  of that data 
     */
    private void firstPassObservation(NiteObservation observation) {
	List cods = meta.getCodings();
	Iterator cit = cods.iterator();
	NCoding nc;

	while (cit.hasNext())  {
	    nc = (NCoding) cit.next();
	    if (nc.getType()==NCoding.INTERACTION_CODING) {
		String colour = observation.getShortName();
		colour = colour + "." + nc.getName();
		NOMElement rel = findRootWithColour(colour);
		if (rel==null) {
		    System.out.println("Coding " + nc.getName() + " not found for observation " + observation.getShortName() + ". (colour " + colour + ")");
		    continue;
		}
		firstPassFile(rel);
	    } else {
		//System.out.println("Agent CODING: " + nc.getName());
		Iterator agit = meta.getAgents().iterator();
		while (agit.hasNext()) {
		    NiteAgent age = (NiteAgent)agit.next();
		    String colour = observation.getShortName() + "." + age.getShortName() + "." + nc.getName();
		    NOMElement rel = findRootWithColour(colour);
		    if (rel==null) {
			System.out.println("Coding " + nc.getName() + " not found for observation " + observation.getShortName() + ". (colour " + colour + ")");
			continue;
		    }
		    firstPassFile(rel);
		}
	    }
	}
    }

    private void firstPassFile(NOMElement element) {
	elements=0;
	layer_indices=new Hashtable();
	List els = element.getChildren();
	if (els==null) { return; }
	Iterator elit=els.iterator();
	while (elit.hasNext()) {
	    NOMElement nel = (NOMElement)elit.next();
	    firstPassElement(nel);
	}	
    }

    private NElement getMetaElement(String name) {
	Object o = metaIndex.get((Object)name);
	if (o==null) {
	    NElement metael=meta.getElementByName(name);
	    metaIndex.put((Object)name, (Object)metael);
	    return metael;
	} else {
	    return (NElement)o;
	}
    }

    private int findRecursionDepth(NOMElement element, NLayer layer) {
	if (!layer.getRecursive()) { return 0; }
	NOMElement par = element.getParentInFile();
	if (par==null || par.isStreamElement()) { return 0; }
	NElement metael=getMetaElement(par.getName());
	NLayer player = metael.getLayer();
	if (player==layer) { return 1 + findRecursionDepth(par, layer);	} 
	else { return 0; }
    }

    private void firstPassElement(NOMElement element) {
	NElement metael=getMetaElement(element.getName());
	NLayer layer = metael.getLayer();
	int index=0;
	String layer_name = layer.getName();

	int recursion_depth=0;

	if (layer.getRecursive()) {
	    recursion_depth=findRecursionDepth(element, layer);
	    //	    System.out.println("Element " + element.getID() + " has recursion level " + recursion_depth);
	    layer_name = layer_name + recursion_depth;
	} 

	Object reclev=layer_recursion.get((Object)layer.getName());
	if (reclev==null || ((Integer)reclev).intValue() < recursion_depth) {
	    layer_recursion.put((Object)layer.getName(), (Object) new Integer(recursion_depth));
	} 

	Integer ind;
	Object o=layer_indices.get((Object)layer_name);
	if (o==null) { ind = new Integer(0); } 
	else { ind = new Integer(((Integer)o).intValue()+1);  }
	//	System.out.println("Putting value " + ind.toString() + " into layer " + layer_name);
	layer_indices.put((Object)layer_name, (Object)ind);
	index = ind.intValue();

	String cname="";
	Object cont = layer.getContainer();
	if (cont instanceof NCoding) {
	    cname=((NCoding)cont).getName();
	} else {
	    System.err.println("Trying to process a non-coding element " + element.getName() + ":" + element.getID());
	    System.exit(0);
	}


	idhash.put((Object)element.getID(), (Object) 
		   new ElementInfo(layer, cname, recursion_depth, index));

	List els = element.getChildren();
	if (els==null) { return; }
	Iterator elit=els.iterator();
	while (elit.hasNext()) {
	    NOMElement nel = (NOMElement)elit.next();
	    if (nel.getColour().equals(element.getColour())) {
		firstPassElement(nel);
	    }
	}	
    }

    class ElementInfo {
	private NLayer layer;
	private String groupname;
	private int recursionlevel;
	private int index;

	public ElementInfo (NLayer layer, String groupname, 
			    int recursionlevel, int index) {
	    this.layer=layer;
	    this.groupname=groupname;
	    this.recursionlevel=recursionlevel;
	    this.index=index;
	}
	
	public String getTrackName() {
	    if (layer.getRecursive()) {
		return groupname + "." +  layer.getName() + recursionlevel;
	    } else {
		return groupname + "." +  layer.getName();
	    }
	}

	public String getShortTrackName() {
	    if (layer.getRecursive()) {
		return layer.getName() + recursionlevel;
	    } else {
		return layer.getName();
	    }
	}

	public String getGroupName() {
	    return groupname;
	}

	public int getRecursionLevel() {
	    return recursionlevel;
	}

	public int getIndex() {
	    return index;
	}

	public NLayer getLayer() {
	    return layer;
	}
    }

    
    /*----------------------------*/
    /* Writing Specification file */
    /*----------------------------*/    
    
    class LayerInfo {
	NLayer layer;
	NLayer pointed_to_by=null;
	boolean output_meta=false;
	boolean output_data=false;
	String type;
	
	public LayerInfo (NLayer layer) {
	    this.layer=layer;
	}

	public void setPointedTo(NLayer pointer) {
	    pointed_to_by=pointer;
	}

	public NLayer getPointedTo() {
	    return pointed_to_by;
	}

	public void setOutputMeta(boolean opm) {
	    output_meta=opm;
	}
	
	public boolean getOutputMeta() {
	    return output_meta;
	}

	public void setOutputData(boolean opd) {
	    output_data=opd;
	}

	public boolean getOutputData() {
	    return output_data;
	}

	public void setType(String type) {
	    this.type=type;
	}

	public String getType() {
	    return type;
	}
    }

    private void writeWithIndent(OutputStreamWriter out, String str) throws IOException {
	for (int i=0; i<level; i++) { out.write(INDENT); }
	out.write(str);	
    }

    private String indent() {
	String ret = "";
	for (int i=0; i<level; i++) { ret = ret + INDENT; }
	return ret;
    }


    private void append(NLayer layer, String string) {
	String start_with = (String)layer_data.get((Object)layer);
	if (start_with==null) {
	    start_with="";
	}
	start_with= start_with + string;
	//	System.out.println("Added a string to layer: " + layer.getName() + ": " + start_with);
	layer_data.put((Object)layer, (Object)start_with);
    }

    /*---------------------------------*/
    /* write the enumerated attributes */
    /*---------------------------------*/
    private void writeSpecAttributes(OutputStreamWriter out) throws IOException {
	List codings = meta.getCodings();
	Iterator cit = codings.iterator();
	while (cit.hasNext()) {
	    NCoding ncod=(NCoding)cit.next();
	    // System.out.println("Write attrs for coding " + ncod.getName());
	    writeSpecAttrsLayer(out, (NLayer)ncod.getTopLayer());
	}
    }

    private void writeSpecAttrsLayer(OutputStreamWriter out, NLayer layer) throws IOException {
	if (layer==null) { return; }
	// System.out.println("Write attrs for layer " + layer.getName());
	List contents=layer.getContentElements();
	Iterator lit=contents.iterator();
	while(lit.hasNext()) {
	    NElement nele=(NElement)lit.next();
	    writeSpecAttrsElement(out, nele);
	}
	NLayer kid=layer.getChildLayer();
	if (kid!=null && kid.getContainer()==layer.getContainer()) {
	    writeSpecAttrsLayer(out,kid);
	}
    }
    
    private void writeSpecAttrsElement(OutputStreamWriter out, NElement element) throws IOException {
	if (element==null) { return; }
	// System.out.println("Write attrs for element " + element.getName());
	List atts=element.getAttributes();
	Iterator ait=atts.iterator();
	while(ait.hasNext()) {
	    NAttribute nat=(NAttribute)ait.next();
	    // System.out.println("Write attr " + nat.getName() + " of type "+ nat.getType());
	    if (nat.getType()==NAttribute.ENUMERATED_ATTRIBUTE) {
		enum_count++;
		if (enum_count==1) {writeWithIndent(out, "<valuetype-def>\n");}
		String nm = element.getName() + "_" + nat.getName();
		writeWithIndent(out,"<valueset name=\"" + nm + "\">\n");
		level++;
		List vlist=nat.getEnumeratedValues();
		Iterator vit=vlist.iterator();
		while (vit.hasNext()) {
		    writeWithIndent(out,"<value-el>" + (String)vit.next() +
				    "</value-el>\n");
		}
		level--;
		writeWithIndent(out,"</valueset>\n");
		if (enum_count==1) {writeWithIndent(out,"</valuetype-def>\n");}
	    }
	}
    }


    /*----------------------------*/
    /* Write the body of the spec */
    /*----------------------------*/

    private String writeSpecBodyCoding(NCoding coding) {
	String codstr="";
	if (coding.getType()==NCoding.INTERACTION_CODING) {
	    // System.out.println("Writing interaction coding " + coding.getName() );
	    codstr = codstr + indent() + "<group name=\"" + coding.getName() + "\">\n";
	    level++;
	    NLayer layer=coding.getTopLayer();
	    String sbl = writeSpecBodyLayer(layer);
	    if (sbl==null || sbl.equals("")) { return null; }
	    codstr = codstr + sbl;
	    level--;
	    codstr = codstr + indent() + "</group>\n";
	} else 	if (coding.getType()==NCoding.AGENT_CODING) {
	    List agents = meta.getAgents();
	    if (agents==null) { return null; }
	    Iterator ait=agents.iterator();
	    while(ait.hasNext()) {
		NAgent agent= (NAgent)ait.next();
		level++;
		NLayer layer=coding.getTopLayer();
		String sbl = writeSpecBodyLayer(layer);
		if (sbl!=null && !sbl.equals("")) {
		    codstr = codstr + indent() + "<group name=\"" + coding.getName() + "." + agent.getShortName() + "\">\n";
		    codstr = codstr + sbl;
		    level--;
		    codstr = codstr + indent() + "</group>\n";
		}
	    }
	}
	return codstr;
    }

    private void writeGroup(String name, NLayer layer) {

    }

    private String writeSpecBodyLayer(NLayer layer) {
	String laystr="";
	String ltype="primary";
	String ref="";
	Object o = layer.getContainer();
	String cname="";
	if (o instanceof NCoding) { cname=((NCoding)o).getName(); }
	else { 
	    System.err.println("Trying to write a non-coding layer " + layer.getName());
	    System.exit(0);
	}

	if (layer.getLayerType()==NLayer.FEATURAL_LAYER) { return null; } 

	LayerInfo li = (LayerInfo)layer_info.get((Object)layer);
	//	System.out.println("Write layer " + layer.getName() + ". layer info is " + li);
	if (li==null) { li=new LayerInfo(layer); }
	if (li.getOutputMeta()) { return null; } 

	/* do the kid! */
	if (layer.getLayerType()==NLayer.STRUCTURAL_LAYER) {
	    NLayer kid = layer.getChildLayer();
	    if (kid!=null) {
		LayerInfo kli = (LayerInfo)layer_info.get((Object)layer.getChildLayer());
		if (kli== null) {
		    kli = new LayerInfo (layer.getChildLayer());
		}
		if (kli.getPointedTo()==null) {
		    ltype="span";
		    if (kid.getContainer()==layer.getContainer()) {
			//			System.out.println("Layer " + layer.getName() + " has the same container to its child layer " + kid.getName());
			laystr = laystr + writeSpecBodyLayer(layer.getChildLayer());
			kli.setPointedTo(layer);
			layer_info.put((Object)layer.getChildLayer(), (Object)kli);
			ref=" ref=\"" + cname + "." + layer.getChildLayer().getName() + "\"";
		    } else {
			String kidcode = ((NCoding)kid.getContainer()).getName();
			//			System.out.println("Layer " + layer.getName() + " has a different container to its child layer " + kid.getName());
			kli.setPointedTo(layer);			
			layer_info.put((Object)layer.getChildLayer(), (Object)kli);
			ref=" ref=\"" + kidcode + "." + layer.getChildLayer().getName() + "\"";
		    }
		}
	    }
	}
	
	/*
	if (ltype.equals("span")) {
	    System.out.println("Writing structural layer " + layer.getName());
	} else {
	    System.out.println("Writing primary layer " + layer.getName());
	}
	*/
	
	laystr = laystr + indent() + "<track-spec name=\"" + layer.getName() + "\" type=\"" + ltype + "\"" + ref + ">\n";
	//	System.out.println("Writing layer " + layer.getName() + " which is " + ltype);
	level++;
	List atlist = getLayerAttributes(layer);
	atthash = new HashSet();
	if (command_line) {
	    System.out.println("Please select display for track '" + layer.getName() + "'");
	    requestDisplayInfo(atlist);
	}
	laystr = laystr + writeSpecBodyAttributes(atlist);
	level--;
	laystr = laystr + indent() + "</track-spec>\n";

	li.setType(ltype);
	li.setOutputMeta(true);
	layer_info.put((Object)layer, (Object)li);
	return laystr;
    }

    class AttributeInfo {
	String name;
	boolean display=false;
	int type;
	String elname;

	public AttributeInfo (String name) {
	    this.name=name;
	}

	public AttributeInfo (String name, int type) {
	    this.name=name;
	    this.type=type;
	}

	public AttributeInfo (String name, int type, String elname) {
	    this.name=name;
	    this.type=type;
	    this.elname=elname;
	}

	public AttributeInfo (String name, boolean display) {
	    this.name=name;
	    this.display=display;
	}

	public void setDisplay(boolean display) {
	    this.display=display;
	}

	public boolean getDisplay() {
	    return display;
	}

	public String getName() {
	    return name;
	}

	public void setType(int type) {
	    this.type=type;
	}

	public int getType() {
	    return type;
	}

	public String getElname() {
	    return elname;
	}

    }

    private boolean member(List ats, String s) {
	for (Iterator iterator=ats.iterator(); iterator.hasNext(); ) {
	    AttributeInfo ai = (AttributeInfo)iterator.next();
	    //System.out.println("Comparing " + ai.getName() + " with " + s); 
	    if (ai.getName().equals(s)) { return true; }
	}
	return false;
    }

    private AttributeInfo findAttribute(List ats, String name) {
	for (Iterator iterator=ats.iterator(); iterator.hasNext(); ) {
	    AttributeInfo ai = (AttributeInfo)iterator.next();
	    if (ai.getName().equals(name)) { return ai; }
	}
	return null;
    }

    private List getLayerAttributes(NLayer layer) {
	ArrayList attributes = new ArrayList();
	List contents=layer.getContentElements();
	Iterator lit=contents.iterator();
	int ind=0;
	while(lit.hasNext()) {
	    NElement nelement=(NElement)lit.next();
	    List list = nelement.getAttributes();
	    for(Iterator iterator = list.iterator(); iterator.hasNext();) {
		NAttribute nattribute = (NAttribute)iterator.next();
		String s = nattribute.getName();
		if(!s.equals(meta.getIDAttributeName()) && !s.equals(meta.getStartTimeAttributeName()) && !s.equals(meta.getEndTimeAttributeName()) && !member(attributes,s) ) {
		    attributes.add(ind++, new AttributeInfo(s, nattribute.getType(), nelement.getName()));
		}
            }
	}

	// Questionable whether we always want to do this, but
	// otherwise we're losing information - put the element name
	// in as an attribute!
	
	attributes.add(ind++, new AttributeInfo(ELEMENT_NAME_ATTRIBUTE, NAttribute.STRING_ATTRIBUTE, ""));
	return (List)attributes;
    }

    private void requestDisplayInfo(List attributes) {
	if (attributes==null  || attributes.size()==0) { return; }
	
	String userinput = null;

	String choices = "   0: PCDATA content\n";
	
	for (int ii=0; ii<attributes.size(); ii++) {
	    AttributeInfo atinfo = (AttributeInfo)attributes.get(ii);
	    int num = ii+1;
	    choices = choices + "   " + num + ": Attribute '" + atinfo.getName() + "'\n";
	}

	InputStreamReader isr = new InputStreamReader ( System.in );
	BufferedReader br = new BufferedReader ( isr );
	String s = null;
	boolean done = false;
	int choice=-1;
	try {
	    while (!done) {
		System.out.print(choices);
		s = br.readLine ();
		if (s!=null) {
		    try {
			choice = Integer.parseInt(s);
			if (choice >= 0 && choice <= attributes.size()) {
			    done=true;
			    if (choice>0) {
				AttributeInfo ai = (AttributeInfo) attributes.get(choice-1);
				ai.setDisplay(true);
			    }
			} else {
			    System.out.println("Please type a valid number (0 through " + attributes.size() + "):");
			}
		    } catch (Exception e) {
			System.out.println("Please type a number (0 through " + attributes.size() + "):");
		    }
		}
	    }
	}
	catch ( IOException ioe ) {
	    // won't happen too often from the keyboard
	}
    }


    private String writeSpecBodyAttributes(List attributes) {
	String atstr = "";
	if (attributes==null) { return null; }
	Iterator ait=attributes.iterator();
	while (ait.hasNext()) {
	    AttributeInfo nat = (AttributeInfo)ait.next();
	    String name=nat.getName();
	    if (name.equals(meta.getIDAttributeName()) ||
		name.equals(meta.getStartTimeAttributeName()) ||
		name.equals(meta.getEndTimeAttributeName())) {
		continue;
	    }
	    String dstr="false";
	    if (command_line) {
		if (nat.getDisplay()) { dstr="true"; }
	    } else {
		// just display the first if we have no information
		if (!display) { dstr="true"; display=true; }
	    }
	    if (nat.getType()==NAttribute.ENUMERATED_ATTRIBUTE) {
		String nm = nat.getElname() + "_" + nat.getName();
		atstr = atstr + indent() + "<attribute name=\"" + nat.getName() + "\" valuetype=\"" + nm + "\" display=\"" + dstr + "\"/>\n";
	    } else { // do they distinguish between text and numeric?
		atstr = atstr + indent() + "<attribute name=\"" + nat.getName() + "\" valuetype=\"String\" display=\"" + dstr + "\"/>\n";
	    }
	}
	return atstr;
    }


    /*-----------------------*/
    /* write the actual data */
    /*-----------------------*/

    /** Uses the metadata information to calculate which files should
     *  be processed for a given observation. Then write the
     *  observation file
     */
    private void writeObservation(NiteObservation observation, String output_dir) {
	List cods = meta.getCodings();
	Iterator cit = cods.iterator();
	NCoding nc;

	try {
	    OutputStream fout= new FileOutputStream(output_dir + File.separator + observation.getShortName() + ".anvil");
	    OutputStream bout= new BufferedOutputStream(fout);
	    OutputStreamWriter out = new OutputStreamWriter(bout, "8859_1");
	    
	    out.write("<?xml version=\"1.0\" ");
	    out.write("encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n");  
	    out.write("<annotation>\n" + INDENT + "<head>\n" + INDENT + 
		      INDENT + "<specification src=\"");
	    out.write(output_dir + File.separator + SPEC_FILE);
	    out.write("\"/>\n");

	    NSignal signal = findBestSignal();
	    if (signal != null) {
		String type="video";
		if (signal.getMediaType()==NSignal.AUDIO_SIGNAL) { type="audio"; }
		String location = signal.getFilename(observation.getShortName(), (String)null);
		out.write(INDENT + INDENT + "<" + type + " src=\"" + location + "\"/>\n");
	    }

	    out.write(INDENT + "</head>\n\n" + INDENT + "<body>\n");
	    
	    layer_data = new Hashtable();
	    while (cit.hasNext())  {
		nc = (NCoding) cit.next();
		if (nc.getType()==NCoding.INTERACTION_CODING) {
		    String colour = observation.getShortName();
		    colour = colour + "." + nc.getName();
		    NOMElement rel = findRootWithColour(colour);
		    if (rel==null) {
			System.out.println("Coding " + nc.getName() + " not found for observation " + observation.getShortName() + ". (colour " + colour + ")");
			continue;
		    }
		    writeData(rel);
		} else {
		    //System.out.println("Agent CODING: " + nc.getName());
		    Iterator agit = meta.getAgents().iterator();
		    while (agit.hasNext()) {
			NiteAgent age = (NiteAgent)agit.next();
			String colour = observation.getShortName() + "." + age.getShortName() + "." + nc.getName();
			NOMElement rel = findRootWithColour(colour);
			if (rel==null) {
			    System.out.println("Coding " + nc.getName() + " not found for observation " + observation.getShortName() + ". (colour " + colour + ")");
			    continue;
			}
			writeData(rel);
		    }
		}
	    }

	    /* Anvil insists on getting the lower levels befor the
               things that point to them - this seems to do the trick
               (just put out primary layers before spans) */

	    Enumeration keys = layer_data.keys();
	    while (keys.hasMoreElements()) {
		NLayer key = (NLayer)keys.nextElement();
		LayerInfo nli=(LayerInfo)layer_info.get(key);
		if (nli.getType().equals("primary")) {
		    //  System.out.println("WRITING Layer: " + key.getName() + ".");
		    if (layer_data.get((Object)key) != null) {
			out.write((String)layer_data.get((Object)key));
			level=2;
			writeWithIndent(out, "</track>\n");
		    } 
		}
	    }

	    keys = layer_data.keys();
	    while (keys.hasMoreElements()) {
		NLayer key = (NLayer)keys.nextElement();
		LayerInfo nli=(LayerInfo)layer_info.get(key);
		if (nli.getType().equals("span")) {
		    //  System.out.println("WRITING Layer: " + key.getName() + ".");
		    if (layer_data.get((Object)key) != null) {
			out.write((String)layer_data.get((Object)key));
			level=2;
			writeWithIndent(out, "</track>\n");
		    } 
		}
	    }
	    out.write(INDENT + "</body>\n</annotation>\n");
	    out.flush();
	    out.close();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private NSignal findBestSignal() {
	NSignal backup = null;
	for (Iterator sit=meta.getSignals().iterator(); sit.hasNext(); ) {
	    NSignal sig=(NSignal)sit.next();
	    if (sig.getType()==NSignal.AGENT_SIGNAL) { continue; }
	    if (sig.getMediaType()==NSignal.VIDEO_SIGNAL) { return sig; } 
	    else { backup=sig; }
	}
	return backup;
    }

    private void writeData(NOMElement element) {
	elements=0;
	List els = element.getChildren();
	if (els==null) { return; }
	Iterator elit=els.iterator();
	while (elit.hasNext()) {
	    NOMElement nel = (NOMElement)elit.next();
	    writeDataElement(nel);
	} 

	/*
	elit=els.iterator();
	while (elit.hasNext()) {
	    NOMElement nel = (NOMElement)elit.next();
	    writeDataElement(out, nel);
	}
	if (elements>0) {
	    level--;
	    writeWithIndent(out, "</track>\n");
	    elements=0;
	}
	*/
    }

    private void writeDataElement(NOMElement element) {
	ElementInfo info = (ElementInfo) idhash.get((Object) element.getID());
	if (info==null) {
	    System.err.println("Failed to get info for element " + element.getID());
	    System.exit(0);
	}
	System.out.println("Writing the data element " + element.getID() + "(a " + element.getName() + "). Layer: " + info.getLayer().getName() + ". ID: " + info.getIndex());
	int index=info.getIndex();
	NLayer layer = info.getLayer();
	String groupname = info.getGroupName();
	String name = groupname + "." + layer.getName();

	if (layer.getLayerType()==NLayer.FEATURAL_LAYER) { return; } 

	LayerInfo linfo =  (LayerInfo)layer_info.get((Object)layer);
	if (linfo==null) {
	    System.err.println("Failed to get info for layer " + layer.getName());
	    System.exit(0);
	}

	if (info.getRecursionLevel() > 0 ) { 
	    System.out.println("WARNING: Anvil output for recursive elements will be incomplete! (" + element.getID() + ")");
	    return;
	}
	
	if (index==0) {
	    current_track=name;
	    String ref="";
	    if (linfo.getType()==null) {
		System.out.println("Failed to get the type of layer " + layer.getName());
		linfo.setType("primary");
		//		System.exit(0);
	    }
	    if (linfo.getType().equals("span")) {
		NLayer clay = layer.getChildLayer();
		NCoding nco = (NCoding)clay.getContainer();
		LayerInfo cinfo = (LayerInfo)layer_info.get((Object)clay);
		ref = " ref=\"" + nco.getName() + "." + clay.getName() + "\"";
	    } 
	    //	    String ref = clay.getName();
	    level=2;
	    append(layer, indent() + "<track name=\"" + name + "\" type=\"" + linfo.getType() + "\"" + ref + ">\n");
	    level++;
	}

	append(layer, indent() + "<el index=\"" + index + "\"");
	
	if (linfo.getType().equals("primary")) {
	    append(layer, " start=\"" + element.getStartTime() + "\"");
	    append(layer, " end=\"" + element.getEndTime() + "\"");
	} else {
	    List kids = element.getChildren();
	    if (kids==null) {
		append(layer, " start=\"0\"");
		append(layer, " end=\"0\"");
	    } else {
		NOMElement first = (NOMElement)kids.get(0);
		NOMElement last = (NOMElement)kids.get(kids.size()-1);
		ElementInfo ei1 = (ElementInfo)idhash.get((Object)first.getID());
		ElementInfo ei2 = (ElementInfo)idhash.get((Object)last.getID());
                Integer firstid = new Integer(ei1.getIndex());
                Integer lastid = new Integer(ei2.getIndex());
		// Integer firstid=(Integer)idhash.get((Object)first.getID());
		// Integer lastid=(Integer)idhash.get((Object)last.getID());
		//Integer firstid=new Integer(0);
		//Integer lastid=new Integer(0);
		if (firstid==null || lastid==null) {
		    System.out.println("Failed to get ID from hash for: " + first.getID() + " or: " + last.getID());
		    firstid = new Integer(0);
		    lastid = new Integer(0);
		}
		append(layer," start=\"" + firstid.toString() + "\"");
		append(layer," end=\"" + lastid.toString() + "\"");
	    }
	}
	append(layer, ">\n");
	level++;
	List attrs=element.getAttributes();
	Iterator ait=attrs.iterator();
	while (ait.hasNext()) {
	    NOMAttribute nat = (NOMAttribute)ait.next();
	    String atname=nat.getName();
	    if (atname.equals(meta.getIDAttributeName()) ||
		atname.equals(meta.getStartTimeAttributeName()) ||
		atname.equals(meta.getEndTimeAttributeName())) {
		continue;
	    }
	    append(layer, indent() + "<attribute name=\"" + atname + "\">" );
	    if (nat.getType()==NOMAttribute.NOMATTR_NUMBER) {
		append(layer, nat.getDoubleValue().toString());
	    } else {
		append(layer, nat.getStringValue());
	    }
	    append(layer, "</attribute>\n");
	}
	append(layer, indent() + "<attribute name=\"" + ELEMENT_NAME_ATTRIBUTE + "\">" + element.getName() + "</attribute>\n");
	
	level--;
	append(layer, indent() + "</el>\n");
	elements++;

	List els = element.getChildren();
	if (els==null) { return; }
	Iterator elit=els.iterator();
	while (elit.hasNext()) {
	    NOMElement nel = (NOMElement)elit.next();
	    if (nel.getColour().equals(element.getColour())) {
		writeDataElement(nel);
	    }
	}	


    }


    /**
     * Called to start the  application.
     * Legal command line arguments are:
     *<ul>
     *<li> corpus </li>
     *</ul>
     *
     */
    public static void main(String args[]){
	if (args.length != 2 && args.length != 4 && args.length != 6) { usage(); }
	String dir=".";
	String corpus=null;
	String mode = null;

	for (int i=0; i<args.length; i++) {
	    String arg=args[i];
	    if (arg.equals("-corpus")) {
		 i++; corpus=args[i];
	    } else if (arg.equals("-d")) {
		 i++; dir=args[i];
	    } else if (arg.equals("-mode")) {
		 i++; mode=args[i];
	    } else {
		usage();
	    }
	}
	
	boolean cl=true;
	if (mode!=null && mode.equals("gui")) {
	    cl=false;
	} 
	if (corpus == null) { usage(); }
	
	NomToAnvil m = new NomToAnvil(corpus, dir, cl);
    }


    private static void usage () {
	System.err.println("Usage: java NomToAnvil -corpus <path-to-metadata> [ -d <output-directory> ] [ -mode <gui/command_line> ]");
	System.exit(0);
    }


}
