package net.sourceforge.nite.tools.videolabeler;

import java.awt.Color;
import java.util.HashMap;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import net.sourceforge.nite.gui.util.ColourFactory;
import net.sourceforge.nite.nstyle.handler.TextStyleHandler;

/**
 * <p>A Feeltrace colour map maps emotions to colours. It contains various
 * methods to facilitate processes where emotion colours are involved.</p>
 *
 * <p>The hue of an emotion colour is determined by the clockwise angle between
 * the line through the origin and (-1,0) and the line through the origin and
 * the emotion point. The colours vary from red (angle 0) to yellow (angle
 * 0.5*PI) to green (angle PI) to blue (angle 1.5*PI) to red again (angle
 * 2*PI).</p>
 *
 * <p>The saturation of an emotion colour is determined by the distance of the
 * emotion point from the origin (saturation 0 for distance 0, saturation 1 for
 * distance 1).</p>
 */
public class FeeltraceColourMap {
    
    private HashMap styleMap;
    
    /**
     * <p>Constructs a new Feeltrace colour map.</p>
     */
    public FeeltraceColourMap() {
        styleMap = new HashMap();
        double maxAngle = 2.0*Math.PI;
        for (double angle = 0.0; angle < maxAngle; angle += 0.1) {
            String styleName = getStyleName(angle);
            double hue = getEmotionHue(angle);
            Color colour = ColourFactory.getInstance().getTextColour(hue);
            TextStyleHandler styleHandler = new TextStyleHandler();
            styleHandler.init("", null);
            styleHandler.setName(styleName);
            styleHandler.makeNewStyle();
            Style style = styleHandler.getStyle();
            StyleConstants.setForeground(style,colour);
            StyleConstants.setBackground(style,colour);
            styleMap.put(styleName,style);
        }
    }

    /**
     * <p>Returns a style map that can be added to a text area to display text
     * in the colour of an emotion. The
     * {@link net.sourceforge.nite.gui.util.ColourFactory ColourFactory} is used
     * to get text colours that contrast with a white background. Call
     * {@link #getStyleName(double, double) getStyleName()} to get the style
     * name for a certain emotion colour.</p>
     *
     * @return a style map
     */
    public HashMap getStyleMap() {
        return styleMap;
    }
    
    /**
     * <p>Returns the style name for an emotion with the specified angle.</p>
     */
    private String getStyleName(double angle) {
        if (Double.isNaN(angle))
            return "";
        long n = Math.round(angle*10.0);
        long max = (long)Math.floor(20.0*Math.PI);
        if (n < 0)
            n = 0;
        else if (n > max)
            n = max;
        return "angle" + n;
    }

    /**
     * <p>Returns the angle of the specified emotion.</p>
     */
    private double getAngle(double evaluation, double activation) {
        double x = evaluation;
        double y = activation;
        if (x == 0.0) {
            if (y == 0.0)
                return Double.NaN;
            else if (y < 0.0)
                return 1.5*Math.PI;
            else // y > 0.0
                return 0.5*Math.PI;
        } else if (x < 0.0) {
            if (y == 0.0)
                return 0.0;
            else if (y < 0.0)
                return 1.5*Math.PI + Math.atan(-x/-y);
            else // y > 0.0
                return Math.atan(y/-x);
        } else { // x > 0.0
            if (y == 0.0)
                return Math.PI;
            else if (y < 0.0)
                return Math.PI + Math.atan(-y/x);
            else // y > 0.0
                return 0.5*Math.PI + Math.atan(x/y);
        }
    }
    
    /**
     * <p>Returns the style name for the specified emotion. The returned
     * name is one of the names in the style map (see {@link #getStyleMap()
     * getStyleMap()}. If evaluation and activation are 0 (no angle can be
     * determined), this method returns an empty string.</p>
     *
     * @param evaluation the evaluation of the emotion
     * @param activation the activation of the emotion
     * @return a style name or an empty string
     */
    public String getStyleName(double evaluation, double activation) {
        double angle = getAngle(evaluation,activation);
        return getStyleName(angle);
    }
    
    /**
     * <p>Returns the hue for the specified angle.</p>
     */
    private double getEmotionHue(double angle) {
        if (Double.isNaN(angle))
            return angle;
        else if (angle >= 0.0 && angle <= 0.5*Math.PI)
            return angle/(0.5*Math.PI) * 1.0/6.0;
        else if (angle > 0.5*Math.PI && angle <= Math.PI)
            return 1.0/6.0 + (angle-0.5*Math.PI)/(0.5*Math.PI) * 1.0/6.0;
        else if (angle > Math.PI && angle <= 1.5*Math.PI)
            return 1.0/3.0 + (angle-Math.PI)/(0.5*Math.PI) * 1.0/3.0;
        else if (angle > 1.5*Math.PI && angle <= 2.0*Math.PI)
            return 2.0/3.0 + (angle-1.5*Math.PI)/(0.5*Math.PI) * 1.0/3.0;
        else
            return 0.0;
    }
    
    /**
     * <p>Returns the hue for the specified emotion. If evaluation and
     * activation are 0 (no angle can be determined), this method returns
     * <code>Double.NaN</code>.</p>
     *
     * @param evaluation the evaluation of the emotion
     * @param activation the activation of the emotion
     * @return the hue or <code>Double.NaN</code>
     */
    public double getEmotionHue(double evaluation, double activation) {
        double angle = getAngle(evaluation,activation);
        return getEmotionHue(angle);
    }

    /**
     * <p>Returns the colour for the specified emotion.</p>
     *
     * @param evaluation the evaluation of the emotion
     * @param activation the activation of the emotion
     * @return a colour
     */
    public Color getEmotionColour(double evaluation, double activation) {
        double hue = getEmotionHue(evaluation,activation);
        if (Double.isNaN(hue))
            return Color.WHITE;
        double x = evaluation;
        double y = activation;
        double sat = Math.sqrt(x*x + y*y);
        return Color.getHSBColor((float)hue,(float)sat,1.0f);
    }
}
