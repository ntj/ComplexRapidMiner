package de.tud.inf.example.set.attributevalues;

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

	private NominalMapping nm; 
	
	public MapValue() {
	}
	
	public MapValue(double[] spacing, double[] origin, int[] dimension, double[] values, NominalMapping nm) {
		this.spacing = spacing;
		this.origin = origin;
		this.dimension = dimension;
		zValues =  new double[dimension[0] * dimension[1]];
		int min = Math.min(values.length, zValues.length);
		// set values
		for (int i = 0; i < min; i++)
			zValues[i] = values[i];
		this.nm = nm;
	}
	


	public double getDoubleValue() {
		return 0;
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		StringBuffer buf = new StringBuffer();
		if(nm == null){
			for (int i=0; i< zValues.length;i++){
				buf.append(zValues[i]);
				buf.append(" ");
				if(i % dimension[1] == (dimension[1])-1)
					buf.append("| ");
			}
		}
		else{
			for (int i=0; i< zValues.length;i++){
				buf.append(nm.mapIndex((int)zValues[i]));
				buf.append(" ");
				if(i % dimension[1] == (dimension[1])-1)
					buf.append("| ");
			}
		}
		return buf.toString();		
	}

	public int getValueType() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.MAP;
	}

	public double getValueAt(double x, double y) {
		int ix = (int) ((x - origin[0]) / spacing[0]);
		int iy = (int) ((y - origin[1]) / spacing[1]);
		return zValues[ix * dimension[1] + iy];
	}
	

	/**
	 * maps internal double values to String, call this method iff there is definitely an nominal mapping in map value
	 * @param x
	 * @param y
	 * @return
	 */
	public String getStringValueAt(double x, double y) {
		int ix = (int) ((x - origin[0]) / spacing[0]);
		int iy = (int) ((y - origin[1]) / spacing[1]);
		return nm.mapIndex((int) zValues[ix * dimension[1] + iy]);
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
	public void setValues(double[] z, double[] o, double[] s, int[] e, NominalMapping nm) {
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
		this.nm = nm;
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
