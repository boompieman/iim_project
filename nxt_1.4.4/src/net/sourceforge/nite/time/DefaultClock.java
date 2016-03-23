/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.time;

import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.meta.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.io.File;
import javax.swing.JInternalFrame;
import javax.swing.JTextPane;
import java.awt.Rectangle;
import java.awt.Container;
import java.awt.Point;
import java.awt.Dimension;
import javax.swing.JComponent;

/**
 * An implementation of the Clock interface.
 *
 * @author jeanc, jonathan, judy 
 */
public class DefaultClock implements Clock {
    ClockFace ui;
    private double time; 
    public Set timeHandlers = new HashSet(); // of type TimeHandler
    private NMediaHandler nmh=null;
    private PlayingTimeHandler masterPlayer=null; // the player that controls time of this clock
    private int synccounter=0;
    private final static int MAXSYNC=20; // after this number of polls
					 // we'll try to sync all media

    private double LOW_CUTOFF=0.2;  // print warning
    private double HIGH_CUTOFF=1.0; // intervene
    private boolean DEBUG=false;     // verbose
    private int intervention=PAUSE;  // intervention type for drift

    /** Register a TimeHandler that will henceforth be able to set and
     * get times from this Clock. This now does a different thing for
     * PlayingTimeHandlers, which are assumed to be continuous, and
     * TimeHandlers which are assumed to be
     * discrete. PlayingTimeHandlers get their own MediaHandler, so
     * all media instances can be controlled 'as one'. */ 
    public void registerTimeHandler(TimeHandler display) {
	if (display instanceof PlayingTimeHandler) {
	    // wait until we get a register max time - this makes sure
	    // all registered playing time handlers are actually
	    // playing a media file and can sensibly be asked what
	    // time it is!
	    /*
	    if (nmh==null) {
		nmh=new NMediaHandler(this);
		timeHandlers.add(nmh);
	    }
	    if (ui!=null) { ui.setSlaveMode(true); }
	    nmh.registerPlayer((PlayingTimeHandler)display);
	    */
	} else {
	    timeHandlers.add(display);
	    if (display.getClock()!=this) {
		display.setClock(this);
	    }
	}
    } 

    /** Called by registered displays to allow the slider to be set
     * correctly. This now also registers the playing time handlers,
     * so we can make sure they're all actually playing a media
     * file. */
    public void registerMaxTime(int max, PlayingTimeHandler display) {
	boolean makemaster=false;
	if (nmh==null) {
	    nmh=new NMediaHandler(this);
	    timeHandlers.add(nmh);
	    makemaster=true;
	}
	if (ui!=null) { 
	    ui.setSlaveMode(true); 
	    ui.registerMaxTime(max);
	}
	nmh.registerPlayer((PlayingTimeHandler)display);
	if (makemaster) {
	    setMasterPlayer(display);
	}
    }

    /** align all signals to the master signal */
    private void alignTimes() {
	if (nmh==null || nmh.playingHandlers==null) { return; }
	if (masterPlayer!=null && !masterPlayer.pastEndTime(time)) { 
	    time=masterPlayer.getTime(); 
	}
	
	double realtime = time;
	Iterator it = nmh.playingHandlers.iterator();
	PlayingTimeHandler tochange=null;
	double maxout = 0.0;
	String fs = "Real time: " + time + " ";
	if (masterPlayer!=null) { fs+= "("+  masterPlayer.getFileName() + " " + 
				      masterPlayer.pastEndTime(time) + "). "; }
	else { fs += "(nil). "; }
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    if (th.isMaster()) { continue; }
	    if (th.pastEndTime(realtime)) { continue; }
	    double gt = th.getTime();
	    double diff = Math.abs(gt-realtime);
	    fs += "'" + th.getFileName() + "': " + gt + "; ";	    
	    if (diff>LOW_CUTOFF && diff>maxout) {
		tochange=th;
		maxout=diff;
	    } 
	}

	if (DEBUG) { 
	    System.err.println(fs); 
	}

	if (tochange!=null) {
	    if (maxout > HIGH_CUTOFF) {
		if (intervention==WARN) {
		    System.err.println("Signals have become out of sync. You may want to pause the playback and play again to resynchronize");
		} else {
		    System.err.println("WARNING: Player '" + tochange.getFileName() + "' is " + maxout + " seconds out. Pausing to re-align signals. Please wait.");
		    if (ui!=null) {
			JDesktopPane desktop=((JInternalFrame)ui).getDesktopPane();
			//JOptionPane.showMessageDialog(null,"Syncing: if this happens too often you may be running too many videos", "Syncing signals!", JOptionPane.PLAIN_MESSAGE);
		    }
		    pause();
		    play();
		}
	    }
	    else if (DEBUG) {
		System.err.println("WARNING: Player '" + tochange.getFileName() + "' is " + maxout + " seconds out (no action taken)");
	    }
	    //tochange.acceptTimeChange(realtime);
	} else if (DEBUG) {
	    System.err.println("All players within " + LOW_CUTOFF + "s of real time.");
	}
    }

    /** Since we allow the clock to run past teh end time of the
     * master media file, we need to check for this starte and have
     * the ClockFace set its own time */
    protected boolean masterPastEnd(double t) {
	if (masterPlayer==null || masterPlayer.pastEndTime(t)) { return true; }	
	return false;
    }

    /** Finds out the time from the master media - also sends sync
     * signals if neccesary. */
    public double pollMediaTime() {
	synccounter++;
	if (synccounter >= MAXSYNC) {
	    //System.out.println("ALIGN");
	    alignTimes();
	    synccounter=0;
	}
	if (DEBUG) System.out.println("Polling master: " + masterPlayer);
	if (nmh==null || nmh.playingHandlers==null) { return time; }
	if (masterPlayer==null || masterPlayer.pastEndTime(time)) { return time; }
	return masterPlayer.getTime();
    }
    
    /** Deregister a TimeHandler so that it can no longer set and get
     * times from this Clock */
    public void deregisterTimeHandler(TimeHandler display) {
	if (display instanceof PlayingTimeHandler) {
	    if (nmh!=null) { nmh.deregisterPlayer((PlayingTimeHandler)display); }
	    if (ui!=null && nmh.playingHandlers.size()==0) {
		ui.setSlaveMode(false);
	    }
	} else {	
	    timeHandlers.remove(display);
	}
    } 

    /** return the handler for all PlayingTimeHandlers on this clock */
    public NMediaHandler getMediaHandler() {
	return nmh;
    } 


    /** Return the current system time */
    public double getSystemTime(){
	// if we have media playing, assume that knows the time better
	// than us (we only get updated every 200ms when playing). Oops
	// - this has some undesired side-effects like not showing
	// time advancing when playing a segment. Needs some attention...
	// jonathan 19.5.05
	if (ui!=null && ui.slaveMode()) {
	    if (DEBUG) System.out.println("Poll for media time");
	    time = pollMediaTime();
	}
	return time;
    }
    
    /** Receive notification of a new span and pass the new span to
     * all registered TimeHandlers. This version plays the segment
     * through in real time; if you don't want that behaviour, use the
     * version with a third (boolean) argument to turn off the playing
     * behaviour. */
    public void setTimeSpan(double start, double end) {
	setTimeSpan(start, end, true);
    }

    /** Receive notification of a new span and pass the new span to
     * all registered TimeHandlers; the third argument specifies
     * whether the segment is 'played' in real time by the clock. */
    public void setTimeSpan(double start, double end, boolean play){
	if (Double.isNaN(start) || Double.isNaN(end)) { return; }
	//System.out.println("Span passed to default clock - passing on to handlers (" + start + ", " + end + ").");
	Iterator it = timeHandlers.iterator();
	while (it.hasNext()) {
	    TimeHandler th = (TimeHandler) it.next();
	    th.acceptTimeSpanChange(start,end);
	}
	if (play) { 
	    if (nmh!=null) { // there is a media handler...
		ui.acceptTimeSpanChange(start, end); 
	    }
	}
    }

    /** Receive notification of a new span and pass the new span to
     * all registered TimeHandlers other than the one passed as an
     * argument. This version plays the segment through in real time;
     * if you don't want that behaviour, use the version with a fourth
     * (boolean) argument to turn off the playing behaviour. */
    public void setTimeSpan(double start, double end, TimeHandler setter){
	setTimeSpan(start, end, setter, true);
    }

    /** Receive notification of a new span and pass the new span to
     * all registered TimeHandlers other than the one passed as an
     * argument; the third argument specifies whether the segment is
     * 'played' in real time by the clock. */
    public void setTimeSpan(double start, double end, TimeHandler setter, boolean play){
	if (Double.isNaN(start) || Double.isNaN(end)) { return; }
	//System.out.println("Span passed to default clock - passing on to handlers (" + start + ", " + end + ").");
	Iterator it = timeHandlers.iterator();
	while (it.hasNext()) {
	    TimeHandler th = (TimeHandler)it.next();
	    if (th != setter) {
		th.acceptTimeSpanChange(start, end);
		//System.out.println("Accepted " + th.getID());
	    }
	}
	if (play) { 
	    if (nmh!=null) { // there is a media handler...
		ui.acceptTimeSpanChange(start, end); 
	    }
	}
    }

    /**
     * Set the time and notify all handlers.
     */
    public void setSystemTime(double newTime) {        
	time=newTime;
	Iterator it = timeHandlers.iterator();
	if (DEBUG) 
	    System.out.println("SIMPLE: " + newTime);
	while (it.hasNext()) {
	    TimeHandler th = (TimeHandler) it.next();
	    th.acceptTimeChange(time);
	}
	if (ui.getClockFaceTime()!=newTime) {
	    ui.setClockFaceTime(newTime);
	}
    }

    /**
     * Set the time and notify all handlers except the master.
     */
    protected void setSystemTime(double newTime, boolean slavemode) {       
	time=newTime;
	if (DEBUG) 
	    System.out.println("SLAVEMODE: " + newTime);
	Iterator it = timeHandlers.iterator();
	while (it.hasNext()) {
	    TimeHandler th = (TimeHandler) it.next();
	    //if (DEBUG) System.out.println("Handler: " + th );
	    if (!(th instanceof PlayingTimeHandler)) {
		th.acceptTimeChange(time);
	    }
	}
	if (ui.getClockFaceTime()!=newTime) {
	    ui.setClockFaceTime(newTime);
	}
    }

    protected void changeRate(float rate) {
	if (nmh!=null) { nmh.setPlayRate(rate); }
    }

    /**
     * Set the time and notify all handlers, except the one that just set the time.
     */
    public void setSystemTime(double newTime, TimeHandler setter) {
	time=newTime;
	if (DEBUG) 
	    System.out.println("SETTER: " + newTime);
	Iterator it = timeHandlers.iterator();
	while (it.hasNext()) {
	    TimeHandler th = (TimeHandler) it.next();
	    if (th != setter) {
		th.acceptTimeChange(time);
	    }
	}
    }

    public DefaultClock() {
	ui = new ClockFace(this);
	time=0;
    }

    /** this constructor takes the list of 'NSignal's to be (potentially)
     * synced by the clock. */
    public DefaultClock(NMetaData meta, String observation) {
	ui = new ClockFace(this, meta, observation);
	time=0;
    }

    /** Return the visual display of this clock (as a control strip) */
    public JInternalFrame getDisplay() {
	return (JInternalFrame)ui;
    }

    /** Called by registered displays to allow the slider to be set correctly. */
    protected double pollForMaxTime() {
	double maxtime=net.sourceforge.nite.gui.textviewer.NTextElement.UNTIMED;
	Iterator it = timeHandlers.iterator();
	while (it.hasNext()) {
	    TimeHandler th = (TimeHandler) it.next();
	    if (!(th instanceof PlayingTimeHandler)) { // these register maxtimes
		double mt = th.getMaxTime();
		if (mt>maxtime) {
		    maxtime=mt;
		}
	    }
	}
	return maxtime;
    }

    /** 
     * set the number of seconds the fast-forward and rewind buttons jump
     */
    public void setWindSkip(double seconds) {
	ui.setWindSkip(seconds);
    }

    /** 
     * get the number of seconds the fast-forward and rewind buttons jump
     */
    public double getWindSkip() {
	return ui.getWindSkip();
    }

    /** ensure visibility of interface */
    public void ensureVisible(Container desk) {
	if (ui.isShowing()) { return; } 
	try {
	    desk.add(ui);
	} catch(IllegalArgumentException iae) { // bad placement!
	    ui.setLocation(new Point(100, 100));	    
	    desk.add(ui);
	}
	ui.moveToFront();
    }

    /** 
     * ensure we have some buttons to play with!
     */
    public void ensureVisible(Rectangle rect, Container desk) {
	//System.out.println("ENSURING VISIBILITY: " + ui.isShowing() + "; ");
	if (ui.isShowing()) { return; } 
	ui.setLocation(new Point(rect.x, rect.y));
	ui.setSize(new Dimension(rect.width, rect.height));

	try {
	    desk.add(ui);
	} catch(IllegalArgumentException iae) { // bad placement!
	    ui.setLocation(new Point(100, 100));	    
	    desk.add(ui);
	}
	ui.moveToFront();
    }

    /** set the player to send synchronize messages to registered TimeHandlers */
    public void setSendSynchronization(boolean val) {
	ui.setSendSynchronization(val);
    }

    /** show the signal and jump to init time. this will involve
     * either finding the already-registered signal and moving that
     * window to the front, or actually starting up a new media player
     * and registering it */
    protected void showSignal (String filename, NSignal signal, double d, boolean p, String agent) {
	showSignal(filename, signal, agent);
	if (player!=null) {
	    player.acceptTimeChangeWhenPrefetched(d);
	}
	if (p) {
	    player.playWhenPrefetched();
	}
    }

    NITEMediaPlayer player = null;
    /** show the signal: this will involve either finding the
     * already-registered signal and moving that window to the front,
     * or actually starting up a new media player and registering
     * it */
    public void showSignal (String filename, NSignal signal, String agent) {
	boolean found=false;
	JDesktopPane desktop=null;
	boolean master=false;

	if (nmh==null) {
	    nmh=new NMediaHandler(this);
	    timeHandlers.add(nmh);
	    if (ui!=null) { ui.setSlaveMode(true); }
	    // make the first player the master by default
	    master=true;
	}
	Iterator it = nmh.playingHandlers.iterator();
	String shortName = new String(filename);
	if (filename.indexOf(File.separator)>=0) {
	    shortName = shortName.substring(shortName.lastIndexOf(File.separator)+1);
	}
	
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    if (desktop==null && th instanceof JInternalFrame) { 
		desktop = ((JInternalFrame)th).getDesktopPane();
	    }
	    String pf = th.getFileName();
	    pf=pf.substring(pf.lastIndexOf(File.separator)+1);
	    //System.out.println("Compare '" + pf + "' to '" + shortName + "'");
	    if (pf.equals(shortName)) {
		found=true;
		if (th instanceof JInternalFrame) {
		    try {
			((JInternalFrame)th).setIcon(false);
			((JInternalFrame)th).moveToFront();
		    } catch (java.beans.PropertyVetoException ex) {	}
		}
		break;
	    }
	}
	if (!found) {
	    // create a new media player
	    String name = signal.getName();
	    if (agent!=null) {
		name += " " + agent;
	    }
	    if (signal.getMediaType() == NSignal.VIDEO_SIGNAL) {
		player = new NITEVideoPlayer(new File(filename), this, name);
	    } else if (signal.getMediaType() == NSignal.AUDIO_SIGNAL) {
		player = new NITEAudioPlayer(new File(filename), this , name);
	    }
	    
	    player.setLocation(new Point(50, 50));
	    if (desktop==null) {
		if (ui!=null) {
		    desktop=((JInternalFrame)ui).getDesktopPane();
		}
		if (desktop==null) {
		    it = timeHandlers.iterator();
		    while (it.hasNext()) {
			TimeHandler th = (TimeHandler) it.next();
			if (th instanceof JTextPane) { 
			    try { // we just know how to deal with JTextPanes
				Container c = ((JTextPane)th).getRootPane().getParent();
				if (c instanceof JInternalFrame) {
				    desktop = ((JInternalFrame)c).getDesktopPane();
				    break;
				}
			    } catch (NullPointerException nex) { }
			}
		    }
		}
	    }

	    if (desktop!=null) { 
		desktop.add(player);
		if (master) {
		    player.setMaster(true);
		    masterPlayer=player;
		}
	    }
	}
	
    }

    /** return true if the media is in the 'playing' state */
    public boolean isMediaPlaying() {
        if (nmh==null) { return false; }
        Iterator it = nmh.playingHandlers.iterator();
	    while (it.hasNext()) {
	        PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	        if(th.isPlaying())
	        return true;
	    }
	    return false;
    }

    /** pass a play message to any ClockFace ui component, causing any
     * media files to start playing */
    public void play() {
	if (ui!=null) {
	    ui.play();
	}
    }

    /** pass a pause message to any ClockFace ui component, causing any
     * media files to be paused */
    public void pause() {
	if (ui!=null) {
	    ui.pause();
	}
    }

    /** set the PlayingTimeHandler that controls time for this Clock */
    public void setMasterPlayer(PlayingTimeHandler pth) {
	masterPlayer=pth;
	if (DEBUG) System.out.println("Setting " + pth.getFileName() + " to be master " + nmh + nmh.playingHandlers);
	if (pth==null && ui!=null) { ui.setSlaveMode(false); }
	if (nmh==null || nmh.playingHandlers==null) { return; }
	Iterator it = nmh.playingHandlers.iterator();
	while (it.hasNext()) {
	    PlayingTimeHandler th = (PlayingTimeHandler) it.next();
	    if (DEBUG) System.out.println ("test: " + th.getFileName());
	    if (th==pth) {
		th.setMaster(true);
	    } else {
		th.setMaster(false);		
	    }
	}
	if (ui!=null && pth!=null) { ui.setSlaveMode(true); }
    }

    /** set the low-end cutoff - if video or audio drift from the
     * master signal by more than this number, a warning will be
     * printed if we're in verbose mode */
    private void setWarningDrift(double warn) {
	LOW_CUTOFF=warn;
    }

    /** set the high-end cutoff - if video or audio drift from the
     * master signal by more than this number, we intervene either by pausing
     * and restarting all the signals, or by printing a warning message  */
    public void setMaximumDrift(double drift) {
        HIGH_CUTOFF=drift;
    }

    /** set the verbosity. In verbose mode you get a printout of all
     * signal values at all triel points; otherwise only interventions
     * are noted. */
    public void setVerbose(boolean verb) {
	DEBUG=verb;
    }

    public static final int PAUSE=0;
    public static final int WARN=1;

    /** set the intervention type when signal drift exceeds the
     * threshold: PAUSE to reset all the signals by pausing and
     * restarting; WARN to simply print a message */
    public void setInterventionType(int ervention) {
	if (ervention==PAUSE || ervention==WARN) {
	    intervention=ervention;
	}
    }
    
}


