package ids.test;

import ids.clustering.algorithm.HMRFKmeans;
import ids.clustering.algorithm.HMRFKmeansParams;
import ids.clustering.algorithm.HMRFWKmeans;
import ids.clustering.algorithm.HMRFWKmeansV2;
import ids.utils.CommonUtils;

public class TestHMRFWKmeans {

	private static double[][] data;
	private static double[][] constraints;
	private static int k = 3;
	private static CommonUtils utils;
	
	public static void main(String[] args) {
		// utilities
		utils = new CommonUtils(false);
		
		// read the data
		data = utils.readDataFile("C:\\_Research\\Datasets\\Iris\\iris.data", 150, 4);
		// read the constraints
		constraints = utils.readDataFile("C:\\_Research\\Datasets\\Iris\\constraints.data");
		
		// test simple hmrf-kmeans
		//TestHMRF();
		
		// test w hmrf-kmeans
		//TestHMRFW();
		
		// test w hmrf-kmeans with other IDX
		//TestHMRFW_idx();
		
		// test w hmrf-kmeans with other IDX and another distance function (6/6/2013)
		TestHMRFW_V2_idx();
	}
	
	public static void TestHMRF() {
		HMRFKmeansParams par = new HMRFKmeansParams();
		par.constraints = constraints;
		par.verbose = true;
		
		HMRFKmeans kmeans = new HMRFKmeans(data, k, par);
		kmeans.cluster();
	}
	
	public static void TestHMRFW() {
		HMRFKmeansParams par = new HMRFKmeansParams();
		par.constraints = constraints;
		par.verbose = true;
		
		HMRFWKmeans wkmeans = new HMRFWKmeans(data, k, par);
		wkmeans.cluster();
	}

	public static void TestHMRFW_idx() {
		int[] d1_index = {0, 1};
		double[][] d1_data = utils.getColumns(data, d1_index);
		
		int[] d2_index = {2, 3};
		double[][] d2_data = utils.getColumns(data, d2_index);
		
		// cluster the first domain
		HMRFKmeansParams par = new HMRFKmeansParams();
		par.constraints = constraints;
		par.verbose = true;
		
		HMRFWKmeans d1_wkmeans = new HMRFWKmeans(d1_data, k, par);
		d1_wkmeans.cluster();
		
		par.otherIDX = d1_wkmeans.getIDX();
		par.debug = true;
		// cluster second domain
		HMRFWKmeans d2_wkmeans = new HMRFWKmeans(d2_data, k, par);
		d2_wkmeans.cluster();		
	}

	public static void TestHMRFW_V2_idx() {
		int[] d1_index = {0, 1};
		double[][] d1_data = utils.getColumns(data, d1_index);
		
		int[] d2_index = {2, 3};
		double[][] d2_data = utils.getColumns(data, d2_index);
		
		// cluster the first domain
		HMRFKmeansParams par = new HMRFKmeansParams();
		par.constraints = constraints;
		par.verbose = true;
		
		HMRFWKmeansV2 d1_wkmeans = new HMRFWKmeansV2(d1_data, k, par);
		d1_wkmeans.cluster();
		
		par.otherIDX = d1_wkmeans.getIDX();
		par.debug = true;
		// cluster second domain
		HMRFWKmeansV2 d2_wkmeans = new HMRFWKmeansV2(d2_data, k, par);
		d2_wkmeans.cluster();		
	}
	
}
