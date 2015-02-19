package ids.framework;

import ids.clustering.algorithm.HMRFKmeansParams;
import ids.clustering.algorithm.HMRFKmeansU;
import ids.clustering.model.Domain;
import ids.clustering.model.ObjectiveFunctionType;
//import ids.clustering.utils.ClusterUtils;
//import ids.utils.CommonUtils;
import ids.utils.ConstraintsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstraintBasedU {

	// input parameters
	private int maximum_number_iterations = 30;
	private int minimum_number_iterations = 5;
	private boolean useTC = true;
	private boolean keepCentroids = true;
	
	// output
	private boolean verbose = false;
	private boolean debug = false;
			
	private ConstraintsUtils constraintUtils;
	//private CommonUtils utils;
	//private ClusterUtils clusterUtils;
	private ObjectiveFunctionType obj_type;
	
	// statistics of the objective functions
	public List<Double> d1_objF;
	public List<Double> d2_objF;
	public List<Double> d1_objF_term1;
	public List<Double> d2_objF_term1;
	
	// domains
	private Domain d1;
	private Domain d2;
	public Domain getDomain1() { return d1; }
	public Domain getDomain2() { return d2; }
	
	public ConstraintBasedU(Domain d1, Domain d2, ExperimentParams par, ObjectiveFunctionType obj_type) {
		
		// parameters
		this.d1 = d1;
		this.d2 = d2;
		this.maximum_number_iterations = par.maximum_number_iterations;
		this.minimum_number_iterations = par.minimum_number_iterations;
		this.useTC = par.useTC;
		this.keepCentroids = par.keepCentroids;
		
		// output
		this.verbose = par.verbose;
		this.debug = par.debug;
		
		// initialize constraint utilities
		this.constraintUtils = new ConstraintsUtils(false);
		//this.utils = new CommonUtils(false);
		//this.clusterUtils = new ClusterUtils(false);
		this.obj_type = obj_type;
		
		// statistics to plot
		d1_objF = new ArrayList<Double>();
		d2_objF = new ArrayList<Double>();
		d1_objF_term1 = new ArrayList<Double>();
		d2_objF_term1 = new ArrayList<Double>();
	}
	
	public void Start() {
		// exit
		boolean d1_exit = false;
		boolean d2_exit = false;
		
		// initialization
		double[][] constraints = null;
		
		HMRFKmeansParams par1 = new HMRFKmeansParams();
		par1.useTC = useTC;
		par1.verbose = verbose;
		par1.constraints = constraints; // there are null here
		par1.max_number_of_iterations = d1.number_of_iterations;
		par1.distanceFunction = d1.distance;
		par1.centeroids = d1.centroids; // since we start from T1
		par1.obj_type = obj_type;
		
		HMRFKmeansParams par2 = new HMRFKmeansParams();
		par2.useTC = useTC;
		par2.verbose = verbose;
		par2.constraints = constraints; // there are null here
		par2.max_number_of_iterations = d2.number_of_iterations;
		par2.distanceFunction = d2.distance;
		par2.obj_type = obj_type;
		
		// run
		int t = 0;
		while (t < maximum_number_iterations) {
			t++;
			
			// domain 1
			if (debug) System.out.println("Domain 1");
			par1.constraints = constraints;
			HMRFKmeansU d1_kmeans = new HMRFKmeansU(d1.data, d1.k, par1);
			d1_kmeans.cluster();
			d1.old_idx = d1.idx;
			d1.idx = d1_kmeans.getIDX();
			d1.centroids = d1_kmeans.getCentroids();
			d1.objF = d1_kmeans.getObjF();
			d1_objF.add(new Double(d1.objF));
			d1_objF_term1.add(new Double(d1_kmeans.getObjF_term1()));
			
			// exchange info between domain 1 and 2
			par1.centeroids = null;
			if (keepCentroids) par1.centeroids = d1_kmeans.getCentroids(); // we do not need them any more
			// get constraints from domain 1 to domain 2
			constraints = constraintUtils.getConstraintList(d1);
			
			// domain 2
			if (debug) System.out.println("Domain 2");
			par2.constraints = constraints;
			HMRFKmeansU d2_kmeans = new HMRFKmeansU(d2.data, d2.k, par2);
			d2_kmeans.cluster();
			d2.old_idx = d2.idx;
			d2.idx = d2_kmeans.getIDX();
			d2.centroids = d2_kmeans.getCentroids();
			d2.objF = d2_kmeans.getObjF();
			d2_objF.add(new Double(d2.objF));
			d2_objF_term1.add(new Double(d2_kmeans.getObjF_term1()));
			
			// exchange info between domain 2 and 1
			par2.centeroids = null;
			if (keepCentroids) par2.centeroids = d2_kmeans.getCentroids();
			// get constraints from domain 2 to domain 1 (for next the iteration)
			constraints = constraintUtils.getConstraintList(d2);
			
			// exit condition
			if (t > minimum_number_iterations) {
				d1_exit = false;
				d2_exit = false;
				if (Arrays.equals(d1.idx, d1.old_idx)) d1_exit = true;
				if (Arrays.equals(d2.idx, d2.old_idx)) d2_exit = true;
				if (d1_exit&d2_exit) {
					if (debug) System.out.println("Constrainted Based: No change on iteration: " + t);
					break;
				}
			}
		}
		
		// output
		if (verbose) {
			System.out.println("Universal Constrainted Based: Done on iteration: " + t);
			System.out.println("Value of Obj. Function Domain 1: " + getDomain1().objF);
			System.out.println("Value of Obj. Function Domain 2: " + getDomain2().objF);
		}
	}
	
	public double[] getD1_objF() {
		double[] res = new double[d1_objF.size()];
		for (int i = 0; i < d1_objF.size(); i++) {
			res[i] = d1_objF.get(i);
		}
		return res;
	}
	
	public double[] getD2_objF() {
		double[] res = new double[d2_objF.size()];
		for (int i = 0; i < d2_objF.size(); i++) {
			res[i] = d2_objF.get(i);
		}
		return res;
	}
	
	public double[] getD1_objF_term1() {
		double[] res = new double[d1_objF_term1.size()];
		for (int i = 0; i < d1_objF_term1.size(); i++) res[i] = d1_objF_term1.get(i);
		return res;
	}
	
	public double[] getD2_objF_term1() {
		double[] res = new double[d2_objF_term1.size()];
		for (int i = 0; i < d2_objF_term1.size(); i++) res[i] = d2_objF_term1.get(i);
		return res;
	}
	
}
