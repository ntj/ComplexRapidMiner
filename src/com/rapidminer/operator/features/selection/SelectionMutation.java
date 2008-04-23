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
package com.rapidminer.operator.features.selection;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.IndividualOperator;


/**
 * Inverts the used bit for every feature of every example set with a given
 * fixed probability.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: SelectionMutation.java,v 1.1 2006/04/14 07:47:17 ingomierswa
 *          Exp $
 */
public class SelectionMutation extends IndividualOperator {

	private double probability;

    private Random random;
    
    
	public SelectionMutation(double probability, Random random) {
		this.probability = probability;
        this.random = random;
	}

	public List<Individual> operate(Individual individual) {
		List<Individual> l = new LinkedList<Individual>();
		AttributeWeightedExampleSet clone = (AttributeWeightedExampleSet) individual.getExampleSet().clone();
		double prob = probability < 0 ? 1.0d / clone.getAttributes().size() : probability;
		for (Attribute attribute : clone.getAttributes()) {
			if (random.nextDouble() < prob) {
				clone.flipAttributeUsed(attribute);
			}
		}
		if (clone.getNumberOfUsedAttributes() > 0) {
			l.add(new Individual(clone));
		}
		// add also original ES
		l.add(individual);
		return l;
	}
}
