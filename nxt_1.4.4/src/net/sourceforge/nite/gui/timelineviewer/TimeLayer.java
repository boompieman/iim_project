package net.sourceforge.nite.gui.timelineviewer;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import net.sourceforge.nite.nom.nomwrite.NOMElement;

/** Used to add custom layers to TimeGrid
 *
 * @see TimeGrid
 * @author Craig Nicol
 **/

public class TimeLayer extends JComponent implements TimedComponent {
    private int startMS=0; // start and end in milliseconds
    private int endMS=0;
    private TimeGrid parentGrid=null;
	private NOMElement element=null;
   private int cornerRadius=5;
   protected Color background=Color.white;
   
	public TimeLayer(int start, int end, TimeGrid tg, NOMElement el) {
		startMS = start;
		endMS = end;
		parentGrid = tg;
		element = el;

		setName("TimeLayer");
		tg.registerMaxTime(end);
	}
	
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
		return getName();
    }

   /** if the default getHeight() is used, this will ask the grid
    * for a height equivalent to the number of layers returned.
    **/
	public int getRecursiveDepth() {
		return 1;
	}

	// TODO: Add TimeGrid methods to make these work
	// for horizontal and vertical grids

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

    	public void paint(Graphics g) {
    		Color c = Color.black;
    		int wid = getWid();
    		int hei = getHei();
    		int x = getXval();
    		int y = getYval();
    		g.setColor(background);

    		//System.out.println("Layer From: " + x + " for: " + wid);
    		if (wid<1) wid=1; // make sure we draw something if only a line..
    		if (hei<1) hei=1;
    		g.fillRoundRect(x, y, wid, hei, cornerRadius, cornerRadius);

    		g.setColor( Color.black );
    		g.drawRoundRect(x, y, wid-1, hei-1, cornerRadius, cornerRadius);

	   
	        //Draw a 1/4-tick every minute, 1/2 tick every 10 and 3/4 every hour
	        //TODO: Move this code to a child of TimeLayer so
	        // other children can use this method to draw
		// a proper bacground (?)
		if (!parentGrid.isHorizontal()) { return; };
	   
	         int tickheight = hei / 4;
	        for (int time = 0; time < endMS; time += 60000) {
		   int tickx = x + parentGrid.timeToPixels(time);
		   if ((time % (60 * 60 * 1000)) == 0) { //hour
		      g.drawLine(tickx, y, tickx, y+(tickheight*3));
		   } else if ((time % (10 * 60 * 1000)) == 0) { //10min
		      g.drawLine(tickx, y, tickx, y+(tickheight*2));
		   } else {
		      g.drawLine(tickx, y, tickx, y+tickheight);
		   }   
		}
	   
    	}
 
}
