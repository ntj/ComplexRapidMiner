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
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.features.FeatureOperator;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.features.PopulationOperator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;


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

	public static final String PARAMETER_MAX_NUMBER_OF_ATTRIBUTES = "max_number_of_attributes";
	
	public static final String PARAMETER_MIN_NUMBER_OF_ATTRIBUTES = "min_number_of_attributes";
	
	public static final String PARAMETER_EXACT_NUMBER_OF_ATTRIBUTES = "exact_number_of_attributes";
	
	
	public BruteForceSelection(OperatorDescription description) {
		super(description);
	}

	public Population createInitialPopulation(ExampleSet es) throws OperatorException {
		int minNumberOfFeatures   = getParameterAsInt(PARAMETER_MIN_NUMBER_OF_ATTRIBUTES);
		int maxNumberOfFeatures   = getParameterAsInt(PARAMETER_MAX_NUMBER_OF_ATTRIBUTES);
		int exactNumberOfFeatures = getParameterAsInt(PARAMETER_EXACT_NUMBER_OF_ATTRIBUTES);
		
		if (exactNumberOfFeatures > 0) {
			logNote("Using exact number of features for feature selection (" + exactNumberOfFeatures + "), ignoring possibly defined range for the number of features.");
		} else {
			if ((maxNumberOfFeatures > 0) && (minNumberOfFeatures > maxNumberOfFeatures)) {
				throw new UserError(this, 210, PARAMETER_MAX_NUMBER_OF_ATTRIBUTES, PARAMETER_MIN_NUMBER_OF_ATTRIBUTES);
			}
		}
		
		AttributeWeightedExampleSet exampleSet = new AttributeWeightedExampleSet(es);
		for (Attribute attribute : exampleSet.getAttributes())
			exampleSet.setAttributeUsed(attribute, false);
		
		Population pop = new Population();
		Attribute[] allAttributes = exampleSet.getAttributes().createRegularAttributeArray();
		if (exactNumberOfFeatures > 0) {
			addAllWithExactNumber(pop, exampleSet, allAttributes, 0, exactNumberOfFeatures);
		} else {
			addAllInRange(pop, exampleSet, allAttributes, 0, minNumberOfFeatures, maxNumberOfFeatures);
		}
		return pop;
	}

	/** Add all attribute combinations with a fixed size to the population. */
	private void addAllWithExactNumber(Population pop, AttributeWeightedExampleSet es, Attribute[] allAttributes, int startIndex, int exactNumberOfFeatures) {
		if (es.getNumberOfUsedAttributes() > exactNumberOfFeatures)
			return;
		for (int i = startIndex; i < allAttributes.length; i++) {
			AttributeWeightedExampleSet clone = (AttributeWeightedExampleSet)es.clone();
			clone.setAttributeUsed(allAttributes[i], true);
			if (clone.getNumberOfUsedAttributes() == exactNumberOfFeatures) {
				pop.add(new Individual(clone));
			} else {
				addAllWithExactNumber(pop, clone, allAttributes, i + 1, exactNumberOfFeatures);
			}
		}
	}
	
	/** Recursive method to add all attribute combinations to the population. */
	private void addAllInRange(Population pop, 
			            AttributeWeightedExampleSet es, Attribute[] allAttributes, int startIndex, 
			            int minNumberOfFeatures, int maxNumberOfFeatures) {
		if (startIndex >= allAttributes.length)
			return;
		int numberOfFeatures = es.getNumberOfUsedAttributes();
		if (maxNumberOfFeatures > 0) {
			if (numberOfFeatures > maxNumberOfFeatures) {
				return;
			}
		}
		
		// recursive call
		Attribute attribute = allAttributes[startIndex];
		AttributeWeightedExampleSet ce2 = (AttributeWeightedExampleSet) es.clone();
		ce2.setAttributeUsed(attribute, false);
		addAllInRange(pop, ce2, allAttributes, startIndex + 1, minNumberOfFeatures, maxNumberOfFeatures);

		AttributeWeightedExampleSet ce1 = (AttributeWeightedExampleSet) es.clone();
		ce1.setAttributeUsed(attribute, true);
		numberOfFeatures = ce1.getNumberOfUsedAttributes(); 
		if (numberOfFeatures > 0) {
			if (((maxNumberOfFeatures < 1) || (numberOfFeatures <= maxNumberOfFeatures)) && (numberOfFeatures >= minNumberOfFeatures)) {
				pop.add(new Individual(ce1));
			}
		}
		addAllInRange(pop, ce1, allAttributes, startIndex + 1, minNumberOfFeatures, maxNumberOfFeatures);
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

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_MIN_NUMBER_OF_ATTRIBUTES, "Determines the minimum number of features used for the combinations.", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_MAX_NUMBER_OF_ATTRIBUTES, "Determines the maximum number of features used for the combinations (-1: try all combinations up to possible maximum)", -1, Integer.MAX_VALUE, -1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_EXACT_NUMBER_OF_ATTRIBUTES, "Determines the exact number of features used for the combinations (-1: use the feature range defined by min and max).", -1, Integer.MAX_VALUE, -1);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
