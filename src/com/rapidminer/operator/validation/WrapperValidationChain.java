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

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.CombinedInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SpecificInnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;


/**
 * This operator evaluates the performance of feature weighting algorithms
 * including feature selection. The first inner operator is the algorithm to be
 * evaluated itself. It must return an attribute weights vector which is applied
 * on the data. The second operator is used to create a new model and a
 * performance vector is retrieved using the third inner operator. This
 * performance vector serves as a performance indicator for the actual
 * algorithm.
 * 
 * @author Ingo Mierswa
 * @version $Id: WrapperValidationChain.java,v 1.10 2006/03/21 15:35:52
 *          ingomierswa Exp $
 */
public abstract class WrapperValidationChain extends OperatorChain {

	private static final Class[] OUTPUT_CLASSES = { PerformanceVector.class, AttributeWeights.class };

	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private PerformanceCriterion lastPerformance;

	private IOContainer learnResult;

	private IOContainer methodResult;

	public WrapperValidationChain(OperatorDescription description) {
		super(description);
		addValue(new Value("performance", "The last performance (main criterion).") {

			public double getValue() {
				if (lastPerformance != null)
					return lastPerformance.getAverage();
				else
					return Double.NaN;
			}
		});
		addValue(new Value("variance", "The variance of the last performance (main criterion).") {

			public double getValue() {
				if (lastPerformance != null)
					return lastPerformance.getVariance();
				else
					return Double.NaN;
			}
		});
	}

	/** Returns the maximum number of innner operators. */
	public int getMaxNumberOfInnerOperators() {
		return 3;
	}

	/** Returns the minimum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 3;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		CombinedInnerOperatorCondition condition = new CombinedInnerOperatorCondition();
		condition.addCondition(new SpecificInnerOperatorCondition("Wrapper", 0, new Class[] { ExampleSet.class }, new Class[] { AttributeWeights.class }));
		condition.addCondition(new SpecificInnerOperatorCondition("Training", 1, new Class[] { ExampleSet.class }, new Class[] { Model.class }));
		condition.addCondition(new SpecificInnerOperatorCondition("Testing", 2, new Class[] { ExampleSet.class, Model.class }, new Class[] { PerformanceVector.class }));
		return condition;
	}

	private Operator getMethod() {
		return getOperator(0);
	}

	private Operator getLearner() {
		return getOperator(1);
	}

	private Operator getEvaluator() {
		return getOperator(2);
	}

	/**
	 * Can be used by subclasses to set the performance of the example set. Will
	 * be used for plotting only.
	 */
	void setResult(PerformanceCriterion pc) {
		lastPerformance = pc;
	}

	/** Applies the method. */
	IOContainer useMethod(ExampleSet methodTrainingSet) throws OperatorException {
		return methodResult = getMethod().apply(new IOContainer(new IOObject[] { methodTrainingSet }));
	}

	/** Applies the learner. */
	IOContainer learn(ExampleSet trainingSet) throws OperatorException {
		if (methodResult == null) {
			throw new RuntimeException("Wrong use of MethodEvaluator.evaluate(ExampleSet): No preceding invocation of useMethod(ExampleSet)!");
		}
		learnResult = getLearner().apply(new IOContainer(new IOObject[] { trainingSet }));
		methodResult = null;
		return learnResult;
	}

	/** Applies the applier and evaluator. */
	IOContainer evaluate(ExampleSet testSet) throws OperatorException {
		if (learnResult == null) {
			throw new RuntimeException("Wrong use of ValidationChain.evaluate(ExampleSet): No preceding invocation of learn(ExampleSet)!");
		}
		IOContainer result = getEvaluator().apply(learnResult.append(new IOObject[] { testSet }));
		learnResult = null;
		return result;
	}
}
