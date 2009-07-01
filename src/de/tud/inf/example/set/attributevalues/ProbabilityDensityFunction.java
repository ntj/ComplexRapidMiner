package de.tud.inf.example.set.attributevalues;

public interface ProbabilityDensityFunction extends ComplexValue{

	public double getProbabilityAt(int x);
	
	public double getProbabilityFor(double[] position);
	
	public double getMinValue(int dimension);
	
	public double getMaxValue(int dimension);

	public boolean isPointInPDF(Double[] tempVal) ;
	
	public double[] getRandomValue();

	
	/**
	 * 
	 * @return returns the measurement point for the uncertainty
	 */
	public double[] getValue();
	

	/**
	 * Sets the measurement center for the uncertainty
	 * @param value
	 */
	public void setValue(double value[]);
	

	/**
	 * Returns the uncertainty parameters for the PDF
	 * @return
	 */
	public double getUncertainty();	
}
