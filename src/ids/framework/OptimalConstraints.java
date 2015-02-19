package ids.framework;

import java.io.Serializable;

import ids.clustering.model.Domain;
import ids.clustering.utils.ClusterValidation;
import ids.clustering.utils.ValidationIndex;
import ids.utils.CommonUtils;

@SuppressWarnings("serial")
public class OptimalConstraints implements Serializable {
	
	private Domain d1;
	private Domain d2;
	private ExperimentParams par;
	private int number_iterations;
	private int start_n;
	private int end_n;	
	private int[] groundtruth;
	
	private boolean debug = false;
	private boolean verbose = false;
	
	private int ni;
	private double[][] d1_res;
	private double[][] d2_res;
	
	private CommonUtils utils;
	private ClusterValidation valid;
	
	// save
	private boolean saveResutls;
	private String savePath;
	
	// Constructors
	public OptimalConstraints(OptimalConstraintsPar inputPar) {
		// domains
		this.d1 = inputPar.domain1;
		this.d2 = inputPar.domain2;
		this.groundtruth = inputPar.groundtruth;
		
		// saving
		this.savePath = inputPar.savePath;
		this.saveResutls = inputPar.saveResults;
		
		// output
		this.verbose = inputPar.verbose;
		this.debug = inputPar.debug;
		
		// constraint based
		this.par = new ExperimentParams();
		this.par.maximum_number_iterations = inputPar.maximum_number_iterations;
		this.par.minimum_number_iterations = inputPar.minimum_number_iterations;
		this.par.debug = this.debug;
		this.par.verbose = this.verbose;
		
		// optimal
		this.number_iterations = inputPar.number_iterations;
		this.start_n = inputPar.start_n;
		this.end_n = inputPar.end_n;
		
		init();
	}
	public OptimalConstraints(Domain domain1, Domain domain2, int[] gt, String s_path) {
		this.d1 = domain1;
		this.d2 = domain2;
		this.groundtruth = gt;
		this.savePath = s_path;
		
		// constraint based
		this.par = new ExperimentParams();
		this.par.debug = this.debug;
		this.par.verbose = this.verbose;		
		this.par = new ExperimentParams();
		this.par.maximum_number_iterations = 30;
		this.par.minimum_number_iterations = 10;
		
		this.number_iterations = 10;
		this.start_n = 5;
		this.end_n = 30;
		
		init();
	}
	public OptimalConstraints(Domain domain1, Domain domain2, int[] m, int start_c, int end_c) {
		this.d1 = domain1;
		this.d2 = domain2;
		this.groundtruth = m;
		this.savePath = "";
		this.saveResutls = false;
		
		this.par = new ExperimentParams();
		this.par.debug = this.debug;
		this.par.verbose = this.verbose;
		this.par = new ExperimentParams();
		this.par.maximum_number_iterations = 30;
		this.par.minimum_number_iterations = 10;
		
		this.number_iterations = 10;
		this.start_n = start_c;
		this.end_n = end_c;
		
		init();
	}
	public OptimalConstraints(Domain domain1, Domain domain2, int[] m, int start_c, int end_c, String s_path) {
		this.d1 = domain1;
		this.d2 = domain2;
		this.groundtruth = m;
		this.savePath = s_path;
		
		this.par = new ExperimentParams();
		this.par.debug = this.debug;
		this.par.verbose = this.verbose;
		this.par = new ExperimentParams();
		this.par.maximum_number_iterations = 30;
		this.par.minimum_number_iterations = 10;
		
		this.number_iterations = 10;
		this.start_n = start_c;
		this.end_n = end_c;
		
		init();
	}
	
	/**
	 * Finds optimal number of constraints using ConstraintBased class, which
	 * utilizes the HMRF-Kmeans algorithm
	 */
	public void find() {
		// run
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < ni; j++) {
				d1.number_constraints = i+start_n;
				d2.number_constraints = j+start_n;
				d1.objF = 0;
				d2.objF = 0;
				
				// framework
				double[] d1_temp = new double[number_iterations];
				double[] d2_temp = new double[number_iterations];
				for (int t = 0; t < number_iterations; t++) {
					ConstraintBased f = new ConstraintBased(d1, d2, par);
					f.Start();
					// keep the result
					//d1_temp[t] = f.getDomain1().objF;
					//d2_temp[t] = f.getDomain2().objF;
					d1_temp[t] = valid.findDBIndex(d1.data, f.getDomain1().idx, f.getDomain1().centroids, f.getDomain1().k, f.getDomain1().distance);
					d2_temp[t] = valid.findDBIndex(d2.data, f.getDomain2().idx, f.getDomain2().centroids, f.getDomain2().k, f.getDomain2().distance);
				}
								
				// keep only the average
				d1_res[i][j] = utils.findMean(d1_temp);
				d2_res[i][j] = utils.findMean(d2_temp);
			}
		}
		
		// save
		if (saveResutls) save();
	}
	
	/**
	 * Finds optimal number of constraints using ConstraintWBased class, which
	 * utilizes the HMRFW-Kmeans algorithm.
	 */
	public void findW() {
		// run
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < ni; j++) {
				d1.number_constraints = i+start_n;
				d2.number_constraints = j+start_n;
				d1.objF = 0;
				d2.objF = 0;
				
				// framework
				double[] d1_temp = new double[number_iterations];
				double[] d2_temp = new double[number_iterations];
				for (int t = 0; t < number_iterations; t++) {
					ConstraintWBased f = new ConstraintWBased(d1, d2, par);
					f.Start();
					// keep the result
					//d1_temp[t] = f.getDomain1().objF;
					//d2_temp[t] = f.getDomain2().objF;
					d1_temp[t] = valid.findDBIndex(d1.data, f.getDomain1().idx, f.getDomain1().centroids, f.getDomain1().k, f.getDomain1().distance);
					d2_temp[t] = valid.findDBIndex(d2.data, f.getDomain2().idx, f.getDomain2().centroids, f.getDomain2().k, f.getDomain2().distance);
				}
								
				// keep only the average
				d1_res[i][j] = utils.findMean(d1_temp);
				d2_res[i][j] = utils.findMean(d2_temp);
			}
		}
		
		// save
		if (saveResutls) save();
	}
	
	/** 
	 * Find DB indices for both domains with respect to the specific number of
	 * constraints in each domain. Use the ConstraintBased class.
	 * @param d1_n - number of constraints in first domain
	 * @param d2_n - number of constraints in second domain
	 * @return Pair of two DB indices values
	 */
	public OptimalConstraintsPair findOnce(int d1_n, int d2_n) {
		// run
		d1.number_constraints = d1_n;
		d2.number_constraints = d2_n;
		d1.objF = 0;
		d2.objF = 0;
		
		// framework
		double[] d1_temp = new double[number_iterations];
		double[] d2_temp = new double[number_iterations];
		for (int t = 0; t < number_iterations; t++) {
			ConstraintBased f = new ConstraintBased(d1, d2, par);
			f.Start();
			// keep the result
			d1_temp[t] = valid.findDBIndex(d1.data, f.getDomain1().idx, f.getDomain1().centroids, f.getDomain1().k, f.getDomain1().distance);
			d2_temp[t] = valid.findDBIndex(d2.data, f.getDomain2().idx, f.getDomain2().centroids, f.getDomain2().k, f.getDomain2().distance);
		}
						
		// keep only the average
		double d1_r = utils.findMean(d1_temp);
		double d2_r = utils.findMean(d2_temp);
		return new OptimalConstraintsPair(d1_r, d2_r);
	}
	
	/** 
	 * Finds specified validation indices for both domains with respect to the specific number of
	 * constraints in each domain. Use the ConstraintBased class.
	 * @param d1_n - number of constraints in first domain
	 * @param d2_n - number of constraints in second domain
	 * @return Pair of two DB indices values
	 */
	public OptimalConstraintsPair findOnce(int d1_n, int d2_n, ValidationIndex vi) {
		// run
		d1.number_constraints = d1_n;
		d2.number_constraints = d2_n;
		d1.objF = 0;
		d2.objF = 0;
		
		// framework
		double[] d1_temp = new double[number_iterations];
		double[] d2_temp = new double[number_iterations];
		for (int t = 0; t < number_iterations; t++) {
			ConstraintBased f = new ConstraintBased(d1, d2, par);
			f.Start();
			// keep the result
			d1_temp[t] = valid.Validate(f.getDomain1(), groundtruth, vi);
			d2_temp[t] = valid.Validate(f.getDomain2(), groundtruth, vi);
		}
						
		// keep only the average
		double d1_r = utils.findMean(d1_temp);
		double d2_r = utils.findMean(d2_temp);
		return new OptimalConstraintsPair(d1_r, d2_r);
	}
	
	/** 
	 * Find DB indices for both domains with respect to the specific number of
	 * constraints in each domain. Use the ConstraintWBased class.
	 * @param d1_n - number of constraints in first domain
	 * @param d2_n - number of constraints in second domain
	 * @return Pair of two DB indices values
	 */
	public OptimalConstraintsPair findWOnce(int d1_n, int d2_n) {
		// run
		d1.number_constraints = d1_n;
		d2.number_constraints = d2_n;
		d1.objF = 0;
		d2.objF = 0;
		
		// framework
		double[] d1_temp = new double[number_iterations];
		double[] d2_temp = new double[number_iterations];
		for (int t = 0; t < number_iterations; t++) {
			ConstraintWBased f = new ConstraintWBased(d1, d2, par);
			f.Start();
			// keep the result
			d1_temp[t] = valid.findDBIndex(d1.data, f.getDomain1().idx, f.getDomain1().centroids, f.getDomain1().k, f.getDomain1().distance);
			d2_temp[t] = valid.findDBIndex(d2.data, f.getDomain2().idx, f.getDomain2().centroids, f.getDomain2().k, f.getDomain2().distance);
		}
						
		// keep only the average
		double d1_r = utils.findMean(d1_temp);
		double d2_r = utils.findMean(d2_temp);
		return new OptimalConstraintsPair(d1_r, d2_r);
	}
	
	/** 
	 * Find DB indices for both domains with respect to the specific number of
	 * constraints in each domain. Use the ConstraintWBased class.
	 * @param d1_n - number of constraints in first domain
	 * @param d2_n - number of constraints in second domain
	 * @return Pair of two DB indices values
	 */
	public OptimalConstraintsPair findWOnceV2(int d1_n, int d2_n) {
		// run
		d1.number_constraints = d1_n;
		d2.number_constraints = d2_n;
		d1.objF = 0;
		d2.objF = 0;
		
		// framework
		double[] d1_temp = new double[number_iterations];
		double[] d2_temp = new double[number_iterations];
		for (int t = 0; t < number_iterations; t++) {
			ConstraintWBasedV2 f = new ConstraintWBasedV2(d1, d2, par);
			f.Start();
			// keep the result
			d1_temp[t] = valid.findDBIndex(d1.data, f.getDomain1().idx, f.getDomain1().centroids, f.getDomain1().k, f.getDomain1().distance);
			d2_temp[t] = valid.findDBIndex(d2.data, f.getDomain2().idx, f.getDomain2().centroids, f.getDomain2().k, f.getDomain2().distance);
		}
						
		// keep only the average
		double d1_r = utils.findMean(d1_temp);
		double d2_r = utils.findMean(d2_temp);
		return new OptimalConstraintsPair(d1_r, d2_r);
	}
	
	/** 
	 * Finds specified validation index for both domains with respect to the specific number of
	 * constraints in each domain. Use the ConstraintWBased class.
	 * @param d1_n - number of constraints in first domain
	 * @param d2_n - number of constraints in second domain
	 * @return Pair of two DB indices values
	 */
	public OptimalConstraintsPair findWOnce(int d1_n, int d2_n, ValidationIndex vi) {
		// run
		d1.number_constraints = d1_n;
		d2.number_constraints = d2_n;
		d1.objF = 0;
		d2.objF = 0;
		
		// framework
		double[] d1_temp = new double[number_iterations];
		double[] d2_temp = new double[number_iterations];
		for (int t = 0; t < number_iterations; t++) {
			ConstraintWBased f = new ConstraintWBased(d1, d2, par);
			f.Start();
			// keep the result
			d1_temp[t] = valid.Validate(f.getDomain1(), groundtruth, vi);
			d2_temp[t] = valid.Validate(f.getDomain2(), groundtruth, vi);
		}
						
		// keep only the average
		double d1_r = utils.findMean(d1_temp);
		double d2_r = utils.findMean(d2_temp);
		return new OptimalConstraintsPair(d1_r, d2_r);
	}
	
	/** 
	 * Finds specified validation index for both domains with respect to the specific number of
	 * constraints in each domain. Use the ConstraintWBasedV2 class.
	 * @param d1_n - number of constraints in first domain
	 * @param d2_n - number of constraints in second domain
	 * @return Pair of two DB indices values
	 */
	public OptimalConstraintsPair findWOnceV2(int d1_n, int d2_n, ValidationIndex vi) {
		// run
		d1.number_constraints = d1_n;
		d2.number_constraints = d2_n;
		d1.objF = 0;
		d2.objF = 0;
		
		// framework
		double[] d1_temp = new double[number_iterations];
		double[] d2_temp = new double[number_iterations];
		for (int t = 0; t < number_iterations; t++) {
			ConstraintWBasedV2 f = new ConstraintWBasedV2(d1, d2, par);
			f.Start();
			// keep the result
			d1_temp[t] = valid.Validate(f.getDomain1(), groundtruth, vi);
			d2_temp[t] = valid.Validate(f.getDomain2(), groundtruth, vi);
		}
						
		// keep only the average
		double d1_r = utils.findMean(d1_temp);
		double d2_r = utils.findMean(d2_temp);
		return new OptimalConstraintsPair(d1_r, d2_r);
	}
	
	public void save() {
		// write it to file
		if (verbose) System.out.println("Save to the " + savePath);
		utils.saveToFile(d1_res, savePath + "\\" + System.currentTimeMillis() + "_number_constraints_d1.data");
		utils.saveToFile(d2_res, savePath + "\\" + System.currentTimeMillis() + "_number_constraints_d2.data");
	}

	private void init() {
		this.ni = this.end_n - this.start_n + 1;
		d1_res = new double[ni][ni];
		d2_res = new double[ni][ni];
				
		utils = new CommonUtils(verbose);
		valid = new ClusterValidation(verbose);
	}
	
	public class OptimalConstraintsPair implements Serializable {
		public double dd1 = 0;
		public double dd2 = 0;
		public OptimalConstraintsPair(double d1_res, double d2_res) {
			this.dd1 = d1_res;
			this.dd2 = d2_res;
		}
	}
	
}
