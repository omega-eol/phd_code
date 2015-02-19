package ids.clustering.algorithm;

import ids.clustering.model.Clusters;
import ids.clustering.model.Distance;
import ids.clustering.utils.ClusterUtils;
import ids.utils.CommonUtils;

import java.util.Arrays;
import java.util.logging.Logger;

public class Kmeans {

	// logger
	private Logger log;
	
	// data
	private double[][] data;
	private int n = 0;
	private int dim = 0;
	private int k = 0; // number of clusters
	private boolean verbose = false;
	private boolean debug = false;
	
	// Utilities
	private CommonUtils utils;
	private ClusterUtils clusterUtils;
	
	// distance
	private Distance distanceFunction = Distance.SQEUCLIDEAN;
	
	// cluster membership
	private int[] idx;
	private int[] prev_idx;
	public int[] getIDX() {
		return idx;
	}
	// cluster centroids
	private double[][] centroids = null;
	public double[][] getCentroids() {
		return centroids;
	}
	// value of the objective function
	private double objF = 0;
	public double getObjF() { return objF; }
	
	// number of iterations
	private int number_of_iterations = 0;
	// maximum number of iterations
	private int max_number_of_iterations = 100;
	
	// Constructors
	public Kmeans(double[][] inputData, int numberOfClusters) {
		CheckParameters(inputData, numberOfClusters);
		// Initialization
		Initialization();
	}	
	public Kmeans(double[][] inputData, int numberOfClusters, Distance distance) {
		CheckParameters(inputData, numberOfClusters);
		this.distanceFunction = distance;
		// Initialization
		Initialization();
	}
	public Kmeans(double[][] inputData, int numberOfClusters, double[][] cent) {
		CheckParameters(inputData, numberOfClusters);
		this.centroids = cent;
		// Initialization
		Initialization();
	}
	public Kmeans(double[][] inputData, int numberOfClusters, double[][] cent, Distance distance) {
		CheckParameters(inputData, numberOfClusters);
		this.distanceFunction = distance;
		this.centroids = cent;
		// Initialization
		Initialization();
	}
	
	private void CheckParameters(double[][] inputData, int numberOfClusters) {
		this.data = inputData;
		if (this.data == null) {
			System.out.println("Data set is not set.");
			System.exit(1);
		}
		this.k = numberOfClusters;
		this.n = this.data.length;
		this.dim = this.data[0].length;
		if (this.n == 0) {
			System.out.println("The number of data objects is 0.");
			System.exit(1);
		}
		if (this.dim == 0) {
			System.out.println("The dimension of data objects is 0.");
			System.exit(1);
		}
		if (this.k < 2) {
			System.out.println("Number of clusters cannot be set less then 2 (current os " + k + ")");
			System.exit(1);
		}
	}
	
	private void Initialization() {
		// utilities
		utils = new CommonUtils(false);
		clusterUtils = new ClusterUtils(false);
		
		// logger
		log = Logger.getLogger(getClass().getName());
		
		// initialize the membership vector
		idx = new int[n];
		Arrays.fill(idx, -1);
		prev_idx = idx.clone();
		
		// if centroids are still null then generate them randomly
		if (centroids == null) {
			centroids = new double[k][dim];
			int[] points = utils.getRandomPermutation(n, k);
			for (int i = 0; i<k; i++) {
				centroids[i] = data[points[i]];
			}
			if (debug) {
				log.info("Randomly genereted Centroids:");
				utils.printMatrix(centroids, k, dim);
			}			
		}
		
		// cluster memberships without any constraints
		idx = clusterUtils.getClusterMemberships(data, centroids, distanceFunction);
	}
	
	public void debugRun() {
		this.verbose = true;
		this.debug = true;
		cluster();
	}
	
	// Cluster - main procedure
	public void cluster() {
		while (true) {
			// one cycle
			number_of_iterations++;
			if (debug) log.info("Start iteration " + number_of_iterations);
			
			// EM algorithm
			E_step();
			M_step();
			
			// stop condition
			if (Arrays.equals(idx, prev_idx)) break;
			if (number_of_iterations >= max_number_of_iterations) {
				if (debug) System.out.println("The maximum number of iterations (" + max_number_of_iterations + ") has been reached");
				break;
			}
			prev_idx = idx.clone();
		}
		
		// output
		System.out.println("");
		System.out.println("Kmeans:");
		System.out.println("Number of clusters: " + k);
		System.out.println("Number of samples: " + n + ", with " + dim + " features");
		System.out.println("Distance funciton: " + distanceFunction);
		System.out.println("Number of iterations: " + number_of_iterations);
		System.out.println("Objective function value: " + objF);
		System.out.println("");
	}
	
	private void E_step() {
		objF = 0;
		for (int i = 0; i<n; i++) {
			double f_min = Double.MAX_VALUE;
			int j_min = 0;
			
			// assign points to the closest cluster
			for (int j=0; j<k; j++) {
				double f = getObjectiveFunction(i, j);
				if (f<f_min) {
					f_min = f;
					j_min = j;
				}
			}
			idx[i] = j_min;
			objF = objF + f_min;
		}		
		if (debug) log.info("E-Step: the value of the objective function: " + objF);
	}
	
	private void M_step() {
		Clusters clusters = clusterUtils.getClusterCentoids(data, idx, k, distanceFunction);
		centroids = clusters.centroids;
		if (debug) { 
			log.info("M-Step:");
			log.info("Cluster centroids: ");
			utils.printMatrix(centroids, k, dim);
		}
	}
	
	private double getObjectiveFunction(int point_index, int cluster_index) {
		return utils.getDistance(data[point_index], centroids[cluster_index], distanceFunction);
	}
}
