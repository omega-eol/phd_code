package ids.clustering.model;

import java.io.Serializable;

/**
 * The View class is very similar to the Domain class but less fields related to
 * IDS framework
 * @author Artur.Abdullin
 *
 */
@SuppressWarnings("serial")
public class View implements Serializable {

	/**
	 * Input data set
	 */
	public double[][] data;
	
	/**
	 * 
	 */
	public Distance distance;
	
	/**
	 * Pre-computed distance of the input data matrix
	 */
	public double[] pd;
	
	/**
	 * The maximum distance in the data set
	 */
	public double pd_max = -1.0;
	
	/**
	 * Default constructor
	 */
	public View() {};
	
	/** Common constructor
	 * 
	 */
	public View(double[][] input_data, Distance distanceMeasure) {
		this.data = input_data;
		this.distance = distanceMeasure;
	}
}
