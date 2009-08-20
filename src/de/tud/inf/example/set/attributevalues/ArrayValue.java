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
	
	public ArrayValue(){}
	
	
	public double getDoubleValue() {
		// TODO Auto-generated method stub
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
		return Ontology.ARRAY;
	}
	
	public void setValues(int d1, int d2, double[][] val){
		this.dim[0] = d1;
		this.dim[1] = d2;
		values = new double[dim[0]][dim[1]];
		for(int i=0;i<values.length;i++)
			for(int j=0;j<values[i].length;j++)
				values[i][j] = val[i*dim[1]+j][0];
	}
	

}
	


