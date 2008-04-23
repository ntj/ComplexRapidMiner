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
package com.rapidminer.operator.preprocessing.discretization;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

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
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.tools.Ontology;


/**
 * This operator discretizes a numerical attribute to either a nominal or
 * an ordinal attribute. The numerical values are mapped to the classes
 * according to the thresholds specified by the user. The user can define the
 * classes by specifying the upper limits of each class. The lower limit of the
 * next class is automatically specified as the upper limit of the previous one.
 * Hence, the upper limits must be given in ascending order. A parameter
 * defines to which adjacent class values that are equal to the given limits
 * should be mapped. If the upper limit in the last list entry is not equal to
 * Infinity, an additional class which is automatically named is added. If a '?'
 * is given as class value the according numerical values are mapped to unknown
 * values in the resulting attribute.
 * 
 * @author Tobias Malbrecht
 * @version $Id: UserBasedDiscretization.java,v 1.3 2007/06/15 16:58:38 ingomierswa Exp $
 */
public class UserBasedDiscretization extends Operator {


	/** The parameter name for &quot;Attribute type of the discretized attribute.&quot; */
	public static final String PARAMETER_ATTRIBUTE_TYPE = "attribute_type";

	/** The parameter name for &quot;&quot; */
	public static final String PARAMETER_UPPER_LIMIT = "upper_limit";

	/** The parameter name for &quot;Defines the classes and the upper limits of each class.&quot; */
	public static final String PARAMETER_CLASSES = "classes";

	/** The parameter name for &quot;Include the upper limits of the classes in the classes.&quot; */
	public static final String PARAMETER_INCLUDE_UPPER_LIMIT = "include_upper_limit";
	public static final String[] attributeTypeStrings = { "nominal", "ordinal" };
	public static final int ATTRIBUTE_TYPE_NOMINAL = 0;
	public static final int ATTRIBUTE_TYPE_ORDINAL = 1;
	
	private boolean includeUpperLimit = true;
	
	public UserBasedDiscretization(OperatorDescription description) {
		super(description);
	}
	
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		exampleSet.recalculateAllAttributeStatistics();
		List thresholdList = getParameterList(PARAMETER_CLASSES);
		includeUpperLimit = getParameterAsBoolean(PARAMETER_INCLUDE_UPPER_LIMIT);
		int attributeType = Ontology.NOMINAL;
		switch (getParameterAsInt(PARAMETER_ATTRIBUTE_TYPE)) {
		case ATTRIBUTE_TYPE_NOMINAL:
			attributeType = Ontology.NOMINAL;
			break;
		case ATTRIBUTE_TYPE_ORDINAL:
			attributeType = Ontology.ORDERED;
			break;
		}

		int numberOfClasses = thresholdList.size();
		
		Object[] thresholdPairs = new Object[thresholdList.size()];
		Iterator i = thresholdList.iterator();
		int j = 0;
		Double lastValue = new Double(Double.NEGATIVE_INFINITY);
		while (i.hasNext()) {
			Object[] pair = (Object[]) i.next();
			thresholdPairs[j] = pair;
			Double currValue = (Double) pair[1];
			if (Double.compare(currValue, lastValue) < 0) {
				throw new UserError(this, 927);
			}
			lastValue = currValue;
			j++;
		}
		
		if (!lastValue.equals(Double.POSITIVE_INFINITY)) {
			numberOfClasses++;
		}

		String[] values = new String[numberOfClasses];
		double[] limits = new double[numberOfClasses];
		for (int b = 0; b < thresholdPairs.length; b++) {
			Object[] pair = (Object[]) thresholdPairs[b];
			limits[b] = Double.valueOf((Double) pair[1]);
			values[b] = (String) pair[0];
			if (values[b].equals("")) {
				values[b] = "range" + (b+1);
			}
		}
		
		if (!lastValue.equals(Double.POSITIVE_INFINITY)) {
			limits[numberOfClasses - 1] = Double.POSITIVE_INFINITY;
			values[numberOfClasses - 1] = "range" + numberOfClasses;
		}
		
		boolean[] numerical = new boolean[exampleSet.getAttributes().size()]; // needed since value type is changed!
		int a = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (!attribute.isNominal()) {
				numerical[a] = true;
				attribute = exampleSet.getAttributes().replace(attribute, AttributeFactory.changeValueType(attribute, attributeType));
				for (int b = 0; b < numberOfClasses; b++) {
					if (!values[b].equals("?")) {
						attribute.getMapping().mapString(values[b]);
					}
				}
			} else {
				numerical[a] = false;
			}
			a++;
		}
		
		// change data
		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			a = 0;
			for (Attribute attribute : exampleSet.getAttributes()) {
				if (numerical[a]) {
					double value = example.getValue(attribute);
					if (Double.isNaN(value)) {
						example.setValue(attribute, Double.NaN);
					} else {
						for (int b = 0; b < limits.length; b++) {
							if (value < limits[b] || (includeUpperLimit && value == limits[b])) {
								if (values[b].equals("?")) {
									example.setValue(attribute, Double.NaN);
								} else {
									example.setValue(attribute, attribute.getMapping().mapString(values[b]));
								}
								break;
							}
						}
					}
				}
				a++;
			}
		}
		return new IOObject[] { exampleSet };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = null; 
		type = new ParameterTypeCategory(PARAMETER_ATTRIBUTE_TYPE, "Attribute type of the discretized attribute.", attributeTypeStrings, ATTRIBUTE_TYPE_NOMINAL);
		type.setExpert(false);
		types.add(type);
		ParameterType threshold = new ParameterTypeDouble(PARAMETER_UPPER_LIMIT, "", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		List<Object> defaultList = new LinkedList<Object>();
		Object[] defaultListEntry = { "last", new Double(Double.POSITIVE_INFINITY) };
		defaultList.add(defaultListEntry);
		type = new ParameterTypeList(PARAMETER_CLASSES, "Defines the classes and the upper limits of each class.", threshold, defaultList);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeBoolean(PARAMETER_INCLUDE_UPPER_LIMIT, "Include the upper limits of the classes in the classes.", true);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
