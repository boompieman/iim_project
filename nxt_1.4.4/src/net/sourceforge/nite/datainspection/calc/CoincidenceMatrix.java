package net.sourceforge.nite.datainspection.calc;
import net.sourceforge.nite.datainspection.data.*;
import net.sourceforge.nite.datainspection.impl.*;

import java.util.*;
import java.io.*;

/**
 * CoincidenceMatrix has methods for comparing Two Classifications. Both classifications must contain exactly the same items in the same order.
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


public class CoincidenceMatrix {

private Classification first;
private Classification second;
private DistanceMetric metric;
// the list of values that occur in the judgements
private List values;
// the coincidence matrix
private double[][] coinm;

/**
 * @param f the first Classification
 * @param s the second Classification
 * required: f and s are classifications of the same ordered list of items/units classified
 * the constructor computes the coincidence matrix
 */
public CoincidenceMatrix(Classification f, Classification s){
	first = f;
	second = s;
	makeValueList(f,s);
	coinm = new double[numberOfValues()][numberOfValues()];
	for (int i=0;i<numberOfValues();i++)
		for(int j=0;j<numberOfValues();j++)
			coinm[i][j]=0.0;
	fillMatrix();
}

/**
 *
 * creates an empty coincidence matrix of given size 
 * with default values the numbers (0,1,...,size-1) 
 */
public CoincidenceMatrix(int size ){
	coinm = new double[size][size];
	for (int i=0;i<size;i++)
		for(int j=0;j<size;j++)
			coinm[i][j]=0.0;
	setDefaultValues();
}

/**
 * @param conf_m the confusion matrix from which this coincidence matrix is computed (CoinMatrix = ConfMatrix + ConfMatrix^T)
 */
public CoincidenceMatrix(ConfusionMatrix conf_m){
	int size=conf_m.size();
	coinm = new double[size][size];
	for (int i=0;i<size;i++)
		for(int j=0;j<size;j++)
			coinm[i][j]=conf_m.entry(i,j)+conf_m.entry(j,i);
	setValues(conf_m.getValues());
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
public void setEntry(int row, int col, double cv){
	coinm[row][col]=cv;	
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


public List getValues(){
	return values;
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

// fill the coincidence matrix coinm
private void fillMatrix(){
	int nv = numberOfValues();
	Value cValue,kValue;
	int nr_units = first.size();
	int count;
	for (int c=0; c<nv; c++){
		//cValue = (StringValue)getValue(c);
		cValue = getValue(c);
		for (int k=0;k<nv;k++){
			//kValue = (StringValue)getValue(k);
			kValue = getValue(k);
			count =0;
			for (int i=0;i<nr_units;i++){
				if (cValue.equals(getValue(first.values,i))&&kValue.equals(getValue(second.values,i)))count++;
				if (cValue.equals(getValue(second.values,i))&&kValue.equals(getValue(first.values,i)))count++;	
			}
			coinm[c][k]=count;
		}
	}	
}



private Value getValue(int n){
	return (Value)values.get(n);
}

private Value getValue(List lst,int i){
	return (Value)lst.get(i);
}

public void showMatrix(){
	System.out.println("CoincidenceMatrix");
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


public int nrOfItems(){
	return first.size();
}


private double getN(){
	return totalItems();
}

private double product(int c, int k){
	return metric.distance(rowvalue(c),colvalue(k));
}


private Value rowvalue(int c){
	 return first.getValue(c);
} 

private Value colvalue(int k){
	 return second.getValue(k);
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
	
	//System.out.println("D_OBS :"+ dobs);
	//System.out.println("D_CHA :"+ denominator);
	//System.out.println("alpha :"+alpha);
	
	return alpha;
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
	String[] items = {"1","2","3","4","5","6","7","8","9","10"};
	String[] values_meg  = {"0","1","0","0","0","0","0","0","1","0"};	
	String[] values_owen = {"1","1","1","0","0","1","0","0","0","0"};
	Classification meg = new Classification("Meg",items,values_meg);
	Classification owen = new Classification("Owen",items,values_owen);
	CoincidenceMatrix matrix = new CoincidenceMatrix(meg,owen);
	matrix.showValues(); // values are 0 and 1
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
	matrix = new CoincidenceMatrix(ben,gerry);
	matrix.showValues(); // values are a,b,c,d,e
	matrix.showMatrix(); 
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
	alpha = matrix.alphaNominal(new BooleanMetric()); 
	System.out.println("alpha is :" + alpha); // correct answer is: 0.692
}


}