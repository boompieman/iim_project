/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import net.sourceforge.nite.nxt.ObjectModelElement;
import javax.swing.JComponent;

/**
 * An individual piece of text which can be added to an
 * NTextArea. Each NTextElement can be associated with two XML source
 * elements - one which caused the element to be displayed and another
 * which will be edited when the text is clicked on. It can also have
 * its own style.
 *
 */
public class NTextElement {
    public static final int UNTIMED=-999;
    private String stylename;
    private String content;
    private int position;
    private double starttime=UNTIMED;
    private double endtime=UNTIMED;
    private ObjectModelElement dataElement;
    private ObjectModelElement editElement;

    /**
     * Creates a new NTextElement with the specified content and
     * display style, and with start and end times 
     */
    public NTextElement(String t, String s, int pos, double start, double end){
	stylename =s;
	starttime = start;
	endtime = end;
	content = t;
	position = pos;
    }

    /**
     * Creates a new NTextElement with the specified content and
     * display style
     */
    public NTextElement(String s, String style){
	this.stylename = style;
	content = s;
	starttime=UNTIMED;
	endtime=UNTIMED;
    }

    /**
     * Creates a new NTextElement with the specified content and
     * display style; with start and end times and with an object model
     * element (an item in the source document with which this text
     * element is associated).  */
    public NTextElement(String t, String s, int pos, double start, 
			double end, ObjectModelElement element){
	stylename =s;
	starttime = start;
	endtime = end;
	content = t;
	position = pos;
	dataElement = element;
    }

    public NTextElement(String s, String style, double start, double end){
	this.stylename = style;
	starttime = start;
	endtime = end;
	content = s;
    }

    /**
     * Creates a new NTextElement with the specified content and
     * display style; with start and end times and with an object model
     * element (an item in the source document with which this text
     * element is associated).  */
    public NTextElement(String t, String s, double start, 
			double end, ObjectModelElement element){
	stylename =s;
	starttime = start;
	endtime = end;
	content = t;
	dataElement = element;
    }


    /**
     * Creates a new NTextElement with the specified content and
     * display style and with an object model element (an item in the
     * source document with which this text element is associated).
     */
    public NTextElement(String s, String style, ObjectModelElement element){
	this.stylename = style;
	content = s;
	starttime = UNTIMED;
	endtime = UNTIMED;
	dataElement = element;
    }

    /**
     * Creates a new NTextElement with the specified content and
     * display style; with start and end times and with TWO object
     * model elements - the first is the element that caused this
     * element to be displayed, and the second is the element
     * associated with this for editing purposes. 
     */
    public NTextElement(String t, String s, int pos, double start, 
			double end, ObjectModelElement element,
			ObjectModelElement edit_element){
	stylename =s;
	starttime = start;
	endtime = end;
	content = t;
	position = pos;
	dataElement = element;
	editElement = edit_element;
    }

    /**
     * Creates a new NTextElement with the specified content and
     * display style and with TWO object model elements - the first is
     * the element that caused this element to be displayed, and the
     * second is the element associated with this for editing
     * purposes.  */
    public NTextElement(String s, String style, ObjectModelElement element,
			ObjectModelElement edit_element){
	this.stylename = style;
	content = s;
	starttime = UNTIMED;
	endtime = UNTIMED;
	dataElement = element;
	editElement = edit_element;
    }

    public NTextElement(){

    }
    
    /**
     * Returns a new copy of this NTextElement
     * @return
     */
    public NTextElement copy(){
    	NTextElement newEl = new NTextElement();
    	newEl.setDataElement(this.getDataElement());
    	newEl.setEditElement(this.getEditElement());
    	newEl.setEndTime(this.getEndTime());
    	newEl.setStartTime(this.getStartTime());
    	newEl.setPosition(this.getPosition());
    	newEl.setText(this.getText());
    	newEl.setStyle(this.getStyle());
    	return newEl;
    }

   
    public void setStartTime(double s){
	starttime = s;
    }

    public void setEndTime(double e){
	endtime = e;
    }

    public int getPosition(){
	return position;
    }

    public double getStartTime(){
	return starttime;
    }

    public double getEndTime(){
	return endtime;
    }

    /**
     *Returns the content of this text element as a string
     */
    public String getText(){
	return content;
    }

    /**
     * Sets the content of this text element to be the argument string
     */
    public void setText(String s){
	content = s;
    }

    public void setPosition(int p){
	position = p;
    }

    /**
     * Return the style object which specifies the way the content is displayed 
     */
    public String getStyle(){
	return stylename;
    }

    /**
     * Sets the content of this text element to be displayed in the specified style
     */
    public void setStyle(String name){
       stylename =name;
    }

    public String toString(){
	return content + " " + starttime + " " + endtime;
    }

    /**
     * Returns the dataElement.
     * @return ObjectModelElement
     */
    public ObjectModelElement getDataElement() {
        return dataElement;
    }

    /**
     * Sets the dataElement.
     * @param dataElement The dataElement to set
     */
    public void setDataElement(ObjectModelElement dataElement) {
        this.dataElement = dataElement;
    }

    /**
     * Returns the display Element - by default this is the same as
     * the dataElement, but it may be different if the displayed
     * element is different to the edited one.
     * @return ObjectModelElement */
    public ObjectModelElement getEditElement() {
	if (editElement==null) {
	    return dataElement;
	} else {
	    return editElement;
	}
    }

    /**
     * Sets the editElement - by default this is the same as the
     * dataElement, but it may be different for specialised displays
     * (where one wishes particular highlighting and editing
     * behaviour). The edit element defines the element that is edited
     * when this textelement is highlighted.
     *
     * @param dataElement The dataElement to set */
    public void setEditElement(ObjectModelElement editElement) {
        this.editElement = editElement;
    }

}

