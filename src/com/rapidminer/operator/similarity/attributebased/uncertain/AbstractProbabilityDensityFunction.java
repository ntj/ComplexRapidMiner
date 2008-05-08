package com.rapidminer.operator.similarity.attributebased.uncertain;

/**
 * Abstract class of an object encapsulating a Probability Density Function (pdf).
 * 
 * @author Michael Huber
 */
public abstract class AbstractProbabilityDensityFunction {
	
	
	
	
	
	protected double[] value;
	protected double uncertainty;
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
	
	
	public abstract double getValueAt(int x);
	
	public abstract double getMinValue(int dimension);
	
	public abstract double getMaxValue(int dimension);

	public abstract boolean isPointInPDF(Double[] tempVal) ;
	
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
	
}
