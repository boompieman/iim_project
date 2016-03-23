/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, 
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

//NXT imports
import net.sourceforge.nite.gui.transcriptionviewer.*;
import net.sourceforge.nite.util.*;
import net.sourceforge.nite.nxt.*;
import net.sourceforge.nite.gui.mediaviewer.*;
import net.sourceforge.nite.meta.impl.*;
import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.nom.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import net.sourceforge.nite.time.*;
import net.sourceforge.nite.search.*;
import javax.swing.*;
import java.text.DecimalFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.logging.*;


/**
 * This util class allows you to control the media-interface on the basis of predefined segments, with
 * actions such as 'goto next segment', 'loop', 'go to previous segment', 'play', 'pause', etc
 * <p>
 * To use this class, create a SegmentReplayer, then add all segments to it that you want to have 
 * available. Usually you would initialize it with data from a layer of non-overlapping selections,
 * in order to annotate something else along that _selection_ of the timeline.
 * <p>
 * When you create NEW segments, you can add them directly to this replayer.
 * <p>
 * Next, you should get the Actions from the segmentreplayer and add them to your GUI somewhere.
 * In general, a segmentreplayer needs not have its own physical GUI. For convenience purposes
 * however you can request a panel of the replayer that contains buttons for the actions.
 * The Action objects that you get on request are pointers to the 'real' actions, i.e. if you want
 * to change icons or keyboard shortcuts or mnemonics you can do that, and they will change in 
 * the convenience panel as well.
 * <p>
 * It is possible to automatically open a SegmentReplayer for all elements in a certain layer for the ContinuousVideoLabeler.
 * To do this, add the attributes segmentreplayer="true" and replaysegmentname="<name of segment elements>" to the corpussettingsnode
 * in the config XML file. THIS OPTINO WILL MOST PROBABLY DISAPPEAR IN THE FUTURE. USE IT FOR TESTING, BUT DON"T DEPEND ON IT TOO MUCH!
 * <p>
 * <p>
 * @author Dennis Reidsma, UTwente
 */
public class SegmentReplayer implements TimeHandler, NOMWriteElementContainer {


    Clock clock;
    /**
     * the playing will be started and stopped through the clockface.
     */
    ClockFace clockFace;

    /**
     * the playing will be started and stopped through the clockface.
     */
    public SegmentReplayer(ClockFace c) {
        clockFace = c;
        setClock(c.getClock());
    }

  /*================================================
    Core functions for replay of a 'current segment'
    ================================================*/

    /* ==== Replay options, states ==== */

    public static final int INACTIVE = 0;
    public static final int PLAYONCE = 1;
    public static final int LOOP     = 2;
    /**
     * The segmentreplayer has a few internal states.
     * <ul>
     * <li> {@link #LOOP}: keep repeating the current segment over and over again.
     * <li> {@link #PLAYONCE}: play the current segment once till the end.
     * <li> {@link #INACTIVE}: the segmentreplayer is currently not doing anything.
     * </ul
     */
    protected int STATE = INACTIVE;


    /**
     * effect depends on newstate.
     * LOOP: jump to start, start playing, keep looping
     * PLAYONCE: jump to start, start playing
     * INACTIVE: stop playing */
    public void setState(int newState) {
        STATE = newState;
        switch(STATE) {
            case INACTIVE:
                stopPlaying();
                break;
            case LOOP:
                jumpToStart();
                startPlaying();
                break;
            case PLAYONCE:
                jumpToStart();
                startPlaying();
                break;
        }
    }
     
    /** Jump to the start of the current segment. Used for e.g. looping or
     * jumping to start of new current segment.
     */
    protected void jumpToStart() {
        //System.out.println("Jump to start");
        if (currentSegment==null)return;
        clock.setSystemTime(currentSegment.getStartTime(),this);
    }
    
    /** Stop replaying. Time is kept as it is. State is kept as it is. The state may be changed by the caller of this method.
     */
    protected void stopPlaying() {
        //System.out.println("Stop playing");
        clockFace.pause();
    }

    /** Start replaying. If the clock time was outside the bounds of the current element, 
     * time is set to the start of the current element.
     * State is kept as it is. The state may be changed by the caller of this method.
     */
    protected void startPlaying() {
        //System.out.println("Start playing");
        if (currentSegment!=null) {
            if ((clock.getSystemTime() < currentSegment.getStartTime()) || (clock.getSystemTime() > currentSegment.getEndTime())) {
                clock.setSystemTime(currentSegment.getStartTime(),this);
            }
        }
        clockFace.play();
    }
    
    /* ==== Available segments, setting another segment to be replayed ==== */

    protected List availableSegments = new ArrayList();
    /** Arg: a list of NOMElements. These elements are checked for proper timing. All elements that are
     * OK for timing are then added to this segmentreplayer. the state of the replayer is set to inactive. */
    public void setAvailableSegments(List newSegments) {
        setState(INACTIVE);
        availableSegments = new ArrayList();
        for (int i = 0; i < newSegments.size(); i++) {
            NOMElement nextsegment = (NOMElement)newSegments.get(i);
            if ((nextsegment.getStartTime()!=NOMElement.UNTIMED)&&(nextsegment.getEndTime()!=NOMElement.UNTIMED)) {
                availableSegments.add(nextsegment);
            }
        }
    }
    /** Arg: a new NOMElement (at the end). It will be checked for proper timing. If it's
     * OK for timing, it's added to this segmentreplayer. The state of the replayer is kept as it was. */
    public void addAvailableSegment(NOMElement newSegment) {
        addAvailableSegment(newSegment,-1);
    }
    /** Arg: a new NOMElement (at the given index, -1 means at end. It will be checked for proper timing. If it's
     * OK for timing, it's added to this segmentreplayer. The state of the replayer is kept as it was. */
    public void addAvailableSegment(NOMElement newSegment, int index) {
        if ((newSegment.getStartTime()!=NOMElement.UNTIMED)&&(newSegment.getEndTime()!=NOMElement.UNTIMED)) {
            if (index >= 0) {
                availableSegments.add(index,newSegment); 
            } else {
                availableSegments.add(newSegment); 
            }
        }
    }
    /** An index of -1 means no element selected. Index points into available segments; currentsegment should be in agreement. Modify by setCurrentSegment
     */
    protected int currentIndex = -1;
    /** The currently selected segment for LOOP or REPLAY
     */
    protected NOMElement currentSegment = null;
    /**
     * Very useful to have if you want to be able to e.g. inspect the current element of the replayer on other attributes using another GUI element.
     */
    public NOMWriteElement getCurrentElement() {
        return (NOMWriteElement) currentSegment;
    }
    /**
     * Sets the segment that is now going to be replayed.
     * Afterwards, currentSegment and currentIndex will be in agreement
     * If the segment is not properly timed, a message is logged and the element is null and index -1.
     * If the segment is not in the list of availablesegments, currentsegment will be null and index -1.
     * <p>Further reaction depends on state:
     * <br>LOOP: state stays in LOOP, clock jumps to start of new segment
     * <br>INACTIVE: stay INACTIVE. Only jump to start if current time outside segment time
     * <br>PLAYONCE: stay PLAYONCE, clock jumps to start of new segment.
     */
    public void setCurrentSegment(NOMElement newSegment) {
        if ((newSegment.getStartTime()==NOMElement.UNTIMED)||(newSegment.getEndTime()==NOMElement.UNTIMED)) {
            Logger.global.severe("Can't set element as current element for SegmentReplayer: element " + newSegment.getID() + " is not properly timed!");
            setNull();
            return;
        }
        currentIndex = availableSegments.indexOf(newSegment);
        if (currentIndex == -1) {
            Logger.global.severe("Can't set element as current element for SegmentReplayer: element " + newSegment.getID() + " is not available!");
            setNull();
            return;
        }
        segNrL.setText("Number: "+(currentIndex+1));
        currentSegment = newSegment;
        enableActions(true);
        switch (STATE) {
            case INACTIVE:
                double time = clock.getSystemTime();
                if ((time<currentSegment.getStartTime())||(time>currentSegment.getEndTime())) {
                    jumpToStart();
                }
                break;
            case LOOP:
                setState(INACTIVE);
                jumpToStart();
                setState(LOOP);
                break;
            case PLAYONCE:
                setState(INACTIVE);
                jumpToStart();
                setState(PLAYONCE);
                break;
        }
    }
    /**
     * sets current segment. If -1, element will be set to null. If outside bounds, element will be null and index will be -1, otherwise 
     * element and index will reflect new current element. Calls setCurrentSegment(NOMElement newSegment)
     * to do the job.
     */
    protected void setCurrentSegment(int i) {
        if ((i < -1) || (i >= availableSegments.size())) {
            Logger.global.severe("Can't set element as current element for SegmentReplayer: index " + i + " outside bounds.");
            setNull();
            return;
        }
        if (i == -1) {
            setNull();
            return;
        }
        setCurrentSegment((NOMElement)availableSegments.get(i));
    }
    
    /**
     * set current element to null: state will be inactive, next&previous etc actions will be set inactive
     */
    protected void setNull() {
        currentSegment = null;
        setState(INACTIVE);
        enableActions(false);
        segNrL.setText("<no segment>");

    }

    /**
     * sets the next segment as current. cycles around the list.
     */
    protected void nextSegment() {
        int i = currentIndex + 1;
        if (i>=availableSegments.size()) {
            i = 0;
        }
        setCurrentSegment(i);
    }
    /**
     * sets the previous segment as current. cycles around the list.
     */
    protected void previousSegment() {
        int i = currentIndex - 1;
        if (i<0) {
            i = availableSegments.size()-1;
        }
        setCurrentSegment(i);
    }  
    /**
     * sets the first segment as current.
     */
    protected void firstSegment() {
        setCurrentSegment(0);
    }        
    /**
     * sets the last segment as current.
     */
    protected void lastSegment() {
        setCurrentSegment(availableSegments.size()-1);
    }        
        
  /*================================================
    GUI: the actions that are used to browse through
    the segments. And a convenience panel that shows 
    them.
    ================================================*/
    
    /**
     * Actions: Each action is stored in the action map under its (static) name.
     * The method setupActions
     * creates and initializes these actions. 
     * getActionMap also initializes map if non-existent...
     * <p>
     * Example: the action to go to the next segment is called NEXT_SEGMENT_ACTION. 
     */
    private ActionMap actMap;
    /**
     */
    public ActionMap getActionMap() {
        if (actMap == null) {
            setupActions();
        }
        return actMap;
    }
    /**
     * Next segment action: go to the next segment.
     */
    public static final String NEXT_SEGMENT_ACTION = "Go to next segment";
    /**
     * Previous segment action: go to the previous segment.
     */
    public static final String PREVIOUS_SEGMENT_ACTION = "Go to previous segment";
    /**
     * Go to first segment
     */
    public static final String FIRST_SEGMENT_ACTION = "Go to first segment";
    /**
     * Go to last segment
     */
    public static final String LAST_SEGMENT_ACTION = "Go to last segment";
    /**
     * Start loop-replaying the current segment
     */
    public static final String LOOP_ACTION = "Start loop-replaying";
    /**
     * Play the current segment once
     */
    public static final String PLAYONCE_ACTION = "Play segment once";
    /**
     * Pause replay
     */
    public static final String PAUSE_ACTION = "Pause replaying";


    protected boolean actionsEnabled = true;
    /**DR Note: fst and lst always active*/
    protected void enableActions(boolean en) {
        if (actionsEnabled==en) {
            return;
        }
        ActionMap amap = getActionMap(); 
        amap.get(NEXT_SEGMENT_ACTION).setEnabled(en);
        amap.get(PREVIOUS_SEGMENT_ACTION).setEnabled(en);
        amap.get(LOOP_ACTION).setEnabled(en);
        amap.get(PLAYONCE_ACTION).setEnabled(en);
        amap.get(PAUSE_ACTION).setEnabled(en);
    }
    
    
    /**
     * 
     */
    protected void setupActions() {
        actMap = new ActionMap();
        Action act = null;
        
        //Next segment action: go to the next segment.
        act = new AbstractAction("Next segment") {
            public void actionPerformed(ActionEvent ev) {
                nextSegment();
            }
        };
        /*if (getClass().getResource("/eclipseicons/etool16/save_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/save_edit.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }*/
        act.putValue(Action.SHORT_DESCRIPTION,"Go to the next segment.");
        actMap.put(NEXT_SEGMENT_ACTION, act);
        
        //Previous segment action: go to the previous segment.
        act = new AbstractAction("Previous segment") {
            public void actionPerformed(ActionEvent ev) {
                previousSegment();
            }
        };
        /*if (getClass().getResource("/eclipseicons/etool16/save_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/save_edit.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }*/
        act.putValue(Action.SHORT_DESCRIPTION,"Go to the previous segment.");
        actMap.put(PREVIOUS_SEGMENT_ACTION, act);

        //First segment action: go to the first segment.
        act = new AbstractAction("First segment") {
            public void actionPerformed(ActionEvent ev) {
                firstSegment();
            }
        };
        /*if (getClass().getResource("/eclipseicons/etool16/save_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/save_edit.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }*/
        act.putValue(Action.SHORT_DESCRIPTION,"Go to the first segment.");
        actMap.put(FIRST_SEGMENT_ACTION, act);

        //Last segment action: go to the last segment.
        act = new AbstractAction("Last segment") {
            public void actionPerformed(ActionEvent ev) {
                lastSegment();
            }
        };
        /*if (getClass().getResource("/eclipseicons/etool16/save_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/save_edit.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }*/
        act.putValue(Action.SHORT_DESCRIPTION,"Go to the last segment.");
        actMap.put(LAST_SEGMENT_ACTION, act);

        //Start loop-replaying the current segment
        act = new AbstractAction("Loop") {
            public void actionPerformed(ActionEvent ev) {
                setState(LOOP);
            }
        };
        /*if (getClass().getResource("/eclipseicons/etool16/save_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/save_edit.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }*/
        act.putValue(Action.SHORT_DESCRIPTION,"Start looping.");
        actMap.put(LOOP_ACTION, act);

        //Start loop-replaying the current segment
        act = new AbstractAction("Play once") {
            public void actionPerformed(ActionEvent ev) {
                setState(PLAYONCE);
            }
        };
        /*if (getClass().getResource("/eclipseicons/etool16/save_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/save_edit.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }*/
        act.putValue(Action.SHORT_DESCRIPTION,"Play once.");
        actMap.put(PLAYONCE_ACTION, act);

        //Start loop-replaying the current segment
        act = new AbstractAction("Pause") {
            public void actionPerformed(ActionEvent ev) {
                setState(INACTIVE);
            }
        };
        /*if (getClass().getResource("/eclipseicons/etool16/save_edit.gif") != null) {
            Icon value = new ImageIcon(getClass().getResource("/eclipseicons/etool16/save_edit.gif"));
            act.putValue(Action.SMALL_ICON, value);
        }*/
        act.putValue(Action.SHORT_DESCRIPTION,"Pause.");
        actMap.put(PAUSE_ACTION, act);
    }
    
    private JPanel simplegui = null;
    private JLabel segNrL = new JLabel("<no segment>");
    /** 
     * returns a trivially simple gui panel for controlling this replayer.
     */
    public JPanel getGuiPanel() {
        if (simplegui == null) {
            initGuiPanel();
        }
        return simplegui;
    }
    protected void initGuiPanel() {
        simplegui = new JPanel();
        ActionMap amap = getActionMap();
        simplegui.add(new JButton((Action)amap.get(FIRST_SEGMENT_ACTION)));
        simplegui.add(new JButton((Action)amap.get(PREVIOUS_SEGMENT_ACTION)));
        simplegui.add(segNrL);
        simplegui.add(new JButton((Action)amap.get(NEXT_SEGMENT_ACTION)));
        simplegui.add(new JButton((Action)amap.get(LAST_SEGMENT_ACTION)));
        simplegui.add(new JButton((Action)amap.get(PAUSE_ACTION)));
        simplegui.add(new JButton((Action)amap.get(PLAYONCE_ACTION)));
        simplegui.add(new JButton((Action)amap.get(LOOP_ACTION)));
    }
    
    
  /*================================================
                    TimeHandler
    ================================================*/

    /**The reaction to changes in the system time depends on the state.
     * LOOP: check if we reached end of segment; if so, jump to start.
     * PLAYONCE: check if we reached end of segment; if so: stop playing.
     * INACTIVE: nothing.                   */
    public void acceptTimeChange(double systemTime) {
        if (currentSegment==null)return;
        switch (STATE) {
            case INACTIVE: 
                //System.out.println("timechange/inactive");
                break;
            case LOOP:
                //System.out.println("timechange/loop");
                if (systemTime >= currentSegment.getEndTime()) {
                    setState(INACTIVE);
                    jumpToStart();
                    setState(LOOP);
                }
                break;
            case PLAYONCE:
                //System.out.println("timechange/playonce");
                if (systemTime >= currentSegment.getEndTime()) {
                    jumpToStart();
                    setState(INACTIVE);//if i do stopplaying HERE, I'll get loopy calls to clockface.pause/accepttimechange/...
                }
                break;
        }
    }
        
    public void acceptTimeSpanChange(double start, double end) {
        //we are never responsible for timeSPANchanges.
        //if this happens, we don't understand them, and just stop controlling replay for a while, until 
        //one of the actions (loop, nextsegment, etc) are activated again.
        STATE = INACTIVE;
        //DR: but we might consider jumping to the strat time of the span?
    }
        
    public Clock getClock() {
        return clock;
    }
        
    public void setClock(Clock clock)  {
        if (this.clock != null) {
            System.out.println("Tried setting segmentreplayer to a different clock, is not allowed");
            throw new RuntimeException("Tried setting segmentreplayer to a different clock, is not allowed");
        }
        this.clock = clock;
        clock.registerTimeHandler(this);
    }
        
    public void setTime(double time) {
        clock.setSystemTime(time, this);
    }
        
    public void setTimeHighlightColor(java.awt.Color color) {
    }
        
    public void setTimeSpan(double start, double end) {
        clock.setTimeSpan(start, end);
    }
    
    /** get the latest time handled by this TimeHandler - this returns
     * 0.0 because we're not responsible for actual media here! */
    public double getMaxTime() {
        return 0.0;
    }
}
