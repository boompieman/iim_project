package net.sourceforge.nite.datainspection.calc;
import net.sourceforge.nite.datainspection.data.*;
import net.sourceforge.nite.datainspection.impl.*;

import java.util.*;
import java.io.*;

/**
 * This class ConfusionMatrix has methods for comparing Two Classifications. Both classifications must contain exactly the same items in the same order.
 * <br> encapsulates a square matrix of double with row and column names.
 * <br> Rows and Column names are unique Values that occur in the ordered list of values
 * <br> confusion matrix contains double entries for each pair of Values in the ordered list of class labels (Values)
 * <br> confusion(rowValue,colValue) = the (weighted) count of times that rowValue was confused with colValue
 * <br> a confusion matrix is not nec. symmetrical.
 * <br> kappaCohen - according to Cohen(1966) - see: Di Eugenio in: Comput. Ling. 2004, Vol.30, nr.1
 * @see Classification 
 */


public class ConfusionMatrix  {

private Classification first;
private Classification second;

// the list of values (types, not tokens) that occur in the judgements
private List values;

// the confusion matrix 
private double[][] confusion;

private ValueComparator valueComparator = new ValueComparator();

/**
 * construct a confusion matrix from the given Classifications
 * condition: f and s are equally long and agree on items that have been classified.
 * moreover: items (units) occur in the same order in both classifications
 */
public ConfusionMatrix(Classification f, Classification s){
	first = f;
	second = s;
	makeValueList(f,s);
	
	confusion = new double[numberOfValues()][numberOfValues()];
	for (int i=0;i<numberOfValues();i++)
		for(int j=0;j<numberOfValues();j++)
			confusion[i][j]=0.0;
	fillConfusionMatrix();
}

/**
 * create Confusion Matrix with the given matrix as entries
 * the ordered list of values (class labels) is : 0,1,2,...,matrix.length-1
 */
public ConfusionMatrix(double[][] matrix){
	confusion = matrix;
	setDefaultValues();
}

/**
 * create Confusion Matrix with the given matrix as entries and the given list as class labels
 * assumes ordering of labels in list and matrix columns and rows are equal.
 * @param matrix the entries of the confusion matrix
 * @param values the ordered list of class labels
 */
public ConfusionMatrix(double[][] matrix, List vals){
	confusion = matrix;
	setValues(vals);
}

/**
 *
 * creates an empty confusion matrix of given size 
 * with default values the numbers (0,1,...,size-1) 
 */
public ConfusionMatrix(int size ){
	confusion = new double[size][size];
	for (int i=0;i<size;i++)
		for(int j=0;j<size;j++)
			confusion[i][j]=0.0;
	setDefaultValues();
}


public ConfusionMatrix(List values){
	int size = values.size();
	confusion = new double[size][size];
	for (int i=0;i<size;i++)
		for(int j=0;j<size;j++)
			confusion[i][j]=0.0;
	setValues(values);	
}


/**
 * @return confusion[row][col]
 */
public double entry(int row, int col){
	return confusion[row][col];	
}

/**
 * @return confusion[rowValue][colValue]
 */
public double entry(Value rowValue, Value colValue){
	int row = valueIndex(rowValue);
	int col = valueIndex(colValue);
	if (col==-1||row==-1){
		System.out.println("ConfusionMatrix.entry(Value,Value) - argument not correct Value");
		return -100000.0;
	}
	return confusion[row][col];	
}

/**
 * default values is the list [StringValue("0"),StringValue("1"),...,StringValue(size-1)]
 */
private void setDefaultValues(){
	values = new ArrayList();
	for (int i=0;i<confusion.length;i++)
		values.add(new StringValue(""+i));
}

/**
 * set the list of class labels used
 * the order should be the same as the order of Values in the row and columns of the matrix
 */
public void setValues(List vals){
	this.values = vals;	
}

private void setValuesFromStrings(List strings){
	String str;
	List vals = new ArrayList();
	int len = strings.size();
	for (int i=0;i<len;i++){
		str = (String)strings.get(i);
		vals.add(new StringValue(str));
	}
	
	setValues(vals);
}

/**
 * set confusion[rowValue][colValue] to cv
 */
public void setEntry(Value rowValue, Value colValue, double cv){
	confusion[valueIndex(rowValue)][valueIndex(colValue)]=cv;	
}
public void setEntry(int row, int col, double cv){
	confusion[row][col]=cv;	
}


public void add(Value rowValue, Value colValue){
	confusion[valueIndex(rowValue)][valueIndex(colValue)]++;	
}



// makes the List values that contains all values that occur at least once in either f or s as values
// assigned to some item by one of the judges
private void makeValueList(Classification f, Classification s){
	values	= new ArrayList();
	for (int i=0; i< f.size();i++){
		if (!values.contains(f.getValue(i)))
			values.add(f.getValue(i));
	}
	for (int i=0; i< s.size();i++){
		if (!values.contains(s.getValue(i)))
			values.add(s.getValue(i));
	}
	Collections.sort(values,valueComparator);
}

public int numberOfValues(){
	return values.size();
}


// since the units occur in the same order we can fill the confusion matrix as follows:
private void fillConfusionMatrix(){
	Value cValue,kValue;
	int c,k;
	int nr_units = first.size();
	for (int i=0;i<nr_units;i++){
		cValue = getValue(first.values,i);
		kValue = getValue(second.values,i);
		c =  valueIndex(cValue);
		k =  valueIndex(kValue);
		confusion[c][k]++;	
	}			
}

// return the index of val in the list of values 
// return -1 if val does not occur
private int valueIndex(Value val){
	for (int i=0;i<values.size();i++){
		if (val.equals((Value)values.get(i))) 
			return i;	
	}	
	return -1;
}

/**
 * @return the ordered list of Values
 */
public List getValues(){
	return values;
}


// return n-th Value in list values
public Value getValue(int n){
	return (Value)values.get(n);
}

private Value getValue(List lst,int i){
	return (Value)lst.get(i);
}


public void printMatrix(String filename){
	try{
		PrintWriter pw = new PrintWriter(new FileOutputStream(filename,false),true);
		printMatrix(pw);
		double kappa = kappa();
		pw.println("kappa :"+ kappa);
			
		pw.close();
		System.out.println("ConfusionMatrix printed on :"+ filename);	
	}catch(IOException exc){
		System.out.println("could not open output file");	
	}
	
}
public String toString(){
	String result="";
	int nv = confusion.length;
	String value;
	int len;
    result += rowLabelString(" ")+"\t";
	for (int i=0; i<values.size();i++){
		result += (""+i+"\t");	
	}
	result += ("SUM");	
    for (int c=0; c<nv; c++){
	   value = ((Value)values.get(c)).toString();
	   result += ("\n");
	   result += (rowLabelString(value)+"\t");
	   for (int k=0;k<nv;k++)
		result += (confusion[c][k]+ "\t");
	   result += (totalRow(c));
        }
        result += ("\n----");
        result += ("\n"+rowLabelString("SUM")+"\t"); 
        for (int i=0;i<nv;i++)
        	result += (totalColumn(i)+"\t");
        result += (totalItems()+"\n");
        return result;
}

public void printMatrix(PrintWriter pw){
	if (pw==null) return;
	int nv = confusion.length;
	String value;
	int len;
	printHeader(pw);
	for (int c=0; c<nv; c++){
	   value = ((Value)values.get(c)).toString();
	   pw.println("\n");
	   pw.print(rowLabelString(value)+"\t");
	   for (int k=0;k<nv;k++)
		pw.print(confusion[c][k]+ "\t");
	   pw.print(totalRow(c));
        }
        pw.println("\n----");
        pw.print("\n"+rowLabelString("SUM")+"\t"); 
        for (int i=0;i<nv;i++)
        	pw.print(totalColumn(i)+"\t");
        pw.println(totalItems()+"\n");
}

private void printHeader(PrintWriter pw){
	pw.print(rowLabelString(" ")+"\t");
	for (int i=0; i<values.size();i++){
		pw.print(""+i+"\t");	
	}
	pw.print("SUM");	
}

private String trimToSize(String str, int n){
	int strlen = str.length();
	if (n==strlen) return str;
	if (n<strlen)
		return str.substring(0,n);
	// n > strlen 
	String ending = emptyString(n);
	str = str+ending;
	return str.substring(0,n);
}

// return empty string of length n
private String emptyString(int n){
	char[] value = new char[n];
	System.out.println("n="+n);
	for (int i=0;i<n;i++) value[i]=' ';
	return new String(value,0,n);
}


private int maxValueLength(){
	int rowLabelLength = 0;
	int vlen;
	for (int i=0;i<values.size();i++){
		vlen = ((Value)values.get(i)).toString().length();
		if (vlen  > rowLabelLength )
			rowLabelLength = vlen; 	
	}
	return rowLabelLength;
}

private static final int maxRowLabelLength = 16;	

/**
 * @return str+"               ".substring(0,showStringLength)
 */
private String rowLabelString(String str){
	int n = Math.min(maxValueLength()+2,maxRowLabelLength);
	return trimToSize(str,n);
}

/**
 * print all Values that occur in first or second on SO
 */
public void showValues(){
	for (int i=0; i<values.size();i++)
		System.out.println(values.get(i));	
}

/**
 * print all Values in list values on given PrintWriter
 */
public void printValues(PrintWriter pw){
	for (int i=0; i<values.size();i++)
		pw.println(values.get(i));	
}


/**
 * create and return a ConfusionMatrix from the data in a CSV file
 * format:
 *	labelName1;x;x;x;x;x;x
 *	labelName2;x;x;x;x;x;x
 *	...
 *	labelNameN;x;x;x;x;x;x
 *
 * @param reader
 * @param counter  the number of data columns (size of matrix)
 */
public static ConfusionMatrix readCSVFile(BufferedReader reader, int counter){
		double[][] m = new double[counter][counter];
		StringTokenizer tokens;
		String line;
		String token;
		List label_names = new ArrayList();
		int i=0;
		int j=0;
		try{
		while ((line = reader.readLine())!=null){
			tokens = new StringTokenizer(line,";",false);
			if (tokens.hasMoreTokens()){ 
				token = tokens.nextToken();
				label_names.add(new StringValue(token));
				j=0;
				while (tokens.hasMoreTokens()){
					token=tokens.nextToken();
					m[i][j]=Double.parseDouble(token);
					j++;
				}	
			} 
			i++;	
		}
		}catch(IOException ioexc){}
		ConfusionMatrix result = new ConfusionMatrix(m,label_names);
		return result;
}





/**
 *  kappa is computed using the confusion matrix
 * method: Cohen - see also Di Eugenio and Glass in Comput. Ling. 2004
 */
public double kappa(){
	double agreement_observed = agreement_observed();
	System.out.println("p0 = "+agreement_observed);
	
	double agreement_expected = agreement_expected();
	System.out.println("pe = "+agreement_expected);
	double kappa = (agreement_observed - agreement_expected) / (1.0-agreement_expected);	
	return kappa;
}

private double agreement_observed(){
	double total_agreed = 0.0;
	for (int i=0; i< confusion.length; i++){
		total_agreed += confusion[i][i];
	}
	return total_agreed / totalItems();	
}

private double agreement_expected(){
	double sum = 0.0;
	double total = totalItems();
	for (int i=0; i<confusion.length; i++){
		sum+= (totalRow(i)/total)*(totalColumn(i)/total);
	}
	return sum;
}

// total of all counts in confusion matrix
public double totalItems(){
	double total = 0.0;
	for (int i=0;i<confusion.length;i++)	
		total+=totalRow(i);
	return total;
}


public double totalColumn(int i){
	int size = numberOfValues();
	double sum = 0.0;
	for (int c=0; c<size; c++){
		sum=sum+confusion[c][i];
	}
	return sum;	
}

public double totalRow(int i){
	int size = numberOfValues();
	double sum = 0.0;
	for (int c=0; c<size; c++){
		sum=sum+confusion[i][c];
	}
	return sum;
}

/**
 * size() equals numberOfValues() 
 * @return the number of columns (is number of rows) of this confusion matrix
 */
public int size(){
	return confusion.length;
}


// test
public static void main(String[] args){
	

	String[] items =       {"1","2","3","4","5","6","7","8","9","10"};
	String[] values_meg  = {"0","1","0","0","0","0","0","0","1","0"};	
	String[] values_owen = {"1","1","1","0","0","1","0","0","0","0"};
	Classification meg = new Classification("Meg",items,values_meg);
	Classification owen = new Classification("Owen",items,values_owen);
	ConfusionMatrix matrix = new ConfusionMatrix(meg,owen);
	matrix.printMatrix("confusion-test1.txt"); 
	// correct matrix
	//   5  3     //
	//   1  1     //
	// correct matrix
	/*
	// example from Viera and Garrett Understanding Interobserver Agreement: the Kappa Statistic. In: Family Medicine (May 2005)
	double[][] matrix2 = new double[2][2];
	matrix2[0][0]=1.0;
	matrix2[0][1]=6.0;
	matrix2[1][0]=9.0;
	matrix2[1][1]=84.0;
	ConfusionMatrix cf = new ConfusionMatrix(matrix2);
	System.out.println("kappa Cohen :"+ cf.kappa());
	cf.printMatrix("confusion-test2.txt");
	AgreementMatrix agm = new AgreementMatrix(cf);
	System.out.println("kappa Siegel & Castellan :"+ agm.kappa());
	CoincidenceMatrix coinm = new CoincidenceMatrix(cf);
	System.out.println("kappa Krippendorff :"+ coinm.kappaKrippendorff());
	
	// examples from Di Eugenio and Glass: "Kappa: A Second Look" (Comput.Ling. Vol.30; Nr.1; pp.95-101, 2004)
	matrix2 = new double[2][2];
	matrix2[0][0]=70.0;
	matrix2[0][1]=25.0;
	matrix2[1][0]=0.0;
	matrix2[1][1]=55.0;
	cf = new ConfusionMatrix(matrix2);
	cf.showValues();
	cf.printMatrix("confusion-test3.txt");
	
	System.out.println("kappa Cohen :"+ cf.kappa());
	agm = new AgreementMatrix(cf);
	System.out.println("kappa Siegel & Castellan :"+ agm.kappa());
	coinm = new CoincidenceMatrix(cf);
	System.out.println("kappa Krippendorff :"+ coinm.kappaKrippendorff());
	
	matrix2 = new double[2][2];
	matrix2[0][0]=70.0;
	matrix2[0][1]=15.0;
	matrix2[1][0]=10.0;
	matrix2[1][1]=55.0;
	cf = new ConfusionMatrix(matrix2);
	cf.showValues();
	cf.printMatrix("confusion-test4.txt");
	
	System.out.println("kappa Cohen :"+ cf.kappa());
	agm = new AgreementMatrix(cf);
	System.out.println("kappa Siegel & Castellan :"+ agm.kappa());
	coinm = new CoincidenceMatrix(cf);
	System.out.println("kappa Krippendorff :"+ coinm.kappaKrippendorff());
	
	matrix2 = new double[4][4];
	matrix2[0][0]=64.0;
	matrix2[0][1]=3.0;
	matrix2[0][2]=1.0;
	matrix2[0][3]=4.0;
	matrix2[1][0]=7.0;
	matrix2[1][1]=3.0;
	matrix2[1][3]=2.0;
	matrix2[2][0]=5.0;
	matrix2[2][2]=2.0;
	matrix2[3][0]=1.0;
	
	cf = new ConfusionMatrix(matrix2);
	cf.showValues();
	cf.printMatrix("confusion-test4.txt");
	
	System.out.println("kappa Cohen :"+ cf.kappa());
*/
	
	// read from file
	try{
		BufferedReader reader = new BufferedReader(new FileReader("wxu_nynke2_confusion.txt"));
		ConfusionMatrix mr = readCSVFile(reader,12);
		mr.printMatrix("confusion_wxu_nynke2.txt");
		double kap = mr.kappa();
		System.out.println("kappa : "+kap);
	}catch(IOException iox){}
	
	
}

private class ValueComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			Value val1 = (Value)o1;
			Value val2 = (Value)o2;
			return (val1.toString()).compareTo(val2.toString());
		}
		
		public boolean equals(Object obj) {
			return super.equals(obj);
		}
	}

}
