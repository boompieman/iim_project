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
 * This class loads a head file using an instance of the Head
 * class who's methods are used.
 * 
 * @author Alistsair Kennedy
 * @version 1.4 2013
 */
public class HeadHandler extends DefaultHandler {

	private Head head;
	private StringBuffer sb;
	private Paragraph currentPara;
	private SG currentSG;
	private ArrayList<String> currentSGWords;
	private String currentPOS;
	private boolean recordWord;
	private int refCount;
	private int headNumber;
	private int currentSGNum;
	
	/**
	 * Initializes the class, when it is passed an instance of
	 * the Head class
	 * 
	 * @param h
	 */
	public HeadHandler(Head h){
		super();
		head = h;
		sb = new StringBuffer();
		currentPara = new Paragraph();
		currentSG = new SG();
		currentPOS = "";
		recordWord = false;
		currentSGWords = new ArrayList<String>();
		refCount = 0;
		headNumber = 0;
		currentSGNum = 0;
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if(localName.equals("head")){
			head.setClassNum(Integer.parseInt(atts.getValue("class")));
			head.setSectionNum(Integer.parseInt(atts.getValue("section")));
			head.setSubSectionNum(Integer.parseInt(atts.getValue("subSection")));
			head.setHeadGroupNum(Integer.parseInt(atts.getValue("headGroup")));
			head.setHeadName(atts.getValue("name"));
			headNumber = Integer.parseInt(atts.getValue("number"));
			head.setHeadNum(headNumber);
		}
		else if(localName.equals("pos")){
			currentPOS = atts.getValue("type");
			head.setPOSStart(currentPOS);
		}
		else if(localName.equals("paragraph")){
			head.incrementParaCount();
			currentPara = new Paragraph(headNumber, Integer.parseInt(atts.getValue("number")), atts.getValue("name"), currentPOS);
		}
		else if(localName.equals("sg")){
			currentSGWords = new ArrayList<String>();
			currentSGNum = Integer.parseInt(atts.getValue("number"));
			refCount = 0;
		}
		else if(localName.equals("word")){
			recordWord = true;
			if(atts.getIndex("headRef") == 1){
				refCount++;
			}
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(localName.equals("word")){
			recordWord = false;
			currentSGWords.add(sb.toString());
			sb = new StringBuffer();
		}
		else if(localName.equals("sg")){
			currentSG = new SG(currentSGNum, currentPara.getParaNum(), headNumber, currentSGWords, currentPOS);
			currentPara.addSG(currentSG);
		}
		else if(localName.equals("paragraph")){
			head.addPara(currentPara, currentPOS);
		}
	}

	public void characters(char[] chars, int start, int length) throws SAXException {
		if(recordWord){
			sb.append(new String(chars, start, length));
		}
	}
}
