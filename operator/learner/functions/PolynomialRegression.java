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
package com.rapidminer.operator.learner.functions;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.LoggingHandler;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.optimization.ec.es.ESOptimization;
import com.rapidminer.tools.math.optimization.ec.es.Individual;
import com.rapidminer.tools.math.optimization.ec.es.OptimizationValueType;

/**
 * <p>This regression learning operator fits a polynomial of all attributes to
 * the given data set. If the data set contains a label Y and three attributes
 * X1, X2, and X3 a function of the form<br />
 * <br />
 * <code>Y = w0 + w1 * X1 ^ d1 + w2 * X2 ^ d2 + w3 * X3 ^ d3</code><br />
 * <br />
 * will be fitted to the training data.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: PolynomialRegression.java,v 1.1 2008/07/31 17:07:14 ingomierswa Exp $
 */
public class PolynomialRegression extends AbstractLearner {

	private static final String PARAMETER_MAX_ITERATIONS = "max_iterations";
	
	private static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	private static final String PARAMETER_MAX_DEGREE = "max_degree";
	
	private static final String PARAMETER_REPLICATION_FACTOR = "replication_factor";
	
	
	private static class RegressionOptimization extends ESOptimization {

		private int replicationFactor;
		
		private ExampleSet exampleSet;
		
		private Attribute label;
		
		public RegressionOptimization(ExampleSet exampleSet,
									  int replicationFactor,				
									  int maxIterations,
									  double maxDegree,
									  RandomGenerator random, 
									  LoggingHandler logging) {
			super(getMinVector(exampleSet, replicationFactor), getMaxVector(exampleSet, replicationFactor, maxDegree), 
					1, exampleSet.getAttributes().size() * 2 * replicationFactor + 1, ESOptimization.INIT_TYPE_RANDOM,
					maxIterations, maxIterations, ESOptimization.TOURNAMENT_SELECTION,
					1.0, true, ESOptimization.GAUSSIAN_MUTATION, 0.01d, 0.0d, false,
					random, logging);
			
			
			this.replicationFactor = replicationFactor;
			this.exampleSet = exampleSet;
			this.label = exampleSet.getAttributes().getLabel();
			
			int index = 0;
			for (int a = 0; a < exampleSet.getAttributes().size(); a++) {
				for (int f = 0; f < replicationFactor; f++) {
					setValueType(index++, OptimizationValueType.VALUE_TYPE_DOUBLE);
					setValueType(index++, OptimizationValueType.VALUE_TYPE_INT);
				}
			}
			setValueType(exampleSet.getAttributes().size() * replicationFactor * 2, OptimizationValueType.VALUE_TYPE_DOUBLE);
		}

		private static double[] getMinVector(ExampleSet exampleSet, int replicationFactor) {
			double[] result = new double[exampleSet.getAttributes().size() * replicationFactor * 2 + 1];
			int index = 0;
			for (int a = 0; a < exampleSet.getAttributes().size(); a++) {
				for (int f = 0; f < replicationFactor; f++) {
					result[index++] = -100;
					result[index++] = 1;
				}
			}
			result[result.length - 1] = -100;			
			return result;
		}

		private static double[] getMaxVector(ExampleSet exampleSet, int replicationFactor, double maxDegree) {
			double[] result = new double[exampleSet.getAttributes().size() * replicationFactor * 2 + 1];
			int index = 0;
			for (int a = 0; a < exampleSet.getAttributes().size(); a++) {
				for (int f = 0; f < replicationFactor; f++) {
					result[index++] = 100;
					result[index++] = maxDegree;
				}
			}
			result[result.length - 1] = 100;			
			return result;
		}
		
		public PerformanceVector evaluateIndividual(Individual individual) throws OperatorException {
			double[] values = individual.getValues();
			double[][] coefficients = getCoefficients(values);
			double[][] degrees = getDegrees(values);
			double offset = getOffset(values);
			
			double error = 0.0d;
			for (Example example : exampleSet) {
				double prediction = PolynomialRegressionModel.calculatePrediction(example, coefficients, degrees, offset);
				double diff = Math.abs(example.getValue(label) - prediction);
				error += diff;
			}
			
			PerformanceVector performanceVector = new PerformanceVector();
			performanceVector.addCriterion(new EstimatedPerformance("Polynomial Regression Error", error, 1, true));
			return performanceVector;
		}	
		
		public double[][] getCoefficients(double[] values) {
			double[][] coefficients = new double[replicationFactor][exampleSet.getAttributes().size()];
			for (int a = 0; a < exampleSet.getAttributes().size(); a++) {
				for (int f = 0; f < replicationFactor; f++) {
					coefficients[f][a] = values[a + f * 2];
				}
			}
			return coefficients;
		}
		
		public double[][] getDegrees(double[] values) {
			double[][] degrees = new double[replicationFactor][exampleSet.getAttributes().size()];
			for (int a = 0; a < exampleSet.getAttributes().size(); a++) {
				for (int f = 0; f < replicationFactor; f++) {
					degrees[f][a] = values[a + f * 2 + 1];
				}
			}
			return degrees;
		}
		
		public double getOffset(double[] values) {
			return values[values.length - 1];
		}
	}
	
	public PolynomialRegression(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {
		RegressionOptimization optimization = 
			new RegressionOptimization(exampleSet, 
					                   getParameterAsInt(PARAMETER_REPLICATION_FACTOR), 
					                   getParameterAsInt(PARAMETER_MAX_ITERATIONS), 
					                   getParameterAsInt(PARAMETER_MAX_DEGREE), 
					                   RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED)), 
					                   this);
		
		optimization.optimize();
		double[] values = optimization.getBestValuesEver();
		
		double[][] coefficients = optimization.getCoefficients(values);
		double[][] degrees = optimization.getDegrees(values);
		double offset = optimization.getOffset(values);
		
		return new PolynomialRegressionModel(exampleSet, coefficients, degrees, offset);
	}

	public boolean supportsCapability(LearnerCapability lc) {
		if (lc.equals(LearnerCapability.NUMERICAL_ATTRIBUTES))
			return true;
		if (lc.equals(LearnerCapability.NUMERICAL_CLASS))
			return true;
        if (lc == LearnerCapability.WEIGHTED_EXAMPLES)
            return true;
		return false;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_MAX_ITERATIONS, "The maximum number of iterations used for model fitting.", 1, Integer.MAX_VALUE, 100);
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeInt(PARAMETER_MAX_DEGREE, "The maximal degree used for the final polynomial.", 1, Integer.MAX_VALUE, 5);
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeInt(PARAMETER_REPLICATION_FACTOR, "The amount of times each input variable is replicated, i.e. how many different degrees and coefficients can be applied to each variable", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		
        type = new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1);
        types.add(type);
		return types;
	}
}
