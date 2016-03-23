package net.sourceforge.nite.datainspection.timespan;
import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;
import net.sourceforge.nite.datainspection.impl.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import net.sourceforge.nite.search.*;
import net.sourceforge.nite.time.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import java.util.*;
import net.sourceforge.nite.query.*;
import net.sourceforge.nite.util.*;
/**
 * Panel allowing zoom, time aligned as listener, and as actuator (click on panel to set time).
 * Simple implementation, not suitable for large amounts of data.
 *
 * You can add sub images for a named annotator. Each annotator list of sub images will be drawn
 * overlaying each other; the different annotator renders are drawn in rows above each other.
 * 
 * <p>As a {@link net.sourceforge.nite.search.QueryResultHandler QueryResultHandler} it will render
 * all query results by marking their times on the panel some distance below the lowest annotation
 * in red.
 * Handles query highlighting.
 */
class TimespanReportPanel extends JPanel implements TimeHandler, QueryResultHandler {
    
    public double zoom=2;
    public int w = 100;
    public int h = 20; //height per row
    
    /** Transform used to effect zoom on drawing */
    private AffineTransform at;
    
    /** The image being displayed. Externally available, so other classes can draw upon it. */
    public BufferedImage img;
    
    
    /**The amount (pixels) of space used by legenda, i.e. the location of the 0-secs line. needed for the timealignment and for drawing the subimages */
    public double headeroffset=50;
    /**The resolution of the images. Only needed for the timealignment; all drawing is done elsewhere. */
    public double millisecsperpixel=200;
    
    public ArrayList annotatorNames = new ArrayList();
    /** per-annotator lists of sub images. Key: annotator name. Value: arraylist of images */
    public Map subImages = new TreeMap();
    
    /** the annotations of two annotators are often in some way related to each other. this variable is a quick and dirty way for
    flexible visualisation of these relations. Key: Pair of annotator names. Value: List of Pairs of Doubles, signifying timestamps
    in the two annotations that should be related. This relation may e.g. be the alignment between two boundaries or segments. */
    public Map betweenAnnotatorRelations = new HashMap();

    public ArrayList annotatorsToDraw = new ArrayList();
    
    public TimespanReportPanel(BufferedImage newimg,int neww,int newh/*height per row*/,Clock newc, ArrayList annotatorNames) {
        if (newimg==null) {
            newimg = new BufferedImage(neww,newh, BufferedImage.TYPE_INT_ARGB );
        }
        img= newimg;
        w=neww;
        h=newh;
        this.annotatorNames=annotatorNames;
        annotatorsToDraw=annotatorNames;
        for (int i = 0; i < annotatorNames.size(); i ++ ) {
            subImages.put(annotatorNames.get(i),new ArrayList());
        }
        setZoom(1);
        initTimeHandler(newc);
    }

    public void addSubImage(String annotatorName, BufferedImage img) {
        ((ArrayList)subImages.get(annotatorName)).add(img);
    }
    
    /*===========================================================
                ZOOM functionality
                ================================================*/

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = ((Graphics2D)g);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
            
        g2.transform(at);
        g2.drawImage(img,null,0,0);
        //for each annotator: draw name, then draw sub images
        for (int i = 0; i < annotatorsToDraw.size(); i ++ ) {
            String nextName = (String) annotatorsToDraw.get(i);
            //draw name
            g2.setColor(Color.black);
            g2.drawString(nextName, 0,(1+i)*(h+4));
            
            //draw sub images
            ArrayList imgs = (ArrayList)subImages.get(nextName);
            for (int j = 0; j < imgs.size(); j++) {
                g2.drawImage((BufferedImage)imgs.get(j), (BufferedImageOp)null,(int)headeroffset , i*(h+4));
            }
        }
        
        g2.setColor(Color.black);
        //for each betweenAnnotatorRelation, 
        //this should be done upon modification, not every paint call!.
        Iterator pairIt = betweenAnnotatorRelations.keySet().iterator();
        System.out.println("draw relations");
        while (pairIt.hasNext()) {
            Pair nextPair = (Pair)pairIt.next();
            //System.out.println("draw relations " + nextPair.o1 + ", " + nextPair.o2);
            //if both annotators are drawn, draw the relation.
            if ((annotatorsToDraw.contains(nextPair.o1)) && (annotatorsToDraw.contains(nextPair.o2))) {
                //System.out.println("Yes");
                double time1=0;
                double time2=0;
                ArrayList relationList = (ArrayList)betweenAnnotatorRelations.get(nextPair);
                int annIndex1 = annotatorsToDraw.indexOf(nextPair.o1);
                int annIndex2 = annotatorsToDraw.indexOf(nextPair.o2);
                for (int i = 0; i < relationList.size(); i++ ) {
                    Pair rel = (Pair)relationList.get(i);
                    time1 = ((Double)rel.o1).doubleValue();
                    time2 = ((Double)rel.o2).doubleValue();
                    g2.drawLine((int)(headeroffset + time1*1000/millisecsperpixel),annIndex1*(h+4)+h/2,(int)(headeroffset + time2*1000/millisecsperpixel),annIndex2*(h+4)+h/2);
                }
            }
        }
        
    }
    public void setZoom(double newZoom) {
        if (newZoom != 0) {
            zoom = newZoom;
            at = AffineTransform.getScaleInstance(zoom, zoom);
      		setPreferredSize(new Dimension((int)(zoom * (double)w), (int)(zoom*(double)h*annotatorNames.size())+250));
            //warn scrollbars (panel size may have changed)
            revalidate();
            repaint();
	    }
    }            


    /*===========================================================
                TIMEHANDLING functionality
                ================================================*/
    protected Clock c;
    
    protected void initTimeHandler(Clock newc) {
        c = newc;
        newc.registerTimeHandler(this);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                double newTime = (((double)e.getX()-50d*zoom)*millisecsperpixel)/(1000d*zoom)  ;
                if (getClock()!=null)((ClockFace)getClock().getDisplay()).setTime(newTime);
            }
        });
    }
                
    double oldtime = -100000;
    
    public void acceptTimeChange(double systemTime) {
 	    if (Math.abs(systemTime-oldtime)<1) return;
 	    //paint on image of panel
        Graphics2D g = (Graphics2D)img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.black);
        g.setXORMode(Color.green);
        g.draw3DRect(50+(int)(((double)oldtime)*1000d/millisecsperpixel),0,(int)(1000d/millisecsperpixel),h*(5),false );
        g.draw3DRect(50+(int)(((double)systemTime)*1000d/millisecsperpixel),0,(int)(1000d/millisecsperpixel),h*5,false );
        oldtime=systemTime;
        repaint();
        
    }
    public void acceptTimeSpanChange(double start, double end) {
        acceptTimeChange(start);
    }
    public double getMaxTime() {
        return 0;
    }
    public void setClock(Clock clock) {
        c = clock;
    }
    public Clock getClock() {
        return c;
    }
    public void setTime(double time) {
    }
    public void setTimeHighlightColor(Color color)
    {
    }
    public void setTimeSpan(double start, double end){}

    /*===========================================================
                QUERYHANDLING functionality
                ================================================*/

    protected Color highlightColor = Color.red;
    protected java.util.List oldQResults = new ArrayList();
    public void acceptQueryResult(NOMElement result) {
        paintQueryHigh(oldQResults, Color.gray);
        oldQResults.clear();
        oldQResults.add(result);
        paintQueryHigh(oldQResults,highlightColor);
    }
    public void acceptQueryResults(java.util.List results) {
        paintQueryHigh(oldQResults, Color.gray);
        oldQResults.clear();
        oldQResults.addAll(results);
        paintQueryHigh(oldQResults,highlightColor);
    }
    public void setQueryHighlightColor(Color color) {
        highlightColor = color;
    }
    protected void paintQueryHigh(java.util.List results,Color c) {
        //TODO: jump window to lowest time!
        for (int i = 0; i < results.size(); i++) {
            NOMElement next = (NOMElement)results.get(i);
            double start = next.getStartTime();
            double end = next.getEndTime();
     	    //paint on image of panel
            Graphics2D g = (Graphics2D)img.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c);
            g.fill3DRect(50+(int)(((double)start*1000d)/millisecsperpixel),annotatorNames.size()*h+5,(int)(((end-start)*1000d)/millisecsperpixel),h-5,false );            
        }
        repaint();
    }

}