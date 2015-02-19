package ids.test;

import cern.colt.Arrays;
import ids.clustering.model.Distance;
import ids.clustering.utils.ClusterUtils;
import ids.utils.CommonUtils;
import ids.utils.UniqueResult;

public class TestCommonUtils {

	
	private static CommonUtils utils;
	//private static double[][] input = {{1.0, 5.5, 13.2}, {0.2, .04, 4.5}};
	private static double[][] input = {{1.0, 5.5, 13.2}, {0.2, .04, 4.5}, {3.4, 3.4, 4.3}};
	
	private static double[] input1 = {1.2, 2.0, 0.5, 4.5, .3, 2.3};
	
	public static void main(String[] args) {
		utils = new CommonUtils(true);
		
		// test matrix transpose		
		TestMatrixTranspose();
		
		// maximum values in matrix
		TestMaxValueSearch();
		
		// find stats
		//TestStats();
		
		// read file
		//TestReadFile();
		
		// Test Distance
		//TestDistance();
		
		// Test linspace
		TestLinspace();
		
		// Test mean vector
		TestMeanVector();
		
		// Test square standard deviation
		TestSqStd();
		
		// Test Unique Result
		TestUniqueResult();
	}
	
	public static void TestUniqueResult() {
		double[] uq_input = {0.02, 0, 1, 2, 0.02, 1, 0, 0, 1, 1, 10, 10, 11, 11, 3, 3, 2, 11, 0};
		// 0 - 4
		// 0.02 - 2
		// 1 - 4
		// 2 - 2
		// 3 - 2
		UniqueResult<Double> ur = utils.findUnique(uq_input);
		System.out.println("Domain:");
		utils.printVector(ur.domain);
		System.out.println("Frequencies");
		utils.printVector(ur.frequency);
		
		int[] uq_input_int = {2, 0, 1, 2, 2, 1, 0, 0, 1, 1, 10, 10, 11, 11, 3, 3, 2, 11, 0};
		// 0 - 4
		// 1 - 4
		// 2 - 4
		// 3 - 2
		UniqueResult<Integer> ur_int = utils.findUnique(uq_input_int);
		System.out.println("Domain:");
		utils.printVector(ur_int.domain);
		System.out.println("Frequencies");
		utils.printVector(ur_int.frequency);
	}
	
	public static void TestSqStd() {
		double[][] a = { {0, 0.1, 1, 10, 34, 45.6} , {0, 0.1, -5, 12, 23, 12.6 } };
		double[] a_std = utils.findStd(a);
		System.out.println("Square of Std Vector");
		utils.printVector(a_std);
	}
	
	public static void TestMeanVector() {
		double[][] a = { {0, 0.1, 1, 10, 34, 45.6} , {0, 0.1, -5, 12, 23, 12.6 } };
		double[] a_mean = utils.findMeanVector(a);
		System.out.println("Mean Vector");
		utils.printVector(a_mean);
	}
	
	public static void TestDistance() {
		System.out.println("\nTest Distance");
		
		ClusterUtils clusterUtils = new ClusterUtils(true);
		int k = 2;
		
		// read clustering result
		String idxFileName = "C:\\_Research\\_Framework\\Phd_code\\Test\\javaDistTest\\q.data";
		int[] idx = clusterUtils.convertDataMatrixToIDX(utils.readDataFile(idxFileName));
		
		// read class distribution
		String dataFileName = "C:\\_Research\\_Framework\\Phd_code\\Test\\javaDistTest\\d.data";
		double[][] data = utils.readDataFile(dataFileName);
		int n = data.length;
		
		// find distance vector first
		float[] pd = utils.getDistance(data, Distance.EUCLIDEAN);
		System.out.println("Distance vector");
		utils.printVector(pd);
		
		// convert to square form
		float[][] sf = utils.ConvertToSquareForm(pd, n);
		System.out.println("Distance Matrix");
		utils.printMatrix(sf);
	}
	
	public static void TestStats() {
		System.out.println("Input Vector");
		utils.printVector(input1);
		
		System.out.println("Mean value: " + utils.findMean(input1));
		System.out.println("Sum: " + utils.findSum(input1));
	}
	
	public static void TestMatrixTranspose() {
		System.out.println("Input Matrix");
		utils.printMatrix(input);
		
		double[][] inputT = utils.transpose(input);
		System.out.println("Transpose Matrix");
		utils.printMatrix(inputT);
	}

	public static void TestMaxValueSearch() {
		double[] max_res_dim1 = utils.getMaxValue(input, 1);
		System.out.println("Maximum values along dim 1");
		utils.printVector(max_res_dim1);
		
		double[] max_res_dim2 = utils.getMaxValue(input, 2);
		System.out.println("Maximum values along dim 2");
		utils.printVector(max_res_dim2);		
	}
	
	public static void TestReadFile() {
		double[][] data = utils.readDataFile("C:\\_Research\\Datasets\\Heart\\ndata_p.data");
	}
	
	public static void TestLinspace() {
		double[] x = utils.linspace(0, 12, 5);
		double[] y = {0, 0.1, 11, 3.4, 2.5, 0.5, 12, 7.8, 10.5, 4.0, 4.1, 3.9, 4.0, 1.2, 1.3};
		System.out.println("X vector from 0 to 4:");
		utils.printVector(x);
		
		System.out.println("PDF of y vector");
		int[] pdf = utils.histc(y, x);
		utils.printVector(pdf);
	}
}
