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
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.features.PopulationOperator;


/**
 * This operator performs the weighting under the naive assumption that the
 * features are independent from each other. Each attribute is weighted with a
 * linear search. This approach may deliver good results after short time if the
 * features indeed are not highly correlated.
 * 
 * @author Ingo Mierswa
 * @version $Id: ForwardWeighting.java,v 1.11 2006/03/21 15:35:46 ingomierswa
 *          Exp $
 */
public class ForwardWeighting extends FeatureWeighting {

	public ForwardWeighting(OperatorDescription description) {
		super(description);
	}

	public PopulationOperator getWeightingOperator(String parameter) {
		double[] weights = new double[] { 0.25d, 0.5d, 0.75d, 1.0d };
		if ((parameter != null) && (parameter.length() != 0)) {
			try {
				String[] weightStrings = parameter.split(" ");
				weights = new double[weightStrings.length];
				for (int i = 0; i < weights.length; i++)
					weights[i] = Double.parseDouble(weightStrings[i]);
			} catch (Exception e) {
				logError("Could not create weights: " + e.getMessage() + "! Use standard weights.");
				weights = new double[] { 0.25d, 0.5d, 0.75d, 1.0d };
			}
		}
		return new SimpleWeighting(0.0d, weights);
	}

	public Population createInitialPopulation(ExampleSet es) {
		Population initPop = new Population();
		AttributeWeightedExampleSet nes = new AttributeWeightedExampleSet((ExampleSet) es.clone());
		for (Attribute attribute : nes.getAttributes())
			nes.setWeight(attribute, 0.0d);
		for (Attribute attribute : es.getAttributes()) {
			AttributeWeightedExampleSet forwardES = (AttributeWeightedExampleSet) nes.clone();
			forwardES.setWeight(attribute, 1.0d);
			initPop.add(new Individual(forwardES));
		}
		return initPop;
	}
}
