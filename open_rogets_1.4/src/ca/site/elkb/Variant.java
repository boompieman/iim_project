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
 * Allows to obtain a variant of an English spelling. A British spelling variant
 * can be obtained form an American spelling and vice-versa.
 * 
 * <p>
 * The default American and British word list is <TT>AmBr.lst</TT> contained
 * in the <TT>$HOME/roget_elkb</TT> directory. It is loaded by the default
 * constructor.
 * </p>
 * 
 * @author Mario Jarmasz and Alistsair Kennedy
 * @version 1.4 2013
 */

public class Variant implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6871983966165029804L;

	/***************************************************************************
	 * Location of user's <TT>Home</TT> directory.
	 **************************************************************************/
	public static final String USER_HOME = System.getProperty("user.home");

	/***************************************************************************
	 * Location of the <i>ELKB</i> data directory.
	 **************************************************************************/
	public static final String ELKB_PATH = System.getProperty("elkb.path",
			USER_HOME + "/roget_elkb");

	/***************************************************************************
	 * Location of the default American and British spelling word list.
	 **************************************************************************/
	// Name of file that contains American to British spelling
	public static final String AMBR_FILE = ELKB_PATH + "/AmBr.lst";

	// only contains one hastable?
	private HashMap<String, String> amBrHash;

	private HashMap<String, String> brAmHash;

	/**
	 * Default constructor.
	 */
	public Variant() {
		amBrHash = new HashMap<String, String>();
		brAmHash = new HashMap<String, String>();
		loadFromFile(AMBR_FILE);
	}

	/**
	 * Constructor that builds the <TT>Variant</TT> object using the
	 * information contained in the specified file. This file must contain only
	 * the American and British spellings in the following format: <BR>
	 * <CODE>American spelling:British spellling</CODE>. </BR> For example:
	 * <BR>
	 * <CODE>airplane:aeroplane</CODE> <BR>
	 * 
	 * @param filename
	 */
	public Variant(String filename) {
		amBrHash = new HashMap<String, String>();
		brAmHash = new HashMap<String, String>();
		loadFromFile(filename);
	}

	/**
	 * Loads an American to British dictionary from a file.
	 * @param filename
	 */
	private void loadFromFile(String filename) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			StringTokenizer st;

			for (;;) {
				String line = new String();
				String american = new String();
				String british = new String();

				line = br.readLine();

				if (line == null) {
					br.close();
					break;
				} else {
					st = new StringTokenizer(line, ":");
					american = st.nextToken();
					british = st.nextToken();
					amBrHash.put(american, british);
					brAmHash.put(british, american);
				}
			}
		} catch (Exception e) {
			// System.out.println(line);
			System.out.println("Error:" + e);
		}
	}

	/**
	 * Returns the British spelling of a word, or <TT>null</TT> if the word
	 * cannot be found.
	 * 
	 * @param american
	 * @return British translation
	 */
	public String amToBr(String american) {
		return amBrHash.get(american);
	}

	/**
	 * Returns the American spelling of a word, or <TT>null</TT> if the word
	 * cannot be found.
	 * 
	 * @param british
	 * @return American translation
	 */
	public String brToAm(String british) {
		return brAmHash.get(british);
	}

}
