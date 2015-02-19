package ids.test;

import ids.clustering.algorithm.HMRFKmeansParams;
import ids.clustering.algorithm.HMRFKmeansU;
import ids.clustering.model.Distance;
import ids.clustering.model.ObjectiveFunctionType;
import ids.clustering.utils.ClusterValidation;
import ids.utils.CommonUtils;

public class TestHMRFKmeansU {

	public static void main(String[] args) {
		System.out.println("Start testing Universal HMRF-Kmeans");
		heart_HMRF_cdata();
	}
	
	private static void heart_HMRF_cdata() {
		// Utilities
		CommonUtils utils = new CommonUtils(false);
		
		double[][] cdata = utils.readDataFile("datasets/heart/cdata.csv");
		//int[] class_column = utils.readVectorFromFile("datasets/heart/class_column.csv");
		
		// number of clusters
		int k = 2;
		
		HMRFKmeansParams par = new HMRFKmeansParams();
		par.constraints = utils.readDataFile("datasets/heart/constraints.csv");
		par.verbose = true;
		par.useTC = true;
		par.debug = false;
		par.distanceFunction = Distance.MATCH;
		HMRFKmeansU kmeans = new HMRFKmeansU(cdata, k, par);
		kmeans.cluster();		
	}
	
	private static void heart_HMRF_ndata() {
		// Utilities
		CommonUtils utils = new CommonUtils(false);
		
		double[][] ndata = utils.readDataFile("datasets/heart/ndata.csv");
		//double[][] cdata = utils.readDataFile("datasets/heart/cdata.csv");
		//int[] class_column = utils.readVectorFromFile("datasets/heart/class_column.csv");
		
		// number of clusters
		int k = 2;
		
		HMRFKmeansParams par = new HMRFKmeansParams();
		par.constraints = utils.readDataFile("datasets/heart/constraints.csv");
		par.verbose = true;
		par.useTC = true;
		par.debug = false;
		HMRFKmeansU kmeans = new HMRFKmeansU(ndata, k, par);
		kmeans.cluster();		
	}
	
	private static void testIris_Kmeans() {
		int n = 150;
		CommonUtils utils = new CommonUtils(false);
		
		// read iris data set
		double[][] d1_data = utils.readDataFile("datasets/iris/iris_t1.csv", n, 2);
		double[][] d2_data = utils.readDataFile("datasets/iris/iris_t2.csv", n, 2);
		
		// actual class labels
		double[][] class_column = utils.readDataFile("datasets/iris/iris_class.csv", 150, 1);
		int[] iris_class = new int[n]; 
		for (int i = 0; i < n; i++) iris_class[i] = (int)class_column[i][0];
		
		// Number of clusters
		int k = 3;
		int[] seeds = {66, 53, 104};
		
		// Domain 1 centroids
		double[][] d1_centroids = utils.getRows(d1_data, seeds);
		double[][] d2_centroids = utils.getRows(d2_data, seeds);
		
		// cluster parameters
		HMRFKmeansParams par = new HMRFKmeansParams();
		par.centeroids = d1_centroids;
		par.verbose = true;
		par.debug = false;
		par.obj_type = ObjectiveFunctionType.CENTROIDS;
		
		// cluster T1
		System.out.println("Cluster T1");
		HMRFKmeansU d1_kmeans =  new HMRFKmeansU(d1_data, k, par);
		d1_kmeans.cluster();
		
		// cluster T1
		par.centeroids = d2_centroids;
		System.out.println("Cluster T1");
		HMRFKmeansU d2_kmeans =  new HMRFKmeansU(d2_data, k, par);
		d2_kmeans.cluster();
	}
	
	private static void testIris_HMRFKmeans() {
		int n = 150;
		CommonUtils utils = new CommonUtils(false);
		ClusterValidation valid = new ClusterValidation(true);
		
		// read iris data set
		double[][] d1_data = utils.readDataFile("datasets/iris/iris_t1.csv", n, 2);
		double[][] d2_data = utils.readDataFile("datasets/iris/iris_t2.csv", n, 2);
		
		// actual class labels
		double[][] class_column = utils.readDataFile("datasets/iris/iris_class.csv", 150, 1);
		int[] iris_class = new int[n]; 
		for (int i = 0; i < n; i++) iris_class[i] = (int)class_column[i][0];
		
		// read constraints
		double[][] constraints = utils.readDataFile("datasets/iris/iris_constraints.csv");
		
		
		// Number of clusters
		int k = 3;
		int[] seeds = {66, 53, 104};
		
		// Domain 1 centroids
		double[][] d1_centroids = utils.getRows(d1_data, seeds);
		double[][] d2_centroids = utils.getRows(d2_data, seeds);
		
		// cluster parameters
		HMRFKmeansParams par = new HMRFKmeansParams();
		//par.centeroids = d1_centroids;
		par.constraints = constraints;
		par.verbose = true;
		par.debug = true;
		par.obj_type = ObjectiveFunctionType.NO_WEIGTHS;
		
		// cluster T1
		System.out.println("Cluster T1");
		HMRFKmeansU d1_kmeans =  new HMRFKmeansU(d1_data, k, par);
		d1_kmeans.cluster();
		
		double nmi = valid.findNMI(d1_kmeans.getIDX(), iris_class);
		
//		// cluster T1
//		par.centeroids = d2_centroids;
//		System.out.println("Cluster T1");
//		HMRFKmeansU d2_kmeans =  new HMRFKmeansU(d2_data, k, par, ObjectiveFunctionType.KMEANS);
//		d2_kmeans.cluster();
	}
	
}
