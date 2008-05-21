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
package com.rapidminer.operator.preprocessing.normalization;

import java.util.HashMap;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.preprocessing.PreprocessingOperator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.tools.Tupel;


/**
 * This operator performs a normalization. This can be done between a user
 * defined minimum and maximum value or by a z-transformation, i.e. on mean 0
 * and variance 1.
 * 
 * @author Ingo Mierswa
 * @version $Id: Normalization.java,v 1.9 2008/05/09 19:23:19 ingomierswa Exp $
 */
public class Normalization extends PreprocessingOperator {


	/** The parameter name for &quot;Determines whether to perform a z-transformation (mean 0 and variance 1) or not; this scaling ignores min- and max-setings&quot; */
	public static final String PARAMETER_Z_TRANSFORM = "z_transform";

	/** The parameter name for &quot;The minimum value after normalization&quot; */
	public static final String PARAMETER_MIN = "min";

	/** The parameter name for &quot;The maximum value after normalization&quot; */
	public static final String PARAMETER_MAX = "max";
	/** Creates a new Normalization operator. */
	public Normalization(OperatorDescription description) {
		super(description);
	}

	/**
	 * Depending on the parameter value of &quot;standardize&quot; this method
	 * creates either a ZTransformationModel or a MinMaxNormalizationModel.
	 */
	public Model createPreprocessingModel(ExampleSet exampleSet) throws OperatorException {
		if (getParameterAsBoolean(PARAMETER_Z_TRANSFORM)) {
			exampleSet.recalculateAllAttributeStatistics();
			HashMap<String, Tupel<Double, Double>> attributeMeanVarianceMap = new HashMap<String, Tupel<Double, Double>>(); 
			for (Attribute attribute : exampleSet.getAttributes()) {
				if (!attribute.isNominal()) {
					attributeMeanVarianceMap.put(attribute.getName(), new Tupel<Double, Double>( 
							exampleSet.getStatistics(attribute, Statistics.AVERAGE),
                            exampleSet.getStatistics(attribute, Statistics.VARIANCE)));
				}
			}
			ZTransformationModel model = new ZTransformationModel(exampleSet, attributeMeanVarianceMap);
			return model;
		} else {
			double min = getParameterAsDouble(PARAMETER_MIN);
			double max = getParameterAsDouble(PARAMETER_MAX);
			if (max <= min)
				throw new UserError(this, 116, "max", "Must be greater than 'min'");
			
			// calculating attribute ranges
			HashMap<String, Tupel<Double, Double>> attributeRanges = new HashMap<String, Tupel<Double, Double>>();
			exampleSet.recalculateAllAttributeStatistics();
			for (Attribute attribute : exampleSet.getAttributes()) {
				if (!attribute.isNominal()) {
					attributeRanges.put(attribute.getName(), new Tupel<Double, Double>(exampleSet.getStatistics(attribute, Statistics.MINIMUM), exampleSet.getStatistics(attribute, Statistics.MAXIMUM)));
				}
			}
			return new MinMaxNormalizationModel(exampleSet, min, max, attributeRanges);
		}
	}

	/** Returns a list with all parameter types of this model. */
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeBoolean(PARAMETER_Z_TRANSFORM, "Determines whether to perform a z-transformation (mean 0 and standard deviation 1) or not; this scaling ignores min- and max-setings", true);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeDouble(PARAMETER_MIN, "The minimum value after normalization", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d));
		types.add(new ParameterTypeDouble(PARAMETER_MAX, "The maximum value after normalization", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d));
		return types;
	}
}
