package com.rapidminer.operator.similarity.attributebased;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
	
	public SimpleProbabilityDensityFunction(double uncertainty,
			boolean absoluteError) {
		super(uncertainty, absoluteError);

	}

	public SimpleProbabilityDensityFunction(Double[] value, double uncertainty,
			boolean absoluteError) {
		super(value, uncertainty, absoluteError);
	}

	public double getMinValue(int dimension) {
		if(absoluteError){
			return value[dimension] - uncertainty;
		}
		return value[dimension] - uncertainty*value[dimension];
	}
	
	public double getMaxValue(int dimension) {
		if(absoluteError){
			return value[dimension] + uncertainty;
		}
		return value[dimension] + uncertainty*value[dimension];
	}

	public Double[] getValue() {
		return value;
	}

	public void setValue(Double value[]) {
		this.value = value;
	}

	public double getUncertainty() {
		return uncertainty;
	}

	public void setUncertainty(double uncertainty) {
		this.uncertainty = uncertainty;
	}

	@Override
	public double getValueAt(int x) {
		throw new NotImplementedException();
	}
}
