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
		StringBuffer build = new StringBuffer();
		for (int i=0; i< values.length;i++){
			build.append("|");
			for (int j=0; j < values[i].length;j++){
				build.append(values[i][j]);
				build.append(" ");
			}
		}
		return build.toString();
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
