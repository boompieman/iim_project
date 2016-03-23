package net.sourceforge.nite.tools.videolabeler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;

import net.sourceforge.nite.nom.NOMException;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteAttribute;
import net.sourceforge.nite.nom.nomwrite.impl.NOMWriteElement;
import net.sourceforge.nite.time.Clock;
import net.sourceforge.nite.time.TimeHandler;

/**
 * <p>A Feeltrace circle is part of a Feeltrace target control panel (set at
 * construction). It displays a circle with an X axis and a Y axis through its
 * origin. Emotions consisting of evaluation and activation can be displayed
 * in the circle. The X axis represents evaluation values between -1.0 and +1.0.
 * The Y axis represents activation values between -1.0 and +1.0.</p>
 *
 * <p>In the circle labels for some predefined emotions will be displayed if
 * the method {@link FeeltraceTargetControlPanel#showLabels() showLabels()} of
 * the parent panel returns true.</p>
 *
 * <p>Every location within the circle is mapped to a colour using the
 * Feeltrace colour map returned by the method
 * {@link FeeltraceAnnotationLayer#getColourMap() getColourMap()} of the
 * Feeltrace annotation layer. The circle itself is white. Emotions will be
 * displayed as a small circle in the emotion colour and a black border for
 * contrast. The cursor will also have this appearance when the user moves the
 * cursor in the circle. If the user is annotating, the cursor will be closed
 * (filled with the emotion colour). Otherwise it will be open (have a thick
 * border in the emotion colour).</p>
 *
 * <p>If the method {@link FeeltraceTargetControlPanel#clickAnnotation()
 * clickAnnotation()} of the parent panel returns true, the user can start
 * annotating by clicking somewhere in the circle. Annotating ends when the user
 * clicks again. If <code>clickAnnotation</code> returns false, the user should
 * keep the mouse button pressed while annotating.</p>
 *
 * <p>By default existing annotations will be displayed in the circle. This
 * class is a <code>TimeHandler</code> and it will query the corpus at every
 * time event to display the annotations at the current time or in the current
 * time span. This may be turned off with {@link #setReplay(boolean)
 * setReplay()}.</p>
 *
 * <p>The panel should have enough space for a circle with diameter 200 plus
 * the axis labels. If the panel is not big enough, it will display the text
 * "panel too small".</p>
 */
public class FeeltraceCircle extends JPanel implements TimeHandler {
    
    private static HashMap labels = new HashMap();
    private Rectangle circle = new Rectangle(); // the bounding rectangle of the
            // Feeltrace circle, set in paintComponent;
            // width and height are 0 if the circle is not displayed
    private Point2D currentEmotion = null;
    private Point2D newEmotion = null;
    private boolean annotating = false;
    private FeeltraceTargetControlPanel parent;
    private Clock clock;
    private double currentTime = Double.NaN;
    private double currentSpanStart = Double.NaN;
    private double currentSpanEnd = Double.NaN;
    private boolean replay = true;
    
    private static final int SPACE = 5;
    private static final int AXIS_FONT_SIZE = 14;
    private static final int LABEL_FONT_SIZE = 12;
    private static final int MIN_DIAMETER = 200;

    /**
     * <p>Constructs a new Feeltrace circle to be part of the specified
     * target control panel.</p>
     *
     * @param parent the Feeltrace target control panel that will contain this
     * circle
     */
    public FeeltraceCircle(FeeltraceTargetControlPanel parent) {
        super();
        this.parent = parent;
        setClock(Document.getInstance().getClock());
        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                handleMouseMoved(e);
            }
            
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }
            
            public void mouseClicked(MouseEvent e) {
                handleMouseClicked(e);
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                handleComponentShown(e);
            }
            
            public void componentHidden(ComponentEvent e) {
                handleComponentHidden(e);
            }
        });
    }

    /**
     * <p>Enables or disables the visualisation of existing annotations. As
     * a corpus query is performed at every time event, replay might seriously
     * degrade performance.</p>
     *
     * @param replay true if replay should be enabled, false if it should be
     * disabled
     */
    public void setReplay(boolean replay) {
        this.replay = replay;
    }

    /**
     * <p>Called when the circle is shown. Registers this circle as a
     * <code>TimeHandler</code> with the clock.</p>
     */
    private void handleComponentShown(ComponentEvent e) {
        clock.registerTimeHandler(this);
    }

    /**
     * <p>Called when the circle is hidden. Deregisters this circle as a
     * <code>TimeHandler</code> with the clock.</p>
     */
    private void handleComponentHidden(ComponentEvent e) {
        clock.deregisterTimeHandler(this);
    }

    /**
     * <p>Called when (part of) the circle needs to be repainted. Paints the
     * circle, labels (if they should be shown) and annotations at the current
     * time or in the current time span (if replay is enabled).</p>
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle rect = new Rectangle(0,0,getWidth(),getHeight());
        circle = (Rectangle)rect.clone();
        Font axisFont = new Font("Dialog",Font.BOLD,AXIS_FONT_SIZE);
        g.setFont(axisFont);
        FontMetrics metrics = g.getFontMetrics();
        int textHeight = metrics.getHeight();
        circle.grow(-2*SPACE,-2*SPACE-textHeight);
        Rectangle textRect = measureLines(g,"VERY\nNEGATIVE");
        circle.x += textRect.width;
        circle.width -= textRect.width;
        textRect = measureLines(g,"VERY\nPOSITIVE");
        circle.width -= textRect.width;
        if (circle.width < MIN_DIAMETER || circle.height < MIN_DIAMETER) {
            textRect = measureString(g,"panel too small");
            int x = (rect.width-textRect.width)/2;
            int y = (rect.height-textRect.height)/2 + textRect.height;
            g.drawString("panel too small",x,y);
            circle = new Rectangle();
            return;
        }
        if (circle.width < circle.height) {
            int diff = circle.height - circle.width;
            circle.y += diff/2;
            circle.height = circle.width;
        } else if (circle.width > circle.height) {
            int diff = circle.width - circle.height;
            circle.x += diff/2;
            circle.width = circle.height;
        }
        Point org = new Point(circle.x + circle.width/2,circle.y + circle.height/2);
        paintCircle(g,circle);
        textRect = measureLines(g,"VERY\nNEGATIVE");
        textRect.translate(circle.x-textRect.width-SPACE,org.y-textRect.height/2);
        paintLabel(g,"VERY\nNEGATIVE",textRect,PL_ALIGN_RIGHT);
        textRect = measureLines(g,"VERY\nPOSITIVE");
        textRect.translate(circle.x+circle.width+SPACE,org.y-textRect.height/2);
        paintLabel(g,"VERY\nPOSITIVE",textRect,PL_ALIGN_LEFT);
        textRect = measureLines(g,"VERY ACTIVE");
        textRect.translate(org.x-textRect.width/2,circle.y-SPACE-textRect.height);
        paintLabel(g,"VERY ACTIVE",textRect,PL_ALIGN_CENTRE);
        textRect = measureLines(g,"VERY PASSIVE");
        textRect.translate(org.x-textRect.width/2,circle.y+circle.height+SPACE);
        paintLabel(g,"VERY PASSIVE",textRect,PL_ALIGN_CENTRE);
        if (parent.showLabels())
            paintLabels(g);
        if (replay) {
            if (!Double.isNaN(currentTime))
                paintAnnotations(g,currentTime,currentTime);
            else if (!Double.isNaN(currentSpanStart))
                paintAnnotations(g,currentSpanStart,currentSpanEnd);
        }
    }
    
    /**
     * <p>Returns the bounding rectangle for the specified string if it is
     * drawn in the specified graphics object (in the current font of that
     * object). The height will always equal the font height. The top left
     * location of the returned rectangle will always be (0,0).</p>
     */
    private Rectangle measureString(Graphics g, String s) {
        FontMetrics metrics = g.getFontMetrics();
        Rectangle2D bounds = metrics.getStringBounds(s,g);
        return new Rectangle(0,0,(int)bounds.getWidth(),metrics.getHeight());
    }

    /**
     * <p>Returns the bounding rectangle for the specified string if it is
     * drawn in the specified graphics object (in the current font of that
     * object). The string may consist of several lines, separated by \r and/or
     * \n characters. The height of each line will always equal the font
     * height. The width will be the width of the longest line. The top left
     * location of the returned rectangle will always be (0,0).</p>
     */
    private Rectangle measureLines(Graphics g, String s) {
        String[] lines = s.split("[\\r\\n]+");
        int width = 0;
        int height = 0;
        for (int i = 0; i < lines.length; i++) {
            Rectangle rect = measureString(g,lines[i]);
            height += rect.height;
            if (rect.width > width)
                width = rect.width;
        }
        return new Rectangle(0,0,width,height);
    }

    /**
     * <p>Paints the Feeltrace circle without labels in the specified
     * rectangle (whose width and height should be equal).</p>
     */
    private void paintCircle(Graphics g, Rectangle circle) {
        g.setColor(Color.WHITE);
        g.fillOval(circle.x,circle.y,circle.width,circle.height);
        g.setColor(Color.BLACK);
        g.drawOval(circle.x,circle.y,circle.width,circle.height);
        int centreX = circle.x + circle.width/2;
        int centreY = circle.y + circle.height/2;
        g.drawLine(centreX,circle.y,centreX,circle.y+circle.height);
        g.drawLine(circle.x,centreY,circle.x+circle.width,centreY);
    }
    
    private static final int PL_ALIGN_LEFT = 0;
    private static final int PL_ALIGN_CENTRE = 1;
    private static final int PL_ALIGN_RIGHT = 2;

    /**
     * <p>Paints a label in the specified rectangle. The align parameter should
     * be one of the constants PL_ALIGN_LEFT, PL_ALIGN_CENTRE or
     * PL_ALIGN_RIGHT. The label may consist of several lines, but the text
     * should fit in the specified rectangle (see also measureLines()).</p>
     */
    private void paintLabel(Graphics g, String s, Rectangle rect, int align) {
        String[] lines = s.split("[\\r\\n]+");
        int y = rect.y;
        for (int i = 0; i < lines.length; i++) {
            Rectangle lineRect = measureString(g,lines[i]);
            int x = rect.x;
            if (align == PL_ALIGN_CENTRE)
                x += (rect.width-lineRect.width)/2;
            else if (align == PL_ALIGN_RIGHT)
                x += rect.width-lineRect.width;
            y += lineRect.height;
            g.drawString(lines[i],x,y);
        }
    }
    
    /**
     * <p>Paints the labels for the predefined emotions in the Feeltrace circle.
     * The private variable circle should have been set to the bounding
     * rectangle of the Feeltrace circle before you call this method.</p>
     */
    private void paintLabels(Graphics g) {
        Font labelFont = new Font("Dialog",0,LABEL_FONT_SIZE);
        g.setFont(labelFont);
        Iterator it = labels.keySet().iterator();
        while (it.hasNext()) {
            String label = (String)it.next();
            Point2D pt = (Point2D)labels.get(label);
            paintLabel(g,label,logicalToPhysical(pt));
        }
    }

    /**
     * <p>Paints a label so that its centre is at the specified point.</p>
     */
    private void paintLabel(Graphics g, String s, Point p) {
        Rectangle rect = measureString(g,s);
        int x = p.x-rect.width/2;
        int y = p.y+rect.height/2;
        g.drawString(s,x,y);
    }

    /**
     * <p>Displays the annotations between the specified start and end time.
     * The private variable circle should have been set to the bounding
     * rectangle of the Feeltrace circle before you call this method.</p>
     */
    private void paintAnnotations(Graphics g, double start, double end) {
        Document doc = Document.getInstance();
        String query = "($a " + parent.getLayer().getCodeElement().getName() +
                "): start($a) <= \"" + start + "\" && end($a) > \"" + end + "\"";
        if (parent.getAgent() != null)
            query += " && $a@" + doc.getMetaData().getAgentAttributeName() +
                    " == \"" + parent.getAgent().getShortName() + "\"";
        try {
            List elems = doc.searchAnnotations(query);
            Iterator it = elems.iterator();
            while (it.hasNext()) {
                NOMElement elem = (NOMElement)it.next();
                paintAnnotation(g,elem);
            }
        } catch (Throwable ex) {
            System.out.println("ERROR: Could not search the corpus: " + ex.getMessage());
        }
    }

    /**
     * <p>Displays the specified annotation in the Feeltrace circle. The private
     * variable circle should have been set to the bounding rectangle of the
     * Feeltrace circle before you call this method.</p>
     */
    private void paintAnnotation(Graphics g, NOMElement elem) {
        FeeltraceAnnotationLayer layer = parent.getFeeltraceLayer();
        double eval = layer.getEvaluation(elem);
        double activ = layer.getActivation(elem);
        Point pt = logicalToPhysical(new Point2D.Double(eval,activ));
        int diam = circle.width/20;
        FeeltraceColourMap colourMap = parent.getFeeltraceLayer().getColourMap();
        Color colour = colourMap.getEmotionColour(eval,activ);
        g.setColor(colour);
        g.fillOval(pt.x-diam/2,pt.y-diam/2,diam,diam);
        g.setColor(Color.BLACK);
        g.drawOval(pt.x-diam/2,pt.y-diam/2,diam,diam);
    }

    /**
     * <p>Converts a logical point (denoting an emotion, with X and Y values
     * between -1.0 and +1.0) to the physical point (coordinates in this panel)
     * where the logical point can be displayed in the Feeltrace circle.
     * The private variable should have been set. If it is set to a rectangle
     * with width and height 0, this method returns null.</p>
     */
    private Point logicalToPhysical(Point2D pt) {
        if (circle.width <= 0)
            return null;
        double diam = circle.width;
        double factorX = diam/2.0;
        double transX = circle.x + factorX;
        double factorY = -diam/2.0;
        double transY = circle.y - factorY;
        int x = (int)(pt.getX()*factorX + transX);
        int y = (int)(pt.getY()*factorY + transY);
        return new Point(x,y);
    }

    /**
     * <p>Converts a physical point (coordinates in this panel) in the Feeltrace
     * circle to a logical point (denoting an emotion, with X and Y values
     * between -1.0 and +1.0). The private variable should have been set. If it
     * is set to a rectangle with width and height 0, this method returns
     * null.</p>
     */
    private Point2D physicalToLogical(Point pt) {
        if (circle.width <= 0)
            return null;
        double diam = circle.width;
        double factorX = 2.0/diam;
        double transX = -1 - factorX*circle.x;
        double factorY = -2.0/diam;
        double transY = 1 - factorY*circle.y;
        double x = (double)pt.x*factorX + transX;
        double y = (double)pt.y*factorY + transY;
        return new Point2D.Double(x,y);
    }
    
    /**
     * <p>Sets the cursor for the specified logical point. If the point is
     * null or not in the circle, the cursor will be set to the default cursor.
     * Otherwise it will be set to a circle with the colour of the emotion
     * denoted by the specified point. The circle will be closed (filled) if
     * the user is currently annotating. Otherwise the circle will be open
     * (have a thick border).</p>
     */
    private void setCursor(Point2D pt) {
        if (circle.width <= 0) {
            setCursor(Cursor.getDefaultCursor());
            return;
        }
        if (pt == null || !isValid(pt)) {
            setCursor(Cursor.getDefaultCursor());
            return;
        }
        int prefDiam = circle.width/10;
        Dimension dim = Toolkit.getDefaultToolkit().getBestCursorSize(prefDiam,prefDiam);
        if (prefDiam >= dim.width)
            prefDiam = dim.width-1;
        Image img = new BufferedImage(dim.width,dim.height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)img.getGraphics();
        Stroke oldStroke = g.getStroke();
        int strokeWidth = prefDiam/4;
        Stroke stroke = new BasicStroke(strokeWidth);
        g.setStroke(stroke);
        FeeltraceColourMap colourMap = parent.getFeeltraceLayer().getColourMap();
        Color colour = colourMap.getEmotionColour(pt.getX(),pt.getY());
        g.setColor(colour);
        if (annotating)
            g.fillOval(0,0,prefDiam,prefDiam);
        else {
            int offset = strokeWidth/2;
            g.drawOval(offset,offset,prefDiam-2*offset,prefDiam-2*offset);
        }
        g.setStroke(oldStroke);
        g.setColor(Color.BLACK);
        g.drawOval(0,0,prefDiam,prefDiam);
        Cursor cursor = getToolkit().createCustomCursor(img,new Point(prefDiam/2,prefDiam/2),"feeltrace");
        setCursor(cursor);
    }
    
    /**
     * <p>Determines whether the specified logical point is in the circle
     * (the distance from the origin is less than 1). If the specified point
     * is null, this method returns false.</p>
     */
    private boolean isValid(Point2D logPt) {
        if (logPt == null)
            return false;
        double x = logPt.getX();
        double y = logPt.getY();
        double dist = x*x + y*y;
        return dist <= 1.0;
    }
    
    /**
     * <p>Called when the user moved the mouse in this panel. If the user is
     * annotating the and the mouse is in the circle, the emotion for the
     * current mouse position will be saved in the private variable
     * newEmotion. A new annotation may be started at the next time event.</p>
     */
    private void handleMouseMoved(MouseEvent e) {
        Point2D logPt = physicalToLogical(e.getPoint());
        setCursor(logPt);
        if (annotating && isValid(logPt)) {
            newEmotion = logPt;
        }
    }

    /**
     * <p>Sets the target of the current annotation. This method is called in
     * response to the targetSet event. The current emotion will be set in
     * the evaluation and activation attribute of the annotation.</p>
     *
     * @param annotation the current annotation
     * @return true if the target was set successfully, false otherwise
     */
    public boolean setTarget(NOMWriteElement annotation) {
        if (currentEmotion == null)
            return false;
        NOMWriteAttribute evalAttr = new NOMWriteAttribute(
                parent.getFeeltraceLayer().getEvaluationAttribute(),
                new Double(currentEmotion.getX()));
        NOMWriteAttribute activAttr = new NOMWriteAttribute(
                parent.getFeeltraceLayer().getActivationAttribute(),
                new Double(currentEmotion.getY()));
        try {
            annotation.addAttribute(evalAttr);
            annotation.addAttribute(activAttr);
        } catch (NOMException ex) {
            System.out.println("ERROR: Could not add attribute to annotation: " + ex.getMessage());
            return false;
        }
        return true;
    }
    
    /**
     * <p>Returns the emotion of the current annotation. If there is no current
     * annotation or the emotion has not been set, this method returns null.</p>
     *
     * @return the current emotion
     */
    public Point2D getCurrentEmotion() {
        return currentEmotion;
    }

    /**
     * <p>Called when the user presses the mouse. If clickAnnotation() is false,
     * this will start the annotation process.</p>
     */
    private void handleMousePressed(MouseEvent e) {
        if (parent.clickAnnotation())
            return;
        Point2D logPt = physicalToLogical(e.getPoint());
        if (logPt == null)
            return;
        currentEmotion = null;
        newEmotion = null;
        annotating = true;
        setCursor(logPt);
        if (isValid(logPt)) {
            currentEmotion = logPt;
            parent.processAnnotationStarted();
            parent.processAnnotationTargetSet();
        }
    }
    
    /**
     * <p>Called when the user presses the mouse. If clickAnnotation() is false,
     * this will end the annotation process.</p>
     */
    private void handleMouseReleased(MouseEvent e) {
        if (parent.clickAnnotation())
            return;
        Point2D logPt = physicalToLogical(e.getPoint());
        if (logPt == null)
            return;
        annotating = false;
        setCursor(logPt);
        currentEmotion = null;
        newEmotion = null;
        parent.processAnnotationEnded();
    }

    /**
     * <p>Called when the user clicks the mouse button. If clickAnnotation()
     * is true, this will start or end the annotation process.</p>
     */
    private void handleMouseClicked(MouseEvent e) {
        if (!parent.clickAnnotation())
            return;
        Point2D logPt = physicalToLogical(e.getPoint());
        if (logPt == null)
            return;
        currentEmotion = null;
        newEmotion = null;
        annotating = !annotating;
        setCursor(logPt);
        if (annotating && isValid(logPt)) {
            currentEmotion = logPt;
            parent.processAnnotationStarted();
            parent.processAnnotationTargetSet();
        }
        if (!annotating) {
            currentEmotion = null;
            parent.processAnnotationEnded();
        }
    }

    //////////////////////////////////////////////////////////////////////////
    // TimeHandler interface
    
    public void acceptTimeChange(double systemTime) {
        currentTime = systemTime;
        currentSpanStart = Double.NaN;
        currentSpanEnd = Double.NaN;
        if (annotating && newEmotion != null &&
                (currentEmotion == null || !currentEmotion.equals(newEmotion))) {
            parent.processAnnotationEnded();
            parent.processAnnotationStarted();
            currentEmotion = newEmotion;
            parent.processAnnotationTargetSet();
        }
        repaint();
    }

    public void setTime(double time) {
        clock.setSystemTime(time);
    }

    public void acceptTimeSpanChange(double start, double end) {
        currentTime = Double.NaN;
        currentSpanStart = start;
        currentSpanEnd = end;
        repaint();
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
        return 0.0;
    }

    static {
        labels.put("furious",new Point2D.Double(-0.606138107,0.872122762));
        labels.put("terrified",new Point2D.Double(-0.89769821,0.641943734));
        labels.put("disgusted",new Point2D.Double(-1.076726343,0.401534527));
        labels.put("angry",new Point2D.Double(-0.493606138,0.544757033));
        labels.put("afraid",new Point2D.Double(-0.595907928,0.242966752));
        labels.put("excited",new Point2D.Double(0.375959079,0.657289003));
        labels.put("interested",new Point2D.Double(0.391304348,0.498721228));
        labels.put("happy",new Point2D.Double(0.641943734,0.319693095));
        labels.put("pleased",new Point2D.Double(0.488491049,0.109974425));
        labels.put("exhilerated",new Point2D.Double(0.877237852,0.708439898));
        labels.put("delighted",new Point2D.Double(1.020460358,0.488491049));
        labels.put("blissful",new Point2D.Double(1.112531969,0.145780051));
        labels.put("sad",new Point2D.Double(-0.519181586,-0.3657289));
        labels.put("bored",new Point2D.Double(-0.319693095,-0.524296675));
        labels.put("despairing",new Point2D.Double(-1.030690537,-0.560102302));
        labels.put("depressed",new Point2D.Double(-0.641943734,-0.943734015));
        labels.put("content",new Point2D.Double(0.468030691,-0.335038363));
        labels.put("relaxed",new Point2D.Double(0.386189258,-0.391304348));
        labels.put("serene",new Point2D.Double(0.989769821,-0.560102302));
    }
}
