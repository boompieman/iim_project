/* NXT Corpus Preferences
 * Copyright (c) 2008, Jean Carletta, Jonathan Kilgour
 * Created by Jonathan Kilgour 30/4/08 
 */
package net.sourceforge.nite.corpus;

import net.sourceforge.nite.search.S;
import java.util.List;
import java.io.*;
import net.sourceforge.nite.util.*;

/** This is a convenience class that provides a standard location for
 * corpus preferences and backwards compatibility with the old style
 * preferences so people don't lose bookmarks */
public class PreferenceLoader {
    private static final String DAT_SERIALISATION = ".nxtSearch";
    private String directory = System.getProperty("user.home");

    public PreferenceLoader(String directory) {
	this.directory=directory;
    }

    public Preferences getPreferences() {
	File file = new File(directory + File.separator + DAT_SERIALISATION);
	Preferences preferences = new Preferences();
	try {
	    ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
	    try {
		preferences  = (Preferences)is.readObject();
	    } catch (Exception ex) {
		System.out.println("Inner catch");
		is = new ObjectInputStream(new FileInputStream(file));
		//S sprefs = new S();
		S sprefs  = (S)is.readObject();
		Debug.print("Reading old-style preferences - transforming.." + sprefs);
		preferences.corpora = sprefs.corpora;
		preferences.names = sprefs.names;
		preferences.bookmarksQueries = sprefs.bookmarksQueries;
		preferences.bookmarksNames = sprefs.bookmarksNames;
		preferences.autoloaded = sprefs.autoloaded;
		preferences.corpusLoaded = sprefs.corpusLoaded;
		//preferences.saveDir = sprefs.saveDir;
	    }

	} catch (Exception e) {
	    Debug.print("Failed to read preferences ..." + file.getPath(), Debug.ERROR);
	    //e.printStackTrace();
	    file.delete();
	    preferences = new Preferences();
	}
	return preferences;
    }

    /** save preferences to disk */
    public void savePreferences(Preferences preferences) {
	try {
	    File file = new File( directory + File.separator + DAT_SERIALISATION);
	    if( file.exists() ) { file.delete(); }
	    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
	    out.writeObject(preferences);
	    out.close();
	} catch (Exception e){
	    Debug.print(e.toString(), Debug.ERROR);
	}
    }


}
