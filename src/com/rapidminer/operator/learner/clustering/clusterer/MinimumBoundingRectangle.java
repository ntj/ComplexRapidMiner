package com.rapidminer.operator.learner.clustering.clusterer;

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
	
/*	
	public double minDistance(MinimumBoundingRectangle otherElement) {
		//XXX: Die Frage hierbei ist, ob man das nicht gleich auslagert!
	}
*/
}