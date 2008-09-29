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

import java.util.Iterator;

/**
 * Adapter to build a flat copy from a hierarchical cluster model. Only the nodes on top level are considered.
 * 
 * @author Michael Wurst
 * @version $Id: FlattendClusterModel.java,v 1.5 2008/09/12 10:30:42 tobiasmalbrecht Exp $
 */
public class FlattendClusterModel extends FlatCrispClusterModel {

	private static final long serialVersionUID = 8473015413253377809L;

	public FlattendClusterModel(HierarchicalClusterModel hcm) {
		super(hcm);
		Iterator<ClusterNode> topLevelNodes = hcm.getRootNode().getSubNodes();
		while (topLevelNodes.hasNext()) {
			ClusterNode cn = topLevelNodes.next();
			DefaultCluster flatCluster = new DefaultCluster(cn.getId());
			flatCluster.setDescription(cn.getDescription());
			Iterator<String> objIds = cn.getObjectsInSubtree();
			while (objIds.hasNext())
				flatCluster.addObject(objIds.next());
			addCluster(flatCluster);
		}
	}
}
