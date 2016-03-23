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

import java.io.*;
import java.util.*;

/**
 * Represents a symbolic pointer to a location where a specific word or phrase
 * can be found in <i>Roget's Thesaurus</i>. A reference is identified by a
 * keyword, head number and part of speech sequence.
 * <p>
 * An example of a Reference is: <i>obstetrics</i> 167 n. This instance of a
 * <TT>Reference</TT> is represented as:
 * <ul>
 * <li><b>Reference name</b>: obstetrics</li>
 * <li><b>Head number</b>: 167</li>
 * <li><b>Part-of-speech</b>: N.</li>
 * </ul>
 * A Reference is always liked to an index entry, for example: <i>stork</i>.
 * 
 * @serial refName
 * @serial pos
 * @serial headNum
 * @serial indexEntry
 * 
 * @author Mario Jarmasz and Alistsair Kennedy
 * @version 1.4 2013
 */

public class Reference implements Serializable {

	private static final long serialVersionUID = 1968676003308407486L;

	// Attributes
	private String refName;

	private String pos;

	private int headNum;

	private String indexEntry;

	/**
	 * Default constructor.
	 */
	public Reference() {
		this.refName = new String();
		this.pos = new String();
		this.headNum = 0;
		this.indexEntry = new String();
	}

	/**
	 * Constructor which sets the reference name, Head number and
	 * part-of-speech.
	 * 
	 * @param name
	 * @param head
	 * @param p
	 */
	public Reference(String name, int head, String p) {
		this.refName = name;
		this.headNum = head;
		this.pos = p;
	}

	/**
	 * Constructor which sets the referebnce name, Head number, part-of-speech,
	 * and Index entry.
	 * 
	 * @param name
	 * @param head
	 * @param p
	 * @param entry
	 */
	public Reference(String name, int head, String p, String entry) {
		this(name, head, p);
		this.indexEntry = entry;
	}

	/**
	 * Constructor that creates a <TT>Reference</TT> object by parsing a
	 * string. An example of a reference that can be parsed is: <CODE>light 417
	 * N.</CODE>. The Index entry is not parsed with the reference.
	 * 
	 * @param ref
	 */
	public Reference(String ref) {
		this();

		StringTokenizer st = new StringTokenizer(ref);
		int phraseLength = st.countTokens() - 2;

		for (int i = 0; i < phraseLength; i++) {
			this.refName += st.nextToken() + " ";
		}
		this.refName = refName.trim();

		Integer head = new Integer(st.nextToken());
		this.headNum = head.intValue();
		this.pos = st.nextToken();
	}

	/**
	 * Converts to a string representation the <TT>Reference</TT> object. The
	 * returned string will be similar to: <BR>
	 * <CODE> word list 87 n. [thesaurus] </CODE><BR>
	 * <CODE> RefName HeadNum POS [IndexEntry] </CODE><BR>
	 * The Index entry is printed only if it has been assigned.
	 */
	public String toString() {
		return (getIndexEntry().equals("") ? getRefName() + " " + getHeadNum()
				+ " " + getPos() : getRefName() + " " + getHeadNum() + " "
				+ getPos() + " [" + getIndexEntry() + "]");
	}

	/**
	 * Prints this Reference to the standard output. The Refernce is printed
	 * similar to: <BR>
	 * <CODE> word list 87 n. </CODE><BR>
	 * <CODE> RefName HeadNum POS </CODE>
	 */
	public void print() {
		System.out.println(getRefName() + " " + getHeadNum() + " " + getPos());
	}

	/**
	 * Returns the name of this Reference.
	 * 
	 * @return reference name
	 */
	public String getRefName() {
		return this.refName;
	}

	/**
	 * Sets the name of this Reference.
	 * 
	 * @param name
	 */
	public void setRefName(String name) {
		this.refName = name;
	}

	/**
	 * Returns the part-of-speech of this Reference.
	 * 
	 * @return POS
	 */
	public String getPos() {
		return this.pos;
	}

	/**
	 * Sets the part-of-speech of this Reference.
	 * 
	 * @param p
	 */
	public void setPos(String p) {
		this.pos = p;
	}

	/**
	 * Returns the Head number of this Reference.
	 * 
	 * @return head number
	 */
	public int getHeadNum() {
		return this.headNum;
	}

	/**
	 * Sets the Head number of this Reference.
	 * 
	 * @param head
	 */
	public void setHeadNum(int head) {
		this.headNum = head;
	}

	/**
	 * Returns the Index entry of this Reference.
	 * 
	 * @return index entry
	 */
	public String getIndexEntry() {
		return this.indexEntry;
	}

	/**
	 * Sets the Index entry of this Reference.
	 * 
	 * @param entry
	 */
	public void setIndexEntry(String entry) {
		this.indexEntry = entry;
	}

}
