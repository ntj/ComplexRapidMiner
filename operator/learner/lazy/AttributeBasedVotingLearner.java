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

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.learner.meta.Vote;


/**
 * AttributeBasedVotingLearner is very lazy. Actually it does not learn at all but creates an
 * {@link AttributeBasedVotingModel}. This model simply calculates the average of the
 * attributes as prediction (for regression) or the mode of all attribute values 
 * (for classification). AttributeBasedVotingLearner is especially useful if it is used
 * on an example set created by a meta learning scheme, e.g. by {@link Vote}.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeBasedVotingLearner.java,v 1.5 2008/05/09 19:23:24 ingomierswa Exp $
 */
public class AttributeBasedVotingLearner extends AbstractLearner {

	public AttributeBasedVotingLearner(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) {
		exampleSet.recalculateAttributeStatistics(exampleSet.getAttributes().getLabel());
		double majorityPrediction;
		if (exampleSet.getAttributes().getLabel().isNominal()) {
			majorityPrediction = exampleSet.getStatistics(exampleSet.getAttributes().getLabel(), Statistics.MODE);
		} else {
			majorityPrediction = exampleSet.getStatistics(exampleSet.getAttributes().getLabel(), Statistics.AVERAGE);
		}
		return new AttributeBasedVotingModel(exampleSet, majorityPrediction);
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
		
		return false;
	}
}
