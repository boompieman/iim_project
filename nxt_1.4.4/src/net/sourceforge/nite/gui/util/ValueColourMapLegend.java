package net.sourceforge.nite.gui.util;

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
import java.util.ArrayList;
import net.sourceforge.nite.query.*;

/**
 * ValueColourMapLegend provides a JPanel with a legend for the colour/label combinations in a ValueColourMap.
 * It works best if the objects (values) in the ValueColourMap have sensible string representations, since 
 * values are represented by Strings in the ValueColourMapLegend.
 * 
 * If the contents of the map change, the legend should be refreshed (call refresh()...)
 * @author: Dennis Reidsma, UTwente
 */
public class ValueColourMapLegend extends JPanel {
    
    protected ArrayList values = new ArrayList(); // list of objects
    protected ArrayList colours = new ArrayList(); //arraylist of Color
    protected ValueColourMap vcm = null;
    public BufferedImage img;

    //is size info needed? or do we just set preferred size?
    
    public ValueColourMapLegend(ValueColourMap newVcm) {
        vcm = newVcm;
        refresh();
    }
    
    /**
     * recalculate legend, redisplay, reset preferred size of panel.
     */
    public void refresh() {
        if (vcm==null)return;
        values.clear();
        colours.clear();
        //get value texts from map, store locally
        Iterator valueIt = vcm.getKeys().iterator();
        while (valueIt.hasNext()) {
            values.add(valueIt.next());
        }
        //sort value texts alphabetically
        Collections.sort(values, new Comparator() {
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
            public boolean equals(Object obj) {
                return false;
            }
        });
        //get colours from map for each text, store locally in right order
        valueIt = values.iterator();
        while (valueIt.hasNext()) {
            colours.add(vcm.getValueColour(valueIt.next()));
        }
        //calculate new size of panel
        int height = 20 * values.size();
        setPreferredSize(new Dimension(100,height));
        //clear panel
        img = new BufferedImage(500,height, BufferedImage.TYPE_INT_ARGB );
        Graphics2D g = (Graphics2D)img.getGraphics();
        //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        //                   RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.white);
        g.fill3DRect(0,0,500,height,true);
        //iterate value texts, draw colour + label
        valueIt = values.iterator();
        int i = 0;
        while (valueIt.hasNext()) {
            String next = valueIt.next().toString();
            g.setColor(vcm.getValueColour(next));
            g.fill3DRect(0,i*20, 40, 18,true);
            g.drawString(next, 45, i*20+18);
            i++;
        }
        
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = ((Graphics2D)g);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
            
        g2.drawImage(img,null,0,0);
    }

}