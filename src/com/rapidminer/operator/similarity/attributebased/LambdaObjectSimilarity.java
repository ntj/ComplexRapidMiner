package com.rapidminer.operator.similarity.attributebased;

/**
 * Lambda distance.
 * 
 * @author Michael Wurst
 * @see com.rapidminer.operator.learner.clustering.clusterer.uncertain.DBScanEAClustering
 */
public class LambdaObjectSimilarity extends AbstractRealValueBasedSimilarity {

	//private static final long serialVersionUID = -8688112978579558373L;

	private double lambda;
	public LambdaObjectSimilarity(double lambda) {
		super();
		this.lambda = lambda;
	}
	
	public double similarity(double[] e1, double[] e2) {
		double sum = 0.0;
		int counter = 0;
		for (int i = 0; i < e1.length; i++) {
			if ((!Double.isNaN(e1[i])) && (!Double.isNaN(e2[i]))) {
				sum = sum + (e1[i] - e2[i]) * (e1[i] - e2[i]);
				counter++;
			}
		}
		double d = Math.sqrt(sum);
		if (counter > 0)
			return d;
		else
			return Double.NaN;
	}

	public boolean isDistance() {
		return true;
	}
}
