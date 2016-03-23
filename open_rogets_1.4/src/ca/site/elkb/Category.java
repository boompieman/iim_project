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

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Represents the <i>Roget's Thesaurus Tabular Synopsis of Categories</i>. The
 * topmost level of this ontology divides the <i>Thesaurus</i> into eight
 * Classes:
 * <OL>
 * <LI><i>Abstract Relations</i></LI>
 * <LI><i>Space</i></LI>
 * <LI><i>Matter</i></LI>
 * <LI><i>Intellect: the exercise of the mind (Formation of ideas)</i></LI>
 * <LI><i>Intellect: the exercise of the mind (Communication of ideas)</i></LI>
 * <LI><i>Volition: the exercise of the will (Individual volition)</i></LI>
 * <LI><i>Volition: the exercise of the will (Social volition)</i></LI>
 * <LI><i>Emotion, religion and morality</i></LI>
 * </OL>
 * <br>
 * <p>
 * Classes are further divided into Sections, Sub-sections, Head groups, and
 * Heads.
 * </p>
 * 
 * @author Mario Jarmasz and Alistsair Kennedy
 * @version 1.4 2013
 */

public class Category {
	// Attributes
	private int classCount;

	private int sectionCount;

	private int subSectionCount;

	private int headGroupCount;

	private int headCount;

	private ArrayList<RogetClass> classList;

	private ArrayList<HeadInfo> headList;

	// Constructors
	// 1. No params
	// 2. Filename

	/**
	 * Default Constructor
	 */
	public Category() {
		classCount = 0;
		sectionCount = 0;
		headCount = 0;
		subSectionCount = 0;
		headGroupCount = 0;
		classList = new ArrayList<RogetClass>();
		headList = new ArrayList<HeadInfo>();
	}

	/**
	 * Constructor that builds the <TT>Category</TT> object using the
	 * information contained in a file. The default file for the <i>ELKB</i> is
	 * <TT>rogetMap.rt</TT> contained in the <TT>$HOME/roget_elkb</TT>
	 * directory.
	 * 
	 * @param filename
	 */
	public Category(String filename) {
		this();
		loadFromFile(filename);
	}

	// Get methods only

	/**
	 * Returns the number of <i>Roget's</i> Classes in this ontology.
	 */
	public int getClassCount() {
		return classCount;
	}

	/**
	 * Returns the number of Sections in this ontology.
	 * 
	 * @return section count
	 */
	public int getSectionCount() {
		return sectionCount;
	}

	/**
	 * Returns the number of Sub-sections in this ontology.
	 * 
	 * @return subsection count
	 */
	public int getSubSectionCount() {
		return subSectionCount;
	}

	/**
	 * Returns the number of Head groups in this ontology.
	 * 
	 * @return number of head groups
	 */
	public int getHeadGroupCount() {
		return headGroupCount;
	}

	/**
	 * Returns the number of Heads in this ontology.
	 * 
	 * @return number of heads
	 */
	public int getHeadCount() {
		return headCount;
	}

	/**
	 * Returns the <i>Roget's</i> Class at the specified position in the array
	 * of Classes.
	 * 
	 * @param index
	 * @return RogetClass
	 */
	public RogetClass getRogetClass(int index) {
		RogetClass rogClass;
		index--;
		if ((index >= 0) && (index < classCount)) {
			rogClass = classList.get(index);
		} else {
			rogClass = null;
		}
		return rogClass;
	}

	/**
	 * Prints the <i>Roget's</i> Class at the specified position in the array
	 * of Classes to the standard output.
	 * 
	 * @param index
	 */
	public void printRogetClass(int index) {
		RogetClass rogClass = getRogetClass(index);
		if (rogClass == null) {
			System.out.println(index + " is not a valid Class number");
		} else {
			rogClass.print();
		}
	}

	/**
	 * loads the categories from an xml file using the CategoryHandler class
	 * 
	 * @param fileName
	 */
	private void loadFromFile(String fileName) {
		try{
			//System.out.println("Loading from: " + fileName);
			
			System.setProperty("org.xml.sax.driver","org.apache.crimson.parser.XMLReaderImpl");
			
			XMLReader xr = XMLReaderFactory.createXMLReader();
			CategoryHandler handler = new CategoryHandler(this);
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);
			
			xr.parse(fileName);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Returns the array of <TT>RogetClass</TT> objects.
	 * 
	 * @return ArrayList of classes
	 */
	public ArrayList<RogetClass> getClassList() {
		return classList;
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
	 * Returns the array of <TT>HeadInfo</TT> objects.
	 * 
	 * @return ArrayList of heads
	 */
	public HeadInfo getHeadInfo(int headNum) {
		return headList.get(headNum);
	}

	/**
	 * Prints the array of <TT>HeadInfo</TT> objects to the standard output.
	 */
	public void printHeadInfo() {
		Iterator<HeadInfo> iter = headList.iterator();
		while (iter.hasNext())
			System.out.println(iter.next());
	}

	/**
	 * Converts to a string representation the <TT>Category</TT> object. The
	 * following following format is used - <TT>Category:classCount:sectionCount:subSectionCount:headGroupCount:headCount</TT>.
	 */
	public String toString() {
		StringBuffer sbInfo = new StringBuffer();

		sbInfo.append("Category:");
		sbInfo.append(getClassCount());
		sbInfo.append(":");
		sbInfo.append(getSectionCount());
		sbInfo.append(":");
		sbInfo.append(getSubSectionCount());
		sbInfo.append(":");
		sbInfo.append(getHeadGroupCount());
		sbInfo.append(":");
		sbInfo.append(getHeadCount());

		return sbInfo.toString();
	}

	/**
	 * Increments class Count
	 */
	public void classCountIncrement() {
		classCount++;
	}

	/**
	 * adds to classList
	 * 
	 * @param rtClass
	 */
	public void addToClassList(RogetClass rtClass) {
		classList.add(rtClass);
	}

	/**
	 * Increments subSection count.
	 */
	public void subSectionCountIncrement() {
		subSectionCount++;
	}
	
	/**
	 * Increments head group count.
	 */
	public void headGroupCountIncrement() {
		headGroupCount++;
	}

	/**
	 * adds a RogetHead to the headList.
	 * 
	 * @param rogetHead
	 */
	public void addToHeadList(HeadInfo rogetHead) {
		headList.add(rogetHead);
	}

	/**
	 * Increments Section count.
	 */
	public void sectionCountIncrement() {
		sectionCount++;
	}
	
	/**
	 * increments headCount
	 */
	public void headCountIncrement() {
		headCount++;
	}
	
}

// End of the Category class
