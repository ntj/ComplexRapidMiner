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
package com.rapidminer.operator.features.weighting;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.dialog.AttributeWeightsDialog;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;


/**
 * This operator shows a window with the currently used attribute weights and
 * allows users to change the weight interactively.
 * 
 * @author Ingo Mierswa
 * @version $Id: InteractiveAttributeWeighting.java,v 1.10 2006/03/21 15:35:46
 *          ingomierswa Exp $
 */
public class InteractiveAttributeWeighting extends Operator {

	public InteractiveAttributeWeighting(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		AttributeWeights weights = null;
		ExampleSet exampleSet = null;
		try {
			weights = getInput(AttributeWeights.class);
		} catch (MissingIOObjectException e) {
			log("No feature weights found in input. Trying to find an example set...");
			weights = new AttributeWeights();
			try {
				exampleSet = getInput(ExampleSet.class);
				for (Attribute attribute : exampleSet.getAttributes()) {
					weights.setWeight(attribute.getName(), 1.0d);
				}
				log("ExampleSet found! Initially all attributes will be used with weight 1.");
			} catch (MissingIOObjectException mioe) {
				log("No examples found! Starting dialog without any weights.");
			}
		}

		AttributeWeightsDialog attributeWeightsDialog = new AttributeWeightsDialog(weights);
		attributeWeightsDialog.setVisible(true);

		if (attributeWeightsDialog.isOk()) {
			weights = attributeWeightsDialog.getAttributeWeights();
		}

		if (exampleSet != null) {
			return new IOObject[] { exampleSet, weights };
		} else {
			return new IOObject[] { weights };
		}
	}

	public Class<?>[] getInputClasses() {
		return new Class[0];
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { AttributeWeights.class };
	}
}
