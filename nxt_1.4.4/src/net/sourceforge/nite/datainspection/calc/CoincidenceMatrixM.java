package net.sourceforge.nite.datainspection.calc;
import net.sourceforge.nite.datainspection.data.*;
import net.sourceforge.nite.datainspection.impl.*;


import java.util.*;
import java.io.*;

/**

DR: DOCUMENTATION IS WRONG! THIS CLASS IS FOR MULTIPLE ANNOTATORS!
note: this class considers string values. the UNDEF or MISSING VALUE is therefore also specified as a String. We may want to change this.

 * CoincidenceMatrixM has methods for comparing Two Classifications
 * <br> The coincidence matrix accounts for Values (labels) 
 * <br> coincidence_matrix[Val1][Val2] is the number of times that one classification labeled some item Val1 the other Val2
 * <br> coincidence_matrix is symmetrical along the main-diagonal.
 * <br> the total over all entry numbers is 2*N, where N is the number of items judged (2*N values are given).
 *
 * <br> This class contains a method for computing kappa (a type of distance measure between classifications)
 * <br> This distance uses a distance metric on the type of Values(class labels) assigned to the items(units).
 * <br> Usually the Boolean Metric is used: distance = 0 iff values are equal otherwise it is 1.
 * <br> If you think that some labels are more equal than others you may use a weighted kappa, that uses your
 * <br> own DistanceMetric 
 *
 * <br> kappaKrippendorf - returns the same value as alphaNominal with the standard BooleanMetric.
 * <br> alphaNominal(DistanceMetric) - according to Krippendorff  - requires a DistanceMetric defined on the values of the classification
 * <br> kappaCohen is computed using the confusion matrix (this kappa may differ from kappa Krippendorff)
 * <br> The implementation is based on "Computing Krippendorff's Alpha-Reliability"
 *
 * @see Classification 
 * @see DistanceMetric
 */


public class CoincidenceMatrixM {

private DistanceMetric metric;
// the list of values that occur in the judgements
private List values;
// the coincidence matrix
private double[][] coinm;
//
List classifications;

/**
 * @param f the first Classification
 * @param s the second Classification
 * required: f and s are classifications of the same ordered list of items/units classified
 * the constructor computes the coincidence matrix
 */
public CoincidenceMatrixM(List cls){
	this.classifications = cls;
	makeValueList(classifications);
	coinm = new double[numberOfValues()][numberOfValues()];
	for (int i=0;i<numberOfValues();i++)
		for(int j=0;j<numberOfValues();j++)
			coinm[i][j]=0.0;
	fillMatrix();
}

private Value undefValue;

/**
 * Use this constructor when the Classifications contain "MISSING CASES" or a special value to be interpreted as "UNDEF"
 * @param undef the String that specifys the StringValue that is considered as the Value that stands for "UNDEFINED" (or "MISSING VALUE")
 */
public CoincidenceMatrixM(List cls, Value undef){
	undefValue = undef;
	this.classifications = cls;
	makeValueListUndef(classifications);
	coinm = new double[numberOfValues()][numberOfValues()];
	for (int i=0;i<numberOfValues();i++)
		for(int j=0;j<numberOfValues();j++)
			coinm[i][j]=0.0;
	fillMatrixUndef();
}

/**
 *
 * creates an empty coincidence matrix of given size 
 * with default values the numbers (0,1,...,size-1) 
 */
public CoincidenceMatrixM(int size ){
	coinm = new double[size][size];
	for (int i=0;i<size;i++)
		for(int j=0;j<size;j++)
			coinm[i][j]=0.0;
	setDefaultValues();
}



/**
 *
 * creates a coincidence matrix with given contents and size equal m.length 
 * and with default values the numbers (0,1,...,size-1) 
 */
public CoincidenceMatrixM(double[][] m ){
	coinm = m;
	setDefaultValues();
}

/**
 * @return the size of this matrix (equals the number of rows, equals the number of columns, equals the nr of Values in the values list)
 */
public int size(){
	return coinm.length;
}


/**
 * makes a copy of this matrix without the row and column of the given index Value
 * if Value does not occur in the values list then this matrix self is returned (not a copy!)
 * @return a new CoincidenceMatrixM constructed from this one by removing the given Value entries
 */
public CoincidenceMatrixM remove(Value val){
	int indexRemovedValue = this.valueIndex(val);
	if (indexRemovedValue==-1){
		System.out.println("CoincidenceMatrixM remove: value: "+ val + "does not occur");
		 return this;
	}
	int newsize = this.size()-1;
	CoincidenceMatrixM result = new CoincidenceMatrixM(newsize);
	
	List newvalues = new ArrayList();
	for (int i=0;i<size();i++){
		if (i!=indexRemovedValue) 
			newvalues.add(values.get(i));	
	}
	result.setValues(newvalues);
	// fill the new matrix
	for (int i=0;i<indexRemovedValue;i++){
		for (int j=0;j<indexRemovedValue;j++){
			result.setEntry(i,j,this.entry(i,j));
		}	
	}
	for (int i=indexRemovedValue;i<size()-1;i++){
		for (int j=indexRemovedValue;j<size()-1;j++){
			result.setEntry(i,j,this.entry(i+1,j+1));
		}	
	}
	return result;
}


private void setDefaultValues(){
	values = new ArrayList();
	for (int i=0;i<coinm.length;i++)
		values.add(new StringValue(""+i));
}

/**
 * set the list of class labels used
 * the order should be the same as the order of Values in the row and columns of the matrix
 */
public void setValues(List vals){
	this.values = vals;	
}

/**
 * @return coinm[row][col]
 */
public double entry(int row, int col){
	return coinm[row][col];	
}

/**
 * @return coinm[Value r][Value c]
 */
public double entry(Value rowValue, Value colValue){
	int row = valueIndex(rowValue);
	int col = valueIndex(colValue);
	if (col==-1||row==-1){
		System.out.println("ConfusionMatrix.entry(Value,Value) - argument not correct Value");
		return -100000;
	}
	return coinm[row][col];	
}

/**
 * set coinm[rowValue][colValue] to cv
 */
public void setEntry(Value rowValue, Value colValue, double cv){
	coinm[valueIndex(rowValue)][valueIndex(colValue)]=cv;	
}


/**
 * set coinm[rowValue][colValue] to cv
 */
public void setEntry(int row, int col, double cv){
	coinm[row][col]=cv;	
}

// makes the List values that contains all values that occur at least once in either f or s as values
// assigned to some item by one of the judges
private void makeValueList(List classifications){
	values	= new ArrayList();
	Classification cl;
	for (int i=0; i< classifications.size();i++){
		cl = (Classification)classifications.get(i);
		for (int j=0; j< cl.size();j++){
			if (!values.contains(cl.getValue(j)))
				values.add(cl.getValue(j));
		}
	}
	Comparator comp = new Comparator(){
		public int compare(Object obj1,Object obj2){
		  try{
		  	Value v1 = (Value)obj1;
		  	Value v2 = (Value)obj2;
		 	return v1.toString().compareTo(v2.toString());
		  }catch(ClassCastException ccexc){ 
		  	return -1;
	          }
		}};
	Collections.sort(values,comp);
}

// makes the List values that contains all values that occur at least once in either f or s as values
// assigned to some item by one of the judges
// but leaves out the  UNDEF VALUE
private void makeValueListUndef(List classifications){
	values	= new ArrayList();
	Classification cl;
	for (int i=0; i< classifications.size();i++){
		cl = (Classification)classifications.get(i);
		for (int j=0; j< cl.size();j++){
			if (!values.contains(cl.getValue(j)))
				values.add(cl.getValue(j));
		}
	}
	Comparator comp = new Comparator(){
		public int compare(Object obj1,Object obj2){
		  try{
		  	Value v1 = (Value)obj1;
		  	Value v2 = (Value)obj2;
		 	return v1.toString().compareTo(v2.toString());
		  }catch(ClassCastException ccexc){ 
		  	return -1;
	          }
		}};
	values.remove(undefValue);
	Collections.sort(values,comp);
}

public List getValues(){
	return values;
}

public int nrOfItems(){
	return ((Classification)classifications.get(0)).size();	
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

// fill the matrix coinm (following Krippendorff) 
private void fillMatrix(){
	Classification second,first;
	int nv = numberOfValues();
	Value cValue,kValue;
	int nr_units = ((Classification)classifications.get(0)).size();
	int count;
	for (int c=0; c<nv; c++){
		//cValue = (StringValue)getValue(c);
		cValue = getValue(c);
		for (int k=0;k<nv;k++){
			//kValue = (StringValue)getValue(k);
			kValue = getValue(k);
			count =0;
			for (int i=0;i<nr_units;i++){
				for (int f=0;f<classifications.size();f++){
					first = (Classification)classifications.get(f);
					for (int s=f+1;s<classifications.size();s++){
						second =(Classification) classifications.get(s); 
						if (cValue.equals(getValue(first.values,i))&&kValue.equals(getValue(second.values,i)))count++;
						if (cValue.equals(getValue(second.values,i))&&kValue.equals(getValue(first.values,i)))count++;	
					}
				}
			}
			coinm[c][k]=count;
		}
	}	
}


// fill the matrix coinm (following Krippendorff; method C)
// for handling annotations with "MISSING or UNDEFINED CASES"
private void fillMatrixUndef(){
	Classification second,first;
	int nv = numberOfValues();
	Value cValue,kValue;
	int nr_units = ((Classification)classifications.get(0)).size();
	double count;
	for (int c=0; c<nv; c++){
		//cValue = (StringValue)getValue(c);
		cValue = getValue(c);
		for (int k=0;k<nv;k++){
			//kValue = (StringValue)getValue(k);
			kValue = getValue(k);
			for (int i=0;i<nr_units;i++){
				count =0.0;
				for (int f=0;f<classifications.size();f++){
					first = (Classification)classifications.get(f);
					for (int s=f+1;s<classifications.size();s++){
						second =(Classification) classifications.get(s); 
						if (cValue.equals(getValue(first.values,i))&&kValue.equals(getValue(second.values,i)))count++;
						if (cValue.equals(getValue(second.values,i))&&kValue.equals(getValue(first.values,i)))count++;	
					}
				}
				int nrOfValuesDef = nrOfValuesInUnit(i);
				//System.out.println("nr of def vals in unit "+ i + "= "+ nrOfValuesDef);
				double telop =0.0;
				if (count==0.0) {
					 telop = 0.0;
				} else{ 
					 telop = (double)count/(nrOfValuesDef-1);
				}
				//System.out.println("telop is "+ telop);
				coinm[c][k]=coinm[c][k]+telop;
				//System.out.println("coinm[c][k] is "+ coinm[c][k]);
			}
		}
	}	
}

// return the number of defined values in the u-th unit of this list of classifications

private int nrOfValuesInUnit(int u){
	int sum=0;
	Classification cl;
	for (int i=0;i<classifications.size();i++){
		cl = (Classification)classifications.get(i);
		if ((cl.getValue(u)).equals(undefValue)){
			sum++;
		}
	}
	return classifications.size()-sum;
}


public int totalNumberOfItemsLabeledUndefined(){
	int sum=0;
	Classification cl;
	for (int i=0;i<classifications.size();i++){
		cl = (Classification)classifications.get(i);
		sum = sum + cl.nrOfValues(undefValue);
	}
	return sum;
	
}



private Value getValue(int n){
	return (Value)values.get(n);
}

private Value getValue(List lst,int i){
	return (Value)lst.get(i);
}

public void showMatrix(){
	System.out.println("CoincidenceMatrixM");
	int nv = coinm.length;
	for (int c=0; c<nv; c++){
	   System.out.println(" ");
	   for (int k=0;k<nv;k++)
		System.out.print(coinm[c][k]+ " ");
        }
        System.out.println("\n");	
}


public void printMatrix(String filename){
	try{
		PrintWriter pw = new PrintWriter(new FileOutputStream(filename,false),true);
		printMatrix(pw);
		pw.close();
	}catch(IOException exc){
		System.out.println("could not open output file");	
	}
	
}


public void printMatrix(PrintWriter pw){
	if (pw==null) return;
	int valueStringLength = 30;
	int nv = coinm.length;
	String value;
	int len;
	for (int c=0; c<nv; c++){
	   value = ((Value)values.get(c)).toString();
	   
	   pw.println("\n");
	   pw.print(showString(value,valueStringLength)+"\t");
	   for (int k=0;k<nv;k++)
		pw.print(coinm[c][k]+ "\t");
        }
        pw.println("\n");
        System.out.println("CoincidenceMatrix printed ");	
}

private String showString(String str, int n){
	String result;
	int len = str.length();
	if (len<n)
		result = str;
	else 
		result = str.substring(0,n);
	len = result.length();
	for (int i=0;i<n-len;i++)
		result=result+" ";
	return result;
}

/**
 * print all Values that occur in first or second on SO
 */
public void showValues(){
	for (int i=0; i<values.size();i++)
		System.out.println(values.get(i));	
}

/**
 * print all Values that occur in first or second on SO
 */
public void printValues(PrintWriter pw){
	for (int i=0; i<values.size();i++)
		pw.println(values.get(i));	
}

/**
 * @return number of class labels used
 */
public int numberOfValues(){
	return values.size();
}



// this is n=2*r where r is the number of items judged.
private double getN(){
	return totalItems();
}

private double product(int c, int k){
	return metric.distance(rowvalue(c),colvalue(k));
}


private Value rowvalue(int c){
	 return (Value)values.get(c);
} 

private Value colvalue(int k){
	 return (Value)values.get(k);
} 

/**
 * computes alpha for nominal values using the given distance metric <br>
 * the distance metric should be appropriate for the Values that occur in the Classification for which<br>
 * this CoincidenceMatrix is computed at contruction 
 * @return alpha = 1.0 - (D_observed / D_chance )
 */
public double alphaNominal(DistanceMetric dist){
	double denominator = Dchance(dist);
	if (denominator==0.0){
		 System.out.println("WARNING: CoincidenceMatrix.alphaNominal could not be computed since Dchance equals 0.0. returned 0.0 instead");
		 return 0.0;
	}
	double dobs = Dobserved(dist);
	double alpha = 1.0 - ( dobs /denominator);
	
	System.out.println("D_OBS :"+ dobs);
	System.out.println("D_CHA :"+ denominator);
	System.out.println("alpha :"+alpha);
	
	return alpha;
}

public double alpha(){
	DistanceMetric metric = new BooleanMetric();
	return  alphaNominal(metric);
}



/**
 * returns the same value as alpha when using the standard Boolean Metric.
 * return kappa = (pa-pe)/(1-pe) with pa = 1-Dobserved() and pe = 1-Dchance()
 * @return kappa according to Krippendorff's alpha method using the standard Boolean Metric
 */
public double kappaKrippendorff(){
	double kappa;
	double pa, pe;
	DistanceMetric metric = new BooleanMetric();
	pa = 1-Dobserved(metric);
	pe = 1-Dchance(metric);	
	kappa = (pa-pe)/(1-pe);
	return kappa;
}


private double Dobserved(DistanceMetric dist){
	return (1.0/getN())*obs_sum(dist);
}

private double Dchance(DistanceMetric dist){
	double n = getN();
	double cs = chance_sum(dist);
	if (n==0||n==1||cs==0.0) return 0.0;
	return (1.0/(n*(n-1)))*cs;
	
}


// total of all counts in coincidence matrix matrix
private double totalItems(){
	double total = 0.0;
	for (int i=0;i<coinm.length;i++)	
		total+=totalRow(i);
	return total;
}


private double totalColumn(int i){
	int size = numberOfValues();
	double sum = 0.0;
	for (int c=0; c<size; c++){
		sum=sum+coinm[c][i];
	}
	return sum;	
}

private double totalRow(int i){
	int size = numberOfValues();
	double sum = 0.0;
	for (int c=0; c<size; c++){
		sum=sum+coinm[i][c];
	}
	return sum;
}


private double chance_sum(DistanceMetric dist){
	double sum=0.0;
	double delta;
	int size = numberOfValues();
	for (int c=0; c<size; c++){
		for (int k=0;k<size;k++){
			delta = dist.distance(getValue(c),getValue(k));
			sum+= (double)( totalColumn(c)*totalRow(k)*delta*delta);
		}
	}
	return sum;
}

private double obs_sum(DistanceMetric dist){
	double sum = 0.0;
	double delta;
	int size = numberOfValues();
	for (int c=0; c<size; c++){
		for (int k=0;k<size;k++){
			delta = dist.distance(getValue(c),getValue(k));
			sum+= (double)(coinm[c][k]*delta*delta);
		}
	}
	return sum;
}

/**
 * set the distance metric used
 */
public void setDistanceMetrics (DistanceMetric dist){
	this.metric=dist;
}	
public void showDistanceMatrix(String outFile){
	PrintWriter outWriter=null;
	double delta=0.0;
	int size = numberOfValues();
        try{
         outWriter=new PrintWriter(new FileWriter(outFile));
	 
	 for (int c=0; c<size; c++){
	 	String line="";
		for (int k=0;k<size;k++){
			delta = metric.distance(getValue(c),getValue(k));
			line+=Double.toString(delta*delta)+"\t";
		}
		outWriter.println(line);
	}
	} catch (Exception exc) {
 			exc.printStackTrace();
 	} finally {
 			 if (outWriter != null) outWriter.close();
 	}
}


// test with examples from Krippendorff's Alpha-Reliability document
public static void main(String[] args){
	
	CoincidenceMatrixM matrix, newM;
	List cls;
	
	/*
	String[] items = {"1","2","3","4","5","6","7","8","9","10"};
	String[] values_meg  = {"0","1","0","0","0","0","0","0","1","0"};	
	String[] values_owen = {"1","1","1","0","0","1","0","0","0","0"};
	Classification meg = new Classification("Meg",items,values_meg);
	Classification owen = new Classification("Owen",items,values_owen);
	Classification rks = new Classification("Rks",items,values_owen);
	cls = new ArrayList();
	cls.add(meg); cls.add(meg); cls.add(meg);
	matrix = new CoincidenceMatrixM(cls);
	//matrix.showValues(); // values are 0 and 1
	matrix.showMatrix(); 
	// correct matrix
	//   10  4     //
	//    4  2     //
	// correct matrix
	double alpha = matrix.alphaNominal(new BooleanMetric()); 
	System.out.println("alpha is :" + alpha); // correct answer is: 0.095238
	System.out.println("kappa-Krippendorff is :" + matrix.kappaKrippendorff());
	
	
	// second example from Krippendorff's Alpha-Reliability document
	String[] items2 =       {"1","2","3","4","5","6","7","8","9","10","11","12"};
	String[] values_ben  =   {"a","a","b","b","d","c","c","c","e","d","d","a"};	
	String[] values_gerry =  {"b","a","b","b","b","c","c","c","e","d","d","d"};
	Classification ben = new Classification("Ben",items2,values_ben);
	Classification gerry = new Classification("Gerry",items2,values_gerry);
	cls = new ArrayList();
	cls.add(ben); cls.add(gerry);
	matrix = new CoincidenceMatrixM(cls);
	matrix.showValues(); // values are a,b,c,d,e
	matrix.showMatrix();
	
	CoincidenceMatrixM newM = matrix.remove(new StringValue("a"));
	newM.showMatrix();
	System.out.println("kappa-Krippendorff is :" + newM.kappaKrippendorff());
	*/
	 
	/*********** correct coincidence matrix	*************
							totals
	a	2	1		1		4
	b	1	4		1		6
	c			6			6
	d	1	1		4		6
	e					2	2
	       ------------------------------------------------
		4	6	6	6	2	24
	*********************************************************/
	/*
	alpha = matrix.alphaNominal(new BooleanMetric()); 
	System.out.println("alpha is :" + alpha); // correct answer is: 0.692
	
	
	
	
	double[][] mat = { { 166.0,76.0,0.0,2.0,1.0 },
	                   { 76.0,968.0,40.0,71.0,118.0 },
	                   { 0.0,40.0,76.0,0.0,1.0},
	                   {2.0, 71.0,0.0,212.0,2.0},
	                   {1.0,118.0,1.0,2.0,226.0}
	                  };
	CoincidenceMatrixM adrcoin = new CoincidenceMatrixM(mat);
	System.out.println("kappa kripp is : "+adrcoin.kappaKrippendorff());
	
	
	*/
	
	// third example from Krippendorff's Alpha-Reliability document (
	// C. Nominal data, multiple observers, missing data
	// missing value is identified by "."
	
	String[] items3 =      {"1","2","3","4","5","6","7","8","9","10","11","12"};
	
	String[] values_a  =   {"1","2","3","3","2","1","4","1","2",".",".","."};	
	String[] values_b =    {"1","2","3","3","2","2","4","1","2","5",".","."};
	String[] values_c =    {".","3","3","3","2","3","4","2","2","5","1","3"};
	String[] values_d =    {"1","2","3","3","2","4","4","1","2","5","1","."};
	
	Classification cl_a = new Classification("A",items3,values_a);
	Classification cl_b = new Classification("B",items3,values_b);
	Classification cl_c = new Classification("C",items3,values_c);
	Classification cl_d = new Classification("D",items3,values_d);
	
	cls = new ArrayList();
	cls.add(cl_a); 
	cls.add(cl_b);
	cls.add(cl_c);
	cls.add(cl_d);
	
	// compute matrix with "." as UNDEF value
	matrix = new CoincidenceMatrixM(cls,new StringValue("."));
	matrix.showValues();
	matrix.showMatrix();
	
	/******** correct matrix ***************
	
	7 	4/3 	1/3 	1/3 	0 	| 9
	4/3 	10 	4/3 	1/3 	0 	| 13
	1/3 	4/3 	8 	1/3 	0 	| 10
	1/3 	1/3 	1/3 	4 	0 	| 5
	0 	0 	0 	0 	3 	| 3
	-----------------------------------------------------
	9 	13 	10 	5 	3   	| 40
	
	*************************************************/
	
	
	//newM = matrix.remove(new StringValue("."));
	//newM.showMatrix();
	System.out.println("kappa-Krippendorff is :" + matrix.alpha());
	System.out.println("kappa-Krippendorff is :" + matrix.kappaKrippendorff());
}

}