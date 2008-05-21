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
package com.rapidminer.operator.similarity.clustermodel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.ClusterNode;

/**
 * Cluster model similarity based on tree depth.
 * 
 * @author Michael Wurst
 * @version $Id: TreeDistance.java,v 1.3 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class TreeDistance extends MostSpecificCommonNodeSimilarity {

	private static final long serialVersionUID = 4442795652362134420L;

	private final Map<ClusterNode, Integer> depthMap = new HashMap<ClusterNode, Integer>();

	public void init(ClusterModel cm) throws OperatorException {
		super.init(cm);
		calculateDepth(getClusterModel().getRootNode(), 0);
	}

	public boolean isDistance() {
		return true;
	}

	public double similarity(String x, String y) {
		return -2.0 * getWeightOfNode(getMostSpecificCommonNode(x, y)) + (getWeightOfNode(getNode(x)) + getWeightOfNode(getNode(y)));
	}

	protected double getWeightOfNode(ClusterNode cn) {
		return depthMap.get(cn);
	}

	private void calculateDepth(ClusterNode cn, int depth) {
		depthMap.put(cn, depth);
		Iterator it = cn.getSubNodes();
		while (it.hasNext())
			calculateDepth((ClusterNode) it.next(), depth + 1);
	}
}
