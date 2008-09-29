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
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.parameter.ParameterType;

/**
 * This operator learns decision stumps, i.e. a small decision tree with only
 * one single split. This decision stump works on both numerical and nominal
 * attributes. 
 * 
 * @author Ingo Mierswa
 * @version $Id: DecisionStumpLearner.java,v 1.5 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class DecisionStumpLearner extends AbstractTreeLearner {
    
    public DecisionStumpLearner(OperatorDescription description) {
        super(description);
    }

	public Pruner getPruner() throws OperatorException {
		return null;
	}

	public List<Terminator> getTerminationCriteria(ExampleSet exampleSet) {
		List<Terminator> result = new LinkedList<Terminator>();
		result.add(new SingleLabelTermination());
		result.add(new NoAttributeLeftTermination());
		result.add(new EmptyTermination());
		result.add(new MaxDepthTermination(2));
		return result;
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
        for (ParameterType type : types) {
        	if (type.getKey().equals(PARAMETER_MINIMAL_LEAF_SIZE)) {
        		type.setDefaultValue(Integer.valueOf(1));
        	}
        }
        return types;
    }
}
