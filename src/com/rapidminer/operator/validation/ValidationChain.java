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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.CombinedInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SpecificInnerOperatorCondition;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;


/**
 * Abstract superclass of operator chains that split an {@link ExampleSet} into
 * a training and test set and return a performance vector. The two inner
 * operators must be a learner returning a {@link Model} and an operator or
 * operator chain that can apply this model and returns a
 * {@link PerformanceVector}. Hence the second inner operator usually is an
 * operator chain containing a model applier and a performance evaluator.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ValidationChain.java,v 1.9 2008/05/09 19:22:54 ingomierswa Exp $
 */
public abstract class ValidationChain extends OperatorChain {

	/** The parameter name for &quot;Indicates if a model of the complete data set should be additionally build after estimation.&quot; */
	public static final String PARAMETER_CREATE_COMPLETE_MODEL = "create_complete_model";
	
	private PerformanceCriterion lastPerformance;

	private IOContainer learnResult;

	public ValidationChain(OperatorDescription description) {
		super(description);

		addValue(new Value("performance", "The last performance average (main criterion).") {

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
		addValue(new Value("deviation", "The standard deviation of the last performance (main criterion).") {

			public double getValue() {
				if (lastPerformance != null)
					return lastPerformance.getStandardDeviation();
				else
					return Double.NaN;
			}
		});
	}

	/**
	 * This is the main method of the validation chain and must be implemented
	 * to estimate a performance of inner operators on the given example set.
	 * The implementation can make use of the provided helper methods in this
	 * class.
	 */
	public abstract IOObject[] estimatePerformance(ExampleSet inputSet) throws OperatorException;

	/** Returns the maximum number of innner operators. */
	public int getMaxNumberOfInnerOperators() {
		return 2;
	}

	/** Returns the minimum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 2;
	}

	public InputDescription getInputDescription(Class cls) {
		if (ExampleSet.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	/** Returns the the classes this operator provides as output. */
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	/** Returns the the classes this operator expects as input. */
	public Class[] getOutputClasses() {
		if (getParameterAsBoolean(PARAMETER_CREATE_COMPLETE_MODEL)) {
			return new Class[] { PerformanceVector.class, Model.class };
		} else {
			return new Class[] { PerformanceVector.class };
		}
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		CombinedInnerOperatorCondition condition = new CombinedInnerOperatorCondition();
		condition.addCondition(new SpecificInnerOperatorCondition("Training", 0, new Class[] { ExampleSet.class }, new Class[] { Model.class }));
		condition.addCondition(new SpecificInnerOperatorCondition("Testing", 1, new Class[] { ExampleSet.class, Model.class }, new Class[] { PerformanceVector.class }));
		return condition;
	}

	/**
	 * Returns the first encapsulated inner operator (or operator chain), i.e.
	 * the learning operator (chain).
	 */
	protected Operator getLearner() {
		return getOperator(0);
	}

	/**
	 * Returns the second encapsulated inner operator (or operator chain), i.e.
	 * the application and evaluation operator (chain)
	 */
	private Operator getEvaluator() {
		return getOperator(1);
	}

	/** Can be used by subclasses to set the performance of the example set. */
	protected void setResult(PerformanceCriterion pc) {
		lastPerformance = pc;
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet eSet = getInput(ExampleSet.class);
		IOObject[] estimation = estimatePerformance(eSet);
		IOObject[] result = estimation;
		if (getParameterAsBoolean(PARAMETER_CREATE_COMPLETE_MODEL)) {
			Model model = learn(eSet).get(Model.class);
			result = new IOObject[estimation.length + 1];
			System.arraycopy(estimation, 0, result, 0, estimation.length);
			result[result.length - 1] = model;
		}
		return result;
	}

	/** Applies the learner (= first encapsulated inner operator). */
	protected IOContainer learn(ExampleSet trainingSet) throws OperatorException {
		return learnResult = getLearner().apply(new IOContainer(new IOObject[] { trainingSet }));
	}

	/**
	 * Applies the applier and evaluator (= second encapsulated inner operator).
	 * In order to reuse possibly created predicted label attributes, we do the
	 * following: We compare the predicted label of <code>testSet</code>
	 * before and after applying the inner operator. If it changed, the
	 * predicted label is removed again. No outer operator could ever see it.
	 * The same applies for the confidence attributes in case of classification
	 * learning.
	 */
	protected IOContainer evaluate(ExampleSet testSet, IOContainer learnResult) throws OperatorException {
		if (learnResult == null) {
			throw new RuntimeException("Wrong use of ValidationChain.evaluate(ExampleSet): " + "No preceding invocation of learn(ExampleSet)!");
		}
		Attribute predictedBefore = testSet.getAttributes().getPredictedLabel();
		IOContainer evalInput = learnResult.append(new IOObject[] { testSet });
		IOContainer result = getEvaluator().apply(evalInput);
		Attribute predictedAfter = testSet.getAttributes().getPredictedLabel();

		// remove predicted label and confidence attributes if there is a new prediction which is not equal to an old one
		if ((predictedAfter != null) && ((predictedBefore == null) || (predictedBefore.getTableIndex() != predictedAfter.getTableIndex()))) {
			PredictionModel.removePredictedLabel(testSet);
		}
		return result;
	}
	
	/**
	 * Applies the applier and evaluator (= second encapsulated inner operator).
	 * In order to reuse possibly created predicted label attributes, we do the
	 * following: We compare the predicted label of <code>testSet</code>
	 * before and after applying the inner operator. If it changed, the
	 * predicted label is removed again. No outer operator could ever see it.
	 * The same applies for the confidence attributes in case of classification
	 * learning.
	 */
	protected IOContainer evaluate(ExampleSet testSet) throws OperatorException {
		if (learnResult == null) {
			throw new RuntimeException("Wrong use of ValidationChain.evaluate(ExampleSet): " + "No preceding invocation of learn(ExampleSet)!");
		}
		IOContainer result = evaluate(testSet, learnResult);
		learnResult = null;
		return result;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeBoolean(PARAMETER_CREATE_COMPLETE_MODEL, "Indicates if a model of the complete data set should be additionally build after estimation.", false);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
