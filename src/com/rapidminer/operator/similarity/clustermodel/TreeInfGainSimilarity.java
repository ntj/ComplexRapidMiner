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
import com.rapidminer.tools.IterationArrayList;


/**
 * Yields a similarity based on the formula given in Ref.
 * 
 * @author Michael Wurst
 * @version $Id: TreeInfGainSimilarity.java,v 1.3 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class TreeInfGainSimilarity extends MostSpecificCommonNodeSimilarity {

	private static final long serialVersionUID = -3715472089970116894L;

	private final Map<ClusterNode, Integer> numItemsMap = new HashMap<ClusterNode, Integer>();

	private int overallNumItems;

	public void init(ClusterModel cm) throws OperatorException {
		super.init(cm);
		calculateNumItems(getClusterModel().getRootNode());
		overallNumItems = new IterationArrayList<String>(getClusterModel().getRootNode().getObjectsInSubtree()).size();
	}

	public double similarity(String x, String y) {
		return (overallNumItems * getWeightOfNode(getMostSpecificCommonNode(x, y))) / ((getWeightOfNode(getNode(x)) + getWeightOfNode(getNode(y))));
	}

	protected double getWeightOfNode(ClusterNode cn) {
		return numItemsMap.get(cn);
	}

	private void calculateNumItems(ClusterNode cn) {
		numItemsMap.put(cn, cn.getNumberOfObjectsInSubtree());
		Iterator it = cn.getSubNodes();
		while (it.hasNext())
			calculateNumItems((ClusterNode) it.next());
	}
}
