package com.rapidminer.operator.uncertain;

import java.util.Random;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 * @author Peter B. Volk
 * 
 * Generate a number of 5 samples to every value.
 * 
 *
 */
public class MonteCarloSampling extends AbstractSampleStrategy {


	public Double[][] getSamples() {
		Double[][] ret = new Double[this.sampleRate][];
		
		Random r = new Random();
		//get the random values an add them to the point list
		for (int i = 0 ;i<this.sampleRate;){
			Double tempVal[] = new Double[element.length];
			
			for(int j = 0;j<element.length;j++){
				Double d = r.nextDouble();
				double min = pdf.getMaxValue(j);
				double max = pdf.getMaxValue(j);
				double diff = max-min;
				d = d-0.5;
				diff = (diff/2.0)*d;
				tempVal[j]= this.element[j]+diff;
			}
			if(pdf.isPointInPDF(tempVal)){
				ret[i] = tempVal;
				i++;
			}
		}
		return ret;
	}

	public double[] getSamplesFromValue(double value) {
		throw new NotImplementedException();
	}
}
