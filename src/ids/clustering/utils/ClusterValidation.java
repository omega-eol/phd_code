package ids.clustering.utils;

import java.io.Serializable;
import java.util.Set;

import ids.clustering.model.Distance;
import ids.clustering.model.Domain;
import ids.utils.CommonUtils;
import ids.utils.SearchResult;
import ids.utils.UniqueResult;

@SuppressWarnings("serial")
public class ClusterValidation implements Serializable {

	private CommonUtils utils;
	private ClusterUtils clusterUtils;
	private boolean verbose;	
	
	public ClusterValidation() {
		verbose = false;
		utils = new CommonUtils(false);
		clusterUtils = new ClusterUtils(false);
	}
	public ClusterValidation(boolean vr) {
		verbose = vr;
		utils = new CommonUtils(verbose);
		clusterUtils = new ClusterUtils(verbose);
	}
	
	/**
	 * Finds the value of the specified validation index
	 * @param d - input domain
	 * @param m - ground truth 
	 * @param vi - validation index
	 * @return
	 */
	public double Validate(Domain d, int[] m, ValidationIndex vi) {
		double res = 0;
		switch (vi) {
			case DB:
				res = findDBIndex(d.data, d.idx, d.centroids, d.k, d.distance);
				break;
			case NMI:
				res = findNMI(d.idx, m);
				break;
			//TODO finish for other validation indices
		}
		
		return res;
	}

	/* EXTERNAL */
	
	/**
	 * Find the normalized mutual information (NMI)
	 * @param X
	 * @param y
	 * @param modeX
	 * @param modeY
	 * @return
	 */
	public double[] findNMI(double[][] X, double[] y, String modeX, String modeY) {
		int n = X.length;
		if (n == 0) return null;
		if (n != y.length) return null;
		int dim = X[0].length;
		
		// initialization
		double[] mu = new double[dim];
		
		// pdf of Y
		double[] yi = null;
		double[] py = null;
		if (modeY.toLowerCase().equals("unique")) {
			CoordinatesPair cp = getDiscretePDF(y);
			yi = cp.x_double;
			py = cp.y_double;			
		} else if (modeY.toLowerCase().equals("bin")) {
			CoordinatesPair cp = getHistBin(y);
			yi = cp.x_double;
			py = cp.y_double; 
		} else {
			return null;
		}
		
		int npointsY = yi.length;
		double hy = findH(py);
		
		// for each dimension
		double[] xi = null;
		double[] px = null;
		for (int i = 0; i < dim; i++) {
			double[] x = utils.getColumn(X, i);
			
			if (modeX.toLowerCase().equals("unique")) {
				CoordinatesPair cp = getDiscretePDF(x);
				xi = cp.x_double;
				px = cp.y_double;			
			} else if (modeX.toLowerCase().equals("bin")) {
				CoordinatesPair cp = getHistBin(x);
				xi = cp.x_double;
				px = cp.y_double; 
			} else {
				return null;
			}
			
			int npointsX = xi.length;
			double hx = findH(px);
			
			// find pdf of joint probability
			double[][] pxy = new double[npointsX][npointsY];
			double stepX = xi[1] - xi[0];
			double stepY = yi[1] - yi[0];
			for (int k = 0; k < npointsX; k++) {
				for (int j = 0; j < npointsY; j++) {
					boolean[] a = null;
					boolean[] b = null;;
					if ((modeX.toLowerCase().equals("unique"))&(modeY.toLowerCase().equals("unique"))) {
						a = utils.getIndexByValue(x, xi[k]);
						b = utils.getIndexByValue(y, yi[j]);
					} else if ((modeX.toLowerCase().equals("bin"))&(modeY.toLowerCase().equals("bin"))) {
						a = utils.findValueBetweenAandB(x, xi[k], xi[k] + stepX);
						b = utils.findValueBetweenAandB(y, yi[j], yi[j] + stepY);
					} else if ((modeX.toLowerCase().equals("unique"))&(modeY.toLowerCase().equals("bin"))) {
						a = utils.getIndexByValue(x, xi[k]);
						b = utils.findValueBetweenAandB(y, yi[j], yi[j] + stepY);
					} else if ((modeX.toLowerCase().equals("bin"))&(modeY.toLowerCase().equals("unique"))) {
						a = utils.findValueBetweenAandB(x, xi[k], xi[k] + stepX);
						b = utils.getIndexByValue(y, yi[j]);
					}
					pxy[k][j] = 1.0*utils.findSum(utils.findAandB(a, b))/n;
					if ((pxy[k][j] > 0)&(px[k] != 0)&(py[j] != 0)) {
						mu[i] += pxy[k][j]*Math.log(pxy[k][j]/px[k]/py[j])/Math.log(2.0);
					}
				}
			}
			
			// normalized
			mu[i] = mu[i]/Math.sqrt(hy*hx);
		}
		
		if (verbose) {
			System.out.println("Normalized Mutual Information per feature");
			utils.printVector(mu);
		}		
		return mu;
	}
	
	/**
	 * Find the normalized mutual information (NMI)
	 * @param x - input vector
	 * @param y
	 * @param modeX
	 * @param modeY
	 * @return
	 */
	public double findNMI(double[] x, double[] y, String modeX, String modeY) {
		int n = x.length;
		if (n == 0) return -1.0;
		if (n != y.length) return -1.0;
				
		// initialization
		double mu = 0;
		
		// pdf of Y
		double[] yi = null;
		double[] py = null;
		if (modeY.toLowerCase().equals("unique")) {
			CoordinatesPair cp = getDiscretePDF(y);
			yi = cp.x_double;
			py = cp.y_double;			
		} else if (modeY.toLowerCase().equals("bin")) {
			CoordinatesPair cp = getHistBin(y);
			yi = cp.x_double;
			py = cp.y_double; 
		} else {
			return -1.0;
		}
		
		int npointsY = yi.length;
		double hy = findH(py);
		
		// for each dimension
		double[] xi = null;
		double[] px = null;
		
		if (modeX.toLowerCase().equals("unique")) {
			CoordinatesPair cp = getDiscretePDF(x);
			xi = cp.x_double;
			px = cp.y_double;			
		} else if (modeX.toLowerCase().equals("bin")) {
			CoordinatesPair cp = getHistBin(x);
			xi = cp.x_double;
			px = cp.y_double; 
		} else {
			return -1.0;
		}
		
		int npointsX = xi.length;
		double hx = findH(px);
		
		// find pdf of joint probability
		double[][] pxy = new double[npointsX][npointsY];
		double stepX = xi[1] - xi[0];
		double stepY = yi[1] - yi[0];
		for (int k = 0; k < npointsX; k++) {
			for (int j = 0; j < npointsY; j++) {
				boolean[] a = null;
				boolean[] b = null;;
				if ((modeX.toLowerCase().equals("unique"))&(modeY.toLowerCase().equals("unique"))) {
					a = utils.getIndexByValue(x, xi[k]);
					b = utils.getIndexByValue(y, yi[j]);
				} else if ((modeX.toLowerCase().equals("bin"))&(modeY.toLowerCase().equals("bin"))) {
					a = utils.findValueBetweenAandB(x, xi[k], xi[k] + stepX);
					b = utils.findValueBetweenAandB(y, yi[j], yi[j] + stepY);
				} else if ((modeX.toLowerCase().equals("unique"))&(modeY.toLowerCase().equals("bin"))) {
					a = utils.getIndexByValue(x, xi[k]);
					b = utils.findValueBetweenAandB(y, yi[j], yi[j] + stepY);
				} else if ((modeX.toLowerCase().equals("bin"))&(modeY.toLowerCase().equals("unique"))) {
					a = utils.findValueBetweenAandB(x, xi[k], xi[k] + stepX);
					b = utils.getIndexByValue(y, yi[j]);
				}
				pxy[k][j] = 1.0*utils.findSum(utils.findAandB(a, b))/n;
				if ((pxy[k][j] > 0)&(px[k] != 0)&(py[j] != 0)) {
					mu += pxy[k][j]*Math.log(pxy[k][j]/px[k]/py[j])/Math.log(2.0);
				}
			}

		}
		// normalized
		mu = mu/Math.sqrt(hy*hx);
		if (verbose) System.out.printf("Normalized Mutual Information: %5.4f\n", mu);
		return mu;
	}
	
	/**
	 * Find the normalized mutual information (NMI)
	 * @param X
	 * @param y
	 * @param modeX
	 * @param modeY
	 * @return
	 */
	public double findNMI(int[] x, int[] y) {
		int n = x.length;
		if (n == 0) return -1.0;
		if (n != y.length) return -1.0;
		
		// initialization
		double mu = 0;
		
		// pdf of Y
		CoordinatesPair cp_y = getDiscretePDF(y);
		int[] yi = cp_y.x_int;
		double[] py = cp_y.y_double;			
		int npointsY = yi.length;
		double hy = findH(py);
		
		// for each dimension
		CoordinatesPair cp_x = getDiscretePDF(x);
		int[] xi = cp_x.x_int;
		double[] px = cp_x.y_double;			
		int npointsX = xi.length;
		double hx = findH(px);
		
		// find pdf of joint probability
		double[][] pxy = new double[npointsX][npointsY];
		for (int k = 0; k < npointsX; k++) {
			for (int j = 0; j < npointsY; j++) {
				boolean[] a = null;
				boolean[] b = null;;
				a = utils.getIndexByValue(x, xi[k]);
				b = utils.getIndexByValue(y, yi[j]);
				pxy[k][j] = 1.0*utils.findSum(utils.findAandB(a, b))/n;
				if ((pxy[k][j] > 0)&(px[k] != 0)&(py[j] != 0)) {
					mu += pxy[k][j]*Math.log(pxy[k][j]/px[k]/py[j])/Math.log(2.0);
				}
			}			
		}
		
		// normalized
		mu = mu/Math.sqrt(hy*hx);
		// output
		if (verbose) System.out.printf("Normalized Mutual Information: %5.4f\n", mu);
		return mu;
	}
	
	private double findH(double[] py) {
		int n = py.length;
		if (n == 0) return .0;
		double h = 0;
		for (int i = 0; i < n; i++) {
			if (py[i] > 0) h += py[i]*Math.log(py[i])/Math.log(2.0);
		}
		return h;
	}
	private double findH(int[] py) {
		int n = py.length;
		if (n == 0) return .0;
		double h = 0;
		for (int i = 0; i < n; i++) {
			if (py[i] > 0) h += py[i]*Math.log(py[i])/Math.log(2.0);
		}
		return -1.0*h;
	}
	private CoordinatesPair getHistBin(double[] y) {
		SearchResult<Double> sr_min = utils.getMinValue(y);
		SearchResult<Double> sr_max = utils.getMaxValue(y);
		double[] yi = utils.linspace(sr_min.getValue(), sr_max.getValue(), 100);
		int[] py_int = utils.histc(y, yi);
		double py_sum = utils.findSum(py_int);
		double[] py = new double[yi.length];
		for (int i = 0; i < yi.length; i++) py[i] = py_int[i]*1.0/py_sum;
		return new CoordinatesPair(yi, py);
	}
	private CoordinatesPair getDiscretePDF(double[] y) {
		int n = y.length;
		if (n==0) return null;
		UniqueResult<Double> un = utils.findUnique(y);
		double[] yi = new double[un.frequency.length];
		double[] py = new double[un.frequency.length];
		for (int i = 0; i < un.frequency.length; i++) {
			yi[i] = (double)un.domain[i];
			py[i] = 1.0*un.frequency[i]/n;
		}
		return new CoordinatesPair(yi, py);
	}
	private CoordinatesPair getDiscretePDF(int[] y) {
		int n = y.length;
		if (n==0) return null;
		UniqueResult<Integer> un = utils.findUnique(y);
		int[] yi = new int[un.frequency.length];
		double[] py = new double[un.frequency.length];
		for (int i = 0; i < un.frequency.length; i++) {
			yi[i] = (int)un.domain[i];
			py[i] = 1.0*un.frequency[i]/n;
		}
		return new CoordinatesPair(yi, py);
	}
	private class CoordinatesPair {
		public double[] x_double;
		public double[] y_double;
		public int[] x_int;
		
		public CoordinatesPair(double[] x_, double[] y_) {
			this.x_double = x_;
			this.y_double = y_;
		}
		public CoordinatesPair(int[] x_, double[] y_) {
			this.x_int = x_;
			this.y_double = y_;
		}
	}
	
	/* INTERNAL */
	
	/**
	 * Finds Silhouette index
	 * @param data - input data set nxd
	 * @param idx - input membership vector nx1
	 * @param k - number of clusters
	 * @param distance - distance measure
	 * @return Silhouette index for each points nx1
	 */
	public double[] findSilhouette(double[][] data, int[] idx, int k, float[] pd) {
		int n = data.length;
		if (n == 0) return null;
		
		// store silhouette index here 
		double[] s = new double[n];
		
		// is the average distance from the i-th point to points in another cluster k.
		// by default should be Infinity
		double[][] avgd_between = new double[n][k];
		utils.fillMatrix(avgd_between, Double.POSITIVE_INFINITY);
		
		// is the average distance from the i-th point to the other points in its own cluster
		double[] avgd_within = new double[n];
		
		// for every point in the data set
		for (int j = 0; j < n; j++) {
			double[] distj = utils.getDistance(j, pd, n);
			// compute average distance by cluster number
			for (int i = 0; i < k; i++) {
				Set<Integer> members = utils.getIndicesByValue(idx, i);
				double distj_sum = 0;
				for (Integer m : members) distj_sum += distj[m]; 
				if (i == idx[j]) {
					avgd_within[j] = distj_sum / Math.max(members.size()-1,1);
				} else {
					avgd_between[j][i] = distj_sum / members.size();
				}
			}
		}
		
		// Calculate the silhouette values
		double[] minAvgDBetween = utils.getMinValueInRows(avgd_between);
		for (int j = 0; j < n; j++) {
			s[j] = (minAvgDBetween[j] - avgd_within[j]) / Math.max(avgd_within[j], minAvgDBetween[j]);
		}
		if (verbose) System.out.printf("Silhouette Index: %5.4f\n", utils.findMean(s));
		return s;
	}
	
	/**
	 * Finds Silhouette index
	 * @param data - input data set nxd
	 * @param idx - input membership vector nx1
	 * @param k - number of clusters
	 * @param distance - distance measure
	 * @return Silhouette index for each points nx1
	 */
	public double[] findSilhouette(double[][] data, int[] idx, int k, Distance distance) {
		int n = data.length;
		if (n == 0) return null;
		
		// store silhouette index here 
		double[] s = new double[n];
		
		// is the average distance from the i-th point to points in another cluster k.
		// by default should be Infinity
		double[][] avgd_between = new double[n][k];
		utils.fillMatrix(avgd_between, Double.POSITIVE_INFINITY);
		
		// is the average distance from the i-th point to the other points in its own cluster
		double[] avgd_within = new double[n];
		
		// for every point in the data set
		for (int j = 0; j < n; j++) {
			double[] distj = utils.getDistance(data, data[j], distance);
			// compute average distance by cluster number
			for (int i = 0; i < k; i++) {
				Set<Integer> members = utils.getIndicesByValue(idx, i);
				double distj_sum = 0;
				for (Integer m : members) distj_sum += distj[m]; 
				if (i == idx[j]) {
					avgd_within[j] = distj_sum / Math.max(members.size()-1,1);
				} else {
					avgd_between[j][i] = distj_sum / members.size();
				}
			}
		}
		
		// Calculate the silhouette values
		double[] minAvgDBetween = utils.getMinValueInRows(avgd_between);
		for (int j = 0; j < n; j++) {
			s[j] = (minAvgDBetween[j] - avgd_within[j]) / Math.max(avgd_within[j], minAvgDBetween[j]);
		}
		if (verbose) System.out.printf("Silhouette Index: %5.4f\n", utils.findMean(s));
		return s;
	}
	
	/**
	 * Finds Dunn`s index of the membership vector
	 * @param data - input data
	 * @param idx - input membership vector
	 * @param k - number of clusters
	 * @param ds - distance measure
	 * @return
	 */
	public double findDunnIndex(double[][] data, int[] idx, int k, Distance ds) {
		int n = data.length;
		if (n == 0) return 0;		
		// pre-compute distance between all points in the data set
		float[] pd_dist = utils.getDistance(data, ds);		
		return findDunnIndex(data, idx, k, pd_dist);
	}
	
	/**
	 * Finds Dunn`s index of the membership vector
	 * @param data - input data
	 * @param idx - input membership vector
	 * @param k - number of clusters
	 * @param ds_dist - distance vector
	 * @return
	 */
	public double findDunnIndex(double[][] data, int[] idx, int k, float[] pd_dist) {
		int n = data.length;
		if (n == 0) return 0;
		
		// find all clusters` diameters
		double[] diam = new double[k];
		for (int i = 0; i < k; i++) {
			diam[i] = getClusterDiameter(pd_dist, idx, i);
			if (verbose) System.out.printf("Cluster diameter: diam[%d] = %5.4f\n", i, diam[i]);
		}
		
		// find maximum cluster diameter
		SearchResult<Double> sr = utils.getMaxValue(diam);
		double max_diam = sr.getValue();
		if (max_diam == 0) { 
			System.out.printf("Maximum cluster diameter is 0!\n");
			return -1;
		}
		if (verbose) System.out.printf("Maximum cluster diameter is %5.4f\n", max_diam);
		
		// find distance between all clusters
		double[][] d = new double[k][k];
		for (int i = 0; i < k; i++) d[i][i] = Double.MAX_VALUE;
		for (int i = 0; i < k; i++) {
			for (int j = i+1; j < k; j++) {
				d[i][j] = getDistanceBetweenClusters(pd_dist, idx, i, j)/max_diam;
				d[j][i] = d[i][j];
			}
		}
		if (verbose) {
			System.out.println("Distance between the clusters:");
			utils.printMatrix(d);
		}
		
		// find Dunn`s index
		double res = utils.getMinValue(d);
		return res;
	}
	
	/**
	 * Finds minimum distance between any two points from two different clusters
	 * @param pd - distance vector
	 * @param idx - membership vector
	 * @param index_i - index of the first cluster
	 * @param index_j - index of the second cluster
	 * @return distance between clusters
	 */
	private double getDistanceBetweenClusters(float[] pd, int[] idx, int index_i, int index_j) {
		double cluster_ij = Double.MAX_VALUE;
		int n = idx.length;
		if (n==0) return -1.0;
		
		// get cluster data for cluster i and j
		Set<Integer> cluster_data_index_i = utils.getIndicesByValue(idx, index_i);
		Set<Integer> cluster_data_index_j = utils.getIndicesByValue(idx, index_j);
		
		for (Integer s_i : cluster_data_index_i) {
			for (Integer s_j : cluster_data_index_j) {
				double d = utils.getDistanceFromDistanceVector(pd, s_i, s_j, n);
				if (d<cluster_ij) cluster_ij = d;
			}
		}		
		return cluster_ij;
	}
	
	/**
	 * Finds cluster diameter, which is the longest distance within cluster
	 * @param pd
	 * @param idx
	 * @param i
	 * @return cluster`s diameter
	 */
	private double getClusterDiameter(float[] pd, int[] idx, int i) {
		double cluster_d = Double.MIN_VALUE;
		int n = idx.length;
		
		// get cluster data
		Set<Integer> cluster_data_index = utils.getIndicesByValue(idx, i);
		for (Integer s1 : cluster_data_index) {
			for (Integer s2 : cluster_data_index) {
				double d = utils.getDistanceFromDistanceVector(pd, s1, s2, n);
				if (d > cluster_d) cluster_d = d;
			}
		}		
		return cluster_d;
	}
	
	/**
	 * Finds Davies-Bouldin index of the membership vector
	 * @param data - input data
	 * @param idx - input membership vector
	 * @param centroid - cluster`s centeroids 
	 * @param k - number of clusters
	 * @param ds - distance measure
	 * @return
	 */
	public double findDBIndex(double[][] data, int[] idx, double[][] centroid, int k, Distance ds) {
		int n = data.length;
		if (n == 0) return 0;
		
		// average distance between all objects in the cluster and its centroid
		double[] Sn = new double[k];
		// distance between clusters centroids
		double[][] S = new double[k][k];
		
		// for every cluster
		for (int i = 0; i < k; i++) {
			SearchResult<Double> sr = clusterUtils.getClusterData(data, idx, i);
			Sn[i] = findDispersion(sr.getData(), centroid[i], ds);
			for (int j = i+1; j < k; j++) {
				S[i][j] = utils.getDistance(centroid[i], centroid[j], ds);
				S[j][i] = S[i][j];
			}
		}
		
		// find DB index
		double[][] r = new double[k][k];
		for (int i = 0; i < k; i++) {
			for (int j = i+1; j < k; j++) {
				r[i][j] = (Sn[i] + Sn[j])/(S[i][j] + Double.MIN_VALUE);
				r[j][i] = r[i][j];
			}
		}
		
		return utils.findMean(utils.getMaxValue(r, 2));
	}	
	private double findDispersion(double[][] data_i, double[] centoid, Distance ds) {
		double[] pd = utils.getDistance(data_i, centoid, ds);
		return utils.findMean(pd);
	}
}
