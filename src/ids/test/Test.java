package ids.test;

import java.util.ArrayList;

public class Test {
	
	public static void main(String[] args) {
		int i = 125;
		int v_tt = 0;
		ArrayList<Integer> temp = new ArrayList<Integer>();
		while(true) {
			v_tt = v_tt + 1;
			int q = i % 10;
			temp.add(q);
			
			i = (int)(i/10);
			
			System.out.println("q = " + q);
			System.out.println("i = " + i);
			if (i == 0) break;
			//if (v_tt>3) break;
		}
		
	}

}
