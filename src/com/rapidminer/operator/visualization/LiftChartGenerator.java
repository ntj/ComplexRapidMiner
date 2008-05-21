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
import com.rapidminer.tools.math.LiftDataGenerator;


/**
 * This operator creates a Lift chart for the given example set and model. The model
 * will be applied on the example set and a lift chart will be produced afterwards.
 * 
 * Please note that a predicted label of the given example set will be removed during 
 * the application of this operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: LiftChartGenerator.java,v 1.4 2008/05/09 19:23:14 ingomierswa Exp $
 *
 */
public class LiftChartGenerator extends Operator {

	public LiftChartGenerator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		Model model = getInput(Model.class);
		
		if (exampleSet.getAttributes().getLabel() == null) {
			throw new UserError(this, 105);
		}
		if (!exampleSet.getAttributes().getLabel().isNominal()) {
			throw new UserError(this, 101, "Lift Charts", exampleSet.getAttributes().getLabel());
		}
		if (exampleSet.getAttributes().getLabel().getMapping().getValues().size() != 2) {
			throw new UserError(this, 114, "Lift Charts", exampleSet.getAttributes().getLabel());
		}
		
		if (exampleSet.getAttributes().getPredictedLabel() != null) {
			logWarning("Input example already has a predicted label which will be removed.");
			PredictionModel.removePredictedLabel(exampleSet);
		}
		
		exampleSet = model.apply(exampleSet);
		if (exampleSet.getAttributes().getPredictedLabel() == null) {
			throw new UserError(this, 107);
		}
		
		LiftDataGenerator liftDataGenerator = new LiftDataGenerator();
		List<double[]> liftPoints = liftDataGenerator.createLiftDataList(exampleSet);
		liftDataGenerator.createLiftChartPlot(liftPoints);

		PredictionModel.removePredictedLabel(exampleSet);
		
		return new IOObject[] { exampleSet, model };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class, Model.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, Model.class };
	}	
}
