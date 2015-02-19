package ids.clustering.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import ids.clustering.model.Clusters;
import ids.clustering.model.Distance;
import ids.clustering.model.ObjectiveFunctionType;
import ids.clustering.utils.ClusterUtils;
import ids.utils.CommonUtils;
import ids.utils.ConstraintsUtils;
import ids.utils.FindMaxDistance;
import ids.utils.SearchResult;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

/**
 * This is the universal HMRF-Kmeans clustering algorithm which suppose to work with any
 * distance function
 * @author Artur.Abdullin
 *
 */
public class HMRFKmeansU {
	
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
	private FindMaxDistance mDistance;
	// logger
	private Logger log = Logger.getLogger(getClass().getName());
	
	// distance
	private Distance distanceFunction = Distance.SQEUCLIDEAN;
	private double phi_d = 0;
	
	// cluster membership
	private int[] idx = null;;
	private int[] prev_idx = null;
	public int[] getIDX() { return idx; }
	
	// value of the objective function
	private double objF = 0;
	public double getObjF() { return objF; }
	private ArrayList<Double> objF_log = new ArrayList<Double>();
	public double[] getObjF_all() {
		int v_tt = objF_log.size();
		double[] res = new double[v_tt];
		for (int i = 0; i < v_tt; i++) res[i] = objF_log.get(i);
		return res;
	}
	private double objF_term1 = 0;
	public double getObjF_term1() { return objF_term1; }
	
	// cluster selection
	private boolean useTC = true;
	
	// number of iterations
	private int number_of_iterations = 0;
	// maximum number of iterations
	private int max_number_of_iterations = 100;
	// maximum number of iterations in ICM algorithm
	private int max_number_of_iterations_icm = 100;
	
	// cluster centroids
	private double[][] centroids = null;
	public double[][] getCentroids() { return centroids; }
	
	// constraints
	private DoubleMatrix2D Mconstraints = null;
	private DoubleMatrix2D Cconstraints = null;
	private double[][] constraints_list = null;
	
	// objective function type, by default this is the basic HMRF-Kmeans with w = 1
	private ObjectiveFunctionType obj_type = ObjectiveFunctionType.NO_WEIGTHS;
	
	// Constructors
	public HMRFKmeansU(double[][] inputData, int numberOfClusters, HMRFKmeansParams par) {
		// check input parameters
		checkInputParameters(inputData, numberOfClusters);
		
		// parse parameters
		parseParams(par);
		
		if (verbose) System.out.println("HMRF-Kmeans:");		
		// Initialization
		Initialization();
	}
	public HMRFKmeansU(int[][] inputData, int numberOfClusters, HMRFKmeansParams par) {
		if (inputData == null) System.exit(1);
		int n = inputData.length;
		if (n == 0) System.exit(1);
		int m = inputData[0].length;
		double[][] res = new double[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) res[i][j] = (double)inputData[i][j];
		}
		
		// check input parameters
		checkInputParameters(res, numberOfClusters);
		
		// parse parameters
		parseParams(par);
		
		if (verbose) System.out.println("HMRF-Kmeans:");		
		// Initialization
		Initialization();
	}
	
	private void checkInputParameters(double[][] inputData, int numberOfClusters) {
		this.data = inputData;		
		if (this.data == null) {
			System.out.println("Data set is not set.");
			System.exit(1);
		}
		
		this.k = numberOfClusters;
		if (this.k < 2) {
			System.out.println("Number of clusters cannot be set less then 2 (current os " + k + ")");
			System.exit(1);
		}
		
		this.n = this.data.length;
		if (this.n == 0) {
			System.out.println("The number of data objects is 0.");
			System.exit(1);
		}
		
		this.dim = this.data[0].length;		
		if (this.dim == 0) {
			System.out.println("The dimension of data objects is 0.");
			System.exit(1);
		}
	}
	
	private void parseParams(HMRFKmeansParams par) {
		// type of objective function
		this.obj_type = par.obj_type;
		
		// verbose
		this.verbose = par.verbose;
		this.debug = par.debug;
		
		// distance
		this.distanceFunction = par.distanceFunction;
		this.phi_d = par.phi_d;
		
		// max iterations
		this.max_number_of_iterations = par.max_number_of_iterations;
		this.max_number_of_iterations_icm = par.max_number_of_iterations_icm;
		
		// constraints
		this.constraints_list = par.constraints;
		
		// centroids
		this.centroids = par.centeroids;
		
		// cluster selection
		this.useTC = par.useTC;
	}
	
	private void Initialization() {
		// utilities
		utils = new CommonUtils(false);
		clusterUtils = new ClusterUtils(false);
		
		// initialize the membership vector
		idx = new int[n];
		Arrays.fill(idx, -1);
		prev_idx = new int[n];
		Arrays.fill(prev_idx, -1);
	
		// find the maximum distance in the data set
		if (phi_d == 0) {
			mDistance = new FindMaxDistance(false);
			phi_d = mDistance.getMaxDistance(data, n, dim, distanceFunction);
			if (debug) log.info("The maximum distance in the data set is: " + phi_d);
		}
		
		// centroids message
		String message = "Using user specified centroids";
		
		// constraint list
		if (constraints_list != null) {
			if (verbose) System.out.println("Constraints are detected");
			parseConstraintList();
			
			if (useTC) {
				if (verbose) System.out.println("Using TC to propogate constrains");
				ConstraintsUtils constraintsUtils = new ConstraintsUtils(debug);
				Mconstraints = constraintsUtils.TransitiveClosure(Mconstraints);
				
				// get neighborhoods
				int[] neighborhoodLambda = constraintsUtils.getNeighborhood();
				// number of neighborhoods
				int lambda = utils.getMaxValue(neighborhoodLambda).getValue() + 1;
				if (debug) log.info("Number of lambda neighborhoods is " + lambda);
				
				// infer cannot-link constraints
				constraintsUtils.inferCannotLinkConstraints(Cconstraints, neighborhoodLambda, lambda);
				
				// get new centroids based on lambda neigh
				if (centroids == null) { // replace centroids only iff its null
					message = "Using TC to generate the centroids";
					centroids = clusterSelection(neighborhoodLambda, lambda);
				}
				
			}
		} else {
			if (verbose) System.out.println("No constraints are detected");
		}
		
		// constraints
		if (centroids == null) {			
			// randomly generate centroids
			message = "Generating random centroids";
			centroids = clusterUtils.generateRandomClusterCentroids(data, k);
		}
		if (verbose) System.out.println(message);
		if (debug) utils.printMatrix(centroids);	
		
		// cluster memberships without any constraints
		idx = clusterUtils.getClusterMemberships(data, centroids, distanceFunction);
	}
	
	/**
	 * This function parses constraint list in the following format
	 * |point 1| |point2| |constraint_type|
	 * constraint type could have two values: 1 for must-link constraints and 2 for
	 * cannot-link constraints
	 */
	private void parseConstraintList() {
		if (debug) log.info("Parsing constraints..");
		Mconstraints = new SparseDoubleMatrix2D(n, n);
		Cconstraints = new SparseDoubleMatrix2D(n, n);
		for (int i = 0; i < constraints_list.length; i++) {
			int a = (int)constraints_list[i][0] - 1; // -1 since data ID starts from 0 but from 1 in the constant file
			int b = (int)constraints_list[i][1] - 1;
			
			if (((int)constraints_list[i][2])==1) { // must-link constraints
				Mconstraints.setQuick(a, b, 1.0);
				Mconstraints.setQuick(b, a, 1.0);
				if (debug) {
					double d = utils.getDistance(data[a], data[b], distanceFunction);
					log.info("Creating a must-link constraint between object " + a + " and " + b + ". Distance between points is " + d);
				}
			} else if (((int)constraints_list[i][2])==2) { // cannot-link constraints
				Cconstraints.setQuick(a, b, 1.0);
				Cconstraints.setQuick(b, a, 1.0);
				if (debug) {
					double d = utils.getDistance(data[a], data[b], distanceFunction);
					log.info("Creating a cannot-link constraint between object " + a + " and " + b + ". Distance between points is " + d);
				}
			} else {
				log.severe("Cannot find constraints type");
			}			
		} // end for loop
		if (debug) log.info("Parsing constraints..done.");
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
		if (verbose) {
			System.out.println("");
			System.out.println("Maximum distance: " + phi_d);
			System.out.println("Number of clusters: " + k);
			System.out.println("Number of samples: " + n + ", with " + dim + " features");
			System.out.println("Distance funciton: " + distanceFunction);
			if (constraints_list != null) {
				// get number of cannot link constraints
				IntArrayList c_rowList = new IntArrayList();
				IntArrayList c_columnList = new IntArrayList();
				DoubleArrayList c_values = new DoubleArrayList();
				Cconstraints.getNonZeros(c_rowList, c_columnList, c_values);
				int n_cannot_link = c_values.size();
				
				IntArrayList m_rowList = new IntArrayList();
				IntArrayList m_columnList = new IntArrayList();
				DoubleArrayList m_values = new DoubleArrayList();
				Mconstraints.getNonZeros(m_rowList, m_columnList, m_values);
				int n_must_link = m_values.size() - n;
				
				System.out.println("Number of Must Constraints: " + n_must_link);
				System.out.println("Number of Cannot Constraints: " + n_cannot_link);
			}
			System.out.println("Number of iterations: " + number_of_iterations);
			System.out.println("Objective function value: " + objF);
			System.out.println("");
		}
	}
	
	private double[][] clusterSelection(int[] nLambda, int lambda) {
		double[][] c = new double[k][dim];
		if (lambda > k) {
			Clusters cluster_lambda = clusterUtils.getClusterCentoids(data, n, dim, nLambda, lambda, distanceFunction);
			double[][] c_lambda = cluster_lambda.centroids;
			
			// find the largest subgraph and normalized it
			int[] w = cluster_lambda.clusterSizes;
			double[] dw  = new double[lambda];
			SearchResult<Integer> sr = utils.getMaxValue(w);
			int max_w = sr.getValue();
			if (max_w != 0) {
				for (int i = 0; i < w.length; i++) {
					dw[i] = (double)w[i]/max_w;
				}
			} else {
				log.severe("ACHTUNG! Division by zero! The maximum neigh size is 0.");
			}
			
			// do the weighted farther first algorithm
			int[] c_index = new int[k];
			for (int i = 0; i < k; i++) c_index[i] = -1;
			int cnc = 0; // current number of clusters
			c_index[cnc] = sr.getIndex(); // first cluster is the biggest cluster
			c[cnc] = c_lambda[sr.getIndex()];
			
			// distance between cluster`s centroids
			double[] dc = new double[lambda];
			
			while ((cnc+1) < k) {
				for (int i = 0; i < lambda; i++) {
					// the current candidate
					double[] x = c_lambda[i];
					dc[i] = 0;
					if (!utils.isContained(c_index, cnc+1, i)) {
						for (int j = 0; j < cnc+1; j++) {
							double[] y = c_lambda[c_index[j]];
							dc[i] += dw[c_index[j]] * dw[i] * utils.getDistance(x, y, distanceFunction);
						}
					}
				}
				cnc++;
				SearchResult<Double> srd = utils.getMaxValue(dc);
				c_index[cnc] = srd.getIndex();
				c[cnc] = c_lambda[srd.getIndex()];
			}
			
		} else {
			int[] index = utils.getRandomPermutation(n, k-lambda);
			double[][] temp = new double[lambda][dim];
			Clusters cl = clusterUtils.getClusterCentoids(data, n, dim, nLambda, lambda, distanceFunction);
			temp = cl.centroids;
			for (int i = 0; i < lambda; i++) {
				c[i] = temp[i];
			}
			for (int i = 0; i < k-lambda; i++) {
				c[lambda + i] = data[index[i]];
			}			
		}
		return c;
	}
	
	private void E_step() {
		int[] prev_idx_icm = idx.clone();
		// number of iterations
		int v_tt = 0;
		
		while (true) {
			v_tt++;
			objF = 0;
			objF_term1 = 0; // value of the objective function but only first term
			if (debug) log.info("Starting ICM iteration " + v_tt);
			
			// randomize points the data set
			int[] rand_index = utils.getRandomPermutation(n, n);
			
			for (int i = 0; i<n; i++) {
				int current_point_index = rand_index[i];
				
				double f_min = Double.MAX_VALUE;
				int j_min = 0;
				
				// assign points to clusters
				for (int j=0; j<k; j++) {
					double f = getObjectiveFunction(current_point_index, j);
					//System.out.println("Distance is  " + f);
					if (f<f_min) {
						f_min = f;
						j_min = j;
					}
				}
				//System.out.println("Adding " + f_min + " to the objective function " + objF);
				idx[current_point_index] = j_min;
				objF = objF + f_min;
				
				//how would kmeans work
				/*
				double f_term1_min = Double.MAX_VALUE;
				for (int j=0; j<k; j++) {
					double f_term = utils.getDistance(data[current_point_index], centroids[j], distanceFunction);
					if (f_term<f_term1_min) f_term1_min = f_term;
				}
				objF_term1 = objF_term1 + f_term1_min;
				*/
				objF_term1 = objF_term1 +  utils.getDistance(data[current_point_index], centroids[j_min], distanceFunction);
			}
			
			// break loop
			if (Arrays.equals(prev_idx_icm, idx)) {
				if (debug) {
					log.info("ICM algorithm has been finished in (" + v_tt + ") iterations");
					log.info("Value of the objective function is " + objF);
				}
				break;
			}
			if (v_tt >= max_number_of_iterations_icm) {
				log.warning("Reached maximum number of iterations (" + max_number_of_iterations_icm + ") in ICM algorithm");
				break;
			}
			
			prev_idx_icm = idx.clone();
		}
		
		if (debug) {
			//log.info("E-Step: the value of the objective function: " + objF);
			//System.out.println("Membership vector: ");
			//Arrays.toString(idx);
		}
		
		if (verbose) System.out.printf("E-Step: the value of the objective function: %5.4f (%5.4f)\n", objF, objF_term1);
		objF_log.add(objF);
	}
	
	private void M_step() {
		Clusters clusters = clusterUtils.getClusterCentoids(data, n, dim, idx, k, distanceFunction);
		centroids = clusters.centroids;
		if (debug) { 
			log.info("M-Step:");
			log.info("Cluster centroids: ");
			utils.printMatrix(centroids, k, dim);
		}
	}
	
	private double getObjectiveFunction(int point_index, int cluster_index) {
		double kmeans_part = 0;
		double must_link_part = 0;
		double cannot_link_part = 0;
		
		// simple k-means
		kmeans_part = utils.getDistance(data[point_index], centroids[cluster_index], distanceFunction);
		
		// Different type of objective functions
		if (obj_type == ObjectiveFunctionType.CENTROIDS) {
			return kmeans_part;
		} else if (obj_type == ObjectiveFunctionType.NO_WEIGTHS) {
			
			// check if there are any constraints
			if (constraints_list == null) {
				if (debug) {
					System.out.println("The constraint list is NULL. EXIT.");
					log.info("The constraint list is NULL. EXIT.");
				}
				return kmeans_part;
			}
			
			// get must-link constraints for the current point
			DoubleMatrix1D target = Mconstraints.viewRow(point_index);
			IntArrayList cols = new IntArrayList();
			DoubleArrayList values = new DoubleArrayList();
			target.getNonZeros(cols, values);		
			// must-link constraints part
			for (int i = 0; i < cols.size(); i++) {
				int other_point = cols.get(i); // index of the connected point
				if ((cluster_index != idx[other_point])&(point_index != other_point)) {
					must_link_part += utils.getDistance(data[point_index], data[other_point], distanceFunction);
					/*
					if (debug) log.info("Must-link: Objective function has been modified - points " 
							+ point_index + " and " + other_point + " in cluster " + cluster_index + ". Current value of the must link part: " + must_link_part);
					*/
				}
			}
			
			// get cannot-link constraints for the current point
			target = Cconstraints.viewRow(point_index);
			cols = new IntArrayList();
			values = new DoubleArrayList();
			target.getNonZeros(cols, values);		
			// cannot-link constraints part
			for (int i = 0; i < cols.size(); i++) {
				int other_point = cols.get(i);
				if (cluster_index == idx[other_point]) {
					cannot_link_part += (phi_d - utils.getDistance(data[point_index], data[other_point], distanceFunction));
					/*
					if (debug) log.info("Cannot-link: Objective function has been modified - points "
							+ point_index + " and " + other_point + " in cluster " + cluster_index + ". Current value of: " + cannot_link_part);
					*/
				}
			}
			
			// return the sum of all components
			double res = kmeans_part + must_link_part + cannot_link_part;
			if (debug) {
				/*
				if (kmeans_part >0) System.out.println("k-means part of the distance is " + kmeans_part);
				if (must_link_part >0) System.out.println("must-link part of the distance is " + must_link_part);
				if (cannot_link_part >0) System.out.println("cannot-link part of the distance is " + cannot_link_part);
				System.out.println("The total distance between point " + point_index + " and cluster " + cluster_index + ": " + res);
				*/
			}
			//if (kmeans_part >0) System.out.println("k-means part of the distance is " + kmeans_part);
			//if (must_link_part >0) System.out.println("must-link part of the distance is " + must_link_part);
			//if (cannot_link_part >0) System.out.println("cannot-link part of the distance is " + cannot_link_part);
			//System.out.println("The total distance between point " + point_index + " and cluster " + cluster_index + ": " + res);
			return res;
		} else if (obj_type == ObjectiveFunctionType.CONST_WEIGHTS) {
			
			double w = 0.0439/0.3937;
			
			// check if there are any constraints
			if (constraints_list == null) {
				if (debug) {
					System.out.println("The constraint list is NULL. EXIT.");
					log.info("The constraint list is NULL. EXIT.");
				}
				return kmeans_part;
			}
			
			// get must-link constraints for the current point
			DoubleMatrix1D target = Mconstraints.viewRow(point_index);
			IntArrayList cols = new IntArrayList();
			DoubleArrayList values = new DoubleArrayList();
			target.getNonZeros(cols, values);		
			// must-link constraints part
			for (int i = 0; i < cols.size(); i++) {
				int other_point = cols.get(i); // index of the connected point
				if ((cluster_index != idx[other_point])&(point_index != other_point)) {
					must_link_part += w*utils.getDistance(data[point_index], data[other_point], distanceFunction);
				}
			}
			
			// get cannot-link constraints for the current point
			target = Cconstraints.viewRow(point_index);
			cols = new IntArrayList();
			values = new DoubleArrayList();
			target.getNonZeros(cols, values);		
			// cannot-link constraints part
			for (int i = 0; i < cols.size(); i++) {
				int other_point = cols.get(i);
				if (cluster_index == idx[other_point]) {
					cannot_link_part += w*(phi_d - utils.getDistance(data[point_index], data[other_point], distanceFunction));
				}
			}
			
			// return the sum of all components
			double res = kmeans_part + must_link_part + cannot_link_part;
			return res;
			
		} else {
			System.out.println("Could not find the specified type of objective function. EXIT");
			log.severe("Could not find the specified type of objective function. EXIT");
			System.exit(1);
			return 0;
		}
		
	}
	
}

