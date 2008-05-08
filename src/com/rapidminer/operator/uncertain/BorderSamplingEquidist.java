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
 * @author Peter B. Volk
 * 
 */
public class BorderSamplingEquidist extends AbstractSampleStrategy {

	public BorderSamplingEquidist(double[] element, AbstractProbabilityDensityFunction pdf) {
		this.element = element;
		this.pdf = pdf;
		pdf.setValue(element);
		
	}

	public BorderSamplingEquidist() {
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
			for(int i = 0;i<sampleRate;i++){
				newVal = new Double[element.length];
				for(int j=1;j<Math.pow(2,element.length);j=j*2){
					if((j&i)>0){
						newVal[new Double((Math.log(j)/Math.log(2))).intValue()]=pdf.getMaxValue(new Double((Math.log(j)/Math.log(2))).intValue());
					}else{
						newVal[new Double((Math.log(j)/Math.log(2))).intValue()]=pdf.getMinValue(new Double((Math.log(j)/Math.log(2))).intValue());
					}
					
				}
				samples[i] = newVal;
			}
			
			return samples;
		}
		throw new NullPointerException();
	}

}
