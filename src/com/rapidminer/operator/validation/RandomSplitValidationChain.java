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
package com.rapidminer.operator.validation;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.math.AverageVector;


/**
 * A <code>RandomSplitValidationChain</code> splits up the example set into a
 * training and test set and evaluates the model. The first inner operator must
 * accept an {@link com.rapidminer.example.ExampleSet} while the second must
 * accept an {@link com.rapidminer.example.ExampleSet} and the output of the
 * first (which is in most cases a {@link com.rapidminer.operator.Model}) and
 * must produce a {@link com.rapidminer.operator.performance.PerformanceVector}.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: RandomSplitValidationChain.java,v 1.17 2006/04/12 18:04:24
 *          ingomierswa Exp $
 */
public class RandomSplitValidationChain extends ValidationChain {

	public static final String PARAMETER_SPLIT_RATIO = "split_ratio";
	
	public static final String PARAMETER_SAMPLING_TYPE = "sampling_type";
	
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	public RandomSplitValidationChain(OperatorDescription description) {
		super(description);
	}

	public IOObject[] estimatePerformance(ExampleSet inputSet) throws OperatorException {
		double splitRatio = getParameterAsDouble(PARAMETER_SPLIT_RATIO);
		SplittedExampleSet eSet = new SplittedExampleSet(inputSet, splitRatio, getParameterAsInt(PARAMETER_SAMPLING_TYPE), getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));

		eSet.selectSingleSubset(0);
		learn(eSet);
		eSet.selectSingleSubset(1);
		IOContainer evalRes = evaluate(eSet);
		List<AverageVector> averageVectors = new LinkedList<AverageVector>();
		Tools.handleAverages(evalRes, averageVectors);
		PerformanceVector performanceVector = Tools.getPerformanceVector(averageVectors);
		if (performanceVector != null)
			setResult(performanceVector.getMainCriterion());

		AverageVector[] result = new AverageVector[averageVectors.size()];
		averageVectors.toArray(result);
		return result;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_SPLIT_RATIO, "Relative size of the training set", 0.0d, 1.0d, 0.7d);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeCategory(PARAMETER_SAMPLING_TYPE, "Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)", SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.STRATIFIED_SAMPLING));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
