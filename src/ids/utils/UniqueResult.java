package ids.utils;

public class UniqueResult<T> {
	
	public T[] domain;
	public int[] frequency;
	
	public UniqueResult(T[] d, int[] f) {
		this.domain = d;
		this.frequency = f;
	}
}


