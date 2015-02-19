package ids.framework;

import ids.clustering.model.Clusters;
import ids.clustering.model.View;
import ids.clustering.utils.ClusterUtils;
import ids.utils.CommonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class MultiviewKmeans implements Serializable {

	// input parameters
	private int maximum_number_iterations = 30;
	private int minimum_number_iterations = 5;
	private int iteration_window = 3;
	
	// output
	private boolean verbose = false;
	private int[] finalIDX = null;
	
	// Utilities
	private CommonUtils utils;
	private ClusterUtils clusterUtils;
		
	// statistics
	public List<Double> view1_objF = new ArrayList<Double>();
	public List<Double> view2_objF = new ArrayList<Double>();
	
	// views data
	private View view1;
	private View view2;
	private int n = 0;
	
	// number of clusters
	int k = 2;
	
	public MultiviewKmeans(View view1, View view2, int k) {
		if (view1.data.length != view2.data.length) System.out.println("Different number of points in views");
		
		// parameters
		this.view1 = view1;
		this.view2 = view2;
		this.k = k;
		this.n = view1.data.length;
		
		// initialization
		utils = new CommonUtils(false);
		clusterUtils = new ClusterUtils(false);
	}	
	public MultiviewKmeans(View view1, View view2, int k, boolean verbose) {
		if (view1.data.length != view2.data.length) System.out.println("Different number of points in views");
		
		// parameters
		this.view1 = view1;
		this.view2 = view2;
		this.k = k;
		this.n = view1.data.length;
		this.verbose = verbose;
		
		// initialization
		utils = new CommonUtils(this.verbose);
		clusterUtils = new ClusterUtils(this.verbose);
	}
	
	public int[] Cluster() {
		// randomly initialize centroids for second view
		double[][] centroids1 = null;
		double[][] centroids2 = clusterUtils.generateRandomClusterCentroids(view2.data, k);
		
		// do first E-step
		int[] idx1 = null;
		int[] idx2 = clusterUtils.getClusterMemberships(view2.data, centroids2, view2.distance);
		
		// value of the objective functions
		double obj_view1 = 0;
		double obj_view2 = 0;
		double view1_min = Integer.MAX_VALUE;
		double view2_min = Integer.MAX_VALUE;
		
		// run
		int t = 0;
		while (t < maximum_number_iterations) {
			t++;
		
			// VIEW 1
			// M-step:
			Clusters cl1  = clusterUtils.getClusterCentoids(view1.data, idx2, k, view1.distance);
			centroids1 = cl1.centroids;
			
			// E-Step
			idx1 = clusterUtils.getClusterMemberships(view1.data, centroids1, view1.distance);
			
			// VIEW 2
			// M-step:
			Clusters cl2 = clusterUtils.getClusterCentoids(view2.data, idx1, k, view2.distance);
			centroids2 = cl2.centroids;
			
			// E-step:
			idx2 = clusterUtils.getClusterMemberships(view2.data, centroids2, view2.distance);
			
			// re-compute the objective function for each view
			obj_view1 = clusterUtils.getKMeansObjectiveFunction(view1.data, centroids1, idx1, view1.distance);
			obj_view2 = clusterUtils.getKMeansObjectiveFunction(view2.data, centroids2, idx2, view2.distance);
			System.out.printf("Iteration %d:\tView1: %5.4f\tView2: %5.4f\n", t, obj_view1, obj_view2);
			
			// save the values
			view1_objF.add(obj_view1);
			view2_objF.add(obj_view2);
			
			// exit condition
			if (t > minimum_number_iterations) {
				// for view1
				double[] view1_temp = new double[iteration_window];
				double[] view2_temp = new double[iteration_window];
				int counter = -1;
				for (int i = t; i > (t - iteration_window); i--) {
					counter++;
					view1_temp[counter] = view1_objF.get(i-1);
					view2_temp[counter] = view2_objF.get(i-1);
				}
				
				// find minimum
				double temp1 = utils.getMin(view1_temp);
				double temp2 = utils.getMin(view2_temp);
				
				if (temp1 < view1_min) {
					view1_min = temp1;
				} else {
					break;
				}
				if (temp2 < view2_min) {
					view2_min = temp2;
				} else {
					break;
				}
				
			}
			
		}
		
		// output
		System.out.println("Multiview Spherical K-means: Done on iteration: " + t);
		System.out.printf("Value of Obj. Function Domain 1: %5.4f (min: %5.4f)\n", obj_view1, view1_min);
		System.out.printf("Value of Obj. Function Domain 1: %5.4f (min: %5.4f)\n", obj_view2, view2_min);
				
		// find consensus membership
		int[] consensus = new int[n];
		for (int i = 0; i < n; i++) {
			consensus[i] = Integer.MIN_VALUE;
			if (idx1[i] == idx2[i]) consensus[i] = idx1[i];
		}
		
		// find consensus centroids
		Clusters cl1 = clusterUtils.getClusterCentoids(view1.data, consensus, k, view1.distance);
		Clusters cl2 = clusterUtils.getClusterCentoids(view2.data, consensus, k, view2.distance);
		double[][] cons_centroids1 = cl1.centroids;
		double[][] cons_centroids2 = cl2.centroids;
		
		// find consensus IDX
		int[] common_idx = new int[n];
		
		// find maximum distance in data set
		if (view1.pd_max == -1.0) {
			view1.pd_max = utils.getMaxDistance(view1.data, view1.distance);
			if (verbose) System.out.printf("The maximum distance in View 1 is %5.4f\n", view1.pd_max);
		}
		if (view2.pd_max == -1.0) {
			view2.pd_max = utils.getMaxDistance(view2.data, view2.distance);
			if (verbose) System.out.printf("The maximum distance in View 2 is %5.4f\n", view2.pd_max);
		}
		
		// header
		if (verbose) {
			System.out.printf("Point\t");
			for (int j = 0; j < k; j++) System.out.printf("%d\t", j);
			System.out.printf("Final Index\n");
		}
		
		// run
		for (int i = 0; i < n; i++) {
			if (verbose) System.out.printf("%d\t", i);
			double f_min = Double.MAX_VALUE;
			int index_min = 0;
			for (int j = 0; j < k; j++) {
				double current_f = utils.getDistance(view1.data[i], cons_centroids1[j], view1.distance)/view1.pd_max + 
						utils.getDistance(view2.data[i], cons_centroids2[j], view2.distance)/view1.pd_max;
				if (current_f < f_min) {
					f_min = current_f;
					index_min = j;
				}
				if (verbose) System.out.printf("%5.4f\t", current_f);
			}
			common_idx[i] = index_min;
			if (verbose) System.out.printf("%d\n", common_idx[i]);
		}
		
		this.finalIDX = common_idx;
		return common_idx;
	}
	
	// return centrois for view 1
	public double[][] getCentroidsView1() {
		if (finalIDX == null) this.Cluster();
		Clusters cl = clusterUtils.getClusterCentoids(view1.data, finalIDX, k, view1.distance);
		return cl.centroids;
	}
	// return centrois for view 2
	public double[][] getCentroidsView2() {
		if (finalIDX == null) this.Cluster();
		Clusters cl = clusterUtils.getClusterCentoids(view2.data, finalIDX, k, view2.distance);
		return cl.centroids;
	}
	
	// Objective functions
	public double[] getObjFunctionView1() {
		int r = view1_objF.size();
		double[] res = new double[r];
		for (int i = 0; i < r; i++) res[i] = view1_objF.get(i);
		return res;
	}
	
	public double[] getObjFunctionView2() {
		int r = view2_objF.size();
		double[] res = new double[r];
		for (int i = 0; i < r; i++) res[i] = view2_objF.get(i);
		return res;
	}
}
