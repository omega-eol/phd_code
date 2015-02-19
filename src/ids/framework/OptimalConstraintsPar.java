package ids.framework;

import java.io.Serializable;

import ids.clustering.model.Domain;

@SuppressWarnings("serial")
public class OptimalConstraintsPar implements Serializable {
	
	public Domain domain1;
	public Domain domain2;
	public int number_iterations;
	public int start_n;
	public int end_n;	
	public int[] groundtruth;
	
	public boolean debug = false;
	public boolean verbose = false;
	
	public boolean saveResults;
	public String savePath;

	// par of constraint-based or constraintW-based class
	public int maximum_number_iterations = 30;
	public int minimum_number_iterations = 10;
	
	// must for calling it from Matlab
	public OptimalConstraintsPar() {}
}
