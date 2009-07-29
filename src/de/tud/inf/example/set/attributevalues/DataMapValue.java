package de.tud.inf.example.set.attributevalues;

import java.util.HashMap;

import com.rapidminer.tools.Ontology;

/**
 * encapulates simple HashMap as complex object
 * TODO: think about type parameter here
 * @author Antje Gruner
 *
 */
public class DataMapValue implements ComplexValue{

	
	private HashMap<Double,Double> map;
	public double getDoubleValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		/*
		String str = "";
		for(Double key:map.keySet()){
			str += "{"+ key + "  " + map.get(key) +"} "; 
		}
		return str;
		*/
		return map.toString();
	}

	public int getValueType() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.DATA_MAP;
	}
	
	public double get(String key){
		return map.get(key).doubleValue();
	}
	
	
	
	
	public void setValues(double[][] values){
		map = new HashMap<Double,Double>();
		for (int i =0;i<values.length;i++)
			map.put(values[i][0], values[i][1]);
	}

}
