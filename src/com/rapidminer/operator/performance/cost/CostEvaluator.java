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
package com.rapidminer.operator.performance.cost;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.MeasuredPerformance;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeMatrix;
/**
 * This operator provides the ability to evaluate classification costs.
 * Therefore a cost matrix might be specified, denoting the costs for every
 * possible classification outcome: predicted label x real label.
 * Costs will be minimized during optimization.
 * 
 * @author Sebastian Land
 * @version $Id: CostEvaluator.java,v 1.5 2008/07/13 11:00:58 ingomierswa Exp $
 */
public class CostEvaluator extends Operator {


	public CostEvaluator(OperatorDescription description) {
		super(description);
	}

	private static final String PARAMETER_COST_MATRIX = "cost_matrix";
	private static final String PARAMETER_KEEP_EXAMPLE_SET = "keep_exampleSet";

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		Attribute label = exampleSet.getAttributes().getLabel();
		if (label != null) {
			if (label.isNominal()) {
				double[][] costMatrix = getParameterAsMatrix(PARAMETER_COST_MATRIX);
				MeasuredPerformance criterion = new ClassificationCostCriterion(costMatrix, label, exampleSet.getAttributes().getPredictedLabel());
				PerformanceVector performance = new PerformanceVector();
				performance.addCriterion(criterion);
				// now measuring costs
				criterion.startCounting(exampleSet, false);
				for (Example example: exampleSet) {
					criterion.countExample(example);
				}
				if (getParameterAsBoolean(PARAMETER_KEEP_EXAMPLE_SET)) {
					return new IOObject[] {exampleSet, performance};
				} else {
					return new IOObject[] {performance};
				}
			}
		}
		return new IOObject[] {exampleSet};
	}
	
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_KEEP_EXAMPLE_SET, "Indicates if the example set should be kept.", false));
		types.add(new ParameterTypeMatrix(PARAMETER_COST_MATRIX, "The cost matrix in Matlab single line format", "Cost Matrix", "Predicted Class", "True Class", true, false));
		return types;
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { PerformanceVector.class };
	}
}
