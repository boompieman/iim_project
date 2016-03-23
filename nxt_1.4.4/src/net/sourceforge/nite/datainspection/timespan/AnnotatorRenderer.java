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
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.util.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;
import java.util.*;
import net.sourceforge.nite.query .*;
/**
 * The annotator renderer is responsible for rendering onto buffered images information for one annotator,
 * such as location of boundaries, or segments, etc.
 */
class AnnotatorRenderer {
    /**
     * Create a buffered image, with given size and resolution, and draw the given boundarylist on it. 
     * The width of the resulting image is dependent on the highest timestamp.
     * The background of the image is transparent.
     * The boundarylist is supposed to be sorted on time.
     */
    public static BufferedImage renderBoundaryList(ArrayList boundaryList, int height, int milliSecsPerPixel) {
        int width = (int)(10 + (((Boundary)boundaryList.get(boundaryList.size()-1)).time*1000d)/(double)milliSecsPerPixel);
        
        BufferedImage result = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB );
        Graphics2D g = (Graphics2D)result.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        //do I need to draw transparent on background first?
        g.setBackground(new Color(1f,1f,1f,0f));
        g.clearRect(0,0,width,height);
        g.setColor(Color.blue);
        for (int i = 1; i < boundaryList.size(); i++) {
            Boundary b = (Boundary)boundaryList.get(i);
            double x = ((double)(b.time*1000))/(double)milliSecsPerPixel;
            Rectangle2D r = new Rectangle2D.Double(x,0,1,(double)height-1);
            g.setColor(Color.green);
            g.fill(r);
        }
        return result;
    }

    /**
     * Create a buffered image, with given size and resolution, and draw the given segmentlist on it. 
     * The width of the resulting image is dependent on the highest timestamp.
     * The background of the image is transparent.
     * The segmentlist is supposed to be sorted on time.
     */
    public static BufferedImage renderSegmentList(ArrayList segmentList, int height, int milliSecsPerPixel,NOMElementToTextDelegate segmentToText) {
        int width = (int)(10 + (((NOMWriteElement)segmentList.get(segmentList.size()-1)).getEndTime()*1000d)/(double)milliSecsPerPixel);
        if (width==0) return null;
        
        BufferedImage result = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB );
        Graphics2D g = (Graphics2D)result.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        //do I need to draw transparent on background first?
        g.setBackground(new Color(1f,1f,1f,0f));
        g.clearRect(0,0,width,height);
        g.setColor(Color.blue);
        for (int i = 1; i < segmentList.size(); i++) {
            NOMWriteElement nwe = (NOMWriteElement)segmentList.get(i);
            double x = (nwe.getStartTime()*1000d)/(double)milliSecsPerPixel;
            double w = ((nwe.getEndTime()-nwe.getStartTime())*1000d)/(double)milliSecsPerPixel;
            Rectangle2D r = new Rectangle2D.Double(x,0,w,(double)height-1);
            //determine color from label....
            String s = segmentToText.getTextForNOMElement(nwe);
            g.setColor(ValueColourMap.getColour(s));
            g.fill(r);
            g.setColor(Color.white);
            g.draw(r);
        }
        return result;
    }
        
}