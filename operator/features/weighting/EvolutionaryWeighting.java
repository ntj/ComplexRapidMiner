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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.features.PopulationOperator;
import com.rapidminer.operator.features.selection.AbstractGeneticAlgorithm;
import com.rapidminer.operator.features.selection.SelectionCrossover;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * This operator performs the weighting of features with an evolutionary
 * strategies approach. The variance of the gaussian additive mutation can be
 * adapted by a 1/5-rule.
 * 
 * @author Ingo Mierswa
 * @version $Id: EvolutionaryWeighting.java,v 1.27 2006/04/14 07:47:17
 *          ingomierswa Exp $
 */
public class EvolutionaryWeighting extends AbstractGeneticAlgorithm {


	/** The parameter name for &quot;The (initial) variance for each mutation.&quot; */
	public static final String PARAMETER_MUTATION_VARIANCE = "mutation_variance";

	/** The parameter name for &quot;If set to true, the 1/5 rule for variance adaption is used.&quot; */
	public static final String PARAMETER_1_5_RULE = "1_5_rule";

	/** The parameter name for &quot;If set to true, the weights are bounded between 0 and 1.&quot; */
	public static final String PARAMETER_BOUNDED_MUTATION = "bounded_mutation";

	/** The parameter name for &quot;Probability for an individual to be selected for crossover.&quot; */
	public static final String PARAMETER_P_CROSSOVER = "p_crossover";

	/** The parameter name for &quot;Type of the crossover.&quot; */
	public static final String PARAMETER_CROSSOVER_TYPE = "crossover_type";
	private WeightingMutation weighting = null;
    
	public EvolutionaryWeighting(OperatorDescription description) {
		super(description);
	}

	public PopulationOperator getCrossoverPopulationOperator(ExampleSet eSet) throws UndefinedParameterError {
		return new WeightingCrossover(getParameterAsInt(PARAMETER_CROSSOVER_TYPE), getParameterAsDouble(PARAMETER_P_CROSSOVER), getRandom());
	}

	public PopulationOperator getMutationPopulationOperator(ExampleSet eSet) throws UndefinedParameterError {
		this.weighting = new WeightingMutation(getParameterAsDouble(PARAMETER_MUTATION_VARIANCE), getParameterAsBoolean(PARAMETER_BOUNDED_MUTATION), getRandom());
		return weighting;
	}

	protected List<PopulationOperator> getPostProcessingPopulationOperators(ExampleSet eSet) throws UndefinedParameterError {
		List<PopulationOperator> otherPostOps = new LinkedList<PopulationOperator>();
		if (getParameterAsBoolean(PARAMETER_1_5_RULE)) {
			otherPostOps.add(new VarianceAdaption(weighting, eSet.getAttributes().size()));
		}
		return otherPostOps;
	}

	public Population createInitialPopulation(ExampleSet exampleSet) throws UndefinedParameterError {
		int numberOfIndividuals = getParameterAsInt(PARAMETER_POPULATION_SIZE);
		Population initPop = new Population();
		for (int i = 0; i < numberOfIndividuals; i++) {
			AttributeWeightedExampleSet nes = new AttributeWeightedExampleSet((ExampleSet) exampleSet.clone());
			for (Attribute attribute : nes.getAttributes()) {
				nes.setWeight(attribute, getRandom().nextDouble());
			}
			initPop.add(new Individual(nes));
		}
		return initPop;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeDouble(PARAMETER_MUTATION_VARIANCE, "The (initial) variance for each mutation.", 0.0d, Double.POSITIVE_INFINITY, 1.0d));
		types.add(new ParameterTypeBoolean(PARAMETER_1_5_RULE, "If set to true, the 1/5 rule for variance adaption is used.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_BOUNDED_MUTATION, "If set to true, the weights are bounded between 0 and 1.", false));
		ParameterType type = new ParameterTypeDouble(PARAMETER_P_CROSSOVER, "Probability for an individual to be selected for crossover.", 0.0d, 1.0d, 0.0d);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeCategory(PARAMETER_CROSSOVER_TYPE, "Type of the crossover.", SelectionCrossover.CROSSOVER_TYPES, SelectionCrossover.UNIFORM));
		return types;
	}
}
