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
package com.rapidminer.operator.features.selection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;


/**
 * Removes useless attribute from the example set. Useless attributes are
 * <ul>
 * <li>nominal attributes which has the same value for more than <code>p</code>
 * percent of all examples.</li>
 * <li>numerical attributes which standard deviation is less or equal to a
 * given deviation threshold <code>t</code>.</li>
 * </ul>
 * 
 * @author Ingo Mierswa
 * @version $Id: RemoveUselessFeatures.java,v 1.12 2006/04/05 08:57:27
 *          ingomierswa Exp $
 */
public class RemoveUselessFeatures extends Operator {

	/** The parameter name for &quot;Removes all numerical attributes with standard deviation less or equal to this threshold.&quot; */
	public static final String PARAMETER_NUMERICAL_MIN_DEVIATION = "numerical_min_deviation";

	/** The parameter name for &quot;Removes all nominal attributes which provides more than the given amount of only one value.&quot; */
	public static final String PARAMETER_NOMINAL_SINGLE_VALUE_UPPER = "nominal_single_value_upper";

	/** The parameter name for &quot;Removes all nominal attributes which provides less than the given amount of at least one value (-1: remove attributes with values occuring only once).&quot; */
	public static final String PARAMETER_NOMINAL_SINGLE_VALUE_LOWER = "nominal_single_value_lower";
    
	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public RemoveUselessFeatures(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		ExampleSet clone = (ExampleSet) exampleSet.clone();
		clone.recalculateAllAttributeStatistics();

		double numericalMinDeviation = getParameterAsDouble(PARAMETER_NUMERICAL_MIN_DEVIATION);
		double nominalSingleValueUpper = getParameterAsDouble(PARAMETER_NOMINAL_SINGLE_VALUE_UPPER);
		double nominalSingleValueLower = getParameterAsDouble(PARAMETER_NOMINAL_SINGLE_VALUE_LOWER);

		if (nominalSingleValueLower < 0.0d) {
			nominalSingleValueLower = 1.0d / clone.size();
		}

		Iterator<Attribute> i = clone.getAttributes().iterator();
		while (i.hasNext()) {
			Attribute attribute = i.next();

			if (attribute.isNominal()) {
				Collection values = attribute.getMapping().getValues();
				double[] valueCounts = new double[values.size()];
				Iterator v = values.iterator();
				int n = 0;
				while (v.hasNext()) {
					String value = (String) v.next();
					valueCounts[n] = clone.getStatistics(attribute, Statistics.COUNT, value);
					n++;
				}

				if (clone.getStatistics(attribute, Statistics.UNKNOWN) / clone.size() >= nominalSingleValueUpper) {
					i.remove();
					continue;
				}
				
				// check for single values which dominates other values and
				// calculate maximum
				double maximumValueCount = Double.NEGATIVE_INFINITY;
				for (n = 0; n < valueCounts.length; n++) {
					double percent = valueCounts[n] / clone.size();
					maximumValueCount = Math.max(maximumValueCount, percent);
					if (percent >= nominalSingleValueUpper) {
						i.remove();
						break;
					}
				}
				// check if the maximum is below lower bound to remove widely
				// spreaded attributes
				if (maximumValueCount <= nominalSingleValueLower) {
					i.remove();
					continue;
				}
			} else if (attribute.isNumerical()) {
				if (clone.getStatistics(attribute, Statistics.UNKNOWN) / clone.size() >= nominalSingleValueUpper) {
					i.remove();
					continue;
				}
				
				// remove numerical attribute with low deviation
				if (Math.sqrt(clone.getStatistics(attribute, Statistics.VARIANCE)) <= numericalMinDeviation)
					i.remove();
			} else {
				// do nothing for data attributes
				log("Attribute '" + attribute.getName() + "' is not numerical and not nominal, do nothing...");
			}
			checkForStop();
		}

		if (clone.getAttributes().size() <= 0) {
			logWarning("Example set does not not have any attribute after removing the useless attributes!");
		}

		return new IOObject[] { clone };
	}

	public Class<?>[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class<?>[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_NUMERICAL_MIN_DEVIATION, "Removes all numerical attributes with standard deviation less or equal to this threshold.", 0.0d, Double.POSITIVE_INFINITY, 0.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_NOMINAL_SINGLE_VALUE_UPPER, "Removes all nominal attributes which provides more than the given amount of only one value.", 0.0d, 1.0d, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_NOMINAL_SINGLE_VALUE_LOWER, "Removes all nominal attributes which provides less than the given amount of at least one value (-1: remove attributes with values occuring only once).", -1.0d, 1.0d, -1.0d);
		type.setExpert(false);
		types.add(type);
		return types;
	}

}
