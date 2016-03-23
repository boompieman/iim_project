/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.nstyle.handler;

import java.util.Set;

import net.sourceforge.nite.gui.actions.OutputComponent;

/**
 * @author judyr
* A data structure for representing data which should be output on a component.
 */
public class OutputData {


		Set data;
		String targetid;
		String type;
		OutputComponent target;
		
		
		public OutputData(){
		        
		
		}
		
		public OutputData(String t, Set d){
			targetid = t;
			data = d;
		}	
		public String getTargetID(){
		return targetid;	
		}
		
		public OutputComponent getTarget(){
			return target;
		}
		
		public Set getData(){
		return data;	
		}
		
		public void setData(Set d){
		data = d;
			
		}
		
		public void setTarget(OutputComponent o){
		target = o;	
		}
        /**
         * Returns the targetid.
         * @return String
         */
        public String getTargetid() {
            return targetid;
        }

        /**
         * Returns the type.
         * @return String
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the targetid.
         * @param targetid The targetid to set
         */
        public void setTargetid(String targetid) {
            this.targetid = targetid;
        }

        /**
         * Sets the type.
         * @param type The type to set
         */
        public void setType(String type) {
            this.type = type;
        }
        
        
        
        }

	

