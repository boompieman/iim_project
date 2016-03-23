package net.sourceforge.nite.util;

import java.util.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.meta.impl.*;

/** 
 * Metadata and corpus data viewer
 *
 * @author Jonathan Kilgour, UEdin
 */
public class CorpusViewer {
    public static final int HTML=0;
    public static final int TEXT=1;
    public static final int GRAPHICAL=2;
    CorpusVisualiser cv = null;

    /** main constructor - can be called from other programs this way. Valid parameters are
     * <ul>
     * <li>meta NMetaData - metadata file from existing corpus</li>
     * <li>mode int - mode. Use one of the integer constants in this class: HTML, TEXT or GRAPHICAL</li>
     * <li>outputdir String - name the output directory into which
     * results are written (may be null for GRAPHICAL mode
     * output).</li>
     * <li>showcontent boolean - if true (default is false) show, or
     * link to, actual corpus coverage data</li> </ul>
     */
    public CorpusViewer(NMetaData meta, int mode, String outputdir, boolean showcontent) {
	if (mode==HTML) {
	    cv = new HTMLCorpusVisualiser(meta, outputdir, showcontent);
	} else {
	    System.err.println("Sorry: there is no corpus visualiser implementation for your chosen mode");
	}
	if (cv!=null) { cv.visualiseCorpus(); }
    }

    /** act out the visualisation (shouold probably chec if it has been done first?? */
    public void visualise() {
	if (cv!=null) { 
	    cv.visualiseCorpus();
	}
    }

    /** get back a handle for the visualisation */
    public Object getVisualisationObject() {
	if (cv==null) { return null; }
	return cv.getVisualisationObject();
    }

    private static void usage() {
	System.err.println("Usage: java net.sourceforge.nite.util.CorpusViewer -c metadata-filename [ -mode (html|text|graphical)] [ -showcoverage ]");
	// probably shouldn't exit, but it'll do for now.
	System.exit(0);
    }

    /**
     * Called to start the  application.
     * Legal command line arguments are:
     *<ul>
     * <li> -corpus corpus </li>
     * <li> -output <directory> </li>
     * <li> -mode (html|graphical|text) </li>
     * <li> -showcoverage </li>
     *</ul>
     *
     */
    public static void main(String[] args) {
	String corpus=null;
	boolean showcoverage=false;
	int modei=HTML;
	String outputdir="corpusdoc";
	
	if (args.length < 2 || args.length > 7) { usage(); }
	for (int i=0; i<args.length; i++) {
	    String flag=args[i];
	    if (flag.equals("-corpus") || flag.equals("-c")) {
		i++; if (i>=args.length) { usage(); }
		corpus=args[i];
	    } else if (flag.equals("-mode") || flag.equals("-m")) {
		i++; if (i>=args.length) { usage(); }
		String modes=args[i];
		if (modes.matches("graphic.*")) { modei=GRAPHICAL; }
		else if (modes.matches("text.*")) { modei=TEXT; }
		else if (!modes.matches("html")) { 
		    System.err.println("Failed to decode mode: " + modes);
		    usage();
		}
	    } else if (flag.equals("-output") || flag.equals("-o")) {
		i++; if (i>=args.length) { usage(); }
		outputdir=args[i];
	    } else if (flag.equals("-showcoverage") || flag.equals("-s")) {
		showcoverage=true;
	    } else {
		usage();
	    }
	}
	if (corpus == null) { usage(); }

	NiteMetaData meta=null;
	try {
	    meta = new NiteMetaData(corpus);
	} catch (NiteMetaException nme) {
	    System.err.println("Failed to find metadata file " + corpus);
	    usage();
	}	
	
	CorpusViewer cv = new CorpusViewer(meta, modei, outputdir, showcoverage);
    }

}
