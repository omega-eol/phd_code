package ids.clustering.model;

import java.io.Serializable;


@SuppressWarnings("serial")
public class PairDoublesMatrix implements Serializable {
	public double[][] v1;
	public double[][] v2;
	
	public PairDoublesMatrix(double[][] matrix1, double[][] matrix2) {
		this.v1 = matrix1;
		this.v2 = matrix2;
	}
}
