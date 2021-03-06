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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ConstructionDescription;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.table.ComplexCompositeAttribute;
import de.tud.inf.example.table.ConstantArrayAttribute;
import de.tud.inf.example.table.DataMapAttribute;
import de.tud.inf.example.table.GaussAttribute;
import de.tud.inf.example.table.HistogramAttribute;
import de.tud.inf.example.table.MapAttribute;
import de.tud.inf.example.table.MatrixAttribute;
import de.tud.inf.example.table.PointListAttribute;
import de.tud.inf.example.table.RelationalAttribute;
import de.tud.inf.example.table.TensorAttribute;
import de.tud.inf.example.table.UniformAttribute;


/**
 * This class is used to create and clone attributes. It should be used to
 * create attributes instead of directly creating them by using constructors.
 * Additionally, it provides some helper methods for attribute creation purposes
 * (name creation, block numbers,...).
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeFactory.java,v 2.12 2006/03/21 15:35:39 ingomierswa
 *          Exp $
 */
public class AttributeFactory {

	/** The prefix of the name of generated attributes. */
	private static final String GENSYM_PREFIX = "gensym";

	/**
	 * The current highest id counters for generated attribute names. The counter will be increased each time an
	 * attribute name is generated more than once.
	 */
	private static Map<String,AtomicInteger> nameCounters = new HashMap<String,AtomicInteger>();

	static {
		resetNameCounters();
	}

	/** Creates a simple single attribute depending on the given value type. */
	public static Attribute createAttribute(String name, int valueType) {
		String attributeName = (name != null) ? name : createName();
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.DATE_TIME)) {
			return new DateAttribute(attributeName, valueType);
		} else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.BINOMINAL)) {
			return new BinominalAttribute(attributeName);
		} else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.NOMINAL)) {
			return new PolynominalAttribute(attributeName, valueType);
		} else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.NUMERICAL)) {
			return new NumericalAttribute(attributeName, valueType);
		} else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.RELATIONAL)) {
			return new RelationalAttribute(attributeName, valueType);
		}
		else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.MAP))
			return new MapAttribute(attributeName, valueType,"");
		else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.DATA_MAP))
			return new DataMapAttribute(attributeName, valueType,"");
		else {
			throw new RuntimeException("AttributeFactory: cannot create attribute with value type '" + Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(valueType) + "' (" + valueType + ")!");
		}
	}

	/**
	 * create Attribute, where an additional information (hint) is necessary, e.g. {@link com.rapidminer.operator.visualization.ProcessLogOperator}
	 * @param name
	 * @param valueType
	 * @param hint
	 * @return
	 */
	public static Attribute createAttribute(String name, int valueType, String hint){
		String attributeName = (name != null) ? name : createName();
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.ARRAY))
			return new ConstantArrayAttribute(attributeName, valueType,hint);
		else 
			throw new RuntimeException("AttributeFactory: cannot create attribute with value type '" + Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(valueType) + "' (" + valueType + ")!");
		
	}
	
	public static Attribute createCompositeAttribute(String name, int valueType, List<Attribute> innerAttributes, List<Attribute> parameters, String hint){
		String attributeName = (name != null) ? name : createName();
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.UNIFORM))
			return new UniformAttribute(attributeName,valueType,innerAttributes,parameters,hint);
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.GAUSS))
			return new GaussAttribute(attributeName,valueType,innerAttributes,parameters,hint);
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.HISTOGRAM))
			return new HistogramAttribute(attributeName,valueType,innerAttributes,parameters,hint);
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.COMPLEX_VALUE))
			return new ComplexCompositeAttribute(attributeName,valueType,innerAttributes,parameters,hint);
		else {
			throw new RuntimeException("AttributeFactory: cannot create attribute with value type '" + Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(valueType) + "' (" + valueType + ")!");
		}
	}
	
	
	/**
	 * those attribute types just have one inner relational attribute, and cannot be parameterized for each row, but for the complete dataset
	 * @param name
	 * @param valueType
	 * @param innerAttribute
	 * @param hint
	 * @return
	 */
	public static Attribute createProxyAttribute(String name, int valueType, RelationalAttribute innerAttribute, String hint){
		String attributeName = (name != null) ? name : createName();
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.MATRIX))
			return new MatrixAttribute(attributeName,valueType,innerAttribute,hint);
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.TENSOR))
			return new TensorAttribute(attributeName,valueType,innerAttribute,hint);
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.POINT_LIST))
			return new PointListAttribute(attributeName,valueType,innerAttribute,hint);
		//since there are no parameter attributes, ArrayAttribute must be a constant one
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.ARRAY))
			return new ConstantArrayAttribute(attributeName,valueType,innerAttribute,hint);
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.DATA_MAP)){
			//if attribute, which stores keys of this map is nominal, set correct valueType
			if(innerAttribute.getInnerAttributeAt(0).isNominal() )
				return new DataMapAttribute(attributeName,Ontology.DATA_MAP_STRING,innerAttribute,hint);
			return new DataMapAttribute(attributeName,valueType,innerAttribute,hint);
		}
		else {
			throw new RuntimeException("AttributeFactory: cannot create attribute with value type '" + Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(valueType) + "' (" + valueType + ")!");
		}
	}
	
	/**
	 * create complex proxy attribute, which has additional parameter attributes (e.g. map)
	 */
	public static Attribute createProxyAttribute(String name, int valueType, RelationalAttribute innerAttribute, List<Attribute> parameters, String symbol,String hint){
		String attributeName = (name != null) ? name : createName();
		//if attribute, which stores z values of this map is nominal, set correct valueType
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.MAP)){
			if(innerAttribute.getInnerAttributeAt(0).isNominal() )
				return new MapAttribute(attributeName,Ontology.MAP_STRING,innerAttribute,parameters,hint);
			return new MapAttribute(attributeName,valueType,innerAttribute,parameters,hint);
		}
		/*
		//create parameterized array attribute if there are parameter attributes, else create constant array attribute
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.ARRAY) && parameters.size() > 0)
			return new ArrayAttribute(attributeName,valueType,innerAttribute,hint,parameters.get(0),parameters.get(1));
		*/
		else return createProxyAttribute(name,valueType,innerAttribute,hint);
	}
	

	/**
	 * Creates a simple single attribute depending on the given value type. The
	 * name is randomly created. This attribute can also be used for generators
	 * to define their desired input attributes for compatibility checks.
	 */
	public static Attribute createAttribute(int valueType) {
		return createAttribute(createName(), valueType);
	}

	/** Creates a single numerical constructed attribute. */
	public static Attribute createAttribute(String functionName, ConstructionDescription[] arguments) {
		return createAttribute(Ontology.NUMERICAL, Ontology.SINGLE_VALUE, functionName, arguments);
	}

	/** Creates a simple attribute depending on the given value type. */
	public static Attribute createAttribute(int valueType, int blockType, String functionName, ConstructionDescription[] arguments) {
		Attribute attribute = createAttribute(valueType);
		attribute.setBlockType(blockType);
		attribute.getConstruction().setFunction(functionName);
		attribute.getConstruction().setArguments(arguments);
		return attribute;
	}

	/** Creates a simple attribute depending on the given value type. */
	public static Attribute createAttribute(String name, int valueType, int blockType) {
		Attribute attribute = createAttribute(name, valueType);
		attribute.setBlockType(blockType);
		return attribute;
	}

	// ================================================================================

	/**
	 * Simple clone factory method for attributes. Invokes
	 * {@link #createAttribute(Attribute att, String name)} with name = null.
	 */
	public static Attribute createAttribute(Attribute attribute) {
		return createAttribute(attribute, null);
	}

	/**
	 * Simple clone factory method for attributes. Returns the clone of the
	 * given attribute and sets the function name to the given one if not null.
	 * In this case the attribute is used as an argument of returned attribute.
	 * This method might be usefull for example to create a prediction attribute
	 * with the same properties as the original label attribute.
	 */
	public static Attribute createAttribute(Attribute attribute, String functionName) {
		Attribute result = (Attribute) attribute.clone();
		if (functionName == null) {
			result.setName(attribute.getName());
		} else {
			result.setName(functionName + "(" + attribute.getName() + ")");
			result.getConstruction().setFunction(functionName);
			result.getConstruction().setArguments(new ConstructionDescription[] { attribute.getConstruction() });
		}
		return result;
	}

	// ================================================================================
	// changes the value type of the given attribute
	// ================================================================================

	/**
	 * Changes the value type of the given attribute and returns a new attribute
	 * with the same properties but the new value type. Since values within examples are
	 * not altered it is not suggested to use this method to change attributes within an
	 * exampleset in use. Operators should create a new attribute to ensure parallel executability.
	 */
	public static Attribute changeValueType(Attribute attribute, int valueType) {
		Attribute result = createAttribute(attribute.getName(), valueType);
		if (attribute.isNominal() && result.isNominal())
			result.setMapping(attribute.getMapping());
		result.setTableIndex(attribute.getTableIndex());
		return result;
	}

	// ================================================================================
	// helper methods
	// ================================================================================

	/** Resets the counters for the generated attribute names. */
	public static void resetNameCounters() {
		nameCounters.clear();
	}

	/** Creates a new unsused attribute name. */
	public static String createName() {
		return createName(GENSYM_PREFIX);
	}

	/** Creates a new unsused attribute name with a given prefix. */
	public static String createName(String prefix) {
        AtomicInteger counter = nameCounters.get(prefix);
        if (counter == null) {
            nameCounters.put(prefix, new AtomicInteger(1));
            return prefix;
        } else {
            return prefix + counter.getAndIncrement();   
        }
	}
}
