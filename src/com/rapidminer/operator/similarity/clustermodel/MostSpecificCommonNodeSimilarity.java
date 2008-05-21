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

import java.util.Iterator;

import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.ClusterNode;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;
import com.rapidminer.operator.learner.clustering.SimpleHierarchicalClusterModel;
import com.rapidminer.operator.similarity.SimilarityAdapter;

/**
 * A similarity based on a hierarchical clustering. The similarity of two objects is the weight of the most specific tree node, which subtree contains
 * both objects.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: MostSpecificCommonNodeSimilarity.java,v 1.5 2008/05/09 19:22:49 ingomierswa Exp $
 */
public abstract class MostSpecificCommonNodeSimilarity extends SimilarityAdapter implements ClusterModelSimilarity {

	private HierarchicalClusterModel hcm;

	private com.rapidminer.operator.learner.clustering.ClusterModelPredecessorIndex predIndex;

	private com.rapidminer.operator.learner.clustering.ClusterModelItemIndex itemIndex;

	protected ClusterNode getMostSpecificCommonNode(String x, String y) {
		ClusterNode cn1 = itemIndex.getCluster(x);
		ClusterNode cn2 = itemIndex.getCluster(y);
		if ((cn1 == null) || (cn2 == null))
			return null;
		ClusterNode common = predIndex.getMostSpecificCommonNode(cn1, cn2);
		if (common == null)
			return null;
		return common;
	}

	protected ClusterNode getNode(String x) {
		ClusterNode cn1 = itemIndex.getCluster(x);
		return cn1;
	}

	public Iterator<String> getIds() {
		return itemIndex.getAllIds();
	}

    public int getNumberOfIds() {
        return itemIndex.getNumberOfIds();
    }
    
	public boolean isDistance() {
		return false;
	}

	public void init(ClusterModel cm) throws OperatorException {
		if (hcm != null)
			throw new OperatorException("Cluster model similarities can only be initialized once.");
		if (cm instanceof HierarchicalClusterModel) {
			this.hcm = (HierarchicalClusterModel) cm;
		} else if (cm instanceof FlatClusterModel) {
			this.hcm = new SimpleHierarchicalClusterModel((FlatClusterModel) cm);
		} else {
			throw new OperatorException("ClusterModel must be FlatClusterModel or HierarchicalClusterModel");
		}
		itemIndex = new com.rapidminer.operator.learner.clustering.ClusterModelItemIndex(this.hcm);
		predIndex = new com.rapidminer.operator.learner.clustering.ClusterModelPredecessorIndex(this.hcm);
	}

	protected HierarchicalClusterModel getClusterModel() {
		return hcm;
	}

	public boolean isSimilarityDefined(String x, String y) {
		return (itemIndex.getCluster(x) != null) && (itemIndex.getCluster(y) != null);
	}
}
