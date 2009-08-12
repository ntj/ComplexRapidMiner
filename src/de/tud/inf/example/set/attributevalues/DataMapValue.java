package de.tud.inf.example.set.attributevalues;

import java.util.HashMap;

import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.tools.Ontology;

/**
 * encapulates simple HashMap as complex object
 * @author Antje Gruner
 *
 */
public class DataMapValue implements ComplexValue{

	
	private HashMap<Double,Double> map;
	/**
	 * maps keys of HashMap entries to string keys
	 */
	private NominalMapping keyMapping = null;
	public double getDoubleValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		return map.toString();
	}

	public int getValueType() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.DATA_MAP;
	}
	
	public double get(String key){
		Double dKey = new Double(keyMapping.mapString(key));
		return map.get(dKey).doubleValue();
	}
	
	public double get(Double key){
		return map.get(key).doubleValue();
	}
	
	public void setValues(double[][] values){
		map = new HashMap<Double,Double>();
		for (int i =0;i<values.length;i++)
			map.put(values[i][0], values[i][1]);
	}
	
	public void setValues(double[][] values, NominalMapping keyMapping){
		setValues(values);
		this.keyMapping = keyMapping;
	}

}
