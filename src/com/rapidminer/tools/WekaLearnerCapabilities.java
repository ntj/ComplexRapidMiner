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
package com.rapidminer.tools;

import com.rapidminer.operator.learner.LearnerCapability;

import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Capabilities;
import weka.core.WeightedInstancesHandler;

/**
 * Checks if a given classifier supports the desired capability.
 * 
 * @author Ingo Mierswa
 * @version $Id: WekaLearnerCapabilities.java,v 1.6 2006/04/12 11:17:42
 *          ingomierswa Exp $
 */
public class WekaLearnerCapabilities {

	public static boolean supportsCapability(Classifier classifier, LearnerCapability lc) {
		Capabilities capabilities = classifier.getCapabilities();

		if (lc == LearnerCapability.POLYNOMINAL_ATTRIBUTES) {
			return capabilities.handles(Capabilities.Capability.NOMINAL_ATTRIBUTES);
		} else if (lc == LearnerCapability.BINOMINAL_ATTRIBUTES) {
			return capabilities.handles(Capabilities.Capability.BINARY_ATTRIBUTES);
		} else if (lc == LearnerCapability.NUMERICAL_ATTRIBUTES) {
			return capabilities.handles(Capabilities.Capability.NUMERIC_ATTRIBUTES);
		} else if (lc == LearnerCapability.POLYNOMINAL_CLASS) {
			return capabilities.handles(Capabilities.Capability.NOMINAL_CLASS);
		} else if (lc == LearnerCapability.BINOMINAL_CLASS) {
			return capabilities.handles(Capabilities.Capability.BINARY_CLASS);
		} else if (lc == LearnerCapability.NUMERICAL_CLASS) {
			return capabilities.handles(Capabilities.Capability.NUMERIC_CLASS);
		} else if (lc == LearnerCapability.UPDATABLE) {
		    return (classifier instanceof UpdateableClassifier);
        } else if (lc == LearnerCapability.WEIGHTED_EXAMPLES) {
            return (classifier instanceof WeightedInstancesHandler);
        }
		return false;
	}
}
