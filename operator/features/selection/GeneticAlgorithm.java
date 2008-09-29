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

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.features.PopulationOperator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * A genetic algorithm for feature selection (mutation=switch features on and
 * off, crossover=interchange used features). Selection is done by roulette
 * wheel. Genetic algorithms are general purpose optimization / search
 * algorithms that are suitable in case of no or little problem knowledge. <br/>
 * 
 * A genetic algorithm works as follows
 * <ol>
 * <li>Generate an initial population consisting of
 * <code>population_size</code> individuals. Each attribute is switched on
 * with probability <code>p_initialize</code></li>
 * <li>For all individuals in the population
 * <ul>
 * <li>Perform mutation, i.e. set used attributes to unused with probability
 * <code>p_mutation</code> and vice versa.</li>
 * <li>Choose two individuals from the population and perform crossover with
 * probability <code>p_crossover</code>. The type of crossover can be
 * selected by <code>crossover_type</code>.</li>
 * </ul>
 * </li>
 * <li>Perform selection, map all individuals to sections on a roulette wheel
 * whose size is proportional to the individual's fitness and draw
 * <code>population_size</code> individuals at random according to their
 * probability.</li>
 * <li>As long as the fitness improves, go to 2</li>
 * </ol>
 * 
 * If the example set contains value series attributes with blocknumbers, the
 * whole block will be switched on and off.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: GeneticAlgorithm.java,v 1.7 2008/05/09 19:23:18 ingomierswa Exp $
 */
public class GeneticAlgorithm extends AbstractGeneticAlgorithm {

	/** The parameter name for &quot;Initial probability for an attribute to be switched on.&quot; */
	public static final String PARAMETER_P_INITIALIZE = "p_initialize";

	/** The parameter name for &quot;Probability for an attribute to be changed (-1: 1 / numberOfAtt).&quot; */
	public static final String PARAMETER_P_MUTATION = "p_mutation";

	/** The parameter name for &quot;Probability for an individual to be selected for crossover.&quot; */
	public static final String PARAMETER_P_CROSSOVER = "p_crossover";

	/** The parameter name for &quot;Type of the crossover.&quot; */
	public static final String PARAMETER_CROSSOVER_TYPE = "crossover_type";
	
	public static final String PARAMETER_MAX_NUMBER_OF_ATTRIBUTES = "max_number_of_attributes";
	
	public static final String PARAMETER_MIN_NUMBER_OF_ATTRIBUTES = "min_number_of_attributes";
	
	public static final String PARAMETER_EXACT_NUMBER_OF_ATTRIBUTES = "exact_number_of_attributes";
	
	
	public GeneticAlgorithm(OperatorDescription description) {
		super(description);
	}

	/**
	 * Sets up a population of given size and creates ExampleSets with randomly
	 * selected attributes (the probability to be switched on is controlled by
	 * pInitialize).
	 */
	public Population createInitialPopulation(ExampleSet es) throws OperatorException {
		int minNumber    = getParameterAsInt(PARAMETER_MIN_NUMBER_OF_ATTRIBUTES);
		int maxNumber    = getParameterAsInt(PARAMETER_MAX_NUMBER_OF_ATTRIBUTES);
		int exactNumber  = getParameterAsInt(PARAMETER_EXACT_NUMBER_OF_ATTRIBUTES);
		
		if (exactNumber > 0) {
			logNote("Using exact number of features for feature selection (" + exactNumber + "), ignoring possibly defined range for the number of features.");
		} else {
			if ((maxNumber>0) && (minNumber > maxNumber)) {
				throw new UserError(this, 210, PARAMETER_MAX_NUMBER_OF_ATTRIBUTES, PARAMETER_MIN_NUMBER_OF_ATTRIBUTES);
			}	
		}
		
		Population initP = new Population();
		
		if (exactNumber > 0) { // exact feature number
			while (initP.getNumberOfIndividuals() < getParameterAsInt(PARAMETER_POPULATION_SIZE)) { 
				AttributeWeightedExampleSet nes = new AttributeWeightedExampleSet(es);
				double prob = 1.0d / nes.getAttributes().size() * (double)exactNumber;
				for (Attribute attribute : nes.getAttributes()) {
					if (getRandom().nextDouble() < prob) {
						nes.setAttributeUsed(attribute, true);
					} else {
						nes.setAttributeUsed(attribute, false);
					}
				}

				// add result with exact number of features
				int numberOfFeatures = nes.getNumberOfUsedAttributes();
				if (exactNumber == numberOfFeatures)
					initP.add(new Individual(nes));
			}
		} else { // within range
			while (initP.getNumberOfIndividuals() < getParameterAsInt(PARAMETER_POPULATION_SIZE)) { 
				AttributeWeightedExampleSet nes = new AttributeWeightedExampleSet(es);
				for (Attribute attribute : nes.getAttributes()) {
					if (getRandom().nextDouble() < (1.0d - getParameterAsDouble(PARAMETER_P_INITIALIZE)))
						nes.flipAttributeUsed(attribute);
				}				
				int numberOfFeatures = nes.getNumberOfUsedAttributes(); 
				if (((maxNumber < 1) || (numberOfFeatures <= maxNumber)) && (numberOfFeatures >= minNumber)) {
					initP.add(new Individual(nes));
				}
			}	
		}
		return initP;
	}

	/**
	 * Returns an operator that performs the mutation. Can be overridden by
	 * subclasses.
	 */
	protected PopulationOperator getMutationPopulationOperator(ExampleSet eSet) throws UndefinedParameterError {
		double pMutation = getParameterAsDouble(PARAMETER_P_MUTATION);
		int minNumber    = getParameterAsInt(PARAMETER_MIN_NUMBER_OF_ATTRIBUTES);
		int maxNumber    = getParameterAsInt(PARAMETER_MAX_NUMBER_OF_ATTRIBUTES);
		int exactNumber  = getParameterAsInt(PARAMETER_EXACT_NUMBER_OF_ATTRIBUTES);
		return new SelectionMutation(pMutation, getRandom(), minNumber, maxNumber, exactNumber);
	}

	/**
	 * Returns an operator that performs crossover. Can be overridden by
	 * subclasses.
	 */
	protected PopulationOperator getCrossoverPopulationOperator(ExampleSet eSet) throws UndefinedParameterError {
		double pCrossover = getParameterAsDouble(PARAMETER_P_CROSSOVER);
		int crossoverType = getParameterAsInt(PARAMETER_CROSSOVER_TYPE);
		int minNumber     = getParameterAsInt(PARAMETER_MIN_NUMBER_OF_ATTRIBUTES);
		int maxNumber     = getParameterAsInt(PARAMETER_MAX_NUMBER_OF_ATTRIBUTES);
		int exactNumber   = getParameterAsInt(PARAMETER_EXACT_NUMBER_OF_ATTRIBUTES);
		return new SelectionCrossover(crossoverType, pCrossover, getRandom(), minNumber, maxNumber, exactNumber);
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
		types.add(new ParameterTypeDouble(PARAMETER_P_INITIALIZE, "Initial probability for an attribute to be switched on.", 0, 1, 0.5));
		type = new ParameterTypeDouble(PARAMETER_P_MUTATION, "Probability for an attribute to be changed (-1: 1 / numberOfAtt).", -1.0d, 1.0d, -1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_P_CROSSOVER, "Probability for an individual to be selected for crossover.", 0.0d, 1.0d, 0.5d);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeCategory(PARAMETER_CROSSOVER_TYPE, "Type of the crossover.", SelectionCrossover.CROSSOVER_TYPES, SelectionCrossover.UNIFORM));
		return types;
	}
}
