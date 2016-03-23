/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.util.List;


import javax.swing.JOptionPane;

import net.sourceforge.nite.gui.actions.NiteAction;

/**
 * @author judyr
 *
 * An option panel is used to throw up a dialogue box in front of the user where they can type input.
 * Other components like buttons, lists, combo boxes and text fields can be added to these, in fact any jComponent
 */
public class OptionPaneHandler extends JComponentHandler {
	
	
	/**
	 * A JOptionPane can display an arbritary number of messages on it. Messages can be components. 
	 * These messages are stored here
	 * */
	private List  messages;
	
	/**
	 * A List of options the user can take when confronted by this option pane. These appear 
	 * as buttons. Examples: Yes, No, Cancel
	 * */

    /**
     * @see net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
    	component = new JOptionPane();
    }

    
    


    
    
    /**
     * Should be able to add any component to this
     * */
    public void addChild(NDisplayObjectHandler child){
    	if (child instanceof JComponentHandler) {
            JComponentHandler jch= (JComponentHandler) child;
            //simply add the component to the messages list
            messages.add(jch.getJComponent());
            
        }
    	
    }





  


 
}
