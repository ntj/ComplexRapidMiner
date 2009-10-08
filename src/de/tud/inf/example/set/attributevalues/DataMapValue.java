package de.tud.inf.example.set.attributevalues;

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalMapping;
import com.rapidminer.tools.Ontology;

/**
 * 
 * encapsulates simple Map<Double,Double> as complex object
 * @author Antje Gruner
 * @param <T>
 * @param <K>
 *
 */
public class DataMapValue implements ComplexValue{

	
	private Map<Integer, Double> map;
	
	/**
	 * maps keys of HashMap entries to string keys
	 */
	private NominalMapping keyMapping = null;
	
	DataMapValue(){
		
	}
	
	DataMapValue(Map<Integer, Double> map){
		this.map = map;
	}
	
	public DataMapValue(Map<Integer, Double> map, NominalMapping keyMapping ){
		this.map = map;
		this.keyMapping = keyMapping;
	}
	
	public double getDoubleValue() {
		return 0;
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		if(keyMapping == null)
			return map.toString();
		Map<String,Double> strMap = new HashMap<String, Double>();
		for(Map.Entry<Integer,Double> entry: map.entrySet()){
			strMap.put(keyMapping.mapIndex(entry.getKey()), entry.getValue());
		}
		return strMap.toString();
	}

	public int getValueType() {
		return Ontology.DATA_MAP;
	}
	
	public double get(String key){
		Double dKey = new Double(keyMapping.mapString(key));
		return map.get(dKey);
	}
	
	
	public void setValues(double[][] values){
		map = new HashMap<Integer,Double>();
		for (int i =0;i<values.length;i++)
			map.put((int)values[i][0],values[i][1]);
	}
	
	public void setValues(double[][] values,NominalMapping keyMapping){
		this.keyMapping = keyMapping;
		map = new HashMap<Integer,Double>();
		for (int i =0;i<values.length;i++)
			map.put((int)values[i][0],values[i][1]);
	}
	
	public void setValues(String[] keys, double[] values){
		map = new HashMap<Integer, Double>();
		keyMapping = new PolynominalMapping();
		for(int i=0;i<keys.length;i++)
			map.put(keyMapping.mapString(keys[i]), values[i]);
	}

	public void setMap(Map<Integer,Double> map){
		this.map = map;
	}
	
	public void setStringMap(Map<String,Double> strMap){
		map = new HashMap<Integer, Double>();
		keyMapping = new PolynominalMapping();
		for (Map.Entry<String, Double> mapEntry: strMap.entrySet()) {
			map.put(keyMapping.mapString(mapEntry.getKey()), mapEntry.getValue());
		}
	}
	
	public void setStringIntMap(Map<String,Integer> strMap){
		map = new HashMap<Integer, Double>();
		keyMapping = new PolynominalMapping();
		for (Map.Entry<String, Integer> mapEntry: strMap.entrySet()) {
			map.put(keyMapping.mapString(mapEntry.getKey()), mapEntry.getValue().doubleValue());
		}
	}

	public NominalMapping getKeyMapping() {
		return keyMapping;
	}

	public Map<Integer, Double> getMap() {
		return map;
	}
	
	public Map<String, Double> getStringMap() {
		Map<String,Double> result = null;
		if(keyMapping != null){
			result = new HashMap<String, Double>();
			for (Map.Entry<Integer, Double> mapEntry: map.entrySet()) {
				result.put(keyMapping.mapIndex(mapEntry.getKey()), mapEntry.getValue());
			}
		}
		return result; 
	}
	
	public int size(){
		return map.size();
	}
	
	
}
