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
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.features.FeatureOperator;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.KeepBest;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.features.PopulationOperator;
import com.rapidminer.operator.features.RedundanceRemoval;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * <p>
 * This operator realizes the two deterministic greedy feature selection
 * algorithms forward selection and backward elimination. However, we added some
 * enhancements to the standard algorithms which are described below:
 * </p>
 * 
 * <h4>Forward Selection</h4>
 * <ol>
 * <li>Create an initial population with {@rapidminer.math n} individuals where
 * {@rapidminer.math n} is the input example set's number of attributes. Each
 * individual will use exactly one of the features.</li>
 * <li>Evaluate the attribute sets and select only the best {@rapidminer.math k}.</li>
 * <li>For each of the {@rapidminer.math k} attribute sets do: If there are
 * {@rapidminer.math j} unused attributes, make {@rapidminer.math j} copies of the attribute
 * set and add exactly one of the previously unused attributes to the attribute
 * set.</li>
 * <li>As long as the performance improved in the last {@rapidminer.math p}
 * iterations go to 2</li>
 * </ol>
 * 
 * <h4>Backward Elimination</h4>
 * <ol>
 * <li>Start with an attribute set which uses all features.</li>
 * <li>Evaluate all attribute sets and select the best {@rapidminer.math k}.</li>
 * <li>For each of the {@rapidminer.math k} attribute sets do: If there are
 * {@rapidminer.math j} attributes used, make {@rapidminer.math j} copies of the attribute
 * set and remove exactly one of the previously used attributes from the
 * attribute set.</li>
 * <li>As long as the performance improved in the last {@rapidminer.math p}
 * iterations go to 2</li>
 * </ol>
 * 
 * <p>
 * The parameter {@rapidminer.math k} can be specified by the parameter
 * <code>keep_best</code>, the parameter {@rapidminer.math p} can be specified by
 * the parameter <code>generations_without_improval</code>. These parameters
 * have default values 1 which means that the standard selection algorithms are
 * used. Using other values increase the runtime but might help to avoid local
 * extrema in the search for the global optimum.
 * </p>
 * 
 * <p>
 * Another unusual parameter is <code>maximum_number_of_generations</code>.
 * This parameter bounds the number of iterations to this maximum of feature
 * selections / deselections. In combination with
 * <code>generations_without_improval</code> this allows several different
 * selection schemes (which are described for forward selection, backward
 * elimination works analogous):
 * 
 * <ul>
 * <li><code>maximum_number_of_generations</code> = {@rapidminer.math m} and
 * <code>generations_without_improval</code> = {@rapidminer.math p}: Selects
 * maximal {@rapidminer.math m} features. The selection stops if not performance
 * improvement was measured in the last {@rapidminer.math p} generations.</li>
 * <li><code>maximum_number_of_generations</code> = {@rapidminer.math -1} and
 * <code>generations_without_improval</code> = {@rapidminer.math p}: Tries to
 * selects new features until no performance improvement was measured in the
 * last {@rapidminer.math p} generations.</li>
 * <li><code>maximum_number_of_generations</code> = {@rapidminer.math m} and
 * <code>generations_without_improval</code> = {@rapidminer.math -1}: Selects
 * maximal {@rapidminer.math m} features. The selection stops is not stopped until all
 * combinations with maximal {@rapidminer.math m} were tried. However, the result
 * might contain less features than these.</li>
 * <li><code>maximum_number_of_generations</code> = {@rapidminer.math -1} and
 * <code>generations_without_improval</code> = {@rapidminer.math -1}: Test all
 * combinations of attributes (brute force, this might take a very long time and
 * should only be applied to small attribute sets).</li>
 * </ul>
 * </p>
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: FeatureSelectionOperator.java,v 1.1 2006/04/14 11:42:27
 *          ingomierswa Exp $
 */
public class FeatureSelectionOperator extends FeatureOperator {

	/** The parameter name for &quot;Forward selection or backward elimination.&quot; */
	public static final String PARAMETER_SELECTION_DIRECTION = "selection_direction";

	/** The parameter name for &quot;Keep the best n individuals in each generation.&quot; */
	public static final String PARAMETER_KEEP_BEST = "keep_best";

	/** The parameter name for &quot;Stop after n generations without improval of the performance (-1: stops if the maximum_number_of_generations is reached).&quot; */
	public static final String PARAMETER_GENERATIONS_WITHOUT_IMPROVAL = "generations_without_improval";

	/** The parameter name for &quot;Delivers the maximum amount of generations (-1: might use or deselect all features).&quot; */
	public static final String PARAMETER_MAXIMUM_NUMBER_OF_GENERATIONS = "maximum_number_of_generations";
	
	public static final int FORWARD_SELECTION = 0;

	public static final int BACKWARD_ELIMINATION = 1;

	private static final String[] DIRECTIONS = { "forward", "backward" };

	private int generationsWOImp;

	private int maxGenerations;

	
	public FeatureSelectionOperator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		this.maxGenerations = getParameterAsInt(PARAMETER_MAXIMUM_NUMBER_OF_GENERATIONS);
		this.generationsWOImp = getParameterAsInt(PARAMETER_GENERATIONS_WITHOUT_IMPROVAL);
		return super.apply();
	}

	int getDefaultDirection() {
		return FORWARD_SELECTION;
	}

	/**
	 * May <tt>es</tt> have <i>n</i> features. The initial population
	 * contains (depending on wether forward selection or backward elimination
	 * is used) either
	 * <ul>
	 * <li><i>n</i> elements with exactly 1 feature switched on or
	 * <li>1 element with all <i>n</i> features switched on.
	 * </ul>
	 */
	public Population createInitialPopulation(ExampleSet es) throws UndefinedParameterError {
		int direction = getParameterAsInt(PARAMETER_SELECTION_DIRECTION);
		Population initP = new Population();
		if (direction == FORWARD_SELECTION) {
			AttributeWeightedExampleSet nes = new AttributeWeightedExampleSet((ExampleSet) es.clone());
			nes.getAttributes().clearRegular();
			for (Attribute attribute : es.getAttributes()) {
				AttributeWeightedExampleSet forwardES = (AttributeWeightedExampleSet) nes.clone();
				forwardES.getAttributes().addRegular(attribute);
				if (forwardES.getNumberOfUsedAttributes() > 0)
					initP.add(new Individual(forwardES));
			}
		} else {
			AttributeWeightedExampleSet nes = new AttributeWeightedExampleSet((ExampleSet) es.clone());
			for (Attribute attribute : nes.getAttributes()) {
				nes.setAttributeUsed(attribute, true);
			}
			if (nes.getNumberOfUsedAttributes() > 0)
				initP.add(new Individual(nes));
		}
		return initP;
	}

	/**
	 * The operators performs two steps:
	 * <ol>
	 * <li>forward selection/backward elimination
	 * <li>kick out all but the <tt>keep_best</tt> individuals
	 * <li>remove redundant individuals
	 * </ol>
	 */
	public List<PopulationOperator> getPreEvaluationPopulationOperators(ExampleSet input) throws OperatorException {
		int direction = getParameterAsInt(PARAMETER_SELECTION_DIRECTION);
		int keepBest = getParameterAsInt(PARAMETER_KEEP_BEST);
		List<PopulationOperator> preOp = new LinkedList<PopulationOperator>();
		preOp.add(new KeepBest(keepBest));
		if (direction == FORWARD_SELECTION) {
			preOp.add(new ForwardSelection(input));
			if (this.maxGenerations <= 0)
				this.maxGenerations = input.getAttributes().size() - 1;
			else
				this.maxGenerations--; // ensures the correct number of
										// features
		} else {
			preOp.add(new BackwardElimination());
			if (this.maxGenerations <= 0)
				this.maxGenerations = input.getAttributes().size();
		}
		preOp.add(new RedundanceRemoval());
		return preOp;
	}

	/** empty list */
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
		ParameterType type = new ParameterTypeCategory(PARAMETER_SELECTION_DIRECTION, "Forward selection or backward elimination.", DIRECTIONS, getDefaultDirection());
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeInt(PARAMETER_KEEP_BEST, "Keep the best n individuals in each generation.", 1, Integer.MAX_VALUE, 1));
		types.add(new ParameterTypeInt(PARAMETER_GENERATIONS_WITHOUT_IMPROVAL, "Stop after n generations without improval of the performance (-1: stops if the maximum_number_of_generations is reached).", -1, Integer.MAX_VALUE, 1));
		types.add(new ParameterTypeInt(PARAMETER_MAXIMUM_NUMBER_OF_GENERATIONS, "Delivers the maximum amount of generations (-1: might use or deselect all features).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
