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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.AttributeTypeException;
import com.rapidminer.example.Example;


/**
 * This is an efficient implementation of a {@link NominalMapping}
 * which can be used for binominal attributes, i.e. for attributes
 * with only two different values.
 * 
 * @author Ingo Mierswa
 * @version $Id: BinominalMapping.java,v 1.3 2008/05/09 19:22:44 ingomierswa Exp $
 */
public class BinominalMapping implements NominalMapping {

	private static final long serialVersionUID = 6566553739308153153L;

	/** The index of the first value. */
	private static final int FIRST_VALUE_INDEX = 0;

	/** The index of the second value. */
	private static final int SECOND_VALUE_INDEX = 1;
	
	/** The first nominal value. */
	private String firstValue = null;

	/** The second nominal value. */
	private String secondValue = null;
	
	public BinominalMapping() {}
	
	/** Clone constructor. */
	private BinominalMapping(BinominalMapping mapping) {
		this.firstValue  = mapping.firstValue;
		this.secondValue = mapping.secondValue;
	}
	
	/** Clone constructor. */
	/* pp */ BinominalMapping(NominalMapping mapping) {
		if (mapping.size() > 0)
			firstValue = mapping.mapIndex(0);
		if (mapping.size() > 1)
			secondValue = mapping.mapIndex(1);
	}
	
	public Object clone() {
		return new BinominalMapping(this);
	}
	
	/**
	 * Returns the index for the nominal attribute value <code>str</code>. If
	 * the string is unknown, a new index value is assigned. Returns -1, if str
	 * is null.
	 */
	public int mapString(String str) {
		if (str == null)
			return -1;
		// lookup string
		int index = getIndex(str);
		if (index < 0) {
			// if string is not found, set it
			if (firstValue == null) {
				firstValue = str;
				return FIRST_VALUE_INDEX;
			} else if (secondValue == null) {
				secondValue = str;
				return SECOND_VALUE_INDEX;
			} else {
				throw new AttributeTypeException("Cannot map another string for binary attribute: already mapped two strings!");
			}
		} else {
			return index;
		}
	}

	/**
	 * Returns the index of the given nominal value or -1 if this value was not
	 * mapped before by invoking the method {@link #mapIndex(int)}.
	 */
	public int getIndex(String str) {
		if (str.equals(firstValue))
			return FIRST_VALUE_INDEX;
		else if (str.equals(secondValue))
			return SECOND_VALUE_INDEX;
		else
			return -1;
	}

	/**
	 * Returns the attribute value, that is associated with this index.
	 * Index counting starts with 0. <b>WARNING:</b> In order to iterate over
	 * all values please use the collection returned by {@link #getValues()}.
	 */
	public String mapIndex(int index) {
		switch (index) {
			case FIRST_VALUE_INDEX:
				return firstValue;
			case SECOND_VALUE_INDEX:
				return secondValue;
			default:
				throw new AttributeTypeException("Cannot map index of binary attribute to nominal value: index " + index + " is out of bounds!");
		}
	}

	/** Sets the given mapping. Please note that this will overwrite existing mappings and might
	 *  cause data changes in this way. */
	public void setMapping(String nominalValue, int index) {
		if (index == FIRST_VALUE_INDEX) {
			firstValue = nominalValue;
		} else if (index == SECOND_VALUE_INDEX) {
			secondValue = nominalValue;
		} else {
			throw new AttributeTypeException("Cannot set mapping of binary attribute to index '" + index + "'.");
		}
	}
	
	/**
	 * Returns the index of the first value if this attribute is a
	 * classification attribute, i.e. if it is binominal.
	 */
	public int getNegativeIndex() {
		return FIRST_VALUE_INDEX;
	}

	/**
	 * Returns the index of the second value if this attribute is a
	 * classification attribute. Works for all binominal attributes.
	 */
	public int getPositiveIndex() {
		return SECOND_VALUE_INDEX;
	}

	public String getNegativeString() {
		return firstValue;
	}

	public String getPositiveString() {
		return secondValue;
	}
	
	/** Returns the values of the attribute as an enumeration of strings. */
	public List<String> getValues() {
		if (firstValue == null)
			return new LinkedList<String>();
		else if (secondValue == null)
			return Arrays.asList(new String[] { firstValue });
		else
			return Arrays.asList(new String[] { firstValue, secondValue });
	}

	/** Returns the number of different nominal values. */
	public int size() {
		if (firstValue == null)
			return 0;
		else if (secondValue == null)
			return 1;
		else
			return 2;
	}
	
	/**
	 * This method rearranges the string to number mappings such that they are
	 * in alphabetical order. <br>
	 * <b>VERY IMPORTANT NOTE:</b> Do not call this method when this attribute
	 * is already associated with an {@link AbstractExampleTable} and it already
	 * contains {@link Example}s. All examples will be messed up!
	 */
	public void sortMappings() {
		if (size() == 2) {
			if (firstValue.compareTo(secondValue) > 0) {
				String dummy = secondValue;
				secondValue = firstValue;
				firstValue = dummy;
			}
		}
	}
	
	/** Clears all mappings for nominal values. */
	public void clear() {
		firstValue = null;
		secondValue = null;
	}
}
