/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;



import java.util.Set;

import net.sourceforge.nite.gui.actions.InputComponent;

/**
 * @author judyr
 *
 *A class to represent data which is entered by the user on the interface
 */
public class DynamicOutputData extends OutputData {
        
       InputComponent source;
        String componentID;

	public DynamicOutputData(String compid){
	        
        componentID = compid;
	}

        /**
         * Returns the componentID.
         * @return String
         */
        public String getComponentID() {
            return componentID;
        }

        /**
         * Returns the source.
         * @return InputComponent
         */
        public InputComponent getSource() {
            return source;
        }

        /**
         * Sets the componentID.
         * @param componentID The componentID to set
         */
        public void setComponentID(String componentID) {
            this.componentID = componentID;
        }

        /**
         * Sets the source.
         * @param source The source to set
         */
        public void setSource(InputComponent source) {
            this.source = source;
        }
        
        public Set getData(){
                
        	return source. getSelectedObjectModelElements();        
        }
        }


