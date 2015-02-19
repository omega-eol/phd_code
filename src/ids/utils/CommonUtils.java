package ids.utils;

import ids.clustering.model.Distance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Random;
import java.util.Set;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

@SuppressWarnings("serial")
public class CommonUtils implements Serializable {

	private Random rand;
	private boolean verbose = false;
	private Logger log;
	
	// Constructor
	public CommonUtils(boolean verbose) {
		rand = new Random();
		this.verbose = verbose;
		if (verbose) log = Logger.getLogger(getClass().getName());
	}
	
	// FUNCTIONS	
	// find unique value
	public UniqueResult<Double> findUnique(double[] array) {
		// Initialize a map <key, value>
		Map<Double, Integer> m = new HashMap<Double, Integer>();
		
		// create a map
		for (int i = 0; i < array.length; i++) {
			double t = array[i];
			if (m.containsKey(t)) {
				// increase value by 1
				Integer v = m.get(t);
				m.put(t, v + 1);
			} else {
				m.put(t, 1);
			}
		}
		
		// holders
		Double[] d = new Double[m.size()];
		int[] f = new int[m.size()];
		int counter = -1;
		for (double key : m.keySet()) {
			counter++;
			d[counter] = key;
		}
		
	    // sort domain
	    Arrays.sort(d);
	    for (int i = 0; i<d.length; i++) f[i] = m.get(d[i]);
	    return new UniqueResult<Double>(d, f);
	}
	// find unique value
	public UniqueResult<Integer> findUnique(int[] array) {
		// Initialize a map
		Map<Integer, Integer> m = new HashMap<Integer, Integer>();
		
		// create a map
		for (int i = 0; i < array.length; i++) {
			int t = array[i];
			if (m.containsKey(t)) {
				// increase value by 1
				Integer v = m.get(t);
				m.put(t, v + 1);
			} else {
				m.put(t, 1);
			}
		}
		
		// holders
		Integer[] d = new Integer[m.size()]; // values
		int[] f = new int[m.size()]; // frequencies
		int counter = -1;
		for (int key : m.keySet()) {
			counter++;
			d[counter] = key;
		}
	    
		// sort domain
	    Arrays.sort(d);
	    for (int i = 0; i<d.length; i++) f[i] = m.get(d[i]);
	    return new UniqueResult<Integer>(d, f);
	}
		
	// find sum of the vector
	public double findSum(double[] array) {
		double res = 0;
		for (int i = 0; i < array.length; i++) res += array[i];
		return res;
	}
	public double findSum(int[] array) {
		double res = 0;
		for (int i = 0; i < array.length; i++) res += array[i];
		return res;
	}
	public int findSum(boolean[] array) {
		int res = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i]) res += 1;
		}
		return res;
	}
	
	
	// find mean value of the vector
	public double findMean(double[] array) {
		int n = array.length;
		if (n==0) return 0;
		return findSum(array)/n;
	}
	public double findMean(int[] array) {
		int n = array.length;
		if (n==0) return 0;
		return findSum(array)/n;
	}
	
	/**
	 * Return mode of a vector
	 * @param input - input array
	 * @return mode
	 */
	public double getMode(double[] input) {
		// find all unique value and their frequencies
		UniqueResult<Double> ur = findUnique(input);
		
		// find domain with the max frequency
		SearchResult<Integer> sr = getMaxValue(ur.frequency);
		
		// return the domain with the highest frequency
		return ur.domain[sr.getIndex()];
	}
	public int getMode(int[] input) {
		// find all unique value and their frequencies
		UniqueResult<Integer> ur = findUnique(input);
		SearchResult<Integer> sr = getMaxValue(ur.frequency);
		return ur.domain[sr.getIndex()];
	}
	/**
	 * Find the column-wise mode of the matrix - mode of each feature
	 * @param data - input matrix
	 * @return
	 */
	public double[] getMode(double[][] data) {
		if (data == null) return null;
		int n = data.length;
		if (n == 0) return null;
		int m = data[0].length;
		
		// get number of columns (features)
		double[] res = new double[m];
		for (int i = 0; i<m; i++) {
			double[] col_vector = getColumn(data, i);
			res[i] = getMode(col_vector);
		}
		
		return res;
	}
	
	/**
	 * Find maximum distance in data set
	 * @param data - input data set
	 * @param distance - distance measure
	 * @return maximum distance in data set
	 */
	public double getMaxDistance(double[][] data, Distance distance) {
		if (data == null) return 0;
		int n = data.length;
		if (n == 0) return 0;
		int dim = data[0].length;
		
		if (distance == Distance.MATCH) return dim;
		if (distance == Distance.COSINE) return 1;
		
		double maxD = 0;
		int m = n*(n-1)/2;
		int index = 0;
		int step = (int)(m*0.01);
		if (step==0) step = m;
		
		for (int i=0; i<n; i++) {
			for (int j=i+1; j<n; j++) {
				index = index + 1;
				double currentD = getDistance(data[i], data[j], distance);
				if (currentD>maxD) {
					maxD = currentD;
				}
				// print
				if ((verbose)&((index % step)==0)) System.out.println((double)index/m*100 + "% is done.");
			}
		}
		
		return maxD;
	}
	
	public Distance getDistanceEnum(String str) {
		if (str.toUpperCase().equals("MATCH")) {
			return Distance.MATCH;
		} else if (str.toUpperCase().equals("SQEUCLIDEAN")) {
			return Distance.SQEUCLIDEAN;
		} else if (str.toUpperCase().equals("EUCLIDEAN")) {
			return Distance.EUCLIDEAN;
		} else if (str.toUpperCase().equals("COSINE")) {
			return Distance.COSINE;
		} else {
			return null;
		}
	}
	
	/**
	 * Returns distance between every point in the input data set with n objects
	 * @param data - input data set with n objects
	 * @param distance - distance measure
	 * @return distance vector of size n(n-1)/2
	 */
	public float[] getDistance(double[][] data, Distance distance) {
		int n = data.length;
		if (n==0) return null;
		
		// number of elements in the output vector
		int m = n*(n-1)/2;
		double s = m*32.0/8.0/1024.0/1000.0;
		System.out.printf("Trying to create a array of float of size %d (would take %6.2f MB)\n", m, s);
		float[] pd = new float[m];
		
		System.out.println("Calculating the distance..");
		int counter = -1;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				counter++;
				pd[counter] = (float)getDistance(data[i], data[j], distance);
				if ( verbose && ( ( counter % (int)(m*0.01) )==0 ) ) System.out.printf("%d is done.\n", (int)(counter*1.0/m*100));
			}
		}
		System.out.println("Done.");		
		
		return pd;
	}
	/**
	 * Finds the distance between every point in the input data set with n objects
	 * and saves it to a file.
	 * Very useful for a very large data sets
	 * @param data - input data set with n objects
	 * @param distance - distance measure
	 * @param filename - output filename
	 */
	public void getDistance(double[][] data, Distance distance, String filename) {
		int n = data.length;
		if (n==0) System.out.println("There is no element in data");
		
		// number of elements in the output vector
		long m = n*(n-1)/2;
		
		// create a buffer
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		// find the distance
		System.out.println("Calculating the distance..");
		int counter = -1;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				counter++;
				double pd = getDistance(data[i], data[j], distance);
				try {
					out.write(String.format("%5.4f\n", pd));
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
				if ( verbose && ( ( counter % (int)(m*0.01) )==0 ) ) System.out.printf("%d is done.\n", (int)(counter*1.0/m*100));
			}
		}
		
		// close the buffer
		try {
			out.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println("Done.");		
	}	
	
	/** 
	 * Finds distance between point j and all the other data points
	 * @param j - target point index
	 * @param pd - distance vector
	 * @param n - number of objects
	 * @return
	 */
	public double[] getDistance(int j, float[] pd, int n) {
		if (n == 0) return null;
		double[] res = new double[n];
		for (int i = 0; i < n; i++) res[i] = getDistanceFromDistanceVector(pd, i, j, n);
		return res;
	}
		
	/**
	 * Returns the distance between two objects row and col. Where distance is represented as the distance vector pd.
	 * @param pd - distance vector
	 * @param row - index of the first object (starts from 0!)
	 * @param col - index of the second object (start from 0!)
	 * @param n - number of objects in the data set
	 * @return distance
	 */
	public float getDistanceFromDistanceVector(float[] pd, int row, int col, int n) {
		if (row == col) return 0;
		int index = -1;
		if (row < col) {
			index = getDistanceIndex(row, col, n);
		} else {
			index = getDistanceIndex(col, row, n);
		}
		return pd[index];
	}
	private int getDistanceIndex(int i, int j, int n) {
		int index = -1;
		if (i>j) {
			System.out.println("CommonUtils: getDistanceIndex: I is less than J");
			return index;
		}
		//index = n*(i-1)-i*(i+1)/2+j;
		index = (n-1)*i-i*(i-1)/2+j-i-1;
		return index;
	}
	
	/**
	 * Converts distance vector to distance in matrix form
	 * @param pd - distance vector
	 * @param n - number of objects
	 * @return distance matrix
	 */
	public float[][] ConvertToSquareForm(float[] pd, int n) {
		if (n==0) return null;
		if (pd.length == 0) return null;
		
		float[][] res = new float[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				res[i][j] = getDistanceFromDistanceVector(pd, i, j, n);
				res[j][i] = res[i][j];
			}
		}
		return res;
	}
	
	/**
	 * Returns distance between data points and a vector
	 * @param data - input data
	 * @param target - target point
	 * @param distance - distance measure
	 * @return
	 */
	public double[] getDistance(double[][] data, double[] target, Distance distance) {
		int n = data.length;
		if (n == 0) return null;
		//int dim = data[0].length;
		double[] res = new double[n];
		for (int i = 0; i < n; i++) {
			res[i] = getDistance(data[i], target, distance);
		}		
		return res;
	}	
	// distance between two vectors
	public double getDistance(double[] a, double[] b, Distance distance) {
		double pd = 0;
		int dim = a.length;
		if (dim != b.length) {
			System.out.println("Achtung! Input vectors have different size!");
			return -1;
		}
		if ((dim ==0)||(b.length == 0)) {
			System.out.println("Achtung! One of the input vectors have size = 0!");
			return -1;
		}
		
		if (distance==Distance.SQEUCLIDEAN) {
			for (int i=0; i<dim; i++) {
				pd += (a[i]-b[i])*(a[i]-b[i]);
			}
		} else if (distance==Distance.EUCLIDEAN) {
			for (int i=0; i<dim; i++) {
				pd += (a[i]-b[i])*(a[i]-b[i]);
			}
			pd = Math.sqrt(pd);
		} else if (distance==Distance.COSINE) {
			double a_norm = 0;
			double b_norm = 0;
			for (int i = 0; i < dim; i++) {
				pd += a[i]*b[i];
				a_norm += a[i]*a[i];
				b_norm += b[i]*b[i];
			}
			pd = 1 - pd/Math.sqrt(a_norm)/Math.sqrt(b_norm);
		} else if (distance==Distance.MATCH) {
			for (int i = 0; i < dim; i++) {
				if ((int)a[i] != (int)b[i]) pd++;
			}
		}
		else {
			System.out.println("Distance is not set!");
		}
		//System.out.println("Distance is " + pd);
 		return pd;
	}
	
	// Find mean vector, for each dimension
	public double[] findMeanVector(double[][] data) {
		int n = data.length;
		if (n==0) {
			System.out.println("Error:findMeanVector: Division by zero");
			return null;
		}
		int dim = data[0].length;
		double[] mean = new double[dim];
		// find sum
		for (int i=0;i<n;i++) {
			for (int j=0; j<dim; j++) {
				mean[j] = mean[j] + data[i][j];
			}
		}
		// find mean
		for (int j=0; j<dim;j++) mean[j] = mean[j]/n;
		return mean;
	}
	
	// find sdt vector for each dimension
	public double[] findSqStd(double[][] data, double[] data_mean) {
		int n = data.length;
		if (n==0) return null;
		int m = data[0].length;
		
		// find standard deviation for each feature
		double[] std = new double[m];
		Arrays.fill(std, 0.0);
		
		for (int j = 0; j < m; j++) {
			for (int i = 0; i < n; i++) {
				std[j] = std[j] + (data[i][j] - data_mean[j])*(data[i][j] - data_mean[j]);
			}
			std[j] = std[j]/(n-1);
		}
		
		return std;
	}
	public double[] findSqStd(double[][] data) {
		int n = data.length;
		if (n==0) return null;
		//int m = data[0].length;
		
		// get the mean value of each column (feature)
		double[] data_mean = findMeanVector(data);
		return findSqStd(data, data_mean);
	}
	public double[] findStd(double[][] data, double[] mean_data) {
		int n = data.length;
		if (n==0) return null;
		double[] res = findSqStd(data, mean_data);
		int m = data[0].length;
		for (int j = 0; j < m; j++) res[j] = Math.sqrt(res[j]);
		return res;
	}
	public double[] findStd(double[][] data) {
		int n = data.length;
		if (n==0) return null;
		int m = data[0].length;
		double[] res = findSqStd(data);
		for (int j = 0; j < m; j++) res[j] = Math.sqrt(res[j]);
		return res;
	}
	
	public double[] findNorm(double[] input, int dim) {
		double[] res = new double[dim];
		double sum = 0;
		for (int i = 0; i < dim; i++) {
			sum = sum + input[i]*input[i];
		}
		sum = Math.sqrt(sum);
		for (int i = 0; i < dim; i++) {
			res[i] = input[i]/sum;
		}
		return res;
	}
	
	// search
	public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
	     Set<T> keys = new HashSet<T>();
	     for (Entry<T, E> entry : map.entrySet()) {
	         if (value.equals(entry.getValue())) {
	             keys.add(entry.getKey());
	         }
	     }
	     return keys;
	}
	/**
	 * Returns Set of Integers, in condition if input[i] == value, then i is added to the output set
	 * @param input - input vector
	 * @param value - input value
	 * @return set of Integers
	 */
	public Set<Integer> getIndicesByValue(int[] input, int value) {
	     Set<Integer> keys = new HashSet<Integer>();
	     for (int i = 0; i<input.length; i++) {
	    	 if (input[i] == value) {
	             keys.add(i);
	             //if (verbose) System.out.println("Object " + i + " is a member of cluster " + value);
	         }
	     }
	     return keys;
	}
	
	/*
	 * Returns array of integer 1 - item match the value, 0 - item does not match the value 
	 */
	public boolean[] getIndexByValue(int[] input, int value) {
	    int n = input.length; 
		boolean[] index = new boolean[n];
	     for (int i = 0; i<n; i++) {
	    	 if (input[i] == value) {
	             index[i] = true;
	         } else {
	        	 index[i] = false;
	         }
	     }
	     return index;
	}
	public boolean[] getIndexByValue(double[] input, double value) {
	    int n = input.length; 
		boolean[] index = new boolean[n];
	     for (int i = 0; i<n; i++) {
	    	 if (input[i] == value) {
	             index[i] = true;
	         } else {
	        	 index[i] = false;
	         }
	     }
	     return index;
	}
	
	/**
	 * Returns true for the element i, if and only if a <= input(i) < b
	 * @param input - input array
	 * @param a - lower bound
	 * @param b - upper bound
	 * @return
	 */
	public boolean[] findValueBetweenAandB(double[] input, double a, double b) {
		int n = input.length;
		if (n == 0) return null;
		boolean[] res = new boolean[n];
		for (int i = 0; i < n; i++) {
			if ((a <= input[i])&(input[i] < b)) {
				res[i] = true;
			} else {
				res[i] = false;
			}
		}
		return res;
	} 
	
	public int[] getElementsByIndeces(List<Integer> input, Integer[] index) {
		int[] res = new int[index.length];
		for (int i = 0; i < index.length; i++) res[i] = input.get(index[i]);
		return res;
		
	}
	
	// max and min
	public SearchResult<Integer> getMaxValue(int[] input) {
		int value = Integer.MIN_VALUE;
		int index = -1;
		for (int i = 0; i < input.length; i++) {
			if (input[i] > value) {
				value = input[i];
				index = i;
			}
		}
		if (verbose) log.info("Maximum value of array input[" + index + "] is " + value);
		return new SearchResult<Integer>(value, index);
	}
	public SearchResult<Double> getMaxValue(double[] input) {
		double value = Double.MIN_VALUE;
		int index = -1;
		for (int i = 0; i < input.length; i++) {
			if (input[i] > value) {
				value = input[i];
				index = i;
			}
		}
		if (verbose) log.info("Maximum value of array input[" + index + "] is " + value);
		return new SearchResult<Double>(value, index);
	}
	/**
	 * Finds the maximum value in each row of the input matrix
	 * @param input
	 * @return
	 */
	public double[] getMaxValue(double[][] input) {
		int rows = input.length;
		int cols = input[0].length;
		if ((rows == 0)||(cols == 0)) return null;
		
		double[] res = new double[rows];
		for (int i = 0; i < rows; i++) {
			SearchResult<Double> sr = getMaxValue(input[i]);
			res[i] = sr.getValue();
		}
		return res;
	}
	public double[] getMaxValue(double[][] input, int dim) {
		if ((dim > 2)||(dim < 1)) {
			System.out.println("Cannot work with such dimensions: " + dim);
			return null;
		}
		int rows = input.length;
		int cols = input[0].length;
		if ((rows == 0)||(cols == 0)) return null;
		
		double[] res = null;
		if (dim == 1) { // find maximum along columns
			res = getMaxValue(transpose(input));
		} else {
			res = getMaxValue(input);
		}
		
		return res;
	}
	/**
	 * Finds minimum value in the input matrix
	 * @param data - input matrix
	 * @return minimum value
	 */
	public double getMinValue(double[][] data) {
		int n = data.length;
		if (n==0) return 0;
		int dim = data[0].length;
		
		double res = Double.MAX_VALUE;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < dim; j++) {
				if (data[i][j] < res) res = data[i][j];
			}
		}
		return res;
	}
	
	/**
	 * Finds a minimum value in of each row in the input matrix
	 * @param data - input matrix
	 * @return array of minimum values for each row
	 */
	public double[] getMinValueInRows(double[][] data) {
		int n = data.length;
		if (n==0) return null;
		
		double[] res = new double[n];
		for (int i = 0; i < n; i++) {
			SearchResult<Double> sr = getMinValue(data[i]);
			res[i] = sr.getValue();
		}
		return res;
	}
	
	public double getMin(double[] input) {
		if (input == null) {
			System.out.println("CommonUtilities: getMinValue: The inpur vector is null");
			return -1.0;
		}
		int n = input.length;
		if (n == 0) return .0;
		
		double min_value = Double.MAX_VALUE;
		for (int i = 0; i < n; i++) {
			if (input[i] < min_value) {
				min_value = input[i];
			}
		}
		return min_value;
	}
	
	public SearchResult<Double> getMinValue(double[] input) {
		int n = input.length;
		if (n == 0) return null;
		
		double value = Double.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < n; i++) {
			if (input[i] < value) {
				value = input[i];
				index = i;
			}
		}
		if (verbose) log.info("Minimum value of array input[" + index + "] is " + value);
		return new SearchResult<Double>(value, index);
	}
	
	/**
	 * Performs logical a[i]&b[i] with boolean arrays a and b
	 * @param a - first logical array
	 * @param b - second logical array
	 * @return returns a&b
	 */
	public boolean[] findAandB(boolean[] a, boolean[] b) {
		int n = a.length;
		if (n != b.length) return null;
		boolean[] ab = new boolean[n];
		for (int i = 0; i < n; i++) {
			ab[i] = a[i]&b[i];
		}
		return ab;
	}
	
	/**
	 * Add a integer "a" to each element of vector "input"
	 * @param input - input vector
	 * @param a - integer
	 * @return
	 */
	public int[] addToVector(int[] input, int a) {
		if (input==null) return null;
		int n = input.length;
		if (n==0) return null;
		int[] res = new int[n];
		for (int i = 0; i < n; i++) res[i] = input[i] + a;
		return res;
	}
	
	// MATRIX
	public double[][] getColumns(double[][] data, int[] indices) {
		int n = data.length;
		if (n==0) {
			System.out.println("CommonUtils: getColumns: No data records");
			return null;
		}
		
		int dim = indices.length;
		if (dim == 0) {
			System.out.println("CommonUtils: getColumns: No column indices");
			return null;
		}
		
		double[][] res = new double[n][dim];
		for (int i = 0; i<n; i++) {
			for (int j = 0; j < dim; j++) {
				int index = indices[j];
				res[i][j] = data[i][index];
			}
		}		
		return res;
	}
	public double[] getColumn(double[][] data, int index) {
		int n = data.length;
		if (n==0) {
			System.out.println("CommonUtils: getColumn: No data records");
			return null;
		}		
		if (index < 0) {
			System.out.println("CommonUtils: getColumn: Column index is not valid, less than 0");
			return null;
		}
		
		double[] res = new double[n];
		for (int i = 0; i<n; i++) res[i] = data[i][index];
		return res;
	}
	
	/**
	 * Return a specified column of the input matrix
	 * @param data - input matrix
	 * @param index - index of the column
	 * @return return a column of a matrix
	 */
	public int[] getColumn(int[][] data, int index) {
		int n = data.length;
		if (n==0) {
			System.out.println("CommonUtils: getColumn: No data records");
			return null;
		}		
		if (index < 0) {
			System.out.println("CommonUtils: getColumn: Column index is not valid, less than 0");
			return null;
		}
		
		int[] res = new int[n];
		for (int i = 0; i<n; i++) res[i] = data[i][index];
		return res;
	}
	
	public void fillColumn(int[][] data, int[] column, int index) {
		int n = column.length;
		if (data.length != n) {
			System.out.println("Input matrix and data column has to be the same length");
		}
		for (int i = 0; i<n; i++) data[i][index] = column[i];
	}
	
	/**
	 * Returns rows selected by rowIndices
	 * @param data - input data
	 * @param rowIndices - array of row indices
	 * @return set (double[][]) of data
	 */
	public double[][] getRows(double[][] data, int[] rowIndices) {
		// error checking
		if (rowIndices==null) return null;
		int n_rows = rowIndices.length;
		if (data.length==0) return null;
		int m = data[0].length;
		
		// run
		double[][] res = new double[n_rows][m];
		for (int i = 0; i < n_rows; i++) {
			int row = rowIndices[i];
			res[i] = data[row];
		}
		
		return res;
	}
	
	
	// Transpose
	public double[][] transpose(double[][] input) {
		int n = input.length;
		int m = input[0].length;
		double[][] res = new double[m][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				res[j][i] = input[i][j];
			}
		}
		return res;
	}
	// Invert
	/**
	 * Inverts input matrix, so y[i][j] = 1/x[i][j]
	 * @param x - input matrix
	 * @return y - inverted matrix
	 */
	public double[][] invert(double[][] x) {
		int n = x.length;
		if (n == 0) return null;
		int dim = x[0].length;
		if (dim == 0) return null;
		
		// output
		double[][] y = new double[n][dim];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < dim; j++) {
				if (x[i][j] != 0) {
					y[i][j] = 1/x[i][j];
				} else {
					System.out.println("CommonUtils: invert: Divizion by Zero!");
					y[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}
		return y;
	}
	
	/**
	 * Counts the number of values in vector x that fall between the elements in the edges vector (which must contain monotonically nondecreasing values)
	 * @param x
	 * @param edges
	 * @return
	 */
	public int[] histc(double[] x, double[] edges) {
		int n = x.length;
		if (n ==0) return null;
		double[] x_sorted = x.clone();
		Arrays.sort(x_sorted);
		
		int n_edges = edges.length;
		int[] count = new int[n_edges];
		
		int k = 0;
		for (int i = 0; i < n; i++) {
			if (k+1 < n_edges) {
				if ((x_sorted[i] >= edges[k])&(x_sorted[i] < edges[k+1])) {
					count[k]++; 
				} else {
					k++;
					i--;
				}
			} else {
				if (x_sorted[i] == edges[k]) {
					count[k]++;
				} else {
					break;
				}
			}
		}
		return count;
	}
	
	/**
	 * The linspace function generates linearly spaced vector from a to b 
	 * @param a 
	 * @param b
	 * @return
	 */
	public double[] linspace(double a, double b) {
		return linspace(a, b, 100);
	}
	public double[] linspace(double a, double b, int n) {
		double[] res = new double[n];
		double step = (b-a)/(n-1);
		res[0] = a;
		for (int i = 1; i < n; i++) res[i] = res[i-1] + step;
		return res;
	}
	
	// permutations
	public int[] getRandomPermutation(int n, int k) {
		int[] res = new int[k];
		for (int i=0; i<k; i++) {
			res[i] = -1;
		}
		int counter = 1;
		
		// get indices
		for (int i=0; i<k; i++) {
			while (true) {
				int r = rand.nextInt(n);
				if (!isContained(res, counter, r)) {
					res[i] = r;
					counter++;
					break;
				}
			}
		}
		return res;
	}	
	public boolean isContained(int[] array, int n, int target) {
		for (int i=0; i<n; i++) {
			if (array[i]==target) return true;
		}
		return false;
	}
	
	// input
	/**
	 * Read data from a file
	 * @param fileName - name of the data file
	 * @param n - number of objects to read
	 * @param dim - number of dimensions
	 * @return - data matrix double[n][dim]
	 */
	public double[][] readDataFile(String fileName, int n, int dim) {
		double[][] res = new double[n][dim];
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String str = "";
			for (int i = 0; i < n; i++) {
				str = in.readLine();
				//System.out.print((i+1) + ": ");
				String[] items = str.split(",");
				for (int j = 0; j < dim; j++) {
					res[i][j] = Double.parseDouble(items[j]);
					//System.out.print(res[i][j] + " ");
				}
				//System.out.print("\n");
			}
			// close file
			in.close();
			
			System.out.println("File has been read: " + fileName);
			System.out.println("n: " + n + ", dim: " + dim);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return res;
	}
	public double[][] readDataFile(String fileName) {
		List<Double[]> temp = null;
		double[][] res = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String line = in.readLine();
			int dim = line.split(",").length;
			temp = new ArrayList<Double[]>();
			while (line != null) {
				Double[] d = new Double[dim];
				String[] items = line.split(",");
				for (int j = 0; j < dim; j++) {
					d[j] = Double.parseDouble(items[j]);
					//System.out.printf("%5.4f\t", d[j]);
				}
				temp.add(d);
				//System.out.println();
				line = in.readLine();
			}			
			// close file
			in.close();
			
			// convert from List<Double[]> to double[][]
			int n = temp.size();
			if (n > 0) {
				res = new double[n][dim];
				for (int i = 0; i < n; i++) {
					Double[] d = temp.get(i);
					for (int j = 0; j < dim; j++) {
						res[i][j] = d[j];
					}
				}
				
				System.out.println("File has been read: " + fileName);
				System.out.println("n: " + n + ", dim: " + dim);				
			}			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return res;
	}
	public int[] readVectorFromFile(String fileName) {
		List<Integer> temp = new ArrayList<Integer>();
		int[] res = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String line = in.readLine();
			while (line != null) {
				int t = Integer.parseInt(line);
				temp.add(t);
				line = in.readLine();
			}			
			// close file
			in.close();
			
			// convert from List<Integer> to int[]
			int n = temp.size();
			if (n > 0) {
				res = new int[n];
				for (int i = 0; i < n; i++) res[i] = temp.get(i);
				System.out.println("File has been read: " + fileName);
				System.out.println("n: " + n);				
			}			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return res;
	}
	// read sparse matrix
	public DoubleMatrix2D readSparseDataFile(String fileName, int n, int dim) {
		DoubleMatrix2D res = new SparseDoubleMatrix2D(n, dim);
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String str = "";
			for (int i = 0; i < n; i++) {
				str = in.readLine();
				System.out.print((i+1) + ": ");
				String[] items = str.split(",");
				for (int j = 0; j < dim; j++) {
					double temp = Double.parseDouble(items[j]);
					res.set(i, j, temp);
					System.out.print(temp + " ");
				}
				System.out.print("\n");
			}
			// close file
			in.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return res;
	}
	
	// output
	public void printMap(Map<Integer, Double> a) {
		for (int i=0;i<a.size();i++) System.out.println(i + ": " + a.get(i));
	}
	public void printVector(int[] a) {
		for (int i=0;i<a.length;i++) System.out.println(i + ": " + a[i]);
	}
	public void printVector(Integer[] a) {
		for (int i=0;i<a.length;i++) System.out.println(i + ": " + a[i]);
	}
	public void printVector(float[] a) {
		for (int i=0;i<a.length;i++) System.out.println(i + ": " + a[i]);
	}
	public void printVector(double[] a) {
		for (int i=0;i<a.length;i++) System.out.println(i + ": " + a[i]);
	}
	public void printVector(Double[] a) {
		for (int i=0;i<a.length;i++) System.out.println(i + ": " + a[i]);
	}
	public void printMatrix(double[][] d, int nrows, int ncols) {
		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				//System.out.print(d[i][j] + "\t");
				System.out.printf("%5.4f\t", d[i][j]);
			}
			System.out.print("\n");
		}
	}
	public void printMatrix(double[][] d) {
		int rows = d.length;
		int cols = d[0].length;
		if ((rows == 0)||(cols == 0)) return;
		printMatrix(d, rows, cols);
	}
	public void printMatrix(float[][] d) {
		int n = d.length;
		if (n == 0) return;
		int m = d[0].length;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				System.out.printf("%5.4f\t", d[i][j]);
			}
			System.out.print("\n");
		}
	}
	public void printMatrix(DoubleMatrix2D d, int nrows, int ncols) {
		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				System.out.print(d.getQuick(i, j) + "\t");
			}
			System.out.print("\n");
		}
	}
	public void printMatrix(int[][] d) {
		int nrows = d.length;
		if (nrows == 0) return;
		int ncols = d[0].length; 
		if (ncols == 0) return;
		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				System.out.printf("%d\t", d[i][j]);
			}
			System.out.print("\n");
		}
	}
	
	/**
	 * Fills input matrix data with value
	 * @param data - input matrix
	 * @param value - input value
	 */
	public void fillMatrix(double[][] data, double value) {
		int n = data.length;
		if (n == 0) return;
		int m = data[0].length;
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				data[i][j] = value;
			}
		}
	}
	
	/**
	 * Randomly shuffles the input data
	 * @param data - input data
	 * @return
	 */
	public double[][] shuffleData(double[][] data) {
		int n = data.length;
		if (n == 0) return null;
		int m = data[0].length;
		
		// get permutations
		int index[] = getRandomPermutation(n, n);
		
		// here we going to store the result
		double res[][] = new double[n][m];
		
		// for each element
		for (int i = 0; i < n; i++) {
			res[i] = data[index[i]].clone();
		}		
		return res;
	}
	
	/**
	 * Does z-normalization of the input data set
	 * @param data
	 */
	public double[][] znormData(double[][] data) {
		int n = data.length;
		if (n == 0) return null;
		int m = data[0].length;
		
		// output will be here
		double[][] res = new double[n][m];
		
		// find mean value for each feature
		double[] mean_dim = findMeanVector(data);
		// find square of standard deviation
		double[] std_dim = findStd(data, mean_dim);
		// do normalization x_new = (x-mu)/std
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				res[i][j] = (data[i][j]-mean_dim[j])/std_dim[j];
			}
		}
		return res;
	}
	
	// write to disk
	public void saveToFile(double[][] data, String filename) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			int n = data.length;
			int dim = data[0].length;
			
			// write
			String str = "";
			for (int i = 0; i < n; i++) {
				str = "";
				for (int j = 0; j < dim; j++) {
					str += String.format("%5.4f", data[i][j]) + ",";					
				}
				out.write(str.substring(0, str.length()-1) + "\n");
			}
			
			// close file
			out.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	public void saveToFile(double[] data, String filename) {
		if (verbose) System.out.println("Saving data to " + filename);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			int n = data.length;
						
			// write
			String str = "";
			for (int i = 0; i < n; i++) {
				str = String.format("%5.4f", data[i]) + "\n";
				out.write(str);
			}
			
			// close file
			out.close();
			if (verbose) System.out.println("Done saving data");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	public void saveToFile(float[] data, String filename) {
		if (verbose) System.out.println("Saving data to " + filename);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			int n = data.length;
						
			// write
			String str = "";
			for (int i = 0; i < n; i++) {
				str = String.format("%5.4f", data[i]) + "\n";
				out.write(str);
			}
			
			// close file
			out.close();
			if (verbose) System.out.println("Done saving data");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
}
