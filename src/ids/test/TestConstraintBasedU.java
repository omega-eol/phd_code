package ids.test;

import ids.clustering.model.Distance;
import ids.clustering.model.Domain;
import ids.clustering.model.ObjectiveFunctionType;
import ids.clustering.utils.ClusterValidation;
import ids.framework.ConstraintBasedU;
import ids.framework.ExperimentParams;
import ids.utils.CommonUtils;

public class TestConstraintBasedU {

	public static void main(String[] args) {
		Test_Iris();
	}
	
	public static void Test_Heart() {
		int k = 2;
		
	}
	
	public static void Test_Iris() {
		int k = 3;
		
		// load utilities
		CommonUtils utils = new CommonUtils(false);
		
		// read class labels
		int[] class_labels = utils.readVectorFromFile("datasets/iris/iris_class.csv");
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile("datasets/iris/iris_t1.csv");
		d1.k = k;
		d1.name = "Iris data set, T1";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		d1.number_constraints = 2;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile("datasets/iris/iris_t2.csv");
		d2.k = k;
		d2.name = "Iris data set, T2";
		d2.number_of_iterations = 1;
		d2.distance = Distance.SQEUCLIDEAN;
		d2.number_constraints = 2;
		
		// parameters
		ExperimentParams par = new ExperimentParams();
		par.maximum_number_iterations = 30;
		par.minimum_number_iterations = 10;
		par.verbose = true;
		par.debug = false;
		par.useTC = true;
		par.keepCentroids = true;
		
		// framework
		ConstraintBasedU fw = new ConstraintBasedU(d1, d2, par, ObjectiveFunctionType.CONST_WEIGHTS);
		fw.Start();

		// get the value of the objective functions
		double[] d1_obj = fw.getD1_objF();
		double[] d2_obj = fw.getD2_objF();
		// save it hard drive
		utils.saveToFile(d1_obj, "datasets/iris/results/d1_obj.csv");
		utils.saveToFile(d2_obj, "datasets/iris/results/d2_obj.csv");
		
		// Evaluate clustering
		ClusterValidation valid = new ClusterValidation();
		double d1_nmi = valid.findNMI(fw.getDomain1().idx, class_labels);
		double d2_nmi = valid.findNMI(fw.getDomain2().idx, class_labels);
		System.out.printf("Domain 1 NMI: %5.4f\nDomain 2 NMI: %5.4f\n", d1_nmi, d2_nmi);
	}
		
}
