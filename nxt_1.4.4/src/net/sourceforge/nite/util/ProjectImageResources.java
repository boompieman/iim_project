/**
 * Copyright (c) 2006, Jean Carletta, Jonathan Kilgour, Dennis Reidsma
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.util;

import java.io.*;
import java.util.*;
import java.lang.Math;

import net.sourceforge.nite.search.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.gui.util.NOMElementStartTimeComparator;

/**
 * This process examines two element streams and produces a third: an
 * projected image of the first timed stream onto the second. This
 * version allows for resources, and creates a new resource if
 * required. Inputs:
 * 1. the resource to be projected
 * 2. The resource name to be produced (this must have children in an
       existing timed stream)
 * 3. If the target resource is newly created, this tells the program what 
      timed resource it refers to.
 **/
public class ProjectImageResources { 
    
    NOMWriteCorpus nom;
    NiteMetaData meta;
    NResourceData resourcedata;
    String corpusname;
    String observationname;
    String sourceelement;
    String targetelement;
    String targetreference;
    String sourceresource;
    String targetresource;
    String targetcoding;
    private Engine searchEngine = new Engine();
    int segind=0;
    int asegind=0;

    /** utility to return time-ordered list of elemenst in a
     * particular layer. Note that this list can contain overlaps if
     * the layer is in an agent coding. */
    private List getElementsInLayer (NLayer layer) {
	List retlist = new ArrayList();
	if (layer==null || layer.getContentElements()==null) {
	    System.err.println("Fatal error: layer is: " + layer);
	    System.exit(0);
	}
	for (Iterator elit=layer.getContentElements().iterator(); elit.hasNext(); ) {
	    NElement nel = (NElement) elit.next();
	    List nellist = nom.getElementsByName(nel.getName());
	    //Debug.print("Element " + nel.getName() + " returns "+nellist.size());
	    if (nellist!=null) { retlist.addAll(nellist); }
	}
	Collections.sort(retlist, new NOMElementStartTimeComparator());
	return retlist;
    }

    /** Find the time-ordered list of elements which lie more than
     * half-within the temporal boundaries and that belong to the
     * given agent (unless the agent is null) */
    private List getElementsInRange(List allels, double st, double en, NAgent agent, NResource targetres) {
	List retlist = new ArrayList();
	for (Iterator elit=allels.iterator(); elit.hasNext(); ) {
	    NOMElement el = (NOMElement)elit.next();
	    if (el.getResource()==null || !targetres.dependsOn(el.getResource())) {
		continue;
	    }
	    //Debug.print("El: "+el.getText()+" ("+el.getStartTime()+", "+el.getEndTime()+"); compare to: ("+st+", "+en+"). ");
	    // check we at least overlap the time period
	    if (el.getEndTime()<st) { continue; }
	    if (el.getStartTime()>en) { break; }
	    // check the agent
	    if (agent!=null && el.getAgent()!=null) {
		if (agent!=el.getAgent()) { continue; }
	    }
	    // check the midpoint is in the time period
	    double mid = el.getStartTime() + ((el.getEndTime()-el.getStartTime()) / 2);
	    if (mid>st && mid<=en) { //make one end inclusive to ensure no duplication
		retlist.add(el);
	    }
	}
	return retlist;
    }

    private NPointer getPointer(NElement el, NLayer lay) {
	List ps = el.getPointers();
	if (ps==null) { return null; }
	for (Iterator pit=ps.iterator(); pit.hasNext(); ) {
	    NPointer np = (NPointer)pit.next();
	    if (np==null || np.getTarget()==null) { continue; }
	    if (np.getTarget().equalsIgnoreCase(lay.getName())) { return np; }
	}
	return null;
    }


    public ProjectImageResources(String corp, String obs, String source, String target, String reference) {
	corpusname = corp;
	observationname=obs;
	sourceresource=source;
	targetresource=target;
	boolean writeresources=false;

	try {
	    /* First load the metadata */
	    meta = new NiteMetaData(corpusname);
	    resourcedata = meta.getResourceData();
	    if (resourcedata==null) {
		usage("No resource data associated with metadata: use ProjectImageResources instead");
	    }

	    /* set up a new NOM Corpus */
	    nom = new NOMWriteCorpus(meta);
	    nom.setLogStream(System.err);

	    /* Check the validity of the source and target resources */
	    NResource sourcer = resourcedata.getResourceByID(sourceresource);
	    NResource targetr = resourcedata.getResourceByID(targetresource);

	    if (sourcer==null) { 
		usage("Source resource " + sourceresource + " must already exist in the resource file.");
	    }

	    NCoding scod = meta.getCodingByName(sourcer.getCoding());
	    NLayer slay = scod.getTopLayer();

	    if (targetr==null) { 
		if (reference==null) {
		    usage("Target resource " + targetresource + " does not already exist in the resource file. \n If that's the case, the reference resource must be specified (-r)! ");
		}
		NResource refr = resourcedata.getResourceByID(reference);
		if (refr==null) {
		    usage("Target resource " + targetresource + " does not already exist in the resource file. \n If that's the case, the reference resource ("+reference+") must exist! ");
		}
		targetr = new NiteResource(sourcer.getResourceGroup(), targetresource, "Projected image of "+sourceresource+" over "+reference+" resource", "AUTOMATIC", (String)null, targetresource, (String)null);
		String obstr=observationname;
		if (obstr==null) { obstr=".*"; }
		targetr.addDependency(refr, obstr);
		resourcedata.addResource(scod.getName(), targetr);
		writeresources=true; 
		Debug.print("Warning: Target resource " + targetresource + " automatically added to resource file! \n Assumed to be same coding as source element! \n Please check resource file for correct dependencies etc.");
	    }

	    nom.preferResourceLoad(sourceresource);
	    nom.preferResourceLoad(targetresource);

	    NCoding tcod = meta.getCodingByName(targetr.getCoding());
	    NLayer tlay = tcod.getTopLayer();

	    if (slay==null || (slay.getLayerType()!=NLayer.TIMED_LAYER && slay.getLayerType()!=NLayer.STRUCTURAL_LAYER)) {
		usage("Source resource " + sourceresource + " must implement a coding whose top layer is timed or structural.");
	    }
	    if (tlay==null || (tlay.getLayerType()!=NLayer.TIMED_LAYER && tlay.getLayerType()!=NLayer.STRUCTURAL_LAYER)) {
		usage("Target resource " + targetresource + " must implement a coding whose top layer is timed or structural.");
	    }

	    NLayer tchildlayer = tlay.getChildLayer();
	    if (tchildlayer==null) {
		Debug.print("Warning: target layer '"+tlay.getName()+"' has no child layer. The mapping will not be very useful!");
	    }

	    sourceelement = ((NElement)slay.getContentElements().get(0)).getName();
	    NElement tel = (NElement)tlay.getContentElements().get(0);
	    targetelement = tel.getName();

	    if (slay.getContentElements().size()>1) {
		Debug.print("Warning: source layer '"+slay.getName()+"' contains multiple elements: using the first one: '"+sourceelement+"'.");
	    }

	    if (tlay.getContentElements().size()>1) {
		Debug.print("Warning: target layer '"+tlay.getName()+"' contains multiple elements: using the first one: '"+targetelement+"'.");
	    }

	    // check the pointer from target to source
	    String role=null;

	    NPointer np = getPointer(tel, slay);
	    if (np==null) {
		Debug.print("INFO: You have no pointer from your target element ("+targetelement+") \n to your source element ("+sourceelement+") in the metadata. Program will continue \n without adding pointers. Or rerun after adding this to the target element \n metadata declaration:\n          <pointer number=\"1\" role=\"source_element\" target=\""+slay.getName()+"\"/>\n");
	    } else {
		role=np.getRole();	    
	    }

	    /* Get the list of observations (either the one named
	     * observation or all of them) */
	    List obslist=null;
	    if (observationname!=null) {
		obslist=new ArrayList();
		obslist.add(meta.getObservationWithName(observationname));
	    } else {
		obslist = meta.getObservations();
	    }
	    
	    int totalsegs=0;
	    int observations=0;

	    /* Now loop through observations cretaing the projected image */
	    for (Iterator oit=obslist.iterator(); oit.hasNext(); ) {
		NObservation nob = (NObservation)oit.next();
		String obsname = nob.getShortName();
		Debug.print("Observation: " + obsname);
		observations++;
		nom.loadData(nob);
		List allels = getElementsInLayer(tchildlayer);
		//Debug.print("Target child layer '"+tchildlayer.getName()+"' has "+allels.size()+" elements.");

		// First get the source elements and order by time
		List sourcelist = nom.getElementsByName(sourceelement);
		if (sourcelist==null || sourcelist.size()<=1) { 
		    System.err.println("No source elements '" + sourceelement + "' in observation " 
				       + obsname +  ": ignoring!");
		    continue;
		}
		Collections.sort(sourcelist, new NOMElementStartTimeComparator());
		Debug.print("Source Elements: " + sourcelist.size());
		totalsegs += sourcelist.size();

		// Now check there are no target elements already existing for this observation
		/* possibly replace with a check for elements belonging to target resource?
		List targetlist = nom.getElementsByName(targetelement);
		if (targetlist!=null && targetlist.size()>0) { 
		    usage("There are already target elements for observation " + 
				obsname + ". Please delete / move them and try again.");
		}
		*/

		// Now we can do the real work: iterate through the
		// source elements creating matching target
		// elements. They will point to the source elements
		for (int sind=0; sind<sourcelist.size(); sind++ ) {
		    NOMElement sourcel = (NOMElement)sourcelist.get(sind);
		    if (sourcel.getResource()==null || (sourcel.getResource()!=sourcer && 
						!sourcer.dependsOn(sourcel.getResource()))) {
			continue;
		    }
		    if (sourcel==null) { continue; }
		    NOMElement tar = new NOMWriteAnnotation(nom, targetelement, obsname, sourcel.getAgent(), targetr);
		    if (role!=null) {
			tar.addPointer(new NOMWritePointer(nom,role,tar,sourcel));
		    }
		    if (tchildlayer==null && tlay.getLayerType()==NLayer.TIMED_LAYER) {
			tar.setStartTime(sourcel.getStartTime());
			tar.setEndTime(sourcel.getEndTime());
		    } else {
			List kids = getElementsInRange(allels, sourcel.getStartTime(), sourcel.getEndTime(), sourcel.getAgent(), targetr);
			//Debug.print(kids.size()+" elements for agent "+sourcel.getAgentName()+" between "+sourcel.getStartTime()+" and "+sourcel.getEndTime());
			if (kids.size()==0) {
			    if (tlay.getLayerType()==NLayer.TIMED_LAYER) {
				tar.setStartTime(sourcel.getStartTime());
				tar.setEndTime(sourcel.getEndTime());
			    }
			} else {
			    for (Iterator kit=kids.iterator(); kit.hasNext(); ) {
				NOMElement kid = (NOMElement)kit.next();
				tar.addChild(kid);
			    }
			}
		    }
		    tar.addToCorpus();
		}
		if (writeresources) { resourcedata.writeResourceFile(); }
		nom.serializeCorpusChanged();
		nom.clearData();
	    }
	} catch (NOMException nex) {
	    nex.printStackTrace();
	} catch (NiteMetaException nme) {
	    nme.printStackTrace();
	} catch (Throwable th) {
	    th.printStackTrace();
	}

    }


    /**
     * Called to start the  application.
     */
    public static void main(String args[]){
	String corpus=null;
	String observation=null;
	String sourceel=null;
	String targetel=null;
	String refres=null;
	String outputdir=".";
	
	if (args.length < 6 || args.length > 10) { usage(); }
	for (int i=0; i<args.length; i++) {
	    String flag=args[i];
	    if (flag.equals("-corpus") || flag.equals("-c")) {
		i++; if (i>=args.length) { usage(); }
		corpus=args[i];
	    } else if (flag.equals("-source") || flag.equals("-s")) {
		i++; if (i>=args.length) { usage(); }
		sourceel=args[i];
	    } else if (flag.equals("-target") || flag.equals("-t")) {
		i++; if (i>=args.length) { usage(); }
		targetel=args[i];
	    } else if (flag.equals("-observation") || flag.equals("-o")) {
		i++; if (i>=args.length) { usage(); }
		observation=args[i];
	    } else if (flag.equals("-reference") || flag.equals("-r")) {
		i++; if (i>=args.length) { usage(); }
		refres=args[i];
	    } else {
		usage();
	    }
	}
	if (corpus == null) { usage("No corpus metadata provided as the -c argument"); }
	if (sourceel == null) { usage("No source element provided as the -s argument"); }
	if (targetel == null) { usage("No target element provided as the -t argument"); }

	ProjectImageResources m = new ProjectImageResources(corpus, observation, sourceel, targetel, refres);
	
    }

    private static void usage () {
	System.err.println("Usage: java ProjectImageResources -c[orpus] <path-to-metadata> -s[ource] <resourcename> -t[arget] <resourcename> [ -o[bservation] <observation_name> ] [ -r[eference] <resourcename> ]");
	System.exit(0);
    }
    private static void usage (String s) {
	System.err.println("ERROR: "+s);
	usage();
    }


}
