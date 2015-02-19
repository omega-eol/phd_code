package ids.experiment.dataset;

import java.io.Serializable;

import ids.clustering.model.Clusters;
import ids.clustering.model.Domain;
import ids.clustering.model.ObjectiveFunctionType;
import ids.clustering.model.PairDoublesMatrix;
import ids.clustering.model.PairDoubles;
import ids.clustering.utils.ClusterUtils;
import ids.clustering.utils.ClusterValidation;
import ids.framework.ConstraintBasedU;
import ids.framework.ExperimentParams;
import ids.utils.CommonUtils;

@SuppressWarnings("serial")
public class OptimalNumberConstraints implements Serializable {

	private Domain domain1;
	private Domain domain2;
	
	private int numberOfIterations = 10;
	private boolean verbose = true;	
	
	public OptimalNumberConstraints(Domain domain1, Domain domain2, int numberOfIterations) {
		this.domain1 = domain1;
		this.domain2 = domain2;
		this.numberOfIterations = numberOfIterations;
	}
	
	public PairDoublesMatrix RunAllNMI(int minimumNumberOfConstraints, int maximumNumberOfConstraints, int[] class_labels) {
		int n = (maximumNumberOfConstraints - minimumNumberOfConstraints) + 1;
		if (n<=0) {
			System.out.println("Erorr: Please check the input parameters");
			return null;
		} 
		
		// Run
		double[][] d1_res = new double[n][n];
		double[][] d2_res = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				PairDoubles res = RunOnceNMI(minimumNumberOfConstraints + i, minimumNumberOfConstraints + j, class_labels);
				d1_res[i][j] = res.v1;
				d2_res[i][j] = res.v2;
			}
		}
		
		// output
		if (verbose) {
			// Domain 1
			System.out.println("Domain 1");
			// print header
			System.out.printf("\t");
			for (int i = 0; i < n; i++) System.out.printf("%d\t", minimumNumberOfConstraints+i);
			System.out.printf("\n");
			// print value
			for (int i = 0; i < n; i++) {
				System.out.printf("%d\t", minimumNumberOfConstraints+i);
				for (int j = 0; j < n; j++) System.out.printf("%5.4f\t", d1_res[i][j]);
				System.out.printf("\n");
			}
			
			// Domain 1
			System.out.println("------------------");
			System.out.println("Domain 2");
			// print header
			System.out.printf("\t");
			for (int i = 0; i < n; i++) System.out.printf("%d\t", minimumNumberOfConstraints+i);
			System.out.printf("\n");
			// print value
			for (int i = 0; i < n; i++) {
				System.out.printf("%d\t", minimumNumberOfConstraints+i);
				for (int j = 0; j < n; j++) System.out.printf("%5.4f\t", d2_res[i][j]);
				System.out.printf("\n");
			}
		}
		
		return new PairDoublesMatrix(d1_res, d2_res);
	}
	
	public PairDoublesMatrix RunAllDB(int minimumNumberOfConstraints, int maximumNumberOfConstraints) {
		int n = (maximumNumberOfConstraints - minimumNumberOfConstraints) + 1;
		if (n<=0) {
			System.out.println("Erorr: Please check the input parameters");
			return null;
		} 
		
		// Run
		double[][] d1_res = new double[n][n];
		double[][] d2_res = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				PairDoubles res = RunOnceDB(minimumNumberOfConstraints + i, minimumNumberOfConstraints + j);
				d1_res[i][j] = res.v1;
				d2_res[i][j] = res.v2;
			}
		}
		
		// output
		if (verbose) {
			// Domain 1
			System.out.println("Domain 1");
			// print header
			System.out.printf("\t");
			for (int i = 0; i < n; i++) System.out.printf("%d\t", minimumNumberOfConstraints+i);
			System.out.printf("\n");
			// print value
			for (int i = 0; i < n; i++) {
				System.out.printf("%d\t", minimumNumberOfConstraints+i);
				for (int j = 0; j < n; j++) System.out.printf("%5.4f\t", d1_res[i][j]);
				System.out.printf("\n");
			}
			
			// Domain 1
			System.out.println("------------------");
			System.out.println("Domain 2");
			// print header
			System.out.printf("\t");
			for (int i = 0; i < n; i++) System.out.printf("%d\t", minimumNumberOfConstraints+i);
			System.out.printf("\n");
			// print value
			for (int i = 0; i < n; i++) {
				System.out.printf("%d\t", minimumNumberOfConstraints+i);
				for (int j = 0; j < n; j++) System.out.printf("%5.4f\t", d2_res[i][j]);
				System.out.printf("\n");
			}
		}
		
		return new PairDoublesMatrix(d1_res, d2_res);
	}
	
	public PairDoubles RunOnceNMI(int numberOfConstraintsDomain1, int numberOfConstraintsDomain2, int[] class_labels) {
		// set the experiment parameters
		ExperimentParams par = new ExperimentParams();
		par.keepCentroids = true;
		par.maximum_number_iterations = 30;
		par.minimum_number_iterations = 10;
		par.useTC = true;
		
		// set the number of constraints
		domain1.number_constraints = numberOfConstraintsDomain1;
		domain2.number_constraints = numberOfConstraintsDomain2;
		if (verbose) System.out.printf("Number of Constraints in Domain 1: %d\nNumber of Constraints in Domain 2: %d\n", 
				numberOfConstraintsDomain1, numberOfConstraintsDomain2);
		
		// helpers
		ClusterValidation valid = new ClusterValidation(true);
		CommonUtils utils = new CommonUtils(false);
		
		// run
		double[] d1_nmi = new double[numberOfIterations];
		double[] d2_nmi = new double[numberOfIterations];
		if (verbose) System.out.println("Experimental Results:");
		for (int i = 0; i < numberOfIterations; i++) {
			// start the framework
			ConstraintBasedU f = new ConstraintBasedU(domain1, domain2, par, ObjectiveFunctionType.NO_WEIGTHS);
			f.Start();
			
			d1_nmi[i] = valid.findNMI(f.getDomain1().idx, class_labels);
			d2_nmi[i] = valid.findNMI(f.getDomain2().idx, class_labels);
			if (verbose) System.out.printf("%d.\t%5.4f\t%5.4f\n", i+1, d1_nmi[i], d2_nmi[i]);
		}
		
		// result
		double d1_res = utils.findMean(d1_nmi);
		double d2_res = utils.findMean(d2_nmi);
		if (verbose) {
			System.out.println("------------------");
			System.out.printf("mean:\t%5.4f\t%5.4f\n", d1_res, d2_res);
		}
		
		return new PairDoubles(d1_res, d2_res);
	}
	
	public PairDoubles RunOnceDB(int numberOfConstraintsDomain1, int numberOfConstraintsDomain2) {
		// set the experiment parameters
		ExperimentParams par = new ExperimentParams();
		par.keepCentroids = true;
		par.maximum_number_iterations = 30;
		par.minimum_number_iterations = 10;
		par.useTC = true;
		
		// set the number of constraints
		domain1.number_constraints = numberOfConstraintsDomain1;
		domain2.number_constraints = numberOfConstraintsDomain2;
		if (verbose) System.out.printf("Number of Constraints in Domain 1: %d\nNumber of Constraints in Domain 2: %d\n", 
				numberOfConstraintsDomain1, numberOfConstraintsDomain2);
		
		// helpers
		ClusterValidation valid = new ClusterValidation(false);
		CommonUtils utils = new CommonUtils(false);
		
		// run
		double[] d1_db = new double[numberOfIterations];
		double[] d2_db = new double[numberOfIterations];
		if (verbose) System.out.println("Experimental Results:");
		for (int i = 0; i < numberOfIterations; i++) {
			// start the framework
			ConstraintBasedU f = new ConstraintBasedU(domain1, domain2, par, ObjectiveFunctionType.NO_WEIGTHS);
			f.Start();
			
			d1_db[i] = valid.findDBIndex(f.getDomain1().data, f.getDomain1().idx, f.getDomain1().centroids, f.getDomain1().k, f.getDomain1().distance);
			d2_db[i] = valid.findDBIndex(f.getDomain2().data, f.getDomain2().idx, f.getDomain2().centroids, f.getDomain2().k, f.getDomain2().distance);
			if (verbose) System.out.printf("%d.\t%5.4f\t%5.4f\n", i+1, d1_db[i], d2_db[i]);
		}
		
		// result
		double d1_res = utils.findMean(d1_db);
		double d2_res = utils.findMean(d2_db);
		if (verbose) {
			System.out.println("------------------");
			System.out.printf("mean:\t%5.4f\t%5.4f\n", d1_res, d2_res);
		}
		
		return new PairDoubles(d1_res, d2_res);
	}
	
	/**
	 * This method specially design for the MIRFlickr data set to validate DB index on the current domain and data,
	 * and eternal db indx based on Tags
	 * @param numberOfConstraintsDomain1
	 * @param numberOfConstraintsDomain2
	 * @return
	 */
	public double[] RunOnce4DB(int numberOfConstraintsDomain1, int numberOfConstraintsDomain2, double[][] tags) {
		// set the experiment parameters
		ExperimentParams par = new ExperimentParams();
		par.keepCentroids = true;
		par.maximum_number_iterations = 50;
		par.minimum_number_iterations = 10;
		par.useTC = true;
		
		// set the number of constraints
		domain1.number_constraints = numberOfConstraintsDomain1;
		domain2.number_constraints = numberOfConstraintsDomain2;
		if (verbose) System.out.printf("Number of Constraints in Domain 1: %d\nNumber of Constraints in Domain 2: %d\n", 
				numberOfConstraintsDomain1, numberOfConstraintsDomain2);
		
		// helpers
		ClusterValidation valid = new ClusterValidation(false);
		CommonUtils utils = new CommonUtils(false);
		ClusterUtils clusterUtils = new ClusterUtils(false);
		
		// run
		double[] d1_db = new double[numberOfIterations];
		double[] d2_db = new double[numberOfIterations];
		double[] d1_ext_db = new double[numberOfIterations];
		double[] d2_ext_db = new double[numberOfIterations];
		if (verbose) System.out.println("Experimental Results:");
		for (int i = 0; i < numberOfIterations; i++) {
			// start the framework
			ConstraintBasedU f = new ConstraintBasedU(domain1, domain2, par, ObjectiveFunctionType.NO_WEIGTHS);
			f.Start();
			
			// internal validation
			d1_db[i] = valid.findDBIndex(f.getDomain1().data, f.getDomain1().idx, f.getDomain1().centroids, f.getDomain1().k, f.getDomain1().distance);
			d2_db[i] = valid.findDBIndex(f.getDomain2().data, f.getDomain2().idx, f.getDomain2().centroids, f.getDomain2().k, f.getDomain2().distance);
			// external validation
			Clusters c1 = clusterUtils.getClusterCentoids(tags, f.getDomain1().idx, f.getDomain1().k, f.getDomain1().distance);
			Clusters c2 = clusterUtils.getClusterCentoids(tags, f.getDomain2().idx, f.getDomain2().k, f.getDomain2().distance);
			d1_ext_db[i] = valid.findDBIndex(tags, f.getDomain1().idx, c1.centroids, f.getDomain1().k, f.getDomain1().distance);
			d2_ext_db[i] = valid.findDBIndex(tags, f.getDomain2().idx, c2.centroids, f.getDomain2().k, f.getDomain2().distance);
			if (verbose) {
				System.out.printf("%d.\t%5.4f\t%5.4f\n", i+1, d1_db[i], d2_db[i]);
				System.out.printf("  \t%5.4f\t%5.4f\n", d1_ext_db[i], d2_ext_db[i]);
			}
		}
		
		// result
		double d1_res = utils.findMean(d1_db);
		double d2_res = utils.findMean(d2_db);
		double d1_ext_res = utils.findMean(d1_ext_db);
		double d2_ext_res = utils.findMean(d2_ext_db);
		if (verbose) {
			System.out.println("------------------");
			System.out.printf("mean internal:\t%5.4f\t%5.4f\n", d1_res, d2_res);
			System.out.printf("mean external:\t%5.4f\t%5.4f\n", d1_ext_res, d2_ext_res);
		}
		
		double[] res = new double[4];
		res[0] = d1_res;
		res[1] = d2_res;
		res[2] = d1_ext_res;
		res[3] = d2_ext_res;
		return res;
	}
	
}
