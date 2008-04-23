/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.learner.functions;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.tools.LoggingHandler;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.optimization.ec.es.ESOptimization;
import com.rapidminer.tools.math.optimization.ec.es.Individual;


/**
 * Evolutionary Strategy approach for optimization of the logistic regression problem. 
 * 
 * @author Ingo Mierswa
 * @version $Id: LogisticRegressionOptimization.java,v 1.4 2007/07/14 12:31:38 ingomierswa Exp $
 */
public class LogisticRegressionOptimization extends ESOptimization {    
    
    private ExampleSet exampleSet;
    
    private Attribute label;
    
    private Attribute weight;
    
    /** Creates a new evolutionary SVM optimization. */
    public LogisticRegressionOptimization(
            ExampleSet exampleSet, // training data
            int initType, // start population creation type para
            int maxIterations, int generationsWithoutImprovement, int popSize, // GA paras
            int selectionType, double tournamentFraction, boolean keepBest, // selection paras
            int mutationType, // type of mutation
            double crossoverProb,
            boolean showConvergencePlot,
            RandomGenerator random,
            LoggingHandler logging) {

        super(-1.0d, 1.0d, popSize, exampleSet.getAttributes().size() + 1, 
              initType, maxIterations, generationsWithoutImprovement, selectionType, tournamentFraction, keepBest,
              mutationType, crossoverProb, showConvergencePlot, random, logging);
        
        this.exampleSet = exampleSet;
        this.label  = exampleSet.getAttributes().getLabel();
        this.weight = exampleSet.getAttributes().getWeight();
    }
    
    public PerformanceVector evaluateIndividual(Individual individual) {
        double[] beta = individual.getValues();
        
        double fitness = 0.0d;
        for (Example example : exampleSet) {
            double eta = 0.0d;
            int i = 0;
            for (Attribute attribute : example.getAttributes()) {
                double value = example.getValue(attribute);
                eta += beta[i] * value;
                i++;
            }
            eta += beta[beta.length - 1];
            double pi = Math.exp(eta) / (1 + Math.exp(eta));
            
            double classValue = example.getValue(label);
            double currentFitness = classValue * Math.log(pi) + (1 - classValue) * Math.log(1 - pi);
            double weightValue = 1.0d;
            if (weight != null)
                weightValue = example.getValue(weight);
            fitness += weightValue * currentFitness;
        }
        
        PerformanceVector performanceVector = new PerformanceVector();
        performanceVector.addCriterion(new EstimatedPerformance("log_reg_fitness", fitness, 1, false));
        return performanceVector;
    }

    public LogisticRegressionModel train() throws OperatorException {
        optimize();
        return new LogisticRegressionModel(this.exampleSet, getBestValuesEver());
    }
}
