/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.time;
import net.sourceforge.nite.time.*;
import net.sourceforge.nite.gui.util.TimeLabel;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.util.Debug;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.awt.*;
import java.net.*;
import javax.swing.event.*;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import javax.media.Time;

import com.jgoodies.looks.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

/** 
 * A control-strip for playing through time in NXT containing play /
 * pause / forward / rewind buttons, a speed slider and a progress
 * slider. 
 *
 * Original implementation Judy Robertson;
 * Rate slider by Dennis Hofs;
 * Other work by Jonathan & Jean.
 * 
 *  */
public class ClockFace extends JInternalFrame {
    //the user interface components
    private double seconds=0;
    protected JPanel buttonPanel;
    protected JPanel checkPanel;
    protected JPanel ratePanel;
    protected JButton playButton;
    protected JButton fastForwardButton;
    protected JButton rewindButton;
    protected JButton openButton;
    private boolean slavemode=false;
    private JComboBox signalCombo;
    protected ImageIcon playImage, pauseImage, fastForwardImage, rewindImage;
    protected JDesktopPane desktop;
    //protected JToolBar buttonBar;
    protected JSlider timeSlider;
    protected JSlider rateSlider;
    protected JTextField skip;
    protected Action resetRateAction;
    protected JCheckBox synchroCheck;
    TimeSliderListener timeSliderHandler;
    TimeLabel tl;
    double windSeconds = 5; // default value to wind / rewind on click
    // component in which video is playing
    Component visualComponent = null;
    int videoWidth = 0;
    int videoHeight = 0;
    double stopTime = 0;
    float rate = 1;
    private int maxtime=5;
    private int syncDelayMillis=200;
    private double add = syncDelayMillis/1000.0; 
    private boolean maxregistered = false;
    
    DefaultClock niteclock;
    Time oldTime;
    TimeMonitor timeMonitor;
    Timer timeThread;
    boolean playingSegment;
    boolean synchrobutton=true;
    boolean mutebutton;
    //these are required to close the player neatly
    boolean bclosing;
    boolean bprcoessedevent;
    long beforeTime, afterTime = 0;
    private int id;
    java.util.List signals=null;
    NMetaData meta=null;
    String observationname=null;
    private boolean DEBUG=false;     // verbose
    private HashMap sigmap = new HashMap(); // store mappings from Strings to NSignals
    private HashMap sigfmap = new HashMap(); // store mappings from Strings to filename Strings
    private HashMap sigamap = new HashMap(); // store mappings from Strings to agent names

    /** this constructor only displays the buttons, but does not show the
     * choice of signals. */
    protected ClockFace(DefaultClock c) {
	super("NITE Clock", true, true, true, true);
	niteclock = c;
	setUpGUI();
	//setSize(new Dimension(430, 230));
	//setSize(new Dimension(400, 150));
	setVisible(true);
	timeThread = new Timer(syncDelayMillis, new TimeMonitor(this));
    }

    /** this constructor allows the clock face to display a set of
     * signals as a popup menu, and open them when selected */
    protected ClockFace(DefaultClock c, NMetaData meta, String observationname) {
	super("NITE Clock", true, true, true, true);
	setSize(new Dimension(390, 180));
	niteclock = c;
	this.observationname = observationname;
	this.meta = meta;
	this.signals=meta.getSignals();
	setUpGUI();
	setVisible(true);
	timeThread = new Timer(syncDelayMillis, new TimeMonitor(this));
    }

    protected void setUpGUI() {
	desktop = new JDesktopPane();
	//buttonBar = new JToolBar();
	//buttonBar.setFloatable(false);
	//buttonBar.setOrientation(javax.swing.SwingConstants.HORIZONTAL);

	FormLayout mainlayout = new FormLayout("pref:grow", //cols
					       "pref:grow, 3dlu, pref, 3dlu, pref"); //rows 
	PanelBuilder mainbuilder = new PanelBuilder(mainlayout);

	FormLayout sliderlayout = new FormLayout("pref, 3dlu, pref:grow", //cols
						"pref"); //rows 
	
	PanelBuilder sliderbuilder=new PanelBuilder(sliderlayout);

	FormLayout buttonlayout = new FormLayout("pref, 0px, pref, 0px, pref, 5dlu, pref, 2px, pref, 5dlu, pref", //cols
						 "pref"); //rows 

	PanelBuilder buttonbuilder = new PanelBuilder(buttonlayout);


	//slider for showing time progress
	timeSlider = new JSlider(0, maxtime);
	timeSlider.setValue(0);
	timeSliderHandler = new TimeSliderListener(this);
	timeSlider.addChangeListener(timeSliderHandler);
	ToolTipManager.sharedInstance().setInitialDelay(0);
	ToolTipManager.sharedInstance().setReshowDelay(0);
	//remember to set max and min values for the slider!!

        CellConstraints cc = new CellConstraints();
	tl = new TimeLabel(niteclock);
	sliderbuilder.add(tl, cc.xy(1,1));
	sliderbuilder.add(timeSlider, cc.xy(3,1));
	
	buttonPanel = new JPanel();
	checkPanel = new JPanel();

	// find the images on the classpath
	playImage = loadIcon("/net/sourceforge/nite/icons/misc/play.gif");
	pauseImage = loadIcon("/net/sourceforge/nite/icons/misc/pause.gif");
	fastForwardImage = loadIcon("/net/sourceforge/nite/icons/misc/fastforward.gif");
	rewindImage = loadIcon("/net/sourceforge/nite/icons/misc/rewind.gif");

	if (playImage==null) {
	    playButton = new JButton("Play");	    
	} else {
	    playButton = new FlatButton(playImage);
	}
	playButton.setToolTipText("Play");
	playButton.setActionCommand("play");
	playButton.addActionListener(new ControlButtonListener());
	
	if (fastForwardImage==null) {
	    fastForwardButton = new JButton("Skip");
	} else {
	    fastForwardButton = new FlatButton(fastForwardImage);
	}
	fastForwardButton.setToolTipText("Wind Forward 'skip' seconds");
	fastForwardButton.setActionCommand("forward");
	fastForwardButton.addActionListener(new ControlButtonListener());
	
	if (rewindImage==null) {
	    rewindButton = new JButton ("Back");
	} else {
	    rewindButton = new FlatButton(rewindImage);
	}
	rewindButton.setToolTipText("Wind Backward 'skip' seconds");
	rewindButton.setActionCommand("rewind");
	rewindButton.addActionListener(new ControlButtonListener());
	
	synchroCheck = new JCheckBox("Sync Text Areas", synchrobutton);
	synchroCheck.setToolTipText("Toggle synchronisation");
	synchroCheck.setActionCommand("synchro");
	synchroCheck.addActionListener(new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {
		    if (synchrobutton)
			synchrobutton = false;
		    else
			synchrobutton = true;
		}
	    });
	
	//now add these to the button control panel
	/*
	timeSlider.setEnabled(false);
	playButton.setEnabled(false);
	rewindButton.setEnabled(false);
	fastForwardButton.setEnabled(false);
	*/
	//buttonPanel.add(openButton);
	//	buttonPanel.add(playButton);
	//buttonPanel.add(rewindButton);
	//buttonPanel.add(fastForwardButton);
	buttonbuilder.add(playButton, cc.xy(1,1));
	buttonbuilder.add(rewindButton, cc.xy(3,1));
	buttonbuilder.add(fastForwardButton, cc.xy(5,1));
	buttonbuilder.add(new JLabel("skip:"), cc.xy(7,1));

	skip = new JTextField(4);
        skip.setText(""+windSeconds);
	//skip.setActionMap(null);
	//skip.setInputMap(JComponent.WHEN_FOCUSED, null);
	skip.resetKeyboardActions();
        skip.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                skipEntered();
            }
        });
	buttonbuilder.add(skip, cc.xy(9,1));
	buttonbuilder.add(synchroCheck, cc.xy(11,1));

	//checkPanel.setLayout(new BorderLayout());
	//checkPanel.add(synchroCheck, BorderLayout.NORTH);
	
	//JPanel typeBoxPanel = new JPanel();

	//typeBoxPanel.add(new JLabel("time:"));
	//typeBoxPanel.add(tl);
	//typeBoxPanel.add(new JLabel("skip:"));
	//JC changing columns arg from 2 to 4 to make wide enough
	// to take a double as small as .04, equivalent to frame rate.
	//typeBoxPanel.add(skip);
	//	checkPanel.add(tl, BorderLayout.SOUTH);
	//checkPanel.add(typeBoxPanel, BorderLayout.SOUTH);
	//buttonPanel.add(checkPanel);


	// create rate slider and reset button
	ratePanel = new JPanel(new BorderLayout());
	JLabel rateLabel = new JLabel("Rate: ");
	Hashtable labels = new Hashtable();
	labels.put(new Integer(-100),new JLabel("-4x"));
	labels.put(new Integer(-80),new JLabel("-3x"));
	labels.put(new Integer(-50),new JLabel("-2x"));
	labels.put(new Integer(0),new JLabel("0"));
	labels.put(new Integer(50),new JLabel("+2x"));
	labels.put(new Integer(80),new JLabel("+3x"));
	labels.put(new Integer(100),new JLabel("+4x"));
	rateSlider = new JSlider(-100,100,0);
	rateSlider.addChangeListener(new RateSliderListener());
	rateSlider.setLabelTable(labels);
	rateSlider.setMajorTickSpacing(50);
	rateSlider.setMinorTickSpacing(10);
	rateSlider.setSnapToTicks(false);
	rateSlider.setPaintTicks(true);
	rateSlider.setPaintLabels(true);
	rateSlider.setToolTipText("Set the play rate.");
	resetRateAction = new ResetRateAction();
	resetRateAction.setEnabled(false);
	JButton resetRateButton = new JButton(resetRateAction);
	ratePanel.add(rateLabel,BorderLayout.WEST);
	ratePanel.add(rateSlider,BorderLayout.CENTER);
	ratePanel.add(resetRateButton,BorderLayout.EAST);

	//buttonBar.setLayout(new BorderLayout());


	//buttonBar.add(timeSlider, BorderLayout.NORTH);
	mainbuilder.add(sliderbuilder.getPanel(), cc.xy(1,1));
	mainbuilder.add(buttonbuilder.getPanel(), cc.xy(1,3,"c,c"));
	mainbuilder.add(ratePanel, cc.xy(1,5));
	desktop.setLayout(new BorderLayout());
	
	getContentPane().setLayout(new BorderLayout());

	// Signal selecter
	if (signals!=null && signals.size()>0) {
	    signalCombo = new JComboBox();
	    Iterator it = signals.iterator();
	    signalCombo.addItem("   --    ");
	    while (it.hasNext()) {
		NSignal sig = (NSignal)it.next();
		String stype = "video: ";
		if (sig.getMediaType() == NSignal.AUDIO_SIGNAL) {
		    stype="audio: ";
		}
		if (sig.getType()==NSignal.AGENT_SIGNAL) {
		    List agents = meta.getAgents();
		    for (Iterator ait=agents.iterator(); ait.hasNext(); ) {
			NAgent nag = (NAgent)ait.next();
			String display = stype + "agent " + nag.getShortName() + " " + sig.getName();
			String filename = sig.getFilename(observationname, nag.getShortName());
			if ((new File(filename)).exists()) {
			    signalCombo.addItem(display);
			    sigmap.put(display, sig);
			    sigamap.put(display, nag.getShortName());
			    sigfmap.put(display, filename);
			} else {
			    Debug.print("INFO: Failed to find signal: " + filename, Debug.DEBUG);
			}
		    }
		} else {
		    String display=stype + sig.getName();
		    String filename = sig.getFilename(observationname, (String)null);
		    //Debug.print("SIGNAL File: " + filename, Debug.DEBUG);
		    if ((new File(filename)).exists()) {
			signalCombo.addItem(display);
			sigmap.put(display, sig);
			sigfmap.put(display, filename);
		    } else {
			Debug.print("INFO: Failed to find signal: " + filename, Debug.DEBUG);
		    }
		}
	    }
	    signalCombo.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			showSelectedSignal();
		    }
		});
	    
	    JPanel signalPanel = new JPanel();
	    signalPanel.add(new JLabel("Signal: "));
	    signalPanel.add(signalCombo);
	    getContentPane().add(signalPanel,BorderLayout.NORTH);
	}
	//getContentPane().add(buttonBar, BorderLayout.SOUTH);
	getContentPane().add(mainbuilder.getPanel(), BorderLayout.SOUTH);
    }

    class FlatButton extends JButton {
	public FlatButton(ImageIcon i) {
	    super(i);
	    setBorderPainted(false);
	}
    }


    protected void displayTime(Time t) {
	
    }
    
    public void play() {
	if (!maxregistered) {
	    double mt = niteclock.pollForMaxTime();
	    if (mt>maxtime) {
		maxtime=(int)mt;
		timeSlider.setMaximum(maxtime);	
	    }
	    maxregistered=true;
	}
	timeThread = new Timer(syncDelayMillis, new TimeMonitor(this));
                // recreate because syncDelayMillis may have changed
	timeThread.start();
	// END CHECK
	playButton.setActionCommand("pause");
	playButton.setToolTipText("pause");
	if (pauseImage!=null) {
	    playButton.setIcon(pauseImage);
	} else {
	    playButton.setText("Pause");
	}
	if (niteclock.getMediaHandler()!=null) {
	    niteclock.getMediaHandler().play();
	}
	double tick = (double)rateSlider.getValue()/100.0;
	rate = (float)Math.pow(4.0,tick);

	//if the player time has got past the total duration, reset
	/*
	if (player.getDuration().getSeconds()
	    <= player.getMediaTime().getSeconds()) {
	    //	    player.setMediaTime(new Time(0));
	    playButton.setEnabled(false);
	    player.stop();
	    //when the player is stopped, the play button should be shown
	    playButton.setActionCommand("play");
	    playButton.setToolTipText("play");
	    playButton.setIcon(playImage);
	}
	*/
    }
    
    public void pause() {
	timeThread.stop();
	//System.out.println("pause");
	//when the player is stopped, the play button should be shown
	playButton.setActionCommand("play");
	playButton.setToolTipText("play");
	if (playImage!=null) {
	    playButton.setIcon(playImage);
	} else {
	    playButton.setText("Play");
	}
	if (niteclock.getMediaHandler()!=null) {
	    niteclock.getMediaHandler().pause();
	}
	niteclock.setSystemTime(seconds);
    }

    //rewinds by a set number of seconds (see windSeconds). 
    public void rewind() {
	if (seconds < windSeconds) {
	    seconds=0.0;
	} else {
	    seconds -= windSeconds;
	}
	timeSlider.setValue((int)seconds);
	if (niteclock.getMediaHandler()!=null) {
	    niteclock.getMediaHandler().rewind(windSeconds);
	}
	niteclock.setSystemTime(seconds);
	checkButtonStatus();
    }

    
    //forward winds by a set number of seconds (windSeconds). 
    public void fastForward() {
	seconds += windSeconds;
	// END CHECK
	timeSlider.setValue((int)seconds);
	niteclock.setSystemTime(seconds);
	if (niteclock.getMediaHandler()!=null) {
	    niteclock.getMediaHandler().fastForward(windSeconds);
	}
	checkButtonStatus();
    }

    /** get the time according to the clock-face */
    public double getClockFaceTime() {
	return seconds;
    }

    /** set the time according to the clock-face */
    public void setClockFaceTime(double time) {
	seconds=time;
    }

    private void checkButtonStatus() {

    }

    /** We're the king of time, we'll only accept time changes using
     * setClockFaceTime - i.e. explicitly not from other
     * timehandlers */
    public void acceptTimeChange(double systemTime) {

    }
    
    /** This gets called if we control-right-click to play the
     * currently selected segment.  */
    public void acceptTimeSpanChange(double start, double end) {
	seconds=start;
	niteclock.setSystemTime(start);
	stopTime = end;
	playingSegment = true;
	play();
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

    }

    /**
     * The slider which the user can use to reset the time on the video.
     * */
    class TimeSliderListener implements ChangeListener {
	ClockFace parent;
	boolean is_hand_adjusted=false;
	
	TimeSliderListener(ClockFace p) {
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
		//Time newtime = new Time((double) source.getValue());
		seconds = (double)source.getValue();
		//player.setMediaTime(newtime);
		
		checkButtonStatus();
		//JC: since the time monitor seems to poll continually
		//JC: for changes I don't think this is necessary, and
		//JC: it could be wrong when we aren't in synchronized
		//mode.
		//seconds = newtime;
		niteclock.setSystemTime(seconds);
		is_hand_adjusted=false;
	    }
	}
    }
    
    private class RateSliderListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                resetRateAction.setEnabled(rateSlider.getValue() != 0);
                broadcastRate();
            }
        }
    }

    class ControlButtonListener implements ActionListener {
	ControlButtonListener() {
	    
	}
	public void actionPerformed(ActionEvent e) {
	    if (e.getActionCommand().equals("play")) {
		play();
	    } else if (e.getActionCommand().equals("pause")) {
		pause();
	    } else if (e.getActionCommand().equals("rewind")) {
		rewind();
	    } else if (e.getActionCommand().equals("forward")) {
		fastForward();
	    }
	}
    }


    /**
     * This monitor is used to update ui components, and the NITEClock
     * about media time progress 
     */
    class TimeMonitor implements ActionListener {
	private ClockFace parent;
	TimeMonitor(ClockFace p) {
	    //System.out.println("Time monitor starting: " + seconds);
	    parent = p;
	}
	
	public void actionPerformed(ActionEvent e) {
	    //Time time = niteclock.getMediaHandlerplayer.getMediaTime();
	    //update the niteclock every syncDelayMillis milliseconds
	    if (slavemode && !niteclock.masterPastEnd(seconds)) {
		if (DEBUG) 
		    System.out.println("Slave mode");
		seconds=niteclock.pollMediaTime();
	    } else {
		if (DEBUG)
		    System.out.println("NON-Slave mode");
		seconds+=add*rate;
	    }
	    //System.out.println("update time: " + seconds + " add: " + add + " (rate=" + rate + ")");
	    if (synchrobutton) {
		//JC changed next line from < to != because after
		//playing a segment or when have gone past end of
		//video and back to beginning, system time can be
		//greater than media time
		if (DEBUG) 
		    System.out.println("syncro: " + seconds + "; " + niteclock.getSystemTime());
		//if (slavemode || niteclock.getSystemTime() != seconds) {
		    niteclock.setSystemTime(seconds, slavemode);
		    //niteclock.setSystemTime(time.getSeconds(), parent);
		    //niteclock.getMediaHandler().setTime(time.getSeconds(), parent);
		    //}
	    } 
	    else if (DEBUG) 
		System.out.println("No syncro: " + seconds);

	    //else {
	    // make sure the label is still updated
	    
	    tl.acceptTimeChange(seconds);
	    //	    }

	    if (playingSegment && (stopTime <= seconds)) {
		pause();
		System.out.println("Segment Stop time is reached, pause");
		playingSegment = false;
	    }

	    if (maxtime <= seconds) {
		pause();
		System.out.println(" Stop time is reached, pause");
		playingSegment = false;
	    }

	
	    /**JC:  update the slider to show the media time on the slider 
	     * unless the user happens to be sliding it right now.  This
	     * works for me to keep this code from clobbering the user's
	     * changes using the timeSlider, but I'm not sure how these
	     * events work, and whether this code can ever be executed
	     * after the user's sliding has finished but before the effect
	     * of changing the time has taken place. */
	    if ((timeSliderHandler != null) && (timeSlider.getValueIsAdjusting() == false)){
		//briefly deregister the slider event handler
		timeSlider.removeChangeListener(timeSliderHandler);
		timeSlider. setValue((int)seconds);
		//add the change listenr back now 
		timeSlider.addChangeListener(timeSliderHandler);
	    }
	    
	}
    }

    // utility method to load images from the classpath there is a bug
    // in the system call 'new ImageIcon' which throws a
    // NullPointerException but does not propagete and allow me to
    // catch it!! Obviously it works if the image is on the classpath.
    private ImageIcon loadIcon(String name) {
	try {
	    // get resource
	    URL resource = ClassLoader.getSystemClassLoader().getResource(name);
	    if( resource == null ){ resource = getClass().getClassLoader().getResource(name); }
	    if( resource == null ){ resource = getClass().getResource(name); } //[DR: without this line, it doesn't always find icons
	    if (resource==null) { // oddly, this doesn't get caught below!
		System.err.println("WARNING: Failed to load image " + name + ". Please make sure the image is on your CLASSPATH.");
		return null;
	    }
	    // return image
	    //return new ImageIcon(Toolkit.getDefaultToolkit().getImage(resource));
            return new ImageIcon(resource); // [DR] this might be simpler and have the same effect?
	} catch(Exception e) { 
	    System.err.println("WARNING: Failed to load image " + name + ". Please make sure the image is on your CLASSPATH.");
	    return null;
	}
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
	windSeconds=seconds;
    }

    /** 
     * get the number of seconds the fast-forward and rewind buttons jump
     */
    public double getWindSkip() {
	return windSeconds;
    }


    /** set the player to send synchronize messages */
    public void setSendSynchronization(boolean val) {
	synchrobutton=val;
	synchroCheck.setSelected(val);
    }

    /** change the rate of play of all the known media players */
    public void broadcastRate() {
	double tick = (double)rateSlider.getValue()/100.0;
	rate = (float)Math.pow(4.0,tick);
	niteclock.changeRate(rate);
	//System.out.println("Changed rate to " + rate);
    }
    
	
    /**
     * Action for the reset button of the rate slider. Resets the play rate to 0.
     */
    private class ResetRateAction extends AbstractAction {
	public ResetRateAction() {
	    super("Reset");
	    putValue(Action.SHORT_DESCRIPTION,"Reset the play rate to 0.");
	}
	
	public void actionPerformed(ActionEvent e) {
	    rateSlider.setValue(0);
	    broadcastRate();
	    resetRateAction.setEnabled(false);
	}
    }

    /** Called by registered displays to allow the slider to be set correctly. */
    public void registerMaxTime(int max) {
	maxregistered=true;
	maxtime=max;
	timeSlider.setMaximum(max);	
	timeSlider.repaint();
	timeSlider.updateUI();
    }

    /**
     * <p>Returns the maximum time. If the maximum has not yet been registered
     * with {@link #registerMaxTime(int) registerMaxTime()}, this method will
     * return -1.</p>
     *
     * @return the maximum time or -1
     */
    public int getMaxTime() {
        if (!maxregistered)
            return -1;
        else
            return maxtime;
    }

    /** set the rate at which the clock attempts to synchronize with
     * its TimeHandlers. The default is 200 milliseconds (5 times a
     * second). If you set this too low, you'll find degradation in
     * performance! */
    public void setSyncRate(int milliseconds) {
	syncDelayMillis=milliseconds;
	add = syncDelayMillis/1000.0; 
    }

    /**
     * Called when the user presses Enter in the skip field. This method
     * will try to parse the current text value. If it is in the format
     * h:m:s or m:s, the system time will be changed. m and s must be between
     * 0 and 59. h must be 0 or greater. The text field will be updated to
     * show the current system time in a good format.
     * JC: this doc must be for a different method - this was just parsing
     * an int to find how many seconds to skip when rewinding or fast 
     * forwarding, and I've changed to double so can get more like
     * frame-rate skips.
     */

    private void skipEntered() {
        String time = skip.getText();
	boolean parseError=false;
	double t=-1;
	try {
	    //	    t = Integer.parseInt(time);
         t = Double.parseDouble(time);
	} catch(NumberFormatException ex) {
	    parseError = true;
	}

        if (t < 0) { parseError = true; }
        if (!parseError) {
            setWindSkip(t);
	} 
	skip.setText(""+windSeconds);
    }

    
    /**
     * If the selected signal is not already loaded, this loads it;
     * otherwise de-iconify and move to the top of the other windows.
     */
    private void showSelectedSignal() {
	if (meta==null || signals==null || observationname==null) {
	    return;
	}
	int selection = signalCombo.getSelectedIndex();
	if (selection<1) { return; }
	boolean wasplaying=false;
	if (niteclock.isMediaPlaying()) {
	    wasplaying=true;
	    pause();
	}
	
	/*
	// This is the old way before we handled agent signals..
	// the -1 is because the first entry in the selection box which is always null..
	NSignal sig = (NSignal)signals.get(selection-1);
	String filename = meta.getSignalPath() + File.separator
	    + observationname + "."	+ sig.getName()	+ "." + sig.getExtension();
	*/				    
	String filename = (String)sigfmap.get(signalCombo.getSelectedItem());
	NSignal sig = (NSignal)sigmap.get(signalCombo.getSelectedItem());
	String a = (String) sigamap.get(signalCombo.getSelectedItem());
	if (filename==null || sig==null) { 
	    return; 
	}
	
	//System.out.println("Show signal; " + filename);
	niteclock.showSignal(filename, sig, seconds, wasplaying, a);

	if (niteclock.player.isFMJManagerImplementation()) {
		rateSlider.setEnabled(false);
	}
    
    }

    /* Jonathan's attempt to have the clock face be a slave to a
     * single audio or video signal and pass round the time of the
     * signal, not our time. */

    /** set this clock face as a slave not master of the current time. */
    public void setSlaveMode(boolean sm) {
	slavemode=sm;
    }

    /** return this clock's mode: if true, we're not giving out our
     * time as the master time, but polling a media signal instead */
    public boolean slaveMode() {
	return slavemode;
    }
}
