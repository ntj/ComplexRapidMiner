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
package com.rapidminer.operator.features.construction;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Example;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilter;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

/**
 * Generates a new attribute and sets the attributes values according to
 * the fulfilling of the specified conditions. Sets the attribute value
 * as the one which corresponds to the first matching condition.
 * 
 * @author Tobias Malbrecht
 * @version $Id: ConditionedFeatureGeneration.java,v 1.1 2008/08/08 15:27:31 tobiasmalbrecht Exp $
 */
public class ConditionedFeatureGeneration extends Operator {

	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public static final String PARAMETER_ATTRIBUTE_NAME = "attribute_name";
	
	public static final String PARAMETER_VALUE_TYPE = "value_type";
	
	public static final String PARAMETER_VALUES = "values";
	
	public static final String PARAMETER_CONDITIONS = "conditions";
	
	public static final String PARAMETER_DEFAULT_VALUE = "default_value";
	
	
	public ConditionedFeatureGeneration(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

		Attribute attribute = AttributeFactory.createAttribute(getParameterAsString(PARAMETER_ATTRIBUTE_NAME), getParameterAsInt(PARAMETER_VALUE_TYPE) + 1);

		double mappedDefaultValue = Double.NaN;
		String defaultValue = getParameterAsString(PARAMETER_DEFAULT_VALUE);
		if (!defaultValue.equals("?")) {
			if (attribute.isNominal()) {
				mappedDefaultValue = attribute.getMapping().mapString(defaultValue);
			} else {
				try {
					mappedDefaultValue = Double.parseDouble(defaultValue);
				} catch (NumberFormatException e) {
					logError("default value has to be ? or numerical for numerical attributes: no feature is generated");
					return new IOObject[] { exampleSet };
				}
			}
		}
		
		List valueConditionList = getParameterList(PARAMETER_VALUES);
		int numberOfValueConditions = valueConditionList.size();
		String[] values = new String[numberOfValueConditions];
		double[] mappedValues = new double[numberOfValueConditions];
		AttributeValueFilter[] filters = new AttributeValueFilter[numberOfValueConditions];
		Iterator iterator = valueConditionList.iterator();
		int j = 0;
		while (iterator.hasNext()) {
			Object[] pair = (Object[]) iterator.next();
			values[j] = (String) pair[0];
			if (values[j].equals("?")) {
				mappedValues[j] = Double.NaN;
			} else {
				if (attribute.isNominal()) {
					mappedValues[j] = attribute.getMapping().mapString(values[j]);
				} else {
					try {
						mappedValues[j] = Double.parseDouble(values[j]);
					} catch (NumberFormatException e) {
						logError("values have to be numerical for numerical attributes: no feature is generated");
						return new IOObject[] { exampleSet };
					}
				}
			}
			filters[j] = new AttributeValueFilter(exampleSet, (String) pair[1]);
			j++;
		}

		exampleSet.getExampleTable().addAttribute(attribute);
		exampleSet.getAttributes().addRegular(attribute);
		
		for (Example example : exampleSet) {
			example.setValue(attribute, mappedDefaultValue);
			for (int i = 0; i < numberOfValueConditions; i++) {
				AttributeValueFilter filter = filters[i];
				if (filter.conditionOk(example)) {
					example.setValue(attribute, mappedValues[i]);
					break;
				}
			}
		}
		
		exampleSet.recalculateAllAttributeStatistics();
		return new IOObject[] { exampleSet };
	}

	public Class<?>[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class<?>[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeString(PARAMETER_ATTRIBUTE_NAME, "Attribute name.", false);
		type.setExpert(false);
		types.add(type);
		String[] valueTypes = new String[Ontology.VALUE_TYPE_NAMES.length - 1];
		for (int i = 1; i < Ontology.VALUE_TYPE_NAMES.length; i++) {
			valueTypes[i - 1] = Ontology.VALUE_TYPE_NAMES[i];
		}
		type = new ParameterTypeCategory(PARAMETER_VALUE_TYPE, "Attribute value type.", valueTypes, 0);
		type.setExpert(false);
		types.add(type);
		ParameterType valueCondition = new ParameterTypeString(PARAMETER_CONDITIONS, "Value condition.", false);
		type = new ParameterTypeList(PARAMETER_VALUES, "Values and conditions.", valueCondition);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeString(PARAMETER_DEFAULT_VALUE, "Default value.", "?");
		type.setExpert(true);
		types.add(type);
		return types;
	}
}
