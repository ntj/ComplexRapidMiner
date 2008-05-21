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

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.features.weighting.ChiSquaredWeighting;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.OperatorService;

/**
 * The CHAID decision tree learner works like the 
 * {@link com.rapidminer.operator.learner.tree.DecisionTreeLearner}
 * with one exception: it used a chi squared based criterion
 * instead of the information gain or gain ratio criteria.
 * 
 * @author Ingo Mierswa
 * @version $Id: CHAIDLearner.java,v 1.4 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class CHAIDLearner extends DecisionTreeLearner {

	public CHAIDLearner(OperatorDescription description) {
		super(description);
	}

    /** This method calculates the benefit of the given attribute. This implementation
     *  utilizes the defined {@link Criterion}. Subclasses might want to override this
     *  method in order to calculate the benefit in other ways. */
    protected Benefit calculateBenefit(ExampleSet trainingSet, Attribute attribute) throws OperatorException {
    	Operator weightOp = null;
		try {
			weightOp = OperatorService.createOperator(ChiSquaredWeighting.class);
		} catch (OperatorCreationException e) {
			logWarning("Cannot create chi squared calculation operator.");
		}
		
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
    
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        // remove criterion selection
        Iterator<ParameterType> i = types.iterator();
        while (i.hasNext()) {
        	if (i.next().getKey().equals(PARAMETER_CRITERION))
        		i.remove();
        }
        return types;
    } 
}
