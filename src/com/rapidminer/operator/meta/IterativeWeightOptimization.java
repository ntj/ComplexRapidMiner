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

import java.util.Arrays;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeNumber;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * Performs an iterative feature selection guided by the AttributeWeights. Its
 * an backward feature elemination where the feature with the smallest weight
 * value is removed. After each iteration the weight values are updated (e.g. by
 * a learner like JMySVMLearner).
 * 
 * @author Daniel Hakenjos
 * @version $Id: IterativeWeightOptimization.java,v 1.3 2006/04/05 08:57:26
 *          ingomierswa Exp $
 */
public class IterativeWeightOptimization extends OperatorChain {


	/** The parameter name for &quot;The parameter to set the weight value&quot; */
	public static final String PARAMETER_PARAMETER = "parameter";

	/** The parameter name for &quot;The minimum difference between two weights.&quot; */
	public static final String PARAMETER_MIN_DIFF = "min_diff";

	/** The parameter name for &quot;Number iterations without performance improvement.&quot; */
	public static final String PARAMETER_ITERATIONS_WITHOUT_IMPROVEMENT = "iterations_without_improvement";
	private static final Class[] INPUT_CLASSES = { ExampleSet.class, AttributeWeights.class };

	private static final Class[] OUTPUT_CLASSES = { AttributeWeights.class, PerformanceVector.class };

	private PerformanceVector best;

	private String[] names;

	private double[] weights;

	private double currentweight, lastperf;

	// The Operator to set the weight
	private Operator operator;

	// the parameter of the operator
	private String parameter;

	// the minimum difference between two weights
	private double min_diff;

	private AttributeWeights bestweights;

	private AttributeWeights currentweights;

	/**
	 * @param description
	 */
	public IterativeWeightOptimization(OperatorDescription description) {
		super(description);
		addValue(new Value("performance", "performance of the last evaluated weight") {

			public double getValue() {
				return lastperf;
			}
		});
		addValue(new Value("best_performance", "best performance") {

			public double getValue() {
				if (best != null)
					return best.getMainCriterion().getAverage();
				else
					return Double.NaN;
			}
		});
	}

	public IOObject[] apply() throws OperatorException {

		IOContainer input = getInput();
		ExampleSet exampleSet = (ExampleSet) input.get(ExampleSet.class).clone();

		currentweights = input.get(AttributeWeights.class);
		names = new String[exampleSet.getAttributes().size()];
		int index = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			names[index] = attribute.getName();
			if (Double.isNaN(currentweights.getWeight(names[index]))) {
				throw new OperatorException("The AttributeWeights don't match with the ExampleSet.");
			}
			index++;
		}

		bestweights = (AttributeWeights) currentweights.clone();

		getParametersToOptimize();
		operator.getParameters().setParameter("weight_relation", "greater");
		lastperf = Double.NaN;

		weights = new double[names.length];
		for (int i = 0; i < names.length; i++) {
			weights[i] = Math.abs(currentweights.getWeight(names[i]));
		}
		Arrays.sort(weights);

		int nullindex = 0;
		currentweight = 0.0d;

		best = null;
		IOContainer container;
		PerformanceVector performance;
		boolean lastiteration = false;
		int not_zero;
		int iter = 0;
		int max_iter_without_improvement = getParameterAsInt(PARAMETER_ITERATIONS_WITHOUT_IMPROVEMENT);
		int iter_without_improvement = 0;

		while (true) {
			iter++;
			not_zero = 0;
			for (int i = 0; i < names.length; i++) {
				if (Math.abs(currentweights.getWeight(names[i])) > currentweight)
					not_zero++;
			}
			if (currentweight == 0.0d) {
				not_zero = names.length;
			}
			if (not_zero == 0) {
				log("Stopped after " + iter + " iterations. No attributes left.");
				break;
			}

			// set the weight
			operator.getParameters().setParameter(parameter, Double.toString(currentweight));

			log(operator + "." + parameter + " = " + currentweight);

			container = input.copy();

			// apply the input to the inner operators
			for (int i = 0; i < getNumberOfOperators(); i++) {
				container = getOperator(i).apply(container);
			}

			// get the PerformanceVector
			if (!container.contains(PerformanceVector.class)) {
				// PerformanceVector should be available --> see
				// checkIO(IOContainer);
				throw new OperatorException("Cannot find PerformanceVector!");
			}
			performance = container.get(PerformanceVector.class);
			lastperf = performance.getMainCriterion().getFitness();
			log("Performance: " + performance.toResultString());

			if ((best == null) || (performance.compareTo(best) > 0)) {
				best = performance;
				bestweights = (AttributeWeights) currentweights.clone();
				iter_without_improvement = 0;
			} else {
				iter_without_improvement++;
			}

			if (iter_without_improvement >= max_iter_without_improvement) {
				break;
			}

			if (lastiteration) {
				break;
			}

			AttributeWeights evalweights = container.get(AttributeWeights.class);
			double w;
			for (int i = 0; i < names.length; i++) {
				w = evalweights.getWeight(names[i]);
				if (Double.isNaN(w)) {
					currentweights.setWeight(names[i], 0.0d);
					continue;
				}
				if (currentweights.getWeight(names[i]) != 0.0d) {
					currentweights.setWeight(names[i], w);
				}
			}

			weights = new double[names.length];
			for (int i = 0; i < names.length; i++) {
				weights[i] = Math.abs(currentweights.getWeight(names[i]));
			}
			Arrays.sort(weights);

			nullindex = 0;
			while ((weights[nullindex] == 0.0d) && (nullindex < names.length - 1)) {
				nullindex++;
			}

			while ((Math.abs(weights[nullindex]) < min_diff) && (nullindex < names.length - 1)) {
				nullindex++;
			}
			currentweight = weights[nullindex];
			if (nullindex == names.length - 2) {
				lastiteration = true;
				if (weights[nullindex] == 0.0d)
					break;
			}
			if (nullindex == names.length - 1) {
				break;
			}

			inApplyLoop();
		}

		input.remove(AttributeWeights.class);

		return new IOObject[] { best, bestweights };
	}

	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public void getParametersToOptimize() throws OperatorException {
		min_diff = getParameterAsDouble(PARAMETER_MIN_DIFF);

		String keyvalue = getParameterAsString(PARAMETER_PARAMETER);
		String[] parameter = keyvalue.split("\\.");

		if ((parameter.length < 2) || (parameter.length > 3)) {
			throw new UserError(this, 907, keyvalue);
		}

		operator = getProcess().getOperator(parameter[0]);

		if (operator == null) {
			throw new UserError(this, 109, parameter[0]);
		}

		ParameterType targetType = operator.getParameters().getParameterType(parameter[1]);
		this.parameter = parameter[1];

		if (targetType == null) {
			throw new UserError(this, 906, parameter[0] + "." + parameter[1]);
		}
		if (!(targetType instanceof ParameterTypeNumber)) {
			throw new UserError(this, 909, parameter[0] + "." + parameter[1]);
		}
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		return new LastInnerOperatorCondition(new Class[] { PerformanceVector.class, AttributeWeights.class });
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeString(PARAMETER_PARAMETER, "The parameter to set the weight value");
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_MIN_DIFF, "The minimum difference between two weights.", 0.0d, Double.POSITIVE_INFINITY, 1.0e-10);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_ITERATIONS_WITHOUT_IMPROVEMENT, "Number iterations without performance improvement.", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
