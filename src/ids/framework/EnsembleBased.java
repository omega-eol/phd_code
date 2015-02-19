package ids.framework;

import ids.clustering.algorithm.HMRFKmeansParams;
import ids.clustering.algorithm.HMRFKmeansU;
import ids.clustering.model.Clusters;
import ids.clustering.model.Distance;
import ids.clustering.model.Domain;
import ids.clustering.model.ObjectiveFunctionType;
import ids.clustering.utils.ClusterUtils;
import ids.utils.CommonUtils;

public class EnsembleBased {

	private Domain domain1;
	private Domain domain2;
	int numberOfInstancesDomain1 = 1;
	int numberOfInstancesDomain2 = 1;
	private int[][] groupIDX;
	private int[] finalIDX;
	public boolean debug = false;
	
	// Utilities
	CommonUtils utils = new CommonUtils(false);
	ClusterUtils clusterUtils = new ClusterUtils(false);
		
	public EnsembleBased(Domain d1, Domain d2, int run_d1, int run_d2) {
		this.domain1 = d1;
		this.domain2 = d2;
		this.numberOfInstancesDomain1 = run_d1;
		this.numberOfInstancesDomain2 = run_d2;
		
		// cluster
		groupIDX = cluster();
	}
	
	public EnsembleBased(Domain d1, int run_d1) {
		this.domain1 = d1;
		this.domain2 = null;
		this.numberOfInstancesDomain1 = run_d1;
		this.numberOfInstancesDomain2 = 0;
		
		// cluster
		groupIDX = clusterDomain1();
	}
	
	private int[][] clusterDomain1() {
		// number of data objects
		int n = domain1.data.length;
		
		// total number of algorithms that will be used in clustering
		int totalNumberOfInstances = numberOfInstancesDomain1;
		
		// final IDX holder
		int[][] algorithmIDX = new int[n][totalNumberOfInstances];
		
		// do domain 1 first
		for (int i = 0; i < numberOfInstancesDomain1; i++) {
			HMRFKmeansParams domain1_par = new HMRFKmeansParams();
			domain1_par.distanceFunction = domain1.distance;
			domain1_par.obj_type = ObjectiveFunctionType.CENTROIDS;
			domain1_par.verbose = true;
			HMRFKmeansU kmeans = new HMRFKmeansU(domain1.data, domain1.k, domain1_par);
			kmeans.cluster();
			int[] idx = kmeans.getIDX();
			for (int j = 0; j < n; j++) algorithmIDX[j][i] = idx[j];
		}
		
		return algorithmIDX;
	}
	
	/**
	 * Cluster domain 1 and domain 2
	 * @return
	 */
	private int[][] cluster() {
		if (domain1.data.length != domain2.data.length) {
			System.out.println("Data in domains has been the same length");
			return null;
		}
		int n = domain1.data.length;
		
		// total number of algorithms that will be used in clustering
		int totalNumberOfInstances = numberOfInstancesDomain1 + numberOfInstancesDomain2;
		
		// final IDX holder
		int[][] algorithmIDX = new int[n][totalNumberOfInstances];
		int instancesCounter = -1;
		
		// do domain 1 first
		for (int i = 0; i < numberOfInstancesDomain1; i++) {
			instancesCounter++;
			HMRFKmeansParams domain1_par = new HMRFKmeansParams();
			domain1_par.distanceFunction = domain1.distance;
			domain1_par.obj_type = ObjectiveFunctionType.CENTROIDS;
			domain1_par.verbose = true;
			HMRFKmeansU kmeans = new HMRFKmeansU(domain1.data, domain1.k, domain1_par);
			kmeans.cluster();
			int[] idx = kmeans.getIDX();
			for (int j = 0; j < n; j++) algorithmIDX[j][instancesCounter] = idx[j];
		}
		
		// same thing for domain 2
		for (int i = 0; i < numberOfInstancesDomain2; i++) {
			instancesCounter++;
			HMRFKmeansParams domain2_par = new HMRFKmeansParams();
			domain2_par.distanceFunction = domain2.distance;
			domain2_par.obj_type = ObjectiveFunctionType.CENTROIDS;
			domain2_par.verbose = true;
			HMRFKmeansU kmeans = new HMRFKmeansU(domain2.data, domain2.k, domain2_par);
			kmeans.cluster();
			int[] idx = kmeans.getIDX();
			for (int j = 0; j < n; j++) algorithmIDX[j][instancesCounter] = idx[j];
		}

		return algorithmIDX;
	}
	
	/**
	 * This function use voting method as a consensus function
	 * @return final cluster membership
	 */
	public int[] Do_Voting() {
		if (groupIDX == null) cluster();		
		finalIDX = null;
		
		int n = groupIDX.length;
		int totalNumberOfInstances = groupIDX[0].length;
		int[] res = new int[n];
		
		// solve clustering correspondence
		if (debug) System.out.print("Solving cluster correspondence problem.."); 
		int[][] temp = new int[n][totalNumberOfInstances];
		int[] firstColumn = utils.getColumn(groupIDX, 0);
		utils.fillColumn(temp, firstColumn, 0);
		for (int index = 1; index < totalNumberOfInstances; index++) {
			int[] currentColumn = utils.getColumn(groupIDX, index);
			int[] q = clusterUtils.findClusterCorrespondence(firstColumn, currentColumn);
			if (q != null) {
				utils.fillColumn(temp, q, index);
			} else {
				utils.fillColumn(temp, currentColumn, index);
			}
			
		}
		if (debug) System.out.println("done.");
		
		// find mode
		for (int i = 0; i < n; i++) res[i] = utils.getMode(temp[i]);
		
		// output
		if (debug) {
			int v_tt = 20;
			if (v_tt > n) v_tt = n;
			
			for (int i = 0; i < v_tt; i++) {
				System.out.printf("%d.\t", i);
				for (int j = 0; j < totalNumberOfInstances; j++) {
					System.out.printf("%d\t", temp[i][j]);
				}
				System.out.printf("%d\n", res[i]);
			}
		}
		
		finalIDX = res;
		return finalIDX;
	}
	
	/**
	 * Use clustering method (kmodes) as a consensus function
	 * @param final_k
	 * @return final cluster membership
	 */
	public int[] Do_Clustering(int final_k) {
		if (groupIDX == null) cluster();		
		finalIDX = null;
		
		int n = groupIDX.length;
		int[] res = new int[n];
		
		HMRFKmeansParams par = new HMRFKmeansParams(Distance.MATCH, ObjectiveFunctionType.CENTROIDS);
		par.verbose = debug;
		HMRFKmeansU kmeans = new HMRFKmeansU(groupIDX, final_k, par);
		kmeans.cluster();
		res = kmeans.getIDX();
				
		if (debug) {
			int v_tt = 20;
			if (v_tt > n) v_tt = n;
			System.out.println("Centroids:");
			utils.printMatrix(kmeans.getCentroids());
			
			System.out.println("Membership matrix");
			for (int i = 0; i < v_tt; i++) {
				System.out.printf("%d.\t", i);
				for (int j = 0; j < groupIDX[0].length; j++) {
					System.out.printf("%d\t", groupIDX[i][j]);
				}
				System.out.printf("%d\n", res[i]);
			}
		}
		
		finalIDX = res;
		return finalIDX;
	}
	
	/**
	 * Returns domain`s centroids
	 * @return
	 */
	public double[][] getCentroidD1() { return getDomainCentroids(domain1); }	
	public double[][] getCentroidD2() { return getDomainCentroids(domain2); };
	
	private double[][] getDomainCentroids(Domain d) {
		if (finalIDX != null) {
			Clusters cl =  clusterUtils.getClusterCentoids(d.data, finalIDX, d.k, d.distance);
			return cl.centroids;
		}		
		System.out.println("PLese cluster first");
		return null;
	}
	
	public void clear() {
		this.groupIDX = null;
		this.finalIDX = null;
	}
	
	public void printMembershipMatrix() {
		if ((groupIDX != null)&&(finalIDX != null)) {
			int n = domain1.data.length;
			int m = groupIDX[0].length;
			
			// print header
			System.out.printf("#\t");
			for (int j = 0; j < m; j++) System.out.printf("%d\t", j);
			System.out.printf("Final IDX\n");
			
			// print data
			for (int i = 0; i < n; i++) {
				System.out.printf("%d.\t", i);
				for (int j = 0; j < m; j++) {
					System.out.printf("%d\t", groupIDX[i][j]);
				}
				System.out.printf("%d\n", finalIDX[i]);
			}
		}
	}
	
	
}
