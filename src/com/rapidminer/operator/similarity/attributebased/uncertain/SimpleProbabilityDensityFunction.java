package com.rapidminer.operator.similarity.attributebased.uncertain;


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

	public SimpleProbabilityDensityFunction(double[] value, double uncertainty,
			boolean absoluteError) {
		super(value, uncertainty, absoluteError);
	}

	public double getMinValue(int dimension) {
		if(dimension>=value.length){
			throw new NumberFormatException("number of dimensions in the example set lower than requested dimension ("+dimension+"/"+value.length+")");
		}
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

	@Override
	public double getProbabilityAt(int x) {
		throw new NotImplementedException();
	}

	@Override
	public boolean isPointInPDF(Double[] value) {
		//check all dimensions to see if it violates the extrema values
		for(int i = 0;i<value.length ;i++){
			if(value[i]> this.getMaxValue(i) || value[i]<this.getMinValue(i)){
				return false;
			}
		}
		return true;
	}

	@Override
	public double getProbabilityFor(double[] position) {
		//calc complete volume of the object
		double volume = 1.0;
		for(int i = 0;i<this.value.length;i++){
			volume = volume*(getMaxValue(i)-getMinValue(i));
		}
		return 1.0/volume;
	}
}
