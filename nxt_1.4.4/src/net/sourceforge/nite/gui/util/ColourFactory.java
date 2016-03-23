package net.sourceforge.nite.gui.util;

import java.awt.Color;

/**
 * <p>A colour factory is a singleton object that can create text colours and
 * background colours for a certain hue. The text colours will be legible on a
 * white background. The background colours will have a good contrast with
 * black text.</p>
 */
public class ColourFactory {
    
    private static final double RED_LEFT = 0.0;
    private static final double YELLOW = 1.0/6.0;
    private static final double GREEN = 1.0/3.0;
    private static final double CYAN = 0.5;
    private static final double BLUE = 2.0/3.0;
    private static final double MAGENTA = 5.0/6.0;
    private static final double RED_RIGHT = 1.0;
    
    private static final double RED_BRIGHTNESS = 1.0;
    private static final double YELLOW_BRIGHTNESS = 0.625;
    private static final double GREEN_BRIGHTNESS = 0.75;
    private static final double CYAN_BRIGHTNESS = 0.625;
    private static final double BLUE_BRIGHTNESS = 1.0;
    private static final double MAGENTA_BRIGHTNESS = 0.75;
    
    private ComposedSlopeFunction colourToBrightness;
    
    private static ColourFactory instance = null;

    /**
     * Used by getColourFactory()
     */
    private ColourFactory() {
        double[] colours = new double[7];
        double[] brightness = new double[7];
        colours[0] = RED_LEFT;
        colours[1] = YELLOW;
        colours[2] = GREEN;
        colours[3] = CYAN;
        colours[4] = BLUE;
        colours[5] = MAGENTA;
        colours[6] = RED_RIGHT;
        
        brightness[0] = RED_BRIGHTNESS;
        brightness[1] = YELLOW_BRIGHTNESS;
        brightness[2] = GREEN_BRIGHTNESS;
        brightness[3] = CYAN_BRIGHTNESS;
        brightness[4] = BLUE_BRIGHTNESS;
        brightness[5] = MAGENTA_BRIGHTNESS;
        brightness[6] = RED_BRIGHTNESS;
        
        colourToBrightness = new ComposedSlopeFunction(colours,brightness);
    }
    
    /**
     * <p>Returns the singleton colour factory.</p>
     *
     * @return the singleton colour factory
     */
    public static ColourFactory getInstance() {
        if (instance == null)
            instance = new ColourFactory();
        return instance;
    }

    /**
     * <p>Returns a text colour with the specified hue.</p>
     *
     * @param hue the hue (between 0.0 and 1.0)
     * @return the text colour
     */
    public Color getTextColour(double hue) {
        float sat = 1.0f;
        float bright = (float)colourToBrightness.perform(hue);
        return Color.getHSBColor((float)hue,sat,bright);
    }

    /**
     * <p>Returns a background colour with the specified hue.</p>
     *
     * @param hue the hue (between 0.0 and 1.0)
     * @return the background colour
     */
    public Color getBackgroundColor(double hue) {
        float sat = 1.5f - (float)colourToBrightness.perform(hue);
        float bright = 1.0f;
        return Color.getHSBColor((float)hue,sat,bright);
    }

    /**
     * <p>This function is composed of 1 or more joining slope functions.
     * The constructor takes two equal-length arrays of double values. The
     * combination of two values at the same index in the two arrays, correspond
     * to a point where two slope functions will join. The first and last points
     * define the start and end of the function. Therefore the arrays should
     * define at least two points. The function is only defined between the
     * first and the last X.</p>
     */
    private class ComposedSlopeFunction {
        double[] points;
        SlopeFunction[] functions;

        /**
         * <p>Constructs a new composed slope function for the specified
         * coordinates.</p>
         */
        public ComposedSlopeFunction(double[] xs, double[] ys) {
            points = new double[xs.length-1];
            functions = new SlopeFunction[xs.length-1];
            for (int i = 0; i < xs.length-1; i++) {
                double x1 = xs[i];
                double x2 = xs[i+1];
                double y1 = ys[i];
                double y2 = ys[i+1];
                points[i] = x2;
                functions[i] = new SlopeFunction(x1,x2,y1,y2);
            }
        }
        
        /**
         * <p>Returns the function value at the specified X value.</p>
         */
        public double perform(double x) {
            SlopeFunction f = null;
            for (int i = 0; (f == null) && (i < points.length-1); i++) {
                if (x <= points[i])
                    f = functions[i];
            }
            if (f == null)
                f = functions[functions.length-1];
            double result = f.perform(x);
            return result;
        }
    }
    
    /**
     * <p>This class defines the following mathematical function:</p>
     *
     * <p>f(x) = (y2-y1)/2 * sin( PI*(x-x1)/(x2-x1) - PI/2 ) + y1/2 + y2/2</p>
     *
     * <p>The function uses four parameters: x1, x2, y1 and y2. It takes the
     * sine fragment between -PI/2 and PI/2 (running from -1 to 1) and scales
     * and translates it so that the X values -PI/2 and PI/2 are mapped to
     * x1 and x2, and the Y values -1 and 1 are mapped to y1 and y2.</p>
     *
     * <p>In effect the function defines a slope from y1 to y2 between the
     * X values x1 and x2, starting and ending with a gradient of 0.</p>
     */
    private class SlopeFunction {
        private double factorF;
        private double transF;
        private double factorX;
        private double transX;
        
        private double x1;
        private double x2;
        private double y1;
        private double y2;
        
        /**
         * <p>Constructs a new slope function for the parameters x1, x2, y1
         * and y2.</p>
         */
        public SlopeFunction(double x1, double x2, double y1, double y2) {
            factorF = (y2-y1)/2.0;
            transF = y1/2.0 + y2/2.0;
            factorX = Math.PI/(x2-x1);
            transX = -x1*Math.PI/(x2-x1)-Math.PI/2.0;
            
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }
        
        /**
         * <p>Returns the function value at the specified X value, which should
         * be between x1 and x2 (inclusive).</p>
         */
        public double perform(double x) {
            double result = factorF * Math.sin(factorX*x + transX) + transF;
            return result;
        }
    }
}
