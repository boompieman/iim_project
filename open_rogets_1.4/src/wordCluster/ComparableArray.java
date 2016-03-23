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

package wordCluster;
import java.util.ArrayList;
import java.util.Collection;

import ca.site.elkb.HeadInfo;


/*******************************************************************************
 * ComparableArray: an ArrayList than can be compared according to its size
 * 
 * Author : Mario Jarmasz Created: October, 2003
 * 
 * @author Mario Jarmasz and Alistsair Kennedy
 * @version 1.2 Nov 2008
 ******************************************************************************/

public class ComparableArray<T> extends ArrayList<T> implements Comparable<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8044048522806367644L;
	
	private HeadInfo headInfo;

	/**
	 * Constructor
	 */
	public ComparableArray() {
		super();
		headInfo = new HeadInfo();
	}

	/**
	 * Constructor passed a Collection
	 * 
	 * @param c
	 */
	public ComparableArray(Collection<T> c) {
		super(c);
		headInfo = new HeadInfo();
	}

	/**
	 * Compares two ArrayList. They are equal is they contain the same elements
	 * and are of the same size
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(Object other) {
		int result;
		ComparableArray<T> otherArray = (ComparableArray<T>) other;
		result = size() - otherArray.size();
		HeadInfo otherHI = otherArray.getHeadInfo();
		result = otherHI.getClassNum() - headInfo.getClassNum();

		// if they're the same size, they may not be equal!
		if (result == 0) {
			if (equals(otherArray) == true) {
				result = 0;
			} else {
				// same size, different elements, the first will
				// be bigger
				result = 1;
			}
		}
		return result;
	}
	
	/**
	 * set HeadInfo
	 * 
	 * @param hi
	 */
	public void setHeadInfo(HeadInfo hi){
		headInfo = hi;
	}
	
	/**
	 * get HeadInfo
	 * @return headInfo stored in the Comparable Array
	 */
	public HeadInfo getHeadInfo(){
		return headInfo;
	}

}
