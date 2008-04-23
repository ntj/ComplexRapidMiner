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

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * An example filter that discretizes all numeric attributes in the dataset into
 * nominal attributes. This discretization is performed by simple binning. Skips
 * all special attributes including the label.
 * 
 * @author Ingo Mierswa, Buelent Moeller
 * @version $Id: SimpleBinDiscretization.java,v 1.9 2006/04/05 08:57:27
 *          ingomierswa Exp $
 */
public class SimpleBinDiscretization extends Discretization {

	public static final String NUMBER_OF_BINS = "number_of_bins";
	
	public SimpleBinDiscretization(OperatorDescription description) {
		super(description);
	}

	public double[][] getRanges(ExampleSet exampleSet) throws UndefinedParameterError {
		int numberOfBins = getParameterAsInt(NUMBER_OF_BINS);
		double[][] ranges = new double[exampleSet.getAttributes().size()][numberOfBins];

		int a = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (!attribute.isNominal()) { // skip nominal attributes 
				double min = exampleSet.getStatistics(attribute, Statistics.MINIMUM);
                double max = exampleSet.getStatistics(attribute, Statistics.MAXIMUM);
				for (int b = 0; b < numberOfBins; b++) {
					ranges[a][b] = min + (((double) (b + 1) / (double) numberOfBins) * (max - min));
				}
			}
			a++;
		}
		return ranges;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(NUMBER_OF_BINS, "Defines the number of bins which should be used for each attribute.", 2, Integer.MAX_VALUE, 2);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
