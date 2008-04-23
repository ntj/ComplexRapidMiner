package com.rapidminer.operator.learner.clustering.clusterer;

import com.rapidminer.operator.similarity.attributebased.AbstractProbabilityDensityFunction;



public abstract class SampleStrategy {

	protected AbstractProbabilityDensityFunction pdf;

	public abstract double[][] getSamples();
	
	//public abstract double[] getSamplesFromValue(double value);
	
	public abstract double[] getElement();

	public abstract void setElement(double[] element);

	public abstract int getSampleRate();

	public AbstractProbabilityDensityFunction getPdf() {
		return pdf;
	}

	public void setPdf(AbstractProbabilityDensityFunction pdf) {
		this.pdf = pdf;
	}
}
