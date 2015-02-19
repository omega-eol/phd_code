package ids.clustering.model;

import cern.colt.matrix.DoubleMatrix2D;

public class Constraints {

	public DoubleMatrix2D Cconstraints;
	public DoubleMatrix2D Mconstraints;
	
	public Constraints(DoubleMatrix2D Cc, DoubleMatrix2D Mc) {
		Cconstraints = Cc;
		Mconstraints = Mc;
	}
	
}
