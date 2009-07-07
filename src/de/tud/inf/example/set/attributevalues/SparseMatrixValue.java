package de.tud.inf.example.set.attributevalues;

import de.tud.inf.example.ComplexAttributeInstantiationException;

/**
 * creates a matrix from key/value pairs, i.e. entries which are not set remain zero 
 * @author Antje Gruner
 *
 */
public class SparseMatrixValue extends AbstractMatrixValue{

	public SparseMatrixValue(int nrRows, int nrColumns) {
		super(nrRows, nrColumns);
	}
	
	protected boolean set(int key, double value){
		int nrRows = m.getRowDimension();
		int nrColumns = m.getColumnDimension();
		if((key>0)&&(key < nrRows*nrColumns)){
			m.set(key/nrColumns, key%nrColumns, value);
			return true; 
		}
		return false;
	}
	

	
	public void setValues(double[][] values) {
		//assume that all inner lists have two entries
		for(int i=0;i<values.length;i++){
				this.set((int)values[i][0],values[i][1]);
		}
	}

	
	
	
	

}
