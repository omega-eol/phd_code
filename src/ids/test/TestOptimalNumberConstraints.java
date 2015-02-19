package ids.test;

import ids.clustering.model.Distance;
import ids.clustering.model.Domain;
import ids.clustering.model.PairDoubles;
import ids.clustering.model.PairDoublesMatrix;
import ids.experiment.dataset.OptimalNumberConstraints;
import ids.utils.CommonUtils;

public class TestOptimalNumberConstraints {

	public static void main(String[] args) {
		
		// number of clusters
		int k = 2;
		
		// load utilities
		CommonUtils utils = new CommonUtils(false);
		
		// read class labels
		int[] class_labels = utils.readVectorFromFile("datasets/heart/class_column.csv");
		//class_labels = utils.addToVector(class_labels, -1);
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile("datasets/heart/ndata.csv");
		d1.k = k;
		d1.name = "Heart data set: numerical domain";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		d1.number_constraints = -1;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile("datasets/heart/cdata.csv");
		d2.k = k;
		d2.name = "Heart data set: categorical domain";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
		d2.number_constraints = -1;
		
		// Find optimal number of constraints
		OptimalNumberConstraints onc = new OptimalNumberConstraints(d1, d2, 10);
		PairDoubles res = onc.RunOnceNMI(5, 5, class_labels);
		
	}
	
	public static void TestAdultDataAllNMI() {
		
		// number of clusters
		int k = 2;
		
		// load utilities
		CommonUtils utils = new CommonUtils(false);
		
		// read class labels
		int[] class_labels = utils.readVectorFromFile("datasets/adult/class_column.csv");
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile("datasets/adult/ndata.csv");
		d1.k = k;
		d1.name = "Adult data set: numerical domain";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		d1.number_constraints = -1;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile("datasets/adult/cdata.csv");
		d2.k = k;
		d2.name = "Adult data set: categorical domain";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
		d2.number_constraints = -1;
		
		// Find optimal number of constraints
		OptimalNumberConstraints onc = new OptimalNumberConstraints(d1, d2, 2);
		PairDoublesMatrix res = onc.RunAllNMI(5, 6, class_labels);
		
		// save to hard drive
		String domain1_filename = "datasets/adult/results/number_constraints_d1_nmi.csv";
		String domain2_filename = "datasets/adult/results/number_constraints_d2_nmi.csv";
		utils.saveToFile(res.v1, domain1_filename);
		utils.saveToFile(res.v2, domain2_filename);
		
	}
	
	public static void TestHearDataAllNMI() {
		
		// number of clusters
		int k = 2;
		
		// load utilities
		CommonUtils utils = new CommonUtils(false);
		
		// read class labels
		int[] class_labels = utils.readVectorFromFile("datasets/heart/class_column.csv");
		class_labels = utils.addToVector(class_labels, -1);
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile("datasets/heart/ndata.csv");
		d1.k = k;
		d1.name = "Heart data set: numerical domain";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		d1.number_constraints = -1;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile("datasets/heart/cdata.csv");
		d2.k = k;
		d2.name = "Heart data set: categorical domain";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
		d2.number_constraints = -1;
		
		// Find optimal number of constraints
		OptimalNumberConstraints onc = new OptimalNumberConstraints(d1, d2, 10);
		PairDoublesMatrix res = onc.RunAllNMI(2, 30, class_labels);
		
		// save to hard drive
		String domain1_filename = "C://Repo//phd_code//Constraint//optimalNumberConstraints/results/heart/number_constraints_d1_nmi.csv";
		String domain2_filename = "C://Repo//phd_code//Constraint//optimalNumberConstraints/results/heart/number_constraints_d2_nmi.csv";
		utils.saveToFile(res.v1, domain1_filename);
		utils.saveToFile(res.v2, domain2_filename);
		
	}
	
	public static void TestCreditDataAllNMI() {
		// number of clusters
		int k = 2;
		
		// load utilities
		CommonUtils utils = new CommonUtils(false);
		
		// read class labels
		int[] class_labels = utils.readVectorFromFile("datasets/credit/class_column.csv");
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile("datasets/credit/ndata.csv");
		d1.k = k;
		d1.name = "Credit card data set: numerical domain";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		d1.number_constraints = -1;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile("datasets/credit/cdata.csv");
		d2.k = k;
		d2.name = "Credit card set: categorical domain";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
		d2.number_constraints = -1;
		
		// Find optimal number of constraints
		OptimalNumberConstraints onc = new OptimalNumberConstraints(d1, d2, 10);
		PairDoublesMatrix res = onc.RunAllNMI(2, 30, class_labels);
		
		// save to hard drive
		//String domain1_filename = "C://Repo//phd_code//Constraint//optimalNumberConstraints/results/credit_card/number_constraints_d1_nmi.csv";
		String domain1_filename = "C://_Research//_Framework//Phd_code//Constraint//optimalNumberConstraints//results//credit_card//number_constraints_d1_nmi.csv";
		//String domain2_filename = "C://Repo//phd_code//Constraint//optimalNumberConstraints/results/credit_card/number_constraints_d2_nmi.csv";
		String domain2_filename = "C://_Research//_Framework//Phd_code//Constraint//optimalNumberConstraints//results//credit_card//number_constraints_d2_nmi.csv";
		utils.saveToFile(res.v1, domain1_filename);
		utils.saveToFile(res.v2, domain2_filename);
		
	}
	
	public static void TestHearDataAllDB() {
		
		// number of clusters
		int k = 2;
		
		// load utilities
		CommonUtils utils = new CommonUtils(false);
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile("datasets/heart/ndata.csv");
		d1.k = k;
		d1.name = "Heart data set: numerical domain";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		d1.number_constraints = -1;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile("datasets/heart/cdata.csv");
		d2.k = k;
		d2.name = "Heart data set: categorical domain";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
		d2.number_constraints = -1;
		
		// Find optimal number of constraints
		OptimalNumberConstraints onc = new OptimalNumberConstraints(d1, d2, 10);
		PairDoublesMatrix res = onc.RunAllDB(2, 30);
		
		// save to hard drive
		String domain1_filename = "C://Repo//phd_code//Constraint//optimalNumberConstraints/results/heart/number_constraints_d1.csv";
		String domain2_filename = "C://Repo//phd_code//Constraint//optimalNumberConstraints/results/heart/number_constraints_d2.csv";
		utils.saveToFile(res.v1, domain1_filename);
		utils.saveToFile(res.v2, domain2_filename);
		
	}
	
	public static void TestCreditDataAllDB() {
		
		// number of clusters
		int k = 2;
		
		// load utilities
		CommonUtils utils = new CommonUtils(false);
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile("datasets/credit/ndata.csv");
		d1.k = k;
		d1.name = "Credit card data set: numerical domain";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		d1.number_constraints = -1;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile("datasets/credit/cdata.csv");
		d2.k = k;
		d2.name = "Credit card data set: categorical domain";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
		d2.number_constraints = -1;
		
		// Find optimal number of constraints
		OptimalNumberConstraints onc = new OptimalNumberConstraints(d1, d2, 10);
		PairDoublesMatrix res = onc.RunAllDB(2, 30);
		
		// save to hard drive
		//String domain1_filename = "C://Repo//phd_code//Constraint//optimalNumberConstraints/results/credit_card/number_constraints_d1_db.csv";
		String domain1_filename = "C://_Research//_Framework//Phd_code//Constraint//optimalNumberConstraints//results//credit_card//number_constraints_d1_db.csv";
		//String domain2_filename = "C://Repo//phd_code//Constraint//optimalNumberConstraints/results/credit_card/number_constraints_d2_db.csv";
		String domain2_filename = "C://_Research//_Framework//Phd_code//Constraint//optimalNumberConstraints//results//credit_card//number_constraints_d2_db.csv";
		utils.saveToFile(res.v1, domain1_filename);
		utils.saveToFile(res.v2, domain2_filename);
		
	}
	
	
	public static void TestHearDataOnceDB() {
		
		// number of clusters
		int k = 2;
		
		// load utilities
		CommonUtils utils = new CommonUtils(false);
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile("datasets/heart/ndata.csv");
		d1.k = k;
		d1.name = "Heart data set: numerical domain";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		d1.number_constraints = -1;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile("datasets/heart/cdata.csv");
		d2.k = k;
		d2.name = "Heart data set: categorical domain";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
		d2.number_constraints = -1;
		
		// Find optimal number of constraints
		OptimalNumberConstraints onc = new OptimalNumberConstraints(d1, d2, 10);
		PairDoubles res = onc.RunOnceDB(10, 10);
	}
}
