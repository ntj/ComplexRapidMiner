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
package com.rapidminer.operator.learner.bayes;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;


/**
 * Naive Bayes learner.
 * 
 * @author Tobias Malbrecht
 * @version $Id: NaiveBayes.java,v 1.15 2008/06/03 14:17:47 tobiasmalbrecht Exp $
 */
public class NaiveBayes extends AbstractLearner {

	public static final String PARAMETER_LAPLACE_CORRECTION = "laplace_correction";
	
	public NaiveBayes(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {
		boolean laplaceCorrectionEnabled = getParameterAsBoolean(PARAMETER_LAPLACE_CORRECTION);
		return new DistributionModel(exampleSet, laplaceCorrectionEnabled);
	}

	public boolean supportsCapability(LearnerCapability lc) {
		if (lc == LearnerCapability.POLYNOMINAL_ATTRIBUTES)
			return true;
		if (lc == LearnerCapability.BINOMINAL_ATTRIBUTES)
			return true;
		if (lc == LearnerCapability.NUMERICAL_ATTRIBUTES)
			return true;
		if (lc == LearnerCapability.POLYNOMINAL_CLASS)
			return true;
		if (lc == LearnerCapability.BINOMINAL_CLASS)
			return true;
		if (lc == LearnerCapability.WEIGHTED_EXAMPLES)
			return true;
		return false;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeBoolean(PARAMETER_LAPLACE_CORRECTION, "Use Laplace correction to prevent high influence of zero probabilities.", true);
		type.setExpert(true);
		types.add(type);
		return types;
	}
}
