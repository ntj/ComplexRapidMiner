/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.example.table;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ConstructionDescription;
import com.rapidminer.example.Statistics;
import com.rapidminer.tools.Ontology;


/**
 * This is a possible abstract superclass for all attribute implementations.
 * Most methods of {@link Attribute} are already implemented here.
 *  
 * @author Ingo Mierswa
 * @version $Id: AbstractAttribute.java,v 1.3 2007/06/18 13:38:47 ingomierswa Exp $
 */
public abstract class AbstractAttribute implements Attribute {

	/** Optionally contains the name of the attribute. */
	private String name;

	/**
	 * An int indicating the value type in terms of the
	 * Ontology.ATTRIBUTE_VALUE_TYPE.
	 */
	private int valueType = Ontology.ATTRIBUTE_VALUE;

	/**
	 * An int indicating the block type in terms of the
	 * Ontology.ATTRIBUTE_BLOCK_TYPE.
	 */
	private int blockType = Ontology.ATTRIBUTE_BLOCK;

	/** Index of this attribute in its ExampleTable. */
	private int index = UNDEFINED_ATTRIBUTE_INDEX;

	/** The default value for this Attribute. */
	private double defaultValue = 0.0;
	
    /** Contains all attribute statistics calculation algorithms. */
	private List<Statistics> statistics = new LinkedList<Statistics>();
	
	/** The current attribute construction description object. */
	private ConstructionDescription constructionDescription;
	
	// --------------------------------------------------------------------------------

	/**
	 * Creates a simple attribute which is not part of a series and does not
	 * provide a unit string. This constructor should only be used for
	 * attributes which were not generated with help of a generator, i.e.
	 * this attribute has no function arguments.
	 */
	AbstractAttribute(String name, int valueType) {
		this(name, valueType, Ontology.SINGLE_VALUE);
	}

	/** Creates a new attribute. */
	AbstractAttribute(String name, int valueType, int blockType) {
		this.name = name;
		this.valueType = valueType;
		this.blockType = blockType;
		this.index = UNDEFINED_ATTRIBUTE_INDEX;
		this.constructionDescription = new ConstructionDescription(this, this.name);
	}

	/** Clones this attribute. */
	public abstract Object clone();
	
	/**
	 * Returns true if the given attribute has the same name and table index. Of
	 * course it would be still possible that both attributes are part of
	 * different {@link AbstractExampleTable}s.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof AbstractAttribute))
			return false;
		AbstractAttribute a = (AbstractAttribute) o;
		if (!this.name.equals(a.getName()))
			return false;
		if (this.index != a.getTableIndex())
			return false;
		return true;
	}

	public int hashCode() {
		return name.hashCode() ^ Integer.valueOf(this.index).hashCode();
	}

	public double getValue(DataRow row) {
		return row.get(getTableIndex(), getDefault());
	}

	public void setValue(DataRow row, double value) {
		row.set(getTableIndex(), value, getDefault());
	}
	
	/** Returns the name of the attribute. */
	public String getName() {
		return name;
	}

	/** Sets the name of the attribtue. */
	public void setName(String v) {
		this.name = v;
	}

	/** Returns the index in the example table. */
	public int getTableIndex() {
		return index;
	}

	/** Sets the index in the example table. */
	public void setTableIndex(int i) {
		this.index = i;
	}

	// --- meta data of data ---

	/**
	 * Returns the block type of this attribute.
	 * 
	 * @see com.rapidminer.tools.Ontology#ATTRIBUTE_BLOCK_TYPE
	 */
	public int getBlockType() {
		return blockType;
	}

	/**
	 * Sets the block type of this attribute.
	 * 
	 * @see com.rapidminer.tools.Ontology#ATTRIBUTE_BLOCK_TYPE
	 */
	public void setBlockType(int b) {
		this.blockType = b;
	}

	/**
	 * Returns the value type of this attribute.
	 * 
	 * @see com.rapidminer.tools.Ontology#ATTRIBUTE_VALUE_TYPE
	 */
	public int getValueType() {
		return valueType;
	}

	/** Returns the attribute statistics. */
	public Iterator<Statistics> getAllStatistics() {
		return statistics.iterator();
	}
	
    public void registerStatistics(Statistics statistics) {
        this.statistics.add(statistics);
    }
    
    public double getStatistics(String name) {
        return getStatistics(name, null);
    }
    
    public double getStatistics(String name, String parameter) {
        for (Statistics statistics : this.statistics) {
            if (statistics.handleStatistics(name)) {
                return statistics.getStatistics(name, parameter);
            }
        }
        throw new RuntimeException("No statistics object was available for attribute statistics '" + name + "'!");
    }
    
	/** Returns the construction description. */
	public ConstructionDescription getConstruction() {
		return constructionDescription;
	}
	
	// ================================================================================
	// default value
	// ================================================================================

	public void setDefault(double value) {
		this.defaultValue = value;
	}

	public double getDefault() {
		return defaultValue;
	}

	// ================================================================================
	// string and result methods
	// ================================================================================

	/** Returns a human readable string that describes this attribute. */
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("#");
		result.append(index);
		result.append(": ");
		result.append(name);
		result.append(" (");
		result.append(Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(valueType));
		result.append("/");
		result.append(Ontology.ATTRIBUTE_BLOCK_TYPE.mapIndex(blockType));
		result.append(")");
		return result.toString();
	}

	/**
	 * Writes the (non transient) attribute data to an output stream. Sublasses
	 * which has to overwrite this method should first invoke
	 * super.writeAttributeData(DataOutput).
	 */
	public void writeAttributeData(DataOutput out) throws IOException {
		out.writeUTF(name);
		out.writeInt(valueType);
		out.writeInt(blockType);
	}

	/**
	 * Reads the attribute data and initializes the corresponding fields from
	 * the given input stream. The name and the valuetype are not read since
	 * this information was already used by the attribute factory to create the
	 * correct attribute instance. Subclasses which has to overwrite this method
	 * should first invoke super.readAttributeData(DataInput).
	 */
	public void readAttributeData(DataInput in) throws IOException {
		setBlockType(in.readInt());
	}
}
