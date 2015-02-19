package ids.test;

import ids.clustering.algorithm.Kmeans;
import ids.utils.CommonUtils;

public class TestKmeans {

	public static void main(String[] args) {
		runKmeansIrisT12();
	}
	
	private static void RunKmeans() {		
		CommonUtils utils = new CommonUtils(false);
		
		// read class distribution
		String dataFileName = "D:\\Repo\\phd_code\\Test\\adult\\ndata.data";
		double[][] data = utils.readDataFile(dataFileName);
		
		int k = 2;		
		Kmeans km = new Kmeans(data, k);
		km.debugRun();		
	}
	
	/**
	 * Run Kmeans on T1 and T2
	 */
	private static void runKmeansIrisT12() {
		int n = 150;
		CommonUtils utils = new CommonUtils(false);
		
		// read iris data set
		double[][] d1_data = utils.readDataFile("datasets/iris/iris_t1.csv", n, 2);
		double[][] d2_data = utils.readDataFile("datasets/iris/iris_t2.csv", n, 2);
		double[][] class_column = utils.readDataFile("datasets/iris/iris_class.csv", 150, 1);
		int[] iris_class = new int[n]; 
		for (int i = 0; i < n; i++) iris_class[i] = (int)class_column[i][0];
		
		// Number of clusters
		int k = 3;
		int[] seeds = {66, 53, 104};
		
		// Domain 1
		double[][] d1_centroids = utils.getRows(d1_data, seeds);
		Kmeans d1_km = new Kmeans(d1_data, k, d1_centroids);
		d1_km.cluster();
		
		// Domain 2
		double[][] d2_centroids = utils.getRows(d2_data, seeds);
		Kmeans d2_km = new Kmeans(d2_data, k, d2_centroids);
		d2_km.cluster();
		
	}
}
