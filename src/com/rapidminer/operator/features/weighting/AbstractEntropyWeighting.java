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
package com.rapidminer.operator.features.weighting;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.tree.Criterion;
import com.rapidminer.operator.learner.tree.DecisionTreeLearner;
import com.rapidminer.operator.learner.tree.NumericalSplitter;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;

/**
 * This operator calculates the relevance of a feature by computing the 
 * an entropy value of the class distribution, if the given example set would 
 * have been splitted according to the feature.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractEntropyWeighting.java,v 1.2 2007/06/16 12:48:55 ingomierswa Exp $
 */
public abstract class AbstractEntropyWeighting extends Operator {

	public AbstractEntropyWeighting(OperatorDescription description) {
		super(description);
	}

	public abstract Criterion getEntropyCriterion();
	
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		Attribute label = exampleSet.getAttributes().getLabel();
		if (!label.isNominal()) {
			throw new UserError(this, 101, getName(), label.getName());
		}

        // calculate the actual infogain values and assign them to weights
        Criterion criterion = getEntropyCriterion();
        NumericalSplitter splitter = new NumericalSplitter(criterion, getParameterAsInt(DecisionTreeLearner.PARAMETER_NUMERICAL_SAMPLE_SIZE));
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

		// normalize
		weights.normalize();
		
		return new IOObject[] { exampleSet,	weights	};
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, AttributeWeights.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(DecisionTreeLearner.PARAMETER_NUMERICAL_SAMPLE_SIZE, "Indicates the number of examples which should be used for determining the gain for numerical attributes (-1: use all examples).", -1, Integer.MAX_VALUE, -1));
		return types;
	}
}
