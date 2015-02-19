package ids.utils;

import ids.clustering.model.ConstraintType;
import ids.clustering.model.Domain;
import ids.clustering.model.Pair;
import ids.clustering.utils.ClusterUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

public class ConstraintsUtils {
	
	private boolean verbose = false;
	private Logger log; 
	private ClusterUtils clusterUtils;
	private CommonUtils commonUtils;
	
	// Transitive Closure
	int tc_n = 0;
	int[] tc_done;
	double[][] tc_A;
	int[] tc_c; // neighborhood
	// Sparse Transitive Closure
	DoubleMatrix2D tc_sA;
	
	
	public ConstraintsUtils(boolean verbose) {
		this.verbose = verbose;
		if (verbose) log = Logger.getLogger(getClass().getName());
		clusterUtils = new ClusterUtils(verbose);
		commonUtils = new CommonUtils(verbose);
	}
	
	// generate constraints from cluster points
	public double[][] getConstraintList(Domain domain) {
		if (domain.number_constraints == 0) return null;		
		//int dim = domain.data[0].length;
		
		// temp
		List<PointClusterPair> points_lits = new ArrayList<PointClusterPair>();		
		
		// get the points of interest first
		for (int i = 0; i < domain.k; i++) {
			// get cluster data
			SearchResult<Double> sr = clusterUtils.getClusterData(domain.data, domain.idx, i);
			List<Integer> points = sr.getIndices();
			
			if (points != null) {
				// get distance between each points in the cluster to it centroid
				double[] pd = commonUtils.getDistance(sr.getData(), domain.centroids[i], domain.distance);
				
				// sort resulted index according to the distance
				ArrayIndexComparator comp = new ArrayIndexComparator(pd);
				Integer[] index = comp.createIndex();
				Arrays.sort(index, comp);
							
				// map to the global indexing
				int[] global_index = commonUtils.getElementsByIndeces(points, index);
				
				if (verbose) {
					System.out.println("Cluster: " + i);
					for (int j = 0; j < global_index.length; j++) {
						double real_pd = commonUtils.getDistance(domain.data[global_index[j]], domain.centroids[i], domain.distance);
						System.out.println(index[j].toString() + ": distance: " + pd[index[j]] + ": global index: " + global_index[j] + ": real distance: " + real_pd);
					}
				}
				
				// save top domain.number_constraints points
				int n_points = domain.number_constraints;
				if (global_index.length < domain.number_constraints) n_points = global_index.length;
				for (int j = 0; j < n_points; j++) {
					PointClusterPair pair = new PointClusterPair(global_index[j], i);
					points_lits.add(pair);
				}
			} else {
				System.out.println("Empty cluster found.");
			} // if points != null
		} // for loop for each cluster
		
		// number of constraints
		int n = points_lits.size();
		if (verbose) System.out.println("Constraints Utils: Number of constraints: " + n);
		
		// print points_index
		/*
		System.out.println("Points indeces");
		for (int i = 0; i < n; i++) {
			PointClusterPair pair = points_lits.get(i);
			System.out.println(i + ": " + pair.point_index + ": " + pair.cluster_index);
		}
		*/
		
		// generate constraints
		int nc = n*(n-1)/2;
		double[][] res = new double[nc][3];
		int v_tt = -1;
		for (int i = 0; i < n; i++) {
			PointClusterPair pair1 = points_lits.get(i);			
			for (int j = i + 1; j < n; j++) {
				v_tt++;
				PointClusterPair pair2 = points_lits.get(j);
				res[v_tt][0] = pair1.point_index + 1; // since first object has index 1, not 0!
				res[v_tt][1] = pair2.point_index + 1;
				if (pair1.cluster_index == pair2.cluster_index) {
					// create a must-link constraint
					res[v_tt][2] = 1;
				} else {
					// create a cannot-link constraint
					res[v_tt][2] = 2;
				}
			}
		}
		
		// print points_index
		/*
		System.out.println("Points constraints");
		for (int i = 0; i < nc; i++) {
			System.out.println(i + ": " + res[i][0] + " " + res[i][1] + " " + res[i][2]);
		}
		*/
		
		return res;
	}
	private class PointClusterPair {
		public int point_index;
		public int cluster_index;
		public PointClusterPair(int point, int cluster) {
			this.point_index = point;
			this.cluster_index = cluster;
		}
	}
	
	
	// In this function we create sets of cannot-link constraint between any
	// pair of lambda neighborhoods.
	public void inferCannotLinkConstraints(DoubleMatrix2D C, int[] nLambda, int lambda) {
		CommonUtils utils = new CommonUtils(verbose);
		for (int i = 0; i < lambda; i++) { // for every neighborhood
			// first set
			Set<Integer> set_i = utils.getIndicesByValue(nLambda, i);
			for (int j = i+1; j < lambda; j++) {
				// second set
				Set<Integer> set_j = utils.getIndicesByValue(nLambda, j);
				if ((set_i.size()==1)&(set_j.size()==1)) break; // nothing to infer
				if (testForCannotLinkConstraints(set_i, set_j, C)) {
					inferCannotLinksInN(set_i, set_j, C);
				}
			}
		} 
	}
	private boolean testForCannotLinkConstraints(Set<Integer> set_i, Set<Integer> set_j, DoubleMatrix2D C) {
		for (Integer i : set_i) {
			for (Integer j : set_j) {
				if (C.getQuick(i,j)==1.0) return true;
			}
		}
		return false;
	}
	private void inferCannotLinksInN(Set<Integer> set_i, Set<Integer> set_j, DoubleMatrix2D C) {
		for (Integer i : set_i) {
			for (Integer j : set_j) {
				C.setQuick(i, j, 1.0);
				C.setQuick(j, i, 1.0);
				if (verbose) {
					log.info("Creating cannot-link constraints between points " + i + " and " + j);
				}
			}
		}
	}
	
	// dense version of the transitive closure
	public double[][] TransitiveClosure(double[][] A) {
		// initialization
		initTC(A);		
		
		// run
		int label = -1; // start from 0!
		for (int i = 0; i < tc_n; i++) {
			if (tc_done[i] == 0) {
				label++;
				ladelDFS(i, label);
			}
		}
		
		// compute the tc
		double[][] tc = new double[tc_n][tc_n];
		for (int i = 0; i<tc_n; i++) {
			for (int j = i+1; j < tc_n; j++) {
				if (tc_c[i] == tc_c[j]) {
					tc[i][j] = 1;
					tc[j][i] = 1;
				}
			}
		}
		// add diagonal
		for (int i = 0; i < tc_n; i++) tc[i][i] = 1;
		return tc;
	}
	public int[] getNeighborhood() {
		return tc_c;
	}
	private void ladelDFS(int row, int label) {
		tc_done[row] = 1;
		tc_c[row] = label;
		for (int i = 0; i < tc_n; i++) {
			if ((tc_A[row][i]==1)&(tc_done[i]==0)) ladelDFS(i, label);
		}
	}
	private void initTC(double[][] A) {
		// number of nodes
		tc_n = A.length;
		// labels
		tc_done = new int[tc_n];
		
		// output
		tc_A = A.clone();
		
		// tc_c
		tc_c = new int[tc_n];
	}
	
	
	// sparse transitive closure
	public DoubleMatrix2D TransitiveClosure(DoubleMatrix2D A) {
		// initialization
		initSTC(A);		
		
		// run
		int label = -1; // start from -1, so first neigh will start form 0!
		for (int i = 0; i < tc_n; i++) {
			if (tc_done[i] == 0) {
				label++;
				ladelSDFS(i, label);
			}
		}
		
		// compute the tc
		DoubleMatrix2D tc = new SparseDoubleMatrix2D(tc_n, tc_n);
		for (int i = 0; i<tc_n; i++) {
			for (int j = i+1; j < tc_n; j++) {
				if (tc_c[i] == tc_c[j]) {
					tc.setQuick(i, j, 1.0);
					tc.setQuick(j, i, 1.0);
				}
			}
		}
		// add diagonal
		for (int i = 0; i < tc_n; i++) tc.setQuick(i, i, 1.0);
		return tc;
	}
	private void ladelSDFS(int row, int label) {
		tc_done[row] = 1;
		tc_c[row] = label;
		for (int i = 0; i < tc_n; i++) {
			if ((tc_sA.getQuick(row, i)==1.0)&(tc_done[i]==0)) ladelSDFS(i, label);
		}
	}
	private void initSTC(DoubleMatrix2D A) {
		// number of nodes
		tc_n = A.rows();
		// labels
		tc_done = new int[tc_n];
		
		// output
		tc_sA = new SparseDoubleMatrix2D(tc_n, tc_n);
		tc_sA = A.copy();
		
		// tc_c
		tc_c = new int[tc_n];
	}
	
	public ArrayList<Pair> ParseConstraints(double[][] constraints, int n) throws Exception {
		ArrayList<Pair> res = new ArrayList<Pair>();
		for (int i=0; i<n; i++) {
			ConstraintType type;
			if (((int)constraints[i][2])==1) {
				type = ConstraintType.MUST_LINK;
				if (verbose) {
					log.info("Creating must-link constraint between object " + (int)constraints[i][0] +
						" and " + (int)constraints[i][1]);
				}					
			} else if (((int)constraints[i][2])==2) {
				type = ConstraintType.CANNOT_LINK;
				if (verbose) {
					log.info("Creating cannot-link constraint between object " + (int)constraints[i][0] +
						" and " + (int)constraints[i][1]);
				}
			} else {
				throw new IllegalArgumentException("Cannot find constraint type");
			}
			Pair pair = new Pair((int)constraints[i][0], (int)constraints[i][1], type);
			res.add(pair);
		}
		return res;
	}
	
	/**
	 * Parse constraint from double[][] matrix in format
	 * <point_index_a> <point_index_b> <constraint_type>
	 * if "constraint_type" = 1 then this is a must-link constraint
	 * if "constraint_type" = 2 then this is a cannot-link constraint
	 * @param constraints - double[][] matrix of constraints
	 * @param n - number of points in data set
	 * @param type - the target constraint type 
	 * @return constraint matrix in sparse format
	 */
	public DoubleMatrix2D ParseConstraints(double[][] constraints, int n, ConstraintType type) {
		if (constraints==null) return null;
		int n_constraints = constraints.length;
		if (n_constraints == 0) return null;
		
		if (verbose) System.out.println("Parsing constraints..");
		DoubleMatrix2D res = new SparseDoubleMatrix2D(n, n);
		
		// run
		int counter = 0;
		for (int i = 0; i < n_constraints; i++) {
			int a = (int)constraints[i][0] - 1; // -1 since data ID starts from 0 but from 1 in the constant file
			int b = (int)constraints[i][1] - 1;
			
			if ( (((int)constraints[i][2])==1)&&(type == ConstraintType.MUST_LINK) ) { // must-link constraints
				counter++;
				res.setQuick(a, b, 1.0);
				res.setQuick(b, a, 1.0);
				if (verbose) {
					System.out.println("Creating a must-link constraint between object " + a + " and " + b);
				}
			} else if ( (((int)constraints[i][2])==2)&&(type == ConstraintType.CANNOT_LINK)  ) { // cannot-link constraints
				counter++;
				res.setQuick(a, b, 1.0);
				res.setQuick(b, a, 1.0);
				if (verbose) {
					System.out.println("Creating a cannot-link constraint between object " + a + " and " + b);
				}
			} else {
				System.out.println("Cannot find constraints type or not a target constraint type");
			}			
		} // end for loop
		if (verbose) { 
			System.out.printf("Done. %d constraints has been parsed.\n", counter*2);
		}
		return res;
	}
}


