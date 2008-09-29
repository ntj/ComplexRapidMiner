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
package com.rapidminer.operator.performance;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;

public class ForecastingPerformanceEvaluator extends AbstractPerformanceEvaluator {

	public static final String PARAMETER_HORIZON = "horizon";
	
	/** The proper criteria to the names. */
	private static final Class[] SIMPLE_CRITERIA_CLASSES = {
        com.rapidminer.operator.performance.PredictionTrendAccuracy.class
	};
	
	public ForecastingPerformanceEvaluator(OperatorDescription description) {
		super(description);
	}
	
	protected void checkCompatibility(ExampleSet exampleSet) throws OperatorException {
		Tools.isLabelled(exampleSet);
		Tools.isNonEmpty(exampleSet);
	}

	protected double[] getClassWeights(Attribute label) throws UndefinedParameterError {
		return null;
	}

	public List<PerformanceCriterion> getCriteria() {
		List<PerformanceCriterion> allCriteria = new LinkedList<PerformanceCriterion>();
		for (int i = 0; i < SIMPLE_CRITERIA_CLASSES.length; i++) {
			try {
				PerformanceCriterion criterion = (PerformanceCriterion)SIMPLE_CRITERIA_CLASSES[i].newInstance();
				((ForecastingCriterion)criterion).setParent(this);
				allCriteria.add(criterion);
			} catch (InstantiationException e) {
				LogService.getGlobal().logError("Cannot instantiate " + SIMPLE_CRITERIA_CLASSES[i] + ". Skipping...");
			} catch (IllegalAccessException e) {
				LogService.getGlobal().logError("Cannot instantiate " + SIMPLE_CRITERIA_CLASSES[i] + ". Skipping...");
			}
		}
		return allCriteria;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = new LinkedList<ParameterType>();
		types.add(new ParameterTypeInt(PARAMETER_HORIZON, "Indicates the horizon for the calculation of the forecasting performance measures.", 1, Integer.MAX_VALUE, false));
		types.addAll(super.getParameterTypes());
		return types;
	}
}
