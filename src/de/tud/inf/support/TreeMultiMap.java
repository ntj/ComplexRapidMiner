package de.tud.inf.support;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;

public class TreeMultiMap<K, V> {
	protected TreeMap<K, ArrayList<V>> data;
	
	public TreeMultiMap() {
		data = new TreeMap<K, ArrayList<V>>();
	}
	
	public ArrayList<V> get(K key) {
		return data.get(key);
	}
	
	public V getElement(K key, int index) {
		return data.get(key).get(index);
	}

	public V put(K key, V value) {
		if(data.containsKey(key) == false) {
			data.put(key, new ArrayList<V>());
		} 
		
		data.get(key).add(value);

		return value;
	}


	public int size() {
		return data.size();
	}
	
	public int totalSize() {
		K currentKey = data.lastKey();
		
		int size = 0;
		if(currentKey != null) {
			do {
				size += data.get(currentKey).size();
				//currentKey = data.higherKey(currentKey);
			} while(currentKey != null);
		}
		
		return size;
	}
	
	public Entry<K, ArrayList<V>> lastEntry() {
		//return data.lastEntry();
		return null;
	}
	
	public K lastKey() {
		return data.lastKey();
	}
	
	public Entry<K, ArrayList<V>> firstEntry() {
		///return data.firstEntry();
		return null;
	}
	
	public K firstKey() {
		return data.firstKey();
	}
	
	public Entry<K, ArrayList<V>> higherEntry(K key){
		//return data.higherEntry(key);
		return null;
	}
	
	public K higherKey(K key) {
		//return data.higherKey(key);
		return null;
	}
	
	public Entry<K, ArrayList<V>> lowerEntry(K key){
		//return data.lowerEntry(key);
		return null;
	}
	
	public K lowerKey(K key) {
		//return data.lowerKey(key);
		return null;
	}
	
	public boolean remove(K key, V value) {
		boolean ret_val = data.get(key).remove(value);
		if(data.get(key).isEmpty()) {
			data.remove(key);
		}
		return ret_val;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		K currentKey = data.lastKey();
		do {
			ArrayList<V> currentList = data.get(currentKey);
			sb.append(currentKey);
			sb.append(":");
			sb.append(currentList);
			sb.append("\n");
			//currentKey = data.lowerKey(currentKey);
		} while (currentKey != null);
		
		return sb.toString();
	}
}
