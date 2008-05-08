package com.rapidminer.operator.uncertain;

import com.rapidminer.operator.similarity.attributebased.uncertain.AbstractProbabilityDensityFunction;

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
public class SimpleSampling extends AbstractSampleStrategy {

	public SimpleSampling(double[] element, AbstractProbabilityDensityFunction pdf) {
		this.element = element;
		this.pdf = pdf;
		
	}

	public SimpleSampling() {
		this.element = null;
		
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
		if(pdf != null){
			Double[][] samples = new Double[sampleRate][];
			Double newVal[] = new Double[element.length];
			//here do the following. i is a bivector of the length of the |dim|. each bit represents the 
			for(int i = 0;i<Math.pow(2,element.length);i++){
				newVal = new Double[element.length];
				for(int j=1;j<Math.pow(2,element.length);j=j*2){
					if((j&i)>0){
						newVal[j]=pdf.getMaxValue(j/2);
					}else{
						newVal[j]=pdf.getMinValue(j/2);
					}
					
				}
				samples[i] = newVal;
			}
			
			return samples;
		}
		throw new NullPointerException();
	}

}
