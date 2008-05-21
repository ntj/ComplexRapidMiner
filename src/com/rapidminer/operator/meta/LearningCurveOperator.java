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
package com.rapidminer.operator.meta;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.CombinedInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SpecificInnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * This operator first divides the input example set into two parts, a training set and a test set
 * according to the parameter &quot;training_ratio&quot;. It then uses iteratively bigger subsets 
 * from the fixed training set for learning (the first operator) and calculates the corresponding 
 * performance values on the fixed test set (with the second operator).
 * 
 * @author Ingo Mierswa
 * @version $Id: LearningCurveOperator.java,v 1.17 2006/04/05 09:42:02
 *          ingomierswa Exp $
 */
public class LearningCurveOperator extends OperatorChain {


	/** The parameter name for &quot;The fraction of examples which shall be maximal used for training (dynamically growing), the rest is used for testing (fixed)&quot; */
	public static final String PARAMETER_TRAINING_RATIO = "training_ratio";

	/** The parameter name for &quot;The fraction of examples which would be additionally used in each step.&quot; */
	public static final String PARAMETER_STEP_FRACTION = "step_fraction";

	/** The parameter name for &quot;Starts with this fraction of the training data and iteratively add step_fraction examples from the training data (-1: use step_fraction).&quot; */
	public static final String PARAMETER_START_FRACTION = "start_fraction";

	/** The parameter name for &quot;Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)&quot; */
	public static final String PARAMETER_SAMPLING_TYPE = "sampling_type";

	/** The parameter name for &quot;The local random seed for random number generation (-1: use global random generator).&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	private double lastFraction = Double.NaN;

	private double lastPerformance = Double.NaN;

	private double lastDeviation = Double.NaN;

	public LearningCurveOperator(OperatorDescription description) {
		super(description);
		addValue(new Value("fraction", "The used fraction of data.") {

			public double getValue() {
				return lastFraction;
			}
		});
		addValue(new Value("performance", "The last performance (main criterion).") {

			public double getValue() {
				return lastPerformance;
			}
		});
		addValue(new Value("deviation", "The variance of the last performance (main criterion).") {

			public double getValue() {
				return lastDeviation;
			}
		});
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet originalExampleSet = getInput(ExampleSet.class);
		double trainingRatio = getParameterAsDouble(PARAMETER_TRAINING_RATIO);
		double stepFraction = getParameterAsDouble(PARAMETER_STEP_FRACTION);
		double startFraction = getParameterAsDouble(PARAMETER_START_FRACTION);
		if (startFraction <= 0.0d)
			startFraction = stepFraction;
		int samplingType = getParameterAsInt(PARAMETER_SAMPLING_TYPE);
		int localSeed = getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED);
		
		// fix training and test set
		SplittedExampleSet trainTestSplittedExamples = new SplittedExampleSet(originalExampleSet, trainingRatio, samplingType, localSeed);
		trainTestSplittedExamples.selectSingleSubset(0);
		this.lastFraction = startFraction;
		while (lastFraction <= 1.0d) {
			// learns a model on the growing example set
			trainTestSplittedExamples.selectSingleSubset(0);
			SplittedExampleSet growingTrainingSet = new SplittedExampleSet(trainTestSplittedExamples, lastFraction, samplingType, localSeed);
			growingTrainingSet.selectSingleSubset(0);
			IOContainer input = new IOContainer(new IOObject[] { growingTrainingSet });
			input = getOperator(0).apply(input);
			
			// apply the learned model on the test set
			trainTestSplittedExamples.selectSingleSubset(1);
			input = input.append(trainTestSplittedExamples);
			for (int i = 1; i < getNumberOfOperators(); i++) {
				input = getOperator(i).apply(input);
			}

			PerformanceVector performance = input.remove(PerformanceVector.class);
			this.lastPerformance = performance.getMainCriterion().getAverage();
			this.lastDeviation = performance.getMainCriterion().getStandardDeviation();
			this.lastFraction += stepFraction;
			inApplyLoop();
		}
		return new IOObject[0];
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}

	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		CombinedInnerOperatorCondition condition = new CombinedInnerOperatorCondition();
		condition.addCondition(new SpecificInnerOperatorCondition("Training", 0, new Class[] { ExampleSet.class }, new Class[] { Model.class }));
		condition.addCondition(new SpecificInnerOperatorCondition("Testing", 1, new Class[] { ExampleSet.class, Model.class }, new Class[] { PerformanceVector.class }));
		return condition;
	}

	public List<ParameterType> getParameterTypes() { 
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_TRAINING_RATIO, "The fraction of examples which shall be maximal used for training (dynamically growing), the rest is used for testing (fixed)", 0.0d, 1.0d, 0.05); 
		type.setExpert(false); 
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_STEP_FRACTION, "The fraction of examples which would be additionally used in each step.", 0.0d, 1.0d, 0.05); 
		type.setExpert(false); 
		types.add(type);
		types.add(new ParameterTypeDouble(PARAMETER_START_FRACTION, "Starts with this fraction of the training data and iteratively add step_fraction examples from the training data (-1: use step_fraction).", -1.0d, 1.0d, -1.0d));
		types.add(new ParameterTypeCategory(PARAMETER_SAMPLING_TYPE, "Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)", SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.STRATIFIED_SAMPLING));
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "The local random seed for random number generation (-1: use global random generator).", -1, Integer.MAX_VALUE, -1));
		return types; 
	} 
}
