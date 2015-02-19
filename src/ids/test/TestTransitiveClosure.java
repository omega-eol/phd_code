package ids.test;

import java.util.Arrays;

import cern.colt.matrix.DoubleMatrix2D;
import ids.clustering.model.ConstraintType;
import ids.utils.CommonUtils;
import ids.utils.ConstraintsUtils;

public class TestTransitiveClosure {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		heartTC();				
	}
	
	private static void heartTC() {
		CommonUtils utils = new CommonUtils(false);
		ConstraintsUtils constraintsUtils = new ConstraintsUtils(true);
		
		double[][] ndata = utils.readDataFile("datasets/heart/ndata.csv");
		int n = ndata.length;
		double[][] constraints = utils.readDataFile("datasets/heart/constraints.csv");
		DoubleMatrix2D d_constraints = constraintsUtils.ParseConstraints(constraints, n, ConstraintType.MUST_LINK);
		
		DoubleMatrix2D tc = constraintsUtils.TransitiveClosure(d_constraints);
		int[] neighborhoodLambda = constraintsUtils.getNeighborhood();
		System.out.println("Neighborhood Lambda: ");
		//utils.printVector(neighborhoodLambda);
		
		int[] rand_index = utils.getRandomPermutation(10, 10);
		System.out.println("Test random permutations");
		utils.printVector(rand_index);
	}
	
	private static void testIris() {
		CommonUtils c_utils = new CommonUtils(true);
		double[][] A = c_utils.readDataFile("C:\\_Research\\Datasets\\Iris\\a.data", 10, 10); 
		
		ConstraintsUtils utils = new ConstraintsUtils(true);
		double[][] tc = utils.TransitiveClosure(A);
		
		System.out.println("Dense Transitive Closure");
		c_utils.printMatrix(tc, 10, 10);

		System.out.println("Neibh");
		int[] neigh = utils.getNeighborhood();
		System.out.println(Arrays.toString(neigh));
		
		// SPARSE
		DoubleMatrix2D sA = c_utils.readSparseDataFile("C:\\_Research\\Datasets\\Iris\\a.data", 10, 10); 
		DoubleMatrix2D stc = utils.TransitiveClosure(sA);
		
		System.out.println("Sparse Transitive Closure");
		c_utils.printMatrix(stc, 10, 10);

		System.out.println("Neibh");
		int[] sneigh = utils.getNeighborhood();
		System.out.println(Arrays.toString(sneigh));
	}

}
