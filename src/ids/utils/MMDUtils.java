package ids.utils;

import java.io.Serializable;
import org.apache.commons.math3.special.Erf;

@SuppressWarnings("serial")
public class MMDUtils implements Serializable {

	private double mmd = 0;
	private double eps = 0;
	
	// Constructors
	public MMDUtils() {}
	public MMDUtils(double[] x, double[] y, double sigma, double alpha) {
		findMMD(x, y, sigma, alpha);
	}
	public MMDUtils(double[][] x, double[][] y, double sigma, double alpha) {
		findMMD(x, y, sigma, alpha);
	}	
	
	public void findMMD(double[][] x, double[][] y, double sigma, double alpha) {
		double m = x.length;
		if (m != y.length) {
			System.out.println("Data sets has different number of points");
		}
		double mmd_sq = 0;
		double sigma_sq = 0;
		double t = 0;
		double th = 0;
		
		// find MMD
		for (int i = 0; i < m; i++) {
			t = 0;
			for (int j = 0; j < m; j++) {
				if (i != j) {
					t = t + getGaussianKernel(x[i], x[j], sigma) + getGaussianKernel(y[i], y[j], sigma) -
							getGaussianKernel(x[i], y[j], sigma) - getGaussianKernel(x[j], y[i], sigma);
				}
			}
			
			mmd_sq = mmd_sq + 1/m/(m-1)*t;
			sigma_sq = sigma_sq + t*t;
		}
		sigma_sq = 4*sigma_sq/m/m/(m-1)/(m-1) - 4/m*mmd_sq*mmd_sq;
		th = Math.sqrt(2*sigma_sq)*Erf.erfInv(1-2*alpha);
		
		this.mmd = mmd_sq;
		this.eps = th;
		if (this.mmd <= this.eps) {
			System.out.println("Distributions are the same");
		} else {
			System.out.println("Distributions are different");
		}
	}
	public void findMMD(double[] x, double[] y, double sigma, double alpha) {
		double m = x.length;
		if (m != y.length) {
			System.out.println("Data sets has different number of points");
		}
		double mmd_sq = 0;
		double sigma_sq = 0;
		double t = 0;
		double th = 0;
		
		// find MMD
		for (int i = 0; i < m; i++) {
			t = 0;
			for (int j = 0; j < m; j++) {
				if (i != j) {
					t = t + getGaussianKernel(x[i], x[j], sigma) + getGaussianKernel(y[i], y[j], sigma) -
							getGaussianKernel(x[i], y[j], sigma) - getGaussianKernel(x[j], y[i], sigma);
				}
			}
			
			mmd_sq = mmd_sq + 1/m/(m-1)*t;
			sigma_sq = sigma_sq + t*t;
		}
		sigma_sq = 4*sigma_sq/m/m/(m-1)/(m-1) - 4/m*mmd_sq*mmd_sq;
		th = Math.sqrt(2*sigma_sq)*Erf.erf(1-2*alpha);
		
		this.mmd = mmd_sq;
		this.eps = th;
		if (this.mmd <= this.eps) {
			System.out.println("Distributions are the same");
		} else {
			System.out.println("Distributions are different");
		}
	}
	
	public double getMMD() {
		return this.mmd;
	}
	public double getEps() {
		return this.eps;
	}
	
	// Gaussian Kernel
	private double getGaussianKernel(double x[], double y[], double sigma) {
		if (sigma == 0) {
			System.out.println("Sigma is set to 0");
			return -1.0;
		}
		int n = x.length;
		if (n != y.length) {
			System.out.println("Vector x and y have different length");
			return -1.0;
		}
		double c[] = new double[n];
		double ss = 0;
		for (int i = 0; i < n; i++) {
			c[i] = x[i] - y[i];
			ss = ss + c[i]*c[i];
		}
		return Math.exp(-ss/2/sigma/sigma);
	}
	private double getGaussianKernel(double x, double y, double sigma) {
		if (sigma == 0) {
			System.out.println("Sigma is set to 0");
			return -1.0;
		}
		return Math.exp(-(x-y)*(x-y)/2/sigma/sigma);
	}
	
}
