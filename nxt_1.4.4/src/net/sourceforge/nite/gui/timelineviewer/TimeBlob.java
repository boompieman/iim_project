package net.sourceforge.nite.gui.timelineviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import net.sourceforge.nite.nom.nomwrite.NOMElement;

/**
 * A simple blob on a timeline. Most of the settings come from the
 * parent element (a 'Grid'). We just hold our layer name which can't
 * change; start and end times in milliseconds; any label to display.
 * This is somewhat influenced by Mike Flynn's timeline approach in
 * JFerret.
 *
 * @see		TimeGrid
 * @author	Craig Nicol, Jonathan Kilgour, Mike Flynn
 */
public class TimeBlob implements TimedComponent {
    private int startMS=0; // start and end in milliseconds
    private int endMS=0;
    private TimeGrid parentGrid=null;
    private String layerName=null; // unique for blobs in one line in the parent TimeGrid
    private int recursiveDepth=1;
    private String label=null;
    private boolean selected=false;
    private boolean highlighted=false;
    protected static final int cornerRadius=5; // roundness of myWidgets
    protected static final Font	font = Font.decode("Comic Sans MS-PLAIN-12");
    protected NOMElement element;

    /** Create a blob on the TimeGrid */
    /**
       @param grid - grid to draw the blob on.
       @param st - start time in ms
       @param en - end time in ms
       @param name - name of parent layer for this blob
       @param depth - each layer is split into horizontal levels, this defines which of these to put the blob on.
       @param label - what to write on the blob
    **/
    public TimeBlob(TimeGrid grid, int st, int en, String name, int depth, String label) {
    	initialise(grid, st, en, name, depth, label, null);
    }

    /** Create a blob on the TimeGrid without NiteTimeline delegates */
    /**
       @param grid - grid to draw the blob on.
       @param el - Element to be added 
    **/
    public TimeBlob(TimeGrid grid, NOMElement el) {
            noDelegateInitialiseElement(grid, el);
    }
    
    /** Create a blob on the TimeGrid with NiteTimeline delegates */
    /**
       @param grid - grid to draw the blob on.
       @param el - Element to be added
       @param timeline - Grandparent NiteTimeline managing this blob 
    **/
   public TimeBlob(TimeGrid grid, NOMElement el, NiteTimeline timeline) {
        if (timeline == null) {
            noDelegateInitialiseElement(grid, el);
        } else {
        	initialise(grid, 
    			(int) (el.getStartTime() * 1000.0), (int) (el.getEndTime() * 1000.0),
    			timeline.getElementToLayerDelegate().getTranscriptionText(el), // layer name
    			timeline.getElementLayerDepth(el), // depth
    			timeline.getElementToTextDelegate().getTranscriptionText(el), // label
    			el);    	
		}
    }
    
    private void noDelegateInitialiseElement(TimeGrid grid, NOMElement el) {
    	initialise(grid, 
    			(int) (el.getStartTime() * 1000.0), (int) (el.getEndTime() * 1000.0),
    			el.getName(), // layer name
    			el.getRecursiveDepth(), // depth
    			el.getText(), // label
    			el);    
    }
    
    private void initialise(TimeGrid grid, int st, int en, String name, int depth, String label, NOMElement el) {
    	//System.out.println("initialise(" + grid + ", " + st + "," + en + "," + name + "," + depth + "," + label + "," + el);
    	parentGrid=grid;
    	layerName=name;
    	startMS=st;
    	endMS=en;
    	recursiveDepth=depth;
    	this.label=label;
    	element = null;
    	Component vc = createVisualComponent();
    	//vc.setPreferredSize(new Dimension(vc.getBounds().width, vc.getBounds().height)); // Shrink mouse listening window
    	grid.registerMaxTime(en);
    	grid.registerLayer(name, depth);
    	vc.addMouseListener((myWidget) vc);
    	vc.addMouseMotionListener((myWidget) vc);
    	element = el;
    	grid.add(vc);
    	//vc.paint(grid.getGraphics());
    }
    // SETTING AND GETTING params
    
    /** NOMElement displayed by this blob **/
    public NOMElement getNOMElement() {
    	return element;
    }
    
    /** start time in milliseconds */
    public int getStart() {
	return startMS;
    }

    /** end time in milliseconds */
    public int getEnd() {
	return endMS;
    }

    /** length in milliseconds */
    public int getLength() {
	return endMS - startMS;
    }

    /** Layer name */
    public String getLayerName() {
	return layerName;
    }

    /** Recursive depth */
    public int getRecursiveDepth() {
	return recursiveDepth;
    }

    /** get Colour from parent grid */
    protected Color getCol() {
	return parentGrid.getColour(this);
    }
    
    /** is this blob selected? **/
    public boolean getSelected() {
    	return selected;
    }
    
    public void setSelected(boolean s) {
        selected = s;
    }
    
    /** is this blob highlighted? **/
    protected boolean getHighlighted() {
    	return parentGrid.getHighlighted(this);
    }

    protected TimeBlob getBlob() {
    	return this;
    }
    
    /** get height from parent grid */
    protected int getHei() {
	return parentGrid.getHeight(this);
    }

    /** get width from parent grid */
    protected int getWid() {
	return parentGrid.getWidth(this);
    }

    /** get x position on parent grid */
    protected int getXval() {
	return parentGrid.getX(this);
    }

    /** get y position on parent grid */
    protected int getYval() {
	return parentGrid.getY(this);
    }

    /** is the NOMElement of this blob a reult of an active query **/
    protected boolean isQueryResult() {
    	return parentGrid.isQueryResult(this);
    }
    
    /** get Dimension from parent grid */
    protected Dimension getDim() {
	return parentGrid.getDimension(this);
    }

    /** create a visual representation of the blob: a myWidget */
    protected Component createVisualComponent() {
	JPanel widget = new myWidget();
	return widget;
    }

    /** the visual element of the blob */
    public class myWidget extends JPanel implements MouseListener, MouseMotionListener {
    	
    	public void paint(Graphics g) {
    		Color c = getCol();
    		int wid = getWid();
    		int hei = getHei();
    		int x = getXval();
    		int y = getYval();
    		g.setColor(c);
    		//System.out.println("Blob From: " + x + " for: " + wid);
    		if (wid<1) wid=1; // make sure we draw something if only a line..
    		if (hei<1) hei=1;
    		g.fillRoundRect(x, y, wid, hei, cornerRadius, cornerRadius);
    		g.setColor(getSelected() ? parentGrid.getSelectedColor()
    				   : (isQueryResult() ? parentGrid.getQueryHighlightColor() 
    				      : (getHighlighted() ? parentGrid.getTimeHighlightColor() 
    					     : Color.black) ) );
    		//g.setColor(selected ? Color.red : c);
    		//if (selected) {
    		g.drawRoundRect(x, y, wid-1, hei-1, cornerRadius, cornerRadius);
    		//}
    		//TODO: Hide text if it overflows blob
    		if(parentGrid.showLabels() && label!=null) {
    			g.setColor(distinctColor(c));
    			g.setFont(font);
    			if (parentGrid.isHorizontal()) { x+=1; y+=parentGrid.getBlobFatness()/2; }
    			else { y+=14; x+=1; } // y increases by size of font plus a wee bit
    			g.drawString(label, x, y);
    		}
    	}
    	
    	/**************************************************************************
         * 
         * Mouse interaction and selection
         *
         **************************************************************************/
         
        protected void processMouseMotionEvent(MouseEvent e) {
        	//System.out.println("Selection started...");
        }
        	
        private String rangeToString(int low, int mid, int high) {
        	return "" + low + " <= " + mid + " <= " + high;
        }
        
        private boolean inRange(int low, int mid, int high) {
        	return (low <= mid) && (mid <= high);
        }
        
    	/* (non-Javadoc)
		 * @see javax.swing.JComponent#getBounds(java.awt.Rectangle)
		 */
		public Rectangle getBounds(Rectangle rv) {
		   if (rv == null) rv = new Rectangle();
		   
    		rv.width = getWid();
    		rv.height = getHei();
    		rv.x = getXval();
    		rv.y = getYval();
    		if (rv.width<1) rv.width=1; // make sure we draw something if only a line..
    		if (rv.height<1) rv.height=1;
			return rv;
		}

		public Rectangle getBounds() {
			return getBounds(null);
		}

		public Dimension getPreferredSize() {
			int width = getWid();
			int height = getHei();
	    		if (width<1) width=1; // make sure we draw something if only a line..
	    		if (height<1) height=1;
			
			return new Dimension(width, height);
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		public void mouseClicked(MouseEvent e) {
    		//TODO: Allow multiple selection - but this should be config controlled
			//TODO: Allow editing
    		int x = getXval();
    		int y = getYval();
    		int w = getWid();
    		int h = getHei();
    		boolean thisclicked = 
    			inRange(x, e.getX(), x + w) &&
    			inRange(y, e.getY(), y + h);
    		if (thisclicked != selected) {
    			parentGrid.repaint(x, y, w, h);
    		}
    		selected = thisclicked;
    		if(selected) { parentGrid.setSelected(getBlob()); }
    	}

	public boolean contains (int x, int y) {
		return (inRange(getXval(), x, getXval()+getWid()) && inRange(getYval(), y, getYval()+getHei()));
	}

    	public void mousePressed(MouseEvent e) {
    		// TODO Auto-generated method stub
    	}

    	public void mouseReleased(MouseEvent e) {
    		// TODO Auto-generated method stub
    	}

    	public void mouseEntered(MouseEvent e) {
    		// TODO Auto-generated method stub
    		//System.out.println("blob mouseEntered...");				
    	}

    	public void mouseExited(MouseEvent e) {
    		// TODO Auto-generated method stub
    		//System.out.println("blob mouseExited...");				
    	}    

    	public void mouseDragged(MouseEvent e) {
    		// TODO Auto-generated method stub
    		//System.out.println("blob mouseDragged...");				
    	}    

    	public void mouseMoved(MouseEvent e) {
    		// TODO Auto-generated method stub
    		//System.out.println("blob mouseMoved...");	
     		int x = getXval();
    		int y = getYval();
    		int w = getWid();
    		int h = getHei();
    		int b = 5; // Border size
    		
    		System.out.println("TimeBlob moved :: " + x + " : " + e.getX() + " : " + (x + w) + ", " + 
    													y + " : " + e.getY() + " : " + (y + h));
    		
    		if(parentGrid.isHorizontal())	{ // Watch x edges
    			if(inRange(x,e.getX(),x+b) || inRange(x+w-b,e.getX(),x+w)) {
    				System.err.println("TimeBlob::Inside drag region.");
    			}
    		} else { // Watch y edges
    			if(inRange(y,e.getY(),y+b) || inRange(y+h-b,e.getX(),y+h)) {
    				System.err.println("TimeBlob::Inside drag region.");
    			}    		
    		}		
    	}    

    }

    /**
     * Return black or white, threshholding on the brightness
     */
    protected Color distinctColor(Color color) {
	float[] hsb = Color.RGBtoHSB(color.getRed(),
				     color.getGreen(),
				     color.getBlue(),	
				     null);
	return hsb[2]>0.5f ? Color.black : Color.white;
    }
    
}
