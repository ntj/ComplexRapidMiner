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

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * This operator iterates several times through the inner operators and in each
 * cycle evaluates a performance measure. The IOObjects that are produced as
 * output of the inner operators in the best cycle are then returned. The target
 * of this operator are methods that involve some non-deterministic elements
 * such that the performance in each cycle may vary. An example is k-means with
 * random intialization.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: RandomOptimizationChain.java,v 1.11 2006/04/05 08:57:26
 *          ingomierswa Exp $
 */
public class RandomOptimizationChain extends OperatorChain {


	/** The parameter name for &quot;The number of iterations to perform&quot; */
	public static final String PARAMETER_ITERATIONS = "iterations";

	/** The parameter name for &quot;Timeout in minutes (-1 = no timeout)&quot; */
	public static final String PARAMETER_TIMEOUT = "timeout";
	private int iteration;

	private double currentBestPerformance = Double.NaN;

	private double avgPerformance = 0.0;

	public RandomOptimizationChain(OperatorDescription description) {
		super(description);
		addValue(new Value("iteration", "The number of the current iteration.") {

			public double getValue() {
				return iteration;
			}
		});

		addValue(new Value("performance", "The current best performance") {

			public double getValue() {
				return currentBestPerformance;
			}
		});

		addValue(new Value("avg_performance", "The average performance") {

			public double getValue() {
				return avgPerformance;
			}
		});

	}

	public IOObject[] apply() throws OperatorException {

		int numCycles = getParameterAsInt(PARAMETER_ITERATIONS);

		iteration = 0;
		double maxValue = Double.NEGATIVE_INFINITY;
		double perfSum = 0.0;
		IOContainer bestResult = null;

		long stoptime;
		int timeout = getParameterAsInt(PARAMETER_TIMEOUT);
		if (timeout == -1) {
			stoptime = Long.MAX_VALUE;
		} else {
			stoptime = System.currentTimeMillis() + 60L * 1000 * timeout;
		};
		for (int i = 0; i < numCycles; i++) {

			IOContainer io = applyInnerLoop();

			PerformanceVector perf = io.get(PerformanceVector.class);

			perfSum = perfSum + perf.getMainCriterion().getAverage();

			if (perf.getMainCriterion().getFitness() > maxValue) {

				maxValue = perf.getMainCriterion().getFitness();
				bestResult = io;
			}

			currentBestPerformance = maxValue;
			iteration++;

			avgPerformance = perfSum / iteration;

			if (java.lang.System.currentTimeMillis() > stoptime) {
				log("Runtime exceeded in iteration " + iteration + ".");
				break;
			}
            
            inApplyLoop();
		}

		return bestResult.getIOObjects();
	}

	/**
	 * Applies the inner operator .
	 */
	private IOContainer applyInnerLoop() throws OperatorException {

		IOContainer container = getInput().copy();
		for (int i = 0; i < getNumberOfOperators(); i++) {
			container = getOperator(i).apply(container);
		}

		return container;
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}

	public boolean shouldReturnInnerOutput() {
		return true;
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		return new LastInnerOperatorCondition(new Class[] { PerformanceVector.class });
	}

	/**
	 * Returns the highest possible value for the maximum number of innner
	 * operators.
	 */
	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	/** Returns 1 for the minimum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeInt(PARAMETER_ITERATIONS, "The number of iterations to perform", 1, Integer.MAX_VALUE, false));
		types.add(new ParameterTypeInt(PARAMETER_TIMEOUT, "Timeout in minutes (-1 = no timeout)", 1, Integer.MAX_VALUE, -1));

		return types;
	}

}
