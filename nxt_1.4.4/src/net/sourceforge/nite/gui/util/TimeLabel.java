/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, 
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

import net.sourceforge.nite.time.*;
import javax.swing.JTextField;
import java.text.DecimalFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A JTextField that displays the time for a given NXT Clock.
 * The user can enter a time and if the user input can be parsed
 * as a time, the time will be changed in the NXT Clock.
 * 
 * @author Dennis Reidsma, Dennis Hofs, UTwente
 */
public class TimeLabel extends JTextField implements TimeHandler {

    Clock clock;

    public TimeLabel(Clock c) {
        super(5);
        setText("0:00:00");
        setHorizontalAlignment(JTextField.RIGHT);
        setClock(c);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeEntered();
            }
        });
    }

    /**
     * Takes a number of seconds and returns a string in the format of h:mm:ss
     */
    private String formatTime(double seconds) {
        int s = (int)(seconds%60);
        int m = (int)((seconds/60)%60);
        int h = (int)(seconds/3600);
        DecimalFormat nnFormat = new DecimalFormat("00");
        return h + ":" + nnFormat.format(m) + ":" + nnFormat.format(s);
    }    

    public void acceptTimeChange(double systemTime) {
	//System.out.println("Set time: " + systemTime);
	setText(formatTime(systemTime));
    }
        
    public void acceptTimeSpanChange(double start, double end) {
        setText(formatTime(start) + " -- " + formatTime(end));
    }
        
    public Clock getClock() {
        return clock;
    }
        
    public void setClock(Clock clock)  {
        this.clock = clock;
        clock.registerTimeHandler(this);
    }
        
    public void setTime(double time) {
        clock.setSystemTime(time);
    }
        
    public void setTimeHighlightColor(java.awt.Color color) {
    }
        
    public void setTimeSpan(double start, double end) {
        clock.setTimeSpan(start, end);
    }
    
    /**
     * Called when the user presses Enter in the text field. This method
     * will try to parse the current text value. If it is in the format
     * h:m:s or m:s, the system time will be changed. m and s must be between
     * 0 and 59. h must be 0 or greater. The text field will be updated to
     * show the current system time in a good format.
     */
    private void timeEntered() {
        String time = getText();
        String[] hms = time.split(":");
        String hs = "0";
        String ms = "0";
        String ss = "0";
        boolean parseError = false;
        if (hms.length == 3) {
            hs = hms[0];
            ms = hms[1];
            ss = hms[2];
        } else if (hms.length == 2) {
            ms = hms[0];
            ss = hms[1];
        } else {
            parseError = true;
        }
        int h = 0;
        int m = 0;
        int s = 0;
        if (!parseError) {
            try {
                h = Integer.parseInt(hs);
                m = Integer.parseInt(ms);
                s = Integer.parseInt(ss);
            } catch(NumberFormatException ex) {
                parseError = true;
            }
        }
        if ((h < 0) || (m < 0) || (m > 59) || (s < 0) || (s > 59))
            parseError = true;
        if (!parseError)
            setTime(h*3600+m*60+s);
        acceptTimeChange(clock.getSystemTime());
    }

    /** get the latest time handled by this TimeHandler - this returns
     * 0.0 because we're only a label! */
    public double getMaxTime() {
	return 0.0;
    }
}
