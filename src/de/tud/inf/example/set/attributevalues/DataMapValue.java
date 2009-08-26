package de.tud.inf.example.set.attributevalues;

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.tools.Ontology;

/**
 * 
 * encapsulates simple Map<String,Double> as complex object
 * TODO think about type parameters here, if a map with double keys is needed
 * @author Antje Gruner
 * @param <K>
 *
 */
public class DataMapValue implements ComplexValue{

	
	private Map<String, Double> map;
	/**
	 * maps keys of HashMap entries to string keys
	 */
	private NominalMapping keyMapping = null;
	
	
	public DataMapValue(){
		
	}
	
	public DataMapValue(Map<String,Double> map){
		this.map = map;
	}	
	
	public double getDoubleValue() {
		return 0;
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		return map.toString();
	}

	public int getValueType() {
		return Ontology.DATA_MAP;
	}
	
	public double get(String key){
		Double dKey = new Double(keyMapping.mapString(key));
		return map.get(dKey);
	}
	
	
	public void setValues(double[][] values){
		map = new HashMap<String,Double>();
		for (int i =0;i<values.length;i++)
			map.put(keyMapping.mapIndex((int)values[i][0]),new Double(values[i][1]));
	}
	
	public void setValues(double[][] values,NominalMapping keyMapping){
		this.keyMapping = keyMapping;
		map = new HashMap<String,Double>();
		for (int i =0;i<values.length;i++)
			map.put(keyMapping.mapIndex((int)values[i][0]),new Double(values[i][1]));
	}
	
	/*
	public void setValues(double[] keys, T[] values){
		map = new HashMap<String,T>();
		for (int i =0;i<keys.length;i++){
			map.put(keyMapping.mapIndex((int)keys[i]),values[i]);
		}
	}
	
	
	public void setValues(double[] keys, T[] values, NominalMapping keyMapping){
		this.keyMapping = keyMapping;
		setValues(keys,values);		
	}
	*/

	public NominalMapping getKeyMapping() {
		return keyMapping;
	}

	public Map<String, Double> getMap() {
		return map;
	}
	

	
	
}
