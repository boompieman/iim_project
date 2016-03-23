/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

import java.io.File;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JFrame;
import javax.swing.JDesktopPane;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.time.DefaultClock;

/** This is a standalone utility to check the JMF playability of media
 * files. Jonathan Kilgour 11/10/04  */

public class Play {
    public Play(String filename) {
	DefaultClock clock = new DefaultClock();
	JFrame frame = new JFrame();
	frame.setTitle("Media Player");
	JDesktopPane desktop = new JDesktopPane();
	NITEMediaPlayer nmp = new NITEVideoPlayer(new File(filename), clock);
	desktop.add(nmp);
	desktop.setSize(new Dimension(400, 500));
	frame.getContentPane().add(desktop);
	frame.setSize(new Dimension(400, 500));
	// Centre on screen
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
    }

    /** Start the application. */
    public static void main(String[] args) {
	String file=null;

	if (args.length < 2 || args.length > 2) { usage(); }

	for (int i = 0; i < args.length; i++) {
	    String flag = args[i];
	    if (flag.equals("-file") || flag.equals("-f")) {
		i++;
		file = args[i];
	    } else {
		usage();
	    }
	}
	if (file == null) {
	    usage();
	}

	Play player = new Play(file);
    }

    private static void usage() {
	System.err.println("Usage: java Play -f <filename>");
	System.exit(0);
    }

}
