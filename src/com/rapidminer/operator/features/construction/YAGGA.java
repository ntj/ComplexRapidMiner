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
package com.rapidminer.operator.features.construction;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.generator.FeatureGenerator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.features.PopulationOperator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * YAGGA is an acronym for Yet Another Generating Genetic Algorithm. Its
 * approach to generating new attributes differs from the original one. The
 * (generating) mutation can do one of the following things with different
 * probabilities:
 * <ul>
 * <li>Probability {@rapidminer.math p/4}: Add a newly generated attribute to the
 * feature vector</li>
 * <li>Probability {@rapidminer.math p/4}: Add a randomly chosen original attribute
 * to the feature vector</li>
 * <li>Probability {@rapidminer.math p/2}: Remove a randomly chosen attribute from
 * the feature vector</li>
 * </ul>
 * Thus it is guaranteed that the length of the feature vector can both grow and
 * shrink. On average it will keep its original length, unless longer or shorter
 * individuals prove to have a better fitness.
 * 
 * Since this operator does not contain algorithms to extract features from
 * value series, it is restricted to example sets with only single attributes.
 * For (automatic) feature extraction from values series the value series plugin
 * for RapidMiner written by Ingo Mierswa should be used. It is available at <a
 * href="http://rapid-i.com">http://rapid-i.com</a>.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: YAGGA.java,v 1.4 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class YAGGA extends AbstractGeneratingGeneticAlgorithm {


	/** The parameter name for &quot;Probability for mutation (-1: 1/n).&quot; */
	public static final String PARAMETER_P_MUTATION = "p_mutation";

	/** The parameter name for &quot;Max total number of attributes in all generations (-1: no maximum).&quot; */
	public static final String PARAMETER_MAX_TOTAL_NUMBER_OF_ATTRIBUTES = "max_total_number_of_attributes";
	public YAGGA(OperatorDescription description) {
		super(description);
	}

	/**
	 * Since the mutation of YAGGA also creates new attributes this method
	 * returns null.
	 */
	protected PopulationOperator getGeneratingPopulationOperator(ExampleSet exampleSet) {
		return null;
	}

	/** Returns the generating mutation <code>PopulationOperator</code>. */
	protected PopulationOperator getMutationPopulationOperator(ExampleSet eSet) throws OperatorException {
		List<FeatureGenerator> generators = getGenerators();
		if (generators.size() == 0) {
			logWarning("No FeatureGenerators specified for " + getName() + ".");
		}
		List<Attribute> attributes = new LinkedList<Attribute>();
		for (Attribute attribute : eSet.getAttributes()) {
			attributes.add(attribute);
		}
		double pMutation = getParameterAsDouble(PARAMETER_P_MUTATION);
		int maxNumberOfAttributes = getParameterAsInt(PARAMETER_MAX_TOTAL_NUMBER_OF_ATTRIBUTES);
		return new GeneratingMutation(attributes, pMutation, maxNumberOfAttributes, generators, getRandom());
	}

	/** Creates a initial population. */
	public Population createInitialPopulation(ExampleSet es) throws UndefinedParameterError {
		Population population = super.createInitialPopulation(es);
		Population popRemovedDeselected = new Population();
		for (int i = 0; i < population.getNumberOfIndividuals(); i++) {
			popRemovedDeselected.add(new Individual(population.get(i).getExampleSet().createCleanClone()));
		}
		return popRemovedDeselected;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_P_MUTATION, "Probability for mutation (-1: 1/n).", 0, 1, -1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_MAX_TOTAL_NUMBER_OF_ATTRIBUTES, "Max total number of attributes in all generations (-1: no maximum).", -1, Integer.MAX_VALUE, -1);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
