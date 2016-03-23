/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.mediaviewer;
import java.io.File;
import java.awt.Dimension;
import net.sourceforge.nite.time.DefaultClock;

/**
 * An audio player. 
 */
public class NITEAudioPlayer extends NITEMediaPlayer {

    public NITEAudioPlayer(DefaultClock c){
	super(c);
	// make the default size big enough so most audios fit on...
	setSize(new Dimension(400,120)); 
	setTitle("NITE Audio player");
    }

    public NITEAudioPlayer(File f, DefaultClock c){
	super(f,c);
	// make the default size big enough so most audios fit on...
	setSize(new Dimension(210,55)); 
	setTitle("NITE Audio player");
    }

    public NITEAudioPlayer(File f, DefaultClock c, String n){
	super(f,c,n);
	// make the default size big enough so most audios fit on...
	setSize(new Dimension(210,55)); 
	setTitle("NITE Audio player");
    }

}
