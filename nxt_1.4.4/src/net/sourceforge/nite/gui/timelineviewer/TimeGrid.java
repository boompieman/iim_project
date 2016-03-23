package net.sourceforge.nite.gui.timelineviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.*;

import net.sourceforge.nite.gui.util.NOMElementSelectionListener;
import net.sourceforge.nite.gui.util.NOMElementSelector;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.query.QueryResultHandler;
import net.sourceforge.nite.time.DefaultClock;
import net.sourceforge.nite.time.Clock;
import net.sourceforge.nite.util.Debug;

/**
 * A simple timeline. This can display layers of elements Influenced
 * but not the same as the Timeline in JFerret by Mike Flynn. This
 * should be used with TimeBlob.
 *
 * To allow this class to create new elements, you need to pass a valid class
 * to @link #setElementCreator.
 * 
 * @see		TimeBlob
 * @author	Craig Nicol, Jonathan Kilgour, Mike Flynn
 */

/* TODO: Add vertical scrollbar when required. */

public class TimeGrid extends JPanel implements net.sourceforge.nite.time.ScrollingTimeHandler, MouseListener, NOMElementSelector, QueryResultHandler {
    private class layerHeader extends JComponent {
	static final int headerheight = 20;
	static final int headerheightdiv2 = headerheight/2;
	private int headerwidth = 40;

	public layerHeader() {
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
	    //TODO make these dimensions more flexible
	    if (horizontal) return new Dimension(headerwidth, 300);
	    else return new Dimension(600, headerwidth);
	}

	public String getName() {
	    return "LayerHeader";
	}

	public void paintComponent(Graphics g) {
	    super.paintComponent(g); // Draw background

	    FontMetrics fm = g.getFontMetrics();
	    Rectangle r = g.getClipBounds();

	    Iterator layerNames = layerLocations.keySet().iterator();
	    headerwidth = 40;
	    while (layerNames.hasNext()) {
		String name = (String) layerNames.next();
		if (fm.stringWidth(name) > headerwidth) 
		    {
			headerwidth = fm.stringWidth(name);
		    }

		int y = ((Integer) layerLocations.get(name)).intValue();
		if (horizontal) g.drawString(name, headerheightdiv2, y+(fm.getHeight()/2));
		else g.drawString(name, y-(fm.stringWidth(name)/2), headerheightdiv2);
	    }
	    headerwidth += 10; // Add safety margin

	    // JAK - this setSize can cause repaint and hence looping!
            // I believe it is redundant - this component returns a
            // preferred size when asked.
            // setSize(getPreferredSize());
	}
    }

    private class timeHeader extends JLabel {
	//TODO Need to eliminate magic numbers
	static final int headerheight = 20;

	Color textcolor;

	public timeHeader() {
	    textcolor = Color.red;
	    initialise();
	}

	public timeHeader(Color c) {
	    textcolor = c;
	    initialise();
	}

	private void initialise() {
	    if (horizontal) setPreferredSize(new Dimension(getMaxTimeInMilliseconds()+extra, headerheight));
	    if (!horizontal) setPreferredSize(new Dimension(headerheight, getMaxTimeInMilliseconds()+extra));
	    //setPreferredSize(new Dimension(600,20));
	}

	//Need this to ensure callback sets up repainting correctly.
	public Dimension getPreferredSize() { 
	    if (horizontal) return new Dimension(timeToPixels(getMaxTimeInMilliseconds()+extra), headerheight);
	    else return new Dimension(headerheight, timeToPixels(getMaxTimeInMilliseconds()+extra));
	}

	public String getName() {
	    return "TimeHeader";
	}

	// TODO: Just use String.format() in Java 1.5, this is
	// a quick hack for 1.4.2
	private String twoDigitFormat(int i) {
	    if (i < 10) return "0" + i;
	    else return "" + i % 100;
	}

	public void paintComponent(Graphics g) {
	    super.paintComponent(g);

	    FontMetrics fm = g.getFontMetrics();
	    Rectangle r = g.getClipBounds();
	    int stime = 0;
	    int etime = 0;
	    int location;

	    if(horizontal) {
		stime = pixelsToTime(r.x);
		etime = stime + pixelsToTime(getVisibleRect().width);
		location = r.y;
	    } else {
		stime = pixelsToTime(r.y);
		etime = stime + pixelsToTime(getVisibleRect().height);
		location = r.x;
	    }
	    stime -= (stime % interval); // start on an interval boundary to match lines

	    // This needs to be kept in sync with the grid
	    // TODO update interval to prevent things getting squashed
	    //for(int time = 0; ; time += interval) {
	    for(int time = stime; time <= etime; time += interval) {
		int y = timeToPixels(time);
		//g.setColor(gridLineColour);
		g.setColor(textcolor);

		// Time in hh:mm:ss - ignore milliseconds for clarity
		String t = "" + (time / 3600000) + ":" + twoDigitFormat((time % 3600000) / 60000) + ":" + twoDigitFormat((time % 60000) / 1000);
		if (!horizontal) g.drawString(t, location + headerheight/2, y+(fm.getHeight()/4));
		else g.drawString(t, y-(fm.stringWidth(t)/2), location + headerheight/2);
	    }
            setSize(getPreferredSize());
	}
    }

    private int millisPerPixel=1000;
    private int interval = 100000;
    private int defaultDuration = 500; // in milliseconds
    private int layerFatness = 20; // the width (or height if horizontal) of all elements
    private int layerBreak = 5;    // the spacing between layers
    private Color gridLineColour=Color.lightGray;
    private Color selectedColour = Color.red;
    private Color timeHighlightColour=Color.blue;
    private Color queryHighlightColour = Color.orange;
    private Color elementColour=new Color(230,200,255);
    private int chosencolours=0;
    private boolean horizontal = true; // orientation of everything
    private int maxtime = 0;
    private int extra = 50000; // extra milliseconds of display after last thing..
    private boolean showLabels=false;
    private Map layerColours = new HashMap();
    private Map layerLocations = new HashMap();
    private Map layerDepth = new HashMap();
    private Set queryResults = new HashSet();
    private int totaldepth=0;
    private DefaultClock niteclock;
    private int starttime = -1;
    private int endtime = -1;
    private int selectionstarttime = -1;
    private int selectionendtime = -1;
    private TimeBlob selectedBlob = null;
    private TimeBlob lastSelectedBlob = null;
    private List neselectionlisteners = new ArrayList();
    private layerHeader layerH = null;
    private timeHeader timeH = null;
    private boolean canEditElements = false;
    private NOMElement firstElement = null;
    private TimelineNOMElementCreator elementCreator = null; // can only create elements if this is non-null
    private NiteTimeline timeline = null;
    private boolean snapToTime = false; // Do mouse actions synchronise with the time cursor?

    /* Let's just use defaults.
       public TimeGrid(int scale, Color lineColor) {
       super(null); // no layout manager
       millisPerPixel = scale;
       gridLineColour = lineColor;
       doLayout();
       }
    */

    public TimeGrid() {
	super(null); // no layout manager
    }

    public TimeGrid(NiteTimeline ntl) {
        super(null); // no layout manager
        timeline = ntl;
    }

    public void reset() {
	removeAll();
	layerColours = new HashMap();
	layerLocations = new HashMap();
	layerDepth = new HashMap();
	totaldepth=0;
	chosencolours=0;
	elementColour=new Color(230,200,255);
    }

    /**
     * Paints the background - a grid and a scale.
     */
    protected void paintComponent(Graphics g) {
	//TODO: add layer names
	super.paintComponent(g);			// background
	drawGrid(g);
	Component[] cs = getComponents();
	for (int i=0; i<cs.length; i++) {
	    Component c = cs[i];
	    c.paint(g);
	}
    }

    public void addTimeHeader(JScrollPane jsp) {
	if (timeH == null) { timeH = new timeHeader(); };
	if (horizontal) {
	    jsp.setColumnHeaderView(timeH);
	} else {
	    jsp.setRowHeaderView(timeH);
	}
	timeH.addNotify();
    }

    public void addLayerHeader(JScrollPane jsp) {
	if (layerH == null) { layerH = new layerHeader(); };
	if (horizontal) {
	    jsp.setRowHeaderView(layerH);
	} else {
	    jsp.setColumnHeaderView(layerH);
	}
	layerH.addNotify();
    }

    public void testCustomLayer() {
	System.out.println("WARNING: testCustomLayer only works for horizontal grids.");
	JComponent jc = new timeHeader(Color.blue);
	jc.setBounds(jc.getX(), jc.getY(), jc.getWidth(), 50);
		
	addCustomLayer(jc);
    }

    public void addCustomLayer(JComponent clayer) {
	int pixeldepth = 0;
		
	if (horizontal)
	    pixeldepth = clayer.getHeight();
	else
	    pixeldepth = clayer.getWidth();

	int depth = ((pixeldepth + layerFatness - 1) / layerFatness); 
	registerLayer(clayer.getName(), depth);
		
	int offset = ((Integer) layerLocations.get(clayer.getName())).intValue();
	depth = ((Integer) layerDepth.get(clayer.getName())).intValue();

	// TODO: Add code to handle hesizing clayers
	int width = timeToPixels(getMaxTimeInMilliseconds());
	if (horizontal)
	    clayer.reshape(0, offset, width, depth);
	else
	    clayer.reshape(offset, 0, depth, width);

	System.out.println("clayer bounds: " + clayer.getBounds());

	this.add(clayer);
	clayer.repaint();
    }

    /** return the latest end time of any blob **/
    public double getMaxTimeInSeconds() {
	return (double) maxtime / 1000.0;
    }

    public int getMaxTimeInMilliseconds() {
	return maxtime;
    }

    /**
     * Do certain mouse actions snap to the current time?
     * Currently only affects:
     * <ul>
     * <li>Creation of new blobs (when clock is running).</li>
     * </ul>
     * @param snapToTime the value to set
     */
    public void setSnapToTime(boolean snapToTime) {
	this.snapToTime = snapToTime;
    }

    /**
     * Does the grid allow the start and end points of existing elements to be edited?
     * @param canEditElements the canEditElements to set
     */
    public void setCanEditElements(boolean canEditElements) {
	this.canEditElements = canEditElements;
    }

    /** set scale of grid - the number is the number of milliseconds
     * per pixel (default is 1000) 
     * NOTE: This automatically redraws the window */
    public void setScale(int pix) {
	if (pix<=0) { return; }
	millisPerPixel=pix;
	interval = 100*pix;
	/* Force update of size */
	registerMaxTime(maxtime);
	if (timeH != null) { timeH.repaint(); };
	if (layerH != null) { layerH.repaint(); };
	repaint();
    }

    /** set orientation of grid - true = horizontal; false = vertical */
    public void setHorizontal(boolean horiz) {
	horizontal=horiz;
    }

    /** set orientation of grid - true = horizontal; false = vertical */
    public void setLayerColour(String layername, Color col) {
	layerColours.remove(layername);
	layerColours.put(layername, col);	
    }

    /** get orientation of grid - true = horizontal; false = vertical */
    public boolean isHorizontal() {
	return horizontal;
    }

    /** set width of all blobs (or height if horizontal) */
    public void setBlobFatness(int val) {
	layerFatness=val;
    }

    /** get width of all blobs (or height if horizontal) */
    public int getBlobFatness() {
	return layerFatness;
    }

    /** set property where we try to show labels on Blobs */
    public void setBlobLabel(boolean val) {
	showLabels=val;
    }

    /** return true if Blobs should have labels */
    public boolean showLabels() {
	return showLabels;
    }

    public void setSelected(TimeBlob tb) {
	if (selectedBlob != null) {
	    selectedBlob.setSelected(false);
        }
	selectedBlob = tb;
	if (tb != null && !tb.getSelected()) {
	    tb.setSelected(true);
	}
	//System.out.println("Selected Blob : " + tb.element.getID());
	notifyNOMElementSelectionListeners();
	repaint();
    }

    public TimeBlob getSelectedBlob() {
	return selectedBlob;
    }

    // Return a List of selected elements
    public List getSelectedElements() {
	if (selectedBlob == null) {
	    return new ArrayList();
	}
	List l = new ArrayList();
	l.add(selectedBlob.element);
	return l;
    }

    private void notifyNOMElementSelectionListeners() {
	List selection = getSelectedElements();
	for (Iterator it = neselectionlisteners.iterator(); it.hasNext(); ) {
	    NOMElementSelectionListener l = (NOMElementSelectionListener) it.next();
	    l.selectionChanged(selection);
	}
    }

    public void addNOMElementSelectionListener(NOMElementSelectionListener l) {
	neselectionlisteners.remove(l);
	neselectionlisteners.add(l);
    }

    public void removeNOMElementSelectionListener(NOMElementSelectionListener l) {
	neselectionlisteners.remove(l);
    }

    /** translate time in milliseconds to pixels onscreen */
    public int timeToPixels(int timeinmilliseconds) {
	if (millisPerPixel == 0) return 0;
	return timeinmilliseconds / millisPerPixel;	
    }

    /** translate pixels onscreen to time in milliseconds */
    private int pixelsToTime(int pixels) {
	return pixels * millisPerPixel;
    }

    /**
     * Draw a grid on the background, representing time.
     */
    public void drawGrid(Graphics g) {
	for(int time = 0; ; time += interval) {
	    int y = timeToPixels(time);
	    if (horizontal  && y>getSize().width ) return;
	    if (!horizontal && y>getSize().height) return;
	    int s = timeToPixels(starttime);
	    int e = timeToPixels(endtime);
	    g.setColor(timeHighlightColour);
	    if (!horizontal) g.drawRect(0, s, getWidth(), e-s);
	    else g.drawRect(s, 0, e-s, getHeight());
	    g.setColor(gridLineColour);
	    if (!horizontal) g.drawLine(0, y, getWidth(), y);
	    else g.drawLine(y, 0, y, getWidth());
	}
    }

    /**
     * Draw a line on the background.
     */
    protected void drawLine(Graphics g, int x1, int y1, int x2, int y2, Color colour, int width) {
	Graphics2D gr = (Graphics2D) g;
	gr.setColor(colour);
	gr.setStroke(new BasicStroke());
	gr.drawLine(x1, y1, x2, y2);
    }


    // utilities for TimeBlobs 

    /** return dimension of given Blob in screen pixels */
    public Dimension getDimension(TimeBlob tb) {
	return new Dimension(20,20);
    }

    /** return Color of given Blob */
    public Color getColour(TimeBlob tb) {
	return (Color)layerColours.get(tb.getLayerName());
    }

    public boolean getHighlighted(TimeBlob tb) {
	// Highlighted IIF all or part of blob falls within current time selection
	return (tb.getStart() <= endtime && tb.getEnd() > starttime);
    }

    private int adjustedFatness(TimedComponent tb) {
	if (tb instanceof TimeBlob) return layerFatness;
	else return layerFatness * tb.getRecursiveDepth(); //((Integer)layerDepth.get(tb.getLayerName())).intValue();
    }
   
   
    /** return width of given TimedComponent in screen pixels */
    public int getWidth(TimedComponent tb) {
	if (horizontal) return timeToPixels(tb.getLength()); 
	else return adjustedFatness(tb);
    }

    /** return height of given Blob in screen pixels */
    public int getHeight(TimedComponent tb) {
	if (!horizontal) return timeToPixels(tb.getLength()); 
	else return adjustedFatness(tb);
    }

    /** get X position of Blob in screen pixels */
    public int getX(TimedComponent tb) {
	if (horizontal) return timeToPixels(tb.getStart()); 
	else return layerPos(tb);
    }

    /** get Y position of Blob in screen pixels */
    public int getY(TimedComponent tb) {
	if (!horizontal) return timeToPixels(tb.getStart()); 
	else return layerPos(tb);
    }

    /** calculate pos of layer taking recursive layer into account */
    public int layerPos(TimedComponent tb) {
	int laypos=((Integer)layerLocations.get(tb.getLayerName())).intValue();
	int add = (tb.getRecursiveDepth()-1) * (layerFatness + layerBreak);
	return laypos + add;
    }


    /** Called from TimeBlob when a new Blob is added - make sure the
     * scroll bar is long enough! */
    protected void registerMaxTime(int max) {
	if (max >= maxtime) {
	    maxtime=max;
	    if (horizontal) setPreferredSize(new Dimension(timeToPixels(max+extra), 100));
	    else setPreferredSize(new Dimension(100, timeToPixels(max+extra)));
	}
    }

    /** Called when a new layer is added - make sure the layer has a
	colour and location (and max recursive depth */
    public void registerLayer(String layername, int maxdepth) {
	Color c = (Color)layerColours.get(layername);
	if (c==null) {
	    layerColours.put(layername, chooseColour());
	}
	Integer lp = (Integer)layerLocations.get(layername);
	if (lp==null) {
	    layerLocations.put(layername, new Integer(calculatePosition()));
	}
	Integer dp = (Integer)layerDepth.get(layername);
	if (dp==null || dp.intValue()<maxdepth) {
	    layerDepth.remove(layername);
	    layerDepth.put(layername, new Integer(maxdepth));
	    if (dp==null)  totaldepth+=maxdepth;
	    else totaldepth += (maxdepth-dp.intValue());
	}
	if (layerH != null) { // TODO: need to recalculate width
	    layerH.repaint();
	}
    }

    /** select a colour for the layer - could be better! */
    private Color chooseColour() {
	if (chosencolours>0) {
	    elementColour = elementColour.darker();
	    float[]hsb = Color.RGBtoHSB(elementColour.getRed(), elementColour.getGreen(), 
					elementColour.getBlue(), null);
	    if (hsb[2]<0.4) { 
		int blue = elementColour.getRed()+130;
		int red = elementColour.getGreen()+170;
		int green = elementColour.getBlue()+130;
		if (red-blue>35) { blue=red; red+=35; green+=10; }
		else { green+=35; blue+=30; red-=30; }
		if (green>255) { green=255; }
		if (red>255) { red=255; }
		if (blue>255) { blue=255; }
		elementColour = new Color(red, green, blue);
	    }
	}
	chosencolours+=1;
	return elementColour;
    }

    /** Calculate the location for the first group of elements in this layer */
    private int calculatePosition() {
	//return (existingLayers+1)*layerBreak + existingLayers*layerFatness;
	return (totaldepth+1)*layerBreak + totaldepth*layerFatness;
    }

    /** Get the layer at the specified location. If this grid is horizontal,
	l will be a vertical (y) coordinates, otherwise it will be a
	horizontal (x) coordinate. 
    **/ 
    private String getLayerAt(int l) {
	// Find the furthest layer that starts before l
	String layer = null;
	int maxy = -1;
	Iterator layerNames = layerLocations.keySet().iterator();
	while (layerNames.hasNext()) {
	    String name = (String) layerNames.next();
	    int y = ((Integer) layerLocations.get(name)).intValue();
	    if(y > maxy && y <= l) {
		maxy = y;
		layer = name;
	    }
	}
	return layer;
    }

    private int getLayerDepthAt(String layer, int l) {
	int inlayerpos = l - ((Integer) layerLocations.get(layer)).intValue();
	return (inlayerpos / (layerFatness + layerBreak)) + 1;
	// int inlayerpos = (layerdepth-1) * (layerFatness + layerBreak);	
    }

    /**************************************************************************
     * 
     * ScrollingTimeHandler implementation
     *
     **************************************************************************/

    private int lasttime = Integer.MIN_VALUE;
    private double scrolltolerance = 0.9; 

    private int scrollsize(int time, int width) {
	//System.out.println("scrollsize: time, lasttime, width: " + time + ", " + lasttime + ", " + width);
	if ((lasttime >= 0) && (lasttime < time) && ((time - lasttime) < (width * scrolltolerance))) {
	    return 1;
	} else {
	    lasttime = time;
	    return width;
	}

    }

    private void scrollToTime(double time) {
	// Make sure time stays in window by scrolling so time is at top-left
	int t = timeToPixels((int) (time * 1000.0));
	if (isHorizontal()) {
	    this.scrollRectToVisible(new Rectangle(t, this.getVisibleRect().y, scrollsize(t, getVisibleRect().width), 1));
	} else {
	    this.scrollRectToVisible(new Rectangle(this.getVisibleRect().x, t, 1, scrollsize(t, getVisibleRect().height)));    		
	}
    }

    public double getMaxTime() {
	return getMaxTimeInSeconds();
    }

    public void acceptTimeChange(double systemTime) {
	starttime = (int) (systemTime * 1000.0);
	endtime = (int) (systemTime * 1000.0);
	scrollToTime(systemTime);
	repaint();
    }

    public void setTime(double time) {
	niteclock.setSystemTime(time);
    }

    public void acceptTimeSpanChange(double start, double end) {
	starttime = (int) (start * 1000.0);
	endtime = (int) (end * 1000.0);
	scrollToTime(start);
	repaint();
    }

    public void setTimeSpan(double start, double end) {
	niteclock.setTimeSpan(start, end);
    }

    public Clock getClock() {
	return niteclock;
    }

    public void setClock(Clock clock) {
	if (clock != null) {
	    this.niteclock = (DefaultClock) clock;
	    niteclock.registerTimeHandler(this);
	}
    }

    public void setTimeHighlightColor(Color color) {
	timeHighlightColour = color;	
    }

    public Color getTimeHighlightColor() {
	return timeHighlightColour;
    }

    /**************************************************************************
     * 
     * Mouse interaction and selection
     * 
     *************************************************************************/

    protected void processMouseMotionEvent(MouseEvent e) {
	//System.out.println("Selection started...");
    }


    private double mousestarttime = -1;
    private boolean justbuiltblob = false;
    public void mouseClicked(MouseEvent e) {
	JComponent jc = (JComponent)findComponentAt(e.getX(), e.getY());

        if (justbuiltblob == true && jc != this && getSelectedBlob() != null) {
            TimeBlob tb = getSelectedBlob();
            //System.out.println("Resizing this blob.");
        } else if (jc != this && jc != null) { // If there's a child, select it
	    dispatchEvent(new MouseEvent(jc,
					 e.getID(),
					 e.getWhen(),
					 e.getModifiers(),
					 e.getX(),
					 e.getY(),
					 e.getClickCount(),
					 e.isPopupTrigger()
					 ));
	}
		
        justbuiltblob = false;

    }

    public void mousePressed(MouseEvent e) {
	if (selectedBlob != null) {
	    lastSelectedBlob = selectedBlob;
	    selectedBlob.setSelected(false);
	}
	// Clear selection
	setSelected(null);

	int x = e.getX() - getLocation().x; // need to add scrolled position and subtract header
	int y = e.getY() - getLocation().y; // need to add scrolled position and subtract header

	if (snapToTime /* && niteclock.isMediaPlaying() */) {
	    mousestarttime = niteclock.getSystemTime();
	    System.out.println("Setting start time: " + mousestarttime);
	} else {
	    if (isHorizontal()) {
		if (layerH != null) {x -= layerH.getWidth(); };
		if (timeH != null) {y -= timeH.getHeight(); };
		mousestarttime = pixelsToTime(x) / 1000.0f;
	    } else {
		if (timeH != null) {x -= timeH.getWidth(); };
		if (layerH != null) {y -= layerH.getHeight(); };
		mousestarttime = pixelsToTime(y) / 1000.0f;
	    }
	}

    }

    public void mouseReleased(MouseEvent e) {
	JComponent jc = (JComponent)findComponentAt(e.getX(), e.getY());

	// TODO Allow drag-construction of elements
	if (elementCreator != null && jc == this) {
	    double mouseendtime;
	    int x = e.getX() - getLocation().x; // need to add scrolled position and subtract header
	    int y = e.getY() - getLocation().y; // need to add scrolled position and subtract header
	    int layerpos; // coordinate on the 'layer' axis

	    if (isHorizontal()) {
		if (layerH != null) {x -= layerH.getWidth(); };
		if (timeH != null) {y -= timeH.getHeight(); };
		mouseendtime = pixelsToTime(x) / 1000.0f;
		layerpos = y;
	    } else {
		if (timeH != null) {x -= timeH.getWidth(); };
		if (layerH != null) {y -= layerH.getHeight(); };
		mouseendtime = pixelsToTime(y) / 1000.0f;
		layerpos = x;
	    }

	    if (snapToTime /* && niteclock.isMediaPlaying() */) {
		mouseendtime = niteclock.getSystemTime();
	    }
			
            if (mouseendtime == mousestarttime) {
                mouseendtime += defaultDuration / 1000.0f;
            }

            if (mouseendtime < mousestarttime) {
                double temp = mouseendtime;
                mouseendtime = mousestarttime;
                mousestarttime = temp;
            }

	    // TODO We need to discover what layer/depth corresponds to current mouse position
	    // see layerLocations ? 
	    // - need to be able to set defaults (perhaps from NOMElementSelectionListener?)
	    if(mouseendtime > mousestarttime) { // Will be true unless defaultDuration = 0
		String layer = getLayerAt(layerpos);
		int depth = getLayerDepthAt(layer, layerpos);

		TimeBlob tb = new TimeBlob(this, elementCreator.createNewElement((float) mousestarttime, (float) mouseendtime, "New Blob", layer, depth), timeline);
		mousestarttime = -1;
		setSelected(tb);
		justbuiltblob = true;
	    }    		
	    repaint();
	}
    }

    public void mouseEntered(MouseEvent e) {
	// TODO Auto-generated method stub
    }

    public void mouseExited(MouseEvent e) {
	// TODO Auto-generated method stub
    }



    /**
     * @return the selectedColor
     */
    public Color getSelectedColor() {
	return selectedColour;
    }

    /**
     * @param selectedColor the selectedColor to set
     */
    public void setSelectedColor(Color selectedColor) {
	this.selectedColour = selectedColor;
    }

    /**************************************************************************
     * 
     * Query Result Handling
     *
     **************************************************************************/

    public void acceptQueryResult(NOMElement result) {
	queryResults.clear();
	queryResults.add(result);
	repaint();
    }

    public void acceptQueryResults(List results) {
	queryResults.clear();
	queryResults.addAll(results);
	repaint();
    }

    public boolean isQueryResult(TimeBlob tb) {
	return (tb.element != null) && (queryResults.contains(tb.element));
    }

    public void setQueryHighlightColor(Color color) {
	queryHighlightColour = color;
    }

    public Color getQueryHighlightColor() {
	return queryHighlightColour;
    }

    /**
     * @param elementCreator the elementCreator to set
     */
    public void setElementCreator(TimelineNOMElementCreator elementCreator) {
	this.elementCreator = elementCreator;
    }

    /** Assign a timeline to this timegrid (essential if you want to create blobs using delegates)
     * @param ntl the NiteTimeline to set
     */
    public void setTimeline(NiteTimeline ntl) {
	this.timeline = ntl;
    }
}
