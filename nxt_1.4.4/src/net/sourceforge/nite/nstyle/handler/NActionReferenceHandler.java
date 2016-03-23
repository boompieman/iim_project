/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import net.sourceforge.nite.gui.actions.NActionReference;
import net.sourceforge.nite.nstyle.NConstants;

/**
 * @author judyr
 *This processes a NiteActionReference specification which can be associated with every DisplayObject
 * */
public class NActionReferenceHandler extends NDisplayObjectHandler {

	/**
	 * The action reference associated with this handler
	 * */
	private NActionReference actionref;
	

    /**
     * @see net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#createPeer()
     */
    protected void createPeer() {
    	 String actionId = (String) properties.get(NConstants.actionID);
    	 String userInput = (String) properties.get(NConstants.userInput);
         String trigger = (String) properties.get(NConstants.keyBinding);
    	  actionref = new NActionReference();
    	 actionref.setActionID(actionId);
    	 actionref.addKeyboardInput(userInput, actionId);
         actionref.setTrigger(trigger);
    }


    /**
     * @see net.sourceforge.nite.nstyle.handler.NDisplayObjectHandler#addChild(NDisplayObjectHandler)
     * There should be no children for this type of handler
     */
    public void addChild(NDisplayObjectHandler child) {
    }

    /**
     * Returns the actionref.
     * @return NActionReference
     */
    public NActionReference getActionref() {
        return actionref;
    }

    /**
     * Sets the actionref.
     * @param actionref The actionref to set
     */
    public void setActionref(NActionReference actionref) {
        this.actionref = actionref;
    }

}
