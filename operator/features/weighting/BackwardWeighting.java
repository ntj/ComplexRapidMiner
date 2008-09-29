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
 * Uses the backward selection idea for the weighting of features.
 * 
 * @author Ingo Mierswa
 * @version $Id: BackwardWeighting.java,v 1.10 2006/03/21 15:35:46 ingomierswa
 *          Exp $
 */
public class BackwardWeighting extends FeatureWeighting {

	public BackwardWeighting(OperatorDescription description) {
		super(description);
	}

	public PopulationOperator getWeightingOperator(String parameter) {
		double[] weights = new double[] { 1.0d, 0.75d, 0.5d, 0.25d };
		if ((parameter != null) && (parameter.length() != 0)) {
			try {
				String[] weightStrings = parameter.split(" ");
				weights = new double[weightStrings.length];
				for (int i = 0; i < weights.length; i++)
					weights[i] = Double.parseDouble(weightStrings[i]);
			} catch (Exception e) {
				logError("Could not create weights: " + e.getMessage() + "! Use standard weights.");
				weights = new double[] { 1.0d, 0.75d, 0.5d, 0.25d };
			}
		}
		return new SimpleWeighting(1.0d, weights);
	}

	public Population createInitialPopulation(ExampleSet es) {
		Population initPop = new Population();
		AttributeWeightedExampleSet nes = new AttributeWeightedExampleSet((ExampleSet) es.clone());
		for (Attribute attribute : es.getAttributes())
			nes.setWeight(attribute, 1.0d);
		initPop.add(new Individual(nes));
		return initPop;
	}
}
