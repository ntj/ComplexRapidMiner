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
package com.rapidminer.operator.preprocessing.filter;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

/**
 * This operator merges two attributes by simply concatenating the values and store
 * those new values in a new attribute which will be nominal. If the resulting values
 * are actually numerical, you could simply change the value type afterwards with the
 * corresponding operators.
 *
 * @author Ingo Mierswa
 * @version $Id: AttributeMerge.java,v 1.2 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class AttributeMerge extends Operator {
	
	public static final String PARAMETER_FIRST_ATTRIBUTE = "first_attribute";
	
	public static final String PARAMETER_SECOND_ATTRIBUTE = "second_attribute";
	
	public static final String PARAMETER_SEPARATOR = "separator";
	
	
	public AttributeMerge(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		String firstAttributeName = getParameterAsString(PARAMETER_FIRST_ATTRIBUTE);
		String secondAttributeName = getParameterAsString(PARAMETER_SECOND_ATTRIBUTE);
		String separatorString = getParameterAsString(PARAMETER_SEPARATOR);
		
		Attribute firstAttribute = exampleSet.getAttributes().get(firstAttributeName);
		if (firstAttribute == null) {
			throw new UserError(this, 111, firstAttributeName);
		}
		
		Attribute secondAttribute = exampleSet.getAttributes().get(secondAttributeName);
		if (secondAttribute == null) {
			throw new UserError(this, 111, secondAttributeName);
		}
		
		Attribute mergedAttribute = AttributeFactory.createAttribute(firstAttribute.getName() + separatorString + secondAttribute.getName(), Ontology.NOMINAL);
		exampleSet.getExampleTable().addAttribute(mergedAttribute);
		exampleSet.getAttributes().addRegular(mergedAttribute);
		
		for (Example example : exampleSet) {
			double firstValue = example.getValue(firstAttribute);
			double secondValue = example.getValue(secondAttribute);
			
			if (Double.isNaN(firstValue) || Double.isNaN(secondValue)) {
				example.setValue(mergedAttribute, Double.NaN);
			} else {
				String firstValueString = example.getValueAsString(firstAttribute);
				String secondValueString = example.getValueAsString(secondAttribute);
				String mergedValueString = firstValueString + separatorString + secondValueString;
				double mergedValue = mergedAttribute.getMapping().mapString(mergedValueString);
				example.setValue(mergedAttribute, mergedValue);
			}
		}
		
		return new IOObject[] { exampleSet };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_FIRST_ATTRIBUTE, "The first attribute of this merger.", false));
		types.add(new ParameterTypeString(PARAMETER_SECOND_ATTRIBUTE, "The second attribute of this merger.", false));
		types.add(new ParameterTypeString(PARAMETER_SEPARATOR, "Indicated a string which is used as separation of both values.", "_"));
		return types;
	}
}
