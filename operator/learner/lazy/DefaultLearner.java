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
package com.rapidminer.operator.learner.lazy;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;


/**
 * This learner creates a model, that will simply predict a default value for
 * all examples, i.e. the average or median of the true labels (or the mode in
 * case of classification) or a fixed specified value. This learner can be used
 * to compare the results of &quot;real&quot; learning schemes with guessing.
 * 
 * @see com.rapidminer.operator.learner.lazy.DefaultModel
 * @author Stefan Rueping, Ingo Mierswa
 * @version $Id: DefaultLearner.java,v 1.6 2008/05/09 19:23:24 ingomierswa Exp $
 */
public class DefaultLearner extends AbstractLearner {


	/** The parameter name for &quot;The method to compute the default.&quot; */
	public static final String PARAMETER_METHOD = "method";

	/** The parameter name for &quot;Value returned when method = constant.&quot; */
	public static final String PARAMETER_CONSTANT = "constant";
	private static final String[] METHODS = { "median", "average", "mode", "constant" };

	public static final int MEDIAN = 0;

	public static final int AVERAGE = 1;

	public static final int MODE = 2;

	public static final int CONSTANT = 3;

	public DefaultLearner(OperatorDescription description) {
		super(description);
	}

	public boolean supportsCapability(LearnerCapability lc) {
		if (lc == com.rapidminer.operator.learner.LearnerCapability.POLYNOMINAL_ATTRIBUTES)
			return true;
		if (lc == com.rapidminer.operator.learner.LearnerCapability.BINOMINAL_ATTRIBUTES)
			return true;
		if (lc == com.rapidminer.operator.learner.LearnerCapability.NUMERICAL_ATTRIBUTES)
			return true;

		if (lc == com.rapidminer.operator.learner.LearnerCapability.POLYNOMINAL_CLASS)
			return true;
		if (lc == com.rapidminer.operator.learner.LearnerCapability.BINOMINAL_CLASS)
			return true;
		if (lc == com.rapidminer.operator.learner.LearnerCapability.NUMERICAL_CLASS)
			return true;
		return false;
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {
		double value = 0.0;
		double[] confidences = null;
		int method = getParameterAsInt(PARAMETER_METHOD);
		Attribute label = exampleSet.getAttributes().getLabel();
		if ((label.isNominal()) && ((method == MEDIAN) || (method == AVERAGE))) {
			logWarning("Cannot use method '" + METHODS[method] + "' for nominal labels: changing to 'mode'!");
			method = MODE;
		} else if ((!label.isNominal()) && (method == MODE)) {
			logWarning("Cannot use method '" + METHODS[method] + "' for numerical labels: changing to 'average'!");
			method = AVERAGE;
		}
		switch (method) {
			case MEDIAN:
				double[] labels = new double[exampleSet.size()];
				Iterator<Example> r = exampleSet.iterator();
				int counter = 0;
				while (r.hasNext()) {
					Example example = r.next();
					labels[counter++] = example.getValue(example.getAttributes().getLabel());
				}
				java.util.Arrays.sort(labels);
				value = labels[exampleSet.size() / 2];
				break;
			case AVERAGE:
				exampleSet.recalculateAttributeStatistics(label);
				value = exampleSet.getStatistics(label, Statistics.AVERAGE);
				break;
			case MODE:
				exampleSet.recalculateAttributeStatistics(label);
				value = exampleSet.getStatistics(label, Statistics.MODE);
				confidences = new double[label.getMapping().size()];
				for (int i = 0; i < confidences.length; i++) {
					confidences[i] = exampleSet.getStatistics(label, Statistics.COUNT, label.getMapping().mapIndex(i)) / exampleSet.size();
				}
				break;
			case CONSTANT:
				value = getParameterAsDouble(PARAMETER_CONSTANT);
				break;
			default:
				// cannot happen
				throw new OperatorException("DefaultLearner: Unknown default method '" + method + "'!");
		}
		log("Default value is '" + (label.isNominal() ? label.getMapping().mapIndex((int) value) : value + "") + "'.");
		return new DefaultModel(exampleSet, value, confidences);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeCategory(PARAMETER_METHOD, "The method to compute the default.", METHODS, MEDIAN);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeDouble(PARAMETER_CONSTANT, "Value returned when method = constant.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d));
		return types;
	}
}
