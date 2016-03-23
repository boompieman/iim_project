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
 * This process examines two element streams and produces a third:
 * an projected image of the first timed stream onto the second. Inputs:
 * 1. the timed elements to be projected
 * 2. The element name to be produced (this must have children in an
       existing timed stream)
 **/
public class ProjectImage { 
    
    NOMWriteCorpus nom;
    NiteMetaData meta;
    String corpusname;
    String observationname;
    String sourceelement;
    String targetelement;
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
    private List getElementsInRange(List allels, double st, double en, NAgent agent) {
	List retlist = new ArrayList();
	for (Iterator elit=allels.iterator(); elit.hasNext(); ) {
	    NOMElement el = (NOMElement)elit.next();
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


    public ProjectImage(String corp, String obs, String source, String target) {
	corpusname = corp;
	observationname=obs;
	sourceelement=source;
	targetelement=target;

	try {
	    /* First load the metadata */
	    meta = new NiteMetaData(corpusname);

	    /* set up a new NOM Corpus */
	    nom = new NOMWriteCorpus(meta);
	    nom.setLogStream(System.err);

	    /* Check the validity of the source and target element */
	    NElement sel = meta.getElementByName(sourceelement);
	    NElement tel = meta.getElementByName(targetelement);
	    if (sel==null) { 
		usage("Source element " + sourceelement + " must already exist in the metadata file.");
	    }
	    if (tel==null) { 
		usage("Target element " + targetelement + " must already exist in the metadata file.");
	    }
	    NLayer slay = sel.getLayer();
	    NLayer tlay = tel.getLayer();
	    if (slay==null || (slay.getLayerType()!=NLayer.TIMED_LAYER && slay.getLayerType()!=NLayer.STRUCTURAL_LAYER)) {
		usage("Source element " + sourceelement + " must be in a timed or structural layer.");
	    }
	    if (tlay==null || (tlay.getLayerType()!=NLayer.TIMED_LAYER && tlay.getLayerType()!=NLayer.STRUCTURAL_LAYER)) {
		usage("Target element " + targetelement + " must be in a timed or structural layer.");
	    }

	    NLayer tchildlayer = tlay.getChildLayer();
	    if (tchildlayer==null) {
		Debug.print("Warning: target layer '"+tlay.getName()+"' has no child layer. The mapping will not be very useful!");
	    }

	    // check the pointer from target to source
	    String role="source_element";
	    NPointer np = getPointer(tel, slay);
	    if (np==null) {
		Debug.print("WARNING: You have no pointer from your target element \n to your source element in the metadata. Program will continue \n but to maintain validity please add this to your target element \n declaration:\n          <pointer number=\"1\" role=\""+role+"\" target=\""+slay.getName()+"\"/>\n");
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
		Debug.print("Target child layer '"+tchildlayer.getName()+"' has "+allels.size()+" elements.");

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
		List targetlist = nom.getElementsByName(targetelement);
		if (targetlist!=null && targetlist.size()>0) { 
		    usage("There are already target elements for observation " + 
				obsname + ". Please delete / move them and try again.");
		}

		// Now we can do the real work: iterate through the
		// source elements creating matching target
		// elements. They will point to the source elements
		for (int sind=0; sind<sourcelist.size(); sind++ ) {
		    NOMElement sourcel = (NOMElement)sourcelist.get(sind);
		    if (sourcel==null) { continue; }
		    NOMElement tar = new NOMWriteAnnotation(nom, targetelement, obsname, sourcel.getAgent());
		    tar.addPointer(new NOMWritePointer(nom,role,tar,sourcel));
		    if (tchildlayer==null && tlay.getLayerType()==NLayer.TIMED_LAYER) {
			tar.setStartTime(sourcel.getStartTime());
			tar.setEndTime(sourcel.getEndTime());
		    } else {
			List kids = getElementsInRange(allels, sourcel.getStartTime(), sourcel.getEndTime(), sourcel.getAgent());
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
	String outputdir=".";
	
	if (args.length < 2 || args.length > 8) { usage(); }
	for (int i=0; i<args.length; i++) {
	    String flag=args[i];
	    if (flag.equals("-corpus") || flag.equals("-c")) {
		i++; if (i>=args.length) { usage(); }
		corpus=args[i];
	    } else if (flag.equals("-sourceelement") || flag.equals("-s")) {
		i++; if (i>=args.length) { usage(); }
		sourceel=args[i];
	    } else if (flag.equals("-targetelement") || flag.equals("-t")) {
		i++; if (i>=args.length) { usage(); }
		targetel=args[i];
	    } else if (flag.equals("-observation") || flag.equals("-o")) {
		i++; if (i>=args.length) { usage(); }
		observation=args[i];
	    } else {
		usage();
	    }
	}
	if (corpus == null) { usage("No corpus metadata provided as the -c argument"); }
	if (sourceel == null) { usage("No source element provided as the -s argument"); }
	if (targetel == null) { usage("No target element provided as the -t argument"); }

	ProjectImage m = new ProjectImage(corpus, observation, sourceel, targetel);
	
    }

    private static void usage () {
	System.err.println("Usage: java ProjectImage -corpus <path-to-metadata> -sourceelement <elementname> -targetelement <elementname> [ -observation <observation_name> ] ");
	System.exit(0);
    }
    private static void usage (String s) {
	System.err.println("ERROR: "+s);
	usage();
    }


}
