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
 * A video player. 
 *
 * @author judyr 
 */
public class NITEVideoPlayer extends NITEMediaPlayer{

    public NITEVideoPlayer(DefaultClock c){
	super(c);
	// make the default size big enough so most videos fit on...
	setSize(new Dimension(400,400)); 
	setTitle("NITE Video player");
    }

    public NITEVideoPlayer(File f, DefaultClock c){
	super(f,c);
	// make the default size big enough so most videos fit on...
	setSize(new Dimension(400,400)); 
	setTitle("NITE Video player");
    }

    public NITEVideoPlayer(File f, DefaultClock c, String n){
	super(f,c,n);
	// make the default size big enough so most videos fit on...
	setSize(new Dimension(400,320)); 
	setTitle("NITE Video player");
    }

    public static void main(String args[]) {
	DefaultClock c = new DefaultClock();
	NITEVideoPlayer p = new NITEVideoPlayer(c);
	p.setID(3);
	c.registerTimeHandler(p);
    }

}
