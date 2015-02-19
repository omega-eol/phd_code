package ids.experiment.dataset;

import ids.clustering.model.Distance;
import ids.clustering.model.Domain;
import ids.clustering.utils.ValidationIndex;
import ids.framework.OptimalConstraints;
import ids.framework.OptimalConstraints.OptimalConstraintsPair;
import ids.utils.CommonUtils;

public class NumberConstraints {

	/**
	 * find the optimal number of constraints using the provided data set
	 * @param args
	 */
	public static void main(String[] args) {
		// initialization
		int k = 2;
		int start_c = 5;
		int end_c = 40;
		
		// utilities
		CommonUtils utils = new CommonUtils(false);
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile("datasets/heart/ndata.csv");
		d1.k = k;
		d1.name = "Domain 1";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile("datasets/heart/cdata.csv");
		d2.k = k;
		d2.name = "Domain 2";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
				
		OptimalConstraints opt = new OptimalConstraints(d1, d2, null, start_c, end_c);
		ValidationIndex vi = ValidationIndex.DB;
		OptimalConstraintsPair p = opt.findOnce(5, 5, vi);
		System.out.printf("d1: %5.4f, d2: %5.4f\n", p.dd1, p.dd2);
	}

}
