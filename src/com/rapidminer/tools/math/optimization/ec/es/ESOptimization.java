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
package com.rapidminer.tools.math.optimization.ec.es;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.gui.plotter.SimplePlotterDialog;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.tools.LoggingHandler;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.optimization.Optimization;


/**
 * Evolutionary Strategy approach for all real-valued optimization tasks.
 * 
 * @author Ingo Mierswa
 * @version $Id: ESOptimization.java,v 1.6 2008/05/09 19:23:16 ingomierswa Exp $
 */
public abstract class ESOptimization implements Optimization {
    
    /** The names of all available selection schemes. */
    public static final String[] SELECTION_TYPES = { "uniform", "cut", "roulette wheel", "stochastic universal sampling", "Boltzmann", "rank", "tournament", "non dominated sorting" };

    /** Indicates a uniform sampling selection scheme. */
    public static final int UNIFORM_SELECTION = 0;

    /** Indicates a cut selection scheme. */
    public static final int CUT_SELECTION = 1;

    /** Indicates a roulette wheel selection scheme. */
    public static final int ROULETTE_WHEEL = 2;

    /** Indicates a stochastic universal sampling selection scheme. */
    public static final int STOCHASTIC_UNIVERSAL = 3;

    /** Indicates a Boltzmann selection scheme. */
    public static final int BOLTZMANN_SELECTION = 4;

    /** Indicates a rank based selection scheme. */
    public static final int RANK_SELECTION = 5;

    /** Indicates a tournament selection scheme. */
    public static final int TOURNAMENT_SELECTION = 6;

    /** Indicates a multi-objective selection scheme (NSGA II). */
    public static final int NON_DOMINATED_SORTING_SELECTION = 7;
    
    /** The names of the mutation types. */
    public static final String[] MUTATION_TYPES = { "none", "gaussian_mutation", "switching_mutation", "sparsity_mutation" };

    /** Indicates no mutation. */
    public static final int NO_MUTATION = 0;

    /** Indicates a gaussian mutation. */
    public static final int GAUSSIAN_MUTATION = 1;

    /** Indicates a switching mutation. */
    public static final int SWITCHING_MUTATION = 2;

    /** Indicates a hybrid between switching mutation and Gaussian mutation. */
    public static final int SPARSITY_MUTATION = 3;

    /** The names of the initialization types. */
    public static final String[] POPULATION_INIT_TYPES = { "random", "min", "max" };

    /** Indicates that the start population should be randomly initialized. */
    public static final int INIT_TYPE_RANDOM = 0;

    /** Indicates that the start population should be initialized with the minimum value. */
    public static final int INIT_TYPE_MIN = 1;
    
    /** Indicates that the start population should be initialized with the maximum value. */
    public static final int INIT_TYPE_MAX = 2;

    /** This parameter indicates the minimum value for all genes. */
    private double[] min;

    /** This parameter indicates the maximum value for all genes. */
    private double[] max;

    /** The value types, either DOUBLE (default) or INT. */
    private OptimizationValueType[] valueTypes;
    
    /** The number of individuals. */
    private int populationSize;
    
    /** The dimension of each individual. */
    private int individualSize;
    
    /** The maximum number of generations. */
    private int maxGenerations;

    /** The maximum numbers of generations without improvement. */
    private int generationsWithoutImprovement;

    /** The type of start population initialization. */
    private int initType = INIT_TYPE_RANDOM;

    /** The type of the mutation. */
    private int mutationType = GAUSSIAN_MUTATION;
    
    /** The population plotter (if enabled). */
    private PopulationPlotter populationPlotter = null;
    
    /** The mutation operator. */
    private Mutation mutation;
    
    /** The current population. */
    private Population population;

    /** Population operators. */
    private Collection<PopulationOperator> popOps;

    /** Indicates if a convergence plot should be drawn. */
    private boolean showPlot = false;

    /** This field counts the total number of evaluations during optimization. */
    private int totalEvalCounter = 0;

    /** This field counts the number of actually calculated evaluations (unchanged individuals do not have
     *  to be re-evaluated). */
    private int actualEvalCounter = 0;

    /** The random number generator. */
    private RandomGenerator random;
    
    private LoggingHandler logging;
    
    /** Creates a new evolutionary SVM optimization. */
    public ESOptimization(double minValue, double maxValue,
                          int populationSize, int individualSize, int initType, // population paras
                          int maxGenerations, int generationsWithoutImprovement, // GA paras
                          int selectionType, double tournamentFraction, boolean keepBest, // selection paras
                          int mutationType, // type of mutation
                          double crossoverProb,
                          boolean showPlot,
                          RandomGenerator random,
                          LoggingHandler logging) {
        this(createBoundArray(minValue, individualSize), createBoundArray(maxValue, individualSize), 
             populationSize, individualSize, initType, maxGenerations,
             generationsWithoutImprovement, selectionType, tournamentFraction, keepBest,
             mutationType, crossoverProb, showPlot, random, logging);
    }
        
    /** Creates a new evolutionary SVM optimization. */
    public ESOptimization(double[] minValues, double[] maxValues,
            int populationSize, int individualSize, int initType, // population paras
            int maxGenerations, int generationsWithoutImprovement, // GA paras
            int selectionType, double tournamentFraction, boolean keepBest, // selection paras
            int mutationType, // type of mutation
            double crossoverProb,
            boolean showPlot,
            RandomGenerator random,
            LoggingHandler logging) {
        this.logging = logging;
        this.random = random;
        this.showPlot = showPlot;
        this.populationSize = populationSize;
        this.individualSize = individualSize;
        this.min = minValues;
        this.max = maxValues;
        this.valueTypes = new OptimizationValueType[individualSize];
        for (int i = 0; i <  this.valueTypes.length; i++) {
        	this.valueTypes[i] = OptimizationValueType.VALUE_TYPE_DOUBLE;
        }
        this.initType = initType;
        this.maxGenerations = maxGenerations;
        this.generationsWithoutImprovement = generationsWithoutImprovement < 1 ? this.maxGenerations : generationsWithoutImprovement;
        this.mutationType = mutationType;

        // population operators
        popOps = new LinkedList<PopulationOperator>();
        switch (selectionType) {
            case UNIFORM_SELECTION:
                popOps.add(new UniformSelection(populationSize, keepBest, random));
                break;
            case CUT_SELECTION:
                popOps.add(new CutSelection(populationSize));
                break;
            case ROULETTE_WHEEL:
                popOps.add(new RouletteWheel(populationSize, keepBest, random));
                break;
            case STOCHASTIC_UNIVERSAL:
                popOps.add(new StochasticUniversalSampling(populationSize, keepBest, random));
                break;
            case BOLTZMANN_SELECTION:
                popOps.add(new BoltzmannSelection(populationSize, 1.0d, this.maxGenerations, true, keepBest, random));
                break;
            case RANK_SELECTION:
                popOps.add(new RankSelection(populationSize, keepBest, random));
                break;
            case TOURNAMENT_SELECTION:
                popOps.add(new TournamentSelection(populationSize, tournamentFraction, keepBest, random));     
                break;
            case NON_DOMINATED_SORTING_SELECTION:
                popOps.add(new NonDominatedSortingSelection(populationSize));
                this.populationPlotter = new PopulationPlotter();
                popOps.add(this.populationPlotter);
                break;
        }
        popOps.add(new Crossover(crossoverProb, random));
        switch (mutationType) {
            case GAUSSIAN_MUTATION:
                GaussianMutation gm = new GaussianMutation(new double[0], this.min, this.max, this.valueTypes, random);
                popOps.add(gm);
                popOps.add(new VarianceAdaption(gm, individualSize, this.logging));
                recalculateSigma(gm, this.individualSize);
                this.mutation = gm;
                break;
            case SWITCHING_MUTATION:
                this.mutation = new SwitchingMutation(1.0d / individualSize, this.min, this.max, this.valueTypes, random);
                popOps.add(this.mutation);
                break;
            case SPARSITY_MUTATION:
                this.mutation = new SparsityMutation(1.0d / individualSize, this.min, this.max, this.valueTypes, random); 
                popOps.add(this.mutation);
                break;
            default:
                break; // no mutation at all
        }
    }

    private static double[] createBoundArray(double bound, int size) {
        double[] result = new double[size];
        for (int i = 0; i < result.length; i++)
            result[i] = bound;
        return result;
    }
    
    /**
     * Subclasses must implement this method to calculate the fitness of the
     * given individual. Please note that null might be returned for non-valid
     * individuals. The fitness will be maximized.
     */
    public abstract PerformanceVector evaluateIndividual(Individual individual) throws OperatorException;

    /**
     * This method is invoked after each evaluation. The default implementation
     * does nothing but subclasses might implement this method to support online
     * plotting or logging.
     */
    public void nextIteration() throws OperatorException {}

    public double getMin(int index) { return min[index]; }
    
    public double getMax(int index) { return max[index]; }
    
    public void setMin(int index, double v) {
        this.min[index] = v;
        if (mutationType == GAUSSIAN_MUTATION)
        	recalculateSigma((GaussianMutation)this.mutation, this.individualSize);
    }

    public void setMax(int index, double v) {
        this.max[index] = v;
        if (mutationType == GAUSSIAN_MUTATION)
        	recalculateSigma((GaussianMutation)this.mutation, this.individualSize);
    }
    
    protected void recalculateSigma(GaussianMutation mutation, int individualSize) {
    	double[] sigma = new double[individualSize];
    	for (int s = 0; s < sigma.length; s++)
    		sigma[s] = Math.abs(this.max[s] - this.min[s]) / 10.0d;  
    	mutation.setSigma(sigma);
    }
    
    public OptimizationValueType getValueType(int index) { 
    	return this.valueTypes[index]; 
    }
    
    public void setValueType(int index, OptimizationValueType type) { 
    	this.valueTypes[index] = type; 
       	mutation.setValueType(index, type);
    }
    
    // ================================================================================
    // O P T I M I Z A T I O N
    // ================================================================================

    /**
     * Starts the optimization.
     */
    public void optimize() throws OperatorException {
        this.totalEvalCounter = 0;
        this.actualEvalCounter = 0;

        switch (initType) {
            case INIT_TYPE_RANDOM:
                this.population = createRandomStartPopulation();
                break;
            case INIT_TYPE_MIN:
                this.population = createMinStartPopulation();
                break;
            case INIT_TYPE_MAX:
                this.population = createMaxStartPopulation();
                break;
            default:
                break; // this cannot happen
        }

        evaluate(population);
        DataTable dataTable = null;
        SimplePlotterDialog plotter = null;
        if (showPlot) {
            dataTable = new SimpleDataTable("Fitness vs. Generations", new String[] { "Generations", "Best Fitness", "Current Fitness" });
            plotter = new SimplePlotterDialog(dataTable, false);
            plotter.setXAxis(0);
            plotter.plotColumn(1, true);
            plotter.plotColumn(2, true);
            plotter.setVisible(true);
            dataTable.add(new SimpleDataTableRow(new double[] { 
                              0.0d, 
                              population.getBestEver().getFitness().getMainCriterion().getFitness(), 
                              population.getCurrentBest().getFitness().getMainCriterion().getFitness() 
                          }));
        }

        while (true) {
            if (population.getGeneration() >= maxGenerations) {
                logging.log("ES finished: maximum number of iterations reached.");
                break;
            }
            if (population.getGenerationsWithoutImprovement() > generationsWithoutImprovement) {
                logging.log("ES converged in generation " + population.getGeneration() + ": No improvement in last " + generationsWithoutImprovement + " generations.");
                break;
            }
            Iterator i = popOps.iterator();
            while (i.hasNext()) {
                ((PopulationOperator) i.next()).operate(population);
            }
            evaluate(population);
            if (showPlot)
                dataTable.add(new SimpleDataTableRow(new double[] { 
                                population.getGeneration(), 
                                population.getBestEver().getFitness().getMainCriterion().getFitness(), 
                                population.getCurrentBest().getFitness().getMainCriterion().getFitness() 
                              }));
            population.nextGeneration();
            nextIteration();
        }

        if (showPlot) {
            plotter.dispose();
        }

        if (populationPlotter != null) {
        	this.populationPlotter.setCreateOtherPlottersEnabled(true);
        }
        
        logging.log("ES Evaluations: " + actualEvalCounter + " / " + totalEvalCounter);
    }

    /** Evaluates the individuals of the given population. */
    private void evaluate(Population population) throws OperatorException {
        Individual currentBest = null;
        for (int i = population.getNumberOfIndividuals() - 1; i >= 0; i--) {
            Individual current = population.get(i);
            if (current.getFitness() == null) {
                PerformanceVector fitness = evaluateIndividual(current);
                if (fitness != null) {
                	current.setFitness(fitness);
                	if ((currentBest == null) || (fitness.getMainCriterion().getFitness() > currentBest.getFitness().getMainCriterion().getFitness())) {
                		currentBest = (Individual) current.clone();
                		currentBest.setFitness(current.getFitness());
                	}
                } else {
                	population.remove(current);
                }
                actualEvalCounter++;
            }
            totalEvalCounter++;
        }
        if (currentBest != null) {
            population.setCurrentBest(currentBest);
            Individual bestEver = population.getBestEver();
            if ((bestEver == null) || (currentBest.getFitness().getMainCriterion().getFitness() > bestEver.getFitness().getMainCriterion().getFitness())) {
                Individual bestEverClone = (Individual) currentBest.clone();
                bestEverClone.setFitness(currentBest.getFitness());
                population.setBestEver(bestEverClone);
            }
        }
    }    

    /** Returns the current generation. */
    public int getGeneration() {
        return population.getGeneration();
    }

    /** Returns the best fitness in the current generation. */
    public double getBestFitnessInGeneration() {
    	Individual individual = population.getCurrentBest();
    	if (individual != null) {
    		return individual.getFitnessValues()[0];
    	} else {
    		return Double.NaN;
    	}
    }

    /** Returns the best fitness ever. */
    public double getBestFitnessEver() {
    	Individual individual = population.getBestEver();
    	if (individual != null) {
    		return individual.getFitnessValues()[0];
    	} else {
    		return Double.NaN;
    	}
    }

    /** Returns the best performance vector ever. */
    public PerformanceVector getBestPerformanceEver() {
    	Individual individual = population.getBestEver();
    	if (individual != null) {
    		return individual.getFitness();
    	} else {
    		return null;
    	}
    }

    public Population getPopulation() {
    	return this.population;
    }
    
    /**
     * Returns the best values ever. Use this method after optimization to get
     * the best result. Might returns null if the optimization did not work.
     */
    public double[] getBestValuesEver() {
    	Individual individual = population.getBestEver();
    	if (individual != null) {
    		return individual.getValues();
    	} else {
    		return null;
    	}
    }
    
    // ================================================================================
    // S T A R T P O P U L A T I O N S
    // ================================================================================
    
    /** Randomly creates the initial population. */
    private Population createRandomStartPopulation() {
        Population population = new Population();
        for (int p = 0; p < this.populationSize; p++) {
            double[] alphas = new double[this.individualSize];
            for (int j = 0; j < alphas.length; j++) {
            	if (getValueType(j).equals(OptimizationValueType.VALUE_TYPE_INT)) {
            		alphas[j] = (int)Math.round(random.nextDoubleInRange(this.min[j], this.max[j]));
            	} else if (getValueType(j).equals(OptimizationValueType.VALUE_TYPE_BOUNDS)) {
            		boolean upper = random.nextBoolean();
            		if (upper) {
            			alphas[j] = this.max[j];
            		} else {
            			alphas[j] = this.min[j];
            		}
            	} else {
            		alphas[j] = random.nextDoubleInRange(this.min[j], this.max[j]);
            	}
            }
            population.add(new Individual(alphas));
        }
        return population;
    }

    /** Randomly creates the initial population. */
    private Population createMinStartPopulation() {
        Population population = new Population();
        for (int p = 0; p < this.populationSize; p++) {
            double[] alphas = new double[this.individualSize];
            for (int j = 0; j < alphas.length; j++)
                alphas[j] = this.min[j];
            population.add(new Individual(alphas));
        }
        return population;
    }

    /** Randomly creates the initial population. */
    private Population createMaxStartPopulation() {
        Population population = new Population();
        for (int p = 0; p < this.populationSize; p++) {
            double[] alphas = new double[this.individualSize];
            for (int j = 0; j < alphas.length; j++)
                alphas[j] = this.max[j];
            population.add(new Individual(alphas));
        }
        return population;
    }    
}
