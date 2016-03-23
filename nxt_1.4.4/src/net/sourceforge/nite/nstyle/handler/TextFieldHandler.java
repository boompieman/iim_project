/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;


import java.util.Set;
import java.util.TreeSet;

import javax.swing.JTextField;

import net.sourceforge.nite.gui.actions.NiteAction;
import net.sourceforge.nite.gui.actions.InputComponent;

// import net.sourceforge.nite.nxt.PlainTextElement;



/**
 * @author judyr
 *
 * 
 */
public class TextFieldHandler extends JComponentHandler implements InputComponent   {

    /**
     * @see net.sourceforge.net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    
    protected void createPeer() {
        component = new JTextField(content);
        setUpForegroundColour();
        setUpBackgroundColour();
        setUpFont();
       
        setUpToolTip();
        

    }
    
    
   
    public Set getSelectedObjectModelElements(){
	Set s = new TreeSet();
	//	s.add(new PlainTextElement(((JTextField)component).getText()));
    	return s;
    }

    /**
     * @see net.sourceforge.nite.nstyle.handler.JComponentHandler#registerAction(java.lang.String, net.sourceforge.nite.gui.actions.NiteAction)
     */
    public void registerAction(String binding, NiteAction a) {
    	super.registerAction(binding, a);
            ((JTextField)component).addActionListener(a);
    }
    
}
