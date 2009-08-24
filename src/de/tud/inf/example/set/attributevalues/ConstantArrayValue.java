package de.tud.inf.example.set.attributevalues;

import com.rapidminer.tools.Ontology;

/**
 * this complex object encapsulates an double[n][m] object, where n and m are set once and cannot be changed
 * @author Antje Gruner
 *
 */
public class ConstantArrayValue implements ComplexValue{

	/**
	 * 
	 */
	private double[][] values;
	
	
	/**
	 * 
	 * @param d1 nr of lists
	 * @param d2 nr of entries in 
	 */
	public ConstantArrayValue(int d1, int d2) {
		values = new double[d1][d2];
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
		return Ontology.ARRAY;
	}
	
	/**
	 * overrides the internal a sequence of values object with a given sequence of values
	 * it is important that the parameter array contains at least n*m values, where n = values.length and m = values[0].length  
	 * @param values
	 */
	public void setValues(double[] vals){
		for (int i=0;i<values.length;i++)
			for (int j=0;j<values[0].length;j++)
				values[i][j] = vals[i*values[j].length + j];
	}
	
	/**
	 * set values instances of the relational attribute which is an inner attribute of ArrayAttribute
	 * @param values one data value of an relationalAttribute with one inner attribute
	 */
	public void setValues(double[][] relationalValues) {
		for(int i=0;i<values.length;i++)
			for(int j=0;j<values[0].length;j++)
				values[i][j] = relationalValues[i*values[0].length + j][0];
	}
	
	
	
	public double[][] getValues() {
		return values;
	}

	
	
	
	
	
	
}
