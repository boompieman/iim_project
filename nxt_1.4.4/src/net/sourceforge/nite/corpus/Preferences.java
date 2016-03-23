/* NXT Corpus Preferences
 * Copyright (c) 2008, Jean Carletta, Jonathan Kilgour
 * Created by Jonathan Kilgour 30/4/08 
 */
package net.sourceforge.nite.corpus;

import net.sourceforge.nite.search.S;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;

/** This is similar to the search.S class but adds preferences for
 * plugins */
public class Preferences implements java.io.Serializable {

    public static String[]   bookmarks    = {
	"($a)($b):   $a % $b",
	"($a)($b):   $a [[ $b",
	"($a)($b):   $a ]] $b",
	"($a)($b):   $a @ $b",
	"($a)($b):   $a [] $b",
	"($a)($b):   $a # $b",
	"($a)($b):   $a ][ $b",
	"($a)($b):   $a << $b",
	"($b)($c): $b^$c :: ($a): $a^$b",
	"($a)($b)($c): ($a ^ $b or $b ^ $a) and !($a == $b) and $c ^2  $a"
    };

    public List      corpora         = new ArrayList();
    public HashMap   names           = new HashMap();

    public ArrayList bookmarksNames   = new ArrayList();
    public ArrayList bookmarksQueries = new ArrayList();

    public boolean   autoloaded       = false;
    public boolean   corpusLoaded     = false;

    public File      saveDir = new File( System.getProperty("user.dir") );


    // Added by Jonathan Kilgour 30/4/08
    // These are tools that can be used by the corpus manager - users
    // can add their own to the list.
    public static String[] plugs = {
	//    	"net.sourceforge.nite.nxt.GUI",
	//"net.sourceforge.nite.corpus.plugin.ResourceTool",
	//"net.sourceforge.nite.corpus.plugin.OtherTool",
	"net.sourceforge.nite.corpus.plugin.OntologyTool"
    };
    public List plugins = new ArrayList();

    /** Initialize values and then read anything we can find from the
     * default file location. */
    public Preferences () {
	initialize();
    }

    /** Initialize values and then read anything we can find from the
     * given directory location. */
    public Preferences (String dir) {
	initialize();
	saveDir=new File(dir);
    }

    private void initialize() {
	for( int i = 0; i<bookmarks.length; i++ ) {
	    bookmarksNames.add( bookmarks[i] );
	    bookmarksQueries.add( bookmarks[i] );
	}
	for (int p = 0; p<plugs.length; p++) {
	    plugins.add(plugs[p]);
	}
    }

    /** return the directory in which we save (normally the user's
     * home directory) */
    public File getSaveDir() {
	return saveDir.exists() ?
	    new File( System.getProperty("user.dir") ) :
	    saveDir;
    }
}
