package com.rapidminer.operator.similarity.attributebased.uncertain;

import de.tud.inf.example.set.attributevalues.ComplexValue;
/**
 * this interface defines methods for all complex values, i.e PDFs, Matrix values etc 
 * @author Antje Gruner
 *
 */
public interface ProbabilityDensityFunction extends ComplexValue{

	public void setValue(double[] element);
	
	public double getMaxValue(int id);
	
	public double getMinValue(int id);
	
	public double[] getValue();
	
	public double getUncertainty();
	
	public boolean isPointInPDF(Double[] val);
	
	public double[] getRandomValue();
}
