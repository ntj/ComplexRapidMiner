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
package com.rapidminer.operator.preprocessing.discretization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.preprocessing.PreprocessingOperator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.tools.Tupel;

/**
 * This operator discretizes a numerical attribute to either a nominal or an ordinal attribute. The numerical values are mapped to the classes according to the thresholds specified by the user. The user can define the classes by specifying the upper
 * limits of each class. The lower limit of the next class is automatically specified as the upper limit of the previous one. A parameter defines to which adjacent class values that are equal to the given limits should be mapped. If the upper limit
 * in the last list entry is not equal to Infinity, an additional class which is automatically named is added. If a '?' is given as class value the according numerical values are mapped to unknown values in the resulting attribute.
 * 
 * @author Sebastian Land
 * @version $Id: UserBasedDiscretization.java,v 1.8 2008/05/09 19:23:25 ingomierswa Exp $
 */
public class UserBasedDiscretization extends PreprocessingOperator {

	/** The parameter name for &quot;Attribute type of the discretized attribute.&quot; */
	public static final String PARAMETER_ATTRIBUTE_TYPE = "attribute_type";

	/** The parameter name for the upper limit. */
	public static final String PARAMETER_UPPER_LIMIT = "upper_limit";

	/** The parameter name for &quot;Defines the classes and the upper limits of each class.&quot; */
	public static final String PARAMETER_RANGE_NAMES = "classes";

	public static final String[] attributeTypeStrings = { "nominal", "ordinal" };

	public static final int ATTRIBUTE_TYPE_NOMINAL = 0;

	public static final int ATTRIBUTE_TYPE_ORDINAL = 1;

	public UserBasedDiscretization(OperatorDescription description) {
		super(description);
	}

	public Model createPreprocessingModel(ExampleSet exampleSet) throws OperatorException {
		HashMap<String, SortedSet<Tupel<Double, String>>> ranges = new HashMap<String, SortedSet<Tupel<Double, String>>>();
		List rangeList = getParameterList(PARAMETER_RANGE_NAMES);

		TreeSet<Tupel<Double, String>> thresholdPairs = new TreeSet<Tupel<Double, String>>();
		for (Object entry : rangeList) {
			Object[] pair = (Object[]) entry;
			thresholdPairs.add(new Tupel<Double, String>((Double) pair[1], (String) pair[0]));
		}
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (!attribute.isNominal()) {
				ranges.put(attribute.getName(), thresholdPairs);
			}
		}

		DiscretizationModel model = new DiscretizationModel(exampleSet);
		model.setRanges(ranges);
		return model;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = null;
		type = new ParameterTypeCategory(PARAMETER_ATTRIBUTE_TYPE, "Attribute type of the discretized attribute.", attributeTypeStrings, ATTRIBUTE_TYPE_NOMINAL);
		type.setExpert(false);
		types.add(type);
		ParameterType threshold = new ParameterTypeDouble(PARAMETER_UPPER_LIMIT, "The upper limit.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		List<Object> defaultList = new LinkedList<Object>();
		Object[] defaultListEntry = { "last", Double.POSITIVE_INFINITY };
		defaultList.add(defaultListEntry);
		type = new ParameterTypeList(PARAMETER_RANGE_NAMES, "Defines the classes and the upper limits of each class.", threshold, defaultList);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
