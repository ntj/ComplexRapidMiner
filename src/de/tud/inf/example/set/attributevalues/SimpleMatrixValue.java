package de.tud.inf.example.set.attributevalues;


/**
 * manages a simple matrix
 * @author Antje Gruner
 *
 */
public class SimpleMatrixValue extends AbstractMatrixValue{


	
	SimpleMatrixValue(int nrRows, int nrColumns){
		super(nrRows, nrColumns);
	}

	//TODO: integrate into setValues(double[][] values)
	public void setValues(double[] array){
		int x,y;
		int nrRows = m.getRowDimension();
		int nrColumns = m.getColumnDimension();
		int size = Math.min(array.length,nrRows*nrColumns); 
		for(int i=0;i< size;i++){
			x = i/nrColumns;
			y=  i%nrColumns;
			m.set(x, y, array[i]);
		}
	}

	
	public void setValues(double[][] values) {
		if(values != null){
			double[] mValues = new double[values.length];
			for(int i =0;i<values.length;i++){
				mValues[i] = values[i][0];
			}
			setValues(mValues);
		}
	}

	
}
