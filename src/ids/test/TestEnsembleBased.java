package ids.test;

import ids.clustering.model.Distance;
import ids.clustering.model.Domain;
import ids.clustering.utils.ClusterUtils;
import ids.clustering.utils.ClusterValidation;
import ids.framework.EnsembleBased;
import ids.utils.CommonUtils;

public class TestEnsembleBased {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Run it on Iris data set
		TestIris();
	}
	
	public static void TestIris() {
		// utilities
		CommonUtils utils = new CommonUtils(false);
		ClusterValidation valid = new ClusterValidation(true);
		ClusterUtils clusterUtils = new ClusterUtils(false);
		
		// input data sets
		int k = 3;
		double[][] data_d1 = utils.readDataFile("datasets/iris/iris_t1.csv");
		double[][] data_d2 = utils.readDataFile("datasets/iris/iris_t2.csv");
		Domain domain1 = new Domain(data_d1, Distance.SQEUCLIDEAN, k);
		Domain domain2 = new Domain(data_d2, Distance.SQEUCLIDEAN, k);
		
		// test ensemble framework with voting
		EnsembleBased eb = new EnsembleBased(domain1, domain2, 10, 10);
		eb.debug = true;
		int[] finalIDS_voting = eb.Do_Voting();
		int[] finalIDS_clustering = eb.Do_Clustering(k);
		
		// validation
		int[] class_labels = utils.readVectorFromFile("datasets/iris/iris_class.csv");
		double nmi_voting = valid.findNMI(utils.addToVector(finalIDS_voting, 1), class_labels);
		double nmi_clustering = valid.findNMI(clusterUtils.findClusterCorrespondence(class_labels, finalIDS_clustering), class_labels);
	}

}
