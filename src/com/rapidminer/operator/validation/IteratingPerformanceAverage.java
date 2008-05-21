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

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.math.AverageVector;


/**
 * This operator chain performs the inner operators the given number of times.
 * The inner operators must provide a PerformanceVector. These are averaged and
 * returned as result.
 * 
 * @author Ingo Mierswa
 * @version $Id: IteratingPerformanceAverage.java,v 1.17 2006/04/05 08:57:28
 *          ingomierswa Exp $
 */
public class IteratingPerformanceAverage extends OperatorChain {
    
	public static final String PARAMETER_ITERATIONS = "iterations";
	
	public static final String PARAMETER_AVERAGE_PERFORMANCES_ONLY = "average_performances_only";
	
	private PerformanceCriterion lastPerformance;
    
	public IteratingPerformanceAverage(OperatorDescription description) {
		super(description);
        addValue(new Value("performance", "The last performance average (main criterion).") {
            public double getValue() {
                if (lastPerformance != null)
                    return lastPerformance.getAverage();
                else
                    return Double.NaN;
            }
        });
	}

	public IOObject[] apply() throws OperatorException {
		int numberOfIterations = getParameterAsInt(PARAMETER_ITERATIONS);

		List<AverageVector> averageVectors = new LinkedList<AverageVector>();
		for (int i = 0; i < numberOfIterations; i++) {
			IOContainer evalOutput = evaluate();
			Tools.handleAverages(evalOutput, averageVectors, getParameterAsBoolean(PARAMETER_AVERAGE_PERFORMANCES_ONLY));
			inApplyLoop();
		}

        // set last result for plotting purposes. This is an average value and
        // actually not the last performance value!
        PerformanceVector averagePerformance = Tools.getPerformanceVector(averageVectors);
        if (averagePerformance != null)
            lastPerformance = averagePerformance.getMainCriterion();
        
		AverageVector[] result = new AverageVector[averageVectors.size()];
		averageVectors.toArray(result);
		return result;
	}

	/** Applies the inner operator. */
	private IOContainer evaluate() throws OperatorException {
		IOContainer container = getInput().copy();
		for (int i = 0; i < getNumberOfOperators(); i++) {
			container = getOperator(i).apply(container);
		}
		return container;
	}
    
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new LastInnerOperatorCondition(new Class[] { PerformanceVector.class });
	}

	/** Returns the maximum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 0;
	}

	/** Returns the minimum number of innner operators. */
	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[] { PerformanceVector.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_ITERATIONS, "The number of iterations.", 1, Integer.MAX_VALUE, 10);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_AVERAGE_PERFORMANCES_ONLY, "Indicates if only performance vectors should be averaged or all types of averagable result vectors.", true));
		return types;
	}
}
