/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite.impl;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.nite.nom.*;
import net.sourceforge.nite.nom.link.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.util.Debug;

/**
 * NOMFile is an attempt to take the file and directory naming
 * approach out of NOMCorpus and into a coherent package.
 *
 * @author Jonathan Kilgour
 */
public class NOMFile  {
    private NFile coding=null;
    protected NAgent agent=null;
    private NObservation observation=null;
    private NMetaData metadata=null;
    private String filename=null;
    private String colour=null;
    private String indexcolour=null;
    private static final String XML = ".xml";
    private static final String DOT = "\\.";
    private static final String HASH = "#";
    private static final String SEP = ".";
    protected static final int CODING = 0;
    protected static final int RESOURCE = 1;
    private int type = CODING;
    
    public NOMFile(NMetaData m, NFile c, NObservation o, NAgent a) {
	metadata=m;
	coding=c;
	observation=o;

	if (c instanceof NCoding && ((NCoding)coding).getType()==NCoding.AGENT_CODING) {
	    agent=a;
	}
	if (!(c instanceof NCoding) || c instanceof NCorpusResourceCoding) {
	    type=RESOURCE;
	} 
    }

    /** Make a new NOM file from a file or colour name */
    public NOMFile(NMetaData m, String fn) {
	try {
	    metadata=m;
	    String f= fn;
	    int ind = f.lastIndexOf(File.separator);
	    if (ind>0) {
		f = f.substring(ind+1, f.length());
	    }
	    filename=new String(f);
	    ind = f.lastIndexOf(XML);
	    if (ind>0) {
		f = f.substring(0,ind);
	    }
	    ind = f.lastIndexOf(HASH);
	    if (ind>0) {
		f = f.substring(0,ind);
	    }
	    colour=f;
	    //Debug.print("NOMFile colour is now: " + f);
	    if (f.indexOf(SEP)>=0) { 
		String [] parts = f.split(DOT);
		//Debug.print("Non-resource File: " + f + "; parts: " + parts.length + "; index: " + f.indexOf(SEP), Debug.DEBUG);
		observation = metadata.getObservationWithName(parts[0]);
		coding = metadata.getCodingByName(parts[parts.length-1]);
		//System.out.println("Obs: " + parts[0] + "; coding: " + parts[parts.length-1]);
		if (((NCoding)coding).getType()==NCoding.AGENT_CODING) {
		    String astr = parts[1];
		    for (Iterator ait=metadata.getAgents().iterator(); ait.hasNext(); ) {
			NAgent nag = (NAgent)ait.next();
			if (astr.equals(nag.getShortName())) {
			    agent = nag;
			    break;
			}
		    }
		}
	    } else { // we have a non-observation element so set type
		coding = metadata.getNFileByName(f);
		type=RESOURCE;
		//System.err.println("Resource NFile for " + f + " is " + coding.getName());
	    }
	} catch (Exception ex) {
	    // we'll just get some null returns if failure.
	}
	
    }
    
    public NFile getNFile() {
	return coding;
    }
    
    public NObservation getObservation() {
	return observation;
    }

    public NAgent getAgent() {
	return agent;
    }

    public int getType() {
	return type;
    }
    
    /** this is like getColour but we ignore any agent. That's just
     * because we use this kind of colour to index for files (i.e. we
     * don't allow the different agents of a coding to be in separate
     * resources) */
    public String getIndexColour() {
	if (indexcolour!=null) { return indexcolour; }
	try {
	    indexcolour="";
	    if (type==CODING && observation!=null) {
		indexcolour = observation.getShortName() + SEP;
	    }
	    indexcolour += coding.getName();
	} catch (Exception ex) { }
	return indexcolour;
    }

    /** return the 'colour' of the file - which is normally just the
     * filename without the .xml extension */
    public String getColour() {
	if (colour!=null) { return colour; }
	try {
	    colour="";
	    if (type==CODING && observation!=null) {
		colour = observation.getShortName() + SEP;
	    }
	    if (type==CODING && ((NCoding)coding).getType()==NCoding.AGENT_CODING && agent!=null) {
		colour += agent.getShortName() + SEP;
	    }
	    colour += coding.getName();
	} catch (Exception ex) { }
	return colour;
    }

    public String getFilename() {
	filename = getColour() + XML;
	return filename;
    }

    public String getFullFilename() {
	String path = "";
	if (type==CODING) {
	    if (coding!=null) {
		path = ((NCoding)coding).getPath() + File.separator;
	    }
	    return path + getFilename();
	} else {
	    // this isn't really right - but we shouldn't rely on
	    // metadata locations - they could be overridden by a
	    // resource file which we can't access from here!
	    return getFilename();
	}
    }
    

    /** return the list of file names that could be children of this
     * element. Note that the only time this list is not a singleton
     * is when we are in an interaction coding and the child layer is
     * an agent coding. */
    public List getChildCodingFilenames(NCoding child) {
	return null;
    }

    /** return the list of file names that could be children of this
     * element. Note that the only time this list is not a singleton
     * is when we are in an interaction coding and the child layer is
     * an agent coding. */
    public String getParentCodingFilenames(NCoding parent) {
	return null;
    }

    /** return the list of file names that could be children of this
     * element. Note that the only time this list is not a singleton
     * is when we are in an interaction coding and the child layer is
     * an agent coding. */
    public String getCodingFilename(NCoding other) {
	return null;
    }
}
