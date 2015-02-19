package ids.experiment.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import ids.clustering.model.Distance;
import ids.clustering.model.Domain;
import ids.framework.OptimalConstraints;
import ids.utils.CommonUtils;

public class FindOptimalConstraints {
	
	@Option(name="-inum", usage="Input file name of the numerical data")
	private String numDataFile = "ndata.data";
	
	@Option(name="-icat", usage="Input file name of the Categorical data")
	private String catDataFile = "cdata.data";
		
	@Option(name="-k", usage="Number of clusters")
	private int k = 2;
	
	@Option(name="-opath", usage="Output path")
	private String outPath = "";
	
	@Option(name="-start_c", usage="Starting number of constraints")
	private int start_c = 5;
	
	@Option(name="-end_c", usage="Ending number of constraints")
	private int end_c = 10;
	
	/**
	 * Finds the optimal number of constraints
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		new FindOptimalConstraints().doMain(args);
	}
	
	public void doMain(String args[]) throws IOException {
		CmdLineParser parser = new CmdLineParser(this);
		
		parser.setUsageWidth(80);		
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			// if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
            return;
		}

		// output
		System.out.println("Input file name for domain 1: " + numDataFile);
		System.out.println("Input file name for domain 2: " + catDataFile);
		System.out.println("Number of cluster: " + k);
		System.out.println("Output path: " + outPath);
		System.out.println("Starting number of constraints: " + start_c + ", ending number of constraints: " + end_c);
		
		System.out.print("Would you like to continue <yes|no>: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String answer = "";
		try {
			answer = reader.readLine();
		} catch (IOException e) {
			System.out.println("Cannot read your answer: " + e.getMessage());
			System.exit(1);
		}
		
		// start
		if (answer.equalsIgnoreCase("yes")||answer.equalsIgnoreCase("y")) {
			start();
		} else {
			System.out.println("Exit: Have a nice day :)");
			System.exit(0);
		}
			
	}
	
	private void start() {
		// utilities
		CommonUtils utils = new CommonUtils(false);
		
		// Domain 1
		Domain d1 = new Domain();
		d1.data = utils.readDataFile(numDataFile);
		d1.k = k;
		d1.name = "Numerical part of the data set";
		d1.number_of_iterations = 1;
		d1.distance = Distance.SQEUCLIDEAN;
		
		// Domain 2
		Domain d2 = new Domain();
		d2.data = utils.readDataFile(catDataFile);
		d2.k = k;
		d2.name = "Categorical part of the data set";
		d2.number_of_iterations = 1;
		d2.distance = Distance.MATCH;
				
		OptimalConstraints opt = new OptimalConstraints(d1, d2, null, start_c, end_c, outPath);
		opt.find();
	}

}
