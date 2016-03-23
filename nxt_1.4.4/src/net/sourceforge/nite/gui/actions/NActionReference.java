/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.actions;

import javax.swing.InputMap;
import javax.swing.KeyStroke;

/**
 * @author judyr
 *
 *This represents the NiteActionReference specification
 */
public class NActionReference {
    private InputMap inputMap = new InputMap();

    /**A unique ID refering to an action**/
    private String actionID;
    private String trigger;

    public void addKeyboardInput(String stroke, String actionName){
	// Add a KeyStroke
	inputMap.put(KeyStroke.getKeyStroke(stroke), actionName);
    }

    /**
     * Returns the inputMap.
     * @return InputMap
     */
    public InputMap getInputMap() {
        return inputMap;
    }

    /**
     * Sets the inputMap.
     * @param inputMap The inputMap to set
     */
    public void setInputMap(InputMap inputMap) {
        this.inputMap = inputMap;
    }

    /**
     * Returns the actionID.
     * @return String
     */
    public String getActionID() {
        return actionID;
    }

    /**
     * Sets the actionID.
     * @param actionID The actionID to set
     */
    public void setActionID(String actionID) {
        this.actionID = actionID;
    }
    
    /**
     * Returns the trigger.
     * @return String
     */
    public String getTrigger() {
	return trigger;
    }
    
    /**
     * Sets the trigger.
     * @param trigger The trigger to set
     */
    public void setTrigger(String trigger) {
	this.trigger = trigger;
    }

}
