package net.sourceforge.nite.gui.util;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.awt.Color;

/** 
 * A trick:
 * 
 * If you have a number of values in a certain dimension, and you want to keep them apart by colouring
 * them differently, you can use a map that gives a distinguishable colour for each known value.
 * If you don't know the values beforehand, it would be nicer and more efficient if the mapping starts out 
 * empty, and assigns colours to certain values only when you actually request the colour.
 *
 * <p>A value colour map maps objects to colours. For each colour there is a
 * darker foreground variant and a lighter background variant. When you call
 * {@link #getValueColour(java.lang.Object) getValueColour()},
 * {@link #getValueTextColour(java.lang.Object) getValueTextColour()}, or
 * {@link #getValueBackColour(java.lang.Object) getValueBackColour}), the colour
 * map will check whether it already contains an entry for the specified object.
 * If so, the object's colour is returned. Otherwise a new colour is created and
 * the object-colour pair is put in the map.</p>
 *
 * <p>You can choose to use a global colour map for the entire application
 * (see {@link #getGlobalColourMap() getGlobalColourMap()}) or a local colour
 * map (see {@link #getLocalColourMap(boolean) getLocalColourMap()}).</p>
 *
 * <p>When you create a local colour map, you can specify whether the colour
 * map should cycle through a finite set of colours or it should create a new
 * colour for every new object.</p>
 *
 * <p>If you restart your application and you want to have the same colours for
 * the same objects, make sure that you request the colours in the same order
 * every time and let the map cycle through colours.</p>
 *
 * @author Dennis Reidsma, UTwente
 */
public class ValueColourMap {
	
    private static ValueColourMap globalMap = null;
    
    /**
     * <p>Returns the global colour map. If the global colour map has not been
     * created yet, this method will create it. The global colour map will
     * NOT cycle through colours, but create a new colour for every new
     * object.</p>
     *
     * @return the global colour map
     */
    public static ValueColourMap getGlobalColourMap() {
        if (globalMap == null)
            globalMap = new ValueColourMap(false);
        return globalMap;
    }
    
    /**
     * <p>Creates a new local colour map and returns it. If <code>cycle</code>
     * is true, the colour map will cycle through a finite set of colours.
     * Otherwise it will create a new colour for every new object.</p>
     *
     * @param cycle true if the colour map should cycle through colours, false
     * otherwise
     * @return a new local colour map
     */
    public static ValueColourMap getLocalColourMap(boolean cycle) {
        return new ValueColourMap(cycle);
    }
    
    /**
     * <p>Retrieves the foreground colour for the specified object from the
     * global colour map. If the global colour map has not been created yet,
     * this method will create it. If the map does not contain an entry for
     * the specified object, the object will be mapped to a colour and the
     * object-colour pair is put in the global colour map.</p>
     *
     * @param value an object
     * @return the foreground colour for the specified object
     */
    public static Color getColour(Object value) {
        return getGlobalColourMap().getValueColour(value);
    }

    private Map textColourMap = new HashMap();
    private Map backColourMap = new HashMap();
    private boolean cycle = false;
    
    /**
     * <p>Constructs a new colour map. If <code>cycle</code> is true, the colour
     * map will cycle through a finite set of colours. Otherwise it will create
     * a new colour for every new object.</p>
     *
     * @param cycle true if the colour map should cycle through colours, false
     * otherwise
     */
    private ValueColourMap(boolean cycle) {
        this.cycle = cycle;
    }

    /**
     * <p>Returns the foreground colour of the specified object. Same as
     * {@link #getValueTextColour(java.lang.Object) getValueTextColour()}.</p>
     */
    public Color getValueColour(Object value) {
        Color result = (Color)textColourMap.get(value);
        if (result == null)
            result = mapNewColour(value);
        return result;
    }
    
    /**
     * <p>Retrieves the foreground colour for the specified object from this
     * colour map. If the map does not contain an entry for the specified
     * object, the object will be mapped to a colour and the object-colour pair
     * is put in the colour map.</p>
     *
     * @param value an object
     * @return the foreground colour for the specified object
     */
    public Color getValueTextColour(Object value) {
        return getValueColour(value);
    }
    
    /**
     * <p>Retrieves the background colour for the specified object from this
     * colour map. If the map does not contain an entry for the specified
     * object, the object will be mapped to a colour and the object-colour pair
     * is put in the colour map.</p>
     *
     * @param value an object
     * @return the background colour for the specified object
     */
    public Color getValueBackColour(Object value) {
        Color result = (Color)backColourMap.get(value);
        if (result == null) {
            mapNewColour(value);
            result = (Color)backColourMap.get(value);
        }
        return result;
    }
    
    /**
     * <p>Creates a colour for the specified object and puts the new
     * object-colour pair in the colour map.</p>
     */
    private Color mapNewColour(Object value) {
        Color textColour = Color.black;
        Color backColour = Color.gray;
        double hue = -1.0;
        int index = textColourMap.size();
        if (cycle) index = index % 14;
        if (index < 3) {
            hue = index/3.0;
        } else if (index < 6) {
            double subindex = index-3.0;
            hue = 1.0/6.0 + subindex/3.0;
        } else if (index == 6)  {
            textColour = Color.black;
            backColour = Color.gray;
        } else if (index < 10) {
            double subindex = index-7;
            hue = 1.0/12.0 + subindex/3.0;
        } else if (index < 13) {
            double subindex = index-10;
            hue = 3.0/12.0 + subindex/3.0;
        } else if (index == 13) {
            textColour = Color.gray;
            backColour = Color.lightGray;
        } else {
            int i = value.hashCode();
            //DR: something very odd going on... %255 operation leads to negative values....
            //-------    the modulo operator in Java may sometimes return a negative value
            //-------    -255 < x%255 < +255
            //I fixed the calculations to factor out that negative value... But it would be better (=faster) if 'i' were always positive
            //System.out.println(((255-(i*13))%255+255)%255);
            //System.out.println("hash: " + i);
            //System.out.println((i*23)%255);
            //System.out.println((i*17)%255);
            textColour = new Color(((i*17)%255+255)%255,((255-(i*13))%255+255)%255,((i*23)%255+255)%255);
            backColour = textColour.brighter();
        }
        if (hue != -1.0) {
            textColour = ColourFactory.getInstance().getTextColour(hue);
            backColour = ColourFactory.getInstance().getBackgroundColor(hue);
        }
        textColourMap.put(value,myDarker(textColour));
        backColourMap.put(value,myDarker(backColour));
        //textColourMap.put(value,textColour);
        //backColourMap.put(value,backColour);
        return textColour;
    }

    /**
     * For debug purposes only. DR 2005.06.07
     * <p>Shows all keys in the global map
     */
    public void dumpKeys() {
        System.out.println(textColourMap);
        System.out.println(textColourMap.keySet());
    }
    
    public Set getKeys() {
        return textColourMap.keySet();
    }

    private Color myDarker(Color originalcol) { 
	return new Color (darken(originalcol.getRed()), darken(originalcol.getGreen()),
			  darken(originalcol.getBlue()));
    }

    private int darken(int orig) {
	return new Double(orig*0.8).intValue();
    }
}
