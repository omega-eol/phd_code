package ids.clustering.model;

public class Clusters {

	public double[][] centroids;
	public int[] clusterSizes;
	public int[] idx;
	
	public Clusters(double[][] centroids, int[] clusterSizes) {
		this.centroids = centroids;
		this.clusterSizes = clusterSizes;
	}
	public Clusters(double[][] centroids, int[] clusterSizes, int[] idx) {
		this.centroids = centroids;
		this.clusterSizes = clusterSizes;
		this.idx = idx;
	}
	

}
