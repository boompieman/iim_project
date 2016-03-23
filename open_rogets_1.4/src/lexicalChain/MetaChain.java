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

package lexicalChain;

/**
 * MetaChain class: represents a MetaChain used to build LexicalChains
 * A MetaChain is defined by:
 *       + chainHead: the word for which the chain is being built
 *       + startLine: the line at which the chain starts
 *       + senseNo  : the Head number of the head of the chain [unique sense]
 *       + score    : the score of the MetaChain (cf. Jarmasz, 2003)
 *       + words    : array of words that are in the chain
 *       + rels     : array of relations that link the words to the chain
 *       + lines    : array of line numbers of words in the chain
 *
 * This class implements the comparable interface.
 * If two chains have the same score, the one with the smallest
 * line number is the greatest.
 *
 * @author Mario Jarmasz and Alistair Kennedy
 * @version 1.4 2013
 **/

import java.util.*;

public class MetaChain implements Comparable<Object> {

	/******************************************
	 * Iterator (inner clas)
	 *****************************************/

	private class ChainIter implements Iterator<Object> {
		private Iterator<String> wordsIter;
		private Iterator<String> relsIter;
		private Iterator<Integer> linesIter;

		/**
		 * Constructor for Chain Iterator
		 */
		private ChainIter() {
			wordsIter = words.iterator();
			relsIter = rels.iterator();
			linesIter = lines.iterator();
		}

		/**
		 * Gets next object.
		 */
		public Object next() {
			if (!hasNext())
				throw new NoSuchElementException();
			relsIter.next();
			linesIter.next();
			return wordsIter.next();
		}

		/**
		 * Checks to see if there is a next object.
		 */
		public boolean hasNext() {
			return wordsIter.hasNext();
		}

		/**
		 * Removes the current item from the MetaChain
		 */
		public void remove() {
			wordsIter.remove();
			relsIter.remove();
			linesIter.remove();
		}
	}

	/******************************************
	 * Constants
	 *****************************************/
	public static final double T0_WEIGHT = 1.00;
	public static final double T1_WEIGHT_NEAR = 1.00;
	public static final double T1_WEIGHT_VICINAL = 0.75;
	public static final double T1_WEIGHT_FAR = 0.50;

	/******************************************
	 * Attributes
	 *****************************************/
	private String chainHead;
	private int startLine;
	private int senseNo;
	private ArrayList<String> words;
	private ArrayList<String> rels;
	private ArrayList<Integer> lines;

	/**
	 * Constructs a new meta chain
	 */
	public MetaChain() {
		chainHead = new String();
		startLine = 0;
		senseNo = 0;
		words = new ArrayList<String>();
		rels = new ArrayList<String>();
		lines = new ArrayList<Integer>();
	}

	/**
	 * Constructs a meta chain with a given head, start line number and sense number
	 * 
	 * @param head
	 * @param start
	 * @param sense
	 */
	public MetaChain(String head, int start, int sense) {
		this();
		chainHead = head;
		startLine = start;
		senseNo = sense;
	}

	/**
	 * add Add a word, relation, line number triple to MetaChain
	 * 
	 * @param word
	 * @param rel
	 * @param line
	 */
	public void add(String word, String rel, Integer line) {
		words.add(word);
		rels.add(rel);
		lines.add(line);
	}

	/**
	 * remove Remove a word, relation, line number triple to MetaChain
	 * 
	 * @param index
	 * @throws IndexOutOfBoundsException
	 */
	public void remove(int index) throws IndexOutOfBoundsException {
		words.remove(index);
		rels.remove(index);
		lines.remove(index);
	}

	/**
	 * Gets chain head
	 * 
	 * @return chain head
	 */
	public String getChainHead() {
		return chainHead;
	}

	/**
	 * Gets start line
	 * 
	 * @return starting line
	 */
	public int getStartLine() {
		return startLine;
	}

	/**
	 * Gets sense number
	 * 
	 * @return sense number
	 */
	public int getSenseNumber() {
		return senseNo;
	}

	/**
	 * returns a new Chain Iterator
	 * 
	 * @return iterator
	 */
	public Iterator<Object> iterator() {
		return new ChainIter();
	}

	/**
	 * getScore Will be more elaborate in the future.  Currently it
	 * just calculates and returns the Meta Chain's score.
	 */
	public double getScore() {
		double score = 0;
		double delta = 0;
		int prevLineNo = 0;
		int curLineNo = 0;
		String strRel;

		Iterator<String> iterRel = rels.iterator();
		Iterator<Integer> iterLineNo = lines.iterator();

		prevLineNo = startLine;

		// both arrays are the same size, therefore only one check
		// should be enough
		while (iterRel.hasNext()) {
			strRel = iterRel.next();
			curLineNo = iterLineNo.next().intValue();

			if (strRel.equals("T0")) {
				score += T0_WEIGHT;
			} else if (strRel.equals("T1")) {
				delta = curLineNo - prevLineNo;

				if ((delta >= 0) && (delta <= 2)) {
					score += T1_WEIGHT_NEAR;
				} else if ((delta >= 3) && (delta <= 5)) {
					score += T1_WEIGHT_VICINAL;
				} else {
					score += T1_WEIGHT_FAR;
				}

			}

			prevLineNo = curLineNo;
		} // end while

		return score;
	}

	/**
	 * toString String representation of MetaChain: chainHead, words [score: ,
	 * sense:, line: ] example: regard, events, reference, event, respect
	 * [score: 5, sense: 10, line: 4]
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();

		Iterator<String> iter = words.iterator();
		if (iter.hasNext())
			result.append(iter.next());

		while (iter.hasNext()) {
			result.append(", ");
			result.append(iter.next());
		}

		result.append(" [score: ");
		result.append(getScore());
		result.append(", sense: ");
		result.append(senseNo);
		result.append(", line: ");
		result.append(startLine);
		result.append("]");

		return result.toString();
	}

	/**
	 * compareTo Comparison is done according to the score If two chains have
	 * the same score, then the line number is used to break the tie, the
	 * smaller the line number, the greater the score
	 */
	public int compareTo(Object obj) {
		double result;
		MetaChain other = (MetaChain) obj;

		result = (other.getScore() * 100) - (this.getScore() * 100);
		if (result == 0) {
			result = this.startLine - other.startLine;
			// if result == 0 it means that two chains are identical
			// for this to be true, all of the elements would have
			// to be the same
			if (result == 0) {
				result = -1;
			}
		}

		Double dblRes = new Double(result);
		return dblRes.intValue();
	}

	// toStringVerbose() - same but in more detail
}