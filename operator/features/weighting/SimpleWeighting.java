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

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.IndividualOperator;


/**
 * This PopulationOperator realises a simple weighting, i.e. creates a list of
 * clones of each individual and weights one attribute in each of the clones
 * with some different weights.
 * 
 * @author Ingo Mierswa
 * @version $Id: SimpleWeighting.java,v 1.3 2008/05/09 19:23:22 ingomierswa Exp $
 */
public class SimpleWeighting extends IndividualOperator {

	/**
	 * If a weight is equal to this compare weight, all weights are used for
	 * building a new individual.
	 */
	private double compareWeight = 0.0d;

	/** These weights are used for building new individuals. */
	private double[] weights = null;

	/** Creates a simple weighting. */
	public SimpleWeighting(double compareWeight, double[] weights) {
		this.compareWeight = compareWeight;
		this.weights = weights;
	}

	public List<Individual> operate(Individual individual) {
		AttributeWeightedExampleSet exampleSet = individual.getExampleSet();
		List<Individual> l = new LinkedList<Individual>();
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (exampleSet.getWeight(attribute) == compareWeight) {
				for (int w = 0; w < weights.length; w++) {
					AttributeWeightedExampleSet nes = (AttributeWeightedExampleSet) exampleSet.clone();
					nes.setWeight(attribute, weights[w]);
					l.add(new Individual(nes));
				}
			}
		}
		return l;
	}
}
