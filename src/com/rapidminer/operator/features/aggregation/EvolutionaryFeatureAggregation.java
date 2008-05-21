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
package com.rapidminer.operator.features.aggregation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.generator.AlgebraicOrGenerator;
import com.rapidminer.generator.FeatureGenerator;
import com.rapidminer.generator.MinMaxGenerator;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;


/**
 * Performs an evolutionary feature aggregation. Each base feature is only
 * allowed to be used as base feature, in one merged feature, or it may not be
 * used at all.
 * 
 * @author Ingo Mierswa
 * @version $Id: EvolutionaryFeatureAggregation.java,v 1.6 2006/04/05 08:57:23
 *          ingomierswa Exp $
 */
public class EvolutionaryFeatureAggregation extends OperatorChain {

	public static final String PARAMETER_POPULATION_CRITERIA_DATA_FILE = "population_criteria_data_file";
	
	public static final String PARAMETER_AGGREGATION_FUNCTION = "aggregation_function";
	
	public static final String PARAMETER_POPULATION_SIZE = "population_size";
	
	public static final String PARAMETER_MAXIMUM_NUMBER_OF_GENERATIONS = "maximum_number_of_generations";
	
	public static final String PARAMETER_SELECTION_TYPE = "selection_type";
	
	public static final String PARAMETER_TOURNAMENT_FRACTION = "tournament_fraction";
	
	public static final String PARAMETER_CROSSOVER_TYPE = "crossover_type";
	
	public static final String PARAMETER_P_CROSSOVER = "p_crossover";
	
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";	
	
	/** The names for the selection types. */
	private static final String[] SELECTION_TYPES = { "tournament", "non-dominated" };

	/** Indicates tournament selection. */
	private static final int SELECTION_TOURNAMENT = 0;

	/** Indicates NSGA-II selection. */
	private static final int SELECTION_MO = 1;

	/** The names for the aggregation functions. */
	private static final String[] AGGREGATION_FUNCTIONS = { "maximum", "algebraic_or" };

	/** Indicates the maximum aggregation function. */
	private static final int AGGREGATION_MAX = 0;

	/** Indicates the algebraic OR aggregation function. */
	private static final int AGGREGATION_ALGEBRAIC = 1;

	/** The original attributes. */
	private Attribute[] allAttributes;
	
	/** The used feature generator. */
	private FeatureGenerator generator = new MinMaxGenerator(MinMaxGenerator.MAX);

	/** The current generation. */
	private int generation = 0;

	/** The maximum generation. */
	private int maxGeneration = 100;

	/** Creates a new evolutionary feature aggregation algorithm. */
	public EvolutionaryFeatureAggregation(OperatorDescription description) {
		super(description);
	}

	// ================================================================================

	public IOObject[] apply() throws OperatorException {
		// init
		ExampleSet exampleSet = getInput(ExampleSet.class);
		int popSize = getParameterAsInt(PARAMETER_POPULATION_SIZE);
		this.generation = 0;
		this.maxGeneration = getParameterAsInt(PARAMETER_MAXIMUM_NUMBER_OF_GENERATIONS);
		int functionType = getParameterAsInt(PARAMETER_AGGREGATION_FUNCTION);
		switch (functionType) {
			case AGGREGATION_MAX:
				this.generator = new MinMaxGenerator(MinMaxGenerator.MAX);
				break;
			case AGGREGATION_ALGEBRAIC:
				this.generator = new AlgebraicOrGenerator();
				break;
		}
		RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
        
		this.allAttributes = new Attribute[exampleSet.getAttributes().size()];
		int index = 0;
		for (Attribute attribute : exampleSet.getAttributes()) 
			allAttributes[index++] = attribute;
		
		// plotter
		AggregationPopulationPlotter plotter = new AggregationPopulationPlotter(exampleSet, allAttributes, this.generator);
		// crossover
		AggregationCrossover crossover = new AggregationCrossover(getParameterAsInt(PARAMETER_CROSSOVER_TYPE), getParameterAsDouble(PARAMETER_P_CROSSOVER), random);
		// mutation
		AggregationMutation mutation = new AggregationMutation(random);
		// selection
		int selectionType = getParameterAsInt(PARAMETER_SELECTION_TYPE);
		AggregationSelection selection = null;
		switch (selectionType) {
			case SELECTION_TOURNAMENT:
				selection = new AggregationTournamentSelection(popSize, getParameterAsDouble(PARAMETER_TOURNAMENT_FRACTION), random);
				break;
			case SELECTION_MO:
				selection = new AggregationNonDominatedSortingSelection(popSize);
				break;
		}
		// initial population
		List<AggregationIndividual> population = createInitialPopulation(popSize, exampleSet.getAttributes().size(), random);

		// start optimization loop
		while (!solutionGoodEnough()) {
			generation++;
			crossover.crossover(population);
			mutation.mutate(population);
			evaluate(population, exampleSet);
			selection.performSelection(population);
			plotter.operate(population);
            inApplyLoop();
		}

		// write criteria data of the final population into a file
		if (isParameterSet(PARAMETER_POPULATION_CRITERIA_DATA_FILE)) {
			File outFile = getParameterAsFile(PARAMETER_POPULATION_CRITERIA_DATA_FILE);
			SimpleDataTable finalStatistics = plotter.createDataTable(population);
			plotter.fillDataTable(finalStatistics, population);
			PrintWriter out = null;
			try {
				out = new PrintWriter(new FileWriter(outFile));
				finalStatistics.write(out);
			} catch (IOException e) {
				throw new UserError(this, e, 303, new Object[] { outFile, e.getMessage() });
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}

		// return result
		evaluate(population, exampleSet);
		Iterator<AggregationIndividual> i = population.iterator();
		AggregationIndividual bestEver = null;
		PerformanceVector bestPerformance = null;
		while (i.hasNext()) {
			AggregationIndividual current = i.next();
			PerformanceVector currentPerf = current.getPerformance();
			if ((bestPerformance == null) || (currentPerf.compareTo(bestPerformance) > 0)) {
				bestPerformance = currentPerf;
				bestEver = current;
			}
		}

		return new IOObject[] { bestEver.createExampleSet(exampleSet, allAttributes, generator), bestPerformance };
	}

	// ================================================================================

	private List<AggregationIndividual> createInitialPopulation(int popSize, int individualSize, Random random) {
		List<AggregationIndividual> population = new ArrayList<AggregationIndividual>();
		for (int i = 0; i < popSize; i++) {
			int[] individual = new int[individualSize];
			for (int a = 0; a < individual.length; a++) {
				if (random.nextBoolean()) {
					individual[a] = 0;
				} else {
					individual[a] = -1;
				}
			}
			population.add(new AggregationIndividual(individual));
		}
		return population;
	}

	/** Returns true if the maximum number of generations was reached. */
	private boolean solutionGoodEnough() {
		if (generation > maxGeneration)
			return true;
		else
			return false;
	}

	/**
	 * Creates example sets from all individuals and invoke the inner operators
	 * in order to estimate the performance.
	 */
	public void evaluate(List population, ExampleSet originalExampleSet) throws OperatorException {
		Iterator i = population.iterator();
		while (i.hasNext()) {
			AggregationIndividual individual = (AggregationIndividual) i.next();
			if (individual.getPerformance() == null) {
				ExampleSet exampleSet = individual.createExampleSet(originalExampleSet, allAttributes, generator);
				if (exampleSet.getAttributes().size() == 0) {
					i.remove();
				} else {
					IOObject[] operatorChainInput = new IOObject[] { exampleSet };
					IOContainer innerResult = getInput().prepend(operatorChainInput);
					for (int j = 0; j < getNumberOfOperators(); j++) {
						innerResult = getOperator(j).apply(innerResult);
					}
					PerformanceVector performanceVector = innerResult.remove(PerformanceVector.class);
					individual.setPerformance(performanceVector);
				}
			}
		}
	}

	// ================================================================================

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, PerformanceVector.class };
	}

	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		return new LastInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[] { PerformanceVector.class });
	}	
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_POPULATION_CRITERIA_DATA_FILE, "The path to the file in which the criteria data of the final population should be saved.", "crit", true));
		ParameterType type = new ParameterTypeCategory(PARAMETER_AGGREGATION_FUNCTION, "The aggregation function which is used for feature aggregations.", AGGREGATION_FUNCTIONS, AGGREGATION_MAX);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_POPULATION_SIZE, "Number of individuals per generation.", 1, Integer.MAX_VALUE, 10);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_MAXIMUM_NUMBER_OF_GENERATIONS, "Number of generations after which to terminate the algorithm.", 1, Integer.MAX_VALUE, 100);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeCategory(PARAMETER_SELECTION_TYPE, "The type of selection.", SELECTION_TYPES, SELECTION_TOURNAMENT);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeDouble(PARAMETER_TOURNAMENT_FRACTION, "The fraction of the population which will participate in each tournament.", 0.0d, 1.0d, 0.2d));
		types.add(new ParameterTypeCategory(PARAMETER_CROSSOVER_TYPE, "The type of crossover.", AggregationCrossover.CROSSOVER_TYPES, AggregationCrossover.CROSSOVER_UNIFORM));
		types.add(new ParameterTypeDouble(PARAMETER_P_CROSSOVER, "Probability for an individual to be selected for crossover.", 0.0d, 1.0d, 0.9d));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
