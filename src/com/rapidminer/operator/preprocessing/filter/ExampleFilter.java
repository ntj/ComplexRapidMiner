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

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionCreationException;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeStringCategory;


/**
 * This operator takes an {@link ExampleSet} as input and returns a new
 * {@link ExampleSet} including only the {@link Example}s that fulfill a
 * condition. <br/> By specifying an implementation of
 * {@link com.rapidminer.example.set.Condition} and a parameter string, arbitrary
 * filters can be applied. Users can implement their own conditions by writing a
 * subclass of the above class and implementing a two argument constructor
 * taking an {@link ExampleSet} and a parameter string. This parameter string is
 * specified by the parameter <code>parameter_string</code>. Instead of using
 * one of the predefined conditions users can define their own implementation
 * with the fully qualified class name. <br/> For
 * &quot;attribute_value_condition&quot; the parameter string must have the form
 * <code>attribute op value</code>, where attribute is a name of an
 * attribute, value is a value the attribute can take and op is one of the
 * binary logical operators similar to the ones known from Java, e.g. greater
 * than or equals. <br/> For &quot;unknown_attributes&quot; the parameter string
 * must be empty. This filter removes all examples containing attributes that
 * have missing or illegal values. For &quot;unknown_label&quot; the parameter
 * string must also be empty. This filter removes all examples with an unknown
 * label value.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ExampleFilter.java,v 1.6 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class ExampleFilter extends Operator {

	/** The parameter name for &quot;Implementation of the condition.&quot; */
	public static final String PARAMETER_CONDITION_CLASS = "condition_class";

	/** The parameter name for &quot;Parameter string for the condition, e.g. 'attribute=value' for the AttributeValueFilter.&quot; */
	public static final String PARAMETER_PARAMETER_STRING = "parameter_string";

	/** The parameter name for &quot;Indicates if only examples should be accepted which would normally filtered.&quot; */
	public static final String PARAMETER_INVERT_FILTER = "invert_filter";
	
	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public ExampleFilter(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet inputSet = getInput(ExampleSet.class);

		log(getName() + ": input set has " + inputSet.size() + " examples.");

		String className = getParameterAsString(PARAMETER_CONDITION_CLASS);
		String parameter = getParameterAsString(PARAMETER_PARAMETER_STRING);
		log("Creating condition '" + className + "' with parameter '" + parameter + "'");
		Condition condition = null;
		try {
			condition = ConditionedExampleSet.createCondition(className, inputSet, parameter);
		} catch (ConditionCreationException e) {
			throw new UserError(this, 904, className, e.getMessage());
		}
		ExampleSet result = new ConditionedExampleSet(inputSet, condition, getParameterAsBoolean(PARAMETER_INVERT_FILTER));
		return new IOObject[] { result };
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeStringCategory(PARAMETER_CONDITION_CLASS, "Implementation of the condition.", ConditionedExampleSet.KNOWN_CONDITION_NAMES, ConditionedExampleSet.KNOWN_CONDITION_NAMES[0]);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeString(PARAMETER_PARAMETER_STRING, "Parameter string for the condition, e.g. 'attribute=value' for the AttributeValueFilter.", true);
		type.setExpert(false);
		types.add(type);
        types.add(new ParameterTypeBoolean(PARAMETER_INVERT_FILTER, "Indicates if only examples should be accepted which would normally filtered.", false));
		return types;
	}
}
