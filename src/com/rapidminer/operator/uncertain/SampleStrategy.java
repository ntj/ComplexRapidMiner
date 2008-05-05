package com.rapidminer.operator.uncertain;

import com.rapidminer.operator.similarity.attributebased.AbstractProbabilityDensityFunction;



public abstract class SampleStrategy {

	protected AbstractProbabilityDensityFunction pdf;
	protected double[] element;
	protected int sampleRate;

	public abstract Double[][] getSamples();
	
	//public abstract double[] getSamplesFromValue(double value);
	
	
	public void setElement(double[] element) {
		this.element = element;
	}
	
	public double[] getElement() {
		return element;
	}

	public int getSampleRate() {
		return sampleRate;
	}
	
	public AbstractProbabilityDensityFunction getPdf() {
		return pdf;
	}

	public void setPdf(AbstractProbabilityDensityFunction pdf) {
		this.pdf = pdf;
	}
}
