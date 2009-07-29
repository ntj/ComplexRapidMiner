package de.tud.inf.example.set.attributevalues;

import com.rapidminer.tools.Ontology;

/**
 * arrayValue, where dimensions are defined once
 * @author Antje Gruner
 *
 */
public class ConstantArrayValue implements ComplexValue{

	//dimension of array
	private int[] dim = new int[2];
	private double[][] values;
	
	
	
	public ConstantArrayValue(int d1, int d2) {
		dim[0] = d1;
		dim[1] = d2;
		values = new double[dim[0]][dim[1]];
	}

	public double getDoubleValue() {
		return 0;
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		String str = "[";
		//first row and first entry of row
		str += "{" + values[0][0];
		//iterate through columns
		for(int i=1;i<dim[1];i++){
			str += ", " + values[0][i];
		}
		str += "}";
		//iterate through rows of map
		for(int i=0;i<values.length;i++){
			str += " ,{";
			for(int j=0;j<values[i].length;j++)
				str += values[i*dim[1]+j][0];
			str += "}";
		}
		str += "]";
		return str;
	}

	public int getValueType() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.ARRAY;
	}
	
	public void setValues(double[][] val) {
		for(int i=0;i<values.length;i++){
			
		}
		this.values = val;
	}
	
	
	
}
