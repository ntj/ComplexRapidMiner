package com.rapidminer.operator.similarity.attributebased.uncertain;

import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.attributevalues.MatrixValue;
import de.tud.inf.example.set.attributevalues.SimpleMatrixValue;

/**
 * 
 * @author Antje Gruner
 *
 */
public class GaussProbabilityDensityFunction extends AbstractProbabilityDensityFunction{

	private MatrixValue variance;
	
	
	//TODO: think about constructors again
	public GaussProbabilityDensityFunction(SimpleMatrixValue variance){
		super(0,false);
		this.variance = variance;
	}
	
	public GaussProbabilityDensityFunction(
			double[] m, 
			SimpleMatrixValue variance,
			double uncertainty,
			boolean absoluteError) {
		super(m,uncertainty, absoluteError);
		this.variance = variance;
	}
	
	
	public double getMaxValue(int dimension) {
		//assume that value range = 0.9545, i.e. 95% of the points should lie in the interval -> interval: 2*sigma around the mean
		return value[dimension] + 2 * variance.getValueAt(dimension, dimension);
	}

	
	public double getMinValue(int dimension) {
		//assume that range = 0.9545 -> intervall: 2*sigma around the mean
		return value[dimension] - 2 * variance.getValueAt(dimension, dimension);
	}

	
	public double getProbabilityAt(int x) {
		return 0;
	}

	
	public double getProbabilityFor(double[] position) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public boolean isPointInPDF(Double[] tempVal) {
		return false;
	}
	
	public int getValueType() {
		return Ontology.GAUSS;
	}

	
	public void setCovarianceMatrix(double[][] values){
		variance.setValues(values);
	}
	
	public MatrixValue getCovarianceMatrix(){
		return variance;
	}
	

	public double[] getRandomValue() {
		throw new UnsupportedOperationException();
	}
}
