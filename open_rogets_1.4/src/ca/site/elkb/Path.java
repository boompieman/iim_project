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
 * Represents a path in <i>Roget's Thesaurus</i> between two words or phrases. 
 * This is mostly used in an old version of SemDist and may not be too useful.
 * 
 * @author Mario Jarmasz and Alistsair Kennedy
 * @version 1.4 2013
 */

// This class needs work :-(
// Described as very sloppy!
public class Path implements Comparable<Object> {
	private ArrayList<String> rtPath;

	/**
	 * Default constructor.
	 */
	public Path() {
		rtPath = new ArrayList<String>();
	}

	/**
	 * Constructor that initialized this <TT>Path</TT> object with a Path.
	 * 
	 * @param path
	 */
	public Path(ArrayList<String> path) {
		rtPath = path;
	}

	/**
	 * Returns the number of elements in this Path.
	 * 
	 * @return length of path
	 */
	public int length() {
		if (rtPath.isEmpty() == false) {
			// must consider keywords and pathInfo
			return rtPath.size() - 8;
		} else {
			return 0;
		}
	}

	/**
	 * Returns the length in this Path. Size is length - 1.
	 * 
	 * @return length of path - 1
	 */
	public int size() {
		return length() - 1;
	}

	/**
	 * Compares two paths. They are first compared according to their length. If
	 * they are still equal, they are then sorted according to keywords.
	 */
	public int compareTo(Object other) {
		int result;
		Path otherPath = (Path) other;
		result = length() - otherPath.length();

		if (result == 0) {
			result = getKeyWord1().compareTo(otherPath.getKeyWord1());
			if (result == 0) {
				result = getKeyWord2().compareTo(otherPath.getKeyWord2());
			}
		}

		return result;
	}

	/**
	 * Returns the keyword of the the first word or phrase in this Path.
	 * 
	 * @return key word 1
	 */
	public String getKeyWord1() {
		String sKeyWord = new String();
		if (rtPath.isEmpty() == false) {
			sKeyWord = rtPath.get(0);
		}
		return sKeyWord;
	}

	/**
	 * Returns the part-of-speech of the the first word or phrase in this Path.
	 * 
	 * @return POS of word 1
	 */
	public String getPos1() {
		return (rtPath.isEmpty() == false ? rtPath.get(1) : "");
	}

	/**
	 * Returns the keyword of the the second word or phrase in this Path.
	 * 
	 * @return key word 2
	 */
	public String getKeyWord2() {
		String sKeyWord = new String();
		if (rtPath.isEmpty() == false) {
			sKeyWord = rtPath.get(2);
		}
		return sKeyWord;
	}

	/**
	 * Returns the part-of-speech of the the second word or phrase in this Path.
	 * 
	 * @return POS of word 2
	 */
	public String getPos2() {
		return (rtPath.isEmpty() == false ? rtPath.get(3) : "");
	}

	/**
	 * Returns the location in the ontology of the first word or phrase in this
	 * Path.
	 * 
	 * @return path info 1
	 */
	public String getPathInfo1() {
		String sPathInfo = new String();
		if (rtPath.isEmpty() == false) {
			sPathInfo = rtPath.get(4);
		}
		return sPathInfo;
	}

	/**
	 * Returns the location in the ontology of the second word or phrase in this
	 * Path.
	 * 
	 * @return path info 2
	 */
	public String getPathInfo2() {
		String sPathInfo = new String();
		if (rtPath.isEmpty() == false) {
			sPathInfo = rtPath.get(5);
		}
		return sPathInfo;
	}

	/**
	 * Returns the first word or phrase in this Path.
	 * 
	 * @return word 1
	 */
	public String getWord1() {
		String sWord1 = new String();
		if (rtPath.isEmpty() == false) {
			sWord1 = rtPath.get(6);
		}
		return sWord1;
	}

	/**
	 * Returns the second word or phrase in this Path.
	 * 
	 * @return word 2
	 */
	public String getWord2() {
		String sWord2 = new String();
		if (rtPath.isEmpty() == false) {
			sWord2 = rtPath.get(7);
		}
		return sWord2;
	}

	/**
	 * Returns the path between the first and second word or phrase.
	 * 
	 * @return path
	 */
	public String getPath() {
		String sPath = new String();

		if (rtPath.isEmpty() == false) {
			Iterator<String> iter = rtPath.iterator();
			int iTotal = length();
			int iMiddle = iTotal / 2;

			// drop the keyWord and pathInfo
			for (int i = 0; i < 8; i++) {
				iter.next();
			}

			for (int i = 0; i < iMiddle; i++) {
				sPath += iter.next() + " --> ";
			}

			sPath += iter.next();

			// Paths are symmetric, thus we can repeat the procedure
			for (int i = 0; i < iMiddle; i++) {
				sPath += " <-- " + iter.next();
			}
		}

		return sPath;
	}

	/**
	 * Converts to a string representation the <TT>Path</TT> object.
	 */
	public String toString() {
		String info = new String();
		info += "Path between " + getKeyWord1() + " and " + getKeyWord2();
		info += " (length = " + (length() - 1) + ")\n";
		info += getPathInfo1() + "\n";
		info += getPathInfo2() + "\n";
		info += getPath();
		return info;
	}

}
