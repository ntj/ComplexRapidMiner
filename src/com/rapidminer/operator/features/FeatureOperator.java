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
package com.rapidminer.operator.features;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.gui.dialog.IndividualSelector;
import com.rapidminer.gui.dialog.StopDialog;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.Tools;


/**
 * This class is the superclass of all feature selection and generation
 * operators. It provides an easy to use plug-in interface for operators that
 * modify populations. Subclasses just have to supply lists of
 * <tt>PopulationOperators</tt> by overriding
 * <tt>getPreEvalutaionPopulationOperators()</tt> and
 * <tt>getPostEvalutaionPopulationOperators()</tt> during a loop which will
 * terminate if <tt>solutionGoodEnough()</tt> returns true.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: FeatureOperator.java,v 1.14 2008/05/09 19:22:45 ingomierswa Exp $
 *          <br>
 */
public abstract class FeatureOperator extends OperatorChain {

	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	public static final String PARAMETER_SHOW_STOP_DIALOG = "show_stop_dialog";
	
	public static final String PARAMETER_USER_RESULT_INDIVIDUAL_SELECTION = "user_result_individual_selection";
	
	public static final String PARAMETER_SHOW_POPULATION_PLOTTER = "show_population_plotter";
	
	public static final String PARAMETER_PLOT_GENERATIONS = "plot_generations";
	
	public static final String PARAMETER_CONSTRAINT_DRAW_RANGE = "constraint_draw_range";
		
	public static final String PARAMETER_DRAW_DOMINATED_POINTS = "draw_dominated_points";
	
	public static final String PARAMETER_POPULATION_CRITERIA_DATA_FILE = "population_criteria_data_file";
	
	public static final String PARAMETER_MAXIMAL_FITNESS = "maximal_fitness";
	
	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class, AttributeWeights.class, PerformanceVector.class };

	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private Population population;

	/** The optimization stops if this maximal fitness was reached. */
	private double maximalFitness = Double.POSITIVE_INFINITY;

	private boolean checkForMaximalFitness = true;

	private int evaluationCounter = 0;

	private int totalEvaluations = 0;

    private RandomGenerator random;
    
	public FeatureOperator(OperatorDescription description) {
		super(description);
		addValue(new Value("generation", "The number of the current generation.") {

			public double getValue() {
				if (population == null)
					return 0;
				return population.getGeneration();
			}
		});
		addValue(new Value("performance", "The performance of the current generation (main criterion).") {

			public double getValue() {
				if (population == null)
					return Double.NaN;
				if (population.getCurrentBestPerformance() == null)
					return Double.NaN;
				PerformanceVector pv = population.getCurrentBestPerformance();
				if (pv == null)
					return Double.NaN;
				return pv.getMainCriterion().getAverage();
			}
		});
		addValue(new Value("best", "The performance of the best individual ever (main criterion).") {

			public double getValue() {
				if (population == null)
					return Double.NaN;
				PerformanceVector pv = population.getBestPerformanceEver();
				if (pv == null)
					return Double.NaN;
				return pv.getMainCriterion().getAverage();
			}
		});
		addValue(new Value("average_length", "The average number of attributes.") {

			public double getValue() {
				if (population == null)
					return Double.NaN;
				else {
					double lengthSum = 0.0d;
					for (int i = 0; i < population.getNumberOfIndividuals(); i++)
						lengthSum += population.get(i).getExampleSet().getNumberOfUsedAttributes();
					return lengthSum / population.getNumberOfIndividuals();
				}
			}
		});
		addValue(new Value("best_length", "The number of attributes of the best example set.") {

			public double getValue() {
				if (population == null)
					return Double.NaN;
				Individual individual = population.getBestIndividualEver();
				if (individual != null) {
					AttributeWeightedExampleSet eSet = individual.getExampleSet();
					if (eSet != null)
						return eSet.getNumberOfUsedAttributes();
					else
						return Double.NaN;
				} else {
					return Double.NaN;
				}
			}
		});
	}

	/**
	 * Create an initial population. The example set will be cloned before the
	 * method is invoked. This method is invoked after the pre- and
	 * postevaluation population operators were collected.
	 */
	public abstract Population createInitialPopulation(ExampleSet es) throws OperatorException;

	/**
	 * Must return a list of <tt>PopulationOperator</tt>s. All operators are
	 * applied to the population in their order within the list before the
	 * population is evaluated. Since this methode is invoked only once the list
	 * cannot by dynamically changed during runtime.
	 */
	public abstract List<PopulationOperator> getPreEvaluationPopulationOperators(ExampleSet input) throws OperatorException;

	/**
	 * Must return a list of <tt>PopulationOperator</tt>s. All operators are
	 * applied to the population in their order within the list after the
	 * population is evaluated. Since this methode is invoked only once the list
	 * cannot by dynamically changed during runtime.
	 */
	public abstract List<PopulationOperator> getPostEvaluationPopulationOperators(ExampleSet input) throws OperatorException;

	/**
	 * Has to return true if the main loop can be stopped because a solution is
	 * concidered to be good enough according to some criterion.
	 */
	public abstract boolean solutionGoodEnough(Population pop) throws OperatorException;

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		return new LastInnerOperatorCondition(new Class[] { ExampleSet.class}, new Class[] { PerformanceVector.class });
	}

    protected RandomGenerator getRandom() {
        return random;
    }
    
    protected Population getPopulation() {
    	return population;
    }
    
	/**
	 * Applies the feature operator:
	 * <ol>
	 * <li>collects the pre- and postevaluation operators
	 * <li>create an initial population
	 * <li>evaluate the initial population
	 * <li>loop as long as solution is not good enough
	 * <ol>
	 * <li>apply all pre evaluation operators
	 * <li>evaluate the population
	 * <li>update the population's best individual
	 * <li>apply all post evaluation operators
	 * </ol>
	 * <li>return all generation's best individual
	 * </ol>
	 */
	public IOObject[] apply() throws OperatorException {
		// init
        this.random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		this.evaluationCounter = 0;
		this.totalEvaluations = 0;
		this.maximalFitness = getParameterAsDouble(PARAMETER_MAXIMAL_FITNESS);

		ExampleSet es = getInput(ExampleSet.class);

		if (es.getAttributes().size() == 0) {
			throw new UserError(this, 125, 0, 1);
		}
		
		List preOps = getPreEvaluationPopulationOperators(es);
		List postOps = getPostEvaluationPopulationOperators(es);

		// stop dialog
		boolean userDialogOk = true;
		StopDialog stopDialog = null;
		if (getParameterAsBoolean(PARAMETER_SHOW_STOP_DIALOG)) {
			stopDialog = new StopDialog("Stop Dialog", "<html>Press the stop button to abort the search for best feature space.<br>" + "The best individual found so far is returned.</html>");
			stopDialog.setVisible(true);
		}

		// create initial population
		population = createInitialPopulation(es);
		log("Initial population has " + population.getNumberOfIndividuals() + " individuals.");
		evaluate(population);

		// population plotter
		PopulationPlotter popPlotter = null;
		population.updateEvaluation();
		if (getParameterAsBoolean(PARAMETER_SHOW_POPULATION_PLOTTER)) {
			popPlotter = new PopulationPlotter(getParameterAsInt(PARAMETER_PLOT_GENERATIONS), getParameterAsBoolean(PARAMETER_CONSTRAINT_DRAW_RANGE), getParameterAsBoolean(PARAMETER_DRAW_DOMINATED_POINTS));
			popPlotter.operate(population);
		}
		inApplyLoop();

		// optimization loop
		while (userDialogOk && !solutionGoodEnough(population) && !isMaximumReached()) {
			population.nextGeneration();

			applyOpList(preOps, population);

			log(Tools.ordinalNumber(population.getGeneration()) + " generation has " + population.getNumberOfIndividuals() + " individuals.");
			log("Evaluating " + Tools.ordinalNumber(population.getGeneration()) + " population.");

			evaluate(population);
			population.updateEvaluation();
			applyOpList(postOps, population);
			if (popPlotter != null) {
				popPlotter.operate(population);
			}
			userDialogOk = stopDialog == null ? true : stopDialog.isStillRunning();
            inApplyLoop();
		}

		if (stopDialog != null) {
			stopDialog.setVisible(false);
			stopDialog.dispose();
		}

		// optimization finished
		applyOpList(postOps, population);
		log("Optimization finished. " + evaluationCounter + " / " + totalEvaluations + " evaluations performed.");

		// write criteria data of the final population into a file
		if (isParameterSet(PARAMETER_POPULATION_CRITERIA_DATA_FILE)) {
			SimpleDataTable finalStatistics = PopulationPlotter.createDataTable(population);
			PopulationPlotter.fillDataTable(finalStatistics, new HashMap<String, ExampleSet>(), population, getParameterAsBoolean(PARAMETER_DRAW_DOMINATED_POINTS));
			File outFile = getParameterAsFile(PARAMETER_POPULATION_CRITERIA_DATA_FILE);
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

		// create result example set
		Individual bestEver = null;
		if (getParameterAsBoolean(PARAMETER_USER_RESULT_INDIVIDUAL_SELECTION)) {
			IndividualSelector selector = new IndividualSelector(population);
			selector.setVisible(true);
			bestEver = selector.getSelectedIndividual();
			if (bestEver == null)
				logWarning("No individual selected. Using individual with highest fitness for main criterion...");
		}
		if (bestEver == null) {
			bestEver = population.getBestIndividualEver();
		}
		
		// create resulting weights
		AttributeWeightedExampleSet weightedResultSet = bestEver.getExampleSet();
		for (Attribute attribute : weightedResultSet.getAttributes()) {
			if (Double.isNaN(weightedResultSet.getWeight(attribute)))
				weightedResultSet.setWeight(attribute, 1.0d);
		}
		AttributeWeights weights = weightedResultSet.getAttributeWeights();
		Iterator<String> n = weights.getAttributeNames().iterator();
		while (n.hasNext()) {
			String name = n.next();
			if (weightedResultSet.getAttributes().get(name) == null) {
				weights.setWeight(name, 0.0d);
			}
		}
		
		// normalize weights
		weights.normalize();
		
		return new IOObject[] { weightedResultSet.createCleanClone(), weights, bestEver.getPerformance() };
	}

	/** Applies all PopulationOperators in opList to the population. */
	void applyOpList(List opList, Population population) throws OperatorException {
		Iterator i = opList.listIterator();
		while (i.hasNext()) {
			PopulationOperator op = (PopulationOperator) i.next();
			if (op.performOperation(population.getGeneration())) {
				try {
					op.operate(population);
					for (int k = 0; k < population.getNumberOfIndividuals(); k++) {
						if (population.get(k).getExampleSet().getNumberOfUsedAttributes() <= 0) {
							logError("Population operator " + op + " has produced an example set without attributes!");
						}
					}
				} catch (Exception e) {
					throw new UserError(this, e, 108, e.toString());
				}
			}
		}
	}

	/**
	 * Evaluates all individuals in the population by applying the inner
	 * operators.
	 */
	protected void evaluate(Population population) throws OperatorException {
		for (int i = 0; i < population.getNumberOfIndividuals(); i++) {
			evaluate(population.get(i));
		}
	}

	/**
	 * Evaluates the given individual. The performance is set as user data of
	 * the individual and also returned by this method.
	 */
	protected PerformanceVector evaluate(Individual individual) throws OperatorException {
		totalEvaluations++;
		if (individual.getPerformance() != null) {
			return individual.getPerformance();
		} else {
			evaluationCounter++;
			AttributeWeightedExampleSet clone = individual.getExampleSet().createCleanClone();
			IOObject[] operatorChainInput = new IOObject[] { clone };
			IOContainer innerResult = getInput().prepend(operatorChainInput);
			for (int i = 0; i < getNumberOfOperators(); i++) {
				innerResult = getOperator(i).apply(innerResult);
			}

			PerformanceVector performanceVector = innerResult.remove(PerformanceVector.class);
			individual.setPerformance(performanceVector);
			return performanceVector;
		}
	}

	/** This method checks if the maximum was reached for the main criterion. */
	private boolean isMaximumReached() {
		if (checkForMaximalFitness) {
			PerformanceVector pv = population.getBestPerformanceEver();
			if (pv == null) {
				return false;
			} else {
				if (pv.getMainCriterion().getFitness() == Double.POSITIVE_INFINITY)
					return true;
				else if (pv.getMainCriterion().getMaxFitness() == pv.getMainCriterion().getFitness())
					return true;
				else
					return pv.getMainCriterion().getFitness() >= maximalFitness;
			}
		} else {
			return false;
		}
	}

	/**
	 * Sets if the operator should check if the maximum was reached for the main
	 * criterion. Subclasses may want to set this to false, e.g. for
	 * multiobjective optimization.
	 */
	protected void setCheckForMaximum(boolean checkForMaximalFitness) {
		this.checkForMaximalFitness = checkForMaximalFitness;
	}

	/**
	 * Returns if the operator should check if the maximum was reached for the
	 * main criterion. Subclasses may want to set this to false, e.g. for
	 * multiobjective optimization.
	 */
	protected boolean getCheckForMaximum() {
		return this.checkForMaximalFitness;
	}

	/**
	 * Returns the highest possible value for the maximum number of innner
	 * operators.
	 */
	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	/** Returns 0 for the minimum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 1;
	}	
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
		ParameterType type = new ParameterTypeBoolean(PARAMETER_SHOW_STOP_DIALOG, "Determines if a dialog with a button should be displayed which stops the run: the best individual is returned.", false);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_USER_RESULT_INDIVIDUAL_SELECTION, "Determines if the user wants to select the final result individual from the last population.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_SHOW_POPULATION_PLOTTER, "Determines if the current population should be displayed in performance space.", false));
		types.add(new ParameterTypeInt(PARAMETER_PLOT_GENERATIONS, "Update the population plotter in these generations.", 1, Integer.MAX_VALUE, 10));
		types.add(new ParameterTypeBoolean(PARAMETER_CONSTRAINT_DRAW_RANGE, "Determines if the draw range of the population plotter should be constrained between 0 and 1.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_DRAW_DOMINATED_POINTS, "Determines if only points which are not Pareto dominated should be painted.", true));
		types.add(new ParameterTypeFile(PARAMETER_POPULATION_CRITERIA_DATA_FILE, "The path to the file in which the criteria data of the final population should be saved.", "cri", true));
		types.add(new ParameterTypeDouble(PARAMETER_MAXIMAL_FITNESS, "The optimization will stop if the fitness reaches the defined maximum.", 0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
		return types;
	}
}
