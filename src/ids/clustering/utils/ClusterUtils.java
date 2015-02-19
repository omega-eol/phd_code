package ids.clustering.utils;

import ids.clustering.model.Clusters;
import ids.clustering.model.Distance;
import ids.utils.CommonUtils;
import ids.utils.HungarianAlgorithm;
import ids.utils.SearchResult;
import ids.utils.UniqueResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class ClusterUtils implements Serializable {

	private boolean verbose;
	private Logger log;
	private CommonUtils utils;
	
	public ClusterUtils(boolean verbose) {
		this.verbose = verbose;
		if (verbose) log = Logger.getLogger(getClass().getName());
		utils = new CommonUtils(verbose);
	}
	
	/**
	 * Finds value of the objective function
	 * @param data - input data matrix
	 * @param centroids - data centroids
	 * @param idx - cluster memberships
	 * @param distance - type of distance measure
	 */
	public double getKMeansObjectiveFunction(double[][] data, double[][] centroids, int[] idx, Distance distance) {
		// error check
		if (data == null) return -1.0;
		int n = data.length;
		if (n == 0) return 0;
		
		if (idx.length != n) {
			System.out.println("The length of the membership vector is different that number of points in the data set.");
			return -1.0;
		}
		
		// find objective function
		double res = 0;
		for (int i = 0; i < n; i++) res = res + utils.getDistance(data[i], centroids[idx[i]], distance);
		return res;
	}
	
	/**
	 * Randomly generates cluster centroids
	 * @param data - input data
	 * @param k - number of centroids to generate
	 * @return
	 */
	public double[][] generateRandomClusterCentroids(double[][] data, int k) {
		if (data == null) return null;
		int n = data.length;
		if (n == 0) return null;
		int m = data[0].length;
		
		// get number of points
		double[][] centroids = new double[k][m];
		int[] points = utils.getRandomPermutation(n, k);
		for (int i = 0; i<k; i++) {
			centroids[i] = data[points[i]];
		}
		
		return centroids;
	}
	
	/**
	 * Returns cluster centroid`s indices
	 * @param data - input data
	 * @param centroids - double[][] centroids
	 * @param distance - Distance measure
	 * @return indices
	 */
	public int[] getClusterCentroidsIndices(double[][] data, double[][] centroids, Distance distance) {
		int k = centroids.length;
		int[] indices = new int[k];
		for (int i = 0; i < k; i++) {
			double[] pd = utils.getDistance(data, centroids[i], distance);
			SearchResult<Double> sr = utils.getMinValue(pd);
			indices[i] = sr.getIndex();
			
			if (verbose) {
				System.out.printf("%d centroid:\n", i);
				Arrays.toString(centroids[i]);
				System.out.printf("Close point %d\n", indices[i]);
				Arrays.toString(data[indices[i]]);
			}
		}
		return indices;
	}
	
	public double[][] getClusterCentroidsByIndices(double[][] data, int[] centroidsIndices) {
		return utils.getRows(data, centroidsIndices);
	}
	
	public Clusters getClusterCentoids(double[][] data, int[] idx, int k, Distance distance) {
		int n = data.length;
		if (n == 0) return null;
		int dim = data[0].length;
		return getClusterCentoids(data, n, dim, idx, k, distance);
	}
	public Clusters getClusterCentoids(double[][] data, int n, int dim, int[] idx, int k, Distance distance) {
		double[][] centroids = new double[k][dim];
		int[] clusterSizes = new int[k];
		boolean generateNewClusters = false;
		
		// for each cluster
		for (int i = 0; i < k; i++) {
			// get the data, which belong to cluster i
			Set<Integer> cp = utils.getIndicesByValue(idx, i);
			int number_points_i = cp.size();
			
			// save trigger
			if (number_points_i == 0) { // sometimes this happens specially with k-modes algorithm
				generateNewClusters = true;
				break; // exit from the for loop
			}
			
			// save cluster size
			clusterSizes[i] = number_points_i;
			double[][] data_i = new double[number_points_i][dim];
			int index = 0;
			for (Integer j : cp) {
				data_i[index] = data[j];
				index++;
			}
			
			// find centroids
			if (distance==Distance.SQEUCLIDEAN) {
				centroids[i] = utils.findMeanVector(data_i); 
			} else if (distance==Distance.EUCLIDEAN) {
				centroids[i] = utils.findMeanVector(data_i);
			} else if (distance==Distance.COSINE) {
				// same as before but we need to normalize the centroids
				double[] temp = utils.findMeanVector(data_i);
				centroids[i] = utils.findNorm(temp, dim);
			} else if (distance == Distance.MATCH) {
				// find the mode of the data_i
				for (int j = 0; j < dim; j++) { // for every feature
					double[] col_vector = new double[number_points_i];
					
					if (number_points_i==0) System.out.println("0 - Points");
					
					for (int p = 0; p < number_points_i; p++) col_vector[p] = data_i[p][j];
					// find all unique value and their frequencies
					UniqueResult<Double> ur = utils.findUnique(col_vector);
					SearchResult<Integer> sr = utils.getMaxValue(ur.frequency);
					centroids[i][j] = ur.domain[sr.getIndex()];
				}
			} else {
				System.out.println("Unknow distance");
				if (verbose) log.severe("Unknown distance");
			}
		}		
		
		// safety trigger
		if (generateNewClusters) {
			// randomly generate k centroids and find its cluster size
			int[] indices = utils.getRandomPermutation(data.length, k);
			// get centroids
			for (int i = 0; i < k; i++) {
				centroids[i] = data[indices[i]];
			}
			// get membership
			int[] n_idx = getClusterMemberships(data, centroids, distance);
			for (int i = 0; i < k; i++) {
				Set<Integer> cp = utils.getIndicesByValue(n_idx, i);
				clusterSizes[i] = cp.size();
			}
		}
		
		return new Clusters(centroids, clusterSizes);
	}
	
	// return the data part that belongs to the current cluster
	public SearchResult<Double> getClusterData(double[][] data, int[] idx, int i) {
		int dim = data[0].length;		
		// get the data, which belong to cluster i
		Set<Integer> cp = utils.getIndicesByValue(idx, i);
		int number_points_i = cp.size();
		List<Integer> points_i = null;
		double[][] data_i = null;
		// if there are points
		if (number_points_i > 0) {
			points_i = new ArrayList<Integer>(cp);
			data_i = new double[number_points_i][dim];
			int index = 0;
			for (Integer j : cp) {
				data_i[index] = data[j];
				index++;
			}
		}
		return new SearchResult<Double>(data_i, points_i);
	}
	
	// cluster membership
	public int[] getClusterMemberships(double[][] data, double[][] centroids, Distance distance) {
		int n = data.length;
		//int dim = data[0].length;
		int k = centroids.length;
		int[] idx = new int[n];
		double objF = 0;
		
		for (int i = 0; i<n; i++) {
			double f_min = Double.MAX_VALUE;
			int j_min = 0;
			
			// assign points to clusters
			for (int j=0; j<k; j++) {
				double f = utils.getDistance(data[i], centroids[j], distance);
				if (f<f_min) {
					f_min = f;
					j_min = j;
				}
			}
			idx[i] = j_min;
			objF += f_min;
		}
		
		if (verbose) log.info("The objective function is " + objF);
		return idx;
	}
	
	/**
	 * Returns Jaccard Coefficient between membership vector idx and class distribution class_idx.
	 * Note: Function assumes that cluster and class indices start from index 0
	 * @param idx - cluster membership vector
	 * @param class_idx - class distribution
	 * @param numClusters - number of clusters
	 * @param numClasses - number of classes
	 * @return matrix of Jaccard Coefficients
	 */
	public double[][] findJaccardIndex(int[] idx, int[] class_idx, int numClusters, int numClasses) {
		double[][] res = new double[numClusters][numClasses];
		for (int i = 0; i < numClusters; i++) {
			boolean[] a = utils.getIndexByValue(idx, i);
			for (int j = 0; j < numClasses; j++) {
				boolean[] b = utils.getIndexByValue(class_idx, j);
				boolean[] ab = utils.findAandB(a, b);
				int sum_ab = utils.findSum(ab);
				double div = (utils.findSum(a) + utils.findSum(b) - sum_ab);
				if (div != 0) {
					res[i][j] = sum_ab/div;
				} else {
					System.out.println("ClusterUtils: findJaccardIndex: Divizion by Zero!");
					res[i][j] = Double.MIN_VALUE;
				}
			}
		}
		return res;
	}
	
	/**
	 * Returns Jaccard Coefficient between membership vector idx and class distribution class_idx.
	 * Function automatically find number of clusters and classes
	 * @param idx - cluster membership vector
	 * @param class_idx - class distribution
	 * @param numClusters - number of clusters
	 * @param numClasses - number of classes
	 * @return matrix of Jaccard Coefficients
	 */
	public double[][] findJaccardIndex(int[] idx, int[] class_idx) {
		// find domain for membership vector
		UniqueResult<Integer> idxUR = utils.findUnique(idx);
		int numClusters = idxUR.domain.length;
		
		// find domain for class distribution
		UniqueResult<Integer> class_idxUR = utils.findUnique(class_idx);
		int numClasses = class_idxUR.domain.length;
		
		double[][] res = new double[numClusters][numClasses];
		int row = -1;
		int col = -1;
		
		// run
		for (Integer i : idxUR.domain) {
			col = -1;
			row++;
			boolean[] a = utils.getIndexByValue(idx, i);
			for (Integer j : class_idxUR.domain) {
				col++;
				boolean[] b = utils.getIndexByValue(class_idx, j);
				boolean[] ab = utils.findAandB(a, b);
				int sum_ab = utils.findSum(ab);
				double div = (utils.findSum(a) + utils.findSum(b) - sum_ab);
				if (div != 0) {
					res[row][col] = sum_ab/div;
				} else {
					System.out.println("ClusterUtils: findJaccardIndex: Divizion by Zero!");
					res[row][col] = Double.POSITIVE_INFINITY;
				}
			}
		}
		
		// output
		if (verbose) {
			System.out.println("Jaccard coefficient:");
			// print header
			System.out.printf("\t");
			for (int j = 0; j < numClasses; j++) System.out.printf("%d\t", class_idxUR.domain[j]);
			System.out.printf("\n");
			// print Jaccard matrix
			for (int i = 0; i < numClusters; i++) {
				System.out.printf("%d.\t", idxUR.domain[i]);
				for (int j = 0; j < numClasses; j++) {
					System.out.printf("%5.4f\t", res[i][j]);
				}
				System.out.printf("\n");
			}
		}		
		
		return res;
	}
	
	/**
	 * Finds cluster correspondence between cluster membership idx1 and cluster membership idx2 and renames cluster labels in
	 * idx2 to the same as in idx1
	 * @param idx1
	 * @param idx2
	 * @return renamed idx2 (with the same cluster indices as in idx1)
	 */
	public int[] findClusterCorrespondence(int[] idx1, int[] idx2) {
		int n = idx1.length;
		if (n != idx2.length) {
			System.out.println("Cluster Utils: findClusterCorrespondence: idx1 and idx2 has to have same number of elements");
			return null;
		}
		
		// find unique values in idx1 and idx2
		UniqueResult<Integer> ur_idx1 = utils.findUnique(idx1);
		UniqueResult<Integer> ur_idx2 = utils.findUnique(idx2);
		if (ur_idx1.domain.length != ur_idx2.domain.length) {
			System.out.println("Cluster Utils: findClusterCorrespondence: idx1 and idx2 has different number of clusters");
			return null;
		}
		
		// find Jaccard coefficient
		double[][] J = findJaccardIndex(idx1, idx2);
		
		// use Hungarian method - use any kind of Hungarian algorithm
		HungarianAlgorithm h = new HungarianAlgorithm();
		@SuppressWarnings("static-access")
		int[][] match = h.hgAlgorithm(J, "max");
		
		// print match matrix
		if (verbose) {
			System.out.println("Hunguarian Cost Matrix");
			utils.printMatrix(match);
		}
		
		// relabel the idx2
		if (verbose) System.out.println("Class correspondents matrix:");
		int[] target = idx1.clone();
		for (int i = 0; i < match.length; i++) {
			int idx1_index = ur_idx1.domain[match[i][0]];
			int idx2_index = ur_idx2.domain[match[i][1]];
			if (verbose) System.out.printf("%d\t%d", idx1_index, idx2_index);			
			if (idx1_index != idx2_index) {
				if (verbose) System.out.printf("\t(will replace all %d in idx2 by %d)", idx2_index, idx1_index);
				boolean[] index = utils.getIndexByValue(idx2, idx2_index);
				for (int j = 0; j < n; j++) {
					if (index[j]) target[j] = idx1_index;
				}
			}
			if (verbose) System.out.printf("\n");
		}
		
		if (verbose) {
			int v_tt = 15;
			if (target.length < v_tt) v_tt = target.length;
			System.out.printf("Printing first %d elements of input and output arrays\n", v_tt);
			System.out.printf("IDX1\tIDX2\tFINAL IDX\n");
			for (int i = 0; i < v_tt; i++) System.out.printf("%d\t%d\t%d\n", idx1[i], idx2[i], target[i]);
		}
		
		return target;
	}

	// converters
	/**
	 * Converts data matrix nxdim to nx1 membership vector
	 * @return int[] idx - membership vector
	 */
	public int[] convertDataMatrixToIDX(double[][] data) {
		int n = data.length;
		if (n==0) return null;
		
		// convert from double[] to int[]
		int[] m = new int[n];
		for (int i = 0; i < n; i++) m[i] = (int)data[i][0];
		
		// make sure the cluster index starts from 0
		UniqueResult<Integer> ur = utils.findUnique(m);
		Integer[] domain = ur.domain;
		int[] idx = new int[n];
		for (int i = 0; i < domain.length; i++) {
			boolean[] index = utils.getIndexByValue(m, domain[i]);
			for (int j = 0; j < n; j++) {
				if (index[j]) idx[j] = i;
			}
		}
		return idx;
	}
}
