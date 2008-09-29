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

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.preprocessing.PreprocessingModel;
import com.rapidminer.operator.preprocessing.PreprocessingOperator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;

/**
 * This operator maps the values of all nominal values to binary attributes. For example,
 * if a nominal attribute with name &quot;costs&quot; and possible nominal values
 * &quot;low&quot;, &quot;moderate&quot;, and &quot;high&quot; is transformed, the result
 * is a set of three binominal attributes &quot;costs = low&quot;, &quot;costs = moderate&quot;,
 * and &quot;costs = high&quot;. Only one of the values of each attribute is true for a specific
 * example, the other values are false. 
 *
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: NominalToBinominal.java,v 1.7 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class NominalToBinominal extends PreprocessingOperator {

	public static final String PARAMETER_USE_UNDERSCORE_IN_NAME = "use_underscore_in_name";
	
	public static final String PARAMETER_TRANSFORM_BINOIMINAL = "transform_binominal";
	
	public NominalToBinominal(OperatorDescription description) {
		super(description);
	}

	public Model createPreprocessingModel(ExampleSet exampleSet) throws OperatorException {
		PreprocessingModel model = new NominalToBinominalModel(exampleSet, getParameterAsBoolean(PARAMETER_TRANSFORM_BINOIMINAL), getParameterAsBoolean(PARAMETER_USE_UNDERSCORE_IN_NAME));
		return model;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_TRANSFORM_BINOIMINAL, "Indicates if attributes which are already binominal should be transformed.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_UNDERSCORE_IN_NAME, "Indicates if underscores should be used in the new attribute names instead of empty spaces and '='. Although the resulting names are harder to read for humans it might be more appropriate to use these if the data should be written into a database system.", false));
		return types;
	}
}
