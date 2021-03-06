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
 * Represents the topmost element in <i>Roget's Thesaurus Tabular Synopsis of
 * Categories</i>. It is represented by its number, name, subclass name if it
 * is a subclass of an original Roget Class, and range of Sections that it
 * contains. For example, Class <i>4. Intellect: the exercise of the mind
 * (Formation of ideas)</i> is represented as:
 * <ul>
 * <li><b>Class number</b>: 4</li>
 * <li><b>Class number in string format</b>: Class four</li>
 * <li><b>Class Name</b>: Intellect: the exercise of the mind</li>
 * <li><b>First section</b>: 16</li>
 * <li><b>Last section</b>: 22</li>
 * </ul>
 * 
 * @author Mario Jarmasz and Alistsair Kennedy
 * @version 1.4 2013
 ******************************************************************************/

public class RogetClass {
	// Attributes
	private int classNum;

	private String strClassNum;

	private String className;

	// subClasses do not exist any more...
	private String subClassName;

	// sectionStart and End? Not convinced that they should be used
	private int sectionStart;

	private int sectionEnd;

	private ArrayList<Section> sectionList;

	/**
	 * Default constructor.
	 */
	public RogetClass() {
		classNum = 0;
		strClassNum = new String();
		className = new String();
		subClassName = new String();
		sectionStart = 0;
		sectionEnd = 0;
		sectionList = new ArrayList<Section>();
	}

	/**
	 * Constructor which sets the Class number and name.
	 * 
	 * @param num
	 * @param name
	 */
	public RogetClass(int num, String name) {
		this();
		classNum = num;
		className = name;
	}

	/**
	 * Constructor which sets the Class number, Class number in string format,
	 * Class and Sub-class name.
	 * 
	 * @param num
	 * @param snum
	 * @param name
	 * @param subClass
	 */
	public RogetClass(int num, String snum, String name, String subClass) {
		this(num, snum, name);
		subClassName = subClass;
	}

	/**
	 * Constructor which sets the Class number and name, as well as the first
	 * and last Section number.
	 * 
	 * @param num
	 * @param name
	 * @param start
	 * @param end
	 */
	public RogetClass(int num, String name, int start, int end) {
		this(num, name);
		sectionStart = start;
		sectionEnd = end;
	}

	/**
	 * onstructor which sets the Class number, Class number in string format,
	 * Class name, as well as the first and last Section number.
	 * 
	 * @param num
	 * @param snum
	 * @param name
	 * @param start
	 * @param end
	 */
	public RogetClass(int num, String snum, String name, int start, int end) {
		this(num, name, start, end);
		strClassNum = snum;
	}

	/**
	 * Constructor which sets the Class number, Class number in string format,
	 * Class name, Sub-class name as well as the first and last Section number.
	 * 
	 * @param num
	 * @param snum
	 * @param name
	 * @param subClass
	 * @param start
	 * @param end
	 */
	public RogetClass(int num, String snum, String name, String subClass,
			int start, int end) {
		this(num, snum, name, start, end);
		subClassName = subClass;
	}

	/**
	 * Constructor which sets the Class number, Class number in string format
	 * and Class name, while parsing the strings for the Class number and name.
	 * Examples of the strings to be parsed are: <BR>
	 * <CODE> ^classNumber>#^i>#Class one #^/i>#^/classNumber> </CODE><BR>
	 * <CODE> ^classTitle>#^i>#Abstract Relations #^/i>#^/classTitle> </CODE>
	 * 
	 * @param num
	 * @param strClassNum
	 * @param strClassName
	 */
	public RogetClass(int num, String strClassNum, String strClassName) {
		this();
		classNum = num;
		//parseClassNum(strClassNum);
		this.strClassNum = strClassNum;
		//parseClassName(strClassName);
		this.className = strClassName;
	}


	/**
	 * Returns the number of this RogetClass.
	 * 
	 * @return class number
	 */
	public int getClassNum() {
		return classNum;
	}

	/**
	 * Sets the number of this RogetClass.
	 * 
	 * @param num
	 */
	public void setClassNum(int num) {
		classNum = num;
	}

	/**
	 * Returns the number of this RogetClass in string format.
	 * 
	 * @return class number as string
	 */
	public String getStrClassNum() {
		return strClassNum;
	}

	/**
	 * Sets the number of this RogetClass in string format.
	 * 
	 * @param snum
	 */
	public void setStrClassNum(String snum) {
		strClassNum = snum;
	}

	/**
	 * Returns the name of this RogetClass.
	 * 
	 * @return class name
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Sets the name of this RogetClass.
	 * 
	 * @param name
	 */
	public void setClassName(String name) {
		className = name;
	}

	/**
	 * Returns the Sub-class name of this RogetClass.
	 * 
	 * @return sub class name
	 */
	public String getSubClassName() {
		return subClassName;
	}

	/**
	 * Sets the Sub-class name of this RogetClass.
	 * 
	 * @param subClass
	 */
	public void setSubClassName(String subClass) {
		subClassName = subClass;
	}

	/**
	 * Returns the number of the first section of this RogetClass.
	 * 
	 * @return section start number
	 */
	public int getSectionStart() {
		return sectionStart;
	}

	/**
	 * Sets the number of the first section of this RogetClass.
	 * 
	 * @param start
	 */
	public void setSectionStart(int start) {
		sectionStart = start;
	}

	/**
	 * Returns the number of the last section of this RogetClass.
	 * 
	 * @return section end number
	 */
	public int getSectionEnd() {
		return sectionEnd;
	}

	/**
	 * Sets the number of the last section of this RogetClass.
	 * 
	 * @param end
	 */
	public void setSectionEnd(int end) {
		sectionEnd = end;
	}

	/**
	 * Adds a Section to this RogetClass.
	 * 
	 * @param section
	 */
	public void addSection(Section section) {
		sectionList.add(section);
	}

	/**
	 * Returns the array of <TT>Section</TT> objects in this RogetClass.
	 * 
	 * @return ArrayList of sections
	 */
	public ArrayList<Section> getSectionList() {
		return sectionList;
	}

	/**
	 * Returns the number of Sections of this RogetClass.
	 * 
	 * @return number of sections
	 */
	public int sectionCount() {
		return sectionList.size();
	}

	/**
	 * Returns the number of Heads of this RogetClass.
	 * 
	 * @return number of heads
	 */
	public int headCount() {
		int count = 0;
		if (sectionList.isEmpty()) {
			count = 0;
		} else {
			Iterator<Section> iter = sectionList.iterator();
			while (iter.hasNext()) {
				Section section = iter.next();
				count += section.headCount();
			}
		}
		return count;
	}

	/**
	 * Converts to a string representation the <TT>RogetClass</TT> object.
	 */
	public String toString() {
		String info = new String();
		info = super.toString();
		info += "@" + getClassNum() + "@" + getStrClassNum();
		info += "@" + getClassName() + "@" + getSubClassName();
		info += "@" + getSectionStart() + "@" + getSectionEnd();
		return info;
	}

	/**
	 * Prints the contents of this RogetClass to the standard output.
	 */
	public void print() {
		String strNum = new String();

		if (strClassNum.equals("")) {
			strNum = "Class " + classNum + ": ";
		} else {
			strNum = strClassNum + ": ";
		}
		System.out.println(strNum + className);

		if (!(subClassName.equals(""))) {
			System.out.println(subClassName);
		}

		if (sectionList.isEmpty()) {
			System.out.println("There are no Sections in this Class");
		} else {
			Iterator<Section> iter = sectionList.iterator();
			while (iter.hasNext()) {
				Section section = iter.next();
				section.print();
			}
		}
	}

}
