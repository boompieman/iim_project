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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
/**
 * This class loads the Index html file and used functions from 
 * the Index class to create a new index.
 * 
 * @author Alistsair Kennedy
 * @version 1.4 2013
 */
public class IndexHandler extends DefaultHandler {

	private String keyWord;
	private String paragraphWord;
	private boolean isKeyWord;
	private boolean isLocation;
	private Index index;
	private StringBuffer sb;
	
	/**
	 * Loads constructs the class and is passed an Index object
	 * who's methods will be called when building the new index. 
	 * 
	 * @param ind
	 */
	public IndexHandler(Index ind){
		super();
		index = ind;
		keyWord = "";
		paragraphWord = "";
		sb = new StringBuffer();
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (localName.equals("word")){
			isKeyWord = true;
		}
		if(localName.equals("location")){
			isLocation = true;
			paragraphWord = atts.getValue("para");
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("location")){
			isLocation = false;
			String location = sb.toString();
			String[] parts = location.split(" ");
			if(parts.length != 9){
				System.err.println("Parse Error: " + parts.length + " : " + keyWord + " - " + paragraphWord + " " + location);
			}
			else{
				String entry = paragraphWord + "," + parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + "," + parts[4] + "," + parts[5] + "," + parts[6];
				String strPtr = "";
				strPtr = index.addReference(strPtr, entry, parts[7], parts[8]);
				index.addEntry(keyWord, strPtr);
			}
			sb = new StringBuffer();
		}
		if(localName.equals("word")){
			isKeyWord = false;
			keyWord = sb.toString();
			sb = new StringBuffer();

		}
		if (localName.equals("entry")){
			keyWord = "";
		}
	}

	public void characters(char[] chars, int start, int length) throws SAXException {
		if(isKeyWord){
			sb.append(new String(chars, start, length));
		}
		if(isLocation){
			sb.append(new String(chars, start, length));
		}
	}
	
}
