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

/**
 * This operator learns decision trees without pruning using both nominal
 * and numerical attributes.
 * Decision trees are powerful classification methods which often can also
 * easily be understood. This decision tree learner works similar to Quinlan's
 * ID3. 
 * 
 * @author Ingo Mierswa
 * @version $Id: ID3NumericalLearner.java,v 1.3 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class ID3NumericalLearner extends AbstractTreeLearner {
    
    public ID3NumericalLearner(OperatorDescription description) {
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
		result.add(new MaxDepthTermination(exampleSet.size()));
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
}
