package ids.clustering.model;


public class Pair {
	
	public int a;
	public int b;
	public ConstraintType type;
	
	public Pair(int a, int b, ConstraintType t) {
		this.a = a;
		this.b = b;
		this.type = t;
	}

}
