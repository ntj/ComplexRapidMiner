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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.features.FeatureOperator;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.features.PopulationOperator;


/**
 * This feature selection operator selects the best attribute set by trying all
 * possible combinations of attribute selections. It returns the example set
 * containing the subset of attributes which produced the best performance. As
 * this operator works on the powerset of the attributes set it has exponential
 * runtime.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: BruteForceSelection.java,v 1.1 2006/04/14 11:42:27 ingomierswa
 *          Exp $ <br>
 */
public class BruteForceSelection extends FeatureOperator {

	public BruteForceSelection(OperatorDescription description) {
		super(description);
	}

	public Population createInitialPopulation(ExampleSet es) {
		AttributeWeightedExampleSet exampleSet = new AttributeWeightedExampleSet(es);
		for (Attribute attribute : exampleSet.getAttributes())
			exampleSet.setAttributeUsed(attribute, false);
		Population pop = new Population();
		Attribute[] allAttributes = exampleSet.getAttributes().createRegularAttributeArray();
		addAll(pop, exampleSet, allAttributes, 0);
		return pop;
	}

	/** Recursive method to add all attribute combinations to the population. */
	private void addAll(Population pop, AttributeWeightedExampleSet es, Attribute[] allAttributes, int startIndex) {
		if (startIndex >= allAttributes.length)
			return;

		Attribute attribute = allAttributes[startIndex];
		AttributeWeightedExampleSet ce2 = (AttributeWeightedExampleSet) es.clone();
		ce2.setAttributeUsed(attribute, false);
		addAll(pop, ce2, allAttributes, startIndex + 1);

		AttributeWeightedExampleSet ce1 = (AttributeWeightedExampleSet) es.clone();
		ce1.setAttributeUsed(attribute, true);
		if (ce1.getNumberOfUsedAttributes() > 0)
			pop.add(new Individual(ce1));
		addAll(pop, ce1, allAttributes, startIndex + 1);
	}

	/** Does nothing. */
	public List<PopulationOperator> getPreEvaluationPopulationOperators(ExampleSet input) throws OperatorException {
		return new LinkedList<PopulationOperator>();
	}

	/** Returns an empty list if the parameter debug_output is set to false. */
	public List<PopulationOperator> getPostEvaluationPopulationOperators(ExampleSet input) throws OperatorException {
		return new LinkedList<PopulationOperator>();
	}

	/** Stops immediately. */
	public boolean solutionGoodEnough(Population pop) {
		return true;
	}

}
