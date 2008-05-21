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

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * This operator evaluates the performance of feature weighting and selection
 * algorithms. The first inner operator is the algorithm to be evaluated itself.
 * It must return an attribute weights vector which is applied on the test data.
 * This fold is used to create a new model using the second inner operator and
 * retrieve a performance vector using the third inner operator. This
 * performance vector serves as a performance indicator for the actual
 * algorithm. This implementation of a MethodValidationChain works similar to
 * the {@link XValidation}.
 * 
 * @see com.rapidminer.operator.validation.XValidation
 * @author Ingo Mierswa
 * @version $Id: WrapperXValidation.java,v 1.7 2006/04/05 08:57:28 ingomierswa
 *          Exp $
 */
public class WrapperXValidation extends WrapperValidationChain {


	/** The parameter name for &quot;Number of subsets for the crossvalidation&quot; */
	public static final String PARAMETER_NUMBER_OF_VALIDATIONS = "number_of_validations";

	/** The parameter name for &quot;Set the number of validations to the number of examples. If set to true, number_of_validations is ignored&quot; */
	public static final String PARAMETER_LEAVE_ONE_OUT = "leave_one_out";

	/** The parameter name for &quot;Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)&quot; */
	public static final String PARAMETER_SAMPLING_TYPE = "sampling_type";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	/** Total number of iterations. */
	private int number;

	/** Current iteration. */
	private int iteration;

	public WrapperXValidation(OperatorDescription description) {
		super(description);
		addValue(new Value("iteration", "The number of the current iteration.") {

			public double getValue() {
				return iteration;
			}
		});
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet eSet = getInput(ExampleSet.class);
		if (getParameterAsBoolean(PARAMETER_LEAVE_ONE_OUT)) {
			number = eSet.size();
		} else {
			number = getParameterAsInt(PARAMETER_NUMBER_OF_VALIDATIONS);
		}

		int samplingType = getParameterAsInt(PARAMETER_SAMPLING_TYPE);
        int randomSeed = getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED);
		SplittedExampleSet inputSet = new SplittedExampleSet(eSet, number, samplingType, randomSeed);
		log("Starting " + number + "-fold method cross validation");

		// statistics init
		PerformanceVector performanceVector = null;
		AttributeWeights globalWeights = new AttributeWeights();
		for (Attribute attribute : eSet.getAttributes()) {
			globalWeights.setWeight(attribute.getName(), 0.0d);
		}

		for (iteration = 0; iteration < number; iteration++) {

			// training
			inputSet.selectAllSubsetsBut(iteration);

			// apply method
			AttributeWeights weights = useMethod(inputSet).remove(AttributeWeights.class);
			SplittedExampleSet newInputSet = (SplittedExampleSet) inputSet.clone();

			// learn on the same data
			learn(new AttributeWeightedExampleSet(newInputSet, weights, 0.0d).createCleanClone());

			// testing
			newInputSet.selectSingleSubset(iteration);
			IOContainer evalOutput = evaluate(new AttributeWeightedExampleSet(newInputSet, weights, 0.0d).createCleanClone());

			// retrieve performance
			PerformanceVector iterationPerformance = evalOutput.remove(PerformanceVector.class);

			// build performance average
			if (performanceVector == null) {
				performanceVector = iterationPerformance;
			} else {
				for (int i = 0; i < performanceVector.size(); i++) {
					performanceVector.getCriterion(i).buildAverage(iterationPerformance.getCriterion(i));
				}
			}

			// build weights average
			handleWeights(globalWeights, weights);

			setResult(iterationPerformance.getMainCriterion());
			inApplyLoop();
		}
		// end of cross validation

		// build average of weights
		Iterator i = globalWeights.getAttributeNames().iterator();
		while (i.hasNext()) {
			String currentName = (String) i.next();
			globalWeights.setWeight(currentName, globalWeights.getWeight(currentName) / number);
		}

		setResult(performanceVector.getMainCriterion());
		return new IOObject[] { performanceVector, globalWeights };
	}

	private void handleWeights(AttributeWeights globalWeights, AttributeWeights currentWeights) {
		Iterator i = currentWeights.getAttributeNames().iterator();
		while (i.hasNext()) {
			String currentName = (String) i.next();
			double globalWeight = globalWeights.getWeight(currentName);
			double currentWeight = currentWeights.getWeight(currentName);
			if (Double.isNaN(globalWeight)) {
				globalWeights.setWeight(currentName, currentWeight);
			} else {
				globalWeights.setWeight(currentName, globalWeight + currentWeight);
			}
		}
	}

	public Class[] getOutputClasses() {
		return new Class[] { PerformanceVector.class, AttributeWeights.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_VALIDATIONS, "Number of subsets for the crossvalidation", 2, Integer.MAX_VALUE, 10);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_LEAVE_ONE_OUT, "Set the number of validations to the number of examples. If set to true, number_of_validations is ignored", false));
		types.add(new ParameterTypeCategory(PARAMETER_SAMPLING_TYPE, "Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)", SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.STRATIFIED_SAMPLING));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
