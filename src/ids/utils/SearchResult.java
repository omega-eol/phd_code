package ids.utils;

import java.util.List;

public class SearchResult<T> {

	private T value;
	private int index = 0;
	private double[][] data;
	private List<Integer> indices;
		
	/// Constructor
	public SearchResult(T value, int index) {
		this.value = value;
		this.index = index;
	}
	public SearchResult(double[][] data, List<Integer> indices) {
		this.data = data;
		this.indices = indices;
	}
		
	// getters and setters
	public T getValue() {
		return this.value;
	}
	public void setValue(T value) {
		this.value = value;
	}
	public int getIndex() {
		return this.index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
	public double[][] getData() { return data; }
	public void setData(double[][] data) { this.data = data; }
	public List<Integer> getIndices() { return indices; }
	public void setIndices(List<Integer> indices) { this.indices = indices; }
	
}
