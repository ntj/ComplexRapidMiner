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

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.math.container.GeometricDataCollection;
import com.rapidminer.tools.math.container.LinearList;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;


/**
 * A k nearest neighbor implementation.
 * 
 * @author Sebastian Land
 * @version $Id: KNNLearner.java,v 1.7 2008/08/05 09:41:58 stiefelolm Exp $
 * 
 */
public class KNNLearner extends AbstractLearner {


	
	/** The parameter name for &quot;The used number of nearest neighbors.&quot; */
	public static final String PARAMETER_K = "k";

	/** The parameter name for &quot;Indicates if the votes should be weighted by similarity.&quot; */
	public static final String PARAMETER_WEIGHTED_VOTE = "weighted_vote";
	
	public KNNLearner(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {
		DistanceMeasure measure = DistanceMeasures.createMeasure(this, exampleSet);
		Attribute label = exampleSet.getAttributes().getLabel();
		if (label.isNominal()) {
			// classification
			GeometricDataCollection<Integer> samples = new LinearList<Integer>(measure);
			
			Attributes attributes = exampleSet.getAttributes();
	
			int valuesSize = attributes.size();
			for(Example example: exampleSet) {
				double[] values = new double[valuesSize];
				int i = 0;
				for (Attribute attribute: attributes) {
					values[i] = example.getValue(attribute);
					i++;
				}
				int labelValue = (int) example.getValue(label);
				samples.add(values, labelValue);
				checkForStop();
			}
	        return new KNNClassificationModel(exampleSet, samples, getParameterAsInt(PARAMETER_K), getParameterAsBoolean(PARAMETER_WEIGHTED_VOTE));
		} else {
			// regression
			GeometricDataCollection<Double> samples = new LinearList<Double>(measure);
			Attributes attributes = exampleSet.getAttributes();
	
			int valuesSize = attributes.size();
			for (Example example: exampleSet) {
				double[] values = new double[valuesSize];
				int i = 0;
				for (Attribute attribute: attributes) {
					values[i] = example.getValue(attribute);
					i++;
				}
				double labelValue = example.getValue(label);
				samples.add(values, labelValue);
				checkForStop();
			}
	        return new KNNRegressionModel(exampleSet, samples, getParameterAsInt(PARAMETER_K), getParameterAsBoolean(PARAMETER_WEIGHTED_VOTE));
		}
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
		
		if (lc == com.rapidminer.operator.learner.LearnerCapability.WEIGHTED_EXAMPLES)
			return true;
		
		return false;
	}

	public InputDescription getInputDescription(Class cls) {
		if (SimilarityMeasure.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		}
		return super.getInputDescription(cls);
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(PARAMETER_K, "The used number of nearest neighbors.", 1, Integer.MAX_VALUE, 1));
		types.add(new ParameterTypeBoolean(PARAMETER_WEIGHTED_VOTE, "Indicates if the votes should be weighted by similarity.", false));
	
		types.addAll(DistanceMeasures.getParameterTypes(this));
		return types;
	}
}
