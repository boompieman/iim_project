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

package samples;
import java.util.ArrayList;


/**
 * The index allows you to find locations of a word in Roget's Thesaurus.  References to the
 * location of a word can come in two forms.  One is a string indicating the paragraph name
 * followed by head number e.g. <i>relation #9</i>.
 * 
 * The other option is to get references as an integer array containing 9 numbers: The class number,
 * section number, subsection number, headgroup number, head number, POS number, Paragraph number,
 * semicolon group number and word number.  This methos is generally much faster since the precise 
 * location of a word can be found without having to search through the head file using the <i>text</i>
 * class.
 * 
 * @author Alistair Kennedy
 *
 */
public class UseIndex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//load ELKB
		ca.site.elkb.RogetELKB elkb = new ca.site.elkb.RogetELKB("1911X5");
		System.out.println("Search results for animal");
		
		//string references
		ArrayList<String> al1 = elkb.index.getEntryList("animal");
		System.out.println("text references");
		System.out.println(al1);
		
		//numberical references
		ArrayList<int[]> al2 = elkb.index.getEntryListNumerical("animal");
		System.out.println("numerical refereces");
		for(int[] array : al2){
			for(int i : array){
				System.out.print(i + " ");
			}
			System.out.println();
		}
	}
}
