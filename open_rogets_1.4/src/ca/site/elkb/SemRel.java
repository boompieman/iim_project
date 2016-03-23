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

/**
 * Represents a <i>Roget's Thesaurus</i> relation between a word or phrase.
 * This can be a Cross-reference or a See reference. For example:
 * <ul>
 * <li>See <i>drug taking</i></li>
 * <li>646 <i>perfect</i></li>
 * </ul>
 * Relation types currently used by the <i>ELKB</i> are <TT>cref</TT> and
 * <TT>see</TT>.
 * 
 * @author Mario Jarmasz and Alistsair Kennedy
 * @version 1.4 2013
 */

public class SemRel extends Reference {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5128354271966119550L;
	// Attributes
	private String type;

	/**
	 * Default constructor.
	 */
	public SemRel() {
		super();
		type = new String();
	}

	/**
	 * Constructor which sets the relation type, Head number and Reference name.
	 * 
	 * @param t
	 * @param headNum
	 * @param refName
	 */
	public SemRel(String t, int headNum, String refName) {
		this();
		type = t;
		setHeadNum(headNum);
		setRefName(refName);
	}

	/**
	 * Constructor which sets the relation type, Head number and Reference name.
	 * 
	 * @param t
	 * @param headNum
	 * @param refName
	 * @param pos
	 * @param paraNum
	 * @param sgNum
	 */
	public SemRel(String t, int headNum, String refName, String pos, int paraNum, int sgNum) {
		this(t, headNum, refName);
		setPos(pos);
		// setParaNum(paraNum);
		// setSgNum(sgNum);
	}

	/**
	 * Returns the relation type.
	 * 
	 * @return returns type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the relation type.
	 * 
	 * @param t
	 */
	public void setType(String t) {
		type = t;
	}

	/**
	 * Converts to a string representation the <TT>SemRel</TT> object.
	 */
	public String toString() {
		String info = new String();
		info = "SemRel" + "@" + Integer.toHexString(hashCode());
		info += "@" + getType();
		info += "@" + getHeadNum() + "@" + getRefName();
		// info += getPos() + "@" + getParaNum() + "@" + getSgNum();
		return info;
	}

	/**
	 * Prints this relation to the standard output.
	 */
	public void print() {
		String info = new String();
		info = getType() + ": " + getHeadNum() + " " + getRefName();
		System.out.println(info);
	}

}
