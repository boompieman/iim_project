/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import javax.swing.ImageIcon;
import javax.swing.JComponent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import net.sourceforge.nite.nstyle.NConstants;
import net.sourceforge.nite.time.Clock;
import net.sourceforge.nite.time.TimeHandler;
import pnuts.awt.PnutsLayout;



import java.awt.Font;
import java.awt.Color;
import org.jdom.Element;
/**
  * A NITE GUI Element, this class is designed for the display of
  * objects in a grid-like way. It uses PnutsLayout to achieve
  * this. The user specifies the colspan and rowspan of entries using
  * the pnuts scripting language. The gridded entries are displayed on
  * a scrolling pane so that if they are big the user can still
  * inspect all entries. This can display timed elements in reponse to
  * system clock time change notifications by using NTimedLabels
  *
  * @author judyr
  * */
public class GridPanel extends JScrollPane implements TimeHandler {

    private JPanel panel;
    private Clock niteclock;
    private boolean hasBorder = false;
    private TimeIntervalMapper timemap;
    private TimeIntervalIterator currentTimeIterator = null;
    private Set previousSet = null;
    private Set currentSet = null;
    private double minStartTime = 999;
    private double maxEndTime = -999;
    private Color ccolor = new Color(200, 255, 200);
    

    /**
     * Create a GridPanel with the specified number of columns
     * @param cols
     */
    public GridPanel(int cols, boolean border) {
        super(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //super();
        panel = new JPanel();
       
	//,ipadx =5, ipady = 5 padx = 1, pady =1 expand = xy,
	        panel.setLayout(
            new PnutsLayout(
                "cols = " + cols + ",  halign=fill,valign=fill,expand=xy"));
	hasBorder = border;
        this.getViewport().add(panel);
        timemap = new TimeIntervalMapper();

    }
    
    public void setBackgroundColour(Color c){
    	panel.setBackground(c);
    }

    /**
     * Adds a display object to the gridded display with the specified row and column set up
     * In the future we may want to extend this so the user can specifiy the other pnuts parameters,
     * but colspan and rowspan seem necessary just now
     * @param rowspan How many rows the component should take up
     * @param colspan How may columns the component should take up
     * @start The start time of this component
     * @end The end time of this component
     * @param comp The component to be added to the display
     */
    public void addEntry(int rowspan, int colspan, double start, double end, String position, JComponent comp) {
        if ((start != 999) && (end != -999)){
        String pnuts = "colspan = " + colspan + ", rowspan = " + rowspan + ", halign =" + position;
        panel.add(comp, pnuts);
       
       minStartTime = Math.min(minStartTime, start);
        
       maxEndTime = Math.max(maxEndTime, end);
               
       timemap.addObject(comp, start, end);
        }
        
    }

    public void addEntry(int rowspan, int colspan,  String position, JComponent comp) {
        String pnuts = "colspan = " + colspan + ", rowspan = " + rowspan + ", halign =" + position;
        panel.add(comp, pnuts);
       
      
        
    }

    /**
     * Probably this is no longer needed
     * */
    public void addEntries(Object element) {

        if (element instanceof Element) {
            Element e = (Element) element;
            
            		
            	
            
            if (!e
                .getAttributeValue(NConstants.objectType)
                .equals(NConstants.GridPanelEntry)
                && !e.getAttributeValue(NConstants.objectType).equals(
                    NConstants.GridPanel)) {
                String content = getText(e);

                String colstring = e.getAttributeValue(NConstants.ColSpan);
                int colspan = new Integer(colstring).intValue();

                String rowstring = e.getAttributeValue(NConstants.RowSpan);
                int rowspan = new Integer(rowstring).intValue();
                String pnuts =
                    "colspan = " + colspan + ", rowspan = " + rowspan;
                JComponent comp = findComponent(e, content);
 		
                if (comp != null) {
                	panel.add(comp, pnuts);
                if (comp instanceof NTimedLabel){
                    System.out.println("label " + content);
                    
                   
                    //make an entry in the time mapper for the component, index by start and end times
                    Double std = getStartTime(e);
                    Double etd = getEndTime(e);
                double start = 0;
                double end = 0;
                if (std != null) {
                	start = std.doubleValue();
                	minStartTime = Math.min(minStartTime, start);
                }
            	if (etd != null){
            	  end = etd.doubleValue();
            	  maxEndTime = Math.max(maxEndTime, end);
            	}
            	timemap.addObject(comp, start, end);
                }
                }
            }
            List kids = e.getContent();
            Iterator i = kids.iterator();

            while (i.hasNext()) {

                Object next = i.next();
                if (next instanceof Element) {
                    Element nextel = (Element) next;

                    addEntries(nextel);

                }
            }
        }

    }

    private JComponent findComponent(Element e, String content) {

        String tag = e.getAttributeValue(NConstants.objectType);

       
        	Double std = getStartTime(e);
        	Double etd = getEndTime(e);
            double start = 0;
            double end = 0;
            if (std != null) start = std.doubleValue();
            if (etd != null) end = etd.doubleValue();
            ImageIcon icon = null;
            Color textcolour = null;
            Font font = null;
            String imagepath = e.getAttributeValue(NConstants.ImagePath);
            if (imagepath != null) {
                icon = new ImageIcon(imagepath);
            }
            
           int position = SwingConstants.LEFT;
            String layout = e.getAttributeValue(NConstants.layout);
            if (layout != null){
            	if (layout.equalsIgnoreCase("centre")) position = SwingConstants.CENTER;
            	if (layout.equalsIgnoreCase("right")) position = SwingConstants.RIGHT;
            }
            if (e.getAttributeValue(NConstants.foregroundColour)
                != null) {
                textcolour =
                    NConstants.getColour(
                       e.getAttributeValue(NConstants.foregroundColour));
            }

            String name = "Arial";
            int size = 12;
            int style = Font.PLAIN;
            if (e.getAttributeValue(NConstants.fontSize) != null) {
                size =
                    Integer.parseInt(
                        e.getAttributeValue(NConstants.fontSize));
            }
            if (e.getAttributeValue(NConstants.font) != null) {
                name = e.getAttributeValue(NConstants.font);

            }
            String fontstyle = e.getAttributeValue(NConstants.fontStyle);
            if (fontstyle != null) {
                if (fontstyle.equalsIgnoreCase(NConstants.bold)) {
                    style = Font.BOLD;
                } else if (fontstyle.equalsIgnoreCase(NConstants.italic)) {
                    style = Font.ITALIC;
                }
            }

            font = new Font(name, style, size);
            if (tag.equalsIgnoreCase(NConstants.TimedLabel)) {
        	NTimedLabel tlabel = null;
            tlabel =
                new NTimedLabel(content, icon, hasBorder, position);
            
            if (font != null)
                tlabel.setFont(font);
            if (textcolour != null)
                tlabel.setForeground(textcolour);
            
            return tlabel;
            }

        return null;

    }

    private String getText(Element e) {
        String temp = "";
        String type = e.getAttributeValue(NConstants.objectType);

        if (type.equalsIgnoreCase("InformationLabel")
            || (type.equalsIgnoreCase("TimedLabel"))) {
            temp = e.getTextTrim();
        }
        return temp;
    }

    /**
     * Causes the display components which correspond to the specified time to be highlighted
     * This uses the TimeIntervalInterator provided by TimeIntervalMapper to find all the 
     * display components for the specified time. A record of the previous iterator
     * is maintained so that it is possible to turn off the highlighting on components
     * which are no longer in the time frame
     * @see net.sourceforge.nite.time.TimeHandler#acceptTimeChange(double)
     */
    public void acceptTimeChange(double systemTime) {
    	
    	if (currentTimeIterator == null) currentTimeIterator = timemap.getTimeIntervalIterator();
    	currentTimeIterator.setTime(systemTime);
    	
    	
    	//retrieve the display components which are currently in time
    	//scope, and highlight them
    	if (currentTimeIterator != null){
	    currentSet  = currentTimeIterator.getMatchingObjects();
	    Iterator it = currentSet.iterator();
	    while(it.hasNext()){
		NTimedLabel display = (NTimedLabel) it.next(); 
		display.setTimeHighlit(true, ccolor);
	    }
	}
    		
	//if the set of objects which are in time scope has changed
	//since last clock update, find the objects which have changed
	//and remove the highlighting using Set operations
    		
	if (previousSet != null){
	    Set temp = new HashSet(previousSet);
	    temp.removeAll(currentSet);
	    
	    Iterator it = temp.iterator();
	    while(it.hasNext()){
		NTimedLabel display = (NTimedLabel) it.next(); 
		display.setTimeHighlit(false);
	    }
    	}

	//keep a record of the objects which are highlighted now so
	//that they can be unhighlighted later
	previousSet  = currentSet;
    }

    public void setTimeHighlightColor(Color color) {
	ccolor=color;
    }

    /**
     * @see net.sourceforge.nite.time.TimeHandler#setTime(double)
     */
    public void setTime(double time) {
        niteclock.setSystemTime(time);
    }

    /**
     * Causes the display components which are in time scope between the specified start and end times
     * to be highlighted
     *NOT YET IMPLEMENTED
     * @see net.sourceforge.nite.time.TimeHandler#acceptTimeSpanChange(double, double)
     */
    public void acceptTimeSpanChange(double start, double end) {
    	
    	
    	
    }

    /**
     * @see net.sourceforge.nite.time.TimeHandler#setTimeSpan(double, double)
     */
    public void setTimeSpan(double start, double end) {
        niteclock.setTimeSpan(start, end);
    }

    /**
     * @see net.sourceforge.nite.time.TimeHandler#getClock()
     */
    public Clock getClock() {
        return niteclock;
    }
    
    public void setClock(Clock c){
    	niteclock = c;
        niteclock.registerTimeHandler(this);
    }
    
    public Double getStartTime(Element obj) {
        Double starttime = null;

        String s = obj.getAttributeValue(NConstants.nomStartTime);
        if (s == null) {
            return null;
        }

        try {
            starttime = Double.valueOf(s);
        } catch (NumberFormatException e) {
            System.out.println("A number format exception");
            return null;
        }

        return starttime;
	}
	
	 public Double getEndTime(Element obj) {
        Double endtime = null;

        String s = obj.getAttributeValue(NConstants.nomEndTime);
        if (s == null) {
            return null;
        }

        try {
            endtime = Double.valueOf(s);
        } catch (NumberFormatException e) {
            System.out.println("A number format exception");
            return null;
        }

        return endtime;
	 }
	 
	 public double getMinStartTime(){
	 	return minStartTime;	
	 }
	 
	 public double getMaxEndTime(){
	 	return maxEndTime;	
	 }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.time.TimeHandler#getID()
     */
    public int getID() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.time.TimeHandler#setID(int)
     */
    public void setID(int i) {
        // TODO Auto-generated method stub
        
    }

    /** get the largest end time of any element so far added
     * to this Panel. Unimplemented. */
    public double getMaxTime() {
	return 0.0;
    }

}
