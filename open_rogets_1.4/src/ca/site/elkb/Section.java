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
 * Represents a <i>Roget's Thesaurus</i> Section. A Section is defined by the
 * following attributes:
 * <ul>
 * <li>Section number</li>
 * <li>Section number in string format</li>
 * <li>Section name</li>
 * <li>number of the first Head</li>
 * <li>number of the last Head</li>
 * <li>array of Heads</li>
 * </ul>
 * A Section can contain <TT>Head</TT> or <TT>HeadInfo</TT> objects,
 * depending on the use.
 * 
 * @author Mario Jarmasz and Alistsair Kennedy
 * @version 1.4 2013
 */

public class Section {
	// Attributes
	private int sectionNum;

	private String strSectionNum;

	private String sectionName;

	private int headStart;

	private int headEnd;

	private ArrayList<HeadInfo> headInfoList;

	
	/**
	 * Default constructor.
	 */
	public Section() {
		sectionNum = 0;
		strSectionNum = new String();
		sectionName = new String();
		headStart = 0;
		headEnd = 0;
		headInfoList = new ArrayList<HeadInfo>();
	}

	/**
	 * Constructor which sets the Section number and name.
	 * 
	 * @param number
	 * @param name
	 */
	public Section(int number, String name) {
		this();
		sectionNum = number;
		sectionName = name;
	}

	/**
	 * Constructor which sets the Section number and name, as well as the number
	 * of the first and last Head.
	 * 
	 * @param number
	 * @param name
	 * @param start
	 * @param end
	 */
	public Section(int number, String name, int start, int end) {
		this(number, name);
		headStart = start;
		headEnd = end;
	}

	/**
	 * Constructor which sets the Section number, name, and Section number in
	 * string format and Class name, while parsing the strings for the Section
	 * number and name. Examples of the strings to be parsed are: <BR>
	 * <CODE> ^sectionNumber>#Section one #^/sectionNumber> </CODE><BR>
	 * <CODE> ^sectionTitle>#^size=-1>#^b>#Existence
	 * #^/b>#^/size>#^/sectionTitle> </CODE>
	 * 
	 * @param number
	 * @param strNum
	 * @param strName
	 */
	public Section(int number, String strNum, String strName) {
		this();
		sectionNum = number;
		//parseSectNum(strNum);
		this.strSectionNum = strNum;
		//parseSectName(strName);
		this.sectionName = strName;
	}

	/**
	 * Returns the number of this Section.
	 * 
	 * @return section number
	 */
	public int getSectionNum() {
		return sectionNum;
	}

	/**
	 * Sets the number of this Section.
	 * 
	 * @param num
	 */
	public void setSectionNum(int num) {
		sectionNum = num;
	}

	/**
	 * Returns the number of this Section in string format.
	 * 
	 * @return section number as string
	 */
	public String getStrSectionNum() {
		return strSectionNum;
	}

	/**
	 * Sets the number of this Section in string format.
	 * 
	 * @param snum
	 */
	public void setStrSectionNum(String snum) {
		strSectionNum = snum;
	}

	/**
	 * Returns the name of this Section.
	 * 
	 * @return section name
	 */
	public String getSectionName() {
		return sectionName;
	}

	/**
	 * Sets the number of this Section in string format.
	 * 
	 * @param name
	 */
	public void setSectionName(String name) {
		sectionName = name;
	}

	/**
	 * Returns the number of the first Head of this Section.
	 * 
	 * @return head start number
	 */
	public int getHeadStart() {
		return headStart;
	}

	/**
	 * Sets the number of the first Head of this Section.
	 * 
	 * @param start
	 */
	public void setHeadStart(int start) {
		headStart = start;
	}

	/**
	 * Returns the number of the last Head of this Section.
	 * 
	 * @return head end number
	 */
	public int getHeadEnd() {
		return headEnd;
	}

	/**
	 * Sets the number of the last Head of this Section.
	 * 
	 * @param end
	 */
	public void setHeadEnd(int end) {
		headEnd = end;
	}

	/**
	 * Adds a <TT>HeadInfo</TT> object to this Section.
	 * 
	 * @param head
	 */
	public void addHeadInfo(HeadInfo head) {
		headInfoList.add(head);
	}

	/**
	 * Returns the array of <TT>HeadInfo</TT> objects of this Section.
	 * 
	 * @return ArrayList of head info
	 */
	public ArrayList<HeadInfo> getHeadInfoList() {
		return headInfoList;
	}

	/**
	 * Returns the number of Heads in this Section.
	 * 
	 * @return number of heads
	 */
	public int headCount() {
		return headInfoList.size();
	}

	/**
	 * Converts to a string representation the <TT>Section</TT> object.
	 */
	public String toString() {
		String info = new String();
		info = super.toString();
		info += "@" + getSectionNum() + "@" + getSectionName();
		info += "@" + getHeadStart() + "@" + getHeadEnd();
		return info;
	}

	/**
	 * Prints the content of this Section to the standard output.
	 */
	public void print() {
		String info = new String();
		info += getSectionNum() + " " + getSectionName();
		// Maybe I should add code that adds a variable # of tabs ?
		info += "\t" + getHeadStart() + "-" + getHeadEnd();
		System.out.println(info);
	}

	/**
	 * Prints the information regarding the Heads contained in this Section to
	 * the standard output.
	 */
	public void printHeadInfo() {
		if (headInfoList.isEmpty()) {
			System.out.println("This section does not contain any Heads");
		} else {
			System.out.println("SECTION: " + sectionNum + " " + sectionName);
			Iterator<HeadInfo> iter = headInfoList.iterator();
			while (iter.hasNext()) {
				HeadInfo head = iter.next();
				head.print();
			}
		}
	}

}
