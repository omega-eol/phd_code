package ids.test;

import ids.clustering.model.Distance;
import ids.clustering.model.View;
import ids.clustering.utils.ClusterValidation;
import ids.framework.MultiviewKmeans;
import ids.utils.CommonUtils;

public class TestMultiviewSpherical {

	public static void main(String[] args) {
		TestHeartDataset();
	}
	
	private static void TestHeartDataset() {
		// load utilities
		CommonUtils utils = new CommonUtils(false);
		
		// input data
		View view1 = new View(utils.readDataFile("datasets/heart/ndata.csv"), Distance.SQEUCLIDEAN);
		View view2 = new View(utils.readDataFile("datasets/heart/cdata.csv"), Distance.MATCH);
		int k = 2;
		
		// Multiview Spherical K-means
		MultiviewKmeans ms = new MultiviewKmeans(view1, view2, k, true);
		int[] idx = ms.Cluster();

		// Evaluate clustering
		int[] class_labels = utils.readVectorFromFile("datasets/heart/class_column.csv");
		ClusterValidation valid = new ClusterValidation();
		double nmi = valid.findNMI(idx, class_labels);
		System.out.printf("MultiView Kmeans NMI: %5.4f\n", nmi);		
		
	}

}
