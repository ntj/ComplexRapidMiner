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

	private double uncertainty;

	//unprecise value
	private double value;
	
	public SimpleProbabilityDensityFunction(double value, double uncertainty) {
		this.value = value;
		this.uncertainty = uncertainty;
	}
	
	public SimpleProbabilityDensityFunction(double uncertainty) {
		this.value = 0;
		this.uncertainty = uncertainty;
	}
	
	public double getValueAt(double x) {
		if(x > getMinValue() || x < getMaxValue()) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public double getMinValue() {
		return value - uncertainty*value;
	}
	
	public double getMaxValue() {
		return value + uncertainty*value;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getUncertainty() {
		return uncertainty;
	}

	public void setUncertainty(double uncertainty) {
		this.uncertainty = uncertainty;
	}
}
