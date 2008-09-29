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
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;

/**
 * This operator calculates the moving average for a specified numerical 
 * attribute and window width. The average value is placed at the end in order 
 * to ensure fair forecasting processes and to reduce total runtime.
 *  
 * @author Ingo Mierswa
 * @version $Id: MovingAverage.java,v 1.1 2008/09/10 18:26:23 ingomierswa Exp $
 */
public class MovingAverage extends Operator {

	public static final String PARAMETER_ATTRIBUTE_NAMES = "attribute_names";
	
	public static final String PARAMETER_WINDOW_WIDTH = "window_width";
	
	public static final int RESULT_POSITION_START  = 0;
	public static final int RESULT_POSITION_CENTER = 1;
	public static final int RESULT_POSITION_END    = 2;
	
	
	public MovingAverage(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		String nameRegEx = getParameterAsString(PARAMETER_ATTRIBUTE_NAMES);
		int windowWidth = getParameterAsInt(PARAMETER_WINDOW_WIDTH);
		
		for (Attribute attribute : exampleSet.getAttributes()) {
			if ((attribute.isNumerical()) && (attribute.getName().matches(nameRegEx))) {
				double[] lastValues = new double[windowWidth];
				int totalCounter = 0;
				int lastValuesCounter = 0;
				for (Example example : exampleSet) {
					if (totalCounter < windowWidth - 1) {
						lastValues[lastValuesCounter++] = example.getValue(attribute);
					} else {
						lastValues[lastValuesCounter] = example.getValue(attribute);
						double average = 0.0d;
						for (double v : lastValues)
							average += v;
						average /= windowWidth;
						example.setValue(attribute, average);
						
						lastValuesCounter++;
						lastValuesCounter = lastValuesCounter % windowWidth;
					}
					totalCounter++;
				}
			}
		}
		
		return new IOObject[] { exampleSet };
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_ATTRIBUTE_NAMES, "The attributes on which this operator should be applied (regular expressions are possible).", false));
		types.add(new ParameterTypeInt(PARAMETER_WINDOW_WIDTH, "Indicates the number of values which are taken into account.", 2, Integer.MAX_VALUE, 5));
		return types;
	}
}
