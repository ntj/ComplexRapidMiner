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
package com.rapidminer.operator.validation.clustering;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;

;
/**
 * Compares two hierarchical clustering models according to the label of their root node. If this label is equal, 1 is returned, 0 otherwise.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ClusterModelLabelSimilarity.java,v 1.3 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 */
public class ClusterModelLabelSimilarity extends Operator {

	public ClusterModelLabelSimilarity(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		HierarchicalClusterModel hcm1 = getInput(HierarchicalClusterModel.class);
		HierarchicalClusterModel hcm2 = getInput(HierarchicalClusterModel.class);

		if ((hcm1.getRootNode() == null) || (hcm2.getRootNode() == null)) {
			PerformanceVector pv = new PerformanceVector();
			PerformanceCriterion pc = new EstimatedPerformance("accuracy", Double.NaN, 1, false);
			pv.addCriterion(pc);
			logWarning("Could not compare cm, one of them is null");
			return new IOObject[] { pv };
		}
		
		PerformanceVector pv = new PerformanceVector();
		double performance = 0.0;
		if (hcm1.getRootNode().getDescription().equals(hcm2.getRootNode().getDescription()))
			performance = 1.0;
		PerformanceCriterion pc = new EstimatedPerformance("accuracy", performance, 1, false);
		pv.addCriterion(pc);

		return new IOObject[] { pv };
	}

	public Class[] getInputClasses() {
		return new Class[] { ClusterModel.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { PerformanceVector.class };
	}
}
