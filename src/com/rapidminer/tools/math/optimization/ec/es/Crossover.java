/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.tools.math.optimization.ec.es;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Crossover operator for the values of an evolution strategies optimization. An individual is selected
 * with a given fixed propability and a mating partner is determined randomly.
 * This class only impplements uniform crossover.
 * 
 * @author Ingo Mierswa
 * @version $Id: Crossover.java,v 1.1 2007/05/27 22:03:33 ingomierswa Exp $
 */
public class Crossover implements PopulationOperator {

	private double prob;

	private Random random;

    
	public Crossover(double prob, Random random) {
		this.prob = prob;
		this.random = random;
	}

	public void crossover(Individual i1, Individual i2) {
		double[] values1 = i1.getValues();
		double[] values2 = i2.getValues();
		boolean[] swap = new boolean[values1.length];
		for (int i = 0; i < swap.length; i++) {
			swap[i] = random.nextBoolean();
		}
		for (int i = 0; i < swap.length; i++) {
			if (swap[i]) {
				double dummy = values1[i];
				values1[i] = values2[i];
				values2[i] = dummy;
			}
		}
		i1.setValues(values1);
		i2.setValues(values2);
	}

	public void operate(Population population) {
		if (population.getNumberOfIndividuals() < 2)
			return;

		List<Individual> matingPool = new LinkedList<Individual>();
		for (int i = 0; i < population.getNumberOfIndividuals(); i++)
			matingPool.add((Individual) population.get(i).clone());

		List<Individual> l = new LinkedList<Individual>();
		while (matingPool.size() > 1) {
			Individual p1 = matingPool.remove(random.nextInt(matingPool.size()));
			Individual p2 = matingPool.remove(random.nextInt(matingPool.size()));
			if (random.nextDouble() < prob) {
				crossover(p1, p2);
				l.add(p1);
				l.add(p2);
			}
		}
		population.addAll(l);
	}
}
