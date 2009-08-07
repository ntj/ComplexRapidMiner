package de.tud.inf.example.set.attributevalues;

import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.tools.Ontology;

/**
 * this complex value encapsulates a map z = f(x,y), x and y are discrete and
 * equidistant point coordinates, z can be arbitrary
 * z values are stored in y-direction first, i.e zValues = {f(0,0), f(0,1),..., f(0,dimension[1], f(1,0)...)
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
	
	public MapValue(double[] spacing, double[] origin, int[] dimension, double[] values) {
		this(spacing,origin,dimension,values,null);
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

	/**
	 * returns z value according to x and y coordinate values
	 * if the values is not directly given the z value will be interpolated
	 * using a simple bilinear interpolation
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return z value 
	 */
	public double getValueAt(double x, double y) {
		// do x and y lie in the grid?
		if(Math.IEEEremainder(x - origin[0], spacing[0]) == 0.0 && Math.IEEEremainder(y - origin[0], spacing[1]) == 0.0) {
			// -> yes
			int ix = (int) ((x - origin[0]) / spacing[0]);
			int iy = (int) ((y - origin[1]) / spacing[1]);
			return zValues[ix * dimension[1] + iy];
		} else {
			
			// do a bilinear interpolation
			return getInterpolatedValue(x, y);
		}
		
	}
	
	
	/**
	 * returns z value according to x and y index values in z array  
	 * @param x index of x coordinate
	 * @param y index of y coordinate
	 * @return z value 
	 */
	public double getValueAtId(int idX, int idY) {
		return zValues[idX * dimension[1] + idY];
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
	 * @param id position in z array 
	 * @return mapped value
	 */
	public String getStringValueAt(int id) {
		return nm.mapIndex((int)zValues[id]);
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
	
	public double getAverage(){
		double sum =0;
		for(int i =0;i<zValues.length;i++)
			sum += zValues[i];
		return sum/zValues.length;
	}
	
	public double getVariance(){
		 double avg = getAverage();
		 double squaredSum =0;
		 for(int i =0;i<zValues.length;i++)
			 squaredSum += zValues[i]*zValues[i];
		 return squaredSum / zValues.length - (avg * avg);
	}
	
	/**
	 * returns maximum x and y values of map
	 * @return
	 */
	public double[] getExtent(){
		double[] ex = new double[2];
	    ex[0] = origin[0] + spacing[0]*(dimension[0]-1);
	    ex[1] = origin[1] + spacing[1]*(dimension[1]-1);
	    return ex;
		                              
	}
	
	private double getInterpolatedValue(double x,double y) {
		
		x = x-origin[0];
		y = y-origin[1];
		// get the surrounding gridPoints for the x and y value
		int indexX = (int)(x / spacing[0]);
		int indexY = (int)(y/spacing[1]);
		
		if(indexX >= dimension[0]-1 || indexY >= dimension[1]-1)
			throw new IndexOutOfBoundsException("The value is outside the grid");
		
		double x1 = indexX * spacing[0] ;
		double x2 = x1 + spacing[0];
		double y1 = indexY * spacing[1];
		double y2 = y1 + spacing[1];
		
		
		double deltaX1 = x-x1;
		double deltaX2 = x2-x;
		// interpolating the x-direction
		double val1 =(deltaX2/spacing[0])*getValueAtId(indexX, indexY) +(deltaX1/spacing[0])*getValueAtId(indexX+1,indexY) ; 
		double val2 = (deltaX2/spacing[0])*getValueAtId(indexY, indexY+1) + (deltaX1/spacing[0])*getValueAtId(indexX+1, indexY+1);
		
		return ((y2-y)/spacing[1])*val1 + ((y-y1)/spacing[1])*val2;
	}
}
