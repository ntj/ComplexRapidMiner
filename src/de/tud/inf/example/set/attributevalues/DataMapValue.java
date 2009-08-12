package de.tud.inf.example.set.attributevalues;

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.tools.Ontology;

/**
 * encapsulates simple Map as complex object
 * @author Antje Gruner
 * @param <K>
 *
 */
public class DataMapValue<K,V> implements ComplexValue{

	
	private Map<K,V> map;
	/**
	 * maps keys of HashMap entries to string keys
	 */
	private NominalMapping keyMapping = null;
	
	
	public DataMapValue(){
		
	}
	
	public DataMapValue(Map<K,V> map){
		this.map = map;
	}	
	
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
		return ((Double)map.get(dKey)).doubleValue();
	}
	
	public double get(Double key){
		return ((Double) map.get(key)).doubleValue();
	}
	
	public void setValues(double[][] values){
		map = new HashMap<K,V>();
		//TODO check K, V -> mapping
		//for (int i =0;i<values.length;i++)
			//map.put((K)values[i][0],(V)values[i][1]);
	}
	
	public void setValues(double[][] values, NominalMapping keyMapping){
		setValues(values);
		this.keyMapping = keyMapping;
	}

}
