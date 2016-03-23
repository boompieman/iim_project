/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nom.nomwrite.impl;

import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.util.Debug;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * NOMWriteContext holds information about the currently loaded files. 
 *
 * @author jonathan 
 */
public class NOMWriteContext implements NOMContext {
    NOMCorpus nom=null;
    Hashtable noloads = new Hashtable(); // corpus level loads
    Hashtable oloads = new Hashtable();  // per-observation loads

    /** constructor takes the NOM that we have information about  */
    public NOMWriteContext(NOMCorpus corpus) {
	this.nom=corpus;
    }

    /* return the observation names for which there is some data
     * loaded */
    public List getLoadedObservationNames() {
	if (nom==null) return null;
	List obs=nom.getLoadedObservations();
	if (obs==null) { return null; }
	List obsnames = new ArrayList();
	for (Iterator oit=obs.iterator(); oit.hasNext(); ) {
	    NObservation ob = (NObservation)oit.next();
	    obsnames.add(ob.getShortName());
	}
	return obsnames;
    }

    /* return the load type given observation and NFile (where NFile
     * can be NCoding, NCorpusResource, NOntology or
     * NObjectSet). Returns one of UNLOADED meaning nothing has been
     * loaded for this NFile; METADATA meaning the NFile is
     * singly-loaded using the metadata path; METADATA_MULTI meaning a
     * multi-annotator load has been applied using the loadReliability
     * approach; RESOURCE meaning the NFile is singly loaded using a
     * resource path; RESOURCE_MULTI meaning it is multiply loaded
     * using resources. */
    public int getLoadType(String observation, NFile nfile) {
	Hashtable myoloads = (Hashtable)oloads.get(observation);
	if (myoloads==null) { return NOMContext.UNLOADED; }
	LoadFileSet fload = (LoadFileSet)myoloads.get(nfile);
	if (fload==null) { return NOMContext.UNLOADED; }
	return fload.ftype;
    }

    /** only returns a non-null List if the load type is
     * METADATA_MULTI - if so, returns a List of String elements: the
     * annotators IDs */
    public List getAnnotators(String observation, NFile nfile) {
	Hashtable myoloads = (Hashtable)oloads.get(observation);
	if (myoloads==null) { return null; }
	LoadFileSet fload = (LoadFileSet)myoloads.get(nfile);
	if (fload==null) { return null; }
	return fload.annotators;
    }

    /** only returns a non-null List if the load type RESOURCE - if
     * so, returns the NResource itself */
    public NResource getResource(String observation, NFile nfile) {
	Hashtable myoloads = (Hashtable)oloads.get(observation);
	if (myoloads==null) { return null; }
	LoadFileSet fload = (LoadFileSet)myoloads.get(nfile);
	if (fload==null) { return null; }
	Enumeration keys = fload.resources.keys();
	if (fload.resources.size()==1) {
	    return (NResource)fload.resources.keys().nextElement();
	}
	return null;
    }

    /** Only returns a non-null Set if the type is RESOURCE or
     * RESOURCE_MULTI - return all resources loaded for this
     * combination of observation and NFile as a Set of NResource
     * elements. */
    public Set getResources(String observation, NFile nfile) {
	Hashtable myoloads = (Hashtable)oloads.get(observation);
	if (myoloads==null) { return null; }
	LoadFileSet fload = (LoadFileSet)myoloads.get(nfile);
	if (fload==null) { return null; }
	return fload.resources.keySet();
    }

    /** Only returns a non-null List if the type is RESOURCE or
     * RESOURCE_MULTI - return all resources loaded for this
     * combination of observation and NFile as a List of Strings: the
     * result of calling getDescription on each NResource. */
    public List getResourceDescriptions(String observation, NFile nfile) {
	Set resources=getResources(observation,nfile);
	if (resources==null) { return null; }
	List retdesc=new ArrayList();
	for (Iterator rit=resources.iterator(); rit.hasNext(); ) {
	    NResource nr = (NResource)rit.next();
	    retdesc.add(nr.getDescription());
	}
	return retdesc;
    }

    /** Only returns a non-null List if the type is RESOURCE or
     * RESOURCE_MULTI - return all resources loaded for this
     * combination of observation and NFile as a List of Strings: the
     * result of calling getID on each NResource. */
    public List getResourceIDs(String observation, NFile nfile) {
	Set resources=getResources(observation,nfile);
	if (resources==null) { return null; }
	List retdesc=new ArrayList();
	for (Iterator rit=resources.iterator(); rit.hasNext(); ) {
	    NResource nr = (NResource)rit.next();
	    retdesc.add(nr.getID());
	}
	return retdesc;
    }

    /** return a List of NFile elements that have some loaded data
     * corresponding to them for the given observation name. This will
     * not contain any corpus level files loaded unless the observation
     * String is null. */
    public List getLoadedNFilesForObservation(String observation) {
	if (observation==null) {
	    return new ArrayList(noloads.keySet());
	}
	Hashtable myoloads = (Hashtable)oloads.get(observation);
	if (myoloads==null) return null;
	return new ArrayList(myoloads.keySet());
    }

    /** return a List of NFile elements that have no loaded data
     * corresponding to them for the given observation name. This will
     * not contain any unloaded corpus level files unless the observation
     * String is null. */
    public List getUnloadedNFilesForObservation(String observation) {
	return null;
    }

    /** return textual details of a hashtable of LoadFileSet elements
     * indexed by NFile */
    private String textForLoadSet(LoadFileSet lf) {
	if (lf==null) { return ""; }
	String lsret="";
	if (lf.ftype==NOMContext.RESOURCE && lf.resources!=null) {
	    for (Enumeration rit=lf.resources.keys(); rit.hasMoreElements(); ) {
		NResource nr = (NResource)rit.nextElement();
		lsret += "    Resource " + nr.getID() + " files:\n";
		for (Iterator fit=((List)lf.resources.get(nr)).iterator(); fit.hasNext(); ) {
		    lsret += "      "+(String)fit.next()+"\n";
		}
	    }
	} else {
	    if (lf.annotators!=null) {
		lsret += "    Annotators:\n";
		for (Iterator ait=lf.annotators.iterator(); ait.hasNext(); ) {
		    lsret += "      "+(String)ait.next()+"\n";
		}
	    }
	    lsret += "    Files:\n";
	    for (Iterator fit=lf.filenames.iterator(); fit.hasNext(); ) {
		lsret += "      "+(String)fit.next()+"\n";
	    }
	}
	return lsret;
    }

    /** return textual details of a hashtable of LoadFileSet elements
     * indexed by NFile */
    private String textForHashtable(Hashtable hash) {
	if (hash==null) { return ""; }
	String hashret="";
	for (Enumeration oit=hash.keys(); oit.hasMoreElements(); ) {
	    NFile nf = (NFile)oit.nextElement();
	    hashret += "  Coding " + nf.getName() + "\n";
	    hashret += textForLoadSet((LoadFileSet)hash.get(nf));
	}
	return hashret;
    }

    /** return a String containing the context in plain text -
     * formatted for human reading */
    public String getTextualContext() {
        String ret = "Corpus-wide data:\n";
	ret += textForHashtable(noloads);
	for (Enumeration oit=oloads.keys(); oit.hasMoreElements(); ) {
	    String obs = (String)oit.nextElement();
	    ret += "\nObservation " + obs + "\n";
	    ret += textForHashtable((Hashtable)oloads.get(obs));
	}
	return ret;
    }

    /** return an XML formatted String containing the context */
    public String getXMLContext() {
	return "<!-- \n" + getTextualContext() + "-->\n";
    }

    /** clear information about loaded data */
    public void clear() {
	oloads = new Hashtable();	
	noloads = new Hashtable();
    }

    /** clear information about loaded data for the given observation */
    public void clear(String observation) {
	oloads.put(observation, null);
    }

    /** add a loaded file to the context store - caller must work out
     * type and should pass resource only if it's a resource load and
     * annotator only if it's an old-style multi-annotator load. */
    public void addFile(String observation, NFile nfile, String filename, int type,
			   NResource resource, String annotator) {
	//System.out.println("Add: " + observation + "; " + nfile.getName() + "; " + filename + "; type: " + type + "; resource: " + resource + "; annotator: "  + annotator);
	if (nfile==null) {
	    Debug.print("NOM CONTEXT ERROR: cannot add file '"+filename+"' to context without its metadata information", Debug.IMPORTANT); 
	    return;
	}
	if (filename==null) {
	    Debug.print("NOM CONTEXT ERROR: cannot add file to context without a filename", Debug.IMPORTANT); 
	    return;
	}
	if (observation==null) {
	    LoadFileSet addset = (LoadFileSet)noloads.get(nfile);
	    if (addset==null) {
		addset=new LoadFileSet(type,filename,resource,annotator);
		noloads.put(nfile,addset);
	    } else {
		addset.add(type,filename,resource,annotator);
	    }
	} else {
	    Hashtable obsfiles = (Hashtable)oloads.get(observation);
	    if (obsfiles==null) {
		obsfiles=new Hashtable();
		oloads.put(observation,obsfiles);
	    }
	    LoadFileSet addset=(LoadFileSet)obsfiles.get(nfile);
	    if (addset==null) {
		addset=new LoadFileSet(type,filename,resource,annotator);
		obsfiles.put(nfile,addset);
	    } else {
		addset.add(type,filename,resource,annotator);
	    }
	}
    }

    private class LoadFileSet {
	int ftype=UNLOADED;
	Hashtable resources=new Hashtable(); // store filenames in here if resources!
	List filenames=new ArrayList();
	List annotators=new ArrayList();
	
	public LoadFileSet(int ftype, String filename, NResource resource, String annotator) {
	    this.ftype=ftype;
	    if (ftype==NOMContext.RESOURCE && resource!=null) {
		List fls = new ArrayList();
		fls.add(filename);
		resources.put(resource, fls);
	    } else {
		filenames.add(filename);
		if (annotator!=null) {
		    annotators.add(annotator);
		}
	    }
	}

	public void add(int ftype, String filename, NResource resource, String annotator) {
	    if (this.ftype!=ftype) {
		Debug.print("ERROR IN NOMCONTEXT: trying to load files in the same class in multiple ways: " +ftype+"; "+this.ftype, Debug.IMPORTANT);
	    }
	    if (ftype==NOMContext.RESOURCE && resource!=null) {
		List fls = (List)resources.get(resource);
		if (fls==null) {
		    fls = new ArrayList();
		}
		fls.add(filename);
		resources.put(resource, fls);
	    } else {
		filenames.add(filename);
		if (annotator!=null) {
		    annotators.add(annotator);
		}
	    }
	}
    }
}
