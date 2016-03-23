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


/**
 * This example shows how to use the sentRep and sentSim functions from another java 
 * program.  The length of phrases used is 1 and the mean distances of 4.0 is used, 
 * targeting the Head level as the best representation of a sentence.  
 * 
 * Two sentences with similar meaning are compared using Roget's sentence distance
 * and a simple method.  Notice the difference in scores
 * 
 * @author Alistair Kennedy
 *
 */

public class UseSentenceSim {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// creates a sentence building object for Roget's and a Simple word distance one
		sentRep.SentenceFactory rogetRep = new sentRep.RogetSentenceFactory("1911");
		sentRep.SentenceFactory simpleRep = new sentRep.SimpleSentenceFactory();
		
		//the sentences POS tagged
		String sentence1 = "Dogs/NNS and/CC cats/NNS make/VBP excellent/JJ pets/NNS";
		String sentence2 = "Canine/NN or/CC feline/JJ animals/NNS make/VBP good/JJ companions/NNS";
		
		//split the sentence into words and then pass it to the two representation builders.
		String[] s1Words = sentence1.split(" ");
		sentRep.Sentence senRogets1 = rogetRep.buildRepresentationVector(s1Words, 1, 5.0);
		sentRep.Sentence senSimple1 = simpleRep.buildRepresentationVector(s1Words, 0); //second parameter does nothing for simple representation
		
		String[] s2Words = sentence2.split(" ");
		sentRep.Sentence senRogets2 = rogetRep.buildRepresentationVector(s2Words, 1, 5.0);
		sentRep.Sentence senSimple2 = simpleRep.buildRepresentationVector(s2Words, 0);
		
		//print out the cosine distance of the re-weighted Roget's diestance and the simple one
		System.out.println("Roget's Distance: " + senRogets1.similarityModified(senRogets2));
		System.out.println("Simple Distance: " + senSimple1.similarityModified(senSimple2));
		
	}

}
