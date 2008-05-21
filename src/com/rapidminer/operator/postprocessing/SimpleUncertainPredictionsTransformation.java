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
package com.rapidminer.operator.postprocessing;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;

/**
 * This operator sets all predictions which do not have a higher confidence than the 
 * specified one to &quot;unknown&quot; (missing value). This operator is a quite simple
 * version of the CostBasedThresholdLearner which might be useful in simple binominal
 * classification settings (although it does also work for polynominal classifications).
 *  
 * @author Ingo Mierswa
 * @version $Id: SimpleUncertainPredictionsTransformation.java,v 1.2 2008/05/09 19:23:27 ingomierswa Exp $
 */
public class SimpleUncertainPredictionsTransformation extends Operator {

	public static final String PARAMETER_MIN_CONFIDENCE = "min_confidence";
	
	public SimpleUncertainPredictionsTransformation(OperatorDescription description) {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		// checks
		Attribute predictedLabel = exampleSet.getAttributes().getPredictedLabel(); 
		if (predictedLabel == null) {
			throw new UserError(this, 107);
		}
		if (!predictedLabel.isNominal()) {
			throw new UserError(this, 119, predictedLabel, getName());			
		}
		
		double minConfidence = getParameterAsDouble(PARAMETER_MIN_CONFIDENCE);
		
		for (Example example : exampleSet) {
			double predictionValue = example.getValue(predictedLabel);
			String predictionClass = predictedLabel.getMapping().mapIndex((int)predictionValue);
			double confidence = example.getConfidence(predictionClass);
			if (!Double.isNaN(confidence)) {
				if (confidence < minConfidence) {
					example.setValue(predictedLabel, Double.NaN);
				}
			}
		}
		
		return new IOObject[] { exampleSet };
	}

	@Override
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	@Override
	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> list = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_MIN_CONFIDENCE, "The minimal confidence necessary for not setting the prediction to 'unknown'.", 0.0d, 1.0d, 0.5d);
		type.setExpert(false);
		list.add(type);
		return list;
	}
}
