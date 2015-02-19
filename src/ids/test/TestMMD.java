package ids.test;

import ids.utils.CommonUtils;
import ids.utils.MMDUtils;

import java.util.Random;

public class TestMMD {

	// utilities
	private static CommonUtils utils = new CommonUtils(true);
	
	public static void main(String[] args) {
		irisDataSetTest();
	}

	public static void irisDataSetTest() {
		double[][] x = utils.readDataFile("datasets/iris/iris_t1.csv");
		double[][] y = utils.readDataFile("datasets/iris/iris_t2.csv");
				
		// find MMD
		MMDUtils mmdUtils = new MMDUtils(x, y, 0.5, 0.05);
		System.out.println("MMD is " + mmdUtils.getMMD() + ", eps = " + mmdUtils.getEps());
	}
	
	public void heartDataSetTest() {
		double x[][] = getNumericalDataHR();
		double y[][] = getCategoricalDataHR();
		
		// find MMD
		MMDUtils mmdUtils = new MMDUtils(x, y, 0.5, 0.05);
		System.out.println("MMD is " + mmdUtils.getMMD() + ", eps = " + mmdUtils.getEps());
	}
	
	public void simpleTest() {
		int m = 100;
		double x[] = new double[m];
		double y[] = new double[m];
		Random r = new Random();
		
		// populate the array
		for (int i = 0; i < m; i++) {
			x[i] = r.nextDouble();
			y[i] = r.nextDouble();
			//System.out.println("x[" + i + "] = " + x[i] + ", y[" + i + "] = " + y[i]);
		}
		y = x.clone();
		
		// find MMD
		MMDUtils mmdUtils = new MMDUtils(x, y, 0.5, 0.95);
		System.out.println("MMD is " + mmdUtils.getMMD() + ", eps = " + mmdUtils.getEps());
	}
	
	// HEART DATA SET
	// get numerical data 
	private double[][] getNumericalDataHR() {
		int n = 270;
		int dim = 6;
		double data[][] = utils.readDataFile("C:\\_Research\\Datasets\\Heart\\ndata_p.data", n, dim);
		return data;
	}	
	// get categorical data
	private static double[][] getCategoricalDataHR() {
		int n = 270;
		int dim = 7;
		double data[][] = utils.readDataFile("C:\\_Research\\Datasets\\Heart\\cdata.data", n, dim);
		return data;
	}
	
}
