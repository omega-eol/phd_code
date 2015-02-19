package ids.clustering.algorithm;

import ids.clustering.model.Distance;
import ids.clustering.model.ObjectiveFunctionType;

public class HMRFKmeansParams {
	
	// verbose
	public boolean verbose = false;
	public boolean debug = false;
	
	// Distance
	public Distance distanceFunction = Distance.SQEUCLIDEAN;
	// Maximum distance in data set
	public double phi_d = 0;
	
	// maximum number of iterations
	public int max_number_of_iterations = 100;
	// maximum number of iterations in ICM algorithm
	public int max_number_of_iterations_icm = 100;
	
	// Constraints
	public double[][] constraints = null;
	
	// Cluster centroids
	public double[][] centeroids = null;
	
	// cluster selection
	public boolean useTC = true;
	public boolean keepCentroids = false;

	// membership from other domain
	public int[] otherIDX = null;
	
	/**
	 * Type of the objective function in HMRF-Kmeans
	 */
	public ObjectiveFunctionType obj_type = ObjectiveFunctionType.NO_WEIGTHS;
	
	public HMRFKmeansParams() { }
	public HMRFKmeansParams(Distance distance, ObjectiveFunctionType obj_type) {
		this.distanceFunction = distance;
		this.obj_type = obj_type;
	}
}
