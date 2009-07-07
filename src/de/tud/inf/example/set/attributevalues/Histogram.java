package de.tud.inf.example.set.attributevalues;

import com.rapidminer.operator.similarity.attributebased.uncertain.AbstractProbabilityDensityFunction;

/**
 * simple implementation of an n dimensional histogram pdf
 * n-dimensional point value is expected, but actually it is not necessary,
 * therefore actually a test, whether point is in histogram is required, but not implemented 
 * @author Antje Gruner
 *
 */
public class Histogram extends AbstractProbabilityDensityFunction{

	/**
	 * stores minMax values for each dimension, number of rows = dimension, columns = 2
	 */
	private MatrixValue minMax;
	
	
	/**
	 * stores probability entries, dimension = number of correlating attributes
	 */
	private TensorValue probabilities;
	
	
	public Histogram(int dim,int nrInterval,boolean isSparse) {
		super(0,false);
		minMax = new SimpleMatrixValue(dim,2);
		probabilities = new TensorValue(dim,nrInterval,isSparse);
	}

	
	public double getMaxValue(int dimension) {
		return minMax.getValueAt(dimension,1);
	}
	
	public double getMinValue(int dimension) {
		return minMax.getValueAt(dimension,0);
	}
	
	public double getProbabilityAt(int x) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
		
	}
	
	public double getProbabilityFor(double[] position) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
		
	}
	
	
	public boolean isPointInPDF(Double[] tempVal) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();	
	}
	
	public void setMinMax(double[][] minMaxVals){
		minMax.setValues(minMaxVals);
	}
	
	public void setProbabilityValues(double[][] probValues){
		probabilities.setValues(probValues);
	}



	public double[] getRandomValue() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		return "NA";
	}




}
