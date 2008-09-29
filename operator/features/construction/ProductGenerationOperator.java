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

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

/**
 * This operator creates all products of the specified attributes. The attribute
 * names can be specified by regular expressions.
 * 
 * @author Ingo Mierswa
 * @version $Id: ProductGenerationOperator.java,v 1.1 2008/09/04 17:54:08 ingomierswa Exp $
 */
public class ProductGenerationOperator extends Operator {

	public static final String PARAMETER_FIRST_ATTRIBUTE_NAME = "first_attribute_name";
	
	public static final String PARAMETER_SECOND_ATTRIBUTE_NAME = "second_attribute_name";
	
	public ProductGenerationOperator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		List<Attribute> newAttributes = new LinkedList<Attribute>();
		String firstAttributeName = getParameterAsString(PARAMETER_FIRST_ATTRIBUTE_NAME);
		String secondAttributeName = getParameterAsString(PARAMETER_SECOND_ATTRIBUTE_NAME);
		
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attribute.isNumerical()) {
				if (attribute.getName().matches(firstAttributeName)) {
					for (Attribute attribute2 : exampleSet.getAttributes()) {
						if (attribute2.isNumerical()) {
							if (attribute2.getName().matches(secondAttributeName)) {
								newAttributes.add(createAttribute(exampleSet, attribute, attribute2));
							}
						}
					}
				}
			}
		}
		
		for (Attribute attribute : newAttributes) {
			exampleSet.getAttributes().addRegular(attribute);
		}
		
		return new IOObject[] { exampleSet };
	}

	private Attribute createAttribute(ExampleSet exampleSet, Attribute attribute1, Attribute attribute2) {
		Attribute result = AttributeFactory.createAttribute(AttributeFactory.createName("(" + attribute1.getConstruction().toString() + ") * (" + attribute2.getConstruction().toString() + ")"), Ontology.REAL);
		
		exampleSet.getExampleTable().addAttribute(result);
	
		for (Example example : exampleSet) {
			double value1 = example.getValue(attribute1);
			double value2 = example.getValue(attribute2);
			double resultValue = value1 * value2;
			example.setValue(result, resultValue);
		}
		
		return result;
	}
	
	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		
		types.add(new ParameterTypeString(PARAMETER_FIRST_ATTRIBUTE_NAME, "The name(s) of the first attribute to be multiplied (regular expression possible).", false));
		types.add(new ParameterTypeString(PARAMETER_SECOND_ATTRIBUTE_NAME, "The name(s) of the second attribute to be multiplied (regular expression possible).", false));
		
		return types;
	}
}
