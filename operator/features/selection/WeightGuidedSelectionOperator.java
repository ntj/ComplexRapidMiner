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
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.features.FeatureOperator;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.features.PopulationOperator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * <p>
 * This operator uses input attribute weights to determine the order of features
 * added to the feature set starting with the feature set containing only the
 * feature with highest weight. The inner operators must provide a performance
 * vector to determine the fitness of the current feature set, e.g. a cross
 * validation of a learning scheme for a wrapper evaluation. Stops if adding the
 * last <code>k</code> features does not increase the performance or if all
 * features were added. The value of <code>k</code> can be set with the
 * parameter <code>generations_without_improval</code>.
 * </p>
 * 
 * @author Ingo Mierswa
 * @version $Id: WeightGuidedSelectionOperator.java,v 1.1 2006/04/14 11:42:27
 *          ingomierswa Exp $
 */
public class WeightGuidedSelectionOperator extends FeatureOperator {


	/** The parameter name for &quot;Stop after n generations without improval of the performance (-1: stops if the number of features is reached).&quot; */
	public static final String PARAMETER_GENERATIONS_WITHOUT_IMPROVAL = "generations_without_improval";

	/** The parameter name for &quot;Indicates that the absolute values of the input weights should be used to determine the feature adding order.&quot; */
	public static final String PARAMETER_USE_ABSOLUTE_WEIGHTS = "use_absolute_weights";
	private int generationsWOImp;

	private int maxGenerations;

	/** A descending sorted array of all attribute names. */
	private String[] attributeNames;

	public WeightGuidedSelectionOperator(OperatorDescription description) {
		super(description);
	}

	/**
	 * Returns an example set containing only the feature with the biggest
	 * weight.
	 */
	public Population createInitialPopulation(ExampleSet es) throws UndefinedParameterError {
		this.generationsWOImp = getParameterAsInt(PARAMETER_GENERATIONS_WITHOUT_IMPROVAL);
		this.maxGenerations = es.getAttributes().size();
		Population initP = new Population();
		AttributeWeightedExampleSet exampleSet = new AttributeWeightedExampleSet((ExampleSet) es.clone());
		exampleSet.deselectAll();
		exampleSet.setWeight(exampleSet.getAttributes().getRegular(attributeNames[0]), 1.0d);
		initP.add(new Individual(exampleSet));
		return initP;
	}

	/** The operators add the feature with the next highest weight. */
	public List<PopulationOperator> getPreEvaluationPopulationOperators(ExampleSet input) throws OperatorException {
		List<PopulationOperator> preOp = new LinkedList<PopulationOperator>();
		attributeNames = new String[input.getAttributes().size()];
		int index = 0;
		for (Attribute attribute : input.getAttributes())
			attributeNames[index++] = attribute.getName();
		AttributeWeights attributeWeights = getInput(AttributeWeights.class);
		attributeWeights.sortByWeight(attributeNames, AttributeWeights.INCREASING, getParameterAsBoolean(PARAMETER_USE_ABSOLUTE_WEIGHTS) ? AttributeWeights.ABSOLUTE_WEIGHTS : AttributeWeights.ORIGINAL_WEIGHTS);
		preOp.add(new IterativeFeatureAdding(attributeNames, 1));
		return preOp;
	}

	/** Returns an empty list. */
	public List<PopulationOperator> getPostEvaluationPopulationOperators(ExampleSet input) throws OperatorException {
		return new LinkedList<PopulationOperator>();
	}

	/**
	 * Returns true if the best individual is not better than the last
	 * generation's best individual.
	 */
	public boolean solutionGoodEnough(Population pop) throws OperatorException {
		return pop.empty() || ((generationsWOImp > 0) && (pop.getGenerationsWithoutImproval() >= generationsWOImp)) || (pop.getGeneration() >= maxGenerations);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(PARAMETER_GENERATIONS_WITHOUT_IMPROVAL, "Stop after n generations without improval of the performance (-1: stops if the number of features is reached).", -1, Integer.MAX_VALUE, 1));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_ABSOLUTE_WEIGHTS, "Indicates that the absolute values of the input weights should be used to determine the feature adding order.", true));
		return types;
	}
}
