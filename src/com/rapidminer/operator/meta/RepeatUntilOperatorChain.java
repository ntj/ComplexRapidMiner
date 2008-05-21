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
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * Performs its inner operators until all given criteria are met or a timeout
 * occurs.
 * 
 * @author Stefan Rueping
 * @version $Id: RepeatUntilOperatorChain.java,v 1.11 2006/04/05 08:57:26
 *          ingomierswa Exp $
 */
public class RepeatUntilOperatorChain extends OperatorChain {

	/** The parameter name for &quot;Minimal number of attributes in first example set&quot; */
	public static final String PARAMETER_MIN_ATTRIBUTES = "min_attributes";

	/** The parameter name for &quot;Maximal number of attributes in first example set&quot; */
	public static final String PARAMETER_MAX_ATTRIBUTES = "max_attributes";

	/** The parameter name for &quot;Minimal number of examples in first example set&quot; */
	public static final String PARAMETER_MIN_EXAMPLES = "min_examples";

	/** The parameter name for &quot;Maximal number of examples in first example set&quot; */
	public static final String PARAMETER_MAX_EXAMPLES = "max_examples";

	/** The parameter name for &quot;Minimal main criterion in first performance vector&quot; */
	public static final String PARAMETER_MIN_CRITERION = "min_criterion";

	/** The parameter name for &quot;Maximal main criterion in first performance vector&quot; */
	public static final String PARAMETER_MAX_CRITERION = "max_criterion";

	/** The parameter name for &quot;Maximum number of iterations&quot; */
	public static final String PARAMETER_MAX_ITERATIONS = "max_iterations";

	/** The parameter name for &quot;Timeout in minutes (-1 = no timeout)&quot; */
	public static final String PARAMETER_TIMEOUT = "timeout";

	/** The parameter name for &quot;Stop when performance of inner chain behaves like this.&quot; */
	public static final String PARAMETER_PERFORMANCE_CHANGE = "performance_change";

	/** The parameter name for &quot;Evaluate condition before inner chain is applied (true) or after?&quot; */
	public static final String PARAMETER_CONDITION_BEFORE = "condition_before";
	
	public static final String[] COMPARISONS = { "none", "decreasing", "non-increasing" };

	public static final int NONE = 0;

	public static final int DECREASING = 1;

	public static final int NONINCREASING = 2;

	private int iteration;

	private long stoptime;

	private double fitness;

	private IOContainer lastInput;

	public RepeatUntilOperatorChain(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		iteration = 0;
		int timeout = getParameterAsInt(PARAMETER_TIMEOUT);
		if (timeout == -1) {
			stoptime = Long.MAX_VALUE;
		} else {
			stoptime = System.currentTimeMillis() + 60L * 1000 * timeout;
		}
		lastInput = null;
		fitness = Double.NEGATIVE_INFINITY;

		IOContainer input = getInput().copy();
		while (!evaluateCondition(input)) {
			log("Iteration " + iteration);
			for (int j = 0; j < getNumberOfOperators(); j++) {
				input = getOperator(j).apply(input);
			}
			iteration++;
            inApplyLoop();
		}
		if (lastInput != null) {
			// return last known optimal result instead of current (see
			// parameter "performance_change")
			input = lastInput;
		}

		return new IOObject[0];
	}

	/** Evaluates whether the stopping condition is met */
	private boolean evaluateCondition(IOContainer input) throws MissingIOObjectException, UndefinedParameterError {
		if ((iteration == 0) && (!getParameterAsBoolean(PARAMETER_CONDITION_BEFORE))) {
			return false;
		}

		int changeType = getParameterAsInt(PARAMETER_PERFORMANCE_CHANGE);
		if (changeType != NONE) {
			if (iteration > 0) {
				double currentFitness = input.get(PerformanceVector.class).getMainCriterion().getFitness();
				if ((changeType == DECREASING) && (currentFitness < fitness)) {
					return true;
				} else if ((changeType == NONINCREASING) && (currentFitness <= fitness)) {
					return true;
				}
				fitness = currentFitness;
			}
			lastInput = input.copy();
			input.remove(PerformanceVector.class);
		}

		int maxit = getParameterAsInt(PARAMETER_MAX_ITERATIONS);
		if (iteration >= maxit) {
			log("Maximum number of iterations met.");
			return true;
		};

		if (java.lang.System.currentTimeMillis() > stoptime) {
			log("Runtime exceeded.");
			return true;
		};

		int maxAtts = getParameterAsInt(PARAMETER_MAX_ATTRIBUTES);
		int minAtts = getParameterAsInt(PARAMETER_MIN_ATTRIBUTES);
		if ((maxAtts < Integer.MAX_VALUE) || (minAtts > 0)) {
			int nrAtts = input.get(ExampleSet.class).getAttributes().size();
			if ((nrAtts > maxAtts) || (nrAtts < minAtts))
				return false;
		}

		int maxEx = getParameterAsInt(PARAMETER_MAX_EXAMPLES);
		int minEx = getParameterAsInt(PARAMETER_MIN_EXAMPLES);
		if ((maxEx < Integer.MAX_VALUE) || (minEx > 0)) {
			int nrEx = input.get(ExampleSet.class).size();
			if ((nrEx > maxEx) || (nrEx < minEx))
				return false;
		}

		double maxCrit = getParameterAsDouble(PARAMETER_MAX_CRITERION);
		double minCrit = getParameterAsDouble(PARAMETER_MIN_CRITERION);
		if ((maxCrit < Double.POSITIVE_INFINITY) || (minCrit > Double.NEGATIVE_INFINITY)) {
			double crit = input.get(PerformanceVector.class).getMainCriterion().getAverage();
			if ((crit > maxCrit) || (crit < minCrit))
				return false;
		}

		log("All criteria met.");
		return true;
	}

	/** Returns a simple chain condition. */
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new SimpleChainInnerOperatorCondition();
	}

	/** Returns the maximum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 0;
	}

	/** Returns the minimum number of innner operators. */
	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_MIN_ATTRIBUTES, "Minimal number of attributes in first example set", 0, Integer.MAX_VALUE, 0);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_MAX_ATTRIBUTES, "Maximal number of attributes in first example set", 0, Integer.MAX_VALUE, 0);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_MIN_EXAMPLES, "Minimal number of examples in first example set", 0, Integer.MAX_VALUE, 0);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_MAX_EXAMPLES, "Maximal number of examples in first example set", 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_MIN_CRITERION, "Minimal main criterion in first performance vector", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_MAX_CRITERION, "Maximal main criterion in first performance vector", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_MAX_ITERATIONS, "Maximum number of iterations", 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
		type.setExpert(true);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_TIMEOUT, "Timeout in minutes (-1 = no timeout)", 1, Integer.MAX_VALUE, -1);
		type.setExpert(true);
		types.add(type);
		type = new ParameterTypeCategory(PARAMETER_PERFORMANCE_CHANGE, "Stop when performance of inner chain behaves like this.", COMPARISONS, NONE);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeBoolean(PARAMETER_CONDITION_BEFORE, "Evaluate condition before inner chain is applied (true) or after?", true);
		type.setExpert(true);
		types.add(type);
		return types;
	}
}
