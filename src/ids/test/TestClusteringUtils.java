package ids.test;

import ids.clustering.utils.ClusterUtils;
import ids.utils.CommonUtils;
import ids.utils.HungarianAlgorithm;

public class TestClusteringUtils {

	private static ClusterUtils clusterUtils;
	private static CommonUtils utils;
	
	/**
	 * Tests Clustering utils
	 */
	public static void main(String[] args) {
		TestClusterUtils();
	}
	
	@SuppressWarnings("static-access")
	public static void TestClusterUtils() {
		if (clusterUtils == null) clusterUtils = new ClusterUtils(true);
		if (utils == null) utils = new CommonUtils(true);
		
		int k = 4;
				
		// read clustering result
		String idxFileName = "C:\\_Research\\_Framework\\Phd_code\\Test\\idx.data";
		int[] idx = clusterUtils.convertDataMatrixToIDX(utils.readDataFile(idxFileName));
		
		// read class distribution
		String mFileName = "C:\\_Research\\_Framework\\Phd_code\\Test\\m.data";
		int[] m = clusterUtils.convertDataMatrixToIDX(utils.readDataFile(mFileName));
		
		double[][] J0 = clusterUtils.findJaccardIndex(idx, m, k, k);
		utils.printMatrix(J0);
		
		double[][] J = clusterUtils.findJaccardIndex(idx, m);
		System.out.println("Jaccard Index");
		utils.printMatrix(J);
		
		double[][] invJ = utils.invert(J);
		System.out.println("Inverted Jaccard Index");
		utils.printMatrix(invJ);
		
		HungarianAlgorithm h = new HungarianAlgorithm();
		int[][] match = h.hgAlgorithm(J, "max");
		System.out.println("Cluster - class assigment");
		utils.printMatrix(match);
		
		/*
		double[][] testJ = {{0.2, 0.1, 0.8}, {0.7, 0.1, 0.1}};
		int[][] testMatch = h.hgAlgorithm(testJ, "max");
		System.out.println("Cluster - class assigment");
		utils.printMatrix(testMatch);
		*/
		
		int[] temp = clusterUtils.findClusterCorrespondence(m, idx);
		System.out.println("Relabeling the idx");
		utils.printVector(temp);
		
		double[][] J_final = clusterUtils.findJaccardIndex(m, temp);
		System.out.println("Jaccard Index - final");
		utils.printMatrix(J_final);

	}

}
