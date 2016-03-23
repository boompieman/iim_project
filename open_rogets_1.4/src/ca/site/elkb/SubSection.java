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

/*******************************************************************************
 * Represents a <i>Roget's Thesaurus</i> Sub-section.
 * 
 * A Sub-section may or may not exist. Here is an example:
 * <UL>
 * <LI><b>Class one</b>: Abstract Relations</LI>
 * <LI><b>Section one</b>: Existence</LI>
 * <LI><b>Sub-section title</b>: Abstract</LI>
 * <LI><b>Head group</b>:1 Existence - 2 Nonexistence</LI>
 * </UL>
 * 
 * Sub-sections may contain several Head groups.
 * 
 * @author Mario Jarmasz and Alistsair Kennedy
 * @version 1.4 2013
 ******************************************************************************/

// MJ: May, 2003
// This class does not seem complete nor does it seem to be used
// much by the ELKB!
public class SubSection {

	// Attributes
	private String name;

	private int headCount;

	private int headStart;

	private int groupCount;

	private ArrayList<Group> groupList;

	/**
	 * Default constructor.
	 */
	public SubSection() {
		headCount = 0;
		headStart = 0;
		groupCount = 0;
		groupList = new ArrayList<Group>();
		name = new String();
	}

	/**
	 * Constructor which sets the number of the first Head.
	 * 
	 * @param start
	 */
	public SubSection(int start) {
		this();
		headStart = start;
	}

	/**
	 * Constructor which sets the name of the Section by parsing a string. An
	 * example of a string to parse is: <BR>
	 * <CODE> ^subSectionTitle>#Abstract#^/subSectionTitle> </CODE>
	 * 
	 * @param sInfo
	 */
	public SubSection(String sInfo) {
		headCount = 0;
		headStart = 0;
		groupCount = 0;
		groupList = new ArrayList<Group>();
		Parse(sInfo);
	}

	/**
	 * Constructor which sets the number of the first Head and the name of the
	 * Section supplied as a string to be parsed. An example of a string to
	 * parse is: <BR>
	 * <CODE> ^subSectionTitle>#Abstract#^/subSectionTitle> </CODE>
	 * 
	 * @param start
	 * @param sInfo
	 */
	public SubSection(int start, String sInfo) {
		this(sInfo);
		headStart = start;
	}

	/**
	 * Method to parse a string of type: <subSectionTitle>#Abstract#</subSectionTitle>
	 * 
	 * @param sInfo
	 */
	private void Parse(String sInfo) {
		StringTokenizer st = new StringTokenizer(sInfo, "#");
		st.nextToken();
		name = st.nextToken().trim();
	}

	/**
	 * Returns the list of Head groups in this Sub-section.
	 * 
	 * @return ArrayList of groups
	 */
	public ArrayList<Group> getGroupList() {
		return groupList;
	}

	/**
	 * Adds a Head Group to this Sub-section.
	 * 
	 * @param group
	 */
	public void addGroup(Group group) {
		groupList.add(group);
		groupCount++;
		headCount += group.getHeadCount();
	}

	/**
	 * Returns the number of Heads in this Sub-section.
	 * 
	 * @return number of heads
	 */
	public int getHeadCount() {
		return headCount;
	}

	/**
	 * Returns the number of Head groups in this Sub-section.
	 * 
	 * @return group count
	 */
	public int getGroupCount() {
		return groupCount;
	}

	/**
	 * Sets the number of the first Head in this Sub-section.
	 * 
	 * @param start
	 */
	public void setHeadStart(int start) {
		headStart = start;
	}

	/**
	 * Returns the number of the first Head in this Sub-section.
	 * 
	 * @return number of first head
	 */
	public int getHeadStart() {
		return headStart;
	}

	/**
	 * Displays the content of a Sub-section in a similar way to <i>Roget's
	 * Thesaurus Tabular Synopisis of Categories</i> to the standard output. An
	 * example of this display is: <BR>
	 * <CODE> Absolute: 9 Relation 10 Unrelatedness <BR>
	 * 11 Consanguinity <BR>
	 * 12 Correlation <BR>
	 * 13 Identity 14 Contrariety <BR>
	 * 15 Difference </CODE>
	 */
	public void print() {
		System.out.println(toString());
	}

	/**
	 * Converts to a string representation the <TT>SubSection</TT> object.
	 */
	public String toString() {
		String info = new String();
		info += name + ": \t";
		// info +=
		// print all groups
		// info +=
		// print groups
		return info;
	}

}
