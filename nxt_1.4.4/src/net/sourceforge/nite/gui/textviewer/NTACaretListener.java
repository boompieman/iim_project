/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.event.*;

/**
 do we want to include the timecaret behaviour HERE?
 
 why? why not?
 
 document:
 
 ctrl effect
 
 select update also if replay? NO
 
 do we extend the CARET span, or do we just update our private selecte3d-highlights, leaving the caret span to be incomplete?
 if last, we should make the 'updated seelction' properly available from outside. (Myrosia seems to ask for the 'real selection' somewhere)

why do we track ctrl state here? because in caretchanges you never hear about ctrl states :-)

 *
 * @author Dennis Reidsma
 */
public class NTACaretListener implements CaretListener, KeyListener {
    
    /*==============================================================
                CONSTRUCTION: REGISTER LISTENERS ETC
    ==============================================================*/
    
    NTextArea textArea;
    
    /**
     * Creates a new listener. This constructor also takes care of registering as a CaretListener
     * and keylistener to the NTA, etc.
     */
    public NTACaretListener(NTextArea t){
        textArea = t;
        textArea.addCaretListener(this);
        textArea.addKeyListener(this);
    }
    
    /*==============================================================
                CTRL-KEY: KEEP TRACK OF WHETHER CTRL IS UP OR DOWN
    ==============================================================*/
    private boolean pressed;
    private boolean replay=true;

    public void keyTyped(KeyEvent e){
        
    }

    /** Handle the key pressed event from the text field.  As soon as
        you get a control key press, store the fact that ctrl is down*/
    public void keyPressed(KeyEvent e) {
        if (replay && (e.getModifiers()&InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK){
	    // only note that ctrl is pressed if no other key is pressed
	    // jonathan 14.12.04
	    if (e.getKeyCode()==KeyEvent.VK_CONTROL) {
		pressed = true;
	    } else {
		pressed = false;
	    }
        }
    }

    /** Handle the key released event from the text field. As soon as
        the control key is released store that fact*/
    public void keyReleased(KeyEvent e) {
        if (replay && (e.getModifiers()&InputEvent.CTRL_MASK) == 0){
            pressed = false;
        }
    }

    /** turn off media / time playing ability for this document. The
     * control-key listener is disabled so media can't be played */
    protected void setReplayEnabled(boolean value) {
	replay=value;
	if (!replay) {
	    pressed=false; // just in case you have ctrl pressed when you call this!
	}
    }    
    
    /*==============================================================
                SELECTION: IF CARET CHANGES, EITHER PLAY SELECTION
                OR UPDATE SELECTION (DEPENDING ON CTRL STATE)
    ==============================================================*/
    

    public void caretUpdate(CaretEvent e) {
        //System.out.println("caret updated");
        if (pressed) {
            //what if pressed / swept IN current select?
            //what if pressed/selectin OUTSIDE current sleect?
            //what if right mouse?
        	if (e.getDot() >= e.getMark()){
        	    textArea.newTimeSelection(e.getDot(), e.getMark());
        	} else {
        	    textArea.newTimeSelection(e.getMark(), e.getDot());
            }        
        } else {
            //System.out.println("This select should involve calling the NTA with the new span. Afterwards, the highlights are changed. The Caret positions are not necessarily changed to reflect the new highlights!");
            textArea.newSelection(e.getDot(), e.getMark()); //we don't reverse dot and mark here. THat is the job of the NTextArea. We want to be able to see in which direction the user swept, if needed.
        } 
    }
}
