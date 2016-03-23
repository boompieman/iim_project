/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.awt.Color;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import net.sourceforge.nite.nstyle.NConstants;

/**
 * @author judyr
 *
 * This is used to process a document style specified for TextAreas. 
 * There is also functionality for setting up fonts and colours in the JComponentHandler. These styles are only for 
 * specifying text styles for a document. They must be named for use by text elements
 *
 * */
public class TextStyleHandler extends NDisplayObjectHandler {
	Style style= null;
	String name = null;

    private StyleContext stylecontext;
    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
            stylecontext = new StyleContext();
       style =  setUpStyle();

    }
    /**
     * Sets the font size for this style
     * */
    private Style getFontSize(Style sty, String fontsize) {
        if (fontsize != null) {
            try {
                StyleConstants.setFontSize(
                    sty,
                    java.lang.Integer.parseInt(fontsize));
            } catch (NumberFormatException e) {
                System.out.println("Invalid font size: " + fontsize + ".");
            }
        }
        return sty;
    }

/**
 * Sets the font family and whether it is plain, italic or bold
 * */
    private Style getFontStyle(Style sty, String fontstyle) {
        if (fontstyle != null) {
            if (fontstyle.equalsIgnoreCase(NConstants.bold)) {
                StyleConstants.setBold(sty, true);
            } else if (fontstyle.equalsIgnoreCase(NConstants.italic)) {
                StyleConstants.setItalic(sty, true);
            } else if (fontstyle.equalsIgnoreCase(NConstants.boldItalic)) {
                StyleConstants.setBold(sty, true);
                StyleConstants.setItalic(sty, true);
            }
        }
        return sty;
    }


    /**
     * Sets the foreground and background colours of the style
     * */
    public Style getColours(Style sty, String fcol, String bcol) {
	return setColours(sty,fcol,bcol);
    }

    /**
     * Sets the foreground and background colours of the style
     * */
    public Style setColours(Style sty, String fcol, String bcol) {
        if (fcol != null) {
            Color colour = NConstants.getColour(fcol);
            if (colour != null) {
                StyleConstants.setForeground(sty, colour);
            }
        }
        if (bcol != null) {
            Color colour = NConstants.getColour(bcol);
            if (colour != null) {
                StyleConstants.setBackground(sty, colour);
            }
        }
        return sty;
    }


    
	public void makeNewStyle(){
               style = stylecontext.addStyle(name, null); 
	        
	}
	/**
	 * Returns a style object corresponding the the font style specified in the properties componentToData
	 * */
    private Style setUpStyle() {

      if (properties != null){
        name = (String) properties.get(NConstants.Name);
      
        String fontStyleString = (String) properties.get(NConstants.fontStyle);
	String fontSizeString = (String) properties.get(NConstants.fontSize);
	String fColourString = (String) properties.get(NConstants.foregroundColour);
	String bColourString = (String) properties.get(NConstants.backgroundColour);
        Style sty = stylecontext.addStyle(name, null);
        sty = getFontStyle(sty, fontStyleString);
	sty = getFontSize(sty, fontSizeString);
	sty = getColours(sty, fColourString, bColourString);
        return sty;
      }return null;
    }

    
    
    /**
     * The font styles should never have children, so this method does nothing
     * */
     public void addChild(NDisplayObjectHandler child){
     }

    /**
     * Returns the name.
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the style.
     * @return Style
     */
    public Style getStyle() {
           
        return style;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the style.
     * @param style The style to set
     */
    public void setStyle(Style style) {
        this.style = style;
    }

}
