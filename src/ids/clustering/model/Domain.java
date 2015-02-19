package ids.clustering.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Domain implements Serializable {
	
	/**
	 * The domain name
	 */
	public String name;
	//public String type;
	
	/**
	 * Input data
	 */
	public double[][] data;
	
	/**
	 * Number of clusters
	 */
	public int k;
	
	/**
	 * Cluster centroids
	 */
	public double[][] centroids;
	
	/**
	 * Cluster membership
	 */
	public int[] idx;
	
	/**
	 * Cluster membership of previous iteration
	 */
	public int[] old_idx;
	
	/**
	 * Current value of the objective function
	 */
	public double objF;
	
	/**
	 * Number of iteration of an algorithm before constraint or seed exchange
	 */
	public int number_of_iterations;
	
	/**
	 * Current distance measure
	 */
	public Distance distance;
	
	/**
	 * Pre-computed distance of the data set
	 */
	public double[] pd;
	
	/**
	 * Maximum distance in the data set
	 */
	public double max_pd;
	
	/**
	 * number of constraints to be generated per cluster, the total number of points is
	 * k * number_constraints
	 */
	public int number_constraints; 
	
	public Domain() {}
	
	/**
	 * Creates an instance of Domain class
	 * @param data - input data set
	 * @param distance - distance function
	 * @param k - number of clusters
	 */
	public Domain(double[][] data, Distance distance, int k) {
		this.data = data;
		this.distance = distance;
		this.k = k;
	}
}
