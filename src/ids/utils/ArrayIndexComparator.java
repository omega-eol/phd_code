package ids.utils;

import java.util.Comparator;

public class ArrayIndexComparator implements Comparator<Integer> {
	
	public final double[] pd;
			
	public ArrayIndexComparator(double[] array) {
		this.pd = array;
	}
	
	public Integer[] createIndex() {
		Integer[] index = new Integer[pd.length];
		for (int i = 0; i < pd.length; i++) {
			index[i] = i;
		}
		return index;
	}
	
	@Override
	public int compare(Integer o1, Integer o2) {
		return Double.compare(pd[o1], pd[o2]);
	}

}
