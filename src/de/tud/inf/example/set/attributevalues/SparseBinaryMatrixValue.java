package de.tud.inf.example.set.attributevalues;


public class SparseBinaryMatrixValue extends SparseMatrixValue{

	public SparseBinaryMatrixValue(int nrRows, int nrColumns) {
		super(nrRows, nrColumns);
	}

	private boolean set(int key) {
		return super.set(key, 1);
	}

	@Override
	public void setValues(double[][] values) {
		//assume that all inner lists have one entry
		for(int i=0;i<values.length;i++){
			this.set((int)values[i][0]);
		}
	}
	

}
