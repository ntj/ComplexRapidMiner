package de.tud.inf.example.set.attributevalues;

import com.rapidminer.tools.Ontology;
/**
 * 
 * @author Antje Gruner
 *
 */
public class ArrayValue implements ComplexValue{

	//dimension of array
	private int[] dim;
	private double[][] values;
	
	public double getDoubleValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		return "NA";
	}

	public int getValueType() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.ARRAY;
	}
	
	public void setValues(int[] dim, double[] val){
		this.dim[0] = dim[0];
		this.dim[1] = dim[1];
		values = new double[dim[0]][dim[1]];
		for(int i=0;i<values.length;i++)
			for(int j=0;j<values[i].length;j++)
				values[i][j] = val[i*dim[1]+j];
	}
	

}
	


