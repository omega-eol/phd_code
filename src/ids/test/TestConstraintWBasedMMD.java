package ids.test;

import ids.clustering.model.Distance;
import ids.clustering.model.Domain;
import ids.framework.ConstraintBased;
import ids.framework.ConstraintWBasedMMD;
import ids.framework.ExperimentParams;
import ids.utils.CommonUtils;

public class TestConstraintWBasedMMD {

	public static void main(String[] args) {
		// Test MMD based framework on Iris data set
		TestMMDBasedIris();
	}
	
	public static void TestMMDBasedIris() {
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
		Domain d1 = new Domain();
		d1.data = d1_data;
		d1.k = k;
		d1.name = "Iris data set";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		d1.number_constraints = 2;
		d1.centroids = utils.getRows(d1_data, seeds);
		
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = d2_data;
		d2.k = k;
		d2.name = "Iris data set";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
		d2.number_constraints = 2;
		d2.centroids = utils.getRows(d2_data, seeds);
		
		// parameters
		ExperimentParams par = new ExperimentParams();
		par.maximum_number_iterations = 30;
		par.minimum_number_iterations = 10;
		par.verbose = true;
		par.useTC = false;
		
		// framework
		ConstraintWBasedMMD f = new ConstraintWBasedMMD(d1, d2, par);
		f.Start();
		
	}

}
