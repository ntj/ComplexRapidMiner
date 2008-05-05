package com.rapidminer.operator.uncertain;

import com.rapidminer.operator.similarity.attributebased.AbstractProbabilityDensityFunction;

/**
 * Provides a set of five samples for a 2-dimensional point. The
 * point the samples are requested for and four further points
 * that represent the extreme values given by the global
 * fuzziness.
 *            *-------*
 *            |   *   |
 *            *-------*
 *
 * @author Michael Huber
 * 
 */
public class SimpleSampling extends SampleStrategy {

	public SimpleSampling(double[] element, AbstractProbabilityDensityFunction pdf) {
		this.element = element;
		this.pdf = pdf;
		this.sampleRate = 5;
	}

	public SimpleSampling() {
		this.element = null;
		this.sampleRate = 5;
	}


	/**
	 * Generates a set of samples. The first index of the
	 * 2-dimensional array of double represents the dimension, the
	 * second is the index of the respective samples.
	 * 
	 * @return matrix where the first index represents the dimension,
	 * the second the index of the respective samples
	 */
	public Double[][] getSamples() {
		Double[][] samples = new Double[element.length][]; //XXX: Here Be Dragons: es wird von 5 Samples ausgegangen
		pdf.setValue(element[0]);
		Double[] tmpSamples0 = {pdf.getMinValue(), pdf.getMaxValue(), pdf.getMinValue(), pdf.getMaxValue(), pdf.getValue()};
		pdf.setValue(element[1]);
		Double[] tmpSamples1 = {pdf.getMinValue(), pdf.getMaxValue(), pdf.getMaxValue(), pdf.getMinValue(), pdf.getValue()};
		samples[0] = tmpSamples0;
		samples[1] = tmpSamples1;
		
		return samples;
	}

//	public double[] getSamplesFromValue(double value) {
//		double[] samples = {pdf.getMinValue(), pdf.getMaxValue()};
//		return samples;
//	}
	
	

}
