package de.tud.inf.example.set.attributevalues;

import com.rapidminer.tools.Ontology;

/**
 * encapsulates a point cloud in 3-dimensional space
 * @author Antje Gruner
 *
 */
public class PointListValue implements ComplexValue{

	public
	
	double[][] points;
	public double getDoubleValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		//TODO use StringBuilder
		String str = "";
		if(points.length>0){
			//first point
			str += "{" + points[0][0];
			for (int j=1;j<points[0].length;j++){
				str += ", " + points[0][j];
			}
			str += "}";
			for (int i=1;i<points.length;i++){
				//first coordinate
				str += ", {" + points[i][0];
				for (int j=1;j<points[i].length;j++){
					str += ", " + points[i][j];
				}
				str += "}";
			}
			return str;
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
