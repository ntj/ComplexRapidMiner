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
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.preprocessing.PreprocessingOperator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;

/**
 * This operator discretizes all numeric attributes in the dataset into nominal attributes. This discretization is performed by simple binning, i.e. the specified number of equally sized bins is created and the numerical values are simply sorted into
 * those bins. Skips all special attributes including the label.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: BinDiscretization.java,v 1.6 2008/05/09 19:23:25 ingomierswa Exp $
 */
public class BinDiscretization extends PreprocessingOperator {
	
	/** Indicates the number of used bins. */
	public static final String PARAMETER_NUMBER_OF_BINS = "number_of_bins";

	/** Indicates if long range names should be used. */
	public static final String PARAMETER_USE_LONG_RANGE_NAMES = "use_long_range_names";
	
	public BinDiscretization(OperatorDescription description) {
		super(description);
	}

	public Model createPreprocessingModel(ExampleSet exampleSet) throws OperatorException {
		DiscretizationModel model = new DiscretizationModel(exampleSet);

		exampleSet.recalculateAllAttributeStatistics();
		int numberOfBins = getParameterAsInt(PARAMETER_NUMBER_OF_BINS);
		HashMap<Attribute, double[]> ranges = new HashMap<Attribute, double[]>();

		for (Attribute attribute : exampleSet.getAttributes()) {
			if (!attribute.isNominal()) { // skip nominal attributes
				double[] binRange = new double[numberOfBins];
				double min = exampleSet.getStatistics(attribute, Statistics.MINIMUM);
				double max = exampleSet.getStatistics(attribute, Statistics.MAXIMUM);
				for (int b = 0; b < numberOfBins - 1; b++) {
					binRange[b] = min + (((double) (b + 1) / (double) numberOfBins) * (max - min));
				}
				binRange[numberOfBins - 1] = Double.POSITIVE_INFINITY;
				ranges.put(attribute, binRange);
			}
		}
		model.setRanges(ranges, "range", getParameterAsBoolean(PARAMETER_USE_LONG_RANGE_NAMES));
		return (model);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_BINS, "Defines the number of bins which should be used for each attribute.", 2, Integer.MAX_VALUE, 2);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_USE_LONG_RANGE_NAMES, "Indicates if long range names including the limits should be used.", true));
		return types;
	}
}
