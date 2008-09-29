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
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.tree.Criterion;
import com.rapidminer.operator.learner.tree.NumericalSplitter;

/**
 * This operator calculates the relevance of a feature by computing the 
 * an entropy value of the class distribution, if the given example set would 
 * have been splitted according to the feature.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractEntropyWeighting.java,v 1.6 2008/05/09 19:23:22 ingomierswa Exp $
 */
public abstract class AbstractEntropyWeighting extends AbstractWeighting {

	public AbstractEntropyWeighting(OperatorDescription description) {
		super(description);
	}

	public abstract Criterion getEntropyCriterion();
	
	public AttributeWeights calculateWeights(ExampleSet exampleSet) throws OperatorException {
		Attribute label = exampleSet.getAttributes().getLabel();
		if (!label.isNominal()) {
			throw new UserError(this, 101, getName(), label.getName());
		}

        // calculate the actual infogain values and assign them to weights
        Criterion criterion = getEntropyCriterion();
        NumericalSplitter splitter = new NumericalSplitter(criterion);
        AttributeWeights weights = new AttributeWeights(exampleSet);
        for (Attribute attribute : exampleSet.getAttributes()) {
            SplittedExampleSet splitted = null;
            if (attribute.isNominal()) {
                splitted = SplittedExampleSet.splitByAttribute(exampleSet, attribute);
                double weight = criterion.getBenefit(splitted);
                weights.setWeight(attribute.getName(), weight);
            } else {
                double splitValue = splitter.getBestSplit(exampleSet, attribute);
                splitted = SplittedExampleSet.splitByAttribute(exampleSet, attribute, splitValue);
                double weight = criterion.getBenefit(splitted);
                weights.setWeight(attribute.getName(), weight);
            }
        }
		return weights;
	}
}
