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
package com.rapidminer.operator.learner.meta;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.condition.AllInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.Ontology;


/**
 * This meta learner applies a transformation on the label before the inner
 * regression learner is applied.
 * 
 * @author Stefan Rueping, Ingo Mierswa
 * @version $Id: TransformedRegression.java,v 1.8 2006/04/05 08:57:26
 *          ingomierswa Exp $
 */
public class TransformedRegression extends OperatorChain {

	/** The parameter name for &quot;Type of transformation to use on the labels (log, exp, transform to mean 0 and variance 1, rank, or none).&quot; */
	public static final String PARAMETER_TRANSFORMATION_METHOD = "transformation_method";

	/** The parameter name for &quot;Scale transformed values to mean 0 and standard deviation 1?&quot; */
	public static final String PARAMETER_Z_SCALE = "z_scale";

	/** The parameter name for &quot;Interpolate prediction if predicted rank is not an integer?&quot; */
	public static final String PARAMETER_INTERPOLATE_RANK = "interpolate_rank";
    
	public TransformedRegression(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet inputSet = getInput(ExampleSet.class);
		int method = getParameterAsInt(PARAMETER_TRANSFORMATION_METHOD);
		double[] rank = null;
		double mean = 0.0d;
		double stddev = 1.0d;

		Attribute label = inputSet.getAttributes().getLabel();
		inputSet.recalculateAttributeStatistics(label);
        
		ExampleSet eSet = (ExampleSet) inputSet.clone();
		Attribute tempLabel = AttributeFactory.createAttribute("temp_transformed_regression_label", Ontology.REAL);
		eSet.getExampleTable().addAttribute(tempLabel);
		eSet.getAttributes().setLabel(tempLabel);

		// 1. Set new regression labels
		Iterator<Example> r = eSet.iterator();
		switch (method) {
			case TransformedRegressionModel.LOG:
				double offset = 1.0d - inputSet.getStatistics(label, Statistics.MINIMUM);
				rank = new double[1];
				rank[0] = offset;
				while (r.hasNext()) {
					Example e = r.next();
					e.setValue(tempLabel, Math.log(offset + e.getValue(label)));
				}
				break;
			case TransformedRegressionModel.EXP:
				while (r.hasNext()) {
					Example e = r.next();
					e.setValue(tempLabel, Math.exp(e.getValue(label)));
				}
				break;
			case TransformedRegressionModel.RANK:
				double[] dummy = new double[eSet.size()];
				int i = 0;
				while (r.hasNext()) {
					Example e = r.next();
					dummy[i] = e.getValue(label);
					i++;
				}
				java.util.Arrays.sort(dummy);
				// remove double entries
				i = 0;
				for (int j = 0; j < dummy.length; j++) {
					if (dummy[i] != dummy[j]) {
						i++;
						dummy[i] = dummy[j];
					}
				}
				rank = new double[i + 1];
				for (int j = 0; j < i + 1; j++) {
					rank[j] = dummy[j];
				}

				r = eSet.iterator();
				while (r.hasNext()) {
					Example e = r.next();
					e.setValue(tempLabel, java.util.Arrays.binarySearch(rank, e.getValue(label)));
				}
				// }
				break;
			case TransformedRegressionModel.NONE:
				// just for convenience...
				while (r.hasNext()) {
					Example e = r.next();
					e.setValue(tempLabel, e.getValue(label));
				}
				break;
			default:
				// cannot happen
				break;
		}

		if (getParameterAsBoolean(PARAMETER_Z_SCALE)) {
            eSet.recalculateAttributeStatistics(tempLabel);
			mean = eSet.getStatistics(tempLabel, Statistics.AVERAGE);
			stddev = eSet.getStatistics(tempLabel, Statistics.VARIANCE);
			if (stddev <= 0.0d) {
				// catch numerical errors
				stddev = 1.0d;
			};
			r = eSet.iterator();
			while (r.hasNext()) {
				Example e = r.next();
				e.setValue(tempLabel, (e.getValue(tempLabel) - mean) / stddev);
			}
		};

		// 2. Apply learner
		IOContainer input = new IOContainer(new IOObject[] { eSet });
		input = getOperator(0).apply(input);
		Model model = input.remove(Model.class);

		TransformedRegressionModel resultModel = new TransformedRegressionModel(inputSet, method, rank, model, getParameterAsBoolean(PARAMETER_Z_SCALE), mean, stddev, getParameterAsBoolean(PARAMETER_INTERPOLATE_RANK));

		// weights?
		AttributeWeights weights = null;
		try {
			weights = input.remove(AttributeWeights.class);
		} catch (MissingIOObjectException e) {}

		if (weights == null) {
			return new IOObject[] { resultModel };
		} else {
			return new IOObject[] { resultModel, weights };
		}
	}

	/**
	 * Returns true since this operator chain should return the output of the
	 * last inner operator.
	 */
	public boolean shouldReturnInnerOutput() {
		return true;
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		return new AllInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[] { Model.class });
	}

	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	public int getMaxNumberOfInnerOperators() {
		return 1;
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { Model.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeCategory(PARAMETER_TRANSFORMATION_METHOD, "Type of transformation to use on the labels (log, exp, transform to mean 0 and variance 1, rank, or none).", TransformedRegressionModel.METHODS, TransformedRegressionModel.LOG);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeBoolean(PARAMETER_Z_SCALE, "Scale transformed values to mean 0 and standard deviation 1?", false);
		type.setExpert(true);
		types.add(type);
		type = new ParameterTypeBoolean(PARAMETER_INTERPOLATE_RANK, "Interpolate prediction if predicted rank is not an integer?", true);
		type.setExpert(true);
		types.add(type);
		return types;
	}
}
