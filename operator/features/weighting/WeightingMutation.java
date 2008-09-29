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
import java.util.Random;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.IndividualOperator;


/**
 * Changes the weight for all attributes by multiplying them with a gaussian
 * distribution.
 * 
 * @author Ingo Mierswa
 * @version $Id: WeightingMutation.java,v 1.16 2006/03/27 13:22:00 ingomierswa
 *          Exp $
 */
public class WeightingMutation extends IndividualOperator {

	private double variance;

	private boolean bounded;

	private Random random;

	public WeightingMutation(double variance, boolean bounded, Random random) {
		this.variance = variance;
		this.bounded = bounded;
		this.random = random;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	public double getVariance() {
		return variance;
	}

	public List<Individual> operate(Individual individual) {
		AttributeWeightedExampleSet exampleSet = individual.getExampleSet();
		List<Individual> l = new LinkedList<Individual>();
		for (Attribute attribute : exampleSet.getAttributes()) {
			double weight = exampleSet.getWeight(attribute);
			weight = weight + random.nextGaussian() * variance;
			if ((!bounded) || ((weight >= 0) && (weight <= 1)))
				exampleSet.setWeight(attribute, weight);
		}
		if (exampleSet.getNumberOfUsedAttributes() > 0)
			l.add(new Individual(exampleSet));
		return l;
	}
}
