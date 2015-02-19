package ids.exe;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import ids.clustering.model.Distance;
import ids.utils.ArrayIndexComparator;
import ids.utils.CommonUtils;

public class FindDistance {

	public static void main(String[] args) {
		
		String scores_filename = "/fs_domain_score.csv";
		String domain_keys_filename =  "/domain_keys.csv";
		String output_filename = "/domain_correlation.csv";
		
		// RUN
		processDistance(scores_filename, domain_keys_filename, output_filename);
	}
	
	public static void processDistance(String scores_filename, String domain_keys_filename, String output_filename) {
		// load all utilities
		CommonUtils utils = new CommonUtils(false);
		
		// load scores into the memory
		double[][] scores = utils.readDataFile(scores_filename);
		
		// load domains keys
		int[] domain_keys = utils.readVectorFromFile(domain_keys_filename);
		
		// correlation type
		int type_id = 1;
		int num_to_save = 100;
		int num_domains = domain_keys.length;
		
		// Run
		BufferedWriter out = null; 
		try {
			out = new BufferedWriter(new FileWriter(output_filename));
			// write the header
			String header = "parent_id,child,type_id,value\n";
			//System.out.print(header);
			out.write(header);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		// run
		int div = Math.round((float)(num_domains*0.01));
		for (int i = 0; i < num_domains; i++) {
			double[] a = scores[i];
			// now we have the distance between a and everything else
			double[] pd_cos = utils.getDistance(scores, a, Distance.COSINE);
			int parent = domain_keys[i];
			
			// sort resulted index according to the distance
			ArrayIndexComparator comp = new ArrayIndexComparator(pd_cos);
			Integer[] index = comp.createIndex();
			Arrays.sort(index, comp);
			
			// save to file top num_to_sace distances
			try {
				for (int j = 1; j < num_to_save + 1; j++) {
					int child = domain_keys[index[j]];
					double value = pd_cos[index[j]];
					String str = String.format("%d,%d,%d,%5.4f\n", parent, child, type_id, value);
					out.write(str);
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			
			if ( ( i % div )==0 ) System.out.printf("%4.2f%% is done.\n", (i*100.0/num_domains));
		}
		
		try {
			out.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Done.");
	}

	public static void FDistance() {
		String filename = "c:\\Temp\\fs_domain_score.csv";
		String res_filename = "c:\\Temp\\domain_cosine_distance.csv";
		CommonUtils utils = new CommonUtils(true);
		double[][] data = utils.readDataFile(filename);
		utils.getDistance(data, Distance.COSINE, res_filename);
	}
	
}
