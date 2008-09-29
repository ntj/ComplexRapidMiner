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

import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.features.PopulationOperator;


/**
 * Adds iteratively the next feature according to given attribute name array.
 * 
 * @author Ingo Mierswa
 * @version $Id: IterativeFeatureAdding.java,v 1.1 2006/04/14 13:07:13
 *          ingomierswa Exp $
 */
public class IterativeFeatureAdding implements PopulationOperator {

	private String[] attributeNames;

	private int counter;

	public IterativeFeatureAdding(String[] attributeNames, int counter) {
		this.attributeNames = attributeNames;
		this.counter = counter;
	}

	/** The default implementation returns true for every generation. */
	public boolean performOperation(int generation) {
		return true;
	}

	public void operate(Population pop) {
		List<Individual> result = new LinkedList<Individual>();
		for (int i = 0; i < pop.getNumberOfIndividuals(); i++) {
			if (counter < attributeNames.length) {
				AttributeWeightedExampleSet exampleSet = (AttributeWeightedExampleSet) pop.get(i).getExampleSet().clone();
				exampleSet.setWeight(exampleSet.getAttributes().get(attributeNames[counter]), 1.0d);
				result.add(new Individual(exampleSet));
			}
		}
		pop.clear();
		pop.addAllIndividuals(result);
		counter++;
	}
}
