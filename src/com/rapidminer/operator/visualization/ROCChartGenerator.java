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
package com.rapidminer.operator.visualization;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.performance.PerformanceEvaluator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.math.ROCData;
import com.rapidminer.tools.math.ROCDataGenerator;


/**
 * This operator creates a ROC chart for the given example set and model. The model
 * will be applied on the example set and a ROC chart will be produced afterwards. If
 * you are interested in finding an optimal threshold, the operator 
 * {@link com.rapidminer.operator.postprocessing.ThresholdFinder} should be used. If
 * you are interested in the performance criterion Area-Under-Curve (AUC) the usual
 * {@link PerformanceEvaluator} can be used. This operator just presents a ROC plot
 * for a given model and data set.
 * 
 * Please note that a predicted label of the given example set will be removed during 
 * the application of this operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: ROCChartGenerator.java,v 1.9 2008/05/09 19:23:15 ingomierswa Exp $
 *
 */
public class ROCChartGenerator extends Operator {

	public static final String PARAMETER_USE_EXAMPLE_WEIGHTS = "use_example_weights";
	public static final String PARAMETER_USE_MODEL = "use_model";
	
	public ROCChartGenerator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
		if (exampleSet.getAttributes().getLabel() == null) {
			throw new UserError(this, 105);
		}
		if (!exampleSet.getAttributes().getLabel().isNominal()) {
			throw new UserError(this, 101, "ROC Charts", exampleSet.getAttributes().getLabel());
		}
		if (exampleSet.getAttributes().getLabel().getMapping().getValues().size() != 2) {
			throw new UserError(this, 114, "ROC Charts", exampleSet.getAttributes().getLabel());
		}
		
		if (exampleSet.getAttributes().getPredictedLabel() != null && getParameterAsBoolean(PARAMETER_USE_MODEL)) {
			logWarning("Input example already has a predicted label which will be removed.");
			PredictionModel.removePredictedLabel(exampleSet);
		}
		if (exampleSet.getAttributes().getPredictedLabel() == null && !getParameterAsBoolean(PARAMETER_USE_MODEL)) {
			throw new UserError(this, 107);
		}
		Model model = null;
		if (getParameterAsBoolean(PARAMETER_USE_MODEL)) {
			model = getInput(Model.class);		
			exampleSet = model.apply(exampleSet);
		}
		if (exampleSet.getAttributes().getPredictedLabel() == null) {
		    throw new UserError(this, 107);
		}

		ROCDataGenerator rocDataGenerator = new ROCDataGenerator(1.0d, 1.0d);
		ROCData rocPoints = rocDataGenerator.createROCData(exampleSet, getParameterAsBoolean(PARAMETER_USE_EXAMPLE_WEIGHTS)); 
		rocDataGenerator.createROCPlotDialog(rocPoints);

		PredictionModel.removePredictedLabel(exampleSet);
		if (getParameterAsBoolean(PARAMETER_USE_MODEL)) {
			return new IOObject[] { exampleSet, model };
		} else
			return new IOObject[] {exampleSet };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class, Model.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, Model.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_USE_EXAMPLE_WEIGHTS, "Indicates if example weights should be used for calculations (use 1 as weights for each example otherwise).", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_MODEL, "If checked a given model will be applied for generating ROCChart. If not the examples set must have a predicted label.", true));
		return types;
	}
}
