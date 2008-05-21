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

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.UndefinedParameterError;

import Jama.Matrix;

/**
 *  <p>This operator calculates a linear regression model. It uses the Akaike criterion 
 *  for model selection.</p>
 *
 * @author Ingo Mierswa
 * @version $Id: LinearRegression.java,v 1.7 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class LinearRegression extends AbstractLearner {


	/** The parameter name for &quot;The feature selection method used during regression.&quot; */
	public static final String PARAMETER_FEATURE_SELECTION = "feature_selection";

	/** The parameter name for &quot;Indicates if the algorithm should try to delete colinear features during the regression.&quot; */
	public static final String PARAMETER_ELIMINATE_COLINEAR_FEATURES = "eliminate_colinear_features";

	/** The parameter name for &quot;The minimum standardized coefficient for the removal of colinear feature elimination.&quot; */
	public static final String PARAMETER_MIN_STANDARDIZED_COEFFICIENT = "min_standardized_coefficient";

	/** The parameter name for &quot;The ridge parameter used during ridge regression.&quot; */
	public static final String PARAMETER_RIDGE = "ridge";
	/** Attribute selection methods */
	public static final String[] FEATURE_SELECTION_METHODS = { 
		"none",
		"M5 prime",
		"greedy"
	};
	
	/** Attribute selection method: No attribute selection */
	public static final int NO_SELECTION = 0;
	
	/** Attribute selection method: M5 method */
	public static final int M5_PRIME = 1;

	/** Attribute selection method: Greedy method */
	public static final int GREEDY = 2;

	
	public LinearRegression(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {
		Attribute label = exampleSet.getAttributes().getLabel();
		if (label.isNominal()) {
			throw new UserError(this, 102, "linear regression", label.getName());
		}
		
		// start with all attributes
		int numberOfAttributes = exampleSet.getAttributes().size();
		boolean[] attributeSelection = new boolean[numberOfAttributes];
		int counter = 0;
		String[] attributeNames = new String[numberOfAttributes];
		for (Attribute attribute : exampleSet.getAttributes()) {
			attributeSelection[counter] = !attribute.isNominal();
			attributeNames[counter] = attribute.getName();
			counter++;
		}

		// compute and store statistics and turn off attributes with std. dev. = 0
		exampleSet.recalculateAllAttributeStatistics();
		double[] means = new double[numberOfAttributes];
		double[] standardDeviations = new double[numberOfAttributes];
		counter = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attributeSelection[counter]) {
				means[counter] = exampleSet.getStatistics(attribute, Statistics.AVERAGE);
				standardDeviations[counter] = Math.sqrt(exampleSet.getStatistics(attribute, Statistics.VARIANCE));
				if (standardDeviations[counter] == 0) {
					attributeSelection[counter] = false;
				}
			}
			counter++;
		}

		double labelMean = exampleSet.getStatistics(label, Statistics.AVERAGE);
		double classStandardDeviation = Math.sqrt(exampleSet.getStatistics(label, Statistics.VARIANCE));
		
		int numberOfExamples = exampleSet.size();
		double[] coefficients = new double[numberOfAttributes + 1];

		// perform a regression and remove colinear attributes
		do {
			coefficients = performRegression(exampleSet, attributeSelection, means, labelMean);
		} while (getParameterAsBoolean(PARAMETER_ELIMINATE_COLINEAR_FEATURES) && deselectAttributeWithHighestCoefficient(attributeSelection, coefficients, standardDeviations, classStandardDeviation));

		// determine the current number of attributes + 1
		int currentlySelectedAttributes = 1;
		for (int i = 0; i < attributeSelection.length; i++) {
			if (attributeSelection[i]) {
				currentlySelectedAttributes++;
			}
		}

		double error = getSquaredError(exampleSet, attributeSelection, coefficients);
		double akaike = (numberOfExamples - currentlySelectedAttributes) + 2 * currentlySelectedAttributes;

		boolean improved;
		int currentNumberOfAttributes = currentlySelectedAttributes;
		switch (getParameterAsInt(PARAMETER_FEATURE_SELECTION)) {
		case GREEDY:
			do {
				boolean[] currentlySelected = attributeSelection.clone();
				improved = false;
				currentNumberOfAttributes--;
				for (int i = 0; i < attributeSelection.length; i++) {
					if (currentlySelected[i]) {
						// calculate the akaike value without this attribute
						currentlySelected[i] = false;
						double[] currentCoeffs = performRegression(exampleSet, currentlySelected, means, labelMean);
						double currentMSE = getSquaredError(exampleSet, currentlySelected, currentCoeffs);
						double currentAkaike = currentMSE / error * (numberOfExamples - currentlySelectedAttributes) + 2 * currentNumberOfAttributes;
						
						// if the value is improved compared to the current best
						if (currentAkaike < akaike) {
							improved = true;
							akaike = currentAkaike;
							System.arraycopy(currentlySelected, 0, attributeSelection, 0, attributeSelection.length);
							coefficients = currentCoeffs;
						}
						currentlySelected[i] = true;
					}
				}
			} while (improved);
			break;
		case M5_PRIME:
			// attribute removal as in M5 prime
			do {
				improved = false;
				currentNumberOfAttributes--;

				// find the attribute with the smallest standardized coefficient
				double minStadardizedCoefficient = 0;
				int attribute2Deselect = -1;
				int coefficientIndex = 0;
				for (int i = 0; i < attributeSelection.length; i++) {
					if (attributeSelection[i]) {
						double standardizedCoefficient = Math.abs(coefficients[coefficientIndex] * standardDeviations[i] / classStandardDeviation);
						if ((coefficientIndex == 0) || (standardizedCoefficient < minStadardizedCoefficient)) {
							minStadardizedCoefficient = standardizedCoefficient;
							attribute2Deselect = i;
						}
						coefficientIndex++;
					}
				}

				// See whether removing it improves the Akaike score
				if (attribute2Deselect >= 0) {
					attributeSelection[attribute2Deselect] = false;
					double[] currentCoefficients = performRegression(exampleSet, attributeSelection, means, labelMean);
					double currentError = getSquaredError(exampleSet, attributeSelection, currentCoefficients);
					double currentAkaike = currentError / error * (numberOfExamples - currentlySelectedAttributes) + 2 * currentNumberOfAttributes;

					// If it is better than the current best
					if (currentAkaike < akaike) {
						improved = true;
						akaike = currentAkaike;
						coefficients = currentCoefficients;
					} else {
						attributeSelection[attribute2Deselect] = true;
					}
				}
			} while (improved);
			break;
		case NO_SELECTION:
			break;
		}

		return new LinearRegressionModel(exampleSet, attributeSelection, coefficients);
	}
	
	/** This method removes the attribute with the highest standardized coefficient
	 *  greater than the minimum coefficient parameter. Checks only those attributes 
	 *  which are currently selected. Returns true if an attribute was actually 
	 *  deselected and false otherwise. */
	private boolean deselectAttributeWithHighestCoefficient(boolean[] selectedAttributes, double[] coefficients, double[] standardDeviations, double classStandardDeviation) throws UndefinedParameterError {
		double minCoefficient = getParameterAsDouble(PARAMETER_MIN_STANDARDIZED_COEFFICIENT);
		int attribute2Deselect = -1;
		int coefficientIndex = 0;
		for (int i = 0; i < selectedAttributes.length; i++) {
			if (selectedAttributes[i]) {
				double standardizedCoefficient = Math.abs(coefficients[coefficientIndex] * standardDeviations[i] / classStandardDeviation);
				if (standardizedCoefficient > minCoefficient) {
					minCoefficient = standardizedCoefficient;
					attribute2Deselect = i;
				}
				coefficientIndex++;
			}
		}
		if (attribute2Deselect >= 0) {
			selectedAttributes[attribute2Deselect] = false;
			return true;
		}
		return false;
	}

	/** Calculates the squared error of a regression model on the training data. */
	private double getSquaredError(ExampleSet exampleSet, boolean[] selectedAttributes, double[] coefficients) {
		double error = 0;
		Iterator<Example> i = exampleSet.iterator();
		while (i.hasNext()) {
			Example example = i.next();
			double prediction = regressionPrediction(example, selectedAttributes, coefficients);
			double diff = prediction - example.getLabel();
			error += diff * diff;	
		}
		return error;
	}

	/** Calculates the prediction for the given example. */
	private double regressionPrediction(Example example, boolean[] selectedAttributes, double[] coefficients) {
		double prediction = 0;
		int index = 0;
		int counter = 0;
		for (Attribute attribute : example.getAttributes()) {
			if (selectedAttributes[counter++]) {
				prediction += coefficients[index] * example.getValue(attribute);
				index++;
			}
		}
		prediction += coefficients[index];
		return prediction;
	}

	/** Calculate a linear regression only from the selected attributes. The method returns the
	 *  calculated coefficients. */
	private double[] performRegression(ExampleSet exampleSet, boolean[] selectedAttributes, double[] means, double labelMean) throws UndefinedParameterError {
		int currentlySelectedAttributes = 0;
		for (int i = 0; i < selectedAttributes.length; i++) {
			if (selectedAttributes[i]) {
				currentlySelectedAttributes++;
			}
		}

		Matrix independent = null, dependent = null;
		double[] weights = null;
		if (currentlySelectedAttributes > 0) {
			independent = new Matrix(exampleSet.size(), currentlySelectedAttributes);
			dependent = new Matrix(exampleSet.size(), 1);
			int exampleIndex = 0;
			Iterator<Example> i = exampleSet.iterator();
			weights = new double[exampleSet.size()];
			Attribute weightAttribute = exampleSet.getAttributes().getWeight();
			while (i.hasNext()) {
				Example example = i.next();
				int attributeIndex = 0;
				dependent.set(exampleIndex, 0, example.getLabel());
				int counter = 0;
				for (Attribute attribute : exampleSet.getAttributes()) {
					if (selectedAttributes[counter]) {
						double value = example.getValue(attribute) - means[counter];
						independent.set(exampleIndex, attributeIndex, value);
						attributeIndex++;
					}
					counter++;
				}
				if (weightAttribute != null)
					weights[exampleIndex] = example.getValue(weightAttribute);
				else
					weights[exampleIndex] = 1.0d;
				exampleIndex++;
			}
		}

		// compute coefficients without intercept (due to ridge regression)
		double[] coefficients = new double[currentlySelectedAttributes + 1];
		if (currentlySelectedAttributes > 0) {
			double[] coefficientsWithoutIntercept = 
				(new com.rapidminer.tools.math.LinearRegression(independent, 
						                                         dependent, 
						                                         weights, 
						                                         getParameterAsDouble(PARAMETER_RIDGE))).getCoefficients();
			System.arraycopy(coefficientsWithoutIntercept, 0, coefficients, 0, currentlySelectedAttributes);
		}
		// set intercept to class mean
		coefficients[currentlySelectedAttributes] = labelMean;

		// convert coefficients into original scale
		int coefficientIndex = 0;
		for (int i = 0; i < selectedAttributes.length; i++) {
			if (selectedAttributes[i]) {
				// We have centred the input
				coefficients[coefficients.length - 1] -= coefficients[coefficientIndex] * means[i];
				coefficientIndex++;
			}
		}
		
		return coefficients;
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
		types.add(new ParameterTypeCategory(PARAMETER_FEATURE_SELECTION, "The feature selection method used during regression.", FEATURE_SELECTION_METHODS, M5_PRIME));
		types.add(new ParameterTypeBoolean(PARAMETER_ELIMINATE_COLINEAR_FEATURES, "Indicates if the algorithm should try to delete colinear features during the regression.", true));
		types.add(new ParameterTypeDouble(PARAMETER_MIN_STANDARDIZED_COEFFICIENT, "The minimum standardized coefficient for the removal of colinear feature elimination.", 0.0d, Double.POSITIVE_INFINITY, 1.5d));
		types.add(new ParameterTypeDouble(PARAMETER_RIDGE, "The ridge parameter used during ridge regression.", 0.0d, Double.POSITIVE_INFINITY, 1.0E-8));
		return types;
	}
}
