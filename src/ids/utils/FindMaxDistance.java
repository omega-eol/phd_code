package ids.utils;

import ids.clustering.model.Distance;

public class FindMaxDistance {
	
	private static boolean verbose;
	private CommonUtils utils;
	
	// CONSTRACTOR
	public FindMaxDistance(boolean verbose) {
		this.verbose = verbose;
		utils = new CommonUtils(verbose);
		
	}
	
	public double getMaxDistance(double[][] data, int nrows, int ncols, Distance ds) {
		if (ds == Distance.MATCH) return ncols;
		if (ds == Distance.COSINE) return 1;
		
		double maxD = 0;
		int m = nrows*(nrows-1)/2;
		int index = 0;
		int step = (int)(m*0.01);
		if (step==0) step = m;
		
		for (int i=0; i<nrows; i++) {
			for (int j=i+1; j<nrows; j++) {
				index = index + 1;
				double currentD = utils.getDistance(data[i], data[j], ds);
				if (currentD>maxD) {
					maxD = currentD;
				}
				// print
				if ((verbose)&((index % step)==0)) {
					System.out.println((double)index/m*100 + "% is done.");
				}
				
			}
		}
		
		return maxD;
	}
	
}


