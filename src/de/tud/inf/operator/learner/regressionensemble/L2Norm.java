package de.tud.inf.operator.learner.regressionensemble;

public class L2Norm implements Distance {

	public double distance(double d1, double d2) {
		return Math.sqrt((d1 - d2) * (d1 - d2));
	}
}
