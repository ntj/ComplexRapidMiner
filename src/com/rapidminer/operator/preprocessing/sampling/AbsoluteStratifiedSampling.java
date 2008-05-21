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
package com.rapidminer.operator.preprocessing.sampling;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
/**
 * Stratified sampling operator. This operator performs a random sampling of a
 * given size. In contrast to the simple sampling operator, this operator
 * performs a stratified sampling for data sets with nominal label attributes,
 * i.e. the class distributions remains (almost) the same after sampling. Hence,
 * this operator cannot be applied on data sets without a label or with a
 * numerical label. In these cases a simple sampling without stratification
 * is performed. In some cases it might happen that not the exact desired number 
 * of examples is sampled, e.g. if the desired number is 100 from three qually distributed
 * classes the resulting number will be 99 (33 of each class).
 * 
 * @author Sebastian Land
 * @version $Id: AbsoluteStratifiedSampling.java,v 1.3 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class AbsoluteStratifiedSampling extends AbstractStratifiedSampling {

	/** The parameter name for &quot;The fraction of examples which should be sampled&quot; */
	public static final String PARAMETER_SAMPLE_SIZE = "sample_size";
	
	public AbsoluteStratifiedSampling(OperatorDescription description) {
		super(description);
	}

	public double getRatio(ExampleSet exampleSet) throws OperatorException{
		double targetSize = getParameterAsInt(PARAMETER_SAMPLE_SIZE);
		if (targetSize > exampleSet.size()) {
			return 1d;
	    } else {
			return targetSize / ((double) exampleSet.size());
		}
	}
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_SAMPLE_SIZE, "The number of examples which should be sampled", 1, Integer.MAX_VALUE, 100);
		type.setExpert(false);
		types.add(type);
		return types;
	}

}
