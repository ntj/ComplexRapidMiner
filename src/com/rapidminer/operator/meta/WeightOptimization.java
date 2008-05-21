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
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeNumber;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * Performs a feature selection guided by the AttributeWeights. Forward
 * selection means that features with the highest weight-value are selected
 * first (starting with an empty selection). Backward elemination means that
 * features with the smallest weight value are eleminated first (starting with
 * the full feature set).
 * 
 * @author Daniel Hakenjos, Ingo Mierswa
 * @version $Id: WeightOptimization.java,v 1.3 2006/04/05 08:57:26 ingomierswa
 *          Exp $
 */
public class WeightOptimization extends OperatorChain {


	/** The parameter name for &quot;The parameter to set the weight value&quot; */
	public static final String PARAMETER_PARAMETER = "parameter";

	/** The parameter name for &quot;Forward selection or backward elimination.&quot; */
	public static final String PARAMETER_SELECTION_DIRECTION = "selection_direction";

	/** The parameter name for &quot;The minimum difference between two weights.&quot; */
	public static final String PARAMETER_MIN_DIFF = "min_diff";

	/** The parameter name for &quot;Number iterations without performance improvement.&quot; */
	public static final String PARAMETER_ITERATIONS_WITHOUT_IMPROVEMENT = "iterations_without_improvement";
	private static final Class[] INPUT_CLASSES = { ExampleSet.class, AttributeWeights.class };

	private static final Class[] OUTPUT_CLASSES = { ParameterSet.class, PerformanceVector.class, AttributeWeights.class };

	private static final String[] DIRECTIONS = new String[] { "forward selection", "backward elimination" };

	private ParameterSet best;

	private double[] weights;

	private double currentweight, lastweight, lastperf, bestweight;

	// The Operator to set the weight
	private Operator operator;

	// the parameter of the operator
	private String parameter;

	// the minimum difference between two weights
	private double min_diff;

	public WeightOptimization(OperatorDescription description) {
		super(description);
		addValue(new Value("performance", "performance of the last evaluated weight") {

			public double getValue() {
				return lastperf;
			}
		});
		addValue(new Value("best_performance", "best performance") {

			public double getValue() {
				if (best != null)
					return best.getPerformance().getMainCriterion().getAverage();
				else
					return Double.NaN;
			}
		});
		addValue(new Value("weight", "currently used weight") {

			public double getValue() {
				return lastweight;
			}
		});
	}

	public IOObject[] apply() throws OperatorException {

		IOContainer input = getInput();
		input.get(ExampleSet.class).clone();

		AttributeWeights attweights = input.get(AttributeWeights.class);
		Object[] names = attweights.getAttributeNames().toArray();
		weights = new double[names.length];
		for (int i = 0; i < names.length; i++) {
			weights[i] = Math.abs(attweights.getWeight((String) names[i]));
		}
		Arrays.sort(weights);

		int direction = getParameterAsInt(PARAMETER_SELECTION_DIRECTION);
		int max_iter_without_improvement = getParameterAsInt(PARAMETER_ITERATIONS_WITHOUT_IMPROVEMENT);

		getParametersToOptimize();
		operator.getParameters().setParameter("weight_relation", "greater equals");

		int weightindex = weights.length - 1;
		if (direction == 1) {
			// backward elimination
			weightindex = 0;
		}
		lastweight = Double.POSITIVE_INFINITY;
		lastperf = Double.NaN;
		currentweight = weights[weightindex];
		bestweight = currentweight;

		best = null;
		IOContainer container;
		PerformanceVector performance;
		int iter = 0;
		int iter_without_improvement = 0;
		while (true) {
			iter++;
			log("Iteration: " + iter);
			log("Using weight");

			// set the weight
			operator.getParameters().setParameter(parameter, Double.toString(currentweight));
			log(operator + "." + parameter + " = " + currentweight);
			log("Number attributes: " + (weights.length - weightindex));

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

			if ((best == null) || (performance.compareTo(best.getPerformance()) > 0)) {
				String bestValue = Double.toString(currentweight);
				bestweight = currentweight;
				best = new ParameterSet(new Operator[] { operator }, new String[] { parameter }, new String[] { bestValue }, performance);
				iter_without_improvement = 0;
			} else {
				iter_without_improvement++;
			}
			if (iter_without_improvement >= max_iter_without_improvement) {
				break;
			}

			// next weight
			if (((direction == 0) && (weightindex == 0)) || ((direction == 1) && (weightindex == names.length - 1))) {
				inApplyLoop();
				break;
			}

			if (direction == 0) {
				weightindex--;
			} else {
				weightindex++;
			}
			lastweight = currentweight;
			currentweight = weights[weightindex];
			while (Math.abs(currentweight - lastweight) < min_diff) {
				if (weightindex == 0) {
					inApplyLoop();
					break;
				}
				if (direction == 0) {
					weightindex--;
				} else {
					weightindex++;
				}
				lastweight = currentweight;
				currentweight = weights[weightindex];
			}

			inApplyLoop();
		}

		double w;
		for (int i = 0; i < names.length; i++) {
			w = attweights.getWeight((String) names[i]);
			if (w < bestweight) {
				attweights.setWeight((String) names[i], 0.0d);
			}
		}

		input.remove(AttributeWeights.class);

		return new IOObject[] { best, best.getPerformance(), attweights };
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		return new LastInnerOperatorCondition(new Class[] { PerformanceVector.class });
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

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeString(PARAMETER_PARAMETER, "The parameter to set the weight value");
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeCategory(PARAMETER_SELECTION_DIRECTION, "Forward selection or backward elimination.", DIRECTIONS, 0);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_MIN_DIFF, "The minimum difference between two weights.", 0.0d, Double.POSITIVE_INFINITY, 1.0e-10);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_ITERATIONS_WITHOUT_IMPROVEMENT, "Number iterations without performance improvement.", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
