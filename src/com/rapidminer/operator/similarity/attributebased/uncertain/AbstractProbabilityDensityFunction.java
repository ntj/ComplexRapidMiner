package com.rapidminer.operator.similarity.attributebased.uncertain;

import com.rapidminer.tools.Ontology;



/**
 * Abstract class of an object encapsulating a Probability Density Function (pdf).
 * 
 * @author Michael Huber
 */
public abstract class AbstractProbabilityDensityFunction implements ProbabilityDensityFunction{
	/**
	 * represents a data point in multidimensional space
	 */
	protected double[] value;
	protected double uncertainty;
	/**
	 * determines if uncertainty is absolut or relative
	 */
	protected boolean absoluteError;
	
	
	public AbstractProbabilityDensityFunction(double value[], double uncertainty,boolean absoluteError) {
		this.value = value;
		this.uncertainty = uncertainty;
		this.absoluteError = absoluteError;
	}
	
	public AbstractProbabilityDensityFunction(double uncertainty,boolean absoluteError) {
		this.value = null;
		this.uncertainty = uncertainty;
		this.absoluteError = absoluteError;
	}

	
	public void setUncertainty(double uncertainty) {
		this.uncertainty = uncertainty;
	}

	/**
	 * 
	 * @return returns the measurement point for the uncertainty
	 */
	public double[] getValue() {
		return value;
	}

	/**
	 * Sets the measurement center for the uncertainty
	 * @param value
	 */
	public void setValue(double value[]) {
		this.value = value;
	}
	
	
	/**
	 * Returns the uncertainty parameters for the PDF
	 * @return
	 */
	public double getUncertainty() {
		return uncertainty;
	}
	
	
	public int getValueType() {
		return Ontology.UNCERTAIN;
	}

	
	public double getDoubleValue(){
		double sum =0;
		for(int i=0;i<value.length;i++){
			sum += value[i];
		}
		return sum/value.length;
	}
	
}
