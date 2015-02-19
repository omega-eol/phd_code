package ids.clustering.algorithm;

import ids.clustering.model.Clusters;
import ids.clustering.model.Distance;
import ids.clustering.utils.ClusterUtils;
import ids.utils.CommonUtils;
import ids.utils.ConstraintsUtils;
import ids.utils.FindMaxDistance;
import ids.utils.SearchResult;

import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Logger;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

@SuppressWarnings("serial")
public class HMRFWKmeansV2 implements Serializable {
	
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
	private FindMaxDistance mDistance;
	
	// distance
	private Distance distanceFunction = Distance.SQEUCLIDEAN;
	private double phi_d = 0;
	
	// cluster membership
	private int[] idx;
	private int[] prev_idx;
	public int[] getIDX() {
		return idx;
	}
	// membership from the other domain
	private int[] otherIDX; 
	// same as the otherIDX but with relabeled cluster indices 
	private int[] domainIDX;
	private boolean useOtherDomain = false;
	
	// cluster centroids
	private double[][] centroids = null;
	public double[][] getCentroids() {
		return centroids;
	}
	// value of the objective function
	private double objF = 0;
	public double getObjF() { return objF; }
	
	// cluster selection
	private boolean useTC = true;
	private boolean inferCannotLink = true;
	
	// number of iterations
	private int number_of_iterations = 0;
	// maximum number of iterations
	private int max_number_of_iterations = 100;
	// maximum number of iterations in ICM algorithm
	private int max_number_of_iterations_icm = 100;
	
	// constraints
	private DoubleMatrix2D Mconstraints = null;
	private DoubleMatrix2D Cconstraints = null;
	private double[][] constraints_list = null;
	
	// Constructors
	public HMRFWKmeansV2(double[][] inputData, int numberOfClusters, HMRFKmeansParams par) {
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
		
		// parse parameters
		parseParams(par);
		
		// Initialization
		Initialization();
	}	
	
	private void Initialization() {
		// utilities
		utils = new CommonUtils(false);
		clusterUtils = new ClusterUtils(false);
		
		// logger
		log = Logger.getLogger(getClass().getName());
		
		// initialize the membership vector
		idx = new int[n];
		prev_idx = new int[n];
		for (int i=0; i<n; i++) {
			idx[i] = -1;
			prev_idx[i] = -1;
		}
	
		// find the maximum distance in the data set
		if (phi_d == -1) {
			mDistance = new FindMaxDistance(false);
			phi_d = mDistance.getMaxDistance(data, n, dim, distanceFunction);
		}
		if (debug) log.info("Maximum distance in data set is " + phi_d);
		
		// constraints
		if (constraints_list != null) {
			Mconstraints = new SparseDoubleMatrix2D(n, n);
			Cconstraints = new SparseDoubleMatrix2D(n, n);
			for (int i = 0; i < constraints_list.length; i++) {
				int a = (int)constraints_list[i][0] - 1; // -1 since data ID starts from 0 but from 1 in the constant file
				int b = (int)constraints_list[i][1] - 1;
				
				if (((int)constraints_list[i][2])==1) { // must-link constraints
					Mconstraints.setQuick(a, b, 1.0);
					Mconstraints.setQuick(b, a, 1.0);
					if (debug) {
						log.info("Creating must-link constraint between object " + (int)constraints_list[i][0] +
							" and " + (int)constraints_list[i][1]);
					}					
				} else if (((int)constraints_list[i][2])==2) { // cannot-link constraints
					Cconstraints.setQuick(a, b, 1.0);
					Cconstraints.setQuick(b, a, 1.0);
					if (debug) {
						log.info("Creating cannot-link constraint between object " + (int)constraints_list[i][0] +
							" and " + (int)constraints_list[i][1]);
					}
				} else {
					log.severe("Cannot find constraints type");
				}			
			} // end for loop
		}
		
		// use TC only if centroids are set to null
		if (centroids == null) clusterInitialization();
		
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
		
		// other domain
		if (otherIDX != null) {
			useOtherDomain = true;
			domainIDX = clusterUtils.findClusterCorrespondence(idx, otherIDX);			
		}
	}
	
	private void parseParams(HMRFKmeansParams par) {
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
		
		// other domain
		this.otherIDX = par.otherIDX;
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
			System.out.println("HMRF-Kmeans:");
			System.out.println("Number of clusters: " + k);
			System.out.println("Number of samples: " + n + ", with " + dim + " features");
			System.out.println("Distance funciton: " + distanceFunction);
			if (constraints_list != null) System.out.println("Number of constraints: " + constraints_list.length);
			System.out.println("Number of iterations: " + number_of_iterations);
			System.out.println("Objective function value: " + objF);
			System.out.println("");
		}
	}
	
	private void clusterInitialization() {
		if ((useTC)&(constraints_list != null)) {
			// use transitive closure to infer must-link constraints
			ConstraintsUtils constraintsUtils = new ConstraintsUtils(verbose);
			Mconstraints = constraintsUtils.TransitiveClosure(Mconstraints);
			
			// get neighborhoods
			int[] neighborhoodLambda = constraintsUtils.getNeighborhood();
			// number of neighborhoods
			int lambda = utils.getMaxValue(neighborhoodLambda).getValue() + 1;
			if (debug) log.info("Number of lambda neighborhoods is " + lambda);
			
			// infer cannot-link constraints
			if (inferCannotLink) constraintsUtils.inferCannotLinkConstraints(Cconstraints, neighborhoodLambda, lambda);
			
			// get new centroids based on lambda neigh
			centroids = clusterSelection(neighborhoodLambda, lambda);
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
				log.severe("ACHTUNG! Division by zero! The maximum neighborhood size is 0.");
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
			log.info("E-Step: the value of the objective function: " + objF);
			//System.out.println("Membership vector: ");
			//Arrays.toString(idx);
		}
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
		if (constraints_list == null) return kmeans_part;
			
		// get clustering label of the current point in the other domain
		double w_common = 1;
		if (useOtherDomain) {
			//w_common = utils.getDistance(data[point_index], centroids[cluster_index], distanceFunction)/
			//	(utils.getDistance(data[point_index], centroids[domainIDX[point_index]], distanceFunction) + Double.MIN_VALUE);
			if (cluster_index != domainIDX[point_index]) w_common = 0;
			if (debug) System.out.println("w_common: " + w_common);
		}
		
		// get must-link constraints for the current point
		DoubleMatrix1D target = Mconstraints.viewRow(point_index);
		IntArrayList cols = new IntArrayList();
		DoubleArrayList values = new DoubleArrayList();
		target.getNonZeros(cols, values);		
		// must-link constraints part
		for (int i = 0; i < cols.size(); i++) {
			
			// native domain
			int other_point = cols.get(i);
			int cluster_index_other_point = idx[other_point];
			//double[] centroid_other_point = centroids[cluster_index_other_point];
			
			if ((cluster_index != cluster_index_other_point)&(point_index != other_point)) {
				double w_final = 1;
				if (useOtherDomain) {
					// other domain
					//double[] centroid_other_point_domainIDX = centroids[domainIDX[other_point]];
					//double w = utils.getDistance(data[other_point], centroid_other_point, distanceFunction)/
					//		(utils.getDistance(data[other_point], centroid_other_point_domainIDX, distanceFunction) +  Double.MIN_VALUE);
					double w = 0;
					if (cluster_index_other_point == domainIDX[other_point]) w = 1;
					w_final = Math.exp((w_common + w)/2 - 1);
					if (w_final > 1) {
						if (debug) System.out.println("The current weight is higher than 1: " + w_final + "\nSetting it to 1");
						w_final = 1;
					}
				}
				
				must_link_part += w_final*utils.getDistance(data[point_index], data[other_point], distanceFunction);
				if (debug) log.info("Must-link: Objective function has been modified - points " 
						+ point_index + " and " + other_point + " in cluster " + cluster_index + ". Current value of: " + must_link_part + 
						", with w = " + w_final);
			}
		}
		
		// get cannot-link constraints for the current point
		target = Cconstraints.viewRow(point_index);
		cols = new IntArrayList();
		values = new DoubleArrayList();
		target.getNonZeros(cols, values);		
		// cannot-link constraints part
		for (int i = 0; i < cols.size(); i++) {
			
			// native domain
			int other_point = cols.get(i);
			int cluster_index_other_point = idx[other_point];
			//double[] centroid_other_point = centroids[cluster_index_other_point];
			
			if (cluster_index == cluster_index_other_point) {
				double w_final = 1;			
				if (useOtherDomain) {
					// other domain
					//double[] centroid_other_point_domainIDX = centroids[domainIDX[other_point]];
					//double w = utils.getDistance(data[other_point], centroid_other_point, distanceFunction)/
					//		(utils.getDistance(data[other_point], centroid_other_point_domainIDX, distanceFunction) +  Double.MIN_VALUE);
					double w = 0;
					if (cluster_index_other_point == domainIDX[other_point]) w = 1;
					w_final = Math.exp((w_common + w)/2 - 1);
					if (w_final > 1) {
						if (debug) System.out.println("The current weight is higher than 1: " + w_final + "\nSetting it to 1");
						w_final = 1;
					}
				}				
				cannot_link_part += w_final*(phi_d - utils.getDistance(data[point_index], data[other_point], distanceFunction));
				if (debug) log.info("Cannot-link: Objective function has been modified - points "
						+ point_index + " and " + other_point + " in cluster " + cluster_index + ". Current value of: " + cannot_link_part
						+ ", with w: " + w_final);
			}
		}
		
		// return the sum of all components
		if (debug) {
			//if (kmeans_part >0) System.out.println("k-means part of the distance is " + kmeans_part);
			if (must_link_part >0) System.out.println("must-link part of the distance is " + must_link_part);
			if (cannot_link_part >0) System.out.println("cannot-link part of the distance is " + cannot_link_part);
		}
		return kmeans_part + must_link_part + cannot_link_part;
	}

}
