package de.tud.inf.example.set.attributevalues;

import Jama.Matrix;

import com.rapidminer.tools.Ontology;

public abstract class AbstractMatrixValue implements ComplexValue, MatrixValue{

	protected Matrix m;
	
	
	public AbstractMatrixValue(int nrRows, int nrColumns){
		m = new Matrix(nrRows,nrColumns);
	}


	public double getDoubleValue(){return 0;}


	
	public Double getValueAt(int x, int y){
		//first entry must be (0,0)
		if((x>=0) && (y>=0) && (x < m.getRowDimension())&&(y < m.getColumnDimension()))
			return m.get(x, y);
		else return null;
	}
	
	public int getValueType() {
		return Ontology.MATRIX;
	}
	
	/**
	 * 
	 * @param cWidth column width
	 * @param nrDig  nr of digits
	 */
	public void print(int cWidth, int nrDig){
		m.print(cWidth, nrDig);
	}
}
