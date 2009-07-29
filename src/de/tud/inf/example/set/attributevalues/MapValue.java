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

	private double[] spacing = new double[2];
	private double[] origin = new double[2];
	private NominalMapping nm;
	/**
	 * nr of entries within dimension
	 */
	private int[] dimension = new int[2];
	private double[] zValues;

	
	public MapValue() {
	}
	
	public MapValue(double[] spacing, double[] origin, int[] dimension,
			double[] values) {
		this.spacing = spacing;
		this.origin = origin;
		this.dimension = dimension;
		zValues = new double[dimension[0] * dimension[1]];
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
		String str = "";
		if (nm == null) {
			str += "[";
			// first row and first entry of row
			str += "{" + zValues[0];
			// iterate through columns
			for (int i = 1; i < dimension[0]; i++) {
				str += ", " + zValues[i];
			}
			str += "}";
			// iterate through rows of map
			for (int j = 1; j < dimension[1]; j++) {
				// first entry of each row
				str += ", {" + zValues[j * dimension[0]];
				// iterate through columns
				for (int i = 1; i < dimension[0]; i++) {
					str += ", " + zValues[j * dimension[0] + i];
				}
				str += "}";
			}
			str += "]";
		} else {
			str += "[";
			// first row and first entry of row
			str += "{" + nm.mapIndex((int) zValues[0]);
			// iterate through columns
			for (int i = 1; i < dimension[0]; i++) {
				str += ", " + nm.mapIndex((int) zValues[i]);
			}
			str += "}";
			// iterate through rows of map
			for (int j = 1; j < dimension[1]; j++) {
				// first entry of each row
				str += ", {" + nm.mapIndex((int) zValues[j * dimension[0]]);
				// iterate through columns
				for (int i = 1; i < dimension[0]; i++) {
					str += ", "
							+ nm.mapIndex((int) zValues[j * dimension[0] + i]);
				}
				str += "}";
			}
			str += "]";
		}
		return str;
	}

	public int getValueType() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.MAP;
	}

	public double getValueAt(double x, double y) {
		// TODO test
		int ix = (int) ((x - origin[0]) / spacing[0]);
		int iy = (int) ((y - origin[1]) / spacing[1]);
		return zValues[iy * dimension[0] + ix];
	}

	public String getStringValueAt(double x, double y) {
		// TODO test
		int ix = (int) ((x - origin[0]) / spacing[0]);
		int iy = (int) ((y - origin[1]) / spacing[1]);
		return nm.mapIndex((int) zValues[iy * dimension[0] + ix]);
	}

	/**
	 * 
	 * @param z
	 *            values of f(x,y)
	 * @param o
	 *            origin
	 * @param s
	 *            spacing
	 * @param e
	 *            extent
	 */
	public void setValues(double[] z, double[] o, double[] s, int[] e,
			NominalMapping nm) {
		spacing[0] = s[0];
		spacing[1] = s[1];
		origin[0] = o[0];
		origin[1] = o[1];
		dimension[0] = e[0];
		dimension[1] = e[1];
		// create new map array
		zValues = new double[dimension[0] * dimension[1]];
		for (int i = 0; i < zValues.length; i++) {
			zValues[i] = 0;
		}
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

}
