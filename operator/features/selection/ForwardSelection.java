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
package com.rapidminer.operator.features.selection;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.IndividualOperator;


/**
 * This PopulationOperator realises forward selection, i.e. creates a list of
 * clones of each individual and switches on one attribute in each of the
 * clones.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: ForwardSelection.java,v 1.3 2008/05/09 19:23:18 ingomierswa Exp $
 */
public class ForwardSelection extends IndividualOperator {

	private ExampleSet original;

	public ForwardSelection(ExampleSet original) {
		this.original = original;
	}

	public List<Individual> operate(Individual individual) {
		AttributeWeightedExampleSet exampleSet = individual.getExampleSet();
		List<Individual> l = new LinkedList<Individual>();
		for (Attribute attribute : original.getAttributes()) {
			if (!exampleSet.getAttributes().contains(attribute)) {
				AttributeWeightedExampleSet nes = (AttributeWeightedExampleSet) exampleSet.clone();
				nes.getAttributes().addRegular(attribute);
				l.add(new Individual(nes));
			}
		}
		return l;
	}
}
