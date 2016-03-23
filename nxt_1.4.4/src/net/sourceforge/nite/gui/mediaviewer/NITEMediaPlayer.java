/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.mediaviewer;
import net.sourceforge.nite.time.*;
import net.sourceforge.nite.util.Debug;

import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DeallocateEvent;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.MediaTimeSetEvent;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.PrefetchCompleteEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.StopEvent;
import javax.media.Time;
import javax.media.control.FramePositioningControl;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.awt.*;
import java.net.*;
import java.beans.*;
import javax.swing.event.*;
import javax.swing.plaf.InternalFrameUI;

import java.util.Hashtable;

//TODO HACK: (CAN) Try using FMJ's player panel directly...
//import net.sf.fmj.ui.application.*;

/** 
 * A video player implementation using JMF
 *
 * [DR: Added a line in loadIcon, since the system missed part of the classpath in searching for the icons.]
 * 
 * @author judyr, jeanc, jonathan, dennisr
 *
 * BUG FIXES, FEATURE EXTENSIONS jeanc Jan 2004
 * 
 * Added mute button, fixed rewind and fast forward buttons
 * to be labelled properly and to work in first/last five seconds
 * of video, fixed bug whereby the synchronization with a text
 * display didn't work if one went backwards in the video file
 * to replay part of it, fixed time slider so that it updates
 * as video plays; added comments about what else I think should
 * be done to this code.
 *
 * BUG FIXES jonathan Feb 2004. looping bug fixed which was caused by
 * the wrong test being applied in changeState on the TimeSlider;
 * media windows were occasionally starting without a request from the
 * user. This just needed a player.prefetch instead of a player.play
 * call. Stopped exceptions being thrown when file not found -
 * sensible message generated instead; players are now only visible if
 * there's something to play; removed much code duplication from
 * NITEAudioPlayer; sensible default sizes for media windows; grey out
 * play button when end is reached - don't go back to start.
 *
 * JC: Further comments:
 * 
 * The green highlighting means "we're in synchronized mode and this tag 
 * is at current video time".  We need to find a way for this not to interfere
 * with the blue highlighting that means "this is the current selection".
 * It looks inconsistent when one of the currently timed tags which should
 * be green is blue just because it's selected as well.   One shouldn't have to 
 * select a segment (blue highlight) in order to ctrl-right-click to play it,
 * although that would be OK if we could see both highlights at once.

 * [ jonathan ] I'm sure the above are NTextArea issues.

 * Add play speed slider
 * 
 * In my view, if one crtl-right-clicks to play a segment, it should just
 * play and then the system should return to its previous state - playing
 * a segment shouldn't bob up all the other tags that are at the same time
 * in synchronize mode, shouldn't allow for pausing, rewinding, etc., shouldn't
 * reset video (and in synch mode, system) time to the end of the played segment.
 * It ought to be that one can play
 * a video, pause, go back to look at something small, and return to the
 * place where one was.  The current code implements playSegment as a special
 * mode of play (so there's a pause button and everything), and that's why
 * the time resetting occurs.  We'd need to grey out the play button whilst
 * playing a segment and come up with a different kind of highlight to show
 * which one we were playing if we did reimplement.  I haven't tried it because
 * I need to see if my views are contentious first.
 * 
 * When the monitor display comes up, there are already some tags
 * highlighted in green, the colour for "this is a tag at the current
 * system time". The wierd thing is that we aren't actually in synchronized
 * mode when the display comes up, so these tags shouldn't be appearing 
 * at all (in my view).  At the moment, synchronized mode just appears
 * to mean it doesn't highlight tags as one plays.  If uncontentious,
 * changes required to NTextArea.
 * 
 * Rewind and fast forward work when paused but it's impossible to
 * tell that they do because the video doesn't shift to the 
 * correct still picture for the start point until you press play, 
 * despite doing a setMediaTime.  Can this be fixed?

 * [jonathan] - this works though I don't know when it was fixed or
 * who did it

 * 
 * When we get to the end of a video this code automatically goes to
 * the beginning again.  I don't like that (and the user can
 * always use the slider to get back to the beginning).  I'm pretty
 * sure it's because it's easier to implement that - if it doesn't, then
 * we need another icon for the play button greyed out, because it's not
 * appropriate to play *or* pause in the state that there's no video left.
 * If you decide to change this, search on Time(0).  The EndOfMediaEvent
 * instance in ControllerListener is the main one for playing past the end;
 * I'm not sure what exercises the other one (when play is called by a 
 * time that's too big).  Again, my views could be contentious.

 * [ jonathan ] - play is greyed out by
 * playButton.setEnabled(false). I think I've implemented this request
 * though it could clearly equally be applied to the other buttons I
 * suppose.
 * 
 * The  implementation was clearly aiming for rewind, fastforward,
 * back one frame, forward one frame, but the last two aren't 
 * completely implemented.    

 * [Dennis Hofs] Added a rate slider at the bottom of the window. It ranges
 * from -4x (meaning that the media is played 4 times slower than normal) to
 * +4x (the media is played 4 times faster). The default value is 0 (normal
 * play rate). The play rate is computed with an exponential function on the
 * distance of the slider value from 0, so the rate grows faster as the
 * distance of the slider value from 0 increases. The play rate is not changed
 * while the player is playing. A slider change will have its effect when the
 * player is paused and playback is restarted. A Reset button allows the user
 * to reset the rate slider to 0.
 *
 * [jonathan] 5.12.4 Removed the buttons (except Mute) and all the time
 * handling stuff and put it in net.sourceforge.nite.time.ClockFace as
 * a single controlling button-set (making most of the above comments
 * redundant.
 */
public abstract class NITEMediaPlayer
    extends JInternalFrame
    implements ControllerListener, PlayingTimeHandler {
    
    //the user interface components
    protected JPanel videoPanel;
    protected JPanel checkPanel;
    protected JDesktopPane desktop;
    protected JCheckBox muteCheck;
    protected JCheckBox masterCheck;
    private float rate=1;
    double thismaxtime=0;
    TimeSliderListener timeSliderHandler;
    // component in which video is playing
    Component visualComponent = null;
    JComponent visualJComponent;
    JLabel videoname = null;
    int videoWidth = 0;
    int videoHeight = 0;
    //    protected String userpath = "Data" + File.separator + "Images" + File.separator;
    protected String mediaFile=null;
    MediaLocator mrl = null;
    
    protected File currentFile;
    protected URL url;
    // media Player
    protected Player player = null;
    FramePositioningControl fpc;
    int stopFrame = 0;
    double stopTime = 0;
    //Time management
    DefaultClock niteclock;
    Time oldTime;
    boolean playingSegment;
    boolean mutebutton;
    boolean masterbutton=false;
    //these are required to close the player neatly
    boolean bclosing;
    boolean bprcoessedevent;
    long beforeTime, afterTime = 0;
    private int id;
    String name=null;
    boolean setTimeWhenPrefetched=false;
    double prefetchTime=0.0;
    boolean playWhenPrefetched=false;

	public void addComponentListener(ComponentListener l) {
		Debug.print("addComponentListener... " + l);
		super.addComponentListener(l);
		Debug.print("...added ComponentListener");
	}    
    
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		Debug.print("addPropertyChangeListener... " + listener);
		super.addPropertyChangeListener(listener);
		Debug.print("...added PropertyChangeListener");
	}    	
	
    public NITEMediaPlayer(DefaultClock c) {
	super("NITE video player", true, true, true, true);
	niteclock = c;
	setUpGUI();
	addInternalFrameListener(new IFL());
	//	setVisible(true);
    }

    public NITEMediaPlayer(File f, DefaultClock c) {
	super("NITE video player", true, true, true, true);
	niteclock = c;
	currentFile = f;
	setUpGUI();
	addInternalFrameListener(new IFL());
	setSize(new Dimension(400,400));
	//	setVisible(true);
	openFile(currentFile);
	c.registerTimeHandler(this);
    }

    public NITEMediaPlayer(File f, DefaultClock c, String name) {
	super("NITE video player", true, true, true, true);
	niteclock = c;
	this.name=name;
	currentFile = f;
	setUpGUI();
	addInternalFrameListener(new IFL());
	setSize(new Dimension(400,400));
	//	setVisible(true);
	openFile(currentFile);
	c.registerTimeHandler(this);
    }
    
    private void setUpGUI() {
	desktop = new JDesktopPane();
	videoPanel = new JPanel();
	videoPanel.setBackground(Color.black);
	videoPanel.setPreferredSize(new Dimension(300, 300));
	
	checkPanel = new JPanel();
	
	muteCheck = new JCheckBox("Mute");
	muteCheck.setToolTipText("Toggle audio muting");
	muteCheck.setActionCommand("mute");
	muteCheck.addActionListener(new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {
		    try {
			if (mutebutton) {
			    mutebutton = false;
			    //unmute the audio
			    if (player.getGainControl() != null) {
				player.getGainControl().setMute(false);
			    }
			} else {
			    mutebutton = true;
			    // mute the audio
			    if (player.getGainControl() != null) {
				player.getGainControl().setMute(true);
			    }
			}
		    } catch (NullPointerException nex) {
			// happens when there's already no sound on a video
		    }
		}
	    });
	
	masterCheck = new JCheckBox("Master");
	masterCheck.setToolTipText("IF checked, this signal controls the time and guarantees to stay in sync with data");
	masterCheck.setActionCommand("master");
	masterCheck.addActionListener(new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {
		    if (masterbutton) {
			masterbutton = false;
			if (niteclock!=null) { niteclock.setMasterPlayer(null); }
		    } else {
			masterbutton = true;
			if (niteclock!=null) { makeMaster(); }
		    }
		}
	    });
	
	//now add these to the button control panel
	
	checkPanel.setLayout(new BorderLayout());
	String label=name;
	if (label==null) {
	    if (currentFile!=null) { label=currentFile.getName(); }
	}
	if (label!=null) {
		videoname = new JLabel(label);
	    checkPanel.add(videoname, BorderLayout.EAST);
	    //System.out.println("ADDED LABEL: " + label + "; currentFile: " + currentFile);
	}
	checkPanel.add(muteCheck, BorderLayout.WEST);
	checkPanel.add(masterCheck, BorderLayout.CENTER);

	//desktop.add(checkPanel,BorderLayout.SOUTH);
	desktop.setLayout(new BorderLayout());
	
	desktop.add(videoPanel, BorderLayout.CENTER);
	
	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(checkPanel, BorderLayout.SOUTH);
	if (this instanceof NITEVideoPlayer) {
	    getContentPane().add(desktop, BorderLayout.CENTER);
	}
    }

    class FlatButton extends JButton {
	public FlatButton(ImageIcon i) {
	    super(i);
	    setBorderPainted(false);
	}
    }

    protected void displayFrame(int i) {
	Control[] control = player.getControls();
	for (int j = 0; j < control.length; j++) {
	    System.out.println(control[j]);
	}
	
	fpc = (FramePositioningControl) player.getControl(
	      "javax.media.control.FramePositioningControl");
	if (i > 0 && fpc != null)
	    fpc.seek(i);
    }

    protected void displayTime(Time t) {
	
    }

    /** return true if this player has the 'master' button checked,
     * and thus controls time for the application */
    public boolean isMaster() {
	return masterbutton;
    }

    private void makeMaster() {
	niteclock.setMasterPlayer(this);
    }

    /** Make this player control time for the application if the
     * argument is 'true', or stop it from being if 'false'. */
    public void setMaster(boolean mast) {
	masterbutton=mast;
	masterCheck.setSelected(mast);
    }

    /** return a boolean: true the given time is after the
     * end time of the media file.  */
    public boolean pastEndTime(double ctime) {
	return ctime>thismaxtime;
    }

    /** get the current media time */
    public double getTime() {
	if (player==null) { return 0.0; }
	//System.err.println("POLLED For media time: " + name);
	return player.getMediaTime().getSeconds();
    }

    public void play() {
	//if the player time has got past the duration of the video, reset
	if (player==null) return;
        if (player.getDuration().getSeconds() <= player.getMediaTime().getSeconds()) {
//            player.setMediaTime(new Time(0));
            pause();
        } else {
            if (player.getState() == Player.Prefetched)
                player.start();
            else if (player.getState() != Player.Started)
                playWhenPrefetched = true;
        }
    }
    
    public void pause() {
	//System.out.println("pause");
        if (player !=null && player.getState() == Player.Started)
            player.stop();
    }

    //rewinds by the given number of seconds. 
    public void rewind(double seconds) {
	if (player==null) { return; }
	Time t = player.getMediaTime();
	
	double sec = t.getSeconds();
	if (sec > seconds) {
	    sec -= seconds;
	} else {
	    sec = 0;
	}
	Time newtime = new Time(sec);
	
	player.setMediaTime(newtime);
    }

    //rewinds by a set number of seconds (see windSeconds). 
    public void rewind() {
	if (player==null) { return; }
	Time t = player.getMediaTime();
	double windSeconds = niteclock.getWindSkip();
	
	double sec = t.getSeconds();
	if (sec > windSeconds) {
	    sec -= windSeconds;
	} else {
	    sec = 0;
	}
	Time newtime = new Time(sec);
	
	player.setMediaTime(newtime);
    }

    /** go back one frame.  Appears to be unused and only partly implemented */
    protected void backOneFrame() {
	int currframepos = 0;
	int targetframepos = 0;
	Time newtime;
	if (player != null) {
	    currframepos = mapTimeToFrame(player.getMediaTime());
	    
	}
	if (currframepos > 0) {
	    targetframepos = currframepos - 1;
	    newtime = mapFrameToTime(targetframepos);
	    player.setMediaTime(newtime);
	}
	checkButtonStatus();
    }

    // Detect presence of FMJ so we can work around bugs
	public boolean isFMJManagerImplementation()
	{
		try
		{
			Manager.class.getField("FMJ_TAG");
			Debug.print("FMJ does not support rate changes.", Debug.WARNING);
			return true;
		}
		catch (Exception e)
		{	return false;
		}
	}    
    
    /** set the rate of play so we can sound like pinky and perky. 1 is normal. */
    public void setPlayRate(float rate) {
	this.rate=rate;
        if (player == null) return;
        int state = player.getState();
        if (state == Player.Prefetched || state == Player.Started) {
        	// TODO: FMJ implementation of JavaSound and QT do not yet support setRate()
        	if (!isFMJManagerImplementation()) {
        		player.setRate(rate);
        	}
        }
	//System.out.println("set playing rate in NITEMediaHandler: " + this.rate);
    }

    /** for going back/forward one frame at a time.  Not implemented */
    protected Time mapFrameToTime(int i) {
	return null;
    }

    /** for going back/forward one frame at a time.  Untested */
    protected int mapTimeToFrame(Time t) {
	int frame;
	if (player != null) {
	    frame = (int) Math.round(t.getSeconds() * player.getRate());
	    return frame;
	} else
	    return -1;
    }

    /** go forward one frame.  Unimplemented */
    protected void forwardOneFrame() {
	
    }

    private void checkButtonStatus() {
	if (player==null) return; 
	// jonathan - try to make the play button behave appropriately
	// could also extend to fast forward and rewind (and others if
	// they exist)

	//	System.out.println("Current: " + player.getMediaTime().getSeconds() +  "; total" + player.getDuration().getSeconds() + ".");
	if (player.getDuration().getSeconds()
	    <= player.getMediaTime().getSeconds()) {
	    player.stop();
	} 
    }
    
    // forwards by a set number of seconds (windSeconds). 
    public void fastForward() {
	if (player==null) { return; }
	Time t = player.getMediaTime();
	double windSeconds = niteclock.getWindSkip();
	
	double sec = t.getSeconds();
	sec += windSeconds;
	if (sec > player.getDuration().getSeconds()) {
	    sec = player.getDuration().getSeconds();
	};
	Time newtime = new Time(sec);
	
	player.setMediaTime(newtime);
	checkButtonStatus();
    }

    // forwards by the given number of seconds
    public void fastForward(double seconds) {
	if (player==null) { return; }
	Time t = player.getMediaTime();
	
	double sec = t.getSeconds();
	sec += seconds;
	if (sec > player.getDuration().getSeconds()) {
	    sec = player.getDuration().getSeconds();
	};
	Time newtime = new Time(sec);
	
	player.setMediaTime(newtime);
	checkButtonStatus();
    }
    
    protected void playSegment(double s, double e) {
    	if (player != null) {
    		player.stop();
		    player.setMediaTime(new Time(s));
		    stopTime = e;
		    playingSegment = true;
		    if (!isFMJManagerImplementation()) {
		    	player.setRate(rate);
		    }
		    play();
		}
    }

    /** return true if the media is currently playing */
    public boolean isPlaying() {
	if (player==null) { return false; }
        return (player.getState()==Controller.Started);
    }

    /** skip to the given time when ready */
    public void acceptTimeChangeWhenPrefetched(double systemTime) {
	setTimeWhenPrefetched=true;
	prefetchTime=systemTime;
    }

    /** start playing when ready */
    public void playWhenPrefetched() {
	playWhenPrefetched=true;
    }

    /** skip to the given time */
    public void acceptTimeChange(double systemTime) {
	if (player==null) { return; }
	// this should not be required but it's s stop-gap.
	//if (player.getState()==Controller.Started) { return; }
	if (systemTime >= 0) {
            if (player.getState()==Controller.Prefetched || player.getState()==Controller.Started) {
                player.setMediaTime(new Time(systemTime));
	    } else {
                acceptTimeChangeWhenPrefetched(systemTime);
	    }
	    checkButtonStatus();
	}
	//System.out.println("MEDIA: Update time: " + systemTime);	
    }
    
    /** This gets called if we control-right-click to play the
     * currently selected segment.  */
    public void acceptTimeSpanChange(double start, double end) {
	//	System.out.println("Start in accept time span change in player" + start + " " + end);
	if (player != null) {
	    if ((start >= 0) && (end >= 0))
		playSegment(start, end);
	}
    }

    public void setTimeSpan(double start, double end) {
	niteclock.setTimeSpan(start, end);
    }
    
    public void setTime(double time) {
	//player.setMediaTime(new Time(time));
	niteclock.setSystemTime(time);
    }

    public net.sourceforge.nite.time.Clock getClock() {
	return niteclock;
    }

    public void setTimeHighlightColor(Color color) {
	
    }

    public void setClock(Clock c) {
	niteclock = (DefaultClock) c;
	//niteclock.registerTimeHandler(this);
    }

    /**
     * The slider which the user can use to reset the time on the video.
     * */
    class TimeSliderListener implements ChangeListener {
	NITEMediaPlayer parent;
	boolean is_hand_adjusted=false;
	
	TimeSliderListener(NITEMediaPlayer p) {
	    parent = p;
	}

	//JK: note that you get statechanged events when the
	//JK: video is in normal play mode, so this caused looping
	//JK: before the test for 'hand-adjusted-ness'
	public void stateChanged(ChangeEvent c) {
	    JSlider source = (JSlider) c.getSource();
	    
	    if (source.getValueIsAdjusting() == true) { 
		// this only happens when user moves the slider
		is_hand_adjusted=true;
	    } else if (is_hand_adjusted==true) {
		//if the user has finished dragging it, do this
		Time newtime = new Time((double) source.getValue());
		player.setMediaTime(newtime);
		
		checkButtonStatus();
		//JC: since the time monitor seems to poll continually
		//JC: for changes I don't think this is necessary, and
		//JC: it could be wrong when we aren't in synchronized
		//mode.
		niteclock.setSystemTime(newtime.getSeconds(), parent);
		is_hand_adjusted=false;
	    }
	}
    }

    protected void openFile(File file) {
	try {
	    url = file.toURL();
	    mediaFile = url.toExternalForm();
	    if (url.getProtocol().startsWith("file")) {
	    	mediaFile = url.getProtocol() + "://" + url.getPath();
	    }
	} catch (MalformedURLException mue) {
	    mue.printStackTrace();
	}
	
	// Create a media locator from the file name
	if ((mrl = new MediaLocator(mediaFile)) == null)
	    System.err.println("Can't build URL for " + mediaFile);
	
	Debug.print("Attempting to open mrl: " + mrl);
	
	// Create an instance of a player for this media
	try {
		Debug.print("Manager.setHint()...", 0);
	    
	    Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, new Boolean(true));
	    //first of all, clean up after any previous players
	    if (player != null) {
		cleanup();
		player = null;
	    }
	    
		Debug.print("Creating player...", 0);
	    
	    player = Manager.createPlayer(mrl);

	    // Add ourselves as a listener for a player's events
	    // NOTE from jonathan - we need to add
	    // ourselves as a listener *before* we do the start so
	    // we're sure to catch the prefetch complete.
		Debug.print("Adding listener...", 0);
	    
	    player.addControllerListener(this);

	    // ok, we're going to start the player so it will take
	    // care of all the weird states before prefetch complete,
	    // but we'll trap the prefecthed complete event in the
	    // control listener, so we don't start playing
	    // immediately.  
	    //	    player.start();
		Debug.print("Prefetching...", 0);

	    player.prefetch();
	} catch (NoPlayerException npe) {
	    //	    npe.printStackTrace();
	    System.err.println("ERROR: Failed to create player for " + mrl);
	} catch (IOException ioe) {
	    //	    ioe.printStackTrace();
	    System.err.println("ERROR: Media file not found or invalid: " + mrl);
	}
	Debug.print("finished opening media", 0);
    }


    class VideoFileFilter extends javax.swing.filechooser.FileFilter {
	public boolean accept(File f) {
	    String name = f.getName();
	    if (f.isDirectory()) {
		return true;
	    }
	    if (name.endsWith(".mov")
		|| name.endsWith(".avi")
		|| name.endsWith(".mpeg")
		|| name.endsWith(".mp3")
		|| name.endsWith(".mpg")) {
		return true;
	    } else {
		return false;
	    }
	}
	
	public String getDescription() {
	    return ("Video files - mov, avi, mpeg, mp3");
	}
    }

    private boolean containsInterface(Class[] interfaces, String target) {
    	for (int i = 0; i < interfaces.length; ++i) {
    		if (interfaces[i].getName().contains(target)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * This controllerUpdate function must be defined in order to
     * implement a ControllerListener interface. This 
     * function will be called whenever there is a media event
     */
    public void controllerUpdate(ControllerEvent event) {
	// If we're getting messages from a dead player, 
	// just leave
	// System.out.println("Event in video player: " + event);
	if (player == null)
	    return;
	
	if (event instanceof PrefetchCompleteEvent) {
	    //    System.out.println("prefetch event in video player");
	    if (visualComponent == null) {
	    	visualComponent = player.getVisualComponent();
	    }
	    if (visualComponent!=null) {
	    	//Debug.print("adding video visuals... ");
	    	Debug.print("Adding video of type: " + visualComponent.getClass().getName() + ", implementing " + visualComponent.getClass().getInterfaces(), Debug.PROGRAMMER);
	    	if ((new VideoFileFilter().accept(currentFile)) && containsInterface(visualComponent.getClass().getInterfaces(), "QTComponent")) {
	    		// HACK [CN]: Heavyweight, old-style AWT video component can't be inside a Swing panel
	    		player.getVisualComponent();
	    		Frame external = new Frame();
	    		external.setSize(this.getSize().width, this.getSize().height + 50);
	    		external.setLocation(this.getLocation());
	    		videoname.setText(videoname.getText() + "   "); // Move away from Mac resize control
	    		//external.add(visualComponent);
	    		external.add(this);
	    		this.setLocation(0,20);
	    		this.setSize(this.getSize().width, this.getSize().height + 30);
	    		setMute(true);
	    		//player.getGainControl().setMute(true);
	    		external.setVisible(true);
	    	}
	    	
	    	videoPanel.add(visualComponent);
	    	
	    	//Debug.print("added video visuals");
	    }
		videoPanel.validate();
	    setVisible(true);
	    thismaxtime = player.getDuration().getSeconds();
	    //Debug.print("thismaxtime:" + thismaxtime, 0);
	    if (niteclock!=null) {
		//System.out.println("Setting the maximum duration to " + player.getDuration().getSeconds());
		niteclock.registerMaxTime((int)player.getDuration().getSeconds(), this);
		niteclock.ensureVisible(this.getBounds(), this.getParent());
	    }
	    setPlayRate(rate);
	    if (setTimeWhenPrefetched) {
		setTimeWhenPrefetched=false;
		acceptTimeChange(prefetchTime);
	    }
	    if (playWhenPrefetched) {
		playWhenPrefetched=false;
		niteclock.play();
	    }
	} else if (event instanceof MediaTimeSetEvent) {
	    
	} else if (event instanceof EndOfMediaEvent) {

	} else if (event instanceof ControllerErrorEvent) {
	    player = null;
	    System.err.println(((ControllerErrorEvent) event).getMessage());
	} else if (event instanceof ControllerClosedEvent) {
	    synchronized (this) {
		//System.out.println("controller closed");
		if (bclosing == true) {
		    // tell the waiting close() method that we've 
		    // received the ControllerClosedEvent
		    bprcoessedevent = true;
		    notify();
		}
	    }
	} else if (event instanceof DeallocateEvent) {
	    //System.out.println("Deallocate event");
	    synchronized (this) {
		if (bclosing == true) {
		    // tell the waiting close() method that we've 
		    // received the DeallocateEvent
		    bprcoessedevent = true;
		    notify();
		}
	    }
	} else if (event instanceof StopEvent) {
	    synchronized (this) {
		if (bclosing == true) {
		    // tell the waiting close() method that we've 
		    // received the DeallocateEvent
		    
		    bprcoessedevent = true;
		    notify();
		}
	    }
	} else if (event instanceof RealizeCompleteEvent) {
	    // this can be required where we set the mute
	    // programatically bnut before the player has realized.
	    setMute(mutebutton);
	}
    }

    /** set the player to send synchronize messages. This is a
     * backward compatibility aid: in fact we can only select on the
     * clock interface whether all the text areas are synced or not.. */
    public void setSendSynchronization(boolean val) {
	niteclock.setSendSynchronization(val);
    }

    /**
     * Used to deallocate media resources. Code taken from "Core Java
     * Media Framework" . Under some circumstances, this throws an
     * exception because it tries to call player.deallocate on a
     * started player, even though it should have waited until the
     * player was stopped before trying to deallocate. A JMF bug?  */
    protected void cleanup() {
	niteclock.deregisterTimeHandler(this);
	if (player != null) {
	    //timeThread.stop();
	    //timeThread = null;
	    bclosing = true;
	    bprcoessedevent = false;
	    beforeTime = System.currentTimeMillis();
	    
	    // stop the Player
	    player.stop();
	    // wait for the stopEvent to be received
	    synchronized (this) {
		try {
		    if (bprcoessedevent != true) {
			wait();
		    }
		} catch (InterruptedException e) {
		}
	    }
	    
	    afterTime = System.currentTimeMillis();
	    // timing info to compare versions of JMF--not necessary in production code
	    //System.out.println("Stop took " + (afterTime - beforeTime) + " ms");
	    
	    beforeTime = System.currentTimeMillis();
	    bprcoessedevent = false;
	    
	    // deallocate any resources we've used...
	    if (player.getState() != Player.Started)
		player.deallocate();
	    
	    // wait for the dealocateEvent to be received
	    synchronized (this) {
		try {
		    if (bprcoessedevent != true) {
			wait();
		    }
		} catch (InterruptedException e) {
		}
	    }
	    afterTime = System.currentTimeMillis();
	    // timing info to compare versions of JMF--not necessary in production code
	    // System.out.println("Deallocate took " + (afterTime - beforeTime) + " ms");
	    
	    beforeTime = System.currentTimeMillis();
	    bprcoessedevent = false;
	    
	    // close the Player
	    player.close();
	    // wait for the closeEvent to be received
	    
	    synchronized (this) {
		try {
		    if (bprcoessedevent != true) {
			wait();
		    }
		} catch (InterruptedException e) {
		}
	    }
	    afterTime = System.currentTimeMillis();
	    // timing info to compare versions of JMF--not necessary in production code
	    //System.out.println("Close took " + (afterTime - beforeTime) + " ms");
	    
	    bclosing = false;
	    // delete our reference so it can be garbage collected
	    player = null;
	    
	    if (visualComponent!=null) {
		videoPanel.remove(visualComponent);
		visualComponent = null;
	    }
	    repaint();
	}
    }
    
    /**
     * <p>Closes the media player. This method should be called when the media
     * player is not needed anymore.</p>
     */
    public void close() {
	cleanup();
    }
    
    public void Exit() {
	cleanup();
    }
        
    /* (non-Javadoc)
     * @see net.sourceforge.nite.time.TimeHandler#getID()
     */
    public int getID() {
	return id;
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.nite.time.TimeHandler#setID(int)
     */
    public void setID(int i) {
	id = i;
    }

    /** 
     * set the number of seconds the fast-forward and rewind buttons jump
     */
    public void setWindSkip(double seconds) {
	niteclock.setWindSkip(seconds);
	//windSeconds=seconds;
    }

    /** 
     * get the number of seconds the fast-forward and rewind buttons jump
     */
    public double getWindSkip() {
	return niteclock.getWindSkip();
	//return windSeconds;
    }

    /** set the mute on or off */
    public void setMute(boolean val) {
	mutebutton=val;
	muteCheck.setSelected(val);
	if (player!=null && player.getState()>=Player.Realized && player.getGainControl()!=null) {
	    try {
		player.getGainControl().setMute(val);
	    } catch (NullPointerException nex) {
		// happens when there's already no sound on a video
	    }
	}
    }

    /** get the end time of the media - in fact we return 0.0 as we
     * auto-register our time with any clock when we have pre-fetched
     * the media file! */
    public double getMaxTime() {
	return 0.0;
    }

    class IFL implements InternalFrameListener {
	public void internalFrameActivated(InternalFrameEvent e) { }
	public void internalFrameClosed(InternalFrameEvent e) { }
	public void internalFrameClosing(InternalFrameEvent e) { 
	    cleanup();
	}
	public void internalFrameDeactivated(InternalFrameEvent e) { }
	public void internalFrameDeiconified(InternalFrameEvent e) { }
	public void internalFrameIconified(InternalFrameEvent e) { }
	public void internalFrameOpened(InternalFrameEvent e) { }
    }
	
    /** Return the file name being played. */
    public String getFileName() {
	return currentFile.getPath();	
    }

}
