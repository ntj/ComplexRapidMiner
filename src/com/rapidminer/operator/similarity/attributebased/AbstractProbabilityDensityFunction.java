package com.rapidminer.operator.similarity.attributebased;

/**
 * Abstract class of an object encapsulating a Probability Density Function (pdf).
 * 
 * @author Michael Huber
 */
public abstract class AbstractProbabilityDensityFunction {
	
	private  double value;
	
	public abstract double getValueAt(double x);
	
	public abstract double getMinValue();
	
	public abstract double getMaxValue();
	
	public abstract void setValue(double value);
	
	public abstract double getValue();
}
