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

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;


/**
 * This operator applies the given threshold to an example set and maps a soft
 * prediction to crisp values. If the confidence for the second class (usually
 * positive for RapidMiner) is greater than the given threshold the prediction is set
 * to this class.
 * 
 * @author Ingo Mierswa, Martin Scholz
 * @version $Id: ThresholdApplier.java,v 1.3 2008/05/09 19:23:27 ingomierswa Exp $
 */
public class ThresholdApplier extends Operator {

	public ThresholdApplier(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		Threshold threshold = getInput(Threshold.class);

		Attribute predictedLabel = exampleSet.getAttributes().getPredictedLabel();
		if (predictedLabel == null)
			throw new UserError(this, 107);

		int zeroIndex = predictedLabel.getMapping().mapString(threshold.getZeroClass());
		int oneIndex = predictedLabel.getMapping().mapString(threshold.getOneClass());

		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			double oneClassConfidence = example.getConfidence(threshold.getOneClass());
			double crispPrediction = oneClassConfidence > threshold.getThreshold() ? oneIndex : zeroIndex;
			example.setValue(predictedLabel, crispPrediction);
		}

		return new IOObject[] { exampleSet };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class, Threshold.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
}
