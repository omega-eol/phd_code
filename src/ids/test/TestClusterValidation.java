package ids.test;

import ids.clustering.model.Distance;
import ids.clustering.utils.ClusterUtils;
import ids.clustering.utils.ClusterValidation;
import ids.utils.CommonUtils;

public class TestClusterValidation {

	private static ClusterUtils clusterUtils;
	private static CommonUtils utils;
	private static ClusterValidation clusterValid;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestNMI();
	}
	
	private static void TestNMI() {
		if (clusterUtils == null) clusterUtils = new ClusterUtils(true);
		if (utils == null) utils = new CommonUtils(true);
		if (clusterValid == null) clusterValid = new ClusterValidation(true);
		
		// read clustering result
		String classFileName = "C:\\_Research\\_Framework\\Phd_code\\Test\\card\\m.data";
		int[] m = clusterUtils.convertDataMatrixToIDX(utils.readDataFile(classFileName));
				
		// read clustering result
		String idxFileName = "C:\\_Research\\_Framework\\Phd_code\\Test\\card\\idx.data";
		int[] idx = clusterUtils.convertDataMatrixToIDX(utils.readDataFile(idxFileName));
		
		// read class distribution
		String dataFileName = "C:\\_Research\\_Framework\\Phd_code\\Test\\card\\ndata.data";
		double[][] data = utils.readDataFile(dataFileName);
		
		double mu = clusterValid.findNMI(idx, m);
		System.out.printf("Java: NMI: %5.4f\n", mu);
		
	}
	
	private static void TestDunnIndex() {
		if (clusterUtils == null) clusterUtils = new ClusterUtils(true);
		if (utils == null) utils = new CommonUtils(true);
		if (clusterValid == null) clusterValid = new ClusterValidation(true);
		
		int k = 2;
		
		// read clustering result
		String idxFileName = "C:\\_Research\\_Framework\\Phd_code\\Test\\idx.data";
		int[] idx = clusterUtils.convertDataMatrixToIDX(utils.readDataFile(idxFileName));
		
		// read class distribution
		String dataFileName = "C:\\_Research\\_Framework\\Phd_code\\Test\\cl_data.data";
		double[][] data = utils.readDataFile(dataFileName);
		
		// get Dunn`s Index
		double dunn_index = clusterValid.findDunnIndex(data, idx, k, Distance.SQEUCLIDEAN);
		System.out.printf("Dunn`s Index: %5.10f\n", dunn_index);
	}

	private static void TestAdultDunnIndex() {
		if (clusterUtils == null) clusterUtils = new ClusterUtils(true);
		if (utils == null) utils = new CommonUtils(true);
		if (clusterValid == null) clusterValid = new ClusterValidation(true);
		
		int k = 2;
		
		// read clustering result
		String idxFileName = "C:\\_Research\\_Framework\\Phd_code\\Test\\adult\\idx.data";
		int[] idx = clusterUtils.convertDataMatrixToIDX(utils.readDataFile(idxFileName));
		
		// read class distribution
		String dataFileName = "C:\\_Research\\_Framework\\Phd_code\\Test\\adult\\adult.data";
		double[][] data = utils.readDataFile(dataFileName);
		
		// get Dunn`s Index
		double dunn_index = clusterValid.findDunnIndex(data, idx, k, Distance.SQEUCLIDEAN);
		System.out.printf("Dunn`s Index: %5.10f\n", dunn_index);
	}
	
}
