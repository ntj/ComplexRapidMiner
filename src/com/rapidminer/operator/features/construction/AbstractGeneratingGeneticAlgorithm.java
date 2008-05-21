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

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.generator.BasicArithmeticOperationGenerator;
import com.rapidminer.generator.FeatureGenerator;
import com.rapidminer.generator.ReciprocalValueGenerator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.features.PopulationOperator;
import com.rapidminer.operator.features.selection.AbstractGeneticAlgorithm;
import com.rapidminer.operator.features.selection.GeneticAlgorithm;
import com.rapidminer.operator.features.selection.SelectionCrossover;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * In contrast to its superclass {@link GeneticAlgorithm}, the
 * {@link GeneratingGeneticAlgorithm} generates new attributes and thus can
 * change the length of an individual. Therfore specialized mutation and
 * crossover operators are being applied. Generators are chosen at random from a
 * list of generators specified by boolean parameters. <br/>
 * 
 * Since this operator does not contain algorithms to extract features from
 * value series, it is restricted to example sets with only single attributes.
 * For automatic feature extraction from values series the value series plugin
 * for RapidMiner written by Ingo Mierswa should be used. It is available at <a
 * href="http://rapid-i.com">http://rapid-i.com</a>
 * 
 * @rapidminer.reference Ritthoff/etal/2001a
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractGeneratingGeneticAlgorithm.java,v 1.1 2006/04/14
 *          07:47:17 ingomierswa Exp $
 */
public abstract class AbstractGeneratingGeneticAlgorithm extends AbstractGeneticAlgorithm {

	public static final String PARAMETER_P_INITIALIZE = "p_initialize";
	
	public static final String PARAMETER_P_CROSSOVER = "p_crossover";
	
	public static final String PARAMETER_CROSSOVER_TYPE = "crossover_type";
	
	public static final String PARAMETER_USE_PLUS = "use_plus";
	
	public static final String PARAMETER_USE_DIFF = "use_diff";
	
	public static final String PARAMETER_USE_MULT = "use_mult";
	
	public static final String PARAMETER_USE_DIV = "use_div";
	
	public static final String PARAMETER_RECIPROCAL_VALUE = "reciprocal_value";
	
	public AbstractGeneratingGeneticAlgorithm(OperatorDescription description) {
		super(description);
	}

	/**
	 * Returns a specialized generating mutation, e.g. a
	 * <code>AttributeGenerator</code>.
	 */
	protected abstract PopulationOperator getGeneratingPopulationOperator(ExampleSet exampleSet) throws OperatorException;

	/**
	 * Sets up a population of given size and creates ExampleSets with randomly
	 * selected attributes (the probability to be switched on is controlled by
	 * pInitialize).
	 */
	public Population createInitialPopulation(ExampleSet es) throws UndefinedParameterError {
		Population initP = new Population();
		while (initP.getNumberOfIndividuals() < getParameterAsInt(PARAMETER_POPULATION_SIZE)) { 
			AttributeWeightedExampleSet nes = new AttributeWeightedExampleSet((ExampleSet) es.clone());
			for (Attribute attribute : nes.getAttributes()) {
				if (getRandom().nextDouble() > getParameterAsDouble(PARAMETER_P_INITIALIZE))
					nes.flipAttributeUsed(attribute);
			}
			if (nes.getNumberOfUsedAttributes() > 0)
				initP.add(new Individual(nes));
		}
		return initP;
	}

	protected List<PopulationOperator> getPreProcessingPopulationOperators(ExampleSet exampleSet) throws OperatorException {
		List<PopulationOperator> popOps = super.getPreProcessingPopulationOperators(exampleSet);
		PopulationOperator generator = getGeneratingPopulationOperator(exampleSet);
		if (generator != null)
			popOps.add(generator);
		return popOps;
	}

	/** Returns an <code>UnbalancedCrossover</code>. */
	protected PopulationOperator getCrossoverPopulationOperator(ExampleSet exampleSet) throws UndefinedParameterError {
		double pCrossover = getParameterAsDouble(PARAMETER_P_CROSSOVER);
		int crossoverType = getParameterAsInt(PARAMETER_CROSSOVER_TYPE);
		return new UnbalancedCrossover(crossoverType, pCrossover, getRandom());
	}

	/** Returns a list with all generator which should be used. */
	public List<FeatureGenerator> getGenerators() {
		List<FeatureGenerator> generators = new ArrayList<FeatureGenerator>();
		if (getParameterAsBoolean(PARAMETER_USE_PLUS))
			generators.add(new BasicArithmeticOperationGenerator(BasicArithmeticOperationGenerator.SUM));
		if (getParameterAsBoolean(PARAMETER_USE_DIFF))
			generators.add(new BasicArithmeticOperationGenerator(BasicArithmeticOperationGenerator.DIFFERENCE));
		if (getParameterAsBoolean(PARAMETER_USE_MULT))
			generators.add(new BasicArithmeticOperationGenerator(BasicArithmeticOperationGenerator.PRODUCT));
		if (getParameterAsBoolean(PARAMETER_USE_DIV))
			generators.add(new BasicArithmeticOperationGenerator(BasicArithmeticOperationGenerator.QUOTIENT));
		if (getParameterAsBoolean(PARAMETER_RECIPROCAL_VALUE))
			generators.add(new ReciprocalValueGenerator());
		return generators;
	}	
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeDouble(PARAMETER_P_INITIALIZE, "Initial probability for an attribute to be switched on.", 0, 1, 0.5));
		ParameterType type = new ParameterTypeDouble(PARAMETER_P_CROSSOVER, "Probability for an individual to be selected for crossover.", 0, 1, 0.5);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeCategory(PARAMETER_CROSSOVER_TYPE, "Type of the crossover.", SelectionCrossover.CROSSOVER_TYPES, SelectionCrossover.UNIFORM));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_PLUS , "Generate sums.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_DIFF, "Generate differences.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_MULT, "Generate products.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_DIV, "Generate quotients.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_RECIPROCAL_VALUE, "Generate reciprocal values.", true));
		return types;
	}
}
