package ids.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ids.clustering.algorithm.HMRFKmeans;
import ids.clustering.algorithm.HMRFKmeansParams;
import ids.clustering.model.Domain;
import ids.utils.ConstraintsUtils;

public class ConstraintBased {

	private int maximum_number_iterations = 30;
	private int minimum_number_iterations = 5;
	
	// output
	private boolean verbose = false;
	private boolean debug = false;
	
	private boolean useTC;
	private boolean inferCannotLink;
	
	private ConstraintsUtils constraintUtils;
	
	// statistics
	public List<Double> d1_objF;
	public List<Double> d2_objF;
	
	// domains
	private Domain d1;
	private Domain d2;
	public Domain getDomain1() { return d1; }
	public Domain getDomain2() { return d2; }
	
	public ConstraintBased(Domain d1, Domain d2, ExperimentParams par) {
		this.d1 = d1;
		this.d2 = d2;
		parseParams(par);
		constraintUtils = new ConstraintsUtils(false);
		
		// statistics
		d1_objF = new ArrayList<Double>();
		d2_objF = new ArrayList<Double>();
	}
	
	public void Start() {
		// exit
		boolean d1_exit = false;
		boolean d2_exit = false;
		
		// initialization
		double[][] constraints = null;
		
		HMRFKmeansParams par1 = new HMRFKmeansParams();
		par1.useTC = useTC;
		par1.verbose = debug;
		par1.constraints = constraints;
		par1.max_number_of_iterations = d1.number_of_iterations;
		par1.distanceFunction = d1.distance;
		
		HMRFKmeansParams par2 = new HMRFKmeansParams();
		par2.useTC = useTC;
		par2.verbose = debug;
		par2.constraints = constraints;
		par2.max_number_of_iterations = d2.number_of_iterations;
		par2.distanceFunction = d2.distance;
		
		// run
		int t = 0;
		while (t < maximum_number_iterations) {
			t++;
			
			// domain 1
			if (debug) System.out.println("Domain 1");
			par1.constraints = constraints;
			HMRFKmeans d1_kmeans = new HMRFKmeans(d1.data, d1.k, par1);
			d1_kmeans.cluster();
			d1.old_idx = d1.idx;
			d1.idx = d1_kmeans.getIDX();
			d1.centroids = d1_kmeans.getCentroids();
			d1.objF = d1_kmeans.getObjF();
			d1_objF.add(new Double(d1.objF));
			
			// get constraints for domain 1
			constraints = constraintUtils.getConstraintList(d1);
			
			// domain 2
			if (debug) System.out.println("Domain 2");
			par2.constraints = constraints;
			HMRFKmeans d2_kmeans = new HMRFKmeans(d2.data, d2.k, par2);
			d2_kmeans.cluster();
			d2.old_idx = d2.idx;
			d2.idx = d2_kmeans.getIDX();
			d2.centroids = d2_kmeans.getCentroids();
			d2.objF = d2_kmeans.getObjF();
			d2_objF.add(new Double(d2.objF));
			
			// get constraints for domain 1
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
			System.out.println("Constrainted Based: Done on iteration: " + t);
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
	
	private void parseParams(ExperimentParams par) {
		maximum_number_iterations = par.maximum_number_iterations;
		minimum_number_iterations = par.minimum_number_iterations;
		verbose = par.verbose;
		debug = par.debug;
		useTC = par.useTC;
	}
	
}
