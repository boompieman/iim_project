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

package sentRep;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

import sentRep.SentenceFactory.Resource;

public class Sentence {
	
	private Hashtable<String, Pair<Double, Double>> sentence;
	
	private Resource resource;
	
	/**
	 * Constructor creates an empty sentence of a given resource type.
	 * 
	 * @param sentenceType
	 */
	public Sentence(Resource sentenceType){
		sentence = new Hashtable<String, Pair<Double, Double>>();
		resource = sentenceType;
	}
	
	/**
	 * Creates a sentence out of a hashtable with a string and number pair.  A resource type
	 * is also provided.
	 * 
	 * @param representation
	 * @param sentenceType
	 */
	public Sentence(Hashtable<String, Pair<Double, Double>> representation, Resource sentenceType){
		sentence = representation;
		resource = sentenceType;
	}
	
	/**
	 * Adds a feature to the sentence
	 * 
	 * @param key
	 * @param val
	 * @param normalizedVal
	 */
	public void addFeature(String key, double val, double normalizedVal){
		Pair<Double, Double> p;
		double oldVal = 0;
		double oldNormalVal = 0;
		if(containsKey(key)){
			oldVal = getOriginal(key);
			oldNormalVal = getModified(key);
		}
		double newVal = oldVal + val;
		double newNormalVal = oldNormalVal + normalizedVal;
		p = new Pair<Double, Double>();
		p.setModified(newNormalVal);
		p.setOriginal(newVal);
		sentence.put(key, p);
	}
	
	public void deleteFeature(String key){
		sentence.remove(key);
	}
	
	/**
	 * Applies TF.IDF to all the words in the resource.  This version provides normalized
	 * TF.
	 * 
	 * @param docCount
	 * @param totalDocs
	 * @param totalWords
	 */
	public void applyTF_IDF(Hashtable<String, Integer> docCount, int totalDocs, int totalWords) {
		for(String key : keySet()){  //calculate tf idf for sentences
			double idf = Math.log((double)totalDocs/(double)docCount.get(key));
			double tf = getOriginal(key)/(double)totalWords;
			Pair<Double, Double> p = new Pair<Double, Double>();
			p.setModified(tf*idf);
			p.setOriginal(getOriginal(key));
			sentence.put(key, p);
		}
	}
	
	/**
	 * Applies TF.IDF to the sentence, this time with normalized TF.
	 * 
	 * @param docCount
	 * @param totalDocs
	 */
	public void applyTF_IDF(Hashtable<String, Integer> docCount, int totalDocs) {
		applyTF_IDF(docCount, totalDocs, 1);
	}
	
	/**
	 * Checks to see if sentence contains a word.
	 * 
	 * @param key
	 * @return true, if word is found, false otherwise.
	 */
	public boolean containsKey(String key){
		return sentence.containsKey(key);
	}
	
	/**
	 * Gets the modified value for a key word.
	 * 
	 * @param key
	 * @return double value
	 */
	public double getModified(String key){
		return sentence.get(key).getModified();
	}
	
	/**
	 * Gets original value for a key word.
	 * 
	 * @param key
	 * @return double value
	 */
	public double getOriginal(String key){
		return sentence.get(key).getOriginal();
	}
	
	/**
	 * Gets the key set as a set of strings.
	 * 
	 * @return key set
	 */
	public Set<String> keySet(){
		return sentence.keySet();
	}
	
	/**
	 * Gets the resource type.
	 * 
	 * @return Resource enumeration value
	 */
	public Resource getResourceType(){
		return resource;
	}
	
	/**
	 * computes cosine similarity between two hashtables v1 and v2
	 * weights of items are reweighted based on the normal distribution.
	 * 
	 * @param target
	 * @return similarity score
	 */
	public double similarityModified(Sentence target){
		double dotProduct = 0.0;
		double v1DotProduct = 0.0;
		double v2DotProduct = 0.0;
		
		for(String key : keySet()){
			Double d1 = getModified(key);
			v1DotProduct += d1.doubleValue()*d1.doubleValue();
			if(target.containsKey(key)){
				Double d2 = target.getModified(key);
				dotProduct += d1.doubleValue()*d2.doubleValue();
			}
		}
		for(String key : target.keySet()){
			Double d2 = target.getModified(key);
			v2DotProduct += d2.doubleValue()*d2.doubleValue();
		}
		if(v1DotProduct == 0 || v2DotProduct == 0){
			return 0;
		}
		return dotProduct/(Math.sqrt(v1DotProduct)*Math.sqrt(v2DotProduct));
	}
	
	/**
	 * computes cosine similarity between two hashtables v1 and v2
	 * weights of items are not re-weighted.
	 * 
	 * @param target
	 * @return similarity score
	 */
	public double similarityOriginal(Sentence target){
		double dotProduct = 0.0;
		double v1DotProduct = 0.0;
		double v2DotProduct = 0.0;
		
		for(String key : keySet()){
			Double d1 = getOriginal(key);
			v1DotProduct += d1.doubleValue()*d1.doubleValue();
			if(target.containsKey(key)){
				Double d2 = target.getOriginal(key);
				dotProduct += d1.doubleValue()*d2.doubleValue();
			}
		}
		for(String key : target.keySet()){
			Double d2 = target.getOriginal(key);
			v2DotProduct += d2.doubleValue()*d2.doubleValue();
		}
		if(v1DotProduct == 0 || v2DotProduct == 0){
			return 0;
		}
		return dotProduct/(Math.sqrt(v1DotProduct)*Math.sqrt(v2DotProduct));
	}
	
	/**
	 * Generates and prints out the features for the weka ML
	 * algorithms to train/test on.
	 * 
	 * @param sen
	 */
	public double[] getFeatureVector(Sentence sen){
		double inBoth = 0.0;
		double onlyA = 0.0;
		double extendedByA = 0.0;
		double onlyB = 0.0;
		double extendedByB = 0.0;
		double weightA = 0.0;
		double weightB = 0.0;
		
		for(String key : keySet()){
			if(sen.containsKey(key)){
				Double dA = getOriginal(key);
				weightA += dA.doubleValue();
				Double dB = sen.getOriginal(key);
				inBoth += Math.min(dA.doubleValue(), dB.doubleValue());
				if(dA.doubleValue() > dB.doubleValue()){
					extendedByA += dA.doubleValue() - dB.doubleValue();
				}
			}
			else{
				Double dA = getOriginal(key);
				onlyA += dA.doubleValue();
				weightA += dA.doubleValue();
			}
		}

		for(String key : sen.keySet()){
			if(containsKey(key)){
				Double dA = getOriginal(key);
				Double dB = sen.getOriginal(key);
				weightB += dB.doubleValue();
				if(dB.doubleValue() > dA.doubleValue()){
					extendedByB += dB.doubleValue() - dA.doubleValue();
				}
			}
			else{
				Double dB = sen.getOriginal(key);
				onlyB += dB.doubleValue();
				weightB += dB.doubleValue();
			}
		}
		double[] toReturn = new double[8];
		toReturn[0] = similarityOriginal(sen);
		toReturn[1] = (inBoth/weightA);
		toReturn[2] = (onlyA/weightA);
		toReturn[3] = (extendedByA/weightA);
		toReturn[4] = (inBoth/weightB);
		toReturn[5] = (onlyB/weightB);
		toReturn[6] = (extendedByB/weightB);
		toReturn[7] = 0.0;
		//System.out.println(similarityUnweighted(sen1, sen2) + "," + (inBoth/weightA) + "," + (onlyA/weightA) + "," + (extendedByA/weightA) + "," + (inBoth/weightB) + "," + (onlyB/weightB) + "," + (extendedByB/weightB) + ",'" + value + "'");
		return toReturn;
	}
	
	public void printFeatureVector(Sentence sen, String value){
		double[] features = getFeatureVector(sen);
		System.out.println(features[0] + "," + features[1] + "," + features[2] + "," + features[3] + "," + features[4] + "," + features[5] + "," + features[6] + ",'" + value + "'");		
	}
	
	
	/**
	 * Generates and prints out the features for the weka ML
	 * algorithms to train/test on.
	 * 
	 * @param sen
	 * @param value
	 * @param keys
	 * @param bw
	 * @throws IOException 
	 */
	public void printFeatureVector(Sentence sen, String value, String[] keys, BufferedWriter bw){
		double[] features = getFeatureVector(sen);
		try {
			bw.write(features[0] + "," + features[1] + "," + features[2] + "," + features[3] + "," + features[4] + "," + features[5] + "," + features[6] + ",'" + value + "'\n");
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * prints out header information for weka ML algorithms.
	 *
	 */
	public static void printHeader() {
		System.out.println("@relation 'paraphrase'");
		System.out.println("@attribute 'cosine' real");
		System.out.println("@attribute 'inBothA' real");
		System.out.println("@attribute 'onlyA' real");
		System.out.println("@attribute 'extendedByA' real");
		System.out.println("@attribute 'inBothB' real");
		System.out.println("@attribute 'onlyB' real");
		System.out.println("@attribute 'extendedByB' real");
		System.out.println("@attribute 'class' {'1','0'}");
		System.out.println();
		System.out.println("@data");
	}
	
	/**
	 * Prints out header information, this time printing to a buffered writer.
	 * 
	 * @param max
	 * @param bw
	 */
	public static void printHeader(int max, BufferedWriter bw) {
		try{
			bw.write("@relation 'paraphrase'\n");
			bw.write("@attribute 'cosine' real\n");
			bw.write("@attribute 'inBothA' real\n");
			bw.write("@attribute 'onlyA' real\n");
			bw.write("@attribute 'extendedByA' real\n");
			bw.write("@attribute 'inBothB' real\n");
			bw.write("@attribute 'onlyB' real\n");
			bw.write("@attribute 'extendedByB' real\n");
			for(int i = 1; i <= max; i++){
				bw.write("@attribute 'variance"+i+"' real\n");
			}
			bw.write("@attribute 'class' {'1','0'}\n");
			bw.write("\n");
			bw.write("@data\n");

			bw.flush();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
