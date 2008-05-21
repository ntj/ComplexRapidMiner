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

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.operator.similarity.SimilarityUtil;
import com.rapidminer.operator.similarity.attributebased.ExampleBasedSimilarityMeasure;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * A simple k nearest neighbor implementation.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: KNNLearner.java,v 1.5 2008/05/09 19:23:24 ingomierswa Exp $
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

	public Model learn(ExampleSet givenExampleSet) throws OperatorException {
		ExampleSet exampleSet = (ExampleSet)givenExampleSet.clone();
		Tools.checkAndCreateIds(exampleSet);
		SimilarityMeasure sim = SimilarityUtil.resolveSimilarityMeasure(getParameters(), getInput(), exampleSet);
        if(!(sim instanceof ExampleBasedSimilarityMeasure)) 
        	throw new UserError(this, 930);
        return new KNNModel(exampleSet, sim, getParameterAsInt(PARAMETER_K), getParameterAsBoolean(PARAMETER_WEIGHTED_VOTE));
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
		ParameterType type = new ParameterTypeInt(PARAMETER_K, "The used number of nearest neighbors.", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);
		types.add(SimilarityUtil.generateSimilarityParameter());
		types.add(new ParameterTypeBoolean(PARAMETER_WEIGHTED_VOTE, "Indicates if the votes should be weighted by similarity.", false));
		return types;
	}
}
