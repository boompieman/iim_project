package net.sourceforge.nite.tools.videolabeler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.time.Clock;
import net.sourceforge.nite.time.ClockFace;
import net.sourceforge.nite.time.TimeHandler;

/**
 * <p>A Feeltrace timeline is part of a Feeltrace target control panel (set at
 * construction). It displays a bar that from left to right covers the duration
 * of the signal being annotated. An annotation is visualised with the colour
 * of its emotion. The colour is obtained from the Feeltrace colour map
 * returned by the method {@link FeeltraceAnnotationLayer#getColourMap()
 * getColourMap()} of the Feeltrace annotation layer. Time spans that do not
 * contain any annotations will not be coloured and keep the control colour
 * (usually gray).</p>
 *
 * <p>The timeline can only be painted if the duration of the signal is known.
 * This is obtained from the display of the global clock. The display should be
 * an instance of {@link net.sourceforge.nite.time.ClockFace ClockFace}. If no
 * clock face could be found or if the maximum time has not been registered with
 * the clock face, this timeline will not show any annotations.</p>
 * 
 * <p>The timeline is automatically updated whenever the control is shown or
 * resized (the corpus will be searched for annotations). Otherwise the timeline
 * is not automatically updated to prevent many possibly costly corpus searches.
 * The timeline can be updated manually by calling {@link #repaint()
 * repaint()}. A new annotation can be displayed with
 * {@link #showAnnotation(double, double, java.awt.geom.Point2D)
 * showAnnotation()}.</p>
 *
 * <p>This class is a <code>TimeHandler</code>. It will show a black line at
 * the current time or a window around the current time span.</p>
 */
public class FeeltraceTimeLine extends JPanel implements TimeHandler {
    
    double maxTime = Double.NaN;
    BufferedImage buffer = null;
    FeeltraceTargetControlPanel parent;
    private Clock clock;
    private double currentTime = Double.NaN;
    private double currentSpanStart = Double.NaN;
    private double currentSpanEnd = Double.NaN;

    /**
     * <p>Constructs a new Feeltrace timeline to be part of the specified
     * target control panel.</p>
     *
     * @param parent the Feeltrace target control panel that will contain this
     * timeline
     */
    public FeeltraceTimeLine(FeeltraceTargetControlPanel parent) {
        super();
        this.parent = parent;
        setClock(Document.getInstance().getClock());
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                handleComponentShown(e);
            }
            
            public void componentHidden(ComponentEvent e) {
                handleComponentHidden(e);
            }
            
            public void componentResized(ComponentEvent e) {
                repaint();
            }
        });
    }
    
    /**
     * <p>Called when the timeline is shown. Registers this timeline as a
     * <code>TimeHandler</code> with the clock and repaints the timeline.</p>
     */
    private void handleComponentShown(ComponentEvent e) {
        clock.registerTimeHandler(this);
        repaint();
    }
    
    /**
     * <p>Called when the timeline is hidden. Deregisters this timeline as a
     * <code>TimeHandler</code> with the clock.</p>
     */
    private void handleComponentHidden(ComponentEvent e) {
        clock.deregisterTimeHandler(this);
    }
    
    /**
     * <p>Refreshes the buffer image by searching the corpus and painting
     * all found annotations. If the panel is not big enough, if the maximum
     * time is not known or if the corpus could not be searched, the buffer will
     * be set to null. This method sets maxTime to the current maximum time.</p>
     */
    private void refreshBuffer() {
        buffer = null;
        maxTime = Double.NaN;
        int width = getWidth();
        int height = getHeight();
        if (width < 3 || height < 3)
            return;
        JInternalFrame disp = clock.getDisplay();
        if (disp instanceof ClockFace) {
            maxTime = ((ClockFace)disp).getMaxTime();
            if (maxTime == -1)
                maxTime = Double.NaN;
        }
        if (Double.isNaN(maxTime))
            return;
        buffer = new BufferedImage(width-2,height-2,BufferedImage.TYPE_INT_ARGB);
        Graphics g = buffer.getGraphics();
        Document doc = Document.getInstance();
        String query = "($a " + parent.getLayer().getCodeElement().getName() + ")";
        if (parent.getAgent() != null)
            query += ": $a@" + doc.getMetaData().getAgentAttributeName() + " == \"" +
                    parent.getAgent().getShortName() + "\"";
        try {
            List elems = doc.searchAnnotations(query);
            Iterator it = elems.iterator();
            while (it.hasNext()) {
                NOMElement elem = (NOMElement)it.next();
                FeeltraceAnnotationLayer layer = parent.getFeeltraceLayer();
                Point2D emo = new Point2D.Double(layer.getEvaluation(elem),
                        layer.getActivation(elem));
                if (!Double.isNaN(elem.getEndTime()))
                    paintAnnotation(elem.getStartTime(),elem.getEndTime(),emo);
            }
        } catch (Throwable ex) {
            buffer = null;
            System.out.println("ERROR: Could not search the corpus: " + ex.getMessage());
        }
    }
    
    /**
     * <p>Paints an annotation in the buffer image, if the buffer is not
     * null. The panel should be repainted with super.repaint() to show the
     * changed image.</p>
     *
     * @param start the start time of the annotation
     * @param end the end time of the annotation
     * @param emotion the emotion point
     */
    private void paintAnnotation(double start, double end, Point2D emotion) {
        if (buffer == null)
            return;
        double width = buffer.getWidth();
        int left = (int)Math.floor(((width-1.0)*start/maxTime));
        int right = (int)Math.ceil(((width-1.0)*end/maxTime));
        FeeltraceAnnotationLayer layer = parent.getFeeltraceLayer();
        FeeltraceColourMap colours = layer.getColourMap();
        Color colour = colours.getEmotionColour(emotion.getX(),emotion.getY());
        Graphics g = buffer.getGraphics();
        g.setColor(colour);
        g.fillRect(left,0,right-left,buffer.getHeight());
    }
    
    /**
     * <p>Paints a black line at the current time. This should only be called
     * if currentTime and maxTime have been set.</p>
     */
    private void paintTimeMarker(Graphics g) {
        int width = getWidth()-2;
        int pos = (int)((double)(width-1)*currentTime/maxTime);
        Rectangle rect = new Rectangle(pos,1,0,getHeight()-3);
        g.setColor(Color.BLACK);
        g.drawRect(rect.x,rect.y,rect.width,rect.height);
    }

    /**
     * <p>Paints a window (black rectangle) around the current time span. This
     * should only be called if currentSpanStart, currentSpanEnd and maxTime
     * have been set.</p>
     */
    private void paintTimeSpanWindow(Graphics g) {
        int width = getWidth()-2;
        int start = (int)Math.floor(((double)(width-1)*currentSpanStart/maxTime));
        int end = (int)Math.ceil(((double)(width-1)*currentSpanEnd/maxTime));
        Rectangle rect = new Rectangle(start,1,end-start,getHeight()-3);
        g.setColor(Color.BLACK);
        g.drawRect(rect.x,rect.y,rect.width,rect.height);
    }

    /**
     * <p>Updates this timeline by searching the corpus.</p>
     */
    public void repaint() {
        refreshBuffer();
        super.repaint();
    }

    /**
     * <p>Called when (part of) the timeline needs to be painted. Paints the
     * buffered image (if not null) and the time marker or time span window
     * (if possible).</p>
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle rect = new Rectangle(0,0,getWidth(),getHeight());
        if (buffer != null)
            g.drawImage(buffer,1,1,this);
        g.setColor(Color.BLACK);
        rect.width--;
        rect.height--;
        g.drawRect(rect.x,rect.y,rect.width,rect.height);
        if (!Double.isNaN(maxTime)) {
            if (!Double.isNaN(currentTime))
                paintTimeMarker(g);
            else if (!Double.isNaN(currentSpanStart))
                paintTimeSpanWindow(g);
        }
    }

    /**
     * <p>Adds an annotation and updates the timeline.</p>
     *
     * @param start the start time of the annotation
     * @param end the end time of the annotation
     * @param emotion the emotion point
     */
    public void showAnnotation(double start, double end, Point2D emotion) {
        paintAnnotation(start,end,emotion);
        super.repaint();
    }

    //////////////////////////////////////////////////////////////////////////
    // TimeHandler interface
    
    public void acceptTimeChange(double systemTime) {
        currentTime = systemTime;
        currentSpanStart = Double.NaN;
        currentSpanEnd = Double.NaN;
        super.repaint();
    }

    public void setTime(double time) {
        clock.setSystemTime(time);
    }

    public void acceptTimeSpanChange(double start, double end) {
        currentTime = Double.NaN;
        currentSpanStart = start;
        currentSpanEnd = end;
        super.repaint();
    }
    
    public void setTimeSpan(double start, double end) {
        clock.setTimeSpan(start,end);
    }

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
        clock.registerTimeHandler(this);
    }

    public void setTimeHighlightColor(Color color) {
    }

    public double getMaxTime() {
        if (Double.isNaN(maxTime))
            return 0.0;
        else
            return maxTime;
    }
}
