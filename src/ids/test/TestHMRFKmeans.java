package ids.test;

import ids.clustering.algorithm.HMRFKmeans;
import ids.clustering.algorithm.HMRFKmeansParams;
import ids.clustering.model.Distance;
import ids.clustering.utils.ClusterValidation;
import ids.utils.CommonUtils;

public class TestHMRFKmeans {
	// data set
	private static double[][] data;
	private static double[][] cdata;
	private static int n = 0;
	private static int dim = 0;
	private static int k = 0;
	
	// utilities
	private static CommonUtils utils = new CommonUtils(true);
	
	// constraints
	private static double[][] constraints;
	
	public static void main(String[] args) {
		System.out.println("Start testing HMRF-Kmeans");
			
		// test find unique function from common utils
		//TestUnique();
		
		// cluster it with HMRF-Kmeans
		//TestBasicKMEANS();
		
		// cluster it with HMRF-Kmeans with constraints
		TestConstraints();
		
		// cluster categorical part of Heart data set
		//TestCategoricalClustering();
	}
	
	public static void TestCategoricalClustering() {
		cdata = getCategoricalData();
		
		HMRFKmeansParams par = new HMRFKmeansParams();
		par.distanceFunction = Distance.MATCH;
		
		HMRFKmeans kmeans = new HMRFKmeans(cdata, 2, par);
		kmeans.cluster();
		
		// find db index
		ClusterValidation valid = new ClusterValidation();
		double db = valid.findDBIndex(cdata, kmeans.getIDX(), kmeans.getCentroids(), k, par.distanceFunction);
		System.out.println("DB index: " + db);
	}
	
	public static void TestUnique() {
		cdata = getCategoricalData();
		
		double[] col_vector = new double[cdata.length];
		for (int i = 0; i < col_vector.length; i++) col_vector[i] = cdata[i][1];
		//UniqueResult ur = utils.findUnique(col_vector);
	}
	
	public static void TestBasicKMEANS() {
		data = getData();
		k = 2;
		
		HMRFKmeansParams par = new HMRFKmeansParams();
		par.verbose = false;		
		par.distanceFunction = Distance.SQEUCLIDEAN;
				
		HMRFKmeans kmeans = new HMRFKmeans(data, k, par);
		kmeans.cluster();
		
		// find db index
		ClusterValidation valid = new ClusterValidation();
		double db = valid.findDBIndex(data, kmeans.getIDX(), kmeans.getCentroids(), k, Distance.SQEUCLIDEAN);
		System.out.println("DB index: " + db);
	}
	public static void TestConstraints() {
		data = getData();
		constraints = getConstraints();
		
		HMRFKmeansParams par = new HMRFKmeansParams();
		par.constraints = constraints;
		par.verbose = false;
		par.useTC = true;
		par.max_number_of_iterations = 2;
		
		HMRFKmeans kmeans = new HMRFKmeans(data, k, par);
		kmeans.cluster();
	}
	
	// get data 
	private static double[][] getData() {
		if (data==null) {
			n = 270;
			dim = 6;
			k = 2;
			data = utils.readDataFile("C:\\_Research\\Datasets\\Heart\\ndata_p.data", n, dim);
		} 
		return data;
	}
	
	// get categorical data
	private static double[][] getCategoricalData() {
		if (cdata==null) {
			n = 270;
			dim = 7;
			k = 2;
			cdata = utils.readDataFile("C:\\_Research\\Datasets\\Heart\\cdata.data", n, dim);
		} 
		return cdata;
	}
	
	// get constraints
	private static double[][] getConstraints() {
		if (constraints==null) {
			constraints = utils.readDataFile("C:\\_Research\\Datasets\\Iris\\constraints.data", 7, 3);
		}
		return constraints;
	}
	
}
