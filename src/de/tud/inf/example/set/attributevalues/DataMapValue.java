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

	
	private Map<String, Integer> map;
	/**
	 * maps keys of HashMap entries to string keys
	 */
	private NominalMapping keyMapping = null;
	
	
	public DataMapValue(){
		
	}
	
	public DataMapValue(Map<String,Integer> map){
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
	
	public int get(String key){
		Double dKey = new Double(keyMapping.mapString(key));
		return ((Integer)map.get(dKey)).intValue();
	}
	
	
	public void setValues(double[][] values){
		map = new HashMap<String,Integer>();
		for (int i =0;i<values.length;i++)
			map.put(keyMapping.mapIndex((int)values[i][0]),new Integer((int) values[i][1]));
	}
	
	public void setValues(double[][] values, NominalMapping keyMapping){
		this.keyMapping = keyMapping;
		setValues(values);		
	}

	public NominalMapping getKeyMapping() {
		return keyMapping;
	}

	public Map<String, Integer> getMap() {
		return map;
	}
	

	
	
}
