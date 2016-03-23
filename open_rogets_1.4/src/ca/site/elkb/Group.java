/*
 * 
 *    Copyright (C) 2009
 *    Mario Jarmasz, Alistair Kennedy and Stan Szpakowicz
 *    School of Information Technology and Engineering (SITE)
 *    University of Ottawa, 800 King Edward Avenue
 *    Ottawa, Ontario, Canada, K1N 6N5
 *    
 *    and
 *    
 *    Olena Medelyan
 *    Department of Computer Science,
 *    The University of Waikato
 *    Private Bag 3105, Hamilton, New Zealand
 *    
 *    This file is part of Open Roget's Thesaurus ELKB.
 *    
 *    Copyright (c) 2009, Mario Jarmasz, Alistair Kennedy, Stan Szpakowicz and Olena Medelyan
 *    All rights reserved.
 *    
 *    Redistribution and use in source and binary forms, with or without
 *    modification, are permitted provided that the following conditions are met:
 *        * Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *        * Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *        * Neither the name of the University of Ottawa nor the
 *          names of its contributors may be used to endorse or promote products
 *          derived from this software without specific prior written permission.  
 *        * All advertising materials mentioning features or use of this software
 *          must display the following acknowledgement:
 *          This product includes software developed by the University of
 *          Ottawa and its contributors.
 *    
 *    THIS SOFTWARE IS PROVIDED BY Mario Jarmasz, Alistair Kennedy, Stan Szpakowicz 
 *    and Olena Medelyan ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 *    BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 *    A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Mario Jarmasz, Alistair 
 *    Kennedy, Stan Szpakowicz and Olena Medelyan BE LIABLE FOR ANY DIRECT, 
 *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 *    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
 *    OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 *    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 *    EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *     
 */

package ca.site.elkb;

import java.util.*;

/**
 * Represents a <i>Roget's Thesaurus</i> Head group. For example:
 * <UL>
 * 79 Generality &nbsp;&nbsp;&nbsp; 80 Speciality
 * </UL>
 * A <TT>Group</TT> can contain 1,2 or 3 <TT>HeadInfo</TT> objects.
 * 
 * @author Mario Jarmasz and Alistsair Kennedy
 * @version 1.4 2013
 */

public class Group {

	// Attributes
	private int headCount;

	private int headStart;

	private ArrayList<HeadInfo> headList;

	/**
	 * Default constructor.
	 */
	public Group() {
		headCount = 0;
		headStart = 0;
		headList = new ArrayList<HeadInfo>();
	}

	/**
	 * Constructor that takes an integer to indicate first Head number of the
	 * Group.
	 * 
	 * @param start
	 */
	public Group(int start) {
		headCount = 0;
		headStart = start;
		headList = new ArrayList<HeadInfo>();
	}

	/**
	 * Returns the array of <TT>HeadInfo</TT> objects.
	 * 
	 * @return ArrayList of heads
	 */
	public ArrayList<HeadInfo> getHeadList() {
		return headList;
	}

	/**
	 * Add a <TT>HeadInfo</TT> object to this Group.
	 * 
	 * @param head
	 */
	public void addHead(HeadInfo head) {
		headList.add(head);
		headCount++;
	}

	/**
	 * Returns the number of Heads in this Group.
	 * 
	 * @return head count
	 */
	public int getHeadCount() {
		return headCount;
	}

	/**
	 * Sets the number of the first Head in this Group.
	 * 
	 * @param start
	 */
	public void setHeadStart(int start) {
		headStart = start;
	}

	/**
	 * Returns the number of the first Head in this Group.
	 * 
	 * @return first head number
	 */
	public int getHeadStart() {
		return headStart;
	}

	/**
	 * Converts to a string representation the <TT>Group</TT> object.
	 */
	public String toString() {
		String info = new String();
		if (headCount >= 1) {
			info += headList.get(0);
		}
		if (headCount >= 2) {
			info += "\t\t" + headList.get(1);
		}
		if (headCount >= 3) {
			info += "\n\t\t" + headList.get(2);
		}
		return info;
	}

}
