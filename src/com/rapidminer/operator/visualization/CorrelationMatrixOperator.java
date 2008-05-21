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
package com.rapidminer.operator.visualization;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;


/**
 * <p>This operator calculates the correlation matrix between all attributes of the
 * input example set. Furthermore, attribute weights based on the correlations
 * can be returned. This allows the deselection of highly correlated attributes
 * with the help of an
 * {@link com.rapidminer.operator.features.selection.AttributeWeightSelection}
 * operator. If no weights should be created, this operator produces simply a
 * correlation matrix which up to now cannot be used by other operators but can
 * be displayed to the user in the result tab.</p> 
 * 
 * <p>Please note that this simple implementation
 * performs a data scan for each attribute combination and might therefore take
 * some time for non-memory example tables.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: CorrelationMatrixOperator.java,v 1.8 2006/04/14 15:04:22
 *          ingomierswa Exp $
 */
public class CorrelationMatrixOperator extends Operator {

	public static final String PARAMETER_CREATE_WEIGHTS = "create_weights";

	public static final String PARAMETER_NORMALIZE_WEIGHTS = "normalize_weights";
	
	public static final String PARAMETER_SQUARED_CORRELATION = "squared_correlation";
	
	public CorrelationMatrixOperator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		SymmetricalMatrix matrix = new SymmetricalMatrix("Correlation", exampleSet);
		int numberOfAttributes = exampleSet.getAttributes().size();
		boolean squared = getParameterAsBoolean(PARAMETER_SQUARED_CORRELATION);
		boolean createWeights = getParameterAsBoolean(PARAMETER_CREATE_WEIGHTS);
		boolean normalizeWeights = getParameterAsBoolean(PARAMETER_NORMALIZE_WEIGHTS);
		int k = 0;
		for (Attribute firstAttribute : exampleSet.getAttributes()) {
			int l = 0;
			for (Attribute secondAttribute : exampleSet.getAttributes()) {
				matrix.setValue(k, l, getCorrelation(exampleSet, firstAttribute, secondAttribute, squared || createWeights));
				checkForStop();
				l++;
			}
			k++;
		}

		if (createWeights) {
			AttributeWeights weights = new AttributeWeights();
			// use squared correlations for weights --> learning schemes should
			// be able to use both positively and negatively high correlated
			// values
			int i = 0;
			for (Attribute attribute : exampleSet.getAttributes()) {
				double sum = 0.0d;
				for (int j = 0; j < numberOfAttributes; j++) {
					sum += (1.0d - matrix.getValue(i, j)); // actually the
															// squared value
				}
				weights.setWeight(attribute.getName(), sum / numberOfAttributes);
				i++;
			}
			if (normalizeWeights) {
				weights.normalize();
			}
			return new IOObject[] { exampleSet, weights };
		} else {
			return new IOObject[] { exampleSet, matrix };
		}
	}

	/** Updates all sums needed to compute the correlation coefficient. */
	private double getCorrelation(ExampleSet exampleSet, Attribute firstAttribute, Attribute secondAttribute, boolean squared) {
		double sumProd = 0.0d;
		double sumFirst = 0.0d;
		double sumSecond = 0.0d;
		double sumFirstSquared = 0.0d;
		double sumSecondSquared = 0.0d;
		int counter = 0;

		Iterator<Example> reader = exampleSet.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
			double first = example.getValue(firstAttribute);
			double second = example.getValue(secondAttribute);
			double prod = first * second;
			if (!Double.isNaN(prod)) {
				sumProd += prod;
				sumFirst += first;
				sumFirstSquared += first * first;
				sumSecond += second;
				sumSecondSquared += second * second;
				counter++;
			}
		}
		double r = (counter * sumProd - sumFirst * sumSecond) / (Math.sqrt((counter * sumFirstSquared - sumFirst * sumFirst) * (counter * sumSecondSquared - sumSecond * sumSecond)));
		if (squared)
			return r * r;
		else
			return r;
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return getParameterAsBoolean(PARAMETER_CREATE_WEIGHTS) ? new Class[] { ExampleSet.class, AttributeWeights.class } : new Class[] { ExampleSet.class, SymmetricalMatrix.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeBoolean(PARAMETER_CREATE_WEIGHTS, "Indicates if attribute weights based on correlation should be calculated or if the complete matrix should be returned.", false);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_NORMALIZE_WEIGHTS, "Indicates if the attributes weights should be normalized.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_SQUARED_CORRELATION, "Indicates if the squared correlation should be calculated.", false));
		return types;
	}
}
