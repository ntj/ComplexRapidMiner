/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.example.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.AttributeTypeException;
import com.rapidminer.example.Example;
import com.rapidminer.tools.Tools;


/**
 * This is an implementation of {@link NominalMapping} which can
 * be used for nominal attributes with an arbitrary number of 
 * different values.
 * 
 * @author Ingo Mierswa
 * @version $Id: PolynominalMapping.java,v 1.3 2008/05/09 19:22:45 ingomierswa Exp $
 */
public class PolynominalMapping implements NominalMapping {

	private static final long serialVersionUID = 5021638750496191771L;

	/** The map between symbolic values and their indices. */
	private Map<String, Integer> symbolToIndexMap = new HashMap<String, Integer>();

	/** The map between indices of nominal values and the actual nominal value. */
	private List<String> indexToSymbolMap = new ArrayList<String>();
	
	public PolynominalMapping() {}
	
	/* pp */ PolynominalMapping(NominalMapping mapping) {
		this.symbolToIndexMap.clear();
		this.indexToSymbolMap.clear();
		for (int i = 0; i < mapping.size(); i++) {
			int index = i;
			String value = mapping.mapIndex(index);
			this.symbolToIndexMap.put(value, index);
			this.indexToSymbolMap.add(value);
		}
	}
	
	public Object clone() {
		return new PolynominalMapping(this);
	}

	/**
	 * Returns the index for the nominal attribute value <code>str</code>. If
	 * the string is unknown, a new index value is assigned. Returns -1, if str
	 * is null.
	 */
	public int mapString(String str) {
		if (str == null)
			return -1;
		// lookup string in hashtable
		int index = getIndex(str);
		// if string is not yet in the map, add it
		if (index < 0) {
			// new string -> insert
			indexToSymbolMap.add(str);
			index = indexToSymbolMap.size() - 1;
			symbolToIndexMap.put(str, index);
		}
		return index;
	}

	/**
	 * Returns the index of the given nominal value or -1 if this value was not
	 * mapped before by invoking the method {@link #mapIndex(int)}.
	 */
	public int getIndex(String str) {
		Integer index = symbolToIndexMap.get(str);
		if (index == null)
			return -1;
		else
			return index.intValue();
	}

	/**
	 * Returns the attribute value, that is associated with this index.
	 * Index counting starts with 0. <b>WARNING:</b> In order to iterate over
	 * all values please use the collection returned by {@link #getValues()}.
	 */
	public String mapIndex(int index) {
		if ((index < 0) || (index >= indexToSymbolMap.size()))
			throw new AttributeTypeException("Cannot map index of nominal attribute to nominal value: index " + index + " is out of bounds!");
		return indexToSymbolMap.get(index);
	}

	/** Sets the given mapping. Please note that this will overwrite existing mappings and might
	 *  cause data changes in this way. */
	public void setMapping(String nominalValue, int index) {
		String oldValue = indexToSymbolMap.get(index);
		indexToSymbolMap.set(index, nominalValue);
		symbolToIndexMap.remove(oldValue);
		symbolToIndexMap.put(nominalValue, index);
	}
	
	/**
	 * Returns the index of the first value if this attribute is a
	 * classification attribute, i.e. if it is binominal.
	 */
	public int getNegativeIndex() {
		ensureClassification();
		if (mapIndex(0) == null)
			throw new AttributeTypeException("Attribute: Cannot use FIRST_CLASS_INDEX for negative class!");
		return 0;
	}

	/**
	 * Returns the index of the second value if this attribute is a
	 * classification attribute. Works for all binominal attributes.
	 */
	public int getPositiveIndex() {
		ensureClassification();
		if (mapIndex(0) == null)
			throw new AttributeTypeException("Attribute: Cannot use FIRST_CLASS_INDEX for negative class!");
		Iterator<Integer> i = symbolToIndexMap.values().iterator();
		while (i.hasNext()) {
			int index = i.next();
			if (index != 0)
				return index;
		}
		throw new AttributeTypeException("Attribute: No other class than FIRST_CLASS_INDEX found!");
	}

	public String getNegativeString() {
		return mapIndex(getNegativeIndex());
	}

	public String getPositiveString() {
		return mapIndex(getPositiveIndex());
	}
	
	/** Returns the values of the attribute as an enumeration of strings. */
	public List<String> getValues() {
		return indexToSymbolMap;
	}

	/** Returns the number of different nominal values. */
	public int size() {
		return indexToSymbolMap.size();
	}

	/**
	 * This method rearranges the string to number mappings such that they are
	 * in alphabetical order. <br>
	 * <b>VERY IMPORTANT NOTE:</b> Do not call this method when this attribute
	 * is already associated with an {@link ExampleTable} and it already
	 * contains {@link Example}s. All examples will be messed up since the 
	 * indices will not be replaced in the data table.
	 */
	public void sortMappings() {
		List<String> allStrings = new LinkedList<String>(symbolToIndexMap.keySet());
		Collections.sort(allStrings);
		symbolToIndexMap.clear();
		indexToSymbolMap.clear();
		Iterator<String> i = allStrings.iterator();
		while (i.hasNext()) {
			mapString(i.next());
		}
	}
	
	/** Clears all mappings for nominal values. */
	public void clear() {
		symbolToIndexMap.clear();
		indexToSymbolMap.clear();
	}
	
	/**
	 * Throws a runtime exception if this attribute is not a classification
	 * attribute.
	 */
	private void ensureClassification() {
		if (size() != 2)
			throw new AttributeTypeException("Attribute " + this.toString() + " is not a classification attribute!");
	}
	
	public String toString() {
		return indexToSymbolMap.toString() + Tools.getLineSeparator() + symbolToIndexMap.toString();
	}
}
