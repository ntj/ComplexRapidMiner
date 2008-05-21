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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.LastInnerOperatorCondition;
import com.rapidminer.operator.features.weighting.ChiSquaredWeighting;
import com.rapidminer.operator.features.weighting.InfoGainRatioWeighting;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.learner.meta.AbstractMetaLearner;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;

/**
 * Learns a pruned decision tree based on arbitrary feature relevance measurements
 * defined by an inner operator (use for example {@link InfoGainRatioWeighting}
 * for C4.5 and {@link ChiSquaredWeighting} for CHAID. Works only for nominal
 * attributes.
 *
 * @author Ingo Mierswa
 * @version $Id: RelevanceTreeLearner.java,v 1.11 2008/05/09 19:22:53 ingomierswa Exp $
 */
public class RelevanceTreeLearner extends AbstractMetaLearner {
	
	public RelevanceTreeLearner(OperatorDescription description) {
		super(description);
	}
    
    public Model learn(ExampleSet exampleSet) throws OperatorException {
    	TreeBuilder builder = new TreeBuilder(new GainRatioCriterion(),
    			              getTerminationCriteria(exampleSet),
    			              getPruner(),
    			              null,
    			              getParameterAsInt(AbstractTreeLearner.PARAMETER_MINIMAL_LEAF_SIZE),
    			              0.0d) { // not necessary (because of normalization)
    	    protected Benefit calculateBenefit(ExampleSet exampleSet, Attribute attribute) throws OperatorException {
                ExampleSet trainingSet = (ExampleSet)exampleSet.clone();
    	    	Operator weightOp = getOperator(0);
    			double weight = Double.NaN;
    			if (weightOp != null) {
    				IOContainer output = weightOp.apply(new IOContainer(trainingSet));
    				AttributeWeights weights = output.remove(AttributeWeights.class);
    		    	weight = weights.getWeight(attribute.getName());
    			}

    			if (!Double.isNaN(weight)) {
    				return new Benefit(weight, attribute);
    			} else {
    				return null;
    			}
    	    }
    	};

    	// learn tree
    	Tree root = builder.learnTree(exampleSet);
        
        // create and return model
        return new TreeModel(exampleSet, root);
    }
    
	public Pruner getPruner() throws OperatorException {
        if (!getParameterAsBoolean(DecisionTreeLearner.PARAMETER_NO_PRUNING)) {
            return new PessimisticPruner(getParameterAsDouble(DecisionTreeLearner.PARAMETER_CONFIDENCE), new DecisionTreeLeafCreator());
        } else {
            return null;
        }
	}

	public List<Terminator> getTerminationCriteria(ExampleSet exampleSet) throws OperatorException {
		List<Terminator> result = new LinkedList<Terminator>();
		result.add(new SingleLabelTermination());
		result.add(new NoAttributeLeftTermination());
		result.add(new EmptyTermination());
		int maxDepth = getParameterAsInt(DecisionTreeLearner.PARAMETER_MAXIMAL_DEPTH);
		if (maxDepth <= 0) {
			maxDepth = exampleSet.size();
		}
		result.add(new MaxDepthTermination(maxDepth));
		return result;
	}
	
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new LastInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[] { AttributeWeights.class });
	}
	
    public boolean supportsCapability(LearnerCapability capability) {
        if (capability == com.rapidminer.operator.learner.LearnerCapability.BINOMINAL_ATTRIBUTES)
            return true;
        if (capability == com.rapidminer.operator.learner.LearnerCapability.POLYNOMINAL_ATTRIBUTES)
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
        ParameterType type = new ParameterTypeInt(AbstractTreeLearner.PARAMETER_MINIMAL_LEAF_SIZE, "The minimal size of all leaves.", 1, Integer.MAX_VALUE, 2);
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeInt(DecisionTreeLearner.PARAMETER_MAXIMAL_DEPTH, "The maximum tree depth (-1: no bound)", -1, Integer.MAX_VALUE, 10);
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeDouble(DecisionTreeLearner.PARAMETER_CONFIDENCE, "The confidence level used for pruning.", 0.0000001, 0.5, 0.25);
        type.setExpert(false);
        types.add(type);
        types.add(new ParameterTypeBoolean(DecisionTreeLearner.PARAMETER_NO_PRUNING, "Disables the pruning and delivers an unpruned tree.", false));
        return types;
    } 
}
