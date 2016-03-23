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

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class loads the categories from an xml file.  The class
 * requires an instance of the Category class to be passed in
 * the constructor.  Methods from the category class are used
 * by this class.
 * 
 * @author Alistsair Kennedy
 * @version 1.4 2013
 */
public class CategoryHandler extends DefaultHandler {
	private Section rtSection;
	private String subSectInfo;
	private int subSectNum;
	private ArrayList<String> sGroupInfo;
	private int headGroupNum;
	private int iSection;
	private RogetClass rtClass;
	private Category category;
	
	/**
	 * Initializes the CategoryHandler and is passed an instance
	 * of the Category class.
	 * 
	 * @param c
	 */
	public CategoryHandler(Category c) {
		rtSection = new Section();
		subSectInfo = new String();
		subSectNum = 0;
		headGroupNum = 0;
		sGroupInfo = new ArrayList<String>();
		iSection = 0;
		rtClass = new RogetClass();
		category = c;
	}
	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if(localName.equals("thesaurus")){
			//do nothing
		}
		else if(localName.equals("class")){
			String strClassNum = atts.getValue("number");
			String strClassName = atts.getValue("name");
			category.classCountIncrement();
			iSection = 0;
			rtClass = new RogetClass(category.getClassCount(), strClassNum,
					strClassName);
		}
		else if(localName.equals("section")){
			String strSectNum = atts.getValue("number");
			String strSectName = atts.getValue("name");
			iSection++;
			category.sectionCountIncrement();
			subSectInfo = "";
			rtSection = new Section(iSection, strSectNum, strSectName);
		}
		else if(localName.equals("subsection")){
			subSectInfo = atts.getValue("name");
			subSectNum = Integer.parseInt(atts.getValue("number"));
			category.subSectionCountIncrement();
		}
		else if(localName.equals("headGroup")){
			String start = atts.getValue("first");
			String end = atts.getValue("last");
			headGroupNum = Integer.parseInt(atts.getValue("number"));
			sGroupInfo = new ArrayList<String>();
			for(int i = Integer.parseInt(start); i <= Integer.parseInt(end); i++){
				sGroupInfo.add(i + "");
			}
			category.headGroupCountIncrement();
		}
		else if(localName.equals("head")){
			String headName = atts.getValue("name");
			String headNumber = atts.getValue("number");
			HeadInfo rogetHead = new HeadInfo(Integer.parseInt(headNumber), headName, category.getClassCount(), iSection,
					subSectNum, headGroupNum, subSectInfo, sGroupInfo);
			category.headCountIncrement();
			category.addToHeadList(rogetHead);
		}
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(localName.equals("class")){
			category.addToClassList(rtClass);
		}
		if(localName.equals("section")){
			rtClass.addSection(rtSection);
		}
	}

}
