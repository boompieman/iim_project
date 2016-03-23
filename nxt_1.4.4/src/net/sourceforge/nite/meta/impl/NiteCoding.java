/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.meta.impl;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import net.sourceforge.nite.meta.NLayer;
import net.sourceforge.nite.meta.NMetaData;
/** 
 * A coding as referred to in the metadata
 *
 * @author jonathan
 */

public class NiteCoding implements net.sourceforge.nite.meta.NCoding {
    private String name;
    private int type = AGENT_CODING;
    private NLayer top_layer=null;
    private NLayer bottom_layer=null;
    private ArrayList layers=null;
    private String path=null;
    private NMetaData metadata=null;
    
    public NiteCoding (NMetaData meta, String name, int type, NLayer top, NLayer bottom, String path) {
	this.metadata=meta;
	this.name=name;
	this.top_layer=top;
	this.bottom_layer=bottom;
	if (type==INTERACTION_CODING) { this.type=type; }
	this.path=path;
    }

    /** returns the name of the coding - used in file names */
    public String getName() {
	return name;
    }

    /** returns the name of the coding - used in file names */
    public String getFileName() {
	return name;
    }


    /** The type of the coding - returns either AGENT_CODING or
        INTERACTION_CODING */
    public int getType() {
	return type;
    }

    /** returns the top layer in the coding */
    public NLayer getTopLayer() {
	return top_layer;
    }

    /** set the top layer in the coding - should only be used by the
        corpus-building routines and not by users. */
    public void setTopLayer(NiteLayer layer) {
	top_layer=layer;
    }

    /** returns the bottom layer in the coding */
    public NLayer getBottomLayer() {
	return bottom_layer;
    }

    /** set the bottom layer in the coding - should only be used by the
        corpus-building routines and not by users. */
    public void setBottomLayer(NiteLayer layer) {
	bottom_layer=layer;
    }

    /** Returns a List of "NLayer"s belonging to this coding, in
        arbitrary order */
    public List getLayers() {
	if (layers==null) {
	    layers = new ArrayList();
	    NLayer l = bottom_layer;
	    while (l!=null) {
		layers.add(l);
		l=l.getParentLayerInCoding(this);
	    }
	}
	return layers;
    }

    /** returns the directory path where this coding is stored, as it
        is in the metadata file - relative to the metadata file.  */
    public String getRelativePath() {
	if (path==null || path.length()==0) {
	    if (type==INTERACTION_CODING && metadata.getRelativeInteractionCodingPath()!=null) {
		return metadata.getRelativeInteractionCodingPath();
	    }
	    if (type==AGENT_CODING && metadata.getRelativeAgentCodingPath()!=null) {
		return metadata.getRelativeAgentCodingPath();
	    }
	    return metadata.getRelativeCodingPath();
	} else {
	    return path;
	}
    }

    /** returns the directory path where this coding is stored
	- relative to the working directory in which java is
        running (or absolute if metadata path is absolute) */
    public String getPath(){
	String codingpath="";
	String relpath = getRelativePath();
	boolean url=false;
	try {
	    URL u = new URL(relpath);
	    url=true;
	} catch (MalformedURLException muex) { }
	if ( !url && (relpath==null || !(new File(relpath).isAbsolute())) ) {
	    if (((NiteMetaData)metadata).getPath() != null) {
		codingpath=((NiteMetaData)metadata).getPath() + File.separator;
	    }
	}
	if (relpath!=null) {
	    codingpath += relpath;
	}
	if (codingpath.equals("")) { codingpath="."; }

	return codingpath;
    }

    private String codingPath(String coding_name, int coding_type) {
	String xp = "";
	if (coding_type==INTERACTION_CODING) {
	    xp = NiteMetaConstants.intercodingxpath;
	} else {
	    xp = NiteMetaConstants.agentcodingxpath;
	}
	xp += "[@name='" + coding_name + "']/@path";
	return xp;
    }

    /** sets the directory path of the coding - if relative, then
     * it'll be relative to the metadata */
    public void setPath(String path)  {
	try {
	    String cp = codingPath(name, type);
	    //System.out.println("Coding Path: " + cp);
	    Node n=XPathAPI.selectSingleNode(((NiteMetaData)metadata).getDocument(), cp);
	    if (n!=null) { 
		n.setNodeValue(path);
	    } else {
		cp =cp.substring(0,cp.lastIndexOf("/"));
		n=XPathAPI.selectSingleNode(((NiteMetaData)metadata).getDocument(), cp);
		if (n==null) {
		    System.err.println("WARNING: Attempting to set the Coding Path when no coding named " + name + " is present");
		    return;
		}
		((Element)n).setAttribute("path", path);

	    }

	} catch (TransformerException e) {
	    e.printStackTrace();
	} 
	this.path=path;
    }

}

