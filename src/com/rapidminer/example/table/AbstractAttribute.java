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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeDescription;
import com.rapidminer.example.AttributeTransformation;
import com.rapidminer.example.ConstructionDescription;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.tools.Ontology;


/**
 * This is a possible abstract superclass for all attribute implementations.
 * Most methods of {@link Attribute} are already implemented here.
 *  
 * @author Ingo Mierswa
 * @version $Id: AbstractAttribute.java,v 1.14 2008/05/09 19:22:45 ingomierswa Exp $
 */
public abstract class AbstractAttribute implements Attribute {

	/** The basic information about the attribute. Will only be shallowly cloned. */
	private AttributeDescription attributeDescription;
	
	private LinkedList<AttributeTransformation> transformations = new LinkedList<AttributeTransformation>();
	
    /** Contains all attribute statistics calculation algorithms. */
	private List<Statistics> statistics = new LinkedList<Statistics>();
	
	/** The current attribute construction description object. */
	private ConstructionDescription constructionDescription = null;
	
	// --------------------------------------------------------------------------------

	/**
	 * Creates a simple attribute which is not part of a series and does not
	 * provide a unit string. This constructor should only be used for
	 * attributes which were not generated with help of a generator, i.e.
	 * this attribute has no function arguments. Only the last transformation
	 * is cloned, the other transformations are cloned by reference.
	 */
	/* pp */ AbstractAttribute(AbstractAttribute attribute) {
		this.attributeDescription = attribute.attributeDescription;
		
		// copy statistics
		this.statistics = new LinkedList<Statistics>();
		for (Statistics statistics : attribute.statistics) {
			this.statistics.add((Statistics)statistics.clone());
		}
		
		// copy transformations if necessary
		int counter = 0;
		for (AttributeTransformation transformation : attribute.transformations) {
			if (counter < attribute.transformations.size() - 1) {
				addTransformation(transformation);
			} else {
				addTransformation((AttributeTransformation)transformation.clone());
			}
		}
		
		// copy construction description
		this.constructionDescription = (ConstructionDescription)attribute.constructionDescription.clone();
	}
	
	/**
	 * Creates a simple attribute which is not part of a series and does not
	 * provide a unit string. This constructor should only be used for
	 * attributes which were not generated with help of a generator, i.e.
	 * this attribute has no function arguments.
	 */
	/* pp */ AbstractAttribute(String name, int valueType) {
		this.attributeDescription = new AttributeDescription(this, name, valueType, Ontology.SINGLE_VALUE, 0.0d, UNDEFINED_ATTRIBUTE_INDEX);
		this.constructionDescription = new ConstructionDescription(this);
	}

	/** Clones this attribute. */
	public abstract Object clone();
	
	/**
	 * Returns true if the given attribute has the same name and the same table index.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof AbstractAttribute))
			return false;
		AbstractAttribute a = (AbstractAttribute) o;
		return this.attributeDescription.equals(a.attributeDescription);
	}

	public int hashCode() {
		return attributeDescription.hashCode();
	}

	public void addTransformation(AttributeTransformation transformation) {
		this.transformations.add(transformation);
	}
	
	public void clearTransformations() {
		this.transformations.clear();
	}
	
	public AttributeTransformation getLastTransformation() {
		if (this.transformations.size() > 0)
			return this.transformations.getLast();
		else
			return null;
	}
	
	public double getValue(DataRow row) {
		double tableValue = row.get(getTableIndex(), getDefault()); 
		double result = tableValue;
		for (AttributeTransformation transformation : transformations) {
			result = transformation.transform(this, result);
		}
		return result;
	}

	public void setValue(DataRow row, double value) {
		double newValue = value;
		for (AttributeTransformation transformation : transformations) {
			if (transformation.isReversable()) {
				newValue = transformation.inverseTransform(this, newValue);
			} else {
				throw new RuntimeException("Cannot set value for attribute using irreversible transformations. This process will probably work if you deactivate create_view in preprocessing operators.");
			}
		}
		row.set(getTableIndex(), newValue, getDefault());
	}
	
	/** Returns the name of the attribute. */
	public String getName() {
		return this.attributeDescription.getName();
	}

	/** Sets the name of the attribtue. */
	public void setName(String v) {
		this.attributeDescription = (AttributeDescription)this.attributeDescription.clone();
		this.attributeDescription.setName(v);
	}

	/** Returns the index in the example table. */
	public int getTableIndex() {
		return this.attributeDescription.getTableIndex();
	}

	/** Sets the index in the example table. */
	public void setTableIndex(int i) {
		this.attributeDescription = (AttributeDescription)this.attributeDescription.clone();
		this.attributeDescription.setTableIndex(i);
	}

	// --- meta data of data ---

	/**
	 * Returns the block type of this attribute.
	 * 
	 * @see com.rapidminer.tools.Ontology#ATTRIBUTE_BLOCK_TYPE
	 */
	public int getBlockType() {
		return this.attributeDescription.getBlockType();
	}

	/**
	 * Sets the block type of this attribute.
	 * 
	 * @see com.rapidminer.tools.Ontology#ATTRIBUTE_BLOCK_TYPE
	 */
	public void setBlockType(int b) {
		this.attributeDescription = (AttributeDescription)this.attributeDescription.clone();
		this.attributeDescription.setBlockType(b);
	}

	/**
	 * Returns the value type of this attribute.
	 * 
	 * @see com.rapidminer.tools.Ontology#ATTRIBUTE_VALUE_TYPE
	 */
	public int getValueType() {
		return this.attributeDescription.getValueType();
	}

	/** Returns the attribute statistics. */
	public Iterator<Statistics> getAllStatistics() {
		return this.statistics.iterator();
	}
	
    public void registerStatistics(Statistics statistics) {
        this.statistics.add(statistics);
    }
    
	/** Returns the attribute statistics. 
     *   
     *  @deprecated Please use the method {@link ExampleSet#getStatistics(Attribute, String)} instead. */
	@Deprecated
    public double getStatistics(String name) {
        return getStatistics(name, null);
    }
    
	/** Returns the attribute statistics. 
     *   
     *  @deprecated Please use the method {@link ExampleSet#getStatistics(Attribute, String)} instead. */
	@Deprecated
    public double getStatistics(String name, String parameter) {
        for (Statistics statistics : this.statistics) {
            if (statistics.handleStatistics(name)) {
                return statistics.getStatistics(this, name, parameter);
            }
        }
        throw new RuntimeException("No statistics object was available for attribute statistics '" + name + "'!");
    }
    
	/** Returns the construction description. */
	public ConstructionDescription getConstruction() {
		return this.constructionDescription;
	}
	
	// ================================================================================
	// default value
	// ================================================================================

	public void setDefault(double value) {
		this.attributeDescription = (AttributeDescription)this.attributeDescription.clone();
		this.attributeDescription.setDefault(value);
	}

	public double getDefault() {
		return this.attributeDescription.getDefault();
	}

	// ================================================================================
	// string and result methods
	// ================================================================================

	/** Returns a human readable string that describes this attribute. */
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("#");
		result.append(this.attributeDescription.getTableIndex());
		result.append(": ");
		result.append(this.attributeDescription.getName());
		result.append(" (");
		result.append(Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(this.attributeDescription.getValueType()));
		result.append("/");
		result.append(Ontology.ATTRIBUTE_BLOCK_TYPE.mapIndex(this.attributeDescription.getBlockType()));
		result.append(")");
		return result.toString();
	}
}
