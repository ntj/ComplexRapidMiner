package de.tud.inf.example.set.attributevalues;

public interface MatrixValue {

	public void print(int cWidth, int nrDig);
	
	public void setValues(double[][] values);
	
	public Double getValueAt(int x, int y);
}
