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
package com.rapidminer.operator.features.weighting;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeSelectionExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.rules.SingleRuleLearner;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.performance.SimplePerformanceEvaluator;
import com.rapidminer.tools.OperatorService;

/**
 * This operator calculates the relevance of a feature by computing the error
 * rate of a OneR Model on the exampleSet without this feature.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: OneRErrorWeighting.java,v 1.9 2008/05/09 19:23:22 ingomierswa Exp $
 */
public class OneRErrorWeighting extends AbstractWeighting {

	public OneRErrorWeighting(OperatorDescription description) {
		super(description);
	}

	public AttributeWeights calculateWeights(ExampleSet exampleSet) throws OperatorException {		
		Attribute label = exampleSet.getAttributes().getLabel();
		if (!label.isNominal()) {
			throw new UserError(this, 101, "OneR error weighting", label.getName());
		}
		
		// calculate the actual chi-squared values and assign them to weights
		AttributeWeights weights = new AttributeWeights(exampleSet);
		Operator learner;
		try {
			learner = OperatorService.createOperator(SingleRuleLearner.class);
		} catch (OperatorCreationException e) {
			throw new UserError(this, 904, "inner operator", e.getMessage());
		}
		Operator performanceEvaluator;
		try {
			performanceEvaluator = OperatorService.createOperator(SimplePerformanceEvaluator.class);
		} catch (OperatorCreationException e) {
			throw new UserError(this, 904, "performance evaluation operator", e.getMessage());
		}
		
		boolean[] mask = new boolean[exampleSet.getAttributes().size()];
		int i = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			mask[i] = true;
			if (i > 0) {
				mask[i - 1] = false;
			}
			ExampleSet singleAttributeSet = new AttributeSelectionExampleSet(exampleSet, mask);
			// calculating model
			IOContainer ioContainer = new IOContainer(singleAttributeSet);
			Model model = learner.apply(ioContainer).remove(Model.class);
			// applying model
			singleAttributeSet = model.apply(singleAttributeSet);
			// applying performance evaluator
			ioContainer = new IOContainer(singleAttributeSet);
			PerformanceVector performance = performanceEvaluator.apply(ioContainer).remove(PerformanceVector.class);
			double weight = performance.getCriterion(0).getAverage();

			weights.setWeight(attribute.getName(), weight);
			i++;
		}
		return weights;
	}
}
