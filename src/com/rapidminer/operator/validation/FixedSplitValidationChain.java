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
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.math.AverageVector;


/**
 * A FixedSplitValidationChain splits up the example set at a fixed point into a
 * training and test set and evaluates the model (linear sampling). For
 * non-linear sampling methods, i.e. the data is shuffled, the specified amounts
 * of data are used as training and test set. The sum of both must be smaller
 * than the input example set size. <br/>
 * 
 * At least either the training set size must be specified (rest is used for
 * testing) or the test set size must be specified (rest is used for training).
 * If both are specified, the rest is not used at all.
 * 
 * The first inner operator must accept an
 * {@link com.rapidminer.example.ExampleSet} while the second must accept an
 * {@link com.rapidminer.example.ExampleSet} and the output of the first (which
 * in most cases is a {@link com.rapidminer.operator.Model}) and must produce
 * a {@link com.rapidminer.operator.performance.PerformanceVector}.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: FixedSplitValidationChain.java,v 1.18 2006/04/12 18:04:24
 *          ingomierswa Exp $
 */
public class FixedSplitValidationChain extends ValidationChain {

	public static final String PARAMETER_TRAINING_SET_SIZE = "training_set_size";
	
	public static final String PARAMETER_TEST_SET_SIZE = "test_set_size";
	
	public static final String PARAMETER_SAMPLING_TYPE = "sampling_type";
	
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	public FixedSplitValidationChain(OperatorDescription description) {
		super(description);
	}

	public IOObject[] estimatePerformance(ExampleSet inputSet) throws OperatorException {
		int trainingSetSize = getParameterAsInt(PARAMETER_TRAINING_SET_SIZE);
		int testSetSize = getParameterAsInt(PARAMETER_TEST_SET_SIZE);
		int inputSetSize = inputSet.size();
		if (inputSetSize < trainingSetSize + testSetSize) {
			throw new UserError(this, 110, (trainingSetSize + testSetSize) + " (" + trainingSetSize + " for training, " + testSetSize + " for testing)");
		}

		int rest = inputSetSize - (trainingSetSize + testSetSize);
		if ((trainingSetSize < 1) && (testSetSize < 1)) {
			throw new UserError(this, 116, "training_set_size / test_set_size", "either training_set_size or test_set_size or both must be greater than 1.");
		} else if (testSetSize < 1) {
			rest = 0;
			testSetSize = inputSetSize - trainingSetSize;
		} else if (trainingSetSize < 1) {
			rest = 0;
			trainingSetSize = inputSetSize - testSetSize;
		}
		log("Using " + trainingSetSize + " examples for learning and " + testSetSize + " examples for testing. " + rest + " examples are not used.");
		double[] ratios = new double[] { (double) trainingSetSize / (double) inputSetSize, (double) testSetSize / (double) inputSetSize, (double) rest / (double) inputSetSize };
		SplittedExampleSet eSet = new SplittedExampleSet(inputSet, ratios, getParameterAsInt(PARAMETER_SAMPLING_TYPE), getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));

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
		ParameterType type = new ParameterTypeInt(PARAMETER_TRAINING_SET_SIZE, "Absolute size required for the training set (-1: use rest for training)", -1, Integer.MAX_VALUE, 100);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_TEST_SET_SIZE, "Absolute size required for the test set (-1: use rest for testing)", -1, Integer.MAX_VALUE, -1);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeCategory(PARAMETER_SAMPLING_TYPE, "Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)", SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.SHUFFLED_SAMPLING));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
