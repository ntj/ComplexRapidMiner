package com.rapidminer.operator.uncertain;

/**
 * 
 * This method creates a minimum bounding box over a set of samples
 * 
 * @author Peter Volk
 *
 */
public class MinimumBoundingRectangle {

	//double[d]: d ist die Dimension
	private double[] minDimension;
	
	private double[] maxDimension;
	
	private int dimension;
	
	//double[d][s]: d ist die Dimension und s sind die Samples
	public MinimumBoundingRectangle(double[][] sample) {
		this.dimension = sample.length;
		for(int i=0; i<dimension; i++) {
			minDimension[i] = sample[i][0];
			maxDimension[i] = sample[i][0];
			for(int j=1; j<sample[i].length; j++) {
				if(minDimension[i] > sample[i][j])
					minDimension[i] = sample[i][j];
				if(maxDimension[i] < sample[i][j])
					maxDimension[i] = sample[i][j];
			}
		}
	}
	
	public double getMinimumValue(int dimension) {
		return minDimension[dimension];		
	}
	
	public double getMaximumValue(int dimension) {
		return maxDimension[dimension];		
	}
	
	public int getDimension() {
		return dimension;		
	}
	
}