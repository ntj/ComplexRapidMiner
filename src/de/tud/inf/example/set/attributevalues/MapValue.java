package de.tud.inf.example.set.attributevalues;

import com.rapidminer.tools.Ontology;

public class MapValue implements ComplexValue{

	private double[] spacing = new double[2];
	private double[] origin  = new double[2];
	/**
	 * nr of entries within dimension
	 */
	private int[] extent  = new int[2];
	private double[] zValues; 
	
	public MapValue(){}

	public double getDoubleValue() {
		// TODO Auto-generated method stub
		return 0;
	}
	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		return "NA";
	}

	public int getValueType() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.MAP;
	}
	
	public double getValueAt(double x, double y){
		//TODO test
		int ix =  (int) ((x - origin[0])/spacing[0]);
		int iy =  (int) ((y - origin[1])/spacing[1]);
		return zValues[iy*extent[0] +  ix];
	}
	/**
	 * 
	 * @param z values of f(x,y)
	 * @param o origin
	 * @param s spacing
	 * @param e extent
	 */
	public void setValues(double[] z , double[] o, double[] s, int[] e){
		spacing[0] = s[0];
		spacing[1] = s[1];
		origin[0]  = o[0];
		origin[1]  = o[1];
		extent[0]  = e[0];
		extent[1]  = e[1];
		//create new map array
		zValues = new double[extent[0]*extent[1]];
		
		int min = Math.min(z.length, zValues.length);
		//set values
		for (int i =0;i<min;i++)
			zValues[i] = z[i];
	}
	

}
