package de.tud.inf.example.set.attributevalues;

import java.io.BufferedReader;

import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.tools.Ontology;

/**
 * this complex value encapsulates a map z = f(x,y), x and y are discrete and
 * equidistant point coordinates, z can be arbitrary
 * 
 * @author Antje Gruner
 * 
 */
public class MapValue implements ComplexValue {

	/**
	 * step size in x and y direction
	 */
	private double[] spacing = new double[2];
	
	private String str = "";
	
	
	/**
	 * minimal value of x and y
	 */
	private double[] origin = new double[2];
	
	/**
	 * number of entries within dimension
	 */
	private int[] dimension = new int[2];
	
	/**
	 * features z = f(x,y) of this map
	 */
	private double[] zValues;

	
	public MapValue() {
	}
	
	public MapValue(double[] spacing, double[] origin, int[] dimension, double[] values) {
		this.spacing = spacing;
		this.origin = origin;
		this.dimension = dimension;
		zValues =  new double[dimension[0] * dimension[1]];
		int min = Math.min(values.length, zValues.length);
		// set values
		for (int i = 0; i < min; i++)
			zValues[i] = values[i];
	}


	public double getDoubleValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		/*if (str.equals("")){
			StringBuffer buf = new StringBuffer();
			buf.append("[");
			// first row and first entry of row
			buf.append("{");
			buf.append(zValues[0]);
			// iterate through columns
			for (int i = 1; i < dimension[1]; i++) {
				buf.append(", ");
				buf.append(zValues[i]);
			}
			buf.append("}");
			// iterate through rows of map
			for (int x = 1; x < dimension[0]; x++) {
				// first entry of each row
				buf.append(", {");
				buf.append(zValues[x * dimension[1]]);

				// iterate through columns
				for (int y = 1; y < dimension[1]; y++) {
					buf.append(", ");
					buf.append(zValues[x * dimension[1] + y]);
				}
				buf.append("}");
			}
			buf.append("]");
			str = buf.toString();
		}
		return str;
		*/
		//if (str.equals("")){
		StringBuffer buf = new StringBuffer();
		for (int i=0; i< zValues.length;i++){
			buf.append(zValues[i]);
			buf.append(" ");
			if(i % this.dimension[1] == (dimension[1])-1)
				buf.append("| ");
		}
		str = buf.toString();		
		return str;
	}

	public int getValueType() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.MAP;
	}

	public double getValueAt(double x, double y) {
		// TODO test
		int ix = (int) ((x - origin[0]) / spacing[0]);
		int iy = (int) ((y - origin[1]) / spacing[1]);
		return zValues[ix * dimension[1] + iy];
	}
	

	public String getStringValueAt(double x, double y) {
		// TODO test
		//int ix = (int) ((x - origin[0]) / spacing[0]);
		//int iy = (int) ((y - origin[1]) / spacing[1]);
		//return nm.mapIndex((int) zValues[ix * dimension[1] + iy]);
		return "";
	}

	/**
	 * 
	 * @param z
	 *            values of f(x,y)
	 * @param o
	 *            origin
	 * @param s
	 *            spacing
	 * @param e   dimension vector (nr entries)
	 */
	public void setValues(double[] z, double[] o, double[] s, int[] e) {
		spacing[0] = s[0];
		spacing[1] = s[1];
		origin[0] = o[0];
		origin[1] = o[1];
		dimension[0] = e[0];
		dimension[1] = e[1];
		// create new map array
		zValues = new double[dimension[0] * dimension[1]];
		
		int min = Math.min(z.length, zValues.length);
		// set values
		for (int i = 0; i < min; i++)
			zValues[i] = z[i];
	}

	public double[] getSpacing() {
		return spacing;
	}

	public double[] getOrigin() {
		return origin;
	}

	public int[] getDimension() {
		return dimension;
	}

	public double[] getZValues() {
		return zValues;
	}
	
	public int getMapSize(){
		return zValues.length;
	}

}
