package com.rapidminer.operator.uncertain;

import com.rapidminer.operator.similarity.attributebased.uncertain.AbstractProbabilityDensityFunction;



public abstract class AbstractSampleStrategy {

	protected AbstractProbabilityDensityFunction pdf;
	protected double[] element;
	protected int sampleRate;

	public abstract Double[][] getSamples();
	
	public void setValue(double[] ds) {
		this.element = ds;
		pdf.setValue(element);
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

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}
}
