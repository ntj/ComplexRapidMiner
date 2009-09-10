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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.rapidminer.example.Attribute;

import de.tud.inf.example.set.attributevalues.ComplexValue;
import de.tud.inf.example.table.ComplexAttribute;


/**
 * This interface defines methods for all entries of ExampleTable
 * implementations. It provides a set and get method for the data. Subclasses
 * may use a double array, a sparse representation, a file or a database.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: DataRow.java,v 1.7 2008/07/31 17:43:41 ingomierswa Exp $
 */
public abstract class DataRow implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3482048832637144523L;
	
	/**
	 * this map stores value lists for each relational attribute (identified with table index)
	 */
	private Map<Integer,double[][]> relValueMap = new HashMap<Integer,double[][]>();
	

	/** Returns the value for the given index. */
	protected abstract double get(int index, double defaultValue);
	
	
	/** Sets the given data for the given index. */
	protected abstract void set(int index, double value, double defaultValue);

	/**
	 * Ensures that neither <code>get(i)</code> nor <code>put(i,v)</code>
	 * throw a runtime exception for all <i>0 <= i <= numberOfColumns</i>.
	 */
	protected abstract void ensureNumberOfColumns(int numberOfColumns);

	/** Trims the number of columns to the actually needed number. */
	public abstract void trim();
	
	/** Returns a string representation for this data row. */
	public abstract String toString();
	
	/** Returns the value stored at the given {@link Attribute}'s index. 
	 *  Returns Double.NaN if the given attribute is null. */
	public double get(Attribute attribute) {
		if (attribute == null) {
			return Double.NaN;
		} else {
			try {
				
				return attribute.getValue(this);
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new ArrayIndexOutOfBoundsException("DataRow: table index " + attribute.getTableIndex() + " of Attribute " + attribute.getName() + " is out of bounds.");
			}
		}
	}
	
	
	/** Sets the value of the {@link Attribute} to <code>value</code>. */
	public void set(Attribute attribute, double value) {
		attribute.setValue(this, value);
	}
	
	/** Sets the value of the {@link Attribute} to <code>value</code>. */
	public void set(ComplexAttribute attribute, ComplexValue value) {
		attribute.setComplexValue(this, value);
	}
	
	/**
	 * 
	 * @param attribute
	 * @return
	 */
	public ComplexValue getComplexValue(ComplexAttribute attribute)
	{
		if (attribute == null) {
			return null;
		} else {
			try {
				return attribute.getComplexValue(this);
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new ArrayIndexOutOfBoundsException("DataRow: table index " + attribute.getTableIndex() + " of Attribute " + attribute.getName() + " is out of bounds.");
			}
		}
	}
	
	/**
	 * get tuple instances for relational attribute identified with tableIndex (it is not the id of complexAttributes)
	 * @param tableIndex
	 * @return
	 */
	public double[][] getRelativeValuesFor(Integer tableIndex) {
		return relValueMap.get(tableIndex);
	}
	
	
	/**
	 * 
	 * @param valueMap key: tableIndex of relational attribute, values: tuple instances of relational attribute
	 */
	public void setRelationalValues(Map<Integer,double[][]>valueMap){
		this.relValueMap = valueMap;
	}
	
	/**
	 * 
	 * @param valueMap key: tableIndex of relational attribute, values: tuple instances of relational attribute
	 */
	public void setRelationalValues(int tableIndex, double[][] values){
		this.relValueMap.put(tableIndex, values);
	}
}

















