package de.tud.inf.example.set.attributevalues;

import com.rapidminer.tools.Ontology;
/**
 * d-tensor implementation with the same number of entries for each dimension d
 * 2-tensor: quadratic matrix, 1-tensor: scalar
 * @author Antje Gruner
 *
 */
public class TensorValue implements ComplexValue{

	private int dimension;
	private int nrEntries;
	private double[] values;
	private boolean isSparse = false;
	
	/**
	 * 
	 * @param dim number of dimensions
	 * @param nrEnt number of entries for each dimension
	 */
	public TensorValue(int dim, int nrEnt, boolean isSparse){
		this.dimension = dim;
		this.nrEntries = nrEnt;
		values = new double[(int) Math.pow(nrEntries, dimension)];
		this.isSparse = isSparse;
	}
	
	/**
	 * returns the number of indices of that tensor (i.e. the rank)
	 */
	public int getDimension() {
		return dimension;
	}


	public double getDoubleValue() {
		return 0;
	}

	public int getValueType() {
		return Ontology.TENSOR;
	}
	
	public double getValueAt(int key){
		return values[key];
	}
	
	/**
	 * 
	 * @param key stores indices for each dimension
	 * @return 
	 */
	public Double getValueAt(double[] key){
		if(key.length != this.dimension) return null;
		int realKey = 0;
		for(int i = 0; i<key.length;i++){
			if(key[i] <0 || key[i]>=dimension) return null;
			realKey += key[i]*Math.pow(nrEntries,i);
		}
		return getValueAt(realKey);
	}
	
	public void setValue(int key, double value){
		values[key] = value;
	}
	
	public void setValues(double[] newValues){
		int end = Math.min(newValues.length,values.length);
		for(int i=0;i<end;i++)
			values[i] = newValues[i];	
	}
	
	public void setValues(double[][] newValues){
		if(isSparse){ //sparse tensor -> key value entries -> newValues: double[][2]
			for(int i =0;i<newValues.length;i++)
				values[(int)newValues[i][0]] = newValues[i][1];
		}
		else{ //simple tensor -> newValues: double[][1]
			int end = Math.min(newValues.length,values.length);
			for(int i=0;i<end;i++)
				values[i] = newValues[i][0];
		}
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		return "NA";
	}



}
