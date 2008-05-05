package com.rapidminer.operator.similarity.attributebased;

/**
 * A simple Implementation of a Probability Density Function (pdf) that
 * takes just the value and the global fuzziness as parameters.
 * 
 * @author Michael Huber
 * @see com.rapidminer.operator.learner.clustering.clusterer.uncertain.DBScanEAClustering
 * @see com.rapidminer.operator.learner.clustering.clusterer.uncertain.FDBScanClustering
 */
public class SimpleProbabilityDensityFunction extends
		AbstractProbabilityDensityFunction {

	private double fuzziness;

	//unprecise value
	private double value;
	
	public SimpleProbabilityDensityFunction(double value, double fuzziness) {
		this.value = value;
		this.fuzziness = fuzziness;
	}
	
	public SimpleProbabilityDensityFunction(double fuzziness) {
		this.value = 0;
		this.fuzziness = fuzziness;
	}
	
	public double getValueAt(double x) {
		if(x > getMinValue() || x < getMaxValue()) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public double getMinValue() {
		return value - fuzziness;
	}
	
	public double getMaxValue() {
		return value + fuzziness;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getFuzziness() {
		return fuzziness;
	}

	public void setFuzziness(double fuzziness) {
		this.fuzziness = fuzziness;
	}
}
