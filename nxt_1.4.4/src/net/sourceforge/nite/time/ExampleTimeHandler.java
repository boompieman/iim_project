/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.time;

import javax.swing.JPanel;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.awt.event.*;
import java.awt.Color;

/**
 * An example implementation of a time handler.
 * 
 * @author jeanc, jonathan 
 */
public class ExampleTimeHandler extends JInternalFrame implements TimeHandler {

    private double time; 
    //    public DefaultClock clock; 
    public Clock clock; 
    private int id=0;
    
   
    // The following three methods implement PlayingTimeHandler
    public void acceptTimeChange(double systemTime) {
	//	System.out.println("Wee window: set system time");
	time = systemTime;
    }

    public void acceptTimeSpanChange(double start, double end){
	//do something here!
    }


    public void setTimeSpan(double s,double e){
	clock.setTimeSpan(s,e,this);
    }

    public void setTime(double time) {
	clock.setSystemTime(time, (TimeHandler)this);
	//	clock.setSystemTime(time);
    }

    public Clock getClock() {
	return clock;
    }

    public void setClock(Clock clock) {
	this.clock = clock;
    }

    private void init() {
	JPanel p = new JPanel();
	p.setLayout (new GridLayout(2,2,4,4));
	p.add(new JLabel("Set System Time:", JLabel.RIGHT));
	final JTextField timeField = new JTextField(5);
	timeField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    try {
			setTime(java.lang.Double.parseDouble(timeField.getText()));
		    } catch (NumberFormatException e){
			System.out.println("Invalid system time");
		    }
		}
	    });
	p.add(timeField);

	p.add(new JLabel("", JLabel.LEFT));
	p.add(new JLabel("", JLabel.LEFT));
	p.add(new JLabel("Span Start:", JLabel.RIGHT));
	final JTextField startField = new JTextField(5);
	p.add(startField);
	final JTextField endField = new JTextField(5);
	startField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    try {
			setTimeSpan(java.lang.Double.parseDouble(startField.getText()),
			     java.lang.Double.parseDouble(endField.getText()));
		    } catch (NumberFormatException e){
			System.out.println("Invalid span times");
		    }
		}
	    });
	p.add(new JLabel("Span End:", JLabel.RIGHT));
	endField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    try {
			setTimeSpan(java.lang.Double.parseDouble(startField.getText()),
			     java.lang.Double.parseDouble(endField.getText()));
		    } catch (NumberFormatException e){
			System.out.println("Invalid span times");
		    }
		}
	    });
	p.add(endField);

	
	clock.registerTimeHandler(this);
	getContentPane().add(p);
    }

    /** Constructor
     */
    public ExampleTimeHandler() {
	super("Example time handler", true, true, true, true);
	clock = (Clock)new DefaultClock();
	init();
    }

    /** Constructor
     */
    public ExampleTimeHandler(Clock myclock) {
	super("Example time handler", true, true, true, true);
	clock = myclock;
	init();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.time.TimeHandler#setID()
     */
    public void setID(int i) {
        // TODO Auto-generated method stub
        id = i;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.nite.time.TimeHandler#getID()
     */
    public int getID() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setTimeHighlightColor(Color color) {

    }

    /** get the largest end time handled by this TimeHandler. Returns
     * 0.0 since we have no timed elements */
    public double getMaxTime() {
	return 0.0;
    }

}
