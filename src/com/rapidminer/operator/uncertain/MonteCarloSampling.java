package com.rapidminer.operator.uncertain;

import java.util.Random;


/**
 * @author Michael Huber
 * 
 * Generate a number of 5 samples to every value.
 * Should be meaningful samples!!!
 *
 */
public class MonteCarloSampling extends AbstractSampleStrategy {


	public Double[][] getSamples() {
		double min = pdf.getMaxValue(1);
		Random r = new Random();
		//get the random values an add them to the point list
		for (int i = 0 ;i<this.sampleRate;i++){
			r.nextInt();
		}
		return null;
	}

	public double[] getSamplesFromValue(double value) {
	
		return null;
	}
	
	public double[] getElement() {
		return null;
	}

	public void setElement(double[] element) {
	}

	public int getSampleRate() {
		return 0;
	}


}
