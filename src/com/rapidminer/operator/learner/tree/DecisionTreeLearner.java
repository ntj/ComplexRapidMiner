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
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;

/**
 * <p>This operator learns decision trees from both nominal and numerical data.
 * Decision trees are powerful classification methods which often can also
 * easily be understood. This decision tree learner works similar to Quinlan's
 * C4.5 or CART.</p>
 * 
 * <p>The actual type of the tree is determined by the criterion, e.g. using 
 * gain_ratio or Gini for CART / C4.5.</p>
 * 
 * @rapidminer.index C4.5
 * @rapidminer.index CART
 *  
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: DecisionTreeLearner.java,v 1.14 2008/05/09 19:22:53 ingomierswa Exp $
 */
public class DecisionTreeLearner extends AbstractTreeLearner {

	/** The parameter name for the maximum tree depth. */
	public static final String PARAMETER_MAXIMAL_DEPTH = "maximal_depth";
	
	/** The parameter name for &quot;The confidence level used for pruning.&quot; */
	public static final String PARAMETER_CONFIDENCE = "confidence";

	/** The parameter name for &quot;Disables the pruning and delivers an unpruned tree.&quot; */
	public static final String PARAMETER_NO_PRUNING = "no_pruning";    
    
    public DecisionTreeLearner(OperatorDescription description) {
        super(description);
    }

	public Pruner getPruner() throws OperatorException {
        if (!getParameterAsBoolean(PARAMETER_NO_PRUNING)) {
            return new PessimisticPruner(getParameterAsDouble(PARAMETER_CONFIDENCE), new DecisionTreeLeafCreator());
        } else {
            return null;
        }
	}

	public List<Terminator> getTerminationCriteria(ExampleSet exampleSet) throws OperatorException {
		List<Terminator> result = new LinkedList<Terminator>();
		result.add(new SingleLabelTermination());
		result.add(new NoAttributeLeftTermination());
		result.add(new EmptyTermination());
		int maxDepth = getParameterAsInt(PARAMETER_MAXIMAL_DEPTH);
		if (maxDepth <= 0) {
			maxDepth = exampleSet.size();
		}
		result.add(new MaxDepthTermination(maxDepth));
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
        ParameterType type = new ParameterTypeInt(PARAMETER_MAXIMAL_DEPTH, "The maximum tree depth (-1: no bound)", -1, Integer.MAX_VALUE, 10);
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeDouble(PARAMETER_CONFIDENCE, "The confidence level used for the pessimistic error calculation of pruning.", 0.0000001, 0.5, 0.25);
        type.setExpert(false);
        types.add(type);
        types.add(new ParameterTypeBoolean(PARAMETER_NO_PRUNING, "Disables the pruning and delivers an unpruned tree.", false));
        return types;
    }
}
