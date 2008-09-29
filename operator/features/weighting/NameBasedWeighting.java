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
package com.rapidminer.operator.features.weighting;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.features.weighting.AbstractWeighting;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeList;

/**
 * <p>This operator is able to create feature weights based on regular expressions defined
 * for the feature names. For example, the user can map all features with a name starting
 * with "Att" to the weight 0.5 by using the regular expression "Att.*". All other feature 
 * weights whose feature names are not covered by one of the regular expressions are 
 * set to the default weight.</p>
 * 
 * <p>Please note that the weights defined in the regular expression list are set in the order
 * as they are defined in the list, i.e. weights can overwrite weights set before.</p> 
 * 
 * @author Thomas Beckers, Ingo Mierswa
 * @version $Id: NameBasedWeighting.java,v 1.2 2008/07/13 16:39:42 ingomierswa Exp $
 */
public class NameBasedWeighting extends AbstractWeighting {

	public static final String PARAMETER_ATTRIBUTE_NAME_REGEX = "name_regex_to_weights";

	public static final String PARAMETER_DEFAULT_WEIGHT = "default_weight";
	
	
	public NameBasedWeighting(OperatorDescription description) {
		super(description);
	}

	public AttributeWeights calculateWeights(ExampleSet exampleSet) throws OperatorException {
		// init all weights with the default weight
		double defaultWeight = getParameterAsDouble(PARAMETER_DEFAULT_WEIGHT);
		AttributeWeights attributeWeights = new AttributeWeights();
		for (Attribute attribute : exampleSet.getAttributes()) {
			attributeWeights.setWeight(attribute.getName(), defaultWeight);
		}

		List<?> parameterList = getParameterList(PARAMETER_ATTRIBUTE_NAME_REGEX);
		Iterator<?> i = parameterList.iterator();
		while (i.hasNext()) {
			Object[] entry = (Object[])i.next();
			String regex = (String) entry[0];
			Double weighting = (Double) entry[1];
			if ((regex != null) && (weighting != null)) {
				try {
					Pattern pattern = Pattern.compile(regex);
					for (Attribute attribute : exampleSet.getAttributes()) {
						String attributeName = attribute.getName();
						if (pattern.matcher(attributeName).matches()) {
							attributeWeights.setWeight(attributeName, weighting);
						}
					}
				} catch (PatternSyntaxException e) {
					throw new UserError(this, 206, regex, e);
				}
			}
		}
		return attributeWeights;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeList(PARAMETER_ATTRIBUTE_NAME_REGEX, "This list maps different regular expressions for the feature names to the specified weights.", new ParameterTypeDouble("weight", "The new weight for all attributes with a name fulfilling the specified regular expression.", Double.NEGATIVE_INFINITY, Double.MAX_VALUE, false));
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeDouble(PARAMETER_DEFAULT_WEIGHT, "This default weight is used for all features not covered by any of the regular expressions given in the list.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d));
		return types;
	}
}
