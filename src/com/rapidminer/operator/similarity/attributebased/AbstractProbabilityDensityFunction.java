package com.rapidminer.operator.similarity.attributebased;

/**
 * Abstract class of an object encapsulating a Probability Density Function (pdf).
 * 
 * @author Michael Huber
 */
public abstract class AbstractProbabilityDensityFunction {
	
	
	
	
	
	protected Double value[];
	protected double uncertainty;
	protected boolean absoluteError;
	
	
	public AbstractProbabilityDensityFunction(Double value[], double uncertainty,boolean absoluteError) {
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
	
	public abstract void setValue(Double value[]);
	
	public abstract Double[] getValue();
	
}
