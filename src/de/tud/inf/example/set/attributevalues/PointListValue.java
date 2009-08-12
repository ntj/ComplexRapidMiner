package de.tud.inf.example.set.attributevalues;

import com.rapidminer.tools.Ontology;

/**
 * encapsulates a point cloud in 3-dimensional space
 * @author Antje Gruner
 *
 */
public class PointListValue implements ComplexValue{
	/**
	 * defines the maximum number of points which are written in string representation of this object
	 */
	public int maxPlotPoints = 100;
	double[][] points;
	
	public double getDoubleValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		StringBuilder builder = new StringBuilder();
		if(points.length>0){
			//first point
			builder.append("{" + points[0][0]);
			int max = Math.min(maxPlotPoints, points.length);
			for (int j=1;j<points[0].length;j++){
				builder.append(", " + points[0][j]);
			}
			builder.append("}");
			for (int i=1;i<max;i++){
				//first coordinate
				builder.append(", {" + points[i][0]);
				for (int j=1;j<points[i].length;j++){
					builder.append(", " + points[i][j]);
				}
				builder.append("}");
			}
			return builder.toString();
		}
		return "{}";	
	}

	public int getValueType() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.POINT_LIST;
	}
	
	/*
	 * returns an array which contains a point {x,y,z}
	 */
	public double[] getValue(int id){
		return points[id];
	}
	
	
	public void setValues(double[][] points){
		//reference??
		this.points = points;
	}

}
