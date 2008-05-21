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
package com.rapidminer.operator.learner.tree;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.learner.SimplePredictionModel;
import com.rapidminer.operator.learner.meta.SimpleVoteModel;
import com.rapidminer.operator.preprocessing.sampling.AbstractBootstrapping;
import com.rapidminer.operator.preprocessing.sampling.Bootstrapping;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.OperatorService;

/**
 * This operators learns a random forest. The resulting forest model contains serveral 
 * single random tree models.
 *
 * @author Ingo Mierswa
 * @version $Id: RandomForestLearner.java,v 1.6 2008/05/09 19:22:53 ingomierswa Exp $
 */
public class RandomForestLearner extends AbstractLearner {
	
    /** The parameter name for the number of trees. */
    public static final String PARAMETER_NUMBER_OF_TREES = "number_of_trees";
    
	public RandomForestLearner(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {
		// create random tree learner and set parameters
		RandomTreeLearner randomTreeLearner = null;
		try {
			randomTreeLearner = OperatorService.createOperator(RandomTreeLearner.class);
			List<ParameterType> parameters = randomTreeLearner.getParameterTypes();
			for (ParameterType parameter : parameters) {
				Object value = getParameter(parameter.getKey());
                if (value != null)
                    randomTreeLearner.setParameter(parameter.getKey(), value.toString());
			}
		} catch (OperatorCreationException e) {
			throw new OperatorException(getName() + ": cannot construct random tree learner: " + e.getMessage());
		}
		
        Bootstrapping bootstrapping = null;
        try {
            bootstrapping = OperatorService.createOperator(Bootstrapping.class);
            bootstrapping.setParameter(AbstractBootstrapping.PARAMETER_SAMPLE_RATIO, "1.0");
        } catch (OperatorCreationException e) {
            throw new OperatorException(getName() + ": cannot construct random tree learner: " + e.getMessage());
        }
        
        // learn base models
        List<SimplePredictionModel> baseModels = new LinkedList<SimplePredictionModel>();
        int numberOfTrees = getParameterAsInt(PARAMETER_NUMBER_OF_TREES);
        for (int i = 0; i < numberOfTrees; i++) {
            TreeModel model = (TreeModel)randomTreeLearner.learn((ExampleSet)exampleSet.clone());
            baseModels.add(model);
        }
        
        // create and return model
        return new SimpleVoteModel(exampleSet, baseModels);
	}

	public boolean supportsCapability(LearnerCapability capability) {
		if (capability == com.rapidminer.operator.learner.LearnerCapability.BINOMINAL_ATTRIBUTES)
			return true;
		if (capability == com.rapidminer.operator.learner.LearnerCapability.POLYNOMINAL_ATTRIBUTES)
			return true;
		if (capability == com.rapidminer.operator.learner.LearnerCapability.NUMERICAL_ATTRIBUTES)
			return true;
		if (capability == com.rapidminer.operator.learner.LearnerCapability.POLYNOMINAL_CLASS)
			return true;
		if (capability == com.rapidminer.operator.learner.LearnerCapability.BINOMINAL_CLASS)
			return true;
		if (capability == com.rapidminer.operator.learner.LearnerCapability.WEIGHTED_EXAMPLES)
			return true;
		return false;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

        ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_TREES, "The number of learned random trees.", 1, Integer.MAX_VALUE, 10);
        type.setExpert(false);
        types.add(type);
        
		// add random tree parameters
		try {
			Operator randomTreeLearner = OperatorService.createOperator(RandomTreeLearner.class);
            List<ParameterType> innerParameters = randomTreeLearner.getParameterTypes();
            for (ParameterType innerType : innerParameters) {
                if (innerType.getKey().equals(DecisionTreeLearner.PARAMETER_NO_PRUNING)) {
                    innerType.setDefaultValue(false);
                } else {
                    if (!innerType.getKey().equals(DecisionTreeLearner.PARAMETER_CONFIDENCE) && !innerType.getKey().equals("keep_example_set"))
                        types.add(innerType);
                }
            }
		} catch (OperatorCreationException e) {
			logWarning("Cannot create random tree learner: " + e.getMessage());
		}
		
		return types;
	}
}
