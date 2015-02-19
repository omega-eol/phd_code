package ids.test;

import ids.clustering.model.Distance;
import ids.clustering.model.Domain;
import ids.clustering.utils.ClusterValidation;
import ids.framework.ConstraintBased;
import ids.framework.ConstraintWBased;
import ids.framework.ExperimentParams;
import ids.utils.CommonUtils;

public class TestConstraintBased {
	
	public static void main(String[] args) {
		
		// test simple case
		//TestCBased();
		
		// test fancy case
		//TestCWBased();	
		
		// test weight based with credit card data set
		TestCWBased_CreditCard();
	}
	
	public static void TestCBased() {
		int n = 270;
		int k = 2;
		
		CommonUtils utils = new CommonUtils(false);
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile("C:\\_Research\\Datasets\\Heart\\ndata_p.data", n, 6);
		d1.k = k;
		d1.name = "Iris data set";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		d1.number_constraints = 5;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile("C:\\_Research\\Datasets\\Heart\\cdata.data", n, 7);
		d2.k = k;
		d2.name = "Iris data set";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
		d2.number_constraints = 5;
		
		// parameters
		ExperimentParams par = new ExperimentParams();
		par.maximum_number_iterations = 30;
		par.minimum_number_iterations = 10;
		par.verbose = true;
		
		// framework
		ConstraintBased f = new ConstraintBased(d1, d2, par);
		f.Start();
	}
	
	public static void TestCWBased_CreditCard() {
		int n = 690;
		int k = 2;
		
		CommonUtils utils = new CommonUtils(false);
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile("C:\\_Research\\Datasets\\CreditCard\\ndata_p.data", n, 6);
		d1.k = k;
		d1.name = "Iris data set";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		d1.number_constraints = 0;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile("C:\\_Research\\Datasets\\CreditCard\\cdata.data", n, 9);
		d2.k = k;
		d2.name = "Iris data set";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
		d2.number_constraints = 0;
		
		// parameters
		ExperimentParams par = new ExperimentParams();
		par.maximum_number_iterations = 30;
		par.minimum_number_iterations = 10;
		par.verbose = true;
		
		// framework
		ConstraintWBased fw = new ConstraintWBased(d1, d2, par);
		fw.Start();
		
		// validate. Find DB indices for numerical and categorical domains
		ClusterValidation cv = new ClusterValidation(false);
		double db1 = cv.findDBIndex(d1.data, fw.getDomain1().idx, fw.getDomain1().centroids, d1.k, d1.distance);
		double db2 = cv.findDBIndex(d2.data, fw.getDomain2().idx, fw.getDomain2().centroids, d2.k, d2.distance);
		System.out.println("DB index for numerical domain: " + db1);
		System.out.println("DB index for categorical domain: " + db2);
	}
	
	public static void TestCWBased() {
		int n = 270;
		int k = 2;
		
		CommonUtils utils = new CommonUtils(false);
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile("C:\\_Research\\Datasets\\Heart\\ndata_p.data", n, 6);
		d1.k = k;
		d1.name = "Iris data set";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		d1.number_constraints = 0;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile("C:\\_Research\\Datasets\\Heart\\cdata.data", n, 7);
		d2.k = k;
		d2.name = "Iris data set";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
		d2.number_constraints = 0;
		
		// parameters
		ExperimentParams par = new ExperimentParams();
		par.maximum_number_iterations = 30;
		par.minimum_number_iterations = 10;
		par.verbose = true;
		
		// framework
		ConstraintWBased fw = new ConstraintWBased(d1, d2, par);
		fw.Start();
	}
	
}
