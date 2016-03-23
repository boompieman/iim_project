/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author judyr
 *
 * This class is used to map between character positions in a
 * document, and the NTextElements in a document Maintain a flat list
 * of elements in the right order, without repetition. When you need to
 * map a character position to an element, iterate over them until you
 * find one which matches. Then when you do an update you just to
 * increment/decrement the start position of every element after the
 * one which changed
 **/
public class PositionElementMapper {

    // Note that the order of this list must stay in document order for
    // indices to remain correct.
    private List positionToElement = new ArrayList();

    public PositionElementMapper() {

    }

    /**
     * Add an element into the map at the specified index. This means
     * incrementing or decrementing positions of the other text
     * elements after that point in the list
     * @param index
     * @param e */
    public void addElement(int position, NTextElement newEl) {
        //just in case - set the position of the newEl to be the specified offset
        newEl.setPosition(position);
	int index = positionToElement.size();

	// jonathan 6/12/05 we add many strings to the end, so here's a test
	// for that special case
	NTextElement el = (NTextElement) positionToElement.get(index-1);
	if (!(position >= el.getPosition() && position < (el.getPosition() + el.getText().length()))) {
	    // jonathan decided to search from the ennd towards the start of the doc 6.12.05
	    // for (int i = 0; i < positionToElement.size(); i++) {
	    for (int i = positionToElement.size()-1; i >=0 ; i--) {
		el = (NTextElement) positionToElement.get(i);
		if ((position >= el.getPosition())
		    && (position < el.getPosition() + el.getText().length())) {
		    index = i;
		    break;
		}
	    }
	} 

	positionToElement.add(index, newEl);

        // now for every ntextelement in the document after this
        // position, update the position information
        int diff = newEl.getText().length();
        //for (int i = index+1; i < positionToElement.size(); i++) {

	// jonathan decided to search from the ennd towards the start of the doc 6.12.05
        for (int i = positionToElement.size()-1; i >= index+1;  i--) {
            el = (NTextElement) positionToElement.get(i);
            int old = el.getPosition();
            el.setPosition(el.getPosition() + diff);
        }
    }

    /**
     * Adds this element and the 
     * @param e
     */
    public void appendElement(NTextElement e) {
        positionToElement.add(e);
    }

    /**
     *  The  specified textelement has been removed from the document, so remove
     * it from the index. Also update the positions of the text elements after
     * the specified one in the document. 
     * @param e
     */
    public void removeElement(NTextElement e) {
	if (e != null) {
	    int index = positionToElement.size();
	    int position = e.getPosition();
	    for (int i = 0; i < positionToElement.size(); i++) {
		NTextElement el = (NTextElement) positionToElement.get(i);
		if ((position >= el.getPosition())
		    && (position < el.getPosition() + el.getText().length())) {
		    index = i;
		    break;
		}
	    }
	    int diff = e.getText().length();
	    
	    //now for every ntextelement in the document after this
	    //position, update the position information
	    
	    for (int i = index; i < positionToElement.size(); i++) {
		NTextElement el = (NTextElement) positionToElement.get(i);
		int old = el.getPosition();
		el.setPosition(old - diff);
	    }
	    positionToElement.remove(e);
	}
    }


    /**
     * Return the text element which is placed at the specified
     * character position in the document
     * @param index
     * @return 
     */
    public NTextElement getElementAtPosition(int position) {
        NTextElement e = null;
       
        Iterator it = positionToElement.iterator();
        while (it.hasNext()) {
            e = (NTextElement) it.next();
            if ((position >= e.getPosition())
                && (position < e.getPosition() + e.getText().length())) {
                return e;
            }
        }
        return null;
    }

    /**
     * Return the character position of the specified text element
     * @param e
     * @return
     */
    public int getTextElementPosition(NTextElement e) {
        int i = -999;
        Iterator it = positionToElement.iterator();
        while (it.hasNext()) {
            NTextElement current = (NTextElement) it.next();
	    if (current==e) {
                return current.getPosition();
	    }
	    /*
            String s1 = e.getText() + e.getStartTime();
            String s2 = current.getText() + current.getStartTime() ;

            if (s1.equals(s2))
                return current.getPosition();
	    */
        }
        return i;
    }
}
