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

import java.util.List;

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
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


/**
 * This operator evaluates the performance of feature weighting algorithms
 * including feature selection. The first inner operator is the weighting
 * algorithm to be evaluated itself. It must return an attribute weights vector
 * which is applied on the data. Then a new model is created using the second
 * inner operator and a performance is retrieved using the third inner operator.
 * This performance vector serves as a performance indicator for the actual
 * algorithm.
 * 
 * This implementation is described for the {@link RandomSplitValidationChain}.
 * 
 * @author Ingo Mierswa
 * @version $Id: RandomSplitWrapperValidationChain.java,v 1.8 2006/04/05
 *          08:57:28 ingomierswa Exp $
 */
public class RandomSplitWrapperValidationChain extends WrapperValidationChain {

	public static final String PARAMETER_SPLIT_RATIO = "split_ratio";
	
	public static final String PARAMETER_SAMPLING_TYPE = "sampling_type";
	
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	public RandomSplitWrapperValidationChain(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		double splitRatio = getParameterAsDouble(PARAMETER_SPLIT_RATIO);
		SplittedExampleSet eSet = new SplittedExampleSet(getInput(ExampleSet.class), splitRatio, getParameterAsInt(PARAMETER_SAMPLING_TYPE), getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));

		eSet.selectSingleSubset(0);
		AttributeWeights weights = useMethod(eSet).remove(AttributeWeights.class);
		SplittedExampleSet newInputSet = (SplittedExampleSet) eSet.clone();

		// learn on the same data
		learn(new AttributeWeightedExampleSet(newInputSet, weights, 0.0d).createCleanClone());

		// testing
		newInputSet.selectSingleSubset(1);
		IOContainer evalRes = evaluate(new AttributeWeightedExampleSet(newInputSet, weights, 0.0d).createCleanClone());

		PerformanceVector pv = evalRes.remove(PerformanceVector.class);
		setResult(pv.getMainCriterion());

		return new IOObject[] { pv, weights };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_SPLIT_RATIO, "Relative size of the training set", 0, 1, 0.7);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeCategory(PARAMETER_SAMPLING_TYPE, "Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)", SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.STRATIFIED_SAMPLING));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
