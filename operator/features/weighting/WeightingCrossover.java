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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.features.PopulationOperator;
import com.rapidminer.operator.features.selection.SelectionCrossover;


/**
 * Crossover operator for the used weights of example sets. An example set is
 * selected with a given fixed propability and a mating partner is determined
 * randomly. Crossover can be either one point, uniform, or shuffle. Only useful
 * if all example sets have the same attributes and do not contain value series.
 * 
 * @author Ingo Mierswa
 * @version $Id: WeightingCrossover.java,v 1.15 2006/04/14 07:47:17 ingomierswa
 *          Exp $
 */
public class WeightingCrossover implements PopulationOperator {

	private int type;

	private double prob;

	private Random random;

	public WeightingCrossover(int type, double prob, Random random) {
		this.prob = prob;
		this.type = type;
		this.random = random;
	}

	/** The default implementation returns true for every generation. */
	public boolean performOperation(int generation) {
		return true;
	}

	public void crossover(AttributeWeightedExampleSet es1, AttributeWeightedExampleSet es2) {
		switch (type) {
			case SelectionCrossover.ONE_POINT:
				int n = 1 + random.nextInt(es1.getAttributes().size() - 1);
				int counter = 0;
				for (Attribute attribute : es1.getAttributes()) {
					if (counter >= n) {
						double dummy = es1.getWeight(attribute);
						es1.setWeight(attribute, es2.getWeight(attribute));
						es2.setWeight(attribute, dummy);
					}
					counter++;
				}
				break;
			case SelectionCrossover.UNIFORM:
				boolean[] swap = new boolean[es1.getAttributes().size()];
				for (int i = 0; i < swap.length; i++) {
					swap[i] = random.nextBoolean();
				}
				swapWeights(es1, es2, swap);
				break;
			case SelectionCrossover.SHUFFLE:
				swap = new boolean[es1.getAttributes().size()];
				List<Integer> indices = new ArrayList<Integer>();
				for (int i = 0; i < swap.length; i++) {
					indices.add(i);
				}
				if (indices.size() > 0) {
					int toSwap = random.nextInt(indices.size() - 1) + 1;
					for (int i = 0; i < toSwap; i++) {
						swap[indices.remove(random.nextInt(indices.size()))] = true;
					}
				}
				swapWeights(es1, es2, swap);
				break;
			default:
				break;
		}
	}

	private void swapWeights(AttributeWeightedExampleSet es1, AttributeWeightedExampleSet es2, boolean[] swap) {
		int index = 0;
		for (Attribute attribute : es1.getAttributes()) {
			if (swap[index++]) {
				double dummy = es1.getWeight(attribute);
				es1.setWeight(attribute, es2.getWeight(attribute));
				es2.setWeight(attribute, dummy);
			}
		}
	}

	public void operate(Population population) {
		if (population.getNumberOfIndividuals() < 2)
			return;

		LinkedList<AttributeWeightedExampleSet> matingPool = new LinkedList<AttributeWeightedExampleSet>();
		for (int i = 0; i < population.getNumberOfIndividuals(); i++)
			matingPool.add((AttributeWeightedExampleSet) population.get(i).getExampleSet().clone());

		List<Individual> l = new LinkedList<Individual>();

		while (matingPool.size() > 1) {
			AttributeWeightedExampleSet p1 = matingPool.remove(random.nextInt(matingPool.size()));
			AttributeWeightedExampleSet p2 = matingPool.remove(random.nextInt(matingPool.size()));

			if (random.nextDouble() < prob) {
				crossover(p1, p2);
				if (p1.getNumberOfUsedAttributes() > 0)
					l.add(new Individual(p1));
				if (p2.getNumberOfUsedAttributes() > 0)
					l.add(new Individual(p2));
			}
		}

		population.addAllIndividuals(l);
	}
}
