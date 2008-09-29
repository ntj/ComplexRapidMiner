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
package com.rapidminer.operator.learner;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.Ontology;

/**
 * Checks if the the given learner can work on the example set.
 *
 * @author Ingo Mierswa
 * @version $Id: CapabilityCheck.java,v 1.4 2008/05/28 10:52:04 ingomierswa Exp $
 */
public class CapabilityCheck {

	private Learner learner;
	
	private boolean onlyWarn;
	
	public CapabilityCheck(Learner learner, boolean onlyWarn) {
		this.learner = learner;
		this.onlyWarn = onlyWarn;
	}
	
	/**
	 * Checks if this learner can be used for the given example set, i.e. if it
	 * has sufficient capabilities.
	 */
	public void checkLearnerCapabilities(Operator learningOperator, ExampleSet exampleSet) throws OperatorException {
		try {
			// nominal attributes
			if (Tools.containsValueType(exampleSet, Ontology.NOMINAL)) {
				if (Tools.containsValueType(exampleSet, Ontology.BINOMINAL)) {
					if (!learner.supportsCapability(LearnerCapability.BINOMINAL_ATTRIBUTES))
						throw new UserError(learningOperator, 501, LearnerCapability.BINOMINAL_ATTRIBUTES.getDescription());
				} else {
					if (!learner.supportsCapability(LearnerCapability.POLYNOMINAL_ATTRIBUTES))
						throw new UserError(learningOperator, 501, LearnerCapability.POLYNOMINAL_ATTRIBUTES.getDescription());
				}
			} 

			// numerical attributes
			if ((Tools.containsValueType(exampleSet, Ontology.NUMERICAL)) && !learner.supportsCapability(LearnerCapability.NUMERICAL_ATTRIBUTES))
				throw new UserError(learningOperator, 501, LearnerCapability.NUMERICAL_ATTRIBUTES.getDescription());

			// label
			if (exampleSet.getAttributes().getLabel().isNominal()) {
				if (exampleSet.getAttributes().getLabel().getMapping().size() == 2) {
					if (!learner.supportsCapability(LearnerCapability.BINOMINAL_CLASS))
						throw new UserError(learningOperator, 501, LearnerCapability.BINOMINAL_CLASS.getDescription());
				} else {
					if (!learner.supportsCapability(LearnerCapability.POLYNOMINAL_CLASS))
						throw new UserError(learningOperator, 501, LearnerCapability.POLYNOMINAL_CLASS.getDescription());
				}
			} else {
				if (exampleSet.getAttributes().getLabel().isNumerical() && !learner.supportsCapability(LearnerCapability.NUMERICAL_CLASS))
					throw new UserError(learningOperator, 501, LearnerCapability.NUMERICAL_CLASS.getDescription());
			}
		} catch (UserError e) {
			if (onlyWarn) {
				learningOperator.logWarning(e.getMessage());
			} else {
				throw e;
			}
		}
	}
}
