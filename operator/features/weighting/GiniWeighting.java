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
package com.rapidminer.operator.features.weighting;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.learner.tree.Criterion;
import com.rapidminer.operator.learner.tree.GiniIndexCriterion;

/**
 * This operator calculates the relevance of a feature by computing the 
 * Gini index of the class distribution, if the given example set would 
 * have been splitted according to the feature.
 * 
 * @author Ingo Mierswa
 * @version $Id: GiniWeighting.java,v 1.3 2008/05/09 19:23:22 ingomierswa Exp $
 */
public class GiniWeighting extends AbstractEntropyWeighting {

	public GiniWeighting(OperatorDescription description) {
		super(description);
	}

	public Criterion getEntropyCriterion() {
		return new GiniIndexCriterion();
	}
}
