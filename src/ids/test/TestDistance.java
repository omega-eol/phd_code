package ids.test;

import ids.clustering.model.Distance;
import ids.utils.CommonUtils;

public class TestDistance {
	
	public static void main(String[] args) {
		
		double[] a = {0.2, 0.5, 11.0, 21.0, 0.01};
		double[] b = {0.5, 45, .01, 1.0, 3.01};
	
		double distance = 0;
		CommonUtils utils = new CommonUtils(true);
		
		// sqEuclidean distance
		distance = utils.getDistance(a, b, Distance.SQEUCLIDEAN);
		System.out.println("sqEuclidean distance is " + distance);
		
		// Cosine distance
		distance = utils.getDistance(a, b, Distance.COSINE);
		System.out.println("Cosine distance is " + distance);
		
		// Euclidean distance
		distance = utils.getDistance(a, b, Distance.EUCLIDEAN);
		System.out.println("Euclidean distance is " + distance);
		
		// Match distance
		double[] a_m = {2.0, 5.0, 1.0, 1.0, 1.0};
		double[] b_m = {5.0, 5.0, 1.0, 1.0, 3.0};
		distance = utils.getDistance(a_m, b_m, Distance.MATCH);
		System.out.println("Match distance is " + distance);
		
	}
}
