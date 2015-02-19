package ids.framework;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ExperimentParams implements Serializable {
	
	// maximum number of iterations
	public int maximum_number_iterations = 20;
	public int minimum_number_iterations = 3;
		
	public boolean verbose = false;
	public boolean debug = false;
	
	public boolean useTC = true;
	public boolean keepCentroids = false;
}
