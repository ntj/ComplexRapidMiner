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
package com.rapidminer.operator.learner.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adds an buttom up index to hierarchical cluster models.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ClusterModelPredecessorIndex.java,v 1.6 2008/09/12 10:30:29 tobiasmalbrecht Exp $
 */
public class ClusterModelPredecessorIndex implements Serializable{

	private static final long serialVersionUID = 6899534322386857535L;
	
	private final Map<ClusterNode, ClusterNode> predIndex;

	public ClusterModelPredecessorIndex(HierarchicalClusterModel hcm) {
		super();
		predIndex = new HashMap<ClusterNode, ClusterNode>();
		addIndexRec(hcm.getRootNode(), predIndex);
	}

	private void addIndexRec(ClusterNode node, Map<ClusterNode, ClusterNode> index) {
		Iterator<ClusterNode> it = node.getSubNodes();
		while (it.hasNext()) {
			ClusterNode cn = it.next();
			index.put(cn, node);
			addIndexRec(cn, index);
		}
	}

	public List<ClusterNode> getPathToRoot(ClusterNode cn) {
		List<ClusterNode> result = new ArrayList<ClusterNode>();
		ClusterNode node = cn;
		while (node != null) {
			result.add(node);
			node = getPredecessor(node);
		}
		return result;
	}

	public ClusterNode getPredecessor(ClusterNode cn) {
		return predIndex.get(cn);
	}

	public ClusterNode getMostSpecificCommonNode(ClusterNode cn1, ClusterNode cn2) {
		List<ClusterNode> rpath1 = getPathToRoot(cn1);
		List<ClusterNode> rpath2 = getPathToRoot(cn2);
		Set path1set = new HashSet<ClusterNode>(rpath1);
		Iterator<ClusterNode> it = rpath2.iterator();
		ClusterNode node = it.next();
		while ((it.hasNext()) && (!path1set.contains(node)))
			node = it.next();
		return node;
	}
}
